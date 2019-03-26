package com.djyos.dide.ui.autotesting;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;

import com.djyos.dide.ui.arch.ArchHelper;
import com.djyos.dide.ui.git.GitPromptDialog;
import com.djyos.dide.ui.git.GitUriDialog;
import com.djyos.dide.ui.helper.CpuHelper;
import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.helper.LinkHelper;
import com.djyos.dide.ui.helper.ProjectHelper;
import com.djyos.dide.ui.messages.IComponentConstants;
import com.djyos.dide.ui.objects.Board;
import com.djyos.dide.ui.objects.Component;
import com.djyos.dide.ui.objects.Core;
import com.djyos.dide.ui.objects.Cpu;
import com.djyos.dide.ui.objects.OnBoardCpu;
import com.djyos.dide.ui.wizards.board.BoardHelper;
import com.djyos.dide.ui.wizards.board.ReadBoardXml;
import com.djyos.dide.ui.wizards.component.ComponentHelper;
import com.djyos.dide.ui.wizards.component.GetNonCompFiles;
import com.djyos.dide.ui.wizards.component.ReadComponent;
import com.djyos.dide.ui.wizards.cpu.GetNonCpuFiles;
import com.djyos.dide.ui.wizards.cpu.ReadCpuXml;
import com.djyos.dide.ui.wizards.djyosProject.CreateHardWareDesc;
import com.djyos.dide.ui.wizards.djyosProject.DjyosMessages;
import com.djyos.dide.ui.wizards.djyosProject.info.CreateBoardInfo;
import com.djyos.dide.ui.wizards.djyosProject.info.CreateComponentInfo;
import com.djyos.dide.ui.wizards.djyosProject.info.CreateCpuInfo;
import com.djyos.dide.ui.wizards.djyosProject.tools.FileTool;
import com.djyos.dide.ui.wizards.djyosProject.tools.IncludesLinkTool;
import com.djyos.dide.ui.wizards.djyosProject.tools.PathTool;
import com.djyos.dide.ui.wizards.djyosProject.tools.ProjectPattern;
import com.djyos.dide.ui.wizards.djyosProject.tools.ReviseVariableToXML;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class ProjectAutomation extends AbstractHandler implements IComponentConstants{
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		prepare_project();
		return null;
	}
	
	public static void prepare_project() {
		File project_test_file = new File(PathTool.getDjyosPath()+"/project_creation");
		if(project_test_file.exists()) {
			File[] fs = project_test_file.listFiles();
			for(File f:fs) {
				if(f.getName().endsWith(".json")) {
					String json_str = FileTool.readFile(f);
					ProjectObj obj = new ProjectObj();
					try {
						JSONObject jsonObject = JSONObject.fromObject(json_str);
						
						obj.setName(jsonObject.getString("project_name"));
						obj.setType(jsonObject.getInt("project_type"));
						obj.setCore_colck(jsonObject.getInt("core_clock"));
						
						Board board = DideHelper.getBoardByName(jsonObject.getString("board"));
						obj.setBoard(board);
						Cpu cpu = DideHelper.getCpuByName(jsonObject.getString("cpu"));
						obj.setCpu(cpu);
						for(Core c:cpu.getCores()) {
							if(c.getName().equalsIgnoreCase(jsonObject.getString("core"))) {
								obj.setCore(c);
							}
						}
						String compt_json = jsonObject.getString("component");
						JSONObject compt_jsonObject = JSONObject.fromObject(compt_json);
						Iterator keys = compt_jsonObject.keys();
						List<Component> all_components = ReadComponent.getWorkspaceComponents();
						List<Component> need_components = new ArrayList<Component>();
						while (keys.hasNext()){
				            String key = String.valueOf(keys.next());
				            Component c = ComponentHelper.getComponentByName(key, all_components);
				            String configure = c.getConfigure();
				            //通过通过刚刚得到的key值去解析后面的json了
				            String para_json = compt_jsonObject.getString(key);
				            JSONObject para_jsonObject = JSONObject.fromObject(para_json);
				            Iterator para_keys = para_jsonObject.keys();
				            while (para_keys.hasNext()){
				            	String para_key = String.valueOf(para_keys.next());
				            	String para_value = para_jsonObject.get(para_key).toString();
				            	configure = ProjectHelper.reset_configure(para_key,para_value,configure);
				            }
				            c.setConfigure(configure);
				            c.setSelect(true);
				            need_components.add(c);
				        }
						obj.setComponents(need_components);
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					create_project(obj);
				}
			}
		}
	}

	/**
	 * 自动创建工程
	 * @param obj
	 */
	private static void create_project(ProjectObj obj) {
		// TODO Auto-generated method stub
		String projectPath = Platform.getInstanceLocation().getURL().getPath()+obj.getName();
		File workspace_file = new File(Platform.getInstanceLocation().getURL().getPath());
		if(!workspace_file.exists()) {
			workspace_file.mkdir();
		}
		String[] templateNames = {"ibootapp","iboot","App","Apponly"};
		ProjectHelper.importProject(obj.getType(),obj.getName(),templateNames[obj.getType()],projectPath,
				obj.getBoard(),obj.getCore(),ProjectHelper.haveApp(obj.getType()),ProjectHelper.haveIboot(obj.getType()));
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		performFinish(obj,projectPath);
	}

	private static void performFinish(ProjectObj obj, String projectLocation) {
		// TODO Auto-generated method stub
		Cpu cpu = obj.getCpu();
		Board board = obj.getBoard();
		Core core = obj.getCore();
		int index = obj.getType();
		
		String projectName = obj.getName();
//		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
//		System.out.println(project.getLocation()+"  "+project.getFullPath());
//		String projectLocation = project.getLocation().toString();
		File dataFolder = new File(projectLocation + "/data");
		if (!dataFolder.exists()) {
			dataFolder.mkdirs();
		}
		
		List<Component> compontentsChecked = obj.getComponents();
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
				List<Component> cptCheckedSort = new ArrayList<Component>();
				if (index == 0 || index == 1) {
					try {
						initProject(compontentsChecked,cptCheckedSort,projectLocation, false);
						creatProjectConfiure(cptCheckedSort,projectLocation, core, false, index);
					} catch (Exception e) {
						DideHelper.showErrorMessage("配置Iboot初始化文件错误：" + e.getMessage() + "   " + e.getLocalizedMessage()
								+ "\n" + e.getCause());
					}
				}
				if (index == 0 || index == 2 || index == 3) {
					try {
						initProject(compontentsChecked,cptCheckedSort,projectLocation, true);
						creatProjectConfiure(cptCheckedSort,projectLocation, core, true, index);
					} catch (Exception e) {
						DideHelper.showErrorMessage("配置App初始化文件错误：" + e.getMessage());
					}
				}
				// 处理工程的链接
				monitor.setTaskName("配置工程链接...");

				if (cpu != null && board != null && core != null) {
					List<Component> compontentsList = ReadComponent.getAllComponents(cpu, board);
					for(Component c:compontentsList) {
						if(c.getAttribute().equalsIgnoreCase("system") || c.getSelectable().equalsIgnoreCase("required")) {
							boolean add = true;
							for(Component c1:compontentsChecked) {
								if(c1.getName().equalsIgnoreCase(c.getName())) {
									add = false;
									break;
								}
							}
							if(add) {
								if(c.getSelectable().equalsIgnoreCase("required")) {
									c.setSelect(true);
									add_depedents(compontentsList,compontentsChecked,c);
								}
								compontentsChecked.add(c);
							}
						}
					}
					handleCProject(compontentsList,index,compontentsChecked, compontentsChecked, board, cpu, core, projectLocation,
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
				monitor.done();
			}

			private void add_depedents(List<Component> compontentsList,List<Component> compontentsChecked, Component c) {
				// TODO Auto-generated method stub
				List<String> depedents = c.getDependents();
				if(depedents.size() > 0) {
					for(String s:depedents) {
						Component component = ComponentHelper.getComponentByName(s, compontentsList);
						add_depedents(compontentsList,compontentsChecked,component);
					}
				}
			}
		};

		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(
						PlatformUI.getWorkbench().getDisplay().getActiveShell());
				dialog.setCancelable(false);
				try {
					dialog.run(true, true, runnable);
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
	}
	
	public static void handleCProject(List<Component> compontentsList, int index, List<Component> appCompontentsChecked, List<Component> ibootCompontentsChecked,
			Board board, Cpu cpu, Core core, String projectPath, String projectName) {
		String _cpuName = cpu.getCpuName();
		File cpudrvFile = new File(PathTool.getDIDEPath() + DjyosMessages.Cpu_RelativePath);
		File boardDemoFile = new File(PathTool.getDemoBoardFilePath());
		File archSourceFile = new File(PathTool.getDjyosSrcPath() + "/bsp/arch");
		File boardFolder = new File(board.getBoardFolderPath());
		List<File> archXmlFiles = DideHelper.getArchXmlFiles(archSourceFile, new ArrayList<File>());
		File[] cpudrvFiles = cpudrvFile.listFiles();

		boolean isDemoBoard = BoardHelper.isDemoBoard(boardDemoFile, boardFolder);
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
//		if (!projectPath.contains(projectName)) {
//			project = curProject;
//		}
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
		ProjectHelper.resetTargetName(conds, projectName);

		// 开始添加链接
		ArchHelper.handArchLinks(core, archSourceFile, archXmlFiles, project, conds);
		// 给每个Configuration修改配置,增加链接
		ProjectHelper.reviseConfiguration(index,conds,board,cpu,core,isDemoBoard,
				appCompontentsChecked, ibootCompontentsChecked, includes,  PathTool.getDjyosSrcPath(), cpudrvFiles);
		// 组件操作
		List<OnBoardCpu> onBoardCpus = board.getOnBoardCpus();
		OnBoardCpu onBoardCpu = DideHelper.getOnBoardByCpu(onBoardCpus, cpu.getCpuName());
		
		File compInfoFile = new File(project.getLocation().toString() + "/data/hardwares/component_infos.xml");
		FileTool.createNewFile(compInfoFile);
		CreateComponentInfo.createComponentInfo(compInfoFile, compontentsList);
		
		// 排除所有没有描述文件的组件
		List<File> excludeCompFiles = GetNonCompFiles.getNonCompFiles(onBoardCpu, board);
		for (File f : excludeCompFiles) {
			String relativePath = PathTool.resetPath(f.getPath()).replace( PathTool.getDjyosSrcPath(), "");
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
			ComponentHelper.linkComponentResource(true, appCompontentsChecked, compontentsList, PathTool.getDjyosSrcPath(), project, conds);
		}
		if(ibootCompontentsChecked.size() > 0) {
			ComponentHelper.linkComponentResource(false, ibootCompontentsChecked, compontentsList,  PathTool.getDjyosSrcPath(), project, conds);
		}

		List<Board> allBoards = ReadBoardXml.getAllBoards();
		File boardInfoFile = new File(project.getLocation().toString() + "/data/hardwares/board_infos.xml");
		FileTool.createNewFile(boardInfoFile);
		CreateBoardInfo.createBoardInfo(boardInfoFile, allBoards);
		if (isDemoBoard) {
			BoardHelper.setBoardExclude(true, boardFolder.getName(), project, conds);
		} else {
			BoardHelper.setBoardExclude(false, boardFolder.getName(), project, conds);
		}

		List<Cpu> allCpus = ReadCpuXml.getAllCpus();
		// 保存所有的cpu信息
		File cpuInfoFile = new File(project.getLocation().toString() + "/data/hardwares/cpu_infos.xml");
		FileTool.createNewFile(cpuInfoFile);
		CreateCpuInfo.createCpuInfo(cpuInfoFile, allCpus);

		Cpu myCpu = DideHelper.getCpuByName(_cpuName);
		CpuHelper.setCpuFilesExclude(project, conds, myCpu, allCpus);

		DideHelper.saveProjectDescription(project, local_prjd);
	}
	
	public static void creatProjectConfiure(List<Component> cptCheckedSort, String projectLocation, Core core, boolean isApp, int index) {
		String cfgPath = projectLocation + "/src/"+(isApp?"app":"iboot")+"/OS_prjcfg/project_config.h";
		File file = new File(cfgPath);
		String coreConfigure = String.format("%-9s", "#define") + String.format("%-32s", "CFG_CORE_MCLK")
		+ String.format("%-18s", "(" + core.getCoreClk() + "*Mhz)") + "//主频，内核要用，必须定义";
		ComponentHelper.creatProjectConfiure(file, coreConfigure, isApp, cptCheckedSort, cptCheckedSort, index);
	}

	public static void initProject(List<Component> compontentsChecked, List<Component> cptCheckedSort, String projectLocation, boolean isApp) {
		ProjectHelper.handleInitProject(compontentsChecked, cptCheckedSort, projectLocation, isApp);
	}

}
