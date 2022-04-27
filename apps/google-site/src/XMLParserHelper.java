

import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLParserHelper {
	DocumentBuilderFactory dbFactory;
	DocumentBuilder dBuilder;

	public XMLParserHelper() {

		try {
			dbFactory = DocumentBuilderFactory.newInstance();
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getContentUrl(String url) {
		try {
			URL content = new URL(url);
			URLConnection cc = content.openConnection();

			Document doc = dBuilder.parse(cc.getInputStream());

			doc.getDocumentElement().normalize();

			// System.out.println("Root element :" +
			// doc.getDocumentElement().getNodeName());

			NodeList nList = doc.getElementsByTagName("summary");

			// System.out.println("----------------------------");

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					NodeList nl = eElement.getElementsByTagName("div");

					Node node = nl.item(0); // a

					Element aElement = (Element) nNode;

					nl = aElement.getElementsByTagName("a");
					// nl = node.getChildNodes();

					node = nl.item(0); // a

//				System.out.println("Summary : " + eElement.getElementsByTagName("summary").item(0).getFirstChild().getAttributes()TextContent());
//				System.out.println("Last Name : " + eElement.getElementsByTagName("lastname").item(0).getTextContent());
//				System.out.println("Nick Name : " + eElement.getElementsByTagName("nickname").item(0).getTextContent());
//				System.out.println("Salary : " + eElement.getElementsByTagName("salary").item(0).getTextContent());
					return ((Element) node).getAttribute("href");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

//	public static void main(String argv[]) {
//
//		try {
//
//			File fXmlFile = new File("c:/Users/T843327/Desktop/feed.xml");
//
//			Document doc = dBuilder.parse(fXmlFile);
//
//			// optional, but recommended
//			// read this -
//			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
//			doc.getDocumentElement().normalize();
//
//			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
//
//			NodeList nList = doc.getElementsByTagName("summary");
//
//			System.out.println("----------------------------");
//
//			for (int temp = 0; temp < nList.getLength(); temp++) {
//
//				Node nNode = nList.item(temp);
//
//				System.out.println("\nCurrent Element :" + nNode.getNodeName());
//
//				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
//
//					Element eElement = (Element) nNode;
//
//					NodeList nl = eElement.getElementsByTagName("div");
//
//					Node node = nl.item(0); // a
//
//					Element aElement = (Element) nNode;
//
//					nl = aElement.getElementsByTagName("a");
//					// nl = node.getChildNodes();
//
//					node = nl.item(0); // a
//
//					System.out.println(((Element) node).getAttribute("href"));
//
////			System.out.println("Summary : " + eElement.getElementsByTagName("summary").item(0).getFirstChild().getAttributes()TextContent());
////			System.out.println("Last Name : " + eElement.getElementsByTagName("lastname").item(0).getTextContent());
////			System.out.println("Nick Name : " + eElement.getElementsByTagName("nickname").item(0).getTextContent());
////			System.out.println("Salary : " + eElement.getElementsByTagName("salary").item(0).getTextContent());
//
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

}