package org.eclipse.cdt.ui.wizards.djyosProject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.window.Window;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.cdt.ui.wizards.board.BoardMainWizard;
import org.eclipse.cdt.ui.wizards.board.BoardWizard;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.OnBoardCpu;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.ReadTBoardXml;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.Board;

public class SelectBoard extends StatusDialog{

	private String detailsDesc = null;
	private Text detailsField;
	private Label boardSearchLabel;
	private Text boardEditText;
	private Label cpuSelectLabel;
	private Button selectCpuBtn;
	private Tree boardTree;
	private List<Board> boards = new ArrayList<Board>();
	private List<Board> boardsFiltered;
	
	public Board getSelectBoard() {
		TreeItem[] items = boardTree.getSelection();
		Board board = null;
		if (items.length > 0) {
			String boardName = items[0].getText();
			for(int i=0;i<boardsFiltered.size();i++) {
				if(boardsFiltered.get(i).getBoardName().equals(boardName)) {
					board = boardsFiltered.get(i);
					break;
				}
			}
		}
		return board;
	}
	
	private  Listener searchModifyListener = e -> {
		//setLocationForSelection();
		String keyword = boardEditText.getText().trim();
		List<Board> boardsFiltered = getBoardsFiltered(boards,keyword);
		boardTree.removeAll();
		for(int i=0;i<boardsFiltered.size();i++) {
			TreeItem t = new TreeItem(boardTree, SWT.NONE);
			t.setText(boardsFiltered.get(i).getBoardName());
		}
//		tv.setInput(boardsFiltered);
	};
	
	public List<Board> getBoardsFiltered(List<Board> boards,String keyWord){
		
		boardsFiltered = new ArrayList<Board>();
		Pattern pattern = Pattern.compile(keyWord,Pattern.CASE_INSENSITIVE);
		for(int i=0;i<boards.size();i++) {
			Matcher matcher = pattern.matcher(boards.get(i).getBoardName());
			if(matcher.find()) {
				boardsFiltered.add(boards.get(i));
			}
		}
		return boardsFiltered;
		
	}
	
