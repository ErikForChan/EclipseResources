package com.djyos.dide.ui.wizards.djyosProject.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileTool {
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
