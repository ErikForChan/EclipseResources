/*******************************************************************************
 * Copyright (c) 2017 Djyos Team.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.djyos.com
 *
 * Contributors:
 *     Djyos Team - Jiaming Chen
 *******************************************************************************/
package com.djyos.dide.ui.chipdrv;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.messages.IPrompt;

@SuppressWarnings("restriction")
public class ChipdrvCommonProject extends BasicNewResourceWizard {

	private String wz_title, wz_desc;
	private ChipdrvMianWizardPage fMainPage;
	private IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

	public ChipdrvCommonProject(String title, String desc) {
		// TODO Auto-generated constructor stub
		wz_title = title;
		wz_desc = desc;
	}

	@Override
	public void addPages() {
		// TODO Auto-generated method stub
		fMainPage = new ChipdrvMianWizardPage("ChipdrvMainWiazrdPage");
		fMainPage.setTitle(wz_title);
		fMainPage.setDescription(wz_desc);
		addPage(fMainPage);
		super.addPages();
	}

	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		String chipName = fMainPage.getChipName();
		if (chipName == null) {
			DideHelper.showErrorMessage(IPrompt.fillChipdrvName);
			return false;
		} else {
			String djysrcPath = DideHelper.getDjyosSrcPath();
			File myChipDriverFile = new File(djysrcPath + "/bsp/chipdrv/" + chipName);
			if (!myChipDriverFile.exists()) {
				myChipDriverFile.mkdirs();
			}
			File myChipDrvCFile = new File(djysrcPath + "/bsp/chipdrv/" + chipName + "/" + chipName + ".c");
			File myChipDrvHFile = new File(djysrcPath + "/bsp/chipdrv/include/" + chipName + ".h");

			String path = DideHelper.getTemplatePath();
			File CFile = new File(path + "/FileTemp/CFileTemplate");
			File HFile = new File(path + "/FileTemp/HFileTemplate");
			String CString = DideHelper.readFile(CFile);
			String HString = DideHelper.readFile(HFile);
			DideHelper.writeFile(myChipDrvCFile, CString);
			DideHelper.writeFile(myChipDrvHFile, HString);

			openFile(myChipDrvCFile);
			openFile(myChipDrvHFile);

			try {
				final IWorkspace workspace = ResourcesPlugin.getWorkspace();
				workspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return true;
		}
	}

	private void openFile(File file) {
		// TODO Auto-generated method stub
		IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(file.getParentFile().getPath()));
		fileStore = fileStore.getChild(file.getName());
		IFileInfo fetchInfo = fileStore.fetchInfo();
		if (!fetchInfo.isDirectory() && fetchInfo.exists()) {
			IWorkbenchPage page = window.getActivePage();
			try {
				IDE.openEditorOnFileStore(page, fileStore);
			} catch (PartInitException e1) {
				String msg = NLS.bind(IDEWorkbenchMessages.OpenLocalFileAction_message_errorOnOpen,
						fileStore.getName());
				IDEWorkbenchPlugin.log(msg, e1.getStatus());
				MessageDialog.open(MessageDialog.ERROR, window.getShell(),
						IDEWorkbenchMessages.OpenLocalFileAction_title, msg, SWT.SHEET);
			}
		}
	}

	@Override
	public boolean performCancel() {
		// TODO Auto-generated method stub
		return super.performCancel();
	}

}
