package org.eclipse.cdt.ui.wizards.djyosProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.deferred.ChangeQueue.Change;
import org.eclipse.ltk.internal.core.refactoring.resource.RenameResourceProcessor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
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
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICMultiFolderDescription;
import org.eclipse.cdt.core.settings.model.ICMultiResourceDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionPreferences;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.MultiLanguageSetting;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.OptionStringValue;
import org.eclipse.cdt.managedbuilder.internal.core.CommonBuilder;
import org.eclipse.cdt.managedbuilder.internal.core.CommonBuilder.BuildStatus;
import org.eclipse.cdt.managedbuilder.internal.core.CommonBuilder.CfgBuildInfo;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;
import org.eclipse.cdt.ui.wizards.CWizardHandler;
import org.eclipse.cdt.ui.wizards.board.Board;
import org.eclipse.cdt.ui.wizards.board.CreatBoardXml;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.Chip;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.OnBoardCpu;
import org.eclipse.cdt.ui.wizards.component.Component;
import org.eclipse.cdt.ui.wizards.component.ConfigureTarget;
import org.eclipse.cdt.ui.wizards.cpu.Cpu;
import org.eclipse.cdt.ui.wizards.cpu.core.Core;
import org.eclipse.cdt.ui.wizards.djyosProject.tools.DideHelper;
import org.eclipse.cdt.ui.wizards.parsexml.ReviseLinkToXML;
import org.eclipse.cdt.ui.wizards.parsexml.ReviseVariableToXML;

import org.eclipse.cdt.internal.ui.newui.Messages;
import org.eclipse.cdt.internal.ui.wizards.ICDTCommonProjectWizard;

public abstract class DjyosCommonProjectWizard extends BasicNewResourceWizard
{
	private static final String PREFIX= "CProjectWizard"; //$NON-NLS-1$

	boolean addedMemory = false,createdProject = false,clickedMianNext = false,
			existingPath = false,projectExist = false,addedComptCfg = false;
	protected DjyosMainWizardPage fMainPage;//主界面
	protected ComponentConfigWizard cpomtCfgPage = null;//组件配置界面
	protected MemoryMapWizard memoryPage;//Memory向导界面
	protected ModuleConfigurationWizard modulePage;//Module向导界面
	private String deapPath = null,wz_title, wz_desc, lastProjectName,boardModuleTrimPath;
	private String didePath = new DideHelper().getDIDEPath();
	private URI lastProjectLocation = null;
	private IProject curProject;
	protected IProject newProject;
	private IWorkbenchWindow window = PlatformUI.getWorkbench()
			.getActiveWorkbenchWindow();
	
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
	
	/*
	 * 创建工程
	 */
	public void importProject(String projectPath) {
		
		String projectName = fMainPage.getProjectName();//用户填写的工程名
		String templateName = getTemplateName();//用户选择的模板
		String srcPath = didePath + "demo/" + templateName;//模板的路径
//		String destPath = fMainPage.locationArea.locationPathField.getText();//新工程的存放路径 locationArea.locationPathField.getText();
		String userPath = projectPath;
		if(!projectPath.contains(projectName)) {
			projectPath = projectPath+"/"+projectName;
		}
		String destPath = projectPath;
		
		File srcFile = new File(srcPath);
//		File dest = new File(destPath);
		File destFile = new File(destPath);

		if(!destFile.exists()) {
			destFile.mkdir();
			try {
				copyFolder(srcFile,destFile);
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
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				MessageDialog.openError(window.getShell(), "提示",
						"工程创建失败"+e.getMessage());
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				MessageDialog.openError(window.getShell(), "提示",
						"工程创建失败"+e.getMessage());
				e.printStackTrace();
			}
				
			projectExist = false;
			createdProject = true;
						
		}else {
			projectExist = true;
			fMainPage.setErrorMessage("Project is aready existed");
		}		
			
	}
	
	/*
	 * 导入模板工程到当前工作空间
	 */
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

	public IProject getProject() {
		return curProject;
	}
	
