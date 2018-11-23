package com.djyos.dide.ui.wizards.board;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.objects.Board;
import com.djyos.dide.ui.objects.Chip;
import com.djyos.dide.ui.objects.Component;
import com.djyos.dide.ui.objects.OnBoardCpu;
import com.djyos.dide.ui.objects.OnBoardMemory;

public class ReadBoardXml {
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

	public static List<Board> getAllBoards() {
		List<Board> boards = new ArrayList<Board>();
		List<String> paths = new ArrayList<String>();
		String userBoardFilePath = DideHelper.getUserBoardFilePath();
		String demoBoardFilePath = DideHelper.getDemoBoardFilePath();
		paths.add(userBoardFilePath);
		paths.add(demoBoardFilePath);
		for (int i = 0; i < paths.size(); i++) {
			File boardFile = new File(paths.get(i));
			if (boardFile.exists()) {
				File[] files = boardFile.listFiles();
				for (int j = 0; j < files.length; j++) {
					File file = files[j];
					if (file.isDirectory()) {
						File[] mfiles = file.listFiles();
						for (int k = 0; k < mfiles.length; k++) {
							if (mfiles[k].getName().endsWith(".xml")) {
								try {
									Board board = getBoard(mfiles[k]);
									boards.add(board);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								break;
							}
						}
					}
				}
			}
		}
		return boards;
	}

	/*
	 * 获取板件对象
	 */
	public static Board getBoard(File file) throws Exception {
		document = db.parse(file);
		Board board = new Board();
		board.setBoardPath(file.getParentFile().getPath());
		Node nameNode = document.getElementsByTagName("boardName").item(0);
		board.setBoardName(nameNode.getTextContent());
		NodeList onBoardCpuList = document.getElementsByTagName("cpu");
		List<OnBoardCpu> onBoardCpus = new ArrayList<OnBoardCpu>();
		for (int x = 0; x < onBoardCpuList.getLength(); x++) {
			Node cpuNode = onBoardCpuList.item(x);
			NodeList cList = cpuNode.getChildNodes();
			OnBoardCpu cpuOn = new OnBoardCpu();
			for (int i = 0; i < cList.getLength(); i++) {
				Node cNode = cList.item(i);
				if (cNode.getNodeType() == Node.ELEMENT_NODE) {
					String nodeName = cNode.getNodeName();
					String content = cNode.getFirstChild().getTextContent();
					switch (nodeName) {
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
						for (int j = 1; j < chipList.getLength(); j += 2) {
							org.w3c.dom.Node chipNode = chipList.item(j);
							String mName = chipNode.getNodeName();
							String mContent = chipNode.getFirstChild().getTextContent();
							if (mName.equals("interface")) {
								chip.setTheInterface(mContent);
							} else if (mName.equals("chipName")) {
								chip.setChipName(mContent);
							}
						}
						cpuOn.getChips().add(chip);
						break;
					case "peripheral":
						Component component = new Component();
						component.setName(content);
						cpuOn.getPeripherals().add(component);
						break;
					case "memory":
						NodeList memoryList = cNode.getChildNodes();
						OnBoardMemory memory = new OnBoardMemory();
						for (int j = 1; j < memoryList.getLength(); j += 2) {
							org.w3c.dom.Node memoryNode = memoryList.item(j);
							String mName = memoryNode.getNodeName();
							String mContent = memoryNode.getFirstChild().getTextContent();
							if (mName.equals("type")) {
								memory.setType(mContent);
							} else if (mName.equals("startAddr")) {
								memory.setStartAddr(mContent);
							} else if (mName.equals("size")) {
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
