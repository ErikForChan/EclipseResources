package org.eclipse.cdt.ui.wizards.board;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TouchEvent;
import org.eclipse.swt.events.TouchListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.actions.NewWizardShortcutAction;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.wizards.IWizardDescriptor;

import org.eclipse.cdt.ui.wizards.board.onboardcpu.Chip;
import org.eclipse.cdt.ui.wizards.component.Component;
import org.eclipse.cdt.ui.wizards.component.ReadComponentXml;
import org.eclipse.cdt.ui.wizards.cpu.Cpu;
import org.eclipse.cdt.ui.wizards.cpu.ReadCpuXml;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.OnBoardCpu;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.OnBoardMemory;

public class BoardMainWizard extends WizardPage{
	private Tree cpuArhives;
	private Tree cpuArhivesNeed;
	private Tree chipTree;
	private Tree chipOnTree;
	private Tree cpudrvTree;
	private Tree cpudrvOnTree;
	private Tree memoryTree;
	private Button gotoBtn = null;
	private Button backBtn = null;
	private Board newBoard = new Board();
	private List<OnBoardCpu> onBoardCpus = new ArrayList<OnBoardCpu>();
	private Cpu newCpu;
	private Component newComponent;
	private Chip newChip;
	private Text boardNameField;
	private TabFolder folder;
	private Combo interfaceCombo;
	private Text mainClkField;
	private Button rtcClkBtn;
	private Text rtcClkField;
	private Combo memoryTypeCombo;
	private Text addrField;
	private Text sizeField;
	
	private List<Cpu> cpusList = null;
	private List<Cpu> cpusOn = null;
	private List<Component> peripheralsList = null;//外设列表
	private List<Component> peripheralsOn = new ArrayList<Component>();//用到的外设
	private List<Component> allPeripherals = new ArrayList<Component>();//所有外设
	private List<Chip> chipsList = null;
	private List<Chip> chipsOn = null;
	private List<OnBoardMemory> memorys = new ArrayList<OnBoardMemory>();
	private List<Component> thePeripherals;
	private ReadComponentXml rcx = new ReadComponentXml();	
	private Composite boardAttributesCpt;
	private Group ConfigurationGroup;
	private String eclipsePath = getEclipsePath();
	
	public Board getBoard() {
		String boardName = boardNameField.getText().trim();
		newBoard.setBoardName(boardName);
		newBoard.setOnBoardCpus(onBoardCpus);
		for(int i=0;i<onBoardCpus.size();i++) {
			List<Component> peripherals = onBoardCpus.get(i).getPeripherals();
			for(int j=0;j<peripherals.size();j++) {
				System.out.println("peripherals.get(j).getName():  "+peripherals.get(j).getName());
			}
		}
		return newBoard;
	}
	
	public String getEclipsePath() {
		String fullPath = Platform.getInstallLocation().getURL().toString();
		String eclipsePath = fullPath.substring(6,(fullPath.substring(0,fullPath.length()-1)).lastIndexOf("/")+1);
		return eclipsePath;
	}
	
	protected BoardMainWizard(String pageName) {
		super(pageName);
		// TODO Auto-generated constructor stub
		setPageComplete(false);
	}

	public void changeCpusOn(String cpuName,boolean toAdd) {
		if(toAdd) {
			for(int i=0;i<cpusList.size();i++) {
				String curName = cpusList.get(i).getCpuName();
				if(curName.equals(cpuName)) {
					Cpu curCpu = cpusList.get(i);
					newCpu = new Cpu(curCpu.getCpuName(),curCpu.getCores());
					int count = 0;
					if(cpusOn.size() != 0) {
						for(int j=0;j<cpusOn.size();j++) {
							if(cpusOn.get(j).getCpuName().contains(newCpu.getCpuName())) {
								count++;
							}
						}	
					}
			
					newCpu.setCpuName(count==0?newCpu.getCpuName():newCpu.getCpuName()+"("+count+")");;				
					cpusOn.add(newCpu);				
					break;
				}
			}
		}else {
			for(int i=0;i<cpusOn.size();i++) {
				if(cpusOn.get(i).getCpuName().equals(cpuName)) {
					newCpu =  cpusOn.get(i);
					cpusOn.remove(i);
					break;
				}
			}
		}

	}
	
	public void changePeripheralsOn(String cpudrvName,boolean toAdd) {
		if(toAdd) {
			System.out.println("cpudrvName: "+cpudrvName);
			for(int i=0;i<peripheralsList.size();i++) {
				String curName = peripheralsList.get(i).getName();
				System.out.println("curName: "+curName);
				if(curName.equals(cpudrvName)) {
					newComponent = new Component();
					newComponent.setName(curName);
					System.out.println("true");
					peripheralsList.remove(i);
					peripheralsOn.add(newComponent);	

					break;
				}
			}
		}else {
			for(int i=0;i<peripheralsOn.size();i++) {
				if(peripheralsOn.get(i).getName().equals(cpudrvName)) {
					newComponent =  peripheralsOn.get(i);
					peripheralsOn.remove(i);
					peripheralsList.add(newComponent);
					break;
				}
			}
		}

	}
	
