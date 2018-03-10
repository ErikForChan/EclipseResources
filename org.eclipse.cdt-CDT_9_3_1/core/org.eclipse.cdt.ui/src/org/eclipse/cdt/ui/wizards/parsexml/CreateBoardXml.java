package org.eclipse.cdt.ui.wizards.parsexml;
import java.io.File;
import java.io.StringWriter;  
  
import javax.xml.parsers.DocumentBuilder;  
import javax.xml.parsers.DocumentBuilderFactory;  
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;   
import javax.xml.transform.TransformerFactory;  
import javax.xml.transform.dom.DOMSource;  
import javax.xml.transform.stream.StreamResult;  
  
import org.w3c.dom.Document;  
import org.w3c.dom.Element;

public class CreateBoardXml {
	
	 DocumentBuilderFactory factory =  DocumentBuilderFactory.newInstance();  
	 	
     public void creatBoard(Board board,String xmlPath) {
        try {
        	factory.setIgnoringElementContentWhitespace(false);
        	DocumentBuilder builder = factory.newDocumentBuilder();             
        	Document document = builder.parse(xmlPath); 
        	 //��������������ֵ  
        	Element root = document.getDocumentElement();  
        	Element boardElement = document.createElement("board");
        	
        	Element boardName = document.createElement("boardName");
        	boardName.setTextContent(board.getBoardName());
            Element device = document.createElement("cpu");
            device.setTextContent(board.cpu.getDevice());
            Element exClk = document.createElement("exClk");
            exClk.setTextContent(board.getExClk());
            
            boardElement.appendChild(boardName);
            boardElement.appendChild(device);
            boardElement.appendChild(exClk);
            
            root.appendChild(boardElement);      

           TransformerFactory transformerFactory = TransformerFactory.newInstance();  
           Transformer transformer = transformerFactory.newTransformer();  
           transformer.setOutputProperty(OutputKeys.INDENT, "yes");//���ӻ�������������ʱ����Ĭ��Ϊ0  
           transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");//��������Ϊ2
           transformer.setOutputProperty("encoding", "UTF-8");  
                 
           StringWriter writer = new StringWriter();  
           transformer.transform(new DOMSource(document), new StreamResult(writer));  
           transformer.transform(new DOMSource(document), new StreamResult(new File(xmlPath)));
           writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     }
    
}
