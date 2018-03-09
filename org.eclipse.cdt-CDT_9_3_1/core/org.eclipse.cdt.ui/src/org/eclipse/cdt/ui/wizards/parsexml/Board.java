package org.eclipse.cdt.ui.wizards.parsexml;

public class Board {

	public String boardName;
	public String exClk;
	public Cpu cpu;
	public String getExClk() {
		return exClk;
	}
	public void setExClk(String exClk) {
		this.exClk = exClk;
	}
	public Cpu getCpu() {
		return cpu;
	}
	public void setCpu(Cpu cpu) {
		this.cpu = cpu;
	}
	public Board() {	
		super();
		cpu = new Cpu();
		// TODO Auto-generated constructor stub
	}
	public String getBoardName() {
		return boardName;
	}
	public void setBoardName(String boardName) {
		this.boardName = boardName;
	}
	public Board(String boardName, String exClk, Cpu cpu) {
		super();
		this.boardName = boardName;
		this.exClk = exClk;
		this.cpu = cpu;
	}
	
	
		
}
