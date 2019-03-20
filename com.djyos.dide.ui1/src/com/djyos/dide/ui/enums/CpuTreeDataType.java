package com.djyos.dide.ui.enums;

public enum CpuTreeDataType {

	GROUP("group", 1), CPU("cpu", 2), ARCH("arch", 3);
	private String dataType;
	private int index;

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	private CpuTreeDataType(String dataType, int index) {
		this.dataType = dataType;
		this.index = index;
	}

}
