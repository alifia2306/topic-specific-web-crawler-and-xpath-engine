package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sleepycat.persist.EntityCursor;

import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.SimpleDA;

public class AllChannelsServlet extends HttpServlet {
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
		String dbDirectory = getServletContext().getInitParameter("mydb");
		dbWrapper = new DBWrapper();
		dbWrapper.initializeDB(dbDirectory);
		indices = new SimpleDA(dbWrapper.store);
		EntityCursor<Channel> channels = indices.primaryIndexChannel.entities();
		ArrayList<Channel> ChannelsList = new ArrayList<Channel>();
		for (Channel channel : channels) {
			ChannelsList.add(channel);
		}
		channels.close();
		PrintWriter writer = response.getWriter();
		response.setContentType("text/html");
		writer.write("<head>\n<font color=\"green\">Channels Available</font><br><br>\n</head><body>");
		for (Channel channel : ChannelsList) {
			writer.write("<p>\n<a href=\"display?channelname="
					+ channel.getChannelName() + "\">"
					+ channel.getChannelName() + "</a>\n</p>");
		}
		writer.write("</body>");
		writer.flush();
	}
}
