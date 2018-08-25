package com.djyos.dide.ui.startup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import com.djyos.dide.ui.wizards.board.Board;
import com.djyos.dide.ui.wizards.board.GetNonBoardFiles;
import com.djyos.dide.ui.wizards.board.ReadBoardXml;
import com.djyos.dide.ui.wizards.board.onboardcpu.OnBoardCpu;
import com.djyos.dide.ui.wizards.component.Component;
import com.djyos.dide.ui.wizards.component.GetNonCompFiles;
import com.djyos.dide.ui.wizards.component.ReadComponent;
import com.djyos.dide.ui.wizards.cpu.Cpu;
import com.djyos.dide.ui.wizards.cpu.GetNonCpuFiles;
import com.djyos.dide.ui.wizards.cpu.ReadCpuXml;
import com.djyos.dide.ui.wizards.djyosProject.ReadHardWareDesc;
import com.djyos.dide.ui.wizards.djyosProject.info.CreateBoardInfo;
import com.djyos.dide.ui.wizards.djyosProject.info.CreateComponentInfo;
import com.djyos.dide.ui.wizards.djyosProject.info.CreateCpuInfo;
import com.djyos.dide.ui.wizards.djyosProject.info.ReadBoardsInfo;
import com.djyos.dide.ui.wizards.djyosProject.info.ReadComponentsInfo;
import com.djyos.dide.ui.wizards.djyosProject.info.ReadCpusInfo;
import com.djyos.dide.ui.wizards.djyosProject.tools.DideHelper;
import com.djyos.dide.ui.wizards.djyosProject.tools.LinkHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

public class HandleFolderAdded{

	LinkHelper linkHelper = new LinkHelper();
	DideHelper dideHelper = new DideHelper();
	final IWorkspace workspace = ResourcesPlugin.getWorkspace();
	private String didePath = new DideHelper().getDIDEPath();
	String srcLocation = didePath+"djysrc";
	
