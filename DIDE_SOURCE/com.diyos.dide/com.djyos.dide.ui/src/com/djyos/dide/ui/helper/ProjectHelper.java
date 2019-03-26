package com.djyos.dide.ui.helper;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
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
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;

import com.djyos.dide.ui.messages.IComponentConstants;
import com.djyos.dide.ui.objects.Board;
import com.djyos.dide.ui.objects.Component;
import com.djyos.dide.ui.objects.Core;
import com.djyos.dide.ui.objects.Cpu;
import com.djyos.dide.ui.wizards.component.ComponentHelper;
import com.djyos.dide.ui.wizards.djyosProject.DjyosMessages;
import com.djyos.dide.ui.wizards.djyosProject.tools.FileTool;
import com.djyos.dide.ui.wizards.djyosProject.tools.IncludesLinkTool;
import com.djyos.dide.ui.wizards.djyosProject.tools.PathTool;
import com.djyos.dide.ui.wizards.djyosProject.tools.ProjectPattern;

public class ProjectHelper implements IComponentConstants{

	public static boolean haveIboot(int type) {
		if (type == 0 || type == 1) {
			return true;
		}
		return false;
	}
	
	public static boolean haveApp(int type) {
		if (type == 0 || type == 2 || type == 3) {
			return true;
		}
		return false;
	}
	

	static IProject cur_project = null;
	public static IProject importProject(int tIndex, String projectName,String templateName,String projectPath,Board board, Core core
			,boolean haveApp, boolean needIbootLds) {
		// TODO Auto-generated method stub
		String srcPath = PathTool.getTemplatePath() + "/"
				+ core.getArch().getToolchain().replaceAll("\\s+", "-") + "/" + templateName;// 模板的路径
		File srcFile = new File(srcPath);
		File destFile = new File(projectPath);
		if (!srcFile.exists()) {
			DideHelper.showErrorMessage("系统不支持 [" + templateName + "] 类型的工程，请联系开发人员!");
		} else {
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
						importExistingProject(subMonitor.split(1), projectName, projectPath);
						if (!status.isOK()) {
							throw new InvocationTargetException(new CoreException(status));
						}
						String boardFolderPath = board.getBoardFolderPath();
						ProjectHelper.fill_Lds(projectPath, boardFolderPath, haveApp, needIbootLds, core, tIndex);
					}
				};
				
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						ProgressMonitorDialog dialog = new ProgressMonitorDialog(
								window.getShell());
						dialog.setCancelable(false);
						try {
							dialog.run(true, true, op);
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
		return cur_project;

	}
	
