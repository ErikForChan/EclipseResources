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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
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
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
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
import org.eclipse.ui.internal.ide.filesystem.FileSystemConfiguration;
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
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.CommonBuilder;
import org.eclipse.cdt.managedbuilder.internal.core.CommonBuilder.BuildStatus;
import org.eclipse.cdt.managedbuilder.internal.core.CommonBuilder.CfgBuildInfo;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.board.Board;
import org.eclipse.cdt.ui.wizards.board.CreatBoardXml;
import org.eclipse.cdt.ui.wizards.parsexml.Cpu;
import org.eclipse.cdt.ui.wizards.parsexml.ReviseLinkToXML;
import org.eclipse.cdt.ui.wizards.parsexml.ReviseVariableToXML;

import org.eclipse.cdt.internal.ui.newui.Messages;
import org.eclipse.cdt.internal.ui.wizards.ICDTCommonProjectWizard;

public abstract class DjyosCommonProjectWizard extends BasicNewResourceWizard
{
	private static final String PREFIX= "CProjectWizard"; //$NON-NLS-1$
	private static final String OP_ERROR= "CProjectWizard.op_error"; //$NON-NLS-1$
	private static final String title= CUIPlugin.getResourceString(OP_ERROR + ".title"); //$NON-NLS-1$
	private static final String message= CUIPlugin.getResourceString(OP_ERROR + ".message"); //$NON-NLS-1$
	private static final String[] EMPTY_ARR = new String[0];

	boolean addedMemory = false;
	boolean addedModule = false;
	boolean createdProject = false;
	protected IConfigurationElement fConfigElement;
	protected DjyosMainWizardPage fMainPage;
	
	protected MemoryMapWizard memoryPage;//Memory向导界面
	protected ModuleConfigurationWizard modulePage;//Module向导界面
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
	String eclipsePath = getEclipsePath();
	private IProject curProject;

	public DjyosCommonProjectWizard() {
		this(Messages.NewModelProjectWizard_0,Messages.NewModelProjectWizard_1);
	}
	
	public Board getBoard() {
		return fMainPage.getSelectBoard();
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

	/*
	 * 获取用户选中模板的信息
	 */
	public String getTemplateName() {
		int tIndex = fMainPage.getTemplateIndex();
		String templateName = null;
		switch(tIndex) {
		case 0:
			templateName = "ibootapp";break;
		case 1:
			templateName = "iboot";break;
		case 2:
			templateName = "App";break;
		default:
			templateName = "Apponly";break;
		}
		
		return templateName;
	}
	
	/*
	 * 获取当前Eclipse的路径
	 */
	public String getEclipsePath() {
		String fullPath = Platform.getInstallLocation().getURL().toString();
		String eclipsePath = fullPath.substring(6,(fullPath.substring(0,fullPath.length()-1)).lastIndexOf("/")+1);
		return eclipsePath;
	}
	
	/*
	 * 创建工程
	 */
	public void importTemplate(String projectPath) {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		int tIndex = fMainPage.getTemplateIndex();
		String projectName = fMainPage.getProjectName();
		String destPath = null;
		String srcPath = null;
		String templateName = getTemplateName();
		srcPath = eclipsePath + "demo/" + templateName;
		destPath = fMainPage.locationArea.locationPathField.getText();
		if(!destPath.contains(projectName)) {
			destPath = destPath+"/"+projectName;
		}
		
		File src = new File(srcPath);
		File dest = new File(destPath);
		
		File projectFile = new File(projectPath+"/"+projectName);

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
					importExistingProject(subMonitor.split(1),projectName,projectPath);		
					
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
			createdProject = true;
						
		}else {
			fMainPage.setExistedMessage();
		}		
			
	}
	
