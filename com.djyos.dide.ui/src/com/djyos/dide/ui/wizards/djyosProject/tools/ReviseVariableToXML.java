package com.djyos.dide.ui.wizards.djyosProject.tools;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ReviseVariableToXML {
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	public void reviseXmlVariable(String locationName, String localtionPath, IFile iFile, String projectName) {
		factory.setIgnoringElementContentWhitespace(true);
		Document doc;
		try {
			DocumentBuilder db = factory.newDocumentBuilder();
			doc = db.parse(iFile.getLocation().toFile());
			NodeList nameList = doc.getElementsByTagName("name");
			Node titleNode = nameList.item(0);
			titleNode.setTextContent(projectName);

			NodeList linkList = doc.getElementsByTagName("variable");
			for (int i = 0; i < linkList.getLength(); i++) {
				Node node = linkList.item(i);
				NodeList cList = node.getChildNodes();
				for (int j = 1; j < cList.getLength(); j += 2) {
					org.w3c.dom.Node cNode = cList.item(j);
					String nodeName = cNode.getNodeName();
					String linkContent = cNode.getTextContent();
					if (nodeName.equals("name")) {
						if (linkContent.contains(locationName)) {
						}
					}
					if (nodeName.equals("value")) {
						cNode.setTextContent(localtionPath);
					}
				}
			}
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer former = factory.newTransformer();
			former.setOutputProperty(OutputKeys.INDENT, "yes");// 增加换行缩进，但此时缩进默认为0
			former.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");// 设置缩进为2
			former.setOutputProperty("encoding", "UTF-8");
			former.transform(new DOMSource(doc), new StreamResult(iFile.getLocation().toFile()));

		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void add_djyos_links(IFile iFile) {
		factory.setIgnoringElementContentWhitespace(true);
		Document doc;
		try {
			DocumentBuilder db = factory.newDocumentBuilder();
			doc = db.parse(iFile.getLocation().toFile());
			NodeList linkedResources = doc.getElementsByTagName("linkedResources");
			if(linkedResources.getLength() == 0) {
				Node head = doc.getElementsByTagName("projectDescription").item(0);
				
				String[] libos_members = {"bsp","component","djyos","libc","loader","third"};
				Element linkedResourcesElement = doc.createElement("linkedResources");
				for (String m : libos_members) {
					Element linkElement = doc.createElement("link");
						Element link_name = doc.createElement("name");
						link_name.setTextContent("src/libos/"+m);
						Element link_type = doc.createElement("type");
						link_type.setTextContent("2");
						Element locationURI = doc.createElement("locationURI");
						locationURI.setTextContent("DJYOS_SRC_LOCATION/"+m);
						linkElement.appendChild(link_name);
						linkElement.appendChild(link_type);
						linkElement.appendChild(locationURI);
					linkedResourcesElement.appendChild(linkElement);
				}
				head.appendChild(linkedResourcesElement);
			}
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer former = factory.newTransformer();
			former.setOutputProperty(OutputKeys.INDENT, "yes");// 增加换行缩进，但此时缩进默认为0
			former.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");// 设置缩进为2
			former.setOutputProperty("encoding", "UTF-8");
			former.transform(new DOMSource(doc), new StreamResult(iFile.getLocation().toFile()));

		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
