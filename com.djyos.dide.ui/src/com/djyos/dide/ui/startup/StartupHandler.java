package com.djyos.dide.ui.startup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IStartup;

import com.djyos.dide.ui.autotesting.SvnUpdateHandler;
import com.djyos.dide.ui.git.GitHandler;
import com.djyos.dide.ui.handlers.ConfigurationHandler;
import com.djyos.dide.ui.handlers.FileHandler;
import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.objects.Component;
import com.djyos.dide.ui.wizards.component.ReadComponent;

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
				DideHelper.showErrorMessage("Դ������������� ("+c.getName()+") ��ͬ�����޸�!");
			}
		}
		
	}

	private void Auto_Test_Build() {
		// TODO Auto-generated method stub
		File stup_complie_file = new File(DideHelper.getDIDEPath() + "complieAuto.txt");
		if (stup_complie_file.exists()) {
			SvnUpdateHandler svnHandler = new SvnUpdateHandler();
			svnHandler.visitSvn();
		}
	}

	private void Init_Workspace() {
		// TODO Auto-generated method stub
		File file = new File(DideHelper.getDjyosSrcPath());
		if (file.exists()) {
			
			// �������й���δ���ų�������ļ�
			HandleProjectImport folderListener = new HandleProjectImport();
			folderListener.handlProjectImport();

			// ��ʼ�������ռ������
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
