package com.djyos.dide.ui.wizards.cpu;

public interface ICpuConstants {

	static String config_corenum = "�ں˸���";
	static String config_reset = "��λ����";
	static String config_core = "�ں�����";
	static String config_memory = "˽�д洢����";
	static String config_fpu = "��������";
	static String config_group = "��������";
	static String config_shared_memory = "����洢����";
	static String config_head = "Cpu������";
	static String name_cpu = "Cpu����: ";
	static String name_folder = "��Ŀ¼����: ";
	static String title_cpu = "�½�Cpu";
	static String title_group = "�½���Ŀ¼";
	static String title_cpu_revice = "�޸�Cpu����";
	static String title_folder_revice = "�޸���Ŀ¼����";
	
	static String menu_new_cpu = "�½�Cpu";
	static String menu_new_arch = "�½�Arch";
	static String menu_new_group = "�½���Ŀ¼";
	static String menu_revice = "�޸�����";
	static String menu_delete = "ɾ��";
	static String menu_add_cpudrv = "���Cpu��������";
	
	static String list_cpu = "Cpu�б�";
	static String list_arch = "Arch�б�";
	static String warm_prompt = "�Ҽ������Cpu����Ŀ¼";
	static final String info_prompt = "ѡ����Ŀ¼����Cpu������ʾ��Ӧ��������Ϣ :";
	static String wizard_prompt  = "IDE�ּ�Ŀ¼����ʽ�������ϵͳ֧�ֵ��ڶ�Cpu��\r\n" + 
			"���������ڹ���Cpu�ķ���,�������Cpu����Ŀ¼,֧���ֶ���ק";
	static String input_int = "������������";
	static String core_select = "�ں�ѡ��: ";
	
	static final String warning_notDragToSelf = "��������������Ŀ¼�£�";
	static final String warning_notDragToChild = "��Ŀ¼������������Ŀ¼��";
	static final String warning_notDragToCpu = "����������CpuĿ¼�£�";
	
	static String que_exit_cpudrvInterface = "�Ƿ��˳�����ȥ��д����?";
	
	String[] fpuTypes = { "Toolchain default", "fpv5-d16", "fpv5-sp-d16", "fpv4-sp-d16", "fpv4-d16" };
	
}
