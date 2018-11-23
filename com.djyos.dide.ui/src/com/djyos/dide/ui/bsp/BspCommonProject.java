/*******************************************************************************
 * Copyright (c) 2017 Djyos Team.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.djyos.com
 *
 * Contributors:
 *     Djyos Team - Jiaming Chen
 *******************************************************************************/
package com.djyos.dide.ui.bsp;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionPreferences;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionWorkspacePreferences;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.ui.CUIPlugin;
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
import com.djyos.dide.ui.objects.Core;
import com.djyos.dide.ui.objects.Cpu;
import com.djyos.dide.ui.wizards.board.ReadBoardXml;
import com.djyos.dide.ui.wizards.cpu.GetNonCpuFiles;
import com.djyos.dide.ui.wizards.cpu.ReadCpuXml;
import com.djyos.dide.ui.wizards.djyosProject.CreateHardWareDesc;
import com.djyos.dide.ui.wizards.djyosProject.DjyosMessages;
import com.djyos.dide.ui.wizards.djyosProject.info.CreateBoardInfo;
import com.djyos.dide.ui.wizards.djyosProject.info.CreateCpuInfo;
import com.djyos.dide.ui.wizards.djyosProject.tools.ReviseVariableToXML;

@SuppressWarnings("restriction")
public class BspCommonProject extends BasicNewResourceWizard {

	private BspProjectMainWizard fMainPage;
	private String wz_title, wz_desc;
	private IProject curProject;
	private boolean createdProject;
	private String didePath = DideHelper.getDIDEPath();

	public BspCommonProject(String title, String desc) {
		// TODO Auto-generated constructor stub
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
		setNeedsProgressMonitor(true);
		setForcePreviousAndNextButtons(true);
		setWindowTitle(title);
		wz_title = title;
		wz_desc = desc;
	}

	@Override
	public void addPages() {
		// TODO Auto-generated method stub
		fMainPage = new BspProjectMainWizard("BspMainWiazrdPage");
		fMainPage.setTitle(wz_title);
		fMainPage.setDescription(wz_desc);
		addPage(fMainPage);
	}

	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		Cpu cpu = fMainPage.getSelectCpu();
		Board board = fMainPage.getSelectBoard();
		Core core = fMainPage.getSelectCore();
		String projectPath = fMainPage.locationArea.locationPathField.getText();
		String projectName = fMainPage.getProjectName();
		System.out.println("projectPath:  " + projectPath + "\nprojectName:  " + projectName);
		// 导入模板工程
		importProject(projectPath, board, core);

		if (core.getArch().getSerie() == null) {
			return DideHelper.showErrorMessage("Cpu的架构类型为空");
		} else if (core.getArch().getMcpu() == null) {
			return DideHelper.showErrorMessage("Cpu的家族为空");
		}

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (!projectPath.contains(projectName)) {
			project = curProject;
		}

