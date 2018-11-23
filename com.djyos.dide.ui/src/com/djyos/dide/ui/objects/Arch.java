/*******************************************************************************
 * Copyright (c) 2017 Djyos Team.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.djyos.com
 *
 * Contributors:
 *     Djyos Team - Jiaming Chen
 *******************************************************************************/
package com.djyos.dide.ui.objects;

public class Arch {
	private String serie;
	private String architecture;
	private String family;
	private String march;// Cross ARM GCC CSky abiv2 Elf Toolchain
	private String mcpu;
	private String fpuType;
	private String archPath;
	private String toolchain;

	public Arch() {
		super();
	}

	public Arch(String serie, String architecture, String family, String march, String mcpu, String fpuType,
			String archPath, String toolchain) {
		super();
		this.serie = serie;
		this.architecture = architecture;
		this.family = family;
		this.march = march;
		this.mcpu = mcpu;
		this.fpuType = fpuType;
		this.archPath = archPath;
		this.toolchain = toolchain;
	}

	public String getToolchain() {
		return toolchain;
	}

	public void setToolchain(String toolchain) {
		this.toolchain = toolchain;
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
