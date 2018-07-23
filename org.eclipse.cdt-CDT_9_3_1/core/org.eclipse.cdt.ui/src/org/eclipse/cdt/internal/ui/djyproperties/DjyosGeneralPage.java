package org.eclipse.cdt.internal.ui.djyproperties;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.eclipse.cdt.ui.wizards.board.Board;
import org.eclipse.cdt.ui.wizards.board.ReadBoardXml;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.Chip;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.OnBoardCpu;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.OnBoardMemory;
import org.eclipse.cdt.ui.wizards.component.Component;
import org.eclipse.cdt.ui.wizards.cpu.Cpu;
import org.eclipse.cdt.ui.wizards.cpu.ReadCpuXml;
import org.eclipse.cdt.ui.wizards.cpu.core.Core;
import org.eclipse.cdt.ui.wizards.cpu.core.memory.CoreMemory;
import org.eclipse.cdt.ui.wizards.djyosProject.ReadHardWareDesc;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;

public class DjyosGeneralPage extends PropertyPage{
	private OnBoardCpu onBoardCpu = null;
	private Board sBoard = null;
	private List<Cpu> cpusList = null;
	
	private String getDIDEPath() {
		String fullPath = Platform.getInstallLocation().getURL().toString();
		String eclipsePath = fullPath.substring(6,(fullPath.substring(0,fullPath.length()-1)).lastIndexOf("/")+1);
		return eclipsePath;
	}
	
	private IProject getProject(){
		Object	element	= getElement();
		IProject project	= null;
		
		if (element instanceof IProject) {
			project = (IProject) element;
		} else if (element instanceof IAdaptable) {
			project= ((IAdaptable)element).getAdapter(IProject.class);
		}
		return project;
	}
	
	@Override
	protected Control createContents(Composite parent) {
		// TODO Auto-generated method stub
		Group descGroup = ControlFactory.createGroup(parent, "硬件描述", 1);
		descGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		descGroup.setLayout(new GridLayout());
		ReadCpuXml rcx = new ReadCpuXml();
		cpusList =  rcx.getAllCpus();
		getBoardAndCpu();
		List<OnBoardCpu> onBoardCpus = sBoard.getOnBoardCpus();
		String infos = "";
		infos+="板件："+sBoard.getBoardName();
		if(onBoardCpus.size()>1) {
			for(int i=0;i<onBoardCpus.size();i++) {
				List<Component> peripherals = onBoardCpus.get(i).getPeripherals();
				List<Chip> chips = onBoardCpus.get(i).getChips();
				List<OnBoardMemory> memorys = onBoardCpus.get(i).getMemorys();
				infos+="\n\t板载Cpu"+(i+1)+"："+onBoardCpus.get(i).getCpuName();
				for(Cpu cpu:cpusList) {
					if(cpu.getCpuName().equals(onBoardCpus.get(i).getCpuName())) {
						List<Core> cores = cpu.getCores();
						for(int j=0;j<cores.size();j++) {
							infos+="\n\t\t内核"+(j+1)+"：";
							infos+="\n\t\t\t类型："+cores.get(j).getType();
							infos+="\n\t\t\t架构："+cores.get(j).getArch();
							infos+="\n\t\t\t家族："+cores.get(j).getFamily();
							infos+="\n\t\t\t浮点："+cores.get(j).getFpuType();
							List<CoreMemory> coreMemorys = cores.get(j).getMemorys();
							infos+="\n\t\t\t片内内存：";
							if(coreMemorys.size()==0) {
								infos+="无";
							}else {
								for(int k=0;k<coreMemorys.size();k++) {
									infos+="\n\t\t\t\tMemory"+(k+1)+"："+coreMemorys.get(k).getType()+"，"+coreMemorys.get(k).getStartAddr()+"，"+coreMemorys.get(k).getSize();
								}
							}
							
						}
						break;
					}
				}
				if(onBoardCpus.get(i).getRtcClk()!=0) {
					infos+="\n\tRtc频率："+onBoardCpus.get(i).getRtcClk();
				}
				infos+="\n\t板载设备：";
				if(chips.size()==0) {
					infos+="无";
				}else {
					for(int j=0;j<chips.size();j++) {
						infos+= (j!=0?"，":"")+chips.get(j).getChipName();
					}
				}
				
				infos+="\n\t主晶振频率："+onBoardCpus.get(i).getMianClk();
				infos+="\n\tCpu外设：";
				if(peripherals.size()==0) {
					infos+="无";
				}else {
					for(int j=0;j<peripherals.size();j++) {
						infos+= peripherals.get(j).getName()+(j!=peripherals.size()-1?"，":"");
						if(j%3==0 && j!=0) {
							infos+="\n\t";
						}
					}	
				}
				
				infos+="\n\t片外内存：";
				if(memorys.size()==0) {
					infos+="无";
				}else {
					for(int j=0;j<memorys.size();j++) {
						infos+="\n\t\tMemory"+(j+1)+"："+memorys.get(j).getType()+"，"+memorys.get(j).getStartAddr()+"，"+memorys.get(j).getSize();
					}
				}
				
			}
			
		}else if(onBoardCpus.size() == 1){
			List<Component> peripherals = onBoardCpus.get(0).getPeripherals();
			List<Chip> chips = onBoardCpus.get(0).getChips();
			List<OnBoardMemory> memorys = onBoardCpus.get(0).getMemorys();
			infos+="\n\t板载Cpu"+"："+onBoardCpus.get(0).getCpuName();
			
			for(Cpu cpu:cpusList) {
				if(cpu.getCpuName().equals(onBoardCpus.get(0).getCpuName())) {
					List<Core> cores = cpu.getCores();
					for(int j=0;j<cores.size();j++) {
						infos+="\n\t\t内核"+(j+1)+"：";
						infos+="\n\t\t\t类型："+cores.get(j).getType();
						infos+="\n\t\t\t架构："+cores.get(j).getArch();
						infos+="\n\t\t\t家族："+cores.get(j).getFamily();
						infos+="\n\t\t\t浮点："+cores.get(j).getFpuType();
						List<CoreMemory> coreMemorys = cores.get(j).getMemorys();
						infos+="\n\t\t\t片内内存：";
						if(coreMemorys.size()==0) {
							infos+="无";
						}else {
							for(int k=0;k<coreMemorys.size();k++) {
								infos+="\n\t\t\t\tMemory"+(k+1)+"："+coreMemorys.get(k).getType()+"，"+coreMemorys.get(k).getStartAddr()+"，"+coreMemorys.get(k).getSize();
							}	
						}
					}
					break;
				}
			}
			if(onBoardCpus.get(0).getRtcClk()!=0) {
				infos+="\n\tRtc频率："+onBoardCpus.get(0).getRtcClk();
			}
			infos+="\n\t板载设备：";
			if(chips.size()==0) {
				infos+="无";
			}else {
				for(int j=0;j<chips.size();j++) {
					infos+= (j!=0?"，":"")+chips.get(j).getChipName();
				}
			}
			
			infos+="\n\t主晶振频率："+onBoardCpus.get(0).getMianClk();
			infos+="\n\tCpu外设：";
			if(memorys.size()==0) {
				infos+="无";
			}else {
				for(int j=0;j<peripherals.size();j++) {
					infos+= peripherals.get(j).getName()+(j!=peripherals.size()-1?"，":"");
					if(j%3==0 && j!=0) {
						infos+="\n\t";
					}
				}	
			}
			infos+="\n\t片外内存：";
			if(memorys.size()==0) {
				infos+="无";
			}else {
				for(int j=0;j<memorys.size();j++) {
					infos+="\n\t\tMemory"+(j+1)+"："+memorys.get(j).getType()+"，"+memorys.get(j).getStartAddr()+"，"+memorys.get(j).getSize();
				}
			}
			
		}
		
		Label descLabel = new Label(descGroup,SWT.NONE);
		descLabel.setText(infos);
		return descGroup;
	}
	
