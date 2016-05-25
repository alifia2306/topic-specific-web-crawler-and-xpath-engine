package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.*;

@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {
	
	/* TODO: Implement user interface for XPath engine here */
	
	/* You may want to override one or both of the following methods */

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
//		response.setContentType("text/html");
//		try {
//			
//			PrintWriter printWriter = response.getWriter();
//			printWriter.write("<form action=\"handler\" method = \"post\">");
//			printWriter.write("XPath:<br>");
//			printWriter.write("<input type=\"text\" name=\"xpath\" value=\"\"><br>");
//			printWriter.write("xml/html:<br>");
//			printWriter.write("<input type=\"text\" name=\"xml/html\" value=\"\"><br>");
//			printWriter.write("<br>");
//			printWriter.write("<input type=\"submit\" value=\"Submit\">");
//			printWriter.write("</form>");
//			
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("text/html");
		try {
			
			PrintWriter printWriter = response.getWriter();
			printWriter.write("<form action=\"handler\" method = \"post\">");
			printWriter.write("XPath:<br>");
			printWriter.write("<input type=\"text\" name=\"xpath\" value=\"\"><br>");
			printWriter.write("xml/html:<br>");
			printWriter.write("<input type=\"text\" name=\"xml_html\" value=\"\"><br>");
			printWriter.write("<br>");
			printWriter.write("<input type=\"submit\" value=\"Submit\">");
			printWriter.write("</form>");
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	}

}









