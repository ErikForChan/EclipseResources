package org.eclipse.cdt.ui.wizards.cpu;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.ui.wizards.cpu.core.Core;
import org.eclipse.cdt.ui.wizards.cpu.core.memory.Memory;

public class Cpu {
	private int coreNum;
	private String cpuName;
	private List<Core> cores = new ArrayList<Core>();
	
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
	public Cpu(int coreNum, String cpuName, List<Core> cores) {
		super();
		this.coreNum = coreNum;
		this.cpuName = cpuName;
		this.cores = cores;
	}
	public Cpu() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
