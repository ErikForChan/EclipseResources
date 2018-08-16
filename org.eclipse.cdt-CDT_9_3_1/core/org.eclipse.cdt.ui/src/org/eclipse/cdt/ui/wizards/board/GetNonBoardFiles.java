package org.eclipse.cdt.ui.wizards.board;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.ui.wizards.board.onboardcpu.Chip;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.OnBoardCpu;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.OnBoardMemory;
import org.eclipse.cdt.ui.wizards.component.Component;
import org.eclipse.cdt.ui.wizards.djyosProject.tools.DideHelper;
import org.eclipse.cdt.ui.wizards.djyosProject.tools.LinkHelper;

public class GetNonBoardFiles {
	private DideHelper dideHelper = new DideHelper();
	private LinkHelper linkHelper = new LinkHelper();
	private String didePath = dideHelper.getDIDEPath();
	private List<File> excludeBoardFiles = new ArrayList<File>();
	
	public List<File> getNonBoards() {
		List<Board> boards = new ArrayList<Board>();
		List<String> paths = new ArrayList<String>();
		String userBoardFilePath = didePath + "djysrc\\bsp\\boarddrv\\user";
		String demoBoardFilePath = didePath + "djysrc\\bsp\\boarddrv\\demo";
		paths.add(userBoardFilePath);
		paths.add(demoBoardFilePath);
		for (int i = 0; i < paths.size(); i++) {
			File boardFile = new File(paths.get(i));//boarddrv\\user
			File[] files = boardFile.listFiles();
			for (int j = 0; j < files.length; j++) {
				File file = files[j];//board
				if(file.isDirectory()) {
					if(!isBoard(file)) {
						excludeBoardFiles.add(file);
					}
				}
			}
		}
		return excludeBoardFiles;
	}

	public boolean isBoard(File file) {
		File[] files = file.listFiles();
		for(File f:files) {
			if(f.getName().endsWith(".xml") && f.getName().startsWith("Board")) {
				return true;
			}
		}
		return false;
	}
	
}
