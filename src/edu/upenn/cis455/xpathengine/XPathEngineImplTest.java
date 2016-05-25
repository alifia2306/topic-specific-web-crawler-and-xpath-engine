package edu.upenn.cis455.xpathengine;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XPathEngineImplTest {
	XPathEngineImpl xpathengine = (XPathEngineImpl) XPathEngineFactory.getXPathEngine();
	String xpaths[];
	String url;
	
	@Before
	public void intialize()
	{
		
	}
	/**
	 * Testing isValid for xpaths that are valid
	 */
	@Test
	public void test_isValid()
	{
		xpaths = new String[5];
		xpaths[0] = "/breakfast_menu/food[@price = \"$7.95\"][name[text() = \"waff\"]][calories[text() = \"900\"]]";
		xpaths[1] = "/breakfast_menu/food[name[text() = \"waf\\\"f\"][cal[text() = \"900\"]]]";
		xpaths[2] = "/breakfast_menu/food[name[text() = \"waff\"][cal[text() = \"900\"]]]";
		xpaths[3] = "/breakfast_menu/food[name[text() = \"waff\"]]";
		xpaths[4] = "/breakfast_menu/food[name[text() = \"Berry-Berry Belgian Waffles\"]][calories[text() = \"900\"]]";
		xpathengine.setXPaths(xpaths);
		assertTrue(xpathengine.isValid(0));
		assertTrue(xpathengine.isValid(1));
		assertTrue(xpathengine.isValid(2));
		assertTrue(xpathengine.isValid(3));
		assertTrue(xpathengine.isValid(4));
	}
	
	/**
	 * Testing evaluate for xpaths that are valid
	 */
	@Test
	public void test_isEvaluate(){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse("src/food.xml");
			XPathEngineImpl xpathengine2 = (XPathEngineImpl) XPathEngineFactory.getXPathEngine();
			xpaths = new String[5];
			xpaths[0] = "/breakfast_menu/food[@price = \"$7.95\"][name[text() = \"waff\"]][calories[text() = \"900\"]]";
			xpaths[1] = "/breakfast_menu/food[name[text() = \"waf\\\"f\"][cal[text() = \"900\"]]]";
			xpaths[2] = "/breakfast_menu/food[name[text() = \"waff\"][cal[text() = \"900\"]]]";
			xpaths[3] = "/breakfast_menu/food[name[text() = \"waff\"]]";
			xpaths[4] = "/breakfast_menu/food[name[text() = \"Berry-Berry Belgian Waffles\"]][calories[text() = \"900\"]]";
			xpathengine2.setXPaths(xpaths);
			boolean[] result = xpathengine2.evaluate(doc);
			for(int i = 0; i < result.length; i++){
				assertTrue(result[i]);
			}
			
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

}
