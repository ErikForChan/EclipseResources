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
	        //������ URI �����ݽ���Ϊһ�� XML �ĵ�,������Document����  
	        document = db.parse(fileName);  
	        //���ĵ�˳�򷵻ذ������ĵ����Ҿ��и���������Ƶ����� Element �� NodeList  
	        NodeList cpuList = document.getElementsByTagName("cpu");  
	        cpus = new ArrayList<Cpu>();  
	        //����cpus  
	        for(int i=0;i<cpuList.getLength();i++){  
	        	Cpu cpu = new Cpu();  
	            //��ȡ��i��cpu���  
	            org.w3c.dom.Node node = cpuList.item(i);  
	            //��ȡ��i��cpu����������  
	            NamedNodeMap namedNodeMap = node.getAttributes();  
	            //��ȡ��֪��Ϊid������ֵ  
	            String id = namedNodeMap.getNamedItem("id").getTextContent();//System.out.println(id);  
	            cpu.setId(Integer.parseInt(id));  
	              
	            //��ȡbook�����ӽڵ�,������Test���͵Ļ���  
	            NodeList cList = node.getChildNodes();//System.out.println(cList.getLength());9  
	              
	            //��һ��cpu��������Լ�������  
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
