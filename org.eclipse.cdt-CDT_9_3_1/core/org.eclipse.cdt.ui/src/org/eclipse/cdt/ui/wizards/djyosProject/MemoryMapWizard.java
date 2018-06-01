package org.eclipse.cdt.ui.wizards.djyosProject;

import java.io.File;
import java.io.IOException;
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
import org.eclipse.cdt.ui.wizards.board.Board;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.OnBoardCpu;
import org.eclipse.cdt.ui.wizards.cpu.Cpu;
import org.eclipse.cdt.ui.wizards.cpu.core.Core;
import org.eclipse.cdt.ui.wizards.cpu.core.memory.CoreMemory;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;

import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;

import org.eclipse.cdt.internal.ui.djyproperties.MemoryMap;
import org.eclipse.cdt.internal.ui.djyproperties.parsexml.CreateMemoryMapXml;
import org.eclipse.cdt.internal.ui.newui.Messages;
import org.eclipse.cdt.internal.ui.wizards.ICDTCommonProjectWizard;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringDialogField;

public class MemoryMapWizard extends WizardPage {
	public static final String PAGE_ID = "org.eclipse.cdt.managedbuilder.ui.wizard.NewModelProjectWizardPage"; //$NON-NLS-1$

	private static final String EXTENSION_POINT_ID = "org.eclipse.cdt.ui.CDTWizard"; //$NON-NLS-1$
	private static final String ELEMENT_NAME = "wizard"; //$NON-NLS-1$
	private static final String CLASS_NAME = "class"; //$NON-NLS-1$
	public static final String DESC = "EntryDescriptor"; //$NON-NLS-1$
	
	public String ldsHead = "";
	public String ldsDesc = "";
	private Button[] romOnBox = new Button[4];
	private Button[] romOffBox = new Button[4];
	private Button[] ramOnBox = new Button[4];
	private Button[] ramOffBox = new Button[4];
	
	private Text[] romOnStartText = new Text[4];
	private Text[] romOnSizeText = new Text[4];
	private Text[] romOffStartText = new Text[4];
	private Text[] romOffSizeText = new Text[4];
	private Text[] ramOnStartText = new Text[4];
	private Text[] ramOnSizeText = new Text[4];
	private Text[] ramOffStartText = new Text[4];
	private Text[] ramOffSizeText = new Text[4];
	
	boolean clickedNext = false;
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
	
	public void createMemoryMap(String filePath) {
		MemoryMap mMap;
		String flashStart = "",flashSize = "",ramStart= "",ramSize= "",
				extromStart = "",extromSize = "",extramStart = "",extramSize = "",ibootSize = "";
		for(int i=0;i<romOnBox.length;i++) {
			if(romOnBox[i].getSelection()) {
				String startBuffer = (i==0?romOnStartText[i].getText():","+romOnStartText[i].getText());
				String sizeBuffer = (i==0?romOnSizeText[i].getText():","+romOnSizeText[i].getText());
				flashStart+=startBuffer;
				flashSize+=sizeBuffer;
			}
			if(romOffBox[i].getSelection()) {
				String startBuffer = (i==0?romOffStartText[i].getText():","+romOffStartText[i].getText());
				String sizeBuffer = (i==0?romOffSizeText[i].getText():","+romOffSizeText[i].getText());
				ramStart+=startBuffer;
				ramSize+=sizeBuffer;		
			}
			if(ramOnBox[i].getSelection()) {
				String startBuffer = (i==0?ramOnStartText[i].getText():","+ramOnStartText[i].getText());
				String sizeBuffer = (i==0?ramOnSizeText[i].getText():","+ramOnSizeText[i].getText());
				extromStart+=startBuffer;
				extromSize+=sizeBuffer;
			}
			if(ramOffBox[i].getSelection()) {
				String startBuffer = (i==0?ramOffStartText[i].getText():","+ramOffStartText[i].getText());
				String sizeBuffer = (i==0?ramOffSizeText[i].getText():","+ramOffSizeText[i].getText());
				extramStart+=startBuffer;
				extramSize+=sizeBuffer;	
			}
		}
		ibootSize = fIbootSize.getTextControl(ibootComposite).getText()+"K";
		mMap = new MemoryMap(flashStart==""?"null":flashStart,flashSize==""?"null":flashSize,ramStart==""?"null":ramStart,
				ramSize==""?"null":ramSize,extromStart==""?"null":extromStart,extromSize==""?"null":extromSize,
				extramStart==""?"null":extramStart,extramSize==""?"null":extramSize,ibootSize);
		
		File file = new File(filePath);
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		CreateMemoryMapXml cmmx = new CreateMemoryMapXml();
		cmmx.creatMemoryMap(mMap, filePath);
	}
	
