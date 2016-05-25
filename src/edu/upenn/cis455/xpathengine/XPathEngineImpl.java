package edu.upenn.cis455.xpathengine;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.helpers.DefaultHandler;

public class XPathEngineImpl implements XPathEngine {
	String[] xpaths;
	int match = 0;
	HashMap<String, ArrayList<String>> nodeNameToTests;
	HashMap<String, ArrayList<String>> nodeMatches;
	public XPathEngineImpl() {
    // Do NOT add arguments to the constructor!!
	  nodeNameToTests = new HashMap<>();
	  nodeMatches = new HashMap<>();
	  
  }
	
@Override
  /* (non-Javadoc)
 * @see edu.upenn.cis455.xpathengine.XPathEngine#setXPaths(java.lang.String[])
 */
public void setXPaths(String[] s) {
	  xpaths = s;  
  }

@Override
  /* (non-Javadoc)
 * @see edu.upenn.cis455.xpathengine.XPathEngine#isValid(int)
 */
public boolean isValid(int i) {
	  String xpath = xpaths[i].trim();
	 
//	  xpath = xpath.replaceAll("\\s+",""); //remove all whitespaces
	  //system.out.println("initial xpath  " + xpath);
	  if(!xpath.startsWith("/")){
		  return false;
	  }
	  
	  if (xpath.replace("/", "").equals("")) { // If xpath has no step
			return false;
	  }
	  
	  nodeNameToTests.clear();
	  nodeMatches.clear();
	  return checkStep(xpath.substring(1, xpath.length()).trim());
	  
  }
  	
