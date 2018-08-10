package org.eclipse.cdt.ui.startup;

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
import org.eclipse.cdt.ui.wizards.board.Board;
import org.eclipse.cdt.ui.wizards.board.ReadBoardXml;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.OnBoardCpu;
import org.eclipse.cdt.ui.wizards.component.Component;
import org.eclipse.cdt.ui.wizards.component.ReadComponent;
import org.eclipse.cdt.ui.wizards.cpu.Cpu;
import org.eclipse.cdt.ui.wizards.cpu.ReadCpuXml;
import org.eclipse.cdt.ui.wizards.djyosProject.ReadHardWareDesc;
import org.eclipse.cdt.ui.wizards.djyosProject.info.CreateBoardInfo;
import org.eclipse.cdt.ui.wizards.djyosProject.info.CreateComponentInfo;
import org.eclipse.cdt.ui.wizards.djyosProject.info.CreateCpuInfo;
import org.eclipse.cdt.ui.wizards.djyosProject.info.ReadBoardsInfo;
import org.eclipse.cdt.ui.wizards.djyosProject.info.ReadComponentsInfo;
import org.eclipse.cdt.ui.wizards.djyosProject.info.ReadCpusInfo;
import org.eclipse.cdt.ui.wizards.djyosProject.tools.DideHelper;
import org.eclipse.cdt.ui.wizards.djyosProject.tools.LinkHelper;
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
	ReadComponent rcp = new ReadComponent();
	ReadCpuXml rcx = new ReadCpuXml();
	ReadBoardXml rbx = new ReadBoardXml();
	ReadHardWareDesc rhwd = new ReadHardWareDesc();
	List<Cpu> allCpus = null;
	List<Board> boardsList = null;
	ReadCpusInfo readCpusInfo = new ReadCpusInfo();
	ReadBoardsInfo readBoardsInfo = new ReadBoardsInfo();
	ReadComponentsInfo ReadComponentsInfo = new ReadComponentsInfo();
	CreateComponentInfo createComponentInfo = new CreateComponentInfo();
	CreateBoardInfo createBoardInfo = new CreateBoardInfo();
	CreateCpuInfo createCpuInfo = new CreateCpuInfo();
	ReadComponent rc = new ReadComponent();
	private String didePath = new DideHelper().getDIDEPath();
	String srcLocation = didePath+"djysrc";
	
	public void handleAddeed() {
		
		allCpus = rcx.getAllCpus();
		boardsList = rbx.getAllBoards();
		IProject[] projects = workspace.getRoot().getProjects();
		File boardFile = new File(dideHelper.getDjyosSrcPath()+"/boarddrv");
		for(IProject project:projects) {
			final ICProjectDescription local_prjd =  CoreModel.getDefault().getProjectDescription(project);
			ICConfigurationDescription[] conds = local_prjd.getConfigurations();	//获取工程的所有Configuration	
			File compInfoFile = new File(project.getLocation().toString()+"/data/hardwares/component_infos.xml");
			File cpuInfoFile = new File(project.getLocation().toString()+"/data/hardwares/cpu_infos.xml");
			File boardInfoFile = new File(project.getLocation().toString()+"/data/hardwares/board_infos.xml");
			File hardWardFolder = new File(project.getLocation().toString()+"/data/hardwares");
			if(!hardWardFolder.exists()) {
				hardWardFolder.mkdirs();
			}
			File hardWardInfoFile = new File(project.getLocation().toString()+"/data/hardware_info.xml");
			
			List<String> hardwares;
			if(hardWardInfoFile.exists()) {
//				for(int i=0;i<conds.length;i++) {
//					String conName = conds[i].getName();
//					ICResourceDescription rds = conds[i].getRootFolderDescription();
//					IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(rds.getConfiguration());
//					if(!conName.contains("libos")) {
//						cfg.setPostbuildStep("make "+conName+".bin && elf_to_bin.exe "+conName+".elf "+conName+".bin && ren "+conName+".bin new"+conName+".bin");	
//					}
//				}
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
						List<String> compPaths = readCpusInfo.getCpusInfo(compInfoFile);
						
						for (Component component : allCompontents) { 
							String componentPath = component.getParentPath().replace("\\", "/");
							String fileName = component.getFileName();
							String relativePath = componentPath.replace(srcLocation, "");
							String compPath = relativePath+"/"+fileName;
							if(!compPaths.contains(compPath)) {
								if(fileName.endsWith(".c")) {
									IFile ifile = project.getFile("src/libos"+compPath);
									for (int j = 0; j < conds.length; j++) {
										linkHelper.setFileExclude(ifile, conds[j], true);
									}
									
								}else if(fileName.endsWith(".h")) {
									IFolder ifolder = project.getFolder("src/libos"+relativePath);
									for (int j = 0; j < conds.length; j++) {
										linkHelper.setExclude(ifolder, conds[j], true);
									}
								}
							}
							
						}
					}
					createNewFile(compInfoFile);
					createComponentInfo.createComponentInfo(compInfoFile, allCompontents);
				}

				if(boardInfoFile.exists()) {
					List<String> boardNames = readBoardsInfo.getBoardsInfo(boardInfoFile);
					for(Board board:boards) {
						System.out.println("board:   "+board.getBoardName());
						if(!boardNames.contains(board.getBoardName())) {
							System.out.println("ExcludeBoard:   "+board.getBoardName());
							String boardPath = board.getBoardPath().replace("\\", "/");
							String relativePath = boardPath.replace(srcLocation, "");
							IFolder ifolder = project.getFolder("src/libos"+relativePath);
							for (int j = 0; j < conds.length; j++) {
								linkHelper.setExclude(ifolder, conds[j], true);
							}
						}
					}
				}
				createNewFile(boardInfoFile);
				createBoardInfo.createBoardInfo(boardInfoFile, boards);
				
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

//	private void travelFiles(IProject project, ICConfigurationDescription[] conds, File boardFile) {
//		File[] childFiles = boardFile.listFiles();
//		List<String> includePaths = new ArrayList<String>();
//		for(File file:childFiles) {
//			String fileName = file.getName();
//			if(file.isDirectory()) {
//				if (!fileName.equals("include") && !fileName.equals("startup") && !fileName.equals("src")) {
//					String relativePath = file.getPath().replace(dideHelper.getDjyosSrcPath(), "");
//					IFolder folder = project.getFolder("src/libos"+relativePath);
//					for (int i = 0; i < conds.length; i++) {
//						if (conds[i].getName().contains("libos")) {
//							ICResourceDescription resDesc = conds[i].getResourceDescription(folder.getProjectRelativePath(), true);
//							boolean isExclude = resDesc.isExcluded();
//							if(!isExclude) {
//								if(!includePaths.contains(relativePath)) {
//									linkHelper.setExclude(folder, conds[i], true);
//								}
////								travelFiles(project,conds,);
//							}else {
//								break;
//							}
//						}
//					}
//				}
//			}
//		}
//		
//	}
//	
//	@Override
//	public void resourceChanged(IResourceChangeEvent event) {
//		// TODO Auto-generated method stub
//		try {
//			event.getDelta().accept(new IResourceDeltaVisitor() {
//
//				@Override
//				public boolean visit(IResourceDelta delta) throws CoreException {
//					// TODO Auto-generated method stub
//					IResource resource = delta.getResource();
//					
//					switch(resource.getType()){
//					case IResource.PROJECT:
//						System.out.println("PROJECT.getName():   "+resource.getName());
//						break;
//					case IResource.FOLDER:
//						System.out.println("FOLDER.getName():   "+resource.getName());
//						break;
////					case IResource.FILE:
////						System.out.println("FILE.getName():   "+resource.getName());
//					case IResource.ROOT:
//						System.out.println("ROOT.getName():   "+resource.getName());
//						break;
//					}
//					if (resource instanceof IFolder ) {
//						IFolder cruFolder = (IFolder)resource;
//						if(cruFolder.getFullPath().toString().contains("/libos/")) {
//							IProject project = cruFolder.getProject();
//							final ICProjectDescription local_prjd =  CoreModel.getDefault().getProjectDescription(project);
//							ICConfigurationDescription[] conds = local_prjd.getConfigurations();	//获取工程的所有Configuration	
//							
//								switch (delta.getKind()) {
//								case IResourceDelta.COPIED_FROM:
//									System.out.println("IResourceDelta.COPIED_FROM:  "+cruFolder.getFullPath().toString());
//									;
//								case IResourceDelta.ADDED:
//									// handle added resource
////									System.out.println("IResourceDelta.ADDED:  "+cruFolder.getFullPath().toString());
////									for (int i = 0; i < conds.length; i++) {
////										if(conds[i].getName().contains("libos")) {
////											linkHelper.setExclude(cruFolder, conds[i], true);
////										}
////									}
////									try {
////										CoreModel.getDefault().setProjectDescription(project, local_prjd);
////										cruFolder.getParent().refreshLocal(IResource.DEPTH_INFINITE, null);
////		
////									} catch (Exception e1) {
////										// TODO Auto-generated catch block
////										e1.printStackTrace();
////									}
//									break;
//								case IResourceDelta.REMOVED:
//									// handle removed resource
////									System.out.println("IResourceDelta.REMOVED:  "+cruFolder.getFullPath().toString());
//									break;
//								case IResourceDelta.CHANGED:
//									// handle changed resource		
////									System.out.println("IResourceDelta.CHANGED:  "+cruFolder.getFullPath().toString());				
//									break;					
//								}
//						}
//					}
//					return true;
//				}
//				
//			});
//		} catch (CoreException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

}
