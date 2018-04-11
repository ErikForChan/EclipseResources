package org.eclipse.cdt.ui.wizards.parsexml;

import java.io.File;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CreateCpuXml {
	DocumentBuilderFactory factory =  DocumentBuilderFactory.newInstance();  
 	
    public void addCpu(Cpu cpu,String xmlPath) {
       try {
    	   factory.setIgnoringElementContentWhitespace(false);
    	   DocumentBuilder builder = factory.newDocumentBuilder();             
           Document document = builder.parse(xmlPath); 
       	 	//创建属性名、赋值  
           Element root = document.getDocumentElement();
           int count = document.getElementsByTagName("cpu").getLength();
         
           Element cpuElement = document.createElement("cpu");
           cpuElement.setAttribute("id", String.valueOf(count+1));
           Element device = document.createElement("device");
           device.setTextContent(cpu.getDevice());
           Element core = document.createElement("core");
           core.setTextContent(cpu.getCore());
           Element architecture = document.createElement("architecture");
           architecture.setTextContent(cpu.getArchitecture());
           Element flashStart = document.createElement("flashStart");
           flashStart.setTextContent(cpu.getFlashStart());
           Element flashSize = document.createElement("flashSize");
           flashSize.setTextContent(cpu.getFlashSize());
           Element ramStart = document.createElement("ramStart");
           ramStart.setTextContent(cpu.getRamStart());
           Element ramSize = document.createElement("ramSize");
           ramSize.setTextContent(cpu.getRamSize());
           Element fpuType = document.createElement("fpuType");
           fpuType.setTextContent(cpu.getFpuType());
           Element category = document.createElement("category");
           category.setTextContent(cpu.getCategory());
           cpuElement.appendChild(device);
           cpuElement.appendChild(core);
           cpuElement.appendChild(architecture);
           cpuElement.appendChild(flashStart);
           cpuElement.appendChild(flashSize);
           cpuElement.appendChild(ramStart);
           cpuElement.appendChild(ramSize);
           cpuElement.appendChild(fpuType);
           cpuElement.appendChild(category);

           root.appendChild(cpuElement);     

          TransformerFactory transformerFactory = TransformerFactory.newInstance();  
          Transformer transformer = transformerFactory.newTransformer();  
          transformer.setOutputProperty(OutputKeys.INDENT, "yes");//增加换行缩进，但此时缩进默认为0  
          transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");//设置缩进为2
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
