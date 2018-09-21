package com.djyos.dide.ui.startup;

import java.io.File;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IStartup;

import com.djyos.dide.ui.wizards.djyosProject.tools.DideHelper;

public class StartupHandler implements IStartup{

	@Override
	public void earlyStartup() {
		// TODO Auto-generated method stub
		
		FileHandler fileHandler = new FileHandler();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(fileHandler, IResourceChangeEvent.POST_BUILD);
		
		GitHandler gitHandler = new GitHandler(); 
		gitHandler.remindUpdate();
		
		DideHelper dideHelper = new DideHelper();
		String djysrcPath= dideHelper.getDjyosSrcPath();
		File file = new File(djysrcPath);
		if(file.exists()) {
			//初始化工作空间的配置
			ConfigurationHandler cfgHandler=new ConfigurationHandler();
			cfgHandler.handlerConfiguration();
			
			//处理所有工程未被排除编译的文件
			HandleProjectImport folderListener = new HandleProjectImport(); 
			folderListener.handlProjectImport();
		}
		
	}
}
