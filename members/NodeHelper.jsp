<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.*, dbnode.*;"%>

<%

String theUserID = request.getParameter("userID");

String theCharName = request.getParameter("name");

String theLevelID = request.getParameter("levelID");

String theValues = "(" + theUserID + ", \'" + theCharName + "\' , " + theLevelID + ")";

InsertNode insert = new InsertNode();



insert.addNode("INSERT", "INTO test_andy.hasNode (UserID, CharacterName, GameNodeID)");



insert.addNode("VALUES", theValues);

insert.execute();



%>