	private void getBoardAndCpu() {
		IProject project = getProject();
		String srcPath = project.getLocation().toString()+"/src";
		File hardWardInfoFile = new File(srcPath+"/../"+"data/hardware_info.xml");
		ReadHardWareDesc rhwd = new ReadHardWareDesc();
		List<String> hardwares = rhwd.getHardWares(hardWardInfoFile);
		String cpuName=hardwares.get(1);
		String boardName=hardwares.get(0);
//		IFile pfile = project.getFile(".project");
//		DocumentBuilderFactory factory =  DocumentBuilderFactory.newInstance();  
//		factory.setIgnoringElementContentWhitespace(true);    
//		Document doc = null;
//		DocumentBuilder db;
//		try {
//			db = factory.newDocumentBuilder();
//			doc = db.parse(pfile.getLocation().toFile());
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		
//		// 获取根节点
//        Element root = doc.getDocumentElement();
//        NodeList linkList = doc.getElementsByTagName("link");
//        boolean boardGet = false;
//        boolean cpuGet = false;
//        for(int i=0;i<linkList.getLength();i++) {
//        	Node node = linkList.item(i);
//        	NodeList cList = node.getChildNodes();
//			for (int j = 1; j < cList.getLength(); j += 2) {
//				org.w3c.dom.Node cNode = cList.item(j);
//				String nodeName = cNode.getNodeName();
//				String linkContent = cNode.getTextContent();		
//				//节点名字为name
//				if(nodeName.equals("name")) {
//					if(linkContent.contains("src/libos/bsp/boarddrv/")) {												
//						boardName = linkContent.replaceAll("src/libos/bsp/boarddrv/", "");
//						boardGet = true;
//					}else if(linkContent.contains("src/libos/bsp/cpudrv/")) {							
//						cpuName = linkContent.replaceAll("src/libos/bsp/cpudrv/", "");
//						cpuGet = true;	
//					}
//					break;
//				}  	        	
//			}      
//			if(boardGet && cpuGet){
//				break;
//			}
//        }
		
		

		ReadBoardXml rbx = new ReadBoardXml();
		List<Board> boards = new ArrayList<Board>();
		List<String> paths = new ArrayList<String>();	
		String userBoardFilePath = getDIDEPath()+"djysrc\\bsp\\boarddrv\\user";
		String demoBoardFilePath = getDIDEPath()+"djysrc\\bsp\\boarddrv\\demo";
		paths.add(userBoardFilePath);
		paths.add(demoBoardFilePath);
		for(int i=0;i<paths.size();i++) {
			File boardFile = new File(paths.get(i));
			File[] files = boardFile.listFiles();
			for(int j=0;j<files.length;j++){
					File file = files[j];
					File[] mfiles = file.listFiles();
					for(int k=0;k<mfiles.length;k++) {
						if(mfiles[k].getName().endsWith(".xml")) {
							try {
								Board board = rbx.getBoard(mfiles[k]);
								boards.add(board);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;
						}
					}
			}
		}
		
		for(int i=0;i<boards.size();i++) {
			if(boards.get(i).getBoardName().equals(boardName)) {
				sBoard = boards.get(i);
				break;
			}
		}
		
		if(sBoard!=null) {
			List<OnBoardCpu> onBoardCpus = sBoard.getOnBoardCpus();
			for(int i=0;i<onBoardCpus.size();i++) {
				if(onBoardCpus.get(i).getCpuName().equals(cpuName)) {
					onBoardCpu = onBoardCpus.get(i);
					break;
				}
			}
		}
		
	}
	

}
