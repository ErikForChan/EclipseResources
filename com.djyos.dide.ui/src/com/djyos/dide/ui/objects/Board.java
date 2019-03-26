package com.djyos.dide.ui.objects;

import java.util.ArrayList;
import java.util.List;

public class Board {
	String boardName;
	String boardPath;
	private List<OnBoardCpu> onBoardCpus = new ArrayList<OnBoardCpu>();
	private List<OnBoardMemory> share_memorys = new ArrayList<OnBoardMemory>();

	
	public List<OnBoardMemory> getShare_memorys() {
		return share_memorys;
	}

	public void setShare_memorys(List<OnBoardMemory> share_memorys) {
		this.share_memorys = share_memorys;
	}

	public Board(String boardName, List<OnBoardCpu> onBoardCpus) {
		super();
		this.boardName = boardName;
		this.onBoardCpus = onBoardCpus;
	}

	public String getBoardName() {
		return boardName;
	}

	public void setBoardName(String boardName) {
		this.boardName = boardName;
	}

	public List<OnBoardCpu> getOnBoardCpus() {
		return onBoardCpus;
	}

	public void setOnBoardCpus(List<OnBoardCpu> onBoardCpus) {
		this.onBoardCpus = onBoardCpus;
	}

	public String getBoardFolderPath() {
		return boardPath;
	}

	public void setBoardFolderPath(String boardPath) {
		this.boardPath = boardPath;
	}

	public Board() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
