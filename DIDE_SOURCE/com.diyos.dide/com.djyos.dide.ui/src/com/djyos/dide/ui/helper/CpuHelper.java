package com.djyos.dide.ui.helper;

import java.io.File;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.resources.IProject;

import com.djyos.dide.ui.objects.Cpu;
import com.djyos.dide.ui.wizards.cpu.GetNonCpuFiles;
import com.djyos.dide.ui.wizards.djyosProject.DjyosMessages;

public class CpuHelper {
	
	public static void setCpuFilesExclude(IProject project, ICConfigurationDescription[] conds, Cpu myCpu,
			List<Cpu> allCpus) {
		// Exclude没有描述文件的的cpu
		GetNonCpuFiles gncf = new GetNonCpuFiles();
		List<File> excludeCpuFiles = gncf.getNonCpus();
		for (File f : excludeCpuFiles) {
			DideHelper.setFolderExclude(f, project, conds, true);
		}

		// Exclude不需要的cpu
		for (Cpu c : allCpus) {
			if (!c.getCpuName().equals(myCpu.getCpuName())) {
				String parentPath = c.getCpuFolderPath();
				File parentFile = new File(parentPath);
				travelCpuParentInclude(parentFile, project, conds);
				travelCpuParentExclude(parentFile, project, conds);
			}
		}
		
		// Include需要的cpu
		File myCpuParentFile = new File(myCpu.getCpuFolderPath());
		travelCpuParentInclude(myCpuParentFile, project, conds);
	}
	
	private static void travelCpuParentExclude(File parentFile, IProject project, ICConfigurationDescription[] conds) {
		DideHelper.setFolderExclude(parentFile, project, conds, true);
		parentFile = parentFile.getParentFile();
		if (!parentFile.getName().equals("cpudrv")) {
			travelCpuParentExclude(parentFile, project, conds);
		}
	}
	
	private static void travelCpuParentInclude(File file, IProject project, ICConfigurationDescription[] conds) {
		File parentFile = file.getParentFile();
		if (!parentFile.getName().equals("cpudrv")) {
			travelCpuParentInclude(parentFile, project, conds);
		}
		DideHelper.setFolderExclude(file, project, conds, false);
	}
	
	private static void resetTargetName(ICConfigurationDescription[] conds, String projectName) {
		for (ICConfigurationDescription cfgDesc : conds) {
			String s = cfgDesc.getName();
			if (s.contains(DjyosMessages.Configuration_Debug) || s.contains(DjyosMessages.Configuration_Release)) {
				if (!s.contains("libos")) {
					cfgDesc.setName(projectName + "_" + s);
				}
			}
		}
	}
}
