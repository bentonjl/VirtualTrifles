<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.*, dbnode.*;"%>

<%
	String theUserID = request.getParameter("userID");
	String theName = request.getParameter("playerName");
	String theGender = request.getParameter("playerGender");



	String theValues = "(" + theUserID + ", '" + theName + "', '" + theGender + "')";

	
	InsertNode theInsert = new InsertNode();
	theInsert.addNode("INSERT", "INTO test_andy.GameInstance  (UserID, CharacterName, CharacterGender)");
	theInsert.addNode("VALUES", theValues);
	theInsert.execute();
	
%>

<%=theValues%>