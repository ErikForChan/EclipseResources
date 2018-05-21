package org.eclipse.cdt.ui.wizards.board.onboardcpu;

public class OnBoardMemory {
	String name;
	String type;
	String startAddr;
	int size;

	public OnBoardMemory(String name, String type, String startAddr, int size) {
		super();
		this.name = name;
		this.type = type;
		this.startAddr = startAddr;
		this.size = size;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStartAddr() {
		return startAddr;
	}

	public void setStartAddr(String startAddr) {
		this.startAddr = startAddr;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public OnBoardMemory() {
		super();
		// TODO Auto-generated constructor stub
	}
}
