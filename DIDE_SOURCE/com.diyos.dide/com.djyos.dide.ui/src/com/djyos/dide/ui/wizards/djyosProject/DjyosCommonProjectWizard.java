package com.djyos.dide.ui.wizards.djyosProject;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

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
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
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
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.helper.LinkHelper;
import com.djyos.dide.ui.objects.Arch;
import com.djyos.dide.ui.objects.Board;
import com.djyos.dide.ui.objects.Component;
import com.djyos.dide.ui.objects.Core;
import com.djyos.dide.ui.objects.Cpu;
import com.djyos.dide.ui.objects.OnBoardCpu;
import com.djyos.dide.ui.wizards.board.ReadBoardXml;
import com.djyos.dide.ui.wizards.component.GetNonCompFiles;
import com.djyos.dide.ui.wizards.component.ReadComponent;
import com.djyos.dide.ui.wizards.cpu.GetNonCpuFiles;
import com.djyos.dide.ui.wizards.cpu.ReadCpuXml;
import com.djyos.dide.ui.wizards.djyosProject.info.CreateBoardInfo;
import com.djyos.dide.ui.wizards.djyosProject.info.CreateComponentInfo;
import com.djyos.dide.ui.wizards.djyosProject.info.CreateCpuInfo;
import com.djyos.dide.ui.wizards.djyosProject.tools.FileTool;
import com.djyos.dide.ui.wizards.djyosProject.tools.IncludesLinkTool;
import com.djyos.dide.ui.wizards.djyosProject.tools.PathTool;
import com.djyos.dide.ui.wizards.djyosProject.tools.ProjectPattern;
import com.djyos.dide.ui.wizards.djyosProject.tools.ReviseVariableToXML;

public abstract class DjyosCommonProjectWizard extends BasicNewResourceWizard {
	private static final String PREFIX = "CProjectWizard"; //$NON-NLS-1$
	boolean addedMemory = false, createdProject = false, clickedMianNext = false, existingPath = false,
			projectExist = false, addedComptCfg = false;
	protected DjyosMainWizardPage fMainPage;// 主界面
	protected ComponentConfigWizard cpomtCfgPage = null;// 组件配置界面
	private String wz_title, wz_desc;
	private String didePath = PathTool.getDIDEPath();
	String srcLocation = PathTool.getDjyosSrcPath();

	private IProject curProject;
	protected IProject newProject;

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

	/*
	 * 获取用户选中模板的信息
	 */
	public String getTemplateName() {
		int tIndex = fMainPage.getTemplateIndex();
		String[] templateNames = {"ibootapp","iboot","App","Apponly"};
		return templateNames[tIndex];
	}

	public boolean importProject(String projectPath, Board selectedBoard, Core selectedCore, boolean haveApp,
			boolean needIbootLds, int tIndex) {

		String projectName = fMainPage.getProjectName();// 用户填写的工程名
		String templateName = getTemplateName();// 用户选择的模板
		String srcPath = PathTool.getTemplatePath() + "/"
				+ selectedCore.getArch().getToolchain().replaceAll("\\s+", "-") + "/" + templateName;// 模板的路径
		String userPath = projectPath;
		if (!projectPath.contains(projectName)) {
			projectPath = projectPath + "/" + projectName;
		}
		String destPath = projectPath;
		File srcFile = new File(srcPath);
		File destFile = new File(destPath);

		if (!srcFile.exists()) {
			DideHelper.showErrorMessage("系统不支持 [" + templateName + "] 类型的工程，请联系开发人员!");
			return false;
		} else {
			if (!destFile.exists()) {
				destFile.mkdir();
				DideHelper.copyFolder(srcFile, destFile);
				WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
					@Override
					protected void execute(IProgressMonitor monitor)
							throws InvocationTargetException, InterruptedException {
						SubMonitor subMonitor = SubMonitor.convert(monitor, 1);
						// Import as many projects as we can; accumulate errors to
						// report to the user
						@SuppressWarnings("restriction")
						MultiStatus status = new MultiStatus(IDEWorkbenchPlugin.IDE_WORKBENCH, 1,
								DataTransferMessages.WizardProjectsImportPage_projectsInWorkspaceAndInvalid, null);
						importExistingProject(subMonitor.split(1), projectName, userPath, destPath);
						if (!status.isOK()) {
							throw new InvocationTargetException(new CoreException(status));
						}

						projectExist = false;
						createdProject = true;
						String boardFolderPath = selectedBoard.getBoardFolderPath();
						fill_Lds(destPath, boardFolderPath, haveApp, needIbootLds, selectedCore, tIndex);
					}
				};

				try {
					getContainer().run(true, true, op);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					DideHelper.showErrorMessage("工程创建失败" + e.getMessage());
				}

			} else {
				projectExist = true;
				fMainPage.setErrorMessage("Project is aready existed");
				return false;
			}
		}

