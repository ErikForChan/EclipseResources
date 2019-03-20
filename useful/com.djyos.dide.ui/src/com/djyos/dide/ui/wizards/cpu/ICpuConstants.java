package com.djyos.dide.ui.wizards.cpu;

public interface ICpuConstants {

	static String config_corenum = "内核个数";
	static String config_reset = "复位配置";
	static String config_core = "内核配置";
	static String config_memory = "私有存储配置";
	static String config_fpu = "浮点配置";
	static String config_group = "分组配置";
	static String config_shared_memory = "共享存储配置";
	static String config_head = "Cpu配置项";
	static String name_cpu = "Cpu名称: ";
	static String name_folder = "子目录名称: ";
	static String title_cpu = "新建Cpu";
	static String title_group = "新建子目录";
	static String title_cpu_revice = "修改Cpu配置";
	static String title_folder_revice = "修改子目录配置";
	
	static String menu_new_cpu = "新建Cpu";
	static String menu_new_arch = "新建Arch";
	static String menu_new_group = "新建子目录";
	static String menu_revice = "修改配置";
	static String menu_delete = "删除";
	static String menu_add_cpudrv = "添加Cpu外设驱动";
	
	static String list_cpu = "Cpu列表";
	static String list_arch = "Arch列表";
	static String warm_prompt = "右键可添加Cpu和子目录";
	static final String info_prompt = "选中子目录或者Cpu即可显示相应的配置信息 :";
	static String wizard_prompt  = "IDE分级目录的形式管理操作系统支持的众多Cpu，\r\n" + 
			"本界面用于管理Cpu的分类,包括添加Cpu和子目录,支持手动拖拽";
	static String input_int = "请输入正整数";
	static String core_select = "内核选择: ";
	
	static final String warning_notDragToSelf = "不可拖拉到自身目录下！";
	static final String warning_notDragToChild = "父目录不可托拉到子目录！";
	static final String warning_notDragToCpu = "不可托拉到Cpu目录下！";
	
	static String que_exit_cpudrvInterface = "是否退出界面去编写驱动?";
	
	String[] fpuTypes = { "Toolchain default", "fpv5-d16", "fpv5-sp-d16", "fpv4-sp-d16", "fpv4-d16" };
	
}
