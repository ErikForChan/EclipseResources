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
package com.djyos.dide.ui.bsp;

import java.io.File;

import org.eclipse.cdt.ui.wizards.CWizardHandler;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea.IErrorMessageReporter;

import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.objects.Board;
import com.djyos.dide.ui.objects.Core;
import com.djyos.dide.ui.objects.Cpu;
import com.djyos.dide.ui.wizards.board.GetBoardDialog;
import com.djyos.dide.ui.wizards.djyosProject.tools.PathTool;

@SuppressWarnings("restriction")
public class BspProjectMainWizard extends WizardPage {

	public String ldsHead = "", ldsDesc = "", boardModuleTrimPath, projectPath, boardName, initialProjectFieldValue;
	private Text fProjectNameField, fBoardNameField;
	boolean nameValid = false;
	public CWizardHandler h_selected;
	private Board selectedBoard;
	private Cpu selectedCpu;
	private Core selectedCore;
	boolean clickedNext = true;
	public ProjectContentsLocationArea locationArea;
	private IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

	protected BspProjectMainWizard(String pageName) {
		super(pageName);
		setPageComplete(false);
		// TODO Auto-generated constructor stub
	}

	public String getBoardName() {
		return fBoardNameField.getText().trim();
	}

	public Cpu getSelectCpu() {
		return selectedCpu;
	}

	public Board getSelectBoard() {
		return selectedBoard;
	}

