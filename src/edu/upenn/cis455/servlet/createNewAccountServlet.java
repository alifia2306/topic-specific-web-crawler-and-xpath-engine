package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sleepycat.je.Environment;
import com.sleepycat.je.Transaction;

import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.SimpleDA;
import edu.upenn.cis455.storage.User;

public class createNewAccountServlet extends HttpServlet {
	DBWrapper dbWrapper = new DBWrapper();
	SimpleDA indices = null;

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
		writer.write(Account_PAGE);
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
		String firstName = request.getParameter("firstName");
		String lastName = request.getParameter("lastName");
		String userName = request.getParameter("userName");
		String password = request.getParameter("password");
		String repeatPassword = request.getParameter("repeatPassword");
		String databaseDirectory = request.getSession().getServletContext()
				.getInitParameter("mydb");
		dbWrapper.initializeDB(databaseDirectory);
		response.setContentType("text/html");
		PrintWriter writer = response.getWriter();
		if (isUserNameDuplicate(userName)) {
			writer.write(DUPLICATE_USERNAME);
			return;
		} else if (!password.equals(repeatPassword)) {
			writer.write(PASSWORD_MISMATCH);
			return;
		} else {
			Environment envmnt = dbWrapper.myEnv;
			User user = new User();
			user.setFirstName(firstName);
			user.setLastName(lastName);
			user.setUsername(userName);
			user.setPassword(password);
			user.setUserChannels(new ArrayList());
			Transaction trans = envmnt.beginTransaction(null, null);
			try {
				indices = new SimpleDA(dbWrapper.store);
				indices.primaryIndexUser.put(user);
				trans.commit();
			} catch (Exception e1) {
				e1.printStackTrace();
				if (trans != null) {
					trans.abort();
					trans = null;
				}
			}
			dbWrapper.closeDB();
			response.sendRedirect("homePage");
		}
	}

	/**
	 * CHecks if userName is duplicate
	 * 
	 * @return
	 */
	public boolean isUserNameDuplicate(String userName) {

		try {
			indices = new SimpleDA(dbWrapper.store);
			String username = indices.primaryIndexUser.get(userName)
					.getUsername();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static String Account_PAGE = "<head>\n<font color=\"blue\">"
			+ "Create Account Page</font>\n<br>\n<br>\n</head>\n<body>\n"
			+ "<form action=\"new_account\" method = \"post\">\n  "
			+ "First Name:<br>\n  <input type=\"text\" name=\"firstName\" value=\"\">\n "
			+ " <br>\n  Last name:<br>\n  <input type=\"text\" name=\"lastName\" value=\"\">\n"
			+ "  <br>\n  User Name:<br>\n  <input type=\"text\" name=\"userName\" value=\"\">\n  <"
			+ "br>\n  Password:<br>\n  <input type=\"text\" name=\"password\" value=\"\">\n  <br><br>\n "
			+ " Repeat Password:<br>\n  <input type=\"text\" name=\"repeatPassword\" value=\"\">\n  <br><br>\n  "
			+ "<input type=\"submit\" value=\"Submit\">\n</form>\n</body>";

	public static final String DUPLICATE_USERNAME = "<html><body><p>UserName Already Taken!</p>"
			+ "<a href=\"homePage\">Go to home page</a><br/>"
			+ "<a href=\"new_account\">Create a new Account</a><br/>"
			+ "</body></html>";

	public static final String PASSWORD_MISMATCH = "<html><body><p>Passwords Don't Match!</p>"
			+ "<a href=\"homePage\">Go to home page</a><br/>"
			+ "<a href=\"new_account\">Create a new Account</a><br/>"
			+ "</body></html>";
}
