package com.djyos.dide.ui.wizards.djyosProject.tools;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;

import com.djyos.dide.ui.wizards.board.Board;
import com.djyos.dide.ui.wizards.board.ReadBoardXml;
import com.djyos.dide.ui.wizards.board.onboardcpu.OnBoardCpu;
import com.djyos.dide.ui.wizards.component.Component;
import com.djyos.dide.ui.wizards.component.ReadComponent;
import com.djyos.dide.ui.wizards.cpu.Cpu;
import com.djyos.dide.ui.wizards.cpu.ReadCpuXml;

public class ProjectHandler {
	
	private DideHelper dideHelper = new DideHelper();
	private LinkHelper linkHelper = new LinkHelper();
	ReadBoardXml rbx = new ReadBoardXml();
	List<Board> boardsList = rbx.getAllBoards();
	ReadCpuXml rcx = new ReadCpuXml();
	List<Cpu> cpusList =  rcx.getAllCpus();
	
	//通过板件自动创建工程
	public void createProjectsByBoard() {
		for (int i = 0; i < boardsList.size(); i++) {
			Board board = boardsList.get(i);
			OnBoardCpu onBCpu = board.getOnBoardCpus().get(0);
			Cpu cpu = getCpuByBoard(onBCpu);
			ReadComponent rc = new ReadComponent();
			List<Component> compontentsList = rc.getComponents(onBCpu, board);
		}
	}

	//通过板件获取Cpu
	private Cpu getCpuByBoard(OnBoardCpu onBCpu) {
		// TODO Auto-generated method stub
		for(Cpu c:cpusList) {
			if(c.getCpuName().equals(onBCpu.getCpuName())) {
				return c;
			}
		}
		return null;
	}
//	
//	@SuppressWarnings("restriction")
//	private IStatus importExistingProject(IProgressMonitor mon, String projectName, String userPath,String destPath) {
//
//		SubMonitor subMonitor = SubMonitor.convert(mon, 3);
//		
//		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
//
//		IProject project = workspace.getRoot().getProject(projectName);
//		final IProgressMonitor monitor = new NullProgressMonitor();
//
//		if(! userPath.contains(projectName)) {
//			IPath locationPath = new Path(destPath);
//			IProjectDescription description = workspace.newProjectDescription(projectName);
//			description.setLocation(locationPath);
//			try {
//				SubMonitor subTask = subMonitor.split(1).setWorkRemaining(100);
//				subTask.setTaskName(DataTransferMessages.WizardProjectsImportPage_CreateProjectsTask);
//				project.create(description,subTask.split(30));
//				project.open(IResource.BACKGROUND_REFRESH, subTask.split(70));
//				subTask.setTaskName(""); //$NON-NLS-1$
//			} catch (OperationCanceledException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (CoreException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}	
//		}else {
//			try {
//				SubMonitor subTask = subMonitor.split(1).setWorkRemaining(100);
//				subTask.setTaskName(DataTransferMessages.WizardProjectsImportPage_CreateProjectsTask);
//				project.create(subTask.split(30));
//				project.open(IResource.BACKGROUND_REFRESH, subTask.split(70));
//				subTask.setTaskName(""); //$NON-NLS-1$
//			} catch (OperationCanceledException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (CoreException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}	
//		}
//		
//		return Status.OK_STATUS;
//	}
//	
//
//	public void importProject(String projectPath) {
//		
//		String projectName = fMainPage.getProjectName();//用户填写的工程名
//		String templateName = getTemplateName();//用户选择的模板
//		String srcPath = dideHelper.getTemplatePath() + "/" + templateName;//模板的路径
//		String userPath = projectPath;
//		if(!projectPath.contains(projectName)) {
//			projectPath = projectPath+"/"+projectName;
//		}
//		String destPath = projectPath;
//		
//		File srcFile = new File(srcPath);
//		File destFile = new File(destPath);
//
//		if(!destFile.exists()) {
//			destFile.mkdir();
//			try {
//				dideHelper.copyFolder(srcFile,destFile);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//			WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
//				@Override
//				protected void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
//					SubMonitor subMonitor = SubMonitor.convert(monitor, 1);
//					// Import as many projects as we can; accumulate errors to
//					// report to the user
//					@SuppressWarnings("restriction")
//					MultiStatus status = new MultiStatus(IDEWorkbenchPlugin.IDE_WORKBENCH, 1,
//							DataTransferMessages.WizardProjectsImportPage_projectsInWorkspaceAndInvalid, null);
//					importExistingProject(subMonitor.split(1),projectName,userPath,destPath);		
//					
//					if (!status.isOK()) {
//						throw new InvocationTargetException(new CoreException(status));
//					}
//				}
//			};
//			
//			try {
//				getContainer().run(true, true, op);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				dideHelper.showErrorMessage("工程创建失败"+e.getMessage());
//				e.printStackTrace();
//			}
//				
//			projectExist = false;
//			createdProject = true;
//			
//		}else {
//			projectExist = true;
//			fMainPage.setErrorMessage("Project is aready existed");
//		}		
//			
//	}
}
