package dbnode;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DeleteNode implements SQLNode<DeleteNode> {
/***********************************************************************
 * INSTANCE VARIABLES
 */
	private String desc;
	private String type;
	private String expression;
	private Vector<DeleteNode> nodes = null;

/***********************************************************************
 * CONSTRUCTORS
 */
	public DeleteNode() {
		this.desc = "parent";
	}	
	public DeleteNode(String desc) {
		this.desc = desc;
	}
	public DeleteNode(String desc, Vector<DeleteNode> nodes) {
		if(desc == null) {
			this.desc = "parent";
		} else {
			this.desc = desc;
		}
		this.nodes = nodes;
	}
	public DeleteNode(String type, String exp) {
		this.desc = "terminal";
		this.setType(type);
		this.setExpression(exp);
	}
	public DeleteNode(JSONObject json) {
		try {
			if(json.getString("type").equalsIgnoreCase("delete")) {
				JSONArray whereTuples = json.has("where") ? json.getJSONArray("where") : null;
				
				this.addNode("DELETE", json.getString("table"));
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
	@Override
	public String getType() {
		return this.type;
	}
	private void setExpression(String exp) {
		this.expression = exp;
	}
	@Override
	public String getExpression() {
		return this.expression;
	}

/***********************************************************************
 * GENERAL METHODS
 */
/***********************************************************************
 * Removes and returns the node at the top of the list of nodes. 
 * 
 * @return <code>DeleteNode</code> at index 0.
 */
	private DeleteNode popNode() {
		DeleteNode returnNode = null;
		if(nodes != null) {
			returnNode = nodes.firstElement();
			nodes.remove(0);			
		}
		return returnNode;
	}
	
/***********************************************************************
 * Adds a node to the <code>DeleteNode</code>. Each child node added to 
 * the <code>DeleteNode</code> is an individual SQL clause to be executed
 * in an <code>DELETE</code> statement. 
 * 
 * @param node - a <code>DeleteNode</code> to be added as a child node. 
 */
	@Override
	public void addNode(DeleteNode node) {
		if(nodes == null) {
			nodes = new Vector<DeleteNode>();
		}
		nodes.add(node);
	}

/***********************************************************************
 * Adds a node to the <code>DeleteNode</code>. Each child node added to 
 * the <code>DeleteNode</code> is an individual SQL clause to be executed
 * in an <code>DELETE</code> statement. 
 * 
 * @param type - The type of node to add (e.g. DELETE, WHERE, etc...)
 * @param exp - The expression (or predicate) of the delete clause.
 */
	@Override
	public void addNode(String type, String exp) {
		if(nodes == null) {
			nodes = new Vector<DeleteNode>();
		}
		nodes.add(new DeleteNode(type, exp));
	}

/***********************************************************************
 * Returns the <code>DeleteNode</code> at the specified position in this
 * node if it exists. 
 * 
 * This method is essentially a wrapper around 
 * <code>java.util.Vector.get(int index)</code>.
 * 
 * @param index - index of the node to return.
 * @return the <code>DeleteNode</code> located at the <code>index</code>.
 */
	@Override
	public DeleteNode getNode(int index) {
		return (nodes != null) ? nodes.get(index) : null;
	}

/***********************************************************************
 * Returns a <code>Vector</code> of <code>DeleteNode</code>s if this is a
 * parent node. <code>null</code> if this is a terminal node.
 * 
 * @return A <code>Vector</code> or <code>null</code>
 */
	@Override
	public Vector<DeleteNode> getNodes() {
		return this.nodes;
	}

/***********************************************************************
 * Deletes the <code>DeleteNode</code> at the specified index. The remaining
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
	@Override
	public boolean deleteNode(int index) {
		if(nodes != null) {
			nodes.removeElementAt(index);
			return true;
		}
		return false;
	}

/***********************************************************************
 * Tests whether an <code>DeleteNode</code> contains any child nodes or not.
 * 
 * This is essentially a wrapper implementation of
 * <code>java.util.Vector.isEmpty()</code>.
 * 
 * @return <code>true</code> if the node contains child nodes. 
 */
	@Override
	public boolean hasNodes() {
		return (nodes != null) ? !nodes.isEmpty() : false;
	}

/***********************************************************************
 * Returns the number of child nodes this <code>DeleteNode</code> contains. 
 * 
 * This method is essentially a wrapper implementation of 
 * <code>java.util.Vector.size()</code>
 * 
 * @return The number of child nodes as <code>int</code>.
 */
	@Override
	public int size() {
		return (nodes != null) ? nodes.size() : 0;
	}

/***********************************************************************
 * Returns an iterator over the list of <code>DeleteNode</code>s in the 
 * order that they occur in the <code>Vector&ltDeleteNode&gt</code>.
 * 
 * This method is essentially a wrapper implementation of 
 * <code>java.util.Vector.iterator()</code>.
 * 
 * @return an <code>Iterator</code>.
 */
	@Override
	public Iterator<DeleteNode> iterator() {
		return this.nodes.iterator();
	}

/***********************************************************************
 * Executes the SQL <code>DELETE</code> statement contained within the 
 * <code>DeleteNode</code> that calls it.
 * 
 *  @return a <code>null</code> if the update is successful.
 */
	public ResultNode execute() {
		Connection con = new Connection();
		con.executeUpdate(this);	
		con.close();
		return null;
	}
	
/***********************************************************************
 * Takes an <code>DeleteNode</code> full of only <code>delete</code> type, 
 * and constructs a SQL <code>DELETE</code> clause as a <code>String</code>. 
 * 
 * @param delete - a <code>DeleteNode</code> of nodes of only <code>delete</code>
 * type.
 * @return a <code>String</code> formatted as a SQL <code>DELETE</code> 
 * clause.
 */
	private static String constructDeleteClause(DeleteNode delete) {
		Hashtable<String,String> props = ConnectionProperties.getProperties();
		delete = delete.getNode(0);
		String clause = "DELETE FROM " + props.get("dbname") + "." + delete.getExpression();
		return clause;
	}
	
/***********************************************************************
 * Takes a <code>DeleteNode</code> full of only <code>where</code> type,
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
 * @param wheres - a <code>DeleteNode</code> of nodes of only <code>where</code> 
 * type.
 * @return a <code>String</code> formatted as a SQL <code>WHERE</code>
 * clause.
 */
	private static String constructWhereClause(DeleteNode wheres) {
		String clause = "";
		if(wheres.hasNodes()) {
			clause = "WHERE " + wheres.popNode().getExpression();
			for(DeleteNode node : wheres) {
				clause += " AND " + node.getExpression();
			}
		}
		return clause;
	}
	
/***********************************************************************
 * Implementation of a standard <code>toString()</code> method. This 
 * method returns a <code>String</code> representation of the node as 
 * a human-readable SQL <code>DELETE</code> statement.
 * 
 * This method is responsible for constructing the SQL Delete represented
 * by the <code>DeleteNode</code> in a well-formed way so that it can be
 * presented to a <code>Connection</code> and executed properly.
 * 
 * @return The <code>String</code> representation of the SQL delete.
 */
	public String toString() {
		String s = "";
		DeleteNode delete = new DeleteNode();
		DeleteNode wheres = new DeleteNode();

		for(DeleteNode node : this) {
			if(node.getType().equalsIgnoreCase("delete")) {
				delete.addNode(node);
			} else if(node.getType().equalsIgnoreCase("where")) {
				wheres.addNode(node);
			} else {
				// We have a problem
			}
		}
		
		s = String.format("%s\n%s;", constructDeleteClause(delete), constructWhereClause(wheres));
		
		return s;
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
//		try {
//			JSONObject json = new JSONObject("{ 'type' : 'DELETE', " +
//											   "'table' : 'User', " +
//											   "'where' : [" +
//											   		"{'attribute' : 'UserID', 'value' : '47'}," +
//											   		"{'attribute' : 'UserType', 'value' : 'student'}" +
//											   			 "]" +
//											 "}");
//			
//			System.out.println(json.toString(3));
//			
//			DeleteNode delete = new DeleteNode(json);
//			
//			System.out.println(delete.toString());
//			
//			delete.execute();
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
		
		DeleteNode delete = new DeleteNode();
		delete.addNode("DELETE", "User");
		delete.addNode("WHERE", "UserID=43");
		
		System.out.println(delete.toString());
		
		delete.execute();
	}

}
