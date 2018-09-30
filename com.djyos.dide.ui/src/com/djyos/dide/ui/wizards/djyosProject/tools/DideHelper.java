package com.djyos.dide.ui.wizards.djyosProject.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.djyos.dide.ui.wizards.djyosProject.tools.Calculator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.actions.NewWizardShortcutAction;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import com.djyos.dide.ui.arch.Arch;
import com.djyos.dide.ui.arch.ReadArchXml;
import com.djyos.dide.ui.wizards.board.Board;
import com.djyos.dide.ui.wizards.board.ReadBoardXml;
import com.djyos.dide.ui.wizards.component.Component;
import com.djyos.dide.ui.wizards.cpu.core.Core;
import com.ibm.icu.text.DecimalFormat;

public class DideHelper {
	
	private IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	String fullPath = Platform.getInstallLocation().getURL().toString().replace("\\", "/");
	String didePath = fullPath.substring(6,(fullPath.substring(0,fullPath.length()-1)).lastIndexOf("/")+1);
	DecimalFormat df = new DecimalFormat("######0");
	
	//将long转成无符类型
	public long toUnsigned(long s) {
		return s & 0xFFFFFFFFL;
	}

	//获取当前DIDE的绝对路径
	public String getDIDEPath() {
		return didePath;
	}
	
	public String getArchPath() {
		return didePath+"djysrc/bsp/arch";
	}
	
	//获取模板的路径
	public String getTemplatePath() {
		return fullPath.substring(6)+"djyosTemplate";
	}
	
	//获取Djysrc的路径
	public String getDjyosSrcPath() {
		return didePath+"djysrc";
	}
	
	public boolean isFputypeuNeed(Core core) {
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
	
	public void writeFile(File file,String content){
		if(!file.exists()) {
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
//	
//	public static List<String> getLogs() throws SVNException{
//        /*
//         * 对版本库进行初始化操作，使用https或http访问svn时，执行DAVRepositoryFactory.setup(); 对于通过使用svn:// 和 svn+xxx://
//         * 访问svn时，执行SVNRepositoryFactoryImpl.setup(); 对于通过file:/// 访问svn的情况，执行 FSRepositoryFactory.setup();
//         */
//        String url = "https://xiangmuzuserver/svn/硬件组开发库/platform_soft/djyos/trunk";
//        String name = "chenjm@sznari.com";
//        String password = "sunri@2017";
//        long startRevision = 43903;
//        long endRevision = -1; // HEAD (the latest) revision
// 
//        SVNURL svnurl = SVNURL.parseURIEncoded(url); // 某目录在svn的位置，获取目录对应的URL。即版本库对应的URL地址
//        DAVRepositoryFactory.setup(); // 初始化
//        ISVNOptions options = SVNWCUtil.createDefaultOptions(true); // 驱动选项
//        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(name, password); // 提供认证
// 
//        SVNRepository repos = SVNRepositoryFactory.create(svnurl);
//        repos.setAuthenticationManager(authManager); // 设置认证
// 
//        Collection logEntries = null;
//        List<String> result = new ArrayList<String>();
//        logEntries = repos.log(new String[] { "" }, null, startRevision, endRevision, true, true);
//        
//        int entryNum = 0;
//        for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
//            SVNLogEntry logEntry = (SVNLogEntry) entries.next();
//            entryNum++;
//            if(entryNum == logEntries.size()) {
//            	 System.out.println("---------------------------------------------");
//                 System.out.println("revision: " + logEntry.getRevision());
//                 System.out.println("author: " + logEntry.getAuthor());
//                 System.out.println("date: " + logEntry.getDate());
//                 System.out.println("log message: " + logEntry.getMessage());
//            }
// 
////            if (logEntry.getChangedPaths().size() > 0) {
////                System.out.println();
////                System.out.println("changed paths:");
////                Set changedPathsSet = logEntry.getChangedPaths().keySet();
//// 
////                for (Iterator changedPaths = changedPathsSet.iterator(); changedPaths.hasNext();) {
////                    SVNLogEntryPath entryPath = (SVNLogEntryPath) logEntry.getChangedPaths().get(changedPaths.next());
////                    System.out.println(" "
////                            + entryPath.getType()
////                            + " "
////                            + entryPath.getPath()
////                            + ((entryPath.getCopyPath() != null) ? " (from " + entryPath.getCopyPath() + " revision "
////                                    + entryPath.getCopyRevision() + ")" : ""));
////                    result.add(entryPath.getPath());
////                }
////            }
//        }
//        // 倒序
//        Collections.reverse(result);
//        return result;
//    }
//	
	public List<File> getArchXmlFiles(File archFile, ArrayList<File> files) {
		// TODO Auto-generated method stub
		if(archFile != null) {
			File[] cFiles = archFile.listFiles();
			for(File f:cFiles) {
				if(f.isDirectory()) {
					getArchXmlFiles(f,files);
				}else {
					if(f.getName().equals("arch.xml")) {
						files.add(f);
					}
				}
			}
		}
		return files;
	}
	
	public boolean showErrorMessage(String message){
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				MessageDialog.openInformation(window.getShell(),
						"提示",message);
		}
		});
		return false;
	}
	
