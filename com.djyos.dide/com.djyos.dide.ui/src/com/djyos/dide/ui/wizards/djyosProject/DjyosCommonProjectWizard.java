package com.djyos.dide.ui.wizards.djyosProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.registry.WorkingSetRegistry;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import com.djyos.dide.ui.wizards.board.onboardcpu.OnBoardCpu;
import com.djyos.dide.ui.wizards.component.Component;
import com.djyos.dide.ui.wizards.component.GetNonCompFiles;
import com.djyos.dide.ui.wizards.component.ReadComponent;
import com.djyos.dide.ui.wizards.cpu.Cpu;
import com.djyos.dide.ui.wizards.cpu.GetNonCpuFiles;
import com.djyos.dide.ui.wizards.cpu.ReadCpuXml;
import com.djyos.dide.ui.wizards.cpu.core.Core;
import com.djyos.dide.ui.wizards.djyosProject.info.CreateBoardInfo;
import com.djyos.dide.ui.wizards.djyosProject.info.CreateComponentInfo;
import com.djyos.dide.ui.wizards.djyosProject.info.CreateCpuInfo;
import com.djyos.dide.ui.wizards.djyosProject.tools.DideHelper;
import com.djyos.dide.ui.wizards.djyosProject.tools.LinkHelper;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionPreferences;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionWorkspacePreferences;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
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

import com.djyos.dide.ui.arch.Arch;
import com.djyos.dide.ui.arch.ReadArchXml;
import com.djyos.dide.ui.wizards.board.Board;
import com.djyos.dide.ui.wizards.board.ReadBoardXml;
import com.djyos.dide.ui.wizards.djyosProject.ComponentConfigWizard;
import com.djyos.dide.ui.wizards.djyosProject.CreateHardWareDesc;
import com.djyos.dide.ui.wizards.djyosProject.DjyosMainWizardPage;
import com.djyos.dide.ui.wizards.djyosProject.DjyosMessages;
import com.djyos.dide.ui.wizards.djyosProject.LinkProjectFile;
import com.djyos.dide.ui.wizards.djyosProject.tools.ReviseVariableToXML;

public abstract class DjyosCommonProjectWizard extends BasicNewResourceWizard
{
	private static final String PREFIX= "CProjectWizard"; //$NON-NLS-1$