	public SelectBoard(Shell parent) {
		super(parent);
		// TODO Auto-generated constructor stub
		setTitle("选择板件");
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX );	
	}
	public SelectBoard(Shell parent,String sCpu) {
		super(parent);
		// TODO Auto-generated constructor stub
//		curCpuName = sCpu;
		setTitle("SelectBoard");
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX );
	}
	

	@Override
	protected Point getInitialSize() {
		// TODO Auto-generated method stub
		return new Point(500,500);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = (Composite) super.createDialogArea(parent);
//		getBoards();
		Composite boardSearchCpt = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		layout.horizontalSpacing=10;
		boardSearchCpt.setLayout(layout);
		boardSearchCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		//板件查询
		boardSearchLabel = new Label(boardSearchCpt, SWT.None);
		boardSearchLabel.setText("Board Search :");
		boardEditText = new Text(boardSearchCpt, SWT.BORDER);
		boardEditText.addListener(SWT.Modify, searchModifyListener);
		//通过选择cpu获取相应的板件
		cpuSelectLabel = new Label(boardSearchCpt, SWT.None);
		cpuSelectLabel.setText("Select Cpu :");
		selectCpuBtn = new Button(boardSearchCpt, SWT.PUSH);
		selectCpuBtn.setText("Choose...");
		selectCpuBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleSelectCpuPressed();
			}

		});
		Composite boardCpt = new Composite(composite, SWT.NONE);
		GridLayout boardLayout = new GridLayout();
		boardLayout.numColumns=2;
		boardCpt.setLayout(boardLayout);
		boardCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createTreeForBoards(boardCpt);
		boardTree.setSize(170, 200);
		detailsField = new Text(boardCpt,SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		detailsField.setLayoutData(new GridData(GridData.FILL_BOTH));
		detailsField.setEditable(false);
		Button newBoradBtn = new Button(composite,SWT.PUSH);
		newBoradBtn.setText("Create new Board");
		//点击新建板件后弹出新建板件的向导
		newBoradBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
//				BoardMainWizard boardWizard = new BoardMainWizard();
//				boardWizard.
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		return super.createDialogArea(parent);
	}

	protected void okPressed() {
		// TODO Auto-generated method stub
		super.okPressed();
	}
	
	protected void handleSelectCpuPressed() {
		// TODO Auto-generated method stub
		ChooseMCUDialog dialog = new ChooseMCUDialog(getShell());
		if (dialog.open() == Window.OK) {
//			selectCpu = dialog.getSelectCpu();
////			MCUNameField.setText(selectCpu.getDevice());
//			 List<Board> boardsFiltered = getBoardsFiltered(boards,selectCpu.getDevice());
//			 tv.setInput(boardsFiltered);
		}
	}
	
	private void createTreeForBoards(Composite parent) {
		ReadTBoardXml rbx = new ReadTBoardXml();
		Composite composite = new Composite(parent, SWT.NULL);
		boardTree = new Tree(composite, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		boardTree.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		boardTree.setHeaderVisible(true);
		String boardFilePath = getEclipsePath()+"djysrc\\bsp\\boarddrv";
		File boardFile = new File(boardFilePath);
		File[] files = boardFile.listFiles();
		for(int i=0;i<files.length;i++){
			if(files[i].getName().contains("BoardDemo")) {
				System.out.println(files[i].getName());
				File file = files[i];
				File[] mfiles = file.listFiles();
				for(int j=0;j<mfiles.length;j++) {
					System.out.println(mfiles[j].getName());
					if(mfiles[j].getName().endsWith(".xml")) {
						try {
							Board board = rbx.getBoard(mfiles[j]);
							boards.add(board);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		boardsFiltered = boards;
		for(int i=0;i<boards.size();i++) {
			TreeItem t = new TreeItem(boardTree, SWT.NONE);
			t.setText(boards.get(i).getBoardName());
		}

		boardTree.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
				TreeItem[] items = boardTree.getSelection();
				if (items.length > 0) {
					String selectBoardName = items[0].getText();
					detailsDesc = "";
					for(int i=0;i<boards.size();i++) {
						if(boards.get(i).getBoardName().equals(selectBoardName)) {
							Board board = boards.get(i);
							List<OnBoardCpu> cpus = board.getOnBoardCpus();
							System.out.println("cpus.size(): "+cpus.size());
							for(int j=0;j<cpus.size();j++) {
								System.out.println(selectBoardName+"456");
								OnBoardCpu cpu = cpus.get(j);
								String chipString = cpu.getChips().get(0).getChipName();
								String peripheralString = cpu.getPeripherals().get(0).getName();
								for(int k=1;k<cpu.getChips().size();k++) {
									chipString+= (","+cpu.getChips().get(k).getChipName());
								}
								for(int k=1;k<cpu.getPeripherals().size();k++) {
									peripheralString+= (","+cpu.getPeripherals().get(k).getName());
								}
								detailsDesc+="Cpu: "+cpu.getCpuName()
								+"\n主时钟频率: "+cpu.getMianClk()
								+"\nRtc钟频率: "+cpu.getRtcClk()
								+"\n芯片: "+chipString
								+"\n外设: "+peripheralString
								+"\n\n";
							}
							
							break;
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
		columnBoards.setText("板件名称");
		columnBoards.setWidth(140);
		columnBoards.setResizable(false);
		columnBoards.setToolTipText("Board");
	}
	
	public String getEclipsePath() {
		String fullPath = Platform.getInstallLocation().getURL().toString();
		String eclipsePath = fullPath.substring(6,(fullPath.substring(0,fullPath.length()-1)).lastIndexOf("/")+1);
		return eclipsePath;
	}
	
}
