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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionPreferences;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionWorkspacePreferences;
import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;

import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.helper.LinkHelper;
import com.djyos.dide.ui.objects.Board;
import com.djyos.dide.ui.objects.CmpntCheck;
import com.djyos.dide.ui.objects.Component;
import com.djyos.dide.ui.objects.Core;
import com.djyos.dide.ui.objects.Cpu;
import com.djyos.dide.ui.objects.OnBoardCpu;
import com.djyos.dide.ui.wizards.board.GetNonBoardFiles;
import com.djyos.dide.ui.wizards.board.ReadBoardXml;
import com.djyos.dide.ui.wizards.component.GetNonCompFiles;
import com.djyos.dide.ui.wizards.component.ReadComponent;
import com.djyos.dide.ui.wizards.component.ReadComponentCheckXml;
import com.djyos.dide.ui.wizards.cpu.GetNonCpuFiles;
import com.djyos.dide.ui.wizards.cpu.ReadCpuXml;
import com.djyos.dide.ui.wizards.djyosProject.ReadHardWareDesc;
import com.djyos.dide.ui.wizards.djyosProject.info.CreateComponentInfo;
import com.djyos.dide.ui.wizards.djyosProject.info.ReadComponentsInfo;
import com.djyos.dide.ui.wizards.djyosProject.info.ReadIncludeFile;
import com.djyos.dide.ui.wizards.djyosProject.tools.FileTool;
import com.djyos.dide.ui.wizards.djyosProject.tools.ReviseVariableToXML;

import ilg.gnuarmeclipse.core.EclipseUtils;

public class HandleProjectImport {

	final IWorkspace workspace = ResourcesPlugin.getWorkspace();
	String srcLocation = DideHelper.getDjyosSrcPath();

