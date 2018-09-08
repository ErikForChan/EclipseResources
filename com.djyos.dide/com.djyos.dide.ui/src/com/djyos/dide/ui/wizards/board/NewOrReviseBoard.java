package com.djyos.dide.ui.wizards.board;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
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
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
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
import org.eclipse.ui.wizards.IWizardDescriptor;

import com.djyos.dide.ui.wizards.board.onboardcpu.Chip;
import com.djyos.dide.ui.wizards.board.onboardcpu.OnBoardCpu;
import com.djyos.dide.ui.wizards.board.onboardcpu.OnBoardMemory;
import com.djyos.dide.ui.wizards.component.Component;
import com.djyos.dide.ui.wizards.component.ReadComponent;
import com.djyos.dide.ui.wizards.component.ReadComponentXml;
import com.djyos.dide.ui.wizards.cpu.Cpu;
import com.djyos.dide.ui.wizards.cpu.ReadCpuXml;
import com.djyos.dide.ui.wizards.cpu.core.Core;
import com.djyos.dide.ui.wizards.djyosProject.tools.DPluginImages;
import com.djyos.dide.ui.wizards.djyosProject.tools.DeleteFolder;
import com.djyos.dide.ui.wizards.djyosProject.tools.DideHelper;

import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;

public class NewOrReviseBoard extends StatusDialog{

	private Tree cpuArhives, cpuArhivesNeed, chipTree, chipOnTree, cpudrvTree, cpudrvOnTree, memoryTree;
	private Button gotoBtn = null, backBtn = null;
	private List<OnBoardCpu> onBoardCpus = new ArrayList<OnBoardCpu>();
	private Cpu newCpu;
	private Component newComponent;
	private Chip newChip;
	private Text boardNameField, mainClkField, rtcClkField, addrField, sizeField;
	private TabFolder folder;
	private Combo interfaceCombo, memoryTypeCombo;
	private Label mainClkLabel;
	private Button rtcClkBtn,addBtn,deleteBtn;
	private Board boardInit;
	private List<Cpu> cpusList = null, cpusOn = null;
	private List<Component> peripheralsList = new ArrayList<Component>();;// 外设列表
	private List<Component> peripheralsOn = new ArrayList<Component>();// 用到的外设
	private List<Component> allPeripherals;// 所有外设
	private List<Chip> chipsList = null, chipsOn = null;
	private List<OnBoardMemory> memorys = new ArrayList<OnBoardMemory>();
	private List<Component> thePeripherals;// 点击板载cpu时用到的临时所有外设
	private ReadComponentXml rcx = new ReadComponentXml();
	private Composite boardAttributesCpt;
	private Group ConfigurationGroup;
	private	DideHelper dideHelper = new DideHelper();	
	private String didePath = dideHelper.getDIDEPath();
	private IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	public String lastBoardName;

