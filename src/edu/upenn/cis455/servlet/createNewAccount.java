package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class createNewAccount extends HttpServlet{
public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		response.setContentType("text/html");
		PrintWriter writer = response.getWriter();
		writer.write(Account_PAGE);
	}
public static String Account_PAGE = "<head>\n<font color=\"blue\">"
		+ "Create Account Page</font>\n<br>\n<br>\n</head>\n<body>\n"
		+ "<form action=\"homePage\" method = \"post\">\n  "
		+ "First Name:<br>\n  <input type=\"text\" name=\"FirstName\" value=\"\">\n "
		+ " <br>\n  Last name:<br>\n  <input type=\"text\" name=\"LastName\" value=\"\">\n"
		+ "  <br>\n  User Name:<br>\n  <input type=\"text\" name=\"UserName\" value=\"\">\n  <"
		+ "br>\n  Password:<br>\n  <input type=\"text\" name=\"Password\" value=\"\">\n  <br><br>\n "
		+ " Repeat Password:<br>\n  <input type=\"text\" name=\"repeatPassword\" value=\"\">\n  <br><br>\n  "
		+ "<input type=\"submit\" value=\"Submit\">\n</form>\n</body>";
}