  /**
   * checks step
 * @param step
 * @return
 */
public boolean checkStep(String step){
	  //system.out.println("checking step   " + step);
	  ArrayList<String> stepParts = getStepParts(step);
	  for(String part : stepParts){
		  part = part.trim();
		  if(!validPart(part)){
			  return false;
		  }
	  }
	  return true;
  }
  
/**
 * gets parts
* @param step
* @return
*/
  public ArrayList<String> getStepParts(String step){
	  boolean insideQuotes = false;
	  Stack<Character> brackets = new Stack<>();
	  StringBuilder part = new StringBuilder();
	  ArrayList<String> stepParts = new ArrayList<>();
	  
	  if(!step.contains("/")){
		  stepParts.add(step);
		  return stepParts;
	  }
	  else{
		  for(int i = 0; i < step.length(); i++){
			  if(step.charAt(i) == '\"' && step.charAt(i - 1) != '\\'){
				  insideQuotes = !insideQuotes;
			  }
			  
//			  if(step.charAt(i) == '\\' && step.charAt(i+1) == '\"'){
//				  i = i + 1;
//			  }
			  
			  if(step.charAt(i) == '[' && insideQuotes == false){
				  brackets.push(step.charAt(i));
			  }
			  
			  if(step.charAt(i) == ']' && insideQuotes == false){
				  if(brackets.isEmpty()){
					  continue;
				  }
				  else{
					  brackets.pop();
				  }
			  }
			  if(step.charAt(i) == '/' && insideQuotes == false){
				  if(!brackets.isEmpty()){
					  continue;
				  }
				  stepParts.add(part.toString());
				  part = new StringBuilder();
				  continue;
			  }
			  part.append(step.charAt(i));
					  
		  }
		  stepParts.add(part.toString().trim());
		  return stepParts;
	  }
	  
	  
  }
  
  
  /**
   * checks valid part 
 * @param part
 * @return
 */
public boolean validPart(String part){
	  part = part.trim();
	  //system.out.println("checking part: " + part);
	  String regex = "\\s*([A-Za-z]([A-Za-z0-9._-])*\\s*)(\\[.+\\])*\\s*";
	  Pattern pattern = Pattern.compile(regex);
	  Matcher matcher = pattern.matcher(part);
	  boolean insideQuotes = false;
	  boolean insideTest = false;
	  boolean gotNode = false;
	  String nodeName = "";
	  Stack<Character> brackets = new Stack<>();
	  StringBuilder test = new StringBuilder();
	  
	  if(matcher.matches()){
		  int start = part.indexOf("\"");
		  int finish = part.lastIndexOf("\"");
		  
		  for(int i = 0 ; i < part.length(); i++){
			  
//			  if(part.charAt(i) == '\"' && (i == start || i == finish)){
//				  insideQuotes = !insideQuotes;
//			  }
			  
			  if(part.charAt(i) == '\"' && part.charAt(i - 1) != '\\'){
				  insideQuotes = !insideQuotes;
			  }
			  
//			  if(part.charAt(i) == '\\' && part.charAt(i+1) == '\"'){
//				  i = i + 1;
//			  }
			  
			  if(part.charAt(i) == '[' && insideQuotes == false){
				  brackets.push(part.charAt(i));
				  insideTest = true;
				  if(!gotNode){
					  nodeName = part.substring(0,i).trim();
					  gotNode = true;
				  }
			  }
			  if(part.charAt(i) == ']' && insideQuotes == false){
				  brackets.pop();
				  if(brackets.isEmpty()){	  
					  insideTest = false;
					  if(!checkTest(test.toString().trim())){
						  return false;
					  }
					  if(nodeNameToTests.get(nodeName) != null){
						  nodeNameToTests.get(nodeName).add(test.toString());
					  }
					  else{
						  ArrayList<String> testList = new ArrayList<>();
						  testList.add(test.toString().trim());
						  nodeNameToTests.put(nodeName.trim(), testList);
					  }
					  test.setLength(0);

				  }
				  
			  }
			  if(insideTest){
				  test.append(part.charAt(i));
			  }
			  
		  }
		  return true;
	  }
	  else{
		  return false;
	  } 
	  
  }
  

/**
 * checks valid test 
* @param part
* @return
*/
  public boolean checkTest(String test){

	  test = test.substring(1,test.length());
	  //system.out.println("checking test" + test);
	  if (test.matches("\\s*text\\s*\\(\\s*\\)\\s*\\=\\s*\\\".+\\\"\\s*")) {
		  return true;
	  }

	  else if (test.matches("\\s*contains\\s*\\(\\s*text\\s*\\(\\s*\\)\\s*,\\s*\\\".+\\\"\\s*\\)")) {
		  return true;
	  }
	  
	  else if (test.matches("\\s*\\@\\s*[A-Z_a-z][A-Z_a-z0-9-.]*\\s*\\=\\s*\\\".+\\\"\\s*")) {
		  return true;
	  }
	  
	  else {
		  return checkStep(test);
	  }
  }
  
  @Override
  /**
   * evaluates document
 * @param part
 * @return
 */
  public boolean[] evaluate(Document d) { 
	  //system.out.println("Evaluating Document:  " + d.getDocumentURI());

	  boolean[] arrayOfMatches = new boolean[xpaths.length];
	  Arrays.fill(arrayOfMatches, true);
	  for(int i = 0; i < arrayOfMatches.length; i++){
		  if(!isValid(i)){
			  //system.out.println("document is not valid");
			  arrayOfMatches[i] = false;
		  }
		  else{
//			  //system.out.println("line 189: " + xpaths[i]);
			  if(!evaluateXPath(xpaths[i],d)){
				  //system.out.println("document is valid but does not match");
				  arrayOfMatches[i] = false;
			  }
		  }
	  }

	  return arrayOfMatches;
  }
  
  /**
   * evaluates xpath
 * @param part
 * @return
 */
  public boolean evaluateXPath(String xpath, Document d){
	  xpath = xpath.trim();
	  //system.out.println("Evaluating xpath:  " + xpath);
		Node rootElement = d.getDocumentElement();
		ArrayList<Node> nodeList = new ArrayList<>();
		nodeList.add(rootElement);
		String step = xpath.substring(1);
		//system.out.println(nodeList.get(0));
		return evaluateStep(step, nodeList);
  }
  
