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
		//��ʼ�������ռ������
		ConfigurationHandler cfgHandler=new ConfigurationHandler();
		cfgHandler.handlerConfiguration();
		
		//�������й���δ���ų�������ļ�
		HandleFolderAdded folderListener = new HandleFolderAdded(); 
		folderListener.handleAddeed();

	}
}
