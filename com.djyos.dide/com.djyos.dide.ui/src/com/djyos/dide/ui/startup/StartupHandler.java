package com.djyos.dide.ui.startup;

import java.io.File;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IStartup;

import com.djyos.dide.ui.wizards.djyosProject.tools.DideHelper;

public class StartupHandler implements IStartup{
	
	DideHelper dideHelper = new DideHelper();
	@SuppressWarnings("static-access")
	@Override
	public void earlyStartup() {
		// TODO Auto-generated method stub
		
		DideHelper dideHelper = new DideHelper();
		String djysrcPath= dideHelper.getDjyosSrcPath();
//		SetJvmEnv sje = new SetJvmEnv();
//		sje.setenv("Path", dideHelper.getDIDEPath()+"yagarto-gcc4.9/bin;" + 
//				dideHelper.getDIDEPath()+"yagarto-gcc4.9/lib/gcc/arm-none-eabi/4.9.3;"+
//				dideHelper.getDIDEPath()+"csky-gcc4.5.1/bin;"+
//				dideHelper.getDIDEPath()+"csky-gcc4.5.1/csky-abiv2-elf-tools/bin;"+
//				dideHelper.getDIDEPath()+"csky-gcc4.5.1/csky-abiv2-elf-tools/libexec/gcc/csky-abiv2-elf/4.5.1;"+
//				dideHelper.getDIDEPath()+"csky-gcc4.5.1/csky-elf-tools/bin;"+
//				dideHelper.getDIDEPath()+"csky-gcc4.5.1/csky-elf-tools/libexec/gcc/csky-elf/4.5.1;"+
//				System.getenv("Path"));
//		System.out.println("\nsje.getEnv:  "+System.getenv("Path"));
		FileHandler fileHandler = new FileHandler();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(fileHandler, IResourceChangeEvent.POST_BUILD);
		
//		GitHandler gitHandler = new GitHandler(); 
//		gitHandler.remindUpdate();
		
//		File file = new File(djysrcPath);
//		if(file.exists()) {
//			//��ʼ�������ռ������
//			ConfigurationHandler cfgHandler=new ConfigurationHandler();
//			cfgHandler.handlerConfiguration();
//			
//			//�������й���δ���ų�������ļ�
//			HandleProjectImport folderListener = new HandleProjectImport(); 
//			folderListener.handlProjectImport();
//		}
		
//		System.out.println("SvnUpdateHandler:");
		File stup_complie_file = new File(dideHelper.getDIDEPath()+"auto_complier.txt");
		if(stup_complie_file.exists()) {
			SvnUpdateHandler svnHandler = new SvnUpdateHandler();
			svnHandler.visitSvn();
		}

	}
}