		return true;
	}

	private void fill_Lds(String destPath, String boardFolderPath, boolean haveApp, boolean needIbootLds, Core core, int tIndex) {
		// TODO Auto-generated method stub
		boardFolderPath += "/lds";
		if(core.getName() != null) {
			File coreLdsFolder = new File(boardFolderPath+"/"+core.getName());
			if(coreLdsFolder.exists()) {
				boardFolderPath = coreLdsFolder.getPath();
			}
		}
		File destLdsFile = new File(destPath + "/src/lds");
		if (!destLdsFile.exists()) {
			destLdsFile.mkdirs();
		}
		try {
			File memoryLdsFile = new File(boardFolderPath + "/memory.lds");
			File app_LdsFile = new File(boardFolderPath + "/app.lds");
			File iboot_LdsFile = new File(boardFolderPath + "/iboot.lds");
			File bare_LdsFile = new File(boardFolderPath + "/bare.lds");
			
			if(tIndex==0 || tIndex==1) {
				if(iboot_LdsFile.exists()) {
					DideHelper.copyFolder(iboot_LdsFile, new File(destPath + "/src/lds/iboot.lds"));
				}
			}
			if(tIndex==0 || tIndex==2) {
				if(app_LdsFile.exists()) {
					DideHelper.copyFolder(app_LdsFile, new File(destPath + "/src/lds/app.lds"));
				}
			}
			if(tIndex==3) {
				if(bare_LdsFile.exists()) {
					DideHelper.copyFolder(bare_LdsFile, new File(destPath + "/src/lds/app.lds"));
					
				}
			}
			if (memoryLdsFile.exists()) {
				DideHelper.copyFolder(memoryLdsFile, new File(destPath + "/src/lds/memory.lds"));
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/*
	 * 导入模板工程到当前工作空间
	 */
	@SuppressWarnings("restriction")
	private IStatus importExistingProject(IProgressMonitor mon, String projectName, String userPath, String destPath) {

		SubMonitor subMonitor = SubMonitor.convert(mon, 3);
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		
		IProject project = workspace.getRoot().getProject(projectName);
		if (!userPath.contains(projectName)) {
			IPath locationPath = new Path(destPath);
			IProjectDescription description = workspace.newProjectDescription(projectName);
			description.setLocation(locationPath);
			try {
				SubMonitor subTask = subMonitor.split(1).setWorkRemaining(100);
				subTask.setTaskName(DataTransferMessages.WizardProjectsImportPage_CreateProjectsTask);
				project.create(description, subTask.split(30));
				project.open(IResource.BACKGROUND_REFRESH, subTask.split(70));
				subTask.setTaskName(""); //$NON-NLS-1$
			} catch (OperationCanceledException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
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

	private boolean isDemoBoard(File boardDemoFile, File boardFolder) {
		boolean isDemoBoard = false;
		if (boardDemoFile.exists()) {
			File[] boardDemoFiles = boardDemoFile.listFiles();
			for (int j = 0; j < boardDemoFiles.length; j++) {
				if (boardDemoFiles[j].getName().equals(boardFolder.getName())) {
					isDemoBoard = true;
					break;
				}
			}
		}
		return isDemoBoard;
	}

	private void resetTargetName(ICConfigurationDescription[] conds, String projectName) {
		for (ICConfigurationDescription cfgDesc : conds) {
			String s = cfgDesc.getName();
			if (s.contains(DjyosMessages.Configuration_Debug) || s.contains(DjyosMessages.Configuration_Release)) {
				if (!s.contains("libos")) {
					cfgDesc.setName(projectName + "_" + s);
				}
			}
		}
	}

	private void handArchLinks(Core core, File archSourceFile, List<File> archXmlFiles, IProject project,
			ICConfigurationDescription[] conds) {
		// 添加family的链接
		String type = core.getArch().getSerie();// 架构类型
		String family = core.getArch().getMcpu();// 架构家族
		List<File> excludeArchFiles = new ArrayList<File>();
		List<File> stepArchFiles = new ArrayList<File>();
		File[] typeFiles = archSourceFile.listFiles();
		for (File f : typeFiles) {
			if (!f.getName().equals(type)) {
				excludeArchFiles.add(f);
			}
		}
		// 当前的Arch
		File curArchFile = null;
		for (File f : archXmlFiles) {
			if (f.getParentFile().getName().equals(family)) {
				curArchFile = f.getParentFile();
			} else {
				excludeArchFiles.add(f.getParentFile());
				stepArchFiles.add(f.getParentFile());
			}
		}
		getExcludeArchFiles(excludeArchFiles, stepArchFiles);
		// 排除所有未被选中的arch
		for (File f : excludeArchFiles) {
			setFolderExclude(f, project, conds, true);
		}

		// 释放当前的arch
		if (curArchFile != null) {
			IFolder archtectureFolder = project.getFolder(
					"src/libos" + curArchFile.getPath().replace("\\", "/").replace(PathTool.getDjyosSrcPath(), ""));
			List<IFolder> archtectureFolders = new ArrayList<IFolder>();
			getArchFolders(archtectureFolder, archtectureFolders);
			LinkHelper.setFoldersInclude(archtectureFolders, conds);
		}
	}

	public void handleCProject(int index, List<Component> appCompontentsChecked, List<Component> ibootCompontentsChecked,
			Board board, Cpu cpu, Core core, String projectPath, String projectName) {
		String _cpuName = cpu.getCpuName();
		File cpudrvFile = new File(didePath + DjyosMessages.Cpu_RelativePath);
		File boardDemoFile = new File(PathTool.getDemoBoardFilePath());
		File archSourceFile = new File(PathTool.getDjyosSrcPath() + "/bsp/arch");
		File boardFolder = new File(board.getBoardFolderPath());
		List<File> archXmlFiles = DideHelper.getArchXmlFiles(archSourceFile, new ArrayList<File>());
		File[] cpudrvFiles = cpudrvFile.listFiles();

		boolean isDemoBoard = isDemoBoard(boardDemoFile, boardFolder);
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (!projectPath.contains(projectName)) {
			project = curProject;
		}
		// 修改DJYOS_SRC_LOCATION
		ReviseVariableToXML rvtx = new ReviseVariableToXML();
		rvtx.reviseXmlVariable("DJYOS_SRC_LOCATION", "$%7BPARENT-1-ECLIPSE_HOME%7D/djysrc", project.getFile(".project"),
				projectName);
		File hardwareFile = new File(project.getLocation().toString() + "/data/hardwares");
		if (!hardwareFile.exists()) {
			hardwareFile.mkdirs();
		}

		List<String> includes = new ArrayList<String>();
		final ICProjectDescription local_prjd = CoreModel.getDefault().getProjectDescription(project);
		ICConfigurationDescription[] conds = local_prjd.getConfigurations(); // 获取工程的所有Configuration
		// local_prjd.setConfigurationRelations(ICProjectDescriptionPreferences.CONFIGS_LINK_SETTINGS_AND_ACTIVE);
		// 修改编译选项的名称
		resetTargetName(conds, projectName);

		// 开始添加链接
		handArchLinks(core, archSourceFile, archXmlFiles, project, conds);
		// 给每个Configuration修改配置,增加链接
		reviseConfiguration(index,conds,board,cpu,core,isDemoBoard,
				appCompontentsChecked, ibootCompontentsChecked, includes, srcLocation, cpudrvFiles);
		// 组件操作
		List<OnBoardCpu> onBoardCpus = board.getOnBoardCpus();
		OnBoardCpu onBoardCpu = DideHelper.getOnBoardByCpu(onBoardCpus, cpu.getCpuName());
		List<Component> compontentsList = ReadComponent.getAllComponents(cpu, board);
		File compInfoFile = new File(project.getLocation().toString() + "/data/hardwares/component_infos.xml");
		FileTool.createNewFile(compInfoFile);
		CreateComponentInfo.createComponentInfo(compInfoFile, compontentsList);
		
		// 排除所有没有描述文件的组件
		List<File> excludeCompFiles = GetNonCompFiles.getNonCompFiles(onBoardCpu, board);
		for (File f : excludeCompFiles) {
			String relativePath = PathTool.resetPath(f.getPath()).replace(srcLocation, "");
			if (f.isFile()) {
				IFile ifile = project.getFile("src/libos" + relativePath);
				for (int j = 0; j < conds.length; j++) {
					LinkHelper.setFileExclude(ifile, conds[j], true);
				}
			} else if (f.isDirectory()) {
				IFolder ifolder = project.getFolder("src/libos" + relativePath);
				for (int j = 0; j < conds.length; j++) {
					LinkHelper.setFolderExclude(ifolder, conds[j], true);
				}
			}
		}

		if(appCompontentsChecked.size() > 0) {
			linkComponentResource(true, appCompontentsChecked, compontentsList, srcLocation, project, conds);
		}
		if(ibootCompontentsChecked.size() > 0) {
			linkComponentResource(false, ibootCompontentsChecked, compontentsList, srcLocation, project, conds);
		}

		List<Board> allBoards = ReadBoardXml.getAllBoards();
		File boardInfoFile = new File(project.getLocation().toString() + "/data/hardwares/board_infos.xml");
		FileTool.createNewFile(boardInfoFile);
		CreateBoardInfo.createBoardInfo(boardInfoFile, allBoards);
		if (isDemoBoard) {
			setBoardExclude(true, boardFolder.getName(), project, conds);
		} else {
			setBoardExclude(false, boardFolder.getName(), project, conds);
		}

		List<Cpu> allCpus = ReadCpuXml.getAllCpus();
		// 保存所有的cpu信息
		File cpuInfoFile = new File(project.getLocation().toString() + "/data/hardwares/cpu_infos.xml");
		FileTool.createNewFile(cpuInfoFile);
		CreateCpuInfo.createCpuInfo(cpuInfoFile, allCpus);

		Cpu myCpu = DideHelper.getCpuByName(_cpuName);
		setCpuFilesExclude(project, conds, myCpu, allCpus);

		DideHelper.saveProjectDescription(project, local_prjd);
	}

	private void reviseConfiguration(int index, ICConfigurationDescription[] conds,Board board, Cpu cpu, Core core, 
			boolean isDemoBoard,List<Component> appCompontentsChecked,
			List<Component> ibootCompontentsChecked, List<String> includes, String srcLocation, File[] cpudrvFiles) {
		// TODO Auto-generated method stub
		for (int i = 0; i < conds.length; i++) {
			String conName = conds[i].getName();
			ICResourceDescription rds = conds[i].getRootFolderDescription();
			IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(rds.getConfiguration());
			IResourceInfo resourceInfo = cfg.getRootFolderInfo();
			String toolChainName = cfg.getToolChain().getName();
			ProjectPattern.handleBuilderPattern(toolChainName, cfg, conName);
			IToolChain toolchain = resourceInfo.getParent().getToolChain();
			
			boolean isApp = conName.contains("Iboot")?false:true;
			HashMap< String, List<String>> link_map = IncludesLinkTool.getCommonIncludeLinks(board, cpu, core, isApp);
			List<String> links = link_map.get("cIncludes");
			List<String> assemblyLinks = link_map.get("aIncludes");
			
			// 根据所选组件链接
			if (isApp) {
				linkComponentGUN(appCompontentsChecked, links, includes, isDemoBoard, cpudrvFiles, srcLocation,
						assemblyLinks, rds, core);
			} else{
				linkComponentGUN(ibootCompontentsChecked, links, includes, isDemoBoard, cpudrvFiles, srcLocation,
						assemblyLinks, rds, core);
			}
			reviseSettings(toolchain, core);

		}
	}

	private void reviseSettings(IToolChain toolchain, Core core) {
		// TODO Auto-generated method stub
		if (toolchain.getName().equals("Cross ARM GCC")) {
			IOption option1 = toolchain.getOptionBySuperClassId(DjyosMessages.Arch_SuperClassId);
			IOption option2 = toolchain.getOptionBySuperClassId(DjyosMessages.Family_SuperClassId);
			IOption option3 = toolchain.getOptionBySuperClassId(DjyosMessages.FpuABI_SuperClassId);
			IOption option4 = toolchain.getOptionBySuperClassId(DjyosMessages.FpuType_SuperClassId);
			
//			IOption optionScriptfile = toolchain.getOptionBySuperClassId("ilg.gnuarmeclipse.managedbuild.cross.option.cpp.linker.scriptfile");
//			ITool[] tools = toolchain.getToolsBySuperClassId("ilg.gnuarmeclipse.managedbuild.cross.tool.cpp.linker");
//			for(ITool t:tools) {
//				IOption op =  t.getOptionBySuperClassId("ilg.gnuarmeclipse.managedbuild.cross.option.cpp.linker.scriptfile");
//				try {
//					OptionStringValue[]  values = op.getBasicStringListValueElements();
//					op.ValueEl
//					for(OptionStringValue v:values) {
//						System.out.println("value:   "+v.getValue());
//					}
////					op.value
//					
//				} catch (BuildException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			try {
//				String[] values = optionScriptfile.getBasicStringListValue();
//				for(String v:values) {
//					System.out.println("value:   "+v);
//				}
////				optionScriptfile.value
//			} catch (BuildException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			try {
				option1.setValue(DjyosMessages.Arch_Prefix + core.getArch().getMarch());
				option2.setValue(DjyosMessages.Family_Prefix + core.getArch().getMcpu());
				boolean fpNeed = DideHelper.isFputypeuNeed(core);
				if (!fpNeed) {
					option3.setValue(DjyosMessages.FpuABI_Prefix + "soft");
					option4.setValue(DjyosMessages.FpuType_Prefix + "default");
				} else {
					option3.setValue(DjyosMessages.FpuABI_Prefix + "hard");
					option4.setValue(DjyosMessages.FpuType_Prefix + core.getFpuType().replace("-", ""));
				}

			} catch (BuildException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	private void getExcludeArchFiles(List<File> excludeArchFiles, List<File> stepArchFiles) {
		// TODO Auto-generated method stub
		List<File> tempArchFiles = new ArrayList<File>();
		// System.out.println("stepArchFile: "+stepArchFiles.get(0).getName());
		for (File f : stepArchFiles) {
			File parentFile = f.getParentFile();
			if (!tempArchFiles.contains(parentFile) && !parentFile.getName().equals("arch")) {
				tempArchFiles.add(parentFile);
			}
			if (!excludeArchFiles.contains(parentFile) && !parentFile.getName().equals("arch")) {
				excludeArchFiles.add(parentFile);
			}
		}
		if (tempArchFiles.size() > 0) {
			getExcludeArchFiles(excludeArchFiles, tempArchFiles);
		}
	}


	public void getArchFolders(IFolder ifolder, List<IFolder> folders) {
		folders.add(ifolder);
		IFolder parentFolder = (IFolder) ifolder.getParent();
		if (!parentFolder.getName().equals("arch")) {
			getArchFolders(parentFolder, folders);
		}
	}

	private void setCpuFilesExclude(IProject project, ICConfigurationDescription[] conds, Cpu myCpu,
			List<Cpu> allCpus) {
		// Exclude没有描述文件的的cpu
		GetNonCpuFiles gncf = new GetNonCpuFiles();
		List<File> excludeCpuFiles = gncf.getNonCpus();
		for (File f : excludeCpuFiles) {
			setFolderExclude(f, project, conds, true);
		}

		// Exclude不需要的cpu
		for (Cpu c : allCpus) {
			if (!c.getCpuName().equals(myCpu.getCpuName())) {
				String parentPath = c.getCpuFolderPath();
				File parentFile = new File(parentPath);
				travelCpuParentInclude(parentFile, project, conds);
				travelCpuParentExclude(parentFile, project, conds);
			}
		}
		
		// Include需要的cpu
		File myCpuParentFile = new File(myCpu.getCpuFolderPath());
		travelCpuParentInclude(myCpuParentFile, project, conds);
	}

	private void travelCpuParentExclude(File parentFile, IProject project, ICConfigurationDescription[] conds) {
		setFolderExclude(parentFile, project, conds, true);
		parentFile = parentFile.getParentFile();
		if (!parentFile.getName().equals("cpudrv")) {
			travelCpuParentExclude(parentFile, project, conds);
		}
	}

	private void travelCpuParentInclude(File file, IProject project, ICConfigurationDescription[] conds) {
		File parentFile = file.getParentFile();
		if (!parentFile.getName().equals("cpudrv")) {
			travelCpuParentInclude(parentFile, project, conds);
		}
		setFolderExclude(file, project, conds, false);
	}

	private void setBoardExclude(boolean isDemoBoard, String boardName, IProject project,
			ICConfigurationDescription[] conds) {
		// TODO Auto-generated method stub
		File boardDrvFile = new File(PathTool.getDjyosSrcPath() + "/bsp/boarddrv");
		File[] files = boardDrvFile.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				File[] myFiles = file.listFiles();
				String fileTag = isDemoBoard?"demo":"user";
				if (file.getName().equals(fileTag)) {
					for (File f : myFiles) {
						boolean exclude = f.getName().equals(boardName);
						setFolderExclude(f, project, conds, !exclude);
					}
				} else {
					for (File f : myFiles) {
						setFolderExclude(f, project, conds, true);
					}
				}
			}
		}
	}

	// Exclude某个文件夹
	private void setFolderExclude(File f, IProject project, ICConfigurationDescription[] conds, boolean exclude) {
		String filePath = f.getPath().toString().replace("\\", "/");
		String relativePath = filePath.replace(srcLocation, "");
		IFolder folder = project.getFolder("src/libos" + relativePath);
		for (int i = 0; i < conds.length; i++) {
			if (conds[i].getName().contains("libos")) {
				LinkHelper.setFolderExclude(folder, conds[i], exclude);
			}
		}
	}

	private void linkComponentResource(boolean isApp, List<Component> compontentsChecked,
			List<Component> compontentsList, String srcLocation, IProject project, ICConfigurationDescription[] conds) {
		List<String> excludes = new ArrayList<String>();
		List<Component> compontentsExclude = new ArrayList<Component>();
		getCompontentsExclude(compontentsExclude, compontentsChecked, compontentsList);
		for (int j = 0; j < compontentsExclude.size(); j++) {
			Component component = compontentsExclude.get(j);
			String componentPath = component.getParentPath().replace("\\", "/");
			String relativePath = componentPath.replace(srcLocation, "");
			IFolder folder = project.getFolder("src/libos" + relativePath);
			for (int i = 0; i < conds.length; i++) {
				if (conds[i].getName().contains(isApp?"libos_App":"libos_Iboot")) {
					LinkHelper.setFolderExclude(folder, conds[i], true);
				}
			}
		}

		
		// 隐藏不需要编译的文件
		for (int j = 0; j < compontentsChecked.size(); j++) {
			Component component = compontentsChecked.get(j);
			String componentPath = component.getParentPath().replace("\\", "/");
			String relativePath = componentPath.replace(srcLocation, "").replace("\\", "/");
			List<String> excludeFiles = component.getExcludes();
			for (String exclude : excludeFiles) {
				excludes.add("src/libos" + relativePath + exclude);
			}
		}

		for (int j = 0; j < excludes.size(); j++) {
			IFile ifle = project.getFile(excludes.get(j));
			for (int i = 0; i < conds.length; i++) {
				if (isApp) {
					if (conds[i].getName().contains("libos_App")) {
						LinkHelper.setFileExclude(ifle, conds[i], true);
					}
				} else {
					if (conds[i].getName().contains("libos_Iboot")) {
						LinkHelper.setFileExclude(ifle, conds[i], true);
					}
				}
			}
		}

	}

	private void getCompontentsExclude(List<Component> compontentsExclude, List<Component> compontentsChecked,
			List<Component> compontentsList) {
		// TODO Auto-generated method stub
		for (Component list : compontentsList) {
			boolean exist = false;
			for (Component checked : compontentsChecked) {
				if (checked.getParentPath().equals(list.getParentPath())) {
					exist = true;
					break;
				}
			}
			if (!exist) {
				compontentsExclude.add(list);
			}
		}
	}

	private void linkComponentGUN(List<Component> compontentsChecked, List<String> links, List<String> includes,
			boolean isDemoBoard, File[] cpudrvFiles, String srcLocation, List<String> assemblyLinks,
			ICResourceDescription rds, Core core) {
		// TODO Auto-generated method stub
		ICLanguageSetting[] languageSettings = LinkHelper.getLangSetting(rds);
		List<String> myLinks = new ArrayList<String>();
		myLinks.addAll(links);

		for (int j = 0; j < compontentsChecked.size(); j++) {
			Component component = compontentsChecked.get(j);
			List<String> includeFiles = component.getIncludes();// includes
			String componentPath = component.getParentPath().replace("\\", "/");
			String relativePath = componentPath.replace(srcLocation, "").replace("\\", "/");// \component\buffer
			for (String include : includeFiles) {
				includes.add(relativePath + include);
			}
			myLinks.add("${DJYOS_SRC_LOCATION}" + relativePath);
		}

		for (String include : includes) {
			myLinks.add("${DJYOS_SRC_LOCATION}" + include);
		}
		// MACRO Entries
		List<ICLanguageSettingEntry> _entries = new ArrayList<ICLanguageSettingEntry>();
		List<ICLanguageSettingEntry> entriesMACROExist = languageSettings[1]
				.getSettingEntriesList(ICSettingEntry.MACRO);
		LinkHelper.fillSymbols(compontentsChecked, _entries,entriesMACROExist);
		// 添加内核的宏
//		if(core.getName() !=null ) {
//			String coreMarco = "CFG_"+core.getName().replace("-", "_").toUpperCase();
//			_entries.add(CDataUtil.createCMacroEntry(coreMarco, "", 0));
//		}
		
		// C/C++ Entries
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		for (int k = 0; k < myLinks.size(); k++) {
			ICLanguageSettingEntry entry = CDataUtil.createCIncludePathEntry(myLinks.get(k), 0);
			entries.add(entry);
			if (myLinks.get(k).endsWith("include")) {
				assemblyLinks.add(myLinks.get(k));
			}
		}

		// Assembly Entries
		List<ICLanguageSettingEntry> assemblyEntries = new ArrayList<ICLanguageSettingEntry>();
		for (int k = 0; k < assemblyLinks.size(); k++) {
			ICLanguageSettingEntry entry = CDataUtil.createCIncludePathEntry(assemblyLinks.get(k), 0);
			assemblyEntries.add(entry);
		}

		for (int j = 0; j < languageSettings.length; j++) {
			ICLanguageSetting lang = languageSettings[j];// 获取语言类型
			LinkHelper.changeIt(ICSettingEntry.MACRO, _entries, lang.getSettingEntries(ICSettingEntry.MACRO), lang);
			// Assembly添加链接
			if (j == 0) {
				LinkHelper.changeIt(ICSettingEntry.INCLUDE_PATH, assemblyEntries,
						lang.getSettingEntries(ICSettingEntry.INCLUDE_PATH), lang);
			}
			// GNU C/C++ 添加链接
			else {
				LinkHelper.changeIt(ICSettingEntry.INCLUDE_PATH, entries,
						lang.getSettingEntries(ICSettingEntry.INCLUDE_PATH), lang);
			}
		}

	}


	public void getMemoryToLds(String ldsHead, String ldsDesc, String projectName, String sourcePath) {
		if (!sourcePath.contains(projectName)) {
			sourcePath = sourcePath + "/" + projectName;
		}
		File file = new File(sourcePath + "/src/lds/memory.lds");
		String content = ldsHead + ldsDesc;
		FileTool.createNewFile(file);
		FileTool.writeFile(file, content,false);
	}

	@Override
	public boolean performFinish() {

		Cpu cpu = fMainPage.getSelectCpu();
		Board board = fMainPage.getSelectBoard();
		Core core = fMainPage.getSelectCore();
		if (!check_Hardwares(board, cpu, core)) {
			return false;
		}

		@SuppressWarnings("restriction")
		String projectPath = fMainPage.locationArea.locationPathField.getText();

//		String ldsHead = fMainPage.getLdsHead();
//		String ldsDesc = fMainPage.getLdsDesc();
		String projectName = fMainPage.getProjectName();

		int index = fMainPage.getTemplateIndex();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (!projectPath.contains(projectName)) {
			project = curProject;
		}

		String projectLocation = project.getLocation().toString();
		File dataFolder = new File(projectLocation + "/data");
		if (!dataFolder.exists()) {
			dataFolder.mkdirs();
		}

		List<Component> appCompontentsChecked = cpomtCfgPage.getAppCompontentsChecked();
		List<Component> ibootCompontentsChecked = cpomtCfgPage.getIbootCompontentsChecked();
		String warningMsg = cpomtCfgPage.validateThirdCompt(appCompontentsChecked, true);
		if (warningMsg == null) {
			warningMsg = cpomtCfgPage.validateThirdCompt(ibootCompontentsChecked, false);
		}

		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask("配置工程...", 10);

				// set userActive to be true
				ICProjectDescriptionManager prjDescMgr = CCorePlugin.getDefault().getProjectDescriptionManager();
				ICProjectDescriptionWorkspacePreferences prefs = prjDescMgr
						.getProjectDescriptionWorkspacePreferences(true);
				prefs.setConfigurationRelations(ICProjectDescriptionPreferences.CONFIGS_LINK_SETTINGS_AND_ACTIVE);
				prjDescMgr.setProjectDescriptionWorkspacePreferences(prefs, false, new NullProgressMonitor());
				// 生成initPrj.c,initPrj.h,memory.lds文件
				monitor.setTaskName("配置工程初始化文件...");

				File hardWardInfoFile = new File(projectLocation + "/data/hardware_info.xml");
				FileTool.createNewFile(hardWardInfoFile);
				if(DideHelper.get_DIDE_Version() != null) {
					File versionFile = new File(projectLocation + "/data/version.ini");
					FileTool.createNewFile(versionFile);
					FileTool.writeFile(versionFile,"DIDE_VERSION=" + DideHelper.get_DIDE_Version(),false);
				}
				
				CreateHardWareDesc chwd = new CreateHardWareDesc();
				String coreName = DideHelper.getCoreName(core, cpu.getCores().indexOf(core));
				chwd.createHardWareXml(board.getBoardName(), cpu.getCpuName(),coreName, hardWardInfoFile);
				if (index == 0 || index == 1) {
					try {
						cpomtCfgPage.initProject(projectLocation, false);
						cpomtCfgPage.creatProjectConfiure(projectLocation, core, false, index);
					} catch (Exception e) {
						DideHelper.showErrorMessage("配置Iboot初始化文件错误：" + e.getMessage() + "   " + e.getLocalizedMessage()
								+ "\n" + e.getCause());
					}
				}
				if (index == 0 || index == 2 || index == 3) {
					try {
						cpomtCfgPage.initProject(projectLocation, true);
						cpomtCfgPage.creatProjectConfiure(projectLocation, core, true, index);
					} catch (Exception e) {
						DideHelper.showErrorMessage("配置App初始化文件错误：" + e.getMessage());
					}
				}
				monitor.worked(2);
				// 根据MemoryMap配置的内容添加memory.lds文件
				monitor.setTaskName("配置lds文件...");
				if (fMainPage._ibootSize != null) {
					// dideHelper.copyFolder(src, dest);
					// getMemoryToLds(ldsHead, ldsDesc, projectName, sourcePath);
					try {
						File mldsFile = new File(projectLocation.replace("\\", "/") + "/src/lds/memory.lds");
						DideHelper.setFileContent(mldsFile, "IbootSize", fMainPage._ibootSize + "K;");
					} catch (Exception e) {
						DideHelper.showErrorMessage("lds配置错误：" + e.getMessage());
					}
				}
				monitor.worked(6);
				// 处理工程的链接
				monitor.setTaskName("配置工程链接...");

				if (cpu != null && board != null && core != null) {
					handleCProject(index,appCompontentsChecked, ibootCompontentsChecked, board, cpu, core, projectPath,
							projectName);
				}

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
				// 自动编译库
				// if(dideHelper.isAutoBuildNew()) {
				// dideHelper.createBuild(curProject);
				// }
				monitor.done();
			}
		};

		if (warningMsg != null) {
			DideHelper.showErrorMessage(warningMsg);
			return false;
		}
		try {
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(
					PlatformUI.getWorkbench().getDisplay().getActiveShell());
			dialog.setCancelable(false);
			dialog.run(true, true, runnable);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	protected boolean check_Hardwares(Board board, Cpu cpu, Core core) {
		// TODO Auto-generated method stub
		if (board == null) {
			return DideHelper.showErrorMessage("所选板件不存在");
		}

		if (cpu == null) {
			return DideHelper.showErrorMessage("所选Cpu不存在");
		}

		if (core == null) {
			return DideHelper.showErrorMessage("所选内核不存在");
		} else {
			if (core.getArch().getSerie() == null) {
				return DideHelper.showErrorMessage("Cpu的架构类型为空");
			} else if (core.getArch().getMcpu() == null) {
				return DideHelper.showErrorMessage("Cpu的家族为空");
			}
		}

		return true;
	}
	

	public void clearNewProject() {
		String projectName = fMainPage.getProjectNameFieldValue();
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if (createdProject) {
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
	public boolean performCancel() {
		clearNewProject();
		return true;
	}

	@Override
	public void dispose() {
		fMainPage.dispose();
	}

}
