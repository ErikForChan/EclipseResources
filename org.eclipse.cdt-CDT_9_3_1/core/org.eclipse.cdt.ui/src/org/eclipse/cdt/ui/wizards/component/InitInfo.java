package org.eclipse.cdt.ui.wizards.component;

import java.util.List;

public class InitInfo {
	String funName;
	String returnType;
	String grade;
	List<Parameter> parameters;
	List<String> dependents;
	List<String> mutexs;
	public String getFunName() {
		return funName;
	}
	public void setFunName(String funName) {
		this.funName = funName;
	}
	public String getReturnType() {
		return returnType;
	}
	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}
	public String getGrade() {
		return grade;
	}
	public void setGrade(String grade) {
		this.grade = grade;
	}
	public List<Parameter> getParameters() {
		return parameters;
	}
	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
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
	
	public InitInfo(String funName, String returnType, String grade, List<Parameter> parameters,
			List<String> dependents, List<String> mutexs) {
		super();
		this.funName = funName;
		this.returnType = returnType;
		this.grade = grade;
		this.parameters = parameters;
		this.dependents = dependents;
		this.mutexs = mutexs;
	}
	public InitInfo() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
