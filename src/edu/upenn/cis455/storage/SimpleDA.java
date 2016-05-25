package edu.upenn.cis455.storage;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

public class SimpleDA {
	public PrimaryIndex<String, User> primaryIndexUser;
	public PrimaryIndex<String, Channel> primaryIndexChannel;
	public PrimaryIndex<String, CrawledLinks> primaryIndexCrawledLinks;
	public PrimaryIndex<String, Robot> primaryIndexRobot;

	/**
	 * Opening the indices
	 * 
	 * @param store
	 * @throws DatabaseException
	 */
	public SimpleDA(EntityStore store) throws DatabaseException {

		// Primary keys for all classes
		primaryIndexUser = store.getPrimaryIndex(String.class, User.class);
		primaryIndexChannel = store
				.getPrimaryIndex(String.class, Channel.class);
		primaryIndexCrawledLinks = store.getPrimaryIndex(String.class,
				CrawledLinks.class);
		primaryIndexRobot = store.getPrimaryIndex(String.class, Robot.class);

	}
}
