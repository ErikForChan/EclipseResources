package org.eclipse.cdt.ui.wizards.djyosProject;

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
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
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
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICMultiFolderDescription;
import org.eclipse.cdt.core.settings.model.ICMultiResourceDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
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
import org.eclipse.cdt.ui.wizards.cpu.Cpu;
import org.eclipse.cdt.ui.wizards.cpu.core.Core;
import org.eclipse.cdt.ui.wizards.parsexml.ReviseLinkToXML;
import org.eclipse.cdt.ui.wizards.parsexml.ReviseVariableToXML;

import org.eclipse.cdt.internal.ui.newui.Messages;
import org.eclipse.cdt.internal.ui.wizards.ICDTCommonProjectWizard;

public abstract class DjyosCommonProjectWizard extends BasicNewResourceWizard
{
	private static final String PREFIX= "CProjectWizard"; //$NON-NLS-1$
	private static final String OP_ERROR= "CProjectWizard.op_error"; //$NON-5NLS-1$
	private static final String title= CUIPlugin.getResourceString(OP_ERROR + ".title"); //$NON-NLS-1$
	private static final String message= CUIPlugin.getResourceString(OP_ERROR + ".message"); //$NON-NLS-1$
	private static final String[] EMPTY_ARR = new String[0];

	boolean addedMemory = false;
	boolean addedInit = false;
	boolean createdProject = false;
	boolean projectExist = false;
	protected IConfigurationElement fConfigElement;
	protected DjyosMainWizardPage fMainPage;//������
	protected MemoryMapWizard memoryPage;//Memory�򵼽���
	protected ModuleConfigurationWizard modulePage;//Module�򵼽���
	protected InitDjyosProjectWizard initPage;//������ý���
	
	protected IProject newProject;
	private String wz_title;
	private String wz_desc;

