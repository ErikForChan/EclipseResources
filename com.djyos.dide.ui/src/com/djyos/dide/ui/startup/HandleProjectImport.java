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
package com.djyos.dide.ui.startup;

import java.io.File;
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
import org.eclipse.cdt.managedbuilder.core.OptionStringValue;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.helper.LinkHelper;
import com.djyos.dide.ui.objects.Board;
import com.djyos.dide.ui.objects.Component;
import com.djyos.dide.ui.objects.Core;
import com.djyos.dide.ui.objects.Cpu;
import com.djyos.dide.ui.objects.OnBoardCpu;
import com.djyos.dide.ui.wizards.board.GetNonBoardFiles;
import com.djyos.dide.ui.wizards.board.ReadBoardXml;
import com.djyos.dide.ui.wizards.component.GetNonCompFiles;
import com.djyos.dide.ui.wizards.component.ReadComponent;
import com.djyos.dide.ui.wizards.cpu.GetNonCpuFiles;
import com.djyos.dide.ui.wizards.cpu.ReadCpuXml;
import com.djyos.dide.ui.wizards.djyosProject.ReadHardWareDesc;
import com.djyos.dide.ui.wizards.djyosProject.info.CreateBoardInfo;
import com.djyos.dide.ui.wizards.djyosProject.info.CreateComponentInfo;
import com.djyos.dide.ui.wizards.djyosProject.info.CreateCpuInfo;
import com.djyos.dide.ui.wizards.djyosProject.info.ReadComponentsInfo;
import com.djyos.dide.ui.wizards.djyosProject.info.ReadIncludeFile;

public class HandleProjectImport {

	final IWorkspace workspace = ResourcesPlugin.getWorkspace();
	String srcLocation = DideHelper.getDjyosSrcPath();

	public void handlProjectImport() {
		IProject[] projects = workspace.getRoot().getProjects();
		for (IProject project : projects) {
			handleProjectElemExculde(project);
		}
	}

