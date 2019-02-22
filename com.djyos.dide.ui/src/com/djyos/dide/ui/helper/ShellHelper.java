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

public class ShellHelper {
	
	static class Thread_command extends Thread{
		
		private Thread_symbol symbol;
		String command;
		File os_file;
		
		public Thread_command(String command,File os_file) {
			this.command = command;
			this.os_file = os_file;
		}
		
		@Override
		public  void run() {
			// TODO Auto-generated method stub
			synchronized (symbol){
                synchronized (this){
                	Runtime runtime = Runtime.getRuntime();
        			try {
        				runtime.exec(command,null,os_file);
//        				Thread.sleep(3000);
        			} catch (Exception e) {
        				// TODO 自动生成的 catch 块
        				e.printStackTrace();
        			}
                    this.notify();
                }
                try {
                	symbol.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
		}
		
		public void setThreadS(Thread_symbol symbol) {
			this.symbol = symbol;
		}
	}
	
	static class Thread_symbol extends Thread {

		File os_file;

		public Thread_symbol(File os_file) {
			this.os_file = os_file;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			File[] os = os_file.listFiles();
			List<String> symbols = new ArrayList<String>();
			for(File o:os) {
				Map<String, String> map = DideHelper.get_o_symbol(o);
				String symbol = map.get("symbol");
				if(symbol != null) {
					symbols.add(symbol);
				}
//				DideHelper.printToConsole(map.get("o_content"), true);
//				System.out.println(map.get("o_content"));
			}
			System.out.println("start symbol");
			for(String s:symbols) {
				System.out.println("symbol:   "+s);
			}
		}
	}
	
//	class Thread_command extends Thread {
//
//		String commands;
//		File os_file;
//
//		public Thread_command(String commands, File os_file) {
//			this.commands = commands;
//			this.os_file = os_file;
//		}
//
//		@Override
//		public void run() {
//			// TODO Auto-generated method stub
//			Runtime runtime = Runtime.getRuntime();
//			try {
//				runtime.exec(commands, null, os_file);
//				Thread.sleep(3000);
//			} catch (Exception e) {
//				// TODO 自动生成的 catch 块
//				e.printStackTrace();
//			}
//		}
//	}
	
	/**
	 * 将.a解压缩成.o
	 * @param project 当前工程
	 */
	public static void release_a_to_os(IProject project) {
		IFile file =  project.getFile("libos_Iboot_Debug/libos_Iboot.a");
		File f = file.getLocation().toFile();
		
		String parentPath = f.getParentFile().getPath();
		File os_file = new File(parentPath+"/os");
		String commond = "arm-none-eabi-ar -x "+f.getPath();
//		String[] commands = {"cmd","/C",commond};
//		Runtime runtime = Runtime.getRuntime();
		
		Thread_command tc = new Thread_command(commond, os_file);
		Thread_symbol ts  = new Thread_symbol(os_file);
		tc.start();
		ts.start();
		
//		try {
//			runtime.exec(commands,null,os_file);
//			Thread.sleep(3000);
//		} catch (Exception e) {
//			// TODO 自动生成的 catch 块
//			e.printStackTrace();
//		}
//		
////		 synchronized(runtime){
////             try {
////            	runtime.exec(commands,null,os_file);
////				runtime.wait(3000);
////			} catch (Exception e) {
////				// TODO Auto-generated catch block
////				e.printStackTrace();
////			}
////         }
//		
//		File[] os = os_file.listFiles();
//		List<String> symbols = new ArrayList<String>();
//		for(File o:os) {
//			Map<String, String> map = DideHelper.get_o_symbol(o);
//			String symbol = map.get("symbol");
//			if(symbol != null) {
//				symbols.add(symbol);
//			}
////			DideHelper.printToConsole(map.get("o_content"), true);
////			System.out.println(map.get("o_content"));
//		}
//		System.out.println("start symbol");
//		for(String s:symbols) {
//			System.out.println("symbol:   "+s);
//		}
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
