package com.djyos.dide.ui.wizards.cpu;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.utils.ui.controls.ControlFactory;
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

import com.djyos.dide.ui.arch.Arch;
import com.djyos.dide.ui.arch.ReadArchXml;
import com.djyos.dide.ui.wizards.cpu.core.Core;
import com.djyos.dide.ui.wizards.cpu.core.memory.CoreMemory;
import com.djyos.dide.ui.wizards.djyosProject.tools.DPluginImages;
import com.djyos.dide.ui.wizards.djyosProject.tools.DideHelper;

public class NewGroupOrCpuDialog extends StatusDialog {

	private Tree cpuGroupTree, memoryTree, archTree;
	private Composite configContent;
	private Combo memoryTypeCombo;
	private Text addrField, sizeField;
	private Group contentGroup;
	private ScrolledComposite scrolledComposite;
	private Cpu newCpu = new Cpu(), parentCpu = new Cpu(), curCpu = new Cpu(), revisingCpu = new Cpu();
	private Core newCore = new Core();
	private Text groupNameField;
	private String curPath = null, groupName, cpuName, cpuTag = null;
	private List<String> attributes = new ArrayList<String>(), firewareLibs = new ArrayList<String>();
	private Combo numCombo1, numCombo2, numCombo3, numCombo4;
	private Label memorySizeLabel, memoryAddrLabel, memoryTypeLabel;
	private String tempName = null;
	private DideHelper dideHelper = new DideHelper();
	private List<TreeItem> archItems;
	private File configFile;
	private String didePath = dideHelper.getDIDEPath();
	private IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

