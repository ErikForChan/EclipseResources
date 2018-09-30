package com.djyos.dide.ui.wizards.board;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import com.djyos.dide.ui.wizards.board.onboardcpu.OnBoardCpu;
import com.djyos.dide.ui.wizards.board.onboardcpu.OnBoardMemory;
import com.djyos.dide.ui.wizards.djyosProject.tools.DideHelper;
import com.djyos.dide.ui.wizards.djyosProject.tools.LinkHelper;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.ui.CUIPlugin;

public class BoardCommonWizard extends BasicNewResourceWizard{

	protected  BoardMainWizard fMainPage = new BoardMainWizard("New Board");
	private String wz_title, wz_desc;
	LinkHelper linkHelper = new LinkHelper();
	DideHelper dideHelper = new DideHelper();
	private IWorkbenchWindow window = PlatformUI.getWorkbench()
			.getActiveWorkbenchWindow();
	
	public BoardCommonWizard(String title, String desc) {
		// TODO Auto-generated constructor stub
		super();
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
		setNeedsProgressMonitor(true);
		setForcePreviousAndNextButtons(true);
		setWindowTitle(title);
		wz_title = title;
		wz_desc = desc;
	}
	
	@Override
	public boolean needsPreviousAndNextButtons() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addPages() {
		// TODO Auto-generated method stub
		fMainPage.setTitle(wz_title);
		fMainPage.setDescription(wz_desc);
		addPage(fMainPage);
		super.addPages();
	}
	
	@Override
	public boolean isCancelAvailable() {
		// TODO Auto-generated method stub
		return false;
	}

	
	@Override
	public boolean isPageDragable() {
		// TODO Auto-generated method stub
		return false;
	}
	 
	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject[] projects = workspace.getRoot().getProjects();
		Board newBoard = fMainPage.getBoard();
		
		if(newBoard!=null && projects.length>0) {
			String relativePath = newBoard.getBoardPath().replace(dideHelper.getDjyosSrcPath(), "");
//			System.out.println("relativePath:  "+relativePath);
			IRunnableWithProgress runnable = new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					// TODO Auto-generated method stub
					monitor.beginTask("配置板件...", projects.length+1);	
					for(IProject project:projects) {
						IFolder folder = project.getFolder("src/libos" + relativePath);
						final ICProjectDescription local_prjd =  CoreModel.getDefault().getProjectDescription(project);
						if(local_prjd != null) {
							ICConfigurationDescription[] conds = local_prjd.getConfigurations();	//获取工程的所有Configuration	
							for (int i = 0; i < conds.length; i++) {
								if (conds[i].getName().contains("libos")) {
									linkHelper.setExclude(folder, conds[i], true);
								}
							}
							
							try {
								CoreModel.getDefault().setProjectDescription(project, local_prjd);
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							monitor.worked(1);
							try {
								CoreModel.getDefault().setProjectDescription(project, local_prjd);
							} catch (CoreException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
						
					}
					
					monitor.setTaskName("工作空间刷新中...");
					try {
						workspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					monitor.worked(1);
					monitor.done();
				}
				
			};

			try {
				
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(
						PlatformUI.getWorkbench().getDisplay().getActiveShell());
				dialog.setCancelable(false);
				dialog.run(true, true, runnable);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		
		return true;
	}

}
