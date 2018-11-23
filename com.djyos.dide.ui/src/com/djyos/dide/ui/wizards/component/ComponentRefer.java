package com.djyos.dide.ui.wizards.component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.djyos.dide.ui.helper.DideHelper;

public class ComponentRefer {
	private String didePath = DideHelper.getDIDEPath();

	public List<String> getClearCompPaths(String boardName) {
		String componentPath = didePath + "djysrc/component";
		String djyosPath = didePath + "djysrc/djyos";
		String thirdPath = didePath + "djysrc/third";
		String loaderPath = didePath + "djysrc/loader";
		String demoPath = DideHelper.getDemoBoardFilePath() + "/" + boardName;
		String userPath = DideHelper.getUserBoardFilePath() + "/" + boardName;
		List<String> componentPaths = new ArrayList<String>();
		componentPaths.add(componentPath);
		componentPaths.add(djyosPath);
		componentPaths.add(demoPath);
		componentPaths.add(userPath);
		componentPaths.add(loaderPath);
		componentPaths.add(thirdPath);
		return componentPaths;
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

		if (allStrings.size() > 0) {
			return true;
		}

		return false;
	}

	// public boolean idIncluded() {
	//
	// }

}
