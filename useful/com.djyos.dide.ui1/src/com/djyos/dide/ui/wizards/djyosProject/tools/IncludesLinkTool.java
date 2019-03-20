package com.djyos.dide.ui.wizards.djyosProject.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.objects.Arch;
import com.djyos.dide.ui.objects.Board;
import com.djyos.dide.ui.objects.Core;
import com.djyos.dide.ui.objects.Cpu;

public class IncludesLinkTool {
	
	private static String didePath = PathTool.getDIDEPath();
	
	public static HashMap< String, List<String>> getCommonIncludeLinks(Board board, Cpu cpu, Core core, boolean isApp) {
		HashMap< String, List<String>> hMap = 
			      new HashMap< String, List<String>>();
		List<String> links = new ArrayList<String>();
		List<String> assemblyLinks = new ArrayList<String>();
		File boardFolder = new File(board.getBoardFolderPath());
		File cpuFolder = new File(cpu.getCpuFolderPath());
		File boardDemoFile = new File(PathTool.getDemoBoardFilePath());
		File archSourceFile = new File(PathTool.getDjyosSrcPath() + "/bsp/arch");
		boolean isDemoBoard = isDemoBoard(boardDemoFile, boardFolder);
		List<File> archXmlFiles = DideHelper.getArchXmlFiles(archSourceFile, new ArrayList<File>());
		// 根据架构类型选择链接
		linkArmNeeded(core, links, assemblyLinks);
		// 根据cpu链接
		linkCpu(cpuFolder, cpu.getCpuName(), links);
		// 根据板件名链接
		linkBoard(isDemoBoard, links, boardFolder);
		// 根据内核类型、架构、家族链接
		linkArch(core, links, archXmlFiles, assemblyLinks);
		// 添加project_config.h的链接
		links.add("${ProjDirPath}/src/"+(isApp?"app":"iboot")+"/OS_prjcfg");
		
		hMap.put("cIncludes", links);
		hMap.put("aIncludes", assemblyLinks);
		return hMap;
		
	}
	
	private static boolean isDemoBoard(File boardDemoFile, File boardFolder) {
		boolean isDemoBoard = false;
		if (boardDemoFile.exists()) {
			File[] boardDemoFiles = boardDemoFile.listFiles();
			for (int j = 0; j < boardDemoFiles.length; j++) {
				if (boardDemoFiles[j].getName().equals(boardFolder.getName())) {
					isDemoBoard = true;
					break;
				}
			}
		}
		return isDemoBoard;
	}
	
	private static void setArchLinks(Arch arch, List<String> archLinks, List<File> archXmlFiles) {
		// TODO Auto-generated method stub
		File archSrcFile = new File(PathTool.getDjyosSrcPath() + "/bsp/arch");
		if (archSrcFile.exists()) {
			for (File f : archXmlFiles) {
				if (arch.getMcpu() != null) {
					if (f.getParentFile().getName().equals(arch.getMcpu())) {
						File parentFile = f.getParentFile();
						getArchLinks(archLinks, parentFile);
						break;
					}
				}
			}
		}
	}

	private static void getArchLinks(List<String> archLinks, File archFolder) {
		// TODO Auto-generated method stub
		File includeFile = new File(archFolder.getPath().replace("\\", "/") + "/include");
		if (includeFile.exists()) {
			archLinks.add(includeFile.getPath().replace("\\", "/").replace(PathTool.getArchPath(),
					"${DJYOS_SRC_LOCATION}/bsp/arch"));
		}
		if (!archFolder.getParentFile().getName().equals("arch")) {
			getArchLinks(archLinks, archFolder.getParentFile());
		}
	}
	
	public static void linkArch(Core core, List<String> links, List<File> archXmlFiles, List<String> assemblyLinks) {
		// TODO Auto-generated method stub
		List<String> archLinks = new ArrayList<String>();
		setArchLinks(core.getArch(), archLinks, archXmlFiles);
		for (String link : archLinks) {
			links.add(link);
			assemblyLinks.add(link);
		}
	}

	public static void linkBoard(boolean isDemoBoard, List<String> links, File boardFolder) {
		// TODO Auto-generated method stub
		if (isDemoBoard) {
			links.add(PathTool.getRelativeDemoBoardFilePath() + boardFolder.getName());
			links.add(PathTool.getRelativeDemoBoardFilePath() + boardFolder.getName() + "/include");
			links.add(PathTool.getRelativeDemoBoardFilePath() + boardFolder.getName() + "/startup");
		} else {
			links.add(PathTool.getRelativeUserBoardFilePath() + boardFolder.getName());
			links.add(PathTool.getRelativeUserBoardFilePath() + boardFolder.getName() + "/include");
			links.add(PathTool.getRelativeUserBoardFilePath() + boardFolder.getName() + "/startup");
		}
	}

	public static void linkCpu(File cpuFolder, String _cpuName, List<String> links) {
		// TODO Auto-generated method stub
		List<String> cpuLinkStrings = new ArrayList<String>();
		getCpuIncludes(cpuFolder, cpuLinkStrings);
		String firmwarePath = didePath + "djysrc/third/firmware";
		File[] firmwareFiles = new File(firmwarePath).listFiles();
		for (File file : firmwareFiles) {
			if (_cpuName.contains(file.getName())) {
				cpuLinkStrings.add("${DJYOS_SRC_LOCATION}/third/firmware/" + file.getName());
				cpuLinkStrings.add("${DJYOS_SRC_LOCATION}/third/firmware/" + file.getName() + "/Inc");
				break;
			}
		}
		if (cpuLinkStrings != null) {
			for (int k = 0; k < cpuLinkStrings.size(); k++) {
				links.add(cpuLinkStrings.get(k).replace("\\", "/"));
			}
		}
	}

	public static void linkArmNeeded(Core core, List<String> links, List<String> assemblyLinks) {
		// TODO Auto-generated method stub
		if (core.getArch().getSerie().equals("arm")) {
			links.add("${DJYOS_SRC_LOCATION}/third/firmware/CMSIS/include");
			links.add("${DJYOS_SRC_LOCATION}/bsp/arch/arm");
			assemblyLinks.add("${DJYOS_SRC_LOCATION}/bsp/arch/arm");
		}
	}
	
	private static void getCpuIncludes(File cpuFile, List<String> cpuLinkStrings) {
		// TODO Auto-generated method stub
		String DJYOS_SRC_LOCARION = didePath + "djysrc";
		File[] files = cpuFile.listFiles();
		for (File file : files) {
			String path = file.getPath();
			String relativePath = path.substring(DJYOS_SRC_LOCARION.length(), path.length());
			if (file.getName().equals("include")) {
				cpuLinkStrings.add("${DJYOS_SRC_LOCATION}" + relativePath);
				break;
			}
		}
		if (!cpuFile.getParentFile().getName().equals("cpudrv")) {
			getCpuIncludes(cpuFile.getParentFile(), cpuLinkStrings);
		}
	}
}
