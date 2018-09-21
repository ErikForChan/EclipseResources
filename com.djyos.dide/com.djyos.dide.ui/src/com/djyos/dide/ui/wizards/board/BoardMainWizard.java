package com.djyos.dide.ui.wizards.board;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.tmatesoft.svn.core.SVNException;

import com.djyos.dide.ui.wizards.board.onboardcpu.Chip;
import com.djyos.dide.ui.wizards.board.onboardcpu.OnBoardCpu;
import com.djyos.dide.ui.wizards.component.Component;
import com.djyos.dide.ui.wizards.cpu.Cpu;
import com.djyos.dide.ui.wizards.cpu.ReadCpuXml;

import com.djyos.dide.ui.wizards.djyosProject.tools.DeleteFolder;
import com.djyos.dide.ui.wizards.djyosProject.tools.DideHelper;
import com.djyos.dide.ui.wizards.djyosProject.tools.SendEmail;
import com.djyos.dide.ui.wizards.djyosProject.CreateHardWareDesc;
import com.djyos.dide.ui.wizards.djyosProject.ReadHardWareDesc;
import com.djyos.dide.ui.wizards.djyosProject.tools.DPluginImages;

public class BoardMainWizard extends WizardPage{
	
	private Tree tree;
	private Text configInfoText = null;
	private TreeItem t1,t2;
	public static TreeItem fileItem = null,tmssItem = null;
	private MenuItem newBoardItem,deleteItem,reviseItem;
	private Composite infoArea;
	private List<Board> boardsList;
	private Board boardCreated;
	final IWorkspace workspace = ResourcesPlugin.getWorkspace();
	private IWorkbenchWindow window = PlatformUI.getWorkbench()
			.getActiveWorkbenchWindow();
	DideHelper dideHelper = new DideHelper();
	
