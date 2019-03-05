package com.djyos.dide.ui.wizards.component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.objects.Board;
import com.djyos.dide.ui.objects.Chip;
import com.djyos.dide.ui.objects.OnBoardCpu;

public class GetNonCompFiles {
	private static String didePath = DideHelper.getDIDEPath();
	private static List<File> excludeCompFiles;
	static ComponentRefer cRefer = new ComponentRefer();

	// 遍历组件及其子组件
	private static void traverFiles(File file) { // lds
		if (!file.getName().equals("include") && !file.getName().equals("startup") && !file.getName().equals("lds")) {

			List<File> allFiles = DideHelper.sortFileAndFolder(file);
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
					if (!file.getName().equals("firmware") && !file.getName().equals("fs")) {
						excludeCompFiles.add(file);
					}
					if (file.getParentFile().getName().equals("firmware")) {
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

	private static boolean haveChildComp(File file) {
		// TODO Auto-generated method stub
		boolean isComp = false;
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

	public static List<File> getNonCompFiles(OnBoardCpu onBoardCpu, Board board) {
		ComponentRefer cRefer = new ComponentRefer();
		excludeCompFiles = new ArrayList<File>();
		List<String> componentPaths = cRefer.get_notcore_paths(board.getBoardName());
		String chipPath = didePath + "djysrc/bsp/chipdrv";
		for (int i = 0; i < componentPaths.size(); i++) {
			File sourceFile = new File(componentPaths.get(i));// third Explorer
			if (sourceFile.exists()) {
				File[] files = sourceFile.listFiles();// firmfare drv lds
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

}
