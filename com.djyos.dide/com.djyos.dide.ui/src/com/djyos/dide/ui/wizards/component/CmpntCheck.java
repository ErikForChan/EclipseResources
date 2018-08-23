package com.djyos.dide.ui.wizards.component;

public class CmpntCheck {
	private String cmpntName;
	private String checked;
	public String getCmpntName() {
		return cmpntName;
	}
	public void setCmpntName(String cmpntName) {
		this.cmpntName = cmpntName;
	}
	public String isChecked() {
		return checked;
	}
	public void setChecked(String checked) {
		this.checked = checked;
	}
	public CmpntCheck(String cmpntName, String checked) {
		super();
		this.cmpntName = cmpntName;
		this.checked = checked;
	}
	
}