	private static void importExistingProject(IProgressMonitor mon, String projectName,String destPath) {

		SubMonitor subMonitor = SubMonitor.convert(mon, 3);
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		
		IProject project = workspace.getRoot().getProject(projectName);
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
		cur_project =  project;
	}
	
	
	public static void reviseConfiguration(int index, ICConfigurationDescription[] conds,Board board, Cpu cpu, Core core, 
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
				ComponentHelper.linkComponentGUN(appCompontentsChecked, links, includes, isDemoBoard, cpudrvFiles, srcLocation,
						assemblyLinks, rds, core);
			} else{
				ComponentHelper.linkComponentGUN(ibootCompontentsChecked, links, includes, isDemoBoard, cpudrvFiles, srcLocation,
						assemblyLinks, rds, core);
			}
			ProjectHelper.reviseSettings(toolchain, core);

		}
	}
	
	public static void fill_Lds(String destPath, String boardFolderPath, boolean haveApp, boolean needIbootLds, Core core, int tIndex) {
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
	
	public static void handleInitProject(List<Component> typeCompontentsChecked, List<Component> typeCheckedSort,
			String projectLocation, boolean isApp) {
		// TODO Auto-generated method stub
		String content = "", firstInit = "\tuint16_t evtt_main;\r\n\r\n", lastInit = "", gpioInit = "", djyMain = "",
				shellInit = "";
		String earlyCode = "", mediumCode = "", laterCode = "";
		String initHead = DjyosMessages.Automatically_Generated;
		initHead += "#include \"project_config.h\"\r\n" + "#include \"djyos.h\"\r\n" + "#include \"stdint.h\"\r\n"
				+ "#include \"stddef.h\"\r\n" + "#include \"cpu_peri.h\"\r\n" + "extern ptu32_t djy_main(void);\r\n";
		File file = new File(projectLocation + "/src/" + (isApp ? "app" : "iboot") + "/initPrj.c");
		FileTool.createNewFile(file);
		if(typeCheckedSort.size() != 0) {
			for (int i = 0; i < typeCompontentsChecked.size(); i++) {
				handleDependents(typeCompontentsChecked.get(i), typeCompontentsChecked, typeCheckedSort);
				if (!typeCheckedSort.contains(typeCompontentsChecked.get(i))) {
					typeCheckedSort.add(typeCompontentsChecked.get(i));
				}
			}
		}
		for (int i = 0; i < typeCheckedSort.size(); i++) {
			Component c = typeCheckedSort.get(i);
			if (c.isSelect()) {
				String grade = c.getGrade();
				String code = c.getCode();
				String componentName = c.getName();
				List<String> dependents = c.getDependents();

				// 添加分区参数
				String[] configures = c.getConfigure().split("\n");
				String tag = null;
				List<String> paraNames = new ArrayList<String>();
				for (String parameter : configures) {
					if (DideHelper.isParaHead(parameter)) {
						tag = DideHelper.getTag(parameter, tag);
					}
					String[] members = parameter.split("\\s+");
					if (parameter.contains("#define") && tag.equals("obj_para")) {
						paraNames.add(members[1]);
					}
				}

				String codeStrings = "";
				if (code != null) {
					String[] codes = code.split("\n");
					for (int j = 0; j < codes.length; j++) {
						if (codes[j].contains("#include")) {
							initHead += codes[j].trim() + "\r\n";
						} else {
							// 如果函包含可变参，则将配置好的参数替换...
							if (codes[j].contains("...") && paraNames.size() > 0) {
								String replaceParas = "";
								for (String name : paraNames) {
									if (name.equals(paraNames.get(paraNames.size() - 1))) {
										replaceParas += name;
									} else {
										replaceParas += name + ", ";
									}

								}
								codes[j] = codes[j].replace("...", replaceParas);
							}
							codeStrings += "\t" + codes[j].trim() + "\r\n";
						}
					}
				}

				if (grade != null && code != null && !codeStrings.trim().equals("")) {
					if (dependents.contains("cpu_peri_gpio")) {
						gpioInit += codeStrings + "\r\n";
					} else if (componentName.equals("heap")) {
						lastInit += evttMain + codeStrings + "\r\n";
					} else if (componentName.equals("shell")) {
						shellInit += codeStrings + "\r\n";
					} else {
						if (grade.equals("early")) {
							earlyCode += codeStrings + "\r\n";
						} else if (grade.equals("medium")) {
							mediumCode += codeStrings + "\r\n";
						} else if (grade.equals("later")) {
							laterCode += codeStrings + "\r\n";
						}
					}
				}
			}

		}
		content += initHead;
		content += "\r\n" + djyStart + djyMain + djyEnd;
		content += initStart + firstInit + gpioInit + shellInit
				+ "\t//-------------------early-------------------------//\r\n" + earlyCode
				+ "\t//-------------------medium-------------------------//\r\n" + mediumCode
				+ "\t//-------------------later-------------------------//\r\n" + laterCode + lastInit + initEnd;
		FileTool.writeFile(file, content,false);
		ComponentHelper.createCheckXml(isApp, projectLocation, typeCompontentsChecked, typeCompontentsChecked);

	}
	
	private static void handleDependents(Component component, List<Component> typeCompontentsChecked,
			List<Component> typeCheckedSort) {
		// TODO Auto-generated method stub
		List<String> dependents = component.getDependents();
		for (String dep : dependents) {
			for (int j = 0; j < typeCompontentsChecked.size(); j++) {
				Component c = typeCompontentsChecked.get(j);
				if (dep.equals(c.getName())) {
					if (!typeCheckedSort.contains(c)) {
						if (c.getDependents().contains(component.getName())) {
							typeCheckedSort.add(c);
						}
						handleDependents(c, typeCompontentsChecked, typeCheckedSort);
						if (!typeCheckedSort.contains(c)) {
							typeCheckedSort.add(c);
						}
					}
					break;
				}
			}
		}
	}
	
	public static void reviseSettings(IToolChain toolchain, Core core) {
		// TODO Auto-generated method stub
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
	
	public static void resetTargetName(ICConfigurationDescription[] conds, String projectName) {
		for (ICConfigurationDescription cfgDesc : conds) {
			String s = cfgDesc.getName();
			if (s.contains(DjyosMessages.Configuration_Debug) || s.contains(DjyosMessages.Configuration_Release)) {
				if (!s.contains("libos")) {
					cfgDesc.setName(projectName + "_" + s);
				}
			}
		}
	}
	
	public static String reset_configure(String para_key, String para_value, String configure) {
		// TODO Auto-generated method stub
		
		String[] parameters = configure.split("\n");
		String tag = null;
		String cfg = "";
		for (int i = 0; i < parameters.length; i++) {
			String parameter = parameters[i].trim();
			if (DideHelper.isParaHead(parameter)) {
				tag = DideHelper.getTag(parameter, tag);
			}else if (parameter.contains("#define") && !tag.equals("obj_para")  && !tag.equals("symbol")) {
				if(parameter.contains(para_key)) {
					String[] members = parameter.split("//");
					parameter = parameter.split(para_key)[0] + String.format("%-32s", para_key) 
								+ String.format("%-18s",para_value)
								+ "//" + members[members.length-1];
				}
			}
			cfg += parameter+"\n";
		}
		return cfg;
	}
	
}