	public void handleAddeed() {

		IProject[] projects = workspace.getRoot().getProjects();
		File boardFile = new File(dideHelper.getDjyosSrcPath()+"/boarddrv");
		for(IProject project:projects) {
			ReadComponent rcp = new ReadComponent();
			ReadCpuXml rcx = new ReadCpuXml();
			ReadBoardXml rbx = new ReadBoardXml();
			ReadHardWareDesc rhwd = new ReadHardWareDesc();
			ReadCpusInfo readCpusInfo = new ReadCpusInfo();
			ReadBoardsInfo readBoardsInfo = new ReadBoardsInfo();
			ReadComponentsInfo readComponentsInfo = new ReadComponentsInfo();
			CreateComponentInfo createComponentInfo = new CreateComponentInfo();
			CreateBoardInfo createBoardInfo = new CreateBoardInfo();
			CreateCpuInfo createCpuInfo = new CreateCpuInfo();
			ReadComponent rc = new ReadComponent();
			GetNonCompFiles gecf = new GetNonCompFiles();
			final ICProjectDescription local_prjd =  CoreModel.getDefault().getProjectDescription(project);
			ICConfigurationDescription[] conds = local_prjd.getConfigurations();	//获取工程的所有Configuration	
			File compInfoFile = new File(project.getLocation().toString()+"/data/hardwares/component_infos.xml");
			File cpuInfoFile = new File(project.getLocation().toString()+"/data/hardwares/cpu_infos.xml");
			File boardInfoFile = new File(project.getLocation().toString()+"/data/hardwares/board_infos.xml");
			File hardWardFolder = new File(project.getLocation().toString()+"/data/hardwares");
			File hardWardInfoFile = new File(project.getLocation().toString()+"/data/hardware_info.xml");
			
			List<String> hardwares;
			if(hardWardInfoFile.exists()) {
				if(!hardWardFolder.exists()) {
					hardWardFolder.mkdirs();
				}
				hardwares = rhwd.getHardWares(hardWardInfoFile);
				Board sBoard = null;
				OnBoardCpu onBoardCpu = null;
				
				String cpuName = hardwares.get(1);
				String boardName = hardwares.get(0);
				List<Board> boards = rbx.getAllBoards();
				for (int i = 0; i < boards.size(); i++) {
					if (boards.get(i).getBoardName().equals(boardName)) {
						sBoard = boards.get(i);
						break;
					}
				}
				
				//处理组件
				if (sBoard != null) {
					List<OnBoardCpu> onBoardCpus = sBoard.getOnBoardCpus();
					for (int i = 0; i < onBoardCpus.size(); i++) {
						if (onBoardCpus.get(i).getCpuName().equals(cpuName)) {
							onBoardCpu = onBoardCpus.get(i);
							break;
						}
					}
					List<Component> allCompontents = rc.getAllComponents(onBoardCpu, sBoard);
					if(compInfoFile.exists()) {
						List<String> compPaths = readComponentsInfo.getCompsInfo(compInfoFile);
						//排除未被選中的組建
						for (Component component : allCompontents) { 
							String componentPath = component.getParentPath().replace("\\", "/");
							String fileName = component.getFileName();
							String relativePath = componentPath.replace(srcLocation, "");
							String compPath = relativePath+"/"+fileName;
							if(!compPaths.contains(compPath)) {
//								System.out.println("compPath:   "+compPath);
//								if(fileName.endsWith(".c")) {
//									IFile ifile = project.getFile("src/libos"+compPath);
//									for (int j = 0; j < conds.length; j++) {
//										linkHelper.setFileExclude(ifile, conds[j], true);
//									}
//									
//								}else if(fileName.endsWith(".h")) {
//									IFolder ifolder = project.getFolder("src/libos"+relativePath);
//									for (int j = 0; j < conds.length; j++) {
//										linkHelper.setExclude(ifolder, conds[j], true);
//									}
//								}
								IFolder ifolder = project.getFolder("src/libos"+relativePath);
								for (int j = 0; j < conds.length; j++) {
									linkHelper.setExclude(ifolder, conds[j], true);
								}
							}
						}
						//排除不是組建的文件
						List<File> excludeCompFiles = gecf.getExcludeCompFiles(onBoardCpu, sBoard);
						for(File f:excludeCompFiles) {
//							System.out.println("f.getName():   "+f.getName());
							String relativePath = f.getPath().replace("\\", "/").replace(srcLocation, "");
//							System.out.println("relativePath:   "+relativePath);
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
					}
					createNewFile(compInfoFile);
					createComponentInfo.createComponentInfo(compInfoFile, allCompontents);
				}

				//处理板件
				if(boardInfoFile.exists()) {
					List<String> boardNames = readBoardsInfo.getBoardsInfo(boardInfoFile);
					for(Board board:boards) {
						if(!boardNames.contains(board.getBoardName())) {
//							System.out.println("ExcludeBoard:   "+board.getBoardName());
							String boardPath = board.getBoardPath().replace("\\", "/");
							String relativePath = boardPath.replace(srcLocation, "");
							IFolder ifolder = project.getFolder("src/libos"+relativePath);
							for (int j = 0; j < conds.length; j++) {
								linkHelper.setExclude(ifolder, conds[j], true);
							}
						}
					}
					
					GetNonBoardFiles gnbf = new GetNonBoardFiles();
					List<File> excludeBoardFiles = gnbf.getNonBoards();
					for(File f:excludeBoardFiles) {
						String relativePath = f.getPath().replace("\\", "/").replace(srcLocation, "");
						IFolder ifolder = project.getFolder("src/libos"+relativePath);
						for (int j = 0; j < conds.length; j++) {
							linkHelper.setExclude(ifolder, conds[j], true);
						}
					}
				}
//				List<IFolder> nonBoardFolders = getNonBoardFolders(project);
//				for(IFolder folder:nonBoardFolders) {
//					System.out.println("nonBoardFolders：  "+folder.getFullPath());
//					for (int j = 0; j < conds.length; j++) {
//						linkHelper.setExclude(folder, conds[j], true);
//					}
//				}
				createNewFile(boardInfoFile);
				createBoardInfo.createBoardInfo(boardInfoFile, boards);
				
				//处理Cpu
				List<Cpu> allCpus = rcx.getAllCpus();
				if(cpuInfoFile.exists()) {
					List<String> cpuNames = readCpusInfo.getCpusInfo(cpuInfoFile);
					for(Cpu cpu:allCpus) {
						if(!cpuNames.contains(cpu.getCpuName())) {
							String cpuPath =  cpu.getParentPath().replace("\\", "/");
							File cpuFolder = new File(cpuPath);
							List<IFolder> folders = new ArrayList<IFolder>();
							getFolders(project,folders,cpuFolder,cpuName);
							for(IFolder folder:folders) {
								for (int j = 0; j < conds.length; j++) {
									linkHelper.setExclude(folder, conds[j], true);
								}
							}
						}
					}
					
					GetNonCpuFiles gncf = new GetNonCpuFiles();
					List<File> excludeCpuFiles = gncf.getNonCpus();
					for(File f:excludeCpuFiles) {
//						System.out.println("f.getName():   "+f.getName());
						String relativePath = f.getPath().replace("\\", "/").replace(srcLocation, "");
//						System.out.println("relativePath:   "+relativePath);
						IFolder ifolder = project.getFolder("src/libos"+relativePath);
						for (int j = 0; j < conds.length; j++) {
							linkHelper.setExclude(ifolder, conds[j], true);
						}
					}
					
				}
				createNewFile(cpuInfoFile);
				createCpuInfo.createCpuInfo(cpuInfoFile, allCpus);
				
			}
			
			try {
				CoreModel.getDefault().setProjectDescription(project, local_prjd);
			} catch (CoreException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

//			travelFiles(project,conds,boardFile);
		}
		
	}
	
	//获取不存在配置文件的板件
	private List<IFolder> getNonBoardFolders(IProject project){
		List<IFolder> folders = new ArrayList<IFolder>();
		List<String> paths = new ArrayList<String>();
		String userBoardFilePath = dideHelper.getDIDEPath() + "djysrc\\bsp\\boarddrv\\user";
		String demoBoardFilePath = dideHelper.getDIDEPath() + "djysrc\\bsp\\boarddrv\\demo";
		paths.add(userBoardFilePath);
		paths.add(demoBoardFilePath);
		for (int i = 0; i < paths.size(); i++) {
			File boardFile = new File(paths.get(i));
			File[] files = boardFile.listFiles();
			for (int j = 0; j < files.length; j++) {
				File file = files[j];
				if(file.isDirectory()) {
					if(!isContainsXml(file)) {
						String relativePath = file.getPath().replace(dideHelper.getDjyosSrcPath(), "");
						IFolder folder = project.getFolder("src/libos" + relativePath);
						folders.add(folder);
					}
				}
			}
		}
		return folders;
	}
	
	private boolean isContainsXml(File file) {
		File[] files = file.listFiles();
		for(File f:files) {
			if(f.getName().endsWith(".xml")) {
				return true;
			}
		}
		return false;
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

	private void getFolders(IProject project, List<IFolder> folders, File file, String cpuName) {
		// TODO Auto-generated method stub
		String relativePath = file.getPath().replace("\\", "/").replace(srcLocation, "");
		IFolder folder = project.getFolder("src/libos"+relativePath);
		folders.add(folder);
		File parentFile = file.getParentFile();
//		System.out.println("folder:  "+"src/libos"+relativePath);
		if(!parentFile.getName().equals("cpudrv") && !toInclude(parentFile,cpuName)) {
			getFolders(project,folders,parentFile,cpuName);
		}
		
	}

	private boolean toInclude(File parentFile, String cpuName) {
		// TODO Auto-generated method stub
		File[] files = parentFile.listFiles();
		for(File f:files) {
			if(f.getName().equals(cpuName)) {
				return true;
			}
		}
		return false;
	}


}