	//获取用户板件的路径
	public String getUserBoardFilePath() {
		String userBoardPath = didePath+"djysrc/bsp/boarddrv/user";
		File userBoardFile = new File(userBoardPath);
		if(!userBoardFile.exists()) {
			userBoardFile.mkdirs();
		}
		return userBoardPath;
	}
	
	//当前目录及其子目录下是否包含xml文件
	public boolean containsXml(File file) {
		File[] files = file.listFiles();
		for(File f:files) {
			if(f.isDirectory()) {
				if(containsXml(f)) {
					return true;
				}
			}else {
				if(f.getName().endsWith(".xml")) {
					return true;
				}
			}
		}
		return false;
	}
	
	//获取Djyos板件的绝对路径
	public String getDemoBoardFilePath() {
		String demoBoardPath = didePath+"djysrc/bsp/boarddrv/demo";
		File demoBoardFile = new File(demoBoardPath);
		if(!demoBoardFile.exists()) {
			demoBoardFile.mkdirs();
		}
		return demoBoardPath;
	}
	
	//获取用户板件的相对路径
	public String getRelativeUserBoardFilePath() {
		return "${DJYOS_SRC_LOCATION}/bsp/boarddrv/user/";
	}
	
	//获取Djyos板件的相对路径
	public String getRelativeDemoBoardFilePath() {
		return "${DJYOS_SRC_LOCATION}/bsp/boarddrv/demo/";
	}
	//获取某个文件夹下的xml文件
	public File getXmlFile(File parentFile) {
		File file =null;
		File[] files = parentFile.listFiles();
		for(int i=0;i<files.length;i++){
			if(files[i].getName().endsWith(".xml")) {
				file = files[i];
			}
		}
		return file;
	}
	
	//将某个文件夹下的文件按照文件夹...文件的方式排序
	public List<File> sortFileAndFolder(File file) {
		File[] files = file.listFiles();
		List<File> allFiles = new ArrayList<File>();
		List<File> pureFiles = new ArrayList<File>();
		List<File> folderFiles = new ArrayList<File>();

		for (File f : files) {
			if(f.isDirectory()) {
				folderFiles.add(f);
			}else if(f.isFile() && (f.getName().endsWith(".c") || f.getName().endsWith(".h"))) {
				pureFiles.add(f);
			}
		}
		
		allFiles.addAll(folderFiles);
		allFiles.addAll(pureFiles);
		
		return allFiles;
	}
	
	public void objdumpTest() {
//		String command="arm-none-eabi-objdump.exe -h F:\\djyos\\atomic.o";  
//	    String line = null;  
//	    StringBuilder sb = new StringBuilder();  
//	    Runtime runtime = Runtime.getRuntime();  
//	    try {  
//	    Process process = runtime.exec(command);  
//	    BufferedReader  bufferedReader = new BufferedReader  
//	            (new InputStreamReader(process.getInputStream()));  
//	        while ((line = bufferedReader.readLine()) != null) {  
//	            sb.append(line + "\n");  
//	            System.out.println(line);  
//	        }  
//	    } catch (IOException e) {  
//	        // TODO 自动生成的 catch 块  
//	        e.printStackTrace();  
//	    }  

//		long a = Integer.parseInt("1");
//		long b = Long.parseLong("0xFFFFFFFF".substring(2), 16);
//		long c = Integer.parseInt("-1");; //0xFFFFFFFF
//		if(a>b) {
//			System.out.println("a= "+toUnsigned(a));
//			System.out.println("b= "+toUnsigned(b));
//			System.out.println("c= "+toUnsigned(c));
//			System.out.println("a>b");
//		}else {
//			System.out.println("a= "+toUnsigned(a));
//			System.out.println("b= "+toUnsigned(b));
//			System.out.println("c= "+toUnsigned(c));
//			System.out.println("b>a");
//		}
	}
	
