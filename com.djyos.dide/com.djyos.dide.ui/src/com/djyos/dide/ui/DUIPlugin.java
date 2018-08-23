package com.djyos.dide.ui;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.ibm.icu.text.MessageFormat;

public class DUIPlugin extends AbstractUIPlugin{
	public static final String PLUGIN_ID = "com.djyos.dide.ui"; //$NON-NLS-1$
	private static DUIPlugin fgDPlugin;
	
	public DUIPlugin() {

	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		// TODO Auto-generated method stub
		super.start(context);
		fgDPlugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub
		fgDPlugin = null;
		super.stop(context);
	}

	public static DUIPlugin getDefault() {
		return fgDPlugin;
	}
	
	
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
