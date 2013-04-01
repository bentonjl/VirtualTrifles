package dbnode;

import java.util.Iterator;
import java.util.Vector;

public interface SQLNode<T> extends Iterable<T>{
/***********************************************************************
 * INSTANCE VARIABLES
 */
//	private String desc;
//	private String type;
//	private String expression;
//	private Vector<QueryNode> nodes = null;

/***********************************************************************
 * CONSTRUCTORS
 */
//	public QueryNode(String desc);
//	public QueryNode(String desc, Vector<QueryNode> nodes);
//	public QueryNode(String type, String exp);
//	public QueryNode(JSONObject json);
/***********************************************************************
 * GETTERS AND SETTERS
 */
//	private void setType(String type);
	public String getType();
//	private void setExpression(String exp);
	public String getExpression();
/***********************************************************************
 * GENERAL METHODS 
 */
	public void addNode(T node);
	public void addNode(String type, String exp);
	public T getNode(int idx);
	public Vector<T> getNodes();
	public boolean deleteNode(int idx);
	public boolean hasNodes();
	public int size();
	public Iterator<T> iterator();
	/* Return null if no result set is returned */
	public ResultNode execute();
	public String toString();
}
