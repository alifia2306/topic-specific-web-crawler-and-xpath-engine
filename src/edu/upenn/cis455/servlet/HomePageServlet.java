package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HomePageServlet extends HttpServlet {
	public static String homePage = "<head>\n\n<font color=\"blue\">Welcome!</font>"
			+ "\n\n<br>\n<br>\n</head>\n<body>\n<div>\n <a id=\"mybutton\" href=\"view"
			+ "\" title=\"View Channel\">\n  <button style=\"height:50px;width:200px\">"
			+ "View Channel</button>\n </a>\n</div>\n<div>\n <a id=\"mybutton\" href=\"create\" "
			+ "title=\"Create Channel\">\n  <button style=\"height:50px;width:200px\">Create Channel"
			+ "</button>\n </a>\n</div>\n<div>\n <a id=\"mybutton\" href=\"delete\""
			+ " title=\"Delete Channel\">\n  <button style=\"height:50px;width:200px\">Delete Channel"
			+ "</button>\n </a>\n</div>\n<div>\n <a id=\"mybutton\" href=\"all_channels\" "
			+ "title=\"All Channels\">\n  <button style=\"height:50px;width:200px\">All Channels</button>"
			+ "\n </a>\n</div>\n<div>\n <a id=\"mybutton\" href=\"login\" title=\"Login\">\n  "
			+ "<button style=\"height:50px;width:200px\">Login</button>\n </a>"
			+ "\n </a>\n</div>\n<div>\n <a id=\"mybutton\" href=\"logout\" title=\"Logout\">\n  "
			+ "<button style=\"height:50px;width:200px\">Logout</button>\n </a>\n</div>\n\n</body>";

	/*
	 * doGet for HttpServlet class
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		response.setContentType("text/html");
		PrintWriter writer = response.getWriter();
		writer.write(homePage);
		writer.flush();
	}
}
