package org.eclipse.cdt.ui.wizards.cpu;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TreeItem;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.eclipse.cdt.ui.wizards.cpu.core.Core;
import org.eclipse.cdt.ui.wizards.cpu.core.memory.Memory;

public class ReadCpuXml {
	private static DocumentBuilderFactory dbFactory = null;
	private static DocumentBuilder db = null;
	private static Document document = null;
	String fullPath = Platform.getInstallLocation().getURL().toString();
	String eclipsePath = fullPath.substring(6,
			(fullPath.substring(0, fullPath.length() - 1)).lastIndexOf("/") + 1);
	List<CpuBak> cpus = new ArrayList<CpuBak>();
	private CpuBak cpu;
	static {
		try {
			dbFactory = DocumentBuilderFactory.newInstance();
			db = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public List<CpuBak> getAllCpus() {
		
		String sourcePath = eclipsePath+"djysrc/bsp/cpudrv/stm32";
		CpuBak cpu = new CpuBak();
		File sourceFile = new File(sourcePath);
		getCpus(cpu,sourceFile);
		return cpus;
		
	}
	
	public void getCpus(CpuBak cpu, File sourceFile) {
		String filePath = sourceFile.getPath();
		File[] files = sourceFile.listFiles();
		boolean isDeap = true;
		for (File file : files) {
			if (file.isDirectory()) {
				isDeap = false;
			}
		}
		for (File file : files) {
			String curFilePath = filePath + "\\" + file.getName();
			if (file.isDirectory()) {
				getCpus(cpu, new File(curFilePath));
			} else if (file.getName().endsWith(".xml")) {
				try {			
					cpu = getCpu(cpu,curFilePath);
					if(isDeap) {
						CpuBak newCpu = new CpuBak(cpu.getName(),cpu.getCoreNum(),cpu.getType(),cpu.getFamily(),cpu.getArchitecture(),
								cpu.getInstructionSet(),cpu.getEndianeness(),cpu.getFlashStart(),cpu.getFlashSize(),
								cpu.getRamStart(),cpu.getRamSize(),cpu.getFpuABI(),cpu.getFpuType(),cpu.getResetAddr(),cpu.getCategory());
						cpus.add(newCpu);
					} 
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static CpuBak getCpu(CpuBak cpu,String fileName) throws Exception {
		// 将给定 URI 的内容解析为一个 XML 文档,并返回Document对象
		document = db.parse(fileName);
		org.w3c.dom.Node node = document.getElementsByTagName("cpu").item(0);
		// 获取book结点的子节点,包含了Test类型的换行
		NodeList cList = node.getChildNodes();// System.out.println(cList.getLength());9

		ArrayList<String> contents = new ArrayList<>();
		for (int j = 1; j < cList.getLength(); j += 2) {
			org.w3c.dom.Node cNode = cList.item(j);
			String nodeName = cNode.getNodeName();
			String content = cNode.getFirstChild().getTextContent();
			switch(nodeName) {
			case "name":cpu.setName(content);break;
			case "coreNum":cpu.setCoreNum(Integer.parseInt(content));break;
			case "type":cpu.setType(content);break;
			case "family":cpu.setFamily(content);break;
			case "architecture":cpu.setArchitecture(content);break;
			case "instructionSet":cpu.setInstructionSet(content);break;
			case "endianeness":cpu.setEndianeness(content);break;
			case "flashStart":cpu.setFlashStart(content);break;
			case "flashSize":cpu.setFlashSize(content);break;
			case "ramStart":cpu.setRamStart(content);break;
			case "ramSize":cpu.setRamSize(content);break;
			case "fpuABI":cpu.setFpuABI(content);break;
			case "fpuType":cpu.setFpuType(content);break;
			case "resetAddr":cpu.setResetAddr(content);break;
			case "category":cpu.setCategory(content);break;			
			}
//			contents.add(content);
		}
		
		return cpu;   
    }  
	
	public Cpu unitCpu(Cpu cpu,File file) throws SAXException, IOException {
		// 将给定 URI 的内容解析为一个 XML 文档,并返回Document对象
		document = db.parse(file);
		
		NodeList nameList = document.getElementsByTagName("name");
//		org.w3c.dom.Node nameNode = document.getElementsByTagName("name").item(0);
		for(int i=0;i<nameList.getLength();i++) {
			String cpuName = nameList.item(i).getFirstChild().getTextContent();
			cpu.setCpuName(cpuName);
		}

		NodeList coreList = document.getElementsByTagName("core");
		if(coreList.getLength() == 0) {
			NodeList cpuList = document.getElementsByTagName("cpu");
			org.w3c.dom.Node cNode = cpuList.item(0);
			NodeList cList = cNode.getChildNodes();
			List<Core> cores = cpu.getCores();
			if(cores.size() == 0) {//未配置内核个数
				Core core = new Core();
				for(int i=1;i<cList.getLength();i+=2) {
					org.w3c.dom.Node node = cList.item(i);
					String nodeName = node.getNodeName();
					String content = node.getFirstChild().getTextContent();
					switch(nodeName) {
					case "resetAddr":
							core.setResetAddr(content);
						break;
					case "family": 
							core.setFamily(content); 
						break;
					case "arch": 
							core.setArch(content);
						break;
					case "fpuType":
							core.setFpuType(content);
						break;
					}
			}
				cores.add(core);
			}else {
				for(int i=1;i<cList.getLength();i+=2) {
					org.w3c.dom.Node node = cList.item(i);
					String nodeName = node.getNodeName();
					String content = node.getFirstChild().getTextContent();
					switch(nodeName) {
					case "resetAddr":
						for(int j=0;j<cores.size();j++) {
							cores.get(j).setResetAddr(content);
						}
						break;
					case "family": 
						for(int j=0;j<cores.size();j++) {
							cores.get(j).setFamily(content); 
						}
						break;
					case "arch": 
						for(int j=0;j<cores.size();j++) {
							cores.get(j).setArch(content);
						}
						break;
					case "fpuType":
						for(int j=0;j<cores.size();j++) {
							cores.get(j).setFpuType(content);
						}
						break;
					}
			}
			
			}
		}else {
			cpu.setCoreNum(coreList.getLength());
			List<Core> cores;
			boolean isClean = true;
			if(cpu.getCores().size() == 0) {
				cores = new ArrayList<Core>();
			}else {
				isClean = false;
				cores = cpu.getCores();
				System.out.println(cpu.getCpuName()+"\n"+
						cores.get(0).getFpuType()+"\n");
			}
			for(int i=0;i<coreList.getLength();i++) {
				Core core;				
				if(cpu.getCores().size() == 0) {
					core = new Core();
				}else {				
					core = cpu.getCores().get(i);
				}
				
				org.w3c.dom.Node node = coreList.item(i);  
		        NamedNodeMap namedNodeMap = node.getAttributes();  
		        String id = namedNodeMap.getNamedItem("id").getTextContent();
		        core.setId(Integer.parseInt(id));
		        
		        NodeList cList = node.getChildNodes();
		        for (int j = 1; j < cList.getLength(); j += 2) {
					org.w3c.dom.Node cNode = cList.item(j);
					String nodeName = cNode.getNodeName();
					String content = cNode.getFirstChild().getTextContent();
					switch(nodeName) {
					case "resetAddr":core.setResetAddr(content);break;
					case "family": core.setFamily(content); break;
					case "arch": core.setArch(content); break;
					case "fpuType":core.setFpuType(content);break;
					case "memory":				
						NodeList memoryList = cNode.getChildNodes();
						Memory memory = new Memory();
						for (int k = 1; k < memoryList.getLength(); k += 2) {				
							org.w3c.dom.Node mNode = memoryList.item(k);
							String mName = mNode.getNodeName();
							String mContent = mNode.getFirstChild().getTextContent();
							if(mName.equals("memoryType")) {
								memory.setType(mContent);
							}else if(mName.equals("startAddr")) {
								memory.setStartAddr(mContent);
							}else if(mName.equals("size")) {
								memory.setSize(Integer.parseInt(mContent));
							}
						}					
						core.getMemorys().add(memory);
						break;			
					}
//					contents.add(content);
				}
		        if(isClean) {
		        	cores.add(core);
		        }
				
			}
			if(isClean) {
				cpu.setCores(cores);
			}
			
		}
		return cpu;   
	}
}
