package edu.upenn.cis455.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;
import java.util.TimeZone;
import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityCursor;

import edu.upenn.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.CrawledLinks;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.Robot;
import edu.upenn.cis455.storage.SimpleDA;
import edu.upenn.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;

public class XPathCrawler {
	private static String pageUrl;
	private static String dbDirectory;
	private static int maxFiles;
	private static int maxDocSize;
	private DBWrapper berkeleydb;
	private SimpleDA indices;
	private Environment envmnt;
	private static Queue<String> urlsList;
	private static ArrayList<String> urlsSeen;

	/**
	 * XpathConstructor
	 */
	XPathCrawler() {
		urlsList = new LinkedList<>();
		urlsSeen = new ArrayList<String>();
	}

	/**
	 * Main method takes 3-4 arguments pageUrl, dbDirectory, maxDocSize,
	 * maxFiles
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		// Wrong number of arguments
		if (args.length < 3 || args.length > 4) {
			System.out.println("Name: Alifia Haidry");
			System.out.println("Seas id: ahaidry");
			System.exit(0);
		}

		pageUrl = args[0];
		dbDirectory = args[1];
		maxDocSize = Integer.parseInt(args[2]) * 1048576; // In bits
		maxFiles = 100000;

		if (args.length == 4) {
			maxFiles = Integer.parseInt(args[3]);
		}

		XPathCrawler crawler = new XPathCrawlerFactory().getCrawler();
		urlsList.clear();
		urlsSeen.clear();
		urlsList.add(pageUrl);
		crawler.openDatabase();
		crawler.startCrawler();
		crawler.closeDatabase();
	}

	/**
	 * Method to open DB
	 */
	public void openDatabase() {
		berkeleydb = new DBWrapper();
		berkeleydb.initializeDB(dbDirectory);
		indices = new SimpleDA(berkeleydb.store);
		envmnt = berkeleydb.myEnv;
	}

	/**
	 * Method to close DB
	 */
	public void closeDatabase() {
		berkeleydb.closeDB();
	}

