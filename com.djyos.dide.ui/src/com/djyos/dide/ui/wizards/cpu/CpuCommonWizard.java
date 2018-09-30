package com.djyos.dide.ui.wizards.cpu;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import com.djyos.dide.ui.wizards.board.onboardcpu.OnBoardCpu;
import com.djyos.dide.ui.wizards.djyosProject.ReadHardWareDesc;
import com.djyos.dide.ui.wizards.djyosProject.tools.DideHelper;
import com.djyos.dide.ui.wizards.djyosProject.tools.LinkHelper;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.ui.CUIPlugin;
import com.djyos.dide.ui.wizards.board.Board;
import com.djyos.dide.ui.wizards.cpu.Cpu;
import com.djyos.dide.ui.wizards.cpu.CpuMainWiazrdPage;

public class CpuCommonWizard extends BasicNewResourceWizard{

	protected CpuMainWiazrdPage fMianPage = new CpuMainWiazrdPage("New Cpu");
	private String wz_title;
	private String wz_desc;
	private String didePath = new DideHelper().getDIDEPath();
	private LinkHelper linkHelper = new LinkHelper();
	private String srcLocation = didePath+"djysrc";
	
	public CpuCommonWizard(String title, String desc) {
		// TODO Auto-generated constructor stub
		super();
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
		setNeedsProgressMonitor(true);
		setForcePreviousAndNextButtons(true);
		setWindowTitle(title);
		wz_title = title;
		wz_desc = desc;
	}
	
	private void getFolders(IProject project, List<IFolder> folders, File file, String cpuName) {
		// TODO Auto-generated method stub
		String relativePath = file.getPath().replace("\\", "/").replace(srcLocation, "");
		IFolder folder = project.getFolder("src/libos"+relativePath);
		folders.add(folder);
		File parentFile = file.getParentFile();
		
		if(!parentFile.getName().equals("cpudrv") && !toInclude(parentFile,cpuName)) {
			getFolders(project,folders,parentFile,cpuName);
		}
		
	}

	private boolean toInclude(File parentFile, String cpuName) {
		// TODO Auto-generated method stub
		File[] files = parentFile.listFiles();
		for(File f:files) {
			if(f.getName().equals(cpuName)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isCancelAvailable() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean needsPreviousAndNextButtons() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void addPages() {
		// TODO Auto-generated method stub
		fMianPage.setTitle(wz_title);
		fMianPage.setDescription(wz_desc);
		addPage(fMianPage);
		super.addPages();
	}

	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
//		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
//		IProject[] projects = workspace.getRoot().getProjects();
//		Cpu cpu = fMianPage.getCpuCreated();
//		
//		if(cpu != null && projects.length>0) {
//			ReadHardWareDesc rhwd = new ReadHardWareDesc();
//			
//			IRunnableWithProgress runnable = new IRunnableWithProgress() {
//
//				@Override
//				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
//					// TODO Auto-generated method stub
//					monitor.beginTask("配置Cpu...", projects.length+1);	
//					for(IProject project:projects) {
//						File hardWardInfoFile = new File(project.getLocation().toString()+"/data/hardware_info.xml");
//						List<String> hardwares;
//						if(hardWardInfoFile.exists()) {
//							hardwares = rhwd.getHardWares(hardWardInfoFile);
//							String cpuName = hardwares.get(1);
//							String boardName = hardwares.get(0);
//							String cpuPath =  cpu.getParentPath().replace("\\", "/");
//							File cpuFolder = new File(cpuPath);
//							List<IFolder> folders = new ArrayList<IFolder>();
//							getFolders(project,folders,cpuFolder,cpuName);
//
//							final ICProjectDescription local_prjd =  CoreModel.getDefault().getProjectDescription(project);
//							ICConfigurationDescription[] conds = local_prjd.getConfigurations();	//获取工程的所有Configuration	
//							for(IFolder folder:folders) {
//								for (int i = 0; i < conds.length; i++) {
//									if (conds[i].getName().contains("libos")) {
//										linkHelper.setExclude(folder, conds[i], true);
//									}
//								}
//							}
//
//							try {
//								CoreModel.getDefault().setProjectDescription(project, local_prjd);
//							} catch (Exception e1) {
//								// TODO Auto-generated catch block
//								e1.printStackTrace();
//							}
//							monitor.worked(1);
//						}
//					}
//					
//					monitor.setTaskName("工作空间刷新中...");
//					try {
//						workspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
//					} catch (CoreException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					monitor.worked(1);
//					monitor.done();
//				}
//				
//			};
//
//			try {
//				
//				ProgressMonitorDialog dialog = new ProgressMonitorDialog(
//						PlatformUI.getWorkbench().getDisplay().getActiveShell());
//				dialog.setCancelable(false);
//				dialog.run(true, true, runnable);
//			} catch (Exception e) {
//				e.printStackTrace();
//				return false;
//			}
//		}
		
		return true;
	}

	@Override
	public boolean isPageDragable() {
		// TODO Auto-generated method stub
		return false;
	}

}