	public String getLdsHead() {
		ldsHead += "\n"
				+ "/*由于MEMORY命令不能使用符号，这些常量的定义，必须与MEMORY命令处一致 */ \n\n"
				+ "MEMORY\n"
				+ "{";
		for(int i=0;i<romOnBox.length;i++) {
			if(romOnBox[i].getSelection()) {
				ldsHead += "\n\tInnerFlash"+(i+1)+"(RX) : ORIGIN = "+romOnStartText[i].getText()
						+", LENGTH = "+romOnSizeText[i].getText();
			}
		}
		for(int i=0;i<romOffBox.length;i++) {
			if(romOffBox[i].getSelection()) {
				ldsHead += "\n\textrom"+(i+1)+"(RX) : ORIGIN = "+romOffStartText[i].getText()
						+", LENGTH = "+romOffSizeText[i].getText();
			}
		}
		for(int i=0;i<ramOnBox.length;i++) {
			if(ramOnBox[i].getSelection()) {
				ldsHead += "\n\tRAM"+(i+1)+"(RXW) : ORIGIN = "+ramOnStartText[i].getText()
						+", LENGTH = "+ramOnSizeText[i].getText();
			}
		}
		for(int i=0;i<ramOffBox.length;i++) {
			if(ramOffBox[i].getSelection()) {
				ldsHead += "\n\textram"+(i+1)+"(RXW) : ORIGIN = "+ramOffStartText[i].getText()
						+", LENGTH = "+ramOffSizeText[i].getText();
			}
		}
		ldsHead += "\n}"+"\n";
		return ldsHead;
	}
	
