package dbnode;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/***********************************************************************
 * NOTES:
 * -- QueryNode objects should contain only one query. 
 * -- These can be nested queries and subqueries, but there should not be multiple queries (as in a multi-query script) in one QueryNode.
 * -- The ability to do this may come at some later date.
 * -- Perhaps this could be done with a simple method to execute raw SQL.
 * 
 * -- USEFUL DESCRIPTIONS
 * ---- terminal
 * ---- parent
 * 
 * The <code>QueryNode</code> is a class that takes care of the storage
 * and handling of SQL queries.<br><br> 
 * 
 * The idea of this class is to make it easy to create, store, and move
 * SQL queries for general purpose use. A <code>QueryNode</code> represents
 * a single SQL clause (clause type and predicate) as well as a collection
 * of such clauses. <br><br>
 * 
 * An example of the basic structure of a <code>QueryNode</code> can be 
 * illustrated as:<br><br>
 * <code>&ltQueryNode&gt</code><br>
 * <code>&nbsp&nbsp&nbsp&nbsp&ltQueryNode&gtSELECT '*'&lt/QueryNode&gt</code><br>
 * <code>&nbsp&nbsp&nbsp&nbsp&ltQueryNode&gtFROM table1&lt/QueryNode&gt</code><br>
 * <code>&nbsp&nbsp&nbsp&nbsp&ltQueryNode&gtWHERE attr='value'&lt/QueryNode&gt</code><br>
 * <code>&lt/QueryNode&gt</code><br><br>
 * 
 * <code>QueryNode</code> objects can be constructed using objects of  
 * type <code>JSONObject</code> from the json.org Java library for handling
 * <code>JSON</code> objects. <code>QueryNode</code> has a constructor 
 * <code>QueryNode(JSONObject json)</code> to support this. <br><br>
 * 
 * When using a <code>JSONObject</code>, the underlying <code>JSON</code>
 * must be structured in a particular way. Below is a description of the 
 * proper way to construct query data in a <code>JSON</code> object so 
 * that a <code>QueryNode</code> can be successfully constructed. <br><br>
 * <code>
 * 	   {'type' : 'query', <br>
 * &nbsp'table' : &lttables&gt , <br>
 * &nbsp'select' : ['&ltattr1&gt', '&ltattr2&gt, etc...],<br>
 * &nbsp'where' : [ <br>
 * &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp{'attribute' : '&ltattr1&gt', 'value', '&ltval1&gt'},<br>
 * &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp{'attribute' : '&ltattr2&gt', 'value', '&ltval2&gt'},<br>
 * &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp...<br>
 * &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp],<br>
 * &nbsp'join' : [ <br>
 * &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp{'table' : 'tableName', 'lhs' : '&ltattr&gt', 'rhs' : '&ltattr&gt'},<br>
 * &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp{'table' : 'tableName', 'lhs' : '&ltattr&gt', 'rhs' : '&ltattr&gt'},<br>
 * &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp...<br>
 * &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp]<br>
 * }
 * </code><br><br>
 * NOTE:
 * <ul>
 * <li>Things in angle brackets (e.g. &ltattr1&gt) indicate variables.</li>
 * <li>&lttables&gt can be represented by either a single table (e.g. 'myTable') or an array of table names (e.g. ['myTable', 'myTable2', etc...]).</li>
 * <li>Anything NOT in angle brackets should be constructed how it is. </li>
 * <li>Keys 'table', 'select', and 'from' are required. The other keys (and their value counterparts) are optional.</li>
 * <li>Keys can appear in any order in the <code>JSON</code></li>
 * </ul>
 * 
 * Here is an example <code>JSON</code> structure that will work.<br><br>
 * <code>
 * 	   {'type' : 'query', <br>
 * &nbsp'table' : 'User', <br>
 * &nbsp'select' : ['User.FirstName', 'User.LastName', 'Class.ClassName],<br>
 * &nbsp'join' : [ <br>
 * &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp{'table' : 'hasClass', 'lhs' : 'User.UserID', 'rhs' : 'hasClass.UserID'},<br>
 * &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp{'table' : 'Class', 'lhs' : 'hasClass.ClassID', 'rhs' : 'Class.ClassID'},<br>
 * &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp],<br>
 * &nbsp'where' : [ <br>
 * &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp{'attribute' : 'User.UserID', 'value', '1'},<br>
 * &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp{'attribute' : 'User.UserType', 'value', 'student'},<br>
 * &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp]<br>
 * }
 * </code><br>
 * Which corresponds to the SQL statement:<br><br>
 * <code>
 * SELECT User.FirstName, User.LastName, Class.ClassName<br>
 * FROM User<br>
 * JOIN hasClass ON User.UserID=hasClass.UserID<br>
 * JOIN Class ON hasClass.ClassID=Class.ClassID<br>
 * WHERE User.UserID=1<br>
 * AND User.UserType='student';<br><br>
 * 
 * @author Bobby Frankenberger
 *
 */

