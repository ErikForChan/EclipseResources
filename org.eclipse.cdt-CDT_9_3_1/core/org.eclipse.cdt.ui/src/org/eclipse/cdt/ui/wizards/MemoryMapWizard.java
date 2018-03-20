package org.eclipse.cdt.ui.wizards;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WorkingSetGroup;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea.IErrorMessageReporter;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.parsexml.Board;
import org.eclipse.cdt.ui.wizards.parsexml.Cpu;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;

import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;

import org.eclipse.cdt.internal.ui.newui.Messages;
import org.eclipse.cdt.internal.ui.wizards.ICDTCommonProjectWizard;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringDialogField;

public class MemoryMapWizard extends WizardPage implements IWizardItemsListListener {
	public static final String PAGE_ID = "org.eclipse.cdt.managedbuilder.ui.wizard.NewModelProjectWizardPage"; //$NON-NLS-1$

	private static final String EXTENSION_POINT_ID = "org.eclipse.cdt.ui.CDTWizard"; //$NON-NLS-1$
	private static final String ELEMENT_NAME = "wizard"; //$NON-NLS-1$
	private static final String CLASS_NAME = "class"; //$NON-NLS-1$
	public static final String DESC = "EntryDescriptor"; //$NON-NLS-1$
	
	public String ldsHead = "";
	public String ldsDesc = "";
	private Button[] romOnBox = new Button[5];
	private Button[] romOffBox = new Button[5];
	private Button[] ramOnBox = new Button[5];
	private Button[] ramOffBox = new Button[5];
	
	private Text[] romOnStartText = new Text[5];
	private Text[] romOnSizeText = new Text[5];
	private Text[] romOffStartText = new Text[5];
	private Text[] romOffSizeText = new Text[5];
	private Text[] ramOnStartText = new Text[5];
	private Text[] ramOnSizeText = new Text[5];
	private Text[] ramOffStartText = new Text[5];
	private Text[] ramOffSizeText = new Text[5];
	
	public Cpu cpu;
	public Board board;
	private IntegerFieldEditor fIbootSize;
	private Composite ibootComposite;
	
//	public boolean validatePage() {
//		int typeFilled = 0;
//		for(int i=0;i<romOnBox.length;i++) {
//			if(romOnBox[i].getSelection()) {
//				typeFilled++;
//				break;
//			}
//		}
//		for(int i=0;i<romOffBox.length;i++) {
//			if(romOffBox[i].getSelection()) {
//				typeFilled++;
//				break;
//			}
//		}
//		for(int i=0;i<ramOnBox.length;i++) {
//			if(ramOnBox[i].getSelection()) {
//				typeFilled++;
//				break;
//			}
//		}
//		for(int i=0;i<ramOffBox.length;i++) {
//			if(ramOffBox[i].getSelection()) {
//				typeFilled++;
//				break;
//			}
//		}
//		if(typeFilled < 4) {
//			return false;
//		}
//		return true;
//	}
//	
	/**
	 * Creates a new project creation wizard page.
	 *
	 * @param pageName the name of this page
	 */
	public MemoryMapWizard(String pageName) {
		super(pageName);
		setPageComplete(false);
	}
	
