package org.eclipse.cdt.internal.ui.djyproperties;

public class Module {
	String name;
	String parent;
	String rule;
	String haveChild;
	boolean isChecked;
	boolean visited;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getParent() {
		return parent;
	}
	public void setParent(String parent) {
		this.parent = parent;
	}
	public boolean isChecked() {
		return isChecked;
	}
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}
	
	public String getHaveChild() {
		return haveChild;
	}
	public void setHaveChild(String haveChild) {
		this.haveChild = haveChild;
	}
	public String getRule() {
		return rule;
	}
	public void setRule(String rule) {
		this.rule = rule;
	}
	
	public boolean isVisited() {
		return visited;
	}
	public void setVisited(boolean visited) {
		this.visited = visited;
	}
	public Module(String name, String parent,String haveChild, boolean isChecked,boolean visited) {
		super();
		this.name = name;
		this.parent = parent;
		this.haveChild = haveChild;
		this.isChecked = isChecked;
		this.visited = visited;
	}
	
}
