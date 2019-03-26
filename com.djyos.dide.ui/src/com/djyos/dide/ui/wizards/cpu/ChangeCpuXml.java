package com.djyos.dide.ui.wizards.cpu;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.objects.Cpu;

public class ChangeCpuXml {
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
	
	public static void changeCoreNum(File f, int coreNum) {
		// TODO Auto-generated method stub
		try {
			document = db.parse(f);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NodeList coreList = document.getElementsByTagName("core");
		Node pNode = coreList.item(0).getParentNode();
		if(coreList.getLength() < coreNum) {
			for(int i=coreList.getLength();i<coreNum;i++) {
				 Element coreNode=document.createElement("core");
				 coreNode.setAttribute("id", String.valueOf(i + 1));
				 pNode.appendChild(coreNode);
			}
		}else if(coreList.getLength() > coreNum) {
			for(int i=coreNum;i<coreList.getLength();i++) {
				pNode.removeChild(coreList.item(i));
			}
		} 
		
		try {
			tran(document,f);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void tran(Document doc,File file) throws TransformerException{
        TransformerFactory tff = TransformerFactory.newInstance();
        Transformer tf = tff.newTransformer();
        tf.setOutputProperty(OutputKeys.INDENT, "yes");// 增加换行缩进，但此时缩进默认为0
        tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");// 设置缩进为2
        tf.setOutputProperty("encoding", "UTF-8");
        DOMSource xmlSource = new DOMSource(doc);
        StreamResult streamresult = new StreamResult(file);
        tf.transform(xmlSource, streamresult);
    }
	
}
