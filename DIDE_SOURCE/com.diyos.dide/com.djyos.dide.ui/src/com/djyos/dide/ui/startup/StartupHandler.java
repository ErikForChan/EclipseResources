package com.djyos.dide.ui.startup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IStartup;

import com.djyos.dide.ui.autotesting.ProjectAutomation;
import com.djyos.dide.ui.git.GitAutomation;
import com.djyos.dide.ui.git.GitHandler;
import com.djyos.dide.ui.handlers.ConfigurationHandler;
import com.djyos.dide.ui.handlers.FileHandler;
import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.objects.Component;
import com.djyos.dide.ui.wizards.component.ReadComponent;
import com.djyos.dide.ui.wizards.djyosProject.tools.PathTool;

public class StartupHandler implements IStartup {

	File didePrefsFile = new File(PathTool.getDIDEPath() + "IDE/configuration/.settings/com.djyos.ui.prefs");

	@Override
	public void earlyStartup() {
		// TODO Auto-generated method stub
		FileHandler fileHandler = new FileHandler();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(fileHandler, IResourceChangeEvent.POST_BUILD);

		Handle_Git();

		Init_Workspace();

		Auto_Test_Build();
		
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
		File stup_complie_file = new File(PathTool.getDIDEPath() + "auto_complier.txt"); //$NON-NLS-1$
		if (stup_complie_file.exists()) {
			GitAutomation.start_gitMonitor();
		}
	}

	private void Init_Workspace() {
		// TODO Auto-generated method stub
		File file = new File(PathTool.getDjyosSrcPath());
		if (file.exists()) {
			
			// 处理所有工程未被排除编译的文件：组件、板件、Cpu、arch
			HandleProjectImport folderListener = new HandleProjectImport();
			folderListener.handlProjectImport();

			// 初始化工作空间的配置，设置一些默认的参数
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