  /**
   * evaluates step
 * @param part
 * @return
 */
  public boolean evaluateStep(String step, ArrayList<Node> nodeList){
	  int count = 0;
	  //system.out.println("Evaluating step:  " +step );
	  step = step.trim();
	  ArrayList<String> stepParts = getStepParts(step);
	  ArrayList<Node> nextNodes = nodeList;
	  int matches = 0;
	  for(String part: stepParts){
		  part = part.trim();
		  //system.out.println("part:" + part);
		  nextNodes = evaluatePart(part, nextNodes);
		  if(nextNodes == null){
			  return false;
		  }
		  matches = nextNodes.size();
	  }

	  if(nodeNameToTests.isEmpty()){
		  return matches > 0;
	  }
	  for (Entry<String, ArrayList<String>> entry : nodeNameToTests.entrySet()) {
		  for(String test : entry.getValue()){
			  if(testButNotStep(test)){
				   count++;
			  }
		  }		   
	  }
	  if(count == match){
		  return true;
	  }
	  else{
		  return false;
	  }
	  
  }
  
  /**
   * evaluates test but not step
 * @param part
 * @return
 */
  public boolean testButNotStep(String test){
	  test = test.substring(1,test.length());
	  //system.out.println("checking test" + test);
	  if (test.matches("\\s*text\\s*\\(\\s*\\)\\s*\\=\\s*\\\".+\\\"\\s*")) {
		  return true;
	  }

	  else if (test.matches("\\s*contains\\s*\\(\\s*text\\s*\\(\\s*\\)\\s*,\\s*\\\".+\\\"\\s*\\)")) {
		  return true;
	  }
	  
	  else if (test.matches("\\s*\\@\\s*[A-Z_a-z][A-Z_a-z0-9-.]*\\s*\\=\\s*\\\".+\\\"\\s*")) {
		  return true;
	  }
	  
	  else {
		  return false;
	  }
  }
  
  /**
   * evaluates part
 * @param part
 * @return
 */
  public ArrayList<Node> evaluatePart(String part, ArrayList<Node> nodeList){
	  //system.out.println("Evaluating step Part  " + part);
	  String nodeName = "";
	  ArrayList<String> tests = new ArrayList<>();
	  ArrayList<Node> nextNodes = new ArrayList<Node>();
	  
	  // if only node name
	  if(!part.contains("[")){
		  nodeName = part.trim();
	  }
	  
	  else{
		  nodeName = part.split("\\[")[0].trim();
	  }
	  //system.out.println("NodeName for part  " + nodeName);
	  
	  for(Node node: nodeList){
		  //system.out.println("node from Nodelist: " + node.getNodeName());
		  if(nodeNameToTests.containsKey(nodeName)){
			  //system.out.println("Nodename in map" + nodeNameToTests.get(nodeName));
			  tests = nodeNameToTests.get(nodeName);
			  for(String test : tests){
				  
				  if(evaluateTest(test, node)){
					  NodeList childNodes = node.getChildNodes();
					  for(int i = 0 ; i < childNodes.getLength(); i++){
						  nextNodes.add(childNodes.item(i));
					  }
				  }
			  }
		  }
		  
		  //no tests
		  else{
			  if(nodeName.equals(node.getNodeName())){
				  //system.out.println("Nodename not in map but matches current node name");
				  NodeList childNodes = node.getChildNodes();
				  for(int i = 0 ; i < childNodes.getLength(); i++){
					  nextNodes.add(childNodes.item(i));
				  }
			  }
		  }
	  }
	  return nextNodes;
  }
  
