package org.eclipse.cdt.ui.wizards.parsexml;

import java.io.File;
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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ReviseLinkToXML {
	DocumentBuilderFactory factory =  DocumentBuilderFactory.newInstance();  
	
	public void reviseXmlLink(String boardNamePath,String boardName,String localPath,String cpuCategory,IFile iFile,String flag) {
		factory.setIgnoringElementContentWhitespace(true);
		 // 从XML文档中获取DOM文档实例
		 // 获取Document对象
		String projectPath = iFile.getLocation().toString().substring(0,iFile.getLocation().toString().lastIndexOf("/"));
        Document doc;
		try {
			DocumentBuilder db = factory.newDocumentBuilder();
			doc = db.parse(iFile.getLocation().toFile());
	        // 获取根节点
	        Element root = doc.getDocumentElement();
	        NodeList linkList = doc.getElementsByTagName("link"); 
	        for(int i=0;i<linkList.getLength();i++) {
	        	Node node = linkList.item(i);
	        	NodeList cList = node.getChildNodes();
				for (int j = 1; j < cList.getLength(); j += 2) {
					org.w3c.dom.Node cNode = cList.item(j);
					String nodeName = cNode.getNodeName();
					String linkContent = cNode.getTextContent();
					if(nodeName.equals("name")) {
						if(linkContent.contains(boardNamePath)) {	
							File dfile = new File(projectPath+"/"+linkContent);
							if(dfile.exists()) {
								dfile.delete();
							}
							cNode.setTextContent(boardNamePath+"/"+boardName);							
						}
					}
					if(nodeName.equals("locationURI")) {					
						if(linkContent.contains(localPath)) {
							if(flag.equals("boarddrv")) {
								cNode.setTextContent(localPath+"/"+boardName);							
							}else if(flag.equals("startup")){
								cNode.setTextContent(localPath+"/"+cpuCategory+"/"+boardName);					
							}else if(flag.equals("arch")){
								cNode.setTextContent(localPath+"/"+boardName);
							}else if(flag.equals("cpudrv")){
								cNode.setTextContent(localPath+"/"+boardName);
							}							
						}
			        	
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
		}catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        // 定位id为001的节点
	}
}
