package com.djyos.dide.ui.arch;

public class Arch {
	private String serie;
	private String architecture;
	private String family;
	private String march;
	private String mcpu;
	private String fpuType;
	private String archPath;
	
	public Arch() {
		super();
	}

	public Arch(String serie, String architecture, String family, String march, String mcpu, String fpuType) {
		super();
		this.serie = serie;
		this.architecture = architecture;
		this.family = family;
		this.march = march;
		this.mcpu = mcpu;
		this.fpuType = fpuType;
	}
	
	public String getArchPath() {
		return archPath;
	}

	public void setArchPath(String archPath) {
		this.archPath = archPath;
	}

	public String getSerie() {
		return serie;
	}

	public void setSerie(String serie) {
		this.serie = serie;
	}

	public String getArchitecture() {
		return architecture;
	}
	public void setArchitecture(String architecture) {
		this.architecture = architecture;
	}
	public String getFamily() {
		return family;
	}
	public void setFamily(String family) {
		this.family = family;
	}
	public String getMarch() {
		return march;
	}
	public void setMarch(String march) {
		this.march = march;
	}
	public String getMcpu() {
		return mcpu;
	}
	public void setMcpu(String mcpu) {
		this.mcpu = mcpu;
	}
	public String getFpuType() {
		return fpuType;
	}
	public void setFpuType(String fpuType) {
		this.fpuType = fpuType;
	}
	
}
