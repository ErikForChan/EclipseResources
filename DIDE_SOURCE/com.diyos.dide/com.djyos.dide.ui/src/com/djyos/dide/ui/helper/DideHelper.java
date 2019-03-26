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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.tcmodification.IFolderInfoModification;
import org.eclipse.cdt.managedbuilder.tcmodification.IToolListModification;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.actions.NewWizardShortcutAction;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.actions.BuildUtilities;
import org.eclipse.ui.wizards.IWizardDescriptor;

import com.djyos.dide.ui.DPluginImages;
import com.djyos.dide.ui.arch.ReadArchXml;
import com.djyos.dide.ui.job.BuildTarget;
import com.djyos.dide.ui.objects.Arch;
import com.djyos.dide.ui.objects.Board;
import com.djyos.dide.ui.objects.Component;
import com.djyos.dide.ui.objects.Core;
import com.djyos.dide.ui.objects.Cpu;
import com.djyos.dide.ui.objects.OnBoardCpu;
import com.djyos.dide.ui.wizards.board.ReadBoardXml;
import com.djyos.dide.ui.wizards.cpu.ReadCpuXml;
import com.djyos.dide.ui.wizards.djyosProject.tools.ConsoleFactory;
import com.djyos.dide.ui.wizards.djyosProject.tools.FileTool;
import com.djyos.dide.ui.wizards.djyosProject.tools.PathTool;
import com.djyos.dide.ui.wizards.djyosProject.tools.ReviseVariableToXML;
import com.ibm.icu.text.DecimalFormat;

@SuppressWarnings("restriction")
public class DideHelper {

	public static String didePath = new File(System.getProperty("user.dir")).getParentFile().getPath().replace("\\", "/")
			+ "/";
	static DecimalFormat df = new DecimalFormat("######0");
	
	// Exclude某个文件夹
	public static void setFolderExclude(File f, IProject project, ICConfigurationDescription[] conds, boolean exclude) {
		String filePath = f.getPath().toString().replace("\\", "/");
		String relativePath = filePath.replace(PathTool.getDjyosSrcPath(), "");
		IFolder folder = project.getFolder("src/libos" + relativePath);
		for (int i = 0; i < conds.length; i++) {
			if (conds[i].getName().contains("libos")) {
				LinkHelper.setFolderExclude(folder, conds[i], exclude);
			}
		}
	}
	
	/**
	 * 获取DIDE的版本
	 * @return
	 */
	public static String get_DIDE_Version() {
		File versionFile = new File(didePath + "IDE/DIDE.ini");
		if(versionFile.exists()) {
			String content = FileTool.readFile(versionFile);
			String[] lines = content.split("\n");
			for(String line:lines) {
				line = line.trim();
				if(line.startsWith("version")) {
					String[] datas = line.split("=");
					return datas[1];
				}
			}
		}
		return null;
	}
	
	public static String getCoreName(Core core, int index) {
		String coreName = (core.getName() == null)?("Core" + (index + 1)):core.getName();
		return coreName;
	}
	
	public static Tree buildTree(Composite memoryComposite, int tWidth, int tHeight, String columTitle, int style) {
		Tree t = new Tree(memoryComposite, style);
		t.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		t.setHeaderVisible(true);
		t.setSize(tWidth,tHeight);
		final TreeColumn columnMemory = new TreeColumn(t, SWT.NONE);
		columnMemory.setText(columTitle);
		columnMemory.setWidth(tWidth);
		columnMemory.setResizable(false);
		columnMemory.setImage(DPluginImages.CFG_CPMT_OBJ.createImage());
		return t;
	}
	
	public static void getToolchain() {
//		 IToolChain[] r_tcs = ManagedBuildManager.getRealToolChains();
//		 IResourceInfo resourceInfo = cfg.getRootFolderInfo();
//		 IToolListModification mod = getModification(tcmmgr,resourceInfo);
//		 IFolderInfoModification foim = (IFolderInfoModification)mod;
//		 for(IToolChain tc:r_tcs) {
//		 System.out.println("tc.getName(): "+tc.getName());
//		 }
	}
	
	public static void saveProjectDescription(IProject project, ICProjectDescription local_prjd) {
		try {
			CoreModel.getDefault().setProjectDescription(project, local_prjd);
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	

	/**
	 * 编译目标编译选项
	 * @param project 工程
	 * @param targetName 目标编译选项
	 */
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
		FileTool.writeFile(file, bufAll.toString(),false);

	}

	/**
	 * 将long转成无符类型
	 * @param s
	 * @return
	 */
	public static long toUnsigned(long s) {
		return s & 0xFFFFFFFFL;
	}

	public static boolean isFputypeuNeed(Core core) {
		// TODO Auto-generated method stub
		Arch curArch = core.getArch();
		File archSourceFile = new File(PathTool.getDjyosSrcPath() + "/bsp/arch");
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
			if (b.getBoardName().equalsIgnoreCase(boardName)) {
				return b;
			}
		}
		return new Board();
	}

