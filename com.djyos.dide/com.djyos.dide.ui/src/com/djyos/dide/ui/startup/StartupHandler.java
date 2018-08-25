package com.djyos.dide.ui.startup;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IStartup;

public class StartupHandler implements IStartup{

	@Override
	public void earlyStartup() {
		// TODO Auto-generated method stub
		
//		FileHandler fileHandler = new FileHandler();
//		ResourcesPlugin.getWorkspace().addResourceChangeListener(fileHandler, IResourceChangeEvent.POST_BUILD);
		
//		GitHandler gitHandler = new GitHandler(); 
//		gitHandler.remindUpdate();
//		
		//初始化工作空间的配置
		ConfigurationHandler cfgHandler=new ConfigurationHandler();
		cfgHandler.handlerConfiguration();
		
		//处理所有工程未被排除编译的文件
		HandleFolderAdded folderListener = new HandleFolderAdded(); 
		folderListener.handleAddeed();

	}
}