	public ICLanguageSetting[] getLangSetting(ICResourceDescription rcDes) {
		switch (rcDes.getType()) {
		case ICSettingBase.SETTING_PROJECT:
		case ICSettingBase.SETTING_CONFIGURATION:
		case ICSettingBase.SETTING_FOLDER:
			ICFolderDescription foDes = (ICFolderDescription)rcDes;
			return foDes.getLanguageSettings();
		case ICSettingBase.SETTING_FILE:
			ICFileDescription fiDes = (ICFileDescription)rcDes;
			ICLanguageSetting langSetting = fiDes.getLanguageSetting();
			return (langSetting != null) ? new ICLanguageSetting[] { langSetting } : null;
		}
		return null;
	}
	
	private void changeIt(int kind,List<ICLanguageSettingEntry>  entries, ICLanguageSettingEntry[] ents, ICLanguageSetting lang) {
		List<ICLanguageSettingEntry> lsEntries = new ArrayList<ICLanguageSettingEntry>();
		for(int i=0;i<ents.length;i++) {
			lsEntries.add(ents[i]);
		}
		if (entries != null) {
			for(int i=0;i<entries.size();i++) {
				lsEntries.add(entries.get(i));
			}
		}
		setSettingEntries(kind, lsEntries, lang);
	}
	
	private void setSettingEntries(int kind, List<ICLanguageSettingEntry> incs,ICLanguageSetting lang) {
			lang.setSettingEntries(kind, incs);//ICLanguageSetting
	}
	
	private String getCpuSrcPath(String cpuName) {
		String sourcePath = didePath+"djysrc/bsp/cpudrv";
		File sourceFile = new File(sourcePath);
		File[] files = sourceFile.listFiles();
		String path = null;
		for(File file:files){//cpudrv下的所有文件 Atmel stm32
			if(file.isDirectory() && !file.isFile()) {
				getDeapPath(file,cpuName);
				if(deapPath!=null) {
					path = deapPath;
					break;
				}
			}
			
		}
		return path;
	}
	