	/**
	 * Method to start Crawler
	 */
	public void startCrawler() {
		int filesCrawled = 0;
		while (filesCrawled < maxFiles && !urlsList.isEmpty()) {
			try {

				String currentUrl = urlsList.remove();
				boolean crawledAlready = false;
				long lastCrawled = 0;
				// If empty url then go to next iteration.
				if (currentUrl.trim().length() == 0) {
					continue;
				}

				// If url is seen in this crawl, set crawledAlready to true for
				// not modified check
				if (urlsSeen.contains(currentUrl.trim())) {
					System.out.println(currentUrl
							+ ": Crawled Before in this crawling Session.");
					// continue;
					crawledAlready = true;
				}

				URL currentURL = new URL(currentUrl);

				// If disallowed by robots.txt
				if (!isRobotAllowed(currentUrl)) {
					continue;
				}

				Client client = new Client("head", currentUrl);

				// If crawled already, set if-modified-since request header
				if (isCrawledAlready(currentUrl) || crawledAlready == true) {
					CrawledLinks crawledLinks = indices.primaryIndexCrawledLinks
							.get(currentUrl);
					Long previousCrawlLong = crawledLinks.getLastCrawled();
					Date date = new Date(previousCrawlLong);
					SimpleDateFormat f = new SimpleDateFormat(
							"EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
					f.setTimeZone(TimeZone.getTimeZone("GMT"));
					String previousCrawl = f.format(date).toString();
					client.requestMap.put("if-modified-since", previousCrawl);
					crawledAlready = true;
				}
				client.sendRequest();

				// Unsupported Status Code
				if (!client.responseMap.get("status").equals("200")
						&& !client.responseMap.get("status").equals("304")
						&& !client.responseMap.get("status").equals("307")
						&& !client.responseMap.get("status").equals("301")) {
					System.out.println(currentURL.toString()
							+ ": Unsupported Status Code "
							+ client.responseMap.get("status"));
					continue;
				}

				// Unsupported Content type
				String contentType = client.responseMap.get("content-type");
				if (contentType != null) {
					if (!contentType.contains("xml")
							&& !contentType.contains("html")) {
						System.out.println(currentURL.toString()
								+ ": Unsupported Content-Type "
								+ client.responseMap.get("content-type"));
						continue;
					}
				} else if (!currentUrl.endsWith("/")
						&& !currentUrl.endsWith("html")
						&& !currentUrl.endsWith("htm")
						&& !currentUrl.contains("xml")) {
					System.out.println(currentURL.toString()
							+ ": Unsupported Content-Type "
							+ client.responseMap.get("content-type"));
					continue;
				}

				// Unsupported Content Length
				String len = client.responseMap.get("content-length");
				int contentLength = Integer.parseInt(len);
				if (contentLength > maxDocSize) {
					System.out.println(currentURL.toString()
							+ ": Exceeds maximum document size");
					continue;
				}

				// If redirect request, add to url List
				if (client.responseMap.containsKey("location")) {
					String location = client.responseMap.get("location");
					if (!location.startsWith("http://")
							&& !location.startsWith("https://")) {
						if (location.startsWith("/")) {
							location = currentURL.getProtocol() + "://"
									+ currentURL.getHost() + location;
						} else if (location.startsWith("www")) {
							location = currentURL.getProtocol() + "://"
									+ location;
						} else {
							System.out.println(location + ": Unsupported URL");
							continue;
						}
					}
					System.out.println(currentUrl
							+ ": Redirected to Location -> " + location);
					urlsList.add(location);
					continue;
				}

				// if not modified, extract links if html and update last
				// crawled time.
				if (client.responseMap.get("status").equals("304")
						&& crawledAlready == true) {
					System.out
							.println(currentURL.toString() + ": Not Modified");
					CrawledLinks link = indices.primaryIndexCrawledLinks
							.get(currentUrl);
					String previousCrawlContent = link.getPageContents();
					String conType = link.getContentType();
					if (conType.contains("html")) {
						ArrayList<String> listOfLinks = new ArrayList<String>();
						listOfLinks = getLinksFromHtml(previousCrawlContent,
								currentUrl);
						urlsList.addAll(listOfLinks);
					} else if (conType.contains("xml")) {
						updateChannels(currentUrl);
					}
					link.setLastCrawled(new Date().getTime());
					Transaction transaction = envmnt.beginTransaction(null,
							null);
					indices.primaryIndexCrawledLinks.put(link);
					try {
						transaction.commit();
					} catch (Exception e) {
						if (transaction != null) {
							transaction.abort();
							transaction = null;
						}
					}

					continue;
				}

				// Send Get request
				System.out.println(currentURL.toString() + ": Downloading...");
				Client clientGet = new Client("get", currentUrl);
				clientGet.sendRequest();
				filesCrawled++;
				lastCrawled = new Date().getTime();
				CrawledLinks link = new CrawledLinks();

				// If already crawled but not modified.
				if (crawledAlready == true) {
					link = indices.primaryIndexCrawledLinks.get(currentUrl);
				}

				// If crawled for the first time set contents and commit
				// transaction to db
				else {
					link.setUrl(currentUrl);
					link.setContentType(contentType);
				}
				String body = clientGet.body;
				link.setPageContents(body);
				link.setLastCrawled(lastCrawled);
				Transaction transaction = envmnt.beginTransaction(null, null);
				indices.primaryIndexCrawledLinks.put(link);
				try {
					transaction.commit();
				} catch (Exception e) {
					if (transaction != null) {
						transaction.abort();
						transaction = null;
					}
				}

				// Update last crawled time of the domain
				String domain = currentURL.getHost();
				Robot robot = indices.primaryIndexRobot.get(domain);
				robot.setLastCrawl(lastCrawled);
				Transaction robotTransaction = envmnt.beginTransaction(null,
						null);

				indices.primaryIndexRobot.put(robot);
				try {
					robotTransaction.commit();
				} catch (Exception e) {
					if (robotTransaction != null) {
						robotTransaction.abort();
						robotTransaction = null;
					}
				}

				// Add to seen urls
				if (!crawledAlready) {
					urlsSeen.add(currentUrl);
				}

				// If html extract links
				if (contentType.contains("html")) {
					ArrayList<String> listOfLinks = new ArrayList<String>();
					listOfLinks = getLinksFromHtml(body, currentUrl);
					urlsList.addAll(listOfLinks);
				} else {
					updateChannels(currentUrl);
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	/**
	 * Method to check if robots are cached for the domain
	 * 
	 * @param domain
	 * @return
	 */
	public boolean isRobotsCached(String domain) {
		try {
			indices.primaryIndexRobot.get(domain).getDomain();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Method to check if Url is crawled Already
	 * 
	 * @param url
	 * @return
	 */
	public boolean isCrawledAlready(String url) {
		try {
			indices.primaryIndexCrawledLinks.get(url).getUrl();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Method to check if robots.txt allows url
	 * 
	 * @param url
	 * @return
	 */
	public boolean isRobotAllowed(String url) {
		URL urlLink = null;
		try {
			urlLink = new URL(url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RobotsTxtInfo robotTxtHelp = new RobotsTxtInfo();
		String domain = urlLink.getHost();
		ArrayList<String> allowedUrls = new ArrayList<String>();
		ArrayList<String> disallowedUrls = new ArrayList<String>();
		long lastCrawled = 0;
		long crawlDelay = 0;

		// Add robots to db if not cached
		if (!isRobotsCached(domain)) {
			String requestLink = urlLink.getProtocol() + "://" + domain
					+ "/robots.txt";
			Client client = new Client("GET", requestLink);
			client.sendRequest();
			HashMap<String, ArrayList<String>> defaultMap = new HashMap<String, ArrayList<String>>();
			HashMap<String, ArrayList<String>> cis455Map = new HashMap<String, ArrayList<String>>();
			long crawlDelayDefault = 0;
			long crawlDelayCis455 = 0;
			if (client.responseMap.get("status").equals("200")) {
				BufferedReader reader = new BufferedReader(new StringReader(
						client.body));
				String line = "";
				int flag = 0;
				try {
					while ((line = reader.readLine()) != null) {

						if (line.contains("User-agent")) {
							String agent = line.split(":")[1].trim();
							if (agent.equals("cis455crawler")) {
								flag = 1;
							} else if (agent.equals("*")) {
								flag = 2;
							} else {
								flag = 0;
							}
						} else if (line.contains("Disallow")) {
							String disallowed = line.split(":")[1].trim();
							if (flag == 1) {
								if (cis455Map.containsKey("disallow")) {
									cis455Map.get("disallow").add(disallowed);
								} else {
									ArrayList<String> disallowList = new ArrayList<String>();
									disallowList.add(disallowed);
									cis455Map.put("disallow", disallowList);
								}
							} else if (flag == 2) {
								if (defaultMap.containsKey("disallow")) {
									defaultMap.get("disallow").add(disallowed);
								} else {
									ArrayList<String> disallowList = new ArrayList<String>();
									disallowList.add(disallowed);
									defaultMap.put("disallow", disallowList);
								}
							}
						} else if (line.contains("Allow")) {
							String allowed = line.split(":")[1].trim();
							if (flag == 1) {
								if (cis455Map.containsKey("allow")) {
									cis455Map.get("allow").add(allowed);
								} else {
									ArrayList<String> allowList = new ArrayList<String>();
									allowList.add(allowed);
									cis455Map.put("allow", allowList);
								}
							} else if (flag == 2) {
								if (defaultMap.containsKey("allow")) {
									defaultMap.get("allow").add(allowed);
								} else {
									ArrayList<String> allowList = new ArrayList<String>();
									allowList.add(allowed);
									defaultMap.put("allow", allowList);
								}
							}
						} else if (line.contains("Crawl-delay")) {
							String crawl = line.split(":")[1].trim();
							if (flag == 1) {
								crawlDelayCis455 = Long.parseLong(crawl);
							} else if (flag == 2) {
								crawlDelayDefault = Long.parseLong(crawl);
							}

						}

					}
					if (!cis455Map.isEmpty()) {
						if (cis455Map.containsKey("allow")) {
							allowedUrls = cis455Map.get("allow");
						}
						if (cis455Map.containsKey("disallow")) {
							disallowedUrls = cis455Map.get("disallow");
						}

						crawlDelay = crawlDelayCis455;
					} else if (!defaultMap.isEmpty()) {
						if (defaultMap.containsKey("allow")) {
							allowedUrls = cis455Map.get("allow");
						}
						if (defaultMap.containsKey("disallow")) {
							disallowedUrls = cis455Map.get("disallow");
						}

						crawlDelay = crawlDelayDefault;
					} else {
						System.out
								.println("no robot rules in robot.txt for Domain: "
										+ domain);
						return true;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// If no robots.txt, considering everything allowed.
			else {
				System.out.println("robots.txt missing for Domain: " + domain);
				return true;
			}
			Robot robot = new Robot();
			robot.setDomain(domain);
			robot.setCrawlDelay(crawlDelay);
			robot.setLastCrawl(lastCrawled);
			robot.setAllowedLinks(allowedUrls);
			robot.setDisallowedlinks(disallowedUrls);
			Transaction transaction = envmnt.beginTransaction(null, null);
			try {
				indices.primaryIndexRobot.put(robot);
				transaction.commit();
			} catch (Exception e) {
				e.printStackTrace();
				if (transaction != null) {
					transaction.abort();
					transaction = null;
				}
			}
		} else {
			allowedUrls = indices.primaryIndexRobot.get(domain)
					.getAllowedLinks();
			disallowedUrls = indices.primaryIndexRobot.get(domain)
					.getDisallowedlinks();
			lastCrawled = indices.primaryIndexRobot.get(domain).getLastCrawl();
			crawlDelay = indices.primaryIndexRobot.get(domain).getCrawlDelay();
		}
		long currentDate = new Date().getTime();
		if (currentDate - lastCrawled < crawlDelay * 1000) {
			// System.out.println(url +
			// ": Disallowed by robots due to crawl delay");
			urlsList.add(url);
			return false;
		}
		String path = url.split(domain)[1];
		for (String link : allowedUrls) {
			if (path.startsWith(link)) {
				return true;
			}
		}
		for (String link : disallowedUrls) {
			if (path.startsWith(link) && !link.equals("")) {
				System.out.println(url
						+ ":  Disallowed due to permissions for link ->  "
						+ link);
				return false;
			}
		}

		return true;
	}

	/**
	 * Method to update channels.
	 * 
	 * @param url
	 */
	public void updateChannels(String url) {
		String xmlContents = indices.primaryIndexCrawledLinks.get(url)
				.getPageContents().trim();
		DocumentBuilder dBuilder;
		org.w3c.dom.Document document = null;
		try {
			dBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xmlContents));
			document = dBuilder.parse(is);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (document == null) {
			return;
		}
		ArrayList<Channel> listOfChannels = new ArrayList<Channel>();
		EntityCursor<Channel> channels = indices.primaryIndexChannel.entities();
		for (Channel channel : channels) {
			listOfChannels.add(channel);
		}
		channels.close();
		XPathEngineImpl xpathEngine = (XPathEngineImpl) XPathEngineFactory
				.getXPathEngine();
		for (Channel channel : listOfChannels) {
			ArrayList<String> urlsMatched = channel.getUrlsMatched();
			ArrayList<String> xpaths = channel.getXpaths();
			for (String xpath : xpaths) {
				String[] path = new String[] { xpath };
				xpathEngine.setXPaths(path);
				boolean match = xpathEngine.evaluate(document)[0];
				if (match) {
					if (!urlsMatched.contains(url)) {
						urlsMatched.add(url);
						break;
					}
				}
			}
			channel.setUrlsMatched(urlsMatched);
			Transaction transaction = envmnt.beginTransaction(null, null);
			try {
				indices.primaryIndexChannel.put(channel);
				transaction.commit();
			} catch (Exception e) {
				e.printStackTrace();
				if (transaction != null) {
					transaction.abort();
					transaction = null;
				}
			}

		}
	}

	/**
	 * Method to extract links from html
	 * 
	 * @param content
	 * @param url
	 * @return
	 */
	public ArrayList<String> getLinksFromHtml(String content, String url) {
		Document doc;
		ArrayList<String> hrefUrls = new ArrayList<String>();
		try {
			doc = Jsoup.connect(url).get();
			Elements links = doc.select("a[href]");
			for (Element link : links) {
				hrefUrls.add(link.attr("abs:href"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return hrefUrls;

	}

}
