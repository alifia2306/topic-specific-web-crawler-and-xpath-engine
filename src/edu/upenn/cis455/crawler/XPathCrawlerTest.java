package edu.upenn.cis455.crawler;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import edu.upenn.cis455.storage.CrawledLinks;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.Robot;
import edu.upenn.cis455.storage.SimpleDA;

public class XPathCrawlerTest {
	DBWrapper dbWrapper;
	SimpleDA indices;
	Long lastCrawled = new Date().getTime();

	@Before
	public void setup() {
		dbWrapper = new DBWrapper();
		dbWrapper.initializeDB("./storage");
		indices = new SimpleDA(dbWrapper.store);
	}

	/**
	 * Test for main URL
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testPageUrl() throws InterruptedException {
		String pageUrl = "https://dbappserv.cis.upenn.edu/crawltest.html";
		String dbDirectory = "./storage";
		int maxFiles = 1000;
		int maxDocSize = 80;
		String args[] = { pageUrl, dbDirectory, maxFiles + "", maxDocSize + "" };
		XPathCrawler.main(args);
		Thread.sleep(10000);
		dbWrapper = new DBWrapper();
		dbWrapper.initializeDB("./storage");
		indices = new SimpleDA(dbWrapper.store);
		CrawledLinks links = indices.primaryIndexCrawledLinks
				.get("https://dbappserv.cis.upenn.edu/crawltest.html");
		Robot robot = indices.primaryIndexRobot.get("dbappserv.cis.upenn.edu");
		assertEquals(links.getUrl(), pageUrl);
		dbWrapper.closeDB();
	}

	/**
	 * Test for other URL
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testOtherUrl() throws InterruptedException {
		String pageUrl = "https://dbappserv.cis.upenn.edu/crawltest/nytimes/National.xml";
		String dbDirectory = "./storage";
		int maxFiles = 1000;
		int maxDocSize = 80;
		String args[] = { pageUrl, dbDirectory, maxFiles + "", maxDocSize + "" };
		XPathCrawler.main(args);
		Thread.sleep(10000);
		dbWrapper = new DBWrapper();
		dbWrapper.initializeDB("./storage");
		indices = new SimpleDA(dbWrapper.store);
		CrawledLinks links = indices.primaryIndexCrawledLinks
				.get("https://dbappserv.cis.upenn.edu/crawltest/nytimes/National.xml");
		Robot robot = indices.primaryIndexRobot.get("dbappserv.cis.upenn.edu");
		assertEquals(links.getUrl(), pageUrl);
		dbWrapper.closeDB();
	}

}
