package org.eclipse.cdt.ui.wizards.board.onboardcpu;

import java.util.List;

public class TBoard {
	String boardName;
	private List<OnBoardCpu> onBoardCpus;

	public TBoard(String boardName, List<OnBoardCpu> onBoardCpus) {
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



	public TBoard() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
