package edu.upenn.cis455.storage;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class CrawledLinks {
	
	@PrimaryKey
	private String url;
	private Long lastCrawled;
	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	/**
	 * @return the lastCrawled
	 */
	public Long getLastCrawled() {
		return lastCrawled;
	}
	/**
	 * @param lastCrawled the lastCrawled to set
	 */
	public void setLastCrawled(Long lastCrawled) {
		this.lastCrawled = lastCrawled;
	}
	/**
	 * @return the pageContents
	 */
	public String getPageContents() {
		return pageContents;
	}
	/**
	 * @param pageContents the pageContents to set
	 */
	public void setPageContents(String pageContents) {
		this.pageContents = pageContents;
	}
	/**
	 * @return the contentType
	 */
	public String getContentType() {
		return contentType;
	}
	/**
	 * @param contentType the contentType to set
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	private String pageContents;
	private String contentType;
}