	boolean addedMemory = false,createdProject = false,clickedMianNext = false,
			existingPath = false,projectExist = false,addedComptCfg = false;
	protected DjyosMainWizardPage fMainPage;//主界面
	protected ComponentConfigWizard cpomtCfgPage = null;//组件配置界面
	private String wz_title, wz_desc, lastProjectName;
	private String didePath = new DideHelper().getDIDEPath();
	private IProject curProject;
	protected IProject newProject;
	private DideHelper dideHelper = new DideHelper();
	private LinkHelper linkHelper = new LinkHelper();
	
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
		fMainPage = new DjyosMainWizardPage(CUIPlugin.getResourceString(PREFIX));
		fMainPage.setTitle(wz_title);
		fMainPage.setDescription(wz_desc);
		addPage(fMainPage);
	}
	
	public boolean haveIboot() {
		int index = fMainPage.getTemplateIndex();
		if (index == 0 || index == 1) {
			return true;
		}
		return false;
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
		case 3:
			templateName = "Apponly";break;
		}
		
		return templateName;
	}
	
	public void importProject(String projectPath, Board selectedBoard, boolean haveApp, boolean haveIboot) {
		
		String projectName = fMainPage.getProjectName();//用户填写的工程名
		String templateName = getTemplateName();//用户选择的模板
		String srcPath = dideHelper.getTemplatePath() + "/" + templateName;//模板的路径
		String userPath = projectPath;
		if(!projectPath.contains(projectName)) {
			projectPath = projectPath+"/"+projectName;
		}
		String destPath = projectPath;
		
		File srcFile = new File(srcPath);
		File destFile = new File(destPath);

		if(!destFile.exists()) {
			destFile.mkdir();
			try {
				dideHelper.copyFolder(srcFile,destFile);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
				@Override
				protected void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					SubMonitor subMonitor = SubMonitor.convert(monitor, 1);
					// Import as many projects as we can; accumulate errors to
					// report to the user
					@SuppressWarnings("restriction")
					MultiStatus status = new MultiStatus(IDEWorkbenchPlugin.IDE_WORKBENCH, 1,
							DataTransferMessages.WizardProjectsImportPage_projectsInWorkspaceAndInvalid, null);
					importExistingProject(subMonitor.split(1),projectName,userPath,destPath);		
					
					if (!status.isOK()) {
						throw new InvocationTargetException(new CoreException(status));
					}
				}
			};
			
			try {
				getContainer().run(true, true, op);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				dideHelper.showErrorMessage("工程创建失败"+e.getMessage());
				e.printStackTrace();
			}
				
			projectExist = false;
			createdProject = true;
			
			File destLdsFile = new File(destPath+"/src/lds");
			if(!destLdsFile.exists()) {
				destLdsFile.mkdirs();
			}
			try {
				if(haveApp && haveIboot) {
					dideHelper.copyFolder(new File(selectedBoard.getBoardPath()+"/lds"), new File(destPath+"/src/lds"));
				}else if(haveApp) {
					dideHelper.copyFolder(new File(selectedBoard.getBoardPath()+"/lds/debug.lds"), new File(destPath+"/src/lds/debug.lds"));
					dideHelper.copyFolder(new File(selectedBoard.getBoardPath()+"/lds/memory.lds"), new File(destPath+"/src/lds/memory.lds"));
					dideHelper.copyFolder(new File(selectedBoard.getBoardPath()+"/lds/release.lds"), new File(destPath+"/src/lds/release.lds"));
				}else if(haveIboot) {
					dideHelper.copyFolder(new File(selectedBoard.getBoardPath()+"/lds/iboot.lds"), new File(destPath+"/src/lds/iboot.lds"));
					dideHelper.copyFolder(new File(selectedBoard.getBoardPath()+"/lds/memory.lds"), new File(destPath+"/src/lds/memory.lds"));
				}
				
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			
		}else {
			projectExist = true;
			fMainPage.setErrorMessage("Project is aready existed");
		}		
			
	}
	
	private void setFoldersToExclude(File parentFolder, ICConfigurationDescription[] conds, IProject project) {
		// TODO Auto-generated method stub
		File[] files = parentFolder.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				String fileName = file.getName();
				String relativePath = file.getPath().substring(dideHelper.getDjyosSrcPath().length())
						.replace("\\", "/");
//				System.out.println("relativePath：  " + relativePath);
				if ((!fileName.equals("libc") && !fileName.equals("include") && !fileName.equals("startup")
						&& !fileName.contains("cortex-") && !relativePath.contains("/third/"))
						|| (relativePath.equals("/third/firmware"))) {
					setFoldersToExclude(file, conds, project);
				}

				if (!fileName.equals("include") && !fileName.equals("startup") && !fileName.equals("src")) {
					IFolder folder = project.getFolder("src/libos" + relativePath);
					for (int i = 0; i < conds.length; i++) {
						if (conds[i].getName().contains("libos_App")
								|| conds[i].getName().contains("libos_Iboot")) {
							linkHelper.setExclude(folder, conds[i], true);
						}
					}
				}

			}
		}
	}

	/*
	 * 导入模板工程到当前工作空间
	 */
	@SuppressWarnings("restriction")
	private IStatus importExistingProject(IProgressMonitor mon, String projectName, String userPath,String destPath) {

		SubMonitor subMonitor = SubMonitor.convert(mon, 3);
		
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();

		IProject project = workspace.getRoot().getProject(projectName);
		final IProgressMonitor monitor = new NullProgressMonitor();

		if(! userPath.contains(projectName)) {
			IPath locationPath = new Path(destPath);
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

	public IProject getProject() {
		return curProject;
	}

	public void handleCProject(List<Component> appCompontentsChecked,List<Component> ibootCompontentsChecked,Board board,Cpu cpu,Core core,String projectPath,String projectName,int selectIndex) {
		String _boardName = board.getBoardName();
		String _cpuName = cpu.getCpuName();
		String firmwareLib = cpu.getFirmwareLib();
		String cpudrvPath = didePath+DjyosMessages.Cpu_RelativePath;
		String boardPath = didePath+DjyosMessages.Board_RelativePath;
		String srcLocation = didePath+"djysrc";
		File cpudrvFile = new File(cpudrvPath);
		File[] cpudrvFiles = cpudrvFile.listFiles();
		File boardDemoFile = new File(dideHelper.getDemoBoardFilePath());
		File archSourceFile = new File(dideHelper.getDjyosSrcPath() + "/bsp/arch");
		List<File> archXmlFiles = dideHelper.getArchXmlFiles(archSourceFile, new ArrayList<File>());
		
		boolean isDemoBoard = false;
		if(boardDemoFile.exists()) {
			File[] boardDemoFiles = boardDemoFile.listFiles();
			for(int j=0;j<boardDemoFiles.length;j++) {
				if(boardDemoFiles[j].getName().equals(_boardName)) {
					isDemoBoard = true;
					break;
				}
			}
		}
		
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if(! projectPath.contains(projectName)) {
			project = curProject;
		}	
		
		File hardwareFile = new File(project.getLocation().toString()+"/data/hardwares");
		if(!hardwareFile.exists()) {
			hardwareFile.mkdirs();
		}
		
		List<String> includes = new ArrayList<String>();		

		final ICProjectDescription local_prjd =  CoreModel.getDefault().getProjectDescription(project);
		ICConfigurationDescription[] conds = local_prjd.getConfigurations();	//获取工程的所有Configuration		
//		local_prjd.setConfigurationRelations(ICProjectDescriptionPreferences.CONFIGS_LINK_SETTINGS_AND_ACTIVE);
		//修改编译选项的名称
    	for (ICConfigurationDescription cfgDesc : conds) {
			String s = cfgDesc.getName();
			if(s.contains(DjyosMessages.Configuration_Debug) || s.contains(DjyosMessages.Configuration_Release)) {
				if(!s.contains("libos")) {
					cfgDesc.setName(projectName+"_"+s);
				}	
			}
		}
    	
    	//给每个Configuration修改配置,增加链接
		for(int i=0;i<conds.length;i++) {
			String conName = conds[i].getName();
			ICResourceDescription rds = conds[i].getRootFolderDescription();
			IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(rds.getConfiguration());

			cfg.setBuildCommand("${cross_make}");
			cfg.setBuildArguments("SHELL=cmd.exe"+" "+cfg.getBuildArguments().replaceAll("SHELL=cmd.exe", "").trim());
			
			if(!conName.contains("libos")) {
				cfg.setPostbuildStep("make "+conName+".bin && elf_to_bin.exe "+conName+".elf "+conName+".bin && ren "+conName+".bin new"+conName+".bin");	
			}
			
			IResourceInfo resourceInfo = cfg.getRootFolderInfo();
			IToolChain toolchain = resourceInfo.getParent().getToolChain();
			ICSourceEntry[] sourceEntrys = conds[i].getSourceEntries();
			List<String> links = new ArrayList<String>();
			List<String> assemblyLinks = new ArrayList<String>();
			//根据架构类型选择链接
			if(core.getArch().getSerie().equals("arm")) {
				links.add("${DJYOS_SRC_LOCATION}/third/firmware/CMSIS/include");
				links.add("${DJYOS_SRC_LOCATION}/bsp/arch/arm");
				assemblyLinks.add("${DJYOS_SRC_LOCATION}/bsp/arch/arm");
			}
			//根据cpu链接
			List<String> cpuLinkStrings = new ArrayList<String>();
			File cpuFolder = new File(cpu.getParentPath());
			getCpuIncludes(cpuFolder,cpuLinkStrings);
			String firmwarePath = didePath+"djysrc/third/firmware";
			File[] firmwareFiles = new File(firmwarePath).listFiles();
			for(File file:firmwareFiles) {
				if(_cpuName.contains(file.getName())) {
					cpuLinkStrings.add("${DJYOS_SRC_LOCATION}/third/firmware/"+file.getName());
					cpuLinkStrings.add("${DJYOS_SRC_LOCATION}/third/firmware/"+file.getName()+"/Inc");
					break;
				}
			}
			if(cpuLinkStrings!=null) {
				for(int k=0;k<cpuLinkStrings.size();k++) {
					links.add(cpuLinkStrings.get(k).replace("\\", "/"));
				}
			}
			//根据板件名链接
			if(isDemoBoard){
				links.add(dideHelper.getRelativeDemoBoardFilePath()+_boardName);
				links.add(dideHelper.getRelativeDemoBoardFilePath()+_boardName+"/include");
				links.add(dideHelper.getRelativeDemoBoardFilePath()+_boardName+"/startup");
			}else {
				links.add(dideHelper.getRelativeUserBoardFilePath()+_boardName);
				links.add(dideHelper.getRelativeUserBoardFilePath()+_boardName+"/include");
				links.add(dideHelper.getRelativeUserBoardFilePath()+_boardName+"/startup");
			}
			//根据内核类型、架构、家族链接
			List<String> archLinks = new ArrayList<String>();
			setArchLinks(core.getArch(),archLinks,archXmlFiles);
			for(String link:archLinks) {
				links.add(link);
				assemblyLinks.add(link);
			}
			
			//根据所选组件链接
	    	if(conName.contains("App")) {
	    		//添加project_config.h的链接
		    	links.add("${ProjDirPath}/src/app/OS_prjcfg");
	    		linkComponentGUN(appCompontentsChecked,links,includes,isDemoBoard,cpudrvFiles,_cpuName,srcLocation,_boardName,assemblyLinks,rds);
	    	}else if(conName.contains("Iboot")){
	    		links.add("${ProjDirPath}/src/iboot/OS_prjcfg");
	    		linkComponentGUN(ibootCompontentsChecked,links,includes,isDemoBoard,cpudrvFiles,_cpuName,srcLocation,_boardName,assemblyLinks,rds);
	    	}
//	    	System.out.println("getArchitecture:  "+core.getArch().getArchitecture());
//	    	System.out.println("getFamily:  "+core.getArch().getFamily());
//	    	System.out.println("getSerie:  "+core.getArch().getSerie());
//	    	System.out.println("getFpuType:  "+core.getArch().getFpuType());
//	    	System.out.println(""+core.getArch().getArchitecture());
			IOption option1 = toolchain.getOptionBySuperClassId(DjyosMessages.Arch_SuperClassId);
			IOption option2 = toolchain.getOptionBySuperClassId(DjyosMessages.Family_SuperClassId);        				
			IOption option3 = toolchain.getOptionBySuperClassId(DjyosMessages.FpuABI_SuperClassId);
			IOption option4 = toolchain.getOptionBySuperClassId(DjyosMessages.FpuType_SuperClassId);
			try {
				option1.setValue(
						DjyosMessages.Arch_Prefix+core.getArch().getArchitecture());
				option2.setValue(
						DjyosMessages.Family_Prefix+core.getArch().getFamily());
				boolean fpNeed = dideHelper.isFputypeuNeed(core);
				if(!fpNeed){
					option3.setValue(
							DjyosMessages.FpuABI_Prefix+"soft");
					option4.setValue(
							DjyosMessages.FpuType_Prefix+"default");
				}else {
					option3.setValue(
							DjyosMessages.FpuABI_Prefix+"hard");
					option4.setValue(
							DjyosMessages.FpuType_Prefix+core.getFpuType().replace("-", ""));
				}
				
			} catch (BuildException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		//添加family的链接
		String type = core.getArch().getSerie();//架构类型
		String arch = core.getArch().getArchitecture();//架构
		String family = core.getArch().getFamily();//架构家族
		List<File> excludeArchFiles = new ArrayList<File>();
		List<File> stepArchFiles = new ArrayList<File>();
		File[] typeFiles = archSourceFile.listFiles();
		for(File f:typeFiles) {
			if(!f.getName().equals(type)) {
				excludeArchFiles.add(f);
			}
		}
		//当前的Arch
		File curArchFile = null;
		for(File f:archXmlFiles) {
			if(f.getParentFile().getName().equals(family)) {
				curArchFile = f.getParentFile();
			}else {
				excludeArchFiles.add(f.getParentFile());
				stepArchFiles.add(f.getParentFile());
			}
		}
		getExcludeArchFiles(excludeArchFiles,stepArchFiles);
		//排除所有未被选中的arch
		for(File f:excludeArchFiles) {
			setFolderExclude(f, project, conds);
		}
		//释放当前的arch
		if(curArchFile != null) {
			IFolder archtectureFolder = project.getFolder("src/libos"+curArchFile.getPath().replace("\\", "/").replace(dideHelper.getDjyosSrcPath(), ""));
			List<IFolder> archtectureFolders = new ArrayList<IFolder>();
			getArchFolders(archtectureFolder, archtectureFolders); 
			setFoldersInclude(archtectureFolders,conds);
		}
		
		//组件操作
		ReadComponent rc = new ReadComponent();
		OnBoardCpu onBoardCpu = null ;
		List<OnBoardCpu> onBoardCpus = board.getOnBoardCpus();
		for (int i = 0; i < onBoardCpus.size(); i++) {
			if (onBoardCpus.get(i).getCpuName().equals(cpu.getCpuName())) {
				onBoardCpu = onBoardCpus.get(i);
				break;
			}
		}
		List<Component> compontentsList = rc.getAllComponents(onBoardCpu, board);
		CreateComponentInfo createComponentInfo = new CreateComponentInfo();
		File compInfoFile = new File(project.getLocation().toString()+"/data/hardwares/component_infos.xml");
		createNewFile(compInfoFile);
		createComponentInfo.createComponentInfo(compInfoFile, compontentsList);
		//為組建添加link項		
		linkComponentResource(true,appCompontentsChecked,compontentsList,srcLocation,_cpuName,project,conds);
		linkComponentResource(false,ibootCompontentsChecked,compontentsList,srcLocation,_cpuName,project,conds);
		
		//排除所有没有描述文件的组件
		GetNonCompFiles gecf = new GetNonCompFiles();
		List<File> excludeCompFiles = gecf.getExcludeCompFiles(onBoardCpu, board);
		for(File f:excludeCompFiles) {
			String relativePath = f.getPath().replace("\\", "/").replace(srcLocation, "");
			if(f.isFile()) {
				IFile ifile = project.getFile("src/libos"+relativePath);
				for (int j = 0; j < conds.length; j++) {
					linkHelper.setFileExclude(ifile, conds[j], true);
				}
			}else if(f.isDirectory()) {
				IFolder ifolder = project.getFolder("src/libos"+relativePath);
				for (int j = 0; j < conds.length; j++) {
					linkHelper.setExclude(ifolder, conds[j], true);
				}
			}
		}
		
		LinkProjectFile lpf = new LinkProjectFile();
		ReviseVariableToXML rvtx = new ReviseVariableToXML();
		rvtx.reviseXmlVariable("DJYOS_SRC_LOCATION","file:/"+dideHelper.getDjyosSrcPath(), 
				project.getFile(".project"),projectName);

		ReadBoardXml rbx = new ReadBoardXml();
		CreateBoardInfo createBoardInfo = new CreateBoardInfo();
		List<Board> allBoards = rbx.getAllBoards();
		File boardInfoFile = new File(project.getLocation().toString()+"/data/hardwares/board_infos.xml");
		createNewFile(boardInfoFile);
		createBoardInfo.createBoardInfo(boardInfoFile, allBoards);
		
		if(isDemoBoard) {
			setBoardExclude(true,board.getBoardName(),project,conds);
//			notExcludes.add("src/libos/bsp/boarddrv/demo/"+board.getBoardName());
		}else {
			setBoardExclude(false,board.getBoardName(),project,conds);
//			notExcludes.add("src/libos/bsp/boarddrv/user/"+board.getBoardName());
		}
		
		ReadCpuXml rcx = new ReadCpuXml();
		List<Cpu> allCpus = rcx.getAllCpus();
		//保存所有的cpu信息
		File cpuInfoFile = new File(project.getLocation().toString()+"/data/hardwares/cpu_infos.xml");
		createNewFile(cpuInfoFile);
		CreateCpuInfo createCpuInfo = new CreateCpuInfo();
		createCpuInfo.createCpuInfo(cpuInfoFile, allCpus);
		
		Cpu myCpu = null;
		for(Cpu c:allCpus) {
			if(c.getCpuName().equals(_cpuName)) {
				myCpu = c;
				break;
			}
		}
		setCpuFilesExclude(project,conds,myCpu,allCpus);
		
		try {
			CoreModel.getDefault().setProjectDescription(project, local_prjd);
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	private void getExcludeArchFiles(List<File> excludeArchFiles,List<File> stepArchFiles) {
		// TODO Auto-generated method stub
		List<File> tempArchFiles = new ArrayList<File>();
//		System.out.println("stepArchFile:   "+stepArchFiles.get(0).getName());
		for(File f:stepArchFiles) {
			File parentFile = f.getParentFile();
			if(!tempArchFiles.contains(parentFile) && !parentFile.getName().equals("arch")) {
				tempArchFiles.add(parentFile);
			}
			if(!excludeArchFiles.contains(parentFile) && !parentFile.getName().equals("arch")) {
				excludeArchFiles.add(parentFile);
			}
		}
		if(tempArchFiles.size()>0) {
			getExcludeArchFiles(excludeArchFiles,tempArchFiles);
		}
//		if(!excludeArchFiles.contains(file)) {
//			excludeArchFiles.add(file);
//		}
//		File parentFile = file.getParentFile();
//		if(!parentFile.getName().equals("cpudrv")) {
//			getExcludeArchFiles(parentFile,excludeArchFiles);
//		}
	}

	private void setArchLinks(Arch arch, List<String> archLinks, List<File> archXmlFiles) {
		// TODO Auto-generated method stub
		File archSrcFile = new File(dideHelper.getDjyosSrcPath()+"/bsp/arch");
		if(archSrcFile.exists()) {
			for(File f:archXmlFiles) {
				if(arch.getFamily()!=null) {
					if(f.getParentFile().getName().equals(arch.getFamily())) {
						File parentFile = f.getParentFile();
						getArchLinks(archLinks,parentFile);
						break;
					}
				}
			}
		}
//		archLinks.add("${DJYOS_SRC_LOCATION}/bsp/arch/"+arch.getSerie()+"/"+arch.getArchitecture()+"/"+arch.getFamily()+"/include");
//		archLinks.add("${DJYOS_SRC_LOCATION}/bsp/arch/"+arch.getSerie()+"/"+arch.getArchitecture()+"/include");
//		archLinks.add("${DJYOS_SRC_LOCATION}/bsp/arch/"+arch.getSerie()+"/include");
	}

	private void getArchLinks(List<String> archLinks, File archFolder) {
		// TODO Auto-generated method stub
		File includeFile = new File(archFolder.getPath()+"/include");
		if(includeFile.exists()) {
			archLinks.add(includeFile.getPath().replace(dideHelper.getArchPath(), "${DJYOS_SRC_LOCATION}/bsp/arch"));
		}
		if(!archFolder.getParentFile().getName().equals("arch")) {
			getArchLinks(archLinks,archFolder.getParentFile());
		}
	}

	private void createNewFile(File file) {
		// TODO Auto-generated method stub
		if(file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void getArchFolders(IFolder ifolder,List<IFolder> folders){
		folders.add(ifolder);
		System.out.println("ifolder:   "+ifolder.getFullPath().toString());
		IFolder parentFolder = (IFolder) ifolder.getParent();
		if(!parentFolder.getName().equals("arch")) {
			getArchFolders(parentFolder,folders);
		}
	}

	private void setCpuFilesExclude(IProject project, ICConfigurationDescription[] conds, Cpu myCpu, List<Cpu> allCpus) {
		// TODO Auto-generated method stub
		File cpuDrvFile = new File(dideHelper.getDjyosSrcPath()+"/bsp/cpudrv");
		
		//Exclude没有描述文件的的cpu
		GetNonCpuFiles gncf = new GetNonCpuFiles();
		List<File> excludeCpuFiles = gncf.getNonCpus();
		for(File f:excludeCpuFiles) {
			setFolderExclude(f,project,conds);
		}
		
		//Exclude不需要的cpu
		for(Cpu c:allCpus) {
			if(!c.getCpuName().equals(myCpu.getCpuName())) {
				String parentPath = c.getParentPath();
				File parentFile = new File(parentPath);
				travelCpuParentInclude(parentFile.getParentFile(),project,conds);
				travelCpuParentExclude(parentFile,project,conds);
			}
		}
		//Include需要的cpu
		File myCpuParentFile= new File(myCpu.getParentPath());
		travelCpuParentInclude(myCpuParentFile,project,conds);
	}

	private void travelCpuParentExclude(File parentFile, IProject project, ICConfigurationDescription[] conds){
		setFolderExclude(parentFile,project,conds);
		parentFile = parentFile.getParentFile();
		if(!parentFile.getName().equals("cpudrv")) {
			travelCpuParentExclude(parentFile,project,conds);
		}
	}
	
	private void travelCpuParentInclude(File file, IProject project, ICConfigurationDescription[] conds){
		File parentFile = file.getParentFile();
		if(!parentFile.getName().equals("cpudrv")) {
			travelCpuParentInclude(parentFile,project,conds);
		}
		setFolderInclude(file,project,conds);
	}

	private boolean existChildXml(File f) {
		// TODO Auto-generated method stub
		File[] files = f.listFiles();
		for(File file : files){
			if(file.getName().endsWith(".xml") && file.getName().contains("cpu_group")) {
				return true;
			}
		}
		return false;
	}

	private void setBoardExclude(boolean isDemoBoard, String boardName, IProject project, ICConfigurationDescription[] conds) {
		// TODO Auto-generated method stub
		File boardDrvFile = new File(dideHelper.getDjyosSrcPath() + "/bsp/boarddrv");
		File[] files = boardDrvFile.listFiles();
		for (File file : files) {
			if(file.isDirectory()) {
				File[] myFiles = file.listFiles();
				if (isDemoBoard) {
					if (file.getName().equals("demo")) {
						for (File f : myFiles) {
							if (!f.getName().equals(boardName)) {
								setFolderExclude(f, project, conds);
							} else {
								setFolderInclude(f, project, conds);
							}
						}
					} else {
						for (File f : myFiles) {
							setFolderExclude(f, project, conds);
						}
					}
				} else {
					if (file.getName().equals("user")) {
						for (File f : myFiles) {
							if (!f.getName().equals(boardName)) {
								setFolderExclude(f, project, conds);
							} else {
								setFolderInclude(f, project, conds);
							}
						}
					} else {
						for (File f : myFiles) {
							setFolderExclude(f, project, conds);
						}
					}
				}
			}
		}
	}
	
	//Exclude某个文件夹
	private void setFolderExclude(File f, IProject project, ICConfigurationDescription[] conds){
		String srcLocation = dideHelper.getDjyosSrcPath();
		String filePath = f.getPath().toString().replace("\\", "/");
		String relativePath = filePath.replace(srcLocation, "");
		IFolder folder = project.getFolder("src/libos"+relativePath);
		for (int i = 0; i < conds.length; i++) {
			if(conds[i].getName().contains("libos")) {
				linkHelper.setExclude(folder, conds[i], true);
			}
		}
	}
	
	private void setFolderInclude(File f, IProject project, ICConfigurationDescription[] conds){
		String srcLocation = dideHelper.getDjyosSrcPath();
		String filePath = f.getPath().toString().replace("\\", "/");
		String relativePath = filePath.replace(srcLocation, "");
		IFolder folder = project.getFolder("src/libos"+relativePath);
		for (int i = 0; i < conds.length; i++) {
			if(conds[i].getName().contains("libos")) {
					linkHelper.setExclude(folder, conds[i], false);
			}
		}
	}

	//include多个文件夹
	private void setFoldersInclude(List<IFolder> folders, ICConfigurationDescription[] conds) {
		// TODO Auto-generated method stub
		for (int i = 0; i < conds.length; i++) {
			if(conds[i].getName().contains("libos")) {
				for(int k=folders.size()-1;k>=0;k--) {
					System.out.println("folders.get(k):   "+folders.get(k).getFullPath().toString());
					linkHelper.setExclude(folders.get(k), conds[i], false);
				}
			}
		}
	}

	private void linkComponentResource(boolean isApp,List<Component> compontentsChecked, List<Component> compontentsList, String srcLocation, String _cpuName, IProject project, ICConfigurationDescription[] conds) {
		// TODO Auto-generated method stub
		List<String> notExcludes = new ArrayList<String>();
		List<String> excludes = new ArrayList<String>();	
		
		List<Component> compontentsExclude = new ArrayList<Component>();
		for(Component component:compontentsChecked) {
			System.out.println("compontentsChecked:    "+component.getName());
		}
		getCompontentsExclude(compontentsExclude,compontentsChecked,compontentsList);
		for(int j=0;j<compontentsExclude.size();j++) {
			Component component = compontentsExclude.get(j);
			String componentName = component.getName().replace("\\", "/");
//			System.out.println("compontentsExclude:    "+component.getName());
			String componentPath = component.getParentPath().replace("\\", "/");
			String relativePath = componentPath.replace(srcLocation, "");
			IFolder folder = project.getFolder("src/libos"+relativePath);
			IFile file = project.getFile("src/libos"+relativePath+"/"+component.getFileName());
			for (int i = 0; i < conds.length; i++) {
				if(isApp) {
					if(conds[i].getName().contains("libos_App")) {
//						if(component.getFileName().endsWith(".c")) {
//							linkHelper.setFileExclude(file, conds[i], true);
//						}else if(component.getFileName().endsWith(".h")) {
//							linkHelper.setExclude(folder, conds[i], true);
//						}
						linkHelper.setExclude(folder, conds[i], true);
					}
				}else {
//					if(component.getFileName().endsWith(".c")) {
//						linkHelper.setFileExclude(file, conds[i], true);
//					}else if(component.getFileName().endsWith(".h")) {
//						linkHelper.setExclude(folder, conds[i], true);
//					}
					if(conds[i].getName().contains("libos_Iboot")) {
						linkHelper.setExclude(folder, conds[i], true);
					}
				}
			}
		}
		
		
		for(int j=0;j<compontentsChecked.size();j++){
			Component component = compontentsChecked.get(j);
			String componentName = component.getName();
			String componentPath = component.getParentPath().replace("\\", "/");
			String relativePath = componentPath.replace(srcLocation, "").replace("\\", "/");	// /third/.../componentName
			List<String> excludeFiles = component.getExcludes();
			for (String exclude : excludeFiles) {
				excludes.add("src/libos" + relativePath + exclude);
//				System.out.println("exclude:    /src/libos" + relativePath + exclude);
			}	
			
//			notExcludes.add("src/libos"+relativePath);
		}
		
		// 隐藏不需要编译的文件
//		System.out.println("excludes.size():  " + excludes.size());
		for (int j = 0; j < excludes.size(); j++) {
			System.out.println("excludes.get(j):  " + excludes.get(j));
			IFile ifle = project.getFile(excludes.get(j));
			for (int i = 0; i < conds.length; i++) {
				if (isApp) {
					if (conds[i].getName().contains("libos_App")) {
						linkHelper.setFileExclude(ifle, conds[i], true);
					}
				} else {
					if (conds[i].getName().contains("libos_Iboot")) {
						linkHelper.setFileExclude(ifle, conds[i], true);
					}
				}
			}
		}
		
		//不隐藏需要编译的组件
//		for(int j=0;j<notExcludes.size();j++) {
//			IFolder ifle = project.getFolder(notExcludes.get(j));
//			for (int i = 0; i < conds.length; i++) {
//				if(isApp) {
//					if(conds[i].getName().contains("libos_App")) {
//						List<IFolder> includeFolders = new ArrayList<IFolder>();
//						linkHelper.getFolders(ifle, includeFolders);
//						for(int k=includeFolders.size()-1;k>=0;k--) {
//							linkHelper.setExclude(includeFolders.get(k), conds[i], false);
//						}
//					}
//				}else {
//					if(conds[i].getName().contains("libos_Iboot")) {
//						List<IFolder> includeFolders = new ArrayList<IFolder>();
//						linkHelper.getFolders(ifle, includeFolders);
//						for(int k=includeFolders.size()-1;k>=0;k--) {
//							linkHelper.setExclude(includeFolders.get(k), conds[i], false);
//						}
//					}
//				}
//				
//			}
//		}
		
	}

	private void getCompontentsExclude(List<Component> compontentsExclude, List<Component> compontentsChecked,
			List<Component> compontentsList) {
		// TODO Auto-generated method stub
		for(Component list:compontentsList) {
			boolean exist = false;
			for(Component checked:compontentsChecked) {
				if(checked.getParentPath().equals(list.getParentPath())) {
					exist = true;
					break;
				}
			}
			if(!exist) {
				compontentsExclude.add(list);
			}
		}
//		sortComponent(compontentsExclude);
	}

	private void linkComponentGUN(List<Component> compontentsChecked, List<String> links, List<String> includes, boolean isDemoBoard, File[] cpudrvFiles, String _cpuName, String srcLocation, String _boardName, List<String> assemblyLinks, ICResourceDescription rds) {
		// TODO Auto-generated method stub
		List<String> myLinks = new ArrayList<String>();
		myLinks.addAll(links);
		
		for(int j=0;j<compontentsChecked.size();j++) {
			Component component = compontentsChecked.get(j);
			String componentName = compontentsChecked.get(j).getName();
			String linkFolder = component.getLinkFolder();
			List<String> includeFiles = component.getIncludes();//includes
			String componentPath = component.getParentPath().replace("\\", "/");
			String relativePath = componentPath.replace(srcLocation, "").replace("\\", "/");//\component\buffer
			for(String include:includeFiles) {
				includes.add(relativePath+include);
			}
			myLinks.add("${DJYOS_SRC_LOCATION}"+relativePath);
		}
		
		for(String include:includes) {
			myLinks.add("${DJYOS_SRC_LOCATION}"+include);
		}
		//MACRO Entries
		List<ICLanguageSettingEntry>  _entries = new ArrayList<ICLanguageSettingEntry>();
		linkHelper.fillSymbols(compontentsChecked,_entries);
		//C/C++ Entries
		List<ICLanguageSettingEntry>  entries = new ArrayList<ICLanguageSettingEntry>();
		for(int k=0;k<myLinks.size();k++) {
			ICLanguageSettingEntry entry = CDataUtil.createCIncludePathEntry(myLinks.get(k), 0);
			entries.add(entry);
		}
		
		//Assembly Entries
		List<ICLanguageSettingEntry>  assemblyEntries = new ArrayList<ICLanguageSettingEntry>();
		for(int k=0;k<assemblyLinks.size();k++) {
			ICLanguageSettingEntry entry = CDataUtil.createCIncludePathEntry(assemblyLinks.get(k), 0);
			assemblyEntries.add(entry);
		}
		for(int k=0;k<myLinks.size();k++) {
			if(myLinks.get(k).endsWith("include")) {
				ICLanguageSettingEntry entry = CDataUtil.createCIncludePathEntry(myLinks.get(k), 0);
				assemblyEntries.add(entry);
			}
		}
		
		ICLanguageSetting[] languageSettings = linkHelper.getLangSetting(rds);
		for (int j=0; j<languageSettings.length; j++) {
			ICLanguageSetting lang = languageSettings[j];//获取语言类型	
			linkHelper.changeIt(ICSettingEntry.MACRO,_entries,lang.getSettingEntries(ICSettingEntry.MACRO),lang);
			
			//Assembly添加链接
			if(j==0) {	
				linkHelper.changeIt(ICSettingEntry.INCLUDE_PATH,assemblyEntries,lang.getSettingEntries(ICSettingEntry.INCLUDE_PATH),lang);
			}
			//GNU C/C++ 添加链接
			else {
				linkHelper.changeIt(ICSettingEntry.INCLUDE_PATH,entries,lang.getSettingEntries(ICSettingEntry.INCLUDE_PATH),lang);
			}			
		}
		
	}

	private void getCpuIncludes(File cpuFile, List<String> cpuLinkStrings) {
		// TODO Auto-generated method stub
		String DJYOS_SRC_LOCARION = didePath+"djysrc";
		File[] files = cpuFile.listFiles();
		for(File file:files) {
			String path = file.getPath();
			String relativePath = path.substring(DJYOS_SRC_LOCARION.length(), path.length());
			if(file.getName().equals("include")) {
				cpuLinkStrings.add("${DJYOS_SRC_LOCATION}"+relativePath);
				break;
			}
		}
		if(!cpuFile.getParentFile().getName().equals("cpudrv")) {
			getCpuIncludes(cpuFile.getParentFile(),cpuLinkStrings);
		}		
	}

	private boolean fileContainsName(File file, String _cpuName) {
		// TODO Auto-generated method stub
		File[] childFiles = file.listFiles();
		for(File cfile:childFiles) {
			if(cfile.isDirectory()) {
				if(cfile.getName().equals(_cpuName)) {
					return true;
				}else {
					if(fileContainsName(cfile,_cpuName)) {
						return true;
					}else {
						continue;
					}
				}
			}
		}
		return false;
	}

	private String getCpuLinkString(File file, String _cpuName, String cpuLinkString) {
		// TODO Auto-generated method stub
		File[] files = file.listFiles();
		String CPUDRV_LOCARION = didePath+"djysrc/cpudrv/";
		for(int j=0;j<files.length;j++) {
			String path = files[j].getPath();
			String relativePath = path.substring(CPUDRV_LOCARION.length()+4, path.length());
			if(_cpuName.endsWith(files[j].getName().toUpperCase())) {
				cpuLinkString=relativePath; //\stm32\f5
			}else if(_cpuName.contains(files[j].getName().toUpperCase())){
				getCpuLinkString(files[j],_cpuName,cpuLinkString);
			}
		}
		return cpuLinkString;
	}

	private List<String> getLinkStrings(File file,String _cpuName,String componentName,List<String> linkStrings) {
		// TODO Auto-generated method stub
		File[] files = file.listFiles();
		String DJYOS_SRC_LOCARION = didePath+"djysrc";
		for(int j=0;j<files.length;j++) {
			String path = files[j].getPath();
			String relativePath = path.substring(DJYOS_SRC_LOCARION.length(), path.length());
			if(_cpuName.endsWith(files[j].getName().toUpperCase())) {
				linkStrings.add("${DJYOS_SRC_LOCATION}"+relativePath+"/src/"+componentName);
			}else if(_cpuName.contains(files[j].getName().toUpperCase())){
				getLinkStrings(files[j],_cpuName,componentName,linkStrings);
			}else if(files[j].getName().equals("src")) {
				linkStrings.add("${DJYOS_SRC_LOCATION}"+relativePath);
			}
		}
		return linkStrings;
	}
	
	public boolean addMemoryToLds(String content, String path, String projectName) throws IOException {

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

		return true;
	}
	
    public void getMemoryToLds(String ldsHead,String ldsDesc,String projectName,String sourcePath) {
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
			addMemoryToLds(content,path,projectName);	
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
	}

	@Override
	public boolean performFinish() {
		
		Cpu cpu = fMainPage.getSelectCpu();
		Board board = fMainPage.getSelectBoard();
		Core core = fMainPage.getSelectCore();
		if(core.getArch().getSerie() == null) {
			return dideHelper.showErrorMessage("Cpu的架构类型为空");
		}else if(core.getArch().getArchitecture() == null) {
			return dideHelper.showErrorMessage("Cpu的架构为空");
		}else if(core.getArch().getFamily() == null) {
			return dideHelper.showErrorMessage("Cpu的架构家族为空");
		}
		String projectPath = fMainPage.locationArea.locationPathField.getText();
		
    	String ldsHead = fMainPage.getLdsHead();
    	String ldsDesc = fMainPage.getLdsDesc();
    	String projectName = fMainPage.getProjectName();
		String sourcePath = fMainPage.projectPath;
		
		int index = fMainPage.getTemplateIndex();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if(! projectPath.contains(projectName)) {
			project = curProject;
		}	
		
		String projectLocation = project.getLocation().toString();
		File dataFolder = new File(projectLocation+"/data");
		if(!dataFolder.exists()) {
			dataFolder.mkdirs();
		}
		File hardWardInfoFile = new File(projectLocation+"/data/hardware_info.xml");
    	try {
			hardWardInfoFile.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	CreateHardWareDesc chwd = new CreateHardWareDesc();
		chwd.createHardWareXml(board.getBoardName(), cpu.getCpuName(), hardWardInfoFile);
		
    	List<Component> compontentsChecked = new ArrayList<Component>();
    	List<Component> appCompontentsChecked = cpomtCfgPage.getAppCompontentsChecked();
    	List<Component> ibootCompontentsChecked = cpomtCfgPage.getIbootCompontentsChecked();
    	
    	IRunnableWithProgress runnable = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
				monitor.beginTask("配置工程...", 10);	
				
				//set userActive to be true		
				ICProjectDescriptionManager prjDescMgr= CCorePlugin.getDefault().getProjectDescriptionManager();
				ICProjectDescriptionWorkspacePreferences prefs= prjDescMgr.getProjectDescriptionWorkspacePreferences(true);
				prefs.setConfigurationRelations(ICProjectDescriptionPreferences.CONFIGS_LINK_SETTINGS_AND_ACTIVE);
				prjDescMgr.setProjectDescriptionWorkspacePreferences(prefs, false, new NullProgressMonitor());
				
				//生成initPrj.c,initPrj.h,memory.lds文件
				monitor.setTaskName("配置工程初始化文件...");
				if (index == 0 || index == 1) {
					try {
						cpomtCfgPage.initProject(projectLocation, false);
						cpomtCfgPage.creatProjectConfiure(projectLocation, core, false);
					} catch (Exception e) {
						dideHelper.showErrorMessage("配置Iboot初始化文件错误："+e.getMessage()+"   "+e.getLocalizedMessage()+"\n"+e.getCause());
					}
				}
				if (index == 0 || index == 2 || index == 3) {
					try {
						cpomtCfgPage.initProject(projectLocation, true);
						cpomtCfgPage.creatProjectConfiure(projectLocation, core, true);
					} catch (Exception e) {
						dideHelper.showErrorMessage("配置App初始化文件错误："+e.getMessage());
					}
				}
//				monitor.worked(2);
				
				//根据MemoryMap配置的内容添加memory.lds文件
//				monitor.setTaskName("配置lds文件...");
//				try {
//					dideHelper.copyFolder(src, dest);
////					getMemoryToLds(ldsHead,ldsDesc,projectName,sourcePath);
//				} catch (Exception e) {
//					dideHelper.showErrorMessage("lds配置错误："+e.getMessage());
//				}
				monitor.worked(6);
				
				//处理工程的链接
				monitor.setTaskName("配置工程链接...");
//				try {
					handleCProject(appCompontentsChecked,ibootCompontentsChecked,board,cpu,core,projectPath,projectName,index);
//				} catch (Exception e) {
//					dideHelper.showErrorMessage("链接配置错误："+e.getMessage());
//				}
				monitor.worked(1);
				
				monitor.setTaskName("工程刷新中...");
				try {
					final IWorkspace workspace = ResourcesPlugin.getWorkspace();
					IProject project = workspace.getRoot().getProject(projectName);
					project.refreshLocal(IResource.DEPTH_INFINITE, null);
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
    	
//		createBuild(curProject);
//    	if(createBuild(curProject)) {
//    		MessageDialog.openInformation(window.getShell(),
//    				"提示","后台编译库完成");
//    	}
		
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
		File libFile = new File(project.getLocation().toString()+"/lib");
		
		for (IConfiguration cfg : cfgs) {
			IBuilder builder = cfg.getEditableBuilder();
			String cfgName = cfg.getName();
			if (cfgName.startsWith("libos")) {
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
//									cb.performPrebuildGeneration(IncrementalProjectBuilder.FULL_BUILD, binfo,
//											status, monitor);
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
//			if(isClean) {
//				File cfgFile = new File(project.getLocation().toString()+"/"+cfgName);
//				File[] files = cfgFile.listFiles();
//				for(File file:files) {
//					if(file.getName().endsWith(".a")) {
//						try {
//							dideHelper.copyFolder(file,libFile);
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//				}
//			}
		}
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
    
    public void clearNewProject() {
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
		if (clickedMianNext) {
			return true;
		}
		return false;
	}
    
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
	
	@Override
	public boolean isPageDragable() {
		// TODO Auto-generated method stub
		return true;
	} 
	
}
