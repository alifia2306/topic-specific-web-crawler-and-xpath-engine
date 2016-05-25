package edu.upenn.cis455.storage;

import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class Channel {
	
	@PrimaryKey
	String channelName;
	String userName;
	/**
	 * @return the channelName
	 */
	public String getChannelName() {
		return channelName;
	}
	/**
	 * @param channelName the channelName to set
	 */
	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}
	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}
	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	/**
	 * @return the urlsMatched
	 */
	public ArrayList<String> getUrlsMatched() {
		return urlsMatched;
	}
	/**
	 * @param urlsMatched the urlsMatched to set
	 */
	public void setUrlsMatched(ArrayList<String> urlsMatched) {
		this.urlsMatched = urlsMatched;
	}
	/**
	 * @return the xpaths
	 */
	public ArrayList<String> getXpaths() {
		return xpaths;
	}
	/**
	 * @param xpaths the xpaths to set
	 */
	public void setXpaths(ArrayList<String> xpaths) {
		this.xpaths = xpaths;
	}
	ArrayList<String> urlsMatched = new ArrayList<>();
	ArrayList<String> xpaths = new ArrayList<>();
	
	
}
