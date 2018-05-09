package org.eclipse.cdt.ui.wizards.board;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.ui.wizards.board.onboardcpu.OnBoardCpu;

public class Board {
	String boardName;
	private List<OnBoardCpu> onBoardCpus = new ArrayList<OnBoardCpu>();

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



	public Board() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
