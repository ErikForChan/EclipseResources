package com.djyos.dide.ui.startup;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IStartup;

public class StartupHandler implements IStartup{

	@Override
	public void earlyStartup() {
		// TODO Auto-generated method stub
		
//		SaveFileHandler saveHandler = new SaveFileHandler();
//		ResourcesPlugin.getWorkspace().addResourceChangeListener(saveHandler, IResourceChangeEvent.POST_BUILD);
		
//		GitHandler gitHandler = new GitHandler();
//		gitHandler.remindUpdate();
//		
		AddDefaultPostCmd defaultPostCmd=new AddDefaultPostCmd();
		defaultPostCmd.getDefaultPostCmd();
		
		HandleFolderAdded folderListener = new HandleFolderAdded(); 
		folderListener.handleAddeed();

	}
}