	public String getDeapPath(File sourceFile,String cpuName) {
		File[] files = sourceFile.listFiles();
		String path = null;
		for (File file : files) {
			if (file.isDirectory()) {
				if(file.getName().equals(cpuName)) {
					deapPath = file.getPath();
					break;
				}else if(!file.getName().equals("include") && !file.getName().equals("src")){
					getDeapPath(file,cpuName);//如果为目录，则继续扫描该目录
				}			
			}
		}
		return deapPath;
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
		
		File boardDemoFile = new File(boardPath+"/demo");
		File[] boardDemoFiles = boardDemoFile.listFiles();
		boolean isDemoBoard = false;
		for(int j=0;j<boardDemoFiles.length;j++) {
			if(boardDemoFiles[j].getName().equals(_boardName)) {
				isDemoBoard = true;
				break;
			}
		}
		
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		
		if(! projectPath.contains(projectName)) {
			project = curProject;
		}	
		
		List<String> includes = new ArrayList<String>();		

		final ICProjectDescription local_prjd =  CoreModel.getDefault().getProjectDescription(project);
		ICConfigurationDescription[] conds = local_prjd.getConfigurations();	//获取工程的所有Configuration		
		local_prjd.setConfigurationRelations(ICProjectDescriptionPreferences.CONFIGS_LINK_SETTINGS_AND_ACTIVE);
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
			IResourceInfo resourceInfo = cfg.getRootFolderInfo();
			IToolChain toolchain = resourceInfo.getParent().getToolChain();
			ICSourceEntry[] sourceEntrys = conds[i].getSourceEntries();
			List<String> links = new ArrayList<String>();
			List<String> assemblyLinks = new ArrayList<String>();
			//根据架构类型选择链接
			if(core.getType().equals("arm")) {
				links.add("${DJYOS_SRC_LOCATION}/third/firmware/CMSIS/include");
				links.add("${DJYOS_SRC_LOCATION}/bsp/arch/arm");
				assemblyLinks.add("${DJYOS_SRC_LOCATION}/bsp/arch/arm");
			}
			//根据cpu链接
			List<String> cpuLinkStrings = new ArrayList<String>();
			String links_cpuPath = getCpuSrcPath(_cpuName);
			getCpuIncludes(new File(links_cpuPath),cpuLinkStrings);
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
				links.add("${DJYOS_SRC_LOCATION}/bsp/boarddrv/demo/"+_boardName);
				links.add("${DJYOS_SRC_LOCATION}/bsp/boarddrv/demo/"+_boardName+"/include");
				links.add("${DJYOS_SRC_LOCATION}/bsp/boarddrv/demo/"+_boardName+"/startup");
			}else {
				links.add("${DJYOS_SRC_LOCATION}/bsp/boarddrv/user/"+_boardName);
				links.add("${DJYOS_SRC_LOCATION}/bsp/boarddrv/user/"+_boardName+"/include");
				links.add("${DJYOS_SRC_LOCATION}/bsp/boarddrv/user/"+_boardName+"/startup");
			}

//			//根据Cpu的固件库链接
//			//查看
//			if(firmwareLib!=null) {
//				links.add("${DJYOS_SRC_LOCATION}/third/firmware/"+firmwareLib);
//				File file = new File(eclipsePath+"djysrc/third/firmware/"+firmwareLib);
//				File[] files = file.listFiles();
//				for(int j=0;j<files.length;j++) {//  firmware/firmwareLib/.......
//					if(files[j].isDirectory()) {
//						File[] childFiles = files[j].listFiles();
//						for(int k=0;k<childFiles.length;k++) {
//							if(childFiles[k].getName().endsWith(".h")) {//  firmware/firmwareLib/include/
//								links.add("${DJYOS_SRC_LOCATION}/third/firmware/"+firmwareLib+"/"+files[j].getName());
//								break;
//							}
//						}
//					}
//				}
//			}
			//根据内核类型、架构、家族链接
			links.add("${DJYOS_SRC_LOCATION}/bsp/arch/"+core.getType()+"/"+core.getArch()+"/"+core.getFamily()+"/include");
			assemblyLinks.add("${DJYOS_SRC_LOCATION}/bsp/arch/"+core.getType()+"/"+core.getArch()+"/"+core.getFamily()+"/include");
			//添加project_config.h的链接
			if(selectIndex == 0 || selectIndex == 1){
				links.add("${ProjDirPath}/src/iboot/OS_prjcfg");
	    	}
	    	if(selectIndex == 0 || selectIndex == 2 || selectIndex == 3){
	    		links.add("${ProjDirPath}/src/app/OS_prjcfg");
	    	}
			
			//根据所选组件链接
	    	if(conName.contains("App")) {
	    		linkComponentGUN(appCompontentsChecked,links,includes,isDemoBoard,cpudrvFiles,_cpuName,srcLocation,_boardName,assemblyLinks,rds);
	    	}else if(conName.contains("Iboot")){
	    		linkComponentGUN(ibootCompontentsChecked,links,includes,isDemoBoard,cpudrvFiles,_cpuName,srcLocation,_boardName,assemblyLinks,rds);
	    	}
	    	
