package com.djyos.dide.ui.objects;

import java.util.ArrayList;
import java.util.List;

public class SelectObj {
	
	private int count;
	private int last_check_index;
	private List<Integer> para_index_s = new ArrayList<Integer>();
	
	public int getLast_check_index() {
		return last_check_index;
	}
	public void setLast_check_index(int last_check_index) {
		this.last_check_index = last_check_index;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public List<Integer> getPara_index_s() {
		return para_index_s;
	}
	public void setPara_index_s(List<Integer> para_index_s) {
		this.para_index_s = para_index_s;
	}
	
	
	
}
