package com.djyos.dide.ui.messages;

public interface IPrompt {

	static final String promptLabel = "提示";

	/**
	 * Djyos源码不存在 提示信息
	 */
	static final String djysrcNotExit = "Djyos源码不存在，请重启DIDE根据提示下载";
	
	/**
	 * 工会已经存在  提示信息
	 */
	static final String projectExit = "工作空间或者磁盘已经存在目标工程 !";
	// static final String djysrcNotExit = "Djyos源码不存在，请重启Eclipse根据提示下载";

	/**
	 * 芯片驱动 提示信息
	 */
	static final String fillChipdrvName = "请填写芯片名称...";
	
	/**
	 * memory.lds 提示信息
	 */
	String Lds_Head_Promopt = "\n/*由于MEMORY命令不能使用符号，这些常量的定义，必须与MEMORY命令处一致 */ \n\n" + "MEMORY\n" + "{";
	
	/**
	 * 模板1 提示信息
	 */
	static String templateDesc0 = "用于开发iboot和App的工程，App由iboot\n启动，" + "用于App和iboot由一个团队维护的情况";
	
	/**
	 * 模板2 提示信息
	 */
	static String templateDesc1 = "用于开发iboot的工程，用于App和iboot由不同" + "团队维护的情况";
	
	/**
	 * 模板2 提示信息
	 */
	static String templateDesc2 = "用于开发App的工程，App工程需要输入iboot的尺" + "寸 用于App和iboot由不同团队维护的情况";
	
	/**
	 * 模板4 提示信息
	 */
	static String templateDesc3 = "用于开发无需iboot，自启动运行的App工程";
	
}
