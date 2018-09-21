package com.djyos.dide.ui.wizards.component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TreeItem;

import com.djyos.dide.ui.wizards.board.onboardcpu.Chip;
import com.djyos.dide.ui.wizards.board.onboardcpu.OnBoardCpu;

import com.djyos.dide.ui.wizards.board.Board;
import com.djyos.dide.ui.wizards.component.Component;
import com.djyos.dide.ui.wizards.cpu.Cpu;
import com.djyos.dide.ui.wizards.cpu.ReadCpuXml;
import com.djyos.dide.ui.wizards.djyosProject.tools.DPluginImages;
import com.djyos.dide.ui.wizards.djyosProject.tools.DideHelper;

public class ReadComponent {
	private List<Component> components = new ArrayList<Component>();
	private DideHelper dideHelper = new DideHelper();
	private String didePath = dideHelper.getDIDEPath();

	private void getCpuSrcPath(File cpuFolder, List<String> cpuSrcPaths) {
		File[] files = cpuFolder.listFiles();
		for(File f:files) {
			if(f.getName().equals("src")) {
				cpuSrcPaths.add(f.getPath());
				break;
			}
		}
		if(!cpuFolder.getParentFile().getName().equals("cpudrv")) {
			getCpuSrcPath(cpuFolder.getParentFile(),cpuSrcPaths);
		}
	}

