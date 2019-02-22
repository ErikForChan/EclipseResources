package com.djyos.dide.ui.wizards.djyosProject.tools;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;

public class ProjectPattern {
	
	static String pattern = "$(call loop, 50, ${INPUTS}, ${COMMAND} ${FLAGS} ${OUTPUT})";
	
	public static void handleBuilderPattern(String toolChainName, IConfiguration cfg, String conName) {
		if (toolChainName.indexOf("CSky") != -1 || toolChainName.indexOf("CKcore") != -1) {
			cfg.setBuildCommand("make");
			cfg.setBuildArguments("SHELL=bash.exe" + " "
					+ cfg.getBuildArguments().replaceAll("SHELL=cmd.exe", "").replaceAll("SHELL=bash.exe", "").trim());
		} else {
			cfg.setBuildCommand("${cross_make}");
			cfg.setBuildArguments(
					"SHELL=cmd.exe" + " " + cfg.getBuildArguments().replaceAll("SHELL=cmd.exe", "").trim());
		}

		if (!conName.contains("libos")) {
			cfg.setPostbuildStep("");
			ITool[] tools = cfg.getToolChain().getTools();
			for (ITool tool : tools) {
				String toolName = tool.getName();
				if (toolName.contains("Cross ARM GNU C++ Linker") || toolName.contains("CSky Elf C++ Linker")) {
					String curPattern = tool.getCommandLinePattern().split("&&")[0].trim();
					tool.setCommandLinePattern(curPattern+" && addsh.exe ${ConfigName}.elf");
				}
				if (toolName.contains("Cross ARM GNU Create Flash Image") || toolName.contains("CSky Elf Create Flash Image") ) {
					String curPattern = tool.getCommandLinePattern().split("&&")[0].trim();
					tool.setCommandLinePattern(curPattern+" && addver.exe ${ConfigName}.bin");
				}
			}
		}
		if (conName.contains("libos")) {
			if(conName.contains("_App")) {
				cfg.setArtifactName("libos_App");
			}else if(conName.contains("_Iboot")) {
				cfg.setArtifactName("libos_Iboot");
			} 
			
			ITool[] tools = cfg.getToolChain().getTools();
			for (ITool tool : tools) {
				if (tool.getName().contains("Cross ARM GNU Archiver") || tool.getName().contains("CSky Elf Archiver")) {
					if (!tool.getCommandLinePattern().equals(pattern)) {
						tool.setCommandLinePattern(pattern);
						IOption[] options = tool.getOptions();
						for(IOption op:options) {
							if(op.getName().equalsIgnoreCase("Archiver flags")) {
								try {
									op.setValue("-rPu");
								} catch (BuildException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}
				}

			}
		}
	}
	
}
