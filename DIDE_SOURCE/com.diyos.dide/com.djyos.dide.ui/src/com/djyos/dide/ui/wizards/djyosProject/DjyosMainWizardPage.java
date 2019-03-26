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
package com.djyos.dide.ui.wizards.djyosProject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.ui.wizards.CWizardHandler;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
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
import com.djyos.dide.ui.messages.IDataLegalPrompt;
import com.djyos.dide.ui.messages.IPrompt;
import com.djyos.dide.ui.objects.Board;
import com.djyos.dide.ui.objects.Core;
import com.djyos.dide.ui.objects.CoreMemory;
import com.djyos.dide.ui.objects.Cpu;
import com.djyos.dide.ui.objects.OnBoardCpu;
import com.djyos.dide.ui.objects.OnBoardMemory;
import com.djyos.dide.ui.wizards.board.GetBoardDialog;
import com.djyos.dide.ui.wizards.djyosProject.tools.PathTool;

@SuppressWarnings("restriction")
public class DjyosMainWizardPage extends WizardPage {
	public static final String PAGE_ID = "org.eclipse.cdt.managedbuilder.ui.wizard.NewModelProjectWizardPage"; //$NON-NLS-1$

	public static final String DESC = "EntryDescriptor"; //$NON-NLS-1$
	public String ldsHead = "", ldsDesc = "", boardModuleTrimPath, projectPath, boardName, initialProjectFieldValue;
	private static IntegerFieldEditor fIbootSize;
	private static Composite ibootComposite;

	private Text fProjectNameField, fBoardNameField;
	private static Text projectTypeDesc;
	public CWizardHandler h_selected;
	private static Button[] radioBtns = new Button[4];
	private Board selectedBoard;
	private Cpu selectedCpu;
	private Core selectedCore;
	boolean clickedNext = true;
	private IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

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

	public ProjectContentsLocationArea locationArea;

	boolean nameValid = false;

	private Listener nameModifyListener = e -> {
		setLocationForSelection();
		boolean valid = Validate_DjyosMainPage();
		if (nameValid != valid) {
			setPageComplete(valid);
			nameValid = valid;
		}

	};

	private Listener boardModifyListener = e -> {
		// setLocationForSelection();
		boolean valid = Validate_DjyosMainPage();
		setPageComplete(valid);
	};

	private Listener ibootSizeModifyListener = e -> {
		String bootSize = fIbootSize.getTextControl(ibootComposite).getText();
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
		boolean isInt = pattern.matcher(bootSize).matches();
		if (!isInt) {
			fIbootSize.getTextControl(ibootComposite).setText("");
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			MessageDialog.openError(window.getShell(), IPrompt.promptLabel, IDataLegalPrompt.promoteDesc_Int_Data);
		} else {
			boolean valid = Validate_DjyosMainPage();
			setPageComplete(valid);
		}
	};

