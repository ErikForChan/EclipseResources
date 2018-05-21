package org.eclipse.cdt.ui.wizards.cpu.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.ui.wizards.cpu.core.memory.CoreMemory;

public class Core {
	private int id;
	private String type;
	private String arch;
	private String family;
	private String fpuType;
	private String resetAddr;
	private List<CoreMemory> memorys = new ArrayList<CoreMemory>();
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getArch() {
		return arch;
	}
	public void setArch(String arch) {
		this.arch = arch;
	}
	public String getFamily() {
		return family;
	}
	public void setFamily(String family) {
		this.family = family;
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

	public Core(int id, String type, String arch, String family, String fpuType, String resetAddr,
			List<CoreMemory> memorys) {
		super();
		this.id = id;
		this.type = type;
		this.arch = arch;
		this.family = family;
		this.fpuType = fpuType;
		this.resetAddr = resetAddr;
		this.memorys = memorys;
	}
	public Core() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
