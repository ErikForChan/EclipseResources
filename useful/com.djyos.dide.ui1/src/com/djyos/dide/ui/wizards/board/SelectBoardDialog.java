package com.djyos.dide.ui.wizards.board;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.djyos.dide.ui.DPluginImages;
import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.objects.Board;
import com.djyos.dide.ui.objects.Core;
import com.djyos.dide.ui.objects.CoreMemory;
import com.djyos.dide.ui.objects.Cpu;
import com.djyos.dide.ui.objects.OnBoardCpu;
import com.djyos.dide.ui.wizards.cpu.ReadCpuXml;
import com.djyos.dide.ui.wizards.djyosProject.tools.PathTool;
import com.djyos.dide.ui.wizards.djyosProject.tools.UnitData;

public class SelectBoardDialog extends StatusDialog {

	private Text detailsField;
	private Label boardSearchLabel;
	private Text boardEditText;
	private Text cpuEditText;
	private Label cpuSelectLabel;
	private Tree boardTree;
	private List<Board> boards = new ArrayList<Board>();
	private List<Board> boardsFiltered;
	private Board boardSelected = null;
	private TreeItem t1, t2;
	private List<Cpu> allCpus = ReadCpuXml.getAllCpus();

	public Board getSelectBoard() {
		return boardSelected;
	}

	private Listener searchModifyListener = e -> {
		String keyword = boardEditText.getText().trim();
		List<Board> boardsFiltered = getBoardsFiltered(boards, keyword);
		boardTree.removeAll();
		Init_Board_Tree(boardsFiltered);
	};

	private Listener cpuModifyListener = e -> {
		String keyword = cpuEditText.getText().trim();
		List<Board> boardsFiltered = getBoardsFilteredByCpu(boards, keyword);
		boardTree.removeAll();
		Init_Board_Tree(boardsFiltered);
	};

	public List<Board> getBoardsFiltered(List<Board> boards, String keyWord) {

		boardsFiltered = new ArrayList<Board>();
		Pattern pattern = Pattern.compile(keyWord, Pattern.CASE_INSENSITIVE);
		for (int i = 0; i < boards.size(); i++) {
			Matcher matcher = pattern.matcher(boards.get(i).getBoardName());
			if (matcher.find()) {
				boardsFiltered.add(boards.get(i));
			}
		}
		return boardsFiltered;

	}

	public List<Board> getBoardsFilteredByCpu(List<Board> boards, String cpuName) {

		boardsFiltered = new ArrayList<Board>();
		Pattern pattern = Pattern.compile(cpuName, Pattern.CASE_INSENSITIVE);
		for (int i = 0; i < boards.size(); i++) {
			List<OnBoardCpu> onBoardCpus = boards.get(i).getOnBoardCpus();
			for (int j = 0; j < onBoardCpus.size(); j++) {
				Matcher matcher = pattern.matcher(onBoardCpus.get(j).getCpuName());
				if (matcher.find()) {
					boardsFiltered.add(boards.get(i));
					break;
				}
			}
		}
		return boardsFiltered;

	}

	public SelectBoardDialog(Shell parent) {
		super(parent);
		// TODO Auto-generated constructor stub
		setTitle("选择板件");
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
	}

