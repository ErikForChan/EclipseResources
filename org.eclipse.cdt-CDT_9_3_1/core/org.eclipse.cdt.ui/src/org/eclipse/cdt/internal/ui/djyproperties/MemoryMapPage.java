package org.eclipse.cdt.internal.ui.djyproperties;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.w3c.dom.Document;

import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;

import org.eclipse.cdt.internal.ui.djyproperties.parsexml.CreateMemoryMapXml;
import org.eclipse.cdt.internal.ui.djyproperties.parsexml.ReadMemoryMapByDom;
import org.eclipse.cdt.internal.ui.preferences.PreferencesMessages;

public class MemoryMapPage extends PropertyPage{

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
	
	private Composite ibootComposite;
	private IntegerFieldEditor fIbootSize;
	
	public MemoryMapPage() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	private IProject getProject(){
		Object	element	= getElement();
		IProject project	= null;
		
		if (element instanceof IProject) {
			project = (IProject) element;
		} else if (element instanceof IAdaptable) {
			project= ((IAdaptable)element).getAdapter(IProject.class);
		}
		return project;
	}
	
	public void fillContents(){
		IProject project = getProject();
		setTitle("Memory Map ("+project.getName()+")");
		String filePath = project.getLocation().toString()+"/data/MemoryMap.xml";
		ReadMemoryMapByDom rmmb = new ReadMemoryMapByDom();
		MemoryMap mMap = rmmb.getMemoryMap(filePath);
		String[] flashStarts = mMap.getFlashStart().split(",");
		String[] flashSizes = mMap.getFlashSize().split(",");
		String[] ramStarts = mMap.getRamStart().split(",");
		String[] ramSizes = mMap.getRamSize().split(",");
		String[] extromStarts = mMap.getExtromStart().split(",");
		String[] extromSizes = mMap.getExtromSize().split(",");
		String[] extramStarts = mMap.getExtramStart().split(",");
		String[] extramSizes = mMap.getExtramSize().split(",");
		String ibootSize = mMap.getIbootSize();
		for (int i = 0; i < flashStarts.length; i++) {
			romOnBox[i].setSelection(true);
			romOnStartText[i].setText(flashStarts[i]);
			romOnSizeText[i].setText(flashSizes[i]);
		}	
		for (int i = 0; i < ramStarts.length; i++) {
			romOffBox[i].setSelection(true);
			romOffStartText[i].setText(ramStarts[i]);
			romOffSizeText[i].setText(ramSizes[i]);
		}	
		for (int i = 0; i < extromStarts.length; i++) {
			if(! extromStarts[i].equals("null")) {
				ramOnBox[i].setSelection(true);
				ramOnStartText[i].setText(extromStarts[i]);
				ramOnSizeText[i].setText(extromSizes[i]);
			}
			
		}	
		for (int i = 0; i < extramStarts.length; i++) {
			ramOffBox[i].setSelection(true);
			ramOffStartText[i].setText(extramStarts[i]);
			ramOffSizeText[i].setText(extramSizes[i]);
		}
		fIbootSize.getTextControl(ibootComposite).setText(ibootSize.substring(0, ibootSize.length()-1));
	}

	@Override
	protected Control createContents(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
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
//		fIbootSize.getTextControl(ibootComposite).addListener(SWT.Modify, ibootSizeModifyListener);
		BidiUtils.applyBidiProcessing(fIbootSize.getTextControl(ibootComposite), BidiUtils.BTD_DEFAULT);
		ControlFactory.createLabel(content, "K");
		fillContents();
		return composite;
	
	}

	@Override
	protected void performApply() {
		// TODO Auto-generated method stub
		super.performApply();
	}

	@Override
	protected void performDefaults() {
		// TODO Auto-generated method stub
		fillContents();
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		// TODO Auto-generated method stub
		updateMemoryMap();
		return super.performOk();
	}
	
	private void updateMemoryMap() {
		MemoryMap mMap;
		IProject project = getProject();
		String filePath = project.getLocation().toString()+"/src/data/MemoryMap.xml";
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
		if(file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		CreateMemoryMapXml cmmx = new CreateMemoryMapXml();
		cmmx.creatMemoryMap(mMap, filePath);
		
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
	
}