public class QueryNode implements SQLNode<QueryNode> {
/***********************************************************************
 * INSTANCE VARIABLES
 */
	private String desc;
	private String type;
	private String expression;
	private Vector<QueryNode> nodes = null;

/***********************************************************************
 * CONSTRUCTORS
 */
	public QueryNode() {
		this.desc = "parent";
	}	
	public QueryNode(String desc) {
		this.desc = desc;
	}
	public QueryNode(String desc, Vector<QueryNode> nodes) {
		if(desc == null) {
			this.desc = "parent";
		} else {
			this.desc = desc;
		}
		this.nodes = nodes;
	}
	public QueryNode(String type, String exp) {
		this.desc = "terminal";
		this.setType(type);
		this.setExpression(exp);
	}
	public QueryNode(JSONObject json) {
		try {
			if(json.getString("type").equalsIgnoreCase("query")) {
				JSONArray selectColumns = json.getJSONArray("select");
				JSONArray joins = json.has("join") ? json.getJSONArray("join") : null;
				JSONArray whereTuples = json.has("where") ? json.getJSONArray("where") : null;
				String selectExpression = "";
				for(int i = 0; i < selectColumns.length(); i++) {
					selectExpression += selectColumns.get(i).toString();
					selectExpression += (i != selectColumns.length()-1) ? ", " : "";
				}
				
				this.addNode("SELECT", selectExpression);
				if(json.get("table") instanceof JSONArray) {
					JSONArray tables = json.getJSONArray("table");
					String tableList = "";
					for(int i = 0; i < tables.length(); i++) {
						tableList += tables.getString(i);
						tableList += (i != tables.length()-1) ? ", " : "";
					}
					this.addNode("FROM", tableList);
				} else {
					this.addNode("FROM", json.getString("table"));
				}
				if(joins != null) {
					for(int i = 0; i < joins.length(); i++) {
						this.addNode("JOIN", String.format("%s ON %s=%s", 
											joins.getJSONObject(i).getString("table"),
											joins.getJSONObject(i).getString("lhs"),
											joins.getJSONObject(i).getString("rhs")));
					}
				}
				if(whereTuples != null) {
					for(int i = 0; i < whereTuples.length(); i++) {
						this.addNode("WHERE", String.format("%s='%s'", 
											whereTuples.getJSONObject(i).getString("attribute"),
											whereTuples.getJSONObject(i).getString("value")));
					}
				}
			} else {
				// wrong node type exception
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

/***********************************************************************
 * GETTERS AND SETTERS
 */
	private void setType(String type) {
		this.type = type;
	}
	public String getType() {
		return this.type;
	}
	private void setExpression(String exp) {
		this.expression = exp;
	}
	public String getExpression() {
		return this.expression;
	}
/***********************************************************************
 * GENERAL METHODS 
 */
/***********************************************************************
 * Removes and returns the node at the top of the list of nodes. 
 * 
 * @return <code>QueryNode</code> at index 0.
 */
	private QueryNode popNode() {
		QueryNode returnNode = null;
		if(nodes != null) {
			returnNode = nodes.firstElement();
			nodes.remove(0);			
		}
		return returnNode;
	}
	
/***********************************************************************
 * Adds a node to the <code>QueryNode</code>. Each child node added to 
 * the <code>QueryNode</code> is an individual SQL clause to be executed
 * in a query. 
 * 
 * @param type - The type of node to add (e.g. SELECT, FROM, WHERE, etc...)
 * @param exp - The expression (or predicate) of the query clause.
 */
	public void addNode(String type, String exp) {
		String on = "";
		if(nodes == null) {
			nodes = new Vector<QueryNode>();
		}
		if(type.equalsIgnoreCase("join")) {
			on = exp.substring(exp.indexOf("ON") + 3);
			on = addDBNameToJoinOnExp(on);
			exp = exp.substring(0, exp.indexOf("ON")) + "ON " + on;
		}
		nodes.add(new QueryNode(type, exp));
	}
	
/***********************************************************************
 * Adds a node to the <code>QueryNode</code>. Each child node added to 
 * the <code>QueryNode</code> is an individual SQL clause to be excecuted
 * in a query. 
 * 
 * @param node - a <code>QueryNode</code> to be added as a child node. 
 */
	public void addNode(QueryNode node) {
		if(nodes == null) {
			nodes = new Vector<QueryNode>();
		}
		nodes.add(node);
	}
	
/***********************************************************************
 * Deletes the <code>QueryNode</code> at the specified index. The remaining
 * nodes in index positions greater than the one specified are shifted 
 * downward by one to maintain a list with no gaps. 
 * 
 * This method is essentially a wrapper around 
 * <code>java.util.Vector.removeElementAt(int index)</code>.
 * 
 * @param index - The index of the node to remove.
 * @return <code>true</code> if the node was successfully removed. 
 * <code>false</code> if the node contains no children nodes.
 */
	public boolean deleteNode(int index) {
		if(nodes != null) {
			nodes.removeElementAt(index);
			return true;
		}
		return false;
	}
	
/***********************************************************************
 * Returns the <code>QueryNode</code> at the specified position in this
 * node if it exists. 
 * 
 * This method is essentially a wrapper around 
 * <code>java.util.Vector.get(int index)</code>.
 * 
 * @param index - index of the node to return.
 * @return the <code>QueryNode</code> located at the <code>index</code>.
 */
	public QueryNode getNode(int index) {
		return (nodes != null) ? nodes.get(index) : null;
	}
	
/***********************************************************************
 * Returns a <code>Vector</code> of <code>QueryNode</code>s if this is a
 * parent node. <code>null</code> if this is a terminal node.
 * 
 * @return A <code>Vector</code> or <code>null</code>
 */
	public Vector<QueryNode> getNodes() {
		return this.nodes;
	}
	
/***********************************************************************
 * Tests whether a <code>QueryNode</code> contains any child nodes or not.
 * 
 * This is essentially a wrapper implementation of
 * <code>java.util.Vector.isEmpty()</code>.
 * 
 * @return <code>true</code> if the node contains child nodes. 
 */
	public boolean hasNodes() {
		return (nodes != null) ? !nodes.isEmpty() : false;
	}
	
/***********************************************************************
 * Returns an iterator over the list of <code>QueryNode</code>s in the 
 * order that they occur in the <code>Vector&ltQueryNode&gt</code>.
 * 
 * This method is essentially a wrapper implementation of 
 * <code>java.util.Vector.iterator()</code>.
 * 
 * @return an <code>Iterator</code>.
 */
	public Iterator<QueryNode> iterator() {
		return this.nodes.iterator();
	}
	
/***********************************************************************
 * Returns the number of child nodes this <code>QueryNode</code> contains. 
 * 
 * This method is essentially a wrapper implementation of 
 * <code>java.util.Vector.size()</code>
 * 
 * @return The number of child nodes as <code>int</code>.
 */
	public int size() {
		return (nodes != null) ? nodes.size() : 0;
	}
	
/************************************************************************
 * Private helper function that takes the part of a <code>JOIN</code> 
 * expression that comes after the <code>ON</code> and injects the name 
 * of the database to query. This name comes from DBNodeProperties.xml
 * file.<br><br>
 * 
 * The part of the expression that should be passed as a parameter to this
 * helper function is the part that comes after the <code>ON</code> in a 
 * SQL <code>JOIN</code> statement. As an example:<br><br>
 * <code>
 * SELECT *<br>
 * FROM User<br>
 * JOIN Password ON User.UserID=Password.UserID<br><br>
 * <code>
 * In the above code, the part after the <code>ON</code> (that is 
 * <code>User.UserID=Password.UserID</code> would be passed to the function
 * and the function would return <code>myTable.User.UserID=myTable.Password.UserID<code>
 * if <code>myTable</code> were the table specified in the DBNodeProperties.xml
 * file. 
 * 
 * @param exp - The part of the SQL <code>JOIN</code> expression that comes
 * after the <code>ON</code> keyword. 
 * @return The same part of the SQL <code>JOIN</code> expression passed as an 
 * argument, except with the table name prepended to the left and right hand
 * sides of the expression. 
 */
	private String addDBNameToJoinOnExp(String exp) {
		Hashtable<String,String> props = ConnectionProperties.getProperties();
		String db = props.get("dbname");
		String[] parts = exp.split("=");
		return String.format("%s.%s=%s.%s", db, parts[0], db, parts[1]);
	}
	
///***********************************************************************
// * Analyzes a <code>QueryNode</code> and determines what type of SQL
// * statement is contained within (e.g. <code>SELECT</code>, <code>UPDATE</code>,
// * <code>INSERT</code>, etc...). The type of statement is returned as
// * a <code>StatementType</code> object of type <code>enum</code>.
// * 
// * @param node - The node of which to determine the statement type.
// * @return a <code>StatementType</code> indicating the SQL statement type.
// */
//	private static StatementType determineStatementType(QueryNode node) {
//		if(node.size() == 0) {
//			if(node.getType().equalsIgnoreCase("SELECT")) {
//				return StatementType.SELECT;
//			} else if(node.getType().equalsIgnoreCase("INSERT")) {
//				return StatementType.INSERT;
//			} else if(node.getType().equalsIgnoreCase("UPDATE")) {
//				return StatementType.UPDATE;
//			} else if(node.getType().equalsIgnoreCase("DELETE")) {
//				return StatementType.DELETE;
//			} else {
//				return null;
//			}
//		} else {
//			QueryNode newNode = node.getNode(0);
//			return determineStatementType(newNode);
//		}
//	}
	
/***********************************************************************
 * Takes a <code>QueryNode</code> full of only <code>select</code> type, 
 * and constructs a SQL <code>SELECT</code> clause as a <code>String</code>. 
 * 
 * @param selects - a <code>QueryNode</code> of nodes of only <code>select</code>
 * type.
 * @return a <code>String</code> formatted as a SQL <code>SELECT</code> 
 * clause.
 */
	private static String constructSelectClause(QueryNode selects) {
		String clause = "SELECT " + selects.popNode().getExpression();
		for(QueryNode node : selects) {
			clause += ", " + node.getExpression();
		}
		return clause;
	}
	
/***********************************************************************
 * Takes a <code>QueryNode</code> full of only <code>from</code> type, 
 * and constructs a SQL <code>FROM</code> clause as a <code>String</code>. 
 * 
 * @param from - a <code>QueryNode</code> of nodes of only <code>from</code>
 * type.
 * @return a <code>String</code> formatted as a SQL <code>FROM</code> 
 * clause.
 */
	private static String constructFromClause(QueryNode from) {
		Hashtable<String,String> props = ConnectionProperties.getProperties();
		from = from.popNode();
		String clause = "FROM " + props.get("dbname") + "." + from.getExpression();
		return clause;
	}
	
/***********************************************************************
 * Takes a <code>QueryNode</code> full of only <code>join</code> type,
 * and constructs a SQL <code>JOIN</code> clause as a <code>String</code>.
 * 
 * @param joins - a <code>QueryNode</code> of nodes of only <code>join</code> 
 * type.
 * @return a <code>String</code> formatted as a SQL <code>JOIN</code>
 * clause.
 */
	private static String constructJoinClause(QueryNode joins) {
		Hashtable<String,String> props = ConnectionProperties.getProperties();
		String clause = "";
		if(joins.hasNodes()) {
			for(QueryNode node : joins) {
				clause += "JOIN " + props.get("dbname") + "." + node.getExpression() + "\n";
			}
		}
		return clause;
	}
	
/***********************************************************************
 * Takes a <code>QueryNode</code> full of only <code>where</code> type,
 * and constructs a SQL <code>WHERE</code> clause as a <code>String</code>.
 * The resulting clause includes subsequent <code>WHERE</code> statements
 * as <code>AND</code> additions. As in:<br>
 * <br>
 * <code>
 * WHERE attr1='val1'<br>
 * AND attr2='val2'<br>
 * AND attr3='val3'<br>
 * </code>
 * etc...
 * @param wheres - a <code>QueryNode</code> of nodes of only <code>where</code> 
 * type.
 * @return a <code>String</code> formatted as a SQL <code>WHERE</code>
 * clause.
 */
	private static String constructWhereClause(QueryNode wheres) {
		String clause = "";
		if(wheres.hasNodes()) {
			clause = "WHERE " + wheres.popNode().getExpression() + "\n";
			for(QueryNode node : wheres) {
				clause += "AND " + node.getExpression() + "\n";
			}
		}
		return clause;
	}
	
/***********************************************************************
 * Implementation of a standard <code>toString()</code> method. This 
 * method returns a <code>String</code> representation of the node as 
 * a human-readable SQL query.
 * 
 * This method is responsible for constructing the SQL Query represented
 * by the <code>QueryNode</code> in a well-formed way so that it can be
 * presented to a <code>Connection</code> and executed properly.
 * 
 * @return The <code>String</code> representation of the SQL query.
 */
	public String toString() {
		String s = "";
		QueryNode selects = new QueryNode();
		QueryNode from = new QueryNode();
		QueryNode joins = new QueryNode();
		QueryNode wheres = new QueryNode();

		for(QueryNode node : this) {
			if(node.getType().equalsIgnoreCase("select")) {
				selects.addNode(node);
			} else if(node.getType().equalsIgnoreCase("from")) {
				from.addNode(node);
			} else if(node.getType().equalsIgnoreCase("where")) {
				wheres.addNode(node);
			} else if(node.getType().equalsIgnoreCase("join")) {
				joins.addNode(node);
			} else {
				// We have a problem
			}
		}
		
		if(joins.hasNodes()) {
			/** It is a JOIN */
			s = String.format("%s\n%s\n%s\n%s;", constructSelectClause(selects), 
												constructFromClause(from),
												constructJoinClause(joins),
												constructWhereClause(wheres));
		} else {
			s = String.format("%s\n%s\n%s;", constructSelectClause(selects), 
												constructFromClause(from), 
												constructWhereClause(wheres));
		}
		
		return s;
	}
	
/***********************************************************************
 * Executes the SQL query contained within the <code>QueryNode</code>
 * that calls it.
 * 
 *  @return a <code>ResultNode</code> with the query results. 
 */
	public ResultNode execute() {
		ResultNode result = new ResultNode();
		// Open a connection to the database
		Connection con = new Connection();
		// Do some magic to send the query
		result = con.executeQuery(this);
		// Collect the result and package in the ResultNode
		
		con.close();
		return result;
	}

	public static void main(String[] args) {
		
//		try {
//
//			JSONObject json = new JSONObject("{ 'type' : 'query', " +
//											   "'select' : ['*'], " +
//											   "'table' : ['User'], " +
//											   "'join' : [" +
//											   		"{'table' : 'hasNode', 'lhs' : 'User.UserID', 'rhs' : 'hasNode.UserID'}" +
//										   			 	"]," +
//											   "'where' : [" +
//											   		"{'attribute' : 'User.UserID', 'value' : '1'}," +
//											   		"{'attribute' : 'User.UserType', 'value' : 'student'}" +
//											   			 "]" +
//											 "}");
//			
////			JSONObject json = new JSONObject("{'type':'query','table':'test_bobby.User','select':['*'],'where':[{'attribute':'UserID','value':'1'}]}");
//			
//			
//			
//			System.out.println(json.toString(3));
//			
//			QueryNode query = new QueryNode(json);
//			
//			System.out.println(query.toString());
//			
////			ResultNode result = query.execute();
////			
////			System.out.println(result.toString());
////			
////			System.out.println(result.toJSON());
//
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		QueryNode query = new QueryNode();
		query.addNode("SELECT", "*");
		query.addNode("FROM", "User");
		query.addNode("JOIN", "Password ON User.UserID=Password.UserID");
		query.addNode("WHERE", "User.EmailAddress='" + "rjfrankenberger@gmail.com" + "'");
		query.addNode("WHERE", "Password.Password='" + "pa$$word" + "'");
		
		System.out.println(query.toString());
		
		String exp = "test_bobby.hasNode ON User.UserID=hasNode.UserID";
		exp = exp.substring(exp.indexOf("ON") + 3);
		String[] s = exp.split("=");
		System.out.println(exp);
		for (String string : s) {
			System.out.println(string);
		}
		System.out.println(query.addDBNameToJoinOnExp(exp));
		
		System.exit(1);
	}

}
