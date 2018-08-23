package com.djyos.dide.ui.wizards.cpu.configDialogs;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.Platform;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.djyos.dide.ui.wizards.cpu.Cpu;
import com.djyos.dide.ui.wizards.cpu.core.Core;
import com.djyos.dide.ui.wizards.cpu.core.memory.CoreMemory;

import org.eclipse.cdt.utils.ui.controls.ControlFactory;

import com.djyos.dide.ui.wizards.djyosProject.tools.DPluginImages;

public class ResetConfigurationDialog extends StatusDialog{

	private Tree cpuConfigTree;
	private Tree memoryTree;
	private Group contentGroup;
	private ScrolledComposite scrolledComposite;
	private Composite configContent;
	private Combo memoryTypeCombo;
	private Text addrField;
	private Text sizeField;
	private Cpu newCpu = new Cpu();
	private Core newCore = new Core();
	private String curPath = null;
	private Text CpuNameField;
	private String eclipsePath = getEclipsePath();	
	private List<String> configsList = new ArrayList<String>();
	private List<String> attributes = new ArrayList<String>();
	private List<String> firewareLibs = new ArrayList<String>();
	private boolean isCpu = true;
	
	public String getEclipsePath() {
		String fullPath = Platform.getInstallLocation().getURL().toString();
		String eclipsePath = fullPath.substring(6,(fullPath.substring(0,fullPath.length()-1)).lastIndexOf("/")+1);
		return eclipsePath;
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
	
	public ResetConfigurationDialog(Shell parent,List<String> configs,Cpu cpu,String curFilePath,boolean isGroup) {
		super(parent);
		// TODO Auto-generated constructor stub
		setTitle("重设配置");
		attributes = configs;
		if(cpu.getCoreNum()!=0) {
			newCpu.setCoreNum(cpu.getCoreNum());
			for(int i=0;i<cpu.getCoreNum();i++) {
				newCpu.getCores().add(new Core());
			}		
		}
		curPath = curFilePath;
		if(isGroup) {
			isCpu = false;
		}
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX );
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(parent,SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite cpuNameCpt = new Composite(composite,SWT.NONE);
		GridLayout cpuNameLayout = new GridLayout();
		cpuNameLayout.numColumns = 2;
		cpuNameLayout.marginHeight=20;
		cpuNameCpt.setLayout(cpuNameLayout);
		cpuNameCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label nameLabel = new Label(cpuNameCpt,SWT.NONE);
		nameLabel.setText("Cpu名称: ");
		CpuNameField = new Text(cpuNameCpt,SWT.BORDER);
		CpuNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Composite cpuConfigCpt = new Composite(composite, SWT.NULL);
		GridLayout layoutAttributes = new GridLayout();
		layoutAttributes.numColumns = 2;
		layoutAttributes.marginHeight = 5;
		cpuConfigCpt.setLayout(layoutAttributes);
		cpuConfigCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite cpuConfigListCpt = new Composite(cpuConfigCpt, SWT.NULL);
		cpuConfigTree = new Tree(cpuConfigListCpt, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		cpuConfigTree.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		cpuConfigTree.setHeaderVisible(true);
		
		contentGroup = ControlFactory.createGroup(cpuConfigCpt, "分组配置", 1);
		contentGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		contentGroup.setLayout(new GridLayout());
		
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
		
		cpuConfigTree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeItem[] items = cpuConfigTree.getSelection();
				if (items.length > 0) {
					String selectConfigName = items[0].getText();
					
//					if(! attributes.contains(selectConfigName)) {//如果selectConfigName不在attributes中，说明selectConfigName已经配置了
//						IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow(); 
//						MessageDialog.openInformation(window.getShell(), "提示", "父目录已配置该选项，如果继续配置，将会覆盖父目录的配置");
//					}
					contentGroup.setText(items[0].getText());
					scrolledComposite.dispose();
					switch (selectConfigName) {
					case "内核个数":
						 creatCoreNumContent(contentGroup);
						break;
					case "内核配置":
						creatCoreContent(contentGroup);
						break;
					case "存储配置":
						creatMemoryContent(contentGroup);
						break;
					case "浮点配置":
						creatFloatContent(contentGroup);
						break;
					case "复位配置":
						creatResetContent(contentGroup);
						break;
					case "固件库":
						creatFirmwareLibContent(contentGroup);
						break;
					}		
				}
			}
		});
		final TreeColumn columnGroupList = new TreeColumn(cpuConfigTree, SWT.NONE);
		columnGroupList.setText("Cpu配置项");
		columnGroupList.setWidth(90);
		columnGroupList.setResizable(false);
		columnGroupList.setToolTipText("Cpu Attributes");
		cpuConfigTree.setSize(120, 220);
		List<String> cons = new ArrayList<String>();
		cons.add("内核个数");
		cons.add("复位配置");
		cons.add("浮点配置");
		cons.add("内核配置");
		cons.add("存储配置");
		cons.add("固件库");
		//之前是attributes
		for(int i=0;i<cons.size();i++) {
			configsList.add(cons.get(i));
			TreeItem t = new TreeItem(cpuConfigTree, SWT.NONE);
			t.setText(cons.get(i));

			if(! attributes.contains(cons.get(i))) {
				t.setImage(DPluginImages.CFG_DONE_VIEW.createImage());
			}
			
		}		
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
		nameLabel.setText("固件库选择: ");
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
		