	public String getLdsHead() {
		ldsHead += "MEMORY\n"
				+ "{";
		for(int i=0;i<romOnBox.length;i++) {
			if(romOnBox[i].getSelection()) {
				ldsHead += "\n\t InnerFlash"+(i+1)+"(RX) : ORIGIN = "+romOnStartText[i].getText()
						+", LENGTH = "+romOnSizeText[i].getText();
			}
		}
		for(int i=0;i<romOffBox.length;i++) {
			if(romOffBox[i].getSelection()) {
				ldsHead += "\n\t extrom"+(i+1)+"(RX) : ORIGIN = "+romOffStartText[i].getText()
						+", LENGTH = "+romOffSizeText[i].getText();
			}
		}
		for(int i=0;i<ramOnBox.length;i++) {
			if(ramOnBox[i].getSelection()) {
				ldsHead += "\n\t RAM"+(i+1)+"(RXW) : ORIGIN = "+ramOnStartText[i].getText()
						+", LENGTH = "+ramOnSizeText[i].getText();
			}
		}
		for(int i=0;i<ramOffBox.length;i++) {
			if(ramOffBox[i].getSelection()) {
				ldsHead += "\n\t extram"+(i+1)+"(RXW) : ORIGIN = "+ramOffStartText[i].getText()
						+", LENGTH = "+ramOffSizeText[i].getText();
			}
		}
		ldsHead += "\n}"+"\n";
		return ldsHead;
	}
	
	public String getLdsDesc() {
		int ibootSize = Integer.parseInt(fIbootSize.getTextControl(ibootComposite).getText());
		ldsDesc += "\nIboot Size = "+ibootSize+"K;\n";
		for(int i=0;i<romOnBox.length;i++) {
			if(romOnBox[i].getSelection()) {
				ldsDesc += "\n InnerFlash"+(i+1)+"Offset = "+"ORIGIN(InnerFlash"+(i+1)+");";
				ldsDesc += "\n InnerFlash"+(i+1)+"Range = "+"LENGTH(InnerFlash"+(i+1)+");";
			}
		}
		for(int i=0;i<romOffBox.length;i++) {
			if(romOffBox[i].getSelection()) {
				ldsDesc += "\n ExtRom"+(i+1)+"Start = "+"ORIGIN(extRom"+(i+1)+");";
				ldsDesc += "\n ExtRom"+(i+1)+"Size = "+"LENGTH(extRom"+(i+1)+");";
			}
		}
		for(int i=0;i<ramOnBox.length;i++) {
			if(ramOnBox[i].getSelection()) {
				ldsDesc += "\n InnerRAM"+(i+1)+"Start = "+"ORIGIN(RAM"+(i+1)+");";
				ldsDesc += "\n InnerRAM"+(i+1)+"Size = "+"LENGTH(RAM"+(i+1)+");";
			}
		}
		for(int i=0;i<ramOffBox.length;i++) {
			if(ramOffBox[i].getSelection()) {
				ldsDesc += "\n ExtRam"+(i+1)+"Start = "+"ORIGIN(extRam"+(i+1)+");";
				ldsDesc += "\n ExtRam"+(i+1)+"Size = "+"LENGTH(extRam"+(i+1)+");";
			}
		}
		return ldsDesc;
	}

	@SuppressWarnings("restriction")
	@Override
	public void createControl(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		initializeDialogUnits(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);
		
		Group RomGroup = ControlFactory.createGroup(composite, "ROM", 1);
		RomGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_FILL | GridData.FILL_HORIZONTAL));
		RomGroup.setLayout(new GridLayout(1,true));
		createROMContent(RomGroup);
		
		Group RamGroup = ControlFactory.createGroup(composite, "RAM", 1);
		RamGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_FILL | GridData.FILL_HORIZONTAL));
		RamGroup.setLayout(new GridLayout(1,true));
		createRAMContent(RamGroup);
		
		Group IbootSizeGroup = ControlFactory.createGroup(composite, "Others", 1);
		IbootSizeGroup.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.FILL_HORIZONTAL));
		IbootSizeGroup.setLayout(new GridLayout(1,true));
		
		Composite content = new Composite(IbootSizeGroup, SWT.NULL);
		GridLayout gd = new GridLayout(2,true);
		content.setLayout(gd);
		content.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		ibootComposite = new Composite(content, SWT.NULL);
		gd = new GridLayout(2,true);
		ibootComposite.setLayout(gd);
		ibootComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fIbootSize= new IntegerFieldEditor(CCorePreferenceConstants.MAX_INDEX_DB_CACHE_SIZE_MB, "Iboot Size: ", ibootComposite, 4);
		fIbootSize.setValidRange(1, 10000);
		fIbootSize.getTextControl(ibootComposite).addListener(SWT.Modify, ibootSizeModifyListener);
		BidiUtils.applyBidiProcessing(fIbootSize.getTextControl(ibootComposite), BidiUtils.BTD_DEFAULT);
		ControlFactory.createLabel(content, "K");
