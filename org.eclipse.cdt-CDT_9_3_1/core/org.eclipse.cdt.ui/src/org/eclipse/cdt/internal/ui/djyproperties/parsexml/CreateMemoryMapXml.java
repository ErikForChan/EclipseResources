package org.eclipse.cdt.internal.ui.djyproperties.parsexml;

import java.io.File;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.eclipse.cdt.internal.ui.djyproperties.MemoryMap;

public class CreateMemoryMapXml {
	 DocumentBuilderFactory factory =  DocumentBuilderFactory.newInstance();  
	 
	 public void creatMemoryMap(MemoryMap mMap,String xmlPath) {
	        try {
	        	factory.setIgnoringElementContentWhitespace(false);
	        	DocumentBuilder builder = factory.newDocumentBuilder();             
	        	Document document = builder.newDocument(); 
	        	 //创建属性名、赋值  
	        	Element memoryMap = document.createElement("memoryMap");
	        	
	        	Element flashStart = document.createElement("flashStart");
	        	flashStart.setTextContent(mMap.getFlashStart());
	            Element flashSize = document.createElement("flashSize");
	            flashSize.setTextContent(mMap.getFlashSize());
	            Element ramStart = document.createElement("ramStart");
	            ramStart.setTextContent(mMap.getRamStart());
	            Element ramSize = document.createElement("ramSize");
	            ramSize.setTextContent(mMap.getRamSize());
	            Element extromStart = document.createElement("extromStart");
	            extromStart.setTextContent(mMap.getExtromStart());
	            Element extromSize = document.createElement("extromSize");
	            extromSize.setTextContent(mMap.getExtromSize());
	            Element extramStart = document.createElement("extramStart");
	            extramStart.setTextContent(mMap.getExtramStart());
	            Element extramSize = document.createElement("extramSize");
	            extramSize.setTextContent(mMap.getExtramSize());
	            Element ibootSize = document.createElement("ibootSize");
	            ibootSize.setTextContent(mMap.getIbootSize());
	            
	            memoryMap.appendChild(flashStart);
	            memoryMap.appendChild(flashSize);
	            memoryMap.appendChild(ramStart);
	            memoryMap.appendChild(ramSize);
	            memoryMap.appendChild(extromStart);
	            memoryMap.appendChild(extromSize);
	            memoryMap.appendChild(extramStart);
	            memoryMap.appendChild(extramSize);
	            memoryMap.appendChild(ibootSize);

	            document.appendChild(memoryMap);
	            
	           TransformerFactory transformerFactory = TransformerFactory.newInstance();  
	           Transformer transformer = transformerFactory.newTransformer();  
	           transformer.setOutputProperty(OutputKeys.INDENT, "yes");//增加换行缩进，但此时缩进默认为0  
	           transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");//设置缩进为2
	           transformer.setOutputProperty("encoding", "UTF-8");  
	                 
	           StringWriter writer = new StringWriter();  
	           transformer.transform(new DOMSource(document), new StreamResult(writer));  
	           transformer.transform(new DOMSource(document), new StreamResult(new File(xmlPath)));
	           writer.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	     }
	 
}
