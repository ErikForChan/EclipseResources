package com.djyos.dide.ui.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.internal.ide.actions.BuildUtilities;

import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.job.BuildTarget;
import com.djyos.dide.ui.wizards.djyosProject.tools.FileTool;
import com.djyos.dide.ui.wizards.djyosProject.tools.PathTool;

public class GitUpdateHandler {

	final IWorkspace workspace = ResourcesPlugin.getWorkspace();
	IProject[] projects = workspace.getRoot().getProjects();
	IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
	File gitVerFile = new File(PathTool.getDIDEPath() + "IDE/configuration/.settings/com.djyos.gitVerson.prefs");
	
	private void setGitVersion(File svnVerFile, long version) {
		// TODO Auto-generated method stub
		System.out.println("setSvnVersion");
		BufferedReader br = null;
		String line = null;
		StringBuffer bufAll = new StringBuffer(); // 保存修改过后的所有内容，不断增加
		boolean find = false;
		try {
			br = new BufferedReader(new FileReader(svnVerFile));
			while ((line = br.readLine()) != null) {
				// 修改内容核心代码
				if (line.startsWith("SVN_VERION")) {
					line = "SVN_VERION=" + version;
					find = true;
				}
				bufAll.append(line + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					br = null;
				}
			}
		}
		if (!find) {
			bufAll.append("SVN_VERION=" + version + "\n");
		}
		FileTool.writeFile(svnVerFile, bufAll.toString(),false);
	}
	
	public void visitGit() {
		if (!gitVerFile.exists()) {
			try {
				gitVerFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Job buildJob = null;
		if (projects.length != 0) {

			CUIPlugin.getDefault().startGlobalConsole();
			for (IProject project : projects) {
				ICProjectDescription projectDescription = CoreModel.getDefault().getProjectDescription(project, false);
				if (projectDescription != null) {
					ICConfigurationDescription[] cfgds = projectDescription.getConfigurations();
					ArrayList<ICConfigurationDescription> libcfgdList = new ArrayList<ICConfigurationDescription>();
					for (ICConfigurationDescription cfgd : cfgds) {
						if (cfgd.getName().startsWith("libos")) {
							libcfgdList.add(cfgd);
						}
					}
					ICConfigurationDescription[] libosCfgds = new ICConfigurationDescription[libcfgdList.size()];
					if (cfgds != null && cfgds.length > 0) {
						BuildUtilities.saveEditors(null);
						buildJob = new BuildTarget(libcfgdList.toArray(libosCfgds), 0,
								IncrementalProjectBuilder.INCREMENTAL_BUILD);
						buildJob.schedule();
					}
				}
			}
		}
		
	}
	
}
