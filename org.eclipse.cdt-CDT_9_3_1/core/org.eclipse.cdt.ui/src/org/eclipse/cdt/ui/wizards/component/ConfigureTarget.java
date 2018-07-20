package org.eclipse.cdt.ui.wizards.component;

public enum ConfigureTarget {
	HEADER("header", 1), CMDLINE("cmdline", 2);
	// 成员变量  
    private String name;  
    private int index; 
	private ConfigureTarget(String name, int index) {
		this.name = name;
		this.index = index;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	
}
