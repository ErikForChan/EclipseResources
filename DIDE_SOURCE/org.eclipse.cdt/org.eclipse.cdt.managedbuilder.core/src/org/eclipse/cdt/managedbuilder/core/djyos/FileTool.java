package org.eclipse.cdt.managedbuilder.core.djyos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileTool {
	
	public static void setErrorFile(File errorFile, String errMsg) {
		// TODO Auto-generated method stub
		BufferedReader br = null;
		String line = null;
		StringBuffer bufAll = new StringBuffer(); // 保存修改过后的所有内容，不断增加
		try {
			br = new BufferedReader(new FileReader(errorFile));
			while ((line = br.readLine()) != null) {
				StringBuffer buf = new StringBuffer();
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
		bufAll.append(errMsg + "\n");
		errorFile.delete();
		writeFile(errorFile, bufAll.toString());

	}

	public static void writeFile(File file, String content) {
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
}
