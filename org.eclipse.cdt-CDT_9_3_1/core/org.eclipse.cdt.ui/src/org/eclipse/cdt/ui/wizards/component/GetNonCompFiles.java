package org.eclipse.cdt.ui.wizards.component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.ui.wizards.board.Board;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.Chip;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.OnBoardCpu;
import org.eclipse.cdt.ui.wizards.djyosProject.tools.DideHelper;
import org.eclipse.cdt.ui.wizards.djyosProject.tools.LinkHelper;

public class GetNonCompFiles {
	private DideHelper dideHelper = new DideHelper();
	private LinkHelper linkHelper = new LinkHelper();
	private String didePath = dideHelper.getDIDEPath();
	private String deapPath = null;
	private List<File> excludeCompFiles = new ArrayList<File>();
	private List<String> componentPaths = new ArrayList<String>();

	// 遍历组件及其子组件
	private void traverFiles(File file) {
		if (!file.getName().equals("include")  && !file.getName().equals("startup")) {
			File[] files = file.listFiles();
			List<File> allFiles = new ArrayList<File>();
			List<File> pureFiles = new ArrayList<File>();
			List<File> folderFiles = new ArrayList<File>();

			for (File f : files) {
				if (f.isDirectory()) {
					folderFiles.add(f);
				} else if (f.isFile() && (f.getName().endsWith(".c") || f.getName().endsWith(".h"))) {
					pureFiles.add(f);
				}
			}
			allFiles.addAll(folderFiles);
			allFiles.addAll(pureFiles);

			boolean hExist = false;
			for (File f : allFiles) {
				if (f.getName().endsWith(".h") && f.getName().contains("component_config")) {
					hExist = true;
					if (!isComponent(f)) {
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
							if (isComponent(f)) {
								haveComp = true;
							}
						}
					}
					if (!haveComp) {
						if (!haveChildComp(file)) {
							excludeCompFiles.add(file);
						}
					}
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
					if (isComponent(f)) {
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

	public List<File> getExcludeCompFiles(OnBoardCpu onBoardCpu, Board board) {
		String componentPath = didePath + "djysrc/component";
		String djyosPath = didePath + "djysrc/djyos";
		String thirdPath = didePath + "djysrc/third";
		String demoPath = didePath + "djysrc/bsp/boarddrv/demo/" + board.getBoardName();
		String userPath = didePath + "djysrc/bsp/boarddrv/user/" + board.getBoardName();
		String chipPath = didePath + "djysrc/bsp/chipdrv";
		String cpuPath = didePath + "djysrc/bsp/cpudrv";
		componentPaths.add(componentPath);
		componentPaths.add(djyosPath);
		componentPaths.add(demoPath);
		componentPaths.add(userPath);
		componentPaths.add(thirdPath);
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
									if (!isComponent(file)) {
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

		if (chips.size() > 0) {
			File chipFile = new File(chipPath);
			if (chipFile.exists()) {
				File[] chipfiles = chipFile.listFiles();
				for (File file : chipfiles) {
					if (file.isDirectory()) {
						traverFiles(file);
					}
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
				} else if (!file.getName().equals("include") && !file.getName().equals("src")  && !file.getName().equals("startup")) {
					getDeapPath(file, cpuName);// 如果为目录，则继续扫描该目录
				}
			}
		}
		return deapPath;
	}

	public boolean isComponent(File file) {
		FileReader reader = null;
		List<String> allStrings = new ArrayList<String>();
		try {
			reader = new FileReader(file.getPath());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader br = new BufferedReader(reader);
		String str = null;
		boolean start = false, stop = false;
		try {
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (allStrings.size() != 0) {
			return true;
		}

		return false;
	}

}
