package com.djyos.dide.ui.objects;

import java.util.ArrayList;
import java.util.List;

import com.djyos.dide.ui.bsp.BspTemplate;

public class BspStep {
	private String name;
	private String info;
	private List<BspTemplate> templates = new ArrayList<BspTemplate>();

	public BspStep(String name, String info, List<BspTemplate> templartes) {
		super();
		this.name = name;
		this.info = info;
		this.templates = templartes;
	}

	public BspStep() {
		// TODO Auto-generated constructor stub
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public List<BspTemplate> getTemplates() {
		return templates;
	}

	public void setTemplates(List<BspTemplate> templartes) {
		this.templates = templartes;
	}

}
