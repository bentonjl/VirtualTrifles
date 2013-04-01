package dbnode;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/***********************************************************************
 * NOTES:
 * -- Connections don't construct the query, they will call the QueryNode's "toString" method to get a query string. 
 * -- It is expected that the QueryNode's toString will return a properly formatted SQL query.
 * 
 * @author Bobby Frankenberger
 *
 */

public class Connection {
/***********************************************************************
 * INSTANCE VARIABLES
 */
	private java.sql.Connection con = null;
	private Session session = null;
	
	private String driver;
	private boolean ssh = false;
	private String hostname;
	private String user;
	private String pass;
	private String dbName;
	
	/** Non-SSH Only Properties */
	private int port;
	
	/** SSH Only Properties */
	private String sshUser;
	private String sshPass;
	private String sshHostname;
	private int sshPort;
	private int localPort;
	private int remotePort;
	
/***********************************************************************
 * CONSTRUCTORS
 */
	public Connection() {
		Hashtable<String,String> props = ConnectionProperties.getProperties();
		
		this.driver = props.get("driver");
		this.ssh = props.get("type").equalsIgnoreCase("SSHTunnel");
		this.dbName = props.get("dbname");
		if(this.ssh) {
			this.hostname = props.get("remotehost");
			this.user = props.get("dbuser");
			this.pass = props.get("dbpass");
			this.sshUser = props.get("sshuser");
			this.sshPass = props.get("sshpass");
			this.sshHostname = props.get("sshhost");
			this.sshPort = Integer.parseInt(props.get("sshport"));
			this.localPort = Integer.parseInt(props.get("localport"));
			this.remotePort = Integer.parseInt(props.get("remoteport"));
		} else {
			this.hostname = props.get("host");
			this.user = props.get("dbuser");
			this.pass = props.get("dbpass");
		}
		
		this.con = ssh ? getSSHConnection() : getStandardConnection();
	}

/***********************************************************************
 * GENERAL METHODS
 */
	
/***********************************************************************
 * Retrieves a connection to a SQL database using the default properties
 * 
 *  @return a <code>java.sql.Connection</code> to a SQL database.
 */
	private java.sql.Connection getConnection() {
		return this.con;
	}
	
/***********************************************************************
 * Retrieves a connection to a SQL database through an SSH tunnel.
 * 
 * This method utilizes the <code>jcraft.jsch</code> library to make an 
 * SSH tunnel.
 * 
 * @return a <code>java.sql.Connection</code> via an SSH Tunnel.
 */
	private java.sql.Connection getSSHConnection() {
		java.sql.Connection con = null;
		
		try {
			doSSHTunnel(this.sshUser, this.sshPass, this.sshHostname, this.sshPort, 
						this.hostname, this.localPort, this.remotePort);
			Class.forName(this.driver);
			con = DriverManager.getConnection("jdbc:mysql://localhost:" + this.localPort, 
															this.user, this.pass);
		} catch (JSchException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return con;
	}
	
/***********************************************************************
 * Retrieves a standard connection to a SQL database (i.e. without using
 * an SSH tunnel).
 * 
 * @return a <code>java.sql.Connection</code> via an SSH Tunnel.
 */
	private java.sql.Connection getStandardConnection() {
		java.sql.Connection con = null;
		
		try {
			Class.forName(this.driver);
//			con = DriverManager.getConnection("jdbc:mysql://localhost:" + this.localPort, 
//															this.user, this.pass);
			con = DriverManager.getConnection(this.hostname, this.user, this.pass);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return con;
		
	}
	
	public void close() {
		if(this.ssh) {
			try {
				closeSSHTunnel(this.hostname, this.localPort, this.remotePort);
				this.con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
/***********************************************************************
 * Executes the query found in the <code>QueryNode</code> passed as an 
 * argument. The <code>ResultSet</code> returned is packaged into a 
 * <code>ResultNode</code> object and returned.<br><br>
 * 
 * It may be important to know that the <code>Connection.close()</code>
 * method is called if a <code>SQLException</code> is encountered during
 * the execution of this method.
 * 
 * @param query - A <code>QueryNode</code> containing the query data.
 * @return = A <code>ResultNode</code> containing the result of the query.
 */
	public ResultNode executeQuery(QueryNode query) {
		ResultNode result = new ResultNode("table");
		String statement = query.toString();
		
		try {
			Statement stmt = this.con.createStatement();
			ResultSet rs = stmt.executeQuery(statement);
			ResultSetMetaData md = rs.getMetaData();
			
			while(rs.next()) {
				// Build the ResultNode one row at a time.
				ResultNode row = new ResultNode(rs.getRow() - 1, null);
				for(int i = 1; i <= md.getColumnCount(); i++) {
					row.addNode(md.getColumnName(i), rs.getString(i));
				}
				result.addNode(row);
			}
			
		} catch (SQLException e) {
			// Close the connection in the event of an exception. 
			try {
				if(!this.con.isClosed()) {
					this.close();
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			e.printStackTrace();
		} 
		
		return result;
	}
	
	public boolean executeInsert(InsertNode insert) {
		String statement = insert.toString();
		boolean result = false;
		
		
		
		
		
		try {
			Statement stmt = this.con.createStatement();
			result = stmt.execute(statement);
			
			
		} catch (SQLException e) {
			// Close the connection in the event of an exception. 
			try {
				if(!this.con.isClosed()) {
					this.close();
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			e.printStackTrace();
		}
		
		return result;
	}
	
	public int executeUpdate(UpdateNode update) {
		String statement = update.toString();
		int result = -1;
		
		try {
			Statement stmt = this.con.createStatement();
			result = stmt.executeUpdate(statement);
		} catch (SQLException e) {
			// Close the connection in the event of an exception. 
			try {
				if(!this.con.isClosed()) {
					this.close();
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			e.printStackTrace();
		}
		
		return result;
	}
	
	public int executeUpdate(DeleteNode delete) {
		String statement = delete.toString();
		int result = -1;
		
		try {
			Statement stmt = this.con.createStatement();
			result = stmt.executeUpdate(statement);
		} catch (SQLException e) {
			// Close the connection in the event of an exception. 
			try {
				if(!this.con.isClosed()) {
					this.close();
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			e.printStackTrace();
		}
		
		return result;
	}
	
/***********************************************************************
 * Makes an SSH Tunnel using the <code>jcraf.jsch</code> library.
 */
	private void doSSHTunnel( String sshUser, String sshPass, 
									 String sshHostname, int sshPort, String remoteHost, 
									 int localPort, int remotePort ) throws JSchException {
	    final JSch jsch = new JSch();
	    
	    this.session = jsch.getSession( sshUser, sshHostname, 22 );
	    this.session.setPassword( sshPass );
	    
	    final Properties config = new Properties();
	    config.put( "StrictHostKeyChecking", "no" );
	    this.session.setConfig( config );
	    
	    this.session.connect();	    
	    this.session.setPortForwardingL(localPort, remoteHost, remotePort);
	}
	
	private void closeSSHTunnel(String remoteHost, int localPort, int remotePort) {
		try {
			this.session.delPortForwardingL(localPort);
			this.session.disconnect();
		} catch (JSchException e) {
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		QueryNode node = new QueryNode();
		
		node.addNode("SELECT", "*");
		node.addNode("FROM", "User");
		node.addNode("WHERE", "FirstName='John'");
		
//		String query = "SELECT * FROM test.myTable WHERE firstName='Bobby';";
		Connection mycon = new Connection();
//		java.sql.Connection con = mycon.getConnection();
		
//		ResultNode result = mycon.executeQuery(node);
		
		System.out.println(mycon.dbName);
		
		mycon.close();
		
//		System.out.println(result.toString());
		
		System.exit(1);

	}

}