		String projectLocation = project.getLocation().toString();
		File dataFolder = new File(projectLocation + "/data");
		if (!dataFolder.exists()) {
			dataFolder.mkdirs();
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

				// 生成memory.lds文件
				monitor.setTaskName("配置工程初始化文件...");
				File hardWardInfoFile = new File(projectLocation + "/data/hardware_info.xml");
				try {
					hardWardInfoFile.createNewFile();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				CreateHardWareDesc chwd = new CreateHardWareDesc();
				chwd.createHardWareXml(board.getBoardName(), cpu.getCpuName(), hardWardInfoFile);
				monitor.worked(6);

				// 处理工程的链接
				monitor.setTaskName("配置工程链接...");
				try {
					configure_BspProject(board, cpu, core, projectPath, projectName);
				} catch (Exception e) {
					DideHelper.showErrorMessage("链接配置错误：" + e.getMessage());
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

		return true;
	}

	@Override
	public boolean performCancel() {
		// TODO Auto-generated method stub
		clearNewProject();
		return super.performCancel();
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

	public void importProject(String projectPath, Board selectedBoard, Core selectedCore) {

		String projectName = fMainPage.getProjectName();// 用户填写的工程名
		String srcPath = DideHelper.getTemplatePath() + "/"
				+ selectedCore.getArch().getToolchain().replaceAll("\\s+", "-") + "/" + "bsp";// 模板的路径
		String userPath = projectPath;
		if (!projectPath.contains(projectName)) {
			projectPath = projectPath + "/" + projectName;
		}
		String destPath = projectPath;
		File srcFile = new File(srcPath);
		File destFile = new File(destPath);

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
					MultiStatus status = new MultiStatus(IDEWorkbenchPlugin.IDE_WORKBENCH, 1,
							DataTransferMessages.WizardProjectsImportPage_projectsInWorkspaceAndInvalid, null);
					importExistingProject(subMonitor.split(1), projectName, userPath, destPath);

					if (!status.isOK()) {
						throw new InvocationTargetException(new CoreException(status));
					}
				}
			};

			try {
				getContainer().run(true, true, op);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				DideHelper.showErrorMessage("工程创建失败" + e.getMessage());
				e.printStackTrace();
			}
			createdProject = true;

			File destLdsFile = new File(destPath + "/src/lds");
			if (!destLdsFile.exists()) {
				destLdsFile.mkdirs();
			}
			File memoryLdsFile = new File(selectedBoard.getBoardPath() + "/lds/memory.lds");
			File bspLdsFile = new File(selectedBoard.getBoardPath() + "/lds/bsp.lds");
			if (memoryLdsFile.exists()) {
				DideHelper.copyFolder(memoryLdsFile, new File(destPath + "/src/lds/memory.lds"));
			}
			if (bspLdsFile.exists()) {
				DideHelper.copyFolder(bspLdsFile, new File(destPath + "/src/lds/bsp.lds"));
			}

		} else {
			fMainPage.setErrorMessage("Project is aready existed");
		}

	}

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

	public void configure_BspProject(Board board, Cpu cpu, Core core, String projectPath, String projectName) {
		String _cpuName = cpu.getCpuName();
		File boardDemoFile = new File(DideHelper.getDemoBoardFilePath());
		File archSourceFile = new File(DideHelper.getDjyosSrcPath() + "/bsp/arch");
		List<File> archXmlFiles = DideHelper.getArchXmlFiles(archSourceFile, new ArrayList<File>());
		File boardFolder = new File(board.getBoardPath());
		File cpuFolder = new File(cpu.getParentPath());

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

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (!projectPath.contains(projectName)) {
			project = curProject;
		}
		ReviseVariableToXML rvtx = new ReviseVariableToXML();
		// $%7BPARENT-1-ECLIPSE_HOME%7D/djysrc
		rvtx.reviseXmlVariable("DJYOS_SRC_LOCATION", "$%7BPARENT-1-ECLIPSE_HOME%7D/djysrc", project.getFile(".project"),
				projectName);

		File hardwareFile = new File(project.getLocation().toString() + "/data/hardwares");
		if (!hardwareFile.exists()) {
			hardwareFile.mkdirs();
		}

		final ICProjectDescription local_prjd = CoreModel.getDefault().getProjectDescription(project);
		ICConfigurationDescription[] conds = local_prjd.getConfigurations(); // 获取工程的所有Configuration
		// 修改编译选项的名称
		for (ICConfigurationDescription cfgDesc : conds) {
			String s = cfgDesc.getName();
			if (s.contains(DjyosMessages.Configuration_Debug) || s.contains(DjyosMessages.Configuration_Release)) {
				if (!s.contains("libos")) {
					cfgDesc.setName(projectName + "_" + s);
				}
			}
		}

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
			setFolderExclude(f, project, conds);
		}

		// 释放当前的arch
		if (curArchFile != null) {
			IFolder archtectureFolder = project.getFolder(
					"src/libos" + curArchFile.getPath().replace("\\", "/").replace(DideHelper.getDjyosSrcPath(), ""));
			List<IFolder> archtectureFolders = new ArrayList<IFolder>();
			getArchFolders(archtectureFolder, archtectureFolders);
			setFoldersInclude(archtectureFolders, conds);
		}