	public BoardMainWizard(String pageName) {
		super(pageName);
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
		}
		setPageComplete(true);
	}

	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		initializeDialogUnits(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);
		
		ScrolledComposite scrolledComposite = new ScrolledComposite(composite, SWT.V_SCROLL
                | SWT.H_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		infoArea = new Composite(scrolledComposite, SWT.NONE);
		GridLayout infoLayout = new GridLayout();
		infoLayout.numColumns = 1;
		infoArea.setLayout(infoLayout);
		GridData data = new GridData(GridData.FILL_BOTH);
		infoArea.setLayoutData(data);
		
		Point cPoint = composite.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		Text projectTypeDesc;
		String promoteString = "右键板件列表可";
		String extraString = "新建板件 : ";
		String QAQ = "\tIDE分级目录的形式管理操作系统支持的众多Cpu，本界面用于管理Cpu的分类,\n包括添加Cpu和分组,手动拖拽即可移动分组";
		String descTitle = "分组/Cpu描述";
		
		Composite promoteArea = new Composite(infoArea, SWT.NONE);
		GridLayout promoteLayout = new GridLayout();
		promoteLayout.numColumns = 2;
		promoteArea.setLayout(promoteLayout);
		GridData data1 = new GridData(GridData.FILL_BOTH);
		promoteArea.setLayoutData(data1);
		
		Label promoteLabel = new Label(promoteArea,SWT.NULL);
		promoteLabel.setText(promoteString);
		FontData newFontData = promoteLabel.getFont().getFontData()[0];
		newFontData.setStyle(SWT.BOLD);
		newFontData.setHeight(8);
		promoteLabel.setFont(new Font(infoArea.getDisplay(),newFontData));
		
		Label extraLabel = new Label(promoteArea,SWT.NULL);
		extraLabel.setText(extraString);
		extraLabel.setForeground(infoArea.getDisplay().getSystemColor(SWT.COLOR_RED));
		FontData newFontData1 = extraLabel.getFont().getFontData()[0];
		newFontData1.setStyle(SWT.ITALIC | SWT.BOLD);
		newFontData1.setHeight(11);
		extraLabel.setFont(new Font(infoArea.getDisplay(),newFontData1));
		
		Composite contentCpt = new Composite(infoArea, SWT.NULL);
		GridLayout contentLayout = new GridLayout();
		contentLayout.numColumns = 2;
		contentCpt.setLayout(contentLayout); 
		contentCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		createBoardTree(contentCpt);
		
		initPopup();
		handleTreeDrag();
		Composite sashForm = new Composite(contentCpt, SWT.NULL);
		sashForm.setLayout(new RowLayout());
		configInfoText = new Text(sashForm, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		configInfoText.setEditable(false);
		configInfoText.setLayoutData(new RowData(350,350));
		configInfoText.setText("选中板件即可显示选中的板件配置信息");

		Point point = infoArea.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		scrolledComposite.setContent(infoArea);
		scrolledComposite.setMinHeight(point.y);
		scrolledComposite.setExpandHorizontal(true);
	    scrolledComposite.setExpandVertical(true);
	    
//	    try {
//			List<String> infos = dideHelper.getLogs();
////			SendEmail email = new SendEmail();
////			email.send();
////			for(String s:infos) {
////				System.out.println("s：\n   "+s);
////			}
//		} catch (SVNException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
	    
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
		Dialog.applyDialogFont(composite);
	}
	
	private void handleTreeDrag() {
		// TODO Auto-generated method stub
		// 定义拖放源对象
		DragSource dragSource = new DragSource(tree, DND.DROP_MOVE | DND.DROP_COPY);
		// 设置传输的数据为文本型String类型
		dragSource.setTransfer(new Transfer[] { TextTransfer.getInstance() });
		// 注册拖放源时的事件处理
		dragSource.addDragListener(new DragSourceListener() {

			@Override
			public void dragStart(DragSourceEvent event) {
				// TODO Auto-generated method stub
				if (tree.getSelectionCount() == 0)
					event.doit = false;
			}

			@Override
			public void dragSetData(DragSourceEvent event) {
				// TODO Auto-generated method stub
				if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
					event.data = tree.getSelection()[0].getText(0);
					fileItem = tree.getSelection()[0];
				}
			}

			@Override
			public void dragFinished(DragSourceEvent event) {
				// TODO Auto-generated method stub
			}
		});

		// 定义拖放目标对象
		DropTarget dropTarget = new DropTarget(tree, DND.DROP_MOVE | DND.DROP_DEFAULT | DND.DROP_COPY);
		// 设置目标对象可传输的数据类型
		dropTarget.setTransfer(new Transfer[] { TextTransfer.getInstance() });
		// 注册目标对象的事件处理
		dropTarget.addDropListener(new DropTargetListener() {

			@Override
			public void dropAccept(DropTargetEvent event) {
				// TODO Auto-generated method stub

			}

			@Override
			public void drop(DropTargetEvent event) {
				// TODO Auto-generated method stub
				if (event.item == null)
					return;
				// 首先获得目标对象中拖拽的树节点
				TreeItem eventItem = (TreeItem) event.item;

				if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
					// 获得数据源设置的字符串
					String s = (String) event.data;
					// 在tmss位置插入一个节点
					File srcFile = new File(fileItem.getData().toString());
					File destFile = new File(eventItem.getData().toString() + "\\" + s);
					String isDropable = isFileDropable(fileItem,srcFile, new File(eventItem.getData().toString()));
					if (isDropable == null) {
						tmssItem = new TreeItem(eventItem, SWT.NONE);
						tmssItem.setImage(DPluginImages.DESC_BOARD_VIEW.createImage());
						tmssItem.setExpanded(false);

						tmssItem.setText(s);
						tmssItem.setData(destFile);
						try {
							dideHelper.copyFolder(srcFile, destFile);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						DeleteFolder dlf = new DeleteFolder();
						dlf.deleteFolder(fileItem.getData().toString());
						// 删除原来的节点
						if (tree != null) {
							fileItem.dispose();
						}
					} else {
						IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						MessageDialog.openError(window.getShell(), "提示", isDropable);
					}

				}
			}

			@Override
			public void dragOver(DropTargetEvent event) {
				// TODO Auto-generated method stub
				event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SELECT;
			}

			@Override
			public void dragOperationChanged(DropTargetEvent event) {
				// TODO Auto-generated method stub
				if (event.detail == DND.DROP_DEFAULT)
					event.detail = DND.DROP_COPY;
			}

			@Override
			public void dragLeave(DropTargetEvent event) {
				// TODO Auto-generated method stub

			}

			@Override
			public void dragEnter(DropTargetEvent event) {
				// TODO Auto-generated method stub
				if (event.detail == DND.DROP_DEFAULT)
					event.detail = DND.DROP_COPY;
			}
		});
				
	}
	
	private String isFileDropable(TreeItem fileItem, File srcFile, File destFile) {
		// TODO Auto-generated method stub
		File tempSrcFile = new File(srcFile.getPath());
		File tempDestFile = new File(destFile.getPath());
		
		if(fileItem.getText().contains("板件")){
			return "该目录不可拖动！";
		}
		
		File parentSrcFile = tempSrcFile.getParentFile();
		if (parentSrcFile.getName().equals(destFile.getName())) {
			return "该目录下已经存在["+tempSrcFile.getName()+"]！";
		}
		
		tempDestFile = new File(destFile.getPath());
		File[] destFiles = tempDestFile.listFiles();
		for(File file : destFiles) {
			if(file.getName().endsWith(".xml")) {
				return "不可托拉到板件下！";
			}
		}
		return null;
	}
	
	private void initPopup() {
		// TODO Auto-generated method stub
		Menu menu = new Menu(tree);
		
		newBoardItem = new MenuItem(menu, SWT.PUSH);
		newBoardItem.setText("新建板件");
		newBoardItem.setImage(DPluginImages.DESC_BOARD_VIEW.createImage());

		reviseItem = new MenuItem(menu, SWT.PUSH);
		reviseItem.setText("维护板件");
		reviseItem.setImage(DPluginImages.CPU_REVISE_VIEW.createImage());

		deleteItem = new MenuItem(menu, SWT.PUSH);
		deleteItem.setText("删除板件");
		deleteItem.setImage(DPluginImages.CFG_DELETE_OBJ.createImage());

		newBoardItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				NewOrReviseBoard newBoardDialog = new NewOrReviseBoard(infoArea.getShell(),true,null);
				if(newBoardDialog.open() == Window.OK) {
					Board board = newBoardDialog.getBoard();
					boardCreated = board;
					TreeItem t = new TreeItem(t2, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL, 0);
					t.setData(board.getBoardPath());
					t.setImage(DPluginImages.DESC_BOARD_VIEW.createImage());
					t.setText(board.getBoardName());	
					tree.select(t);
					displayBoardDetails(t);
				}	
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		reviseItem.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				handleReviseClick();
			}
		});
		
		deleteItem.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = tree.getSelection();
	        	boolean isSure = MessageDialog.openQuestion(window.getShell(), "提示",
	        			"您确认要删除板件["+items[0].getText()+"]吗?");
	        	if(isSure) {   
	        		String curFilePath = items[0].getData().toString();//获取当前选中文件的路径
					DeleteFolder dlf = new DeleteFolder();
					dlf.deleteFolder(curFilePath);
					items[0].dispose();
//	        		IProject[] projects = workspace.getRoot().getProjects();
//	        		List<IProject> impactedProjects = new ArrayList<IProject>();
//	        		String message = "以下工程用到该板件：\n";
//					for(IProject project:projects) {
//						ReadHardWareDesc rhwd = new ReadHardWareDesc();
//						File hardWardInfoFile = new File(project.getLocation().toString()+"/data/hardware_info.xml");
//						List<String> hardwares;
//						if(hardWardInfoFile.exists()) {
//							hardwares = rhwd.getHardWares(hardWardInfoFile);
//							if(hardwares.get(0).equals(items[0].getText())) {
//								impactedProjects.add(project);
//								message += project.getName()+"\n";
//							}
//						}
//					}
//					message += "是否删除？";
//					boolean toDelete = MessageDialog.openQuestion(window.getShell(), "提示",message);
//					if(toDelete) {
//						String curFilePath = items[0].getData().toString();//获取当前选中文件的路径
//						DeleteFolder dlf = new DeleteFolder();
//						dlf.deleteFolder(curFilePath);
//						items[0].dispose();
//						for(IProject project:impactedProjects) {
//							try {
//								project.delete(true, true, new NullProgressMonitor());
//							} catch (CoreException e1) {
//								// TODO Auto-generated catch block
//								e1.printStackTrace();
//							}
//						}
//					}
	        	}
			}
		});
		tree.setMenu(menu);
	}

	private void createBoardTree(Composite contentCpt) {
		// TODO Auto-generated method stub
		Composite sourceTreeCpt = new Composite(contentCpt, SWT.NULL);
		tree = new Tree(sourceTreeCpt, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setSize(200, 360);
		tree.setHeaderVisible(true);
		sourceTreeCpt.setTouchEnabled(true);
		final TreeColumn columnArhives = new TreeColumn(tree, SWT.NONE);
		columnArhives.setText("板件列表");
		columnArhives.setWidth(190);
		columnArhives.setResizable(true);
		columnArhives.setImage(DPluginImages.CFG_CPMT_OBJ.createImage());
		
		t2 = new TreeItem(tree, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		t2.setData(dideHelper.getUserBoardFilePath());
		t2.setImage(DPluginImages.DESC_GROUP_VIEW.createImage());
		t2.setText("用户板件");
		
		t1 = new TreeItem(tree, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		t1.setData(dideHelper.getDemoBoardFilePath());
		t1.setImage(DPluginImages.TREE_FLODER_VIEW.createImage());
		t1.setText("Djyos板件");		
			
		ReadBoardXml rbx = new ReadBoardXml();
		boardsList = rbx.getAllBoards();
		for(int i=0;i<boardsList.size();i++) {
			TreeItem t;
			if(boardsList.get(i).getBoardPath().contains("demo")) {
				 t = new TreeItem(t1, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
			}else{
				 t = new TreeItem(t2, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
			}
			
			t.setData(boardsList.get(i).getBoardPath());
			t.setImage(DPluginImages.DESC_BOARD_VIEW.createImage());
			t.setText(boardsList.get(i).getBoardName());		
		}
		
		tree.addListener(SWT.MouseDown,new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				Point point = new Point(event.x, event.y);
				TreeItem item = tree.getItem(point);
				if(item != null) {
					String itemText = item.getText();
					if(itemText.contains("板件")) {
						deleteItem.setEnabled(false);
						reviseItem.setEnabled(false);
						configInfoText.setText("选中板件即可显示选中的板件配置信息");
					}else {
						deleteItem.setEnabled(true);
						reviseItem.setEnabled(true);
						displayBoardDetails(item);
					}
				}
			}
		});
	
		tree.addListener(SWT.MouseDoubleClick, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				handleReviseClick();
			}
		});
		
	}
	
	protected void handleReviseClick() {
		// TODO Auto-generated method stub
		TreeItem[] items = tree.getSelection();
		if(items.length>0) {
			String itemName = items[0].getText().trim();
			TreeItem parentItem = items[0].getParentItem();
			if(!itemName.contains("板件")) {
				Board board = getBoardByName(itemName);
				NewOrReviseBoard dialog = new NewOrReviseBoard(infoArea.getShell(),false,board);
				if(dialog.open() == Window.OK) {
					String reviseBoardName = dialog.lastBoardName;
					//扫描工作空间中所有用得到此板件的工程，并修改它们的板件名
//					IProject[] projects = workspace.getRoot().getProjects();
//					for(IProject project:projects) {
//						ReadHardWareDesc rhwd = new ReadHardWareDesc();
//						File hardWardInfoFile = new File(project.getLocation().toString()+"/data/hardware_info.xml");
//						List<String> hardwares;
//						if(hardWardInfoFile.exists()) {
//							hardwares = rhwd.getHardWares(hardWardInfoFile);
//							if(hardwares.get(0).equals(itemName)) {
//								hardWardInfoFile.delete();
//								CreateHardWareDesc chwd = new CreateHardWareDesc();
//								chwd.createHardWareXml(reviseBoardName, hardwares.get(1), hardWardInfoFile);
//							}
//						}
//					}
					
					items[0].dispose();
					TreeItem item = new TreeItem(parentItem,SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL,0);
					item.setText(reviseBoardName);			
					item.setImage(DPluginImages.DESC_BOARD_VIEW.createImage());
					item.setExpanded(false);
					item.setData(new File(parentItem.getData().toString()+"/"+reviseBoardName));
					tree.select(item);
					displayBoardDetails(item);
				};
			}
		}
	}

	protected void displayBoardDetails(TreeItem item) {
		// TODO Auto-generated method stub
		String boardDesc = "";
		Board curBoard = null;
		if(item != null) {
			String itemText = item.getText();
			ReadBoardXml rbx = new ReadBoardXml();
			boardsList = rbx.getAllBoards();
			for(Board borad:boardsList) {
				if(borad.getBoardName().endsWith(itemText)) {
					curBoard = borad;
					break;
				}
			}
			List<OnBoardCpu> onBoardCpus = curBoard.getOnBoardCpus();
			boardDesc += "板件 ["+itemText+"] 配置信息：\n\n";
			int cpuCount = 1;
			for(OnBoardCpu onBoardCpu:onBoardCpus) {
				Cpu cpu = getCpuByOnBoardCpu(onBoardCpu);
				if(cpu != null) {
					if(onBoardCpus.size()<2) {
						boardDesc += "板载Cpu："+onBoardCpu.getCpuName()+"\n";
					}else {
						boardDesc += "板载Cpu"+cpuCount+"："+onBoardCpu.getCpuName()+"\n";
					}
//					cpuDesc += "\t架构:"+cpu.getCores().get(0).getArch()+"，家族:"+cpu.getCores().get(0).getFamily()
//							+"\n浮点:"+cpu.getCores().get(0).getFpuType()+"，复位地址:"+cpu.getCores().get(0).getResetAddr()+"\n";
					String chipString = "";
					String peripheralString = "";
					List<Chip> chips = onBoardCpu.getChips(); 
					List<Component> components = onBoardCpu.getPeripherals();
					for(int i=0;i<chips.size();i++) {
						chipString+="  "+chips.get(i).getChipName();
					}
					for(int i=0;i<components.size();i++) {
						peripheralString+="  "+components.get(i).getName()+",";
					}
					boardDesc +=  "主晶振频率: "+onBoardCpu.getMianClk() +"\n" 
							+ "Rtc钟频率: " +onBoardCpu.getRtcClk() +"\n"
							+ "板载芯片: " +chipString+ "\n" 
							+ "Cpu外设: " +peripheralString;
					cpuCount++;
				}
				
			}
			configInfoText.setText(boardDesc);
		}
	}

	private Cpu getCpuByOnBoardCpu(OnBoardCpu onBoardCpus) {
		// TODO Auto-generated method stub
		ReadCpuXml rcx = new ReadCpuXml();
		List<Cpu> cpusList =  rcx.getAllCpus();
		for(Cpu cpu:cpusList) {
			if(cpu.getCpuName().equals(onBoardCpus.getCpuName())) {
				return cpu;
			}
		}
		return null;
	}
	
	private Board getBoardByName(String baordName) {
		for(Board board:boardsList) {
			if(board.getBoardName().endsWith(baordName)) {
				return board;
			}
		}
		return null;
	}
	public Board getBoard() {
		return boardCreated;
	}
}
