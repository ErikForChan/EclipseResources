package com.djyos.dide.ui.objects;

public class CoreMemory {
	String name;
	String type;
	String startAddr;
	String size;

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

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public CoreMemory(String name, String type, String startAddr, String size) {
		super();
		this.name = name;
		this.type = type;
		this.startAddr = startAddr;
		this.size = size;
	}

	public CoreMemory() {
		super();
		// TODO Auto-generated constructor stub
	}

}
