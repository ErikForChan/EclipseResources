package com.djyos.dide.ui.wizards.cpu;

import java.io.File;
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
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
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

import com.djyos.dide.ui.DPluginImages;
import com.djyos.dide.ui.arch.ArchHelper;
import com.djyos.dide.ui.arch.ReadArchXml;
import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.objects.Arch;
import com.djyos.dide.ui.objects.Core;
import com.djyos.dide.ui.objects.CoreMemory;
import com.djyos.dide.ui.objects.Cpu;
import com.djyos.dide.ui.swt.DjyosUI;
import com.djyos.dide.ui.wizards.djyosProject.tools.FileTool;

public class NewGroupOrCpuDialog extends StatusDialog implements ICpuConstants{

	private Tree cpuGroupTree, memoryTree, archTree, sharedMemoryTree;
	private Composite configContent;
	private Combo memoryTypeCombo,shared_memoryTypeCombo;
	private Text addrField, sizeField, groupNameField, shared_addrField, shared_sizeField;
	private Group contentGroup;
	private ScrolledComposite scrolledComposite;
	private Cpu newCpu = new Cpu(), parentCpu = new Cpu(), curCpu = new Cpu(), revisingCpu = new Cpu(), CpuCreated;
	private Core newCore = new Core();
	private String curPath = null, groupName, cpuName, cpuTag = null, tempName = null;
	private List<String> attributes = new ArrayList<String>();
	private Label memorySizeLabel, memoryAddrLabel, memoryTypeLabel,
	shared_memorySizeLabel, shared_memoryAddrLabel, shared_memoryTypeLabel;
	private File configFile;

	private void setMemoryCpt(boolean isEnable) {
		memoryTypeLabel.setEnabled(isEnable);
		memoryAddrLabel.setEnabled(isEnable);
		memorySizeLabel.setEnabled(isEnable);
		memoryTypeCombo.setEnabled(isEnable);
		sizeField.setEnabled(isEnable);
		addrField.setEnabled(isEnable);
	}
	
	private void setSharedMemoryCpt(boolean isEnable) {
		shared_memoryTypeLabel.setEnabled(isEnable);
		shared_memoryAddrLabel.setEnabled(isEnable);
		shared_memorySizeLabel.setEnabled(isEnable);
		shared_memoryTypeCombo.setEnabled(isEnable);
		shared_addrField.setEnabled(isEnable);
		shared_addrField.setEnabled(isEnable);
	}

	public String getGroupName() {
		return groupName;
	}

	private void init_Window_Constants(Label nameLabel) {
		// TODO Auto-generated method stub
		if (cpuTag.equals("group")) {
			nameLabel.setText(name_folder);
		} else if (cpuTag.equals("cpu")) {
			nameLabel.setText(name_cpu);
		} else if (cpuTag.startsWith("revise")) {
			if (cpuTag.endsWith("cpu")) {
				nameLabel.setText(name_cpu);
			} else if (cpuTag.endsWith("group")) {
				nameLabel.setText(name_folder);
			}
			groupNameField.setText(tempName);
		}
	}
	
