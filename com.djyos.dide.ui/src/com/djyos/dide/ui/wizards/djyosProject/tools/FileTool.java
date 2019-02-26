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
		InputStream input = null; // ׼����һ������Ķ���
		try {
			input = new FileInputStream(f);
			// ��3�������ж�����
			byte b[] = new byte[1024*10000]; // ���е����ݶ�����������֮��
			input.read(b); // ��ȡ���� �������� read ����������
			// ��4�����ر������
			input.close(); // �ر������
			String data = bytesToHexString(b);
			DideHelper.printToConsole(data, true);
//			System.out.println("����Ϊ��\n" + new String(b)); // ��byte�����Ϊ�ַ������
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // ͨ�������̬�ԣ�����ʵ����
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
	 * * Convert byte[] to hex string.�������ǿ��Խ�byteת����int��Ȼ������Integer.toHexString(int)
	 * ��ת����16�����ַ�����
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
	 * ���Ƶ����ļ�
	 * 
	 * @param oldPath
	 *            String ԭ�ļ�·�� �磺c:/fqf.txt
	 * @param newPath
	 *            String ���ƺ�·�� �磺f:/fqf.txt
	 * @return boolean
	 */
	public static void copyFile(String oldPath, String newPath) {
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) { // �ļ�����ʱ
				InputStream inStream = new FileInputStream(oldPath); // ����ԭ�ļ�
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				int length;
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; // �ֽ��� �ļ���С
//					System.out.println(bytesum);
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
			}
		} catch (Exception e) {
			System.out.println("���Ƶ����ļ���������");
			e.printStackTrace();

		}

	}

	/**
	 * ���������ļ�������
	 * 
	 * @param oldPath
	 *            String ԭ�ļ�·�� �磺c:/fqf
	 * @param newPath
	 *            String ���ƺ�·�� �磺f:/fqf/ff
	 * @return boolean
	 */
	public static void copyFolder(String oldPath, String newPath) {

		try {
			(new File(newPath)).mkdirs(); // ����ļ��в����� �������ļ���
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
				if (temp.isDirectory()) {// ��������ļ���
					copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
				}
			}
		} catch (Exception e) {
			System.out.println("���������ļ������ݲ�������");
			e.printStackTrace();

		}

	}
//
//	public static void main(String[] args) throws Exception {
//		// //�������Դ�ļ��������Ǵ��ڵ�
//		// File beforefile = new File("C:/Users/Administrator/Desktop/Untitled-2.html");
//		//
//		// //������Ҫ����֮����ļ������Զ���ģ���������
//		// File afterfile = new
//		// File("C:/Users/Administrator/Desktop/jiekou0/Untitled-2.html");
//		//
//		// //�����ļ���������������ȡbeforefile�ļ�
//		// FileInputStream fis = new FileInputStream(beforefile);
//		//
//		// //�����ļ����������������Ϣд��afterfile�ļ���
//		// FileOutputStream fos = new FileOutputStream(afterfile);
//		//
//		// //�ļ�������
//		// byte[] b = new byte[1024];
//		// //���ļ�����Ϣ��ȡ�ļ��������������ȡ�����Ϊ-1�ʹ����ļ�û�ж�ȡ��ϣ���֮�Ѿ���ȡ���
//		// while(fis.read(b)!=-1){
//		// //���������е�����д��afterfile�ļ���
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
