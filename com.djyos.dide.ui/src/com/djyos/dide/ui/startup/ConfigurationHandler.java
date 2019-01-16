package com.djyos.dide.ui.startup;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import java.util.regex.Matcher;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICMultiConfigDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.MultiConfiguration;

import com.djyos.dide.ui.wizards.djyosProject.DjyosMessages;

public class ConfigurationHandler{
	//BuildConsolePartitioner BuildConsolePreferencePage
	String pattern = "$(call loop, 50, ${INPUTS}, ${COMMAND} ${FLAGS} ${OUTPUT})";
//	String pattern = "${COMMAND} ${FLAGS} ${OUTPUT_FLAG} ${OUTPUT_PREFIX}${OUTPUT} $(call rwildcard,./,*.o)";
//	String pattern = "${COMMAND} ${FLAGS} ${OUTPUT_FLAG} ${OUTPUT_PREFIX}${OUTPUT} $(addsuffix *.o,$(subst \\,/,$(dir $(subst $(dir $(shell for /r  %%i in (*makefile) do @echo %%i)),./,$(dir $(shell for /r  %%i in (*subdir.mk) do @echo %%i))))))";
	public void handlerConfiguration(){
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject[] projects = workspace.getRoot().getProjects();
		if (projects.length > 0) {
			for (IProject project : projects) {
				setBuildingToAuto(workspace);
				setDefaultArchiverCmd(project);
			}
		}
	}
	
	public static IConfiguration getCfg(ICConfigurationDescription cfgd) {
		if (cfgd instanceof ICMultiConfigDescription) {
			ICConfigurationDescription[] cfds = (ICConfigurationDescription[])((ICMultiConfigDescription)cfgd).getItems();
			return new MultiConfiguration(cfds);
		} else 
			return ManagedBuildManager.getConfigurationForDescription(cfgd);
	}


	//设置默认命令为pattern
	public void setDefaultArchiverCmd(IProject project) {
		// TODO Auto-generated method stub
		String str;

		final ICProjectDescription local_prjd = CoreModel.getDefault().getProjectDescription(project);
		if (local_prjd != null) {
			ICConfigurationDescription[] conds = local_prjd.getConfigurations();
			for (ICConfigurationDescription cfgDesc : conds) {
				String s = cfgDesc.getName();
				IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgDesc);

				String toolChainName = cfg.getToolChain().getName();
				if (toolChainName.indexOf("CSky") != -1 || toolChainName.indexOf("CKcore") != -1) {
					cfg.setBuildCommand("make");
					cfg.setBuildArguments("SHELL=bash.exe" + " " + cfg.getBuildArguments()
							.replaceAll("SHELL=cmd.exe", "").replaceAll("SHELL=bash.exe", "").trim());
				} else {
					cfg.setBuildCommand("${cross_make}");
					cfg.setBuildArguments(
							"SHELL=cmd.exe" + " " + cfg.getBuildArguments().replaceAll("SHELL=cmd.exe", "").trim());
				}

				if (!(s.contains("libos") || toolChainName.indexOf("CSky") != -1
						|| toolChainName.indexOf("CKcore") != -1)) {
					str = "make " + s + ".bin && elf_to_bin.exe " + s + ".elf " + s + ".bin && ren " + s + ".bin new"
							+ s + ".bin";
					cfg.setPostbuildStep(str);
				}
				
				if (s.contains("libos")) {
					ITool[] tools = cfg.getToolChain().getTools();
					for (ITool tool : tools) {
						if (tool.getName().contains("Cross ARM GNU Archiver") || tool.getName().contains("CSky Elf Archiver")) {
							if (!tool.getCommandLinePattern().equals(pattern)) {
								tool.setCommandLinePattern(pattern);
							}
						}

					}
				}
				
				try {
					CoreModel.getDefault().setProjectDescription(project, local_prjd);
				} catch (CoreException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	//设置工程不为自动编译
	private void setBuildingToAuto(IWorkspace workspace) {
		// TODO Auto-generated method stub
		IWorkspaceDescription desc = workspace.getDescription();
		desc.setAutoBuilding(false);
		try {
			workspace.setDescription(desc);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
