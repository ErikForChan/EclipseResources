package org.eclipse.cdt.ui.wizards.cpu;

public class CpuBak {
	public String name;
	public int coreNum;
	public String type;
	public String family;
	public String architecture;
	public String instructionSet;
	public String endianeness;
	public String flashStart;
	public String flashSize;
	public String ramStart;
	public String ramSize;
	public String fpuABI;
	public String fpuType;
	public String resetAddr;
	public String category;

	public CpuBak(String name, int coreNum, String type, String family, String architecture,
			String instructionSet, String endianeness, String flashStart, String flashSize, String ramStart,
			String ramSize, String fpuABI, String fpuType, String resetAddr, String category) {
		super();
		this.name = name;
		this.coreNum = coreNum;
		this.type = type;
		this.family = family;
		this.architecture = architecture;
		this.instructionSet = instructionSet;
		this.endianeness = endianeness;
		this.flashStart = flashStart;
		this.flashSize = flashSize;
		this.ramStart = ramStart;
		this.ramSize = ramSize;
		this.fpuABI = fpuABI;
		this.fpuType = fpuType;
		this.resetAddr = resetAddr;
		this.category = category;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCoreNum() {
		return coreNum;
	}

	public void setCoreNum(int coreNum) {
		this.coreNum = coreNum;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFamily() {
		return family;
	}

	public void setFamily(String family) {
		this.family = family;
	}

	public String getArchitecture() {
		return architecture;
	}

	public void setArchitecture(String architecture) {
		this.architecture = architecture;
	}

	public String getInstructionSet() {
		return instructionSet;
	}

	public void setInstructionSet(String instructionSet) {
		this.instructionSet = instructionSet;
	}

	public String getEndianeness() {
		return endianeness;
	}

	public void setEndianeness(String endianeness) {
		this.endianeness = endianeness;
	}

	public String getFlashStart() {
		return flashStart;
	}

	public void setFlashStart(String flashStart) {
		this.flashStart = flashStart;
	}

	public String getFlashSize() {
		return flashSize;
	}

	public void setFlashSize(String flashSize) {
		this.flashSize = flashSize;
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

	public String getFpuABI() {
		return fpuABI;
	}

	public void setFpuABI(String fpuABI) {
		this.fpuABI = fpuABI;
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

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public CpuBak() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
