package com.djyos.dide.ui.wizards.djyosProject;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.actions.NewWizardShortcutAction;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea.IErrorMessageReporter;
import org.eclipse.ui.wizards.IWizardDescriptor;

import com.djyos.dide.ui.wizards.board.onboardcpu.OnBoardCpu;
import com.djyos.dide.ui.wizards.board.onboardcpu.OnBoardMemory;
import com.djyos.dide.ui.wizards.cpu.Cpu;
import com.djyos.dide.ui.wizards.cpu.core.Core;
import com.djyos.dide.ui.wizards.cpu.core.memory.CoreMemory;
import com.djyos.dide.ui.wizards.djyosProject.tools.DideHelper;

import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.CWizardHandler;
import com.djyos.dide.ui.wizards.board.Board;
import com.djyos.dide.ui.wizards.board.BoardWizard;
import com.djyos.dide.ui.wizards.djyosProject.ComponentConfigWizard;
import com.djyos.dide.ui.wizards.djyosProject.DjyosCommonProjectWizard;
import com.djyos.dide.ui.wizards.djyosProject.DjyosMessages;
import com.djyos.dide.ui.wizards.djyosProject.GetBoardDialog;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;

import org.eclipse.cdt.internal.ui.newui.Messages;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;

@SuppressWarnings("restriction")
public class DjyosMainWizardPage extends WizardPage {
	public static final String PAGE_ID = "org.eclipse.cdt.managedbuilder.ui.wizard.NewModelProjectWizardPage"; //$NON-NLS-1$

	private static final String EXTENSION_POINT_ID = "org.eclipse.cdt.ui.CDTWizard"; //$NON-NLS-1$
	private static final String ELEMENT_NAME = "wizard"; //$NON-NLS-1$
	private static final String CLASS_NAME = "class"; //$NON-NLS-1$
	public static final String DESC = "EntryDescriptor"; //$NON-NLS-1$
	public String ldsHead = "",ldsDesc = "",
				  boardModuleTrimPath,projectPath,boardName,initialProjectFieldValue;
	DideHelper dideHelper = new DideHelper();
	// Widgets
	private static IntegerFieldEditor fIbootSize;
	private static Composite ibootComposite;

	private Composite right;
	private Tree tree;
	private Button showSup;
	private Label rightLabel,categorySelectedLabel;
	private Text fProjectNameField,fBoardNameField;

	private static Text projectTypeDesc;
	public CWizardHandler h_selected;
	private static Button[] radioBtns = new Button[4];
	private Combo boardCombo;
	private Board board,selectedBoard;
	private Cpu selectedCpu,defaultCpu;
	private Core selectedCore;
	boolean clickedNext = true;
	private IWorkbenchWindow window = PlatformUI.getWorkbench()
			.getActiveWorkbenchWindow();
	
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
	
	@SuppressWarnings("restriction")
	public static ProjectContentsLocationArea locationArea;

	boolean nameValid = false;
	
	private  Listener nameModifyListener = e -> {
		setLocationForSelection();
		boolean valid = validatePageBefore();
		if(nameValid!=valid) {
			setPageComplete(valid);
			nameValid = valid;
		}

	};
	
	private  Listener boardModifyListener = e -> {
		//setLocationForSelection();
		boolean valid = validatePageBefore();
		setPageComplete(valid);
	};
	
	private  Listener ibootSizeModifyListener = e -> {
		String bootSize = fIbootSize.getTextControl(ibootComposite).getText();
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");  
        boolean isInt =  pattern.matcher(bootSize).matches();
        if(!isInt) {
        	fIbootSize.getTextControl(ibootComposite).setText("");
        	IWorkbenchWindow window = PlatformUI.getWorkbench()
    				.getActiveWorkbenchWindow();
        	MessageDialog.openError(window.getShell(), promoteTitle,
        			promoteDesc_Int_Data);
        }else {
        	boolean valid = validatePageBefore();
    		setPageComplete(valid);
        }
	};
	
	void setLocationForSelection() {
		locationArea.updateProjectName(getProjectNameFieldValue());
	}
	