	public void changeChipsOn(String chipName,boolean toAdd) {
		if(toAdd) {
			for(int i=0;i<chipsList.size();i++) {
				String curName = chipsList.get(i).getChipName();
				if(curName.equals(chipName)) {
					newChip = new Chip();
					newChip.setChipName(curName);
					String curChip = chipsList.get(i).getChipName();
					int count = 0;
					if(chipsOn.size() != 0) {
						for(int j=0;j<chipsOn.size();j++) {
							if(chipsOn.get(j).getChipName().contains(curChip)) {
								count++;
							}
						}	
					}		
					if(count!=0) {
						newChip.setChipName(newChip.getChipName()+"("+count+")");
					}		
					chipsOn.add(newChip);
					break;
				}
			}
		}else {
			for(int i=0;i<chipsOn.size();i++) {
				if(chipsOn.get(i).equals(chipName)) {
					newChip = chipsOn.get(i);
					chipsOn.remove(i);
					break;
				}
			}
		}

	}
	
	private  Listener nameModifyListener = e -> {
		boolean valid = validatePage();
		setPageComplete(valid);
	};
	
	/*
	 * Returns the action for the given wizard id, or null if not found.
	 */
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
	
	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(parent, SWT.NONE);
		initializeDialogUnits(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
//		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_CENTER));

		ScrolledComposite scrolledComposite = new ScrolledComposite(composite, SWT.V_SCROLL
                | SWT.H_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite infoArea = new Composite(scrolledComposite, SWT.NONE);
		GridLayout infoLayout = new GridLayout();
		infoLayout.marginHeight = 3;
		infoLayout.numColumns = 1;
		infoArea.setLayout(infoLayout);
		GridData data = new GridData(GridData.FILL_BOTH);
		infoArea.setLayoutData(data);
		
		Composite boardNameCpt = new Composite(infoArea, SWT.NULL);
		GridLayout layoutBoardName = new GridLayout();
		layoutBoardName.numColumns = 2;
		boardNameCpt.setLayout(layoutBoardName);
		boardNameCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label nameLabel = new Label(boardNameCpt, SWT.NONE);
		nameLabel.setText("Board Name: ");
		boardNameField = new Text(boardNameCpt, SWT.BORDER);
		GridData boardNameData = new GridData(GridData.FILL_HORIZONTAL);
		boardNameField.setLayoutData(boardNameData);
		boardNameField.addListener(SWT.Modify, nameModifyListener);
		
		boardAttributesCpt = new Composite(infoArea, SWT.NULL);
		GridLayout layoutAttributes = new GridLayout();
		layoutAttributes.numColumns = 3;
//		layoutAttributes.marginLeft = 20;
		boardAttributesCpt.setLayout(layoutAttributes);
		createTreeForCpus(boardAttributesCpt);
		createTransferButtons(boardAttributesCpt);
		createTreeForNeedCpus(boardAttributesCpt);
		Button newCpuBtn = new Button(boardAttributesCpt,SWT.PUSH);
		newCpuBtn.setText("New Cpu");
		newCpuBtn.setBackground(boardAttributesCpt.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		newCpuBtn.setForeground(boardAttributesCpt.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		newCpuBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		newCpuBtn.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				IAction action = getAction("org.eclipse.cdt.ui.wizards.NewCWizard4");
				action.run();	
				cpuArhives.removeAll();
				ReadCpuXml rcx = new ReadCpuXml();
				cpusList =  rcx.getAllCpus();
				for(int i=0;i<cpusList.size();i++) {
					TreeItem t = new TreeItem(cpuArhives, SWT.NONE);
					t.setText(cpusList.get(i).getCpuName());
				}
				super.widgetSelected(e);
			}
			
		});
		cpuArhives.setSize(170, 180);
		cpuArhivesNeed.setSize(170, 180);
		createContentForAttribute(infoArea);		
		
		Point point = infoArea.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		scrolledComposite.setContent(infoArea);
		scrolledComposite.setMinHeight(point.y);
		scrolledComposite.setExpandHorizontal(true);
	    scrolledComposite.setExpandVertical(true);

		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
		Dialog.applyDialogFont(composite);
	}
	
	private void createContentForAttribute(Composite parent) {
		// TODO Auto-generated method stub
		
		ConfigurationGroup = ControlFactory.createGroup(parent, "Configurations(Enable for no Cpu Selected)", 1);
		ConfigurationGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_FILL | GridData.FILL_HORIZONTAL));
		ConfigurationGroup.setLayout(new GridLayout());
		ConfigurationGroup.setEnabled(false);
		
		folder= new TabFolder(ConfigurationGroup, SWT.NONE | SWT.H_SCROLL | SWT.V_SCROLL);
		folder.setLayout(new TabFolderLayout());
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		folder.setEnabled(false);
		
		TabItem item= new TabItem(folder, SWT.NONE);
		item.setText("时钟"); //$NON-NLS-1$
		item.setControl(createClkContent(folder));

		item= new TabItem(folder, SWT.NONE);
		item.setText("Cpu外设"); //$NON-NLS-1$
		item.setControl(createPeripheralsContent(folder));
		
		TabItem chipItem= new TabItem(folder, SWT.NONE);
		chipItem.setText("板载设备"); //$NON-NLS-1$
		chipItem.setControl(createChipContent(folder));
		
		item= new TabItem(folder, SWT.NONE);
		item.setText("存储"); //$NON-NLS-1$
		item.setControl(createMemoryContent(folder));
