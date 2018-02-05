package org.eclipse.cdt.ui.wizards.parsexml;
import java.io.File;  
import java.io.StringWriter;  
  
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

public class CreateBoardXml {
	
	 DocumentBuilderFactory factory =  DocumentBuilderFactory.newInstance();  
	 	
     public void creatBoard(Board board,File file) {
        try {
        	DocumentBuilder builder = factory.newDocumentBuilder();             
            Document document = builder.newDocument();   
        	 //创建属性名、赋值  
            Element root = document.createElement("board");  
            
            Element device = document.createElement("device");
            device.setTextContent(board.cpu.getDevice());
            Element core = document.createElement("core");
            core.setTextContent(board.cpu.getCore());
            Element architecture = document.createElement("architecture");
            architecture.setTextContent(board.cpu.getArchitecture());
            Element flashStart = document.createElement("flashStart");
            flashStart.setTextContent(board.cpu.getFlashStart());
            Element flashSize = document.createElement("flashSize");
            flashSize.setTextContent(board.cpu.getFlashSize());
            Element ramStart = document.createElement("ramStart");
            ramStart.setTextContent(board.cpu.getRamStart());
            Element ramSize = document.createElement("ramSize");
            ramSize.setTextContent(board.cpu.getRamSize());
            Element fpuType = document.createElement("fpuType");
            fpuType.setTextContent(board.cpu.getFpuType());
            Element exClk = document.createElement("exClk");
            exClk.setTextContent(board.getExClk());
            Element ibootSize = document.createElement("ibootSize");
            ibootSize.setTextContent(board.getIbootSize());
            
            root.appendChild(device);
            root.appendChild(core);
            root.appendChild(architecture);
            root.appendChild(flashStart);
            root.appendChild(flashSize);
            root.appendChild(ramStart);
            root.appendChild(ramSize);
            root.appendChild(fpuType);
            root.appendChild(exClk);
            root.appendChild(ibootSize);
            document.appendChild(root);      

           TransformerFactory transformerFactory = TransformerFactory.newInstance();  
           Transformer transformer = transformerFactory.newTransformer();  
           transformer.setOutputProperty(OutputKeys.INDENT, "yes");//增加换行缩进，但此时缩进默认为0  
           transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");//设置缩进为2
           transformer.setOutputProperty("encoding", "UTF-8");  
                 
           StringWriter writer = new StringWriter();  
           transformer.transform(new DOMSource(document), new StreamResult(writer));  
           transformer.transform(new DOMSource(document), new StreamResult(file));
		} catch (ParserConfigurationException | TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     }
    
}
