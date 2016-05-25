package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LogoutServlet extends HttpServlet {

	/*
	 * doGet for HttpServlet class
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		HttpSession session = request.getSession();
		session.invalidate();
		PrintWriter writer = response.getWriter();
		writer.println(LOGOUT);
		writer.flush();
	}

	public static String LOGOUT = "<html><body>Logout Successful<br><br>Go back to "
			+ "<a href=\"homePage\">Go to home page</a><br/>"
			+ "<a href=\"login\">Go to Login Page</a><br/>";
}
