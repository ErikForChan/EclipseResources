/*******************************************************************************
 * Copyright (c) 2017 Djyos Team.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.djyos.com
 *
 * Contributors:
 *     Djyos Team - Jiaming Chen
 *******************************************************************************/
package com.djyos.dide.ui.wizards.board;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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

import com.djyos.dide.ui.DPluginImages;
import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.messages.IPrompt;
import com.djyos.dide.ui.objects.Board;
import com.djyos.dide.ui.objects.Core;
import com.djyos.dide.ui.objects.CoreMemory;
import com.djyos.dide.ui.objects.Cpu;
import com.djyos.dide.ui.objects.OnBoardCpu;
import com.djyos.dide.ui.wizards.cpu.ReadCpuXml;
import com.djyos.dide.ui.wizards.djyosProject.tools.DeleteFolderUtils;
import com.djyos.dide.ui.wizards.djyosProject.tools.PathTool;
import com.djyos.dide.ui.wizards.djyosProject.tools.TestTool;

public class BoardMainWizard extends WizardPage {

	private Tree tree;
	private Text configInfoText = null;
	private TreeItem t1, t2;
	public static TreeItem fileItem = null, tmssItem = null;
	private MenuItem newBoardItem, deleteItem, reviseItem;
	private Composite infoArea;
	private List<Board> boardsList;
	private Board boardCreated;
	final IWorkspace workspace = ResourcesPlugin.getWorkspace();
	private IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	private List<Cpu> allCpus;

