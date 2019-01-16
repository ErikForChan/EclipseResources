package com.djyos.dide.ui.objects;

import java.util.ArrayList;
import java.util.List;

public class Cpu {
	private int coreNum;
	private String cpuName;
	private String parentPath;
	private List<Core> cores = new ArrayList<Core>();
	private List<CoreMemory> shared_memorys = new ArrayList<CoreMemory>();
	
	public void setSharedMemoryNames() {
		for(int i=0;i<shared_memorys.size();i++) {
			shared_memorys.get(i).setName("memory"+(i+1));
		}
	}

	public List<CoreMemory> getShared_memorys() {
		return shared_memorys;
	}

	public void setShared_memorys(List<CoreMemory> shared_memorys) {
		this.shared_memorys = shared_memorys;
	}

	public String getCpuFolderPath() {
		return parentPath;
	}

	public void setCpuFolderPath(String parentPath) {
		this.parentPath = parentPath;
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
