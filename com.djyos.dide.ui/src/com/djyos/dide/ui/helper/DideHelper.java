package com.djyos.dide.ui.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.actions.NewWizardShortcutAction;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.actions.BuildUtilities;
import org.eclipse.ui.wizards.IWizardDescriptor;

import com.djyos.dide.ui.arch.ReadArchXml;
import com.djyos.dide.ui.builder.BuildTarget;
import com.djyos.dide.ui.objects.Arch;
import com.djyos.dide.ui.objects.Board;
import com.djyos.dide.ui.objects.Component;
import com.djyos.dide.ui.objects.Core;
import com.djyos.dide.ui.objects.Cpu;
import com.djyos.dide.ui.objects.OnBoardCpu;
import com.djyos.dide.ui.wizards.board.ReadBoardXml;
import com.djyos.dide.ui.wizards.cpu.ReadCpuXml;
import com.ibm.icu.text.DecimalFormat;

@SuppressWarnings("restriction")
public class DideHelper {

	static String didePath = new File(System.getProperty("user.dir")).getParentFile().getPath().replace("\\", "/")
			+ "/";
	static DecimalFormat df = new DecimalFormat("######0");

	/********* * 与编译有关的函数* * *********/
	public static void buildTarget(IProject project, String targetName) {
		final ICProjectDescription local_prjd = CoreModel.getDefault().getProjectDescription(project);
		ICConfigurationDescription[] conds = local_prjd.getConfigurations();

		for (ICConfigurationDescription cfg : conds) {
			if (cfg.getName().equals(targetName)) {

				ICConfigurationDescription[] cfgds = new ICConfigurationDescription[1];
				cfgds[0] = cfg;

				BuildUtilities.saveEditors(null);
				Job buildJob = new BuildTarget(cfgds, 0, IncrementalProjectBuilder.INCREMENTAL_BUILD);
				buildJob.schedule();
			}
		}
	}

	public static boolean createBuild(IProject project) {
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration[] cfgs = info.getManagedProject().getConfigurations();

		for (IConfiguration cfg : cfgs) {
			String cfgName = cfg.getName();

			if (cfgName.startsWith("libos")) {
				File cfgFile = new File(project.getLocation().toString() + "/" + cfgName);
				boolean compiled = false;
				if (cfgFile.exists()) {
					File[] files = cfgFile.listFiles();
					for (File f : files) {
						if (f.getName().endsWith(".a")) {
							compiled = true;
							break;
						}
					}
				}

				if (!compiled) {
					buildTarget(project, cfgName);
				}
			}
		}
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public static boolean isAutoBuildNew() {
		return isAutoBuild("AUTO_BUILDLIBOS_NEW");
	}

	public static boolean isAutoBuildImport() {
		return isAutoBuild("AUTO_BUILDLIBOS_IMPORT");
	}

	private static boolean isAutoBuild(String target) {
		File didePrefsFile = new File(didePath + "IDE/configuration/.settings/com.djyos.ui.prefs");
		return targetIsTrue(didePrefsFile, target);
	}

	public static boolean targetIsTrue(File file, String target) {
		// TODO Auto-generated method stub
		BufferedReader br = null;
		String line = null;
		try {
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				// 修改内容核心代码
				if (line.startsWith(target)) {
					String[] infos = line.split("=");
					if (infos[1].trim().equals("false")) {
						return false;
					}
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					br = null;
				}
			}
		}
		return true;
	}

