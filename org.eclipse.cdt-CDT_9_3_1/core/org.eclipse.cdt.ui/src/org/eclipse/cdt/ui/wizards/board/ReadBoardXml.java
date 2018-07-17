package org.eclipse.cdt.ui.wizards.board;

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

import org.eclipse.cdt.ui.wizards.board.Board;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.Chip;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.OnBoardMemory;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.OnBoardCpu;
import org.eclipse.cdt.ui.wizards.component.Component;

public class ReadBoardXml {
	private static DocumentBuilderFactory dbFactory = null;
	private static DocumentBuilder db = null;
	private static Document document = null;
	private static Board board = null;
	static {
		try {
			dbFactory = DocumentBuilderFactory.newInstance();
			db = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * 获取板件对象
	 */
	public static Board getBoard(File file) throws Exception {
		document = db.parse(file);
		Board board = new Board();
		Node nameNode = document.getElementsByTagName("boardName").item(0);
		board.setBoardName(nameNode.getTextContent());
		NodeList onBoardCpuList = document.getElementsByTagName("cpu");
		List<OnBoardCpu> onBoardCpus = new ArrayList<OnBoardCpu>();
		for(int x=0;x<onBoardCpuList.getLength();x++) {
			Node cpuNode = onBoardCpuList.item(x);
			NodeList cList = cpuNode.getChildNodes();
			OnBoardCpu cpuOn = new OnBoardCpu();
			for(int i=0;i<cList.getLength();i++){
				Node cNode = cList.item(i);
				if(cNode.getNodeType() == Node.ELEMENT_NODE) {
					String nodeName = cNode.getNodeName();
					String content = cNode.getFirstChild().getTextContent();
					switch(nodeName) {
					case "cpuName":
						cpuOn.setCpuName(content);
						break;
					case "mianClk":
						cpuOn.setMianClk(Float.parseFloat(content));
						break;
					case "rtcClk":
						cpuOn.setRtcClk(Float.parseFloat(content));
						break;
					case "chip":
						NodeList chipList = cNode.getChildNodes();
						Chip chip = new Chip();
						for(int j=1;j<chipList.getLength();j+=2) {
							org.w3c.dom.Node chipNode = chipList.item(j);
							String mName = chipNode.getNodeName();
							String mContent = chipNode.getFirstChild().getTextContent();
							if(mName.equals("interface")) {
								chip.setTheInterface(mContent);
							}else if(mName.equals("chipName")) {
								chip.setChipName(mContent);
							}
						}
						cpuOn.getChips().add(chip);
						break;
					case "peripheral":
						Component component  = new Component();
						component.setName(content);
						cpuOn.getPeripherals().add(component);
						break;
					case "memory":
						NodeList memoryList = cNode.getChildNodes();
						OnBoardMemory memory = new OnBoardMemory();
						for(int j=1;j<memoryList.getLength();j+=2) {
							org.w3c.dom.Node memoryNode = memoryList.item(j);
							String mName = memoryNode.getNodeName();
							String mContent = memoryNode.getFirstChild().getTextContent();
							if(mName.equals("type")) {
								memory.setType(mContent);
							}else if(mName.equals("startAddr")) {
								memory.setStartAddr(mContent);
							}else if(mName.equals("size")) {
								memory.setSize(mContent);
							}
						}
						cpuOn.getMemorys().add(memory);
						break;
					}
				}				
			}
			onBoardCpus.add(cpuOn);
		}
		board.setOnBoardCpus(onBoardCpus);
		return board;
	}
}