	public static Cpu getCpuByName(String cpuName) {
		List<Cpu> allCpus = ReadCpuXml.getAllCpus();
		for (Cpu c : allCpus) {
			if (c.getCpuName().equalsIgnoreCase(cpuName)) {
				return c;
			}
		}
		return new Cpu();
	}
	
	public static Core getCoreByName(String coreName,Cpu cpu) {
		
		for(Core c:cpu.getCores()) {
			if(c.getName().equalsIgnoreCase(coreName)) {
				return c;
			}
		}
		return cpu.getCores().get(0);
		
	}

	public static Cpu getCpuByonBoard(OnBoardCpu onBoardCpu, List<Cpu> allCpus) {
		for (Cpu c : allCpus) {
			if (c.getCpuName().equals(onBoardCpu.getCpuName())) {
				return c;
			}
		}
		showErrorMessage("Cpu:"+onBoardCpu.getCpuName()+"不存在...");
		return new Cpu();
	}

	public static OnBoardCpu getOnBoardByCpu(List<OnBoardCpu> onBoardCpus, String cpuName) {
		for (int i = 0; i < onBoardCpus.size(); i++) {
			if (onBoardCpus.get(i).getCpuName().equalsIgnoreCase(cpuName)) {
				return onBoardCpus.get(i);
			}
		}
		return new OnBoardCpu();
	}

