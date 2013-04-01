package dbnode;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UpdateNode implements SQLNode<UpdateNode> {
/***********************************************************************
 * INSTANCE VARIABLES
 */
	private String desc;
	private String type;
	private String expression;
	private Vector<UpdateNode> nodes = null;

/***********************************************************************
 * CONSTRUCTORS
 */
	public UpdateNode() {
		this.desc = "parent";
	}	
	public UpdateNode(String desc) {
		this.desc = desc;
	}
	public UpdateNode(String desc, Vector<UpdateNode> nodes) {
		if(desc == null) {
			this.desc = "parent";
		} else {
			this.desc = desc;
		}
		this.nodes = nodes;
	}
	public UpdateNode(String type, String exp) {
		this.desc = "terminal";
		this.setType(type);
		this.setExpression(exp);
	}
	public UpdateNode(JSONObject json) {
		try {
			if(json.getString("type").equalsIgnoreCase("update")) {
				JSONArray setTuples = json.getJSONArray("set");
				JSONArray whereTuples = json.has("where") ? json.getJSONArray("where") : null;
				String setExpression = "";
				for(int i = 0; i < setTuples.length(); i++) {
					setExpression += String.format("%s='%s'", 
										setTuples.getJSONObject(i).getString("attribute"),
										setTuples.getJSONObject(i).getString("value"));
					setExpression += (i != setTuples.length()-1) ? ", " : "";
				}
				
				this.addNode("UPDATE", json.getString("table"));
				this.addNode("SET", setExpression);
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
 * @return <code>UpdateNode</code> at index 0.
 */
	private UpdateNode popNode() {
		UpdateNode returnNode = null;
		if(nodes != null) {
			returnNode = nodes.firstElement();
			nodes.remove(0);			
		}
		return returnNode;
	}
	
/***********************************************************************
 * Adds a node to the <code>UpdateNode</code>. Each child node added to 
 * the <code>UpdateNode</code> is an individual SQL clause to be executed
 * in an <code>UPDATE</code> statement. 
 * 
 * @param node - a <code>UpdateNode</code> to be added as a child node. 
 */
	@Override
	public void addNode(UpdateNode node) {
		if(nodes == null) {
			nodes = new Vector<UpdateNode>();
		}
		nodes.add(node);
	}

/***********************************************************************
 * Adds a node to the <code>UpdateNode</code>. Each child node added to 
 * the <code>UpdateNode</code> is an individual SQL clause to be executed
 * in an <code>UPDATE</code> statement. 
 * 
 * @param type - The type of node to add (e.g. UPDATE, VALUES, etc...)
 * @param exp - The expression (or predicate) of the update clause.
 */
	@Override
	public void addNode(String type, String exp) {
		if(nodes == null) {
			nodes = new Vector<UpdateNode>();
		}
		nodes.add(new UpdateNode(type, exp));
	}

/***********************************************************************
 * Returns the <code>UpdateNode</code> at the specified position in this
 * node if it exists. 
 * 
 * This method is essentially a wrapper around 
 * <code>java.util.Vector.get(int index)</code>.
 * 
 * @param index - index of the node to return.
 * @return the <code>UpdateNode</code> located at the <code>index</code>.
 */
	@Override
	public UpdateNode getNode(int index) {
		return (nodes != null) ? nodes.get(index) : null;
	}

/***********************************************************************
 * Returns a <code>Vector</code> of <code>UpdateNode</code>s if this is a
 * parent node. <code>null</code> if this is a terminal node.
 * 
 * @return A <code>Vector</code> or <code>null</code>
 */
	@Override
	public Vector<UpdateNode> getNodes() {
		return this.nodes;
	}

/***********************************************************************
 * Deletes the <code>UpdateNode</code> at the specified index. The remaining
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
 * Tests whether an <code>UpdateNode</code> contains any child nodes or not.
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
 * Returns the number of child nodes this <code>UpdateNode</code> contains. 
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
 * Returns an iterator over the list of <code>UpdateNode</code>s in the 
 * order that they occur in the <code>Vector&ltUpdateNode&gt</code>.
 * 
 * This method is essentially a wrapper implementation of 
 * <code>java.util.Vector.iterator()</code>.
 * 
 * @return an <code>Iterator</code>.
 */
	@Override
	public Iterator<UpdateNode> iterator() {
		return this.nodes.iterator();
	}

/***********************************************************************
 * Executes the SQL <code>UPDATE</code> statement contained within the 
 * <code>UpdateNode</code> that calls it.
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
 * Takes an <code>UpdateNode</code> full of only <code>update</code> type, 
 * and constructs a SQL <code>UPDATE</code> clause as a <code>String</code>. 
 * 
 * @param update - a <code>UpdateNode</code> of nodes of only <code>update</code>
 * type.
 * @return a <code>String</code> formatted as a SQL <code>UPDATE</code> 
 * clause.
 */
	private static String constructUpdateClause(UpdateNode update) {
		Hashtable<String,String> props = ConnectionProperties.getProperties();
		update = update.getNode(0);
		String clause = "UPDATE " + props.get("dbname") + "." + update.getExpression();
		return clause;
	}
	
/***********************************************************************
 * Takes an <code>UpdateNode</code> full of only <code>set</code> type, 
 * and constructs a SQL <code>SET</code> clause as a <code>String</code>. 
 * 
 * @param set - a <code>UpdateNode</code> of nodes of only <code>set</code>
 * type.
 * @return a <code>String</code> formatted as a SQL <code>SET</code> 
 * clause.
 */
	private static String constructSetClause(UpdateNode set) {
		set = set.getNode(0);
		String clause = "SET " + set.getExpression();
		return clause;
	}
	
/***********************************************************************
 * Takes an <code>UpdateNode</code> full of only <code>where</code> type,
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
 * @param wheres - a <code>UpdateNode</code> of nodes of only <code>where</code> 
 * type.
 * @return a <code>String</code> formatted as a SQL <code>WHERE</code>
 * clause.
 */
	private static String constructWhereClause(UpdateNode wheres) {
		String clause = "";
		if(wheres.hasNodes()) {
			clause = "WHERE " + wheres.popNode().getExpression();
			for(UpdateNode node : wheres) {
				clause += " AND " + node.getExpression();
			}
		}
		return clause;
	}
	
/***********************************************************************
 * Implementation of a standard <code>toString()</code> method. This 
 * method returns a <code>String</code> representation of the node as 
 * a human-readable SQL <code>UPDATE</code> statement.
 * 
 * This method is responsible for constructing the SQL Update represented
 * by the <code>UpdateNode</code> in a well-formed way so that it can be
 * presented to a <code>Connection</code> and executed properly.
 * 
 * @return The <code>String</code> representation of the SQL update.
 */
	public String toString() {
		String s = "";
		UpdateNode update = new UpdateNode();
		UpdateNode set = new UpdateNode();
		UpdateNode wheres = new UpdateNode();

		for(UpdateNode node : this) {
			if(node.getType().equalsIgnoreCase("update")) {
				update.addNode(node);
			} else if(node.getType().equalsIgnoreCase("set")) {
				set.addNode(node);
			} else if(node.getType().equalsIgnoreCase("where")) {
				wheres.addNode(node);
			} else {
				// We have a problem
			}
		}
		
		s = String.format("%s\n%s\n%s;", constructUpdateClause(update), constructSetClause(set), constructWhereClause(wheres));
		
		return s;
	}
	
	
	public static void main(String[] args) {
		
//		try {
//			JSONObject json = new JSONObject("{ 'type' : 'UPDATE', " +
//											   "'table' : 'User', " +
//											   "'set' : [" +
//											   		"{'attribute' : 'FirstName', 'value' : 'Andy'}," +
//											   		"{'attribute' : 'LastName', 'value' : 'Neel'}" +
//											   		   "], " +
//											   "'where' : [" +
//											   		"{'attribute' : 'UserID', 'value' : '2'}," +
//											   		"{'attribute' : 'UserType', 'value' : 'student'}" +
//											   			 "]" +
//											 "}");
//			
//			System.out.println(json.toString(3));
//			
//			UpdateNode update = new UpdateNode(json);
//			
//			System.out.println(update.toString());
//			
////			update.execute();
//			
//			
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
		
		
		UpdateNode update = new UpdateNode();
		update.addNode("UPDATE", "User");
		update.addNode("SET", "firstName='Robert', lastName='Frankenberger'");
		update.addNode("WHERE", "UserID=1");
//		update.addNode("WHERE", "lastName='Frankenberger'");
//		update.addNode("WHERE", "firstName='Robert'");
		
		System.out.println(update.toString());
		
		update.execute();
	}

}
