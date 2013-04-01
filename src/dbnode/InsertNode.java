package dbnode;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/***********************************************************************
 * NOES:
 * -- DO NOT LEAVE INSERT INTO COLUMNS AMBIGUOUS!! Performance can't be
 *    guaranteed at this time.
 * 
 * @author Bobby
 *
 */

public class InsertNode implements SQLNode<InsertNode>{
/***********************************************************************
 * INSTANCE VARIABLES
 */
	private String desc;
	private String table;
	private String type;
	private String expression;
	private String[] cols;							// only used if columns are specified
	private String[] values;
	private Hashtable<String, String> pairs;		// stores key-value pairs for insert
	private Vector<InsertNode> nodes = null;

/***********************************************************************
 * CONSTRUCTORS
 */
	public InsertNode() {
		this.desc = "parent";
	}	
	public InsertNode(String desc) {
		this.desc = desc;
	}
	public InsertNode(String desc, Vector<InsertNode> nodes) {
		if(desc == null) {
			this.desc = "parent";
		} else {
			this.desc = desc;
		}
		this.nodes = nodes;
	}
	public InsertNode(String type, String exp) {
		this.desc = "terminal";
		this.setType(type);
		this.setExpression(exp);
		if(type.equalsIgnoreCase("INSERT")) {
			this.table = extractTableFromExp(exp);
			this.cols = buildColArrayFromExp(exp);
		}
		if(type.equalsIgnoreCase("VALUES")) {
			this.values = buildValuesArrayFromExp(exp);
		}
	}
	public InsertNode(String type, String[] values) {
		this.desc = "terminal";
		this.setType(type);
		if(type.equalsIgnoreCase("VALUES")) {
			this.setExpression(buildExpFromValuesArray(values));
			this.values = values;
		} 
	}
	public InsertNode(JSONObject json) {
		try {
			if(json.getString("type").equalsIgnoreCase("insert")) {
				JSONArray insertTuples = json.getJSONArray("insert");
				
//				String insertExpression = "INTO " + json.getString("table") + " (";
				String insertExpression = json.getString("table") + " (";
				String valuesExpression = "(";
				for(int i = 0; i < insertTuples.length(); i++) {
					insertExpression += insertTuples.getJSONObject(i).getString("attribute");
					insertExpression += (i != insertTuples.length()-1) ? ", " : "";
					valuesExpression += "'" + insertTuples.getJSONObject(i).getString("value") + "'";
					valuesExpression += (i != insertTuples.length()-1) ? ", " : "";
				}
				insertExpression += ")";
				valuesExpression += ")";
				
				this.addNode("INSERT", insertExpression);
				this.addNode("VALUES", valuesExpression);
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
	public String getTable() {
		return this.table;
	}
	private void setExpression(String exp) {
		this.expression = exp;
	}
	public String getExpression() {
		return this.expression;
	}
	public String[] getCols() {
		return this.cols;
	}
	public String[] getValues() {
		return this.values;
	}
	public Hashtable<String, String> getPairs() {
		return this.pairs;
	}
	
/***********************************************************************
 * GENERAL METHODS
 */
/***********************************************************************
 * Adds a node to the <code>InsertNode</code>. Each child node added to 
 * the <code>InsertNode</code> is an individual SQL clause to be executed
 * in an <code>INSERT</code> statement. 
 * 
 * @param node - a <code>InsertNode</code> to be added as a child node. 
 */
	public void addNode(InsertNode node) {
		if(nodes == null) {
			nodes = new Vector<InsertNode>();
		}
		nodes.add(node);
	}
	
/***********************************************************************
 * Adds a node to the <code>InsertNode</code>. Each child node added to 
 * the <code>InsertNode</code> is an individual SQL clause to be executed
 * in an <code>INSERT</code> statement. 
 * 
 * @param type - The type of node to add (e.g. INSERT, VALUES, etc...)
 * @param exp - The expression (or predicate) of the insert clause.
 */
	public void addNode(String type, String exp) {
		if(nodes == null) {
			nodes = new Vector<InsertNode>();
		}
		nodes.add(new InsertNode(type, exp));
		if(type.equalsIgnoreCase("INSERT")) {
			this.table = extractTableFromExp(exp);
		}
		if(type.equalsIgnoreCase("VALUES") && this.getNode(0).cols != null && this.getNode(1).values != null) {
			this.pairs = buildHashtableFromArrays(this.getNode(0).getCols(), this.getNode(1).getValues());
		}
	}
	
/***********************************************************************
 * Adds a node to the <code>InsertNode</code> intended to represent the 
 * <code>VALUES</code> clause of an <code>INSERT</code> statement taking
 * an array of <code>String</code>s as the <code>values</code> to be 
 * inserted. 
 * 
 * This method can only be used if the <code>type</code> parameter is
 * <code>"VALUES"</code>. If you want to use a key-value mapping to 
 * build the <code>INSERT</code> statement, you should use the method 
 * <code>addNode(String type, Hashtable<String, String> tuples)</code> instead.
 * 
 * @param type - The type of node to add. Should be <code>"VALUES"</code>.
 * @param values - A <code>String[]</code> of values to be inserted.
 */
	public void addNode(String type, String[] values) {
		if(type.equalsIgnoreCase("values")) {
			if(nodes == null) {
				nodes = new Vector<InsertNode>();
			}
			nodes.add(new InsertNode(type, buildExpFromValuesArray(values)));
		}
		if(type.equalsIgnoreCase("VALUES") && this.getNode(0).cols != null && this.getNode(1).values != null) {
			this.pairs = buildHashtableFromArrays(this.getNode(0).getCols(), this.getNode(1).getValues());
		}
	}
	
	public void addNode(String type, String table, Hashtable<String, String> tuples) {
		if(type.equalsIgnoreCase("insert")) {
			if(nodes == null) {
				nodes = new Vector<InsertNode>();
			}
			/* extract columns and values */
			Enumeration keys = tuples.keys();
			String[] cols = new String[tuples.size()];
			String[] vals = new String[tuples.size()];
			int i = 0;
			while(keys.hasMoreElements()) {
				String key = (String)keys.nextElement();
				cols[i] = key;
				vals[i] = tuples.get(key);
				i++;
			}
			/* Build the INSERT clause */
			nodes.add(new InsertNode(type, buildExpFromColArray(table, cols)));
			/* Build the VALUES clase */
			nodes.add(new InsertNode("VALUES", buildExpFromValuesArray(vals)));
			
			this.pairs = tuples;
			this.table = table;
		}
	}
	
/***********************************************************************
 * A helper function used to encapsulate the task of taking an array of 
 * columns and turning them into the expression portion (predicate) of an 
 * <code>INSERT INTO</code> clause. 
 * 
 * @param values - <code>String[]</code> of columns.
 * @return a <code>String</code> representing the expression.
 */	
	private String buildExpFromColArray(String table, String[] cols) {
		/* Build the INSERT expression */
		String exp = table + " (";
		for (String s : cols) {
			exp += s + ", ";
		}
		/* Remove last comma */
		exp = exp.substring(0, exp.length()-2) + ")";
		return exp;
	}
	
/***********************************************************************
 * A helper function used to encapsulate the task of taking an array of 
 * values and turning them into the expression portion (predicate) of a 
 * <code>VALUES</code> clause. 
 * 
 * @param values - <code>String[]</code> of values.
 * @return a <code>String</code> representing the expression.
 */
	private String buildExpFromValuesArray(String[] values) {
		/* Build the VALUES expression */
		String exp = "(";
		for (String s : values) {
			exp += "'" + s + "', ";
		}
		/* Remove last comma */
		exp = exp.substring(0, exp.length()-2) + ")";
		return exp;
	}
	
/***********************************************************************
 * A helper function used to encapsulate the task of taking a
 * <code>VALUES</code> expression in raw <code>String</code> form and 
 * building from it a <code>String[]</code> of those values.
 * 
 * @param exp - The raw <code>String</code> expression. 
 * @return a <code>String[]</code> of values.
 */
	private String[] buildColArrayFromExp(String exp) {
		/* parse exp into col array */
		exp = exp.substring(exp.indexOf('('));
		String[] split = exp.split("[(', )]");
		int count = 0;
		/* count number of non-empty values */
		for (String s : split) {
			if(!s.equalsIgnoreCase("")) {count++;}
		}
		/* Make sure there are columns listed */
		if(count == 0) {
			return null;
		}
		/* build col array */
		String[] cols = new String[count];
		int i = 0;
		for (String s : split) {
			if(!s.equalsIgnoreCase("")) {
				cols[i] = s;
				i++;
			}
		}
		return cols;
	}
	
/***********************************************************************
 * A helper function used to encapsulate the task of taking a
 * <code>VALUES</code> expression in raw <code>String</code> form and 
 * building from it a <code>String[]</code> of those values.
 * 
 * @param exp - The raw <code>String</code> expression. 
 * @return a <code>String[]</code> of values.
 */
	private String[] buildValuesArrayFromExp(String exp) {
		/* parse exp into values array */
		String[] split = exp.split("(\\(')|(', ')|('\\))");
		int count = 0;
		/* count number of non-empty values */
		for (String s : split) {
			if(!s.equalsIgnoreCase("")) {count++;}
		}
		/* Check that something is in there */
		if(count == 0) {
			return null;
		}
		/* build vals array */
		String[] vals = new String[count];
		int i = 0;
		for (String s : split) {
			if(!s.equalsIgnoreCase("")) {
				vals[i] = s;
				i++;
			}
		}
		return vals;
	}
	
/***********************************************************************
 * A helper function used to encapsulate the task of creating a
 * parent-level node's <code>Hashtable</code> of key-value pairs. 
 * 
 * @param keys - A <code>String[]</code> of the column names (keys).
 * @param vals - A <code>String[]</code> of the values.
 * @return A <code>Hashtable</code> mapping of the key-value pairs.
 */
	private Hashtable<String,String> buildHashtableFromArrays(String[] keys, String[] vals) {
		/* Check same length */
		if(keys.length != vals.length) {
			return null;
		}
		Hashtable<String,String> rv = new Hashtable<String,String>();
		for(int i = 0; i < keys.length; i++) {
			rv.put(keys[i], vals[i]);
		}
		return rv;
	}
	
	private String extractTableFromExp(String exp) {
		return exp.substring(0, exp.indexOf(" "));
	}
	
/***********************************************************************
 * Returns the <code>InsertNode</code> at the specified position in this
 * node if it exists. 
 * 
 * This method is essentially a wrapper around 
 * <code>java.util.Vector.get(int index)</code>.
 * 
 * @param index - index of the node to return.
 * @return the <code>InsertNode</code> located at the <code>index</code>.
 */
	public InsertNode getNode(int index) {
		return (nodes != null) ? nodes.get(index) : null;
	}
	
/***********************************************************************
 * Returns a <code>Vector</code> of <code>InsertNode</code>s if this is a
 * parent node. <code>null</code> if this is a terminal node.
 * 
 * @return A <code>Vector</code> or <code>null</code>
 */
	public Vector<InsertNode> getNodes() {
		return this.nodes;
	}
	
/***********************************************************************
 * Deletes the <code>InsertNode</code> at the specified index. The remaining
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
 * Tests whether an <code>InsertNode</code> contains any child nodes or not.
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
 * Returns the number of child nodes this <code>InsertNode</code> contains. 
 * 
 * This method is essentially a wrapper implementation of 
 * <code>java.util.Vector.size()</code>
 * 
 * @return The number of child nodes as <code>int</code>.
 */
	public int size() {
		return (nodes != null) ? nodes.size() : 0;
	}
	
/***********************************************************************
 * Returns an iterator over the list of <code>InsertNode</code>s in the 
 * order that they occur in the <code>Vector&ltInsertNode&gt</code>.
 * 
 * This method is essentially a wrapper implementation of 
 * <code>java.util.Vector.iterator()</code>.
 * 
 * @return an <code>Iterator</code>.
 */
	public Iterator<InsertNode> iterator() {
		return this.nodes.iterator();
	}
	
/***********************************************************************
 * Executes the SQL insert statement contained within the 
 * <code>InsertNode</code> that calls it.
 * 
 *  @return a <code>null</code> if the insert is successful.
 */
	public ResultNode execute() {
		Connection con = new Connection();
		con.executeInsert(this);	
		con.close();
		/* Query the DB for the primary key of what was just inserted */
		QueryNode query = new QueryNode();
		query.addNode("SELECT", "*");
		query.addNode("FROM", this.getTable());
		Iterator<Map.Entry<String, String>> iter = this.getPairs().entrySet().iterator(); 
		while(iter.hasNext()) {
			Map.Entry<String, String> entry = iter.next();
			query.addNode("WHERE", entry.getKey() + "='" + entry.getValue() + "'");
		}
		return query.execute();
	}
	
/***********************************************************************
 * Takes an <code>InsertNode</code> full of only <code>insert</code> type, 
 * and constructs a SQL <code>INSERT</code> clause as a <code>String</code>. 
 * 
 * @param insert - a <code>InsertNode</code> of nodes of only <code>insert</code>
 * type.
 * @return a <code>String</code> formatted as a SQL <code>INSERT</code> 
 * clause.
 */
	private static String constructInsertClause(InsertNode insert) {
		Hashtable<String,String> props = ConnectionProperties.getProperties();
		insert = insert.getNode(0);
		String clause = "INSERT INTO " + props.get("dbname") + "." + insert.getExpression();
		return clause;
	}
	
/***********************************************************************
 * Takes an <code>InsertNode</code> full of only <code>values</code> type, 
 * and constructs a SQL <code>VALUES</code> clause as a <code>String</code>. 
 * 
 * @param values - a <code>InsertNode</code> of nodes of only <code>values</code>
 * type.
 * @return a <code>String</code> formatted as a SQL <code>VALUES</code> 
 * clause.
 */
	private static String constructValuesClause(InsertNode values) {
		values = values.getNode(0);
		String clause = "VALUES " + values.getExpression();
		return clause;
	}
	
/***********************************************************************
 * Implementation of a standard <code>toString()</code> method. This 
 * method returns a <code>String</code> representation of the node as 
 * a human-readable SQL <code>INSERT</code>.
 * 
 * This method is responsible for constructing the SQL Query represented
 * by the <code>InsertNode</code> in a well-formed way so that it can be
 * presented to a <code>Connection</code> and executed properly.
 * 
 * @return The <code>String</code> representation of the SQL <code>INSERT</code>.
 */
	public String toString() {
		String s = "";
		InsertNode insert = new InsertNode();
		InsertNode values = new InsertNode();

		for(InsertNode node : this) {
			if(node.getType().equalsIgnoreCase("insert")) {
				insert.addNode(node);
			} else if(node.getType().equalsIgnoreCase("values")) {
				values.addNode(node);
			} else {
				// We have a problem
			}
		}
		
		s = String.format("%s\n%s;", constructInsertClause(insert), constructValuesClause(values));
		
		return s;
	}
	
	
	public static void main(String[] args) {
		
//		try {
//			JSONObject json = new JSONObject("{ 'type' : 'INSERT', " +
//											   "'table' : 'test_bobby.User', " +
//									   		   "'insert' : [" +
//										   			"{'attribute' : 'LastName', 'value' : 'Neelands'}," +
//										   			"{'attribute' : 'FirstName', 'value' : 'Andrew'}," +
//										   			"{'attribute' : 'UserType', 'value' : 'student'}," +
//										   			"{'attribute' : 'CreateTime', 'value' : '2013-01-25 19:27:00'}" +
//										   				  "]" +
//										 "}");
//			System.out.println(json.toString(3));
//			InsertNode insert = new InsertNode(json);
//			
//			System.out.println(insert.toString());
//			
//			insert.execute();
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		String j = "{'type':'INSERT','table':'User','insert':[{'attribute':'FirstName','value':'Test'},{'attribute':'LastName','value':'Tester'},{'attribute':'EmailAddress','value':'test@test.com'},{'attribute':'UserType','value':'student'},{'attribute':'CreateTime','value':'2013-3-3 9:5:26'}]}";
		JSONObject json = null;
		try {
			json = new JSONObject(j);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		InsertNode insert = new InsertNode(json);
		
		System.out.println(insert.toString());
		
		insert.execute();
		
		
//		insert.addNode("INSERT", "User (LastName, FirstName, EmailAddress, UserType, CreateTime)");
//		insert.addNode("VALUES", "('Tester', 'Test', 'test@test.com', 'student', '2013-02-11 00:56:23')");
		
//		Hashtable<String,String> ht = new Hashtable<String,String>();
//		ht.put("LastName", "Tester");
//		ht.put("FirstName", "Test");
//		ht.put("EmailAddress", "test@test.com");
//		ht.put("UserType", "student");
//		ht.put("CreateTime", "2013-02-11 00:56:23");
//		insert.addNode("INSERT", "User", ht);
		
//		insert.addNode("INSERT", "User (LastName, FirstName, EmailAddress, UserType, CreateTime)");
//		insert.addNode("VALUES", new String[]{"Tester", "Test", "test@test.com", "student", "2013-02-11 00:56:23"});
//		
//		System.out.print("{");
//		for(int i = 0; i < insert.getNode(0).getCols().length; i++) {
//			System.out.print(String.format("%s=%s", insert.getNode(0).getCols()[i], insert.getNode(1).getValues()[i]));
//			if(i < (insert.getNode(0).getCols().length-1)) {
//				System.out.print(", ");
//			}
//		}
//		System.out.println("}");
//		System.out.println(insert.getPairs().toString());
//		
//		Iterator<Map.Entry<String, String>> iter = insert.getPairs().entrySet().iterator(); 
//		while(iter.hasNext()) {
//			Map.Entry<String, String> entry = iter.next();
//			System.out.printf("%s = %s\n", entry.getKey(), entry.getValue());
//		}
//		
//		System.out.println("TABLE = " + insert.table);
//		
//		ResultNode result = insert.execute();
//		System.out.println(result.getNode(0).getNodeWithAttr("UserID").getValue());
		
		

//		Hashtable<String, String> ht = new Hashtable<String,String>();
//		ht.put("FirstName", "Andrew");
//		ht.put("LastName", "Neelands");
//		ht.put("EmailAddress", "aneelanda@gmail.com");
//		ht.put("UserType", "student");
//		
//		InsertNode insert = new InsertNode();
//		insert.addNode("INSERT", "test_bobby.User", ht);
//		System.out.println(insert.toString());
		
//		ResultNode result = insert.execute();
//		
//		System.out.println(result);
		
		System.exit(1);
		
	}

}
