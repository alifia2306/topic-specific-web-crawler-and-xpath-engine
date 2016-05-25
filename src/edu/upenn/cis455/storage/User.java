package edu.upenn.cis455.storage;

import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class User {

	@PrimaryKey
	String username;
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	/**
	 * @return the name
	 */
	public String getFirstName() {
		return firstName;
	}
	/**
	 * @param name the name to set
	 */
	public void setFirstName(String name) {
		this.firstName = name;
	}
	
	/**
	 * @return the name
	 */
	public String getLastName() {
		return lastName;
	}
	/**
	 * @param name the name to set
	 */
	public void setLastName(String name) {
		this.lastName = name;
	}
	
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 * @return the userChannels
	 */
	public ArrayList<String> getUserChannels() {
		return userChannels;
	}
	/**
	 * @param userChannels the userChannels to set
	 */
	public void setUserChannels(ArrayList<String> userChannels) {
		this.userChannels = userChannels;
	}
	String firstName;
	String lastName;
	String password;
	ArrayList<String> userChannels = new ArrayList<>();
}
