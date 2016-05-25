package edu.upenn.cis455.storage;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class DBTest {
	DBWrapper dbWrapper;
	SimpleDA indices;
	Long lastCrawled = new Date().getTime();
	
	/**
	 * Setup method
	 */
	@Before
	public void setup(){
		dbWrapper = new DBWrapper();
		dbWrapper.initializeDB("./storage");
		indices =  new SimpleDA(dbWrapper.store);
		
	}
	
	/**
	 * Tests adding User
	 */
	@Test
	public void testAddUser() {
		User user = new User();
		user.setFirstName("James");
		user.setLastName("Bond");
		user.setUsername("JB");
		user.setPassword("007");
		indices.primaryIndexUser.put(user);
		assertNotNull(indices.primaryIndexUser.get("JB"));
	}
	
	/**
	 * Tests getting User
	 */
	@Test
	public void testGetUser() {
		User user = indices.primaryIndexUser.get("JB");
		assertEquals("James",user.getFirstName());
		assertEquals("Bond", user.getLastName());
		assertEquals("JB", user.getUsername());
		assertEquals("007", user.getPassword());
	}
	
	/**
	 * Tests adding Links
	 */
	@Test
	public void testAddCrawledLinks() {
		CrawledLinks links = new CrawledLinks();
		links.setContentType("text/html");
		links.setLastCrawled(lastCrawled);
		links.setPageContents("<html><head>Test Content</head></html>");
		links.setUrl("http://www.myhost.com");
		indices.primaryIndexCrawledLinks.put(links);
		assertNotNull(indices.primaryIndexCrawledLinks.get("http://www.myhost.com"));
	}
	
	/**
	 * Tests getting Links
	 */
	@Test
	public void testGetCrawledLinks() {
		CrawledLinks crawledLinks = indices.primaryIndexCrawledLinks.get("http://www.myhost.com");
		assertEquals("text/html", crawledLinks.getContentType());
		assertEquals("<html><head>Test Content</head></html>", crawledLinks.getPageContents());
		assertEquals("http://www.myhost.com", crawledLinks.getUrl());
	}

}
