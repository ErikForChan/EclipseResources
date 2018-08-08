package org.eclipse.cdt.ui.startup;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IStartup;

public class FolderAddedHandler implements IStartup{

	@Override
	public void earlyStartup() {
		// TODO Auto-generated method stub
		HandleFolderAdded folderListener = new HandleFolderAdded(); 
		folderListener.handleAddeed();
	}

}
