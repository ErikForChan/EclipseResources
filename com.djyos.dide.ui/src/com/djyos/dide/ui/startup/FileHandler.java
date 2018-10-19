package com.djyos.dide.ui.startup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.CommonBuilder;
import org.eclipse.cdt.managedbuilder.internal.core.CommonBuilder.BuildStatus;
import org.eclipse.cdt.managedbuilder.internal.core.CommonBuilder.CfgBuildInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.internal.ide.actions.BuildUtilities;

import com.djyos.dide.ui.wizards.component.ComponentRefer;
import com.djyos.dide.ui.wizards.djyosProject.tools.BuildTarget;
import com.djyos.dide.ui.wizards.djyosProject.tools.DideHelper;
import com.djyos.dide.ui.wizards.djyosProject.tools.LinkHelper;

@SuppressWarnings("restriction")
public class FileHandler implements IResourceChangeListener{

	List<String> allStrings = new ArrayList<String>();
	private String didePath = new DideHelper().getDIDEPath();
	LinkHelper linkHelper = new LinkHelper();
	ComponentRefer cRefer = new ComponentRefer();
	DideHelper dideHelper = new DideHelper();
	
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		// TODO Auto-generated method stub
		try {
			event.getDelta().accept(new IResourceDeltaVisitor() {	
				@Override
				public boolean visit(IResourceDelta delta) throws CoreException {
					IResource resource = delta.getResource();
					if (resource instanceof IFile)
					{				
						switch (delta.getKind()) {
						case IResourceDelta.ADDED:
							// handle added resource
							IProject project = resource.getProject();
							File stup_complie_file = new File(dideHelper.getDIDEPath()+"auto_complier.txt");
							
							if (stup_complie_file.exists()) {
								if (resource.getName().endsWith(".a") && resource.getName().startsWith("libos")) {
									String libCfgName = resource.getName().replace(".a", "");
									String commonName = libCfgName.replace("libos", "");
									String targetName = project.getName() + commonName;
									dideHelper.buildTarget(project, targetName);
								}
							}
							
							if (resource.getName().equals(".project")) {
								File hardWardInfoFile = new File(
										project.getLocation().toString() + "/data/hardware_info.xml");
								if (hardWardInfoFile.exists()) {
									
									ConfigurationHandler cfgHandler=new ConfigurationHandler();
									cfgHandler.setDefaultArchiverCmd(project);
									
									HandleProjectImport projectListener = new HandleProjectImport();
									projectListener.handleProjectElemExculde(project);
//									dideHelper.createBuild(project);
								}
							}
							break;
						case IResourceDelta.REMOVED:
							// handle removed resource
							
							break;
						case IResourceDelta.CHANGED:
							// handle changed resource
//							System.out.println("×ÊÔ´Ãû:"+resource.getName()+"   "+resource.getFullPath());	
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

}
