package org.eclipse.cdt.ui.wizards.component;

import java.util.ArrayList;
import java.util.List;

public class Component {
	String name;
	String annotation;
	String attribute;
	String grade;
	String code;
	String configure;
	String includeFile;
	List<String> dependents = new ArrayList<String>();;
	List<String> mutexs = new ArrayList<String>();
//	InitInfo init;

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

	public String getIncludeFile() {
		return includeFile;
	}

	public void setIncludeFile(String includeFile) {
		this.includeFile = includeFile;
	}

//	public InitInfo getInit() {
//		return init;
//	}
//
//	public void setInit(InitInfo init) {
//		this.init = init;
//	}
	
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

	public Component(String name, String annotation, String attribute, String grade, String code,
			String configure, String includeFile, List<String> dependents, List<String> mutexs) {
		super();
		this.name = name;
		this.annotation = annotation;
		this.attribute = attribute;
		this.grade = grade;
		this.code = code;
		this.configure = configure;
		this.includeFile = includeFile;
		this.dependents = dependents;
		this.mutexs = mutexs;
	}

	public Component() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
