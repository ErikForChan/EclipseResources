package org.eclipse.cdt.ui.wizards.board;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
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

import org.eclipse.cdt.ui.wizards.board.onboardcpu.Chip;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.OnBoardCpu;
import org.eclipse.cdt.ui.wizards.component.Component;
import org.eclipse.cdt.ui.wizards.cpu.Cpu;
import org.eclipse.cdt.ui.wizards.cpu.ReadCpuXml;
import org.eclipse.cdt.ui.wizards.djyosProject.tools.DeleteFolder;

import org.eclipse.cdt.internal.ui.CPluginImages;

public class BoardMainWizard1 extends WizardPage{
	
	private Tree tree;
	private Text configInfoText = null;
	private MenuItem newBoardItem,deleteItem,reviseItem;
	private Composite infoArea;
	private List<Board> boardsList;
	IWorkbenchWindow window = PlatformUI.getWorkbench()
			.getActiveWorkbenchWindow();
	
	public BoardMainWizard1(String pageName) {
		super(pageName);
		// TODO Auto-generated constructor stubsuper(pageName);
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
		String extraString = "�Ҽ�����б���½���� :";
		String QAQ = "\tIDE�ּ�Ŀ¼����ʽ�������ϵͳ֧�ֵ��ڶ�Cpu�����������ڹ���Cpu�ķ���,\n�������Cpu�ͷ���,�ֶ���ק�����ƶ�����";
		String descTitle = "����/Cpu����";
		
		Label extraLabel = new Label(infoArea,SWT.NULL);
		extraLabel.setText(extraString);
		extraLabel.setForeground(infoArea.getDisplay().getSystemColor(SWT.COLOR_RED));
		FontData newFontData = extraLabel.getFont().getFontData()[0];
		newFontData.setStyle(SWT.ITALIC | SWT.BOLD);
		newFontData.setHeight(8);
		extraLabel.setFont(new Font(infoArea.getDisplay(),newFontData));
		
		Composite contentCpt = new Composite(infoArea, SWT.NULL);
		GridLayout contentLayout = new GridLayout();
		contentLayout.numColumns = 2;
		contentCpt.setLayout(contentLayout);
		contentCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		createBoardTree(contentCpt);
		
		initPopup();
		Composite sashForm = new Composite(contentCpt, SWT.NULL);
		sashForm.setLayout(new RowLayout());
		configInfoText = new Text(sashForm, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		configInfoText.setEditable(false);
		configInfoText.setLayoutData(new RowData(350,250));
		
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

	private void initPopup() {
		// TODO Auto-generated method stub
		Menu menu = new Menu(tree);
		
		newBoardItem = new MenuItem(menu, SWT.PUSH);
		newBoardItem.setText("�½����");
		newBoardItem.setImage(CPluginImages.DESC_BOARD_VIEW.createImage());

		reviseItem = new MenuItem(menu, SWT.PUSH);
		reviseItem.setText("ά�����");
		reviseItem.setImage(CPluginImages.CPU_REVISE_VIEW.createImage());

		deleteItem = new MenuItem(menu, SWT.PUSH);
		deleteItem.setText("ɾ�����");
		deleteItem.setImage(CPluginImages.CFG_DELETE_OBJ.createImage());

		newBoardItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				NewOrReviseBoard newBoardDialog = new NewOrReviseBoard(infoArea.getShell(),true,null);
				if(newBoardDialog.open() == Window.OK) {
					Board board = newBoardDialog.getBoard();
					TreeItem t = new TreeItem(tree, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
					t.setData(board.getBoardPath());
					t.setImage(CPluginImages.DESC_BOARD_VIEW.createImage());
					t.setText(board.getBoardName());	
				}	
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		reviseItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = tree.getSelection();
				if(items.length>0) {
					String itemName = items[0].getText().trim();//��ȡ��ǰѡ���ļ���·��
					Board board = getBoardByName(itemName);
					NewOrReviseBoard newBoardDialog = new NewOrReviseBoard(infoArea.getShell(),false,board);
					newBoardDialog.open();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		deleteItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = tree.getSelection();
	        	boolean isSure = MessageDialog.openQuestion(window.getShell(), "��ʾ",
	        			"��ȷ��Ҫɾ�����["+items[0].getText()+"]��?");
	        	if(isSure) {      		
					String curFilePath = items[0].getData().toString();//��ȡ��ǰѡ���ļ���·��
					DeleteFolder dlf = new DeleteFolder();
					dlf.deleteFolder(curFilePath);
					items[0].dispose();
	        	}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		tree.setMenu(menu);
	}

	private void createBoardTree(Composite contentCpt) {
		// TODO Auto-generated method stub
		Composite sourceTreeCpt = new Composite(contentCpt, SWT.NULL);
		tree = new Tree(sourceTreeCpt, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setSize(200, 260);
		tree.setHeaderVisible(true);
		sourceTreeCpt.setTouchEnabled(true);
		final TreeColumn columnArhives = new TreeColumn(tree, SWT.NONE);
		columnArhives.setText("����б�");
		columnArhives.setWidth(190);
		columnArhives.setResizable(true);
		columnArhives.setImage(CPluginImages.CFG_CPMT_OBJ.createImage());
		
		ReadBoardXml rbx = new ReadBoardXml();
		boardsList = rbx.getAllBoards();
		for(int i=0;i<boardsList.size();i++) {
			TreeItem t = new TreeItem(tree, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
			t.setData(boardsList.get(i).getBoardPath());
			t.setImage(CPluginImages.DESC_BOARD_VIEW.createImage());
			t.setText(boardsList.get(i).getBoardName());		
		}
		
		tree.addListener(SWT.MouseDown,new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				Point point = new Point(event.x, event.y);
				TreeItem item = tree.getItem(point);
				Board curBoard = null;
				String boardDesc = "";
				if(item == null) {
					
				}
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
					String cpuDesc = "";
					int cpuCount = 1;
					for(OnBoardCpu onBoardCpu:onBoardCpus) {
						if(onBoardCpus.size()<2) {
							cpuDesc += "����Cpu��"+onBoardCpu.getCpuName()+"\n";
						}else {
							cpuDesc += "����Cpu"+cpuCount+"��"+onBoardCpu.getCpuName()+"\n";
						}
						Cpu cpu = getCpuByOnBoardCpu(onBoardCpu);
						cpuDesc += "\t�ܹ�:"+cpu.getCores().get(0).getArch()+"������:"+cpu.getCores().get(0).getFamily()
								+"\n����:"+cpu.getCores().get(0).getFpuType()+"����λ��ַ:"+cpu.getCores().get(0).getResetAddr()+"\n";
						String chipString = "";
						String peripheralString = "";
						List<Chip> chips = onBoardCpu.getChips(); 
						List<Component> components = onBoardCpu.getPeripherals();
						for(int i=0;i<chips.size();i++) {
							chipString+="  "+chips.get(i).getChipName();
						}
						for(int i=0;i<components.size();i++) {
							peripheralString+="  "+components.get(i).getName();
						}
						cpuDesc += cpuDesc
								+ "������Ƶ��: "+onBoardCpu.getMianClk() +"\n" 
								+ "Rtc��Ƶ��: " +onBoardCpu.getRtcClk() +"\n"
								+ "�����豸: " +chipString+ "\n" 
								+ "Cpu����: " +peripheralString;
						cpuCount++;
					}
					configInfoText.setText(cpuDesc);
				}
			}
		});
		
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
	
	private String getDIDEPath() {
		String fullPath = Platform.getInstallLocation().getURL().toString();
		String eclipsePath = fullPath.substring(6,(fullPath.substring(0,fullPath.length()-1)).lastIndexOf("/")+1);
		return eclipsePath;
	}

	private Board getBoardByName(String baordName) {
		for(Board board:boardsList) {
			if(board.getBoardName().endsWith(baordName)) {
				return board;
			}
		}
		return null;
	}
	
}
