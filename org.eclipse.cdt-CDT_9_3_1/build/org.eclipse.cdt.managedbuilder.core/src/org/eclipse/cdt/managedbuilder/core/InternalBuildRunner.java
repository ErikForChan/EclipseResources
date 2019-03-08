/*******************************************************************************
 * Copyright (c) 2010, 2017 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - Initial API and implementation
 * James Blackburn (Broadcom Corp.)
 * IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.core.BuildRunnerHelper;
import org.eclipse.cdt.managedbuilder.buildmodel.BuildDescriptionManager;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildDescription;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.BuildStateManager;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.DescriptionBuilder;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.IConfigurationBuildState;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.IProjectBuildState;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.ParallelBuilder;
import org.eclipse.cdt.managedbuilder.internal.core.CommonBuilder;
import org.eclipse.cdt.managedbuilder.internal.core.CommonBuilder.BuildStatus;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedMakeMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * The build runner for the internal builder.
 *
 * @author dschaefer
 * @since 8.0
 */
public class InternalBuildRunner extends AbstractBuildRunner {
	private static final int PROGRESS_MONITOR_SCALE = 100;
	private static final int TICKS_STREAM_PROGRESS_MONITOR = 1 * PROGRESS_MONITOR_SCALE;
	private static final int TICKS_DELETE_MARKERS = 1 * PROGRESS_MONITOR_SCALE;
	private static final int TICKS_EXECUTE_COMMAND = 1 * PROGRESS_MONITOR_SCALE;
	private static final int TICKS_REFRESH_PROJECT = 1 * PROGRESS_MONITOR_SCALE;