	public static String getFileContent(File file) {
		// TODO Auto-generated method stub
		BufferedReader br = null;
		String line = null;
		StringBuffer bufAll = new StringBuffer(); // 保存修改过后的所有内容，不断增加
		if (file.exists()) {
			try {
				br = new BufferedReader(new FileReader(file));
				while ((line = br.readLine()) != null) {
					bufAll.append(line + "\n");
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						br = null;
					}
				}
			}
		}
		return bufAll.toString();
	}

	public static void setFileContent(File file, String target, String value) {
		// TODO Auto-generated method stub
		BufferedReader br = null;
		String line = null;
		// boolean targetExist = false;
		StringBuffer bufAll = new StringBuffer(); // 保存修改过后的所有内容，不断增加
		try {
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				// 修改内容核心代码
				if (line.startsWith(target)) {
					line = target + " = " + value;
					// targetExist = true;
				}
				bufAll.append(line + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					br = null;
				}
			}
		}
		// if (!targetExist) {
		// bufAll.append(target + "=" + isTrue + "\n");
		// }
		file.delete();
		writeFile(file, bufAll.toString());

	}

	/********* * 与工具有关的函数* * *********/

	// 将long转成无符类型
	public static long toUnsigned(long s) {
		return s & 0xFFFFFFFFL;
	}

	public static boolean isFputypeuNeed(Core core) {
		// TODO Auto-generated method stub
		Arch curArch = core.getArch();
		File archSourceFile = new File(getDjyosSrcPath() + "/bsp/arch");
		List<File> archXmlFiles = getArchXmlFiles(archSourceFile, new ArrayList<File>());
		for (File f : archXmlFiles) {
			if (curArch.getMcpu() != null) {
				if (f.getParentFile().getName().equals(curArch.getMcpu())) {
					ReadArchXml rax = new ReadArchXml();
					Arch arch = new Arch();
					arch = rax.getMutiplyFileArch(f, arch);
					if (arch.getFpuType() != null) {
						return true;
					}
					break;
				}
			}
		}
		return false;
	}

	public static Board getBoardByName(String boardName) {
		List<Board> boards = ReadBoardXml.getAllBoards();
		for (Board b : boards) {
			if (b.getBoardName().equals(boardName)) {
				return b;
			}
		}
		return null;
	}

	public static Cpu getCpuByName(String cpuName) {
		List<Cpu> allCpus = ReadCpuXml.getAllCpus();
		for (Cpu c : allCpus) {
			if (c.getCpuName().equals(cpuName)) {
				return c;
			}
		}
		return null;
	}

	public static Cpu getCpuByonBoard(OnBoardCpu onBoardCpu, List<Cpu> allCpus) {
		for (Cpu c : allCpus) {
			if (c.getCpuName().equals(onBoardCpu.getCpuName())) {
				return c;
			}
		}
		return null;
	}

	public static OnBoardCpu getOnBoardByCpu(List<OnBoardCpu> onBoardCpus, Cpu cpu) {
		for (int i = 0; i < onBoardCpus.size(); i++) {
			if (onBoardCpus.get(i).getCpuName().equals(cpu.getCpuName())) {
				return onBoardCpus.get(i);
			}
		}
		return null;
	}

	public static boolean isDjysrcExist() {
		boolean djysrcExist = true;
		File djysrcFile = new File(getDjyosSrcPath());
		if (djysrcFile.exists()) {
			File[] files = djysrcFile.listFiles();
			if (files.length < 2) {
				djysrcExist = false;
			}
		} else {
			djysrcExist = false;
		}
		return djysrcExist;
	}

	// 根据id获取某个action
	public static IAction getAction(String id) {
		IAction action = null;
		IWizardDescriptor wizardDesc = WorkbenchPlugin.getDefault().getNewWizardRegistry().findWizard(id);
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (wizardDesc != null) {
			action = new NewWizardShortcutAction(window, wizardDesc);
			IConfigurationElement element = Adapters.adapt(wizardDesc, IConfigurationElement.class);
			if (element != null) {
				window.getExtensionTracker().registerObject(element.getDeclaringExtension(), action,
						IExtensionTracker.REF_WEAK);
			}
		}
		return action;
	}

	/********* * 与路径有关的函数* * *********/
	public static String getDIDEPath() {
		return didePath;
	}

	public static String getArchPath() {
		return didePath + "djysrc/bsp/arch";
	}

	public static String getDjyosCookiePath() {
		return didePath + "IDE/djyos/cookies";
	}

	public static String getStepByStepPath() {
		return didePath + "IDE/djyos/cookies/FileTemp/StepByStep";
	}

	// 获取模板的路径
	public static String getTemplatePath() {
		return System.getProperty("user.dir") + "/djyos/cookies";
	}

	// 获取Djysrc的路径
	public static String getDjyosSrcPath() {
		return didePath + "djysrc";
	}

	// 获取用户板件的路径
	public static String getUserBoardFilePath() {
		String userBoardPath = didePath + "djysrc/bsp/boarddrv/user";
		File userBoardFile = new File(userBoardPath);
		if (!userBoardFile.exists()) {
			userBoardFile.mkdirs();
		}
		return userBoardPath;
	}

	// 获取Djyos板件的绝对路径
	public static String getDemoBoardFilePath() {
		String demoBoardPath = didePath + "djysrc/bsp/boarddrv/demo";
		File demoBoardFile = new File(demoBoardPath);
		if (!demoBoardFile.exists()) {
			demoBoardFile.mkdirs();
		}
		return demoBoardPath;
	}

	// 获取用户板件的相对路径
	public static String getRelativeUserBoardFilePath() {
		return "${DJYOS_SRC_LOCATION}/bsp/boarddrv/user/";
	}

	// 获取Djyos板件的相对路径
	public static String getRelativeDemoBoardFilePath() {
		return "${DJYOS_SRC_LOCATION}/bsp/boarddrv/demo/";
	}

	/********* * 与文件操作有关的函数* * *********/
	public static void copyFolder(File src, File dest) {
		if (src.isDirectory()) {
			if (!dest.exists()) {
				dest.mkdir();
			}
			String files[] = src.list();
			for (String file : files) {
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				// 递归复制
				copyFolder(srcFile, destFile);
			}
		} else {
			try {
				InputStream in = new FileInputStream(src);
				OutputStream out = new FileOutputStream(dest);
				byte[] buffer = new byte[1024];
				int length;
				while ((length = in.read(buffer)) > 0) {
					out.write(buffer, 0, length);
				}
				in.close();
				out.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void openFileInDide(File file) {
		// TODO Auto-generated method stub
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(file.getParentFile().getPath()));
				fileStore = fileStore.getChild(file.getName());
				IFileInfo fetchInfo = fileStore.fetchInfo();
				if (!fetchInfo.isDirectory() && fetchInfo.exists()) {
					IWorkbenchPage page = window.getActivePage();
					try {
						IDE.openEditorOnFileStore(page, fileStore);
					} catch (PartInitException e1) {
						String msg = NLS.bind(IDEWorkbenchMessages.OpenLocalFileAction_message_errorOnOpen,
								fileStore.getName());
						IDEWorkbenchPlugin.log(msg, e1.getStatus());
						MessageDialog.open(MessageDialog.ERROR, window.getShell(),
								IDEWorkbenchMessages.OpenLocalFileAction_title, msg, SWT.SHEET);
					}
				}
			}
		});
	}

	public static void createNewFile(File file) {
		// TODO Auto-generated method stub
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String readFile(File file) {
		String encoding = "GBK";
		Long filelength = file.length();
		byte[] filecontent = new byte[filelength.intValue()];
		try {
			FileInputStream in = new FileInputStream(file);
			in.read(filecontent);
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			return new String(filecontent, encoding);
		} catch (UnsupportedEncodingException e) {
			System.err.println("The OS does not support " + encoding);
			e.printStackTrace();
			return null;
		}
	}

	// 将src目录下的所有文件拷贝到命名为boardName的dest目录下
	@SuppressWarnings("unused")
	private static void copyFileToFolder(File src, File dest, String boardName) throws IOException {
		if (src.isDirectory()) {
			if (!dest.exists()) {
				dest.mkdir();
				dest.renameTo(new File(dest.getAbsolutePath().substring(0, dest.getAbsolutePath().lastIndexOf("\\"))
						+ "\\" + boardName));
				dest = new File(dest.getAbsolutePath().substring(0, dest.getAbsolutePath().lastIndexOf("\\")) + "\\"
						+ boardName);
			}
			String files[] = src.list();
			for (String file : files) {
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				// 递归复制
				copyFileToFolder(srcFile, destFile, boardName);
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

	public static void writeFile(File file, String content) {
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		FileWriter writer;
		try {
			writer = new FileWriter(file);
			writer.write(content);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 当前目录及其子目录下是否包含xml文件
	public static boolean travelContainsXml(File file) {
		File[] files = file.listFiles();
		for (File f : files) {
			if (f.isDirectory()) {
				if (travelContainsXml(f)) {
					return true;
				}
			} else {
				if (f.getName().endsWith(".xml")) {
					return true;
				}
			}
		}
		return false;
	}

	// 当前目录下是否包含xml文件
	public static boolean isContainsXml(File file) {
		File[] files = file.listFiles();
		for (File f : files) {
			if (f.getName().endsWith(".xml")) {
				return true;
			}
		}
		return false;
	}

	public static List<File> getArchXmlFiles(File archFile, ArrayList<File> files) {
		// TODO Auto-generated method stub
		if (archFile != null) {
			File[] cFiles = archFile.listFiles();
			for (File f : cFiles) {
				if (f.isDirectory()) {
					getArchXmlFiles(f, files);
				} else {
					if (f.getName().equals("arch.xml")) {
						files.add(f);
					}
				}
			}
		}
		return files;
	}

	// 显示错误信息
	public static boolean showErrorMessage(String message) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				MessageDialog.openInformation(window.getShell(), "提示", message);
			}
		});
		return false;
	}

	// 获取某个文件夹下的xml文件
	public static File getXmlFile(File parentFile) {
		File file = null;
		File[] files = parentFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().endsWith(".xml")) {
				file = files[i];
			}
		}
		return file;
	}

	// 将某个文件夹下的文件按照文件夹...文件的方式排序
	public static List<File> sortFileAndFolder(File file) {
		File[] files = file.listFiles();
		List<File> allFiles = new ArrayList<File>();
		List<File> pureFiles = new ArrayList<File>();
		List<File> folderFiles = new ArrayList<File>();

		for (File f : files) {
			if (f.isDirectory()) {
				folderFiles.add(f);
			} else if (f.isFile() && (f.getName().endsWith(".c") || f.getName().endsWith(".h"))) {
				pureFiles.add(f);
			}
		}

		allFiles.addAll(folderFiles);
		allFiles.addAll(pureFiles);

		return allFiles;
	}

	/********* * 与测试有关的函数* * *********/
	public static void objdumpTest() {
		// String command="arm-none-eabi-objdump.exe -h F:\\djyos\\atomic.o";
		// String line = null;
		// StringBuilder sb = new StringBuilder();
		// Runtime runtime = Runtime.getRuntime();
		// try {
		// Process process = runtime.exec(command);
		// BufferedReader bufferedReader = new BufferedReader
		// (new InputStreamReader(process.getInputStream()));
		// while ((line = bufferedReader.readLine()) != null) {
		// sb.append(line + "\n");
		// System.out.println(line);
		// }
		// } catch (IOException e) {
		// // TODO 自动生成的 catch 块
		// e.printStackTrace();
		// }

		// long a = Integer.parseInt("1");
		// long b = Long.parseLong("0xFFFFFFFF".substring(2), 16);
		// long c = Integer.parseInt("-1");; //0xFFFFFFFF
		// if(a>b) {
		// System.out.println("a= "+toUnsigned(a));
		// System.out.println("b= "+toUnsigned(b));
		// System.out.println("c= "+toUnsigned(c));
		// System.out.println("a>b");
		// }else {
		// System.out.println("a= "+toUnsigned(a));
		// System.out.println("b= "+toUnsigned(b));
		// System.out.println("c= "+toUnsigned(c));
		// System.out.println("b>a");
		// }
	}

	public static void Restart_DIDE() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				boolean gotoRestart = MessageDialog.openQuestion(window.getShell(), "提示", "是否重启？");
				if (gotoRestart) {
					PlatformUI.getWorkbench().restart(true);
				}
			}

		});
	}

	// private static class MyFileter implements FilenameFilter {
	//
	// @Override
	// public boolean accept(File file, String filename) {
	// if (filename != null && !filename.toLowerCase().contains(".")) {
	// return true;
	// } else {
	// return false;
	// }
	// }
	//
	// }
	/********* * 与裁剪有关的函数* * *********/
	// 获取当前的配置文件中与component组件有关的宏配置
	public static void getPrjCfgs(List<String> pjCgfs, File configFile, Component component) {
		// TODO Auto-generated method stub
		FileReader reader;
		try {
			reader = new FileReader(configFile.getPath());
			BufferedReader br = new BufferedReader(reader);
			String str = null;
			boolean start = false, stop = false;
			while ((str = br.readLine()) != null) {
				String[] infos = str.split("\\s+");
				if (start && str.contains("Configure")) {
					stop = true;
					break;
				}
				if (start && !stop) {
					pjCgfs.add(str);// 添加当前组件的所有预定义值
				}
				if (str.contains("Configure") && infos[2].equals(component.getName())) {
					start = true;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// 获取此行参数的标识
	public static String getTag(String parameter, String tag) {
		// TODO Auto-generated method stub
		if (parameter.contains("%$#@num")) {
			return "int";
		} else if (parameter.contains("%$#@string")) {
			return "string";
		} else if (parameter.contains("%$#@enum")) {
			return "enum";
		} else if (parameter.contains("%$#@select")) {
			return "select";
		} else if (parameter.contains("%$#@free")) {
			return "free";
		} else if (parameter.contains("%$#@object_num")) {
			return "obj_num";
		} else if (parameter.contains("%$#@object_para")) {
			return "obj_para";
		}
		return tag;
	}

	public static boolean isParaHead(String parameter) {
		if (parameter.contains("%$#@num") || parameter.contains("%$#@string") || parameter.contains("%$#@enum")
				|| parameter.contains("%$#@object_para") || parameter.contains("%$#@select")
				|| parameter.contains("%$#@free") || parameter.contains("%$#@object_num")) {
			return true;
		}
		return false;
	}

	// 检查参数是否有配置错误
	public static boolean checkParameter(Component component, Boolean isApp, IProject curProject) {

		List<String> pjCgfs = new ArrayList<String>();
		File configFile = new File(curProject.getLocation().toString() + "/src/" + (isApp ? "app" : "iboot")
				+ "/OS_prjcfg/project_config.h");
		getPrjCfgs(pjCgfs, configFile, component);

		String configure = component.getConfigure(), tag = null;
		String[] parametersDefined = configure.split("\n"), infos = null;
		List<String> ranges = null, paras = new ArrayList<String>();

		for (int i = 0; i < parametersDefined.length; i++) {
			String parameter = parametersDefined[i];
			if (isParaHead(parameter)) {
				tag = getTag(parameter, tag);
				infos = parameter.split(",|，");
				ranges = new ArrayList<String>();
				if (tag.equals("int") || tag.equals("string")) {
					for (int j = 1; j < infos.length; j++) {
						ranges.add(infos[j]);
					}
				}

			} else if (parameter.contains("#define")) {
				String[] defines = parameter.trim().split("//");
				String[] members = null;
				if (parameter.startsWith("//")) {
					members = defines[1].trim().split("\\s+");
				} else {
					members = defines[0].trim().split("\\s+");
				}
				paras.add(members[1]);// 将所有参数存放到paras
				List<String> rangesCopy = ranges;
				if (rangesCopy.size() != 0) {
					String minString = rangesCopy.get(0), maxString = rangesCopy.get(1);
					if (tag.equals("enum")) {

					} else if (tag.equals("select")) {

					} else {
						if (tag.equals("int")) {
							if (!handleIntPara(minString, maxString, pjCgfs, members)) {
								return false;
							}
						} else if (tag.equals("string")) {
							if (!handleStringPara(minString, maxString, pjCgfs, members, defines)) {
								return false;
							}
						}
					}
				}
			}
		}
		if (paras.size() > 1) {
			Set set = new HashSet<>(paras);
			if (set.size() < paras.size()) {
				return false;
			}
		}
		return true;

	}

	// 处理String类型的参数
	public static boolean handleStringPara(String minString, String maxString, List<String> pjCgfs, String[] members,
			String[] defines) {
		// TODO Auto-generated method stub
		int min, max;
		String value = null;
		min = Integer.parseInt(minString);
		max = Integer.parseInt(maxString);
		if (pjCgfs.size() > 0) {// 如果已存在该组件的配置
			for (String cfg : pjCgfs) {
				String[] cdefines = cfg.split("//");
				if (cfg.contains(members[1])) {
					int begin = cdefines[0].indexOf("\"");
					int end = cdefines[0].lastIndexOf("\"");
					value = cdefines[0].substring(begin + 1, end);
					break;
				}
			}

		} else {
			int begin = defines[0].indexOf("\"");
			int end = defines[0].lastIndexOf("\"");
			value = defines[0].substring(begin + 1, end);
		}
		if (value.length() < min || value.length() > max) {
			return false;
		}
		return true;
	}

	public static String getridParentheses(String data) {
		if (data.contains("(") || data.contains(")")) {
			data = data.replaceAll("\\(|\\)", "");
		}
		if (data.contains("（") || data.contains("）")) {
			data = data.replaceAll("（", "").replaceAll("）", "");
		}
		return data;
	}

	// 处理Int类型的参数
	public static boolean handleIntPara(String minString, String maxString, List<String> pjCgfs, String[] members) {
		// TODO Auto-generated method stub
		int min;
		long max, curData = -1;
		if (minString.startsWith("0x")) {
			min = Integer.parseInt(minString.substring(2), 16);
		} else {
			min = Integer.parseInt(minString);
		}
		if (maxString.startsWith("0x")) {
			max = Long.parseLong(maxString.substring(2), 16);
		} else {
			max = Long.parseLong(maxString);
		}
		if (pjCgfs.size() > 0) {// 如果已存在该组件的配置
			for (String cfg : pjCgfs) {
				if (cfg.contains(members[1])) {
					String[] cfgs = cfg.trim().split("\\s+");
					if (cfgs[2].startsWith("0x")) {
						curData = Long.parseLong(cfgs[2].substring(2), 16);
					} else if (cfgs[2].contains("+") || cfgs[2].contains("-") || cfgs[2].contains("*")
							|| cfgs[2].contains("/")) {
						String pureCal = getridParentheses(cfgs[2]);
						if (pureCal.startsWith("-")) {
							curData = toUnsigned(Long.parseLong(pureCal));
						} else {
							double result = Calculator.conversion(pureCal);
							BigDecimal bd = new BigDecimal(df.format(result));
							curData = Long.valueOf(bd.toPlainString());
						}
					} else {
						curData = Integer.parseInt(getridParentheses(cfgs[2]));
					}

					break;
				}
			}

		} else {
			if (members[2].startsWith("0x")) {
				curData = Long.parseLong(members[2].substring(2), 16);
			} else if (members[2].contains("+") || members[2].contains("-") || members[2].contains("*")
					|| members[2].contains("/")) {
				String pureCal = getridParentheses(members[2]);
				// System.out.println("pureCal: "+pureCal);
				if (pureCal.startsWith("-")) {
					curData = toUnsigned(Long.parseLong(pureCal));
				} else {
					double result = Calculator.conversion(pureCal);
					BigDecimal bd = new BigDecimal(df.format(result));
					curData = Long.valueOf(bd.toPlainString());
				}
			} else {
				curData = Integer.parseInt(getridParentheses(members[2]));
			}
		}

		if (curData < min || curData > max) {
			return false;
		}

		return true;
	}

}
