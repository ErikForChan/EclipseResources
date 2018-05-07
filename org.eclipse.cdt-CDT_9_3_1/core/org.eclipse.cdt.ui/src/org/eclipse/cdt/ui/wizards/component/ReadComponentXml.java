package org.eclipse.cdt.ui.wizards.component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.Platform;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ReadComponentXml {
	private static DocumentBuilderFactory dbFactory = null;
	private static DocumentBuilder db = null;
	private static Document document = null;
	String fullPath = Platform.getInstallLocation().getURL().toString();
	String eclipsePath = fullPath.substring(6,
			(fullPath.substring(0, fullPath.length() - 1)).lastIndexOf("/") + 1);
	List<Component> components = new ArrayList<Component>();
	static {
		try {
			dbFactory = DocumentBuilderFactory.newInstance();
			db = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public List<Component> getComponents(){
		
		String sourcePath = eclipsePath+"djysrc\\bsp\\cpudrv\\stm32f7\\src";
		File sourceFile = new File(sourcePath);
		File[] files = sourceFile.listFiles();
		for(File file:files) {		
			if(file.isDirectory()) {
				File[] allFiles = file.listFiles();
				for(File f:allFiles) {
					if(f.getName().endsWith(".xml")) {
						try {						
							getComponent(f);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		return components;
		
	}
	
	private void getComponent(File file) throws Exception {
		
		document = db.parse(file);
		org.w3c.dom.Node node = document.getElementsByTagName("component").item(0);
		NodeList cList = node.getChildNodes();
		
		for (int j = 1; j < cList.getLength(); j += 2) {
			org.w3c.dom.Node cNode = cList.item(j);
			NodeList list = cNode.getChildNodes();
			System.out.println("CnodeName : "+cNode.getNodeName());
			for(int i=1;i<list.getLength(); i += 2) {
				Component newComponent = new Component();
				org.w3c.dom.Node dNode = list.item(i);
				String nodeName = dNode.getNodeName();
				if(nodeName.equals("name")) {
					System.out.println("nodeName");
					newComponent.setName(dNode.getTextContent());
					components.add(newComponent);
				}
			}
			
		}	
	}
}