	private void setMemoryCpt(boolean isEnable) {
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

	public NewGroupOrCpuDialog(Shell parent, List<String> configs, Cpu cpu, String curFilePath, String tag) {
		super(parent);
		attributes = configs;
		cpuTag = tag;

		if (cpu.getCoreNum() != 0) {
			parentCpu.setCoreNum(cpu.getCoreNum());
			parentCpu.setCores(cpu.getCores());
			curCpu = parentCpu;
			newCpu = new Cpu();
			newCpu.setCoreNum(cpu.getCoreNum());
			for (int i = 0; i < cpu.getCoreNum(); i++) {
				newCpu.getCores().add(new Core());
			}
		} else {
			// Core onlyCore =
			// if() {
			//
			// }
		}
		curPath = curFilePath;

		if (tag.equals("group")) {
			setTitle("新建分组");
		} else if (tag.equals("cpu")) {
			setTitle("新建Cpu");
		} else if (tag.startsWith("revise")) {
			if (tag.endsWith("group")) {
				setTitle("修改子目录配置");
			} else if (tag.endsWith("cpu")) {
				setTitle("修改Cpu配置");
			}
			ReadCpuXml rcx = new ReadCpuXml();
			File curFile = new File(curPath);
			configFile = dideHelper.getXmlFile(curFile);
			try {
				if (configFile != null) {
					revisingCpu = rcx.unitCpu(revisingCpu, configFile);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tempName = curPath.substring(curPath.lastIndexOf("\\") + 1, curPath.length());
		}

		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite groupNameCpt = new Composite(composite, SWT.NONE);
		GridLayout groupNameLayout = new GridLayout();
		groupNameLayout.numColumns = 2;
		groupNameLayout.marginHeight = 20;
		groupNameCpt.setLayout(groupNameLayout);
		groupNameCpt.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label nameLabel = new Label(groupNameCpt, SWT.NONE);
		groupNameField = new Text(groupNameCpt, SWT.BORDER);
		groupNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		if (cpuTag.equals("group")) {
			nameLabel.setText("子目录名称: ");
		} else if (cpuTag.equals("cpu")) {
			nameLabel.setText("Cpu名称: ");
		} else if (cpuTag.startsWith("revise")) {
			if (cpuTag.endsWith("cpu")) {
				nameLabel.setText("Cpu名称: ");
			} else if (cpuTag.endsWith("group")) {
				nameLabel.setText("子目录名称: ");
			}
			groupNameField.setText(tempName);
		}

		Composite cpuGroupCpt = new Composite(composite, SWT.NULL);
		GridLayout layoutAttributes = new GridLayout();
		layoutAttributes.numColumns = 2;
		cpuGroupCpt.setLayout(layoutAttributes);
		cpuGroupCpt.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite cpuGroupListCpt = new Composite(cpuGroupCpt, SWT.NULL);
		cpuGroupTree = new Tree(cpuGroupListCpt, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		cpuGroupTree.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		cpuGroupTree.setHeaderVisible(true);
		final TreeColumn columnGroupList = new TreeColumn(cpuGroupTree, SWT.NONE);
		columnGroupList.setText("Cpu配置项");
		columnGroupList.setWidth(140);
		columnGroupList.setResizable(false);
		columnGroupList.setToolTipText("Cpu Attributes");
		columnGroupList.setImage(DPluginImages.CFG_CPMT_OBJ.createImage());
		cpuGroupTree.setSize(150, 250);
		List<String> cons = new ArrayList<String>();
		cons.add("内核个数");
		cons.add("复位配置");
		// 如果当前的内核个数不为0，且当前cpu内核的浮点不为空，则添加浮点配置项
		if (curCpu.getCores().size() > 0) {
			boolean fpNeed = dideHelper.isFputypeuNeed(curCpu.getCores().get(0));
			if (fpNeed) {
				cons.add("浮点配置");
			}
		}

		cons.add("内核配置");
		cons.add("存储配置");
		// cons.add("固件库");
		// 之前是attributes
		for (int i = 0; i < cons.size(); i++) {
			TreeItem t = new TreeItem(cpuGroupTree, SWT.NONE);
			t.setText(cons.get(i));
			if (!attributes.contains(cons.get(i))) {
				t.setImage(DPluginImages.CFG_DONE_VIEW.createImage());
			}
		}

		contentGroup = ControlFactory.createGroup(cpuGroupCpt, "分组配置", 1);
		contentGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		contentGroup.setLayout(new GridLayout(1, true));

		scrolledComposite = new ScrolledComposite(contentGroup, SWT.V_SCROLL | SWT.H_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		configContent = new Composite(scrolledComposite, SWT.NONE);
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

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		return super.createDialogArea(parent);
	}

	protected void creatFirmwareLibContent(Group contentGroup2) {
		// TODO Auto-generated method stub
		scrolledComposite = new ScrolledComposite(contentGroup, SWT.V_SCROLL | SWT.H_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		configContent = new Composite(scrolledComposite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		configContent.setLayout(layout);
		configContent.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label nameLabel = new Label(configContent, SWT.NONE);
		nameLabel.setText("固件库选择: ");
		Combo firmwareCombo = new Combo(configContent, SWT.READ_ONLY);

		File firewareFile = new File(dideHelper.getDIDEPath() + "djysrc\\third\\firmware");
		File[] files = firewareFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			firewareLibs.add(files[i].getName());
		}
		for (int i = 0; i < firewareLibs.size(); i++) {
			firmwareCombo.add(firewareLibs.get(i));
		}
		firmwareCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		if (parentCpu.getFirmwareLib() != null) {
			String firmwareLib = parentCpu.getFirmwareLib();
			for (int i = 0; i < firewareLibs.size(); i++) {
				if (firmwareLib.equals(firewareLibs.get(i))) {
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
				if (tempName != null) {
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
		scrolledComposite = new ScrolledComposite(contentGroup, SWT.V_SCROLL | SWT.H_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		configContent = new Composite(scrolledComposite, SWT.NONE);
		configContent.setLayout(new GridLayout());
		configContent.setLayoutData(new GridData(GridData.FILL_BOTH));

		// if(haveCore) {
		// curCpu = parentCpu;
		// }else {
		// curCpu = newCpu;
		// }
		//
		Composite coreSelectCpt = new Composite(configContent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		coreSelectCpt.setLayout(layout);
		coreSelectCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label nameLabel = new Label(coreSelectCpt, SWT.NONE);
		nameLabel.setText("内核选择: ");
		Combo numCombo = new Combo(coreSelectCpt, SWT.READ_ONLY);
		for (int i = 0; i < curCpu.getCoreNum(); i++) {
			numCombo.add("内核" + (i + 1));
		}
		numCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Group resetGroup = ControlFactory.createGroup(configContent, "配置复位地址", 1);
		resetGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		resetGroup.setLayout(new GridLayout());

		Composite coreConfigCpt = new Composite(resetGroup, SWT.NONE);
		GridLayout coreLayout = new GridLayout();
		coreLayout.numColumns = 2;
		coreConfigCpt.setLayout(coreLayout);
		coreConfigCpt.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label abiLabel = new Label(coreConfigCpt, SWT.NONE);
		abiLabel.setText("复位地址： ");
		Text addrText = new Text(coreConfigCpt, SWT.BORDER);
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

		// if(parentCpu.getCores().size()==0) {
		// coreSelectCpt.setVisible(false);
		// if(newCore.getResetAddr()!=null) {
		// addrText.setText(newCore.getResetAddr());
		// }
		// }else {
		// if(parentCpu.getCores().get(0).getResetAddr()!=null) {
		// numCombo.select(0);
		// addrText.setText(parentCpu.getCores().get(0).getResetAddr());
		// }
		// }
		numCombo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int selectIndex = numCombo.getSelectionIndex();
				String addr = curCpu.getCores().get(selectIndex).getResetAddr();
				if (addr != null) {
					addrText.setText(addr);
				} else {
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
				if (newCpu.getCores().size() != 0) {
					int selectIndex = numCombo.getSelectionIndex();
					newCpu.getCores().get(selectIndex).setResetAddr(resetAddr);
					if (tempName != null) {
						revisingCpu.getCores().get(selectIndex).setResetAddr(resetAddr);
					}
					curCpu.getCores().get(selectIndex).setResetAddr(resetAddr);
				} else {
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
		scrolledComposite = new ScrolledComposite(contentGroup, SWT.V_SCROLL | SWT.H_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		configContent = new Composite(scrolledComposite, SWT.NONE);
		configContent.setLayout(new GridLayout());
		configContent.setLayoutData(new GridData(GridData.FILL_BOTH));
		// 内核选择界面
		Composite coreSelectCpt = new Composite(configContent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		coreSelectCpt.setLayout(layout);
		coreSelectCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label nameLabel = new Label(coreSelectCpt, SWT.NONE);
		nameLabel.setText("内核选择: ");
		Combo numCombo = new Combo(coreSelectCpt, SWT.READ_ONLY);
		for (int i = 0; i < curCpu.getCoreNum(); i++) {
			numCombo.add("内核" + (i + 1));
		}
		numCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// 配置浮点界面
		Group floatGroup = ControlFactory.createGroup(configContent, "配置浮点", 1);
		floatGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		floatGroup.setLayout(new GridLayout());

		Composite coreConfigCpt = new Composite(floatGroup, SWT.NONE);
		GridLayout coreLayout = new GridLayout();
		coreLayout.numColumns = 2;
		coreConfigCpt.setLayout(coreLayout);
		coreConfigCpt.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Label abiLabel = new Label(coreConfigCpt,SWT.NONE);
		// abiLabel.setText("Float ABI： ");
		// Combo abiCombo = new Combo(coreConfigCpt,SWT.READ_ONLY);
		// String[] abis = {"Toolchain default","Library(soft)","FP
		// instructions(hard)","Library with FP(softfp)"};
		// abiCombo.setItems(abis);
		// abiCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label fpuTypeLabel = new Label(coreConfigCpt, SWT.NONE);
		fpuTypeLabel.setText("FPU Type： ");
		Combo fpuTypeCombo = new Combo(coreConfigCpt, SWT.READ_ONLY);
		String[] fpuTypes = { "Toolchain default", "fpv5-d16", "fpv5-sp-d16", "fpv4-sp-d16", "fpv4-d16" };
		fpuTypeCombo.setItems(fpuTypes);
		fpuTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// 内核个数为0时
		if (curCpu.getCores().size() == 0) {
			coreSelectCpt.setVisible(false);
			// if(newCore.getFloatABI()!=null) {
			// for(int i=0;i<abis.length;i++) {
			// if(newCore.getFloatABI().equals(abis[i])) {
			// abiCombo.select(i);
			// break;
			// }
			// }
			// }
			if (newCore.getFpuType() != null) {
				if (newCore.getFloatABI().equals("Library(soft)")) {
					fpuTypeCombo.select(0);
					fpuTypeCombo.setEnabled(false);
				} else {
					for (int i = 0; i < fpuTypes.length; i++) {
						if (newCore.getFpuType().equals(fpuTypes[i])) {
							fpuTypeCombo.select(i);
							break;
						}
					}
				}

			} else {
				fpuTypeCombo.deselectAll();
			}
		} else {// 内核个数不为0时
			List<Core> cores = curCpu.getCores();

			numCombo.select(0);
			if (cores.get(0).getFloatABI() != null) {
				// for(int i=0;i<abis.length;i++) {
				// if(abis[i].contains(cores.get(0).getFloatABI())) {
				// abiCombo.select(i);
				// break;
				// }
				// }
				if (cores.get(0).getFpuType() != null) {
					if (cores.get(0).getFloatABI().equals("Library(soft)")) {
						fpuTypeCombo.select(0);
						fpuTypeCombo.setEnabled(false);
					} else {
						for (int i = 0; i < fpuTypes.length; i++) {
							if (fpuTypes[i].contains(cores.get(0).getFpuType())) {
								fpuTypeCombo.select(i);
								break;
							}
						}
					}
				}
			}

		}
		// 内核复选框选择事件
		numCombo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int selectIndex = numCombo.getSelectionIndex();
				String floatABI = curCpu.getCores().get(selectIndex).getFloatABI();
				String fpuType = curCpu.getCores().get(selectIndex).getFpuType();
				// if(floatABI!=null) {
				// for(int i=0;i<abis.length;i++) {
				// if(abis[i].contains(floatABI)) {
				// abiCombo.select(i);
				// }
				// }
				// }else {
				// abiCombo.deselectAll();
				// }
				if (fpuType != null) {
					for (int i = 0; i < fpuTypes.length; i++) {
						if (fpuTypes[i].contains(fpuType)) {
							fpuTypeCombo.select(i);
						}
					}
				} else {
					fpuTypeCombo.deselectAll();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		// //Float ABI复选框选择事件
		// abiCombo.addSelectionListener(new SelectionListener() {
		//
		// @Override
		// public void widgetSelected(SelectionEvent e) {
		// // TODO Auto-generated method stub
		// String floatABI = abiCombo.getText();
		// int selectIndex = numCombo.getSelectionIndex();
		// if(curCpu.getCores().size()!=0) {
		//
		// if(floatABI.contains("hard")) {
		// newCpu.getCores().get(selectIndex).setFloatABI("hard");
		// if(tempName!=null) {
		// revisingCpu.getCores().get(selectIndex).setFloatABI("hard");
		// }
		//
		// curCpu.getCores().get(selectIndex).setFloatABI("hard");
		// }else if(floatABI.contains("softfp")) {
		// newCpu.getCores().get(selectIndex).setFloatABI("softfp");
		// if(tempName!=null) {
		// revisingCpu.getCores().get(selectIndex).setFloatABI("softfp");
		// }
		//
		// curCpu.getCores().get(selectIndex).setFloatABI("softfp");
		// }else if(floatABI.contains("soft")) {
		// newCpu.getCores().get(selectIndex).setFloatABI("soft");
		// if(tempName!=null) {
		// revisingCpu.getCores().get(selectIndex).setFloatABI("soft");
		// }
		//
		// curCpu.getCores().get(selectIndex).setFloatABI("soft");
		// }else{
		// newCpu.getCores().get(selectIndex).setFloatABI("default");
		// if(tempName!=null) {
		// revisingCpu.getCores().get(selectIndex).setFloatABI("default");
		// }
		//
		// curCpu.getCores().get(selectIndex).setFloatABI("default");
		// }
		// }else {
		// if(floatABI.contains("hard")) {
		// newCore.setFloatABI("hard");
		// }else if(floatABI.contains("softfp")) {
		// newCore.setFloatABI("softfp");
		// }else if(floatABI.contains("soft")) {
		// newCore.setFloatABI("soft");
		// }else{
		// newCore.setFloatABI("default");
		// }
		// }
		// if(floatABI.equals("Library(soft)")) {
		// fpuTypeCombo.select(0);
		// fpuTypeCombo.setEnabled(false);
		// if(curCpu.getCores().size()!=0) {
		// newCpu.getCores().get(selectIndex).setFpuType("default");
		// if(tempName!=null) {
		// revisingCpu.getCores().get(selectIndex).setFpuType("default");
		// }
		// curCpu.getCores().get(selectIndex).setFpuType("default");
		// }else {
		// newCore.setFpuType("default");
		// }
		// }else {
		// fpuTypeCombo.setEnabled(true);
		// }
		// }
		//
		// @Override
		// public void widgetDefaultSelected(SelectionEvent e) {
		// // TODO Auto-generated method stub
		//
		// }
		// });
		// //fpuTypeCombo福安选矿选择事件
		fpuTypeCombo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				String fpuType = fpuTypeCombo.getText().trim();
				if (curCpu.getCores().size() != 0) {
					int selectIndex = numCombo.getSelectionIndex();
					if (fpuType.contains("default")) {
						newCpu.getCores().get(selectIndex).setFpuType("default");
						if (tempName != null) {
							revisingCpu.getCores().get(selectIndex).setFpuType("default");
						}

						curCpu.getCores().get(selectIndex).setFpuType("default");
					} else {
						newCpu.getCores().get(selectIndex).setFpuType(fpuType);
						if (tempName != null) {
							revisingCpu.getCores().get(selectIndex).setFpuType(fpuType);
						}

						curCpu.getCores().get(selectIndex).setFpuType(fpuType);
					}
				} else {
					if (fpuType.contains("default")) {
						newCore.setFpuType("default");
					} else {
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
		scrolledComposite = new ScrolledComposite(contentGroup, SWT.V_SCROLL | SWT.H_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		configContent = new Composite(scrolledComposite, SWT.NONE);
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
		for (int i = 0; i < curCpu.getCoreNum(); i++) {
			numCombo.add("内核" + (i + 1));
		}
		numCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Group memoryGroup = ControlFactory.createGroup(configContent, "配置内存", 1);
		GridLayout memoryGroupLayout = new GridLayout();
		memoryGroupLayout.numColumns = 2;
		memoryGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		memoryGroup.setLayout(memoryGroupLayout);

		Composite memoryComposite = new Composite(memoryGroup, SWT.NONE);
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
		btnCpt.setLayout(new GridLayout(2, true));
		btnCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_CENTER));
		Button addBtn = new Button(btnCpt, SWT.PUSH);
		addBtn.setText("添加");
		addBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int memoryCount = memoryTree.getItemCount();// memoryTree的孩子数量
				TreeItem t = new TreeItem(memoryTree, SWT.NONE);// 添加新的内存
				TreeItem[] items = memoryTree.getItems();
				int max = 0;
				if (memoryCount > 0) {
					String maxString = items[memoryCount - 1].getText();
					max = Integer.parseInt(maxString.substring(6, maxString.length()));
				}
				t.setText("memory" + (max + 1));
				CoreMemory memory = new CoreMemory();
				memory.setName("memory" + (max + 1));
				if (newCpu.getCores().size() != 0) {
					int selectIndex = numCombo.getSelectionIndex();
					newCpu.getCores().get(selectIndex).getMemorys().add(memory);
					if (tempName != null) {
						revisingCpu.getCores().get(selectIndex).getMemorys().add(memory);
					}
					if (curCpu.getCores().get(selectIndex).getMemorys().size() != newCpu.getCores().get(selectIndex)
							.getMemorys().size()) {
						curCpu.getCores().get(selectIndex).getMemorys().add(memory);
					}
				} else {
					newCore.getMemorys().add(memory);
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		Button deleteBtn = new Button(btnCpt, SWT.PUSH);
		deleteBtn.setText("删除");
		deleteBtn.setEnabled(false);
		deleteBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int selectIndex = numCombo.getSelectionIndex();
				TreeItem[] items = memoryTree.getSelection();
				if (items.length > 0) {
					for (int i = 0; i < newCpu.getCores().get(selectIndex).getMemorys().size(); i++) {
						String memoryName = newCpu.getCores().get(selectIndex).getMemorys().get(i).getName();
						if (memoryName != null) {
							if (memoryName.equals(items[0].getText().trim())) {
								newCpu.getCores().get(selectIndex).getMemorys().remove(i);
								if (tempName != null) {
									revisingCpu.getCores().get(selectIndex).getMemorys().remove(i);
								}

								curCpu.getCores().get(selectIndex).getMemorys().remove(i);
							}
						}
					}
					items[0].dispose();
				}
				int memoryNum = memoryTree.getItemCount();
				if (memoryNum < 1) {
					deleteBtn.setEnabled(false);
					setMemoryCpt(false);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		Composite memoryContentCpt = new Composite(memoryGroup, SWT.NONE);
		GridLayout detailsLayout = new GridLayout();
		detailsLayout.marginHeight = 20;
		detailsLayout.numColumns = 2;
		memoryContentCpt.setLayout(detailsLayout);
		memoryContentCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		memoryTypeLabel = new Label(memoryContentCpt, SWT.NONE);
		memoryTypeLabel.setText("类型: ");

		memoryTypeCombo = new Combo(memoryContentCpt, SWT.READ_ONLY);
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
				if (selectIndex != -1) {
					List<CoreMemory> memorys = newCpu.getCores().get(selectIndex).getMemorys();// 获取当前内核所有内存

					int comboIndex = memoryTypeCombo.getSelectionIndex();
					TreeItem[] items = memoryTree.getSelection();
					String type = memoryTypeCombo.getItem(comboIndex);//
					int index = -1;//
					if (items.length > 0) {
						String selectMemory = items[0].getText().trim();// 选中的内存
						for (int i = 0; i < memorys.size(); i++) {
							CoreMemory memory = memorys.get(i);
							if (memory.getName() != null) {
								if (memory.getName().equals(selectMemory)) {
									memory.setType(type);
									index = i;
									break;
								}
							}
						}
						if (tempName != null) {
							List<CoreMemory> _memorys = revisingCpu.getCores().get(selectIndex).getMemorys();// 获取当前内核所有内存
							for (int i = 0; i < _memorys.size(); i++) {
								CoreMemory memory = _memorys.get(i);
								if (memory.getName() != null) {
									if (memory.getName().equals(selectMemory)) {
										memory.setType(type);
										break;
									}
								}
							}
						}
					}
					if (index != -1) {
						curCpu.getCores().get(selectIndex).getMemorys().get(index).setType(type);
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		memoryAddrLabel = new Label(memoryContentCpt, SWT.NONE);
		memoryAddrLabel.setText("地址: ");
		addrField = new Text(memoryContentCpt, SWT.BORDER);
		addrField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addrField.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				// TODO Auto-generated method stub
				int selectIndex = numCombo.getSelectionIndex();
				if (selectIndex != -1) {
					List<CoreMemory> memorys = newCpu.getCores().get(selectIndex).getMemorys();// 获取当前内核所有内存

					TreeItem[] items = memoryTree.getSelection();
					if (items.length > 0) {
						String selectMemory = items[0].getText().trim();
						String addr = addrField.getText().trim();
						int index = -1;//
						if (items.length > 0) {
							if (!addr.equals("")) {
								int curNum = -1;
								Pattern pattern = Pattern.compile("^([1-9]\\d*|[0]{1,1})$"); // 含0正整数
								boolean isInt = pattern.matcher(addr).matches();
								if (addr.startsWith("0x")) {
									curNum = Integer.parseInt(addr.substring(2), 16);
								} else {
									if (isInt) {
										curNum = Integer.parseInt(addr);
									}
								}
								if (curNum < 0) {
									addrField.setText("");
									MessageDialog.openInformation(window.getShell(), "提示", "请输入正整数(包含0)");
								} else {
									for (int i = 0; i < memorys.size(); i++) {
										CoreMemory memory = memorys.get(i);
										if (memory.getName() != null) {
											if (memory.getName().equals(selectMemory)) {
												memory.setStartAddr(addr);
												index = i;
												break;
											}
										}

									}
									if (tempName != null) {
										List<CoreMemory> _memorys = revisingCpu.getCores().get(selectIndex)
												.getMemorys();// 获取当前内核所有内存
										System.out.println("_memorys.size()：  " + _memorys.size());
										for (int i = 0; i < _memorys.size(); i++) {
											CoreMemory memory = _memorys.get(i);
											System.out.println("memory.getName()：  " + memory.getName());
											if (memory.getName() != null) {
												if (memory.getName().equals(selectMemory)) {
													memory.setStartAddr(addr);
													break;
												}
											}

										}
									}
								}

							} else {
								// MessageDialog.openInformation(window.getShell(), "提示",
								// "地址不能为空");
							}
						}
						if (index != -1) {
							curCpu.getCores().get(selectIndex).getMemorys().get(index).setStartAddr(addr);
						}
					}

				}
			}
		});

		memorySizeLabel = new Label(memoryContentCpt, SWT.NONE);
		memorySizeLabel.setText("大小: ");
		sizeField = new Text(memoryContentCpt, SWT.BORDER);
		sizeField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		setMemoryCpt(true);
		sizeField.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				// TODO Auto-generated method stub
				int selectIndex = numCombo.getSelectionIndex();
				if (selectIndex != -1) {
					List<CoreMemory> memorys = newCpu.getCores().get(selectIndex).getMemorys();// 获取当前内核所有内存

					TreeItem[] items = memoryTree.getSelection();
					int index = -1;//
					String size = sizeField.getText().trim().replace("k", "");
					if (items.length > 0) {
						String selectMemory = items[0].getText().trim();
						if (!size.equals("")) {
							int curNum = -1;
							Pattern pattern = Pattern.compile("^[+]{0,1}(\\d+)$"); // ^[-\\+]?[\\d]*$
							boolean isInt = pattern.matcher(size).matches();
							if (size.startsWith("0x") && !size.trim().equals("0x")) {
								curNum = Integer.parseInt(size.substring(2), 16);
							} else {
								if (isInt) {
									curNum = Integer.parseInt(size);
								}
							}
							if (curNum < 0) {
								sizeField.setText("");
								MessageDialog.openInformation(window.getShell(), "提示", "请输入正整数(不包含0)");
							} else {
								for (int i = 0; i < memorys.size(); i++) {
									CoreMemory memory = memorys.get(i);
									if (memory.getName() != null) {
										if (memory.getName().equals(selectMemory)) {
											memory.setSize(size);
											index = i;
											break;
										}
									}
								}

								if (tempName != null) {
									List<CoreMemory> _memorys = revisingCpu.getCores().get(selectIndex).getMemorys();// 获取当前内核所有内存
									for (int i = 0; i < _memorys.size(); i++) {
										CoreMemory memory = _memorys.get(i);
										if (memory.getName() != null) {
											if (memory.getName().equals(selectMemory)) {
												memory.setSize(size);
												index = i;
												break;
											}
										}
									}
								}

							}

						} else {
							// MessageDialog.openInformation(window.getShell(), "提示",
							// "大小不能为空");
						}
					}
					if (index != -1) {
						curCpu.getCores().get(selectIndex).getMemorys().get(index).setSize(size);
					}
				}
			}
		});
		// 选择内核的监听事件
		numCombo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int selectIndex = numCombo.getSelectionIndex();
				List<CoreMemory> memorys = curCpu.getCores().get(selectIndex).getMemorys();
				Control[] controls = memoryGroup.getChildren();
				for (Control control : controls) {
					control.setEnabled(true);
				}
				memoryTree.removeAll();
				if (memorys.size() != 0) {
					for (int i = 0; i < memorys.size(); i++) {
						String memoryName = memorys.get(i).getName();
						TreeItem t = new TreeItem(memoryTree, SWT.NONE);
						t.setText(memoryName);
					}
					// memoryTypeLabel.setEnabled(true);
					// memoryTypeCombo.setEnabled(true);
				}
				setMemoryCpt(false);
				setDefaultMemTreeDisplay(memorys);
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
				setMemoryCpt(true);
				TreeItem[] items = memoryTree.getSelection();
				int selectIndex = numCombo.getSelectionIndex();
				List<CoreMemory> memorys = curCpu.getCores().get(selectIndex).getMemorys();
				if (items.length > 0) {
					String selectMemoryName = items[0].getText();
					for (int i = 0; i < memorys.size(); i++) {
						String memoryName = memorys.get(i).getName();
						if (memoryName.equals(selectMemoryName)) {
							if (memorys.get(i).getType() != null) {
								if (memorys.get(i).getType().equals("FLASH")) {
									memoryTypeCombo.select(0);
								} else if (memorys.get(i).getType().equals("RAM")) {
									memoryTypeCombo.select(1);
								}
							} else {
								memoryTypeCombo.deselectAll();
							}
							if (memorys.get(i).getStartAddr() != null) {
								addrField.setText(memorys.get(i).getStartAddr());
							} else {
								addrField.setText("");
							}
							if (memorys.get(i).getSize() != null) {
								sizeField.setText(String.valueOf(memorys.get(i).getSize()));
							} else {
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
			for (CoreMemory memory : memorys) {
				if (memory.getName() == null) {
					memory.setName("memory" + (count + 1));
				}
				count++;
			}
			memoryTree.removeAll();
			setDefaultMemTreeDisplay(memorys);
		} else {
			List<Core> cores = curCpu.getCores();
			numCombo.select(0);
			for (Core core : curCpu.getCores()) {
				List<CoreMemory> memorys = core.getMemorys();
				int i = 0;
				for (CoreMemory memory : memorys) {
					if (memory.getName() == null) {
						memory.setName("memory" + (i + 1));
					}
					i++;
				}
			}
			for (Core core : revisingCpu.getCores()) {
				List<CoreMemory> memorys = core.getMemorys();
				int i = 0;
				for (CoreMemory memory : memorys) {
					if (memory.getName() == null) {
						memory.setName("memory" + (i + 1));
					}
					i++;
				}
			}
			memoryTree.removeAll();
			if (curCpu.getCores().get(0).getMemorys().size() != 0) {
				List<CoreMemory> memorys = curCpu.getCores().get(0).getMemorys();

				memoryTree.removeAll();
				for (int i = 0; i < memorys.size(); i++) {
					String memoryName = memorys.get(i).getName();
					TreeItem t = new TreeItem(memoryTree, SWT.NONE);
					t.setText(memoryName);
					if (i == 0) {
						memoryTree.setSelection(t);
					}
				}
				setDefaultMemTreeDisplay(memorys);
			}
		}

		int selectIndex = numCombo.getSelectionIndex();
		if (selectIndex == -1) {
			Control[] controls = memoryGroup.getChildren();
			for (Control control : controls) {
				control.setEnabled(false);
			}
		} else {
			Control[] controls = memoryGroup.getChildren();
			for (Control control : controls) {
				control.setEnabled(true);
			}
		}

		if (memoryTree.getItemCount() == 0) {
			setMemoryCpt(false);
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

	protected void setDefaultMemTreeDisplay(List<CoreMemory> memorys) {
		// TODO Auto-generated method stub
		if (memorys.size() > 0) {
			if (memorys.get(0).getType() != null) {
				if (memorys.get(0).getType().equals("RAM")) {
					memoryTypeCombo.select(1);
				} else {
					memoryTypeCombo.select(0);
				}
			}
			if (memorys.get(0).getStartAddr() != null) {
				addrField.setText(memorys.get(0).getStartAddr());
			}
			if (memorys.get(0).getSize() != null) {
				sizeField.setText(memorys.get(0).getSize());
			}
		}
	}

	protected void creatCoreContent(Group contentGroup) {
		// TODO Auto-generated method stub
		scrolledComposite = new ScrolledComposite(contentGroup, SWT.V_SCROLL | SWT.H_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		configContent = new Composite(scrolledComposite, SWT.NONE);
		configContent.setLayout(new GridLayout());
		configContent.setLayoutData(new GridData(GridData.FILL_BOTH));
		Composite coreSelectCpt = new Composite(configContent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		coreSelectCpt.setLayout(layout);
		coreSelectCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		archItems = new ArrayList<TreeItem>();
		Label nameLabel = new Label(coreSelectCpt, SWT.NONE);
		nameLabel.setText("内核选择: ");
		numCombo4 = new Combo(coreSelectCpt, SWT.READ_ONLY);
		for (int i = 0; i < curCpu.getCoreNum(); i++) {
			numCombo4.add("内核" + (i + 1));
		}
		numCombo4.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Group coreGroup = ControlFactory.createGroup(configContent, "配置内核", 1);
		contentGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		contentGroup.setLayout(new GridLayout());

		Composite archComposite = new Composite(coreGroup, SWT.NULL);
		initArchTree(archComposite);

		if (curCpu.getCores().size() == 0) {
			coreSelectCpt.setVisible(false);
			resetArchTree(newCore);
		} else {
			List<Core> cores = curCpu.getCores();
			numCombo4.select(0);
			Core core = cores.get(0);
			resetArchTree(core);
		}

		numCombo4.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int index = numCombo4.getSelectionIndex();
				List<Core> cores = curCpu.getCores();
				Core core = cores.get(index);
				resetArchTree(core);
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

	protected void resetArchTree(Core core) {
		// TODO Auto-generated method stub
		if (core.getArch().getSerie() != null) {
			TreeItem[] typeItems = archTree.getItems();
			for (TreeItem item : typeItems) {
				if (item.getText().trim().equals(core.getArch().getSerie())) {
					travelItem(item, core);
					break;
				}
			}
		}
	}

	private void travelItem(TreeItem item, Core core) {
		// TODO Auto-generated method stub
		TreeItem[] items = item.getItems();
		for (TreeItem ti : items) {
			if (ti.getText().trim().equals(core.getArch().getMcpu())) {
				if (!ti.getGrayed()) {
					setParentItemExpand(ti);
					ti.setChecked(true);
				}
			} else {
				if (!ti.getGrayed()) {
					ti.setChecked(false);
				}
				travelItem(ti, core);
			}
		}
	}

	private void setParentItemExpand(TreeItem ti) {
		// TODO Auto-generated method stub
		if (ti.getParentItem() != null) {
			setParentItemExpand(ti.getParentItem());
		}
		ti.setExpanded(true);
	}

	private void initArchTree(Composite archComposite) {
		// TODO Auto-generated method stub
		archTree = new Tree(archComposite, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.CHECK);
		archTree.setLayoutData(new GridData(GridData.FILL_BOTH));
		archTree.setSize(300, 170);
		archTree.setHeaderVisible(true);

		final TreeColumn columnArch = new TreeColumn(archTree, SWT.NONE);
		columnArch.setText("Arch列表");
		columnArch.setWidth(280);
		columnArch.setResizable(false);
		columnArch.setToolTipText("请选择蓝色图标的一项");
		columnArch.setImage(DPluginImages.CFG_CPMT_OBJ.createImage());

		File file = new File(didePath + "djysrc/bsp/arch");
		File[] typeFiles = file.listFiles();
		for (int i = 0; i < typeFiles.length; i++) {
			if (dideHelper.containsXml(typeFiles[i])) {
				TreeItem type = new TreeItem(archTree, SWT.NONE);
				type.setText(typeFiles[i].getName());
				type.setImage(DPluginImages.TREE_FLODER_VIEW.createImage());
				type.setData(typeFiles[i]);// 保存当前节点数据
				type.setGrayed(true);
				type.setChecked(true);
				new TreeItem(type, 0);
				ExpandTree(type);
			}
		}

		archTree.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem item = (TreeItem) e.item;
				if (item == null) {
					return;
				} else {
					if (item.getGrayed()) {
						item.setChecked(true);
					} else {
						for (TreeItem t : archItems) {
							if (t != item) {
								t.setChecked(false);
							}
						}

						// 获取当前的Arch对象
						ReadArchXml rax = new ReadArchXml();
						File xmlFile = new File(item.getData().toString());
						Arch arch = new Arch();
						arch = rax.getMutiplyFileArch(xmlFile, arch);
						try {
							if (newCpu.getCores().size() != 0) {
								int selectIndex = numCombo4.getSelectionIndex();
								newCpu.getCores().get(selectIndex).getArch().setSerie(arch.getSerie());
								newCpu.getCores().get(selectIndex).getArch().setArchitecture(arch.getMarch());
								newCpu.getCores().get(selectIndex).getArch().setFamily(arch.getMcpu());
								if (tempName != null) {
									revisingCpu.getCores().get(selectIndex).getArch().setSerie(arch.getSerie());
									revisingCpu.getCores().get(selectIndex).getArch().setArchitecture(arch.getMarch());
									revisingCpu.getCores().get(selectIndex).getArch().setFamily(arch.getMcpu());
								}

								curCpu.getCores().get(selectIndex).getArch().setSerie(arch.getSerie());
								curCpu.getCores().get(selectIndex).getArch().setArchitecture(arch.getMarch());
								curCpu.getCores().get(selectIndex).getArch().setFamily(arch.getMcpu());
							} else {
								newCore.getArch().setSerie(arch.getSerie());
								newCore.getArch().setArchitecture(arch.getMarch());
								newCore.getArch().setFamily(arch.getMcpu());
							}
						} catch (Exception e2) {
							// TODO: handle exception
							MessageDialog.openInformation(window.getShell(), "提示",
									xmlFile.getName() + "或者其父目录的描述文件配置错误");
						}

						TreeItem[] items = cpuGroupTree.getItems();
						if (arch.getFpuType() != null) {
							boolean containsFloate = false;
							for (TreeItem t : items) {
								if (t.getText().equals("浮点配置")) {
									containsFloate = true;
									break;
								}
							}
							if (!containsFloate) {
								TreeItem t = new TreeItem(cpuGroupTree, SWT.NONE);
								t.setText("浮点配置");
								if (!attributes.contains("浮点配置")) {
									t.setImage(DPluginImages.CFG_DONE_VIEW.createImage());
								}
							}
						} else {
							for (TreeItem t : items) {
								if (t.getText().equals("浮点配置")) {
									t.dispose();
									break;
								}
							}
						}
					}
				}
			}

		});
	}

	private void ExpandTree(TreeItem root) {
		// TODO Auto-generated method stub
		TreeItem[] items = root.getItems();
		for (int i = 0; i < items.length; i++) {
			if (items[i].getData() != null)
				return;
			items[i].dispose();
		}
		File file = (File) root.getData();
		File[] files = file.listFiles();
		if (files == null)
			return;
		for (int i = 0; i < files.length; i++) {
			if ((files[i].isHidden() == false || files[i].getName().endsWith(".xml"))) {
				boolean toExpand = false;
				if (files[i].isDirectory()) {
					boolean isNeed = dideHelper.containsXml(files[i]);
					if (isNeed) {
						toExpand = true;
					}
				}
				if (toExpand) {
					// 当前为文件目录而不是文件的时候，添加新项目，以便只是显示文件夹（包括空文件夹），而不显示文件夹下的文件
					if (files[i].isDirectory() && files[i].getName() != "include" && files[i].getName() != "src") {
						TreeItem item;
						boolean configed = isFamily(files[i]);
						if (configed) {// SWT.ERROR_CANNOT_SET_ENABLED
							item = new TreeItem(root, 0);
							item.setImage(DPluginImages.DESC_ARCH_VIEW.createImage());
							item.setExpanded(false);
							item.setText(files[i].getName());
							File archFile = getArchFile(files[i]);
							item.setData(archFile);
							archItems.add(item);
						} else {
							item = new TreeItem(root, SWT.NONE);
							item.setImage(DPluginImages.TREE_FLODER_VIEW.createImage());
							item.setGrayed(true);
							item.setChecked(true);
							item.setText(files[i].getName());
							item.setData(files[i]);
							new TreeItem(item, 0);
							ExpandTree(item);
						}
					}
				}

			}
		}
	}

	private File getArchFile(File file) {
		// TODO Auto-generated method stub
		File[] files = file.listFiles();
		for (File f : files) {
			if (f.getName().equals("arch.xml")) {
				return f;
			}
		}
		return null;
	}

	protected boolean isFamily(File file) {
		// TODO Auto-generated method stub
		File[] cfiles = file.listFiles();
		for (int j = 0; j < cfiles.length; j++) {
			if (cfiles[j].getName().equals("arch.xml")) {
				return true;
			}
		}
		return false;
	}

	protected void creatCoreNumContent(Group contentGroup) {
		// TODO Auto-generated method stub
		scrolledComposite = new ScrolledComposite(contentGroup, SWT.V_SCROLL | SWT.H_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		configContent = new Composite(scrolledComposite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		configContent.setLayout(layout);
		configContent.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label nameLabel = new Label(configContent, SWT.NONE);
		nameLabel.setText("内核个数: ");
		Text numText = new Text(configContent, SWT.BORDER);
		numText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if (curCpu.getCoreNum() != 0) {
			int coreNum = curCpu.getCoreNum();
			numText.setText(String.valueOf(coreNum));
			// if(parentCpu.getCoreNum()!=0) {
			// numText.setEnabled(false);
			// }
		}
		numText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				// TODO Auto-generated method stub
				String coreNum = numText.getText();
				Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
				boolean isInt = pattern.matcher(coreNum).matches();
				if (!isInt) {
					numText.setText("");
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					MessageDialog.openError(window.getShell(), "提示", "请输入正整数");
				} else {
					newCpu = new Cpu();
					int cNum = Integer.parseInt(coreNum);
					newCpu.setCoreNum(cNum);
					curCpu.setCoreNum(cNum);
					int coreSize = revisingCpu.getCores().size();
					if (tempName != null) {
						revisingCpu.setCoreNum(cNum);
						if (cNum > revisingCpu.getCores().size()) {
							int extra = cNum - coreSize;
							for (int i = 0; i < extra; i++) {
								revisingCpu.getCores().add(new Core());
							}
						} else {
							for (int i = coreSize - 1; i >= cNum; i--) {
								revisingCpu.getCores().remove(i);
							}
						}

					}

					int newCoreSize = newCpu.getCores().size();
					if (cNum > newCoreSize) {
						int extra = cNum - newCoreSize;
						for (int i = 0; i < extra; i++) {
							newCpu.getCores().add(new Core());
						}
					} else {
						for (int i = newCoreSize - 1; i >= cNum; i--) {
							newCpu.getCores().remove(i);
						}
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

	private void getGroupNames(File curFile, List<String> names) {// f1 stm32

		names.add(curFile.getName());
		File parentFile = curFile.getParentFile();
		if (!parentFile.getName().equals("cpudrv")) {
			getGroupNames(parentFile, names);
		}

	}

	@Override
	protected Point getInitialSize() {
		// TODO Auto-generated method stub
		return new Point(570, 560);
	}

	public Cpu getCpuCreated() {
		return newCpu;
	}

	private boolean handleGroupOK() {
		groupName = groupNameField.getText();
		// String completeName = "";
		if (groupName.trim().equals("")) {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			MessageDialog.openError(window.getShell(), "提示", "请填写子目录名称");
			return false;
		} else {
			boolean isOk = true;
			File curFile = new File(curPath);
			if (curFile != null) {
				File[] files = curFile.listFiles();
				for (File f : files) {
					if (f.getName().equals(groupName)) {
						isOk = false;
						break;
					}
				}
			}
			File newGroupFile = null, oldGroupFile = null;
			File xmlFile = null;
			List<String> names = new ArrayList<String>();
			if (tempName == null) {// 新建子目录或者cpu
				if (isOk) {
					curPath = curPath + "/" + groupName;
					newGroupFile = new File(curPath);
					if (!newGroupFile.exists()) {
						newGroupFile.mkdir();
					}
					// getGroupNames(newGroupFile, names);
					// for (int i = names.size() - 1; i >= 0; i--) {
					// completeName += names.get(i);
					// }
					xmlFile = new File(newGroupFile.getPath() + "/cpu_group_" + groupName + ".xml");

					File[] files = newGroupFile.listFiles();
					for (File f : files) {
						if (f.getName().endsWith(".xml") && f.getName().startsWith("cpu_group")) {
							f.delete();
						}
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
				} else {
					MessageDialog.openInformation(window.getShell(), "提示", "该子目录名称已存在！");
				}

			} else {// 修改配置信息
				oldGroupFile = new File(curPath);
				newGroupFile = new File(curPath.substring(0, curPath.lastIndexOf("\\")) + "\\" + groupName);
				// 改名前
				// getGroupNames(oldGroupFile, names);
				// DeleteFolder(oldGroupFile.getPath());
				// if(!newGroupFile.exists()) {
				// newGroupFile.mkdir();
				// }
				// for (int i = names.size() - 1; i >= 0; i--) {
				// completeName += names.get(i);
				// }
				// xmlFile = new File(oldGroupFile.getPath() + "/cpu_group_" + groupName +
				// ".xml");
				// if (xmlFile.exists()) {
				// xmlFile.delete();
				// }

				File[] oldFiles = oldGroupFile.listFiles();
				for (File f : oldFiles) {
					if (f.getName().endsWith(".xml") && f.getName().startsWith("cpu_group")) {
						f.delete();
					}
				}

				oldGroupFile.renameTo(newGroupFile);
				// names = new ArrayList<String>();
				// completeName = "";
				// getGroupNames(newGroupFile, names);
				// for (int i = names.size() - 1; i >= 0; i--) {
				// completeName += names.get(i);
				// }
				xmlFile = new File(newGroupFile.getPath() + "/cpu_group_" + groupName + ".xml");

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
		}
		return true;
	}

	private boolean handleCpuOK() {
		groupName = groupNameField.getText();
		cpuName = groupNameField.getText();
		if (cpuName.trim().equals("")) {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			MessageDialog.openError(window.getShell(), "提示", "请填写Cpu名称");
			return false;
		} else {
			boolean isOk = true;
			File curFile = new File(curPath);
			if (curFile != null) {
				File[] files = curFile.listFiles();
				for (File f : files) {
					if (f.getName().equals(groupName)) {
						isOk = false;
						break;
					}
				}
			}

			File newCpuFile, oldCpuFile;
			File xmlFile;
			if (tempName == null) {// 新建子目录或者cpu
				if (isOk) {
					curPath = curPath + "/" + cpuName;
					newCpuFile = new File(curPath);
					if (!newCpuFile.exists()) {
						newCpuFile.mkdir();
					}
					File[] files = newCpuFile.listFiles();
					for (File f : files) {
						if (f.getName().endsWith(".xml") && f.getName().startsWith("cpu_")) {
							f.delete();
						}
					}
					xmlFile = new File(newCpuFile.getPath() + "/cpu_" + cpuName + ".xml");
					try {
						xmlFile.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					newCpu.setParentPath(newCpuFile.getPath());
					createNewCpuXml(newCpu, xmlFile, cpuName);
				} else {
					MessageDialog.openInformation(window.getShell(), "提示", "该子Cpu名称已存在！");
				}

			} else {// 修改配置信息
				oldCpuFile = new File(curPath);
				newCpuFile = new File(curPath.substring(0, curPath.lastIndexOf("\\")) + "\\" + cpuName);
				xmlFile = new File(oldCpuFile.getPath() + "/cpu_" + tempName + ".xml");
				if (xmlFile.exists()) {
					xmlFile.delete();
				}

				File[] files = oldCpuFile.listFiles();
				for (File f : files) {
					if (f.getName().endsWith(".xml") && f.getName().startsWith("cpu_")) {
						f.delete();
					}
				}

				oldCpuFile.renameTo(newCpuFile);
				xmlFile = new File(newCpuFile.getPath() + "/cpu_" + cpuName + ".xml");
				try {
					xmlFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				createNewCpuXml(revisingCpu, xmlFile, cpuName);
			}

			return true;
		}
	}

	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		boolean isOK = true;
		String errorMsg = null;
		List<Core> cores = curCpu.getCores();
		for (int i = 0; i < cores.size(); i++) {
			if (cpuTag.endsWith("cpu")) {
				if (cores.get(i).getResetAddr() == null) {
					isOK = false;
					errorMsg = "请填写完整内核" + (i + 1) + "的复位地址！";
				}

				boolean fpNeed = dideHelper.isFputypeuNeed(cores.get(i));
				if (fpNeed) {
					if (cores.get(i).getFpuType() == null) {
						isOK = false;
						errorMsg = "请填写完整内核" + (i + 1) + "的浮点配置！";
					}
				}

				if (cores.get(i).getArch().getMcpu() == null) {
					isOK = false;
					errorMsg = "请填写完整内核" + (i + 1) + "的内核配置！";
				}
			}

			List<CoreMemory> memorys = cores.get(i).getMemorys();
			for (CoreMemory memory : memorys) {
				if (memory.getType() == null || memory.getStartAddr() == null || memory.getSize() == null) {
					isOK = false;
					errorMsg = "请填写完整内核" + (i + 1) + "的存储配置！";
					break;
				} else if (memory.getSize().equals("0") || memory.getSize().equals("0x")) {
					isOK = false;
					errorMsg = "内核" + (i + 1) + "的内存大小需大于0！";
					break;
				}
			}
			if (!isOK) {
				break;
			}
		}

		if (isOK) {
			if (cpuTag.equals("group")) {
				if (handleGroupOK()) {
					super.okPressed();
				}
			} else if (cpuTag.equals("cpu")) {
				if (handleCpuOK()) {
					super.okPressed();
				}
			} else if (cpuTag.startsWith("revise")) {
				if (cpuTag.endsWith("cpu")) {
					if (handleCpuOK()) {
						super.okPressed();
					}
				} else if (cpuTag.endsWith("group")) {
					if (handleGroupOK()) {
						super.okPressed();
					}
				}
			}
		} else {
			MessageDialog.openInformation(window.getShell(), "提示", errorMsg);
		}

	}

	private void createNewGroupXml(Cpu cpu, File file) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			factory.setIgnoringElementContentWhitespace(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();

			Element cpuElement = document.createElement("cpu");
			for (int i = 0; i < cpu.getCores().size(); i++) {
				Element core = document.createElement("core");
				core.setAttribute("id", String.valueOf(i + 1));

				Core curCore = cpu.getCores().get(i);
				if (curCore.getArch().getSerie() != null) {
					Element type = document.createElement("type");
					type.setTextContent(curCore.getArch().getSerie());
					core.appendChild(type);
				}
				if (curCore.getArch().getMarch() != null) {
					Element arch = document.createElement("arch");
					arch.setTextContent(curCore.getArch().getMarch());
					core.appendChild(arch);
				}
				if (curCore.getArch().getMcpu() != null) {
					Element family = document.createElement("family");
					family.setTextContent(curCore.getArch().getMcpu());
					core.appendChild(family);
				}
				if (curCore.getFloatABI() != null) {
					Element floatABI = document.createElement("floatABI");
					floatABI.setTextContent(curCore.getFloatABI());
					core.appendChild(floatABI);
				}
				if (curCore.getFpuType() != null) {
					Element fpuType = document.createElement("fpuType");
					fpuType.setTextContent(curCore.getFpuType());
					core.appendChild(fpuType);
				}
				if (curCore.getResetAddr() != null) {
					Element resetAddr = document.createElement("resetAddr");
					resetAddr.setTextContent(curCore.getResetAddr());
					core.appendChild(resetAddr);
				}
				if (curCore.getMemorys().size() != 0) {

					for (int j = 0; j < curCore.getMemorys().size(); j++) {
						CoreMemory curMemory = curCore.getMemorys().get(j);
						Element memory = document.createElement("memory");
						Element type = document.createElement("type");
						type.setTextContent(curMemory.getType());
						memory.appendChild(type);
						Element startAddr = document.createElement("startAddr");
						startAddr.setTextContent(curMemory.getStartAddr());
						memory.appendChild(startAddr);
						Element size = document.createElement("size");
						String memorySize = curMemory.getSize();
						if (!memorySize.contains("k")) {
							memorySize += "k";
						}
						size.setTextContent(memorySize);
						memory.appendChild(size);
						core.appendChild(memory);
					}

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
			dideHelper.showErrorMessage("文件" + file.getName() + "创建失败！ " + e.getMessage());
		}

	}

	private void createPublicXml(Core core, File file) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			factory.setIgnoringElementContentWhitespace(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();

			Element cpuElement = document.createElement("cpu");
			if (core.getArch().getSerie() != null) {
				Element type = document.createElement("type");
				type.setTextContent(core.getArch().getSerie());
				cpuElement.appendChild(type);
			}
			if (core.getArch().getMarch() != null) {
				Element arch = document.createElement("arch");
				arch.setTextContent(core.getArch().getMarch());
				cpuElement.appendChild(arch);
			}
			if (core.getArch().getMcpu() != null) {
				Element family = document.createElement("family");
				family.setTextContent(core.getArch().getMcpu());
				cpuElement.appendChild(family);
			}
			if (core.getFloatABI() != null) {
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
					String memorySize = curMemory.getSize();
					if (!memorySize.contains("k")) {
						memorySize += "k";
					}
					size.setTextContent(memorySize);
					memory.appendChild(size);
					cpuElement.appendChild(memory);
				}

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
			dideHelper.showErrorMessage("文件" + file.getName() + "创建失败！ " + e.getMessage());
		}
	}

	private boolean createNewCpuXml(Cpu cpu, File file, String completeName) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			factory.setIgnoringElementContentWhitespace(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();

			Element cpuElement = document.createElement("cpu");

			Element cpuName = document.createElement("cpuName");
			cpuName.setTextContent(completeName);
			cpuElement.appendChild(cpuName);

			for (int i = 0; i < cpu.getCores().size(); i++) {
				Element core = document.createElement("core");
				core.setAttribute("id", String.valueOf(i + 1));

				Core curCore = cpu.getCores().get(i);
				if (curCore.getArch().getSerie() != null) {
					Element type = document.createElement("type");
					type.setTextContent(curCore.getArch().getSerie());
					core.appendChild(type);
				}
				if (curCore.getArch().getMarch() != null) {
					Element arch = document.createElement("arch");
					arch.setTextContent(curCore.getArch().getMarch());
					core.appendChild(arch);
				}
				if (curCore.getArch().getMcpu() != null) {
					Element family = document.createElement("family");
					family.setTextContent(curCore.getArch().getMcpu());
					core.appendChild(family);
				}
				if (curCore.getFloatABI() != null) {
					Element floatABI = document.createElement("floatABI");
					floatABI.setTextContent(curCore.getFloatABI());
					core.appendChild(floatABI);
				}
				if (curCore.getFpuType() != null) {
					Element fpuType = document.createElement("fpuType");
					fpuType.setTextContent(curCore.getFpuType());
					core.appendChild(fpuType);
				}
				if (curCore.getResetAddr() != null) {
					Element resetAddr = document.createElement("resetAddr");
					resetAddr.setTextContent(curCore.getResetAddr());
					core.appendChild(resetAddr);
				}
				if (curCore.getMemorys().size() != 0) {

					for (int j = 0; j < curCore.getMemorys().size(); j++) {
						CoreMemory curMemory = curCore.getMemorys().get(j);
						Element memory = document.createElement("memory");
						Element type = document.createElement("type");
						type.setTextContent(curMemory.getType());
						memory.appendChild(type);
						Element startAddr = document.createElement("startAddr");
						startAddr.setTextContent(curMemory.getStartAddr());
						memory.appendChild(startAddr);
						Element size = document.createElement("size");
						String memorySize = curMemory.getSize();
						if (!memorySize.contains("k")) {
							memorySize += "k";
						}
						size.setTextContent(memorySize);
						memory.appendChild(size);
						core.appendChild(memory);
					}

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
			dideHelper.showErrorMessage("文件" + file.getName() + "创建失败！ " + e.getMessage());
		}
		return true;
	}

}
