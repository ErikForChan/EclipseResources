package org.eclipse.cdt.ui.wizards;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.DjyosProjectNature;

import org.eclipse.cdt.internal.ui.newui.Messages;

/**
 * The wizard to create new MBS DJYOS Project.
 */
public class DjyosProjectWizard extends DjyosCommonProjectWizard {
	
	public DjyosProjectWizard() {
		super(Messages.NewModelProjectWizard_6, Messages.NewModelProjectWizard_7);
	}
	
}
