package com.djyos.dide.ui.wizards.djyosProject.info;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class ReadIncludeFile {
	private static DocumentBuilderFactory dbFactory = null;
	private static DocumentBuilder db = null;
	private static Document document = null;
	List<String> includePaths = new ArrayList<String>();

	public List<String> getIncludeFiles(File file) {

		try {
			dbFactory = DocumentBuilderFactory.newInstance();
			db = dbFactory.newDocumentBuilder();
			document = db.parse(file);
			NodeList componentList = document.getElementsByTagName("file");
			for (int i = 0; i < componentList.getLength(); i++) {
				String componentPath = componentList.item(i).getFirstChild().getTextContent();
				includePaths.add(componentPath);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return includePaths;

	}
}
