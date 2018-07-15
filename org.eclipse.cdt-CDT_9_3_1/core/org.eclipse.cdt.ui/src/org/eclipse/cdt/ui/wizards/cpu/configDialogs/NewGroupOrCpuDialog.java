package org.eclipse.cdt.ui.wizards.cpu.configDialogs;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.eclipse.cdt.ui.wizards.cpu.Cpu;
import org.eclipse.cdt.ui.wizards.cpu.ReadCpuXml;
import org.eclipse.cdt.ui.wizards.cpu.core.Core;
import org.eclipse.cdt.ui.wizards.cpu.core.memory.CoreMemory;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;

import org.eclipse.cdt.internal.ui.CPluginImages;

public class NewGroupOrCpuDialog extends StatusDialog{

	private Tree cpuGroupTree,memoryTree;
	private Composite configContent;
	private Combo memoryTypeCombo;
	private Text addrField,sizeField;
	private Group contentGroup;
	private ScrolledComposite scrolledComposite;
	private Cpu newCpu = new Cpu(),parentCpu = new Cpu(),curCpu = new Cpu(),revisingCpu = new Cpu();
	private Core newCore = new Core();
	private Text groupNameField;
	private String curPath = null,groupName,cpuName,eclipsePath = getEclipsePath(),cpuTag = null,newConfig = null;
	private boolean haveCore = false;
	private List<String> configsList = new ArrayList<String>(),configsOn = new ArrayList<String>(),attributes = new ArrayList<String>(),firewareLibs = new ArrayList<String>();
	private Label memorySizeLabel,memoryAddrLabel,memoryTypeLabel;
	private String tempName = null;
	/*
	 * ��ȡ��ǰEclipse��·��
	 */
	public String getEclipsePath() {
		String fullPath = Platform.getInstallLocation().getURL().toString();
		String eclipsePath = fullPath.substring(6,(fullPath.substring(0,fullPath.length()-1)).lastIndexOf("/")+1);
		return eclipsePath;
	}
	
	private void setMemoryCpt(boolean isEnable){	
		memoryTypeLabel.setEnabled(isEnable);
		memoryAddrLabel.setEnabled(isEnable);
		memorySizeLabel.setEnabled(isEnable);
		memoryTypeCombo.setEnabled(isEnable);
		sizeField.setEnabled(isEnable);
		addrField.setEnabled(isEnable);
	}
	
	public String getGroupName() {
		return groupName;
	}
	
	private static class MyFileter implements FilenameFilter {

        @Override
        public boolean accept(File file, String filename) {
            if (filename != null && !filename.toLowerCase().contains(".")) {
                return true;
            } else {
                return false;
            }
        }

    }
		
	public NewGroupOrCpuDialog(Shell parent,List<String> configs,Cpu cpu,String curFilePath,String tag) {
		super(parent);
		attributes = configs;
		cpuTag = tag;
		if(cpu.getCoreNum()!=0) {
			parentCpu.setCoreNum(cpu.getCoreNum());
			parentCpu.setCores(cpu.getCores());
			curCpu = parentCpu;
			haveCore = true;
			
			newCpu = new Cpu();
			newCpu.setCoreNum(cpu.getCoreNum());
			for(int i=0;i<cpu.getCoreNum();i++) {
				newCpu.getCores().add(new Core());
			}
		}else {
//			Core onlyCore = 
//			if() {
//				
//			}
		}
		curPath = curFilePath;
	  
		if(tag.equals("group")) {
			setTitle("�½�����");
		}else if(tag.equals("cpu")) {
			setTitle("�½�Cpu");
		}else if(tag.startsWith("revise")) {
			if(tag.endsWith("group")) {
				setTitle("�޸���Ŀ¼����");
			}else if(tag.endsWith("cpu")) {
				setTitle("�޸�Cpu����");
			}
			ReadCpuXml rcx = new ReadCpuXml();
			File curFile = new File(curPath);
			File configFile = getXmlFile(curFile);
			try {
				revisingCpu = rcx.unitCpu(revisingCpu, configFile);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tempName = curPath.substring(curPath.lastIndexOf("\\") + 1, curPath.length());
		}
		
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX );
	}

	private File getXmlFile(File parentFile) {
		File file =null;
		File[] files = parentFile.listFiles();
		for(int i=0;i<files.length;i++){
			if(files[i].getName().endsWith(".xml")) {
				file = files[i];
			}
		}
		return file;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(parent,SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite groupNameCpt = new Composite(composite,SWT.NONE);
		GridLayout groupNameLayout = new GridLayout();
		groupNameLayout.numColumns = 2;
		groupNameLayout.marginHeight=20;
		groupNameCpt.setLayout(groupNameLayout);
		groupNameCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label nameLabel = new Label(groupNameCpt,SWT.NONE);
		groupNameField = new Text(groupNameCpt,SWT.BORDER);
		groupNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		if(cpuTag.equals("group")) {
			nameLabel.setText("��Ŀ¼����: ");
		}else if(cpuTag.equals("cpu")) {
			nameLabel.setText("Cpu����: ");
		}else if(cpuTag.startsWith("revise")) {
			if(cpuTag.endsWith("cpu")) {
				nameLabel.setText("Cpu����: ");
			}else if(cpuTag.endsWith("group")) {
				nameLabel.setText("��Ŀ¼����: ");
			}
			groupNameField.setText(tempName);
		}

		Composite cpuGroupCpt = new Composite(composite, SWT.NULL);
		GridLayout layoutAttributes = new GridLayout();
		layoutAttributes.numColumns = 4;
		layoutAttributes.marginHeight = 5;
		cpuGroupCpt.setLayout(layoutAttributes);
		cpuGroupCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite cpuGroupListCpt = new Composite(cpuGroupCpt, SWT.NULL);
		cpuGroupTree = new Tree(cpuGroupListCpt, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		cpuGroupTree.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		cpuGroupTree.setHeaderVisible(true);
		cpuGroupTree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeItem[] items = cpuGroupTree.getSelection();
				if (items.length > 0) {

				}
			}
		});
		final TreeColumn columnGroupList = new TreeColumn(cpuGroupTree, SWT.NONE);
		columnGroupList.setText("Cpu������");
		columnGroupList.setWidth(90);
		columnGroupList.setResizable(false);
		columnGroupList.setToolTipText("Cpu Attributes");
		cpuGroupTree.setSize(120, 220);
		List<String> cons = new ArrayList<String>();
		cons.add("�ں˸���");
		cons.add("��λ����");
		cons.add("��������");
		cons.add("�ں�����");
		cons.add("�洢����");
//		cons.add("�̼���");
		//֮ǰ��attributes
		for(int i=0;i<cons.size();i++) {
			configsList.add(cons.get(i));
			TreeItem t = new TreeItem(cpuGroupTree, SWT.NONE);
			t.setText(cons.get(i));
//			t.setBackground(color);
			if(! attributes.contains(cons.get(i))) {
				t.setImage(CPluginImages.CFG_DONE_VIEW.createImage());
			}
		}

		contentGroup = ControlFactory.createGroup(cpuGroupCpt, "��������", 1);
		contentGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		contentGroup.setLayout(new GridLayout(1,true));
		
