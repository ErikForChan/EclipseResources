package com.djyos.dide.ui.wizards.djyosProject.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.List;

import com.djyos.dide.ui.helper.DideHelper;

public class FileTool {
	
	public static String read_file_hex(File f) {
		InputStream input = null; // 准备好一个输入的对象
		try {
			input = new FileInputStream(f);
			// 第3步、进行读操作
			byte b[] = new byte[1024*10000]; // 所有的内容都读到此数组之中
			input.read(b); // 读取内容 网络编程中 read 方法会阻塞
			// 第4步、关闭输出流
			input.close(); // 关闭输出流
			String data = bytesToHexString(b);
			DideHelper.printToConsole(data, true);
//			System.out.println("内容为：\n" + new String(b)); // 把byte数组变为字符串输出
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // 通过对象多态性，进行实例化
//		return builder.toString();
		return null;
	}
	
	public static String bToHexString(File f) {
//		FileOutputStream os = null;
		StringBuilder stringBuilder = new StringBuilder("");
//		File hexFile = new File("E:\\hex.txt");
//		DideHelper.createNewFile(hexFile);
		try {
//			os = new FileOutputStream(hexFile.getPath());
			byte[] bts = Files.readAllBytes(Paths.get(f.getPath()));
			System.out.println("bts.length:  "+bts.length);
			int i = 1;
			for(byte b:bts){
				int h = 0xf&(b>>>4);
				int l = 0xf&b;
				String s = (Integer.toHexString(h)+Integer.toHexString(l)).toUpperCase();
				stringBuilder.append(s+" ");
//				stringBuilder.append(i%16 == 0?"\n":" ");
//				System.out.print(s + (i%16 == 0?"\n":" "));
//				stringBuilder.append(i%16 == 0?"\n":" ");
//				if(i%16 == 0) {
//					stringBuilder.append("\n");
//				}else {
//					stringBuilder.append(" ");
//				}
//				os.write(s.getBytes());
//				if(i%16==0){
//					os.write("\n".getBytes());
//				}
				i++;
			}
//			os.flush();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*finally{
			if(os!=null){
				try{
					os.close();
				}catch (Exception e) {
					
				}
			}
		}*/
//		DideHelper.printToConsole(DideHelper.readFile(hexFile), true);
		return stringBuilder.toString();
	}
	
	/*
	 * * Convert byte[] to hex string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)
	 * 来转换成16进制字符串。
	 * 
	 * @param src byte[] data
	 * 
	 * @return hex string
	 */
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
			if((i+1)%16 == 0) {
				stringBuilder.append("\n");
			}else {
				stringBuilder.append(" ");
			}
		}
		return stringBuilder.toString();
	}
	
	/**
	 * 复制单个文件
	 * 
	 * @param oldPath
	 *            String 原文件路径 如：c:/fqf.txt
	 * @param newPath
	 *            String 复制后路径 如：f:/fqf.txt
	 * @return boolean
	 */
	public static void copyFile(String oldPath, String newPath) {
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) { // 文件存在时
				InputStream inStream = new FileInputStream(oldPath); // 读入原文件
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				int length;
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; // 字节数 文件大小
//					System.out.println(bytesum);
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
			}
		} catch (Exception e) {
			System.out.println("复制单个文件操作出错");
			e.printStackTrace();

		}

	}

	/**
	 * 复制整个文件夹内容
	 * 
	 * @param oldPath
	 *            String 原文件路径 如：c:/fqf
	 * @param newPath
	 *            String 复制后路径 如：f:/fqf/ff
	 * @return boolean
	 */
	public static void copyFolder(String oldPath, String newPath) {

		try {
			(new File(newPath)).mkdirs(); // 如果文件夹不存在 则建立新文件夹
			File a = new File(oldPath);
			String[] file = a.list();
			File temp = null;
			for (int i = 0; i < file.length; i++) {
				if (oldPath.endsWith(File.separator)) {
					temp = new File(oldPath + file[i]);
				} else {
					temp = new File(oldPath + File.separator + file[i]);
				}

				if (temp.isFile()) {
					FileInputStream input = new FileInputStream(temp);
					FileOutputStream output = new FileOutputStream(newPath + "/" + (temp.getName()).toString());
					byte[] b = new byte[1024 * 5];
					int len;
					while ((len = input.read(b)) != -1) {
						output.write(b, 0, len);
					}
					output.flush();
					output.close();
					input.close();
				}
				if (temp.isDirectory()) {// 如果是子文件夹
					copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
				}
			}
		} catch (Exception e) {
			System.out.println("复制整个文件夹内容操作出错");
			e.printStackTrace();

		}

	}
//
//	public static void main(String[] args) throws Exception {
//		// //这是你的源文件，本身是存在的
//		// File beforefile = new File("C:/Users/Administrator/Desktop/Untitled-2.html");
//		//
//		// //这是你要保存之后的文件，是自定义的，本身不存在
//		// File afterfile = new
//		// File("C:/Users/Administrator/Desktop/jiekou0/Untitled-2.html");
//		//
//		// //定义文件输入流，用来读取beforefile文件
//		// FileInputStream fis = new FileInputStream(beforefile);
//		//
//		// //定义文件输出流，用来把信息写入afterfile文件中
//		// FileOutputStream fos = new FileOutputStream(afterfile);
//		//
//		// //文件缓存区
//		// byte[] b = new byte[1024];
//		// //将文件流信息读取文件缓存区，如果读取结果不为-1就代表文件没有读取完毕，反之已经读取完毕
//		// while(fis.read(b)!=-1){
//		// //将缓存区中的内容写到afterfile文件中
//		// fos.write(b);
//		// fos.flush();
//		// }
//		String oldPath = "C:/Users/Administrator/Desktop/Untitled-2.html";
//		String newPath = "C:/Users/Administrator/Desktop/jiekou0/Untitled-2.html";
//		TestHtml t = new TestHtml();
//		t.copyFile(oldPath, newPath);
//
//	}
}
