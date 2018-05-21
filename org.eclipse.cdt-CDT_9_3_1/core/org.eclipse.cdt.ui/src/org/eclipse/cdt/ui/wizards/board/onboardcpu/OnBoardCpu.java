package org.eclipse.cdt.ui.wizards.board.onboardcpu;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.ui.wizards.component.Component;

public class OnBoardCpu {
	private int mianClk;
	private int rtcClk;
	private String cpuName;
	private List<Component> peripherals = new ArrayList<Component>();
	private List<Chip> chips = new ArrayList<Chip>();
	private List<OnBoardMemory> memorys = new ArrayList<OnBoardMemory>();
	public int getMianClk() {
		return mianClk;
	}
	public void setMianClk(int mianClk) {
		this.mianClk = mianClk;
	}
	public int getRtcClk() {
		return rtcClk;
	}
	public void setRtcClk(int rtcClk) {
		this.rtcClk = rtcClk;
	}
	public String getCpuName() {
		return cpuName;
	}
	public void setCpuName(String cpuName) {
		this.cpuName = cpuName;
	}
	public List<Component> getPeripherals() {
		return peripherals;
	}
	public void setPeripherals(List<Component> peripherals) {
		this.peripherals = peripherals;
	}
	public List<Chip> getChips() {
		return chips;
	}
	public void setChips(List<Chip> chips) {
		this.chips = chips;
	}
	public List<OnBoardMemory> getMemorys() {
		return memorys;
	}
	public void setMemorys(List<OnBoardMemory> memorys) {
		this.memorys = memorys;
	}
	public OnBoardCpu(int mianClk, int rtcClk, String cpuName, List<Component> peripherals, List<Chip> chips,
			List<OnBoardMemory> memorys) {
		super();
		this.mianClk = mianClk;
		this.rtcClk = rtcClk;
		this.cpuName = cpuName;
		this.peripherals = peripherals;
		this.chips = chips;
		this.memorys = memorys;
	}
	public OnBoardCpu() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