		// 给每个Configuration修改配置,增加链接
		for (int i = 0; i < conds.length; i++) {
			String conName = conds[i].getName();
			ICResourceDescription rds = conds[i].getRootFolderDescription();
			IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(rds.getConfiguration());
			IResourceInfo resourceInfo = cfg.getRootFolderInfo();

			String toolChainName = cfg.getToolChain().getName();
			if (toolChainName.indexOf("CSky") != -1 || toolChainName.indexOf("CKcore") != -1) {
				cfg.setBuildCommand("make");
				cfg.setBuildArguments("SHELL=bash.exe" + " " + cfg.getBuildArguments().replaceAll("SHELL=cmd.exe", "")
						.replaceAll("SHELL=bash.exe", "").trim());
			} else {
				cfg.setBuildCommand("${cross_make}");
				cfg.setBuildArguments(
						"SHELL=cmd.exe" + " " + cfg.getBuildArguments().replaceAll("SHELL=cmd.exe", "").trim());
			}

			String pattern = "$(call loop, 50, ${INPUTS}, ${COMMAND} ${FLAGS} ${OUTPUT})";
			if (!(conName.contains("libos") || toolChainName.indexOf("CSky") != -1
					|| toolChainName.indexOf("CKcore") != -1)) {
				cfg.setPostbuildStep("make " + conName + ".bin && elf_to_bin.exe " + conName + ".elf " + conName
						+ ".bin && ren " + conName + ".bin new" + conName + ".bin");
			}
			if (conName.contains("libos")) {
				ITool[] tools = cfg.getToolChain().getTools();
				for (ITool tool : tools) {
					if (tool.getName().contains("Cross ARM GNU Archiver")
							|| tool.getName().contains("CSky Elf Archiver")) {
						if (!tool.getCommandLinePattern().equals(pattern)) {
							tool.setCommandLinePattern(pattern);
						}
					}

				}
			}

			IToolChain toolchain = resourceInfo.getParent().getToolChain();
			List<String> links = new ArrayList<String>();
			List<String> assemblyLinks = new ArrayList<String>();
			// 根据架构类型选择链接
			if (core.getArch().getSerie().equals("arm")) {
				links.add("${DJYOS_SRC_LOCATION}/third/firmware/CMSIS/include");
				links.add("${DJYOS_SRC_LOCATION}/bsp/arch/arm");
				assemblyLinks.add("${DJYOS_SRC_LOCATION}/bsp/arch/arm");
			}
			// 根据cpu链接
			List<String> cpuLinkStrings = new ArrayList<String>();
			getCpuIncludes(cpuFolder, cpuLinkStrings);
			String firmwarePath = didePath + "djysrc/third/firmware";
			File[] firmwareFiles = new File(firmwarePath).listFiles();
			for (File file : firmwareFiles) {
				if (_cpuName.contains(file.getName())) {
					cpuLinkStrings.add("${DJYOS_SRC_LOCATION}/third/firmware/" + file.getName());
					cpuLinkStrings.add("${DJYOS_SRC_LOCATION}/third/firmware/" + file.getName() + "/Inc");
					break;
				}
			}
			if (cpuLinkStrings != null) {
				for (int k = 0; k < cpuLinkStrings.size(); k++) {
					links.add(cpuLinkStrings.get(k).replace("\\", "/"));
				}
			}
			// 根据板件名链接
			if (isDemoBoard) {
				links.add(DideHelper.getRelativeDemoBoardFilePath() + boardFolder.getName());
				links.add(DideHelper.getRelativeDemoBoardFilePath() + boardFolder.getName() + "/include");
				links.add(DideHelper.getRelativeDemoBoardFilePath() + boardFolder.getName() + "/startup");
			} else {
				links.add(DideHelper.getRelativeUserBoardFilePath() + boardFolder.getName());
				links.add(DideHelper.getRelativeUserBoardFilePath() + boardFolder.getName() + "/include");
				links.add(DideHelper.getRelativeUserBoardFilePath() + boardFolder.getName() + "/startup");
			}
			// 根据内核类型、架构、家族链接
			List<String> archLinks = new ArrayList<String>();
			setArchLinks(core.getArch(), archLinks, archXmlFiles);
			for (String link : archLinks) {
				links.add(link);
				assemblyLinks.add(link);
			}

			if (toolchain.getName().equals("Cross ARM GCC")) {
				IOption option1 = toolchain.getOptionBySuperClassId(DjyosMessages.Arch_SuperClassId);
				IOption option2 = toolchain.getOptionBySuperClassId(DjyosMessages.Family_SuperClassId);
				IOption option3 = toolchain.getOptionBySuperClassId(DjyosMessages.FpuABI_SuperClassId);
				IOption option4 = toolchain.getOptionBySuperClassId(DjyosMessages.FpuType_SuperClassId);
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

		List<Board> allBoards = ReadBoardXml.getAllBoards();
		File boardInfoFile = new File(project.getLocation().toString() + "/data/hardwares/board_infos.xml");
		DideHelper.createNewFile(boardInfoFile);
		CreateBoardInfo.createBoardInfo(boardInfoFile, allBoards);

		if (isDemoBoard) {
			setBoardExclude(true, boardFolder.getName(), project, conds);
			// notExcludes.add("src/libos/bsp/boarddrv/demo/"+board.getBoardName());
		} else {
			setBoardExclude(false, boardFolder.getName(), project, conds);
			// notExcludes.add("src/libos/bsp/boarddrv/user/"+board.getBoardName());
		}

		List<Cpu> allCpus = ReadCpuXml.getAllCpus();
		// 保存所有的cpu信息
		File cpuInfoFile = new File(project.getLocation().toString() + "/data/hardwares/cpu_infos.xml");
		DideHelper.createNewFile(cpuInfoFile);
		CreateCpuInfo.createCpuInfo(cpuInfoFile, allCpus);

		Cpu myCpu = null;
		for (Cpu c : allCpus) {
			if (c.getCpuName().equals(_cpuName)) {
				myCpu = c;
				break;
			}
		}
		setCpuFilesExclude(project, conds, myCpu, allCpus);

		try {
			CoreModel.getDefault().setProjectDescription(project, local_prjd);
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	private void setFoldersInclude(List<IFolder> folders, ICConfigurationDescription[] conds) {
		// TODO Auto-generated method stub
		for (int i = 0; i < conds.length; i++) {
			if (conds[i].getName().contains("libos")) {
				for (int k = folders.size() - 1; k >= 0; k--) {
					LinkHelper.setExclude(folders.get(k), conds[i], false);
				}
			}
		}
	}

	private void getArchFolders(IFolder ifolder, List<IFolder> folders) {
		// TODO Auto-generated method stub
		folders.add(ifolder);
		System.out.println("ifolder:   " + ifolder.getFullPath().toString());
		IFolder parentFolder = (IFolder) ifolder.getParent();
		if (!parentFolder.getName().equals("arch")) {
			getArchFolders(parentFolder, folders);
		}
	}

	private void getExcludeArchFiles(List<File> excludeArchFiles, List<File> stepArchFiles) {
		// TODO Auto-generated method stub
		List<File> tempArchFiles = new ArrayList<File>();
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

	private void setFolderExclude(File f, IProject project, ICConfigurationDescription[] conds) {
		// TODO Auto-generated method stub
		String srcLocation = DideHelper.getDjyosSrcPath();
		String filePath = f.getPath().toString().replace("\\", "/");
		String relativePath = filePath.replace(srcLocation, "");
		IFolder folder = project.getFolder("src/libos" + relativePath);
		for (int i = 0; i < conds.length; i++) {
			if (conds[i].getName().contains("libos")) {
				LinkHelper.setExclude(folder, conds[i], true);
			}
		}
	}

	private void getCpuIncludes(File cpuFile, List<String> cpuLinkStrings) {
		// TODO Auto-generated method stub
		String DJYOS_SRC_LOCARION = didePath + "djysrc";
		File[] files = cpuFile.listFiles();
		for (File file : files) {
			String path = file.getPath();
			String relativePath = path.substring(DJYOS_SRC_LOCARION.length(), path.length());
			if (file.getName().equals("include")) {
				cpuLinkStrings.add("${DJYOS_SRC_LOCATION}" + relativePath);
				break;
			}
		}
		if (!cpuFile.getParentFile().getName().equals("cpudrv")) {
			getCpuIncludes(cpuFile.getParentFile(), cpuLinkStrings);
		}
	}

	private void setArchLinks(Arch arch, List<String> archLinks, List<File> archXmlFiles) {
		// TODO Auto-generated method stub
		File archSrcFile = new File(DideHelper.getDjyosSrcPath() + "/bsp/arch");
		if (archSrcFile.exists()) {
			for (File f : archXmlFiles) {
				if (arch.getMcpu() != null) {
					if (f.getParentFile().getName().equals(arch.getMcpu())) {
						File parentFile = f.getParentFile();
						getArchLinks(archLinks, parentFile);
						break;
					}
				}
			}
		}
	}

	private void getArchLinks(List<String> archLinks, File archFolder) {
		// TODO Auto-generated method stub
		File includeFile = new File(archFolder.getPath().replace("\\", "/") + "/include");
		if (includeFile.exists()) {
			archLinks.add(includeFile.getPath().replace("\\", "/").replace(DideHelper.getArchPath(),
					"${DJYOS_SRC_LOCATION}/bsp/arch"));
		}
		if (!archFolder.getParentFile().getName().equals("arch")) {
			getArchLinks(archLinks, archFolder.getParentFile());
		}
	}

	private void setBoardExclude(boolean isDemoBoard, String boardName, IProject project,
			ICConfigurationDescription[] conds) {
		// TODO Auto-generated method stub
		File boardDrvFile = new File(DideHelper.getDjyosSrcPath() + "/bsp/boarddrv");
		File[] files = boardDrvFile.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
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

	private void setFolderInclude(File f, IProject project, ICConfigurationDescription[] conds) {
		String srcLocation = DideHelper.getDjyosSrcPath();
		String filePath = f.getPath().toString().replace("\\", "/");
		String relativePath = filePath.replace(srcLocation, "");
		IFolder folder = project.getFolder("src/libos" + relativePath);
		for (int i = 0; i < conds.length; i++) {
			if (conds[i].getName().contains("libos")) {
				LinkHelper.setExclude(folder, conds[i], false);
			}
		}
	}

	private void setCpuFilesExclude(IProject project, ICConfigurationDescription[] conds, Cpu myCpu,
			List<Cpu> allCpus) {
		// Exclude没有描述文件的的cpu
		GetNonCpuFiles gncf = new GetNonCpuFiles();
		List<File> excludeCpuFiles = gncf.getNonCpus();
		for (File f : excludeCpuFiles) {
			setFolderExclude(f, project, conds);
		}

		// Exclude不需要的cpu
		for (Cpu c : allCpus) {
			if (!c.getCpuName().equals(myCpu.getCpuName())) {
				String parentPath = c.getParentPath();
				File parentFile = new File(parentPath);
				travelCpuParentInclude(parentFile.getParentFile(), project, conds);
				travelCpuParentExclude(parentFile, project, conds);
			}
		}
		// Include需要的cpu
		File myCpuParentFile = new File(myCpu.getParentPath());
		travelCpuParentInclude(myCpuParentFile, project, conds);
	}

	private void travelCpuParentInclude(File file, IProject project, ICConfigurationDescription[] conds) {
		File parentFile = file.getParentFile();
		if (!parentFile.getName().equals("cpudrv")) {
			travelCpuParentInclude(parentFile, project, conds);
		}
		setFolderInclude(file, project, conds);
	}

	private void travelCpuParentExclude(File parentFile, IProject project, ICConfigurationDescription[] conds) {
		setFolderExclude(parentFile, project, conds);
		parentFile = parentFile.getParentFile();
		if (!parentFile.getName().equals("cpudrv")) {
			travelCpuParentExclude(parentFile, project, conds);
		}
	}

}
