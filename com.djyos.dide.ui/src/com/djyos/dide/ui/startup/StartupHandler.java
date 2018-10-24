package com.djyos.dide.ui.startup;

import java.io.File;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IStartup;

import com.djyos.dide.ui.wizards.djyosProject.tools.DideHelper;

public class StartupHandler implements IStartup {
	DideHelper dideHelper = new DideHelper();
	File didePrefsFile = new File(dideHelper.getDIDEPath() + "IDE/configuration/.settings/com.djyos.ui.prefs");

	@SuppressWarnings("static-access")
	@Override
	public void earlyStartup() {
		// TODO Auto-generated method stub

		DideHelper dideHelper = new DideHelper();
		String djysrcPath = dideHelper.getDjyosSrcPath();
		FileHandler fileHandler = new FileHandler();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(fileHandler, IResourceChangeEvent.POST_BUILD);

		boolean gitPrompt = dideHelper.targetIsTrue(didePrefsFile, "SHOW_GIT_UPDATE_DIALOG");
		if (gitPrompt) {
			GitHandler gitHandler = new GitHandler();
			gitHandler.remindUpdate(0);
		}

		File file = new File(djysrcPath);
		if (file.exists()) {
			// ��ʼ�������ռ������
			ConfigurationHandler cfgHandler = new ConfigurationHandler();
			cfgHandler.handlerConfiguration();

			// �������й���δ���ų�������ļ�
			HandleProjectImport folderListener = new HandleProjectImport();
			folderListener.handlProjectImport();
		}

		// System.out.println("SvnUpdateHandler:");
		File stup_complie_file = new File(dideHelper.getDIDEPath() + "auto_complier.txt");
		if (stup_complie_file.exists()) {
			SvnUpdateHandler svnHandler = new SvnUpdateHandler();
			svnHandler.visitSvn();
		}

	}
}
