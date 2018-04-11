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
    	//������ URI �����ݽ���Ϊһ�� XML �ĵ�,������Document����  
        try {
			document = db.parse(filePath);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
        //���ĵ�˳�򷵻ذ������ĵ����Ҿ��и���������Ƶ����� Element �� NodeList  
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
