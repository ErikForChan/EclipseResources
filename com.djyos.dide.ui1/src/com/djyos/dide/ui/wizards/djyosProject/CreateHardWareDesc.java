package com.djyos.dide.ui.wizards.djyosProject;

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

import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.objects.Board;
import com.djyos.dide.ui.objects.Cpu;

public class CreateHardWareDesc {
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	public void createHardWareXml(String boardName,String cpuName,String coreName, File file){
		try {
			factory.setIgnoringElementContentWhitespace(false);
	    	DocumentBuilder builder = factory.newDocumentBuilder();             
	    	Document document = builder.newDocument();
	    	Element hardwareElement = document.createElement("hardware");
	    	
	    	Element baordElement = document.createElement("baord");
	    	baordElement.setTextContent(boardName);
	    	Element cpuElement = document.createElement("cpu");
	    	cpuElement.setTextContent(cpuName);
	    	Element coreElement = document.createElement("core");
	    	coreElement.setTextContent(coreName);
	    	
	    	hardwareElement.appendChild(baordElement);
	    	hardwareElement.appendChild(cpuElement);
	    	hardwareElement.appendChild(coreElement);
	    	document.appendChild(hardwareElement);
	    	
	    	TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");// 增加换行缩进，但此时缩进默认为0
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");// 设置缩进为3
			transformer.setOutputProperty("encoding", "UTF-8");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(writer));
			transformer.transform(new DOMSource(document), new StreamResult(file));
			writer.close();
	    	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			DideHelper.showErrorMessage("文件"+file.getName()+"创建失败！ "+e.getMessage());
		}
		
	}
	
}
