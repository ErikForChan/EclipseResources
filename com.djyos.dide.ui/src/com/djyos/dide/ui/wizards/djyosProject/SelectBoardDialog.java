package com.djyos.dide.ui.wizards.djyosProject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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

import com.djyos.dide.ui.wizards.board.Board;
import com.djyos.dide.ui.wizards.board.ReadBoardXml;
import com.djyos.dide.ui.wizards.board.onboardcpu.OnBoardCpu;
import com.djyos.dide.ui.wizards.cpu.Cpu;
import com.djyos.dide.ui.wizards.cpu.ReadCpuXml;
import com.djyos.dide.ui.wizards.djyosProject.tools.DPluginImages;
import com.djyos.dide.ui.wizards.djyosProject.tools.DideHelper;

public class SelectBoardDialog extends StatusDialog {

	private String detailsDesc = null;
	private Text detailsField;
	private Label boardSearchLabel;
	private Text boardEditText;
	private Text cpuEditText;
	private Label cpuSelectLabel;
	private Button selectCpuBtn;
	private Tree boardTree;
	private List<Board> boards = new ArrayList<Board>();
	private List<Board> boardsFiltered;
	private Board boardSelected = null;
	private TreeItem t1, t2;
	private DideHelper dideHelper = new DideHelper();
	private ReadCpuXml rcx = new ReadCpuXml();
	private List<Cpu> allCpus = rcx.getAllCpus();

	public Board getSelectBoard() {
		return boardSelected;
	}

	private Listener searchModifyListener = e -> {
		String keyword = boardEditText.getText().trim();
		List<Board> boardsFiltered = getBoardsFiltered(boards, keyword);
		boardTree.removeAll();
		initBoardTree(boardsFiltered);
	};

	private Listener cpuModifyListener = e -> {
		String keyword = cpuEditText.getText().trim();
		List<Board> boardsFiltered = getBoardsFilteredByCpu(boards, keyword);
		boardTree.removeAll();
		initBoardTree(boardsFiltered);
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

	private String toolchain;

	public SelectBoardDialog(Shell parent, String sCpu) {
		super(parent);
		// TODO Auto-generated constructor stub
		setTitle("选择板件");
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
	}

	@Override
	protected Point getInitialSize() {
		// TODO Auto-generated method stub
		return new Point(560, 600);
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
		newBoradBtn.setBackground(boardTreeCpt.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		newBoradBtn.setForeground(boardTreeCpt.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		newBoradBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		detailsField = new Text(boardCpt, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		detailsField.setLayoutData(new GridData(GridData.FILL_BOTH));
		detailsField.setEditable(false);

		// 点击新建板件后弹出新建板件的向导
		newBoradBtn.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				IAction action = dideHelper.getAction("com.djyos.dide.ui.wizards.NewDWizard2");
				action.run();
				ReadBoardXml rbx = new ReadBoardXml();
				boards = rbx.getAllBoards();
				boardTree.removeAll();
				initBoardTree(boards);
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
		ReadBoardXml rbx = new ReadBoardXml();
		Composite composite = new Composite(parent, SWT.NULL);
		boardTree = new Tree(composite, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		boardTree.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		boardTree.setHeaderVisible(true);
		boards = rbx.getAllBoards();
		boardsFiltered = boards;
		initBoardTree(boards);
		boardTree.addListener(SWT.MouseDoubleClick, new Listener() {

			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				Point point = new Point(event.x, event.y);
				TreeItem item = boardTree.getItem(point);
				if (item != null) {
					String itemText = item.getText();
					if (itemText.contains("板件")) {

					} else {
						okPressed();
					}
				}
			}
		});

		boardTree.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

				TreeItem[] items = boardTree.getSelection();
				if (items.length > 0) {
					detailsDesc = "";
					String selectBoardName = items[0].getText();
					if (selectBoardName.contains("板件")) {
						detailsDesc += "选中板件即可显示选中的板件配置信息";
					} else {
						for (int i = 0; i < boards.size(); i++) {
							if (boards.get(i).getBoardName().equals(selectBoardName)) {
								Board board = boards.get(i);
								List<OnBoardCpu> cpus = board.getOnBoardCpus();
								for (int j = 0; j < cpus.size(); j++) {
									OnBoardCpu cpu = cpus.get(j);
									String chipString = "";
									if (cpu.getChips().size() > 0) {
										for (int k = 0; k < cpu.getChips().size(); k++) {
											chipString += ((k != 0 ? "，" : "") + cpu.getChips().get(k).getChipName());
										}
									}

									String peripheralString = "";
									for (int k = 0; k < cpu.getPeripherals().size(); k++) {
										peripheralString += ((k != 0 ? "，" : "")
												+ cpu.getPeripherals().get(k).getName());
									}
									detailsDesc += "Cpu" + (j + 1) + ": " + cpu.getCpuName() + "\n主晶振频率: "
											+ cpu.getMianClk() + "\nRtc钟频率: " + cpu.getRtcClk() + "\n芯片: " + chipString
											+ "\n外设: " + peripheralString + "\n\n";
								}

								break;
							}
						}
					}
					detailsField.setText(detailsDesc);
				}
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

	private void initBoardTree(List<Board> boards) {
		// TODO Auto-generated method stub
		t2 = new TreeItem(boardTree, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		t2.setData(dideHelper.getUserBoardFilePath());
		t2.setImage(DPluginImages.TREE_FLODER_VIEW.createImage());
		t2.setText("用户板件");

		t1 = new TreeItem(boardTree, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		t1.setData(dideHelper.getDemoBoardFilePath());
		t1.setImage(DPluginImages.TREE_FLODER_VIEW.createImage());
		t1.setText("Djyos板件");

		for (int i = 0; i < boards.size(); i++) {
			boolean newTree = true;

			// 判断当前的板件是否需要显示在界面上
			// if (boards.get(i).getOnBoardCpus().size() < 2) {
			// OnBoardCpu onBoardCpu = boards.get(i).getOnBoardCpus().get(0);
			// Cpu cpu = dideHelper.getCpuByonBoard(onBoardCpu, allCpus);
			//
			// if (cpu != null) {
			// if (cpu.getCores().size() < 2) {
			// Arch arch = cpu.getCores().get(0).getArch();
			// String tc = arch.getToolchain();
			// String tcStart = toolchain.split("-")[0];
			// if (!tc.startsWith(tcStart)) {
			// newTree = false;
			// }
			// }
			// }
			// }

			// if (newTree) {
			TreeItem t;
			if (boards.get(i).getBoardPath().contains("demo")) {
				t = new TreeItem(t1, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
			} else {
				t = new TreeItem(t2, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
			}
			t.setData(boards.get(i).getBoardPath());
			t.setImage(DPluginImages.DESC_BOARD_VIEW.createImage());
			t.setText(boards.get(i).getBoardName());
			// }

		}
		t1.setExpanded(true);
		// t2.setExpanded(true);
	}

}
