package com.djyos.dide.ui.wizards.djyosProject.info;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.djyos.dide.ui.objects.Cpu;

public class ReadCpusInfo {
	private static DocumentBuilderFactory dbFactory = null;
	private static DocumentBuilder db = null;
	private static Document document = null;
	List<String> cpuNames = new ArrayList<String>();
	
	public List<String> getCpusInfo(File file) {
		
		try {
			dbFactory = DocumentBuilderFactory.newInstance();
			db = dbFactory.newDocumentBuilder();
			document = db.parse(file);
			NodeList cpuList = document.getElementsByTagName("cpu");
			for(int i=0;i<cpuList.getLength();i++) {
				String cpuName = cpuList.item(i).getFirstChild().getTextContent();
				cpuNames.add(cpuName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cpuNames;
		
	}
}
