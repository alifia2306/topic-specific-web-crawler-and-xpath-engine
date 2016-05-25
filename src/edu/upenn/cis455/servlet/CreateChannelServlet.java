package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sleepycat.je.Environment;
import com.sleepycat.je.Transaction;

import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.SimpleDA;
import edu.upenn.cis455.storage.User;
import edu.upenn.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;

public class CreateChannelServlet extends HttpServlet {
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
		PrintWriter writer = response.getWriter();
		response.setContentType("text/html");
		writer.write(CREATE_CHANNEL_PAGE);
		writer.flush();
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		PrintWriter writer = response.getWriter();
		response.setContentType("text/html");
		HttpSession session = request.getSession();
		if (session == null) {
			writer.write(LOGIN_REQUIRED);
			writer.flush();
			return;
		}
		String userName = (String) session.getAttribute("userName");

		if (userName == null) {
			writer.write(LOGIN_REQUIRED);
			writer.flush();
			return;
		}

		String channelName = "";
		try {
			channelName = request.getParameter("channelName");
		} catch (NullPointerException e) {
			writer.write(EMPTY_FIELD);
			writer.flush();
			return;
		}

		// System.out.println("channelName :   " + channelName);
		String xpaths = request.getParameter("xpaths");
		if (channelName == null || xpaths == null) {
			writer.write(EMPTY_FIELD);
			writer.flush();
			return;
		}
		dbWrapper = new DBWrapper();
		String directory = request.getSession().getServletContext()
				.getInitParameter("mydb");
		dbWrapper.initializeDB(directory);
		indices = new SimpleDA(dbWrapper.store);
		if (isChannelNameTaken(channelName)) {
			writer.write(CHANNEL_NAME_TAKEN);
			writer.flush();
			return;
		}

		// Assuming that xpaths are seperated by a semi-colon.
		String[] xpathsArray;
		if (!xpaths.contains(";")) {
			xpathsArray = new String[] { xpaths };
		} else {
			xpathsArray = xpaths.split(";");
		}

		XPathEngineImpl xPathEngine = (XPathEngineImpl) XPathEngineFactory
				.getXPathEngine();
		xPathEngine.setXPaths(xpathsArray);
		for (int i = 0; i < xpathsArray.length; i++) {
			if (!xPathEngine.isValid(i)) {
				writer.write(XPATHS_NOT_VALID);
				writer.flush();
				return;
			}
		}
		User user = indices.primaryIndexUser.get(userName);
		ArrayList<String> userChannels = user.getUserChannels();
		userChannels.add(channelName);
		user.setUserChannels(userChannels);
		Environment envmnt = dbWrapper.myEnv;
		Transaction transaction = envmnt.beginTransaction(null, null);
		try {
			indices.primaryIndexUser.put(user);
			transaction.commit();
		} catch (Exception e) {
			e.printStackTrace();
			if (transaction != null) {
				transaction.abort();
				transaction = null;
			}
		}

		Channel channel = new Channel();
		channel.setUserName(userName);
		channel.setChannelName(channelName);
		channel.setXpaths(new ArrayList<String>(Arrays.asList(xpaths)));
		channel.setUrlsMatched(new ArrayList<String>());
		Transaction transaction2 = envmnt.beginTransaction(null, null);
		try {
			indices.primaryIndexChannel.put(channel);
			transaction2.commit();
		} catch (Exception e) {
			e.printStackTrace();
			if (transaction2 != null) {
				transaction2.abort();
				transaction2 = null;
			}
		}
		dbWrapper.closeDB();
		writer.write(CHANNEL_CREATED);
		writer.flush();
	}

	/**
	 * To check if channel name is taken
	 * 
	 * @param channelName
	 * @return
	 */
	public boolean isChannelNameTaken(String channelName) {
		try {
			String channel = indices.primaryIndexChannel.get(channelName)
					.getChannelName();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static String CREATE_CHANNEL_PAGE = "<head>\n<font color=\"blue\">Create Channel</font>\n<br>\n<br>"
			+ "\n</head>\n<body>\n<form action=\"create\" method = \"post\">\n  Channel Name:<br>\n "
			+ " <input type=\"text\" name=\"channelName\" value=\"\">\n  <br>\n  "
			+ "Xpaths:<br>\n "
			+ " <input type=\"text\" name=\"xpaths\" value=\"\">\n  <br>\n"
			+ "\n  <input type=\"submit\" "
			+ "value=\"CREATE\">\n</form>\n</body>";

	public static final String LOGIN_REQUIRED = "<html><body><p>Login Required to Add Channels</p>"
			+ "<a href=\"homePage\">Go to home page</a><br/>"
			+ "<a href=\"new_account\">Create a new Account</a><br/>"
			+ "<a href=\"login\">Login</a><br/>" + "</body></html>";

	public static final String EMPTY_FIELD = "<html><body><p>Please Enter both fields!</p>"
			+ "<a href=\"homePage\">Go to home page</a><br/>"
			+ "<a href=\"new_account\">Create a new Account</a><br/>"
			+ "<a href=\"login\">Login</a><br/>" + "</body></html>";

	public static final String CHANNEL_NAME_TAKEN = "<html><body><p>Channel Name Already Taken!</p>"
			+ "<a href=\"homePage\">Go to home page</a><br/>"
			+ "<a href=\"new_account\">Create a new Account</a><br/>"
			+ "<a href=\"login\">Login</a><br/>" + "</body></html>";

	public static final String XPATHS_NOT_VALID = "<html><body><p>One or more xpaths is not valid</p>"
			+ "<a href=\"homePage\">Go to home page</a><br/>"
			+ "<a href=\"new_account\">Create a new Account</a><br/>"
			+ "<a href=\"login\">Login</a><br/>" + "</body></html>";

	public static final String CHANNEL_CREATED = "<html><body><p>Channel Created Successfully.</p>"
			+ "<a href=\"homePage\">Go to home page</a><br/>"
			+ "<a href=\"new_account\">Create a new Account</a><br/>"
			+ "</body></html>";

}
