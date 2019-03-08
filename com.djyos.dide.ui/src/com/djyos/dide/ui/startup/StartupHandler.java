package com.djyos.dide.ui.startup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult.MergeStatus;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.djyos.dide.ui.autotesting.SvnUpdateHandler;
import com.djyos.dide.ui.git.GitAutomation;
import com.djyos.dide.ui.git.GitHandler;
import com.djyos.dide.ui.handlers.ConfigurationHandler;
import com.djyos.dide.ui.handlers.FileHandler;
import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.objects.Component;
import com.djyos.dide.ui.wizards.component.ReadComponent;
import com.ibm.icu.text.SimpleDateFormat;

public class StartupHandler implements IStartup {

	File didePrefsFile = new File(DideHelper.getDIDEPath() + "IDE/configuration/.settings/com.djyos.ui.prefs");

	@Override
	public void earlyStartup() {
		// TODO Auto-generated method stub
		FileHandler fileHandler = new FileHandler();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(fileHandler, IResourceChangeEvent.POST_BUILD);

		Handle_Git();

		Init_Workspace();

//		Auto_Test_Build();
		
		Check_Compt_Repeat();

	}

	private void Check_Compt_Repeat() {
		// TODO Auto-generated method stub
		List<Component>  workspaceCompts = ReadComponent.getWorkspaceComponents();
		List<String> cptNames = new ArrayList<String>();
		for(Component c:workspaceCompts) {
			if(!cptNames.contains(c.getName())) {
				cptNames.add(c.getName());
			}else {
				DideHelper.showErrorMessage("源码中两组件名称 ("+c.getName()+") 相同，请修改!");
			}
		}
		
	}

	private void Auto_Test_Build() {
		// TODO Auto-generated method stub
		GitAutomation.start_gitMonitor();
	}

	private void Init_Workspace() {
		// TODO Auto-generated method stub
		File file = new File(DideHelper.getDjyosSrcPath());
		if (file.exists()) {
			
			// 处理所有工程未被排除编译的文件
			HandleProjectImport folderListener = new HandleProjectImport();
			folderListener.handlProjectImport();

			// 初始化工作空间的配置
			ConfigurationHandler cfgHandler = new ConfigurationHandler();
			cfgHandler.handlerConfiguration();

		}
	}

	private void Handle_Git() {
		// TODO Auto-generated method stub
		boolean gitPrompt = DideHelper.targetIsTrue(didePrefsFile, "SHOW_GIT_UPDATE_DIALOG");
		if (gitPrompt) {
			GitHandler gitHandler = new GitHandler();
			gitHandler.remind_Update(0);
		}
	}
}
