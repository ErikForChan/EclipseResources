package com.djyos.dide.ui.wizards.djyosProject.info;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class ReadBoardsInfo {
	private static DocumentBuilderFactory dbFactory = null;
	private static DocumentBuilder db = null;
	private static Document document = null;
	List<String> baordNames = new ArrayList<String>();
	
	public List<String> getBoardsInfo(File file) {
		
		try {
			dbFactory = DocumentBuilderFactory.newInstance();
			db = dbFactory.newDocumentBuilder();
			document = db.parse(file);
			NodeList boardList = document.getElementsByTagName("board");
			for(int i=0;i<boardList.getLength();i++) {
				String boardName = boardList.item(i).getFirstChild().getTextContent();
				baordNames.add(boardName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return baordNames;
		
	}
}
