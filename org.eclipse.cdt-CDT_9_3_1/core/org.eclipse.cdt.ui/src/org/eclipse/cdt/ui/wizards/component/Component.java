package org.eclipse.cdt.ui.wizards.component;

import java.util.List;

public class Component {
	String name;
	String annotation;
	String type;
	String includeFile;
	List<String> dependents;
	List<String> mutexs;
	InitInfo init;

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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getIncludeFile() {
		return includeFile;
	}

	public void setIncludeFile(String includeFile) {
		this.includeFile = includeFile;
	}

	public InitInfo getInit() {
		return init;
	}

	public void setInit(InitInfo init) {
		this.init = init;
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

	public Component(String name, String annotation, String type, String includeFile, List<String> dependents,
			List<String> mutexs, InitInfo init) {
		super();
		this.name = name;
		this.annotation = annotation;
		this.type = type;
		this.includeFile = includeFile;
		this.dependents = dependents;
		this.mutexs = mutexs;
		this.init = init;
	}

	public Component() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
