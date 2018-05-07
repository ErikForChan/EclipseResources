package org.eclipse.cdt.ui.wizards.board.onboardcpu;

import java.io.File;
import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.eclipse.cdt.ui.wizards.component.Component;

public class CreatTBoardXml {
	 DocumentBuilderFactory factory =  DocumentBuilderFactory.newInstance();  
	 	
     public void creatBoardXml(TBoard board,File file) {
        try {
        	factory.setIgnoringElementContentWhitespace(false);
        	DocumentBuilder builder = factory.newDocumentBuilder();             
        	Document document = builder.newDocument();
        	 //创建属性名、赋值  
        	Element boardElement = document.createElement("board");
        	Element boardName = document.createElement("boardName");
        	boardName.setTextContent(board.getBoardName());
        	boardElement.appendChild(boardName);
        	
        	List<OnBoardCpu> onBoardCpus = board.getOnBoardCpus();
        	//创建Cpu集合
        	for(int i=0;i<onBoardCpus.size();i++) {
        		System.out.println("onBoardCpu................");
        		Element cpuElement = document.createElement("cpu");
        		
        		Element cpuName = document.createElement("cpuName");
        		cpuName.setTextContent(String.valueOf(onBoardCpus.get(i).getCpuName()));
        		cpuElement.appendChild(cpuName);
        		
        		Element mianClk = document.createElement("mianClk");
            	mianClk.setTextContent(String.valueOf(onBoardCpus.get(i).getMianClk()));
            	cpuElement.appendChild(mianClk);
            	
            	if(onBoardCpus.get(i).getRtcClk() != 0) {
            		Element rtcClk = document.createElement("rtcClk");
            		rtcClk.setTextContent(String.valueOf(onBoardCpus.get(i).getRtcClk()));
            		cpuElement.appendChild(rtcClk);
            	}
            	//创建芯片集合
            	List<Chip> chips = onBoardCpus.get(i).getChips();
            	for(int j=0;j<chips.size();j++) {
            		Element chipElement = document.createElement("chip");
            		Element theInterface = document.createElement("interface");
            		theInterface.setTextContent(chips.get(j).getTheInterface());
            		Element chipName = document.createElement("chipName");
            		chipName.setTextContent(chips.get(j).getChipName());
            		
            		chipElement.appendChild(theInterface);
            		chipElement.appendChild(chipName);
            		cpuElement.appendChild(chipElement);
            	}
            	//创建内存集合
            	List<Memory> memorys = onBoardCpus.get(i).getMemorys();
            	for(int j=0;j<memorys.size();j++) {
            		Element memoryElement = document.createElement("memory");
            		Element type = document.createElement("type");
            		type.setTextContent(memorys.get(j).getType());
            		Element startAddr = document.createElement("startAddr");
            		startAddr.setTextContent(memorys.get(j).getStartAddr());
            		Element size = document.createElement("size");
            		size.setTextContent(String.valueOf(memorys.get(j).getSize()));
            		
            		memoryElement.appendChild(type);
            		memoryElement.appendChild(startAddr);
            		memoryElement.appendChild(size);
            		cpuElement.appendChild(memoryElement);
            	}
            	//创建外设集合
            	List<Component> peripherals = onBoardCpus.get(i).getPeripherals();
            	for(int j=0;j<peripherals.size();j++) {
            		Element peripheral = document.createElement("peripheral");
            		peripheral.setTextContent(peripherals.get(j).getName());
            		cpuElement.appendChild(peripheral);
            	}

        		boardElement.appendChild(cpuElement);
        	}
        	
           document.appendChild(boardElement);
           TransformerFactory transformerFactory = TransformerFactory.newInstance();  
           Transformer transformer = transformerFactory.newTransformer();  
           transformer.setOutputProperty(OutputKeys.INDENT, "yes");//增加换行缩进，但此时缩进默认为0  
           transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");//设置缩进为2
           transformer.setOutputProperty("encoding", "UTF-8");  
                 
           StringWriter writer = new StringWriter();  
           transformer.transform(new DOMSource(document), new StreamResult(writer));  
           transformer.transform(new DOMSource(document), new StreamResult(file));
           writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     }
}
