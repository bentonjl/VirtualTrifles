package dbnode;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.xml.sax.helpers.DefaultHandler;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class DBNodeTest extends DefaultHandler {
	
//	List myList = new ArrayList();
//	
//	private static void doSshTunnel( String strSshUser, String strSshPassword, String strSshHost, int nSshPort, String strRemoteHost, int nLocalPort, int nRemotePort ) throws JSchException
//	  {
//	    final JSch jsch = new JSch();
//	    
////	    jsch.addIdentity("C:\\Users\\Bobby\\Dropbox\\Capstone\\Amazon Hosting\\awscapstonekey.pem");
//	    
//	    Session session = jsch.getSession( strSshUser, strSshHost, 22 );
//	    session.setPassword( strSshPassword );
//	    
//	    final Properties config = new Properties();
//	    config.put( "StrictHostKeyChecking", "no" );
//	    session.setConfig( config );
//	    
//	    session.connect();
//	    session.setPortForwardingL(nLocalPort, strRemoteHost, nRemotePort);
//	  }

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try
	    {
//	      String strSshUser = "easynode";											// SSH loging username
//	      String strSshPassword = "490mysqlcapstone";											// SSH login password
//	      String strSshHost = "ec2-107-20-93-77.compute-1.amazonaws.com";		// hostname or ip or SSH server
//	      int nSshPort = 22;                                    				// remote SSH host port number
//	      String strRemoteHost = "127.0.0.1";  									// hostname or ip of your database server
//	      int nLocalPort = 3366;                                				// local port number use to bind SSH tunnel
//	      int nRemotePort = 3306;                               				// remote port number of your database 
//	      String strDbUser = "root";                    						// database loging username
//	      String strDbPassword = "490sqlcapstone";                    			// database login password
//	      String dbtime;
//	      String query = "SELECT * FROM test.myTable WHERE firstName='Bobby';";
//	      
//	      DBNodeTest.doSshTunnel(strSshUser, strSshPassword, strSshHost, nSshPort, strRemoteHost, nLocalPort, nRemotePort);
//	      
	      Class.forName("com.mysql.jdbc.Driver");
//	      java.sql.Connection con = DriverManager.getConnection("jdbc:mysql://localhost:"+nLocalPort, strDbUser, strDbPassword);
	      java.sql.Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3366", "root", "490sqlcapstone");
//	      
//	      	Statement stmt = con.createStatement();
//			ResultSet rs = stmt.executeQuery(query);
//
//			while (rs.next()) {
//				dbtime = rs.getString(1);
//				System.out.println(dbtime);
//			} //end while
//	      
	      con.close();
	    }
	    catch( Exception e )
	    {
	      e.printStackTrace();
	    }
	    finally
	    {
	      System.exit(0);
	    }
		
//		ConnectionProperties props = new ConnectionProperties();
//		Hashtable table = props.getProperties();
//		
//		System.out.println(table.toString());
	      
	      
	      
//		String query = "SELECT * FROM test.myTable WHERE firstName='Bobby';";
//		java.sql.Connection con = Connection.getConnection();
//		
//		try {
//		  	Statement stmt = con.createStatement();
//			ResultSet rs = stmt.executeQuery(query);
//		
//			while (rs.next()) {
//				System.out.println(rs.getString(2));
//			} //end while
//		  
//			con.close();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		
//		System.exit(1);

	}

}