  /**
   * evaluates test
 * @param part
 * @return
 */
  public boolean evaluateTest(String test, Node node){
	  test = test.substring(1, test.length()).trim();
	  //system.out.println("evaluating test   " + test);

	  // regex for text() = "..."
	  String textRegex = "\\s*text\\s*\\(\\s*\\)\\s*\\=\\s*\\\".+\\\"\\s*";   

	  // regex for contains(text(), "...")
	  String containsRegex= "\\s*contains\\s*\\(\\s*text\\s*\\(\\s*\\)\\s*,\\s*\\\".+\\\"\\s*\\)"; 

	  // regex for @attname = "..."
	  String attributeRegex ="\\s*\\@\\s*[A-Z_a-z][A-Z_a-z0-9-.]*\\s*\\=\\s*\\\".+\\\"\\s*";          

	  if(test.matches(textRegex)){
		  return testMatch1(test, node);

	  }else if(test.matches(containsRegex)){  // 
		  return testMatch2(test, node);

	  }else if (test.matches(attributeRegex)){
		  return testMatch3(test, node);
	  }

	  // nested call for children nodes
	  else {
		  //system.out.println("Does not match the 3 regex");
		  ArrayList<Node> childrenNodes = new ArrayList<Node>();
		  NodeList children = node.getChildNodes();
		  for(int ch = 0; ch < children.getLength(); ch ++){
			  childrenNodes.add(children.item(ch));
		  }
		  return evaluateStep(test, childrenNodes);
	  }

  }

  // match for text() = "..."
  public boolean testMatch1(String test, Node node){
	  //system.out.println("Matches first regex");
	  Pattern p = Pattern.compile("\".+\"");
	  Matcher m = p.matcher(test);  
	  String strText = "";
	  if(m.find()){
		  strText = m.group(0);
		  strText = strText.trim().substring(1, strText.length() - 1);
		  strText = strText.replace("\\\"", "\"");
	  }
	  
//	  String strText = test.split("\".+\"")[0];  
	  
//	  System.out.println("node Text" + node.getNodeName());
//	  NodeList list = node.getChildNodes();
//	  for(int i = 0; i < list.getLength(); i++){
//		  Node nodeText = list.item(i);
//		  System.out.println("node Text" + nodeText.getNodeName());
		  Node nodeText = node.getFirstChild();
		  if(nodeText != null && nodeText.getNodeType() == Node.TEXT_NODE &&
				  nodeText.getNodeValue().equals(strText) && nodeNameToTests.containsKey(node.getNodeName())) {
			  //system.out.println("string Text" + strText);
			  //system.out.println("node value" + nodeText.getNodeValue());
			  //system.out.println("Matched!!!!!!!!!!!!!!!!!!!!!!!!!");	
			  if(nodeMatches.containsKey(node.getNodeName())){
				  if(nodeMatches.get(node.getNodeName()).contains(test));
				  else{
					  nodeMatches.get(node.getNodeName()).add(test);
					  match++;
				  }
			  }
			  else{
				  ArrayList<String> testMatched = new ArrayList<>();
				  testMatched.add(test);
				  nodeMatches.put(node.getNodeName(), testMatched);
				  match++;
			  }
			  
			  return true;
		  }
//	  }
	  
	  return false;
  }

  //match for contains(text(), "...")
  public boolean testMatch2(String test, Node node){
	  //system.out.println("Matches second regex");
//	  String strContains = test.split("\"")[1];  
	  Pattern p = Pattern.compile("\".+\"");
	  Matcher m = p.matcher(test);  
	  String strContains = "";
	  if(m.find()){
		  strContains = m.group(0);
		  strContains = strContains.trim().substring(1, strContains.length() - 1);
		  strContains = strContains.replace("\\\"", "\"");
	  }
	  Node nodeContains = node.getFirstChild();
	  
	  
	  if(nodeContains != null && nodeContains.getNodeType() == Node.TEXT_NODE &&
			  nodeContains.getNodeValue().contains(strContains) && nodeNameToTests.containsKey(node.getNodeName())){
		  if(nodeMatches.containsKey(node.getNodeName())){
			  if(nodeMatches.get(node.getNodeName()).contains(test));
			  else{
				  nodeMatches.get(node.getNodeName()).add(test);
				  match++;
			  }
		  }
		  else{
			  ArrayList<String> testMatched = new ArrayList<>();
			  testMatched.add(test);
			  nodeMatches.put(node.getNodeName(), testMatched);
			  match++;
		  }
		  //system.out.println("string Text" + strContains);
		  //system.out.println("node value" + nodeContains.getNodeValue());
		  //system.out.println("Matched!!!!!!!!!!!!!!!!!!!!!!!!!");
		  return true;
	  }
	  return false;
  }