	public static boolean isDjysrcExist() {
		boolean djysrcExist = true;
		File djysrcFile = new File(PathTool.getDjyosSrcPath());
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

	/**
	 * 根据id获取某个action
	 * @param id
	 * @return
	 */
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


	/**
	 * 复制文件
	 * @param src
	 * @param dest
	 */
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

	/**
	 * 在IDE中打开文件
	 * @param file
	 */
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

	/**
	 * 将src目录下的所有文件拷贝到命名为boardName的dest目录下
	 * @param src
	 * @param dest
	 * @param boardName
	 * @throws IOException
	 */
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

	/**
	 * 当前目录及其子目录下是否包含xml文件
	 * @param file
	 * @return
	 */
	public static boolean travelContainsXml(File file) {
		if(file.exists()) {
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
		}
		return false;
	}

	/**
	 * 当前目录下是否包含xml文件
	 * @param file
	 * @return
	 */
	public static boolean isContainsXml(File file) {
		if(file.exists()) {
			File[] files = file.listFiles();
			for (File f : files) {
				if (f.getName().endsWith(".xml")) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 获取所有的arch.xml
	 * @param archFile
	 * @param files
	 * @return
	 */
	public static List<File> getArchXmlFiles(File archFile, ArrayList<File> files) {
		// TODO Auto-generated method stub
		if (archFile.exists()) {
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

	/**
	 * 显示错误信息
	 * @param message
	 * @return
	 */
	public static boolean showErrorMessage(String message) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				MessageDialog.openInformation(window.getShell(), "提示", message);
			}
		});
		return false;
	}

	/**
	 * 获取某个文件夹下的xml文件
	 * @param parentFile
	 * @return
	 */
	public static File getXmlFile(File parentFile) {
		if(parentFile.exists()) {
			File[] files = parentFile.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].getName().endsWith(".xml")) {
					return files[i];
				}
			}
		}
		return null;
	}

	/**
	 *  将某个文件夹下的文件按照文件夹...文件的方式排序
	 * @param file
	 * @return
	 */
	public static List<File> sortFileAndFolder(File file) {
		
		List<File> allFiles = new ArrayList<File>();
		List<File> pureFiles = new ArrayList<File>();
		List<File> folderFiles = new ArrayList<File>();

		if(file.exists()) {
			File[] files = file.listFiles();
			for (File f : files) {
				if (f.isDirectory()) {
					folderFiles.add(f);
				} else if (f.isFile() && (f.getName().endsWith(".c") || f.getName().endsWith(".h"))) {
					pureFiles.add(f);
				}
			}
			allFiles.addAll(folderFiles);
			allFiles.addAll(pureFiles);
		}
		return allFiles;
	}
	
	static Runtime runtime = Runtime.getRuntime();
	static String[] sections = {".ro_shell_cmd",".ex_shell_cmd",".ro_shell_data",".ex_shell_data"}; 
	
	/**
	 * 获取.o文件中的符号
	 * @param f 文件
	 * @return
	 */
	public static Map<String, String> get_o_symbol(File f) {
		Map<String, String> map = new HashMap<String, String>();
		String command = "arm-none-eabi-objdump -ht "+f.getPath();
		String line = null;
		StringBuilder sb = new StringBuilder();
		try {
			Process process = runtime.exec(command);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		
		String[] contents = sb.toString().split("SYMBOL TABLE:");
		String my_section = null;
		String my_symbol = null;
		for(String section:sections) {
			if(contents[0].contains(section)) {
				my_section = section;
				break;
			}
		}
		if(my_section != null) {
			if(contents[1].contains(my_section)) {
				String[] section_splite = contents[1].split(my_section);
				my_symbol = section_splite[section_splite.length-1].trim().split("\\s+")[1];
			}
		}
		map.put("symbol", my_symbol);
		map.put("o_content", sb.toString());
		return map;
	}
	

	/********* * 与测试有关的函数* * *********/
	public static void objdumpTest() {
//		 String command="arm-none-eabi-objdump.exe -h F:\\djyos\\atomic.o";
//		 String line = null;
//		 StringBuilder sb = new StringBuilder();
//		 Runtime runtime = Runtime.getRuntime();
//		 try {
//		 Process process = runtime.exec(command);
//		 BufferedReader bufferedReader = new BufferedReader
//		 (new InputStreamReader(process.getInputStream()));
//		 while ((line = bufferedReader.readLine()) != null) {
//		 sb.append(line + "\n");
//		 System.out.println(line);
//		 }
//		 } catch (IOException e) {
//		 // TODO 自动生成的 catch 块
//		 e.printStackTrace();
//		 }

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
		
		
//		String[] libos_members = {"bsp","component","djyos","libc","loader","third"};
//		for(String m:libos_members) {
//			IFolder newFolder = project.getFolder("src/libos/"+m);
//			if(!newFolder.exists()) {
//				newFolder.getLocation().toFile().mkdir();
//			}
//        	try {
//				newFolder.createLink( new Path("DJYOS_SRC_LOCATION/"+m), 0, null);
//			} catch (CoreException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
    	
//		Project p = (Project) project;
//		boolean changed = p.internalGetDescription().setLinkLocation(getProjectRelativePath(), linkDescription);
//		if(changed) {
//			try {
//				p.writeDescription(IResource.NONE);
//			} catch (CoreException e) {
//				// A problem happened updating the description, so delete the resource from the workspace.
////				workspace.deleteResource(this);
//				throw e; // Rethrow.
//			}
//		}
	}

	/**
	 * 重启DIDE
	 */
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

	/**
	 * 获取此行参数的标识
	 * @param parameter
	 * @param tag
	 * @return
	 */
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
		}else if (parameter.contains("%$#@SYMBOL")) {
			return "symbol";
		}
		return tag;
	}

	/**
	 * 判断parameter是否为开始
	 * @param parameter
	 * @return
	 */
	public static boolean isParaHead(String parameter) {
		if (parameter.contains("%$#@num") || parameter.contains("%$#@string") || parameter.contains("%$#@enum")
				|| parameter.contains("%$#@object_para") || parameter.contains("%$#@select")
				|| parameter.contains("%$#@free") || parameter.contains("%$#@object_num") || parameter.contains("%$#@SYMBOL")) {
			return true;
		}
		return false;
	}
	
	
	private static String get_check_log(Component component, String parameter, String reason) {
		String attribute = null;
		switch (component.getAttribute()) {
		case "system":
			attribute = "核心组件";
			break;
		case "bsp":
			attribute = "bsp组件";
			break;
		case "third":
			attribute = "第三方组件";
			break;
		case "user":
			attribute = "用户组件";
			break;
		}
		String log = "配置文件"+component.getFileName() + ",\t组件名称:"+component.getName()+",\t"+attribute+"\n" +parameter+"\n配置有误,"+reason;
		return log;
	}

	/**
	 * 检查参数是否有配置错误
	 * @param component
	 * @param isApp
	 * @param curProject
	 * @return
	 */
	public static boolean checkParameter(Component component, Boolean isApp, IProject curProject) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		String workspacePath = workspace.getRoot().getLocation().toString();
		File checkLog = new File(workspacePath+"/check_component.log");
		String errCheckMsg = FileTool.readFile(checkLog);
		
		List<String> pjCgfs = new ArrayList<String>();
		if(curProject != null) {
			File configFile = new File(curProject.getLocation().toString() + "/src/" + (isApp ? "app" : "iboot")
					+ "/OS_prjcfg/project_config.h");
			getPrjCfgs(pjCgfs, configFile, component);
		}
		
		String configure = component.getConfigure();
		String tag = null;
		String[] parametersDefined = configure.split("\n");
		String[] infos = null;
		List<String> ranges = null;
		List<String> paras = new ArrayList<String>();

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

			} else if (parameter.contains("#define") && !tag.equals("symbol")) {
				String[] members = parameter.replace("//", " ").trim().split("\\s+");
				paras.add(members[1]);// 将所有参数存放到paras
				List<String> rangesCopy = ranges;
				if (rangesCopy.size() != 0) {
					try {
						String minString = rangesCopy.get(0), maxString = rangesCopy.get(1);
						if (tag.equals("enum")) {

						} else if (tag.equals("select")) {

						} else {
							if (tag.equals("int")) {
								if(members[2].contains("\"")) {
									String log = get_check_log(component,parameter,"原因:"+members[2]+" 不是int类型\n\n");
									if(!errCheckMsg.contains(log)) {
										openFileInDide(new File(component.getParentPath() + "/" + component.getFileName()));
										FileTool.writeFile(checkLog, log, true);
									}
									return false;//CFG_TFTP_PATHDEFAULT  CN_TFTP_PATHDEFAULT
								}else {
									if (!handleIntPara(component,minString, maxString,pjCgfs, members)) {
										return false;
									}
								}
							} else if (tag.equals("string")) {
								if(!members[2].contains("\"")) {
									String log = get_check_log(component,parameter,"原因:"+members[2]+" 不是字符串类型\n\n");
									if(!errCheckMsg.contains(log)) {
										openFileInDide(new File(component.getParentPath() + "/" + component.getFileName()));
										FileTool.writeFile(checkLog, log, true);
									}
									return false;
								}else {
									if (!handleStringPara(component,minString, maxString, pjCgfs, members)) {
										return false;
									}
								}
								
							}
						}
					} catch (Exception e) {
						// TODO: handle exception
						String log = get_check_log(component,parameter,"原因未知\n\n");
						if(!errCheckMsg.contains(log)) {
							openFileInDide(new File(component.getParentPath() + "/" + component.getFileName()));
							FileTool.writeFile(checkLog, log, true);
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

	/**
	 * 处理String类型的参数
	 * @param component
	 * @param minString
	 * @param maxString
	 * @param pjCgfs
	 * @param members
	 * @return
	 */
	public static boolean handleStringPara(Component component, String minString, String maxString, List<String> pjCgfs, String[] members) {
		// TODO Auto-generated method stub
			String value = null;
			int min = Integer.parseInt(minString);
			int max = Integer.parseInt(maxString);
			if (pjCgfs.size() > 0) {// 如果已存在该组件的配置
				for (String cfg : pjCgfs) {
					String[] cdefines = cfg.split("\\s+");
					if (cfg.contains(members[1])) {
						value = cdefines[2].replace("\"", "");
						break;
					}
				}
			} else {
				value = members[2].replace("\"", "");
			}
			if(value == null) {
				value = members[2].replace("\"", "");
			}
			
			if (value.length() < min || value.length() > max) {
				return false;
			}
			
		return true;
	}

	/**
	 * 删除字符串中所有的括号
	 * @param data
	 * @return
	 */
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
	public static boolean handleIntPara(Component component,String minString, String maxString, List<String> pjCgfs, String[] members) {
		// TODO Auto-generated method stub
			int min = minString.startsWith("0x")?Integer.parseInt(minString.substring(2), 16):Integer.parseInt(minString);
			long max = maxString.startsWith("0x")?Long.parseLong(maxString.substring(2), 16):Long.parseLong(maxString);;
			long curData = -1;
			if (pjCgfs.size() > 0) {// 如果已存在该组件的配置
				for (String cfg : pjCgfs) {
					if (cfg.contains(members[1])) {
						String[] cfgs = cfg.trim().split("\\s+");
						if (cfgs[2].startsWith("0x")) {
							curData = Long.parseLong(cfgs[2].substring(2), 16);
						} else if (cfgs[2].contains("+") || cfgs[2].contains("-") || cfgs[2].contains("*")
								|| cfgs[2].contains("/")) {
							String pureCal = getridParentheses(cfgs[2]);
							if (pureCal.startsWith("-") && min>=0) {
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
					if (pureCal.startsWith("-") && min>=0) {
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

	/**
	 * 向控制台打印一条信息，并激活控制台。
	 * 
	 * @param message
	 * @param activate
	 *            是否激活控制台
	 */
	public static void printToConsole(String message, boolean activate) {
		MessageConsoleStream printer = ConsoleFactory.getConsole()
				.newMessageStream();
		printer.setActivateOnWrite(activate);
		printer.println(message);
	}

	/**
	 * 重设.project的djyos文件夹的链接
	 * 
	 * @param project:当前工程
	 */
	public static void reset_djyos_link(IProject project) {
		IFile libosFolder = project.getFile("src/libos");
		if(!libosFolder.exists()) {
			libosFolder.getLocation().toFile().mkdir();
			ReviseVariableToXML rvtx = new ReviseVariableToXML();
			rvtx.add_djyos_links(project.getFile(".project"));
		}
	}
	
	/**
	 * 刷新当前工作空间
	 */
	public static void refresh_workspace(){
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		try {
			workspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
