package org.eclipse.cdt.ui.wizards.component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.Platform;

import org.eclipse.cdt.ui.wizards.board.Board;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.Chip;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.OnBoardCpu;

public class ReadComponent {
	String fullPath = Platform.getInstallLocation().getURL().toString();
	String eclipsePath = fullPath.substring(6,
			(fullPath.substring(0, fullPath.length() - 1)).lastIndexOf("/") + 1);
	List<Component> components = new ArrayList<Component>();
	List<String> componentPaths = new ArrayList<String>();

	private String getCpuSrcPath(String cpuName) {
		String sourcePath = eclipsePath + "djysrc/bsp/cpudrv";
		File sourceFile = new File(sourcePath);
		File[] files = sourceFile.listFiles();
		String path = null;
		for (File file : files) {// cpudrv下的所有文件 Atmel stm32
			if (file.isDirectory()) {
				getDeapPath(file, cpuName);
				if (deapPath != null) {
					path = deapPath + "/../src";
					break;
				}
			}

		}
		return path;
	}

	private String deapPath = null;

	public String getDeapPath(File sourceFile, String cpuName) {
		File[] files = sourceFile.listFiles();
		String path = null;
		for (File file : files) {
			if (file.isDirectory()) {
				if (file.getName().equals(cpuName)) {
					deapPath = file.getPath();
					break;
				} else if (!file.getName().equals("include") && !file.getName().equals("src")) {
					getDeapPath(file, cpuName);// 如果为目录，则继续扫描该目录
				}
			}
		}
		return deapPath;
	}

	// 遍历组件及其子组件
	private void traverFiles(File file) {
		File[] allFiles = file.listFiles();
		boolean hExist = false;;
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
		
		for (File f : allFiles) {
			if (f.getName().endsWith(".h") && f.getName().contains("component_config")) {
				hExist = true;
				break;
			}
		}
		
		for (File f : allFiles) {
			if (f.isFile()) {
				if (hExist) {
					try {
						getComponent(f);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					if(f.getName().endsWith(".c")) {
						try {
							getComponent(f);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

			} else if (f.isDirectory()) {
				traverFiles(f);
			}
		}
	}

	public List<Component> getComponents(OnBoardCpu onBoardCpu,Board board) {
		String componentPath = eclipsePath+"djysrc/component";
		String djyosPath = eclipsePath+"djysrc/djyos";
		String demoPath = eclipsePath+"djysrc/bsp/boarddrv/demo/"+board.getBoardName()+"/drv";
		String testPath = eclipsePath+"djysrc/bsp/cpudrv/Atmel/SamV7/src";
//		componentPaths.add(componentPath);
//		componentPaths.add(djyosPath);
//		componentPaths.add(demoPath);
		componentPaths.add(testPath);
		for(int i=0;i<componentPaths.size();i++) {
			File sourceFile = new File(componentPaths.get(i));
			File[] files = sourceFile.listFiles();
			System.out.println("fileName:   "+sourceFile.getName());
			for(File file:files) {	
				
				if(file.isDirectory()) {
					traverFiles(file);
				}
			}
		}
		
//		// 板载cpu的芯片、cpu的src目录
//		List<Chip> chips = onBoardCpu.getChips();
//		List<String> chipNames = new ArrayList<String>();
//		String cpuSrcPath = getCpuSrcPath(onBoardCpu.getCpuName());
//		for(int i=0;i<chips.size();i++) {
//			chipNames.add(chips.get(i).getChipName());
//		}
//		String chipPath = eclipsePath + "djysrc/bsp/chipdrv";
//		String cpuPath = eclipsePath + "djysrc/bsp/cpudrv";
//		if(chips.size()>0) {
//			File chipFile = new File(chipPath);
//			File[] chipfiles = chipFile.listFiles();
//			for (File file : chipfiles) {
//				if (file.isDirectory() && chipNames.contains(file.getName())) {
//					traverFiles(file);
//				}
//			}
//		}
//		if(cpuSrcPath!= null) {
//			File cpuFile = new File(cpuSrcPath);
//			File[] cpufiles = cpuFile.listFiles();
//			for (File file : cpufiles) {
//				if (file.isDirectory()) {
//					traverFiles(file);
//				}
//			}
//		}
	
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
		}
		newComponent.setFileName(file.getParentFile().getPath());
		
		FileReader reader = null;
		List<String> allStrings = new ArrayList<String>();
		List<String> initcodeStrings = new ArrayList<String>();
		List<String> describeStrings = new ArrayList<String>();
		List<String> configueStrings = new ArrayList<String>();
		List<String> excludeStrings = new ArrayList<String>();
		int iStart = 0,iStop = 0,dStart = 0,dStop = 0,cStart = 0,cStop = 0,eStart = 0,eStop = 0;
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
			}
		}
		
		System.out.println("infos:  " + iStart+"  "+iStop+"  "+dStart+"  "+dStop+"  "+cStart+"  "+cStop+"  "+eStart+"  "+eStop);
		
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
			excludeStrings.add(allStrings.get(i).replaceAll("//../", ""));
		}
		
		newComponent.setCode(listToString(initcodeStrings));
		newComponent.setConfigure(listToString(configueStrings));
		 
		for (String describe : describeStrings) {
			String[] infos = describe.split("//");
			if(infos.length>1 && infos[1].trim().contains(":")) {
				String menbers[] = infos[1].trim().split(":");
				if (menbers[0].trim().equals("component name")) {
					newComponent.setName(menbers[1].trim().replace("\"", ""));
				}else if (menbers[0].trim().equals("parent")) {
					newComponent.setParent(menbers[1].trim().replace("\"", ""));
				}else if (menbers[0].trim().equals("annotation")) {
					newComponent.setAnnotation(menbers[1].trim());
				}else if (menbers[0].trim().equals("attribute")) {
					newComponent.setAttribute(menbers[1].trim());
				}else if (menbers[0].trim().equals("select")) {
					newComponent.setSelectable(menbers[1].trim());
				}else if (menbers[0].trim().equals("grade")) {
					newComponent.setGrade(menbers[1].trim());
				}else if (menbers[0].trim().equals("dependence")) {
					newComponent.getDependents().add(menbers[1].trim().replace("\"", ""));
				}else if (menbers[0].trim().equals("weakdependence")) {
					newComponent.getWeakDependents().add(menbers[1].trim().replace("\"", ""));
				}else if (menbers[0].trim().equals("mutex")) {
					newComponent.getMutexs().add(menbers[1].trim().replace("\"", ""));
				}
			}			
		}
		
		for (String exclude : excludeStrings) {
			System.out.println("exclude:  "+exclude);
			newComponent.getExcludes().add(exclude);
		}
		
		if(allStrings.size() != 0) {
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
