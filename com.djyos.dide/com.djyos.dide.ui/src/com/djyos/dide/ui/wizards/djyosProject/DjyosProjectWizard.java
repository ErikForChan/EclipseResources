package com.djyos.dide.ui.wizards.djyosProject;

import java.io.File;
import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.djyos.dide.ui.wizards.djyosProject.tools.DideHelper;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.DjyosProjectNature;
import com.djyos.dide.ui.wizards.djyosProject.DjyosCommonProjectWizard;

/**
 * The wizard to create new MBS DJYOS Project.
 */
public class DjyosProjectWizard extends DjyosCommonProjectWizard {
	
	public DjyosProjectWizard() {
		super("New Djyos", "创建Djyos工程");
	}
	
}
