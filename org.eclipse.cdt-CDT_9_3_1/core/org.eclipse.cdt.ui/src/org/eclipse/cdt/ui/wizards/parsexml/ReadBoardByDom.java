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
    private static List<Board> boards = null;  
    static{  
        try {  
            dbFactory = DocumentBuilderFactory.newInstance();  
            db = dbFactory.newDocumentBuilder();  
        } catch (ParserConfigurationException e) {  
            e.printStackTrace();  
        }  
    }  
      
    public static List<Board> getBoards(String fileName) throws Exception{  
        //将给定 URI 的内容解析为一个 XML 文档,并返回Document对象  
        document = db.parse(fileName);  
        //按文档顺序返回包含在文档中且具有给定标记名称的所有 Element 的 NodeList  
        NodeList boardList = document.getElementsByTagName("board"); 
        boards = new ArrayList<Board>();
        for(int i=0;i<boardList.getLength();i++) {
        	Board board = new Board();
        	org.w3c.dom.Node node = document.getElementsByTagName("board").item(i);
              
            //获取book结点的子节点,包含了Test类型的换行  
            NodeList cList = node.getChildNodes();//System.out.println(cList.getLength());9  
              
            ArrayList<String> contents = new ArrayList<>();  
            for(int j=1;j<cList.getLength();j+=2){                 
                org.w3c.dom.Node cNode = cList.item(j);  
                String content = cNode.getFirstChild().getTextContent();  
                contents.add(content);  
            }  
            
            board.setBoardName(contents.get(0));
            Cpu cpu = new Cpu(contents.get(1),contents.get(2),contents.get(4),
            		contents.get(5),contents.get(6),contents.get(7),contents.get(3),contents.get(8));
            board.setExClk(contents.get(9));
            board.setCpu(cpu);
            boards.add(board); 
    		
        }

		return boards;
          
    }  
}
