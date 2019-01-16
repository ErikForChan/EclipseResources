package com.djyos.dide.ui.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.helper.LinkHelper;
import com.djyos.dide.ui.startup.HandleProjectImport;
import com.djyos.dide.ui.wizards.djyosProject.ReadHardWareDesc;

public class FileHandler implements IResourceChangeListener {
	
	final IWorkspace workspace = ResourcesPlugin.getWorkspace();
	IProject[] projects = workspace.getRoot().getProjects();
	List<String> projectsExists = getProjectsExisted();

	
	private List<String> getProjectsExisted(){
		List<String> projectsExists =  new ArrayList<String>();
		for (IProject p : projects) {
			projectsExists.add(p.getName());
		}
		return projectsExists;
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		// TODO Auto-generated method stub
		try {
			event.getDelta().accept(new IResourceDeltaVisitor() {
				@Override
				public boolean visit(IResourceDelta delta) throws CoreException {
					IResource resource = delta.getResource();
					if (resource instanceof IFile) {
						switch (delta.getKind()) {
						case IResourceDelta.ADDED:
							// handle added resource
//							System.out.println("resource:   "+resource.getName());
							IProject project = resource.getProject();
							File stup_complie_file = new File(DideHelper.getDIDEPath() + "complieAuto.txt");
							File hardWardInfoFile = new File(
									project.getLocation().toString() + "/data/hardware_info.xml");
							
							if (stup_complie_file.exists()) {
								//自动编译库
								BuildOsAuto(resource,project,stup_complie_file);
							}

							if (resource.getName().equals(".project")) {
								//导入工程时，设置默认命令，对工程进行排除编译的工作
								ExcludeWhenImport(resource,project,hardWardInfoFile);
							}
							
							
							//用户新增的文件，除了djyos、component、当前板件、当前CPU、当前arch，APP 。都排除编译
//							ExcludeNewFile(resource,project,hardWardInfoFile);
							
							break;
						case IResourceDelta.REMOVED:
							// handle removed resource

							break;
						case IResourceDelta.CHANGED:
							// handle changed resource
							break;
						}
					}
					return true;
				}
			});
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void BuildOsAuto(IResource resource, IProject project, File stup_complie_file) {
		// TODO Auto-generated method stub
		if (resource.getName().endsWith(".a") && resource.getName().startsWith("libos")) {
			String libCfgName = resource.getName().replace(".a", "");
			String commonName = libCfgName.replace("libos", "");
			String targetName = project.getName() + commonName;
			DideHelper.buildTarget(project, targetName);
		}
	}

	protected void ExcludeWhenImport(IResource resource, IProject project, File hardWardInfoFile) {
		// TODO Auto-generated method stub
		if (hardWardInfoFile.exists()) {

			ConfigurationHandler cfgHandler = new ConfigurationHandler();
			cfgHandler.setDefaultArchiverCmd(project);

			HandleProjectImport projectListener = new HandleProjectImport();
			projectListener.handleProjectElemExculde(project);
		}
	}

	protected void ExcludeNewFile(IResource resource, IProject project, File hardWardInfoFile) {
		// TODO Auto-generated method stub
		if (resource.getName().endsWith(".c") && projectsExists.contains(project.getName())) {
			String relativePath = resource.getProjectRelativePath().toString();
			final ICProjectDescription local_prjd = CoreModel.getDefault().getProjectDescription(project);
			if (local_prjd != null && hardWardInfoFile.exists()) {
				ICConfigurationDescription[] conds = local_prjd.getConfigurations();
				IFile ifile = project.getFile(relativePath);
				List<String> hardwares = ReadHardWareDesc.getHardWares(hardWardInfoFile);
				String boardName = hardwares.get(0);
				String cpuName = hardwares.get(1);
				boolean toExclude = false;
				
				if(relativePath.contains("libos") && !relativePath.contains("component") && !relativePath.contains("djyos")
						&& !relativePath.contains("arch")) {
					if(relativePath.contains("cpudrv") && relativePath.contains("src")) {
						toExclude = false;
					}else if(relativePath.contains(boardName) && relativePath.contains("drv")) {
						toExclude = false;						
					}else {
						toExclude = true;		
					}
				}
				if(toExclude) {
					System.out.println("relativePath： "+relativePath);
					for (int j = 0; j < conds.length; j++) {
						if (conds[j].getName().startsWith("libos")) {
							LinkHelper.setFileExclude(ifile, conds[j], true);
						}
					}
				}
				
				DideHelper.saveProjectDescription(project, local_prjd);
			}
		}
	}

}
