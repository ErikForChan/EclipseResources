package com.djyos.dide.ui.wizards.board;

import java.io.File;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.resources.IProject;

import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.wizards.djyosProject.tools.PathTool;

public class BoardHelper {

	public static boolean isDemoBoard(File boardDemoFile, File boardFolder) {
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
	
	public static void setBoardExclude(boolean isDemoBoard, String boardName, IProject project,
			ICConfigurationDescription[] conds) {
		// TODO Auto-generated method stub
		File boardDrvFile = new File(PathTool.getDjyosSrcPath() + "/bsp/boarddrv");
		File[] files = boardDrvFile.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				File[] myFiles = file.listFiles();
				String fileTag = isDemoBoard?"demo":"user";
				if (file.getName().equals(fileTag)) {
					for (File f : myFiles) {
						boolean exclude = f.getName().equals(boardName);
						DideHelper.setFolderExclude(f, project, conds, !exclude);
					}
				} else {
					for (File f : myFiles) {
						DideHelper.setFolderExclude(f, project, conds, true);
					}
				}
			}
		}
	}
}