//		TabFolder folder= new TabFolder(composite, SWT.NONE);
//		folder.setLayout(new TabFolderLayout());
//		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
//		
//		TabItem item= new TabItem(folder, SWT.NONE);
//		item.setText("ROM"); //$NON-NLS-1$
//		item.setControl(createROMTabContent(folder));
//
//		item= new TabItem(folder, SWT.NONE);
//		item.setText("RAM"); //$NON-NLS-1$
//		item.setControl(createRAMTabContent(folder));
//		System.out.println("createControl");
		// Show description on opening
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
		Dialog.applyDialogFont(composite);
		
		DjyosCommonProjectWizard nmWizard = (DjyosCommonProjectWizard)getWizard();
		cpu = nmWizard.getCpu();
		board = nmWizard.getBoard();
		if(cpu==null) {
			System.out.println("cpu==null");
		}
		if(cpu!=null) {	
			romOnBox[0].setSelection(true);
			romOnBox[0].setEnabled(false);
			romOnStartText[0].setText(cpu.getFlashStart());
			romOnStartText[0].setEnabled(false);
			romOnSizeText[0].setText(cpu.getFlashSize());
			romOnSizeText[0].setEnabled(false);
			
			String[] ramStarts = cpu.getRamStart().split(",");
			String[] ramSizes = cpu.getRamSize().split(",");
			for (int i = 0; i < ramStarts.length; i++) {
				ramOnBox[i].setSelection(true);
				ramOnBox[i].setEnabled(false);
				ramOnStartText[i].setText(ramStarts[i]);
				ramOnStartText[i].setEnabled(false);
				ramOnSizeText[i].setText(ramSizes[i]);
				ramOnSizeText[i].setEnabled(false);
			}	
		}
		if(! nmWizard.isToCreat) {
			if (! board.getExtromStart().equals("null")) {
				romOffBox[0].setSelection(true);
				romOffBox[0].setEnabled(false);
				romOffStartText[0].setText(board.getExtromStart());
				romOffStartText[0].setEnabled(false);
				romOffSizeText[0].setText(board.getExtromSize());
				romOffSizeText[0].setEnabled(false);
			}
			if (! board.getExtramStart().equals("null")) {
				ramOffBox[0].setSelection(true);
				ramOffBox[0].setEnabled(false);
				ramOffStartText[0].setText(board.getExtramStart());
				ramOffStartText[0].setEnabled(false);
				ramOffSizeText[0].setText(board.getExtramSize());
				ramOffSizeText[0].setEnabled(false);
			}
		}
