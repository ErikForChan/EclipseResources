package com.djyos.dide.ui.wizards.component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.djyos.dide.ui.wizards.board.Board;
import com.djyos.dide.ui.wizards.board.onboardcpu.Chip;
import com.djyos.dide.ui.wizards.board.onboardcpu.OnBoardCpu;
import com.djyos.dide.ui.wizards.djyosProject.tools.DideHelper;
import com.djyos.dide.ui.wizards.djyosProject.tools.LinkHelper;

public class GetNonCompFiles {
	private DideHelper dideHelper = new DideHelper();
	private LinkHelper linkHelper = new LinkHelper();
	private String didePath = dideHelper.getDIDEPath();
	private String deapPath = null;
	private List<File> excludeCompFiles = new ArrayList<File>();
	private List<String> componentPaths = new ArrayList<String>();
	ComponentRefer cRefer = new ComponentRefer();

	// 遍历组件及其子组件
	private void traverFiles(File file) {
		if (!file.getName().equals("include") && !file.getName().equals("startup")) {

			List<File> allFiles = dideHelper.sortFileAndFolder(file);
			boolean hExist = false;
			for (File f : allFiles) {
				if (f.getName().endsWith(".h") && f.getName().contains("component_config")) {
					hExist = true;
					if (!cRefer.isComponent(f)) {
						excludeCompFiles.add(file);
					}
					break;
				}
			}
			if (!hExist) {
				if (!file.getPath().contains("third")) {
					boolean haveComp = false;
					List<File> excludeFiles = new ArrayList<File>();
					for (File f : allFiles) {
						if (f.getName().endsWith(".c")) {
							if (cRefer.isComponent(f)) {
								haveComp = true;
							}
						}
					}
					if (!haveComp) {
						if (!haveChildComp(file)) {
							excludeCompFiles.add(file);
						}
					}
				} else {
					if (file.getParentFile().getName().equals("firmware")) {
						// System.out.print("file: "+file.getName());
						excludeCompFiles.add(file);
					}
				}
			}

			if (file.getName().equals("firmware")) {
				for (File f : allFiles) {
					traverFiles(f);
				}
			}

		}
	}

	private boolean haveChildComp(File file) {
		// TODO Auto-generated method stub
		boolean isComp = false;
		boolean haveComp = false;
		File[] files = file.listFiles();
		for (File f : files) {
			if (!f.getName().equals("include") && !f.getName().equals("startup")) {
				if (f.isDirectory()) {
					isComp = haveChildComp(f);
				} else {
					if (cRefer.isComponent(f)) {
						isComp = true;
					}
				}
			}
			if (isComp) {
				break;
			}
		}
		return isComp;
	}

	public List<File> getNonCompFiles(OnBoardCpu onBoardCpu, Board board) {
		ComponentRefer cRefer = new ComponentRefer();
		List<String> componentPaths = cRefer.getClearCompPaths(board.getBoardName());
		String chipPath = didePath + "djysrc/bsp/chipdrv";
		for (int i = 0; i < componentPaths.size(); i++) {
			File sourceFile = new File(componentPaths.get(i));// third
			if (sourceFile.exists()) {
				File[] files = sourceFile.listFiles();// firmfare
				for (File file : files) {
					if (file.isDirectory()) {
						traverFiles(file);
					} else {
						if (!file.getPath().contains("third")) {
							if (file.getName().endsWith(".c")) {
								try {
									if (!cRefer.isComponent(file)) {
										excludeCompFiles.add(file);
									}
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

		// 板载cpu的芯片、cpu的src目录
		List<Chip> chips = onBoardCpu.getChips();
		List<String> chipNames = new ArrayList<String>();

		for (int i = 0; i < chips.size(); i++) {
			chipNames.add(chips.get(i).getChipName());
		}

		File chipFile = new File(chipPath);
		if (chipFile.exists()) {
			File[] chipfiles = chipFile.listFiles();
			for (File file : chipfiles) {
				if (file.isDirectory()) {
					traverFiles(file);
				}
			}
		}

		return excludeCompFiles;
	}

	private String getCpuSrcPath(String cpuName) {
		String sourcePath = didePath + "djysrc/bsp/cpudrv";
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

	public String getDeapPath(File sourceFile, String cpuName) {
		File[] files = sourceFile.listFiles();
		String path = null;
		for (File file : files) {
			if (file.isDirectory()) {
				if (file.getName().equals(cpuName)) {
					deapPath = file.getPath();
					break;
				} else if (!file.getName().equals("include") && !file.getName().equals("src")
						&& !file.getName().equals("startup")) {
					getDeapPath(file, cpuName);// 如果为目录，则继续扫描该目录
				}
			}
		}
		return deapPath;
	}

}