	public BoardMainWizard(String pageName) {
		super(pageName);
		setPageComplete(true);
	}

	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		boolean srcExist = DideHelper.isDjysrcExist();
		if (srcExist) {
			allCpus = ReadCpuXml.getAllCpus();
			// TODO Auto-generated constructor stub
			createDynamicGroup(composite);
//			TestTool.test_json();
			setErrorMessage(null);
			setMessage(null);
			setControl(composite);
			Dialog.applyDialogFont(composite);
		} else {
			DideHelper.showErrorMessage(IPrompt.djysrcNotExit);
		}

	}

	private void createDynamicGroup(Composite composite) {
		// TODO Auto-generated method stub
		ScrolledComposite scrolledComposite = new ScrolledComposite(composite, SWT.V_SCROLL | SWT.H_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		infoArea = new Composite(scrolledComposite, SWT.NONE);
		GridLayout infoLayout = new GridLayout();
		infoLayout.numColumns = 5;
		infoArea.setLayout(infoLayout);
		GridData data = new GridData(GridData.FILL_BOTH);
		infoArea.setLayoutData(data);

		String promoteString = "右键板件列表可";
		String extraString = "新建板件 : ";
		String serachBoardString = "搜索板件:	 ";
		
		Label searchLabel = new Label(infoArea, SWT.NULL);
		searchLabel.setText(serachBoardString);
		FontData newFontData = searchLabel.getFont().getFontData()[0];
		newFontData.setStyle(SWT.BOLD);
		newFontData.setHeight(8);
		searchLabel.setFont(new Font(infoArea.getDisplay(), newFontData));
		
		Text searchBoardText = new Text(infoArea, SWT.BORDER);
		GridData searchTextGrid = new GridData(GridData.FILL_HORIZONTAL);
		searchTextGrid.horizontalSpan = 2;
		searchBoardText.setLayoutData(searchTextGrid);
		searchBoardText.setMessage("可输入板件名称或者板载Cpu名称");
		searchBoardText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				// TODO Auto-generated method stub
				String text = searchBoardText.getText();
				tree.removeAll();
				List<Board> boardFiltered = new ArrayList<Board>();
				for(Board b:boardsList) {
					String boardName = b.getBoardName();
					if(boardName.toLowerCase().contains(text.toLowerCase())) {
						boardFiltered.add(b);
					}else {
						for(OnBoardCpu onCpu : b.getOnBoardCpus()) {
							if(onCpu.getCpuName().toLowerCase().contains(text.toLowerCase())) {
								boardFiltered.add(b);
								break;
							}
						}
					}
				}
				init_BoardTree(boardFiltered);
			}
		});
		
		
		Label promoteLabel = new Label(infoArea, SWT.NULL);
		promoteLabel.setText(promoteString);
		promoteLabel.setFont(new Font(infoArea.getDisplay(), newFontData));

		Label extraLabel = new Label(infoArea, SWT.NULL);
		extraLabel.setText(extraString);
		extraLabel.setForeground(infoArea.getDisplay().getSystemColor(SWT.COLOR_RED));
		FontData newFontData1 = extraLabel.getFont().getFontData()[0];
		newFontData1.setStyle(SWT.ITALIC | SWT.BOLD);
		newFontData1.setHeight(11);
		extraLabel.setFont(new Font(infoArea.getDisplay(), newFontData1));
		
		SashForm sashForm = new SashForm(infoArea, SWT.HORIZONTAL);
		GridData gds = new GridData(GridData.FILL_BOTH);
		gds.horizontalSpan = 5;
		sashForm.setLayoutData(gds);

		create_BoardTree(sashForm);

		initPopup();
		handleTreeDrag();
		configInfoText = new Text(sashForm, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		configInfoText.setEditable(false);
		configInfoText.setText("选中板件即可显示选中的板件配置信息");

		sashForm.setWeights(new int[] { 2, 3 });// 内部容器之间宽度比例
		Point point = infoArea.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		scrolledComposite.setContent(infoArea);
		scrolledComposite.setMinHeight(point.y);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

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
					String isDropable = isFileDropable(fileItem, srcFile, new File(eventItem.getData().toString()));
					if (isDropable == null) {
						tmssItem = new TreeItem(eventItem, SWT.NONE);
						tmssItem.setImage(DPluginImages.OBJ_BOARD_VIEW.createImage());
						tmssItem.setExpanded(false);

						tmssItem.setText(s);
						tmssItem.setData(destFile);
						DideHelper.copyFolder(srcFile, destFile);
						DeleteFolderUtils.deleteFolder(fileItem.getData().toString());
						// 删除原来的节点
						if (tree != null) {
							fileItem.dispose();
						}
					} else {
						DideHelper.showErrorMessage(isDropable);
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

		if (fileItem.getText().contains("板件")) {
			return "该目录不可拖动！";
		}

		File parentSrcFile = tempSrcFile.getParentFile();
		if (parentSrcFile.getName().equals(destFile.getName())) {
			return "该目录下已经存在[" + tempSrcFile.getName() + "]！";
		}

		tempDestFile = new File(destFile.getPath());
		File[] destFiles = tempDestFile.listFiles();
		for (File file : destFiles) {
			if (file.getName().endsWith(".xml")) {
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
		newBoardItem.setImage(DPluginImages.OBJ_BOARD_VIEW.createImage());

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
				handle_NewBoard_Click();
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
				handle_Revice_Board();
			}
		});

		deleteItem.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = tree.getSelection();
				boolean isSure = MessageDialog.openQuestion(window.getShell(), "提示",
						"您确认要删除板件[" + items[0].getText() + "]吗?");
				if (isSure) {
					String curFilePath = items[0].getData().toString();// 获取当前选中文件的路径
					DeleteFolderUtils.deleteFolder(curFilePath);
					items[0].dispose();
					// IProject[] projects = workspace.getRoot().getProjects();
					// List<IProject> impactedProjects = new ArrayList<IProject>();
					// String message = "以下工程用到该板件：\n";
					// for(IProject project:projects) {
					// ReadHardWareDesc rhwd = new ReadHardWareDesc();
					// File hardWardInfoFile = new
					// File(project.getLocation().toString()+"/data/hardware_info.xml");
					// List<String> hardwares;
					// if(hardWardInfoFile.exists()) {
					// hardwares = rhwd.getHardWares(hardWardInfoFile);
					// if(hardwares.get(0).equals(items[0].getText())) {
					// impactedProjects.add(project);
					// message += project.getName()+"\n";
					// }
					// }
					// }
					// message += "是否删除？";
					// boolean toDelete = MessageDialog.openQuestion(window.getShell(),
					// "提示",message);
					// if(toDelete) {
					// String curFilePath = items[0].getData().toString();//获取当前选中文件的路径
					// DeleteFolder dlf = new DeleteFolder();
					// dlf.deleteFolder(curFilePath);
					// items[0].dispose();
					// for(IProject project:impactedProjects) {
					// try {
					// project.delete(true, true, new NullProgressMonitor());
					// } catch (CoreException e1) {
					// // TODO Auto-generated catch block
					// e1.printStackTrace();
					// }
					// }
					// }
				}
			}
		});
		tree.setMenu(menu);
	}

	protected void handle_NewBoard_Click() {
		NewOrReviseBoardDialog newBoardDialog = new NewOrReviseBoardDialog(infoArea.getShell(), true, null);
		if (newBoardDialog.open() == Window.OK) {
			Board board = newBoardDialog.getBoard();
			boardCreated = board;
			TreeItem t = new TreeItem(t2, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL, 0);
			t.setData(board.getBoardFolderPath());
			t.setImage(DPluginImages.OBJ_BOARD_VIEW.createImage());
			t.setText(board.getBoardName());
			t.setData("type", "board");
			t.setExpanded(true);
			tree.select(t);
			BoardTools.fillBoardChilds(board, t, allCpus);
			boardsList.add(board);
			BoardTools.Display_BoardDetails(t,boardsList,allCpus,configInfoText);
		}
	}

	private void create_BoardTree(SashForm sashForm) {
		// TODO Auto-generated method stub
		tree = new Tree(sashForm, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setHeaderVisible(true);
		final TreeColumn columnArhives = new TreeColumn(tree, SWT.NONE);
		columnArhives.setText("板件列表");
		columnArhives.setWidth(210);
		columnArhives.setResizable(true);
		columnArhives.setImage(DPluginImages.CFG_CPMT_OBJ.createImage());
		
		boardsList = ReadBoardXml.getAllBoards();
		init_BoardTree(boardsList);

	}

	private void init_BoardTree(List<Board> boardsList) {
		// TODO Auto-generated method stub
		t2 = new TreeItem(tree, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		t2.setData(PathTool.getUserBoardFilePath());
		t2.setImage(DPluginImages.OBJ_GROUP_VIEW.createImage());
		t2.setText("用户板件");

		t1 = new TreeItem(tree, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		t1.setData(PathTool.getDemoBoardFilePath());
		t1.setImage(DPluginImages.TREE_FLODER_VIEW.createImage());
		t1.setText("Djyos板件");

		for (int i = 0; i < boardsList.size(); i++) {
			TreeItem t;
			if (boardsList.get(i).getBoardFolderPath().contains("demo")) {
				t = new TreeItem(t1, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
			} else {
				t = new TreeItem(t2, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
			}
			t.setData("type", "board");
			BoardTools.fillBoardChilds(boardsList.get(i), t, allCpus);
			t.setData(boardsList.get(i).getBoardFolderPath());
			t.setImage(DPluginImages.OBJ_BOARD_VIEW.createImage());
			t.setText(boardsList.get(i).getBoardName());
		}

		// t1.setExpanded(true);
		tree.addListener(SWT.MouseDown, new Listener() {

			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				Point point = new Point(event.x, event.y);
				TreeItem item = tree.getItem(point);
				if (item != null) {
					String itemText = item.getText();
					if (itemText.contains("板件")) {
						newBoardItem.setEnabled(true);
						deleteItem.setEnabled(false);
						reviseItem.setEnabled(false);
						configInfoText.setText("选中板件即可显示选中的板件配置信息");
					} else {
						if (!item.getData("type").equals("board")) {
							newBoardItem.setEnabled(false);
							deleteItem.setEnabled(false);
							reviseItem.setEnabled(false);
						} else {
							newBoardItem.setEnabled(true);
							deleteItem.setEnabled(true);
							reviseItem.setEnabled(true);
						}
						BoardTools.Display_BoardDetails(item,boardsList,allCpus,configInfoText);
					}
				}
			}
		});

		tree.addListener(SWT.MouseDoubleClick, new Listener() {

			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				Point point = new Point(event.x, event.y);
				TreeItem item = tree.getItem(point);
				if (item != null && item.getData("type") != null) {
					if (item.getData("type").equals("board")) {
						handle_Revice_Board();
					} else {
						DideHelper.showErrorMessage("请选中板件");
					}
				}
			}
		});
	}

	protected void handle_Revice_Board() {
		// TODO Auto-generated method stub
		TreeItem[] items = tree.getSelection();
		if (items.length > 0) {
			String itemName = items[0].getText().trim();
			TreeItem parentItem = items[0].getParentItem();
			if (!itemName.contains("板件")) {
				Board board = DideHelper.getBoardByName(itemName);
				NewOrReviseBoardDialog dialog = new NewOrReviseBoardDialog(infoArea.getShell(), false, board);
				if (dialog.open() == Window.OK) {
					String reviseBoardName = dialog.lastBoardName;
					// 扫描工作空间中所有用得到此板件的工程，并修改它们的板件名
					// IProject[] projects = workspace.getRoot().getProjects();
					// for(IProject project:projects) {
					// ReadHardWareDesc rhwd = new ReadHardWareDesc();
					// File hardWardInfoFile = new
					// File(project.getLocation().toString()+"/data/hardware_info.xml");
					// List<String> hardwares;
					// if(hardWardInfoFile.exists()) {
					// hardwares = rhwd.getHardWares(hardWardInfoFile);
					// if(hardwares.get(0).equals(itemName)) {
					// hardWardInfoFile.delete();
					// CreateHardWareDesc chwd = new CreateHardWareDesc();
					// chwd.createHardWareXml(reviseBoardName, hardwares.get(1), hardWardInfoFile);
					// }
					// }
					// }

					items[0].dispose();
					TreeItem item = new TreeItem(parentItem, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL, 0);
					item.setText(reviseBoardName);
					item.setData("type", "board");
					item.setImage(DPluginImages.OBJ_BOARD_VIEW.createImage());
					item.setExpanded(true);
					item.setData(new File(parentItem.getData().toString() + "/" + reviseBoardName));
					tree.select(item);
					BoardTools.fillBoardChilds(board, item, allCpus);
					BoardTools.Display_BoardDetails(item,boardsList,allCpus,configInfoText);
				}
			}
		}
	}

	public Board getBoard() {
		return boardCreated;
	}
}
