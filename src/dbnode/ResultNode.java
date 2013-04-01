package dbnode;

import java.util.Iterator;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

public class ResultNode implements Iterable<ResultNode>{
/***********************************************************************
 * INSTANCE VARIABLES
 */
	/* CELL-LEVEL VARIABLES */
	private String attr = "";
	private String value = "";
	
	/* ROW-LEVEL VARIABLES */
	private int rowIdx = -1;
	
	/* TABLE-LEVEL VARIABLES */
	
	/* CROSS-LEVEL VARIABLES */
	private Vector<ResultNode> nodes = null;
	private String name = "";

/***********************************************************************
 * CONSTRUCTORS
 */
	public ResultNode() {}
	
/***********************************************************************
 * Constructor for creating a cell-level node. This <code>ResultNode</code>
 * would represent an individual attribute-value pair inside a specific 
 * instance (row) of some database relation (table). 
 * 
 * @param attr - The attribute that this value belongs to.
 * @param value - The value.
 * @param name - A name to specify for this <code>ResultNode</code>. If 
 * 				<code>null</code> it will default to <code>cell</code>.
 */
	public ResultNode(String attr, String value, String name) {
		this.setAttr(attr);
		this.setValue(value);
		this.name = (name == null) ? "cell" : name;
	}
	
/***********************************************************************
 * Constructor for creating a row-level node. This <code>ResultNode</code>
 * would represent a specific instance (row) of some database relation
 * (table). 
 * 
 * @param rowIdxn - The index of the row.
 * @param name - A name to specify for this <code>ResultNode</code>. If 
 * 				<code>null</code> it will default to <code>row</code>.
 */	
	public ResultNode(int rowIdx, String name) {
		this.setRowIdx(rowIdx);
		this.name = (name == null) ? "row" : name;
	}
/***********************************************************************
 * Constructor for creating a row-level node. This <code>ResultNode</code>
 * would represent a specific instance (row) of some database relation
 * (table). 
 * 
 * @param rowIdx - The index of the row.
 * @param nodes - The <code>Vector&ltResultNode&gt</code> of attr-value pairs
 * 					belonging in this row.
 * @param name - A name to specify for this <code>ResultNode</code>. If 
 * 				<code>null</code> it will default to <code>row</code>.
 */
	public ResultNode(int rowIdx, Vector<ResultNode> nodes, String name) {
		this(rowIdx, name);
		this.nodes = nodes;
	}
	
/***********************************************************************
 * Constructor for creating a <code>ResultNode</code> from a list of 
 * existing <code>ResultNode</code>s as a <code>java.util.Vector</code>. <br><br>
 * While this constructor could be used to construct a <code>ResultNode</code>
 * using any <code>Vector</code> of nodes, it is worth noting that this 
 * constructor only sets the instance variable <code>nodes</code>. That 
 * is because this constructor is intended to be used to create the 
 * table-level node. That is, the intention of this constructor is to be 
 * used to take a list of "rows" as it's argument to construct the whole
 * table of results. 
 * 
 * @param nodes - The list of nodes to add to this <code>ResultNode</code> 
 * 					<code>java.util.Vector</code>.
 * @param name - A name to specify for this <code>ResultNode</code>. If 
 * 				<code>null</code> it will default to <code>table</code>.
 */
	public ResultNode(Vector<ResultNode> nodes, String name) {
		this.nodes = nodes;
		this.name = (name == null) ? "table" : name;
	}
	
