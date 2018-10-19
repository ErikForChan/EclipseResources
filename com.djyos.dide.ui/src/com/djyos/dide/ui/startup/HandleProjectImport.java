package com.djyos.dide.ui.startup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionPreferences;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionWorkspacePreferences;
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
import org.eclipse.core.runtime.NullProgressMonitor;

public class HandleProjectImport{

	LinkHelper linkHelper = new LinkHelper();
	DideHelper dideHelper = new DideHelper();
	final IWorkspace workspace = ResourcesPlugin.getWorkspace();
	private String didePath = new DideHelper().getDIDEPath();
	String srcLocation = didePath+"djysrc";
	
	public void handlProjectImport() {
		IProject[] projects = workspace.getRoot().getProjects();
		File boardFile = new File(dideHelper.getDjyosSrcPath()+"/boarddrv");
		for(IProject project:projects) {
			handleProjectElemExculde(project);
		}
	}
	
	public void handleProjectElemExculde(IProject project) {
		// TODO Auto-generated method stub
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
		if(local_prjd!=null) {
			
			ICProjectDescriptionManager prjDescMgr = CCorePlugin.getDefault().getProjectDescriptionManager();
			ICProjectDescriptionWorkspacePreferences prefs = prjDescMgr
					.getProjectDescriptionWorkspacePreferences(true);
			prefs.setConfigurationRelations(ICProjectDescriptionPreferences.CONFIGS_LINK_SETTINGS_AND_ACTIVE);
			prjDescMgr.setProjectDescriptionWorkspacePreferences(prefs, false, new NullProgressMonitor());
			
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
				System.out.println("sBoard:   "+sBoard.getBoardName());
				//处理组件
				if (sBoard != null) {
//					System.out.println("getBoardName：  "+sBoard.getBoardName());
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
								System.out.println("noSelect:   "+relativePath);
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
						List<File> excludeCompFiles = gecf.getNonCompFiles(onBoardCpu, sBoard);
						for(File f:excludeCompFiles) {
//							System.out.println("f.getName():   "+f.getName());
							String relativePath = f.getPath().replace("\\", "/").replace(srcLocation, "");
//							System.out.println("excludeCompFiles:   "+relativePath);
							if(f.isDirectory()) {
								IFolder ifolder = project.getFolder("src/libos"+relativePath);
								for (int j = 0; j < conds.length; j++) {
									linkHelper.setExclude(ifolder, conds[j], true);
								}
							}else {
								IFile ifile = project.getFile("src/libos"+relativePath);
								for (int j = 0; j < conds.length; j++) {
									linkHelper.setFileExclude(ifile, conds[j], true);
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
					//排除新增的板件 修改为:排除不是本工程的板件
					for(Board board:boards) {
//						if(!boardNames.contains(board.getBoardName())) {
						if(!boardName.equals(board.getBoardName())) {
//							System.out.println("ExcludeBoard:   "+board.getBoardName());
							String boardPath = board.getBoardPath().replace("\\", "/");
							String relativePath = boardPath.replace(srcLocation, "");
							IFolder ifolder = project.getFolder("src/libos"+relativePath);
							for (int j = 0; j < conds.length; j++) {
								linkHelper.setExclude(ifolder, conds[j], true);
							}
						}
					}
					
					//排除没有xml文件的板件，即不是一个完整的板件
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
					//修改为:排除不是本工程的Cpu
					for(Cpu cpu:allCpus) {
						if(!cpuName.equals(cpu.getCpuName())) {
							String cpuPath =  cpu.getParentPath().replace("\\", "/");
							File cpuFolder = new File(cpuPath);
							List<IFolder> folders = new ArrayList<IFolder>();
							getFolders(project,folders,cpuFolder,cpuName);
							for(IFolder folder:folders) {//srm32f7123,f7,stm32
								for (int j = 0; j < conds.length; j++) {
									linkHelper.setExclude(folder, conds[j], true);
								}
							}
						}
					}
					//排除不是Cpu的文件夹
					GetNonCpuFiles gncf = new GetNonCpuFiles();
					List<File> excludeCpuFiles = gncf.getNonCpus();
					for(File f:excludeCpuFiles) {
						String relativePath = f.getPath().replace("\\", "/").replace(srcLocation, "");
						IFolder ifolder = project.getFolder("src/libos"+relativePath);
						for (int j = 0; j < conds.length; j++) {
							linkHelper.setExclude(ifolder, conds[j], true);
						}
					}
					//打开本工程的Cpu
					for(Cpu cpu:allCpus) {
						if(cpuName.equals(cpu.getCpuName())) {
							String cpuPath =  cpu.getParentPath().replace("\\", "/");
							File cpuFolder = new File(cpuPath);
							List<IFolder> folders = new ArrayList<IFolder>();
							getFolders(project,folders,cpuFolder,cpuName);//stm32f7123,f7,stm32
							for(int i=folders.size()-1;i>=0;i--) {//stm32,f7,stm32f7123
								System.out.println("folders.get(i):  "+folders.get(i).getName());
								for (int j = 0; j < conds.length; j++) {
									if(conds[j].getName().startsWith("libos")) {
										linkHelper.setExclude(folders.get(i), conds[j], false);
									}
								}
							}
						}
					}
					
				}
				createNewFile(cpuInfoFile);
				createCpuInfo.createCpuInfo(cpuInfoFile, allCpus);
				
				//排除不是本工程的Arch
				Cpu myCpu = null;
				for(Cpu cpu:allCpus) {
					if(cpu.getCpuName().equals(onBoardCpu.getCpuName())) {
						myCpu = cpu;
					}
				}
				String archType = myCpu.getCores().get(0).getArch().getSerie();
				File archSourceFile = new File(dideHelper.getDjyosSrcPath() + "/bsp/arch");
				File[] archTypeFiles = archSourceFile.listFiles();
				System.out.println("archType:  "+archType);
				IFolder myArchfolder = null;
				for(File f:archTypeFiles) {
					String relativePath = f.getPath().replace("\\", "/").replace(srcLocation, "");
					IFolder ifolder = project.getFolder("src/libos"+relativePath);
					if(!f.getName().equals(archType) && f.isDirectory()) {
						for (int j = 0; j < conds.length; j++) {
							if(conds[j].getName().startsWith("libos")) {
								linkHelper.setExclude(ifolder, conds[j], true);
							}
						}
					}else if(f.getName().equals(archType) && f.isDirectory()) {
						myArchfolder = ifolder;
					}
				}
				if(myArchfolder!=null) {
					for (int j = 0; j < conds.length; j++) {
						if(conds[j].getName().startsWith("libos")) {
							linkHelper.setExclude(myArchfolder, conds[j], false);
						}
					}
				}
				
			}
			
			try {
				CoreModel.getDefault().setProjectDescription(project, local_prjd);
			} catch (CoreException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	//获取不存在配置文件的板件
	private List<IFolder> getNonBoardFolders(IProject project){
		List<IFolder> folders = new ArrayList<IFolder>();
		List<String> paths = new ArrayList<String>();
		String userBoardFilePath = dideHelper.getUserBoardFilePath();
		String demoBoardFilePath = dideHelper.getDemoBoardFilePath();
		paths.add(userBoardFilePath);
		paths.add(demoBoardFilePath);
		for (int i = 0; i < paths.size(); i++) {
			File boardFile = new File(paths.get(i));
			if(boardFile.exists()) {
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
		if(!parentFile.getName().equals("cpudrv")) {
			getFolders(project,folders,parentFile,cpuName);
		}
		
	}

//	private boolean toInclude(File parentFile, String cpuName) {
//		// TODO Auto-generated method stub
//		File[] files = parentFile.listFiles();
//		for(File f:files) {
//			if(f.getName().equals(cpuName)) {
//				return true;
//			}
//		}
//		return false;
//	}

}
