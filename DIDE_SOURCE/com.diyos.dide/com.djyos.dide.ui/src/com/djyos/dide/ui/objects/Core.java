package com.djyos.dide.ui.objects;

import java.util.ArrayList;
import java.util.List;

public class Core {
	private int id;
	private String name;
	private Arch arch = new Arch();
	private String floatABI;
	private String fpuType;
	private String resetAddr;
	private int coreClk;
	private List<CoreMemory> memorys = new ArrayList<CoreMemory>();
	
	public Core(int id, Arch arch, String floatABI, String fpuType, String resetAddr, int coreClk,
			List<CoreMemory> memorys) {
		super();
		this.id = id;
		this.arch = arch;
		this.floatABI = floatABI;
		this.fpuType = fpuType;
		this.resetAddr = resetAddr;
		this.coreClk = coreClk;
		this.memorys = memorys;
	}
	
	
	
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public Arch getArch() {
		return arch;
	}
	public void setArch(Arch arch) {
		this.arch = arch;
	}
	public int getCoreClk() {
		return coreClk;
	}
	public void setCoreClk(int coreClk) {
		this.coreClk = coreClk;
	}
	public String getFloatABI() {
		return floatABI;
	}
	public void setFloatABI(String floatABI) {
		this.floatABI = floatABI;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getFpuType() {
		return fpuType;
	}
	public void setFpuType(String fpuType) {
		this.fpuType = fpuType;
	}
	public String getResetAddr() {
		return resetAddr;
	}
	public void setResetAddr(String resetAddr) {
		this.resetAddr = resetAddr;
	}
	public List<CoreMemory> getMemorys() {
		return memorys;
	}
	public void setMemorys(List<CoreMemory> memorys) {
		this.memorys = memorys;
	}

	public Core() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
