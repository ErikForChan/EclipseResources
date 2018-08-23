package com.djyos.dide.ui.wizards.component;

import java.util.ArrayList;
import java.util.List;

import com.djyos.dide.ui.wizards.component.Parameter;

public class InitInfo {
	String funName;
	String returnType;
	String grade;
	List<Parameter> parameters = new ArrayList<Parameter>();
	
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
	
	public InitInfo(String funName, String returnType, String grade, List<Parameter> parameters) {
		super();
		this.funName = funName;
		this.returnType = returnType;
		this.grade = grade;
		this.parameters = parameters;
	}
	public InitInfo() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