	public ResultNode(String name) {
		this.name = (name == null) ? "table" : name;
	}

	
	
/***********************************************************************
 * GETTERS AND SETTERS
 */
	private void setAttr(String attr) {
		this.attr = attr;
	}
	public String getAttribute() {
		return this.attr;
	}	
	private void setValue(String value) {
		this.value = value;
	}
	public String getValue() {
		return this.value;
	}
	private void setRowIdx(int idx) {
		this.rowIdx = idx;
	}
	
/***********************************************************************
 * GENERAL METHODS 
 */
/***********************************************************************
 * Adds a node to the <code>ResultNode</code>.
 * 
 * Please see the documentation for <code>ResultNode</code> for an 
 * example of how a <code>ResultNode</code> is typically structured. 
 * 
 * @param node - The row to add as a <code>ResultNode</code>
 */
	public void addNode(ResultNode node) {
		if(nodes == null) {
			nodes = new Vector<ResultNode>();
		}
		nodes.add(node);
	}
	
/***********************************************************************
 * Adds a node to the <code>ResultNode</code>.
 * 
 * This method should be used when adding values from a table row. The 
 * <code>ResultNode</code> being added to should represent a table row 
 * from a query result, and the <code>attr</code> and <code>value</code>
 * indicated should be specific "cells" in that table.
 * 
 * Please see the documentation for <code>ResultNode</code> for an 
 * example of how a <code>ResultNode</code> is typically structured. 
 * 
 * @param attr - The attribute (column) that this node relates to.
 * @param value - The value to store at this attribute.
 */
	public void addNode(String attr, String value) {
		if(nodes == null) {
			nodes = new Vector<ResultNode>();
		}
		nodes.add(new ResultNode(attr, value, null));
	}
	
/***********************************************************************
 * Deletes the <code>ResultNode</code> at the specified index. The remaining
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
			nodes.remove(index);
			return true;
		}
		return false;
	}
	
/***********************************************************************
 * Returns the <code>ResultNode</code> at the specified position in this
 * node if it exists. 
 * 
 * This method is essentially a wrapper around 
 * <code>java.util.Vector.get(int index)</code>.
 * 
 * @param index - index of the node to return.
 * @return the <code>ResultNode</code> located at the <code>index</code>.
 */
	public ResultNode getNode(int index) {
		return (nodes != null) ? nodes.get(index) : null;
	}
	
/***********************************************************************
 * Returns the <code>ResultNode</code> that matches the attribute 
 * indicated by <code>attr</code>. 
 * 
 * This method is intended to be used on a "row-level" <code>ResultNode</code>.
 * It is unlikely that this method will return anything at all if it is 
 * used on anything else (like a table-level or cell-level
 * <code>ResultNode</code>. To this end, it is important that the 
 * <code>ResultNode</code> calling this method has it's <code>name</code>
 * attribute initialized as "row". This is the default for any 
 * <code>ResultNode</code> representing a row. It can only be different 
 * if it is explicitly constructed with a different name. 
 * 
 * @param attr - The attribute of the intended <code>ResultNode</code>
 * @return the <code>ResultNode</code> representing the cell for this attribute. 
 */
	public ResultNode getNodeWithAttr(String attr) {
		if(this.name.equalsIgnoreCase("row")) {
			for (ResultNode node : this.nodes) {
				if(node.getAttribute().equalsIgnoreCase(attr)) {
					return node;
				}
			}
		}
		return null;
	}
	
/***********************************************************************
 * Tests whether a <code>ResultNode</code> contains any child nodes or not.
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
 * Returns an iterator over the list of <code>ResultNode</code>s in the 
 * order that they occur in the <code>Vector&ltResultNode&gt</code>.
 * 
 * This method is essentially a wrapper implementation of 
 * <code>java.util.Vector.iterator()</code>.
 * 
 * @return an <code>Iterator</code>.
 */
	public Iterator<ResultNode> iterator() {
		return this.nodes.iterator();
	}
	
/***********************************************************************
 * Returns the number of child nodes this <code>ResultNode</code> contains. 
 * 
 * This method is essentially a wrapper implementation of 
 * <code>java.util.Vector.size()</code>
 * 
 * @return The number of child nodes as <code>int</code>.
 */
	public int size() {
		return (nodes != null) ? nodes.size() : 0;
	}
	
	public String toJSON() {
		if(!this.hasNodes()) {
			return null;
		}
		String json = "{\"results\" : [";
		for(int i = 0; i < this.nodes.size(); i++) {
			ResultNode row = this.getNode(i);
			json += "{";
			for(int j = 0; j < row.size(); j++) {
				json += String.format("\"%s\" : \"%s\"", row.getNode(j).getAttribute(), row.getNode(j).getValue());
				json += (j == row.size()-1) ? "" : ", ";
			}
			json += (i == this.nodes.size()-1) ? "}" : "},";
		}
		json += "], \"size\" : \"" + this.size() + "\" }";
		return json;
	}
	
/***********************************************************************
 * A method to return the data inside the <code>ResultNode</code> in
 * such a way that is easy to read. 
 * 
 * @return a <code>String</code>
 */
	public String toString() {
		String s = "";
		for(int i = 0; i < this.size(); i++) {
			s +="Row " + i + ": \n" + this.getNode(i).toStringHelper(i);
		}
		return s;
	}
	
/***********************************************************************
 * A helper method to the <code>toString</code> method for the 
 * <code>ResultNode</code>. This method simplifies the task of printing
 * out the data by encapsulating the printing of row data in an indexed
 * list format.
 * 
 * @param idx - The row index to be output.
 * @return a <code>String</code> of the data in the row.
 */
	private String toStringHelper(int idx) {
		if(this.size() == 0 && nodes == null) {
			return String.format("%s %s\n", this.attr, this.value);
		} else {
			String s = "";
			for(int i = 0; i < this.size(); i++) {
				s += "  " + i + ": " + this.getNode(i).toStringHelper(i);
			}
			return s;
		}
	}

	public static void main(String[] args) {
		
		
		JSONObject json;
		try {
			json = new JSONObject("{'type':'query','table':'User','select':['*'],'join':[{'table':'hasClass','lhs':'User.UserID','rhs':'hasClass.UserID'},{'table':'Class','lhs':'hasClass.ClassID','rhs':'Class.ClassID'}],'where':[{'attribute':'User.UserID','value':'3'}]}");
			QueryNode query = new QueryNode(json);
			
			System.out.println(query.toString());
			
			ResultNode result = query.execute();
			System.out.println(result.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
