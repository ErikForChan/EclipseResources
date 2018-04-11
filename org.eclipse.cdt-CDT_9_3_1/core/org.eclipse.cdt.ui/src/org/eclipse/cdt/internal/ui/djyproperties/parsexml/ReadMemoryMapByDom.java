package org.eclipse.cdt.internal.ui.djyproperties.parsexml;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.eclipse.cdt.internal.ui.djyproperties.MemoryMap;

public class ReadMemoryMapByDom {
	private static DocumentBuilderFactory dbFactory = null;  
    private static DocumentBuilder db = null;  
    private static Document document = null;  
    private static MemoryMap mMap;
    static{  
        try {  
            dbFactory = DocumentBuilderFactory.newInstance();  
            db = dbFactory.newDocumentBuilder();  
        } catch (ParserConfigurationException e) {  
            e.printStackTrace();  
        }  
    }  
    
    public static MemoryMap getMemoryMap(String filePath) {
    	//将给定 URI 的内容解析为一个 XML 文档,并返回Document对象  
        try {
			document = db.parse(filePath);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
        //按文档顺序返回包含在文档中且具有给定标记名称的所有 Element 的 NodeList  
        NodeList memoryList = document.getElementsByTagName("memoryMap");         
        org.w3c.dom.Node node = memoryList.item(0);  
        NamedNodeMap namedNodeMap = node.getAttributes();           
        NodeList cList = node.getChildNodes();         
        ArrayList<String> contents = new ArrayList<>();  
        for(int j=1;j<cList.getLength();j+=2){                 
            org.w3c.dom.Node cNode = cList.item(j);  
            String content = cNode.getFirstChild().getTextContent();  
            contents.add(content);  
        }  
        
        mMap = new MemoryMap(contents.get(0),contents.get(1),contents.get(2),contents.get(3),contents.get(4),
        		contents.get(5),contents.get(6),contents.get(7),contents.get(8));		
        
    	return mMap;
    }
}
