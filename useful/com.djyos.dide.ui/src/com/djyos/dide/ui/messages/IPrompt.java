package com.djyos.dide.ui.messages;

public interface IPrompt {

	static final String promptLabel = "��ʾ";

	/**
	 * DjyosԴ�벻���� ��ʾ��Ϣ
	 */
	static final String djysrcNotExit = "DjyosԴ�벻���ڣ�������DIDE������ʾ����";
	
	/**
	 * �����Ѿ�����  ��ʾ��Ϣ
	 */
	static final String projectExit = "�����ռ���ߴ����Ѿ�����Ŀ�깤�� !";
	// static final String djysrcNotExit = "DjyosԴ�벻���ڣ�������Eclipse������ʾ����";

	/**
	 * оƬ���� ��ʾ��Ϣ
	 */
	static final String fillChipdrvName = "����доƬ����...";
	
	/**
	 * memory.lds ��ʾ��Ϣ
	 */
	String Lds_Head_Promopt = "\n/*����MEMORY�����ʹ�÷��ţ���Щ�����Ķ��壬������MEMORY���һ�� */ \n\n" + "MEMORY\n" + "{";
	
	/**
	 * ģ��1 ��ʾ��Ϣ
	 */
	static String templateDesc0 = "���ڿ���iboot��App�Ĺ��̣�App��iboot\n������" + "����App��iboot��һ���Ŷ�ά�������";
	
	/**
	 * ģ��2 ��ʾ��Ϣ
	 */
	static String templateDesc1 = "���ڿ���iboot�Ĺ��̣�����App��iboot�ɲ�ͬ" + "�Ŷ�ά�������";
	
	/**
	 * ģ��2 ��ʾ��Ϣ
	 */
	static String templateDesc2 = "���ڿ���App�Ĺ��̣�App������Ҫ����iboot�ĳ�" + "�� ����App��iboot�ɲ�ͬ�Ŷ�ά�������";
	
	/**
	 * ģ��4 ��ʾ��Ϣ
	 */
	static String templateDesc3 = "���ڿ�������iboot�����������е�App����";
	
}