	public void handleProjectElemExculde(IProject project) {
		final ICProjectDescription local_prjd = CoreModel.getDefault().getProjectDescription(project);
		if (local_prjd != null) {

			ICProjectDescriptionManager prjDescMgr = CCorePlugin.getDefault().getProjectDescriptionManager();
			ICProjectDescriptionWorkspacePreferences prefs = prjDescMgr.getProjectDescriptionWorkspacePreferences(true);
			prefs.setConfigurationRelations(ICProjectDescriptionPreferences.CONFIGS_LINK_SETTINGS_AND_ACTIVE);
			prjDescMgr.setProjectDescriptionWorkspacePreferences(prefs, false, new NullProgressMonitor());

			ICConfigurationDescription[] conds = local_prjd.getConfigurations(); // 获取工程的所有Configuration
			File compInfoFile = new File(project.getLocation().toString() + "/data/hardwares/component_infos.xml");
			File cpuInfoFile = new File(project.getLocation().toString() + "/data/hardwares/cpu_infos.xml");
			File boardInfoFile = new File(project.getLocation().toString() + "/data/hardwares/board_infos.xml");
			File hardWardFolder = new File(project.getLocation().toString() + "/data/hardwares");
			File hardWardInfoFile = new File(project.getLocation().toString() + "/data/hardware_info.xml");
			
			List<String> hardwares;
			if (hardWardInfoFile.exists()) {
				if (!hardWardFolder.exists()) {
					hardWardFolder.mkdirs();
				}
				hardwares = ReadHardWareDesc.getHardWares(hardWardInfoFile);
				Board sBoard = null;
				OnBoardCpu onBoardCpu = null;

				String cpuName = hardwares.get(1);
				String boardName = hardwares.get(0);
				String coreName = null;
				if(hardwares.size() > 2) {
					coreName = hardwares.get(2);
				}
				
				sBoard = DideHelper.getBoardByName(boardName);
				Cpu cpu = DideHelper.getCpuByName(cpuName);
				if (sBoard == null || cpu == null) {
					if (sBoard == null) {
						DideHelper.showErrorMessage(project.getName() + ":  板件 [" + boardName + "]不存在");
					} else {
						DideHelper.showErrorMessage(project.getName() + ":  Cpu [" + cpuName + "] 不存在");
					}
					return;
				}
//				System.out.println("sBoard:   " + sBoard.getBoardName());
				// 处理组件
				if (sBoard != null) {
					List<OnBoardCpu> onBoardCpus = sBoard.getOnBoardCpus();
					onBoardCpu = DideHelper.getOnBoardByCpu(onBoardCpus, cpu.getCpuName());
					List<Component> allCompontents = ReadComponent.getAllComponents(cpu, sBoard);
					if (compInfoFile.exists()) {
						List<String> compPaths = ReadComponentsInfo.getCompsInfo(compInfoFile);
						List<File> noneCompFiles = GetNonCompFiles.getNonCompFiles(onBoardCpu, sBoard);
						// 排除新增的组件
						excludeComponentNewed(allCompontents, compPaths, conds, project);
						
						// 排除不是組建的文件
						excludeComponentNot(noneCompFiles, conds, project);
						
						// 打开用户选中的组件
						
						// 打开用户手动打开的组件
						ReadIncludeFile rif = new ReadIncludeFile();
						excludeComponentOpenByUser(rif, conds, project);

					}
//					DideHelper.createNewFile(compInfoFile);
//					CreateComponentInfo.createComponentInfo(compInfoFile, allCompontents);
				}

				// 处理板件
				List<Board> boards = ReadBoardXml.getAllBoards();
				handleBoard(boardInfoFile, boards, boardName, conds, project);
//				DideHelper.createNewFile(boardInfoFile);
//				CreateBoardInfo.createBoardInfo(boardInfoFile, boards);

				// 处理Cpu
				List<Cpu> allCpus = ReadCpuXml.getAllCpus();
				handleCpu(cpuInfoFile, allCpus, cpuName, conds, project);
//				DideHelper.createNewFile(cpuInfoFile);
//				CreateCpuInfo.createCpuInfo(cpuInfoFile, allCpus);

				// 排除不是本工程的Arch
				Cpu myCpu = DideHelper.getCpuByonBoard(onBoardCpu, allCpus);
				excludeArchNotThisProject(myCpu, conds, project, coreName);

				try {
					CoreModel.getDefault().setProjectDescription(project, local_prjd);
				} catch (CoreException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	private void excludeComponentOpenByUser(ReadIncludeFile rif, ICConfigurationDescription[] conds, IProject project) {
		// TODO Auto-generated method stub
		File diskFile = new File(project.getLocation().toString() + "/data/user_handled_files.xml");
		if (diskFile.exists()) {
			List<String> inculdePaths = rif.getIncludeFiles(diskFile);
			for (String incPath : inculdePaths) {
				IFolder ifolder = project.getFolder("src/libos" + incPath);
				for (int j = 0; j < conds.length; j++) {
					if (conds[j].getName().startsWith("libos")) {
						LinkHelper.setExclude(ifolder, conds[j], false);
					}
				}
			}
		}
	}

	private void excludeComponentNot(List<File> excludeCompFiles, ICConfigurationDescription[] conds,
			IProject project) {
		// TODO Auto-generated method stub
		for (File f : excludeCompFiles) {
			String relativePath = null;
			if (f.isDirectory()) {
				relativePath = f.getPath().replace("\\", "/").replace(srcLocation, "");
			} else {
				relativePath = f.getParentFile().getPath().replace("\\", "/").replace(srcLocation, "");
			}
			IFolder ifolder = project.getFolder("src/libos" + relativePath);
			for (int j = 0; j < conds.length; j++) {
				LinkHelper.setExclude(ifolder, conds[j], true);
			}

		}
	}

	private void excludeComponentNewed(List<Component> allCompontents, List<String> compPaths,
			ICConfigurationDescription[] conds, IProject project) {
		// TODO Auto-generated method stub
		for (Component component : allCompontents) {
			String componentPath = component.getParentPath().replace("\\", "/");
			String fileName = component.getFileName();
			String relativePath = componentPath.replace(srcLocation, "");
			String compPath = relativePath + "/" + fileName;
			if (!compPaths.contains(compPath)) {
				IFolder ifolder = project.getFolder("src/libos" + relativePath);
				for (int j = 0; j < conds.length; j++) {
					if(conds[j].getName().startsWith("libos")) {
						LinkHelper.setExclude(ifolder, conds[j], true);
					}
				}
			}
		}

	}

	private void handleBoard(File boardInfoFile, List<Board> boards, String boardName,
			ICConfigurationDescription[] conds, IProject project) {
		// TODO Auto-generated method stub
		if (boardInfoFile.exists()) {
			// 排除新增的板件 修改为:排除不是本工程的板件
			for (Board board : boards) {
				if (!boardName.equals(board.getBoardName())) {
					String boardPath = board.getBoardFolderPath().replace("\\", "/");
					String relativePath = boardPath.replace(srcLocation, "");
					IFolder ifolder = project.getFolder("src/libos" + relativePath);
					for (int j = 0; j < conds.length; j++) {
						if(conds[j].getName().startsWith("libos")) {
							LinkHelper.setExclude(ifolder, conds[j], true);
						}
					}
				}
			}

			// 排除没有xml文件的板件，即不是一个完整的板件
			GetNonBoardFiles gnbf = new GetNonBoardFiles();
			List<File> excludeBoardFiles = gnbf.getNonBoards();
			for (File f : excludeBoardFiles) {
				String relativePath = f.getPath().replace("\\", "/").replace(srcLocation, "");
				IFolder ifolder = project.getFolder("src/libos" + relativePath);
				for (int j = 0; j < conds.length; j++) {
					if(conds[j].getName().startsWith("libos")) {
						LinkHelper.setExclude(ifolder, conds[j], true);
					}
				}
			}
		}
	}

	private void handleCpu(File cpuInfoFile, List<Cpu> allCpus, String cpuName, ICConfigurationDescription[] conds,
			IProject project) {
		// TODO Auto-generated method stub
		if (cpuInfoFile.exists()) {
			// 修改为:排除不是本工程的Cpu
			for (Cpu cpu : allCpus) {
				if (!cpuName.equals(cpu.getCpuName())) {
					String cpuPath = cpu.getCpuFolderPath().replace("\\", "/");
					File cpuFolder = new File(cpuPath);
					List<IFolder> folders = new ArrayList<IFolder>();
					getFolders(project, folders, cpuFolder, "cpudrv");
					for (IFolder folder : folders) {// srm32f7123,f7,stm32
						for (int j = 0; j < conds.length; j++) {
							if(conds[j].getName().startsWith("libos")) {
								LinkHelper.setExclude(folder, conds[j], true);
							}
						}
					}
				}
			}
			// 排除不是Cpu的文件夹
			GetNonCpuFiles gncf = new GetNonCpuFiles();
			List<File> excludeCpuFiles = gncf.getNonCpus();
			for (File f : excludeCpuFiles) {
				String relativePath = f.getPath().replace("\\", "/").replace(srcLocation, "");
				IFolder ifolder = project.getFolder("src/libos" + relativePath);
				for (int j = 0; j < conds.length; j++) {
					if(conds[j].getName().startsWith("libos")) {
						LinkHelper.setExclude(ifolder, conds[j], true);
					}
				}
			}
			// 打开本工程的Cpu
			for (Cpu cpu : allCpus) {
				if (cpuName.equals(cpu.getCpuName())) {
					String cpuPath = cpu.getCpuFolderPath().replace("\\", "/");
					File cpuFolder = new File(cpuPath);
					List<IFolder> folders = new ArrayList<IFolder>();
					getFolders(project, folders, cpuFolder, "cpudrv");// stm32f7123,f7,stm32
					for (int i = folders.size() - 1; i >= 0; i--) {// stm32,f7,stm32f7123
						for (int j = 0; j < conds.length; j++) {
							if (conds[j].getName().startsWith("libos")) {
								LinkHelper.setExclude(folders.get(i), conds[j], false);
							}
						}
					}
				}
			}

		}
	}

	private void excludeArchNotThisProject(Cpu myCpu, ICConfigurationDescription[] conds, IProject project, String coreName) {
		// TODO Auto-generated method stub
		String archType = null;
		List<Core> cores =  myCpu.getCores();
		for(Core core:cores) {
			if(core.getName()!=null) {
				if(core.getName().equalsIgnoreCase(coreName)) {
					archType = core.getArch().getSerie();
					break;
				}
			}
		}
		if(archType == null) {
			archType = cores.get(0).getArch().getSerie();
		}
		File archSourceFile = new File(DideHelper.getDjyosSrcPath() + "/bsp/arch");
		File[] archTypeFiles = archSourceFile.listFiles();

		IFolder myArchfolder = null;
		for (File f : archTypeFiles) {
			String relativePath = f.getPath().replace("\\", "/").replace(srcLocation, "");
			IFolder ifolder = project.getFolder("src/libos" + relativePath);
			if (!f.getName().equals(archType) && f.isDirectory()) {
				for (int j = 0; j < conds.length; j++) {
					if (conds[j].getName().startsWith("libos")) {
						LinkHelper.setExclude(ifolder, conds[j], true);
					}
				}
			} else if (f.getName().equals(archType) && f.isDirectory()) {
				myArchfolder = ifolder;
			}
		}
		if (myArchfolder != null) {
			for (int j = 0; j < conds.length; j++) {
				if (conds[j].getName().startsWith("libos")) {
					LinkHelper.setExclude(myArchfolder, conds[j], false);
				}
			}
		}
	}

	private void getFolders(IProject project, List<IFolder> folders, File file, String tag) {
		// TODO Auto-generated method stub
		String relativePath = file.getPath().replace("\\", "/").replace(srcLocation, "");
		IFolder folder = project.getFolder("src/libos" + relativePath);
		folders.add(folder);
		File parentFile = file.getParentFile();
		if (!parentFile.getName().equals(tag)) {
			getFolders(project, folders, parentFile, tag);
		}

	}

}
