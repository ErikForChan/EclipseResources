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
import org.eclipse.cdt.ui.wizards.cpu.core.memory.CoreMemory;

public class ReadCpuXml {
	private static DocumentBuilderFactory dbFactory = null;
	private static DocumentBuilder db = null;
	private static Document document = null;
	String fullPath = Platform.getInstallLocation().getURL().toString();
	String eclipsePath = fullPath.substring(6,
			(fullPath.substring(0, fullPath.length() - 1)).lastIndexOf("/") + 1);
	List<Cpu> cpus = new ArrayList<Cpu>();
	private Cpu cpu;
	static {
		try {
			dbFactory = DocumentBuilderFactory.newInstance();
			db = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	//获取当前路径下所有Cpu信息，通过扫描各个目下的xml文件
	public List<Cpu> getAllCpus() {
		
		String sourcePath = eclipsePath+"djysrc/bsp/cpudrv/stm32";
		File sourceFile = new File(sourcePath);
		getCpus(sourceFile);
		return cpus;
		
	}
	//遍历父目录，当父目录名为cpudrv时停止扫描
	private void traverseParents(Cpu cpu,File parentFile) {
		if(!parentFile.getName().contains("cpudrv")) {		
				File xmlFile = getXmlFile(parentFile);
				try {
					unitCpu(cpu,xmlFile);
				}catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				parentFile = parentFile.getParentFile();
				traverseParents(cpu,parentFile);
		}
	}
	//获取某个文件夹下的xml文件
	private File getXmlFile(File parentFile) {
		File file =null;
		File[] files = parentFile.listFiles();
		for(int i=0;i<files.length;i++){
			if(files[i].getName().endsWith(".xml")) {
				file = files[i];
			}
		}
		return file;
	}
	//获取Cpu.xml的信息
	public void getCpus(File sourceFile) {
		String filePath = sourceFile.getPath();
		File[] files = sourceFile.listFiles();
		boolean isDeap = true;//是否扫描到最深层的文件
		for (File file : files) {
			if (file.isDirectory() && !file.getName().equals("include") && !file.getName().equals("src")) {
				isDeap = false;
			}
		}
		for (File file : files) {
			String curFilePath = filePath + "\\" + file.getName();
			if (file.isDirectory()) {
				if(!file.getName().equals("include") && !file.getName().equals("src")){
					getCpus(new File(curFilePath));//如果为目录，则继续扫描该目录
				}			
			} else if (file.getName().endsWith(".xml")) {//如果为文件，且为xml格式的文件，则遍历所有父目录，获取当前cpu的信息
				try {			
					Cpu cpu = new Cpu();
					File parentFile = file.getParentFile();
					traverseParents(cpu,parentFile);
					if(isDeap) {
						Cpu newCpu = new Cpu(cpu.getCpuName(),cpu.getCores());
						cpus.add(newCpu);
					} 
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public Cpu unitCpu(Cpu cpu,File file) throws Exception {
		// 将给定 URI 的内容解析为一个 XML 文档,并返回Document对象
		document = db.parse(file);
		
		NodeList nameList = document.getElementsByTagName("cpuName");
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
					case "type":
							core.setType(content);
						break;
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
					case "type":
						for(int j=0;j<cores.size();j++) {
							cores.get(j).setType(content);
						}
						break;
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
					case "type":core.setType(content);break;
					case "resetAddr":core.setResetAddr(content);break;
					case "family": core.setFamily(content); break;
					case "arch": core.setArch(content); break;
					case "fpuType":core.setFpuType(content);break;
					case "memory":				
						NodeList memoryList = cNode.getChildNodes();
						CoreMemory memory = new CoreMemory();
						for (int k = 1; k < memoryList.getLength(); k += 2) {				
							org.w3c.dom.Node mNode = memoryList.item(k);
							String mName = mNode.getNodeName();
							String mContent = mNode.getFirstChild().getTextContent();
							if(mName.equals("type")) {
								memory.setType(mContent);
							}else if(mName.equals("startAddr")) {
								memory.setStartAddr(mContent);
							}else if(mName.equals("size")) {
								memory.setSize(Integer.parseInt(mContent.substring(0,mContent.length())));
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
