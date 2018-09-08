package com.djyos.dide.ui.arch;

import java.io.File;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.djyos.dide.ui.wizards.djyosProject.tools.DideHelper;

public class ReadArchXml {
	private static DocumentBuilderFactory dbFactory = null;
	private static DocumentBuilder db = null;
	private static Document document = null;
	DideHelper dideHelper = new DideHelper();
	
	public Arch getArch(File file){
		
		dbFactory = DocumentBuilderFactory.newInstance();
		try {
			db = dbFactory.newDocumentBuilder();
			document = db.parse(file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Arch arch = new Arch();
		NodeList archList = document.getElementsByTagName("Arch");
		if(archList.getLength() > 0) {
			NodeList cList = archList.item(0).getChildNodes();
			for(int i=0;i<cList.getLength();i++) {
				Node node = cList.item(i);  
				if(node.getNodeType() == Node.ELEMENT_NODE) {
					String nodeName = node.getNodeName();
					String content = node.getFirstChild().getTextContent();
					switch(nodeName) {
					case "type":
						arch.setType(content);
						break;
					case "serial":
						arch.setSerial(content);
						break;
					case "family":
						arch.setFamily(content);
						break;
					}
				}
			}
		}
		return arch;
		
	}
}