	private List<Board> getBoards(){
		ReadBoardXml rbx = new ReadBoardXml();
		List<String> paths = new ArrayList<String>();
		List<Board> boards = new ArrayList<Board>();
		String userBoardFilePath = dideHelper.getUserBoardFilePath();
		String demoBoardFilePath = dideHelper.getDemoBoardFilePath();
		paths.add(userBoardFilePath);
		paths.add(demoBoardFilePath);

		for(int i=0;i<paths.size();i++) {
			File boardFile = new File(paths.get(i));
			File[] files = boardFile.listFiles();
			for(int j=0;j<files.length;j++){
					File file = files[j];
					if(file.isDirectory()) {
						File[] mfiles = file.listFiles();
						for(int k=0;k<mfiles.length;k++) {
							if(mfiles[k].getName().endsWith(".xml")) {
								try {
									Board board = rbx.getBoard(mfiles[k]);
									boards.add(board);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								break;
							}
						}
					}
			}
		}
		
		return boards;
	}
	
	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		lastBoardName = boardNameField.getText().trim();
		String vaildMsg = vaildPage();
		if(vaildMsg!=null) {
			MessageDialog.openInformation(window.getShell(), "提示",
					vaildMsg);
		}else {
			Board board = getBoard();
			String dirPath = dideHelper.getUserBoardFilePath()+"/"+lastBoardName;
			String xmlPath = dirPath+"/Board_"+lastBoardName+".xml";
			File file = new File(xmlPath);
			if(boardInit!=null) {
				DeleteFolder df = new DeleteFolder();
				df.deleteDirectory(boardInit.getBoardPath());
			}
			
			try {
				IRunnableWithProgress runnable = new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						if(boardInit!=null) {
							monitor.beginTask("修改板件配置信息……", 100);
						}else{
							monitor.beginTask("创建板件……", 100);
						}
						
						CreatBoardXml ctbx = new CreatBoardXml();
						File boardDir = new File(dirPath);
						boardDir.mkdirs();
						if (!file.exists()) {
							try {
								file.createNewFile();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						ctbx.creatBoardXml(board, file);
						monitor.worked(10);
						monitor.done();
					}
				};
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(
						PlatformUI.getWorkbench().getDisplay().getActiveShell());
				dialog.setCancelable(false);
				dialog.run(true, true, runnable);
			} catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
			}
			super.okPressed();
		}
	}
	
	public NewOrReviseBoard(Shell parent,boolean toNew,Board board) {
		super(parent);
		// TODO Auto-generated constructor stub
		if(toNew) {
			setTitle("新建板件");
		}else {
			setTitle("维护板件");
			boardInit = board;
		}
		setShellStyle(getShellStyle() | SWT.CLOSE | SWT.MAX);
	}
	
	@Override
	protected Point getInitialSize() {
		// TODO Auto-generated method stub
		return new Point(500, 660);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		createDynamicGroup(composite);

		return super.createDialogArea(parent);
	}

	public String vaildPage() {
		String curBoardName = boardNameField.getText().trim();
		if(boardInit==null) {
			List<Board> boards = getBoards();
			for(Board board:boards){
				if(board.getBoardName().equals(curBoardName)) {
					return "该板件已存在，请重填板件名称！";
				}
			}
		}
		for(OnBoardCpu onBoardCpu:onBoardCpus) {
			String cpuName = onBoardCpu.getCpuName();
			List<OnBoardMemory>  memorys = onBoardCpu.getMemorys();
			for(int i=0;i<memorys.size();i++) {
				OnBoardMemory memory = memorys.get(i);
				if(memory.getStartAddr()==null || memory.getSize()==null || memory.getType() == null) {
					return "请完整填写["+cpuName+"]的存储信息";
				}else {
					int memoryInt = Integer.parseInt(memory.getSize());
					if(memory.getSize().equals("0") || memory.getSize().equals("0x")) {	
						return onBoardCpu.getCpuName()+"的存储memory"+(i+1)+"大小需大于0";
					}
				}
			}
			if(onBoardCpu.getMianClk()==0) {
				return "请完整填写["+cpuName+"]的晶振频率";
			}
			if(onBoardCpu.getRtcClk()==-1) {
				return "请完整填写["+cpuName+"]的Rtc频率";
			}
		}
		return null;
	}
	
 	private void enableOperate(boolean enable) {
		mainClkLabel.setEnabled(enable);
		mainClkField.setEnabled(enable);
		rtcClkBtn.setEnabled(enable);
		chipTree.setEnabled(enable);
		chipOnTree.setEnabled(enable);
		cpudrvTree.setEnabled(enable);
		cpudrvOnTree.setEnabled(enable);
		memoryTree.setEnabled(enable);
		addBtn.setEnabled(enable);	
	}

	public void enableMemory(boolean enable) {
		deleteBtn.setEnabled(enable);
		addrField.setEnabled(enable);
		sizeField.setEnabled(enable);
		memoryTypeCombo.setEnabled(enable);
	}
	
	public Board getBoard() {
		Board newBoard = new Board();
		String boardName = lastBoardName;
		newBoard.setBoardName(boardName);
		if(boardInit==null) {
			newBoard.setBoardPath((dideHelper.getUserBoardFilePath()+"/"+lastBoardName).replace("\\", "/"));
		}
		newBoard.setOnBoardCpus(onBoardCpus);
		return newBoard;
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
				if(chipsOn.get(i).getChipName().equals(chipName)) {
					newChip = chipsOn.get(i);
					chipsOn.remove(i);
					break;
				}
			}
		}

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
				enableMemory(true);
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
		addBtn = new Button(btnCpt,SWT.PUSH);
		addBtn.setText("添加");
		deleteBtn = new Button(btnCpt,SWT.PUSH);
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
				if(memoryTree.getItemCount()<1) {
					enableMemory(false);
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

		addrField.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = memoryTree.getSelection();
				String addr = addrField.getText().trim();
				if(items.length>0) {
					if(!addr.equals("")) {
						String selectMemory = items[0].getText().trim();
						int curNum = -1;
						Pattern pattern = Pattern.compile("^([1-9]\\d*|[0]{1,1})$");  //含0正整数
	    		        boolean isInt =  pattern.matcher(addr).matches();
	    				if(addr.startsWith("0x")) {
	    					curNum = Integer.parseInt(addr.substring(2),16);
	    				}else {
	    					if(isInt) {
	    						curNum = Integer.parseInt(addr);
	    					}
	    				}             					
        				if(curNum<0) {
        					addrField.setText("");
        					MessageDialog.openInformation(window.getShell(), "提示",
        							"请输入正整数(包含0)");
        				}else {
        					for(int i=0;i<memorys.size();i++) {
    							OnBoardMemory memory = memorys.get(i);
    							if(memory.getName().equals(selectMemory)) {
    								memory.setStartAddr(addr);
    								break;
    							}
    						}
        				}
					}else {
//						setPageComplete(false);
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
				String size = sizeField.getText().trim().replace("k", "");
				if(items.length>0) {
					String selectMemory = items[0].getText().trim();
					IWorkbenchWindow window = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow();
					if(! size.equals("")) {
						int curNum = -1;         
						Pattern pattern = Pattern.compile("^[+]{0,1}(\\d+)$");  //^[-\\+]?[\\d]*$
	    		        boolean isInt =  pattern.matcher(size).matches();
	    				if(size.startsWith("0x") && !size.trim().equals("0x")) {
	    					curNum = Integer.parseInt(size.substring(2),16);
	    				}else {
	    					if(isInt) {
	    						curNum = Integer.parseInt(size);
	    					}
	    				}             					
        				if(curNum<0) {
        					sizeField.setText("");
        					MessageDialog.openInformation(window.getShell(), "提示",
        							"请输入正整数(不包含0)");
        				}else {
        					for(int i=0;i<memorys.size();i++) {
    							OnBoardMemory memory = memorys.get(i);
    							if(memory.getName().equals(selectMemory)) {							
    								memory.setSize(size);
    								break;
    							}
    						}
        				}					
					}else {
//						setPageComplete(false);
					}
					
				}
			}
		});
		enableMemory(false);
		return contentCpt;
	}

	private Control createChipContent(TabFolder folder) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(folder, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginLeft = 30;
		composite.setLayout(layout);
		
		Composite compositeTree = new Composite(composite, SWT.NULL);
		chipTree = new Tree(compositeTree, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		chipTree.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		chipTree.setHeaderVisible(true);
		final TreeColumn columnCpudrvs = new TreeColumn(chipTree, SWT.NONE);
		columnCpudrvs.setText("芯片列表");
		columnCpudrvs.setWidth(120);
		columnCpudrvs.setResizable(false);
		columnCpudrvs.setToolTipText("Cpu Drivers");
		chipTree.setSize(140, 155);
		
		Composite btnCpt = new Composite(composite, SWT.NULL);
		btnCpt.setLayout(new GridLayout());
		btnCpt.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.HORIZONTAL_ALIGN_CENTER));
		Button goto2Btn,back2Btn;
		goto2Btn = new Button(btnCpt,SWT.PUSH);
		goto2Btn.setText("   》》  ");
		back2Btn = new Button(btnCpt,SWT.PUSH);
		back2Btn.setText(" 《《    ");
		goto2Btn.setEnabled(false);
		back2Btn.setEnabled(false);
		
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
								break;
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
								break;
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
		columnCpudrvsOn.setText("板载芯片");
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

		chipOnTree.setSize(140, 155);
		
		File chipFile = new File(didePath+"djysrc/bsp/chipdrv");
		File[] files = chipFile.listFiles();
		chipsList = new ArrayList<Chip>();
		for(int i=0;i<files.length;i++) {
			TreeItem t = new TreeItem(chipTree, SWT.NONE);
			t.setText(files[i].getName());
			Chip chip = new Chip();
			chip.setChipName(files[i].getName());
			chipsList.add(chip);
		}

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
		cpudrvTree.setSize(140, 155);
		
		Composite btnCpt = new Composite(composite, SWT.NULL);
		btnCpt.setLayout(new GridLayout());
		btnCpt.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.HORIZONTAL_ALIGN_CENTER));
		Button goto1Btn,back1Btn;
		goto1Btn = new Button(btnCpt,SWT.PUSH);
		goto1Btn.setText("   》》  ");	
		back1Btn = new Button(btnCpt,SWT.PUSH);
		back1Btn.setText(" 《《    ");
		goto1Btn.setEnabled(false);
		back1Btn.setEnabled(false);
		
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

		cpudrvOnTree.setSize(140, 155);
		
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

		mainClkLabel = new Label(contentCpt, SWT.NONE);
		mainClkLabel.setText("晶振频率: ");
		mainClkField = new Text(contentCpt, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		mainClkField.setLayoutData(data);
		ControlFactory.createLabel(contentCpt, "Mhz");
		mainClkField.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				// TODO Auto-generated method stub
				Pattern pattern = Pattern.compile("^[+]{0,1}(\\d+)$|^[+]{0,1}(\\d+\\.\\d+)$");  
		       
				String mainClkString = mainClkField.getText().trim();
				
				if(! mainClkString.equals("")) {
					boolean isMainClkInt =  pattern.matcher(mainClkString).matches();
					if(isMainClkInt) {
						float mianClk = Float.parseFloat(mainClkString);
						TreeItem[] items = cpuArhivesNeed.getSelection();
						if(items.length>0) {
							OnBoardCpu onBoardCpu = new OnBoardCpu();
							if (items.length > 0) {
								String selectCpuName = items[0].getText();
								for(int i=0;i<onBoardCpus.size();i++) {
									if(onBoardCpus.get(i).getCpuName().equals(selectCpuName)) {
										onBoardCpus.get(i).setMianClk(mianClk);
									}
								}
							}
						}
					}else {
						MessageDialog.openInformation(window.getShell(), "提示",
								"请输入正数");
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
					rtcClkField.setEnabled(true);
					TreeItem[] items = cpuArhivesNeed.getSelection();
					OnBoardCpu onBoardCpu = new OnBoardCpu();
					if (items.length > 0) {
						String selectCpuName = items[0].getText();
						for (int i = 0; i < onBoardCpus.size(); i++) {
							if (onBoardCpus.get(i).getCpuName().equals(selectCpuName)) {
								onBoardCpus.get(i).setRtcClk(-1);
							}
						}
					}
				}else {
					rtcClkField.setEnabled(false);
					TreeItem[] items = cpuArhivesNeed.getSelection();
					OnBoardCpu onBoardCpu = new OnBoardCpu();
					if (items.length > 0) {
						String selectCpuName = items[0].getText();
						for (int i = 0; i < onBoardCpus.size(); i++) {
							if (onBoardCpus.get(i).getCpuName().equals(selectCpuName)) {
								onBoardCpus.get(i).setRtcClk(0);
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
		ControlFactory.createLabel(contentCpt, "hz");
		rtcClkField.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				// TODO Auto-generated method stub
				Pattern pattern = Pattern.compile("^[+]{0,1}(\\d+)$|^[+]{0,1}(\\d+\\.\\d+)$");  
				String rtcClkString = rtcClkField.getText().trim();
				if(! rtcClkString.equals("")) {
					boolean isRtcClkInt =  pattern.matcher(rtcClkString).matches();
					if(isRtcClkInt) {
						TreeItem[] items = cpuArhivesNeed.getSelection();
						OnBoardCpu onBoardCpu = new OnBoardCpu();
						if (items.length > 0) {
							String selectCpuName = items[0].getText();
							for (int i = 0; i < onBoardCpus.size(); i++) {
								if (onBoardCpus.get(i).getCpuName().equals(selectCpuName)) {
									onBoardCpus.get(i).setRtcClk(Float.parseFloat(rtcClkString));
								}
							}
						}
					}else {
						MessageDialog.openInformation(window.getShell(), "提示",
								"请输入正数");
					}
				}
			}
		});

		return contentCpt;
	}

	private void createDynamicGroup(Composite composite) {
		// TODO Auto-generated method stub
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
		Text nameLabel = new Text(boardNameCpt, SWT.NONE);
		nameLabel.setText("板件名称: ");
		nameLabel.setEditable(false);
		boardNameField = new Text(boardNameCpt, SWT.BORDER);
		GridData boardNameData = new GridData(GridData.FILL_HORIZONTAL);
		boardNameField.setLayoutData(boardNameData);
		boardNameField.setMessage("请输入板件名");
		if(boardInit!=null) {
			boardNameField.setText(boardInit.getBoardName());
		}
		
		Composite newSearchCpt = new Composite(infoArea, SWT.NULL);
		GridLayout layoutnewSearchCpt = new GridLayout();
		layoutnewSearchCpt.numColumns = 2;
		newSearchCpt.setLayout(layoutnewSearchCpt);
		newSearchCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Button newCpuBtn = new Button(newSearchCpt,SWT.PUSH);
		newCpuBtn.setText("新建Cpu");
		newCpuBtn.setBackground(newSearchCpt.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		newCpuBtn.setForeground(newSearchCpt.getDisplay().getSystemColor(SWT.COLOR_WHITE));
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
					t.setImage(DPluginImages.DESC_CPU_VIEW.createImage());
					t.setText(cpusList.get(i).getCpuName());
				}
				super.widgetSelected(e);
			}
			
		});
		
		Composite searchCpuCpt = new Composite(newSearchCpt, SWT.NULL);
		GridLayout layoutSearchCpuCpt = new GridLayout();
		layoutSearchCpuCpt.numColumns = 2;
		searchCpuCpt.setLayout(layoutSearchCpuCpt);
		searchCpuCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label searchCpuLabel = new Label(searchCpuCpt, SWT.NONE);
		searchCpuLabel.setText("检索Cpu: ");
		Text searchCpuField = new Text(searchCpuCpt, SWT.BORDER);
		searchCpuField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		searchCpuField.setMessage("请输入关键字");
		searchCpuField.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				// TODO Auto-generated method stub
				String cpuText = searchCpuField.getText();
				cpuArhives.removeAll();
				for(int i=0;i<cpusList.size();i++) {
					if(cpusList.get(i).getCpuName().contains(cpuText)) {
						TreeItem t = new TreeItem(cpuArhives, SWT.NONE);
						t.setText(cpusList.get(i).getCpuName());
						t.setImage(DPluginImages.DESC_CPU_VIEW.createImage());
					}				
				}
			}
		});

		boardAttributesCpt = new Composite(infoArea, SWT.NULL);
		GridLayout layoutAttributes = new GridLayout();
		layoutAttributes.numColumns = 3;