	public String getProjectNameFieldValue() {
		if (fProjectNameField == null) {
			return null; //$NON-NLS-1$
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

	@SuppressWarnings("restriction")
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
		creatTemplateUI(composite);
		createProjectAndBoardGroup(composite);
		//createBoardGroup(composite);
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
		layout.verticalSpacing = 20;
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
		//板件
		Label boardLabel = new Label(projectGroup, SWT.NONE);
		boardLabel.setText("Board:");
		fBoardNameField = new Text(projectGroup, SWT.BORDER);
		fBoardNameField.setLayoutData(data);
		fBoardNameField.setEnabled(false);
		fBoardNameField.addListener(SWT.Modify, boardModifyListener);
		fBoardNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		BidiUtils.applyBidiProcessing(fBoardNameField, BidiUtils.BTD_DEFAULT);
		Button selectBoardBtn = new Button(projectGroup, SWT.PUSH);
		selectBoardBtn.setText("Select");
		Button createBoardBtn = new Button(projectGroup, SWT.PUSH);
		createBoardBtn.setText("Create");//BoardWizard
		//创建板件按钮的监听
		createBoardBtn.addSelectionListener(new SelectionAdapter() {
			
			public void widgetSelected(SelectionEvent e) {
				//获取板件向导的Action，通过run()运行该向导
				IAction action = getAction("com.djyos.dide.ui.wizards.NewDWizard2");
				action.run();
			}
			
		});
		//选择板件按钮的监听
		selectBoardBtn.addSelectionListener(new SelectionAdapter(){  
            public void widgetSelected(SelectionEvent e){         	
            	boolean djysrcExist = true;
        		File djysrcFile = new File(dideHelper.getDjyosSrcPath());
        		if(djysrcFile.exists()) {
        			File[] files = djysrcFile.listFiles();
        			if(files.length<2) {
        				djysrcExist = false;
        			}
        		}else {
        			djysrcExist = false;
        		}
        		if(!djysrcExist) {
        			MessageDialog.openInformation(window.getShell(), "提示",
        					"Djyos源码不存在，请重启Eclipse根据提示下载");
        		}else {
            		GetBoardDialog dialog = new GetBoardDialog(getShell());
            		if (dialog.open() == Window.OK) {
            			boardName = dialog.getBoardName();
            			selectedCpu = dialog.getSelectCpu();
            			defaultCpu = dialog.defaultCpu;
            			selectedBoard = dialog.getSelectBoard();
            			selectedCore = dialog.getSelectCore();
            			fBoardNameField.setText(boardName);
            			boardModuleTrimPath = dialog.boardModuleTrimPath;
        			}
        		}
            }
        });  
		
	}
	
	private IAction getAction(String id) {
		// Keep a cache, rather than creating a new action each time,
		// so that image caching in ActionContributionItem works.
		IAction action = null;
		IWizardDescriptor wizardDesc = WorkbenchPlugin.getDefault().getNewWizardRegistry().findWizard(id);
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (wizardDesc != null) {
			action = new NewWizardShortcutAction(window, wizardDesc);
			IConfigurationElement element = Adapters.adapt(wizardDesc, IConfigurationElement.class);
			if (element != null) {
				window.getExtensionTracker().registerObject(element.getDeclaringExtension(), action,
						IExtensionTracker.REF_WEAK);
			}
		}
		return action;
	}
		
	private void copyHoldsOptions(IHoldsOptions src, IHoldsOptions dst, IResourceInfo res){
		if(src instanceof ITool) {
			ITool t1 = (ITool)src;
			ITool t2 = (ITool)dst;
			if (t1.getCustomBuildStep()) return;
			t2.setToolCommand(t1.getToolCommand());
			t2.setCommandLinePattern(t1.getCommandLinePattern());
		}
		IOption op1[] = src.getOptions();
		IOption op2[] = dst.getOptions();
		for(int i = 0; i < op1.length; i++) {
			//setOption(op1[i], op2[i], dst, res);
			String enumVal;
			try {
				switch (op1[i].getValueType()) {
				case IOption.BOOLEAN :
					boolean boolVal = op1[i].getBooleanValue();
					ManagedBuildManager.setOption(res, dst, op2[i], boolVal);
					break;
				case IOption.ENUMERATED :
				case IOption.TREE :
					enumVal = op1[i].getStringValue();
					String enumId = op1[i].getId(enumVal);
					System.out.println("enumVal:   "+enumVal +"\nenumId: "+enumId);
					String out = (enumId != null && enumId.length() > 0) ? enumId : enumVal;
					ManagedBuildManager.setOption(res, dst, op2[i], enumVal);
					break;
				case IOption.STRING :
					ManagedBuildManager.setOption(res, dst, op2[i], op1[i].getStringValue());
					break;
				case IOption.INCLUDE_PATH :
				case IOption.PREPROCESSOR_SYMBOLS :
				case IOption.INCLUDE_FILES:
				case IOption.MACRO_FILES:
				case IOption.UNDEF_INCLUDE_PATH:
				case IOption.UNDEF_PREPROCESSOR_SYMBOLS:
				case IOption.UNDEF_INCLUDE_FILES:
				case IOption.UNDEF_LIBRARY_PATHS:
				case IOption.UNDEF_LIBRARY_FILES:
				case IOption.UNDEF_MACRO_FILES:
					@SuppressWarnings("unchecked")
					String[] data = ((List<String>)op1[i].getValue()).toArray(new String[0]);
					ManagedBuildManager.setOption(res, dst, op2[i], data);
					break;
				case IOption.LIBRARIES :
				case IOption.LIBRARY_PATHS:
				case IOption.LIBRARY_FILES:
				case IOption.STRING_LIST :
				case IOption.OBJECTS :
					@SuppressWarnings("unchecked")
					String[] data2 = ((List<String>)op1[i].getValue()).toArray(new String[0]);
					ManagedBuildManager.setOption(res, dst, op2[i], data2);
					break;
				default :
					break;
				}
				
			} catch (BuildException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
	}
	
	public String getLdsHead() {
		ldsHead += Lds_Head_Promopt;

		List<OnBoardMemory> onBoardMemorys_ROM = new ArrayList<OnBoardMemory>();
		List<OnBoardMemory> onBoardMemorys_RAM = new ArrayList<OnBoardMemory>();
		List<CoreMemory> coreMemorys_ROM = new ArrayList<CoreMemory>();
		List<CoreMemory> coreMemorys_RAM = new ArrayList<CoreMemory>();
		for (int i = 0; i < selectedBoard.getOnBoardCpus().size(); i++) {
			OnBoardCpu onBoardCpu = selectedBoard.getOnBoardCpus().get(i);
			for(int j=0;j<onBoardCpu.getMemorys().size();j++) {			
				if(onBoardCpu.getMemorys().get(j).getType().equals("ROM")) {
					onBoardMemorys_ROM.add(onBoardCpu.getMemorys().get(j));
				}else if(onBoardCpu.getMemorys().get(j).getType().equals("RAM")) {
					onBoardMemorys_RAM.add(onBoardCpu.getMemorys().get(j));
				}
			}
		}	
		for(int i=0;i<selectedCpu.getCores().size();i++) {
			Core core = selectedCpu.getCores().get(i);
			for (int j = 0; j < core.getMemorys().size(); j++) {
				if(core.getMemorys().get(j).getType().equals("FLASH")) {
					coreMemorys_ROM.add(core.getMemorys().get(j));				
				}else if(core.getMemorys().get(j).getType().equals("RAM")) {
					coreMemorys_RAM.add(core.getMemorys().get(j));
				}				
			}
		}
		
		for(int i=0;i<coreMemorys_ROM.size();i++) {
			ldsHead += "\n\tInnerFlash"+(i+1)+"(RX) : ORIGIN = "+coreMemorys_ROM.get(i).getStartAddr()
					+", LENGTH = "+coreMemorys_ROM.get(i).getSize();
		}
		for(int i=0;i<coreMemorys_RAM.size();i++) {
			ldsHead += "\n\tRAM"+(i+1)+"(RXW) : ORIGIN = "+coreMemorys_RAM.get(i).getStartAddr()
					+", LENGTH = "+coreMemorys_RAM.get(i).getSize();
		}
		for(int i=0;i<onBoardMemorys_ROM.size();i++) {
			ldsHead += "\n\textrom"+(i+1)+"(RX) : ORIGIN = "+onBoardMemorys_ROM.get(i).getStartAddr()
					+", LENGTH = "+onBoardMemorys_ROM.get(i).getSize();
		}
		for(int i=0;i<onBoardMemorys_RAM.size();i++) {
			ldsHead += "\n\textRam"+(i+1)+"(RXW) : ORIGIN = "+onBoardMemorys_RAM.get(i).getStartAddr()
					+", LENGTH = "+onBoardMemorys_RAM.get(i).getSize();
		}

		ldsHead += "\n}"+"\n";
		return ldsHead;
	}
	
	public String getLdsDesc() {
		String _ibootSize = fIbootSize.getTextControl(ibootComposite).getText();
		//ibootSize未填，则Memory.lds不添加ibootSize
		if(! _ibootSize.equals("")) {
			int ibootSize = Integer.parseInt(fIbootSize.getTextControl(ibootComposite).getText());
			ldsDesc += "\nIbootSize = "+ibootSize+"K;\n";
		}

		List<OnBoardMemory> onBoardMemorys_ROM = new ArrayList<OnBoardMemory>();
		List<OnBoardMemory> onBoardMemorys_RAM = new ArrayList<OnBoardMemory>();
		List<CoreMemory> coreMemorys_ROM = new ArrayList<CoreMemory>();
		List<CoreMemory> coreMemorys_RAM = new ArrayList<CoreMemory>();
		for (int i = 0; i < selectedBoard.getOnBoardCpus().size(); i++) {
			OnBoardCpu onBoardCpu = selectedBoard.getOnBoardCpus().get(i);
			for(int j=0;j<onBoardCpu.getMemorys().size();j++) {			
				if(onBoardCpu.getMemorys().get(j).getType().equals("ROM")) {
					onBoardMemorys_ROM.add(onBoardCpu.getMemorys().get(j));
				}else if(onBoardCpu.getMemorys().get(j).getType().equals("RAM")) {
					onBoardMemorys_RAM.add(onBoardCpu.getMemorys().get(j));
				}
			}
		}
		for(int i=0;i<selectedCpu.getCores().size();i++) {
			Core core = selectedCpu.getCores().get(i);
			for (int j = 0; j < core.getMemorys().size(); j++) {
				if(core.getMemorys().get(j).getType().equals("FLASH")) {
					coreMemorys_ROM.add(core.getMemorys().get(j));
				}else if(core.getMemorys().get(j).getType().equals("RAM")) {
					coreMemorys_RAM.add(core.getMemorys().get(j));
				}				
			}
		}
		
		for(int i=0;i<coreMemorys_ROM.size();i++) {
			ldsDesc += "\nInnerFlash"+(i+1)+"Offset = "+"ORIGIN(InnerFlash"+(i+1)+");";
			ldsDesc += "\nInnerFlash"+(i+1)+"Range = "+"LENGTH(InnerFlash"+(i+1)+");";
		}
		for(int i=0;i<coreMemorys_RAM.size();i++) {
			ldsDesc += "\nInnerRAM"+(i+1)+"Start = "+"ORIGIN(RAM"+(i+1)+");";
			ldsDesc += "\nInnerRAM"+(i+1)+"Size = "+"LENGTH(RAM"+(i+1)+");";
		}
		for(int i=0;i<onBoardMemorys_ROM.size();i++) {
			ldsDesc += "\nExtRom"+(i+1)+"Start = "+"ORIGIN(extRom"+(i+1)+");";
			ldsDesc += "\nExtRom"+(i+1)+"Size = "+"LENGTH(extRom"+(i+1)+");";
		}
		for(int i=0;i<onBoardMemorys_RAM.size();i++) {
			ldsDesc += "\nExtRam"+(i+1)+"Start = "+"ORIGIN(extRam"+(i+1)+");";
			ldsDesc += "\nExtRam"+(i+1)+"Size = "+"LENGTH(extRam"+(i+1)+");";
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
		radioGl.verticalSpacing = 20;
		RADIOCpt.setLayout(radioGl);
		RADIOCpt.setLayoutData(new GridData(SWT.VERTICAL));
		String[] templateLabels = { DjyosMessages.Iboot_App_Project, DjyosMessages.Iboot_Only_Project, 
				DjyosMessages.App_Project,DjyosMessages.App_Only_Project };

		String[] templateDescs = {templateDesc0,templateDesc1,
				templateDesc2,templateDesc3};
		for (int i = 0; i < radioBtns.length; i++) {
			radioBtns[i] = new Button(RADIOCpt, SWT.RADIO | SWT.LEFT);
			radioBtns[i].setText(templateLabels[i]);
			int a = i;
			radioBtns[i].addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					projectTypeDesc.setText(templateDescs[a]);
					if(a==radioBtns.length-1) {
						fIbootSize.getTextControl(ibootComposite).setEnabled(false);
						fIbootSize.getTextControl(ibootComposite).setText("");
						boolean valid = validatePageBefore();
			    		setPageComplete(valid);
					}else {
						fIbootSize.getTextControl(ibootComposite).setEnabled(true);
					}
					
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
		content.setLayout(new GridLayout(1,true));
		content.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		ibootComposite = new Composite(content, SWT.NULL);
		GridLayout gd = new GridLayout(3,true);
		gd.marginLeft = 100;
		gd.marginHeight = 20;
		ibootComposite.setLayout(gd);
		ibootComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fIbootSize= new IntegerFieldEditor(CCorePreferenceConstants.MAX_INDEX_DB_CACHE_SIZE_MB, DjyosMessages.Ibootsize_Label, ibootComposite, 4);
		fIbootSize.setValidRange(1, 10000);
		BidiUtils.applyBidiProcessing(fIbootSize.getTextControl(ibootComposite), BidiUtils.BTD_DEFAULT);
		ControlFactory.createLabel(group1, "K");
		fIbootSize.getTextControl(ibootComposite).addListener(SWT.Modify, ibootSizeModifyListener);

	}

	@Override
	public boolean canFlipToNextPage() {
		// TODO Auto-generated method stub
		clickedNext = false;
		projectPath = locationArea.locationPathField.getText();
		return super.canFlipToNextPage();
	}

	@Override
	public IWizardPage getNextPage() {
		System.out.println("getNextPage DW");
		DjyosCommonProjectWizard nmWizard = (DjyosCommonProjectWizard)getWizard();
		if(! nmWizard.addedComptCfg) {
			OnBoardCpu onBoardCpu = null;
			List<OnBoardCpu> onBoardCpus = selectedBoard.getOnBoardCpus();
			for(int i=0;i<onBoardCpus.size();i++) {
				if(onBoardCpus.get(i).getCpuName().equals(selectedCpu.getCpuName())) {
					onBoardCpu = onBoardCpus.get(i);
					break;
				}
			}
			nmWizard.cpomtCfgPage = new ComponentConfigWizard("basicComponentCfgPage",onBoardCpu,selectedBoard,haveApp(),haveIboot());
			nmWizard.cpomtCfgPage.setTitle("Component Configuration");
			nmWizard.cpomtCfgPage.setDescription("工程裁剪与配置");
			nmWizard.addPage(nmWizard.cpomtCfgPage);
			nmWizard.addedComptCfg = true;
		} else {
			if (clickedNext) {
				nmWizard.importProject(projectPath);
				nmWizard.clickedMianNext = true;
			}
		}
		clickedNext = true;		
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

	public boolean haveApp() {
		int index = getTemplateIndex();
		if(index == 0 || index == 2 || index == 3) {
			return true;
		}
		return false;
	}
		
	public int getTemplateIndex() {
		int index = 0;
		for (int i = 0; i < radioBtns.length; i++) {
			if(radioBtns[i].getSelection()) {
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
				valid = validatePageBefore();
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
	
	@SuppressWarnings("restriction")
	protected boolean validatePageBefore() {
		IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();
		String projectFieldContents = getProjectNameFieldValue();
		
		String ibootSize = fIbootSize.getTextControl(ibootComposite).getText();
		if(getTemplateIndex() != 3) {
			if(ibootSize.equals("")) {
				return false;
			}
		}
		
		if (projectFieldContents.equals("")) { //$NON-NLS-1$
			setErrorMessage(null);
			setMessage("请填写工程名 !");
			return false;
		}
		
		String prjPathSelect = locationArea.locationPathField.getText();
		if(!prjPathSelect.endsWith(projectFieldContents)) {
			prjPathSelect = prjPathSelect+"\\"+projectFieldContents;
		}
		File prjFile = new File(prjPathSelect);
		if(prjFile.exists()) {
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
    
    private String Lds_Head_Promopt = "\n/*由于MEMORY命令不能使用符号，这些常量的定义，必须与MEMORY命令处一致 */ \n\n" + "MEMORY\n"+ "{";
    private static String templateDesc0 = "用于开发iboot和App的工程，App由iboot\n启动，" + "用于App和iboot由一个团队维护的情况";
    private static String templateDesc1 = "用于开发iboot的工程，用于App和iboot由不同" + "团队维护的情况";
    private static String templateDesc2 = "用于开发App的工程，App工程需要输入iboot的尺" + "寸 用于App和iboot由不同团队维护的情况";
    private static String templateDesc3 = "用于开发无需iboot，自启动运行的App工程";
    private static String promoteTitle = "提示";
    private static String promoteDesc_Int_Data = "请输入正整数";

}