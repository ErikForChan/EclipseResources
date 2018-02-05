package org.eclipse.cdt.ui.wizards;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.DjyosProjectNature;

import org.eclipse.cdt.internal.ui.newui.Messages;

/**
 * The wizard to create new MBS DJYOS Project.
 */
public class DjyosProjectWizard extends DjyosCommonProjectWizard {
	
	public DjyosProjectWizard() {
		super(Messages.NewModelProjectWizard_6, Messages.NewModelProjectWizard_7);
	}

	@Override
	public String[] getNatures() {
		return new String[] { DjyosProjectNature.DJYOS_NATURE_ID };
	}
	
	@Override
	protected IProject continueCreation(IProject prj) {
		if (continueCreationMonitor == null) {
			continueCreationMonitor = new NullProgressMonitor();
		}
		
		try {
			continueCreationMonitor.beginTask("Add DJYOS Project Nature", 1);
			DjyosProjectNature.addDjyosNature(prj, new SubProgressMonitor(continueCreationMonitor, 1));
		} catch (CoreException e) {}
		finally {continueCreationMonitor.done();}
		return prj;
	}

	@Override
	public String[] getContentTypeIDs() {
		return new String[] { CCorePlugin.CONTENT_TYPE_CSOURCE, CCorePlugin.CONTENT_TYPE_CHEADER };
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
		// TODO Auto-generated method stub
		
	}
	
}
