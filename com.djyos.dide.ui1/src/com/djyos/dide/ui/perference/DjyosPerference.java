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
package com.djyos.dide.ui.perference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.wizards.djyosProject.tools.PathTool;

@SuppressWarnings("restriction")
public class DjyosPerference extends PreferencePage implements IWorkbenchPreferencePage {

	boolean showUpdate;
	private Button showGitUpdateButton;
	private Button atuo_buildlibos_new_Button;
	private Button atuo_buildlibos_import_Button;
	private Button noa_buildlibos__Button;
	boolean atuo_buildlibos_new;
	boolean atuo_buildlibos_import;
	boolean noa_buildlibos;
	File didePrefsFile = new File(PathTool.getDIDEPath() + "IDE/configuration/.settings/com.djyos.ui.prefs");

	public DjyosPerference() {
		super();
		setPreferenceStore(CUIPlugin.getDefault().getPreferenceStore());
		setDescription("General settings for DIDE development:");
	}

	/**
	 * The user has pressed Ok. Store/apply this page's values appropriately.
	 */
	@Override
	public boolean performOk() {
		boolean noticeMe = showGitUpdateButton.getSelection();
		boolean build_new = atuo_buildlibos_new_Button.getSelection();
		boolean build_import = atuo_buildlibos_import_Button.getSelection();
		boolean build_noa = noa_buildlibos__Button.getSelection();
		if (!didePrefsFile.exists()) {
			fillGitPrefsFile(didePrefsFile, "SHOW_GIT_UPDATE_DIALOG=" + noticeMe);
		} else {
			if (showUpdate != noticeMe) {
				setDjyosUiPrefs(didePrefsFile, noticeMe, "SHOW_GIT_UPDATE_DIALOG");
			}
		}
		if (build_new != atuo_buildlibos_new) {
			setDjyosUiPrefs(didePrefsFile, build_new, "AUTO_BUILDLIBOS_NEW");
		}
		if (build_import != atuo_buildlibos_import) {
			setDjyosUiPrefs(didePrefsFile, build_import, "AUTO_BUILDLIBOS_IMPORT");
		}
		if (build_noa != noa_buildlibos) {
			setDjyosUiPrefs(didePrefsFile, build_noa, "NOA_BUILDLIBOS");
		}
		return super.performOk();
	}

	private void setDjyosUiPrefs(File didePrefsFile, boolean isTrue, String target) {
		// TODO Auto-generated method stub
		BufferedReader br = null;
		String line = null;
		boolean targetExist = false;
		StringBuffer bufAll = new StringBuffer(); // 保存修改过后的所有内容，不断增加
		try {
			br = new BufferedReader(new FileReader(didePrefsFile));
			while ((line = br.readLine()) != null) {
				// 修改内容核心代码
				if (line.startsWith(target)) {
					line = target + "=" + isTrue;
					targetExist = true;
				}
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
		if (!targetExist) {
			bufAll.append(target + "=" + isTrue + "\n");
		}
		didePrefsFile.delete();
		fillGitPrefsFile(didePrefsFile, bufAll.toString());

	}

	private void fillGitPrefsFile(File dideGitPrefsFile, String content) {
		// TODO Auto-generated method stub
		try {
			dideGitPrefsFile.createNewFile();
			FileWriter writer;
			try {
				writer = new FileWriter(dideGitPrefsFile);
				writer.write(content);
				writer.flush();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), ICHelpContextIds.C_PREF_PAGE);
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

	@Override
	protected Control createContents(Composite parent) {
		// TODO Auto-generated method stub
		initializeDialogUnits(parent);

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = 0;
		layout.verticalSpacing = convertVerticalDLUsToPixels(5);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		if (didePrefsFile.exists()) {
			showUpdate = DideHelper.targetIsTrue(didePrefsFile, "SHOW_GIT_UPDATE_DIALOG");
			atuo_buildlibos_new = DideHelper.targetIsTrue(didePrefsFile, "AUTO_BUILDLIBOS_NEW");
			atuo_buildlibos_import = DideHelper.targetIsTrue(didePrefsFile, "AUTO_BUILDLIBOS_IMPORT");
			noa_buildlibos = DideHelper.targetIsTrue(didePrefsFile, "NOA_BUILDLIBOS");
		} else {
			showUpdate = true;
			atuo_buildlibos_new = false;
			atuo_buildlibos_import = false;
			noa_buildlibos = false;
		}

		showGitUpdateButton = new Button(composite, SWT.CHECK);
		showGitUpdateButton.setText("Djyos源码有更新时提示我");
		showGitUpdateButton.setSelection(showUpdate);

		atuo_buildlibos_new_Button = new Button(composite, SWT.CHECK);
		atuo_buildlibos_new_Button.setText("新建工程后自动编译库");
		atuo_buildlibos_new_Button.setSelection(atuo_buildlibos_new);

		atuo_buildlibos_import_Button = new Button(composite, SWT.CHECK);
		atuo_buildlibos_import_Button.setText("导入工程后自动编译库");
		atuo_buildlibos_import_Button.setSelection(atuo_buildlibos_import);

		noa_buildlibos__Button = new Button(composite, SWT.CHECK);
		noa_buildlibos__Button.setText("编译iboot/App前，如果没有生成.o，则先自动编译库");
		noa_buildlibos__Button.setSelection(noa_buildlibos);

		Dialog.applyDialogFont(composite);
		return composite;
	}
}
