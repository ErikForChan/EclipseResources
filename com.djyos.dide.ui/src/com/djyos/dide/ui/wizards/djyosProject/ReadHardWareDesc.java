package com.djyos.dide.ui.wizards.djyosProject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class ReadHardWareDesc {
	private static DocumentBuilderFactory dbFactory = null;
	private static DocumentBuilder db = null;
	private static Document document = null;

	public static List<String> getHardWares(File file) {
		List<String> hardwares = new ArrayList<String>();
		try {
			dbFactory = DocumentBuilderFactory.newInstance();
			db = dbFactory.newDocumentBuilder();
			document = db.parse(file);
			NodeList boardList = document.getElementsByTagName("baord");
			hardwares.add(boardList.item(0).getFirstChild().getTextContent());
			NodeList cpuList = document.getElementsByTagName("cpu");
			hardwares.add(cpuList.item(0).getFirstChild().getTextContent());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hardwares;
	}
}