	@Override
	protected Point getInitialSize() {
		// TODO Auto-generated method stub
		return new Point(660, 600);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = (Composite) super.createDialogArea(parent);
		Composite boardSearchCpt = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		layout.horizontalSpacing = 10;
		boardSearchCpt.setLayout(layout);
		boardSearchCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// 板件查询
		boardSearchLabel = new Label(boardSearchCpt, SWT.None);
		boardSearchLabel.setText("检索板件 :");
		boardEditText = new Text(boardSearchCpt, SWT.BORDER);
		boardEditText.addListener(SWT.Modify, searchModifyListener);
		// 通过选择cpu获取相应的板件
		cpuSelectLabel = new Label(boardSearchCpt, SWT.None);
		cpuSelectLabel.setText("检索板载Cpu :");
		cpuEditText = new Text(boardSearchCpt, SWT.BORDER);
		cpuEditText.addListener(SWT.Modify, cpuModifyListener);

		Composite boardCpt = new Composite(composite, SWT.NULL);
		GridLayout boardLayout = new GridLayout();
		boardLayout.numColumns = 2;
		boardCpt.setLayout(boardLayout);
		boardCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite boardTreeCpt = new Composite(boardCpt, SWT.NULL);
		boardTreeCpt.setLayout(new GridLayout());

		createTreeForBoards(boardTreeCpt);
		boardTree.setSize(220, 260);
		Button newBoradBtn = new Button(boardTreeCpt, SWT.PUSH);
		newBoradBtn.setText("新建板件");
//		newBoradBtn.setBackground(new Color(boardTreeCpt.getDisplay(), 235, 84, 2));
		newBoradBtn.setImage(DPluginImages.OBJ_BOARD_VIEW.createImage());
		Font font = new Font(boardTreeCpt.getDisplay(), "华文仿宋", 10, SWT.BOLD);
//		FontData newFontData = font.getFontData()[0];
		newBoradBtn.setForeground(boardTreeCpt.getDisplay().getSystemColor(SWT.COLOR_RED));
		newBoradBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		newBoradBtn.setFont(font);
		
		detailsField = new Text(boardCpt, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		detailsField.setLayoutData(new GridData(GridData.FILL_BOTH));
		detailsField.setEditable(false);

		// 点击新建板件后弹出新建板件的向导
		newBoradBtn.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				IAction action = DideHelper.getAction("com.djyos.dide.ui.wizards.NewDWizard2");
				action.run();
				boards = ReadBoardXml.getAllBoards();
				boardTree.removeAll();
				Init_Board_Tree(boards);
				super.widgetSelected(e);
			}

		});
		return super.createDialogArea(parent);
	}

	protected void okPressed() {
		// TODO Auto-generated method stub
		TreeItem[] items = boardTree.getSelection();
		if (items.length > 0) {
			String boardName = items[0].getText();
			for (int i = 0; i < boardsFiltered.size(); i++) {
				if (boardsFiltered.get(i).getBoardName().equals(boardName)) {
					boardSelected = boardsFiltered.get(i);
					break;
				}
			}
		}
		super.okPressed();
	}

	private void createTreeForBoards(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		boardTree = new Tree(composite, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		boardTree.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		boardTree.setHeaderVisible(true);
		boards = ReadBoardXml.getAllBoards();
		boardsFiltered = boards;
		Init_Board_Tree(boards);
		boardTree.addListener(SWT.MouseDoubleClick, new Listener() {

			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				Point point = new Point(event.x, event.y);
				TreeItem item = boardTree.getItem(point);
				if (item != null && item.getData("type") != null) {
					if (item.getData("type").equals("board")) {
						okPressed();
					} else {
						IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						DideHelper.showErrorMessage("请选中板件");
					}
				}
			}
		});

		boardTree.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

				TreeItem[] items = boardTree.getSelection();
				TreeItem item = items[0];
				BoardTools.Display_BoardDetails(item, boards, allCpus, detailsField);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		final TreeColumn columnBoards = new TreeColumn(boardTree, SWT.NONE);
		columnBoards.setText("板件列表");
		columnBoards.setWidth(200);
		columnBoards.setResizable(false);
		columnBoards.setToolTipText("所有板件");
		columnBoards.setImage(DPluginImages.CFG_CPMT_OBJ.createImage());
	}

	// 初始化BoardTree
	private void Init_Board_Tree(List<Board> boards) {
		// TODO Auto-generated method stub
		t2 = new TreeItem(boardTree, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		t2.setData(PathTool.getUserBoardFilePath());
		t2.setImage(DPluginImages.TREE_FLODER_VIEW.createImage());
		t2.setText("用户板件");

		t1 = new TreeItem(boardTree, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		t1.setData(PathTool.getDemoBoardFilePath());
		t1.setImage(DPluginImages.TREE_FLODER_VIEW.createImage());
		t1.setText("Djyos板件");

		for (int i = 0; i < boards.size(); i++) {
			TreeItem t;
			if (boards.get(i).getBoardFolderPath().contains("demo")) {
				t = new TreeItem(t1, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
			} else {
				t = new TreeItem(t2, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
			}
			BoardTools.fillBoardChilds(boards.get(i), t, allCpus);
			t.setData(boards.get(i).getBoardFolderPath());
			t.setData("type", "board");
			t.setImage(DPluginImages.OBJ_BOARD_VIEW.createImage());
			t.setText(boards.get(i).getBoardName());

		}
		t1.setExpanded(true);
	}


}
