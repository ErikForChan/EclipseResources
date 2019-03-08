package com.djyos.dide.ui.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import com.djyos.dide.ui.objects.Component;

public class ShellHelper {
	
	/**
	 * 将.a解压缩成.o
	 * @param project 当前工程
	 * @param libos_file 
	 */
	public static File release_a_to_os(File libos_file) {
		
		String parentPath = libos_file.getParentFile().getPath();
		File os_file = new File(parentPath+"/os");
		if(!os_file.exists()) {
			os_file.mkdir();
		}
		String commond = "arm-none-eabi-ar -x "+libos_file.getPath();
		String[] commands = {"cmd","/C",commond};
		Runtime runtime = Runtime.getRuntime();
		
		try {
			long startTime=System.currentTimeMillis();   //获取开始时间
			runtime.exec(commands,null,os_file);
			long endTime=System.currentTimeMillis(); //获取结束时间
			System.out.println("解压.a的程序运行时间： "+(endTime-startTime)+"  ms");
			Thread.sleep(2000);
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
//		 synchronized(runtime){
//             try {
//            	runtime.exec(commands,null,os_file);
//				runtime.wait(3000);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//         }
		return os_file;
		
	}
	
	public static List<String> parse_o(File os_file, IProgressMonitor monitor, File libos_folder) {
		File[] os = os_file.listFiles();
		System.out.println("os.length:  "+os.length);
		List<String> symbols = new ArrayList<String>();
		long startTime=System.currentTimeMillis();   //获取开始时间
		for(File o:os) {
			Map<String, String> map = DideHelper.get_o_symbol(o);
			String symbol = map.get("symbol");
			if(symbol != null) {
				symbols.add(symbol);
			}
			monitor.worked(1);
		}
		long endTime=System.currentTimeMillis(); //获取结束时间
		System.out.println("分析所有.o的程序运行时间： "+(endTime-startTime)+"  ms");
//		System.out.println("start symbol");
//		for(String s:symbols) {
//			System.out.println("symbol:   "+s);
//		}
		return symbols;
	}
	
	
	/**
	 * 获取目录下所有的.o文件
	 * @param compt_object_checks
	 * @param o_files
	 * @param string 
	 * @return
	 */
	public static List<File> get_src_ofiles(List<Component> compt_object_checks, List<File> o_files, File parentFile) {
		for(Component c:compt_object_checks) {
			String parent_folder_path = c.getParentPath();
			File c_folder = new File(parentFile.getPath() + "\\src\\libos" + parent_folder_path.split("djysrc")[1]);
			File[] child_files = c_folder.listFiles();
			for(File f:child_files) {
				if(f.getName().endsWith(".o")){
					o_files.add(f);
				} 
			}
		}
		return o_files;
	}
	
	
	
	public static void test() {
		File os_file = new File("D:\\SoftWare\\DIDE_Builder\\djysrc\\examples\\explore_stm32f407\\libos_Iboot_Debug\\os");
		File[] fs = os_file.listFiles();
		for(File f:fs) {
			String command = "arm-none-eabi-objdump -ht "+f.getPath();
			Runtime runtime = Runtime.getRuntime();
			StringBuilder sb = new StringBuilder();
			String line = null;
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
			String[] sections = {".ro_shell_cmd",".ex_shell_cmd",".ro_shell_data",".ex_shell_data"}; 
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
					System.out.println("my_symbol:  "+my_symbol);
				}
			}
//			DideHelper.printToConsole(sb.toString(), true);
		}
	}

}
