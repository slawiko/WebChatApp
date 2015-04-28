package practice.chat.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import practice.chat.model.Message;

import static practice.chat.util.MessageUtil.*;

public final class XMLStorage {
	private static final String STORAGE_LOCATION = System.getProperty("user.home") +  File.separator + "history.xml";
	private static final String METHOD = "method";
	private static final String MESSAGES = "messages";
	private static final String MESSAGE = "message";
	private static final String EXPRESSION_MESSAGE = "/messages/message";

	private XMLStorage() {
	}

	public static synchronized void createStorage() throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();

		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement(MESSAGES);
		doc.appendChild(rootElement);

		Transformer transformer = getTransformer();

		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(STORAGE_LOCATION));
		transformer.transform(source, result);
	}

	public static synchronized void addData(Message message, String method) throws ParserConfigurationException, SAXException, IOException, TransformerException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(STORAGE_LOCATION);
		document.getDocumentElement().normalize();

		Element root = document.getDocumentElement();

		Element messageElement = document.createElement(MESSAGE);
		root.appendChild(messageElement);

		messageElement.setAttribute(ID, message.getId());

		Element author = document.createElement(AUTHOR);
		author.appendChild(document.createTextNode(message.getAuthor()));
		messageElement.appendChild(author);

		Element text = document.createElement(TEXT);
		text.appendChild(document.createTextNode(message.getText()));
		messageElement.appendChild(text);

		Element date = document.createElement(DATE);
		date.appendChild(document.createTextNode(message.getDate()));
		messageElement.appendChild(date);

		Element methodElement = document.createElement(METHOD);
		methodElement.appendChild(document.createTextNode(method));
		messageElement.appendChild(methodElement);

		DOMSource source = new DOMSource(document);
		Transformer transformer = getTransformer();

		StreamResult result = new StreamResult(STORAGE_LOCATION);
		transformer.transform(source, result);
	}

	/*public static synchronized void addAll(List<Message> messages) throws ParserConfigurationException, SAXException, IOException, TransformerException {

		if (!isExist()) {
			createStorage();
		}
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(STORAGE_LOCATION);
		document.getDocumentElement().normalize();

		Element root = document.getDocumentElement();

		for (Message message : messages) {

			Element messageElement = document.createElement(MESSAGE);
			root.appendChild(messageElement);

			messageElement.setAttribute(ID, message.getId());

			Element user = document.createElement(AUTHOR);
			user.appendChild(document.createTextNode(message.getAuthor()));
			messageElement.appendChild(user);

			Element text = document.createElement(TEXT);
			text.appendChild(document.createTextNode(message.getText()));
			messageElement.appendChild(text);

			Element date = document.createElement(DATE);
			date.appendChild(document.createTextNode(message.getDate()));
			messageElement.appendChild(date);

			DOMSource source = new DOMSource(document);

			Transformer transformer = getTransformer();

			StreamResult result = new StreamResult(STORAGE_LOCATION);
			transformer.transform(source, result);
		}
	}*/
//unchecked
	public static synchronized void updateData(Message message) throws ParserConfigurationException, SAXException, IOException, TransformerException, XPathExpressionException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(STORAGE_LOCATION);
		document.getDocumentElement().normalize();
		Node messageToUpdate = getNodeById(document, message.getId());

		if (messageToUpdate != null) {
			NodeList childNodes = messageToUpdate.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node node = childNodes.item(i);
				if (TEXT.equals(node.getNodeName())) {
					node.setTextContent(message.getText());
				}
			}

			Transformer transformer = getTransformer();

			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(new File(STORAGE_LOCATION));
			transformer.transform(source, result);
		} else {
			throw new NullPointerException();
		}
	}

	public static synchronized void deleteData(Message message) throws ParserConfigurationException, SAXException, IOException, TransformerException, XPathExpressionException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(STORAGE_LOCATION);
		document.getDocumentElement().normalize();

		NodeList root = document.getElementsByTagName(MESSAGES);
		Node messageToDelete = getNodeById(document, message.getId());
		if (messageToDelete != null) {
			root.item(0).removeChild(messageToDelete);

			Transformer transformer = getTransformer();

			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(new File(STORAGE_LOCATION));
			transformer.transform(source, result);
		} else {
			throw new NullPointerException();
		}
	}

	public static synchronized boolean isExist() {
		File file = new File(STORAGE_LOCATION);
		return file.exists();
	}

	public static synchronized List<Message> getListMessages() throws SAXException, IOException, ParserConfigurationException {
		List<Message> messages = new ArrayList<Message>();
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(STORAGE_LOCATION);
		document.getDocumentElement().normalize();
		Element root = document.getDocumentElement();
		NodeList messageList = root.getElementsByTagName(MESSAGE);
		for (int i = 0; i < messageList.getLength(); i++) {
			Element messageElement = (Element) messageList.item(i);
			String id = messageElement.getAttribute(ID);
			String author = messageElement.getElementsByTagName(AUTHOR).item(0).getTextContent();
			String text = messageElement.getElementsByTagName(TEXT).item(0).getTextContent();
			String date = messageElement.getElementsByTagName(DATE).item(0).getTextContent();
			messages.add(new Message(id, author, text, date));
		}
		return messages;
	}

	private static synchronized List<Message> getListMessages(NodeList nodeList) {
		List<Message> messages = new ArrayList<Message>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element messageElement = (Element) nodeList.item(i);
			String id = messageElement.getAttribute(ID);
			String author = messageElement.getElementsByTagName(AUTHOR).item(0).getTextContent();
			String text = messageElement.getElementsByTagName(TEXT).item(0).getTextContent();
			String date = messageElement.getElementsByTagName(DATE).item(0).getTextContent();
			messages.add(new Message(id, author, text, date));
		}
		return messages;
	}

	public static synchronized List<Message> getSubNodeList(int index) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document document = builder.parse(new FileInputStream(STORAGE_LOCATION));
		XPath xpath = XPathFactory.newInstance().newXPath();

		String expression = EXPRESSION_MESSAGE + "[position() >= " + index + "]";
		return getListMessages((NodeList) xpath.compile(expression).evaluate(document, XPathConstants.NODESET));
	}

	public static synchronized int getStorageSize() throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(STORAGE_LOCATION);
		document.getDocumentElement().normalize();
		Element root = document.getDocumentElement();
		return root.getElementsByTagName(MESSAGE).getLength();
	}

	private static Node getNodeById(Document doc, String id) throws XPathExpressionException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		String expression = "//" + MESSAGE + "[@id='" + id + "']";
		XPathExpression expr = xpath.compile(expression);
		return (Node) expr.evaluate(doc, XPathConstants.NODE);
	}

	private static Transformer getTransformer() throws TransformerConfigurationException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		return transformer;
	}
}