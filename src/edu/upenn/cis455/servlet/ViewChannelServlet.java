package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ViewChannelServlet extends HttpServlet {
	public static String viewPage = "<head>\n<font color=\"blue\">View Channel</font>\n<br>\n<br>"
			+ "\n</head>\n<body>\n<form action=\"view\" method = \"post\">\n  "
			+ "Channel Name:<br>\n  <input type=\"text\" name=\"channel\" value=\"\">\n  <br>\n  \n "
			+ " <input type=\"submit\" value=\"VIEW\">\n</form>\n</body>";

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
		writer.write(viewPage);
		writer.flush();
	}

	/*
	 * doPost for HttpServlet class
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String channel = request.getParameter("channel");
		response.sendRedirect("display?channelname=" + channel);
	}

}
