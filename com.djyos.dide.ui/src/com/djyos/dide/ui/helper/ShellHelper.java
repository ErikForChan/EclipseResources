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
        				// TODO �Զ����ɵ� catch ��
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
//				// TODO �Զ����ɵ� catch ��
//				e.printStackTrace();
//			}
//		}
//	}
	
	/**
	 * ��.a��ѹ����.o
	 * @param project ��ǰ����
	 * @param libos_file 
	 */
	public static File release_a_to_os(File libos_file) {
		
//		IFile file =  project.getFile("libos_Iboot_Debug/libos_Iboot.a");
//		File f = file.getLocation().toFile();
		
		String parentPath = libos_file.getParentFile().getPath();
		File os_file = new File(parentPath+"/os");
		if(!os_file.exists()) {
			os_file.mkdir();
		}
		String commond = "arm-none-eabi-ar -x "+libos_file.getPath();
		String[] commands = {"cmd","/C",commond};
		Runtime runtime = Runtime.getRuntime();
		
		try {
			long startTime=System.currentTimeMillis();   //��ȡ��ʼʱ��
			runtime.exec(commands,null,os_file);
			long endTime=System.currentTimeMillis(); //��ȡ����ʱ��
			System.out.println("��ѹ.a�ĳ�������ʱ�䣺 "+(endTime-startTime)+"  ms");
			Thread.sleep(2000);
		} catch (Exception e) {
			// TODO �Զ����ɵ� catch ��
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
	
	public static List<String> parse_o(File os_file, IProgressMonitor monitor) {
		File[] os = os_file.listFiles();
		System.out.println("os.length:  "+os.length);
		List<String> symbols = new ArrayList<String>();
		long startTime=System.currentTimeMillis();   //��ȡ��ʼʱ��
		for(File o:os) {
			Map<String, String> map = DideHelper.get_o_symbol(o);
			String symbol = map.get("symbol");
			if(symbol != null) {
				symbols.add(symbol);
			}
			monitor.worked(1);
		}
		long endTime=System.currentTimeMillis(); //��ȡ����ʱ��
		System.out.println("��������.o�ĳ�������ʱ�䣺 "+(endTime-startTime)+"  ms");
//		System.out.println("start symbol");
//		for(String s:symbols) {
//			System.out.println("symbol:   "+s);
//		}
		return symbols;
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
				// TODO �Զ����ɵ� catch ��
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