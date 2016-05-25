package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.CrawledLinks;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.SimpleDA;

public class DisplaySelectedChannelServlet extends HttpServlet {

	DBWrapper dbWrapper;
	SimpleDA indices;

	/*
	 * doGet for HttpServlet class
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		dbWrapper = new DBWrapper();
		String dbDirectory = getServletContext().getInitParameter("mydb");
		dbWrapper.initializeDB(dbDirectory);
		indices = new SimpleDA(dbWrapper.store);
		String channelName = request.getParameter("channelname");
		Channel channel = indices.primaryIndexChannel.get(channelName);
		ArrayList<String> urlList = channel.getUrlsMatched();
		PrintWriter writer = response.getWriter();
		response.setContentType("text/html");
		if (urlList.isEmpty()) {
			writer.write("<body><p>Did not match anything</p></body>");
		} else {
			for (String url : urlList) {
				CrawledLinks crawledLinks = indices.primaryIndexCrawledLinks
						.get(url);
				String contents = crawledLinks.getPageContents();
				long lastCrawledLong = crawledLinks.getLastCrawled();
				SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
				String timeCrawled = timeFormat.format(lastCrawledLong);
				SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-DD");
				String dateCrawled = dateFormat.format(lastCrawledLong);
				String lastCrawled = dateCrawled + "T" + timeCrawled;
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append("<body>");
				stringBuilder.append("<div>\nCrawled on: " + lastCrawled
						+ "\n</div><br><br>\n");
				stringBuilder.append("<div>\nLocation: " + url
						+ "\n</div><br><br>\n");
				stringBuilder.append("<div>\n" + "<xmp>" + contents + "</xmp>"
						+ "\n</div><br><br>\n");
				stringBuilder.append("</body>");
				writer.write(stringBuilder.toString());
			}
			writer.close();
			dbWrapper.closeDB();
		}
	}
}
