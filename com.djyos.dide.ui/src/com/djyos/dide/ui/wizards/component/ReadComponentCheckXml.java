package com.djyos.dide.ui.wizards.component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.djyos.dide.ui.objects.CmpntCheck;

public class ReadComponentCheckXml {
	private static DocumentBuilderFactory dbFactory = null;
	private static DocumentBuilder db = null;
	private static Document document = null;
	static {
		try {
			dbFactory = DocumentBuilderFactory.newInstance();
			db = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	public static List<CmpntCheck> getCmpntChecks(File file){
		try {
			document = db.parse(file);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<CmpntCheck> cmpntChecks = new ArrayList<CmpntCheck>();
		Node componentNode = document.getElementsByTagName("component").item(0);
		NodeList componentList = componentNode.getChildNodes();
		for (int i = 0; i < componentList.getLength(); i++) {
			Node cNode = componentList.item(i);
			if (cNode.getNodeType() == Node.ELEMENT_NODE) {
				String nodeName = cNode.getNodeName();
				String content = cNode.getFirstChild().getTextContent();
				CmpntCheck cmpntCheck = new CmpntCheck(nodeName, content);
				cmpntChecks.add(cmpntCheck);
			}
		}
		return cmpntChecks;

	}
}