			IOption option1 = toolchain.getOptionBySuperClassId(DjyosMessages.Arch_SuperClassId);
			IOption option2 = toolchain.getOptionBySuperClassId(DjyosMessages.Family_SuperClassId);        				
			IOption option3 = toolchain.getOptionBySuperClassId(DjyosMessages.FpuABI_SuperClassId);
			IOption option4 = toolchain.getOptionBySuperClassId(DjyosMessages.FpuType_SuperClassId);
			try {
				option1.setValue(
						DjyosMessages.Arch_Prefix+core.getArch());
				option2.setValue(
						DjyosMessages.Family_Prefix+core.getFamily());
				option3.setValue(
						DjyosMessages.FpuABI_Prefix+core.getFloatABI());
				option4.setValue(
						DjyosMessages.FpuType_Prefix+core.getFpuType().replace("-", ""));
			} catch (BuildException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		LinkProjectFile lpf = new LinkProjectFile();
		ReviseVariableToXML rvtx = new ReviseVariableToXML();
		rvtx.reviseXmlVariable("DJYOS_SRC_LOCATION","file:/"+didePath+"djysrc", 
				project.getFile(".project"),projectName);
		ReviseLinkToXML rltx = new ReviseLinkToXML();
		if(isDemoBoard) {
			lpf.addLink(project.getFile(".project"), board.getBoardName(), "bsp/boarddrv/demo/"+board.getBoardName(),
					"boarddrv");
//			lpf.addLink(project.getFile(".project"), "startup", "bsp/boarddrv/demo/"+board.getBoardName()+"/startup",
//					"startup");
		}else {
			lpf.addLink(project.getFile(".project"), board.getBoardName(), "bsp/boarddrv/user/"+board.getBoardName(),
					"boarddrv");
//			lpf.addLink(project.getFile(".project"), "startup", "bsp/boarddrv/user/"+board.getBoardName()+"/startup",
//					"startup");
		}
		//添加固件库链接
		if(firmwareLib!=null) {
			lpf.addLink(project.getFile(".project"), firmwareLib, firmwareLib,
					"firmware"); 
		}
		//添加family的链接
		String type = core.getType();
		String arch = core.getArch();
		String family = core.getFamily();
		lpf.addLink(project.getFile(".project"), family, "bsp/arch/"+type+"/"+arch+"/"+family,
				"arch");
		
		// 添加cpudrv的链接
		String cpuLinkString = "";
		deapPath = null;
		getCpuSrcPath(_cpuName);
		if (deapPath != null) {
			deapPath = deapPath.replace("\\", "/");
			cpuLinkString = deapPath.replace(didePath + "djysrc/", "");
			lpf.addLink(project.getFile(".project"), _cpuName, cpuLinkString,
					"cpudrv");
		}
		
		//為組建添加link項		
		linkComponentResource(true,appCompontentsChecked,srcLocation,cpuLinkString,_cpuName,project,conds);
		linkComponentResource(false,ibootCompontentsChecked,srcLocation,cpuLinkString,_cpuName,project,conds);

		try {
			CoreModel.getDefault().setProjectDescription(project, local_prjd);
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}

	private void linkComponentResource(boolean isApp,List<Component> compontentsChecked, String srcLocation, String cpuLinkString, String _cpuName, IProject project, ICConfigurationDescription[] conds) {
		// TODO Auto-generated method stub
		List<String> notExcludes = new ArrayList<String>();	
		List<String> excludes = new ArrayList<String>();	
		for(int j=0;j<compontentsChecked.size();j++){
			Component component = compontentsChecked.get(j);
			String componentName = component.getName();
			String componentPath = component.getParentPath().replace("\\", "/");
			String linkFolder = component.getLinkFolder();
			String relativePath = componentPath.replace(srcLocation, "").replace("\\", "/");	// /third/.../componentName
			List<String> excludeFiles = component.getExcludes();
//			System.out.println("componentPath:  "+componentPath);
//			System.out.println("srcLocation:  "+srcLocation);
//			System.out.println("relativePath:  "+relativePath);
			notExcludes.add("/src/libos"+relativePath);
			if (linkFolder.equals("third") || linkFolder.equals("component") || linkFolder.equals("djyos")
					|| linkFolder.equals("chipdrv")) {
				for (String exclude : excludeFiles) {
					excludes.add("/src/libos" + relativePath + exclude);
				}
			} else if (linkFolder.equals("cpudrv")) {
				for (String exclude : excludeFiles) {
					excludes.add("/src/libos" + relativePath + exclude);
				}
				String filePath = component.getParentPath();
				String cptName = filePath.substring(filePath.lastIndexOf("\\") + 1, filePath.length());
				componentPath = cpuLinkString.replace(_cpuName, "") + "src/" + cptName;
			}
//			lpf.addLink(project.getFile(".project"), componentName,componentPath, linkFolder);	
			
		}
		//隐藏不需要编译的文件
//		for(int j=0;j<excludes.size();j++) {
//			String ifilePath = (project.getLocation().toString() + excludes.get(j));
//			IFile ifle = project.getFile(ifilePath.substring(project.getLocation().toString().length()));
//			for (int i = 0; i < conds.length; i++) {
//				if(isApp) {
//					System.out.println("excludes:  "+ifle.getFullPath()+"  "+ifilePath);
//					if(conds[i].getName().contains("libos_App")) {
//						setExclude(ifle, conds[i], true);
//					}
//				}else {
//					if(conds[i].getName().contains("libos_Iboot")) {
//						setExclude(ifle, conds[i], true);
//					}
//				}
//			}
//		}
		//不隐藏需要编译的组件
//		IFolder ifle = project.getFolder("/src/libos/component/charset");
//		IFolder ifle1 = project.getFolder("/src/libos/Test");
//		for (int i = 0; i < conds.length; i++) {
//			if(isApp) {
//				if(conds[i].getName().contains("libos_App")) {
//					setExclude(ifle, conds[i], false);
//					setExclude(ifle1, conds[i], false);
//				}
//			}
//		}
//		setExclude(ifle, conds[i], false);
		for(int j=0;j<notExcludes.size();j++) {
			String ifilePath = (project.getLocation().toString() + notExcludes.get(j));
			IFolder ifle = project.getFolder(ifilePath.substring(project.getLocation().toString().length()));
			for (int i = 0; i < conds.length; i++) {
				if(isApp) {
					if(conds[i].getName().contains("libos_App")) {
//						System.out.println("notExcludes:  "+ifle.getFullPath());
//						System.out.println("notExcludes1:  "+ifilePath);
//						System.out.println("notExcludes2:  "+ifilePath.substring(project.getLocation().toString().length()));
						List<IFolder> includeFolders = new ArrayList<IFolder>();
						getFolders(ifle, includeFolders);
						for(int k=includeFolders.size()-1;k>=0;k--) {
							setExclude(includeFolders.get(k), conds[i], false);
						}
						
					}
				}else {
					if(conds[i].getName().contains("libos_Iboot")) {
						List<IFolder> includeFolders = new ArrayList<IFolder>();
						getFolders(ifle, includeFolders);
						for(int k=includeFolders.size()-1;k>=0;k--) {
							setExclude(includeFolders.get(k), conds[i], false);
						}
					}
				}
				
			}
		}
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
			String relativePath = componentPath.replace(srcLocation, "");	// /third/.../componentName
			for(String include:includeFiles) {//	../Inc 
				includes.add(relativePath+include);
			}

			if(linkFolder.equals("component")) {
				myLinks.add("${DJYOS_SRC_LOCATION}/component/"+componentName);
			}else if(linkFolder.equals("djyos")) {
				myLinks.add("${DJYOS_SRC_LOCATION}/djyos/"+componentName);
			}else if(linkFolder.equals("chipdrv")) {
				myLinks.add("${DJYOS_SRC_LOCATION}/bsp/chipdrv/"+componentName);
			}else if(linkFolder.equals("boarddrv")) {
				if(isDemoBoard) {
					myLinks.add("${DJYOS_SRC_LOCATION}/bsp/boarddrv/demo/"+_boardName+"/drv/"+componentName);
				}else {
					myLinks.add("${DJYOS_SRC_LOCATION}/bsp/boarddrv/user/"+_boardName+"/drv/"+componentName);
				}	
			}else if(linkFolder.equals("cpudrv")) {
				for(int k=0;k<cpudrvFiles.length;k++) {
					deapPath = null;
					if(cpudrvFiles[k].isDirectory()) {
						getDeapPath(cpudrvFiles[k],_cpuName);
						if(deapPath != null) {
							deapPath = deapPath.replace("\\", "/");	
							String linkString = deapPath.replace(didePath+"djysrc", "${DJYOS_SRC_LOCATION}").replace(_cpuName, "")+"src/"+componentName;
							myLinks.add(linkString);
						}
					}
				}

			}
		}
		
		for(String include:includes) {
			myLinks.add("${DJYOS_SRC_LOCATION}"+include);
		}
		
		ICLanguageSetting[] languageSettings = getLangSetting(rds);
		for (int j=0; j<languageSettings.length; j++) {
			//Assembly添加链接
			if(j==0) {
				ICLanguageSetting lang = languageSettings[j];//获取语言类型
//				ICLanguageSettingEntry[] ents = null;
				List<ICLanguageSettingEntry>  entries = new ArrayList<ICLanguageSettingEntry>();
				List<ICLanguageSettingEntry>  _entries = new ArrayList<ICLanguageSettingEntry>();
				for(int k=0;k<assemblyLinks.size();k++) {
					ICLanguageSettingEntry entry = CDataUtil.createCIncludePathEntry(assemblyLinks.get(k), 0);
					entries.add(entry);
				}
				fillSymbols(compontentsChecked,_entries);
				changeIt(ICSettingEntry.INCLUDE_PATH,entries,lang.getSettingEntries(ICSettingEntry.INCLUDE_PATH),lang);
				changeIt(ICSettingEntry.MACRO,_entries,lang.getSettingEntries(ICSettingEntry.MACRO),lang);
			}
			//GNU C/C++ 添加链接
			else {
				ICLanguageSetting lang = languageSettings[j];
				ICLanguageSettingEntry[] ents = null;
				List<ICLanguageSettingEntry>  entries = new ArrayList<ICLanguageSettingEntry>();
				List<ICLanguageSettingEntry>  _entries = new ArrayList<ICLanguageSettingEntry>();
				for(int k=0;k<myLinks.size();k++) {
					ICLanguageSettingEntry entry = CDataUtil.createCIncludePathEntry(myLinks.get(k), 0);
					entries.add(entry);
				}
				fillSymbols(compontentsChecked,_entries);
				changeIt(ICSettingEntry.INCLUDE_PATH,entries,lang.getSettingEntries(ICSettingEntry.INCLUDE_PATH),lang);
				changeIt(ICSettingEntry.MACRO,_entries,lang.getSettingEntries(ICSettingEntry.MACRO),lang);
			}			
		}
		
	}

