package org.eclipse.cdt.ui.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.cdt.ui.wizards.board.Board;
import org.eclipse.cdt.ui.wizards.board.ReadBoardXml;
import org.eclipse.cdt.ui.wizards.parsexml.Cpu;
import org.eclipse.cdt.ui.wizards.parsexml.ReadBoardByDom;

public class ChooseBoardDialog extends StatusDialog{

	String curCpuName = null;
	
	public ChooseBoardDialog(Shell parent) {
		super(parent);
		// TODO Auto-generated constructor stub
		setTitle("SelectBoard");
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX );		
	}
	
	public ChooseBoardDialog(Shell parent,String sCpu) {
		super(parent);
		// TODO Auto-generated constructor stub
		curCpuName = sCpu;
		setTitle("SelectBoard");
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX );
	}
	
	String fullPath = Platform.getInstallLocation().getURL().toString();
	String eclipsePath = fullPath.substring(6,(fullPath.substring(0,fullPath.length()-1)).lastIndexOf("/")+1);
	private Label boardSearchLabel;
	private Label cpuSelectLabel;
	private Table boardListTable;
	private Text boardEditText;
	private Button selectCpuBtn;
	private TableViewer tv;
	private TableColumn[] tableColumns = new TableColumn[3];
	private Button filterBoardBox;
	private String[] boardDetails = {"Board name","Cpu name","External clock(MHz)"};
	List<Board> boards = new ArrayList<Board>();
	List<Board> boardsFiltered = new ArrayList<Board>();
	int selectIndex = 0;

	public Board getSelectBoard() {
		return boardsFiltered.get(selectIndex);
	}
	
	public List<Board> getBoardsFiltered(List<Board> boards,String keyWord){
		
		boardsFiltered = new ArrayList<Board>();
		Pattern pattern = Pattern.compile(keyWord,Pattern.CASE_INSENSITIVE);
		for(int i=0;i<boards.size();i++) {
			Matcher matcher = pattern.matcher(boards.get(i).getBoardName());
			Matcher matcher2 = pattern.matcher(boards.get(i).getCpu().getDevice());
			if(matcher.find() || matcher2.find()) {
				boardsFiltered.add(boards.get(i));
			}
		}
		return boardsFiltered;
		
	}
	
	private  Listener searchModifyListener = e -> {
		//setLocationForSelection();
		String keyword = boardEditText.getText().trim();
		List<Board> boardsFiltered = getBoardsFiltered(boards,keyword);
		tv.setInput(boardsFiltered);
	};
	
	public class BoardLabelProvider extends LabelProvider implements ITableLabelProvider {  
        public String getColumnText(Object element, int columnIndex) {  
            if (element instanceof Board){  
                Board board = (Board)element;  
                if(columnIndex == 0){  
                	return board.getBoardName(); 
                }else if(columnIndex == 1){  
                    return board.cpu.getDevice();
                }else if (columnIndex ==2){  
                     return board.getExClk(); 
                }
            }  
            return null;  
        }  
        public Image getColumnImage(Object element, int columnIndex) {  
            return null;  
        }  
    }  
	
	public List<Board> getBoards(){
		
		ReadBoardXml rbx = new ReadBoardXml();
		List<Board> boards = new ArrayList<Board>();
		String boardForderPath = eclipsePath+"djysrc/bsp/boarddrv/demo/";
		File boardForder = new File(boardForderPath);
		File[] files = boardForder.listFiles();
		for(File file : files) {
			Board board = new Board();
			String xmlName = "board_"+file.getName()+".xml";
			try {
				board=rbx.getBoard(boardForderPath+file.getName()+"/"+xmlName);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			boards.add(board);
		}
		return boards;
		
	}
	
	public void setBoardList() {
		ReadBoardByDom rbbd = new ReadBoardByDom();
		try {
			boards = getBoards();
			boardsFiltered = boards;
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		tv.setInput(boards);
	}
		
	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		super.okPressed();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = (Composite) super.createDialogArea(parent);
		GridData gd;
		getBoards();
		Composite boardSearchCpt = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		layout.horizontalSpacing=10;
		boardSearchCpt.setLayout(layout);
		boardSearchCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		boardSearchLabel = new Label(boardSearchCpt, SWT.None);
		boardSearchLabel.setText("Board Search :");
		boardEditText = new Text(boardSearchCpt, SWT.BORDER);
		boardEditText.addListener(SWT.Modify, searchModifyListener);
		
//		Composite cpuSelectCpt = new Composite(boardSearchCpt, SWT.NONE);
//		GridLayout cpuSelectLayout = new GridLayout();
//		cpuSelectLayout.numColumns = 2;
//		cpuSelectLayout.horizontalSpacing= 5;
//		cpuSelectLayout.marginLeft = 5;
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

		Composite boardListCpt = new Composite(composite, SWT.NONE);
		GridLayout boardLayout = new GridLayout();
		boardLayout.marginWidth = 0;
		boardLayout.numColumns = 1;
		boardListCpt.setLayout(boardLayout);
		boardListCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_CENTER));
//		filterBoardBox = new Button(boardListCpt, SWT.CHECK);
//		filterBoardBox.setText("Only display the Board with the cpu selected.");
		boardListTable = new Table(boardListCpt, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.H_SCROLL | SWT.FULL_SELECTION);
		boardListTable.setHeaderVisible(true);
		boardListTable.setLinesVisible(true);
//		filterBoardBox.addSelectionListener(new SelectionListener() {
//			
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				// TODO Auto-generated method stub
//				ReadBoardByDom rbbd = new ReadBoardByDom();
//				try {
//					boards = getBoards();
//				} catch (Exception e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//			
//				if(filterBoardBox.getSelection()) {
//					if(!curCpuName.equals("")) {
//						boards = getBoardsFiltered(boards,curCpuName);
//					}
//				}
//				tv.setInput(boards);
//			}
//			
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
		boardListTable.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectIndex = boardListTable.getSelectionIndex();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
//				if (buttonIsEnabled(2) && table.getSelectionIndex() != -1)
//					buttonPressed(2);
			}
		});
		
		boardListTable.addListener(SWT.MouseDoubleClick, new Listener() {

			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				selectIndex = boardListTable.getSelectionIndex();
				okPressed();
			}
			
		});

		tv = new TableViewer(boardListTable);
		tv.setContentProvider(new IStructuredContentProvider() {

			@Override
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof List) {
					return ((List) inputElement).toArray();
				} else {
					return new Object[0];
				}
			}

			@Override
			public void dispose() {
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		
		tv.setLabelProvider(new BoardLabelProvider());
		for(int i=0;i<tableColumns.length;i++) {
			tableColumns[i] = new TableColumn(boardListTable, SWT.LEFT);
			tableColumns[i].setText(boardDetails[i]);
			tableColumns[i].setWidth(150);
		}
		setBoardList();
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 3;
		boardListTable.setLayoutData(gd);
		return super.createDialogArea(parent);
	}

	Cpu selectCpu;
	protected void handleSelectCpuPressed() {
		// TODO Auto-generated method stub
		ChooseMCUDialog dialog = new ChooseMCUDialog(getShell());
		if (dialog.open() == Window.OK) {
			selectCpu = dialog.getSelectCpu();
//			MCUNameField.setText(selectCpu.getDevice());
			 List<Board> boardsFiltered = getBoardsFiltered(boards,selectCpu.getDevice());
			 tv.setInput(boardsFiltered);
		}
	}

}