//		createDynamicGroup((Composite)getControl());

	}

	private Control createROMContent(Group group) {
		// TODO Auto-generated method stub
		Button fFoldingCheckbox1_on,fFoldingCheckbox2_on;
		Button fFoldingCheckbox1_off,fFoldingCheckbox2_off;
		Label startLabel,sizeLabel;
		
		Composite composite= new Composite(group, SWT.NULL);
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_FILL | GridData.FILL_HORIZONTAL);
		GridData gdBox= new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_FILL);
		GridData gdLabel= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_HORIZONTAL);
		GridData gdText= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_HORIZONTAL);
		composite.setLayoutData(gd);
		GridLayout gd1 = new GridLayout(2,true);
		composite.setLayout(gd1);
		
		//group_onChip
		Group group_onChip = ControlFactory.createGroup(composite, "on-chip", 1);
		group_onChip.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_FILL | GridData.FILL_HORIZONTAL));
		group_onChip.setLayout(new GridLayout(3,true));

		fFoldingCheckbox1_on= new Button(group_onChip, SWT.CHECK);
		fFoldingCheckbox1_on.setVisible(false);
		startLabel = new Label(group_onChip, SWT.CENTER | SWT.PUSH);
		startLabel.setText("start");
		startLabel.setLayoutData(gdLabel);
		sizeLabel = new Label(group_onChip, SWT.CENTER | SWT.PUSH);
		sizeLabel.setText("size");
		sizeLabel.setLayoutData(gdLabel);
		
		for(int i=0;i<5;i++) {
			romOnBox[i] = new Button(group_onChip, SWT.CHECK);
			romOnBox[i].setLayoutData(gdBox);
			romOnStartText[i] = new Text(group_onChip, SWT.BORDER);
			romOnStartText[i].setLayoutData(gdText);
			romOnSizeText[i] = new Text(group_onChip, SWT.BORDER);
			romOnSizeText[i].setLayoutData(gdText);
//			romOnBox[i].addSelectionListener(new SelectionListener() {
//				
//				@Override
//				public void widgetSelected(SelectionEvent e) {
//					// TODO Auto-generated method stub
//					setPageComplete(validatePage());
//				}
//				
//				@Override
//				public void widgetDefaultSelected(SelectionEvent e) {
//					// TODO Auto-generated method stub
//					
//				}
//			} );
		}
			
		//group_offChip
		Group group_offChip = ControlFactory.createGroup(composite, "off-chip", 1);
		group_offChip.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_FILL | GridData.FILL_HORIZONTAL));
		group_offChip.setLayout(new GridLayout(3,true));
		
		fFoldingCheckbox2_off= new Button(group_offChip, SWT.CHECK);
		fFoldingCheckbox2_off.setVisible(false);
		startLabel = new Label(group_offChip, SWT.CENTER | SWT.PUSH);
		startLabel.setText("start");
		startLabel.setLayoutData(gdLabel);
		sizeLabel = new Label(group_offChip, SWT.CENTER | SWT.PUSH);
		sizeLabel.setText("size");
		sizeLabel.setLayoutData(gdLabel);
		
		for(int i=0;i<5;i++) {
			romOffBox[i] = new Button(group_offChip, SWT.CHECK);
			romOffBox[i].setLayoutData(gdBox);
			romOffStartText[i] = new Text(group_offChip, SWT.BORDER);
			romOffStartText[i].setLayoutData(gdText);
			romOffSizeText[i] = new Text(group_offChip, SWT.BORDER);
			romOffSizeText[i].setLayoutData(gdText);
		}
		
		return composite;
	}

	private Control createRAMContent(Group group) {
		// TODO Auto-generated method stub
		Button sFoldingCheckbox1_on,sFoldingCheckbox2_on;
		Button sFoldingCheckbox1_off,sFoldingCheckbox2_off;
		Label startLabel,sizeLabel;
		Composite composite= new Composite(group, SWT.NULL);
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_FILL | GridData.FILL_HORIZONTAL);
		GridData gdBox= new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_FILL);
		GridData gdLabel= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_HORIZONTAL);
		GridData gdText= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_HORIZONTAL);
		composite.setLayoutData(gd);
		GridLayout gd1 = new GridLayout(2,true);
		composite.setLayout(gd1);
		
		//group_onChip
		Group group_onChip = ControlFactory.createGroup(composite, "on-chip", 1);
		group_onChip.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_FILL | GridData.FILL_HORIZONTAL));
		group_onChip.setLayout(new GridLayout(3,true));

		sFoldingCheckbox1_on= new Button(group_onChip, SWT.CHECK);
		sFoldingCheckbox1_on.setVisible(false);
		startLabel = new Label(group_onChip, SWT.CENTER | SWT.PUSH);
		startLabel.setText("start");
		startLabel.setLayoutData(gdLabel);
		sizeLabel = new Label(group_onChip, SWT.CENTER | SWT.PUSH);
		sizeLabel.setText("size");
		sizeLabel.setLayoutData(gdLabel);
		
		for(int i=0;i<5;i++) {
			ramOnBox[i] = new Button(group_onChip, SWT.CHECK);
			ramOnBox[i].setLayoutData(gdBox);
			ramOnStartText[i] = new Text(group_onChip, SWT.BORDER);
			ramOnStartText[i].setLayoutData(gdText);
			ramOnSizeText[i] = new Text(group_onChip, SWT.BORDER);
			ramOnSizeText[i].setLayoutData(gdText);
		}
			
		//group_offChip
		Group group_offChip = ControlFactory.createGroup(composite, "off-chip", 1);
		group_offChip.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_FILL | GridData.FILL_HORIZONTAL));
		group_offChip.setLayout(new GridLayout(3,true));
		
		sFoldingCheckbox2_off= new Button(group_offChip, SWT.CHECK);
		sFoldingCheckbox2_off.setVisible(false);
		startLabel = new Label(group_offChip, SWT.CENTER | SWT.PUSH);
		startLabel.setText("start");
		startLabel.setLayoutData(gdLabel);
		sizeLabel = new Label(group_offChip, SWT.CENTER | SWT.PUSH);
		sizeLabel.setText("size");
		sizeLabel.setLayoutData(gdLabel);
		
		for(int i=0;i<5;i++) {
			ramOffBox[i] = new Button(group_offChip, SWT.CHECK);
			ramOffBox[i].setLayoutData(gdBox);
			ramOffStartText[i] = new Text(group_offChip, SWT.BORDER);
			ramOffStartText[i].setLayoutData(gdText);
			ramOffSizeText[i] = new Text(group_offChip, SWT.BORDER);
			ramOffSizeText[i].setLayoutData(gdText);
		}
		
		return composite;
	}
	
	boolean added = false;
	
	@Override
	public IWizardPage getNextPage() {
		// TODO Auto-generated method stub
		System.out.println("getNextPage MM");
		String modulePageTip = "本版本为测试版，暂无添加依赖关系，以后版本将会陆续添加.";
		DjyosCommonProjectWizard nmWizard = (DjyosCommonProjectWizard)getWizard();
		if(! nmWizard.addedModule) {		
			nmWizard.modulePage = new ModuleConfigurationWizard("basicModuleCfgPage");
			nmWizard.modulePage.setTitle("Module Configuration");
			nmWizard.modulePage.setDescription(modulePageTip);
			nmWizard.addPage(nmWizard.modulePage);
			nmWizard.modulePage.setPageComplete(false);
			nmWizard.addedModule = true;
			if(nmWizard.isToCreat) {
				nmWizard.handleBoard();
			}
			nmWizard.handleCProject();
		}		
		
		return super.getNextPage();
	}

	private  Listener ibootSizeModifyListener = e -> {
		//setLocationForSelection();
		boolean valid = validatePage();
		setPageComplete(valid);
	};
	
	private boolean validatePage() {
		// TODO Auto-generated method stub
		String ibootSizeContent = fIbootSize.getTextControl(ibootComposite).getText();
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");  
		if (ibootSizeContent.equals("")) { //$NON-NLS-1$
			setErrorMessage(null);
			setMessage("iboot Size must be specified !");
			return false;
		}else if(! pattern.matcher(ibootSizeContent).matches()) {
			setErrorMessage(null);
			setMessage("iboot Size must be integer !");
			return false;
		}
		setMessage("Define flash and RAM sizes");
		return true;
	}
	
	@Override
	public void toolChainListChanged(int count) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isCurrent() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<EntryDescriptor> filterItems(List<EntryDescriptor> items) {
		// TODO Auto-generated method stub
		return null;
	}
}