	public Core getSelectCore() {
		return selectedCore;
	}

	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(parent, SWT.NULL);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 25;
		layout.verticalSpacing = 20;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		createDynamicGroup(composite);
		// Show description on opening
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
	}

	private void createDynamicGroup(Composite composite) {
		// TODO Auto-generated method stub
		createProjectAndBoardGroup(composite);
		// createBoardGroup(composite);
		locationArea = new ProjectContentsLocationArea(getErrorReporter(), composite);
		if (initialProjectFieldValue != null) {
			locationArea.updateProjectName(initialProjectFieldValue);
		}
		// Scale the button based on the rest of the dialog
		setButtonLayoutData(locationArea.getBrowseButton());
	}

	public String getProjectNameFieldValue() {
		if (fProjectNameField == null) {
			return null; // $NON-NLS-1$
		}
		return fProjectNameField.getText().trim();
	}

	private String getBoardNameFieldValue() {
		if (fBoardNameField == null) {
			return ""; //$NON-NLS-1$
		}
		return fBoardNameField.getText().trim();
	}

	protected boolean validate_BspMainPage() {
		IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();
		String projectFieldContents = getProjectNameFieldValue();

		if (projectFieldContents.equals("")) { //$NON-NLS-1$
			setErrorMessage(null);
			setMessage("请填写工程名 !");
			return false;
		}

		String prjPathSelect = locationArea.locationPathField.getText();
		if (!prjPathSelect.endsWith(projectFieldContents)) {
			prjPathSelect = prjPathSelect + "\\" + projectFieldContents;
		}
		File prjFile = new File(prjPathSelect);
		if (prjFile.exists()) {
			setErrorMessage("工作空间或者磁盘已经存在目标工程 !");
			return false;
		}

		String boardFieldContents = getBoardNameFieldValue();
		if (boardFieldContents.equals("")) { //$NON-NLS-1$
			setMessage("请选择板件 !");
			return false;
		}

		IStatus nameStatus = workspace.validateName(projectFieldContents, IResource.PROJECT);
		if (!nameStatus.isOK()) {
			setErrorMessage(nameStatus.getMessage());
			return false;
		}

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectNameFieldValue());
		locationArea.setExistingProject(project);

		String validLocationMessage = locationArea.checkValidLocation();
		if (validLocationMessage != null) { // there is no destination location given
			setErrorMessage("工作空间已经存在目标工程 !");
			return false;
		}

		setErrorMessage(null);
		setMessage(null);
		return true;
	}

	private void createProjectAndBoardGroup(Composite parent) {
		Composite projectGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		layout.verticalSpacing = 15;
		projectGroup.setLayout(layout);
		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// 工程名
		Label projectLabel = new Label(projectGroup, SWT.NONE);
		projectLabel.setText(IDEWorkbenchMessages.WizardNewProjectCreationPage_nameLabel);
		fProjectNameField = new Text(projectGroup, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		fProjectNameField.setLayoutData(data);
		fProjectNameField.addListener(SWT.Modify, nameModifyListener);
		BidiUtils.applyBidiProcessing(fProjectNameField, BidiUtils.BTD_DEFAULT);
		Button testBtn = new Button(projectGroup, SWT.PUSH);
		testBtn.setVisible(false);
		Button test1Btn = new Button(projectGroup, SWT.PUSH);
		test1Btn.setVisible(false);

		// 板件
		Label boardLabel = new Label(projectGroup, SWT.NONE);
		boardLabel.setText("Board:");
		fBoardNameField = new Text(projectGroup, SWT.BORDER);
		fBoardNameField.setEnabled(false);
		fBoardNameField.addListener(SWT.Modify, boardModifyListener);
		fBoardNameField.setLayoutData(data);
		BidiUtils.applyBidiProcessing(fBoardNameField, BidiUtils.BTD_DEFAULT);
		Button selectBoardBtn = new Button(projectGroup, SWT.PUSH);
		selectBoardBtn.setText("Select");
		Button createBoardBtn = new Button(projectGroup, SWT.PUSH);
		createBoardBtn.setText("Create");// BoardWizard

		// 创建板件按钮的监听
		createBoardBtn.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				// 获取板件向导的Action，通过run()运行该向导
				IAction action = DideHelper.getAction("com.djyos.dide.ui.wizards.NewDWizard2");
				action.run();
			}

		});
		// 选择板件按钮的监听
		selectBoardBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				boolean djysrcExist = true;
				File djysrcFile = new File(PathTool.getDjyosSrcPath());
				if (djysrcFile.exists()) {
					File[] files = djysrcFile.listFiles();
					if (files.length < 2) {
						djysrcExist = false;
					}
				} else {
					djysrcExist = false;
				}
				if (!djysrcExist) {
					MessageDialog.openInformation(window.getShell(), "提示", "Djyos源码不存在，请重启Eclipse根据提示下载");
				} else {
					GetBoardDialog dialog = new GetBoardDialog(getShell());
					if (dialog.open() == Window.OK) {
						boardName = dialog.getBoardName();
						selectedCpu = dialog.getSelectCpu();
						selectedBoard = dialog.getSelectBoard();
						selectedCore = dialog.getSelectCore();
						boardModuleTrimPath = dialog.boardModuleTrimPath;
						fBoardNameField.setText(boardName);
					}
				}
			}
		});

	}

	private IErrorMessageReporter getErrorReporter() {
		return (errorMessage, infoOnly) -> {
			if (infoOnly) {
				setMessage(errorMessage, IStatus.INFO);
				setErrorMessage(null);
			} else
				setErrorMessage(errorMessage);
			boolean valid = errorMessage == null;
			if (valid) {
				valid = validate_BspMainPage();
			}

			setPageComplete(valid);
		};
	}

	void setLocationForSelection() {
		locationArea.updateProjectName(getProjectNameFieldValue());
	}

	private Listener nameModifyListener = e -> {
		setLocationForSelection();
		boolean valid = validate_BspMainPage();
		if (nameValid != valid) {
			setPageComplete(valid);
			nameValid = valid;
		}

	};

	private Listener boardModifyListener = e -> {
		// setLocationForSelection();
		boolean valid = validate_BspMainPage();
		setPageComplete(valid);
	};

	public String getProjectName() {
		// TODO Auto-generated method stub
		if (fProjectNameField == null) {
			return initialProjectFieldValue;
		}

		return getProjectNameFieldValue();
	}

}
