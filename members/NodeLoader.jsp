<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.*, dbnode.*;"%>

<%

String theUserID = request.getParameter("userID");

QueryNode theQuery = new QueryNode();

theQuery.addNode("SELECT", "test_andy.hasNode.CharacterName, test_andy.hasNode.GameNodeID");
theQuery.addNode("FROM", "test_andy.hasNode, test_andy.GameNode");
theQuery.addNode("WHERE", "test_andy.hasNode.UserID=" + theUserID);
theQuery.addNode("WHERE", "test_andy.hasNode.GameNodeID=test_andy.GameNode.GameNodeID");
theQuery.addNode("WHERE", "test_andy.GameNode.type = 'MS' ORDER BY hasNode.GameNodeID DESC");
ResultNode theResult = theQuery.execute();

String theJSON = "{\"nothing\", \"nothing\"}";



try{
 theJSON = "{\"charName\" : \"" + theResult.getNode(0).getNode(0).getValue() +"\", \"levelID\" : \"" + theResult.getNode(0).getNode(1).getValue() + "\"}"; 
}catch(NullPointerException e){}

%>
<%=theJSON%>