  //match for @attname = "..."
  public boolean testMatch3(String test, Node node){
	  //system.out.println("matches third regex");
	  String attKey = test.split("\"")[0].split("@")[1].split("=")[0].replace("\\s*","");
	  String attValue = test.split("\"")[1].trim(); 
	  
	  NamedNodeMap map = node.getAttributes();
	  if(map != null){
		  for(int i = 0 ; i<map.getLength() ; i++) {
		        Attr attribute = (Attr)map.item(i);     
		        //system.out.println( "___________________________" + attribute.getName()+" = "+attribute.getValue());
		        //system.out.println("aaaaaaaaaaaaaaa"  + attribute.getName().equals(attKey));
		        //system.out.println("bbbbbbbbbbbbbb"  + attribute.getValue().equals(attValue));
		        //system.out.println("cccccccccccccc" + nodeNameToTests.containsKey(node.getNodeName()));
		        if(attribute.getName().trim().equals(attKey.trim()) && attribute.getValue().trim().equals(attValue.trim()) && nodeNameToTests.containsKey(node.getNodeName())){
//		        	if(nodeMatches.containsKey(node)){
//						  nodeMatches.get(node).add(test);
//					  }
//					  else{
//						  ArrayList<String> testMatched = new ArrayList<>();
//						  testMatched.add(test);
//						  nodeMatches.put(node, testMatched);
//					  }
		        if(nodeMatches.containsKey(node.getNodeName())){
		  			  if(nodeMatches.get(node.getNodeName()).contains(test));
		  			  else{
		  				  nodeMatches.get(node.getNodeName()).add(test);
		  				  match++;
		  			  }
		  		  }
		  		  else{
		  			  ArrayList<String> testMatched = new ArrayList<>();
		  			  testMatched.add(test);
		  			  nodeMatches.put(node.getNodeName(), testMatched);
		  			  match++;
		  		  }
		        	//system.out.println("Matched!!!!!!!!!!!!!!!!!!!!!!!!!");
		        	return true;
		        }
		  }
//		  Node nodeAttr = map.getNamedItem(attKey); 
//		  if(nodeAttr != null){
//			  System.out.println("node attribute from map  " + nodeAttr.getNodeValue() );
//		  }
//		  if(nodeAttr != null && attValue.equals(nodeAttr.getNodeValue())){
//			  return true;
//		  }
	  }
//	  System.out.println("attribute key:  " + attKey);
//		System.out.println("attribute value:  " + attValue);
//	  if(node.getNodeType() == Node.ELEMENT_NODE){
//			Element element = (Element) node;
//			String valueFromNode = element.getAttribute(attKey);
//			if(attValue.equals(valueFromNode) && nodeNameToTests.containsKey(node.getNodeName())){
//				match++;
//				
//				System.out.println("value from node:  " + valueFromNode);
//				System.out.println("Matched!!!!!!!!!!!!!!!!!!!!!!!!!");
//				return true;
//			}
//	  }
	  return false;
  }


@Override
public boolean isSAX() {
	return false;
}

@Override
public boolean[] evaluateSAX(InputStream document, DefaultHandler handler) {
	return null;
}     
}