	public String getLdsDesc() {
		int ibootSize = Integer.parseInt(fIbootSize.getTextControl(ibootComposite).getText());
		ldsDesc += "\nIbootSize = "+ibootSize+"K;\n";
		for(int i=0;i<romOnBox.length;i++) {
			if(romOnBox[i].getSelection()) {
				ldsDesc += "\nInnerFlash"+(i+1)+"Offset = "+"ORIGIN(InnerFlash"+(i+1)+");";
				ldsDesc += "\nInnerFlash"+(i+1)+"Range = "+"LENGTH(InnerFlash"+(i+1)+");";
			}
		}
		for(int i=0;i<romOffBox.length;i++) {
			if(romOffBox[i].getSelection()) {
				ldsDesc += "\nExtRom"+(i+1)+"Start = "+"ORIGIN(extRom"+(i+1)+");";
				ldsDesc += "\nExtRom"+(i+1)+"Size = "+"LENGTH(extRom"+(i+1)+");";
			}
		}
		for(int i=0;i<ramOnBox.length;i++) {
			if(ramOnBox[i].getSelection()) {
				ldsDesc += "\nInnerRAM"+(i+1)+"Start = "+"ORIGIN(RAM"+(i+1)+");";
				ldsDesc += "\nInnerRAM"+(i+1)+"Size = "+"LENGTH(RAM"+(i+1)+");";
			}
		}
		for(int i=0;i<ramOffBox.length;i++) {
			if(ramOffBox[i].getSelection()) {
				ldsDesc += "\nExtRam"+(i+1)+"Start = "+"ORIGIN(extRam"+(i+1)+");";
				ldsDesc += "\nExtRam"+(i+1)+"Size = "+"LENGTH(extRam"+(i+1)+");";
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
		if(cpu!=null) {	
			
			int romCount = 0,ramCount = 0;
			for(int i=0;i<cpu.getCores().size();i++) {
				Core core = cpu.getCores().get(i);
				System.out.println("core!=null   " + core.getMemorys().size());
				for(int j=0;j<core.getMemorys().size();j++) {
					CoreMemory memory = core.getMemorys().get(j);
					if(memory.getType().equals("ROM")) {
						romOnBox[romCount].setSelection(true);
						romOnBox[romCount].setEnabled(false);
						romOnStartText[romCount].setText(memory.getStartAddr());
						romOnStartText[romCount].setEnabled(false);
						romOnSizeText[romCount].setText(String.valueOf(memory.getSize()));
						romOnSizeText[romCount].setEnabled(false);
						romCount++;
					}else if(memory.getType().equals("RAM")) {
						ramOnBox[ramCount].setSelection(true);
						ramOnBox[ramCount].setEnabled(false);
						ramOnStartText[ramCount].setText(memory.getStartAddr());
						ramOnStartText[ramCount].setEnabled(false);
						ramOnSizeText[ramCount].setText(String.valueOf(memory.getSize()));
						ramOnSizeText[ramCount].setEnabled(false);
						ramCount++;
					}
				}
								
			}
	
		}
		
		if(board != null){

			int romCount = 0,ramCount = 0;
			for(int i=0;i<board.getOnBoardCpus().size();i++) {
				OnBoardCpu onBoardCpu = new OnBoardCpu();
				System.out.println("onBoardCpu!=null   " + onBoardCpu.getMemorys().size());
				for(int j=0;j<onBoardCpu.getMemorys().size();j++) {
					if(onBoardCpu.getMemorys().get(j).getType().equals("ROM")) {
						romOffBox[romCount].setSelection(true);
						romOffBox[romCount].setEnabled(false);
						romOffStartText[romCount].setText(onBoardCpu.getMemorys().get(j).getStartAddr());
						romOffStartText[romCount].setEnabled(false);
						romOffSizeText[romCount].setText(String.valueOf(onBoardCpu.getMemorys().get(j).getSize()));
						romOffSizeText[romCount].setEnabled(false);
						romCount++;
					}else if(onBoardCpu.getMemorys().get(j).getType().equals("RAM")) {
						ramOffBox[ramCount].setSelection(true);
						ramOffBox[ramCount].setEnabled(false);
						ramOffStartText[ramCount].setText(onBoardCpu.getMemorys().get(j).getStartAddr());
						ramOffStartText[ramCount].setEnabled(false);
						ramOffSizeText[ramCount].setText(String.valueOf(onBoardCpu.getMemorys().get(j).getSize()));
						ramOffSizeText[ramCount].setEnabled(false);
						ramCount++;
					}
					
				}
			}
		}
//		if(! nmWizard.isToCreat) {
//			if (! board.getExtromStart().equals("null")) {
//				romOffBox[0].setSelection(true);
//				romOffBox[0].setEnabled(false);
//				romOffStartText[0].setText(board.getExtromStart());
//				romOffStartText[0].setEnabled(false);
//				romOffSizeText[0].setText(board.getExtromSize());
//				romOffSizeText[0].setEnabled(false);
//			}
//			if (! board.getExtramStart().equals("null")) {
//				ramOffBox[0].setSelection(true);
//				ramOffBox[0].setEnabled(false);
//				ramOffStartText[0].setText(board.getExtramStart());
//				ramOffStartText[0].setEnabled(false);
//				ramOffSizeText[0].setText(board.getExtramSize());
//				ramOffSizeText[0].setEnabled(false);
//			}
//		}
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
		
		for(int i=0;i<4;i++) {
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
		
		for(int i=0;i<4;i++) {
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
		
		for(int i=0;i<4;i++) {
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
		
		for(int i=0;i<4;i++) {
			ramOffBox[i] = new Button(group_offChip, SWT.CHECK);
			ramOffBox[i].setLayoutData(gdBox);
			ramOffStartText[i] = new Text(group_offChip, SWT.BORDER);
			ramOffStartText[i].setLayoutData(gdText);
			ramOffSizeText[i] = new Text(group_offChip, SWT.BORDER);
			ramOffSizeText[i].setLayoutData(gdText);
		}
		
		return composite;
	}	
	
	@Override
	public boolean canFlipToNextPage() {
		// TODO Auto-generated method stub
		return super.canFlipToNextPage();
	}
	
	

	@Override
	public IWizardPage getNextPage() {
		// TODO Auto-generated method stub
		System.out.println("getNextPage MM");
		String modulePageTip = "本版本为测试版，暂无添加依赖关系，以后版本将会陆续添加.";
		DjyosCommonProjectWizard nmWizard = (DjyosCommonProjectWizard)getWizard();
		if(! nmWizard.addedInit) {
			nmWizard.initPage = new InitDjyosProjectWizard("basicModuleCfgPage",null);
			nmWizard.initPage.setTitle("Init Project");
			nmWizard.initPage.setDescription("Init the project you are creating");
			nmWizard.addPage(nmWizard.initPage);
			nmWizard.addedInit = true;
//			nmWizard.handleCProject();
		}else{
//			nmWizard.initPage.moduleCompleted = true;
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
	
}