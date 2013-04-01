package servlets;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dbnode.QueryNode;
import dbnode.ResultNode;

@WebServlet("/auth")
public class Authentication extends HttpServlet {
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		String user = request.getParameter("username");
		String pass = request.getParameter("password");
		
		String json = "{\"goToURL\" : \"" + getCookieValue("goToURL", request) + "\"";
				
		/* Only proceed if valid user */
		if(isValidUser(user)) {
			QueryNode query = new QueryNode();
			query.addNode("SELECT", "*");
			query.addNode("FROM", "User");
			query.addNode("JOIN", "Password ON User.UserID=Password.UserID");
			query.addNode("WHERE", "User.EmailAddress='" + user + "'");
			query.addNode("WHERE", "Password.Password='" + pass + "'");
			/* Check DB for user / pass combo */
			ResultNode result = query.execute();
			/* Check if the query found a match */
			if(result.hasNodes()) {	// User/Pass are valid
				String status = (String)session.getAttribute("loggedIn");

				if(status == null || status.equals("no")) {
					synchronized(session) {
						session.setAttribute("loggedIn", "yes");
						json += ", \"loggedIn\" : \"yes\"";
					}
				} else if(status.equals("yes")) {
					json += ", \"loggedIn\" : \"yes\"";
					// do nothing
				}
				/* Pass a logged-in cookie to client */
				Cookie c = new Cookie("loggedIn", "yes");
				c.setPath("/");
				c.setMaxAge(60 * 60 * 2);
				response.addCookie(c);
				/* Pass a userID cookie to client */
				Cookie id = new Cookie("userID", result.getNode(0).getNodeWithAttr("UserID").getValue());
				id.setPath("/");
				id.setMaxAge(60 * 60 * 2);
				response.addCookie(id);
			} else {
				json += ", \"loggedIn\" : \"no\"";
				json += ", \"message\" : \"Incorrect username or password\"";
			}
		} else {
			json += ", \"loggedIn\" : \"no\"";
			json += ", \"message\" : \"Incorrect username or password\"";
		}
		
		json += "}";
		
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		out.print(json);
	}
	
	private boolean isValidUser(String user) {
		QueryNode query = new QueryNode();
		query.addNode("SELECT", "*");
		query.addNode("FROM", "User");
		query.addNode("WHERE", "User.EmailAddress='" + user + "'");
		ResultNode result = query.execute();
		
		if(result.hasNodes()) { // user exists
			return true;
		}
		return false;
	}
	
	private String getCookieValue(String name, HttpServletRequest request) {
		String s = "";
		Cookie[] cookies = request.getCookies();
		if(cookies != null) {
			for(int i = 0; i < cookies.length; i++) {
				if(name.equals(cookies[i].getName())) {
					s = cookies[i].getValue();
				}
			}
		}
		return s;
		
	}
}
