package com.djyos.dide.ui.job;

import java.io.File;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.djyos.dide.ui.helper.DideHelper;
import com.ibm.icu.text.MessageFormat;

public class BuildTarget extends Job{
	private ICConfigurationDescription[] cfgDescriptions;
	private int cleanKind;
	private int buildKind;

	private static String composeJobName(ICConfigurationDescription[] cfgDescriptions, boolean isCleaning) {
		String firstProjectName = cfgDescriptions[0].getProjectDescription().getName();
		String firstConfigurationName = cfgDescriptions[0].getName();
		if (isCleaning) {
			return MessageFormat.format("Cleaning {1}[{2}], ...  [{0}]",
					new Object[] { cfgDescriptions.length, firstProjectName, firstConfigurationName });
		} else {
			return MessageFormat.format("Building {1}[{2}], ...  [{0}]",
					new Object[] { cfgDescriptions.length, firstProjectName, firstConfigurationName });
		}
	}
	
	/**
	 * Constructor.
	 * 
	 * @param cfgDescriptions - a list of configurations to build, possibly from different projects
	 * @param cleanKind - pass {@link IncrementalProjectBuilder#CLEAN_BUILD} to clean before building
	 * @param buildKind - kind of build. Can be
	 *    {@link IncrementalProjectBuilder#INCREMENTAL_BUILD}
	 *    {@link IncrementalProjectBuilder#FULL_BUILD}
	 *    {@link IncrementalProjectBuilder#AUTO_BUILD}
	 */
	public BuildTarget(ICConfigurationDescription[] cfgDescriptions, int cleanKind, int buildKind) {
		super(composeJobName(cfgDescriptions, buildKind == 0));

		this.cfgDescriptions = cfgDescriptions;
		this.cleanKind = cleanKind;
		this.buildKind = buildKind;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		// Setup the global build console
		CUIPlugin.getDefault().startGlobalConsole();

		IConfiguration[] cfgs = new IConfiguration[cfgDescriptions.length];
		for (int i= 0; i < cfgDescriptions.length; i++) {
			cfgs[i] = ManagedBuildManager.getConfigurationForDescription(cfgDescriptions[i]);
		}
		try {
			if (cleanKind == IncrementalProjectBuilder.CLEAN_BUILD) {
				ManagedBuildManager.buildConfigurations(cfgs, null, monitor, true, cleanKind);
			}
			if (buildKind != 0) {
				ManagedBuildManager.buildConfigurations(cfgs, null, monitor, true, buildKind);
			}
		} catch (CoreException e) {
			return new Status(IStatus.ERROR, "Build error", e.getLocalizedMessage());
		}
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		monitor.done();
		return Status.OK_STATUS;
	}

	@Override
	public boolean belongsTo(Object family) {
		return ResourcesPlugin.FAMILY_MANUAL_BUILD == family;
	}
}