		scrolledComposite = new ScrolledComposite(contentGroup, SWT.V_SCROLL
                | SWT.H_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		configContent = new Composite(scrolledComposite,SWT.NONE);
		configContent.setLayout(new GridLayout());
		configContent.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Point point = configContent.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		scrolledComposite.setContent(configContent);
		scrolledComposite.setMinHeight(point.y);
		scrolledComposite.setExpandHorizontal(true);
	    scrolledComposite.setExpandVertical(true);
	    scrolledComposite.setMinWidth(300);
		
	    cpuGroupTree.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = cpuGroupTree.getSelection();
				if (items.length > 0) {
					String selectConfigName = items[0].getText();
					contentGroup.setText(selectConfigName);
					scrolledComposite.dispose();
					switch (selectConfigName) {
					case "�ں˸���":
						 creatCoreNumContent(contentGroup);
						break;
					case "�ں�����":
						creatCoreContent(contentGroup);
						break;
					case "�洢����":
						creatMemoryContent(contentGroup);
						break;
					case "��������":
						creatFloatContent(contentGroup);
						break;
					case "��λ����":
						creatResetContent(contentGroup);
						break;
					case "�̼���":
						creatFirmwareLibContent(contentGroup);
						break;
					}		
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		return super.createDialogArea(parent);
	}
	
	protected void creatFirmwareLibContent(Group contentGroup2) {
		// TODO Auto-generated method stub
		scrolledComposite = new ScrolledComposite(contentGroup, SWT.V_SCROLL
                | SWT.H_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		configContent = new Composite(scrolledComposite,SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		configContent.setLayout(layout);
		configContent.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label nameLabel = new Label(configContent,SWT.NONE);
		nameLabel.setText("�̼���ѡ��: ");
		Combo firmwareCombo = new Combo(configContent,SWT.READ_ONLY);
		
		File firewareFile = new File(eclipsePath+"djysrc\\third\\firmware");
		File[] files = firewareFile.listFiles();
		for(int i=0;i<files.length;i++) {
			firewareLibs.add(files[i].getName());
		}
		for(int i=0;i<firewareLibs.size();i++) {
			firmwareCombo.add(firewareLibs.get(i));
		}
		firmwareCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		if(parentCpu.getFirmwareLib()!=null) {
			String firmwareLib = parentCpu.getFirmwareLib();
			for(int i=0;i<firewareLibs.size();i++) {
				if(firmwareLib.equals(firewareLibs.get(i))) {
					firmwareCombo.select(i);
				}
			}
		}
		
		firmwareCombo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				String firmware = firmwareCombo.getText();
				newCpu.setFirmwareLib(firmware);
				if(tempName!=null) {
					revisingCpu.setFirmwareLib(firmware);
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});

		Point point = configContent.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		scrolledComposite.setContent(configContent);
		scrolledComposite.setMinHeight(point.y);
		scrolledComposite.setExpandHorizontal(true);
	    scrolledComposite.setExpandVertical(true);
	    scrolledComposite.setExpandVertical(true);
	    scrolledComposite.setMinWidth(300);
		contentGroup.layout();
		
	}
	
	protected void creatResetContent(Group contentGroup) {
		// TODO Auto-generated method stub
		scrolledComposite = new ScrolledComposite(contentGroup, SWT.V_SCROLL
                | SWT.H_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		configContent = new Composite(scrolledComposite,SWT.NONE);
		configContent.setLayout(new GridLayout());
		configContent.setLayoutData(new GridData(GridData.FILL_BOTH));
		
//		if(haveCore) {
//			curCpu = parentCpu;
//		}else {
//			curCpu = newCpu;
//		}
//		
		Composite coreSelectCpt = new Composite(configContent,SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		coreSelectCpt.setLayout(layout);
		coreSelectCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label nameLabel = new Label(coreSelectCpt,SWT.NONE);
		nameLabel.setText("�ں�ѡ��: ");
		Combo numCombo = new Combo(coreSelectCpt,SWT.READ_ONLY);
		for(int i=0;i<curCpu.getCoreNum();i++) {
			numCombo.add("�ں�"+(i+1));
		}
		numCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Group resetGroup = ControlFactory.createGroup(configContent, "���ø�λ��ַ", 1);
		resetGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		resetGroup.setLayout(new GridLayout());
		
		Composite coreConfigCpt = new Composite(resetGroup,SWT.NONE);
		GridLayout coreLayout = new GridLayout();
		coreLayout.numColumns = 2;
		coreConfigCpt.setLayout(coreLayout);
		coreConfigCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label abiLabel = new Label(coreConfigCpt,SWT.NONE);
		abiLabel.setText("��λ��ַ�� ");
		Text addrText = new Text(coreConfigCpt,SWT.BORDER);
		addrText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		if (curCpu.getCores().size() != 0) {
			numCombo.select(0);
			if (curCpu.getCores().get(0).getResetAddr() != null) {
				addrText.setText(curCpu.getCores().get(0).getResetAddr());
			}
		} else {
			coreSelectCpt.setVisible(false);
			if (newCore.getResetAddr() != null) {
				addrText.setText(newCore.getResetAddr());
			}
		}

//		if(parentCpu.getCores().size()==0) {
//			coreSelectCpt.setVisible(false);
//			if(newCore.getResetAddr()!=null) {
//				addrText.setText(newCore.getResetAddr());
//			}
//		}else {
//			if(parentCpu.getCores().get(0).getResetAddr()!=null) {
//				numCombo.select(0);
//				addrText.setText(parentCpu.getCores().get(0).getResetAddr());
//			}
//		}
		numCombo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int selectIndex = numCombo.getSelectionIndex();
				String addr = curCpu.getCores().get(selectIndex).getResetAddr();
				if(addr!=null) {
					addrText.setText(addr);
				}else {
					addrText.setText("");
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		addrText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				// TODO Auto-generated method stub
				String resetAddr = addrText.getText().trim();
				if(newCpu.getCores().size()!=0) {
					int selectIndex = numCombo.getSelectionIndex();
					newCpu.getCores().get(selectIndex).setResetAddr(resetAddr);
					if(tempName!=null) {
						revisingCpu.getCores().get(selectIndex).setResetAddr(resetAddr);
					}			
					curCpu.getCores().get(selectIndex).setResetAddr(resetAddr);
				}else {
					newCore.setResetAddr(resetAddr);
				}
				
			}
		});
		
		Point point = configContent.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		scrolledComposite.setContent(configContent);
		scrolledComposite.setMinHeight(point.y);
		scrolledComposite.setExpandHorizontal(true);
	    scrolledComposite.setExpandVertical(true);
	    scrolledComposite.setExpandVertical(true);
	    scrolledComposite.setExpandVertical(true);
	    scrolledComposite.setMinWidth(300);
		contentGroup.layout();
	}

	protected void creatFloatContent(Group contentGroup) {
		// TODO Auto-generated method stub
		scrolledComposite = new ScrolledComposite(contentGroup, SWT.V_SCROLL
                | SWT.H_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		configContent = new Composite(scrolledComposite,SWT.NONE);
		configContent.setLayout(new GridLayout());
		configContent.setLayoutData(new GridData(GridData.FILL_BOTH));
		//�ں�ѡ�����
		Composite coreSelectCpt = new Composite(configContent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		coreSelectCpt.setLayout(layout);
		coreSelectCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label nameLabel = new Label(coreSelectCpt, SWT.NONE);
		nameLabel.setText("�ں�ѡ��: ");
		Combo numCombo = new Combo(coreSelectCpt, SWT.READ_ONLY);
		for (int i = 0; i < curCpu.getCoreNum(); i++) {
			numCombo.add("�ں�" + (i + 1));
		}
		numCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		//���ø������
		Group floatGroup = ControlFactory.createGroup(configContent, "���ø���", 1);
		floatGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		floatGroup.setLayout(new GridLayout());
		
		Composite coreConfigCpt = new Composite(floatGroup,SWT.NONE);
		GridLayout coreLayout = new GridLayout();
		coreLayout.numColumns = 2;
		coreConfigCpt.setLayout(coreLayout);
		coreConfigCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label abiLabel = new Label(coreConfigCpt,SWT.NONE);
		abiLabel.setText("Float ABI�� ");
		Combo abiCombo = new Combo(coreConfigCpt,SWT.READ_ONLY);
		String[] abis = {"Toolchain default","Library(soft)","FP instructions(hard)","Library with FP(softfp)"};
		abiCombo.setItems(abis);
		abiCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label fpuTypeLabel = new Label(coreConfigCpt,SWT.NONE);
		fpuTypeLabel.setText("FPU Type�� ");
		Combo fpuTypeCombo = new Combo(coreConfigCpt,SWT.READ_ONLY);
		String[] fpuTypes = {"Toolchain default","fpv5-d16","fpv5-sp-d16","fpv4-sp-d16","fpv4-d16"};
		fpuTypeCombo.setItems(fpuTypes);
		fpuTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		//�ں˸���Ϊ0ʱ
		if (curCpu.getCores().size() == 0) {
			coreSelectCpt.setVisible(false);
			if(newCore.getFloatABI()!=null) {
				for(int i=0;i<abis.length;i++) {
					if(newCore.getFloatABI().equals(abis[i])) {
						abiCombo.select(i);
						break;
					}
				}
			}
			if(newCore.getFpuType()!=null) {
				if(newCore.getFloatABI().equals("Library(soft)")) {
					fpuTypeCombo.select(0);
					fpuTypeCombo.setEnabled(false);
				}else {
					for(int i=0;i<fpuTypes.length;i++) {
						if(newCore.getFpuType().equals(fpuTypes[i])) {
							fpuTypeCombo.select(i);
							break;
						}
					}
				}
				
			}else {
				fpuTypeCombo.deselectAll();
			}
		}else {//�ں˸�����Ϊ0ʱ
			List<Core> cores = curCpu.getCores();
			
			numCombo.select(0);
			if(cores.get(0).getFloatABI()!=null) {
				for(int i=0;i<abis.length;i++) {
					if(abis[i].contains(cores.get(0).getFloatABI())) {
						abiCombo.select(i);
						break;
					}
				}
				if(cores.get(0).getFpuType()!=null) {
					if(cores.get(0).getFloatABI().equals("Library(soft)")) {
						fpuTypeCombo.select(0);
						fpuTypeCombo.setEnabled(false);
					}else {
						for(int i=0;i<fpuTypes.length;i++) {
							if(fpuTypes[i].contains(cores.get(0).getFpuType())) {
								fpuTypeCombo.select(i);
								break;
							}
						}
					}
				}
			}
			
		}
		//�ں˸�ѡ��ѡ���¼�
		numCombo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int selectIndex = numCombo.getSelectionIndex();
				String floatABI = curCpu.getCores().get(selectIndex).getFloatABI();
				String fpuType = curCpu.getCores().get(selectIndex).getFpuType();				
				if(floatABI!=null) {
					for(int i=0;i<abis.length;i++) {
						if(abis[i].contains(floatABI)) {
							abiCombo.select(i);
						}
					}
				}else {
					abiCombo.deselectAll();
				}
				if(fpuType!=null) {
					for(int i=0;i<fpuTypes.length;i++) {
						if(fpuTypes[i].contains(fpuType) ) {
							fpuTypeCombo.select(i);
						}
					}
				}else {
					fpuTypeCombo.deselectAll();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		//Float ABI��ѡ��ѡ���¼�
		abiCombo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				String floatABI = abiCombo.getText();
				if(curCpu.getCores().size()!=0) {
					int selectIndex = numCombo.getSelectionIndex();
					if(floatABI.contains("hard")) {
						newCpu.getCores().get(selectIndex).setFloatABI("hard");
						if(tempName!=null) {
							revisingCpu.getCores().get(selectIndex).setFloatABI("hard");
						}
						
						curCpu.getCores().get(selectIndex).setFloatABI("hard");
					}else if(floatABI.contains("softfp")) {
						newCpu.getCores().get(selectIndex).setFloatABI("softfp");
						if(tempName!=null) {
							revisingCpu.getCores().get(selectIndex).setFloatABI("softfp");
						}
						
						curCpu.getCores().get(selectIndex).setFloatABI("softfp");
					}else if(floatABI.contains("soft")) {
						newCpu.getCores().get(selectIndex).setFloatABI("soft");
						if(tempName!=null) {
							revisingCpu.getCores().get(selectIndex).setFloatABI("soft");
						}
						
						curCpu.getCores().get(selectIndex).setFloatABI("soft");
					}else{
						newCpu.getCores().get(selectIndex).setFloatABI("default");
						if(tempName!=null) {
							revisingCpu.getCores().get(selectIndex).setFloatABI("default");
						}
						
						curCpu.getCores().get(selectIndex).setFloatABI("default");
					}
				}else {
					if(floatABI.contains("hard")) {
						newCore.setFloatABI("hard");
					}else if(floatABI.contains("softfp")) {
						newCore.setFloatABI("softfp");
					}else if(floatABI.contains("soft")) {
						newCore.setFloatABI("soft");
					}else{
						newCore.setFloatABI("default");
					}
				}
				if(floatABI.equals("Library(soft)")) {
					fpuTypeCombo.select(0);
					fpuTypeCombo.setEnabled(false);
				}else {
					fpuTypeCombo.setEnabled(true);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		//fpuTypeCombo����ѡ��ѡ���¼�
		fpuTypeCombo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				String fpuType = fpuTypeCombo.getText().trim();
				if(curCpu.getCores().size()!=0) {
					int selectIndex = numCombo.getSelectionIndex();
					if(fpuType.contains("default")) {
						newCpu.getCores().get(selectIndex).setFpuType("default");
						if(tempName!=null) {
							revisingCpu.getCores().get(selectIndex).setFpuType("default");
						}
						
						curCpu.getCores().get(selectIndex).setFpuType("default");
					}else{
						newCpu.getCores().get(selectIndex).setFpuType(fpuType);
						if(tempName!=null) {
							revisingCpu.getCores().get(selectIndex).setFpuType(fpuType);
						}
						
						curCpu.getCores().get(selectIndex).setFpuType(fpuType);
					}
				}else {
					if(fpuType.contains("default")) {
						newCore.setFpuType("default");
					}else{
						newCore.setFpuType(fpuType);
					}
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});

		Point point = configContent.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		scrolledComposite.setContent(configContent);
		scrolledComposite.setMinHeight(point.y);
		scrolledComposite.setExpandHorizontal(true);
	    scrolledComposite.setExpandVertical(true);
	    scrolledComposite.setExpandVertical(true);
	    scrolledComposite.setMinWidth(300);
		contentGroup.layout();
	}

	protected void creatMemoryContent(Group contentGroup) {
		// TODO Auto-generated method stub
		scrolledComposite = new ScrolledComposite(contentGroup, SWT.V_SCROLL
                | SWT.H_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		configContent = new Composite(scrolledComposite,SWT.NONE);
		configContent.setLayout(new GridLayout());
		configContent.setLayoutData(new GridData(GridData.FILL_BOTH));
		Composite coreSelectCpt = new Composite(configContent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		coreSelectCpt.setLayout(layout);
		coreSelectCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label nameLabel = new Label(coreSelectCpt, SWT.NONE);
		nameLabel.setText("�ں�ѡ��: ");
		Combo numCombo = new Combo(coreSelectCpt, SWT.READ_ONLY);
		for (int i = 0; i < curCpu.getCoreNum(); i++) {
			numCombo.add("�ں�" + (i + 1));
		}
		numCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Group memoryGroup = ControlFactory.createGroup(configContent, "�����ڴ�", 1);
		GridLayout memoryGroupLayout = new GridLayout();
		memoryGroupLayout.numColumns = 2;
		memoryGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		memoryGroup.setLayout(memoryGroupLayout);

		Composite memoryComposite = new Composite(memoryGroup,SWT.NONE);
		memoryComposite.setLayout(new GridLayout());
		memoryComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		
		memoryTree = new Tree(memoryComposite, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		memoryTree.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		memoryTree.setHeaderVisible(true);
		memoryTree.setSize(150, 170);
		final TreeColumn columnMemory = new TreeColumn(memoryTree, SWT.NONE);
		columnMemory.setText("Ƭ�ڴ洢");
		columnMemory.setWidth(120);
		columnMemory.setResizable(false);
		columnMemory.setToolTipText("Inner Memory");
		
		Composite btnCpt = new Composite(memoryComposite, SWT.NONE);
		btnCpt.setLayout(new GridLayout(2,true));
		btnCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_CENTER));
		Button addBtn = new Button(btnCpt,SWT.PUSH);
		addBtn.setText("���");
		addBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int memoryCount = memoryTree.getItemCount();//memoryTree�ĺ�������
				TreeItem t = new TreeItem(memoryTree, SWT.NONE);//����µ��ڴ�
				TreeItem[] items = memoryTree.getItems();
				int max = 0;
				if (memoryCount > 0) {
					 String maxString = items[memoryCount-1].getText();
					 max = Integer.parseInt(maxString.substring(6,maxString.length()));
				}
				t.setText("memory"+(max+1));
				CoreMemory memory = new CoreMemory();
				memory.setName("memory"+(max+1));
				if(newCpu.getCores().size()!=0) {
					int selectIndex = numCombo.getSelectionIndex();
					newCpu.getCores().get(selectIndex).getMemorys().add(memory);
					if(tempName!=null) {
						revisingCpu.getCores().get(selectIndex).getMemorys().add(memory);
					}
					
					curCpu.getCores().get(selectIndex).getMemorys().add(memory);
				}else {
					newCore.getMemorys().add(memory);
				}
				
			
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		Button deleteBtn = new Button(btnCpt,SWT.PUSH);
		deleteBtn.setText("ɾ��");
		deleteBtn.setEnabled(false);
		deleteBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int selectIndex = numCombo.getSelectionIndex();
				TreeItem[] items = memoryTree.getSelection();
				if (items.length > 0) {
					for(int i=0;i<newCpu.getCores().get(selectIndex).getMemorys().size();i++) {
						String memoryName = newCpu.getCores().get(selectIndex).getMemorys().get(i).getName();
						if(memoryName!=null) {
							if(memoryName.equals(items[0].getText().trim())) {
								newCpu.getCores().get(selectIndex).getMemorys().remove(i);
								if(tempName!=null) {
									revisingCpu.getCores().get(selectIndex).getMemorys().remove(i);
								}
								
								curCpu.getCores().get(selectIndex).getMemorys().remove(i);
							}
						}						
					}
					items[0].dispose();
				}
				int memoryNum = memoryTree.getItemCount();
				if(memoryNum<1) {
					deleteBtn.setEnabled(false);
					setMemoryCpt(false);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		Composite memoryContentCpt = new Composite(memoryGroup,SWT.NONE);
		GridLayout detailsLayout = new GridLayout();
		detailsLayout.marginHeight = 20;
		detailsLayout.numColumns = 2;
		memoryContentCpt.setLayout(detailsLayout);
		memoryContentCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		memoryTypeLabel = new Label(memoryContentCpt,SWT.NONE);
		memoryTypeLabel.setText("����: ");		

		memoryTypeCombo = new Combo(memoryContentCpt,SWT.READ_ONLY);
		memoryTypeCombo.add("FLASH");
		memoryTypeCombo.add("RAM");
		memoryTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		memoryTypeCombo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				memoryAddrLabel.setEnabled(true);
				memorySizeLabel.setEnabled(true);
				sizeField.setEnabled(true);
				addrField.setEnabled(true);
				int selectIndex = numCombo.getSelectionIndex();
				if(selectIndex!=-1) {
					List<CoreMemory> memorys = newCpu.getCores().get(selectIndex).getMemorys();//��ȡ��ǰ�ں������ڴ�
					
					int comboIndex = memoryTypeCombo.getSelectionIndex();
					TreeItem[] items = memoryTree.getSelection();
					String type = memoryTypeCombo.getItem(comboIndex);//
					int index = -1;//
					if(items.length>0) {
						String selectMemory = items[0].getText().trim();//ѡ�е��ڴ�
						for(int i=0;i<memorys.size();i++) {
							CoreMemory memory = memorys.get(i);
							if(memory.getName()!=null) {
								if(memory.getName().equals(selectMemory)) {							
									memory.setType(type);
									index = i;
									break;
								}
							}		
						}
						if(tempName != null) {
							List<CoreMemory> _memorys = revisingCpu.getCores().get(selectIndex).getMemorys();//��ȡ��ǰ�ں������ڴ�
							for(int i=0;i<_memorys.size();i++) {
								CoreMemory memory = _memorys.get(i);
								if(memory.getName()!=null) {
									if(memory.getName().equals(selectMemory)) {							
										memory.setType(type);
										break;
									}
								}		
							}
						}
					}
					if(index != -1) {
						curCpu.getCores().get(selectIndex).getMemorys().get(index).setType(type);
					}
				}				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		memoryAddrLabel = new Label(memoryContentCpt,SWT.NONE);
		memoryAddrLabel.setText("��ַ: ");
		addrField = new Text(memoryContentCpt,SWT.BORDER);
		addrField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addrField.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				// TODO Auto-generated method stub
				int selectIndex = numCombo.getSelectionIndex();
				if(selectIndex!=-1) {
					List<CoreMemory> memorys = newCpu.getCores().get(selectIndex).getMemorys();//��ȡ��ǰ�ں������ڴ�
					
					TreeItem[] items = memoryTree.getSelection();
					String addr = addrField.getText().trim();//
					int index = -1;//
					if (items.length > 0) {
						String selectMemory = items[0].getText().trim();
						for (int i = 0; i < memorys.size(); i++) {
							CoreMemory memory = memorys.get(i);
							if(memory.getName()!=null) {
								if (memory.getName().equals(selectMemory)) {							
									memory.setStartAddr(addr);
									index = i;
									break;
								}
							}
							
						}
						if(tempName != null) {
							List<CoreMemory> _memorys = revisingCpu.getCores().get(selectIndex).getMemorys();//��ȡ��ǰ�ں������ڴ�
							System.out.println("_memorys.size()��  "+_memorys.size());
							for (int i = 0; i < _memorys.size(); i++) {
								CoreMemory memory = _memorys.get(i);
								System.out.println("memory.getName()��  "+memory.getName());
								if(memory.getName()!=null) {
									if (memory.getName().equals(selectMemory)) {							
										memory.setStartAddr(addr);
										break;
									}
								}
								
							}
						}
					
					}
					if(index != -1) {
						curCpu.getCores().get(selectIndex).getMemorys().get(index).setStartAddr(addr);
					}
				}			
			}
		});

		memorySizeLabel = new Label(memoryContentCpt,SWT.NONE);
		memorySizeLabel.setText("��С: ");
		sizeField = new Text(memoryContentCpt,SWT.BORDER);
		sizeField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		setMemoryCpt(false);
		sizeField.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				// TODO Auto-generated method stub
				int selectIndex = numCombo.getSelectionIndex();
				if(selectIndex!=-1) {
					List<CoreMemory> memorys = newCpu.getCores().get(selectIndex).getMemorys();//��ȡ��ǰ�ں������ڴ�

					TreeItem[] items = memoryTree.getSelection();
					int index = -1;//
					String size = sizeField.getText().trim().replace("k", "");
					if (items.length > 0) {
						String selectMemory = items[0].getText().trim();
						IWorkbenchWindow window = PlatformUI.getWorkbench()
    							.getActiveWorkbenchWindow();
						if (!size.equals("")) {
							int curNum = -1;         
							Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");  
    	    		        boolean isInt =  pattern.matcher(size).matches();
		    				if(size.startsWith("0x")) {
		    					curNum = Integer.parseInt(size.substring(2),16);
		    				}else {
		    					if(isInt) {
		    						curNum = Integer.parseInt(size);
		    					}else {
		    						sizeField.setText("");
		    						MessageDialog.openError(window.getShell(), "��ʾ",
		        							"������������");
		    					}
		    					
		    				}             					
	        				if(curNum<0) {
	        					sizeField.setText("");
	        					MessageDialog.openError(window.getShell(), "��ʾ",
	        							"������������");
	        				}else {
	        					for (int i = 0; i < memorys.size(); i++) {
									CoreMemory memory = memorys.get(i);
									if(memory.getName()!=null) {
										if (memory.getName().equals(selectMemory)) {
											memory.setSize(size);
											index = i;
											break;
										}	
									}
								}
	        					
	        					if(tempName != null) {
	        						List<CoreMemory> _memorys = revisingCpu.getCores().get(selectIndex).getMemorys();//��ȡ��ǰ�ں������ڴ�
	        						for (int i = 0; i < _memorys.size(); i++) {
										CoreMemory memory = _memorys.get(i);
										if(memory.getName()!=null) {
											if (memory.getName().equals(selectMemory)) {
												memory.setSize(size);
												index = i;
												break;
											}	
										}
									}
	        					}
	        				
	        				}
							
						}
					}
					if(index != -1) {
						curCpu.getCores().get(selectIndex).getMemorys().get(index).setSize(size);
					}
				}			
			}
		});
	    //ѡ���ں˵ļ����¼�
		numCombo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int selectIndex = numCombo.getSelectionIndex();
				List<CoreMemory> memorys = curCpu.getCores().get(selectIndex).getMemorys();
				Control[] controls = memoryGroup.getChildren();
				for(Control control : controls) {
					control.setEnabled(true);
				}
				memoryTree.removeAll();
				if(memorys.size()!=0) {
					for(int i=0;i<memorys.size();i++) {
						String memoryName = memorys.get(i).getName();
						TreeItem t = new TreeItem(memoryTree, SWT.NONE);
						t.setText(memoryName);
					}		
					memoryTypeLabel.setEnabled(true);
					memoryTypeCombo.setEnabled(true);
				}else {
					setMemoryCpt(false);
				}
				memoryTypeCombo.deselectAll();
				addrField.setText("");
				sizeField.setText("");
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		
		memoryTree.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				deleteBtn.setEnabled(true);
//				memoryTypeLabel.setEnabled(true);
//				memoryTypeCombo.setEnabled(true);
				setMemoryCpt(true);
				TreeItem[] items = memoryTree.getSelection();
				int selectIndex = numCombo.getSelectionIndex();
				List<CoreMemory> memorys = curCpu.getCores().get(selectIndex).getMemorys();
				if (items.length > 0) {
					String selectMemoryName = items[0].getText();
					for(int i=0;i<memorys.size();i++) {
						String memoryName = memorys.get(i).getName();
						if(memoryName.equals(selectMemoryName)) {
							if(memorys.get(i).getType()!=null) {
								if(memorys.get(i).getType().equals("FLASH")) {
									memoryTypeCombo.select(0);
								}else if(memorys.get(i).getType().equals("RAM")) {
									memoryTypeCombo.select(1);
								}
							}else {
								memoryTypeCombo.deselectAll();
							}
							if(memorys.get(i).getStartAddr()!=null) {
								addrField.setText(memorys.get(i).getStartAddr());
							}else {
								addrField.setText("");
							}
							if(memorys.get(i).getSize()!=null) {
								sizeField.setText(String.valueOf(memorys.get(i).getSize()));	
							}else {
								sizeField.setText("");
							}
						}
					}		
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});

		if (curCpu.getCores().size() == 0) {
			coreSelectCpt.setVisible(false);
			List<CoreMemory> memorys = newCore.getMemorys();
			int count = 0;
			for(CoreMemory memory:memorys) {
				if(memory.getName() == null) {
					memory.setName("memory"+(count+1));
				}
				count++;
			}
			memoryTree.removeAll();
			if(memorys.size()!=0) {
				for(int i=0;i<memorys.size();i++) {
					String memoryName = memorys.get(i).getName();
					TreeItem t = new TreeItem(memoryTree, SWT.NONE);
					t.setText(memoryName);
				}		
			}
			memoryTypeCombo.deselectAll();
			addrField.setText("");
			sizeField.setText("");
		}else {
			List<Core> cores = curCpu.getCores();
			numCombo.select(0);
			for(Core core:curCpu.getCores()) {
				List<CoreMemory> memorys = core.getMemorys();
				int i = 0;
				for(CoreMemory memory:memorys) {
					if(memory.getName() == null) {
						memory.setName("memory"+(i+1));
					}
					i++;
				}
			}
			for(Core core:revisingCpu.getCores()) {
				List<CoreMemory> memorys = core.getMemorys();
				int i = 0;
				for(CoreMemory memory:memorys) {
					if(memory.getName() == null) {
						memory.setName("memory"+(i+1));
					}
					i++;
				}
			}
			memoryTree.removeAll();
			if(curCpu.getCores().get(0).getMemorys().size()!=0){
				List<CoreMemory> memorys = curCpu.getCores().get(0).getMemorys();
				
				memoryTree.removeAll();
				for (int i = 0; i < memorys.size(); i++) {
					String memoryName = memorys.get(i).getName();
					TreeItem t = new TreeItem(memoryTree, SWT.NONE);
					t.setText(memoryName);
				}
				memoryTypeCombo.deselectAll();
				addrField.setText("");
				sizeField.setText("");
			}		
		}
		
		int selectIndex = numCombo.getSelectionIndex();
		if(selectIndex==-1) {
			Control[] controls = memoryGroup.getChildren();
			for(Control control : controls) {
				control.setEnabled(false);
			}
		}else {
			Control[] controls = memoryGroup.getChildren();
			for(Control control : controls) {
				control.setEnabled(true);
			}
		}
		Point point = configContent.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		scrolledComposite.setContent(configContent);
		scrolledComposite.setMinHeight(point.y);
		scrolledComposite.setExpandHorizontal(true);
	    scrolledComposite.setExpandVertical(true);
	    scrolledComposite.setExpandVertical(true);
	    scrolledComposite.setMinWidth(300);
		contentGroup.layout();
		
	}