	private boolean existingPath = false;
	private String lastProjectName = null;
	private URI lastProjectLocation = null;
	private CWizardHandler savedHandler = null;
	private WorkingSetGroup workingSetGroup;
	String boardModuleTrimPath;

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
//		initPage = new InitDjyosProjectWizard("basicModuleCfgPage");
//		initPage.setTitle("Init Project");
//		initPage.setDescription("Init the project you are creating");
		addPage(fMainPage);
	}

	/*
	 * ��ȡ�û�ѡ��ģ�����Ϣ
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
	 * ��ȡ��ǰEclipse��·��
	 */
	public String getEclipsePath() {
		String fullPath = Platform.getInstallLocation().getURL().toString();
		String eclipsePath = fullPath.substring(6,(fullPath.substring(0,fullPath.length()-1)).lastIndexOf("/")+1);
		return eclipsePath;
	}
	
	/*
	 * ��������
	 */
	public void importTemplate(String projectPath) {
		
		String projectName = fMainPage.getProjectName();
		String templateName = getTemplateName();
		String srcPath = eclipsePath + "demo/" + templateName;
		String destPath = fMainPage.locationArea.locationPathField.getText();
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
				
			projectExist = false;
			createdProject = true;
						
		}else {
			projectExist = true;
			fMainPage.setErrorMessage("Project is aready existed");
		}		
			
	}
	
	/*
	 * ����ģ�幤�̵���ǰ�����ռ�
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
	 * ��������ǰ����
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
	
	private void changeIt(List<ICLanguageSettingEntry>  entries, ICLanguageSettingEntry[] ents, ICLanguageSetting lang) {
		List<ICLanguageSettingEntry> lsEntries = new ArrayList<ICLanguageSettingEntry>();
		for(int i=0;i<ents.length;i++) {
			lsEntries.add(ents[i]);
		}
		if (entries != null) {
			for(int i=0;i<entries.size();i++) {
				lsEntries.add(entries.get(i));
			}
		}
		setSettingEntries(1, lsEntries, false, lang);
	}
	
	private void setSettingEntries(int kind, List<ICLanguageSettingEntry> incs, boolean toAll, ICLanguageSetting lang) {
			lang.setSettingEntries(kind, incs);//ICLanguageSetting
	}
	
	private String getCpuSrcPath(String cpuName) {
		String sourcePath = eclipsePath+"djysrc/bsp/cpudrv";
		File sourceFile = new File(sourcePath);
		File[] files = sourceFile.listFiles();
		String path = null;
		for(File file:files){//cpudrv�µ������ļ� Atmel stm32
			if(file.isDirectory()) {
				getDeapPath(file,cpuName);
				if(deapPath!=null) {
					path = deapPath+"/../src";
					break;
				}
			}
			
		}
		return path;
	}
	
	private String deapPath = null;
	
	public String getDeapPath(File sourceFile,String cpuName) {
		File[] files = sourceFile.listFiles();
		String path = null;
		for (File file : files) {
			if (file.isDirectory()) {
				if(file.getName().equals(cpuName)) {
					deapPath = file.getPath();
					break;
				}else if(!file.getName().equals("include") && !file.getName().equals("src")){
					getDeapPath(file,cpuName);//���ΪĿ¼�������ɨ���Ŀ¼
				}			
			}
		}
		return deapPath;
	}
	
	/*
	 * �޸Ĺ��̵�������Ϣ��ͨ���޸�.cproject
	 */
	public void handleCProject(List<Component> compontentsChecked,Board board,Cpu cpu,Core core,String projectPath,String projectName) {
		String _boardName = board.getBoardName();
		String _cpuName = cpu.getCpuName();
		String firmwareLib = cpu.getFirmwareLib();
		String cpudrvPath = eclipsePath+"djysrc/bsp/cpudrv";
		String boardPath = eclipsePath+"djysrc/bsp/boarddrv";
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
		
		final ICProjectDescription local_prjd =  CoreModel.getDefault().getProjectDescription(project);
		ICConfigurationDescription[] conds = local_prjd.getConfigurations();	//��ȡ���̵�����Configuration
		//�޸ı���ѡ�������
    	for (ICConfigurationDescription cfgDesc : conds) {
			String s = cfgDesc.getName();
			if(s.equals("Debug") || s.equals("Release")) {
				cfgDesc.setName(projectName+"_"+s);
			}else if(s.equals("libos_demo_o0")) {
				cfgDesc.setName("libos_Debug");
			}else if(s.equals("libos_demo_o2")) {
				cfgDesc.setName("libos_Release");
			}
		}
    	
    	//��ÿ��Configuration�޸�����,��������
		for(int i=0;i<conds.length;i++) {
			ICResourceDescription rds = conds[i].getRootFolderDescription();
			IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(rds.getConfiguration());
			IResourceInfo resourceInfo = cfg.getRootFolderInfo();
			IToolChain toolchain = resourceInfo.getParent().getToolChain();
			ICSourceEntry[] sourceEntrys = conds[i].getSourceEntries();
			for(int j=0;j<sourceEntrys.length;j++) {
				IPath[] paths = sourceEntrys[j].getExclusionPatterns();
				for(int k=0;k<paths.length;k++) {
					System.out.println("paths:  "+paths[k]);
				}
			}
			List<String> links = new ArrayList<String>();
			List<String> assemblyLinks = new ArrayList<String>();
			//���ݼܹ�����ѡ������
			if(core.getType().equals("arm")) {
				links.add("${DJYOS_SRC_LOCATION}/third/firmware/CMSIS/include");
				links.add("${DJYOS_SRC_LOCATION}/bsp/arch/arm");
				assemblyLinks.add("${DJYOS_SRC_LOCATION}/bsp/arch/arm");
			}
			//����cpu����
			List<String> cpuLinkStrings = new ArrayList<String>();
			for(int k=0;k<cpudrvFiles.length;k++) {//cpudrv�µ������ļ� Atmel stm32
				if(_cpuName.contains(cpudrvFiles[k].getName().toUpperCase())) {
					cpuLinkStrings = getCpuLinkStrings(cpudrvFiles[k],_cpuName,cpuLinkStrings);
					break;
				}
			}
			if(cpuLinkStrings!=null) {
				for(int k=0;k<cpuLinkStrings.size();k++) {
					links.add(cpuLinkStrings.get(k).replace("\\", "/"));
				}
			}
			//���ݰ��������
			if(isDemoBoard){
				links.add("${DJYOS_SRC_LOCATION}/bsp/boarddrv/demo/"+_boardName);
				links.add("${DJYOS_SRC_LOCATION}/bsp/boarddrv/demo/"+_boardName+"/include");
				links.add("${DJYOS_SRC_LOCATION}/bsp/boarddrv/demo/"+_boardName+"/startup");
			}else {
				links.add("${DJYOS_SRC_LOCATION}/bsp/boarddrv/user/"+_boardName);
				links.add("${DJYOS_SRC_LOCATION}/bsp/boarddrv/user/"+_boardName+"/include");
				links.add("${DJYOS_SRC_LOCATION}/bsp/boarddrv/user/"+_boardName+"/startup");
			}

//			//����Cpu�Ĺ̼�������
//			//�鿴
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
			//�����ں����͡��ܹ�����������
			links.add("${DJYOS_SRC_LOCATION}/bsp/arch/"+core.getType()+"/"+core.getArch()+"/"+core.getFamily()+"/include");
			assemblyLinks.add("${DJYOS_SRC_LOCATION}/bsp/arch/"+core.getType()+"/"+core.getArch()+"/"+core.getFamily()+"/include");
			//������ѡ�������
			for(int j=0;j<compontentsChecked.size();j++) {
				Component component = compontentsChecked.get(j);
				String componentName = compontentsChecked.get(j).getName();
				if(component.getLinkFolder().equals("third")) {
					links.add("${DJYOS_SRC_LOCATION}/third/"+componentName);
				}else if(component.getLinkFolder().equals("component")) {
					links.add("${DJYOS_SRC_LOCATION}/component/"+componentName);
				}else if(component.getLinkFolder().equals("djyos")) {
					links.add("${DJYOS_SRC_LOCATION}/djyos/"+componentName);
				}else if(component.getLinkFolder().equals("chipdrv")) {
					links.add("${DJYOS_SRC_LOCATION}/bsp/chipdrv/"+componentName);
				}else if(component.getLinkFolder().equals("cpudrv")) {
					for(int k=0;k<cpudrvFiles.length;k++) {
						deapPath = null;
						getDeapPath(cpudrvFiles[k],_cpuName);
						if(deapPath != null) {
							deapPath = deapPath.replace("\\", "/");
							String linkString = deapPath.replace(eclipsePath+"djysrc", "${DJYOS_SRC_LOCATION}").replace("/"+_cpuName, "")+"/src/"+componentName;
							links.add(linkString);
						}
						
					}

				}
			}
			
			ICLanguageSetting[] languageSettings = getLangSetting(rds);
			for (int j=0; j<languageSettings.length; j++) {
				//Assembly�������
				if(j==0) {
					ICLanguageSetting lang = languageSettings[j];//��ȡ��������
					ICLanguageSettingEntry[] ents = null;
					List<ICLanguageSettingEntry>  entries = new ArrayList<ICLanguageSettingEntry>();
					for(int k=0;k<assemblyLinks.size();k++) {
						ICLanguageSettingEntry entry = CDataUtil.createCIncludePathEntry(assemblyLinks.get(k), 0);
						entries.add(entry);
					}
					ents = lang.getSettingEntries(1);
					changeIt(entries,ents,lang);
				}
				//GNU C/C++ �������
				else {
					ICLanguageSetting lang = languageSettings[j];
					ICLanguageSettingEntry[] ents = null;
					List<ICLanguageSettingEntry>  entries = new ArrayList<ICLanguageSettingEntry>();
					for(int k=0;k<links.size();k++) {
						ICLanguageSettingEntry entry = CDataUtil.createCIncludePathEntry(links.get(k), 0);
						entries.add(entry);
					}
					ents = lang.getSettingEntries(1);
					changeIt(entries,ents,lang);
				}			
			}
			IOption option1 = toolchain.getOptionBySuperClassId("ilg.gnuarmeclipse.managedbuild.cross.option.arm.target.architecture");
			IOption option2 = toolchain.getOptionBySuperClassId("ilg.gnuarmeclipse.managedbuild.cross.option.arm.target.family");        				
			IOption option3 = toolchain.getOptionBySuperClassId("ilg.gnuarmeclipse.managedbuild.cross.option.arm.target.fpu.abi");
			IOption option4 = toolchain.getOptionBySuperClassId("ilg.gnuarmeclipse.managedbuild.cross.option.arm.target.fpu.unit");
			try {
				option1.setValue(
						"ilg.gnuarmeclipse.managedbuild.cross.option.arm.target.arch."+core.getArch());
				option2.setValue(
						"ilg.gnuarmeclipse.managedbuild.cross.option.arm.target.mcpu."+core.getFamily());
				option3.setValue(
						"ilg.gnuarmeclipse.managedbuild.cross.option.arm.target.fpu.unit."+core.getFloatABI());
				option4.setValue(
						"ilg.gnuarmeclipse.managedbuild.cross.option.arm.target.fpu.unit."+core.getFpuType().replace("-", ""));
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
		if(isDemoBoard) {
			rltx.reviseXmlLink("src/libos/bsp/boarddrv",board.getBoardName(), "DJYOS_SRC_LOCATION/bsp/boarddrv","/demo", 
					project.getFile(".project"),"boarddrv");
			rltx.reviseXmlLink("src/libos/bsp/startup",board.getBoardName(), "DJYOS_SRC_LOCATION/bsp/startup","/demo", 
					project.getFile(".project"),"startup");
		}else {
			rltx.reviseXmlLink("src/libos/bsp/boarddrv",board.getBoardName(), "DJYOS_SRC_LOCATION/bsp/boarddrv","/user", 
					project.getFile(".project"),"boarddrv");
			rltx.reviseXmlLink("src/libos/bsp/startup",board.getBoardName(), "DJYOS_SRC_LOCATION/bsp/startup","/user", 
					project.getFile(".project"),"startup");
		}
		//�޸Ĺ̼�������
		if(firmwareLib!=null) {
			rltx.reviseXmlLink("src/libos/third/stm32f4",firmwareLib, "DJYOS_SRC_LOCATION/third/firmware",null, 
					project.getFile(".project"),"firmware");
		}
		//�޸�family������
		String type = core.getType();
		String arch = core.getArch();
		String family = core.getFamily();
		rltx.reviseXmlLink("src/libos/bsp/arch",family, "DJYOS_SRC_LOCATION/bsp/arch/",type+"/"+arch+"/"+family, 
				project.getFile(".project"),"family");

		// �޸�cpudrv������
		String cpuLinkString = "";

		deapPath = null;
		getCpuSrcPath(_cpuName);
		if (deapPath != null) {
			deapPath = deapPath.replace("\\", "/");
			cpuLinkString = deapPath.replace(eclipsePath + "djysrc/bsp/cpudrv/", "");
			rltx.reviseXmlLink("src/libos/bsp/cpudrv", _cpuName, "DJYOS_SRC_LOCATION/bsp/cpudrv",
					cpuLinkString.replace("/" + _cpuName, ""), project.getFile(".project"), "cpudrv");
		}

		//��M�����link�
		LinkProjectFile lpf = new LinkProjectFile();
		for(int j=0;j<compontentsChecked.size();j++){
			Component component = compontentsChecked.get(j);
			String componentName = compontentsChecked.get(j).getName();
			String componentPath = compontentsChecked.get(j).getFileName();
			if(component.getLinkFolder().equals("third")) {
				lpf.addLink(project.getFile(".project"), componentName,componentPath, "third");				
			}else if(component.getLinkFolder().equals("component")) {
				lpf.addLink(project.getFile(".project"), componentName,componentPath, "component");
			}else if(component.getLinkFolder().equals("chipdrv")) {
				lpf.addLink(project.getFile(".project"),componentName, componentPath, "chipdrv");
			}else if(component.getLinkFolder().equals("cpudrv")) {
				String filePath = component.getFileName();
				String cptName = filePath.substring(filePath.lastIndexOf("\\")+1, filePath.length());
				lpf.addLink(project.getFile(".project"),componentName, cpuLinkString.replace("/"+_cpuName, "")+"/src/"+cptName, "cpudrv");
				//DJYOS_SRC_LOCATION\bsp\cpudrv\ stm32\f7
			}
		}
		
		
	}

	private List<String> getCpuLinkStrings(File file, String _cpuName, List<String> cpuLinkStrings) {
		// TODO Auto-generated method stub
		File[] files = file.listFiles();
		String DJYOS_SRC_LOCARION = eclipsePath+"djysrc";
		for(int j=0;j<files.length;j++) {
			String path = files[j].getPath();
			String relativePath = path.substring(DJYOS_SRC_LOCARION.length(), path.length());
			if(_cpuName.endsWith(files[j].getName().toUpperCase())) {
				cpuLinkStrings.add("${DJYOS_SRC_LOCATION}"+relativePath+"/include");
			}else if(_cpuName.contains(files[j].getName().toUpperCase())){
				getCpuLinkStrings(files[j],_cpuName,cpuLinkStrings);
			}else if(files[j].getName().equals("include")) {
				cpuLinkStrings.add("${DJYOS_SRC_LOCATION}"+relativePath);
			}
		}
		return cpuLinkStrings;
	}
	
	private String getCpuLinkString(File file, String _cpuName, String cpuLinkString) {
		// TODO Auto-generated method stub
		File[] files = file.listFiles();
		String CPUDRV_LOCARION = eclipsePath+"djysrc/cpudrv/";
		for(int j=0;j<files.length;j++) {
			String path = files[j].getPath();
			String relativePath = path.substring(CPUDRV_LOCARION.length()+4, path.length());
			System.out.println("relativePath:  "+relativePath);
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
		String DJYOS_SRC_LOCARION = eclipsePath+"djysrc";
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

	/*
	 * �������̵�����һ��Ŀ¼
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
	            // �ݹ鸴��  
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
	 * ����meomory.lds�ļ�
	 */
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

		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	/*
	 * ����moduletrim.c�ļ�
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
	 * ��ȡ�û����õ�momory��Ϣ
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
	
	private void init_projectConfig(String path,Core core) {
		String defineInit = "";
		File file = new File(path);
		if(file.exists()) {
			file.delete();	
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		defineInit += "#ifndef __PROJECT_CONFFIG_H__\r\n" + "#define __PROJECT_CONFFIG_H__\r\n\n"
				+ "#include \"stdint.h\"\n\n";
		defineInit += String.format("%-9s", "#define")+String.format("%-32s","#define CFG_CORE_MCLK")+String.format("%-18s", "("+core.getCoreClk()+"*Mhz)")+"//��Ƶ���ں�Ҫ�ã����붨��";
		defineInit += "\n#endif";
		
		FileWriter writer;
		try {
			writer = new FileWriter(path);
			writer.write(defineInit);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
		
//		String sourcePath = ResourcesPlugin.getWorkspace().getRoot().getLocationURI().toString().substring(6)+"/"+projectName;
		int index = fMainPage.getTemplateIndex();
//    	String path = projectPath+"/src/app/OS_prjcfg/cfg/moduleinit.h";
//    	String pathIboot = projectPath+"/src/iboot/OS_prjcfg/cfg/moduleinit.h";
		boolean containsPrj = projectPath.contains(projectName);
    	String initCPath = containsPrj?initCPath = projectPath+"/src/app":projectPath+"/"+projectName+"/src/app";
    	String initCPathIboot = containsPrj?projectPath+"/src/iboot": projectPath+"/"+projectName+"/src/iboot";
    	List<Component> compontentsChecked = initPage.getCompontentsChecked();
    	
    	try {
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Project Creating...", 100);
					//�����̵�����
					handleCProject(compontentsChecked,board,cpu,core,projectPath,projectName);
					monitor.worked(70);
					//����MemoryMap���õ��������memory.lds�ļ�
					getMemoryToLds(ldsHead,ldsDesc,projectName,sourcePath);
					monitor.worked(20);
					//����initPrj.c,initPrj.h,memory.lds�ļ�
					if(index == 0 || index == 1){
			    		initPage.initProject(initCPathIboot);
			    		init_projectConfig(initCPathIboot+"/OS_prjcfg/cfg/project_config.h",core);
			    	}
					monitor.worked(5);
			    	if(index == 0 || index == 2 || index == 3){
			    		initPage.initProject(initCPath);
			    		init_projectConfig(initCPath+"/OS_prjcfg/cfg/project_config.h",core);
			    	}
					monitor.worked(5);
					monitor.done();
				}
			};
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(
					PlatformUI.getWorkbench().getDisplay().getActiveShell());
			dialog.setCancelable(false);
			dialog.run(true, true, runnable);
		} catch (Exception e) {
			e.printStackTrace();
		}

//    	memoryPage.createMemoryMap(projectPath+"/data/MemoryMap.xml");//����MemoryMap���õ��������memoryMap.xml�ļ�
    	
//    	ICConfigurationDescription[] cfgDescs = getCfgs(curProject);
//		for (ICConfigurationDescription cfgDesc : cfgDescs) {
//			System.out.println("cfgDesc.getName()"+cfgDesc.getName());
//		}
//    	
//    	createBuild(curProject);
		
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
     * ɾ���½��Ĺ���
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
    	if(addedInit && !projectExist) {
    			return true;
    	}else {
    		return false;
    	}
    }
    
    /*
     * ��ȡ��ǰ���̵�����������
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
