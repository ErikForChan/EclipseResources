package com.djyos.dide.ui.arch;

public class Arch {
	private String type;
	private String serial;
	private String family;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getSerial() {
		return serial;
	}
	public void setSerial(String serial) {
		this.serial = serial;
	}
	public String getFamily() {
		return family;
	}
	public void setFamily(String family) {
		this.family = family;
	}
	public Arch(String type, String serial, String family) {
		super();
		this.type = type;
		this.serial = serial;
		this.family = family;
	}
	public Arch() {
		super();
	}
	
}
