<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.*, org.json.*, dbnode.*" %>

<%
	Enumeration e = request.getParameterNames(); // retrieves the uri-encoded JSON object

	JSONObject json = new JSONObject((String)e.nextElement()); // builds a JSONObject
	
	if(json.getString("type").equalsIgnoreCase("query")) {
		QueryNode query = new QueryNode(json);
		ResultNode result = query.execute();
		%> <%=result.toJSON()%> <%
	} else if(json.getString("type").equalsIgnoreCase("insert")) {
		InsertNode insert = new InsertNode(json);
		ResultNode result = insert.execute();
		%> <%=result.toJSON()%> <%
	} else if(json.getString("type").equalsIgnoreCase("update")) {
		UpdateNode update = new UpdateNode(json);
		update.execute();
		%> { "message" : "success" } <%
	} else if(json.getString("type").equalsIgnoreCase("delete")) {
		DeleteNode delete = new DeleteNode(json);
		delete.execute();
		%> { "message" : "success" } <%
	}
	
%>
