package org.eclipse.cdt.ui.wizards.parsexml;

public class Cpu {
	public int id;
	public String device;
	public String core;
	public String flashStart;
	public String flashSize;
	public String ramStart;
	public String ramSize;
	public String architecture;
	public String fpuType;
	public String category;
	public String getDevice() {
		return device;
	}
	public void setDevice(String device) {
		this.device = device;
	}
	public String getCore() {
		return core;
	}
	public void setCore(String core) {
		this.core = core;
	}

	public String getFlashSize() {
		return flashSize;
	}
	public void setFlashSize(String flashSize) {
		this.flashSize = flashSize;
	}
	
	public String getArchitecture() {
		return architecture;
	}
	public void setArchitecture(String architecture) {
		this.architecture = architecture;
	}
	public String getFpuType() {
		return fpuType;
	}
	public void setFpuType(String fpuType) {
		this.fpuType = fpuType;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public String getFlashStart() {
		return flashStart;
	}
	public void setFlashStart(String flashStart) {
		this.flashStart = flashStart;
	}
	public String getRamStart() {
		return ramStart;
	}
	public void setRamStart(String ramStart) {
		this.ramStart = ramStart;
	}
	public String getRamSize() {
		return ramSize;
	}
	public void setRamSize(String ramSize) {
		this.ramSize = ramSize;
	}
	
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public Cpu(int id, String device, String core, String flashStart, String flashSize, String ramStart,
			String ramSize, String architecture, String fpuType, String category) {
		super();
		this.id = id;
		this.device = device;
		this.core = core;
		this.flashStart = flashStart;
		this.flashSize = flashSize;
		this.ramStart = ramStart;
		this.ramSize = ramSize;
		this.architecture = architecture;
		this.fpuType = fpuType;
		this.category = category;
	}
	public Cpu() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
