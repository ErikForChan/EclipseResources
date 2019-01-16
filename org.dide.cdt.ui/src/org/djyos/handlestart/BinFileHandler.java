package org.djyos.handlestart;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IStartup;

public class BinFileHandler implements IStartup {

	@Override
	public void earlyStartup() {
		// TODO Auto-generated method stub
		ResourceUpdater resourceListener = new ResourceUpdater();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener);	
	}

}
