package com.djyos.dide.ui.wizards.cpu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.djyos.dide.ui.helper.DideHelper;

public class GetNonCpuFiles {
	private String didePath = DideHelper.getDIDEPath();
	private List<File> excludeCpuFiles = new ArrayList<File>();

	public List<File> getNonCpus() {
		String sourcePath = didePath + "djysrc/bsp/cpudrv";
		File sourceFile = new File(sourcePath);
		travelFiles(sourceFile);
		return excludeCpuFiles;
	}

	private void travelFiles(File file) {
		// TODO Auto-generated method stub
		File[] files = file.listFiles();
		for (File f : files) {
			if (f.isDirectory()) {
				if (!f.getName().equals("include") && !f.getName().equals("src")) {
					if (!isForCpu(f)) {
						excludeCpuFiles.add(f);
					} else {
						travelFiles(f);
					}
				}
			}
		}
	}

	private boolean isForCpu(File file) {
		// TODO Auto-generated method stub
		boolean isOk = false;
		File[] files = file.listFiles();
		for (File f : files) {
			if (f.isDirectory()) {
				if (!f.getName().equals("include") && !f.getName().equals("src")) {
					isOk = isForCpu(f);
				}
			} else {
				if (f.getName().startsWith("cpu") && f.getName().endsWith(".xml")) {
					isOk = true;
				}
			}
			if (isOk) {
				break;
			}
		}
		return isOk;
	}

}