	void setLocationForSelection() {
		locationArea.updateProjectName(getProjectNameFieldValue());
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

	public DjyosMainWizardPage(String pageName) {
		super(pageName);
		setPageComplete(false);
	}

	@Override
	public void createControl(Composite parent) {
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
//		 System.out.println("Java运行环境的版本:" + System.getProperty("java.version"));
//		 System.out.println("Java运行环境的生产商:" + System.getProperty("java.vendor"));
//		 System.out.println("Java的安装路径：" + System.getProperty("java.home"));
//		 System.out.println("虚拟机实现的版本：" + System.getProperty("java.vm.version"));
//		 System.out.println("虚拟机实现的生产商：" + System.getProperty("java.vm.vendor"));
//		 System.out.println("默认的临时文件路径：" + System.getProperty("java.io.tmpdir"));
//		 System.out.println("用户的账户名称：" + System.getProperty("user.name"));
//		 System.out.println("当前用户工作目录：" + System.getProperty("user.dir"));
//		 System.out.println("用户的home路径：" + System.getProperty("user.home"));
//		 System.out.println("操作系统的名称:" + System.getProperty("os.name"));
//		 System.out.println("操作系统的版本：" + System.getProperty("os.version"));
//		 System.out.println("操作系统的架构：" + System.getProperty("os.arch"));
//		 System.out.println("运行环境规范的名称:" +
//		 System.getProperty("java.specification.name"));
//		 System.out.println("Java类格式化的版本号：" +
//		 System.getProperty("java.class.version"));
//		 System.out.println("类所在的路径：" + System.getProperty("java.class.path"));

		creatTemplateUI(composite);
		createProjectAndBoardGroup(composite);
		locationArea = new ProjectContentsLocationArea(getErrorReporter(), composite);
		if (initialProjectFieldValue != null) {
			locationArea.updateProjectName(initialProjectFieldValue);
		}
		// Scale the button based on the rest of the dialog
		setButtonLayoutData(locationArea.getBrowseButton());
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
					MessageDialog.openInformation(window.getShell(), IPrompt.promptLabel, IPrompt.djysrcNotExit);
				} else {
					GetBoardDialog dialog = new GetBoardDialog(getShell());
					if (dialog.open() == Window.OK) {
						boardName = dialog.getBoardName();
						selectedCpu = dialog.getSelectCpu();
						selectedBoard = dialog.getSelectBoard();
						selectedCore = dialog.getSelectCore();
						boardModuleTrimPath = dialog.boardModuleTrimPath;
						String boardLdsFolderPath = selectedBoard.getBoardFolderPath() + "/lds";
						if(selectedCore.getName() != null) {
							File coreLdsFolder = new File(boardLdsFolderPath+"/"+selectedCore.getName());
							if(coreLdsFolder.exists()) {
								boardLdsFolderPath = coreLdsFolder.getPath();
							}
						}
						File ldsFile = new File(boardLdsFolderPath);
						File[] ldsFiles = ldsFile.listFiles();
						if (ldsFiles.length < 3) {
							radioBtns[0].setSelection(false);
							radioBtns[0].setEnabled(false);

							radioBtns[1].setSelection(true);

							radioBtns[2].setSelection(false);
							radioBtns[2].setEnabled(false);

							radioBtns[3].setSelection(false);
							radioBtns[3].setEnabled(false);
							projectTypeDesc.setText("用于开发iboot的工程，用于App和iboot由不同团队维护的情况");
							// 用于开发iboot的工程，用于App和iboot由不同团队维护的情况
						} else {
							radioBtns[0].setEnabled(true);
							radioBtns[1].setEnabled(true);
							radioBtns[2].setEnabled(true);
							radioBtns[3].setEnabled(true);
						}
						fBoardNameField.setText(boardName);
						File mldsFile = new File(boardLdsFolderPath + "/memory.lds");
						String ibootSize = readIbootSize(mldsFile, "IbootSize");
						if (ibootSize != null) {
							fIbootSize.getTextControl(ibootComposite).setText(ibootSize);
						}
					}
				}
				
				DjyosCommonProjectWizard nmWizard = (DjyosCommonProjectWizard) getWizard();
				nmWizard.addedComptCfg = false;
				clickedNext = false;
			}
		});

	}

	private String readIbootSize(File file, String target) {
		BufferedReader br = null;
		String line = null;
		try {
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				// 修改内容核心代码
				if (line.startsWith(target)) {
					String[] infos = line.split(";|；");
					return infos[0].split("=")[1].trim().replaceFirst("k|K", "");
				}
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
		return null;
	}

	public String getLdsHead() {
		ldsHead += IPrompt.Lds_Head_Promopt;

		List<OnBoardMemory> onBoardMemorys_ROM = new ArrayList<OnBoardMemory>();
		List<OnBoardMemory> onBoardMemorys_RAM = new ArrayList<OnBoardMemory>();
		List<CoreMemory> coreMemorys_ROM = new ArrayList<CoreMemory>();
		List<CoreMemory> coreMemorys_RAM = new ArrayList<CoreMemory>();
		for (int i = 0; i < selectedBoard.getOnBoardCpus().size(); i++) {
			OnBoardCpu onBoardCpu = selectedBoard.getOnBoardCpus().get(i);
			for (int j = 0; j < onBoardCpu.getMemorys().size(); j++) {
				if (onBoardCpu.getMemorys().get(j).getType().equals("ROM")) {
					onBoardMemorys_ROM.add(onBoardCpu.getMemorys().get(j));
				} else if (onBoardCpu.getMemorys().get(j).getType().equals("RAM")) {
					onBoardMemorys_RAM.add(onBoardCpu.getMemorys().get(j));
				}
			}
		}
		for (int i = 0; i < selectedCpu.getCores().size(); i++) {
			Core core = selectedCpu.getCores().get(i);
			for (int j = 0; j < core.getMemorys().size(); j++) {
				if (core.getMemorys().get(j).getType().equals("FLASH")) {
					coreMemorys_ROM.add(core.getMemorys().get(j));
				} else if (core.getMemorys().get(j).getType().equals("RAM")) {
					coreMemorys_RAM.add(core.getMemorys().get(j));
				}
			}
		}

		for (int i = 0; i < coreMemorys_ROM.size(); i++) {
			ldsHead += "\n\tInnerFlash" + (i + 1) + "(RX) : ORIGIN = " + coreMemorys_ROM.get(i).getStartAddr()
					+ ", LENGTH = " + coreMemorys_ROM.get(i).getSize();
		}
		for (int i = 0; i < coreMemorys_RAM.size(); i++) {
			ldsHead += "\n\tRAM" + (i + 1) + "(RXW) : ORIGIN = " + coreMemorys_RAM.get(i).getStartAddr() + ", LENGTH = "
					+ coreMemorys_RAM.get(i).getSize();
		}
		for (int i = 0; i < onBoardMemorys_ROM.size(); i++) {
			ldsHead += "\n\textrom" + (i + 1) + "(RX) : ORIGIN = " + onBoardMemorys_ROM.get(i).getStartAddr()
					+ ", LENGTH = " + onBoardMemorys_ROM.get(i).getSize();
		}
		for (int i = 0; i < onBoardMemorys_RAM.size(); i++) {
			ldsHead += "\n\textRam" + (i + 1) + "(RXW) : ORIGIN = " + onBoardMemorys_RAM.get(i).getStartAddr()
					+ ", LENGTH = " + onBoardMemorys_RAM.get(i).getSize();
		}

		ldsHead += "\n}" + "\n";
		return ldsHead;
	}

	public String _ibootSize;

	public String getLdsDesc() {
		_ibootSize = fIbootSize.getTextControl(ibootComposite).getText();
		// ibootSize未填，则Memory.lds不添加ibootSize
		if (!_ibootSize.equals("")) {
			int ibootSize = Integer.parseInt(fIbootSize.getTextControl(ibootComposite).getText());
			ldsDesc += "\nIbootSize = " + ibootSize + "K;\n";
		}

		List<OnBoardMemory> onBoardMemorys_ROM = new ArrayList<OnBoardMemory>();
		List<OnBoardMemory> onBoardMemorys_RAM = new ArrayList<OnBoardMemory>();
		List<CoreMemory> coreMemorys_ROM = new ArrayList<CoreMemory>();
		List<CoreMemory> coreMemorys_RAM = new ArrayList<CoreMemory>();
		for (int i = 0; i < selectedBoard.getOnBoardCpus().size(); i++) {
			OnBoardCpu onBoardCpu = selectedBoard.getOnBoardCpus().get(i);
			for (int j = 0; j < onBoardCpu.getMemorys().size(); j++) {
				if (onBoardCpu.getMemorys().get(j).getType().equals("ROM")) {
					onBoardMemorys_ROM.add(onBoardCpu.getMemorys().get(j));
				} else if (onBoardCpu.getMemorys().get(j).getType().equals("RAM")) {
					onBoardMemorys_RAM.add(onBoardCpu.getMemorys().get(j));
				}
			}
		}
		for (int i = 0; i < selectedCpu.getCores().size(); i++) {
			Core core = selectedCpu.getCores().get(i);
			for (int j = 0; j < core.getMemorys().size(); j++) {
				if (core.getMemorys().get(j).getType().equals("FLASH")) {
					coreMemorys_ROM.add(core.getMemorys().get(j));
				} else if (core.getMemorys().get(j).getType().equals("RAM")) {
					coreMemorys_RAM.add(core.getMemorys().get(j));
				}
			}
		}

		for (int i = 0; i < coreMemorys_ROM.size(); i++) {
			ldsDesc += "\nInnerFlash" + (i + 1) + "Offset = " + "ORIGIN(InnerFlash" + (i + 1) + ");";
			ldsDesc += "\nInnerFlash" + (i + 1) + "Range = " + "LENGTH(InnerFlash" + (i + 1) + ");";
		}
		for (int i = 0; i < coreMemorys_RAM.size(); i++) {
			ldsDesc += "\nInnerRAM" + (i + 1) + "Start = " + "ORIGIN(RAM" + (i + 1) + ");";
			ldsDesc += "\nInnerRAM" + (i + 1) + "Size = " + "LENGTH(RAM" + (i + 1) + ");";
		}
		for (int i = 0; i < onBoardMemorys_ROM.size(); i++) {
			ldsDesc += "\nExtRom" + (i + 1) + "Start = " + "ORIGIN(extRom" + (i + 1) + ");";
			ldsDesc += "\nExtRom" + (i + 1) + "Size = " + "LENGTH(extRom" + (i + 1) + ");";
		}
		for (int i = 0; i < onBoardMemorys_RAM.size(); i++) {
			ldsDesc += "\nExtRam" + (i + 1) + "Start = " + "ORIGIN(extRam" + (i + 1) + ");";
			ldsDesc += "\nExtRam" + (i + 1) + "Size = " + "LENGTH(extRam" + (i + 1) + ");";
		}

		return ldsDesc;
	}

	public void creatTemplateUI(Composite parent) {
		Group group1 = ControlFactory.createGroup(parent, "Choose Template ", 1);
		GridLayout gl = new GridLayout(2, false);
		gl.marginHeight = 10;
		group1.setLayout(gl);

		Composite RADIOCpt = new Composite(group1, SWT.NONE);
		GridLayout radioGl = new GridLayout();
		radioGl.verticalSpacing = 15;
		RADIOCpt.setLayout(radioGl);
		RADIOCpt.setLayoutData(new GridData(SWT.VERTICAL));
		String[] templateLabels = { DjyosMessages.Iboot_App_Project, DjyosMessages.Iboot_Only_Project,
				DjyosMessages.App_Project, DjyosMessages.App_Only_Project };

		for (int i = 0; i < radioBtns.length; i++) {
			radioBtns[i] = new Button(RADIOCpt, SWT.RADIO | SWT.LEFT);
			radioBtns[i].setText(templateLabels[i]);
			int a = i;
			radioBtns[i].addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Handle_ProjectType_Select(a);
				}

			});
		}
		radioBtns[0].setSelection(true);

		Composite right = new Composite(group1, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginLeft = 2;
		right.setLayout(layout);
		right.setLayoutData(new GridData(GridData.FILL_BOTH | SWT.VERTICAL));
		Label templateLabel = new Label(right, SWT.NONE);
		templateLabel.setText(DjyosMessages.Template_Label);
		projectTypeDesc = new Text(right, SWT.MULTI | SWT.WRAP);
		projectTypeDesc.setLayoutData(new GridData(GridData.FILL_BOTH));
		projectTypeDesc.setText(templateDescs[0]);
		Composite content = new Composite(group1, SWT.NULL);
		content.setLayout(new GridLayout(1, true));
		content.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		ibootComposite = new Composite(content, SWT.NULL);
		GridLayout gd = new GridLayout(3, true);
		gd.marginLeft = 100;
		gd.marginHeight = 20;
		ibootComposite.setLayout(gd);
		ibootComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fIbootSize = new IntegerFieldEditor(CCorePreferenceConstants.MAX_INDEX_DB_CACHE_SIZE_MB,
				DjyosMessages.Ibootsize_Label, ibootComposite, 4);
		fIbootSize.setValidRange(1, 10000);
		BidiUtils.applyBidiProcessing(fIbootSize.getTextControl(ibootComposite), BidiUtils.BTD_DEFAULT);
		ControlFactory.createLabel(group1, "K");
		fIbootSize.getTextControl(ibootComposite).addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				String bootSize = fIbootSize.getTextControl(ibootComposite).getText();
				Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
				boolean isInt = pattern.matcher(bootSize).matches();
				if (!isInt) {
					fIbootSize.getTextControl(ibootComposite).setText("");
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					MessageDialog.openError(window.getShell(), IPrompt.promptLabel, IDataLegalPrompt.promoteDesc_Int_Data);
				} 
//				else {
//					boolean valid = Validate_DjyosMainPage();
//					setPageComplete(valid);
//					
//					DjyosCommonProjectWizard nmWizard = (DjyosCommonProjectWizard) getWizard();
//					if(! nmWizard.addedComptCfg) {
//						clickedNext = true;
//					}
//				}
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		// fIbootSize.getTextControl(ibootComposite).addListener(SWT.Modify,
		// ibootSizeModifyListener);
		// fIbootSize.getTextControl(ibootComposite).setEnabled(false);
	}

	protected void Handle_ProjectType_Select(int index) {
		// TODO Auto-generated method stub
		projectTypeDesc.setText(templateDescs[index]);
//		if (index == radioBtns.length - 1) {
//			fIbootSize.getTextControl(ibootComposite).setEnabled(false);
//			fIbootSize.getTextControl(ibootComposite).setText("");
////			boolean valid = Validate_DjyosMainPage();
////			setPageComplete(valid);
//		} else {
//			fIbootSize.getTextControl(ibootComposite).setEnabled(true);
//		}
		
	}

	@Override
	public boolean canFlipToNextPage() {
		// TODO Auto-generated method stub
		clickedNext = false;
		projectPath = locationArea.locationPathField.getText();
		boolean valid = Validate_DjyosMainPage();
		if(valid) {
			return true;
		}
		return false;
//		return super.canFlipToNextPage();
	}
	
	@Override
	public IWizardPage getNextPage() {
		System.out.println("getNextPage DW");
//		boolean isOK = true;
		DjyosCommonProjectWizard nmWizard = (DjyosCommonProjectWizard) getWizard();
		
		OnBoardCpu onBoardCpu = null;
		List<OnBoardCpu> onBoardCpus = selectedBoard.getOnBoardCpus();
		for (int i = 0; i < onBoardCpus.size(); i++) {
			if (onBoardCpus.get(i).getCpuName().equals(selectedCpu.getCpuName())) {
				onBoardCpu = onBoardCpus.get(i);
				break;
			}
		}
		nmWizard.cpomtCfgPage = new ComponentConfigWizard("basicComponentCfgPage", onBoardCpu, selectedBoard,
				haveApp(), haveIboot());
		nmWizard.cpomtCfgPage.setTitle("Component Configuration");
		nmWizard.cpomtCfgPage.setDescription("工程裁剪与配置");
		nmWizard.addPage(nmWizard.cpomtCfgPage);
		
		int tIndex = getTemplateIndex();
		nmWizard.importProject(projectPath, selectedBoard, selectedCore, haveApp(), needIbootLds(), tIndex);
		
		return super.getNextPage();
	}

	public String getProjectName() {
		if (fProjectNameField == null) {
			return initialProjectFieldValue;
		}

		return getProjectNameFieldValue();
	}

	public boolean haveIboot() {
		int index = getTemplateIndex();
		if (index == 0 || index == 1) {
			return true;
		}
		return false;
	}
	
	public boolean needIbootLds() {
		int index = getTemplateIndex();
		if (index == 3) {
			return false;
		}
		return true;
	}

	public boolean haveApp() {
		int index = getTemplateIndex();
		if (index == 0 || index == 2 || index == 3) {
			return true;
		}
		return false;
	}

	public int getTemplateIndex() {
		int index = 0;
		for (int i = 0; i < radioBtns.length; i++) {
			if (radioBtns[i].getSelection()) {
				index = i;
			}
		}
		return index;
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
				valid = Validate_DjyosMainPage();
			}

			setPageComplete(valid);
		};
	}

	public IProject getProjectHandle() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectName());
	}

	public void setExistedMessage() {
		setMessage("Target project existed !");
	}

	protected boolean Validate_DjyosMainPage() {
		IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();
		String projectFieldContents = getProjectNameFieldValue();

		// String ibootSize = fIbootSize.getTextControl(ibootComposite).getText();
		// if(getTemplateIndex() != 3) {
		// if(ibootSize.equals("")) {
		// return false;
		// }
		// }

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
			setErrorMessage(IPrompt.projectExit);
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
			setErrorMessage(IPrompt.projectExit);
			return false;
		}

		setErrorMessage(null);
		setMessage(null);
		return true;
	}

	String[] templateDescs = { IPrompt.templateDesc0, IPrompt.templateDesc1, IPrompt.templateDesc2, IPrompt.templateDesc3 };

}