	public void handlProjectImport() {
//		IPreferenceStore ps = PlatformUI.getPreferenceStore();
		String openOCD_Path =  DideHelper.didePath + "Tools/OpenOCD/bin";
//		System.out.println("EclipseUtils:  "+EclipseUtils.getVariableValue("openocd_path"));
//		System.out.println("DideHelper.didePath:  "+DideHelper.didePath);
		EclipseUtils.setVariableValue("openocd_path", openOCD_Path);
		IProject[] projects = workspace.getRoot().getProjects();
		for (IProject project : projects) {
			
			IFile file =  project.getFile("libos_Iboot_Debug/libos_Iboot.a");
			File f = file.getLocation().toFile();
			
			//将.a解压缩到os文件夹中
//			String parentPath = f.getParentFile().getPath();
//			File os_file = new File(parentPath+"/os");
//			String commond = "arm-none-eabi-ar -x "+f.getPath();
//			String[] commands = {"cmd","/C",commond};
//			String line = null;
//			StringBuilder sb = new StringBuilder();
//			Runtime runtime = Runtime.getRuntime();
//			try {
//				Process process = runtime.exec(commands,null,os_file);
//				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//				while ((line = bufferedReader.readLine()) != null) {
//					sb.append(line + "\n");
////					System.out.println("line: "+line);
//				}
//			} catch (IOException e) {
//				// TODO 自动生成的 catch 块
//				e.printStackTrace();
//			}
			
			File osFile = new File(f.getParentFile().getPath()+"/os");
			File[] os = osFile.listFiles();
			for(File o:os) {
				System.out.println("\n"+o.getPath()+":\n");
				Map<String, String> map = DideHelper.get_o_content(o);
				String shell = map.get("shell");
				if(shell != null) {
					
				}
				DideHelper.printToConsole(map.get("o_content"), true);
//				System.out.println(map.get("o_content"));
			}
			
			handleProjectElemExculde(project);
			
			IFile libosFolder = project.getFile("src/libos");
			if(!libosFolder.exists()) {
				libosFolder.getLocation().toFile().mkdir();
				ReviseVariableToXML rvtx = new ReviseVariableToXML();
				rvtx.add_djyos_links(project.getFile(".project"));
			}
			
//			String[] libos_members = {"bsp","component","djyos","libc","loader","third"};
//			for(String m:libos_members) {
//				IFolder newFolder = project.getFolder("src/libos/"+m);
//				if(!newFolder.exists()) {
//					newFolder.getLocation().toFile().mkdir();
//				}
//	        	try {
//					newFolder.createLink( new Path("DJYOS_SRC_LOCATION/"+m), 0, null);
//				} catch (CoreException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
        	
//			Project p = (Project) project;
//			boolean changed = p.internalGetDescription().setLinkLocation(getProjectRelativePath(), linkDescription);
//			if(changed) {
//				try {
//					p.writeDescription(IResource.NONE);
//				} catch (CoreException e) {
//					// A problem happened updating the description, so delete the resource from the workspace.
////					workspace.deleteResource(this);
//					throw e; // Rethrow.
//				}
//			}
		}
		
		try {
			workspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
				
				// 处理组件
				if (sBoard != null) {
					List<OnBoardCpu> onBoardCpus = sBoard.getOnBoardCpus();
					onBoardCpu = DideHelper.getOnBoardByCpu(onBoardCpus, cpu.getCpuName());
					List<Component> allCompontents = ReadComponent.getAllComponents(cpu, sBoard);
					if (compInfoFile.exists()) {
					
						List<String> compPaths = ReadComponentsInfo.getCompsInfo(compInfoFile);
						List<File> noneCompFiles = GetNonCompFiles.getNonCompFiles(onBoardCpu, sBoard);
						
						// 排除不是組建的文件
						excludeComponentNot(noneCompFiles, conds, project);
						
						//获取当前工程的check.xml，include或者exclude被选中或者没有选中的组件  //排除新增的组件 除了被选中的
						in_ex_cludeComponent(allCompontents, compPaths, conds, project);
						
						// 打开用户手动打开的组件
						excludeComponentOpenByUser(conds, project);

					}
					DideHelper.createNewFile(compInfoFile);
					CreateComponentInfo.createComponentInfo(compInfoFile, allCompontents);
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

	private void excludeComponentOpenByUser(ICConfigurationDescription[] conds, IProject project) {
		// TODO Auto-generated method stub
		ReadIncludeFile rif = new ReadIncludeFile();
		File diskFile = new File(project.getLocation().toString() + "/data/user_handled_files.xml");
		if (diskFile.exists()) {
			List<String> inculdePaths = rif.getIncludeFiles(diskFile);
			for (String incPath : inculdePaths) {
				IFolder ifolder = project.getFolder("src/libos" + incPath);
				LinkHelper.setProjectFolderExclude(ifolder,conds,false);
			}
		}
	}

	private void excludeComponentNot(List<File> excludeCompFiles, ICConfigurationDescription[] conds,
			IProject project) {
		// TODO Auto-generated method stub
		for (File f : excludeCompFiles) {
			String relativePath = f.isDirectory()?f.getPath().replace("\\", "/").replace(srcLocation, ""):
									f.getParentFile().getPath().replace("\\", "/").replace(srcLocation, "");
			IFolder ifolder = project.getFolder("src/libos" + relativePath);
			LinkHelper.setProjectFolderExclude(ifolder,conds,true);
		}
	}

	/*
	 *  排除新增的组件
	 */
	private void in_ex_cludeComponent(List<Component> allCompontents, List<String> compPaths,
			ICConfigurationDescription[] conds, IProject project) {
		// TODO Auto-generated method stub
		File appCheckFile = new File(project.getLocation().toString() + "/data/app_component_check.xml");
		File ibootCheckFile = new File(project.getLocation().toString() + "/data/iboot_component_check.xml");
		List<String> appCmpntNamesChecks = new ArrayList<String>(), ibootCmpntNamesChecks = new ArrayList<String>();
		if (appCheckFile.exists()) {
			List<CmpntCheck> appCmpntChecks = ReadComponentCheckXml.getCmpntChecks(appCheckFile);
			for(CmpntCheck cc:appCmpntChecks) {
				if(cc.isChecked().equals("true")) {
					appCmpntNamesChecks.add(cc.getCmpntName());
				}
			}
		}
		if (ibootCheckFile.exists()) {
			List<CmpntCheck> ibootCmpntChecks = ReadComponentCheckXml.getCmpntChecks(ibootCheckFile);
			for(CmpntCheck cc:ibootCmpntChecks) {
				if(cc.isChecked().equals("true")) {
					ibootCmpntNamesChecks.add(cc.getCmpntName());
				}
			}
		}
//		for(String check:ibootCmpntNamesChecks) {
//			System.out.println("check:  "+check);
//		}
		for (Component component : allCompontents) {
			String componentPath = component.getParentPath().replace("\\", "/");//组件父目录路径
			String fileName = component.getFileName();//组件文件名称
			String relativePath = componentPath.replace(srcLocation, "");//组件父目录路相对路径
			
			String compPath = relativePath + "/" + fileName;//组件文件相对路径 /component/...........
			IFolder ifolder = project.getFolder("src/libos" + relativePath);
			for (int j = 0; j < conds.length; j++) {
				if(conds[j].getName().startsWith("libos")) {
					boolean toExclude = false;
					if(!(relativePath.contains("component") || relativePath.contains("djyos"))) {
//						System.out.println("compPath:  "+compPath);
						if(conds[j].getName().startsWith("libos_App")) {
							toExclude = appCmpntNamesChecks.contains(component.getName())?false:true;
						}else if(conds[j].getName().startsWith("libos_Iboot")) {
							toExclude = ibootCmpntNamesChecks.contains(component.getName())?false:true;
						}
					}
					LinkHelper.setFolderExclude(ifolder, conds[j], toExclude);
				}
			}
//			if (!compPaths.contains(compPath)) { //compPaths:component_infos.xml中所有的之前已存在的组件，如果不存在当前组件的相对路径，则排除编译
//			}
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
					LinkHelper.setProjectFolderExclude(ifolder,conds,true);
				}
			}

			// 排除没有xml文件的板件，即不是一个完整的板件
			GetNonBoardFiles gnbf = new GetNonBoardFiles();
			List<File> excludeBoardFiles = gnbf.getNonBoards();
			for (File f : excludeBoardFiles) {
				String relativePath = f.getPath().replace("\\", "/").replace(srcLocation, "");
				IFolder ifolder = project.getFolder("src/libos" + relativePath);
				LinkHelper.setProjectFolderExclude(ifolder,conds,true);
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
						LinkHelper.setProjectFolderExclude(folder,conds,true);
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
					LinkHelper.setProjectFolderExclude(ifolder,conds,true);
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
//						System.out.println("folder:  "+folders.get(i).getProjectRelativePath().toString());
						LinkHelper.setProjectFolderExclude(folders.get(i),conds,false);
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
						LinkHelper.setFolderExclude(ifolder, conds[j], true);
					}
				}
			} else if (f.getName().equals(archType) && f.isDirectory()) {
				myArchfolder = ifolder;
			}
		}
		if (myArchfolder != null) {
			for (int j = 0; j < conds.length; j++) {
				if (conds[j].getName().startsWith("libos")) {
					LinkHelper.setFolderExclude(myArchfolder, conds[j], false);
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

//	for (int i = 0; i < conds.length; i++) {
//	if(!conds[i].getName().startsWith("libos")) {
//		System.out.println("conds[i].getName():   "+conds[i].getName());
//		ICResourceDescription rds = conds[i].getRootFolderDescription();
//		IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(rds.getConfiguration());
//		IResourceInfo resourceInfo = cfg.getRootFolderInfo();
//		IToolChain toolchain = resourceInfo.getParent().getToolChain();
//		
//		ITool[] tools = toolchain.getToolsBySuperClassId("ilg.gnuarmeclipse.managedbuild.cross.tool.cpp.linker");
//		for (ITool t : tools) {
//			IOption op = t.getOptionBySuperClassId(
//					"ilg.gnuarmeclipse.managedbuild.cross.option.cpp.linker.scriptfile");
//			try {
//				OptionStringValue[] values = op.getBasicStringListValueElements();
//				for (OptionStringValue v : values) {
//					System.out.println("value:   " + v.getValue());
//				}
//
//			} catch (BuildException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
//}
}
