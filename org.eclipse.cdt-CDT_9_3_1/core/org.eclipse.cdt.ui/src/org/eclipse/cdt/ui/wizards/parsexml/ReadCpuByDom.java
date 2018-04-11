package org.eclipse.cdt.ui.wizards.parsexml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

public class ReadCpuByDom {
	 	private static DocumentBuilderFactory dbFactory = null;  
	    private static DocumentBuilder db = null;  
	    private static Document document = null;  
	    private static List<Cpu> cpus = null;  
	    static{  
	        try {  
	            dbFactory = DocumentBuilderFactory.newInstance();  
	            db = dbFactory.newDocumentBuilder();  
	        } catch (ParserConfigurationException e) {  
	            e.printStackTrace();  
	        }  
	    }  
	      
	    public static List<Cpu> getCpus(String fileName) throws Exception{  
	        //将给定 URI 的内容解析为一个 XML 文档,并返回Document对象  
	        document = db.parse(fileName);  
	        //按文档顺序返回包含在文档中且具有给定标记名称的所有 Element 的 NodeList  
	        NodeList cpuList = document.getElementsByTagName("cpu");  
	        cpus = new ArrayList<Cpu>();  
	        //遍历cpus  
	        for(int i=0;i<cpuList.getLength();i++){  
	        	Cpu cpu = new Cpu();  
	            //获取第i个cpu结点  
	            org.w3c.dom.Node node = cpuList.item(i);  
	            //获取第i个cpu的所有属性  
	            NamedNodeMap namedNodeMap = node.getAttributes();  
	            //获取已知名为id的属性值  
	            String id = namedNodeMap.getNamedItem("id").getTextContent();//System.out.println(id);  
	            cpu.setId(Integer.parseInt(id));  
	              
	            //获取book结点的子节点,包含了Test类型的换行  
	            NodeList cList = node.getChildNodes();//System.out.println(cList.getLength());9  
	              
	            //将一个cpu里面的属性加入数组  
	            ArrayList<String> contents = new ArrayList<>();  
	            for(int j=1;j<cList.getLength();j+=2){                 
	                org.w3c.dom.Node cNode = cList.item(j);  
	                String content = cNode.getFirstChild().getTextContent();  
	                contents.add(content);  
	            }  
	            
	            cpu.setDevice(contents.get(0));
	            cpu.setCore(contents.get(1));
	            cpu.setArchitecture(contents.get(2));
	            cpu.setFlashStart(contents.get(3));
	            cpu.setFlashSize(contents.get(4));
	            cpu.setRamStart(contents.get(5));
	            cpu.setRamSize(contents.get(6));
	            cpu.setFpuType(contents.get(7));
	            cpu.setCategory(contents.get(8));
	            cpus.add(cpu); 
	        }            
	        return cpus;  
	          
	    }  
}