	@SuppressWarnings("deprecation")
	@Override
	public boolean invokeBuild(int kind, IProject project, IConfiguration configuration, IBuilder builder,
			IConsole console, IMarkerGenerator markerGenerator, IncrementalProjectBuilder projectBuilder,
			IProgressMonitor monitor) throws CoreException {
		BuildRunnerHelper buildRunnerHelper = new BuildRunnerHelper(project);

		try {
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}
			monitor.beginTask("", TICKS_STREAM_PROGRESS_MONITOR + TICKS_DELETE_MARKERS + TICKS_EXECUTE_COMMAND //$NON-NLS-1$
					+ TICKS_REFRESH_PROJECT);

			boolean isParallel = builder.getParallelizationNum() > 1;
			boolean resumeOnErr = !builder.isStopOnError();

			int flags = 0;
			IResourceDelta delta = projectBuilder.getDelta(project);
			BuildStateManager bsMngr = BuildStateManager.getInstance();
			IProjectBuildState pBS = bsMngr.getProjectBuildState(project);
			IConfigurationBuildState cBS = pBS.getConfigurationBuildState(configuration.getId(), true);

			// if(delta != null){
			flags = BuildDescriptionManager.REBUILD | BuildDescriptionManager.REMOVED
					| BuildDescriptionManager.DEPS;
			// delta = getDelta(currentProject);
			// }
			boolean buildIncrementaly = delta != null;

			ICConfigurationDescription cfgDescription = ManagedBuildManager
					.getDescriptionForConfiguration(configuration);

			// Prepare launch parameters for BuildRunnerHelper
			String cfgName = configuration.getName();
			String toolchainName = configuration.getToolChain().getName();
			boolean isConfigurationSupported = configuration.isSupported();

			URI workingDirectoryURI = ManagedBuildManager.getBuildLocationURI(configuration, builder);

			String[] errorParsers = builder.getErrorParsers();
			ErrorParserManager epm = new ErrorParserManager(project, workingDirectoryURI, markerGenerator,
					errorParsers);

			List<IConsoleParser> parsers = new ArrayList<IConsoleParser>();
			ManagedBuildManager.collectLanguageSettingsConsoleParsers(cfgDescription, epm, parsers);

			buildRunnerHelper.prepareStreams(epm, parsers, console,
					new SubProgressMonitor(monitor, TICKS_STREAM_PROGRESS_MONITOR));

			IBuildDescription des = BuildDescriptionManager.createBuildDescription(configuration, cBS, delta,
					flags);
			DescriptionBuilder dBuilder = null;
			if (!isParallel) {
				dBuilder = new DescriptionBuilder(des, buildIncrementaly, resumeOnErr, cBS);
				if (dBuilder.getNumCommands() <= 0) {
					buildRunnerHelper.printLine(ManagedMakeMessages
							.getFormattedString("ManagedMakeBuilder.message.no.build", project.getName())); //$NON-NLS-1$
					return false;
				}
			}

			buildRunnerHelper.removeOldMarkers(project, new SubProgressMonitor(monitor, TICKS_DELETE_MARKERS,
					SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

			if (buildIncrementaly) {
				buildRunnerHelper.greeting(IncrementalProjectBuilder.INCREMENTAL_BUILD, cfgName,
						toolchainName, isConfigurationSupported);
			} else {
				buildRunnerHelper.greeting(
						ManagedMakeMessages.getResourceString("ManagedMakeBuider.type.rebuild"), cfgName, //$NON-NLS-1$
						toolchainName, isConfigurationSupported);
			}
			buildRunnerHelper.printLine(ManagedMakeMessages
					.getResourceString("ManagedMakeBuilder.message.internal.builder.header.note")); //$NON-NLS-1$

			OutputStream stdout = buildRunnerHelper.getOutputStream();
			OutputStream stderr = buildRunnerHelper.getErrorStream();

			int status;
			epm.deferDeDuplication();
			try {

				if (dBuilder != null) {
					status = dBuilder.build(stdout, stderr, new SubProgressMonitor(monitor,
							TICKS_EXECUTE_COMMAND, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
				} else {
					status = ParallelBuilder.build(des, null, null, stdout, stderr,
							new SubProgressMonitor(monitor, TICKS_EXECUTE_COMMAND,
									SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK),
							resumeOnErr, buildIncrementaly);
					// Bug 403670:
					// Make sure the build configuration's rebuild status is updated with the result of
					// this successful build. In the non-parallel case this happens within dBuilder.build
					// (the cBS is passed as an instance of IResourceRebuildStateContainer).
					if (status == ParallelBuilder.STATUS_OK)
						cBS.setState(0);
					buildRunnerHelper.printLine(ManagedMakeMessages.getFormattedString("CommonBuilder.7", //$NON-NLS-1$
							Integer.toString(ParallelBuilder.lastThreadsUsed)));
				}
			} finally {
				epm.deDuplicate();
			}

			// 1 ERROR 0 yes
			if (status == ParallelBuilder.STATUS_ERROR || status == ParallelBuilder.STATUS_INVALID) {
				
				String fullPath = Platform.getInstallLocation().getURL().toString().replace("\\", "/");
				String didePath = fullPath.substring(6,
						(fullPath.substring(0, fullPath.length() - 1)).lastIndexOf("/") + 1);
				File stup_complie_file = new File(didePath + "auto_complier.txt"); //$NON-NLS-1$
				if (stup_complie_file.exists()) {
					File errorFile = new File(didePath + "IDE/readme/errorResult.txt"); //$NON-NLS-1$
					String errMsg = project.getName() + "->" + cfgName + "， \n";
					setErrorFile(errorFile, errMsg);
//					 SendErrorEmail email = new SendErrorEmail();
//					 email.send(errMsg);
				}
			}

			bsMngr.setProjectBuildState(project, pBS);
			buildRunnerHelper.close();
			// buildRunnerHelper.goodbye();

			if (status != ICommandLauncher.ILLEGAL_COMMAND) {
				buildRunnerHelper.refreshProject(cfgName, new SubProgressMonitor(monitor,
						TICKS_REFRESH_PROJECT, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
			} else {
			}

			/*
			 * New after Interalbuilder build successthen invoke Externalbuilder
			 */

			BuildStatus buildstatus = new BuildStatus(builder);
			buildstatus.setRebuild();

			if (status == ParallelBuilder.STATUS_OK) {
				buildRunnerHelper.printLine("正在切换到External Builder，请等候..."); //$NON-NLS-1$
				CommonBuilder cb = new CommonBuilder();
				((Configuration) configuration).enableInternalBuilder(false);
				IBuilder myBuilder = configuration.getEditableBuilder();
				CommonBuilder.CfgBuildInfo bInfo = cb.getCfgBuildInfo(myBuilder, true);

				// IPreferenceStore将clearConsole设置为false
				ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE,
						"org.eclipse.cdt.ui"); //$NON-NLS-1$
				preferenceStore.setValue("clearConsole", false); //$NON-NLS-1$

				cb.performPrebuildGeneration(kind, bInfo, buildstatus, monitor);
				// IFolder folder = project.getFolder(cfgName);
				// File makefile = new File(folder.getLocation().toFile().getPath() + "/makefile");
				// if (!makefile.exists()) {
				// makefile.createNewFile();
				// }
				cb.build(kind, bInfo, monitor);
			}
			/*
			 * New after Interalbuilder build successthen invoke Externalbuilder
			 */

		} catch (Exception e) {
			projectBuilder.forgetLastBuiltState();

			String msg = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.error.build", //$NON-NLS-1$
					new String[] { project.getName(), configuration.getName() });
			throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID, msg, e));
		} finally {
			try {
				buildRunnerHelper.close();
			} catch (IOException e) {
				ManagedBuilderCorePlugin.log(e);
			}
			// 刷新当前工程
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
			monitor.done();
		}

		return false;
	}

	private void setErrorFile(File errorFile, String errMsg) {
		// TODO Auto-generated method stub
		BufferedReader br = null;
		String line = null;
		StringBuffer bufAll = new StringBuffer(); // 保存修改过后的所有内容，不断增加
		try {
			br = new BufferedReader(new FileReader(errorFile));
			while ((line = br.readLine()) != null) {
				StringBuffer buf = new StringBuffer();
				bufAll.append(line + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					br = null;
				}
			}
		}
		bufAll.append(errMsg + "\n");
		errorFile.delete();
		writeFile(errorFile, bufAll.toString());

	}

	public void writeFile(File file, String content) {
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FileWriter writer;
		try {
			writer = new FileWriter(file);
			writer.write(content);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