	private void fillSymbols(List<Component> compontentsChecked, List<ICLanguageSettingEntry> entries) {
		// TODO Auto-generated method stub
		for(int j=0;j<compontentsChecked.size();j++) {
			Component component = compontentsChecked.get(j);
			if(component.getTarget().equals(ConfigureTarget.CMDLINE.getName())) {
				String[] parametersDefined = component.getConfigure().split("\n");
				for (int i = 0; i < parametersDefined.length; i++) {
					if (parametersDefined[i].contains("#define")) {
						String parameter = parametersDefined[i];
						String[] defines = parameter.trim().split("//");
						String[] members = defines[0].trim().split("\\s+");
						ICLanguageSettingEntry entry = CDataUtil.createCMacroEntry(members[1], members.length>2?members[2]:"", 0);
						entries.add(entry);
					}
				}
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
	
	/*
	 * 获取用户配置的momory信息
	 */
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
		lastProjectLocation = null;
	}

	@Override
	public boolean performFinish() {
		
		Cpu cpu = fMainPage.getSelectCpu();
		Board board = fMainPage.getSelectBoard();
		Core core = fMainPage.getSelectCore();
		String projectPath = fMainPage.locationArea.locationPathField.getText();
		
    	String ldsHead = fMainPage.getLdsHead();
    	String ldsDesc = fMainPage.getLdsDesc();
    	String projectName = fMainPage.getProjectName();
		String sourcePath = fMainPage.projectPath;
		
		int index = fMainPage.getTemplateIndex();
		boolean containsPrj = projectPath.contains(projectName);
		String srcPath = containsPrj?projectPath+"/src":projectPath+"/"+projectName+"/src";
//    	String initCPath = containsPrj?projectPath+"/src/app":projectPath+"/"+projectName+"/src/app";
//    	String initCPathIboot = containsPrj?projectPath+"/src/iboot": projectPath+"/"+projectName+"/src/iboot";
    	List<Component> compontentsChecked = new ArrayList<Component>();
    	List<Component> appCompontentsChecked = cpomtCfgPage.getAppCompontentsChecked();
    	List<Component> ibootCompontentsChecked = cpomtCfgPage.getIbootCompontentsChecked();
//    	if(appCompontentsChecked!=null) {
//    		for(Component component:appCompontentsChecked) {
//        		if(! compontentsChecked.contains(component)) {
//        			compontentsChecked.add(component);
//        		}
//        	}
//    	}
//    	if(ibootCompontentsChecked!=null) {
//    		for(Component component:ibootCompontentsChecked) {
//        		if(! compontentsChecked.contains(component)) {
//        			compontentsChecked.add(component);
//        		}
//        	}
//    	}
    	
    	IRunnableWithProgress runnable = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
				monitor.beginTask("Project Creating...", 10);			
				//生成initPrj.c,initPrj.h,memory.lds文件
					if(index == 0 || index == 1){
						cpomtCfgPage.initProject(srcPath,false);
						cpomtCfgPage.creatProjectConfiure(srcPath, core, false);
//						ibootCfgPage.initProject(initCPathIboot);
//						ibootCfgPage.creatProjectConfiure(initCPathIboot+"/OS_prjcfg/project_config.h",core);
			    	}
			    	if(index == 0 || index == 2 || index == 3){
			    		cpomtCfgPage.initProject(srcPath,true);
			    		cpomtCfgPage.creatProjectConfiure(srcPath, core, true);
//			    		appCfgPage.initProject(initCPath);
//			    		appCfgPage.creatProjectConfiure(initCPath+"/OS_prjcfg/project_config.h",core);
			    	}

				monitor.worked(7);
				//处理工程的链接
				handleCProject(appCompontentsChecked,ibootCompontentsChecked,board,cpu,core,projectPath,projectName,index);
			
				monitor.worked(2);
				//根据MemoryMap配置的内容添加memory.lds文件
				try {
					getMemoryToLds(ldsHead,ldsDesc,projectName,sourcePath);
				} catch (Exception e) {
					// TODO: handle exception
					MessageDialog.openInformation(window.getShell(), "提示",
							"lds配置错误："+e.getMessage());
				}
				
				monitor.worked(10);
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
		}

//    	memoryPage.createMemoryMap(projectPath+"/data/MemoryMap.xml");//根据MemoryMap配置的内容添加memoryMap.xml文件
    	
//    	ICConfigurationDescription[] cfgDescs = getCfgs(curProject);
//		for (ICConfigurationDescription cfgDesc : cfgDescs) {
//			System.out.println("cfgDesc.getName()"+cfgDesc.getName());
//		}
//    	
//    	createBuild(curProject);
		try {
			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IProject project = workspace.getRoot().getProject(projectName);
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
//		int index = fMainPage.getTemplateIndex();
		if (clickedMianNext) {
			return true;
		}
		return false;
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
	
	//编译排除
	private void setExclude(IFolder ifle, ICConfigurationDescription cfg, boolean exclude) {
		try {
			ICSourceEntry[] newEntries = CDataUtil.setExcluded(ifle.getFullPath(), (ifle instanceof IFolder), exclude, cfg.getSourceEntries());
			cfg.setSourceEntries(newEntries);
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
	}
	
	private void getFolders(IFolder ifle,List<IFolder> folders){
		folders.add(ifle);
		IFolder parentFolder = (IFolder) ifle.getParent();
		if(!parentFolder.getName().equals("libos")) {
			getFolders(parentFolder,folders);
		}
	}

	@Override
	public boolean isPageDragable() {
		// TODO Auto-generated method stub
		return false;
	} 
	
}
