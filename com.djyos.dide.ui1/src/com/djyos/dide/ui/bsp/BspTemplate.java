package com.djyos.dide.ui.bsp;

public class BspTemplate {
	private String fileName;
	private String location;

	public BspTemplate(String fileName, String location) {
		super();
		this.fileName = fileName;
		this.location = location;
	}

	public BspTemplate() {
		// TODO Auto-generated constructor stub
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

}