//		layoutAttributes.marginLeft = 20;
		boardAttributesCpt.setLayout(layoutAttributes);
		
		createTreeForCpus(boardAttributesCpt);
		createTransferButtons(boardAttributesCpt);
		createTreeForNeedCpus(boardAttributesCpt);
		
		cpuArhives.setSize(180, 180);
		cpuArhivesNeed.setSize(180, 180);
		createContentForAttribute(infoArea);		
		
		Point point = infoArea.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		scrolledComposite.setContent(infoArea);
		scrolledComposite.setMinHeight(point.y);
		scrolledComposite.setExpandHorizontal(true);
	    scrolledComposite.setExpandVertical(true);
	}
	
	private void createContentForAttribute(Composite parent) {
		// TODO Auto-generated method stub
		
		ConfigurationGroup = ControlFactory.createGroup(parent, "请选中您要配置的板载cpu", 1);
		ConfigurationGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_FILL | GridData.FILL_HORIZONTAL));
		ConfigurationGroup.setLayout(new GridLayout());
		
		folder= new TabFolder(ConfigurationGroup, SWT.NONE | SWT.H_SCROLL | SWT.V_SCROLL);
		folder.setLayout(new TabFolderLayout());
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));

		TabItem item= new TabItem(folder, SWT.NONE);
		item.setText("时钟"); //$NON-NLS-1$
		item.setControl(createClkContent(folder));

		item= new TabItem(folder, SWT.NONE);
		item.setText("Cpu外设"); //$NON-NLS-1$
		item.setControl(createPeripheralsContent(folder));
		
		TabItem chipItem= new TabItem(folder, SWT.NONE);
		chipItem.setText("板载芯片"); //$NON-NLS-1$
		chipItem.setControl(createChipContent(folder));
		
		item= new TabItem(folder, SWT.NONE);
		item.setText("板载存储"); //$NON-NLS-1$
		item.setControl(createMemoryContent(folder));
		
		enableOperate(false);