	// 遍历组件及其子组件
	private void traverFiles(File file) {
		if(!file.getName().equals("include")) {
			
			List<File> allFiles = dideHelper.sortFileAndFolder(file);
			boolean hExist = false;
			File hFile = null;
			for (File f : allFiles) {
				if (f.getName().endsWith(".h") && f.getName().contains("component_config")) {
					hExist = true;
					hFile = f;
					break;
				}
			}

			if (!hExist) {
				if (!file.getPath().contains("third")) {
					for (File f : allFiles) {
						if (f.isDirectory()) {
							traverFiles(f);
						} else if (f.getName().endsWith(".c")) {
							try {
								getComponent(f);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				} else {
					for (File f : allFiles) {
						if (f.isDirectory()) {
							traverFiles(f);
						}
					}
				}
			}else {
				if (!file.getPath().contains("third")) {
					for (File f : allFiles) {
						if (f.isDirectory()) {
							traverFiles(f);
						}
					}
				}
				try {
					getComponent(hFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
		}	
	}
	
	private Cpu getCpuByBoard(OnBoardCpu onBoardCpu) {
		ReadCpuXml rcx = new ReadCpuXml();
		List<Cpu> cpusList =  rcx.getAllCpus();
		for(int i=0;i<cpusList.size();i++) {
			if(cpusList.get(i).getCpuName().equals(onBoardCpu.getCpuName())) {
				return cpusList.get(i);
			}
		}
		return null;
	}

	public List<Component> getComponents(OnBoardCpu onBoardCpu,Board board) {
		ComponentRefer cRefer = new ComponentRefer();
		List<String> componentPaths = cRefer.getClearCompPaths(board.getBoardName());
		String libcPath = didePath+"djysrc/libc";
		String chipPath = didePath + "djysrc/bsp/chipdrv";
		
		for (int i = 0; i < componentPaths.size(); i++) {
			File sourceFile = new File(componentPaths.get(i));
			if (sourceFile.exists()) {
				File[] files = sourceFile.listFiles();
				for (File file : files) {
					if (file.isDirectory()) {
						traverFiles(file);
					} else {
						if (!file.getPath().contains("third")) {
							if (file.getName().endsWith(".c")) {
								try {
									getComponent(file);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}
				}
			}
		}
		
		traverFiles(new File(libcPath));
		// 板载cpu的芯片、cpu的src目录
		List<Chip> chips = onBoardCpu.getChips();
		List<String> chipNames = new ArrayList<String>();
		
		Cpu cpu = getCpuByBoard(onBoardCpu);
		List<String> cpuSrcPaths = new ArrayList<String>();
		File cpuFolder = new File(cpu.getParentPath());
		getCpuSrcPath(cpuFolder,cpuSrcPaths);
		
		for(int i=0;i<chips.size();i++) {
			chipNames.add(chips.get(i).getChipName());
		}
		if(chips.size()>0) {
			File chipFile = new File(chipPath);
			if(chipFile.exists()) {
				File[] chipfiles = chipFile.listFiles();
				for (File file : chipfiles) {
					if (file.isDirectory() && chipNames.contains(file.getName())) {
						traverFiles(file);
					}
				}
			}
			
		}
		
		for(String path:cpuSrcPaths) {
			traverParentSrc(path);
		}
		
		return components;
	}
	
	public List<Component> getAllComponents(OnBoardCpu onBoardCpu,Board board) {
		ComponentRefer cRefer = new ComponentRefer();
		List<String> componentPaths = cRefer.getClearCompPaths(board.getBoardName());
		String libcPath = didePath+"djysrc/libc";
		String chipPath = didePath + "djysrc/bsp/chipdrv";
		componentPaths.add(chipPath);
		for (int i = 0; i < componentPaths.size(); i++) {
			File sourceFile = new File(componentPaths.get(i));
			if (sourceFile.exists()) {
				List<File> allFiles = dideHelper.sortFileAndFolder(sourceFile);
				for (File file : allFiles) {
					if (file.isDirectory()) {
						traverFiles(file);
					} else {
						if (!file.getPath().contains("third")) {
							if (file.getName().endsWith(".c")) {
								try {
									getComponent(file);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}
				}
			}
		}
		
		traverFiles(new File(libcPath));
		// cpu的src目录
		Cpu cpu = getCpuByBoard(onBoardCpu);
		List<String> cpuSrcPaths = new ArrayList<String>();
		File cpuFolder = new File(cpu.getParentPath());
		getCpuSrcPath(cpuFolder,cpuSrcPaths);
		for(String path:cpuSrcPaths) {
			traverParentSrc(path);
		}
		return components;
	}
	
	private void traverParentSrc(String cpuSrcPath) {
		// TODO Auto-generated method stub
		if(cpuSrcPath!= null) {
			File cpuFile = new File(cpuSrcPath);
			if(cpuFile.exists()) {
				File[] cpufiles = cpuFile.listFiles();
				for (File file : cpufiles) {
					if (file.isDirectory()) {
						traverFiles(file);
					}else {
						if (file.getName().endsWith(".c")) {
							try {
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
			
		}
	}

	public List<Component> getSrcPeripherals(File srcFile){
		File[] srcfiles = srcFile.listFiles();
		for (File file : srcfiles) {
			if (file.isDirectory()) {
				traverFiles(file);
			} else {
				if (!file.getPath().contains("third")) {
					if (file.getName().endsWith(".c")) {
						try {
							getComponent(file);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		return components;
	}
	
	public void getComponent(File file) throws IOException {
		Component newComponent = new Component();
		
		//给新组建的LinkFolder和FileName赋值
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
		}else if(file.getPath().contains("libc")){
			newComponent.setLinkFolder("libc");
		}else if(file.getPath().contains("loader")){
			newComponent.setLinkFolder("loader");
		}
		newComponent.setFileName(file.getName());
		newComponent.setParentPath(file.getParentFile().getPath());
		FileReader reader = null;
		List<String> allStrings = new ArrayList<String>();
		List<String> initcodeStrings = new ArrayList<String>();
		List<String> describeStrings = new ArrayList<String>();
		List<String> configueStrings = new ArrayList<String>();
		List<String> excludeStrings = new ArrayList<String>();
		List<String> includeStrings = new ArrayList<String>();
		int iStart = 0,iStop = 0,dStart = 0,dStop = 0,
				cStart = 0,cStop = 0,eStart = 0,eStop = 0,
				nStart = 0,nStop = 0;
		reader = new FileReader(file.getPath());
		BufferedReader br = new BufferedReader(reader);
		String str = null;
		boolean start = false,stop=false;
		while ((str = br.readLine()) != null) {
			if (str.contains("@#$%component configure")) {
				start = true;
			}
			if (start && !stop) {
				allStrings.add(str);
			}
			if (str.contains("@#$%component end configure")) {
				stop = true;
				break;
			}
		}

		String targetLine = null;
		if(allStrings.size() != 0) {
			for(int i=0;i<allStrings.size();i++) {
				if(allStrings.get(i).contains("%$#@initcode")) {
					iStart = i;
				}else if(allStrings.get(i).contains("%$#@end initcode")){
					iStop = i;
				}else if(allStrings.get(i).contains("%$#@describe")){
					dStart = i;
				}else if(allStrings.get(i).contains("%$#@end describe")){
					dStop = i;
				}else if(allStrings.get(i).contains("%$#@configue")){
					cStart = i;
				}else if(allStrings.get(i).contains("%$#@end configue")){
					cStop = i;
				}else if(allStrings.get(i).contains("%$#@exclude")){
					eStart = i;
				}else if(allStrings.get(i).contains("%$#@end exclude")){
					eStop = i;
				}else if(allStrings.get(i).contains("%$#@include path")){
					nStart = i;
				}else if(allStrings.get(i).contains("%$#@end include path")){
					nStop = i;
				}else if(allStrings.get(i).contains("%$#@target")){
					targetLine = allStrings.get(i);
				}
			}
			String[] tInfos = targetLine.split("=");
			String[] rights = tInfos[1].split("//");
			String targetString = rights[0].trim();
			newComponent.setTarget(targetString);			
			for(int i=iStart+1;i<iStop;i++) {
				String initCode = allStrings.get(i).trim().replaceFirst("//", "");
				if(!initCode.equals("")) {
					initcodeStrings.add(initCode);
				}
			}
			for(int i=dStart+1;i<dStop;i++) {
				describeStrings.add(allStrings.get(i));
			}
			for(int i=cStart+1;i<cStop;i++) {
				String configue = allStrings.get(i).trim();
				configueStrings.add(allStrings.get(i));	
			}
			for(int i=eStart+1;i<eStop;i++) {
				String pureString = allStrings.get(i).replaceFirst("//", "");
				if(!pureString.trim().equals("")) {
					String[] curExcludes = pureString.split(";");
					for(String exclude:curExcludes) {
						excludeStrings.add(exclude.replace("..", "").replace("\\", "/"));
					}
				}
			}
			for(int i=nStart+1;i<nStop;i++) {
				String[] curIncludes = allStrings.get(i).replaceFirst("//", "").split(";");
				for(String include:curIncludes) {
					includeStrings.add(include.replace("..", "").replace("\\", "/"));
				}
			}
			newComponent.setCode(listToString(initcodeStrings));
			newComponent.setConfigure(listToString(configueStrings));
			boolean reach = false;
			for (String describe : describeStrings) {
//				if(describe.contains("mutex")) {
//					describe.replaceAll("依赖", "互斥");
//					reach = true;
//				}
//				if(reach) {
//					if(describe.contains("如果依赖多个组件")) {
//						describe.replaceAll("如果依赖多个组件", "如果与多个组件互斥");
//					}
//				}
				
				String[] infos = describe.split("//");
				if(infos.length>1 && infos[1].trim().contains(":")) {
					String members[] = infos[1].trim().split(":");
					if(!members[1].contains("/") || members[1].split("//").length>1) {
						if (members[0].trim().equals("component name")) {
							newComponent.setName(members[1].trim().replace("\"", ""));
							if (infos.length > 2) {
								newComponent.setAnnotation(infos[2].trim());
							}
						}else if (members[0].trim().equals("parent")) {
							newComponent.setParent(members[1].trim().replace("\"", ""));
						}else if (members[0].trim().equals("attribute")) {
							newComponent.setAttribute(members[1].trim());
						}else if (members[0].trim().equals("select")) {
							newComponent.setSelectable(members[1].trim());
						}else if (members[0].trim().equals("init time")) { //init time
							newComponent.setGrade(members[1].trim());
						}else if (members[0].trim().equals("dependence")) {
							String[] deps = members[1].split(",");
							for(int i=0;i<deps.length;i++) {
								String name = deps[i].trim().replace("\"", "");
								if (!name.equals("none")) {
									newComponent.getDependents().add(name);
								}
							}					
						}else if (members[0].trim().equals("weakdependence")) {		
							String[] deps = members[1].split(",");
							for(int i=0;i<deps.length;i++) {
								String name = deps[i].trim().replace("\"", "");
								if (!name.equals("none")) {
									newComponent.getWeakDependents().add(name);
								}
							}		
						}else if (members[0].trim().equals("mutex")) {
							String[] deps = members[1].split(",");
							for(int i=0;i<deps.length;i++) {
								String name = deps[i].trim().replace("\"", "");
								if (!name.equals("none")) {
									newComponent.getMutexs().add(name);
								}
							}	
						}
					}else {
						dideHelper.showErrorMessage("组件描述文件 "+file.getName()+" 的%$#@describe格式描述有误：/");
					}
				}			
			}
			
			for (String exclude : excludeStrings) {
				newComponent.getExcludes().add(exclude);
			}
			for (String include : includeStrings) {
				newComponent.getIncludes().add(include);
			}

			components.add(newComponent);
		}		
		
	}
	
	//将List<String>转换成String
	public static String listToString(List<String> list) {
		StringBuilder result = new StringBuilder();
		for (String string : list) {
			result.append(string + "\n");
		}
		return result.toString();
	}
	
}
