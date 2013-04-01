<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%
	String goToURL = request.getParameter("goToURL");
%>

<html>
<script src="js/vendor/jquery.js" type="text/javascript"></script>
<script src="js/login.js" type="text/javascript"></script>
<script src="js/cookies.js" type="text/javascript"></script>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
	Username: <input id="username" type="text" name="username"><br>
	Password: <input id="password" type="text" name="password"><br>
	<input id="goToURL" type="hidden" name="goToURL" value="<%=goToURL%>">
	<input type="button" value="Submit" onclick="loginSubmit()">
	<br><br>
	<font color="red"><div id="errorText">
	</div></font>
</body>
</html>
