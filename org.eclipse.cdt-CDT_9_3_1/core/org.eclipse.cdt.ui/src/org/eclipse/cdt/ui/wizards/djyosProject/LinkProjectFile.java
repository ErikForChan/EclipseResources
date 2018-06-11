package org.eclipse.cdt.ui.wizards.djyosProject;

import java.io.File;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class LinkProjectFile {
	DocumentBuilderFactory factory =  DocumentBuilderFactory.newInstance(); 
	private String eclipsePath = getEclipsePath();
	public String getEclipsePath() {
		String fullPath = Platform.getInstallLocation().getURL().toString();
		String eclipsePath = fullPath.substring(6,(fullPath.substring(0,fullPath.length()-1)).lastIndexOf("/")+1);
		return eclipsePath;
	}
	public void addLink(IFile file,String componentName,String componentPath,String tag) {
		 factory.setIgnoringElementContentWhitespace(false);
		 DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document document = builder.parse(file.getLocation().toFile()); 
			Node linkedResourceElement = document.getElementsByTagName("linkedResources").item(0);
			
			Node linkNode = document.createElement("link");
			Node nameNode = document.createElement("name");
			Node typeNode = document.createElement("type");
			typeNode.setTextContent("2");
			Node locationURINode = document.createElement("locationURI");
			String DJYOS_SRC_LOCARION = eclipsePath+"djysrc";
			if(tag.equals("third") || tag.equals("component") || tag.equals("djyos")) {
				componentPath = componentPath.substring(DJYOS_SRC_LOCARION.length()+1, componentPath.length());
				nameNode.setTextContent("src/libos/"+tag+"/"+componentName);
				locationURINode.setTextContent("DJYOS_SRC_LOCATION/"+componentPath.replace("\\", "/"));
			}else if(tag.equals("chipdrv")) {
				componentPath = componentPath.substring(DJYOS_SRC_LOCARION.length()+1, componentPath.length());
				nameNode.setTextContent("src/libos/bsp/"+tag+"/"+componentName);
				locationURINode.setTextContent("DJYOS_SRC_LOCATION/"+componentPath.replace("\\", "/"));
			}else if(tag.equals("cpudrv")) {
				nameNode.setTextContent("src/libos/bsp/"+tag+"/"+componentName);
				locationURINode.setTextContent("DJYOS_SRC_LOCATION/"+componentPath.replace("\\", "/"));
			}
			
			linkNode.appendChild(nameNode);
			linkNode.appendChild(typeNode);
			linkNode.appendChild(locationURINode);
			linkedResourceElement.appendChild(linkNode);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");// 增加换行缩进，但此时缩进默认为0
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");// 设置缩进为2
			transformer.setOutputProperty("encoding", "UTF-8");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(writer));
			transformer.transform(new DOMSource(document), new StreamResult(file.getLocation().toFile()));
			writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}             
        
	}
}
