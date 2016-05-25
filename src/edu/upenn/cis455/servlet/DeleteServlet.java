package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sleepycat.je.Environment;
import com.sleepycat.je.Transaction;

import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.SimpleDA;
import edu.upenn.cis455.storage.User;

public class DeleteServlet extends HttpServlet {
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
		writer.write(DELETE_CHANNEL);
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

		if (channelName == null) {
			writer.write(EMPTY_FIELD);
			writer.flush();
			return;
		}
		dbWrapper = new DBWrapper();
		String directory = request.getSession().getServletContext()
				.getInitParameter("mydb");
		dbWrapper.initializeDB(directory);
		indices = new SimpleDA(dbWrapper.store);
		User user = indices.primaryIndexUser.get(userName);
		ArrayList<String> userChannels = user.getUserChannels();
		if (!userChannels.contains(channelName)) {
			writer.write(CHANNEL_NOT_PRESENT);
			writer.flush();
			return;
		} else
			userChannels.remove(channelName);
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
		indices.primaryIndexChannel.delete(channelName);
		dbWrapper.closeDB();
		writer.write(CHANNEL_DELETED);
		writer.flush();

	}

	/**
	 * Method to check if channel is already present.
	 * 
	 * @param channelName
	 * @return
	 */
	public boolean isChannelPresent(String channelName) {
		try {
			String channel = indices.primaryIndexChannel.get(channelName)
					.getChannelName();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static String DELETE_CHANNEL = "<head>\n<font color=\"blue\">Delete Channel</font>\n<br>\n<br>"
			+ "\n</head>\n<body>\n<form action=\"delete\" method = \"post\">\n  Channel Name:<br>\n "
			+ " <input type=\"text\" name=\"channelName\" value=\"\">\n  <br>\n  "
			+ "\n  <input type=\"submit\" "
			+ "value=\"DELETE\">\n</form>\n</body>";

	public static final String LOGIN_REQUIRED = "<html><body><p>Login Required to Delete Channels</p>"
			+ "<a href=\"homePage\">Go to home page</a><br/>"
			+ "<a href=\"new_account\">Create a new Account</a><br/>"
			+ "<a href=\"login\">Login</a><br/>" + "</body></html>";

	public static final String EMPTY_FIELD = "<html><body><p>Please Enter Channel Name</p>"
			+ "<a href=\"homePage\">Go to home page</a><br/>"
			+ "<a href=\"new_account\">Create a new Account</a><br/>"
			+ "<a href=\"login\">Login</a><br/>" + "</body></html>";

	public static final String CHANNEL_NOT_PRESENT = "<html><body><p>Channel Not Present or not created by you!</p>"
			+ "<a href=\"homePage\">Go to home page</a><br/>"
			+ "<a href=\"new_account\">Create a new Account</a><br/>"
			+ "<a href=\"login\">Login</a><br/>" + "</body></html>";

	public static final String CHANNEL_DELETED = "<html><body><p>Channel Deleted Successfully.</p>"
			+ "<a href=\"homePage\">Go to home page</a><br/>"
			+ "<a href=\"new_account\">Create a new Account</a><br/>"
			+ "</body></html>";
}
