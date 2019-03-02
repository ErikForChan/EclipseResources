package com.djyos.dide.ui.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import com.djyos.dide.ui.objects.Component;
import com.djyos.dide.ui.wizards.component.ComponentHelper;

public class ExceptionHelper {
	
	/*
	 * @parm  configFile �����е�project_config.h
	 * ���ܣ���Դ�������еĲ�����ɾ����������ʱ���޸�project_config.h
	 */
	public static void revice_config_file(File configFile, List<Component> compontents) {
		// TODO Auto-generated method stub  abccj 
		FileReader reader;
		String configure = "";
		Component compt = null;
		try {
			reader = new FileReader(configFile.getPath());
			BufferedReader br = new BufferedReader(reader);
			String str = null;
			String temp_paras = ""; //project_config.h�е�ǰ��������в�������
			String temp_configure = null; //Դ�ļ���ǰ�������������
			while ((str = br.readLine()) != null) {
				boolean toAdd = true;
				String[] members = str.split("\\s+");
				if (str.contains("Configure")) {
					if (compt != null) {
						String[] cur_compt_paras = get_paras_defined(compt.getConfigure()).split("\n");
						for(String para:cur_compt_paras) {
							if(!temp_paras.contains(para.split("\\s+")[1])) {
								configure += para+"\n"; //Դ�����ļ���������define��ӵ�project_config.h��
							}
						}
					}
				}
				if (str.contains("Configure")) {
					compt = ComponentHelper.getComponentByName(members[2], compontents);
					if(compt != null) {
						temp_configure = get_paras_defined(compt.getConfigure());
					}
					temp_paras = "";
				}else if(compt != null &&  str.contains("#define")) {
					temp_paras += str+"\n";
					String para = members[1];
					if(!temp_configure.contains(para) && !str.contains("CFG_CORE_MCLK")) {
						toAdd = false; //str�ѱ�ɾ��
					}
				}
				if(toAdd) {
					configure += str+"\n";
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DideHelper.writeFile(configFile, configure, false);
	}
	
	/*
	 * @configure ����������� һ���ַ���
	 * ���ܣ���һ���ַ����л�ȡ���е�define
	 */
	private static String get_paras_defined(String configure) {
		String[] all_lines = configure.split("\n");
		String paras = "";
		for(String line:all_lines) {
			if(line.contains("#define")) {
				paras += line +"\n";
			}
		}
		return paras;
	}
	
}
