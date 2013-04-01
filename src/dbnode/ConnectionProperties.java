package dbnode;

import java.io.IOException;
import java.util.Hashtable;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/***********************************************************************
 * This class retrieves and stores the properties for a database 
 * connection. 
 * 
 * The properties that this class reads are stored in an 
 * external XML file. Typically this file is named DBNodeProperties.xml.
 * The properties are organized by specifying a database, a connection, 
 * and then a list of properties for making that connection. 
 * 
 * An example: <br><br>
 * <code>
 * &ltdatabase&gt<br>
 * &nbsp&nbsp&ltconnection&gt<br>
 * &nbsp&nbsp&nbsp&nbsp&lttype&gtSSHTunnel&lt/type&gt<br>
 * &nbsp&nbsp&nbsp&nbsp&ltdriver&gtcom.mysql.jdbc.Driver&lt/driver&gt<br>
 * &nbsp&nbsp&nbsp&nbsp&ltsshuser&gtUsername&lt/sshuser&gt<br>
 * &nbsp&nbsp&nbsp&nbspetc...<br>
 * &nbsp&nbsp&lt/connection&gt<br>
 * &lt/database&gt<br>
 * 
 * @author Bobby Frankenberger
 *
 */

public class ConnectionProperties extends DefaultHandler {
/***********************************************************************
 * INSTANCE VARIABLES
 */
//	private String propsFile = "C:\\Users\\Bobby\\Dropbox\\Web Projects\\apache-tomcat-7.0.28\\webapps\\Capstone\\WEB-INF\\conf\\DBNodeProperties.xml"; // home PC
//	private String propsFile = "/var/lib/tomcat7/webapps/bobby/WEB-INF/conf/DBNodeProperties.xml"; // bobby
//	private String propsFile = "/var/lib/tomcat7/webapps/andy/WEB-INF/conf/DBNodeProperties.xml";  // andy
	private String propsFile = "/var/lib/tomcat7/webapps/john/WEB-INF/conf/DBNodeProperties.xml";  // john
//	private String propsFile = "/var/lib/tomcat7/webapps/website/WEB-INF/conf/DBNodeProperties.xml";  // website
	private Hashtable<String,String> props;
	private String tempKey;
	private String tempVal;
	
/***********************************************************************
 * CONSTRUCTORS
 */
/***********************************************************************
 * The default constructor. <br>
 * <br>
 * This constructor initializes the properties file with the default 
 * filename <code>\DBNode\conf\DBNodeProperties.xml</code>.
 */
	private ConnectionProperties() {
		this.props = new Hashtable<String,String>();
		parseXMLFile();
	}
	
/***********************************************************************
 * Constructor which takes a <code>filename</code> as a <code>String</code>
 * to set as the path to the XML properties file. 
 * 
 * @param propsFile <code>String</code> path to XML properties file.
 */
	private ConnectionProperties(String propsFile) {
		this.props = new Hashtable<String,String>();
		this.propsFile = propsFile;
		parseXMLFile();
	}
	
/***********************************************************************
 * GETTERS AND SETTERS
 */	
/***********************************************************************
 * Returns a <code>Hashtable</code> with the properties from the default
 *  <code>\DBNode\conf\DBNodeProperties.xml</code>.
 *  
 * @return The <code>Hashtable</code> with the properties.
 */
	public static Hashtable<String,String> getProperties() {
		ConnectionProperties cp = new ConnectionProperties();
		return cp.props;
	}
	
/***********************************************************************
 * Returns a <code>Hashtable</code> with the properties from the specified
 * properties XML file.
 *  
 * @return The <code>Hashtable</code> with the properties.
 */
	public static Hashtable<String,String> getProperties(String propsFile) {
		ConnectionProperties cp = new ConnectionProperties(propsFile);
		return cp.props;
	}

/***********************************************************************
 * GENERAL METHODS 
 */	
/***********************************************************************
 * Parse's the XML properties file located at <code>propsFile</code>.
 * The results of the parse are stored in the <code>Hashtable</code>
 * called <code>props</code>.
 */
	private void parseXMLFile() {
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
			SAXParser sp = spf.newSAXParser();
			sp.parse(propsFile, this);
		} catch(SAXException se) {
			se.printStackTrace();
		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch (IOException ie) {
			ie.printStackTrace();
		}
	}
	
	
/***********************************************************************
 * EVENT HANDLERS
 */
/***********************************************************************
 * This listener is called when the parser encounters an XML start tag.
 * The code in this function dicates what should happen when a given (or
 * arbitrary) start tag is encountered. 
 * 
 * @param uri
 * @param localName
 * @param qName - The name of the encountered tag.
 * @param attributes - Any specified attributes used to modify an XML
 * tag.
 */
	public void startElement(String uri, String localName, 
							 String qName, Attributes attributes) throws SAXException {
		this.tempVal = "";
		if(!qName.equalsIgnoreCase("connection") && !qName.equalsIgnoreCase("database")) {
			this.tempKey = qName;
		}
	}
	
/***********************************************************************
 * This listener is called when a string of characters inside an XML 
 * open and close tag are encountered. The method specifies what should
 * be done when encountering content inside tags. 
 * 
 * @param ch - The characters stored in a <code>char</code> array.
 * @param start
 * @param length - The length of the <code>char</code> array.
 */
	public void characters(char[] ch, int start, int length) throws SAXException {
		this.tempVal = new String(ch,start,length);
	}
	
	public void endElement(String uri, String localName, 
						   String qName) throws SAXException {
		if(!qName.equalsIgnoreCase("connection") && 
		   !qName.equalsIgnoreCase("database")) {
				props.put(tempKey, tempVal);
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		ConnectionProperties test = new ConnectionProperties();
		test.parseXMLFile();
		System.out.println(test.props.toString());
//		Iterator it = test.myList.iterator();
//		while(it.hasNext()) {
//			System.out.println("Here" + it.next().toString());
//		}

	}

}
