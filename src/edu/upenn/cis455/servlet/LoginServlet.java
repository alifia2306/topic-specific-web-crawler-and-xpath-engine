package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.SimpleDA;

public class LoginServlet extends HttpServlet {

	DBWrapper dbWrapper = new DBWrapper();

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
		writer.write(LOGIN_PAGE);
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
		response.setContentType("text/html");
		PrintWriter writer = response.getWriter();

		String userName = request.getParameter("userName");
		String password = request.getParameter("password");

		String databaseDirectory = getServletConfig().getServletContext()
				.getInitParameter("mydb");

		dbWrapper.initializeDB(databaseDirectory);

		if (userName.trim().equals("")) {
			writer.write(LOGIN_PAGE_EMPTY_USERNAME);
			writer.flush();

			return;
		}
		if (!doesUsernameExist(userName)) {
			writer.write(LOGIN_PAGE_WRONG_USERNAME);
			writer.flush();

			return;

		} else if (!doesPasswordMatch(userName, password)) {
			writer.write(LOGIN_PAGE_WRONG_PASSWORD);
			writer.flush();

			return;
		} else {
			HttpSession session = request.getSession();
			session.setAttribute("userName", userName);
			dbWrapper.closeDB();
			response.sendRedirect("homePage");
		}
	}

	/**
	 * Method to check if username already exists
	 * 
	 * @param userName
	 * @return
	 */
	public boolean doesUsernameExist(String userName) {
		SimpleDA indices = new SimpleDA(dbWrapper.store);
		try {
			String dbUserName = indices.primaryIndexUser.get(userName)
					.getUsername();
		} catch (NullPointerException e) {
			return false;
		}
		return true;
	}

	/**
	 * Method to check if passwords match
	 * 
	 * @param userName
	 * @param password
	 * @return
	 */
	public boolean doesPasswordMatch(String userName, String password) {
		SimpleDA indices = new SimpleDA(dbWrapper.store);

		String dbPassword = indices.primaryIndexUser.get(userName)
				.getPassword();
		if (password.trim().equals(dbPassword.trim())) {
			return true;
		} else {
			return false;
		}
	}

	public static final String LOGIN_PAGE = "<head>\n<font color=\"blue\">Login Page</font>\n<br>\n<br>\n</head>"
			+ "<br>\n<br>\n</head>\n<body>\n"
			+ "<form action=\"login\" method = \"post\">\n   Username:<br>\n  "
			+ "<input type=\"text\" name=\"userName\" value=\"\">\n  <br>\n  "
			+ "Password:<br>\n  <input type=\"text\" name=\"password\" value=\"\">\n"
			+ "  <br><br>\n  <input type=\"submit\" value=\"Submit\">\n  <p>\n	"
			+ "Do not have an account?  <a href=\"new_account\">Create Account"
			+ ".</a>\n  </p>\n</form>\n</body>";

	public static final String LOGIN_PAGE_EMPTY_USERNAME = "<html><body><p>Empty UserName</p>"
			+ "<a href=\"homePage\">Go to home page</a><br/>"
			+ "<a href=\"new_account\">Create a new Account</a><br/>"
			+ "</body></html>";

	public static final String LOGIN_PAGE_WRONG_USERNAME = "<html><body><p>Wrong UserName</p>"
			+ "<a href=\"homePage\">Go to home page</a><br/>"
			+ "<a href=\"login\">Go to login page</a><br/>"
			+ "<a href=\"new_account\">Create a new Account</a><br/>"
			+ "</body></html>";

	public static final String LOGIN_PAGE_WRONG_PASSWORD = "<html><body><p>Wrong Password</p>"
			+ "<a href=\"homePage\">Go to home page</a><br/>"
			+ "<a href=\"login\">Go to login page</a><br/>"
			+ "<a href=\"new_account\">Create a new Account</a><br/>"
			+ "</body></html>";

}
