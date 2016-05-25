package edu.upenn.cis455.storage;

import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class Robot {
	
	@PrimaryKey
	private String domain;
	private Long lastCrawl;
	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}
	/**
	 * @param domain the domain to set
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}
	/**
	 * @return the lastCrawl
	 */
	public Long getLastCrawl() {
		return lastCrawl;
	}
	/**
	 * @param lastCrawl the lastCrawl to set
	 */
	public void setLastCrawl(Long lastCrawl) {
		this.lastCrawl = lastCrawl;
	}
	/**
	 * @return the crawlDelay
	 */
	public Long getCrawlDelay() {
		return CrawlDelay;
	}
	/**
	 * @param crawlDelay the crawlDelay to set
	 */
	public void setCrawlDelay(Long crawlDelay) {
		CrawlDelay = crawlDelay;
	}
	/**
	 * @return the allowedLinks
	 */
	public ArrayList<String> getAllowedLinks() {
		return allowedLinks;
	}
	/**
	 * @param allowedLinks the allowedLinks to set
	 */
	public void setAllowedLinks(ArrayList<String> allowedLinks) {
		this.allowedLinks = allowedLinks;
	}
	/**
	 * @return the disallowedlinks
	 */
	public ArrayList<String> getDisallowedlinks() {
		return disallowedlinks;
	}
	/**
	 * @param disallowedlinks the disallowedlinks to set
	 */
	public void setDisallowedlinks(ArrayList<String> disallowedlinks) {
		this.disallowedlinks = disallowedlinks;
	}
	private Long CrawlDelay;
	private ArrayList<String> allowedLinks;
	private ArrayList<String> disallowedlinks;
}