//		item.setControl(createNewCpuContent(folder));	
	}
	
	private void createTreeForNeedCpus(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(parent, SWT.NULL);
		cpuArhivesNeed = new Tree(composite, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		cpuArhivesNeed.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		cpuArhivesNeed.setHeaderVisible(true);
		if(boardInit!=null) {
			List<OnBoardCpu> onCpus = boardInit.getOnBoardCpus();
			onBoardCpus = onCpus;
			for(OnBoardCpu onBoardCpu:onCpus) {
				TreeItem t = new TreeItem(cpuArhivesNeed, SWT.NONE);
				t.setImage(DPluginImages.DESC_CPU_VIEW.createImage());
				t.setText(onBoardCpu.getCpuName());
			}
		}
		cpuArhivesNeed.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				gotoBtn.setEnabled(false);
				backBtn.setEnabled(true);
				TreeItem[] items = cpuArhivesNeed.getSelection();
				if (items.length > 0) {
					OnBoardCpu onBoardCpu = new OnBoardCpu();
					String selectCpuName = items[0].getText();
					enableOperate(true);
					ConfigurationGroup.setText("板载Cpu["+selectCpuName+"]配置");
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
					allPeripherals = new ArrayList<Component>();
					
					String cpuPath = getCpuPath(selectCpuName);
					List<String> cpuSrcPaths = new ArrayList<String>();
					getCpuSrcPaths(new File(cpuPath),cpuSrcPaths);
					for(String path:cpuSrcPaths) {
						File srcFile = new File(path);
						System.out.println("path:  "+path);
						ReadComponent rcx = new ReadComponent();
						List<Component> somePeripherals = rcx.getSrcPeripherals(srcFile);
						allPeripherals.addAll(somePeripherals);
					}
					
					
//					for(int i=0;i<peripheralsList.size();i++) {
//						Component component = new Component();
//						component.setName(peripheralsList.get(i).getName());
//						allPeripherals.add(component);
////						thePeripherals.add(component);
//					}
					
					
//					interfaceCombo.removeAll();
					List<Component> boardPeripherals = onBoardCpu.getPeripherals();
					thePeripherals = new ArrayList<Component>();
					for(int i=0;i<allPeripherals.size();i++) {
						Component component = new Component();
						Component component1 = new Component();
						component.setName(allPeripherals.get(i).getName());
						component1.setName(allPeripherals.get(i).getName());
						thePeripherals.add(component);
						peripheralsList.add(component1);
					}
					int perSize = allPeripherals.size();
					cpudrvTree.removeAll();
					cpudrvOnTree.removeAll();
					//板载外设为空
					if(boardPeripherals.size() == 0) {
						peripheralsOn = new ArrayList<Component>();
						for(int i=0;i<allPeripherals.size();i++) {
							TreeItem t = new TreeItem(cpudrvTree, SWT.NONE);
							t.setText(allPeripherals.get(i).getName());
						}
					}else {		//板载外设不为空	
						peripheralsOn = boardPeripherals;					
						for(int j=0;j<boardPeripherals.size();j++) {//u1 u2
							String peripheraOnlName = boardPeripherals.get(j).getName();
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
					memoryTypeCombo.deselectAll();
					addrField.setText("");
					sizeField.setText("");
					if(memorysOn == null) {
						memorys = new ArrayList<OnBoardMemory>();
						memoryTypeCombo.setText("");
						addrField.setText("");
						sizeField.setText("");
					}else {
						memorys = memorysOn;
						if(memorys.size()>0) {
							for(int i=0;i<memorys.size();i++) {
								memorys.get(i).setName("memory"+(i+1));
								String memoryOnName = memorys.get(i).getName();
								TreeItem t = new TreeItem(memoryTree, SWT.NONE);
								t.setText(memoryOnName);
							}
							if(memorys.get(0).getType().equals("RAM")) {
								memoryTypeCombo.select(1);
							}else {
								memoryTypeCombo.select(0);
							}
							addrField.setText(memorys.get(0).getStartAddr());
							sizeField.setText(memorys.get(0).getSize());
							memoryTree.setSelection(memoryTree.getItems()[0]);
						}
					}
//					updateConfigurationIntereface();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
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
		gotoBtn.setEnabled(false);
		backBtn.setEnabled(false);
		
		gotoBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = cpuArhives.getSelection();
				if(items.length>0) {
					String selectCpuName = items[0].getText();
					changeCpusOn(selectCpuName,true);
					
					OnBoardCpu onBoardCpu = new OnBoardCpu();
					onBoardCpu.setCpuName(selectCpuName);
					onBoardCpus.add(onBoardCpu);
					updateCpuOn();
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
		ReadCpuXml rcx = new ReadCpuXml();
		cpusList =  rcx.getAllCpus();
		Composite composite = new Composite(parent, SWT.NULL);
		
		cpuArhives = new Tree(composite, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		cpuArhives.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		cpuArhives.setHeaderVisible(true);
		final TreeColumn columnArhives = new TreeColumn(cpuArhives, SWT.NONE);
		columnArhives.setText("Cpu列表");
		columnArhives.setWidth(140);
		columnArhives.setResizable(false);
		columnArhives.setToolTipText("Cpu Arhives");
		
		for(int i=0;i<cpusList.size();i++) {
			TreeItem t = new TreeItem(cpuArhives, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
			t.setImage(DPluginImages.DESC_CPU_VIEW.createImage());
			t.setText(cpusList.get(i).getCpuName());		
		}
		cpuArhives.addMouseMoveListener(new MouseMoveListener() {
			
			@Override
			public void mouseMove(MouseEvent e) {
				// TODO Auto-generated method stub
				Point point = new Point(e.x, e.y); 
				TreeItem item = cpuArhives.getItem(point);
				String descContent = "";
				if(item!=null) {
					for(Cpu cpu:cpusList) {					
						if(cpu.getCpuName().equals(item.getText())) {
							if(cpu.getCores().size()!=0) {
								for(int j=0;j<cpu.getCores().size();j++) {
									Core core = cpu.getCores().get(j);
									descContent+="内核"+(j+1)+": ";
									if(core.getType()!=null) {
										descContent+="\n类型: "+core.getType();
									}
									if(core.getArch()!=null) {
										descContent+="\n架构: "+core.getArch();
									}
									if(core.getFamily()!=null) {
										descContent+="\n家族: "+core.getFamily();
									}
									if(core.getFpuType()!=null) {
										descContent+="\n浮点: "+core.getFpuType();
									}					
									if(core.getResetAddr()!=null) {
										descContent+="\n复位地址: "+core.getResetAddr();
									}
									descContent+="\n";
								}
								cpuArhives.setToolTipText(descContent);
							}
							break;
						}
					}
				}
			}
		});
		
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
		if(newCpu != null) {
			TreeItem t = new TreeItem(cpuArhivesNeed, SWT.NONE);
			t.setImage(DPluginImages.DESC_CPU_VIEW.createImage());
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
	
	private String getCpuPath(String cpuName) {
		String sourcePath = didePath+"djysrc/bsp/cpudrv";
		File sourceFile = new File(sourcePath);
		File[] files = sourceFile.listFiles();
		String path = null;
		for(File file:files){//cpudrv下的所有文件 Atmel stm32
			if(file.isDirectory()) {
				String curPath = getDeapPath(file,cpuName,null);
				if(curPath != null) {
					path = curPath;
					break;
				};
			}	
		}
		return path;
	}

	public String getDeapPath(File sourceFile,String cpuName,String path) {
		File[] files = sourceFile.listFiles();
		if(path == null) {//path为空时，还未扫描到相应cpu，则继续扫描
			for (File file : files) {
				if (file.isDirectory()) {			
					if(file.getName().equals(cpuName)) {
						path = file.getPath();
						break;
					}else if(!file.getName().equals("include") && !file.getName().equals("src")){
						path = getDeapPath(file,cpuName,path);//如果为目录，则继续扫描该目录
					}			
				}
			}
		}
		return path;
	}

	protected void getCpuSrcPaths(File cpuFile, List<String> cpuSrcPaths) {
		// TODO Auto-generated method stub
		File[] files = cpuFile.listFiles();
		for(File file:files) {
			String path = file.getPath();
			if(file.getName().equals("src")) {
				cpuSrcPaths.add(path);
				break;
			}
		}
		if(!cpuFile.getParentFile().getName().equals("cpudrv")) {
			getCpuSrcPaths(cpuFile.getParentFile(),cpuSrcPaths);
		}		
	}

	private IAction getAction(String id) {
		// Keep a cache, rather than creating a new action each time,
		// so that image caching in ActionContributionItem works.
		IAction action = null;
		IWizardDescriptor wizardDesc = WorkbenchPlugin.getDefault().getNewWizardRegistry().findWizard(id);
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
	
}
