package org.eclipse.cdt.ui.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.deferred.ChangeQueue.Change;
import org.eclipse.ltk.internal.core.refactoring.resource.RenameResourceProcessor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.WorkingSetGroup;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.registry.WorkingSetDescriptor;
import org.eclipse.ui.internal.registry.WorkingSetRegistry;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage.ProjectRecord;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.parsexml.Board;
import org.eclipse.cdt.ui.wizards.parsexml.Cpu;
import org.eclipse.cdt.ui.wizards.parsexml.CreateBoardXml;
import org.eclipse.cdt.ui.wizards.parsexml.ReviseLinkToXML;
import org.eclipse.cdt.ui.wizards.parsexml.ReviseVariableToXML;

import org.eclipse.cdt.internal.ui.newui.Messages;
import org.eclipse.cdt.internal.ui.wizards.ICDTCommonProjectWizard;

public abstract class DjyosCommonProjectWizard extends BasicNewResourceWizard
implements IExecutableExtension, IWizardWithMemory, ICDTCommonProjectWizard
{
	private static final String PREFIX= "CProjectWizard"; //$NON-NLS-1$
	private static final String OP_ERROR= "CProjectWizard.op_error"; //$NON-NLS-1$
	private static final String title= CUIPlugin.getResourceString(OP_ERROR + ".title"); //$NON-NLS-1$
	private static final String message= CUIPlugin.getResourceString(OP_ERROR + ".message"); //$NON-NLS-1$
	private static final String[] EMPTY_ARR = new String[0];

	boolean addedMemory = false;
	boolean addedModule = false;
	protected IConfigurationElement fConfigElement;
	protected DjyosMainWizardPage fMainPage;
	
	protected MemoryMapWizard mmPage;
	protected BoardConfigurationWizard bcPage;
	protected ModuleConfigurationWizard mcPage;
	
	protected MemoryMapWizard memoryPage;
	protected ModuleConfigurationWizard modulePage;
	protected IProject newProject;
	private String wz_title;
	private String wz_desc;

	private boolean existingPath = false;
	private String lastProjectName = null;
	private URI lastProjectLocation = null;
	private CWizardHandler savedHandler = null;
	private WorkingSetGroup workingSetGroup;
	String boardModuleTrimPath;
	
	public Cpu cpu;
	boolean isToCreat = false;

	public DjyosCommonProjectWizard() {
		this(Messages.NewModelProjectWizard_0,Messages.NewModelProjectWizard_1);
	}
	
	public Cpu getCpu() {
		return fMainPage.getSelectCpu();
	}

	public DjyosCommonProjectWizard(String title, String desc) {
		super();
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
		setNeedsProgressMonitor(true);
		setForcePreviousAndNextButtons(true);
		setWindowTitle(title);
		wz_title = title;
		wz_desc = desc;
	}

	@Override
	public void addPages() {
		fMainPage= new DjyosMainWizardPage(CUIPlugin.getResourceString(PREFIX));
		fMainPage.setTitle(wz_title);
		fMainPage.setDescription(wz_desc);
		addPage(fMainPage);
	}

	public String getTemplateName() {
		int tIndex = fMainPage.getTemplateIndex();
		String templateName = null;
		if(tIndex==0) {
			templateName = "ibootapp";
		}else if(tIndex==1){
			templateName = "iboot";
		}else if(tIndex==2){
			templateName = "App";
		}else if(tIndex==3){
			templateName = "Apponly";
		}
		return templateName;
	}
	
	public String getEclipsePath() {
		String fullPath = Platform.getInstallLocation().getURL().toString();
		String eclipsePath = fullPath.substring(6,(fullPath.substring(0,fullPath.length()-1)).lastIndexOf("/")+1);
		System.out.println("eclipsePath:  "+eclipsePath);
		return eclipsePath;
	}
	
	public void reName() {
		String templateName = getTemplateName();
		String projectName = fMainPage.getProjectName();
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		
		final IProject project = workspace.getRoot().getProject(templateName);
		if(project.exists()) {
			RenameResourceProcessor processor = new RenameResourceProcessor(project);
			processor.setNewResourceName(projectName);
			try {
				org.eclipse.ltk.core.refactoring.Change change = processor.createChange(new NullProgressMonitor());
				change.perform(new NullProgressMonitor());
			} catch (CoreException e1) {
				e1.printStackTrace();
			}
		}	
		
		final IProject jProject = workspace.getRoot().getProject(projectName);
		if(!jProject.exists()) {
			reName();
		}
		
	}
	
	public void handleBoard() {
		String eclipsePath = getEclipsePath();
		String projectName = fMainPage.getProjectName();
		String boardName = fMainPage.getBoardName();
		cpu = fMainPage.getSelectCpu();
		Board board = fMainPage.getSelectBoard();
		
		String startupPath = eclipsePath+"demo/Startup/cpudrv";
		String startupDestPath = eclipsePath+"djysrc/bsp/startup";
		
		String boardCodePath = eclipsePath+"djysrc/bsp/boarddrv/"+board.getBoardName();
		String newBoardPath = eclipsePath+"djysrc/bsp/boarddrv/"+boardName;
		String boardXmlPath = eclipsePath+"djysrc/bsp/boarddrv/board.xml";
		
		File srcFolder = new File(boardCodePath);
		File folder = new File(newBoardPath);
//		File file = new File(newBoardXmlPath);
		if(!folder.exists()) {
			folder.mkdir();
		}
		CreateBoardXml cbx = new CreateBoardXml();
					
		try {
			copyFolder(srcFolder,folder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		board.setBoardName(boardName);
		cbx.creatBoard(board, boardXmlPath);	
		
		File stpSrcFolder = new File(startupPath);
		File stpDestFolder = new File(startupDestPath+"/"+cpu.getDevice());
		if(!stpDestFolder.exists()) {
			stpDestFolder.mkdir();
		}
		try {
			copyFileToFolder(stpSrcFolder,stpDestFolder,boardName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void handleCProject() {
		String eclipsePath = getEclipsePath();
		String projectName = fMainPage.getProjectName();
		Board board = fMainPage.getSelectBoard();
		final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		String boardName = fMainPage.getBoardName();
		cpu = fMainPage.getSelectCpu();
		System.out.println("project.getFullPath().toString():           "+project.getFullPath().toString());
		final ICProjectDescription local_prjd =  CoreModel.getDefault().getProjectDescription(project);
		ICConfigurationDescription[] conds = local_prjd.getConfigurations();	
		for(int i=0;i<conds.length;i++) {
			ICResourceDescription rds = conds[i].getRootFolderDescription();
			IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(rds.getConfiguration());
			IResourceInfo resourceInfo = cfg.getRootFolderInfo();
			IToolChain toolchain = resourceInfo.getParent().getToolChain();          				
			IOption option1 = toolchain.getOptionBySuperClassId("ilg.gnuarmeclipse.managedbuild.cross.option.arm.target.architecture");
			IOption option2 = toolchain.getOptionBySuperClassId("ilg.gnuarmeclipse.managedbuild.cross.option.arm.target.family");        				
			IOption option3 = toolchain.getOptionBySuperClassId("ilg.gnuarmeclipse.managedbuild.cross.option.arm.target.fpu.unit");
			try {
				option1.setValue(
						"ilg.gnuarmeclipse.managedbuild.cross.option.arm.target.arch."+board.cpu.getArchitecture());
				option2.setValue(
						"ilg.gnuarmeclipse.managedbuild.cross.option.arm.target.mcpu."+board.cpu.getCore());
				option3.setValue(
						"ilg.gnuarmeclipse.managedbuild.cross.option.arm.target.fpu.unit."+board.cpu.getFpuType());
			} catch (BuildException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		try {
			CoreModel.getDefault().setProjectDescription(project, local_prjd);
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		ReviseVariableToXML rvtx = new ReviseVariableToXML();
		rvtx.reviseXmlVariable("DJYOS_SRC_LOCATION","file:/"+eclipsePath+"djysrc", 
				IDEWorkbenchPlugin.getPluginWorkspace().getRoot().getProject(projectName).getFile(".project"),projectName);
		ReviseLinkToXML rltx = new ReviseLinkToXML();
		rltx.reviseXmlLink("src/libos/bsp/boarddrv",boardName, "DJYOS_SRC_LOCATION/bsp/boarddrv",cpu.getDevice(), 
				IDEWorkbenchPlugin.getPluginWorkspace().getRoot().getProject(projectName).getFile(".project"),"boarddrv");
		rltx.reviseXmlLink("src/libos/bsp/startup",boardName, "DJYOS_SRC_LOCATION/bsp/startup",cpu.getDevice(), 
				IDEWorkbenchPlugin.getPluginWorkspace().getRoot().getProject(projectName).getFile(".project"),"startup");
		
		String core= board.cpu.getCore();
		if(core.equals("cortex-m7")) {
			rltx.reviseXmlLink("src/libos/bsp/arch","cortex-m7", "DJYOS_SRC_LOCATION/bsp/arch/arm/cortex-m/armv7e-m","cortex-m7", 
					IDEWorkbenchPlugin.getPluginWorkspace().getRoot().getProject(projectName).getFile(".project"),"arch");
			rltx.reviseXmlLink("src/libos/bsp/cpudrv","stm32f7", "DJYOS_SRC_LOCATION/bsp/cpudrv/cortex-m7","stm32f7", 
					IDEWorkbenchPlugin.getPluginWorkspace().getRoot().getProject(projectName).getFile(".project"),"cpudrv");
		}else if(core.equals("cortex-m4")) {
			rltx.reviseXmlLink("src/libos/bsp/arch","cortex-m4", "DJYOS_SRC_LOCATION/bsp/arch/arm/cortex-m/armv7e-m","cortex-m4", 
					IDEWorkbenchPlugin.getPluginWorkspace().getRoot().getProject(projectName).getFile(".project"),"arch");
			rltx.reviseXmlLink("src/libos/bsp/cpudrv","stm32f4xx", "DJYOS_SRC_LOCATION/bsp/cpudrv/cortex-m7","stm32f4xx",
					IDEWorkbenchPlugin.getPluginWorkspace().getRoot().getProject(projectName).getFile(".project"),"cpudrv");
		}
		
		
		
//		if (arch.equals("armv7e-m")) {
//
//		} else if (arch.equals("cortex-m4")) {
//
//		}
		
	}
	
	private void copyFileToFolder(File src, File dest, String boardName) throws IOException {  
	    if (src.isDirectory()) {  
	        if (!dest.exists()) {  
	            dest.mkdir(); 
	            dest.renameTo(new File(dest.getAbsolutePath().substring(0, dest.getAbsolutePath().lastIndexOf("\\"))+"\\"+boardName));
	            dest = new File(dest.getAbsolutePath().substring(0, dest.getAbsolutePath().lastIndexOf("\\"))+"\\"+boardName);
	        }  
	        String files[] = src.list();  
	        for (String file : files) {  
	            File srcFile = new File(src, file);  
	            File destFile = new File(dest, file); 
	            System.out.println(destFile.getName());
	            // 递归复制  
	            copyFileToFolder(srcFile, destFile,boardName);  
	        }  
	    } else {  
	    	if(!dest.exists()) {
	    		InputStream in = new FileInputStream(src);  
	 	        OutputStream out = new FileOutputStream(dest);  
	 	  
	 	        byte[] buffer = new byte[1024];  
	 	  
	 	        int length;  
	 	          
	 	        while ((length = in.read(buffer)) > 0) {  
	 	            out.write(buffer, 0, length);  
	 	        }  
	 	        in.close();  
	 	        out.close();  
	    	}	       
	    }  
	}  
		
	public void importTemplate() {

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		int tIndex = fMainPage.getTemplateIndex();
		String projectName = fMainPage.getProjectName();
		String fullPath = Platform.getInstallLocation().getURL().toString();
		String eclipsePath = fullPath.substring(6,(fullPath.substring(0,fullPath.length()-1)).lastIndexOf("/")+1);
		String destPath = null;
		String srcPath = null;
		String templateName = getTemplateName();
		srcPath = eclipsePath + "demo/" + templateName;
		destPath = fMainPage.locationArea.locationPathField.getText();
		
		String projectPath = workspace.getRoot().getLocationURI().toString().substring(6)+"/"+projectName;
		
		File src = new File(srcPath);
		File dest = new File(destPath);
		
		File projectFile = new File(projectPath);

		if(!dest.exists() && !projectFile.exists()) {
			dest.mkdir();
			try {
				copyFolder(src,dest);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			final String importName = templateName;
			WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
				@Override
				protected void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					SubMonitor subMonitor = SubMonitor.convert(monitor, 1);
					// Import as many projects as we can; accumulate errors to
					// report to the user
					MultiStatus status = new MultiStatus(IDEWorkbenchPlugin.IDE_WORKBENCH, 1,
							DataTransferMessages.WizardProjectsImportPage_projectsInWorkspaceAndInvalid, null);
					importExistingProject(subMonitor.split(1),projectName,importName);		
					
					if (!status.isOK()) {
						throw new InvocationTargetException(new CoreException(status));
					}
				}
			};
			
			try {
				getContainer().run(true, true, op);
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
			isToCreat = fMainPage.isToCreat();
						
		}else {
			fMainPage.setExistedMessage();
		}		
			
	}
	
	private IStatus importExistingProject(IProgressMonitor mon,String projectName,String templateName) {
		
		SubMonitor subMonitor = SubMonitor.convert(mon, 3);
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProject project = workspace.getRoot().getProject(projectName);
		
		try {
			SubMonitor subTask = subMonitor.split(1).setWorkRemaining(100);
			subTask.setTaskName(DataTransferMessages.WizardProjectsImportPage_CreateProjectsTask);
			project.create(subTask.split(30));
			project.open(IResource.BACKGROUND_REFRESH, subTask.split(70));			
			subTask.setTaskName(""); //$NON-NLS-1$
		
		} catch (OperationCanceledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return Status.OK_STATUS;
	}
	
	private void copyFolder(File src, File dest) throws IOException {  
	    if (src.isDirectory()) {  
	        if (!dest.exists()) {  
	            dest.mkdir();  
	        }  
	        String files[] = src.list();  
	        for (String file : files) {  
	            File srcFile = new File(src, file);  
	            File destFile = new File(dest, file);  
	            // 递归复制  
	            copyFolder(srcFile, destFile);  
	        }  
	    } else {  
	        InputStream in = new FileInputStream(src);  
	        OutputStream out = new FileOutputStream(dest);  
	  
	        byte[] buffer = new byte[1024];  
	  
	        int length;  
	          
	        while ((length = in.read(buffer)) > 0) {  
	            out.write(buffer, 0, length);  
	        }  
	        in.close();  
	        out.close();  
	    }  
	}  

	private void clearProject() {
		if (lastProjectName == null) return;
		try {
			ResourcesPlugin.getWorkspace().getRoot().getProject(lastProjectName).delete(!existingPath, true, null);
		} catch (CoreException ignore) {}
		newProject = null;
		lastProjectName = null;
		lastProjectLocation = null;
	}

	@Override
	public boolean performFinish() {
		String projectName = fMainPage.getProjectName();
		String projectPath = fMainPage.locationArea.locationPathField.getText();
		String sourcePath = ResourcesPlugin.getWorkspace().getRoot().getLocationURI().toString().substring(6)+"/"+projectName;
    	String path = projectPath+"/src/app/OS_prjcfg/include/moduleinit.h";
    	String testpath = projectPath+"/src/app/OS_prjcfg/include";
    	File testFile = new File(testpath);
    	getMemoryToLds();
    	if(testFile.exists()) {
    		File file = new File(path);
    		if(!file.exists()) {
    			try {
    				file.createNewFile();
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}
    		
    		modulePage.fillModuleinit(path);
    	}
		
		return true;
	}
	
	protected boolean setCreated() throws CoreException {
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();

		ICProjectDescription des = mngr.getProjectDescription(newProject, false);

		if(des == null ) {
			return false;
		}

		if(des.isCdtProjectCreating()){
			des = mngr.getProjectDescription(newProject, true);
			des.setCdtProjectCreated();
			mngr.setProjectDescription(newProject, des, false, null);
			return true;
		}
		return false;
	}

    @Override
	public boolean performCancel() {
    	clearNewProject();
        return true;
    }
    
    public void clearNewProject() {
    	
    	int tIndex = fMainPage.getTemplateIndex();
		String projectName = fMainPage.getProjectNameFieldValue();
		String templateName = getTemplateName();
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		
		if(addedModule) {
			IProject project = workspace.getRoot().getProject(projectName);

			if (project.exists()) {
				System.out.println("projectName:  " + project.getName());
				try {
					project.delete(true, true, null);
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
//		if(projectName == null) {
//			if(templateName != null) {
//				try {
//					final IProject project = workspace.getRoot().getProject(templateName);
//					if(project.exists()) {
//						project.delete(true, true, null);
//					}	
//				} catch (CoreException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}		
//		}else {
//			System.out.println("projectName:  "+projectName);
//			IProject project = workspace.getRoot().getProject(projectName);
////			System.out.println("project.toString():  "+project.getLocation().toString());
//			if(project.exists()) {
//				System.out.println("projectName:  "+project.getName());
//				try {
//					project.delete(true, true, null);
//				} catch (CoreException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}else {
//				System.out.println("project==null");
//				if(templateName != null) {
//					try {
//						project = workspace.getRoot().getProject(templateName);
//						if(project.exists()) {
//							project.delete(true, true, null);
//						}				
//					} catch (CoreException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}		
//			}
//			
//		}
		
    }

    public void getMemoryToLds() {
    	Board board = fMainPage.getSelectBoard();
    	String ldsHead = memoryPage.getLdsHead();
    	String ldsDesc = memoryPage.getLdsDesc(board);
    	String templateName = getTemplateName();
    	String projectName = fMainPage.getProjectName();
		String sourcePath = ResourcesPlugin.getWorkspace().getRoot().getLocationURI().toString().substring(6)+"/"+projectName;
    	String path = sourcePath+"/src/lds/memory.lds";
		File file = new File(path);
		String content = ldsHead + ldsDesc;
		
		if(file.exists()) {
			file.delete();
		}
		
		try {
			file.createNewFile();
			addMemoryToLds(content,path);	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    }
    
	@Override
	public abstract String[] getNatures();

	@Override
	public void dispose() {
		fMainPage.dispose();
	}

    @Override
	public boolean canFinish() {
    	if(addedModule) {
    		if(modulePage.moduleCompleted) {
    			return true;
    		}else {
    			return false;
    		}
    		
    	}else {
    		return false;
    	}
//    	return super.canFinish();
    }
    
    public  boolean addMemoryToLds(String content, String path) throws IOException { 
    	
    	String projectName = fMainPage.getProjectName();
    	final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProject project = workspace.getRoot().getProject(projectName);
    	FileWriter writer;
		try {
			writer = new FileWriter(path);
			writer.write(content);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
//		File tmp = File.createTempFile("tmp", null);
//		FileOutputStream tmpOut = new FileOutputStream(tmp);
//		FileInputStream tmpIn = new FileInputStream(tmp);
//		RandomAccessFile raf = null;
//		try {
//			raf = new RandomAccessFile(file, "rw");
//			byte[] buf = new byte[64];
//			int hasRead = 0;
//			while ((hasRead = raf.read(buf)) > 0) {
//				// 把原有内容读入临时文件
//				tmpOut.write(buf, 0, hasRead);
//			}
//			raf.seek(0L);
//			raf.write(content.getBytes());
//			// 追加临时文件内容
//			while ((hasRead = tmpIn.read(buf)) > 0) {
//				raf.write(buf, 0, hasRead);
//			}
//		} catch (IOException e) {
//			System.out.println("写入失败！");
//			e.printStackTrace();
//			return false;
//		} finally {
//			try {
//				raf.close();
//			} catch (IOException e) {
//				System.out.println("写入失败，关闭流失败！");
//				e.printStackTrace();
//				return false;
//			}
//		}
		return true;
	}
    
	public void createModuleTrim(String boardModuleTrimPath,String destModuleTrimPath) {
		String fileName = boardModuleTrimPath.substring(boardModuleTrimPath.lastIndexOf("/")+1, boardModuleTrimPath.length());
		String moduleTrimPath = destModuleTrimPath+"/"+fileName;
		File moduleTrim = new File(moduleTrimPath);
		try {
			copyFolder(new File(boardModuleTrimPath),new File(destModuleTrimPath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		if(moduleTrim.exists()) {
//			 String newName = boardModuleTrimPath.substring(0, boardModuleTrimPath.lastIndexOf("."))+".c";  
//             File newFile = new File(newName);  
//             boolean flag = moduleTrim.renameTo(newFile);  
//		}
	}
    
	@Override
	public String getLastProjectName() {
		return lastProjectName;
	}

	@Override
	public URI getLastProjectLocation() {
		return lastProjectLocation;
	}

	@Override
	public IProject getLastProject() {
		return newProject;
	}

	protected IProgressMonitor continueCreationMonitor;
	protected abstract IProject continueCreation(IProject prj);

	@Override
	public IProject createIProject(String name, URI location) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IProject createIProject(String name, URI location, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getExtensions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getLanguageIDs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IProject getProject(boolean defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IProject getProject(boolean defaults, boolean onFinish) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