	/*
	 * 导入模板工程到当前工作空间
	 */
	private IStatus importExistingProject(IProgressMonitor mon, String projectName, String projectPath) {

		SubMonitor subMonitor = SubMonitor.convert(mon, 3);
		
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();

		IProject project = workspace.getRoot().getProject(projectName);
		final IProgressMonitor monitor = new NullProgressMonitor();

		if(! projectPath.contains(projectName)) {
			IPath locationPath = new Path(projectPath+"/"+projectName);
			IProjectDescription description = workspace.newProjectDescription(projectName);
			description.setLocation(locationPath);
			try {
				SubMonitor subTask = subMonitor.split(1).setWorkRemaining(100);
				subTask.setTaskName(DataTransferMessages.WizardProjectsImportPage_CreateProjectsTask);
				project.create(description,subTask.split(30));
				project.open(IResource.BACKGROUND_REFRESH, subTask.split(70));
				subTask.setTaskName(""); //$NON-NLS-1$
			} catch (OperationCanceledException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}else {
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
		}
		
		curProject = project;
	
		return Status.OK_STATUS;
	}
	
	/*
	 * 重命名当前工程
	 */
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
	
	/*
	 * 处理板件的新建
	 */
	public void handleBoard() {
		String projectName = fMainPage.getProjectName();
		String curBoardName = fMainPage.getBoardName();
		Cpu defaultCpu = fMainPage.defaultCpu;
		cpu = fMainPage.getSelectCpu();
		Board boardTemplate = fMainPage.getSelectBoard();
		
		String startupPath = eclipsePath+"djysrc/bsp/startup/"+defaultCpu.getCategory()+"/"+boardTemplate.getBoardName();
		String startupDestPath = eclipsePath+"djysrc/bsp/startup/"+cpu.getCategory()+"/"+curBoardName;
		
		//新建的板件应该从哪里获取新板件的代码
		String boardCodePath = eclipsePath+"djysrc/bsp/boarddrv/demo/"+boardTemplate.getBoardName();
		String newBoardPath = eclipsePath+"djysrc/bsp/boarddrv/user/"+curBoardName;
		File newBoardFile = new File(newBoardPath);
		if(!newBoardFile.exists()) {
			try {
				newBoardFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//		String boardXmlPath = eclipsePath+"djysrc/bsp/boarddrv/demo/board.xml";
		
		File srcFolder = new File(boardCodePath);
		File folder = new File(newBoardPath);
		if(!folder.exists()) {
			folder.mkdir();
		}
					
		try {
			copyFolder(srcFolder,folder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CreatBoardXml cbx = new CreatBoardXml();
		boardTemplate.setBoardName(curBoardName);
		cbx.creatBoard(boardTemplate, newBoardPath+"/board_"+curBoardName+".xml");	
		
		File stpSrcFolder = new File(startupPath);
		File stpDestFolder = new File(startupDestPath);
		if(!stpDestFolder.exists()) {
			stpDestFolder.mkdir();
		}
		try {
			copyFolder(stpSrcFolder,stpDestFolder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public IProject getProject() {
		return curProject;
	}
	
	/*
	 * 修改工程的配置信息，通过修改.cproject
	 */
	public void handleCProject() {
		String projectName = fMainPage.getProjectName();
		Board board = fMainPage.getSelectBoard();
		String projectPath = fMainPage.locationArea.locationPathField.getText();

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		String boardName = fMainPage.getBoardName();
		cpu = fMainPage.getSelectCpu();
		
		if(! projectPath.contains(projectName)) {
			project = curProject;
		}	
		
		final ICProjectDescription local_prjd =  CoreModel.getDefault().getProjectDescription(project);
		ICConfigurationDescription[] conds = local_prjd.getConfigurations();	

    	for (ICConfigurationDescription cfgDesc : conds) {
			String s = cfgDesc.getName();
			if(s.equals("Debug") || s.equals("Release")) {
				cfgDesc.setName(projectName+"_"+s);
			}
		}
    	
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
				project.getFile(".project"),projectName);
		ReviseLinkToXML rltx = new ReviseLinkToXML();
		rltx.reviseXmlLink("src/libos/bsp/boarddrv",boardName, "DJYOS_SRC_LOCATION/bsp/boarddrv",cpu.getCategory(), 
				project.getFile(".project"),"boarddrv");
		rltx.reviseXmlLink("src/libos/bsp/startup",boardName, "DJYOS_SRC_LOCATION/bsp/startup",cpu.getCategory(), 
				project.getFile(".project"),"startup");
		
		String core= board.cpu.getCore();
		String category = cpu.getCategory();
		String armv = null;
		String stm = null;
		String cpudrv = null;
		if(core.equals("cortex-m7")) {
			armv = "armv7e-m";
			stm = "stm32f7";
		}else if(core.equals("cortex-m4")) {
			armv = "armv7e-m";
			stm = "stm32f4";
		}else if(core.equals("cortex-m3")) {
			armv = "armv7-m";
			stm = "stm32f3";
		}
		
		if(category.contains("stm32f7")) {
			cpudrv = "stm32f7";
		}else if(category.contains("stm32f4")) {
			cpudrv = "stm32f4xx";
		}else if(category.contains("stm32f3")) {
			cpudrv = "stm32f7";
		}else if(category.contains("stm32l4")) {
			cpudrv = "stm32L4xx";
		}else if(category.contains("stm32f1")) {
			cpudrv = "stm32f1xx";
		}
		rltx.reviseXmlLink("src/libos/bsp/cpudrv",cpudrv, "DJYOS_SRC_LOCATION/bsp/cpudrv",category,
				project.getFile(".project"),"cpudrv");
		rltx.reviseXmlLink("src/libos/bsp/arch",core, "DJYOS_SRC_LOCATION/bsp/arch/arm/cortex-m"+armv,category, 
				project.getFile(".project"),"arch");
//		rltx.reviseXmlLink("src/libos/third",stm, "DJYOS_SRC_LOCATION/third/firmware",cpu.getCategory(), 
//				project.getFile(".project"),"third");
		
	}

	/*
	 * 拷贝工程到另外一个目录
	 */
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
	
	/*
	 * 创建meomory.lds文件
	 */
	public boolean addMemoryToLds(String content, String path) throws IOException {

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

		return true;
	}

	/*
	 * 创建moduletrim.c文件
	 */
	public void createModuleTrim(String boardModuleTrimPath, String destModuleTrimPath) {
		String fileName = boardModuleTrimPath.substring(boardModuleTrimPath.lastIndexOf("/") + 1,
				boardModuleTrimPath.length());
		String moduleTrimPath = destModuleTrimPath + "/" + fileName;
		File moduleTrim = new File(moduleTrimPath);
		try {
			copyFolder(new File(boardModuleTrimPath), new File(destModuleTrimPath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * 获取用户配置的momory信息
	 */
    public void getMemoryToLds() {
    	String ldsHead = memoryPage.getLdsHead();
    	String ldsDesc = memoryPage.getLdsDesc();
    	String templateName = getTemplateName();
    	String projectName = fMainPage.getProjectName();
		String sourcePath = fMainPage.projectPath;
		if(!sourcePath.contains(projectName)) {
			sourcePath = sourcePath+"/"+projectName;
		}
    	String path = sourcePath+"/src/lds/memory.lds";
		File file = new File(path);
		if(file.exists()) {
			file.delete();
		}
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
		if(!projectPath.contains(projectName)) {
			projectPath = projectPath+"/"+projectName;
		}
		String sourcePath = ResourcesPlugin.getWorkspace().getRoot().getLocationURI().toString().substring(6)+"/"+projectName;
		int index = fMainPage.getTemplateIndex();
    	String path = projectPath+"/src/app/OS_prjcfg/cfg/moduleinit.h";
    	String pathIboot = projectPath+"/src/iboot/OS_prjcfg/cfg/moduleinit.h";
    	getMemoryToLds();//根据MemoryMap配置的内容添加memory.lds文件
    	memoryPage.createMemoryMap(projectPath+"/data/MemoryMap.xml");//根据MemoryMap配置的内容添加memoryMap.xml文件
    	File file = new File(path);
		File fileIboot = new File(pathIboot);
		
    	if(index == 0 || index == 1){
    		if (fileIboot.exists()) {
    			fileIboot.delete();
    		}
    		try {
				fileIboot.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		modulePage.fillModuleinit(pathIboot);
    	}
    	if(index == 0 || index == 2 || index == 3){
    		if (file.exists()) {
    			file.delete();
    		}
    		try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		modulePage.fillModuleinit(path);
    	}
    	
//    	ICConfigurationDescription[] cfgDescs = getCfgs(curProject);
//		for (ICConfigurationDescription cfgDesc : cfgDescs) {
//			System.out.println("cfgDesc.getName()"+cfgDesc.getName());
//		}
//    	
    	createBuild(curProject);
		
		return true;
	}
	
    @Override
	public boolean performCancel() {
    	clearNewProject();
        return true;
    }
    
	@SuppressWarnings("restriction")
	public boolean createBuild(IProject project) {
		CommonBuilder cb = new CommonBuilder();
		boolean isClean = false;
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration[] cfgs = info.getManagedProject().getConfigurations();
		for (IConfiguration cfg : cfgs) {
			IBuilder builder = cfg.getEditableBuilder();
			String cfgName = cfg.getName();
			if (cfgName.equals("libos_demo_o0") || cfgName.equals("libos_demo_o2")) {
				CfgBuildInfo binfo = new CfgBuildInfo(builder, true);
				BuildStatus status = new BuildStatus(builder);
				status.setRebuild();
				IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
				final ISchedulingRule rule = ruleFactory.modifyRule(binfo.getProject());
				Job backgroundJob = new Job("CDT Common Builder") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						// TODO Auto-generated method stub
						try {
							ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
								@Override
								public void run(IProgressMonitor monitor) throws CoreException {
//									cb.performPrebuildGeneration(IncrementalProjectBuilder.FULL_BUILD, binfo,
//											status, monitor);
									boolean isClean = cb.build(IncrementalProjectBuilder.FULL_BUILD, binfo, monitor);
								}
							}, rule, IWorkspace.AVOID_UPDATE, monitor);
						} catch (CoreException e) {
							return e.getStatus();
						}
						IStatus returnStatus = Status.OK_STATUS;
						return returnStatus;
					}
				};
				backgroundJob.setRule(rule);
				backgroundJob.schedule();
			}
		}
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(isClean) {
			System.out.println("createBuild :file");
			File o0File = new File(project.getLocation().toString()+"libos_demo_o0");
			File o2File = new File(project.getLocation().toString()+"libos_demo_o2");
			File[] o0files = o0File.listFiles();
			File[] o2files = o2File.listFiles();
			File libFile = new File(project.getLocation().toString()+"lib");
			for(File file:o0files) {
				if(file.getName().endsWith(".a")) {
					try {
						copyFolder(file,libFile);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			for(File file:o2files) {
				if(file.getName().endsWith(".a")) {
					try {
						copyFolder(file,libFile);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		return true;
	}
    
    /*
     * 删除新建的工程
     */
    public void clearNewProject() {
    	int tIndex = fMainPage.getTemplateIndex();
		String projectName = fMainPage.getProjectNameFieldValue();
		String templateName = getTemplateName();
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		
		if(createdProject) {
			IProject project = workspace.getRoot().getProject(projectName);

			if (project.exists()) {
				try {
					project.delete(true, true, null);
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
    }

	@Override
	public void dispose() {
		fMainPage.dispose();
	}

    @Override
	public boolean canFinish() {
    	
    	if(addedModule) {
//    		System.out.println("modulePage.moduleCompleted:  "+modulePage.moduleCompleted);
//    		if(modulePage.moduleCompleted) {
    			return true;
//    		}else {
//    			return false;
//    		}
    	}else {
    		return false;
    	}
    }
    
    /*
     * 获取当前工程的所有配置项
     */
	private ICConfigurationDescription[] getCfgs(IProject prj) {
		ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(prj, false);
		if (prjd != null) { 
			ICConfigurationDescription[] cfgs = prjd.getConfigurations();
			if (cfgs != null) {
				return cfgs;
			}
		}		
		return new ICConfigurationDescription[0];
	}
   
}
