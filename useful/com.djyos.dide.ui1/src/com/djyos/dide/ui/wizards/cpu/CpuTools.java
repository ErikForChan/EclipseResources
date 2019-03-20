package com.djyos.dide.ui.wizards.cpu;

import com.djyos.dide.ui.objects.Cpu;

public class CpuTools {
	
	public static void updateCurCpu(Cpu curCpu, Cpu newCpu) {
		if(newCpu.getCoreNum() != 0) {
			curCpu.setCoreNum(newCpu.getCoreNum());
		}
		if(newCpu.getCores().size() != 0) {
			
		}
		
	}

}