	public NewGroupOrCpuDialog(Shell parent, List<String> configs, Cpu cpu, String curFilePath, String tag) {
		super(parent);
		attributes = configs;
		curPath = curFilePath;
		cpuTag = tag;
		newCpu = new Cpu();
		if (cpu.getCoreNum() != 0) {
			parentCpu.setCoreNum(cpu.getCoreNum());
			parentCpu.setCores(cpu.getCores());
			parentCpu.setShared_memorys(cpu.getShared_memorys());
			parentCpu.setSharedMemoryNames();
			curCpu.setCoreNum(cpu.getCoreNum());
			curCpu.setCores(cpu.getCores());
			curCpu.setShared_memorys(cpu.getShared_memorys());
			curCpu.setSharedMemoryNames();
			newCpu.setCoreNum(cpu.getCoreNum());
			for (int i = 0; i < cpu.getCoreNum(); i++) {
				newCpu.getCores().add(new Core());
			}
		}

		switch(tag) {
			case "group":
				setTitle(title_group);
				break;
			case "cpu":
				setTitle(title_cpu);
				break;
			default:
				if (tag.startsWith("revise")) {
					if (tag.endsWith("group")) {
						setTitle(title_folder_revice);
					} else if (tag.endsWith("cpu")) {
						setTitle(title_cpu_revice);
					}
					File curFile = new File(curPath);
					configFile = DideHelper.getXmlFile(curFile);
					if (configFile != null) {
						revisingCpu = ReadCpuXml.unitCpu(revisingCpu, configFile);
					}
					tempName = curPath.substring(curPath.lastIndexOf("\\") + 1, curPath.length());
				}
				break;
		}

		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = DjyosUI.DjyosComposite(parent, SWT.NONE);

		Composite groupNameCpt = new Composite(composite, SWT.NONE);
		GridLayout groupNameLayout = DjyosUI.DjyosGridLayout(2, 0, 20);
		groupNameCpt.setLayout(groupNameLayout);
		groupNameCpt.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label nameLabel = new Label(groupNameCpt, SWT.NONE);
		groupNameField = new Text(groupNameCpt, SWT.BORDER);
		groupNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		init_Window_Constants(nameLabel);

		Composite cpuGroupCpt = new Composite(composite, SWT.NULL);
		GridLayout layoutAttributes = DjyosUI.DjyosGridLayout(2, 0, 0);
		cpuGroupCpt.setLayout(layoutAttributes);
		cpuGroupCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		Composite cpuGroupListCpt = new Composite(cpuGroupCpt, SWT.NULL);
		cpuGroupTree = buildTree(cpuGroupListCpt,160, 290, config_head);
		
		check_Configured();

		contentGroup = ControlFactory.createGroup(cpuGroupCpt, config_group, 1);
		contentGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		contentGroup.setLayout(new GridLayout(1, true));
		cpuGroupTree.select(cpuGroupTree.getItem(0));
		
		init_ScrollCompt();
		contentGroup.setText(config_core);
		creatCoreNumContent();
		resetScrollCompt();
		
		cpuGroupTree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				handle_Tree_MouseDown();
			}
		});

		return super.createDialogArea(parent);
	}
	
	private void init_ScrollCompt() {
		// TODO Auto-generated method stub
		scrolledComposite = new ScrolledComposite(contentGroup, SWT.V_SCROLL | SWT.H_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		configContent = DjyosUI.DjyosComposite(scrolledComposite, SWT.NONE);
	}

	private void check_Configured() {
		// TODO Auto-generated method stub
		List<String> cons = new ArrayList<String>();
		cons.add(config_core);
		cons.add(config_reset);
		cons.add(config_shared_memory);
		// 如果当前的内核个数不为0，且当前cpu内核的浮点不为空，则添加浮点配置项
		if (curCpu.getCores().size() > 0) {
			boolean fpNeed = DideHelper.isFputypeuNeed(curCpu.getCores().get(0));
			if (fpNeed) {
				cons.add(config_fpu);
			}
		}
//		cons.add(config_core);
		cons.add(config_memory);
		for (int i = 0; i < cons.size(); i++) {
			TreeItem t = new TreeItem(cpuGroupTree, SWT.NONE);
			t.setText(cons.get(i));
			if (!attributes.contains(cons.get(i))) {
				t.setImage(DPluginImages.CFG_DONE_VIEW.createImage());
			}
		}
	}

	private void  resetScrollCompt() {
		Point point = configContent.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		scrolledComposite.setContent(configContent);
		scrolledComposite.setMinHeight(point.y);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setMinWidth(300);
	}

	/**
	 * 处理树被选中的相应方法 根据选中的目标，创建选择内核、选择复位地址、 选择浮点配置、选择内存配置 或者选择内核配置的界面
	 */
	protected void handle_Tree_MouseDown() {
		// TODO Auto-generated method stub
		TreeItem[] items = cpuGroupTree.getSelection();
		if (items.length > 0) {
			String selectConfigName = items[0].getText();
			contentGroup.setText(selectConfigName);
			scrolledComposite.dispose();
			init_ScrollCompt();
			switch (selectConfigName) {
			case config_core:
				creatCoreNumContent();
				break;
			case config_memory:
				creatMemoryContent();
				break;
			case config_shared_memory:
				creatSharedMemoryContent();
				break;
			case config_fpu:
				creatFloatContent();
				break;
			case config_reset:
				creatResetContent();
				break;
			}
			resetScrollCompt();
			contentGroup.layout();
		}
	}

	private MemoryHandler getMemoryHandlerInstance() {
        return new MemoryHandler();
    }
	
	public Tree buildTree(Composite memoryComposite, int tWidth, int tHeight, String columTitle) {
		Tree t = new Tree(memoryComposite, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		t.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		t.setHeaderVisible(true);
		t.setSize(tWidth,tHeight);
		final TreeColumn columnMemory = new TreeColumn(t, SWT.NONE);
		columnMemory.setText(columTitle);
		columnMemory.setWidth(tWidth);
		columnMemory.setResizable(false);
		columnMemory.setImage(DPluginImages.CFG_CPMT_OBJ.createImage());
		return t;
	}
	
	private void creatSharedMemoryContent() {
		// TODO Auto-generated method stub
		MemoryHandler memHandler = getMemoryHandlerInstance();
		
		GridLayout layout = DjyosUI.DjyosGridLayout(2, 0, 0);
		configContent.setLayout(layout);
		Composite memoryComposite = new Composite(configContent, SWT.NONE);
		memoryComposite.setLayout(new GridLayout());
		memoryComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		sharedMemoryTree = buildTree(memoryComposite,150,170, "共享片内存储");
		Composite btnCpt = new Composite(memoryComposite, SWT.NONE);
		btnCpt.setLayout(new GridLayout(2, true));
		btnCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_CENTER));
		Button addBtn = new Button(btnCpt, SWT.PUSH);
		addBtn.setText("添加");
		addBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				memHandler.handle_addClick(sharedMemoryTree,true,-1);
			}
		});
		Button deleteBtn = new Button(btnCpt, SWT.PUSH);
		deleteBtn.setText("删除");
		deleteBtn.setEnabled(false);
		deleteBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int memoryNum = memHandler.handle_deleteClick(sharedMemoryTree,true,-1);
				if (memoryNum < 1) {
					deleteBtn.setEnabled(false);
					setSharedMemoryCpt(false);
				}
			}
		});

		Composite memoryContentCpt = new Composite(configContent, SWT.NONE);
		GridLayout detailsLayout = DjyosUI.DjyosGridLayout(2, 0, 20);
		memoryContentCpt.setLayout(detailsLayout);
		memoryContentCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		shared_memoryTypeLabel = new Label(memoryContentCpt, SWT.NONE);
		shared_memoryTypeLabel.setText("类型: ");

		shared_memoryTypeCombo = new Combo(memoryContentCpt, SWT.READ_ONLY);
		shared_memoryTypeCombo.add("FLASH");
		shared_memoryTypeCombo.add("RAM");
		shared_memoryTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		shared_memoryTypeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				setSharedMemoryCpt(true);
				int comboIndex = shared_memoryTypeCombo.getSelectionIndex();
				TreeItem[] items = sharedMemoryTree.getSelection();
				String type = shared_memoryTypeCombo.getItem(comboIndex);//
				if (items.length > 0) {
					String selectMemory = items[0].getText().trim();// 选中的内存
					List<CoreMemory> memorys = newCpu.getShared_memorys();// 获取当前内核所有内存
					int index = memHandler.updateType(memorys,selectMemory,type);
					if (index != -1) {
						curCpu.getShared_memorys().get(index).setType(type);
					}
					if (tempName != null) {
						List<CoreMemory> _memorys = revisingCpu.getShared_memorys();// 获取当前内核所有内存
						memHandler.updateType(_memorys,selectMemory,type);
					}
				}
			}
		});
		
		shared_memoryAddrLabel = new Label(memoryContentCpt, SWT.NONE);
		shared_memoryAddrLabel.setText("地址: ");
		shared_addrField = new Text(memoryContentCpt, SWT.BORDER);
		shared_addrField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		shared_addrField.addMouseTrackListener(new MouseTrackListener() {

			@Override
			public void mouseHover(MouseEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void mouseExit(MouseEvent e) {
				// TODO Auto-generated method stub
					Pattern pattern = Pattern.compile("^([1-9]\\d*|[0]{1,1})$"); // 含0正整数
					TreeItem[] items = sharedMemoryTree.getSelection();
					if (items.length > 0) {
						String selectMemory = items[0].getText().trim();
						String addr = shared_addrField.getText().trim();
						if (items.length > 0 && !addr.equals("")) {
								boolean isInt = pattern.matcher(addr).matches();
								int curNum = parseIntNum(addr,isInt);
								if (curNum < 0) {
									shared_addrField.setText("");
									DideHelper.showErrorMessage("请输入正整数(包含0)");
								} else {
									List<CoreMemory> memorys = newCpu.getShared_memorys();// 获取当前内核所有内存
									int index = memHandler.updateAddr(memorys, selectMemory, addr);
									if (index != -1) {
										curCpu.getShared_memorys().get(index).setStartAddr(addr);
									}
									if (tempName != null) {
										List<CoreMemory> _memorys = revisingCpu.getShared_memorys();// 获取当前内核所有内存
										memHandler.updateAddr(_memorys, selectMemory, addr);
									}
								}
						}
					}

			}

			@Override
			public void mouseEnter(MouseEvent e) {
				// TODO Auto-generated method stub

			}
		});

		shared_memorySizeLabel = new Label(memoryContentCpt, SWT.NONE);
		shared_memorySizeLabel.setText("大小: ");
		shared_sizeField = new Text(memoryContentCpt, SWT.BORDER);
		shared_sizeField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		if(curCpu.getShared_memorys().size()>0) {
			sharedMemoryTree.removeAll();
			for(int i = 0;i<curCpu.getShared_memorys().size();i++) {
				String memoryName = curCpu.getShared_memorys().get(i).getName();
				TreeItem t = new TreeItem(sharedMemoryTree, SWT.NONE);
				t.setText(memoryName == null?("memory"+(i+1)):memoryName);
			}
			shared_memoryTypeCombo.select(0);
			shared_addrField.setText(curCpu.getShared_memorys().get(0).getStartAddr());
			shared_sizeField.setText(curCpu.getShared_memorys().get(0).getSize());
		}
		
		setSharedMemoryCpt(true);

		shared_sizeField.addMouseTrackListener(new MouseTrackListener() {

			@Override
			public void mouseHover(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExit(MouseEvent e) {
				// TODO Auto-generated method stub
				Pattern pattern = Pattern.compile("^[+]{0,1}(\\d+)$"); // ^[-\\+]?[\\d]*$
				TreeItem[] items = sharedMemoryTree.getSelection();
				String size = shared_sizeField.getText().trim().replace("k", "");
				if (items.length > 0) {
					String selectMemory = items[0].getText().trim();
					if (!size.equals("")) {
						boolean isInt = pattern.matcher(size).matches();
						int curNum = parseIntNum(size, isInt);
						if (curNum < 0) {
							shared_sizeField.setText("");
							DideHelper.showErrorMessage("请输入正整数(不包含0)");
						} else {
							List<CoreMemory> memorys = newCpu.getShared_memorys();// 获取当前内核所有内存
							int index = memHandler.updateSize(memorys, selectMemory, size);
							if (index != -1) {
								curCpu.getShared_memorys().get(index).setSize(size);
							}
							if (tempName != null) {
								List<CoreMemory> _memorys = revisingCpu.getShared_memorys();// 获取当前内核所有内存
								memHandler.updateSize(_memorys, selectMemory, size);
							}
						}

					}
				}
			}

			@Override
			public void mouseEnter(MouseEvent e) {
				// TODO Auto-generated method stub

			}
		});

		sharedMemoryTree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				deleteBtn.setEnabled(true);
				setSharedMemoryCpt(true);
				TreeItem[] items = sharedMemoryTree.getSelection();
				List<CoreMemory> memorys = curCpu.getShared_memorys();
				if (items.length > 0) {
					String selectMemoryName = items[0].getText();
					for (CoreMemory memory:memorys) {
						String memoryName = memory.getName();
						if (memoryName.equals(selectMemoryName)) {
							if (memory.getType() != null) {
								shared_memoryTypeCombo.select(memory.getType().equalsIgnoreCase("RAM")?1:0);
							} else {
								shared_memoryTypeCombo.deselectAll();
							}
							shared_addrField.setText(memory.getStartAddr()==null?"":memory.getStartAddr());
							shared_sizeField.setText(memory.getSize()==null?"":String.valueOf(memory.getSize()));
							break;
						}
					}
				}
			}
		});

		if (sharedMemoryTree.getItemCount() == 0) {
			setSharedMemoryCpt(false);
		}

	}
	
	protected void creatMemoryContent() {
		// TODO Auto-generated method stub
		MemoryHandler memHandler = getMemoryHandlerInstance();
		
		Composite coreSelectCpt = new Composite(configContent, SWT.NONE);
		GridLayout layout = DjyosUI.DjyosGridLayout(2,0,0);
		coreSelectCpt.setLayout(layout);
		coreSelectCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label nameLabel = new Label(coreSelectCpt, SWT.NONE);
		nameLabel.setText(core_select);
		Combo numCombo = new Combo(coreSelectCpt, SWT.READ_ONLY);
		for (int i = 0; i < curCpu.getCoreNum(); i++) {
			String coreName = curCpu.getCores().get(i).getName();
			numCombo.add(coreName!=null?coreName:"core"+(i+1));
		}
		numCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Group memoryGroup = ControlFactory.createGroup(configContent, "配置内存", 1);
		GridLayout memoryGroupLayout = DjyosUI.DjyosGridLayout(2, 0, 0);
		memoryGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		memoryGroup.setLayout(memoryGroupLayout);

		Composite memoryComposite = new Composite(memoryGroup, SWT.NONE);
		memoryComposite.setLayout(new GridLayout());
		memoryComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		memoryTree = buildTree(memoryComposite,150,170, "私有片内存储");
		Composite btnCpt = new Composite(memoryComposite, SWT.NONE);
		btnCpt.setLayout(new GridLayout(2, true));
		btnCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_CENTER));
		Button addBtn = new Button(btnCpt, SWT.PUSH);
		addBtn.setText("添加");
		addBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int coreIndex = numCombo.getSelectionIndex();
				memHandler.handle_addClick(memoryTree,false,coreIndex);
			}
		});
		Button deleteBtn = new Button(btnCpt, SWT.PUSH);
		deleteBtn.setText("删除");
		deleteBtn.setEnabled(false);
		deleteBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int coreIndex = numCombo.getSelectionIndex();
				int memoryNum = memHandler.handle_deleteClick(memoryTree, false, coreIndex);
				if (memoryNum < 1) {
					deleteBtn.setEnabled(false);
					setMemoryCpt(false);
				}
			}
		});

		Composite memoryContentCpt = new Composite(memoryGroup, SWT.NONE);
		GridLayout detailsLayout = DjyosUI.DjyosGridLayout(2, 0, 20);
		memoryContentCpt.setLayout(detailsLayout);
		memoryContentCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		memoryTypeLabel = new Label(memoryContentCpt, SWT.NONE);
		memoryTypeLabel.setText("类型: ");

		memoryTypeCombo = new Combo(memoryContentCpt, SWT.READ_ONLY);
		memoryTypeCombo.add("FLASH");
		memoryTypeCombo.add("RAM");
		memoryTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		memoryTypeCombo.addSelectionListener(new SelectionAdapter() {

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
					if (items.length > 0) {
						String selectMemory = items[0].getText().trim();// 选中的内存
						int index = memHandler.updateType(memorys, selectMemory, type);
						if (index != -1) {
							curCpu.getCores().get(selectIndex).getMemorys().get(index).setType(type);
						}
						if (tempName != null) {
							List<CoreMemory> _memorys = revisingCpu.getCores().get(selectIndex).getMemorys();// 获取当前内核所有内存
							memHandler.updateType(_memorys, selectMemory, type);
						}
					}
				}
			}
				
		});

		memoryAddrLabel = new Label(memoryContentCpt, SWT.NONE);
		memoryAddrLabel.setText("地址: ");
		addrField = new Text(memoryContentCpt, SWT.BORDER);
		addrField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		addrField.addMouseTrackListener(new MouseTrackListener() {

			@Override
			public void mouseHover(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExit(MouseEvent e) {
				// TODO Auto-generated method stub
				int selectIndex = numCombo.getSelectionIndex();
				if (selectIndex != -1) {
					Pattern pattern = Pattern.compile("^([1-9]\\d*|[0]{1,1})$"); // 含0正整数
					TreeItem[] items = memoryTree.getSelection();
					if (items.length > 0) {
						String selectMemory = items[0].getText().trim();
						String addr = addrField.getText().trim();
						if (items.length > 0) {
							if (!addr.equals("")) {
								boolean isInt = pattern.matcher(addr).matches();
								int curNum = parseIntNum(addr, isInt);
								if (curNum < 0) {
									addrField.setText("");
									DideHelper.showErrorMessage("请输入正整数(包含0)");
								} else {
									List<CoreMemory> memorys = newCpu.getCores().get(selectIndex).getMemorys();// 获取当前内核所有内存
									int index = memHandler.updateAddr(memorys, selectMemory, addr);
									if (index != -1) {
										curCpu.getCores().get(selectIndex).getMemorys().get(index).setStartAddr(addr);
									}
									if (tempName != null) {
										List<CoreMemory> _memorys = revisingCpu.getCores().get(selectIndex)
												.getMemorys();// 获取当前内核所有内存
										memHandler.updateAddr(_memorys, selectMemory, addr);
									}
								}

							} 
						}
					}

				}
			}

			@Override
			public void mouseEnter(MouseEvent e) {
				// TODO Auto-generated method stub

			}
		});

		memorySizeLabel = new Label(memoryContentCpt, SWT.NONE);
		memorySizeLabel.setText("大小: ");
		sizeField = new Text(memoryContentCpt, SWT.BORDER);
		sizeField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		setMemoryCpt(true);

		sizeField.addMouseTrackListener(new MouseTrackListener() {

			@Override
			public void mouseHover(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExit(MouseEvent e) {
				// TODO Auto-generated method stub
				int selectIndex = numCombo.getSelectionIndex();
				if (selectIndex != -1) {
					Pattern pattern = Pattern.compile("^[+]{0,1}(\\d+)$"); // ^[-\\+]?[\\d]*$
					TreeItem[] items = memoryTree.getSelection();
					String size = sizeField.getText().trim().replace("k", "");
					if (items.length > 0) {
						String selectMemory = items[0].getText().trim();
						if (!size.equals("")) {
							boolean isInt = pattern.matcher(size).matches();
							int curNum = parseIntNum(size, isInt);
							if (curNum < 0) {
								sizeField.setText("");
								DideHelper.showErrorMessage("请输入正整数(不包含0)");
							} else {
								List<CoreMemory> memorys = newCpu.getCores().get(selectIndex).getMemorys();// 获取当前内核所有内存
								int index = memHandler.updateSize(memorys, selectMemory, size);
								if (index != -1) {
									curCpu.getCores().get(selectIndex).getMemorys().get(index).setSize(size);
								}
								if (tempName != null) {
									List<CoreMemory> _memorys = revisingCpu.getCores().get(selectIndex).getMemorys();// 获取当前内核所有内存
									memHandler.updateSize(_memorys, selectMemory, size);
								}

							}

						}
					}
				}
			}

			@Override
			public void mouseEnter(MouseEvent e) {
				// TODO Auto-generated method stub

			}
		});

		// 选择内核的监听事件
		numCombo.addSelectionListener(new SelectionAdapter() {
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
				for (int i = 0; i < memorys.size(); i++) {
					String memoryName = memorys.get(i).getName();
					TreeItem t = new TreeItem(memoryTree, SWT.NONE);
					t.setText(memoryName);
				}
				setMemoryCpt(false);
				setDefault_MemTree_Display(memorys,memoryTypeCombo,addrField,sizeField);
			}
		});

		memoryTree.addSelectionListener(new SelectionAdapter() {
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
								memoryTypeCombo.select(memorys.get(i).getType().equals("RAM")?1:0);
							} else {
								memoryTypeCombo.deselectAll();
							}
							addrField.setText(memorys.get(i).getStartAddr() == null?"":memorys.get(i).getStartAddr());
							sizeField.setText(memorys.get(i).getSize() == null?"":String.valueOf(memorys.get(i).getSize()));
						}
					}
				}
			}
		});

		if (curCpu.getCores().size() == 0) {
			coreSelectCpt.setVisible(false);
			List<CoreMemory> memorys = newCore.getMemorys();
			setCoreMemory(memorys);
			memoryTree.removeAll();
			setDefault_MemTree_Display(memorys,memoryTypeCombo,addrField,sizeField);
		} else {
			numCombo.select(0);
			for (Core core : curCpu.getCores()) {
				List<CoreMemory> memorys = core.getMemorys();
				setCoreMemory(memorys);
			}
			for (Core core : revisingCpu.getCores()) {
				List<CoreMemory> memorys = core.getMemorys();
				setCoreMemory(memorys);
			}
			memoryTree.removeAll();
			if (curCpu.getCores().get(0).getMemorys().size() != 0) {
				List<CoreMemory> memorys = curCpu.getCores().get(0).getMemorys();
				for (int i = 0; i < memorys.size(); i++) {
					String memoryName = memorys.get(i).getName();
					TreeItem t = new TreeItem(memoryTree, SWT.NONE);
					t.setText(memoryName);
					if (i == 0) {
						memoryTree.setSelection(t);
					}
				}
				setDefault_MemTree_Display(memorys,memoryTypeCombo,addrField,sizeField);
			}
		}

		int selectIndex = numCombo.getSelectionIndex();
		if (selectIndex == -1) {
			DjyosUI.enableControls(memoryGroup,false);
		} else {
			DjyosUI.enableControls(memoryGroup,true);
		}

		if (memoryTree.getItemCount() == 0) {
			setMemoryCpt(false);
		}

	}

	private void setCoreMemory(List<CoreMemory> memorys) {
		// TODO Auto-generated method stub
		for (CoreMemory memory : memorys) {
			int index = memorys.indexOf(memory);
			if (memory.getName() == null) {
				memory.setName("memory" + (index + 1));
			}
		}
	}

	protected int parseIntNum(String addr, boolean isInt) {
		// TODO Auto-generated method stub
		if (addr.startsWith("0x")) {
			return Integer.parseInt(addr.substring(2), 16);
		} else {
			if (isInt) {
				return Integer.parseInt(addr);
			}
		}
		return -1;
	}

	protected void creatCoreNumContent() {
		// TODO Auto-generated method stub
		//*******************************************//
		GridLayout layout = DjyosUI.DjyosGridLayout(2, 0, 0);
		configContent.setLayout(layout);
		
		Composite coreSelectCpt = new Composite(configContent, SWT.NONE);
		GridLayout layout_core_select = DjyosUI.DjyosGridLayout(2, 0, 0);
		coreSelectCpt.setLayout(layout_core_select);
		coreSelectCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label nameLabel = new Label(coreSelectCpt, SWT.NONE);
		nameLabel.setText(config_corenum +": ");
		Text numText = new Text(coreSelectCpt, SWT.BORDER);
		numText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Composite coreNameContent = new Composite(configContent, SWT.BORDER);
		GridData gd_core_name = new GridData(GridData.FILL_BOTH);
		gd_core_name.horizontalSpan = 2;
		GridLayout layout_core_name = DjyosUI.DjyosGridLayout(3, 0, 0);
		coreNameContent.setLayout(layout_core_name);
		coreNameContent.setLayoutData(gd_core_name);
		
		if (curCpu.getCoreNum() != 0) {
			int coreNum = curCpu.getCoreNum();
			numText.setText(String.valueOf(coreNum));
			List<Text> coreNameTexts = new ArrayList<Text>();
			for(int i=0;i<coreNum;i++) {
				int index = i;
				Label coreLabel = new Label(coreNameContent, SWT.NONE);
				coreLabel.setText("核"+(i+1)+": ");
				Text coreNameText = new Text(coreNameContent, SWT.BORDER);
				coreNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				coreNameText.setMessage("请选择内核");
				
				Button sCoreBtn = new Button(coreNameContent, SWT.PUSH);
				sCoreBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				sCoreBtn.setText("选择内核");
				String coreName = curCpu.getCores().get(i).getName();
				String mCpu = curCpu.getCores().get(0).getArch().getMcpu();
				if(coreName != null) {
					coreNameText.setText(coreName);
				}else if(mCpu != null){
					coreNameText.setText(mCpu);
				}
				coreNameText.addModifyListener(new ModifyListener() {
					@Override
					public void modifyText(ModifyEvent e) {
						// TODO Auto-generated method stub
						handle__CoreNum_modify(coreNameText,index);
					}
				});
				coreNameTexts.add(coreNameText);
				sCoreBtn.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						// TODO Auto-generated method stub
						handle_sCoreSelected(coreNameTexts,index);
					}
				});
