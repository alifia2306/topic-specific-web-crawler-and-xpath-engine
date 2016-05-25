package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.upenn.cis455.xpathengine.HttpClient;
import edu.upenn.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;

public class XPathHandlerServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/*
	 * doPost for HttpServlet class
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		PrintWriter out = response.getWriter();
		String xpath = request.getParameter("xpath");
		String xml_htmlUrl = request.getParameter("xml_html");

		if (xpath.trim().equals("") || xml_htmlUrl.trim().equals("")) {
			out.println("<html><body><h3>Please fill both xpath and url.</h3></body></html>");
			return;
		}

		xpath = URLDecoder.decode(xpath, "UTF-8");
		xml_htmlUrl = URLDecoder.decode(xml_htmlUrl, "UTF-8");

		System.out.println("xpath = " + xpath);

		System.out.println("url = " + xml_htmlUrl);
		// DocumentBuilderFactory factory =
		// DocumentBuilderFactory.newInstance();
		//
		// DocumentBuilder builder = null;
		// Document document = null;
		// try {
		// builder = factory.newDocumentBuilder();
		// document = builder.parse(xml_htmlUrl);
		// } catch (ParserConfigurationException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
		//
		// catch (SAXException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		HttpClient client = new HttpClient(xml_htmlUrl);
		Document document = null;
		try {
			document = client.getDocument(); // Get the doc using the HTTP
												// client
		} catch (ParserConfigurationException | SAXException e1) {
			e1.printStackTrace();
		}

		XPathEngineImpl xEngine = (XPathEngineImpl) XPathEngineFactory
				.getXPathEngine();
		String[] xpaths = xpath.split(";");
		xEngine.setXPaths(xpaths);
		out.println("<html><body><head><meta charset='utf-8'/><h3>Results</h3></head>");
		if (document == null) {
			out.println("<br>File does not exist!");
		} else {
			out.println("<table border = 1 cellpadding = 10><tr><th>xpath</th><th>Matching</th></tr>");
			boolean[] results = xEngine.evaluate(document);
			for (int i = 0; i < results.length; i++) {
				if (results[i])
					out.println("<tr><td rowspan = 2>" + xpaths[i]
							+ "</td><td rowspan = 2>Matched</td><tr>");
				else
					out.println("<tr><td rowspan = 2>" + xpaths[i]
							+ "</td><td rowspan = 2>Did not Match</td><tr>");
			}
			out.println("</table></body></html>");
		}
	}
}
