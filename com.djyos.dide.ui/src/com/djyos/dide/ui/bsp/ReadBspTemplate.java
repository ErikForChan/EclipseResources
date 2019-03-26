package com.djyos.dide.ui.bsp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.djyos.dide.ui.objects.BspStep;
import com.djyos.dide.ui.wizards.djyosProject.tools.StringUtils;

public class ReadBspTemplate {
	private static DocumentBuilderFactory dbFactory = null;
	private static DocumentBuilder db = null;
	private static Document document = null;

	static {
		try {
			dbFactory = DocumentBuilderFactory.newInstance();
			db = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	public static List<BspStep> getBspSteps(File file) {
		List<BspStep> steps = new ArrayList<>();
		try {
			document = db.parse(file);
			NodeList stepLists = document.getElementsByTagName("step");
			for (int i = 0; i < stepLists.getLength(); i++) {
				BspStep step = new BspStep();// 新建一个BspStep对象
				Node stepNode = stepLists.item(i);
				NodeList sList = stepNode.getChildNodes();
				for (int j = 0; j < sList.getLength(); j++) {
					Node sNode = sList.item(j);
					if (sNode.getNodeType() == Node.ELEMENT_NODE) {
						String nodeName = sNode.getNodeName();
						String content = sNode.getFirstChild().getTextContent();
						switch (nodeName) {
						case "name":
							step.setName(content.trim());
							break;

						case "info":
							step.setInfo(StringUtils.replaceBlank(content));
							break;

						case "template":
							NodeList tList = sNode.getChildNodes();
							BspTemplate template = new BspTemplate();// 新建一个BspTemplate对象
							for (int k = 0; k < tList.getLength(); k++) {
								Node tNode = tList.item(k);
								if (tNode.getNodeType() == Node.ELEMENT_NODE) {
									String tNodeName = tNode.getNodeName();
									String tContent = tNode.getFirstChild().getTextContent();
									switch (tNodeName) {
									case "file":
										template.setFileName(tContent.trim());
										break;
									case "location":
										template.setLocation(tContent.trim());
										break;
									}
								}
							}
							step.getTemplates().add(template);
							break;
						}
					}
				}
				steps.add(step);
			}

		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return steps;
	}
}