//				if (configFile != null) {
//					if (tempName != null) {
//						Cpu singleCpu = ReadCpuXml.unitCpu(new Cpu(), configFile);
//						if (singleCpu.getCores().get(0).getResetAddr() == null) {
//							numText.setEnabled(false);
//							coreNameText.setEnabled(false);
//						}
//					}
//				}else {
//					numText.setEnabled(false);
//					coreNameText.setEnabled(false);
//				}
				
			}
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
					DideHelper.showErrorMessage(input_int);
				}else if(!coreNum.trim().equals("")){
					int cNum = Integer.parseInt(coreNum);
					Handle_CoreNum_modify(cNum,coreNameContent);
				}
				resetScrollCompt();
				contentGroup.layout();
			}
		});
	}
	
	private void handle__CoreNum_modify(Text coreNameText, int index) {
		String coreName = coreNameText.getText();
		newCpu.getCores().get(index).setName(coreName);
		curCpu.getCores().get(index).setName(coreName);
		if (tempName != null) {
			revisingCpu.getCores().get(index).setName(coreName);
		}
	}

	protected void handle_sCoreSelected(List<Text> coreNameTexts,int selectIndex) {
		// TODO Auto-generated method stub
		SelectFamilyDialog dialog = new SelectFamilyDialog(coreNameTexts.get(selectIndex).getShell());
		if(dialog.open() == Window.OK) {
			Arch arch = dialog.getArch();
			for(Text text:coreNameTexts) {
				int textIndex = coreNameTexts.indexOf(text);
				if(textIndex >= selectIndex) {
					String coreName = arch.getMcpu();
					if(coreNameTexts.size() > 2) {
						coreName += "_"+String.valueOf(textIndex + 1);
					}
					text.setText(coreName);
				}
			}
			if (newCpu.getCores().size() != 0) {
				for (int i = selectIndex; i < newCpu.getCores().size(); i++) {
					newCpu.getCores().get(i).setArch(arch);
				}
				if (tempName != null) {
					for (int i = selectIndex; i < revisingCpu.getCores().size(); i++) {
						revisingCpu.getCores().get(i).setArch(arch);
					}
				}
				for (int i = selectIndex; i < curCpu.getCores().size(); i++) {
					curCpu.getCores().get(i).setArch(arch);
				}

			} else {
				newCore.setArch(arch);
			}

			TreeItem[] items = cpuGroupTree.getItems();
			if (arch.getFpuType() != null) {
				boolean containsFloate = false;
				for (TreeItem t : items) {
					if (t.getText().equals(config_fpu)) {
						containsFloate = true;
						break;
					}
				}
				if (!containsFloate) {
					TreeItem t = new TreeItem(cpuGroupTree, SWT.NONE);
					t.setText(config_fpu);
					if (!attributes.contains(config_fpu)) {
						t.setImage(DPluginImages.CFG_DONE_VIEW.createImage());
					}
				}
			}
		}
	}
	
	protected void Handle_CoreNum_modify(int cNum, Composite coreNameContent) {
		newCpu.setCoreNum(cNum);
		curCpu.setCoreNum(cNum);
		// TODO Auto-generated method stub
		if (tempName != null) {
			revisingCpu.setCoreNum(cNum);
			changeCores(revisingCpu,cNum,revisingCpu.getCores().size());
		}
		changeCores(newCpu,cNum,newCpu.getCores().size());
		changeCores(curCpu,cNum,curCpu.getCores().size());
		
		Control[] controls = coreNameContent.getChildren();
		for(Control c:controls) {
			c.dispose();
		}
		
		List<Text> coreNameTexts = new ArrayList<Text>();
		for(int i=0;i<cNum;i++) {
			int index = i;
			Label coreLabel = new Label(coreNameContent, SWT.NONE);
			coreLabel.setText("核"+(i+1)+": ");
			Text coreNameText = new Text(coreNameContent, SWT.BORDER);
			coreNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			coreNameText.setMessage("请选择内核");
			
			Button sCoreBtn = new Button(coreNameContent, SWT.PUSH);
			sCoreBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			sCoreBtn.setText("选择内核");
			if (tempName != null) {
				if(revisingCpu.getCores().get(i).getName() != null) {
					coreNameText.setText(revisingCpu.getCores().get(i).getName());
				}
			}else {
				if(curCpu.getCores().get(i).getName() != null) {
					coreNameText.setText(curCpu.getCores().get(i).getName());
				}
			}
			coreNameText.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					// TODO Auto-generated method stub
					handle__CoreNum_modify(coreNameText,index);
				}
			});
			coreNameTexts.add(coreNameText);
			sCoreBtn.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
					handle_sCoreSelected(coreNameTexts,index);
				}
			});
			coreNameContent.layout();
		}
	}

	protected void changeCores(Cpu cpu, int num, int coreSize) {
		// TODO Auto-generated method stub
		cpu.setCoreNum(num);
		if (num > coreSize) {
			int extra = num - coreSize;
			for (int i = 0; i < extra; i++) {
				cpu.getCores().add(new Core());
			}
		} else {
			for (int i = coreSize - 1; i >= num; i--) {
				cpu.getCores().remove(i);
			}
		}
	}

	protected void creatResetContent() {
		// TODO Auto-generated method stub
		Composite coreSelectCpt = new Composite(configContent, SWT.NONE);
		GridLayout layout = DjyosUI.DjyosGridLayout(2, 0, 0);
		coreSelectCpt.setLayout(layout);
		coreSelectCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label nameLabel = new Label(coreSelectCpt, SWT.NONE);
		nameLabel.setText(core_select);
		Combo numCombo = new Combo(coreSelectCpt, SWT.READ_ONLY);
		for (int i = 0; i < curCpu.getCoreNum(); i++) {
			String coreName = curCpu.getCores().get(i).getName();
			if(coreName!=null) {
				numCombo.add(coreName);
			}else {
				numCombo.add("core"+(i+1));
			}
		}
		numCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Group resetGroup = ControlFactory.createGroup(configContent, "配置复位地址", 1);
		resetGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		resetGroup.setLayout(new GridLayout());

		Composite coreConfigCpt = new Composite(resetGroup, SWT.NONE);
		GridLayout coreLayout = DjyosUI.DjyosGridLayout(2, 0, 0);
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

		numCombo.addSelectionListener(new SelectionAdapter() {
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
		});

		addrText.addMouseTrackListener(new MouseTrackListener() {

			@Override
			public void mouseHover(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExit(MouseEvent e) {
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

			@Override
			public void mouseEnter(MouseEvent e) {
				// TODO Auto-generated method stub

			}
		});
	}

	protected void creatFloatContent() {
		// TODO Auto-generated method stub
		// 内核选择界面
		Composite coreSelectCpt = new Composite(configContent, SWT.NONE);
		GridLayout layout = DjyosUI.DjyosGridLayout(2, 0, 0);
		coreSelectCpt.setLayout(layout);
		coreSelectCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label nameLabel = new Label(coreSelectCpt, SWT.NONE);
		nameLabel.setText(core_select);
		Combo numCombo = new Combo(coreSelectCpt, SWT.READ_ONLY);
		numCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		for (int i = 0; i < curCpu.getCoreNum(); i++) {
			Core core = curCpu.getCores().get(i);
			boolean fpNeed = DideHelper.isFputypeuNeed(core);
			if (fpNeed) {
				String coreName = core.getName();
				coreName = coreName!=null?coreName:("core"+(i+1));
				numCombo.add(coreName);
			}
		}

		// 配置浮点界面
		Group floatGroup = ControlFactory.createGroup(configContent, "配置浮点", 1);
		floatGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		floatGroup.setLayout(new GridLayout());

		Composite coreConfigCpt = new Composite(floatGroup, SWT.NONE);
		GridLayout coreLayout = DjyosUI.DjyosGridLayout(2, 0, 0);
		coreConfigCpt.setLayout(coreLayout);
		coreConfigCpt.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label fpuTypeLabel = new Label(coreConfigCpt, SWT.NONE);
		fpuTypeLabel.setText("FPU Type： ");
		Combo fpuTypeCombo = new Combo(coreConfigCpt, SWT.READ_ONLY);
		fpuTypeCombo.setItems(fpuTypes);
		fpuTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// 内核个数为0时
		if (curCpu.getCores().size() == 0) {
			coreSelectCpt.setVisible(false);
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
			if (cores.get(0).getFpuType() != null) {
				for (int i = 0; i < fpuTypes.length; i++) {
					if (fpuTypes[i].contains(cores.get(0).getFpuType())) {
						fpuTypeCombo.select(i);
						break;
					}
				}
			}

		}
		// 内核复选框选择事件
		numCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int selectIndex = numCombo.getSelectionIndex();
				String fpuType = curCpu.getCores().get(selectIndex).getFpuType();
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
		});

		fpuTypeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				String fpuType = fpuTypeCombo.getText().trim();
				fpuType = fpuType.contains("default")?"default":fpuType;
				if (curCpu.getCores().size() != 0) {
					int selectIndex = numCombo.getSelectionIndex();
					String curCoreName =  numCombo.getText();
					for(Core core:curCpu.getCores()) {
						if(core.getName() != null) {
							if(core.getName().equalsIgnoreCase(curCoreName)) {
								selectIndex = curCpu.getCores().indexOf(core);
							}
						}
					}
					
					newCpu.getCores().get(selectIndex).setFpuType(fpuType);
					curCpu.getCores().get(selectIndex).setFpuType(fpuType);
					if (tempName != null) {
						revisingCpu.getCores().get(selectIndex).setFpuType(fpuType);
					}
				} else {
					newCore.setFpuType(fpuType);
				}
			}
		});

	}


	/**
	 * 设置配置内存界面的默认显示
	 * 
	 * @param memorys
	 *            正在配置的Cpu的内存
	 * @param sizeField2  ·
	 * @param addrField2 
	 * @param memoryTypeCombo2 
	 */
	protected void setDefault_MemTree_Display(List<CoreMemory> memorys, Combo memoryTypeCombo2, Text addrField2, Text sizeField2) {
		// TODO Auto-generated method stub
		if (memorys.size() > 0) {
			if (memorys.get(0).getType() != null) {
				memoryTypeCombo.select(memorys.get(0).getType().equalsIgnoreCase("RAM")?1:0);
			}
			if (memorys.get(0).getStartAddr() != null) {
				addrField.setText(memorys.get(0).getStartAddr());
			}
			if (memorys.get(0).getSize() != null) {
				sizeField.setText(memorys.get(0).getSize());
			}
		}
	}

	/**
	 * 重置arch界面
	 * 
	 * @param core
	 *            当前内核
	 */
	protected void resetArchTree(Core core) {
		// TODO Auto-generated method stub
		if (core.getArch().getSerie() != null) {
			TreeItem[] typeItems = archTree.getItems();
			for (TreeItem item : typeItems) {
				travelItem(item, core);
			}
		}
	}

	/**
	 * 遍历Item的子Item
	 * 
	 * @param item
	 *            当前的Item
	 * @param core
	 *            当前的啮合
	 */
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

	/**
	 * 将父Item设置为展开
	 * 
	 * @param ti
	 *            当前的子Item
	 */
	private void setParentItemExpand(TreeItem ti) {
		// TODO Auto-generated method stub
		if (ti.getParentItem() != null) {
			setParentItemExpand(ti.getParentItem());
		}
		ti.setExpanded(true);
	}

	@Override
	protected Point getInitialSize() {
		// TODO Auto-generated method stub
		return new Point(570, 550);
	}

	public Cpu getCpuCreated() {
		return CpuCreated;
	}

	private boolean handleGroupOK() {
		groupName = groupNameField.getText();
		if (groupName.trim().equals("")) {
			DideHelper.showErrorMessage("请填写子目录名称");
			return false;
		} else {
			if (tempName == null) {// 新建子目录或者cpu
				handleNewGroup();
			} else {// 修改配置信息
				handleReviceGroup();
			}
		}
		return true;
	}

	private void handleReviceGroup() {
		// TODO Auto-generated method stub
		File oldGroupFile = new File(curPath);
		File newGroupFile = new File(curPath.substring(0, curPath.lastIndexOf("\\")) + "\\" + groupName);
		File[] oldFiles = oldGroupFile.listFiles();
		for (File f : oldFiles) {
			if (f.getName().endsWith(".xml") && f.getName().startsWith("cpu_group")) {
				f.delete();
			}
		}
		oldGroupFile.renameTo(newGroupFile);
		File xmlFile = new File(newGroupFile.getPath() + "/cpu_group_" + groupName + ".xml");
		FileTool.createNewFile(xmlFile);
		if (revisingCpu.getCores().size() != 0) {
			CreateCpuXml.createNewGroupXml(revisingCpu, xmlFile);
			List<File> childXmlFiles = new ArrayList<File>();
			getChildXmlFiles(xmlFile.getParentFile(),childXmlFiles);
			childXmlFiles.remove(0);
			int coreNum = revisingCpu.getCoreNum();
			for(File f:childXmlFiles) {
				ChangeCpuXml.changeCoreNum(f,coreNum);
			}
		} else {
			CreateCpuXml.createPublicXml(newCore, xmlFile);
		}
	}

	private void handleNewGroup() {
		// TODO Auto-generated method stub
		boolean groupNameVailable = true;
		File curFile = new File(curPath);
		if (curFile != null) {
			File[] files = curFile.listFiles();
			for (File f : files) {
				if (f.getName().equals(groupName)) {
					groupNameVailable = false;
					break;
				}
			}
		}
		if (groupNameVailable) {
			curPath = curPath + "/" + groupName;
			File newGroupFile = new File(curPath);
			if (!newGroupFile.exists()) {
				newGroupFile.mkdir();
			}
			File xmlFile = new File(newGroupFile.getPath() + "/cpu_group_" + groupName + ".xml");
			File[] files = newGroupFile.listFiles();
			for (File f : files) {
				if (f.getName().endsWith(".xml") && f.getName().startsWith("cpu_group")) {
					f.delete();
				}
			}
			FileTool.createNewFile(xmlFile);
			if (newCpu.getCores().size() != 0) {
				CreateCpuXml.createNewGroupXml(newCpu, xmlFile);
			} else {
				CreateCpuXml.createPublicXml(newCore, xmlFile);
			}
		} else {
			DideHelper.showErrorMessage("该子目录名称已存在！");
		}
	}

	private void getChildXmlFiles(File parentFile, List<File> childXmlFiles) {
		// TODO Auto-generated method stub
		File[] files = parentFile.listFiles();
		for(File f : files) {
			if(f.getName().endsWith(".xml")) {
				childXmlFiles.add(f);
			}else if(f.isDirectory()) {
				getChildXmlFiles(f,childXmlFiles);
			}
		}
	}

	private boolean handleCpuOK() {
		groupName = groupNameField.getText();
		cpuName = groupNameField.getText();
		if (cpuName.trim().equals("")) {
			DideHelper.showErrorMessage("请填写Cpu名称");
			return false;
		} else {
			if (tempName == null) {// 新建子目录或者cpu
				handleNewCpu();
			} else {// 修改配置信息
				handleReviceCpu();
			}
			return true;
		}
	}

	private void handleReviceCpu() {
		// TODO Auto-generated method stub
		File oldCpuFile = new File(curPath);
		File newCpuFile = new File(curPath.substring(0, curPath.lastIndexOf("\\")) + "\\" + cpuName);
		File xmlFile = new File(oldCpuFile.getPath() + "/cpu_" + tempName + ".xml");
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
		FileTool.createNewFile(xmlFile);
		CreateCpuXml.createNewCpuXml(revisingCpu, xmlFile, cpuName);
	}

	private void handleNewCpu() {
		// TODO Auto-generated method stub
		boolean cpuNameVailable = true;
		File curFile = new File(curPath);
		if (curFile != null) {
			File[] files = curFile.listFiles();
			for (File f : files) {
				if (f.getName().equals(groupName)) {
					cpuNameVailable = false;
					break;
				}
			}
		}
		if (cpuNameVailable) {
			curPath = curPath + "/" + cpuName;
			File newCpuFile = new File(curPath);
			if (!newCpuFile.exists()) {
				newCpuFile.mkdir();
			}
			File[] files = newCpuFile.listFiles();
			for (File f : files) {
				if (f.getName().endsWith(".xml") && f.getName().startsWith("cpu_")) {
					f.delete();
				}
			}
			File xmlFile = new File(newCpuFile.getPath() + "/cpu_" + cpuName + ".xml");
			FileTool.createNewFile(xmlFile);
			newCpu.setCpuFolderPath(newCpuFile.getPath());
			CreateCpuXml.createNewCpuXml(newCpu, xmlFile, cpuName);
		} else {
			DideHelper.showErrorMessage("该子Cpu名称已存在！");
		}
	}

	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		boolean isOK = true;
		String errorMsg = null;
		List<Core> cores = curCpu.getCores();
		for (int i = 0; i < cores.size(); i++) {
			String coreName = cores.get(i).getName() == null?String.valueOf(i + 1):cores.get(i).getName();
			if (cpuTag.endsWith("cpu")) {
				if (cores.get(i).getResetAddr() == null) {
					isOK = false;
					errorMsg = "请填写完整内核" + coreName + "的复位地址！";
				}

				boolean fpNeed = DideHelper.isFputypeuNeed(cores.get(i));
				if (fpNeed) {
					if (cores.get(i).getFpuType() == null) {
						isOK = false;
						errorMsg = "请填写完整内核" + coreName + "的浮点配置！";
					}
				}

				if (cores.get(i).getArch().getMcpu() == null) {
					isOK = false;
					errorMsg = "请填写完整内核" + coreName + "的内核配置！";
				}
			}

			List<CoreMemory> memorys = cores.get(i).getMemorys();
			for (CoreMemory memory : memorys) {
				if (memory.getType() == null || memory.getStartAddr() == null || memory.getSize() == null) {
					isOK = false;
					errorMsg = "请填写完整内核" + coreName + "的存储配置！";
					break;
				} else if (memory.getSize().equals("0") || memory.getSize().equals("0x")) {
					isOK = false;
					errorMsg = "内核" + coreName + "的内存大小需大于0！";
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
					CpuCreated = curCpu;
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
			DideHelper.showErrorMessage(errorMsg);
		}

	}

	class MemoryHandler {
		public int updateType(List<CoreMemory> memorys, String selectMemory, String type) {
			for (int i = 0; i < memorys.size(); i++) {
				CoreMemory memory = memorys.get(i);
				if (memory.getName() != null) {
					if (memory.getName().equals(selectMemory)) {
						memory.setType(type);
						return i;
					}
				}
			}
			return -1;
		}

		public int updateAddr(List<CoreMemory> memorys, String selectMemory, String addr) {
			for (int i = 0; i < memorys.size(); i++) {
				CoreMemory memory = memorys.get(i);
				if (memory.getName() != null) {
					if (memory.getName().equals(selectMemory)) {
						memory.setStartAddr(addr);
						return i;
					}
				}
			}
			return -1;
		}
		
		public int updateSize(List<CoreMemory> memorys, String selectMemory, String size) {
			for (int i = 0; i < memorys.size(); i++) {
				CoreMemory memory = memorys.get(i);
				if (memory.getName() != null) {
					if (memory.getName().equals(selectMemory)) {
						memory.setSize(size);
						return i;
					}
				}
			}
			return -1;
		}

		public void handle_addClick(Tree tree, boolean isShared, int coreIndex) {
			// TODO Auto-generated method stub
			int memoryCount = tree.getItemCount();// memoryTree的孩子数量
			TreeItem t = new TreeItem(tree, SWT.NONE);// 添加新的内存
//			TreeItem[] items = tree.getItems();
//			int max = 0;
//			if (memoryCount > 0) {
//				String maxString = items[memoryCount - 1].getText();
//				max = Integer.parseInt(maxString.substring(6, maxString.length()));
//			}
			t.setText("memory" + (memoryCount + 1));
			CoreMemory memory = new CoreMemory();
			memory.setName("memory" + (memoryCount + 1));
			if(isShared) {
				if (newCpu.getCores().size() != 0) {
					newCpu.getShared_memorys().add(memory);
					if (tempName != null) {
						revisingCpu.getShared_memorys().add(memory);
					}
					if (curCpu.getShared_memorys().size() != newCpu.getShared_memorys().size()) {
						curCpu.getShared_memorys().add(memory);
					}
				}
			}else {
				if (newCpu.getCores().size() != 0) {
					newCpu.getCores().get(coreIndex).getMemorys().add(memory);
					if (tempName != null) {
						revisingCpu.getCores().get(coreIndex).getMemorys().add(memory);
					}
					if (curCpu.getCores().get(coreIndex).getMemorys().size() != newCpu.getCores().get(coreIndex)
							.getMemorys().size()) {
						curCpu.getCores().get(coreIndex).getMemorys().add(memory);
					}
				} else {
					newCore.getMemorys().add(memory);
				}
			}
		}
	
		public int handle_deleteClick(Tree tree, boolean isShared, int coreIndex) {
			TreeItem[] items = tree.getSelection();
			if(isShared) {
				if (items.length > 0) {
					for (int i = 0; i < newCpu.getShared_memorys().size(); i++) {
						String memoryName = newCpu.getShared_memorys().get(i).getName();
						if (memoryName != null) {
							if (memoryName.equals(items[0].getText().trim())) {
								newCpu.getShared_memorys().remove(i);
								if (tempName != null) {
									revisingCpu.getShared_memorys().remove(i);
								}

								curCpu.getShared_memorys().remove(i);
							}
						}
					}
					items[0].dispose();
				}
			}else {
				if (items.length > 0) {
					for (int i = 0; i < newCpu.getCores().get(coreIndex).getMemorys().size(); i++) {
						String memoryName = newCpu.getCores().get(coreIndex).getMemorys().get(i).getName();
						if (memoryName != null) {
							if (memoryName.equals(items[0].getText().trim())) {
								newCpu.getCores().get(coreIndex).getMemorys().remove(i);
								if (tempName != null) {
									revisingCpu.getCores().get(coreIndex).getMemorys().remove(i);
								}
								curCpu.getCores().get(coreIndex).getMemorys().remove(i);
							}
						}
					}
					items[0].dispose();
				}
			}
			return tree.getItemCount();
		}
	}
}
