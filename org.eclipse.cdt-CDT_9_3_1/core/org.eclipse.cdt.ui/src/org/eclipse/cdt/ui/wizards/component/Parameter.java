package org.eclipse.cdt.ui.wizards.component;

public class Parameter {
	private String type;
	private String connector;
	private String annotation;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getConnector() {
		return connector;
	}
	public void setConnector(String connector) {
		this.connector = connector;
	}
	public String getAnnotation() {
		return annotation;
	}
	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}
	public Parameter(String type, String connector, String annotation) {
		super();
		this.type = type;
		this.connector = connector;
		this.annotation = annotation;
	}
	public Parameter() {
		super();
		// TODO Auto-generated constructor stub
	}	
	
}
