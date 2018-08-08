package org.eclipse.cdt.ui.startup;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.ui.wizards.djyosProject.DjyosMessages;

public class AddDefaultPostCmd{
	
	public void getDefaultPostCmd(){
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		String str;
		IProject[] projects = workspace.getRoot().getProjects();
		for(IProject project:projects) {			
		    final ICProjectDescription local_prjd =  CoreModel.getDefault().getProjectDescription(project);
		    ICConfigurationDescription[] conds = local_prjd.getConfigurations();	//获取工程的所有Configuration
		    for (ICConfigurationDescription cfgDesc : conds) {
				String s = cfgDesc.getName();
				ICResourceDescription rds = cfgDesc.getRootFolderDescription();				
				IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(rds.getConfiguration());
					if(!s.contains("libos")) {
						str="make "+s+".bin && elf_to_bin.exe "+s+".elf "+s+".bin && ren "+s+".bin new "+s+".bin";											    
					}else {
						str=null;
					}
					cfg.setPostbuildStep(str);					
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
