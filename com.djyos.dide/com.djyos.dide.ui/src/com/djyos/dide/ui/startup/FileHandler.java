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
import org.eclipse.cdt.managedbuilder.internal.ui.commands.BuildConfigurationsJob;
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
							if(resource.getName().endsWith(".a") && resource.getName().startsWith("libos")) {
								if(stup_complie_file.exists()) {
									String libCfgName = resource.getName().replace(".a", "");
									String commonName = libCfgName.replace("libos", "");
									String targetName = project.getName()+commonName;
									buildTarget(project,targetName);
								}
							}
							
							// if(resource.getName().equals(".project")) {
							// IProject project = resource.getProject();
							// File hardWardInfoFile = new
							// File(project.getLocation().toString()+"/data/hardware_info.xml");
							// if(hardWardInfoFile.exists()) {
							// HandleProjectImport projectListener = new HandleProjectImport();
							// projectListener.handleProjectElemExculde(project);
							// createBuild(project);
							// }
							// }
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
	
	@SuppressWarnings("restriction")
	protected void buildTarget(IProject project, String targetName) {
		// TODO Auto-generated method stub
		CommonBuilder cb = new CommonBuilder();
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration[] cfgs = info.getManagedProject().getConfigurations();
		final ICProjectDescription local_prjd =  CoreModel.getDefault().getProjectDescription(project);
		ICConfigurationDescription[] conds = local_prjd.getConfigurations();
		
		for (ICConfigurationDescription cfg : conds) {
			if(cfg.getName().equals(targetName)) {
				
				ICConfigurationDescription[] cfgds = new ICConfigurationDescription[1];
				cfgds[0] = cfg;
				
				BuildUtilities.saveEditors(null);
				Job buildJob =
						new BuildConfigurationsJob(cfgds, 0, IncrementalProjectBuilder.INCREMENTAL_BUILD);
				buildJob.schedule();
			}
		}
	}

	@SuppressWarnings("restriction")
	public boolean createBuild(IProject project) {
		CommonBuilder cb = new CommonBuilder();
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration[] cfgs = info.getManagedProject().getConfigurations();
		
		for (IConfiguration cfg : cfgs) {
			IBuilder builder = cfg.getEditableBuilder();
			String cfgName = cfg.getName();
			
			if (cfgName.startsWith("libos")) {
				File cfgFile = new File(project.getLocation().toString()+"/"+cfgName);
				boolean compiled = false;
				if(cfgFile.exists()) {
					File[] files = cfgFile.listFiles();
					for(File f:files) {
						if(f.getName().endsWith(".a")) {
							compiled = true;
							break;
						}
					}
				}
				
				if(!compiled) {
					CfgBuildInfo binfo = new CfgBuildInfo(builder, true);
					BuildStatus status = new BuildStatus(builder);
					status.setRebuild();
					IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
					final ISchedulingRule rule = ruleFactory.modifyRule(binfo.getProject());
					Job backgroundJob = new Job("Building "+cfgName) {
						@Override
						protected IStatus run(IProgressMonitor monitor) {
							// TODO Auto-generated method stub
							try {
								ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
									@Override
									public void run(IProgressMonitor monitor) throws CoreException {
										boolean isClean = cb.build(IncrementalProjectBuilder.FULL_BUILD, binfo, monitor);
									}
								}, rule, IWorkspace.AVOID_UPDATE, monitor);
							} catch (CoreException e) {
								return e.getStatus();
							}
							return Status.OK_STATUS;
						}
					};
					backgroundJob.setRule(rule);
					backgroundJob.schedule();
				}
				}
		}
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

}
