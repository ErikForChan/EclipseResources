package org.eclipse.cdt.ui.wizards.cpu;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.ui.wizards.cpu.core.Core;
import org.eclipse.cdt.ui.wizards.cpu.core.memory.CoreMemory;

public class Cpu {
	private int coreNum;
	private String cpuName;
	private String firmwareLib;
	private String parentPath;
	private List<Core> cores = new ArrayList<Core>();
	
	public String getParentPath() {
		return parentPath;
	}
	public void setParentPath(String parentPath) {
		this.parentPath = parentPath;
	}
	public String getFirmwareLib() {
		return firmwareLib;
	}
	public void setFirmwareLib(String firmwareLib) {
		this.firmwareLib = firmwareLib;
	}
	public int getCoreNum() {
		return coreNum;
	}
	public void setCoreNum(int coreNum) {
		this.coreNum = coreNum;
	}
	public String getCpuName() {
		return cpuName;
	}
	public void setCpuName(String cpuName) {
		this.cpuName = cpuName;
	}
	public List<Core> getCores() {
		return cores;
	}
	public void setCores(List<Core> cores) {
		this.cores = cores;
	}
	public Cpu(String cpuName, List<Core> cores) {
		super();
		this.cpuName = cpuName;
		this.cores = cores;
	}

	public Cpu(String cpuName, String parentPath, List<Core> cores) {
		super();
		this.cpuName = cpuName;
		this.parentPath = parentPath;
		this.cores = cores;
	}
	public Cpu() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
