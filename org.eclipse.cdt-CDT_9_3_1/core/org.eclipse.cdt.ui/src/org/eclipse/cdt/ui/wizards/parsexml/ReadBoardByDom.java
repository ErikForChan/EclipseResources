package org.eclipse.cdt.ui.wizards.parsexml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

public class ReadBoardByDom {
	private static DocumentBuilderFactory dbFactory = null;  
    private static DocumentBuilder db = null;  
    private static Document document = null;   
    static{  
        try {  
            dbFactory = DocumentBuilderFactory.newInstance();  
            db = dbFactory.newDocumentBuilder();  
        } catch (ParserConfigurationException e) {  
            e.printStackTrace();  
        }  
    }  
      
    public static Board getBoard(String fileName) throws Exception{  
        //������ URI �����ݽ���Ϊһ�� XML �ĵ�,������Document����  
        document = db.parse(fileName);  
        //���ĵ�˳�򷵻ذ������ĵ����Ҿ��и���������Ƶ����� Element �� NodeList  
//        NodeList cpuList = document.getElementsByTagName("cpu");  
        Board board = new Board();
		org.w3c.dom.Node node = document.getElementsByTagName("board").item(0);

		NamedNodeMap namedNodeMap = node.getAttributes();

//		String id = namedNodeMap.getNamedItem("id").getTextContent();// System.out.println(id);
//		board.cpu.setId(Integer.parseInt(id));

		// ��ȡbook�����ӽڵ�,������Test���͵Ļ���
		NodeList cList = node.getChildNodes();// System.out.println(cList.getLength());9

		ArrayList<String> contents = new ArrayList<>();
		for (int j = 1; j < cList.getLength(); j += 2) {
			org.w3c.dom.Node cNode = cList.item(j);
			String content = cNode.getFirstChild().getTextContent();
			contents.add(content);
			System.out.println(content);
		}
		
		if(board==null) {
			System.out.println("board==null");
		}

		board.cpu.setDevice(contents.get(0));
		board.cpu.setCore(contents.get(1));
		board.cpu.setArchitecture(contents.get(2));
		board.cpu.setFlashStart(contents.get(3));
		board.cpu.setFlashSize(contents.get(4));
		board.cpu.setRamStart(contents.get(5));
		board.cpu.setRamSize(contents.get(6));
		board.cpu.setFpuType(contents.get(7));
		board.setExClk(contents.get(8));
		board.setIbootSize(contents.get(9));

		return board;
          
    }  
}
