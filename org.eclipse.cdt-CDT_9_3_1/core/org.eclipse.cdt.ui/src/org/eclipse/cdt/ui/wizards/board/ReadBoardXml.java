package org.eclipse.cdt.ui.wizards.board;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.Platform;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import org.eclipse.cdt.ui.wizards.parsexml.Cpu;
import org.eclipse.cdt.ui.wizards.parsexml.ReadCpuByDom;

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

	public static Board getBoard(String fileName) throws Exception {
		// 将给定 URI 的内容解析为一个 XML 文档,并返回Document对象
		document = db.parse(fileName);
		// 按文档顺序返回包含在文档中且具有给定标记名称的所有 Element 的 NodeList
		NodeList boardList = document.getElementsByTagName("board");
		board = new Board();

		String fullPath = Platform.getInstallLocation().getURL().toString();
		String eclipsePath = fullPath.substring(6,
				(fullPath.substring(0, fullPath.length() - 1)).lastIndexOf("/") + 1);
		String cpuXmlPath = eclipsePath + "/demo/cpu.xml";
		ReadCpuByDom rcb = new ReadCpuByDom();
		List<Cpu> cpus = rcb.getCpus(cpuXmlPath);
		Cpu cpu = null;

		Board board = new Board();
		org.w3c.dom.Node node = document.getElementsByTagName("board").item(0);

		// 获取book结点的子节点,包含了Test类型的换行
		NodeList cList = node.getChildNodes();// System.out.println(cList.getLength());9

		ArrayList<String> contents = new ArrayList<>();
		for (int j = 1; j < cList.getLength(); j += 2) {
			org.w3c.dom.Node cNode = cList.item(j);
			String content = cNode.getFirstChild().getTextContent();
			contents.add(content);
		}
		board.setBoardName(contents.get(0));
		for (int j = 0; j < cpus.size(); j++) {
			if (cpus.get(j).getDevice().equals(contents.get(1))) {
				cpu = cpus.get(j);
				break;
			}
		}
		// Cpu cpu = new Cpu(contents.get(1),contents.get(2),contents.get(4),
		// contents.get(5),contents.get(6),contents.get(7),contents.get(3),contents.get(8));
		board.setExClk(contents.get(2));
		board.setExtromStart(contents.get(3));
		board.setExtromSize(contents.get(4));
		board.setExtramStart(contents.get(5));
		board.setExtramSize(contents.get(6));
		board.setCpu(cpu);

		return board;   
    }  
}
