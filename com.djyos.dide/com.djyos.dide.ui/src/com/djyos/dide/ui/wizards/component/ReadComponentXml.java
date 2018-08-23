package com.djyos.dide.ui.wizards.component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.Platform;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.djyos.dide.ui.wizards.board.onboardcpu.Chip;
import com.djyos.dide.ui.wizards.board.onboardcpu.OnBoardCpu;

import com.djyos.dide.ui.wizards.board.Board;
import com.djyos.dide.ui.wizards.component.Component;

public class ReadComponentXml {
	private static DocumentBuilderFactory dbFactory = null;
	private static DocumentBuilder db = null;
	private static Document document = null;
	String fullPath = Platform.getInstallLocation().getURL().toString();
	String eclipsePath = fullPath.substring(6,
			(fullPath.substring(0, fullPath.length() - 1)).lastIndexOf("/") + 1);
	List<Component> components = new ArrayList<Component>();
	List<String> componentPaths = new ArrayList<String>();
	
	static {
		try {
			dbFactory = DocumentBuilderFactory.newInstance();
			db = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	//遍历组件及其子组件
	private void traverFiles(File file) {
		File[] allFiles = file.listFiles();
		Arrays.sort(allFiles, new Comparator<File>() {
			public int compare(File f1, File f2) {
				boolean isDiectory = f1.isDirectory();
				if (isDiectory)
					return 1;
				else
					return -1;// 如果 if 中修改为 返回-1 同时此处修改为返回 1 排序就会是递减
			}

			public boolean equals(Object obj) {
				return true;
			}
		});
		for(File f:allFiles) {
			if(f.isFile() && f.getName().endsWith(".xml")) {
				try {						
					getComponent(f);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else if(f.isDirectory()) {
				traverFiles(f);
			}
		}
	}
	
	public List<Component> getComponents(){
		
		String componentPath = eclipsePath+"djysrc/component";
		String djyosPath = eclipsePath+"djysrc/djyos";
		componentPaths.add(componentPath);
		componentPaths.add(djyosPath);
		for(int i=0;i<componentPaths.size();i++) {
			File sourceFile = new File(componentPaths.get(i));
			File[] files = sourceFile.listFiles();
			for(File file:files) {		
				if(file.isDirectory()) {
					traverFiles(file);
				}
			}
		}
//		for(int i=0;i<components.size();i++) {
//			System.out.println("component:  "+components.get(i).getName()+"  "+components.get(i).getSelectable());
//		}
		return components;
		
	}
	
	public List<Component> getComponents(OnBoardCpu onBoardCpu,Board board) {

		String componentPath = eclipsePath+"djysrc/component";
		String djyosPath = eclipsePath+"djysrc/djyos";
		String demoPath = eclipsePath+"djysrc/bsp/boarddrv/demo/"+board.getBoardName()+"/drv";
		componentPaths.add(componentPath);
		componentPaths.add(djyosPath);
		componentPaths.add(demoPath);
		for(int i=0;i<componentPaths.size();i++) {
			File sourceFile = new File(componentPaths.get(i));
			File[] files = sourceFile.listFiles();
			for(File file:files) {		
				if(file.isDirectory()) {
					traverFiles(file);
				}
			}
		}
		
		// 板载cpu的芯片、cpu的src目录
		List<Chip> chips = onBoardCpu.getChips();
		List<String> chipNames = new ArrayList<String>();
		String cpuSrcPath = getCpuSrcPath(onBoardCpu.getCpuName());
		for(int i=0;i<chips.size();i++) {
			chipNames.add(chips.get(i).getChipName());
		}
		String chipPath = eclipsePath + "djysrc/bsp/chipdrv";
		String cpuPath = eclipsePath + "djysrc/bsp/cpudrv";
		if(chips.size()>0) {
			File chipFile = new File(chipPath);
			File[] chipfiles = chipFile.listFiles();
			for (File file : chipfiles) {
				if (file.isDirectory() && chipNames.contains(file.getName())) {
					traverFiles(file);
				}
			}
		}
		if(cpuSrcPath!= null) {
			File cpuFile = new File(cpuSrcPath);
			File[] cpufiles = cpuFile.listFiles();
			for (File file : cpufiles) {
				if (file.isDirectory()) {
					traverFiles(file);
				}
			}
		}
	
		return components;

	}
	
	private String getCpuSrcPath(String cpuName) {
		String sourcePath = eclipsePath+"djysrc/bsp/cpudrv";
		File sourceFile = new File(sourcePath);
		File[] files = sourceFile.listFiles();
		String path = null;
		for(File file:files){//cpudrv下的所有文件 Atmel stm32
			if(file.isDirectory()) {
				getDeapPath(file,cpuName);
				if(deapPath!=null) {
					path = deapPath+"/../src";
					break;
				}
			}
			
		}
		return path;
	}
	
	private String deapPath = null;
	
	public String getDeapPath(File sourceFile,String cpuName) {
		File[] files = sourceFile.listFiles();
		String path = null;
		for (File file : files) {
			if (file.isDirectory()) {
				if(file.getName().equals(cpuName)) {
					deapPath = file.getPath();
					break;
				}else if(!file.getName().equals("include") && !file.getName().equals("src")){
					getDeapPath(file,cpuName);//如果为目录，则继续扫描该目录
				}			
			}
		}
		return deapPath;
	}
	
	public List<Component> getPeripherals(File file) {

		List<Component> peripherals = new ArrayList<Component>();
		File[] files = file.listFiles();
		for(File ifile:files){
			if(ifile.isDirectory()) {
				Component peripheral = new Component();
				peripheral.setName(ifile.getName());
				peripherals.add(peripheral);
			}
		}
		return peripherals;

	}

	private void getComponent(File file) throws Exception {	
		document = db.parse(file);
		Node node = document.getElementsByTagName("component").item(0);
		NodeList cList = node.getChildNodes();
		File parentFile =  file.getParentFile();
		String fileName = file.getName();
		File defaultHFile = new File(parentFile.getPath()+"/"+fileName.substring(fileName.indexOf("_")+1, fileName.indexOf("."))+"_config_default.h");
		for (int j = 0; j < cList.getLength(); j ++) {
			Node cNode = cList.item(j);
			if (cNode.getNodeType() == Node.ELEMENT_NODE) {
				Component newComponent = new Component();
				if(file.getPath().contains("djysrc\\component")) {
					newComponent.setLinkFolder("component");
				}else if(file.getPath().contains("cpudrv")) {
					newComponent.setLinkFolder("cpudrv");
				}else if(file.getPath().contains("third")) {
					newComponent.setLinkFolder("third");
				}else if(file.getPath().contains("chipdrv")) {
					newComponent.setLinkFolder("chipdrv");
				}else if(file.getPath().contains("djyos")){
					newComponent.setLinkFolder("djyos");
				}else if(file.getPath().contains("boarddrv")){
					newComponent.setLinkFolder("boarddrv");
				}
				newComponent.setFileName(file.getParentFile().getPath());

				if(defaultHFile.exists()) {
					List<String> defaultsStrings = new ArrayList<String>();
					int initCodeIndex = 0,configureIndex=0;
					boolean initCodeExist = false,configureExist = false;
					StringBuffer sbInitCode = new StringBuffer("");
					StringBuffer sbConfigure = new StringBuffer("");
					FileReader reader = new FileReader(defaultHFile.getPath());
					BufferedReader br = new BufferedReader(reader);
					String str = null;
					while ((str = br.readLine()) != null) {
						defaultsStrings.add(str);
					}
					for(int i=0;i<defaultsStrings.size();i++) {
						String annotation = defaultsStrings.get(i).replace("//", "").trim();
						if(annotation.startsWith("InitCode")) {
							initCodeIndex = i;
							initCodeExist = true;
						}else if(annotation.startsWith("Configure")) {
							configureIndex = i;
							configureExist = true;
						}
					}
					if(initCodeExist) {
						for(int i=initCodeIndex+1;i<configureIndex;i++) {
							sbInitCode.append(defaultsStrings.get(i)+"\n");
						}
					}
					if(configureExist) {
						for(int i=configureIndex+1;i<defaultsStrings.size();i++) {
							sbConfigure.append(defaultsStrings.get(i)+"\n");
						}
					}	
					
					if(sbInitCode!=null && !sbInitCode.toString().equals("")) {
						newComponent.setCode(sbInitCode.toString().trim());
					}
					
					if(sbConfigure!=null && !sbConfigure.toString().equals("")) {
						newComponent.setConfigure(sbConfigure.toString().trim());
					}
					
					br.close();
					reader.close();
				}
				
				NodeList list = cNode.getChildNodes();
				for (int i = 0; i < list.getLength(); i++) {
					org.w3c.dom.Node dNode = list.item(i);
					String nodeName = dNode.getNodeName();
					if (dNode.getNodeType() == Node.ELEMENT_NODE) {
						if (parentFile.getName().equals("charset")) {
							System.out.println("nodeName:  " + nodeName);
						}
						switch (nodeName) {
						case "name":
							newComponent.setName(dNode.getTextContent());
							break;
						case "parent":
							newComponent.setParent(dNode.getTextContent());
							break;
						case "annotation":
							newComponent.setAnnotation(dNode.getTextContent());
							break;
						case "attribute":
							newComponent.setAttribute(dNode.getTextContent());
							break;
						case "select":
							newComponent.setSelectable(dNode.getTextContent());
							break;
						case "grade":
							newComponent.setGrade(dNode.getTextContent());
							break;
						case "dependence":
							if (!dNode.getTextContent().equals("none")) { 
								newComponent.getDependents().add(dNode.getTextContent());
							}
							break;
						case "weakdependence":
							if (!dNode.getTextContent().equals("none")) {
								newComponent.getWeakDependents().add(dNode.getTextContent());
							}
							break;
						case "mutex":
							if (!dNode.getTextContent().equals("none")) {
								newComponent.getMutexs().add(dNode.getTextContent());
							}
							break;
						}
					}
				}
				
				components.add(newComponent);
			}
		}	
	}
}