	protected void creatCoreContent(Group contentGroup) {
		// TODO Auto-generated method stub
		scrolledComposite = new ScrolledComposite(contentGroup, SWT.V_SCROLL
                | SWT.H_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		configContent = new Composite(scrolledComposite,SWT.NONE);
		configContent.setLayout(new GridLayout());
		configContent.setLayoutData(new GridData(GridData.FILL_BOTH));
		Composite coreSelectCpt = new Composite(configContent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		coreSelectCpt.setLayout(layout);
		coreSelectCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label nameLabel = new Label(coreSelectCpt, SWT.NONE);
		nameLabel.setText("�ں�ѡ��: ");
		Combo numCombo = new Combo(coreSelectCpt, SWT.READ_ONLY);
		for (int i = 0; i < curCpu.getCoreNum(); i++) {
			numCombo.add("�ں�" + (i + 1));
		}
		numCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		

		Group coreGroup = ControlFactory.createGroup(configContent, "�����ں�", 1);
		contentGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		contentGroup.setLayout(new GridLayout());
		
		Composite coreConfigCpt = new Composite(coreGroup,SWT.NONE);
		GridLayout coreLayout = new GridLayout();
		coreLayout.numColumns = 2;
		coreConfigCpt.setLayout(coreLayout);
		coreConfigCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		String typePath = eclipsePath+"djysrc\\bsp\\arch";
		File typeFile = new File(typePath);
		File[] files =  typeFile.listFiles();
		List<String> fileNames = new ArrayList<String>(); 
		for(int i=0;i<files.length;i++) {
			if(!files[i].getName().equals("dsp")) {
				fileNames.add(files[i].getName());
			}		
		}
		String[] types = new String[fileNames.size()];
		for(int i=0;i<fileNames.size();i++) {
			types[i] = fileNames.get(i);
		}
		
		Label typeLabel = new Label(coreConfigCpt,SWT.NONE);
		typeLabel.setText("���ͣ� ");
		Combo typeCombo = new Combo(coreConfigCpt,SWT.READ_ONLY);
		typeCombo.setItems(types);
		typeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
		
		Label archLabel = new Label(coreConfigCpt,SWT.NONE);
		archLabel.setText("�ܹ��� ");
		Combo archCombo = new Combo(coreConfigCpt,SWT.READ_ONLY);
//		archCombo.setEnabled(false);
		archCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		//����������ѡ�е��¼�����
		typeCombo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				String type = typeCombo.getText().trim();
				String archPath = typePath+"/"+type;
				File archFile = new File(archPath);
				File[] archFiles =  archFile.listFiles(new MyFileter());
				String[] archs = new String[archFiles.length];
				for(int i=0;i<archFiles.length;i++) {
					if(archFiles[i].isDirectory()) {
						archs[i] = archFiles[i].getName();
					}	
				}
				archCombo.setEnabled(true);
				archCombo.setItems(archs);
				if(newCpu.getCores().size()!=0) {
					int selectIndex = numCombo.getSelectionIndex();
					newCpu.getCores().get(selectIndex).setType(type);
					if(tempName!=null) {
						revisingCpu.getCores().get(selectIndex).setType(type);
					}
					
					curCpu.getCores().get(selectIndex).setType(type);
				}else {
					newCore.setType(type);
				}	
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
				
		Label familyLabel = new Label(coreConfigCpt,SWT.NONE);
		familyLabel.setText("���壺 ");
		Combo familyCombo = new Combo(coreConfigCpt,SWT.READ_ONLY);
		familyCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		familyCombo.setEnabled(false);
		archCombo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				String type = typeCombo.getText().trim();
				String arch = archCombo.getText().trim();
				String familyPath = typePath+"/"+type+"/"+arch;
				File familyFile = new File(familyPath);
				File[] familyFiles =  familyFile.listFiles(new MyFileter());
				String[] familys = new String[familyFiles.length];
				for(int i=0;i<familyFiles.length;i++) {
					familys[i] = familyFiles[i].getName();					
				}
				familyCombo.setEnabled(true);
				familyCombo.setItems(familys);
				if(newCpu.getCores().size()!=0) {
					int selectIndex = numCombo.getSelectionIndex();
					newCpu.getCores().get(selectIndex).setArch(arch);
					if(tempName!=null) {
						revisingCpu.getCores().get(selectIndex).setArch(arch);
					}
					
					curCpu.getCores().get(selectIndex).setArch(arch);
				}else {
					newCore.setArch(arch);
				}		
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}

		});
		familyCombo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				String family = familyCombo.getText().trim();
				if(newCpu.getCores().size()!=0) {
					int selectIndex = numCombo.getSelectionIndex();
					newCpu.getCores().get(selectIndex).setFamily(family);
					if(tempName!=null) {
						revisingCpu.getCores().get(selectIndex).setFamily(family);
					}
					
					curCpu.getCores().get(selectIndex).setFamily(family);
				}else {
					newCore.setFamily(family);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		if (curCpu.getCores().size() == 0) {
			coreSelectCpt.setVisible(false);
			if(newCore.getType()!=null) {
				for(int i=0;i<types.length;i++) {
					if(newCore.getType().equals(types[i])) {
						typeCombo.select(i);
						break;
					}
				}
			}
			if(newCore.getArch()!=null) {			
				String type = newCore.getType();
				String archPath = typePath+"/"+type;
				File archFile = new File(archPath);
				File[] archFiles =  archFile.listFiles(new MyFileter());
				String[] archs = new String[archFiles.length];
				for(int i=0;i<archFiles.length;i++) {
					archs[i] = archFiles[i].getName();
				}
				archCombo.setItems(archs);
				for(int i=0;i<archs.length;i++) {
					if(newCore.getArch().equals(archs[i])) {
						archCombo.select(i);
						break;
					}
				}
			}else {
				archCombo.setEnabled(false);
			}
			if(newCore.getFamily()!=null) {
//				familyCombo.setEnabled(true);
				String type = newCore.getType();
				String arch = newCore.getArch();
				String familyPath = typePath+"/"+type+"/"+arch;
				File familyFile = new File(familyPath);
				File[] familyFiles =  familyFile.listFiles(new MyFileter());
				String[] familys = new String[familyFiles.length];
				for(int i=0;i<familyFiles.length;i++) {
					familys[i] = familyFiles[i].getName();
				}
				familyCombo.setItems(familys);
				for(int i=0;i<familys.length;i++) {
					if(newCore.getFamily().equals(familys[i])) {
						familyCombo.select(i);
						break;
					}
				}
			}else {
				familyCombo.setEnabled(false);
			}
		}else {
			List<Core> cores = curCpu.getCores();
			numCombo.select(0);
			if(cores.get(0).getType()!=null) {				
				for(int i=0;i<types.length;i++) {
					if(cores.get(0).getType().equals(types[i])) {
						typeCombo.select(i);
						break;
					}
				}
				archCombo.setEnabled(true);
			}else {
				typeCombo.setEnabled(false);
				archCombo.setEnabled(false);
			}
			
			String type = typeCombo.getText().trim();
			String archPath = typePath+"/"+type;
			File archFile = new File(archPath);
			File[] archFiles =  archFile.listFiles(new MyFileter());
			String[] archs = new String[archFiles.length];
			for(int i=0;i<archFiles.length;i++) {
				archs[i] = archFiles[i].getName();
			}
			archCombo.setItems(archs);
			
			if (cores.get(0).getArch() != null) {
				
				for(int i=0;i<archs.length;i++) {
					if(cores.get(0).getArch().equals(archs[i])) {
						archCombo.select(i);
						break;
					}
				}
				familyCombo.setEnabled(true);
			}else {
				familyCombo.setEnabled(false);
			}
			
			String arch = archCombo.getText().trim();
			String familyPath = typePath+"/"+type+"/"+arch;
			File familyFile = new File(familyPath);
			File[] familyFiles =  familyFile.listFiles(new MyFileter());
			String[] familys = new String[familyFiles.length];
			for(int i=0;i<familyFiles.length;i++) {
				familys[i] = familyFiles[i].getName();
			}
			familyCombo.setItems(familys);
			if(cores.get(0).getFamily()!=null) {			
				for(int i=0;i<familys.length;i++) {
					if(cores.get(0).getFamily().equals(familys[i])) {
						familyCombo.select(i);
						break;
					}
				}
			}
			
		}
		
		numCombo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				typeCombo.setEnabled(true);
				int selectIndex = numCombo.getSelectionIndex();
				String type = curCpu.getCores().get(selectIndex).getType();
				String arch = curCpu.getCores().get(selectIndex).getArch();
				String family = curCpu.getCores().get(selectIndex).getFamily();
				if(type!=null) {
					for(int i=0;i<types.length;i++) {
						if(type.equals(types[i])) {
							typeCombo.select(i);
							break;
						}
					}
				}else {
					typeCombo.deselectAll();
				}
				if (arch!= null) {
					String stype = typeCombo.getText().trim();
					String archPath = typePath+"/"+stype;
					File archFile = new File(archPath);
					File[] archFiles =  archFile.listFiles(new MyFileter());
					String[] archs = new String[archFiles.length];
					for(int i=0;i<archFiles.length;i++) {
						archs[i] = archFiles[i].getName();
					}
					archCombo.setItems(archs);
					for (int i = 0; i < archs.length; i++) {
						if (arch.equals(archs[i])) {
							archCombo.select(i);
							break;
						}
					}
				}else {
					archCombo.deselectAll();
				}
				if(family!=null) {
					String stype = typeCombo.getText().trim();
					String sarch = archCombo.getText().trim();
					String familyPath = typePath+"/"+stype+"/"+sarch;
					File familyFile = new File(familyPath);
					File[] familyFiles =  familyFile.listFiles(new MyFileter());
					String[] familys = new String[familyFiles.length];				
					for(int i=0;i<familyFiles.length;i++) {
						familys[i] = familyFiles[i].getName();
					}
					familyCombo.setItems(familys);
					for (int i = 0; i < familys.length; i++) {
						if (family.equals(familys[i])) {
							System.out.println("true");
							familyCombo.select(i);					
							break;
						}
					}
				}else {
					familyCombo.deselectAll();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		
		Point point = configContent.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		scrolledComposite.setContent(configContent);
		scrolledComposite.setMinHeight(point.y);
		scrolledComposite.setExpandHorizontal(true);
	    scrolledComposite.setExpandVertical(true);
	    scrolledComposite.setExpandVertical(true);
		contentGroup.layout();
	}

	protected void creatCoreNumContent(Group contentGroup) {
		// TODO Auto-generated method stub
		scrolledComposite = new ScrolledComposite(contentGroup, SWT.V_SCROLL
                | SWT.H_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		configContent = new Composite(scrolledComposite,SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		configContent.setLayout(layout);
		configContent.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label nameLabel = new Label(configContent,SWT.NONE);
		nameLabel.setText("�ں˸���: ");
		Text numText = new Text(configContent,SWT.BORDER);
		numText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if(curCpu.getCoreNum()!=0){
			int coreNum = curCpu.getCoreNum();
			numText.setText(String.valueOf(coreNum));
			if(parentCpu.getCoreNum()!=0) {
				numText.setEnabled(false);
			}			
		}
		numText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				// TODO Auto-generated method stub
				String coreNum = numText.getText();	
				Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");  
		        boolean isInt =  pattern.matcher(coreNum).matches();
		        if(!isInt) {
		        	numText.setText("");
		        	IWorkbenchWindow window = PlatformUI.getWorkbench()
		    				.getActiveWorkbenchWindow();
		        	MessageDialog.openError(window.getShell(), "��ʾ",
							"������������");
		        }else {
		        	newCpu = new Cpu();
					int cNum = Integer.parseInt(coreNum);
					newCpu.setCoreNum(cNum);
					curCpu.setCoreNum(cNum);
					for(int i=0;i<cNum;i++) {
						newCpu.getCores().add(new Core());
					}
					curCpu = newCpu;
		        }		
							
			}
		});
		
		Point point = configContent.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		scrolledComposite.setContent(configContent);
		scrolledComposite.setMinHeight(point.y);
		scrolledComposite.setExpandHorizontal(true);
	    scrolledComposite.setExpandVertical(true);
	    scrolledComposite.setMinWidth(300);
		contentGroup.layout();
	}
	