	//根据id获取某个action
	public IAction getAction(String id) {
		// Keep a cache, rather than creating a new action each time,
		// so that image caching in ActionContributionItem works.
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
	
	//将src目录下的所有文件拷贝到dest目录下
	public void copyFolder(File src, File dest) throws IOException {  
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

	//将src目录下的所有文件拷贝到命名为boardName的dest目录下
	private void copyFileToFolder(File src, File dest, String boardName) throws IOException {
		if (src.isDirectory()) {
			if (!dest.exists()) {
				dest.mkdir();
				dest.renameTo(
						new File(dest.getAbsolutePath().substring(0, dest.getAbsolutePath().lastIndexOf("\\"))
								+ "\\" + boardName));
				dest = new File(dest.getAbsolutePath().substring(0, dest.getAbsolutePath().lastIndexOf("\\"))
						+ "\\" + boardName);
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

	//获取当前的配置文件中与component组件有关的宏配置
	public void getPrjCfgs(List<String> pjCgfs, File configFile, Component component) {
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
	
	//获取此行参数的标识
	public String getTag(String parameter, String tag) {
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
			return"free";
		}else if (parameter.contains("%$#@object_num")) {
			return "obj_num";
		}else if (parameter.contains("%$#@object_para")) {
			return "obj_para";
		}
		return tag;
	}
	
	//检查参数是否有配置错误
	public boolean checkParameter(Component component, Boolean isApp, IProject curProject) {

		List<String> pjCgfs = new ArrayList<String>();
		File configFile = new File(curProject.getLocation().toString() + "/src/" + (isApp ? "app" : "iboot")
				+ "/OS_prjcfg/project_config.h");
		getPrjCfgs(pjCgfs,configFile,component);
	
		String configure = component.getConfigure(),tag = null;
		String[] parametersDefined = configure.split("\n"),infos = null;
		List<String> ranges = null,paras = new ArrayList<String>();
		
		for (int i = 0; i < parametersDefined.length; i++) {
			String parameter = parametersDefined[i];
			if (parameter.contains("%$#@num") || parameter.contains("%$#@string") || parameter.contains("%$#@enum") || parameter.contains("%$#@object_para")
					|| parameter.contains("%$#@select") || parameter.contains("%$#@free") || parameter.contains("%$#@object_num")) {
				tag = getTag(parameter,tag);
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
				paras.add(members[1]);//将所有参数存放到paras
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
		if(paras.size()>1) {
			Set set = new HashSet<>(paras);
			if(set.size()<paras.size()) {
				return false;
			}
		}
		return true;

	}

	//处理String类型的参数
	private boolean handleStringPara(String minString, String maxString, List<String> pjCgfs, String[] members, String[] defines) {
		// TODO Auto-generated method stub
		int min, max;
		String value = null;
		min = Integer.parseInt(minString);
		max = Integer.parseInt(maxString);
		if (pjCgfs.size() > 0) {// 如果已存在该组件的配置
			for (String cfg : pjCgfs) {
				String[] cdefines = cfg.split("//");
				if (cfg.contains(members[1])) {
					String[] cfgs = cdefines[0].trim().split("\\s+");
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

	//处理Int类型的参数
	private boolean handleIntPara(String minString, String maxString, List<String> pjCgfs, String[] members) {
		// TODO Auto-generated method stub
		int min;
		long max,curData = -1;
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
					} else if (cfgs[2].contains("+") || cfgs[2].contains("-")
							|| cfgs[2].contains("*") || cfgs[2].contains("/")) {
						String pureCal = cfgs[2].replaceAll("\\(|\\)", "");
						if (pureCal.startsWith("-")) {
							curData = toUnsigned(Long.parseLong(pureCal));
						} else {
							double result = Calculator.conversion(pureCal);
							BigDecimal bd = new BigDecimal(df.format(result));
							curData = Long.valueOf(bd.toPlainString());
						}
					} else {
						curData = Integer.parseInt(cfgs[2].replaceAll("\\(|\\)", ""));
					}

					break;
				}
			}

		} else {
			if (members[2].startsWith("0x")) {
				curData = Long.parseLong(members[2].substring(2), 16);
			} else if (members[2].contains("+") || members[2].contains("-")
					|| members[2].contains("*") || members[2].contains("/")) {
				String pureCal = members[2].replaceAll("\\(|\\)", "");
				if (pureCal.startsWith("-")) {
					curData = toUnsigned(Long.parseLong(pureCal));
				} else {
					double result = Calculator.conversion(pureCal);
					BigDecimal bd = new BigDecimal(df.format(result));
					curData = Long.valueOf(bd.toPlainString());
				}
			} else {
				curData = Integer.parseInt(members[2].replaceAll("\\(|\\)", ""));
			}
		}

		if (curData < min || curData > max) {
			return false;
		}
		
		return true;
	}

}
