package com.djyos.dide.ui.wizards.djyosProject.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
	public static String replaceBlank(String str) {
		String dest = "";
		if (str != null) {
			Pattern p = Pattern.compile("\\s*|\t|\r|\n");
			Matcher m = p.matcher(str);
			dest = m.replaceAll("");
		}
		return dest;
	}

	public static String replaceDesc(String descContent, String oldStr, String newStr) {
		// TODO Auto-generated method stub
		StringBuffer bufAll = new StringBuffer(); 
		String[] strs = descContent.split("\n");
		for(int i=0;i<strs.length;i++) {
			String s = strs[i];
			if(s.contains(oldStr)) {
				s = s.replace(oldStr, newStr);
			}
			bufAll.append(s);
			if(i < strs.length-1) {
				bufAll.append("\n");
			}
		}
		return bufAll.toString();
	}
}
