package org.eclipse.cdt.ui.wizards.parsexml;

public class Board {

	public String boardName;
	public String exClk;
	public Cpu cpu;
	public String extromStart;
	public String extromSize;
	public String extramStart;
	public String extramSize;
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
	
	public String getExtromStart() {
		return extromStart;
	}
	public void setExtromStart(String extromStart) {
		this.extromStart = extromStart;
	}
	public String getExtromSize() {
		return extromSize;
	}
	public void setExtromSize(String extromSize) {
		this.extromSize = extromSize;
	}
	public String getExtramStart() {
		return extramStart;
	}
	public void setExtramStart(String extramStart) {
		this.extramStart = extramStart;
	}
	public String getExtramSize() {
		return extramSize;
	}
	public void setExtramSize(String extramSize) {
		this.extramSize = extramSize;
	}
	public Board(String boardName, String exClk, Cpu cpu, String extromStart, String extromSize,
			String extramStart, String extramSize) {
		super();
		this.boardName = boardName;
		this.exClk = exClk;
		this.cpu = cpu;
		this.extromStart = extromStart;
		this.extromSize = extromSize;
		this.extramStart = extramStart;
		this.extramSize = extramSize;
	}
		
}