	private void getGroupNames(File curFile,List<String> names) {//f1 stm32 
		
		names.add(curFile.getName());
		File parentFile = curFile.getParentFile();
		if(!parentFile.getName().equals("cpudrv")) {
			getGroupNames(parentFile,names);
		}
		
	}

	@Override
	protected Point getInitialSize() {
		// TODO Auto-generated method stub
		return new Point(520,400);
	}
	
	private boolean handleGroupOK() {
		groupName = groupNameField.getText();
		String completeName = "";
		if(groupName.trim().equals("")) {
			IWorkbenchWindow window = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
			MessageDialog.openError(window.getShell(), "��ʾ",
					"����д��Ŀ¼����");
			return false;
		}else {
			
			File newGroupFile = null,oldGroupFile = null;
			File xmlFile = null;
			List<String> names = new ArrayList<String>();
			if(tempName == null) {//�½���Ŀ¼����cpu
				curPath = curPath+"/"+groupName;
				newGroupFile = new File(curPath); 
				if(!newGroupFile.exists()) {
					newGroupFile.mkdir();
				}
				getGroupNames(newGroupFile,names);
				for(int i=names.size()-1;i>=0;i--) {
					completeName+=names.get(i);
				}
				xmlFile = new File(newGroupFile.getPath()+"/cpu_group_"+completeName+".xml");
				if(xmlFile.exists()) {
					xmlFile.delete();
				}
				try {
					xmlFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (newCpu.getCores().size() != 0) {
					createNewGroupXml(newCpu, xmlFile);
				} else {
					createPublicXml(newCore, xmlFile);
				}
			}else {//�޸�������Ϣ
				oldGroupFile = new File(curPath);
				newGroupFile = new File(
						curPath.substring(0, curPath.lastIndexOf("\\")) + "\\" + groupName);
				//����ǰ
				getGroupNames(oldGroupFile,names);
//				DeleteFolder(oldGroupFile.getPath());
//				if(!newGroupFile.exists()) {
//					newGroupFile.mkdir();
//				}
				for(int i=names.size()-1;i>=0;i--) {
					completeName+=names.get(i);
				}
				xmlFile = new File(oldGroupFile.getPath()+"/cpu_group_"+completeName+".xml");
				if(xmlFile.exists()) {
					xmlFile.delete();
				}
				
				oldGroupFile.renameTo(newGroupFile);
				names = new ArrayList<String>();
				completeName = "";
				getGroupNames(newGroupFile,names);
				for(int i=names.size()-1;i>=0;i--) {
					completeName+=names.get(i);
				}
				xmlFile = new File(newGroupFile.getPath()+"/cpu_group_"+completeName+".xml");
				if(xmlFile.exists()) {
					xmlFile.delete();
				}
				try {
					xmlFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (revisingCpu.getCores().size() != 0) {
					createNewGroupXml(revisingCpu, xmlFile);
				} else {
					createPublicXml(newCore, xmlFile);
				}
			}

			return true;
		}		
	}
	
	private boolean handleCpuOK() {
		List<String> cpuPieceNames = null;
		cpuName = groupNameField.getText();
		if(cpuName.trim().equals("")) {
			IWorkbenchWindow window = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
			MessageDialog.openError(window.getShell(), "��ʾ",
					"����дCpu����");
			return false;
		}else {
			File newCpuFile,oldCpuFile;
			File xmlFile;
			if(tempName == null) {//�½���Ŀ¼����cpu
				curPath = curPath+"/"+cpuName;
				newCpuFile = new File(curPath); 
				if(! newCpuFile.exists()) {
					newCpuFile.mkdir();
				}

				xmlFile = new File(newCpuFile.getPath()+"/cpu_"+cpuName+".xml");
				if(xmlFile.exists()) {
					xmlFile.delete();
				}
				try {
					xmlFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				createNewCpuXml(newCpu,xmlFile,cpuName);
			}else {//�޸�������Ϣ
				oldCpuFile = new File(curPath);
				newCpuFile = new File(
						curPath.substring(0, curPath.lastIndexOf("\\")) + "\\" + cpuName);
				xmlFile = new File(oldCpuFile.getPath()+"/cpu_"+tempName+".xml");
				if(xmlFile.exists()) {
					xmlFile.delete();
				}
				
				oldCpuFile.renameTo(newCpuFile);
				xmlFile = new File(newCpuFile.getPath()+"/cpu_"+cpuName+".xml");
				try {
					xmlFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				createNewCpuXml(revisingCpu,xmlFile,cpuName);
			}

			return true;
		}
	}
	
	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		if(cpuTag.equals("group")) {
			if(handleGroupOK()) {
				super.okPressed();
			}
		}else if(cpuTag.equals("cpu")) {
			if(handleCpuOK()) {
				super.okPressed();
			}
		} else if(cpuTag.startsWith("revise")) {
			if(cpuTag.endsWith("cpu")) {
				if(handleCpuOK()) {
					super.okPressed();
				}
			}else if(cpuTag.endsWith("group")) {
				if(handleGroupOK()) {
					super.okPressed();
				}
			}
		}
	}
	
	private void createNewGroupXml(Cpu cpu,File file) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try { 
			factory.setIgnoringElementContentWhitespace(false);
        	DocumentBuilder builder = factory.newDocumentBuilder();             
        	Document document = builder.newDocument();
        	
			Element cpuElement = document.createElement("cpu");
			for(int i=0;i<cpu.getCores().size();i++) {
				Element core = document.createElement("core");
				core.setAttribute("id", String.valueOf(i+1));
				
				Core curCore = cpu.getCores().get(i);
				if(curCore.getType() != null){
					Element type = document.createElement("type");
					type.setTextContent(curCore.getType());
					core.appendChild(type);
				}
				if(curCore.getArch()!=null) {
					Element arch = document.createElement("arch");
					arch.setTextContent(curCore.getArch());
					core.appendChild(arch);
				}
				if(curCore.getFamily()!=null) {
					Element family = document.createElement("family");
					family.setTextContent(curCore.getFamily());
					core.appendChild(family);
				}
				if(curCore.getFloatABI()!=null) {
					Element floatABI = document.createElement("floatABI");
					floatABI.setTextContent(curCore.getFloatABI());
					core.appendChild(floatABI);
				}
				if(curCore.getFpuType()!=null) {
					Element fpuType = document.createElement("fpuType");
					fpuType.setTextContent(curCore.getFpuType());
					core.appendChild(fpuType);
				}
				if(curCore.getResetAddr()!=null) {
					Element resetAddr = document.createElement("resetAddr");
					resetAddr.setTextContent(curCore.getResetAddr());
					core.appendChild(resetAddr);
				}
				if(curCore.getMemorys().size()!=0) {
					
					for(int j=0;j<curCore.getMemorys().size();j++) {
						CoreMemory curMemory = curCore.getMemorys().get(j);
						Element memory = document.createElement("memory");
						Element type = document.createElement("type");
						type.setTextContent(curMemory.getType());
						memory.appendChild(type);
						Element startAddr = document.createElement("startAddr");
						startAddr.setTextContent(curMemory.getStartAddr());
						memory.appendChild(startAddr);
						Element size = document.createElement("size");
						size.setTextContent(String.valueOf(curMemory.getSize()));
						memory.appendChild(size);
						core.appendChild(memory);
					}
					
				}
				
				cpuElement.appendChild(core);
			}
			
			document.appendChild(cpuElement);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");// ���ӻ�������������ʱ����Ĭ��Ϊ0
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");// ��������Ϊ2
			transformer.setOutputProperty("encoding", "UTF-8");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(writer));
			transformer.transform(new DOMSource(document), new StreamResult(file));
			writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void createPublicXml(Core core,File file) {
		DocumentBuilderFactory factory =  DocumentBuilderFactory.newInstance();  
		try {
			factory.setIgnoringElementContentWhitespace(false);
        	DocumentBuilder builder = factory.newDocumentBuilder();             
        	Document document = builder.newDocument();
		
			Element cpuElement = document.createElement("cpu");
			if(core.getType() != null){
				Element type = document.createElement("type");
				type.setTextContent(core.getType());
				cpuElement.appendChild(type);
			}
			if (core.getArch() != null) {
				Element arch = document.createElement("arch");
				arch.setTextContent(core.getArch());
				cpuElement.appendChild(arch);
			}
			if (core.getFamily() != null) {
				Element family = document.createElement("family");
				family.setTextContent(core.getFamily());
				cpuElement.appendChild(family);
			}
			if(core.getFloatABI()!=null) {
				Element floatABI = document.createElement("floatABI");
				floatABI.setTextContent(core.getFloatABI());
				cpuElement.appendChild(floatABI);
			}
			if (core.getFpuType() != null) {
				Element fpuType = document.createElement("fpuType");
				fpuType.setTextContent(core.getFpuType());
				cpuElement.appendChild(fpuType);
			}
			if (core.getResetAddr() != null) {
				Element resetAddr = document.createElement("resetAddr");
				resetAddr.setTextContent(core.getResetAddr());
				cpuElement.appendChild(resetAddr);
			}
			if (core.getMemorys().size() != 0) {
				
				for (int j = 0; j < core.getMemorys().size(); j++) {
					CoreMemory curMemory = core.getMemorys().get(j);
					Element memory = document.createElement("memory");
					Element type = document.createElement("type");
					type.setTextContent(curMemory.getType());
					memory.appendChild(type);
					Element startAddr = document.createElement("startAddr");
					startAddr.setTextContent(curMemory.getStartAddr());
					memory.appendChild(startAddr);
					Element size = document.createElement("size");
					size.setTextContent(String.valueOf(curMemory.getSize()));
					memory.appendChild(size);
					cpuElement.appendChild(memory);
				}
				
			}

			document.appendChild(cpuElement);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");// ���ӻ�������������ʱ����Ĭ��Ϊ0
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");// ��������Ϊ2
			transformer.setOutputProperty("encoding", "UTF-8");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(writer));
			transformer.transform(new DOMSource(document), new StreamResult(file));
			writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

	private void createNewCpuXml(Cpu cpu,File file,String completeName) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try { 
			factory.setIgnoringElementContentWhitespace(false);
        	DocumentBuilder builder = factory.newDocumentBuilder();             
        	Document document = builder.newDocument();
        	
			Element cpuElement = document.createElement("cpu");
			
			Element cpuName = document.createElement("cpuName");
			cpuName.setTextContent(completeName);
			cpuElement.appendChild(cpuName);
			
			for(int i=0;i<cpu.getCores().size();i++) {
				Element core = document.createElement("core");
				core.setAttribute("id", String.valueOf(i+1));
				
				Core curCore = cpu.getCores().get(i);
				if(curCore.getType() != null){
					Element type = document.createElement("type");
					type.setTextContent(curCore.getType());
					core.appendChild(type);
				}
				if(curCore.getArch()!=null) {
					Element arch = document.createElement("arch");
					arch.setTextContent(curCore.getArch());
					core.appendChild(arch);
				}
				if(curCore.getFamily()!=null) {
					Element family = document.createElement("family");
					family.setTextContent(curCore.getFamily());
					core.appendChild(family);
				}
				if(curCore.getFloatABI()!=null) {
					Element floatABI = document.createElement("floatABI");
					floatABI.setTextContent(curCore.getFloatABI());
					core.appendChild(floatABI);
				}
				if(curCore.getFpuType()!=null) {
					Element fpuType = document.createElement("fpuType");
					fpuType.setTextContent(curCore.getFpuType());
					core.appendChild(fpuType);
				}
				if(curCore.getResetAddr()!=null) {
					Element resetAddr = document.createElement("resetAddr");
					resetAddr.setTextContent(curCore.getResetAddr());
					core.appendChild(resetAddr);
				}
				if(curCore.getMemorys().size()!=0) {
					
					for(int j=0;j<curCore.getMemorys().size();j++) {
						CoreMemory curMemory = curCore.getMemorys().get(j);
						Element memory = document.createElement("memory");
						Element type = document.createElement("type");
						type.setTextContent(curMemory.getType());
						memory.appendChild(type);
						Element startAddr = document.createElement("startAddr");
						startAddr.setTextContent(curMemory.getStartAddr());
						memory.appendChild(startAddr);
						Element size = document.createElement("size");
						size.setTextContent(String.valueOf(curMemory.getSize()));
						memory.appendChild(size);
						core.appendChild(memory);
					}
					
				}
				
				cpuElement.appendChild(core);
			}
			
			document.appendChild(cpuElement);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");// ���ӻ�������������ʱ����Ĭ��Ϊ0
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");// ��������Ϊ2
			transformer.setOutputProperty("encoding", "UTF-8");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(writer));
			transformer.transform(new DOMSource(document), new StreamResult(file));
			writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/** 
	 *  ����·��ɾ��ָ����Ŀ¼�����۴������ 
	 *@param sPath  Ҫɾ����Ŀ¼path 
	 *@return ɾ���ɹ����� true�����򷵻� false�� 
	 */  
	public boolean DeleteFolder(String sPath) {  
	    boolean flag = false;  
	    File file = new File(sPath);  
	    // �ж�Ŀ¼���ļ��Ƿ����  
	    if (!file.exists()) {  // �����ڷ��� false  
	        return flag;  
	    } else {  
	        // �ж��Ƿ�Ϊ�ļ�  
	        if (file.isFile()) {  // Ϊ�ļ�ʱ����ɾ���ļ�����  
	            return deleteFile(sPath);  
	        } else {  // ΪĿ¼ʱ����ɾ��Ŀ¼����  
	            return deleteDirectory(sPath);  
	        }  
	    }  
	}  
	
	/** 
	 * ɾ�������ļ� 
	 * @param   sPath ��ɾ���ļ�path 
	 * @return ɾ���ɹ�����true�����򷵻�false 
	 */  
	public boolean deleteFile(String sPath) {  
	    boolean flag = false;  
	    File file = new File(sPath);  
	    // ·��Ϊ�ļ��Ҳ�Ϊ�������ɾ��  
	    if (file.isFile() && file.exists()) {  
	        file.delete();  
	        flag = true;  
	    }  
	    return flag;  
	}  
	
	/** 
	 * ɾ��Ŀ¼�Լ�Ŀ¼�µ��ļ� 
	 * @param   sPath ��ɾ��Ŀ¼��·�� 
	 * @return  Ŀ¼ɾ���ɹ�����true�����򷵻�false 
	 */  
	public boolean deleteDirectory(String sPath) {  
	    //���sPath�����ļ��ָ�����β���Զ�����ļ��ָ���  
	    if (!sPath.endsWith(File.separator)) {  
	        sPath = sPath + File.separator;  
	    }  
	    File dirFile = new File(sPath);  
	    //���dir��Ӧ���ļ������ڣ����߲���һ��Ŀ¼�����˳�  
	    if (!dirFile.exists() || !dirFile.isDirectory()) {  
	        return false;  
	    }  
	    boolean flag = true;  
	    //ɾ���ļ����µ������ļ�(������Ŀ¼)  
	    File[] files = dirFile.listFiles();  
	    for (int i = 0; i < files.length; i++) {  
	        //ɾ�����ļ�  
	        if (files[i].isFile()) {  
	            flag = deleteFile(files[i].getAbsolutePath());  
	            if (!flag) break;  
	        } //ɾ����Ŀ¼  
	        else {  
	            flag = deleteDirectory(files[i].getAbsolutePath());  
	            if (!flag) break;  
	        }  
	    }  
	    if (!flag) return false;  
	    //ɾ����ǰĿ¼  
	    if (dirFile.delete()) {  
	        return true;  
	    } else {  
	        return false;  
	    }  
	}  

}
