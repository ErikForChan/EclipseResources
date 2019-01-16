package com.djyos.dide.ui.wizards.djyosProject.tools;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.w3c.dom.Document;
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
		// 定位id为001的节点
	}
}
