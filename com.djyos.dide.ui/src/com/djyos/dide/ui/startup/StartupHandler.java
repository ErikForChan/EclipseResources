package com.djyos.dide.ui.startup;

import java.io.File;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IStartup;

import com.djyos.dide.ui.autotesting.SvnUpdateHandler;
import com.djyos.dide.ui.git.GitHandler;
import com.djyos.dide.ui.handlers.ConfigurationHandler;
import com.djyos.dide.ui.handlers.FileHandler;
import com.djyos.dide.ui.helper.DideHelper;

public class StartupHandler implements IStartup {

	File didePrefsFile = new File(DideHelper.getDIDEPath() + "IDE/configuration/.settings/com.djyos.ui.prefs");

	@Override
	public void earlyStartup() {
		// TODO Auto-generated method stub
		FileHandler fileHandler = new FileHandler();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(fileHandler, IResourceChangeEvent.POST_BUILD);

		Handle_Git();

		Init_Workspace();

		Auto_Test_Build();

		Detect_ST_Driver();
	}

	private void Detect_ST_Driver() {
		// TODO Auto-generated method stub

	}

	private void Auto_Test_Build() {
		// TODO Auto-generated method stub
		File stup_complie_file = new File(DideHelper.getDIDEPath() + "auto_complier.txt");
		if (stup_complie_file.exists()) {
			SvnUpdateHandler svnHandler = new SvnUpdateHandler();
			svnHandler.visitSvn();
		}
	}

	private void Init_Workspace() {
		// TODO Auto-generated method stub
		File file = new File(DideHelper.getDjyosSrcPath());
		if (file.exists()) {
			// 初始化工作空间的配置
			ConfigurationHandler cfgHandler = new ConfigurationHandler();
			cfgHandler.handlerConfiguration();

			// 处理所有工程未被排除编译的文件
			HandleProjectImport folderListener = new HandleProjectImport();
			folderListener.handlProjectImport();
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
