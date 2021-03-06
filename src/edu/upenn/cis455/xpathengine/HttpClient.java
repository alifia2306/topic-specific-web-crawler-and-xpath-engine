package edu.upenn.cis455.xpathengine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.StringTokenizer;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Client to fetch contents of URL
 */
public class HttpClient {

	String host;
	URL url;
	PrintWriter output = null;
	BufferedReader input = null;
	String responseHeader = null;
	String responseBody = null;
	int statusCode;
	String contentType = "";
	String urlString;

	public HttpClient(String urlString) throws IOException {
		this.urlString = urlString;
		this.url = new URL(urlString);
		if (url.getProtocol().equalsIgnoreCase("https")) {
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			statusCode = conn.getResponseCode();
			contentType = conn.getContentType();
			input = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
		} else {
			try {
				int port = url.getPort();
				if (port <= 0)
					port = 80;
				Socket clientSocket = new Socket(url.getHost(), port);
				clientSocket.setSoTimeout(10000);
				output = new PrintWriter(clientSocket.getOutputStream(), true);
				input = new BufferedReader(new InputStreamReader(
						clientSocket.getInputStream()));
				String message = "GET " + urlString + " HTTP/1.0\r\nHost: "
						+ url.getHost() + "\r\n\r\n";
				output.println(message);
				String responseLine = input.readLine();
				StringTokenizer tokens = new StringTokenizer(responseLine);
				String httpversion = tokens.nextToken();
				statusCode = Integer.parseInt(tokens.nextToken());
				while ((responseLine = input.readLine()).length() != 0) {
					if (responseLine.toLowerCase().contains("content-type")) {
						contentType = responseLine.split(":")[1].split(";")[0];
					}
					responseHeader += responseLine + "\n";
				}
			} catch (IOException e) {
				System.err
						.println("Error creating ClientSocket to remote host "
								+ urlString);
				e.printStackTrace();
			}
		}
	}

	public String getHeaders() {
		return responseHeader;
	}

	public BufferedReader getReader() {
		return input;
	}

	public String getBody() throws Exception {
		if (statusCode == 200)
			return responseBody;
		else
			throw new Exception("Invalid request. Status code: " + statusCode);
	}

	/**
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public Document getDocument() throws ParserConfigurationException,
			SAXException, IOException {
		Document doc = null;
		String resourcetype = null;
		
		if (contentType != null)
			resourcetype = contentType;
		else if (urlString.endsWith("html") || urlString.endsWith("htm")) {
			resourcetype = "text/html";
		} else if (urlString.endsWith("xml")) {
			resourcetype = "text/xml";
		}
		if (resourcetype.contains("html")) {
			Tidy tidy = new Tidy();
			tidy.setXHTML(true);
			tidy.setTidyMark(false);
			tidy.setShowWarnings(false);
			tidy.setQuiet(true);
			doc = tidy.parseDOM(new BufferedReader(getReader()), null);
		} else if (resourcetype.contains("xml")) {
			DocumentBuilder db = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new BufferedReader(getReader()));
			doc = db.parse(is);
		}
		return doc;
	}
}