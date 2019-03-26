package com.djyos.dide.ui.autotesting;

import java.util.ArrayList;
import java.util.List;

import com.djyos.dide.ui.objects.Board;
import com.djyos.dide.ui.objects.Component;
import com.djyos.dide.ui.objects.Core;
import com.djyos.dide.ui.objects.Cpu;

public class ProjectObj {
	
	private String name;
	private int type;
	private Board board;
	private Cpu cpu;
	private Core core;
	private int core_colck;
	private List<Component> components = new ArrayList<Component>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public Board getBoard() {
		return board;
	}
	public void setBoard(Board board) {
		this.board = board;
	}
	public Cpu getCpu() {
		return cpu;
	}
	public void setCpu(Cpu cpu) {
		this.cpu = cpu;
	}
	public Core getCore() {
		return core;
	}
	public void setCore(Core core) {
		this.core = core;
	}
	public int getCore_colck() {
		return core_colck;
	}
	public void setCore_colck(int core_colck) {
		this.core_colck = core_colck;
	}
	public List<Component> getComponents() {
		return components;
	}
	public void setComponents(List<Component> components) {
		this.components = components;
	}
	
	
	
}
