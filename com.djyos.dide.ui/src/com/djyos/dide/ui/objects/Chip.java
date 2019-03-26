package com.djyos.dide.ui.objects;

public class Chip {
	private String theInterface;
	private String chipName;
	public String getTheInterface() {
		return theInterface;
	}
	public void setTheInterface(String theInterface) {
		this.theInterface = theInterface;
	}
	public String getChipName() {
		return chipName;
	}
	public void setChipName(String chipName) {
		this.chipName = chipName;
	}
	public Chip(String theInterface, String chipName) {
		super();
		this.theInterface = theInterface;
		this.chipName = chipName;
	}
	public Chip() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
