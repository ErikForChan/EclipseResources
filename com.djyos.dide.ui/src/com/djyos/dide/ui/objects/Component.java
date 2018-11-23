package com.djyos.dide.ui.objects;

import java.util.ArrayList;
import java.util.List;

public class Component {
	private String name;
	private String annotation;
	private String attribute;
	private String grade;
	private String code;
	private String configure;
	private String linkFolder;
	private String fileName;// 描述文件.c/.h文件名
	private String selectable;
	private String parent;
	private boolean isSelect = false;
	private List<String> dependents = new ArrayList<String>();
	private List<String> weakDependents = new ArrayList<String>();
	private List<String> mutexs = new ArrayList<String>();
	private List<String> excludes = new ArrayList<String>();
	private List<String> includes = new ArrayList<String>();
	private String parentPath;// .h/.c父目录
	private String target;

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public List<String> getIncludes() {
		return includes;
	}

	public void setIncludes(List<String> includes) {
		this.includes = includes;
	}

	public String getParentPath() {
		return parentPath;
	}

	public void setParentPath(String parentPath) {
		this.parentPath = parentPath;
	}

	public boolean isSelect() {
		return isSelect;
	}

	public void setSelect(boolean isSelect) {
		this.isSelect = isSelect;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public List<String> getExcludes() {
		return excludes;
	}

	public void setExcludes(List<String> excludes) {
		this.excludes = excludes;
	}

	public List<String> getWeakDependents() {
		return weakDependents;
	}

	public void setWeakDependents(List<String> weakDependents) {
		this.weakDependents = weakDependents;
	}

	public String getSelectable() {
		return selectable;
	}

	public void setSelectable(String selectable) {
		this.selectable = selectable;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getLinkFolder() {
		return linkFolder;
	}

	public void setLinkFolder(String linkFolder) {
		this.linkFolder = linkFolder;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public List<String> getDependents() {
		return dependents;
	}

	public void setDependents(List<String> dependents) {
		this.dependents = dependents;
	}

	public List<String> getMutexs() {
		return mutexs;
	}

	public void setMutexs(List<String> mutexs) {
		this.mutexs = mutexs;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getConfigure() {
		return configure;
	}

	public void setConfigure(String configure) {
		this.configure = configure;
	}

	public Component(String name, String annotation, String attribute, String grade, String code, String configure,
			List<String> dependents, List<String> mutexs) {
		super();
		this.name = name;
		this.annotation = annotation;
		this.attribute = attribute;
		this.grade = grade;
		this.code = code;
		this.configure = configure;
		this.dependents = dependents;
		this.mutexs = mutexs;
	}

	public Component() {
		super();
		// TODO Auto-generated constructor stub
	}

}
