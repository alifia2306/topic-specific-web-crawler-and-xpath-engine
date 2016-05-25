package edu.upenn.cis455.crawler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.upenn.cis455.crawler.info.URLInfo;

public class Client {
	private String url;
	private String host;
	private int port;
	private String filePath;
	private Socket clientSocket;
	private String method;
	public HashMap<String,String> requestMap;
	public HashMap<String, String> responseMap;
	private PrintWriter output;
	private BufferedReader reader;
	public String body;
	
	/**Constructor for client
	 * @param method
	 * @param url
	 */
	Client(String method, String url){
		
		this.url = url;
		this.method = method;
		requestMap = new HashMap<>();
		responseMap = new HashMap<>();
		requestMap.put("User-agent", "cis455crawler");
		URL urlInfo = null;
		try {
			urlInfo = new URL(url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		host = urlInfo.getHost();
		port = urlInfo.getPort();
		filePath = urlInfo.getFile();
		requestMap.put("Host", host);
		
	}
		
	/**
	 * Method to get buffered Reader
	 * @return
	 */
	public BufferedReader getBufferedReader(){
		return reader;
	}
		
	/**
	 * Method to send HttpRequest
	 */
	private void sendHttpRequest(){
		InetAddress address;
		try {
			address = InetAddress.getByName(host);
			clientSocket = new Socket(address.getHostAddress(), port);
			clientSocket.setSoTimeout(10000);
			output = new PrintWriter(clientSocket.getOutputStream(), true);
			reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String request = method + " " + filePath + " HTTP/1.0\r\n";
		for (Map.Entry<String, String> entry : requestMap.entrySet()) {
			request = request + entry.getKey() + ": " + entry.getValue() + "\r\n";
		}
		request = request +  "\r\n";
		output.println(request);
		String response = "";
		try {
			
			//response for GET
			if(method.equalsIgnoreCase("get")){
				response = reader.readLine();
				int statusCode = Integer.parseInt(response.trim().split(" ")[1]);
				responseMap.put("status", ""+ statusCode);
				String line = "";
				while ((line = reader.readLine()).length() != 0) { 
					String[] lineParts = line.split(":", 2);
					responseMap.put(lineParts[0].toLowerCase().trim(), lineParts[1].trim());
				}
				if (statusCode != 200 && statusCode != 301 && statusCode != 307)
				{
					reader = null;
					return;
				}
				body = "";
				
				while ((line = reader.readLine()) != null) { 
					body = body + line + "\n";
				}
			}
			
			//response for HEAD
			else if(method.equalsIgnoreCase("head")){
				response = reader.readLine();
				int statusCode = Integer.parseInt(response.trim().split(" ")[1]);
				responseMap.put("status", ""+ statusCode);
				String line = "";
				while ((line = reader.readLine()).length() != 0) { 
					String[] lineParts = line.split(":", 2);
					responseMap.put(lineParts[0].toLowerCase().trim(), lineParts[1].trim());
				}
				
				if (statusCode != 200 && statusCode != 301 && statusCode != 307)
				{
					reader = null;
					return;
				}
			}
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	
	/**
	 * Method to send request for https.
	 */
	private void sendHttpsRequest(){
		URL urlObject;
		HttpsURLConnection connection;
		try {
			urlObject = new URL(url);
			connection = (HttpsURLConnection) urlObject.openConnection();
			connection.setRequestMethod(method.toUpperCase());
			connection.setRequestProperty("User-agent", "cis455crawler");
			connection.setInstanceFollowRedirects(false);

			for (Map.Entry<String, String> entry : requestMap.entrySet()){
				connection.setRequestProperty(entry.getKey(), entry.getValue());
			}
			int statusCode = connection.getResponseCode();
			
			if (statusCode != 200 && statusCode != 301 && statusCode != 304 && statusCode != 307)
			{
				reader = null;
				return;
			}
			responseMap.put("content-length", ""+connection.getContentLength());
			String contentType = connection.getContentType();
			if(contentType == null){
				if(url.endsWith("html") || url.endsWith("htm")){
					contentType = "text/html";
				}
				else if(url.contains("xml")){
					contentType = "text/xml";
				}
			}
			responseMap.put("content-type", contentType);
			
			responseMap.put("status", ""+connection.getResponseCode());
			if (requestMap.containsKey("if-modified-since")){
				Long lastModifiedDate = connection.getLastModified();
				Date date = new Date(lastModifiedDate);
				SimpleDateFormat f = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
				f.setTimeZone(TimeZone.getTimeZone("GMT"));
				String lastModifiedFormattedDate = f.format(date).toString();
				responseMap.put("last-modified", lastModifiedFormattedDate);
			}
				
			if(connection.getHeaderField("Location") != null){
				responseMap.put("location", connection.getHeaderField("Location"));
			}
				
			
				reader = new BufferedReader(new InputStreamReader((connection.getInputStream())));
			
			StringBuilder sb = new StringBuilder();
			String output = "";
			output = reader.readLine();
			sb.append(output);
				while ((output = reader.readLine()) != null) {
					sb.append("\n"+output);
				}
			body = sb.toString();
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Method to send http and https requests
	 */
	public void sendRequest(){
		if(url.startsWith("http://")){
			sendHttpRequest();
		}
		else if(url.startsWith("https://")){
			sendHttpsRequest();
		}
	}
	
	/**
	 * Method to get Document from string
	 */
	public Document getDocument() {
		Document doc = null ;
		sendRequest();
		String resourcetype = "";
		String contentType = responseMap.get("content-type");
		if (contentType != null){
			resourcetype = contentType;
		}
			
		else if (url.endsWith("html") || url.endsWith("htm")) {
			resourcetype = "text/html";
		} 
		
		else if (url.endsWith("xml")) {
			resourcetype = "text/xml";
		}
		
		if (resourcetype.contains("html")) {
			Tidy tidy = new Tidy();
			tidy.setXHTML(true);
			tidy.setTidyMark(false);
			tidy.setShowWarnings(false);
			tidy.setQuiet(true);
			doc = tidy.parseDOM(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), null);
		} else if (resourcetype.contains("xml")) {
			DocumentBuilder db;
			try {
				db = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
				InputSource is = new InputSource();
				is.setCharacterStream(new BufferedReader(getBufferedReader()));
				doc = db.parse(is);
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return doc;
	}
}