		if(newCpu.getFirmwareLib()!=null) {
			String firmwareLib = newCpu.getFirmwareLib();
			for(int i=0;i<firewareLibs.size();i++) {
				System.out.println(firewareLibs.get(i));
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
		contentGroup.layout();
		
	}

	protected void creatResetContent(Group contentGroup2) {
		// TODO Auto-generated method stub
		scrolledComposite = new ScrolledComposite(contentGroup, SWT.V_SCROLL
                | SWT.H_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		configContent = new Composite(scrolledComposite,SWT.NONE);
		configContent.setLayout(new GridLayout());
		configContent.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite coreSelectCpt = new Composite(configContent,SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		coreSelectCpt.setLayout(layout);
		coreSelectCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label nameLabel = new Label(coreSelectCpt,SWT.NONE);
		nameLabel.setText("内核选择: ");
		Combo numCombo = new Combo(coreSelectCpt,SWT.READ_ONLY);
		for(int i=0;i<newCpu.getCoreNum();i++) {
			numCombo.add("内核"+(i+1));
		}
		numCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Group resetGroup = ControlFactory.createGroup(configContent, "配置复位地址", 1);
		resetGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		resetGroup.setLayout(new GridLayout());
		
		Composite coreConfigCpt = new Composite(resetGroup,SWT.NONE);
		GridLayout coreLayout = new GridLayout();
		coreLayout.numColumns = 2;
		coreConfigCpt.setLayout(coreLayout);
		coreConfigCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label abiLabel = new Label(coreConfigCpt,SWT.NONE);
		abiLabel.setText("复位地址： ");
		Text addrText = new Text(coreConfigCpt,SWT.BORDER);
		addrText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		if(newCpu.getCores().size()==0) {
			coreSelectCpt.setVisible(false);
			if(newCore.getResetAddr()!=null) {
				addrText.setText(newCore.getResetAddr());
			}
		}else {
			if(newCpu.getCores().get(0).getResetAddr()!=null) {
				numCombo.select(0);
				addrText.setText(newCpu.getCores().get(0).getResetAddr());
			}
		}
		numCombo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int selectIndex = numCombo.getSelectionIndex();
				String addr = newCpu.getCores().get(selectIndex).getResetAddr();
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
		contentGroup.layout();
	}

	protected void creatFloatContent(Group contentGroup2) {
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
		nameLabel.setText("内核选择: ");
		Combo numCombo = new Combo(coreSelectCpt, SWT.READ_ONLY);
		for (int i = 0; i < newCpu.getCoreNum(); i++) {
			numCombo.add("内核" + (i + 1));
		}
		numCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Group floatGroup = ControlFactory.createGroup(configContent, "配置浮点", 1);
		floatGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		floatGroup.setLayout(new GridLayout());
		
		Composite coreConfigCpt = new Composite(floatGroup,SWT.NONE);
		GridLayout coreLayout = new GridLayout();
		coreLayout.numColumns = 2;
		coreConfigCpt.setLayout(coreLayout);
		coreConfigCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label abiLabel = new Label(coreConfigCpt,SWT.NONE);
		abiLabel.setText("Float ABI： ");
		Combo abiCombo = new Combo(coreConfigCpt,SWT.READ_ONLY);
		String[] abis = {"Toolchain default","Library(Soft)","FP instructions(hard)","Library with FP(softfp)"};
		abiCombo.setItems(abis);
		abiCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label fpuTypeLabel = new Label(coreConfigCpt,SWT.NONE);
		fpuTypeLabel.setText("FPU Type： ");
		Combo fpuTypeCombo = new Combo(coreConfigCpt,SWT.READ_ONLY);
		String[] fpuTypes = {"Toolchain default","crypto-neon-fp-armv8","fpv4-sp-d16","fpv5-d16","fpv5-sp-d16","vfpv4","vfpv4-d16"};
		fpuTypeCombo.setItems(fpuTypes);
		fpuTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		if (newCpu.getCores().size() == 0) {
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
				if(newCore.getFloatABI().equals("Library(Soft)")) {
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
		}else {
			List<Core> cores = newCpu.getCores();
			if(cores.get(0).getFloatABI()!=null) {
				numCombo.select(0);
				for(int i=0;i<abis.length;i++) {
					if(cores.get(0).getFloatABI().equals(abis[i])) {
						abiCombo.select(i);
						break;
					}
				}
			}
			if(cores.get(0).getFpuType()!=null) {
				if(cores.get(0).getFloatABI().equals("Library(Soft)")) {
					fpuTypeCombo.select(0);
					fpuTypeCombo.setEnabled(false);
				}else {
					for(int i=0;i<fpuTypes.length;i++) {
						if(cores.get(0).getFpuType().equals(fpuTypes[i])) {
							fpuTypeCombo.select(i);
							break;
						}
					}
				}
			
			}
		}
		
		numCombo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int selectIndex = numCombo.getSelectionIndex();
				String fpuType = newCpu.getCores().get(selectIndex).getFpuType();
				if(fpuType!=null) {
					for(int i=0;i<fpuTypes.length;i++) {
						if(fpuType.equals(fpuTypes[i])) {
							fpuTypeCombo.select(i);
						}
					}
				}else {
					abiCombo.deselectAll();
					fpuTypeCombo.deselectAll();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		abiCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				String floatABI = abiCombo.getText();
				if(newCpu.getCores().size()!=0) {
					int selectIndex = numCombo.getSelectionIndex();
					newCpu.getCores().get(selectIndex).setFloatABI(floatABI);
				}else {
					newCore.setFloatABI(floatABI);
				}
				if(floatABI.equals("Library(Soft)")) {
					fpuTypeCombo.select(0);
					fpuTypeCombo.setEnabled(false);
				}else {
					fpuTypeCombo.setEnabled(true);
				}
				super.widgetSelected(e);
			}
		});
		fpuTypeCombo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				String fpuType = fpuTypeCombo.getText();
				if(newCpu.getCores().size()!=0) {
					int selectIndex = numCombo.getSelectionIndex();
					newCpu.getCores().get(selectIndex).setFpuType(fpuType);
				}else {
					newCore.setFpuType(fpuType);
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
		contentGroup.layout();
	}

	protected void creatMemoryContent(Group contentGroup2) {
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
		nameLabel.setText("内核选择: ");
		Combo numCombo = new Combo(coreSelectCpt, SWT.READ_ONLY);
		for (int i = 0; i < newCpu.getCoreNum(); i++) {
			numCombo.add("内核" + (i + 1));
		}
		numCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Group memoryGroup = ControlFactory.createGroup(configContent, "配置内存", 1);
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
		columnMemory.setText("片内存储");
		columnMemory.setWidth(120);
		columnMemory.setResizable(false);
		columnMemory.setToolTipText("Inner Memory");
		
		Composite btnCpt = new Composite(memoryComposite, SWT.NONE);
		btnCpt.setLayout(new GridLayout(2,true));
		btnCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_CENTER));
		Button addBtn = new Button(btnCpt,SWT.PUSH);
		addBtn.setText("添加");
		addBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int selectIndex = numCombo.getSelectionIndex();
				
				int memoryCount = memoryTree.getItemCount();//memoryTree的孩子数量
				TreeItem t = new TreeItem(memoryTree, SWT.NONE);//添加新的内存
				TreeItem[] items = memoryTree.getItems();
				int max = 0;
				if (memoryCount > 0) {
					 String maxString = items[memoryCount-1].getText();
					 max = Integer.parseInt(maxString.substring(6,maxString.length()));
				}
				t.setText("memory"+(max+1));
				CoreMemory memory = new CoreMemory();
				memory.setName("memory"+(max+1));
				newCpu.getCores().get(selectIndex).getMemorys().add(memory);
			
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		Button deleteBtn = new Button(btnCpt,SWT.PUSH);
		deleteBtn.setText("删除");
		deleteBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int selectIndex = numCombo.getSelectionIndex();
				TreeItem[] items = memoryTree.getSelection();
				if (items.length > 0) {
					for(int i=0;i<newCpu.getCores().get(selectIndex).getMemorys().size();i++) {
						if(newCpu.getCores().get(selectIndex).getMemorys().get(i).getName().equals(items[0].getText().trim())) {
							newCpu.getCores().get(selectIndex).getMemorys().remove(i);
						}
					}
					items[0].dispose();
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
		detailsLayout.numColumns = 3;
		memoryContentCpt.setLayout(detailsLayout);
		memoryContentCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		Label typeLabel = new Label(memoryContentCpt,SWT.NONE);
		typeLabel.setText("类型: ");

		memoryTypeCombo = new Combo(memoryContentCpt,SWT.READ_ONLY);
		memoryTypeCombo.add("ROM");
		memoryTypeCombo.add("RAM");
		memoryTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label newLabel = new Label(memoryContentCpt,SWT.NONE);
		newLabel.setVisible(false);
		memoryTypeCombo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int selectIndex = numCombo.getSelectionIndex();
				List<CoreMemory> memorys = newCpu.getCores().get(selectIndex).getMemorys();//获取当前内核所有内存
				int comboIndex = memoryTypeCombo.getSelectionIndex();
				TreeItem[] items = memoryTree.getSelection();
				if(items.length>0) {
					String selectMemory = items[0].getText().trim();//选中的内存
					for(int i=0;i<memorys.size();i++) {
						CoreMemory memory = memorys.get(i);
						if(memory.getName().equals(selectMemory)) {
							String type = memoryTypeCombo.getItem(comboIndex);
							memory.setType(type);
							break;
						}
					}
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		Label addrLabel = new Label(memoryContentCpt,SWT.NONE);
		addrLabel.setText("地址: ");
		addrField = new Text(memoryContentCpt,SWT.BORDER);
		addrField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		newLabel = new Label(memoryContentCpt,SWT.NONE);
		newLabel.setVisible(false);	
		addrField.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				// TODO Auto-generated method stub
				int selectIndex = numCombo.getSelectionIndex();
				List<CoreMemory> memorys = newCpu.getCores().get(selectIndex).getMemorys();//获取当前内核所有内存
				TreeItem[] items = memoryTree.getSelection();
				if (items.length > 0) {
					String selectMemory = items[0].getText().trim();
					for (int i = 0; i < memorys.size(); i++) {
						CoreMemory memory = memorys.get(i);
						if (memory.getName().equals(selectMemory)) {
							String addr = addrField.getText().trim();
							memory.setStartAddr(addr);
							break;
						}
					}
				}

			}
		});
		
		Label sizeLabel = new Label(memoryContentCpt,SWT.NONE);
		sizeLabel.setText("大小: ");
		sizeField = new Text(memoryContentCpt,SWT.BORDER);
		sizeField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		ControlFactory.createLabel(memoryContentCpt, "K");
		sizeField.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				// TODO Auto-generated method stub
				int selectIndex = numCombo.getSelectionIndex();
				List<CoreMemory> memorys = newCpu.getCores().get(selectIndex).getMemorys();//获取当前内核所有内存
				TreeItem[] items = memoryTree.getSelection();
				if (items.length > 0) {
					String selectMemory = items[0].getText().trim();
					String size = sizeField.getText().trim();
					if (!size.equals("")) {
						for (int i = 0; i < memorys.size(); i++) {
							CoreMemory memory = memorys.get(i);
							if (memory.getName().equals(selectMemory)) {
								memory.setSize(size);
								break;
							}
						}
					}

				}
			}
		});
		//选择内核的监听事件
		numCombo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int selectIndex = numCombo.getSelectionIndex();
				List<CoreMemory> memorys = newCpu.getCores().get(selectIndex).getMemorys();
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
				TreeItem[] items = memoryTree.getSelection();
				int selectIndex = numCombo.getSelectionIndex();
				List<CoreMemory> memorys = newCpu.getCores().get(selectIndex).getMemorys();
				if (items.length > 0) {
					String selectMemoryName = items[0].getText();
					boolean memoryExist = false;
					for(int i=0;i<memorys.size();i++) {
						String memoryName = memorys.get(i).getName();
						if(memoryName.equals(selectMemoryName)) {
							if(memorys.get(i).getType()!=null) {
								if(memorys.get(i).getType().equals("ROM")) {
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
								sizeField.setText(memorys.get(i).getSize());
							}else {
								sizeField.setText("");
							}	
							memoryExist = true;
							break;
						}
					}	
					if(!memoryExist) {
						memoryTypeCombo.deselectAll();
						addrField.setText("");
						sizeField.setText("");
					}
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		if (newCpu.getCores().size() == 0) {
			coreSelectCpt.setVisible(false);
			List<CoreMemory> memorys = newCore.getMemorys();
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
			List<Core> cores = newCpu.getCores();
			if(newCpu.getCores().get(0).getMemorys().size()!=0){
				numCombo.select(0);
				List<CoreMemory> memorys = newCpu.getCores().get(0).getMemorys();
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
		
		Point point = configContent.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		scrolledComposite.setContent(configContent);
		scrolledComposite.setMinHeight(point.y);
		scrolledComposite.setExpandHorizontal(true);
	    scrolledComposite.setExpandVertical(true);
		contentGroup.layout();
	}

	protected void creatCoreContent(Group contentGroup2) {
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
		nameLabel.setText("内核选择: ");
		Combo numCombo = new Combo(coreSelectCpt, SWT.READ_ONLY);
		for (int i = 0; i < newCpu.getCoreNum(); i++) {
			numCombo.add("内核" + (i + 1));
		}
		numCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Group coreGroup = ControlFactory.createGroup(configContent, "配置内核", 1);
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
		String[] types = new String[files.length];
		for(int i=0;i<files.length;i++) {
			types[i] = files[i].getName();
		}
		
		Label typeLabel = new Label(coreConfigCpt,SWT.NONE);
		typeLabel.setText("类型： ");
		Combo typeCombo = new Combo(coreConfigCpt,SWT.READ_ONLY);
		typeCombo.setItems(types);
		typeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
		
		Label archLabel = new Label(coreConfigCpt,SWT.NONE);
		archLabel.setText("架构： ");
		Combo archCombo = new Combo(coreConfigCpt,SWT.READ_ONLY);
		archCombo.setEnabled(false);
		archCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		//类型下拉框选中的事件处理
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
		familyLabel.setText("家族： ");
		Combo familyCombo = new Combo(coreConfigCpt,SWT.READ_ONLY);
		familyCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		familyCombo.setEnabled(false);
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
				}else {
					newCore.setFamily(family);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		if (newCpu.getCores().size() == 0) {
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
				archCombo.setEnabled(true);
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
			}
			if(newCore.getFamily()!=null) {
				familyCombo.setEnabled(true);
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
					if(newCore.getArch().equals(familys[i])) {
						familyCombo.select(i);
						break;
					}
				}
			}
		}else {
			List<Core> cores = newCpu.getCores();
			if(cores.get(0).getType()!=null) {
				numCombo.select(0);
				for(int i=0;i<types.length;i++) {
					if(cores.get(0).getType().equals(types[i])) {
						typeCombo.select(i);
						break;
					}
				}
			}else {
				typeCombo.setEnabled(false);
			}
			if (cores.get(0).getArch() != null) {
				archCombo.setEnabled(true);
				String type = typeCombo.getText().trim();
				String archPath = typePath+"/"+type;
				File archFile = new File(archPath);
				File[] archFiles =  archFile.listFiles(new MyFileter());
				String[] archs = new String[archFiles.length];
				for(int i=0;i<archFiles.length;i++) {
					archs[i] = archFiles[i].getName();
				}
				archCombo.setItems(archs);
				for(int i=0;i<archs.length;i++) {
					if(cores.get(0).getArch().equals(archs[i])) {
						archCombo.select(i);
						break;
					}
				}
			}else {
				archCombo.setEnabled(false);
			}
			if(cores.get(0).getFamily()!=null) {
				familyCombo.setEnabled(true);
				String type = typeCombo.getText().trim();
				String arch = archCombo.getText().trim();
				String familyPath = typePath+"/"+type+"/"+arch;
				File familyFile = new File(familyPath);
				File[] familyFiles =  familyFile.listFiles(new MyFileter());
				String[] familys = new String[familyFiles.length];
				for(int i=0;i<familyFiles.length;i++) {
					familys[i] = familyFiles[i].getName();
				}
				familyCombo.setItems(familys);
				for(int i=0;i<familys.length;i++) {
					if(cores.get(0).getFamily().equals(familys[i])) {
						familyCombo.select(i);
						break;
					}
				}
			}else {
				familyCombo.setEnabled(false);
			}
			
		}
		
		numCombo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				typeCombo.setEnabled(true);
				int selectIndex = numCombo.getSelectionIndex();
				String type = newCpu.getCores().get(selectIndex).getType();
				String arch = newCpu.getCores().get(selectIndex).getArch();
				String family = newCpu.getCores().get(selectIndex).getFamily();
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
					for (int i = 0; i < archs.length; i++) {
						if (arch.equals(archs[i])) {
							archCombo.select(i);
							break;
						}
					}
				}else {
					archCombo.deselectAll();
					archCombo.setEnabled(false);
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
					for (int i = 0; i < familys.length; i++) {
						if (family.equals(familys[i])) {
							familyCombo.select(i);
							break;
						}
					}
				}else {
					familyCombo.deselectAll();
					familyCombo.setEnabled(false);
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
		contentGroup.layout();
	}

	protected void creatCoreNumContent(Group contentGroup2) {
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
		nameLabel.setText("内核个数: ");
		Combo numCombo = new Combo(configContent,SWT.READ_ONLY);
		numCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		String[] items = {"1","2","3","4","5"};
		numCombo.setItems(items);
		if(newCpu.getCoreNum()!=0){
			int coreNum = newCpu.getCoreNum();
			numCombo.select(coreNum-1);
		}
		numCombo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int coreNum = Integer.parseInt(numCombo.getText());
				newCpu = new Cpu();
				newCpu.setCoreNum(coreNum);
				for(int i=0;i<coreNum;i++) {
					newCpu.getCores().add(new Core());
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
		contentGroup.layout();
	}

	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		if(isCpu) {
			List<String> cpuPieceNames = null;
			String completeName = "";
			String cpuName = CpuNameField.getText();
			curPath = curPath+"/"+cpuName;
			File newCpuFile = new File(curPath);
			newCpuFile.mkdir();
			List<String> names = new ArrayList<String>();
			getGroupNames(newCpuFile,names);
			for(int i=names.size()-1;i>=0;i--) {
				completeName+=names.get(i);
			}
			
			File xmlFile = new File(curPath+"/cpu_"+completeName+".xml");
			if(! xmlFile.exists()) {
				try {
					xmlFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			createXml(newCpu,xmlFile,completeName);
		}else {
			String groupName = CpuNameField.getText();
			String completeName = "";
			curPath = curPath+"/"+groupName;
			File newGroupFile = new File(curPath); 
			newGroupFile.mkdir();
			List<String> names = new ArrayList<String>();
			getGroupNames(newGroupFile,names);
			for(int i=names.size()-1;i>=0;i--) {
				completeName+=names.get(i);
			}
			File xmlFile = new File(curPath+"/cpu_group_"+completeName+".xml");
			if(! xmlFile.exists()) {
				try {
					xmlFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(newCpu.getCores().size()!=0) {
				createNewCpuXml(newCpu,xmlFile);
			}else {
				createPublicXml(newCore,xmlFile);
			}
		}
		
		super.okPressed();
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
		return new Point(520,420);
	}
	
	private void createXml(Cpu cpu,File file,String completeName) {
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
					Element memory = document.createElement("memory");
					for(int j=0;j<curCore.getMemorys().size();j++) {
						CoreMemory curMemory = curCore.getMemorys().get(j);
						
						Element type = document.createElement("type");
						type.setTextContent(curMemory.getType());
						memory.appendChild(type);
						Element startAddr = document.createElement("startAddr");
						startAddr.setTextContent(curMemory.getStartAddr());
						memory.appendChild(startAddr);
						Element size = document.createElement("size");
						size.setTextContent(String.valueOf(curMemory.getSize()));
						memory.appendChild(size);
						
					}
					core.appendChild(memory);
				}
				
				cpuElement.appendChild(core);
			}
			
			document.appendChild(cpuElement);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");// 增加换行缩进，但此时缩进默认为0
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");// 设置缩进为2
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

	private void createNewCpuXml(Cpu cpu,File file) {
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
					Element memory = document.createElement("memory");
					for(int j=0;j<curCore.getMemorys().size();j++) {
						CoreMemory curMemory = curCore.getMemorys().get(j);
						
						Element type = document.createElement("type");
						type.setTextContent(curMemory.getType());
						memory.appendChild(type);
						Element startAddr = document.createElement("startAddr");
						startAddr.setTextContent(curMemory.getStartAddr());
						memory.appendChild(startAddr);
						Element size = document.createElement("size");
						size.setTextContent(String.valueOf(curMemory.getSize()));
						memory.appendChild(size);
						
					}
					core.appendChild(memory);
				}
				
				cpuElement.appendChild(core);
			}
			
			document.appendChild(cpuElement);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");// 增加换行缩进，但此时缩进默认为0
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");// 设置缩进为2
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
//			if (curCore.getMemorys().size() != 0) {
//				Element memory = document.createElement("memory");
//				for (int j = 0; j < curCore.getMemorys().size(); j++) {
//					Memory curMemory = curCore.getMemorys().get(j);
//
//					Element type = document.createElement("type");
//					type.setTextContent(curMemory.getType());
//					memory.appendChild(type);
//					Element startAddr = document.createElement("startAddr");
//					startAddr.setTextContent(curMemory.getStartAddr());
//					memory.appendChild(startAddr);
//					Element size = document.createElement("size");
//					size.setTextContent(String.valueOf(curMemory.getSize()));
//					memory.appendChild(size);
//
//				}
//				core.appendChild(memory);
//			}

			document.appendChild(cpuElement);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");// 增加换行缩进，但此时缩进默认为0
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");// 设置缩进为2
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
	
	
}