//		item.setControl(createNewCpuContent(folder));
		
//		Composite btnCpt = new Composite(ConfigurationGroup,SWT.NONE); 
//		GridLayout btnLayout = new GridLayout();
//		btnCpt.setLayout(btnLayout);
//		btnCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		Button applyBtn = new Button(btnCpt,SWT.PUSH);
//		applyBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.FILL_HORIZONTAL));
//		applyBtn.setText("   Apply   ");
//		applyBtn.addSelectionListener(new SelectionListener() {
//			
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				// TODO Auto-generated method stub
//				TreeItem[] items = cpuArhivesNeed.getSelection();
//				String cpuName = items[0].getText();
//				String mianClk = mainClkField.getText().trim();
//				String rtcClk = rtcClkField.getText().trim();
//				boolean rtcEnable = rtcClkBtn.getSelection();
//				List<Component> peripherals = peripheralsOn;
//				List<Chip> chips = chipsOn;
//				OnBoardCpu onBoardCpu = new OnBoardCpu();
//				onBoardCpu.setCpuName(cpuName);
//				onBoardCpu.setMianClk(Integer.parseInt(mianClk));
//				onBoardCpu.setRtcClk(Integer.parseInt(rtcClk));
//				onBoardCpu.setPeripherals(peripherals);
//				onBoardCpu.setMemorys(memorys);
//				onBoardCpu.setChips(chips);
//				try {
//					IRunnableWithProgress runnable = new IRunnableWithProgress() {
//						@Override
//						public void run(IProgressMonitor monitor)
//								throws InvocationTargetException, InterruptedException {
//							monitor.beginTask("添加板载Cpu……", 100);
//							onBoardCpus.add(onBoardCpu);
//							monitor.worked(10);
//							monitor.done();
//						}
//					};
//					ProgressMonitorDialog dialog = new ProgressMonitorDialog(
//							PlatformUI.getWorkbench().getDisplay().getActiveShell());
//					dialog.setCancelable(false);
//					dialog.run(true, true, runnable);
//				} catch (InvocationTargetException | InterruptedException e1) {
//					e1.printStackTrace();
//				}
//				
//			}
//			
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
//		
	}
	
	private Control createMemoryContent(TabFolder folder) {
		// TODO Auto-generated method stub
		Composite contentCpt = new Composite(folder, SWT.NONE);
		GridLayout contentLayout = new GridLayout();
		contentLayout.numColumns = 2;
		contentCpt.setLayout(contentLayout);
	
		Composite treeCpt = new Composite(contentCpt, SWT.NONE);
		treeCpt.setLayout(new GridLayout());
		treeCpt.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		
		memoryTree = new Tree(treeCpt, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		memoryTree.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		memoryTree.setHeaderVisible(true);
		memoryTree.setSize(150, 170);
		final TreeColumn columnMemory = new TreeColumn(memoryTree, SWT.NONE);
		columnMemory.setText("板载Memory");
		columnMemory.setWidth(120);
		columnMemory.setResizable(false);
		columnMemory.setToolTipText("Cpu Arhives");
		memoryTree.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = memoryTree.getSelection();
				if(items.length>0) {
					String selectMemory = items[0].getText().trim();
					OnBoardMemory selectedMemory = null;
					for(int i=0;i<memorys.size();i++) {
						if(memorys.get(i).getName().equals(selectMemory)) {
							selectedMemory = memorys.get(i);
							break;
						}
					}
					String[] typeItems = memoryTypeCombo.getItems();
					if(selectedMemory.getType() != null) {
						for(int i=0;i<typeItems.length;i++) {
							if(typeItems[i].equals(selectedMemory.getType())) {
								memoryTypeCombo.select(i);
								break;
							}
						}
					}else{
						memoryTypeCombo.setText("");
					}

					if(selectedMemory.getStartAddr() != null) {
						addrField.setText(selectedMemory.getStartAddr());
					}else {
						addrField.setText("");
					}
					if(selectedMemory.getSize() != null) {
						sizeField.setText(selectedMemory.getSize());
					}else {
						sizeField.setText("");
					}
				}
								
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		Composite btnCpt = new Composite(treeCpt, SWT.NONE);
		btnCpt.setLayout(new GridLayout(2,true));
		btnCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_CENTER));
		Button addBtn = new Button(btnCpt,SWT.PUSH);
		addBtn.setText("添加");
		Button deleteBtn = new Button(btnCpt,SWT.PUSH);
		deleteBtn.setText("删除");
		
		addBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int memoryCount = memoryTree.getItemCount();
				TreeItem t = new TreeItem(memoryTree, SWT.NONE);
				TreeItem[] items = memoryTree.getItems();
				int max = 0;
				if (memoryCount > 0) {
					 String maxString = items[memoryCount-1].getText();
					 max = Integer.parseInt(maxString.substring(6,maxString.length()));
				}
				t.setText("memory"+(max+1));
				OnBoardMemory memory = new OnBoardMemory();
				memory.setName("memory"+(max+1));
				memorys.add(memory);
				
				TreeItem[] cpuItems = cpuArhivesNeed.getSelection();
				OnBoardCpu onBoardCpu = new OnBoardCpu();
				if (cpuItems.length > 0) {
					String selectCpuName = cpuItems[0].getText();
					for (int i = 0; i < onBoardCpus.size(); i++) {
						if (onBoardCpus.get(i).getCpuName().equals(selectCpuName)) {
							onBoardCpus.get(i).setMemorys(memorys);
						}
					}
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		deleteBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = memoryTree.getSelection();
				if (items.length > 0) {
					for(int i=0;i<memorys.size();i++) {
						if(memorys.get(i).getName().equals(items[0].getText().trim())) {
							memorys.remove(i);
						}
					}
					TreeItem[] cpuItems = cpuArhivesNeed.getSelection();
					OnBoardCpu onBoardCpu = new OnBoardCpu();
					if (cpuItems.length > 0) {
						String selectCpuName = cpuItems[0].getText();
						for (int i = 0; i < onBoardCpus.size(); i++) {
							if (onBoardCpus.get(i).getCpuName().equals(selectCpuName)) {
								onBoardCpus.get(i).setMemorys(memorys);
							}
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
		
		Composite detailsCpt = new Composite(contentCpt, SWT.BORDER);
		GridLayout detailsLayout = new GridLayout();
		detailsLayout.marginHeight = 5;
		detailsLayout.numColumns = 2;
		detailsCpt.setLayout(detailsLayout);
		detailsCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		Label typeLabel = new Label(detailsCpt,SWT.NONE);
		typeLabel.setText("类型: ");

		memoryTypeCombo = new Combo(detailsCpt,SWT.READ_ONLY);
		memoryTypeCombo.add("ROM");
		memoryTypeCombo.add("RAM");
		memoryTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		Label newLabel = new Label(detailsCpt,SWT.NONE);
//		newLabel.setVisible(false);
		memoryTypeCombo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int comboIndex = memoryTypeCombo.getSelectionIndex();
				TreeItem[] items = memoryTree.getSelection();
				if(items.length>0) {
					String selectMemory = items[0].getText().trim();
					for(int i=0;i<memorys.size();i++) {
						OnBoardMemory memory = memorys.get(i);
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
		
		Label addrLabel = new Label(detailsCpt,SWT.NONE);
		addrLabel.setText("地址: ");
		addrField = new Text(detailsCpt,SWT.BORDER);
		addrField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		newLabel = new Label(detailsCpt,SWT.NONE);
//		newLabel.setVisible(false);
		addrField.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = memoryTree.getSelection();
				if(items.length>0) {
					String selectMemory = items[0].getText().trim();
					for(int i=0;i<memorys.size();i++) {
						OnBoardMemory memory = memorys.get(i);
						if(memory.getName().equals(selectMemory)) {
							String addr = addrField.getText().trim();
							memory.setStartAddr(addr);
							break;
						}
					}
				}
		
			}
		});
		
		Label sizeLabel = new Label(detailsCpt,SWT.NONE);
		sizeLabel.setText("大小: ");
		sizeField = new Text(detailsCpt,SWT.BORDER);
		sizeField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		ControlFactory.createLabel(detailsCpt, "K");
		sizeField.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = memoryTree.getSelection();
				if(items.length>0) {
					String selectMemory = items[0].getText().trim();
					String size = sizeField.getText().trim();
					if(! size.equals("")) {
						for(int i=0;i<memorys.size();i++) {
							OnBoardMemory memory = memorys.get(i);
							if(memory.getName().equals(selectMemory)) {
								
								memory.setSize(size);
								break;
							}
						}
					}
					
				}
			}
		});

		return contentCpt;
	}

	private Control createChipContent(TabFolder folder) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(folder, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
//		layoutAttributes.marginHeight = 10;
		composite.setLayout(layout);
		
		Composite compositeTree = new Composite(composite, SWT.NULL);
		chipTree = new Tree(compositeTree, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		chipTree.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		chipTree.setHeaderVisible(true);
		final TreeColumn columnCpudrvs = new TreeColumn(chipTree, SWT.NONE);
		columnCpudrvs.setText("Chip列表");
		columnCpudrvs.setWidth(90);
		columnCpudrvs.setResizable(false);
		columnCpudrvs.setToolTipText("Cpu Drivers");
		
		File cpudrvFile = new File(eclipsePath+"djysrc/bsp/chipdrv");
		File[] files = cpudrvFile.listFiles();
		chipsList = new ArrayList<Chip>();
		for(int i=0;i<files.length;i++) {
			TreeItem t = new TreeItem(chipTree, SWT.NONE);
			t.setText(files[i].getName());
			Chip chip = new Chip();
			chip.setChipName(files[i].getName());
			chipsList.add(chip);
		}
		chipTree.setSize(160, 155);
		Composite btnCpt = new Composite(composite, SWT.NULL);
		btnCpt.setLayout(new GridLayout());
		btnCpt.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.HORIZONTAL_ALIGN_CENTER));
		Button goto2Btn,back2Btn;
		goto2Btn = new Button(btnCpt,SWT.PUSH);
		goto2Btn.setText("   》》  ");
		back2Btn = new Button(btnCpt,SWT.PUSH);
		back2Btn.setText(" 《《    ");
		
		goto2Btn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = chipTree.getSelection();
				if (items.length > 0) {
					String selectChipName = items[0].getText();
					changeChipsOn(selectChipName, true);
					// items[0].dispose();
					updateChipOn();
					
					TreeItem[] cpuItems = cpuArhivesNeed.getSelection();
					OnBoardCpu onBoardCpu = new OnBoardCpu();
					if (cpuItems.length > 0) {
						String selectCpuName = cpuItems[0].getText();
						for (int i = 0; i < onBoardCpus.size(); i++) {
							if (onBoardCpus.get(i).getCpuName().equals(selectCpuName)) {
								onBoardCpus.get(i).setChips(chipsOn);
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
		
		back2Btn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = chipOnTree.getSelection();
				if(items.length>0) {
					String selectChipName = items[0].getText();
					changeChipsOn(selectChipName, false);
					
					TreeItem[] cpuItems = cpuArhivesNeed.getSelection();
					OnBoardCpu onBoardCpu = new OnBoardCpu();
					if (cpuItems.length > 0) {
						String selectCpuName = cpuItems[0].getText();
						for (int i = 0; i < onBoardCpus.size(); i++) {
							if (onBoardCpus.get(i).getCpuName().equals(selectCpuName)) {
								onBoardCpus.get(i).setChips(chipsOn);
							}
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
		chipTree.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				goto2Btn.setEnabled(true);
				back2Btn.setEnabled(false);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		Composite chipTreeOn = new Composite(composite, SWT.NULL);
		chipOnTree = new Tree(chipTreeOn, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		chipOnTree.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		chipOnTree.setHeaderVisible(true);
		chipsOn = new ArrayList<Chip>();
		final TreeColumn columnCpudrvsOn = new TreeColumn(chipOnTree, SWT.NONE);
		columnCpudrvsOn.setText("板载Chip");
		columnCpudrvsOn.setWidth(120);
		columnCpudrvsOn.setResizable(false);
		columnCpudrvsOn.setToolTipText("Cpu DriversOn");
		chipOnTree.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				goto2Btn.setEnabled(false);
				back2Btn.setEnabled(true);
				TreeItem[] items = chipOnTree.getSelection();
				if(items.length>0) {
					String selectChip = items[0].getText().trim();
					Chip selectedChip = null;
					for(int i=0;i<chipsOn.size();i++) {
						if(chipsOn.get(i).getChipName().equals(selectChip)) {
							selectedChip = chipsOn.get(i);
							break;
						}
					}
//					String[] interfaceItems = interfaceCombo.getItems();
//					for(int i=0;i<interfaceItems.length;i++) {
//						if(interfaceItems[i].equals(selectedChip.getTheInterface())) {
//							interfaceCombo.select(i);
//							break;
//						}
//					}
//					interfaceCombo.setEnabled(true);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});

		chipOnTree.setSize(160, 155);
		
		Composite peripheralCpt = new Composite(composite, SWT.NULL);
		GridLayout peripheralLayout = new GridLayout();
		peripheralCpt.setLayout(peripheralLayout);
		peripheralCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		Label interfaceLabel = new Label(peripheralCpt,SWT.NONE);
//		interfaceLabel.setText("与Cpu接口: ");
//		interfaceCombo = new Combo(peripheralCpt,SWT.READ_ONLY);
//		interfaceCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		interfaceCombo.setEnabled(false);
//		interfaceCombo.addSelectionListener(new SelectionListener() {
//			
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				// TODO Auto-generated method stub
//				int comboIndex = interfaceCombo.getSelectionIndex();
//				TreeItem[] items = chipOnTree.getSelection();
//				if(items.length>0) {
//					String selectChip = items[0].getText().trim();
//					for(int i=0;i<chipsOn.size();i++) {
//						Chip chip = chipsOn.get(i);
//						if(chip.getChipName().equals(selectChip)) {
//							String theInterface = interfaceCombo.getItem(comboIndex);
//							chip.setTheInterface(theInterface);
//							break;
//						}
//					}
//				}
//				
//			}
//			
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
		
		return composite;
	}

	private Control createPeripheralsContent(TabFolder folder) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(folder, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginLeft = 30;
		composite.setLayout(layout);
		
		Composite compositeTree = new Composite(composite, SWT.NULL);
		cpudrvTree = new Tree(compositeTree, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		cpudrvTree.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		cpudrvTree.setHeaderVisible(true);
		final TreeColumn columnCpudrvs = new TreeColumn(cpudrvTree, SWT.NONE);
		columnCpudrvs.setText("外设列表");
		columnCpudrvs.setWidth(120);
		columnCpudrvs.setResizable(false);
		columnCpudrvs.setToolTipText("Cpu Drivers");
		cpudrvTree.setSize(160, 155);
		
		Composite btnCpt = new Composite(composite, SWT.NULL);
		btnCpt.setLayout(new GridLayout());
		btnCpt.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.HORIZONTAL_ALIGN_CENTER));
		Button goto1Btn,back1Btn;
		goto1Btn = new Button(btnCpt,SWT.PUSH);
		goto1Btn.setText("   》》  ");
		back1Btn = new Button(btnCpt,SWT.PUSH);
		back1Btn.setText(" 《《    ");
		
		goto1Btn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = cpudrvTree.getSelection();
				if(items.length>0) {

					String selectCpudrvName = items[0].getText();
					changePeripheralsOn(selectCpudrvName,true);
					
					updatePeripheralsOn();
					
//					interfaceCombo.removeAll();
//					TreeItem[] itemsOn = cpudrvOnTree.getItems();
//					for(int i=0;i<itemsOn.length;i++) {
//						interfaceCombo.add(itemsOn[i].getText());
//					}
					
					TreeItem[] cpuItems = cpuArhivesNeed.getSelection();
					OnBoardCpu onBoardCpu = new OnBoardCpu();
					if (cpuItems.length > 0) {
						String selectCpuName = cpuItems[0].getText();
						for (int i = 0; i < onBoardCpus.size(); i++) {
							if (onBoardCpus.get(i).getCpuName().equals(selectCpuName)) {
								onBoardCpus.get(i).setPeripherals(peripheralsOn);
							}
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
		
		back1Btn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				goto1Btn.setEnabled(false);
				back1Btn.setEnabled(true);
				TreeItem[] items = cpudrvOnTree.getSelection();
				if(items.length>0) {
					
					String selectCpudrvName = items[0].getText();
					changePeripheralsOn(selectCpudrvName,false);

					updatePeripheralsList();
					
					TreeItem[] cpuItems = cpuArhivesNeed.getSelection();
					OnBoardCpu onBoardCpu = new OnBoardCpu();
					if (cpuItems.length > 0) {
						String selectCpuName = cpuItems[0].getText();
						for (int i = 0; i < onBoardCpus.size(); i++) {
							if (onBoardCpus.get(i).getCpuName().equals(selectCpuName)) {
								onBoardCpus.get(i).setPeripherals(peripheralsOn);
							}
						}
					}
					
					items[0].dispose();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				goto1Btn.setEnabled(true);
				back1Btn.setEnabled(false);
			}
		});
		
		cpudrvTree.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				goto1Btn.setEnabled(true);
				back1Btn.setEnabled(false);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		Composite compositeTreeOn = new Composite(composite, SWT.NULL);
		cpudrvOnTree = new Tree(compositeTreeOn, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		cpudrvOnTree.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		cpudrvOnTree.setHeaderVisible(true);
		cpudrvOnTree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeItem[] items = cpudrvOnTree.getSelection();
				goto1Btn.setEnabled(false);
				back1Btn.setEnabled(true);
			}
		});
		peripheralsOn = new ArrayList<Component>();
		final TreeColumn columnCpudrvsOn = new TreeColumn(cpudrvOnTree, SWT.NONE);
		columnCpudrvsOn.setText("用到的外设");
		columnCpudrvsOn.setWidth(120);
		columnCpudrvsOn.setResizable(false);
		columnCpudrvsOn.setToolTipText("Cpu DriversOn");

		cpudrvOnTree.setSize(160, 155);
		
		return composite;
	}
	
	private Control createClkContent(TabFolder folder) {
		// TODO Auto-generated method stub
		Composite contentCpt = new Composite(folder, SWT.BORDER_DOT);
		GridLayout layoutContent = new GridLayout();
		layoutContent.numColumns = 3;
		layoutContent.marginHeight = 30;
		contentCpt.setLayout(layoutContent);
		contentCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | SWT.BORDER));

		Label mainClkLabel = new Label(contentCpt, SWT.NONE);
		mainClkLabel.setText("晶振频率: ");
		mainClkField = new Text(contentCpt, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		mainClkField.setLayoutData(data);
		ControlFactory.createLabel(contentCpt, "Mhz");
		mainClkField.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				// TODO Auto-generated method stub
				if(! mainClkField.getText().trim().equals("")) {
					TreeItem[] items = cpuArhivesNeed.getSelection();
					if(items.length>0) {
						OnBoardCpu onBoardCpu = new OnBoardCpu();
						if (items.length > 0) {
							String selectCpuName = items[0].getText();
							for(int i=0;i<onBoardCpus.size();i++) {
								if(onBoardCpus.get(i).getCpuName().equals(selectCpuName)) {
									onBoardCpus.get(i).setMianClk(Integer.parseInt(mainClkField.getText().trim()));
								}
							}
						}
					}
					
				}			
			}
		});
		
	    rtcClkBtn = new Button(contentCpt, SWT.CHECK);
	    rtcClkBtn.setText("rtc时钟: ");
		rtcClkField = new Text(contentCpt, SWT.BORDER);
		rtcClkField.setEnabled(false);
		rtcClkField.setLayoutData(data);
		rtcClkBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				if(rtcClkBtn.getSelection()) {
					rtcClkField.setEnabled(true);;
				}else {
					rtcClkField.setEnabled(false);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		ControlFactory.createLabel(contentCpt, "hz");
		rtcClkField.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				// TODO Auto-generated method stub
				if(! rtcClkField.getText().trim().equals("")) {
					TreeItem[] items = cpuArhivesNeed.getSelection();
					OnBoardCpu onBoardCpu = new OnBoardCpu();
					if (items.length > 0) {
						String selectCpuName = items[0].getText();
						for (int i = 0; i < onBoardCpus.size(); i++) {
							if (onBoardCpus.get(i).getCpuName().equals(selectCpuName)) {
								onBoardCpus.get(i).setRtcClk(Integer.parseInt(rtcClkField.getText().trim()));
							}
						}
					}
				}
			}
		});

		return contentCpt;
	}

	private void createTreeForNeedCpus(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(parent, SWT.NULL);
		cpuArhivesNeed = new Tree(composite, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		cpuArhivesNeed.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		cpuArhivesNeed.setHeaderVisible(true);
		cpuArhivesNeed.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				gotoBtn.setEnabled(false);
				backBtn.setEnabled(true);
				TreeItem[] items = cpuArhivesNeed.getSelection();
				if (items.length > 0) {
					OnBoardCpu onBoardCpu = new OnBoardCpu();
					String selectCpuName = items[0].getText();
					ConfigurationGroup.setEnabled(true);
					folder.setEnabled(true);
					ConfigurationGroup.setText("Configurations for "+selectCpuName);
					for(int i=0;i<onBoardCpus.size();i++) {
						if(onBoardCpus.get(i).getCpuName().equals(selectCpuName)) {
							onBoardCpu = onBoardCpus.get(i);
							break;
						}
					}
					if(onBoardCpu.getMianClk()!=0) {
						mainClkField.setText(String.valueOf(onBoardCpu.getMianClk()));
					}else {
						mainClkField.setText("");
					}
					
					if(onBoardCpu.getRtcClk()!=0) {
						rtcClkField.setText(String.valueOf(onBoardCpu.getRtcClk()));
					}else {
						rtcClkField.setText("");
					}
					
					ReadComponentXml rcx = new ReadComponentXml();
					String cpuSrcPath = getCpuSrcPath(selectCpuName);
					peripheralsList = rcx.getPeripherals(new File(cpuSrcPath));
					for(int i=0;i<peripheralsList.size();i++) {
						Component component = new Component();
						component.setName(peripheralsList.get(i).getName());
						allPeripherals.add(component);
//						thePeripherals.add(component);
					}
					
					cpudrvTree.removeAll();
					cpudrvOnTree.removeAll();
//					interfaceCombo.removeAll();
					List<Component> boardPeripherals = onBoardCpu.getPeripherals();
					thePeripherals = new ArrayList<Component>();
					for(int i=0;i<allPeripherals.size();i++) {
						Component component = new Component();
						component.setName(allPeripherals.get(i).getName());
						thePeripherals.add(component);
					}
					int perSize = allPeripherals.size();
					//板载外设为空
					if(boardPeripherals == null) {
						peripheralsOn = new ArrayList<Component>();
						for(int i=0;i<allPeripherals.size();i++) {
							TreeItem t = new TreeItem(cpudrvTree, SWT.NONE);
							t.setText(allPeripherals.get(i).getName());
						}
					}else {		//板载外设不为空	
						peripheralsOn = boardPeripherals;					
						for(int j=0;j<boardPeripherals.size();j++) {//u1 u2
							String peripheraOnlName = boardPeripherals.get(j).getName();
							System.out.println("peripheraOnlName: "+peripheraOnlName);
							for(int i=0;i<thePeripherals.size();i++) {// nand u1 u2
								String peripheralName = thePeripherals.get(i).getName();
								if(peripheralName.equals(peripheraOnlName)) {
									TreeItem t = new TreeItem(cpudrvOnTree, SWT.NONE);
									t.setText(peripheraOnlName);
//									interfaceCombo.add(peripheraOnlName);
									thePeripherals.remove(i);
									break;
								}
							}
						}
						for(int j=0;j<thePeripherals.size();j++) {
							TreeItem t = new TreeItem(cpudrvTree, SWT.NONE);
							t.setText(thePeripherals.get(j).getName());
						}
					}
					
					List<Chip> chips = onBoardCpu.getChips();
					chipOnTree.removeAll();
					if(chips != null) {
						chipsOn = chips;
						for(int i=0;i<chips.size();i++) {
							String chipOnName = chips.get(i).getChipName();
							TreeItem t = new TreeItem(chipOnTree, SWT.NONE);
							t.setText(chipOnName);
						}
					}else {
						chipsOn = new ArrayList<Chip>();
					}
					
					List<OnBoardMemory> memorysOn = onBoardCpu.getMemorys();
					memoryTree.removeAll();
					if(memorysOn == null) {
						memorys = new ArrayList<OnBoardMemory>();
						memoryTypeCombo.setText("");
						addrField.setText("");
						sizeField.setText("");
					}else {
						memorys = memorysOn;
						for(int i=0;i<memorysOn.size();i++) {
							String memoryOnName = memorysOn.get(i).getName();
							TreeItem t = new TreeItem(memoryTree, SWT.NONE);
							t.setText(memoryOnName);
						}
					}
				
//					updateConfigurationIntereface();
				}
			}
		});
		cpusOn = new ArrayList<Cpu>();
		final TreeColumn columnLanguages = new TreeColumn(cpuArhivesNeed, SWT.NONE);
		columnLanguages.setText("板载Cpu");
		columnLanguages.setWidth(140);
		columnLanguages.setResizable(false);
		columnLanguages.setToolTipText("Cpu On-board");
	}

	private void createTransferButtons(Composite parent) {
		// TODO Auto-generated method stub
		Composite btnCpt = new Composite(parent, SWT.NULL);
		btnCpt.setLayout(new GridLayout());
		btnCpt.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.HORIZONTAL_ALIGN_CENTER));
		
		gotoBtn = new Button(btnCpt,SWT.PUSH);
		gotoBtn.setText("   》》  ");
		backBtn = new Button(btnCpt,SWT.PUSH);
		backBtn.setText(" 《《    ");
		
		gotoBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = cpuArhives.getSelection();
				if(items.length>0) {
					String selectCpuName = items[0].getText();
					changeCpusOn(selectCpuName,true);
//					items[0].dispose();
					
					OnBoardCpu onBoardCpu = new OnBoardCpu();
					onBoardCpu.setCpuName(selectCpuName);
					onBoardCpus.add(onBoardCpu);
					updateCpuOn();
					boolean valid = validatePage();
					setPageComplete(valid);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		backBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = cpuArhivesNeed.getSelection();
				if(items.length>0) {
					String selectCpuName = items[0].getText();
					changeCpusOn(selectCpuName,false);
					for(int i=0;i<onBoardCpus.size();i++) {
						if(onBoardCpus.get(i).getCpuName().equals(selectCpuName)) {
							onBoardCpus.remove(i);
						}
					}
					items[0].dispose();
//					updateCpuList();
					boolean valid = validatePage();
					setPageComplete(valid);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
	
	}

	private void createTreeForCpus(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(parent, SWT.NULL);
		cpuArhives = new Tree(composite, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		cpuArhives.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		cpuArhives.setHeaderVisible(true);
		final TreeColumn columnArhives = new TreeColumn(cpuArhives, SWT.NONE);
		columnArhives.setText("Cpu列表");
		columnArhives.setWidth(140);
		columnArhives.setResizable(false);
		columnArhives.setToolTipText("Cpu Arhives");
		ReadCpuXml rcx = new ReadCpuXml();
		cpusList =  rcx.getAllCpus();
		for(int i=0;i<cpusList.size();i++) {
			TreeItem t = new TreeItem(cpuArhives, SWT.NONE);
			t.setText(cpusList.get(i).getCpuName());
		}
		cpuArhives.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				gotoBtn.setEnabled(true);
				backBtn.setEnabled(false);
				TreeItem[] selection = cpuArhives.getSelection();

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}
			
		});
	}

	private boolean validatePage() {
		if(boardNameField.getText().trim().equals("")) {
			setErrorMessage("Board name must be specified");
			return false;
		}else {
			setErrorMessage(null);
			setMessage(null);
		}
		if(cpuArhivesNeed.getItems().length == 0) {
			ConfigurationGroup.setText("Configurations(Enable for no Cpu Selected)");
			ConfigurationGroup.setEnabled(false);
			folder.setEnabled(false);
			setErrorMessage("Must Select a Cpu at least");
			return false;
		}else {
			setErrorMessage(null);
			setMessage(null);
		}
		return true;
	}
	
	private void updatePeripheralsList() {
		TreeItem t = new TreeItem(cpudrvTree, SWT.NONE);
		if(newComponent != null) {
			t.setText(newComponent.getName());
//			interfaceCombo.remove(newComponent.getName());
		}
	}
	
	private void updateCpuList() {
		TreeItem t = new TreeItem(cpuArhives, SWT.NONE);
		if(newCpu != null) {
			t.setText(newCpu.getCpuName());
		}
	}
	
	private void updateCpuOn() {
		TreeItem t = new TreeItem(cpuArhivesNeed, SWT.NONE);
		if(newCpu != null) {
			t.setText(newCpu.getCpuName());
		}
	}
	
	private void updateChipOn() {
		TreeItem t = new TreeItem(chipOnTree, SWT.NONE);
		t.setText(newChip.getChipName());
	}
	
	private void updatePeripheralsOn() {
		TreeItem t = new TreeItem(cpudrvOnTree, SWT.NONE);
		t.setText(newComponent.getName());
//		interfaceCombo.add(newComponent.getName());
	}
	
	private String deapPath = null;
	private String getCpuSrcPath(String cpuName) {
		String sourcePath = eclipsePath+"djysrc/bsp/cpudrv";
		File sourceFile = new File(sourcePath);
		File[] files = sourceFile.listFiles();
		String path = null;
		for(File file:files){//cpudrv下的所有文件 Atmel stm32
			if(file.isDirectory()) {
				getDeapPath(file,cpuName);
				if(deapPath!=null) {
					path = deapPath+"/../src";
					break;
				}	
			}	
		}
		return path;
	}

	public String getDeapPath(File sourceFile,String cpuName) {
		File[] files = sourceFile.listFiles();
		String path = null;
		for (File file : files) {
			if (file.isDirectory()) {
				if(file.getName().equals(cpuName)) {
					System.out.println("cpuName:  "+cpuName);
					deapPath = file.getPath();
					break;
				}else if(!file.getName().equals("include") && !file.getName().equals("src")){
					getDeapPath(file,cpuName);//如果为目录，则继续扫描该目录
				}			
			}
		}
		return deapPath;
	}
	
}
