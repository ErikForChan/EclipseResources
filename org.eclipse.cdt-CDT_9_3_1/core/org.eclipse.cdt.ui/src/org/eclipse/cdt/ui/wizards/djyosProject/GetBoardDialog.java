package org.eclipse.cdt.ui.wizards.djyosProject;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.internal.resources.Folder;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;

import org.eclipse.cdt.ui.wizards.board.Board;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.Chip;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.OnBoardCpu;
import org.eclipse.cdt.ui.wizards.component.Component;
import org.eclipse.cdt.ui.wizards.cpu.Cpu;
import org.eclipse.cdt.ui.wizards.cpu.ReadCpuXml;
import org.eclipse.cdt.ui.wizards.cpu.core.Core;
import org.eclipse.cdt.ui.wizards.cpu.core.memory.CoreMemory;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.preferences.PreferencesMessages;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ComboDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringDialogField;

public class GetBoardDialog extends StatusDialog {

	private Text boardSelectField;
	private Text boardCreateField;
	private StringDialogField[] fDialogFields = new StringDialogField[2];
	private ComboDialogField[] fComboDialogFields = new ComboDialogField[2];
	private static Button[] radioBtns = new Button[2];
	private int selectIndex = 0;
	private String boardName = "";
	private Board boardSelected;
	private Core coreSelected;
	private List<Cpu> boardCpusList = new ArrayList<Cpu>();
	List<Chip> chips = null; 
	List<Component> components = null;
	private Cpu selectCpu;
	String boardModuleTrimPath = "";
	Cpu defaultCpu;
	private Composite content, baordDescCpt;
	private Text boardDetailsDesc, MCUNameField, coreSelectFiled;
	private Button importMCUBtn;
	private Button selectCoreBtn;

	private class CompilerImportBoardAdapter implements IDialogFieldListener {
		@Override
		public void dialogFieldChanged(DialogField field) {
			doValidation();
		}
	}

	private void doValidation() {
		StatusInfo status = new StatusInfo();
		String newText = boardSelectField.getText();
		if (newText.isEmpty()) {
			status.setError(PreferencesMessages.TodoTaskInputDialog_error_enterName);
		} else {
			if (newText.indexOf(',') != -1) {
				status.setError(PreferencesMessages.TodoTaskInputDialog_error_comma);
			}
		}
		updateStatus(status);
	}

	public String getBoardName() {
		return boardName;
	}

	public Cpu getSelectCpu() {
		return selectCpu;
	}

	public Board getSelectBoard() {
		return boardSelected;
	}
	
	public Core getSelectCore() {
		return coreSelected;
	}

	private String[] BoardDetailsComboLabels = { "Architecture", "Family" };

	private String[] BoardDetailsTextLabels = { "Select Cpu", "Core Clock" };

	private String[] Architectures = { "armv4", "armv5", "armv6", "armv7", "armv7-m", "armv7e-m" };

	private String[] Families = { "cortex-m0", "cortex-m3", "cortex-m4", "cortex-m7" };

	public List<String> getBoards() {
		List<String> boards = new ArrayList<String>();
		String newBoardPath = getEclipsePath() + "djysrc/bsp/boarddrv";
		File boardSrc = new File(newBoardPath);
		String files[] = boardSrc.list();
		for (String file : files) {
			boards.add(file);
		}
		return boards;
	}

	public String getEclipsePath() {
		String fullPath = Platform.getInstallLocation().getURL().toString();
		String eclipsePath = fullPath.substring(6,
				(fullPath.substring(0, fullPath.length() - 1)).lastIndexOf("/") + 1);
		return eclipsePath;
	}

	private Listener coreModifyListener = e -> {
		if (MCUNameField.getText().trim() != null) {
			Cpu defaultCpu = null;
			String cpuName = MCUNameField.getText();
			for (int i = 0; i < boardCpusList.size(); i++) {
				if (boardCpusList.get(i).getCpuName().equals(cpuName)) {
					selectCpu = boardCpusList.get(i);
				}
			}
			String coreDesc = "";
			if(coreSelected!=null) {
				String arch = coreSelected.getArch();
				String family = coreSelected.getFamily();
				String fpuType = coreSelected.getFpuType();
				String resetAddr = coreSelected.getResetAddr();
				String memoryString = "";
				for (int k = 0; k < coreSelected.getMemorys().size(); k++) {
					CoreMemory memory = coreSelected.getMemorys().get(k);
					if (memory.getType().equals("ROM")) {
						memoryString += "\n\t\tFlashStart: " + memory.getStartAddr() + "\n\t\tFlashSize: "
								+ memory.getSize();
					} else if (memory.getType().equals("RAM")) {
						memoryString += "\n\t\tRamStart: " + memory.getStartAddr() + "\n\t\tRamSize: "
								+ memory.getSize();
					}
				}
				coreDesc += "\tArch: " + arch + "\n\tFamily: " + family + "\n\tFputype: " + fpuType
						+ "\n\tResetAddr: " + resetAddr + "\n\tMemory:  " + memoryString + "\n";	
			}


			String chipString = "";
			String peripheralString = "";
			OnBoardCpu onBoardCpu = new OnBoardCpu();

			for(int i=0;i<boardSelected.getOnBoardCpus().size();i++) {
				if(boardSelected.getOnBoardCpus().get(i).getCpuName().equals(selectCpu.getCpuName())) {
					onBoardCpu = boardSelected.getOnBoardCpus().get(i);
					break;
				}
			}
			
			chips = onBoardCpu.getChips(); 
			components = onBoardCpu.getPeripherals();
	
			for(int i=0;i<chips.size();i++) {
				chipString+="  "+chips.get(i).getChipName();
			}
			for(int i=0;i<components.size();i++) {
				peripheralString+="  "+components.get(i).getName();
			}
			
			boardDetailsDesc.setText("Cpu: " + selectCpu.getCpuName() +"\n" 
			+coreDesc	
			+ "主晶振频率: "+onBoardCpu.getMianClk() +"\n" 
			+ "Rtc钟频率: " +onBoardCpu.getRtcClk() +"\n"
			+ "板载设备: " +chipString+ "\n" 
			+ "Cpu外设: " +peripheralString);
		}
	};
	
	private Listener clkModifyListener = e -> {
			String coreClk = fDialogFields[1].getTextControl(content).getText().trim();
			Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");  
	        boolean isInt =  pattern.matcher(coreClk).matches();
	        if(!isInt) {
	        	IWorkbenchWindow window = PlatformUI.getWorkbench()
	    				.getActiveWorkbenchWindow();
	        	MessageDialog.openError(window.getShell(), "提示",
						"请输入正整数");
	        }			
	};

//	private Listener boardModifyListener = e -> {
//		if (boardSelectField.getText().trim() != null) {
//			importMCUBtn.setEnabled(true);
//		}
//	};
	
//	private Listener cpuModifyListener = e -> {
//		if (selectCpu != null) {
//			List<Core> cores = selectCpu.getCores();
//			if(cores.size()<2) {
//				coreSelectFiled.setText("Core1");
//			}
//		}
//	};

	
	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		String cpuName = MCUNameField.getText().trim();
		String coreName = coreSelectFiled.getText().trim();
		String coreClk = fDialogFields[1].getTextControl(content).getText().trim();
		boardName = boardSelectField.getText().trim();
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if(boardName.equals("")) {
			MessageDialog.openError(window.getShell(), "提示",
					"请选择板件");
		}else if(cpuName.equals("")){
			MessageDialog.openError(window.getShell(), "提示",
					"请选择Cpu");
		}else if(coreName.equals("")){
			MessageDialog.openError(window.getShell(), "提示",
					"请选择内核");
		}else if(coreClk.equals("")){
			MessageDialog.openError(window.getShell(), "提示",
					"请选择内核时钟");
		}else {
			coreSelected.setCoreClk(Integer.parseInt(coreClk));
			for (int i = 0; i < boardCpusList.size(); i++) {
				if (boardCpusList.get(i).getCpuName().equals(cpuName)) {
					defaultCpu = boardCpusList.get(i);
					break;
				}
			}
			selectCpu = defaultCpu;
			super.okPressed();
		}		
		
	}

	private void copyFolder(File src, File dest) throws IOException {
		if (src.isDirectory()) {
			if (!dest.exists()) {
				dest.mkdir();
			}
			String files[] = src.list();
			for (String file : files) {
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				// 递归复制
				copyFolder(srcFile, destFile);
			}
		} else {
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);

			byte[] buffer = new byte[1024];

			int length;

			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}
			in.close();
			out.close();
		}
	}

	private void copyFileToFolder(File src, File dest, String boardName) throws IOException {
		if (src.isDirectory()) {
			if (!dest.exists()) {
				dest.mkdir();
				dest.renameTo(
						new File(dest.getAbsolutePath().substring(0, dest.getAbsolutePath().lastIndexOf("\\"))
								+ "\\" + boardName));
				dest = new File(dest.getAbsolutePath().substring(0, dest.getAbsolutePath().lastIndexOf("\\"))
						+ "\\" + boardName);
			}
			String files[] = src.list();
			for (String file : files) {
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				System.out.println(destFile.getName());
				// 递归复制
				copyFileToFolder(srcFile, destFile, boardName);
			}
		} else {
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);

			byte[] buffer = new byte[1024];

			int length;

			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}
			in.close();
			out.close();
		}
	}

	public GetBoardDialog(Shell parent) {
		super(parent);
		setTitle("板件选择");
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
	}

	@Override
	protected Point getInitialSize() {
		// TODO Auto-generated method stub
		return new Point(500, 500);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// TODO Auto-generated method stub
		String tipText = "板件模板会陆续添加.";
		CompilerImportBoardAdapter adapter = new CompilerImportBoardAdapter();
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setSize(500, 500);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 5;
		layout.numColumns = 1;
		layout.marginLeft = 5;
		Composite tipCpt = new Composite(composite, SWT.NONE);
		tipCpt.setLayout(layout);
		tipCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label tipLabel = new Label(tipCpt, SWT.NONE);
		tipLabel.setText(tipText);
//		tipLabel.setForeground(tipCpt.getDisplay().getSystemColor(SWT.COLOR_RED));

		content = new Composite(composite, SWT.NONE);
		layout.numColumns = 3;
		content.setLayout(layout);
		content.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label boardSelectLabel = new Label(content, SWT.NONE);
		boardSelectLabel.setText("Select Board: ");
		boardSelectField = new Text(content, SWT.BORDER);
		boardSelectField.setSize(50, 10);
		boardSelectField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		boardSelectField.setEnabled(false);
		Button importOrNewBtn = new Button(content, SWT.PUSH);
		importOrNewBtn.setText(" Select...  ");
		importOrNewBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleImportButtonPressed();
			}
		});
//		boardSelectField.addListener(SWT.Modify, boardModifyListener);

		Label MCULabel = new Label(content, SWT.NONE);
		MCULabel.setLayoutData(new GridData(GridData.BEGINNING));
		MCULabel.setText(BoardDetailsTextLabels[0] + ":    ");
		MCUNameField = new Text(content, SWT.BORDER);
		MCUNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		MCUNameField.setEnabled(false);
		importMCUBtn = new Button(content, SWT.PUSH);
		importMCUBtn.setText(" Select...  ");
		importMCUBtn.setEnabled(false);
		importMCUBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleChooseButtonPressed();
			}

		});
//		MCUNameField.addListener(SWT.Modify, cpuModifyListener);
		
		Label CoreLabel = new Label(content, SWT.NONE);
		CoreLabel.setLayoutData(new GridData(GridData.BEGINNING));
		CoreLabel.setText("Select Core" + ":    ");
		coreSelectFiled = new Text(content, SWT.BORDER);
		coreSelectFiled.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		coreSelectFiled.setEnabled(false);
	    selectCoreBtn = new Button(content, SWT.PUSH);
		selectCoreBtn.setEnabled(false);
		selectCoreBtn.setText(" Select...  ");
		selectCoreBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleSelectCoreButtonPressed();
			}
		});
		coreSelectFiled.addListener(SWT.Modify, coreModifyListener);


		fDialogFields[1] = new StringDialogField();
		fDialogFields[1].setLabelText(BoardDetailsTextLabels[1] + ":");
		fDialogFields[1].getLabelControl(content).setLayoutData(new GridData(GridData.BEGINNING));
		fDialogFields[1].getTextControl(content).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fDialogFields[1].getTextControl(content).addListener(SWT.Modify, clkModifyListener);
//		fDialogFields[1].getTextControl(content).setEnabled(false);
		ControlFactory.createLabel(content, "MHz");

		baordDescCpt = new Composite(composite, SWT.NONE);
		GridLayout gl = new GridLayout();
		gl.numColumns = 1;
		gl.marginHeight = 10;
		gl.marginLeft = 10;
		baordDescCpt.setLayout(gl);
		baordDescCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label baordDescLabel = new Label(baordDescCpt, SWT.NONE);
		baordDescLabel.setText("Board Description:");
		boardDetailsDesc = new Text(baordDescCpt, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		boardDetailsDesc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		boardDetailsDesc.setText("Cpu: \n" 
		+ "\tArch: \n"
		+ "\tFamily: \n"
		+ "\tFputype: \n"
		+ "\tResetAddr: \n"
		+ "\tMemory: \n"
		+ "主晶振频率: \n" 
		+ "Rtc钟频率: \n"
		+ "板载设备: \n" 
		+ "Cpu外设: ");

		boardDetailsDesc.setEditable(false);
		boardDetailsDesc.setBounds(0, 0, 200, 700);
		super.createDialogArea(parent);
		return super.createDialogArea(parent);
	}

	protected void handleSelectCoreButtonPressed() {
		// TODO Auto-generated method stub
		List<Core> cores = selectCpu.getCores();
		SelectCoreDialog dialog = new SelectCoreDialog(getShell(),cores);
		if(dialog.open() == Window.OK) {
			coreSelected = dialog.getSelectCore();
			for(int i=0;i<cores.size();i++) {
				if(coreSelected.equals(cores.get(i))) {
					coreSelectFiled.setText("Core"+(i+1));
				}
			}
		}
		
	}

	protected void handleChooseButtonPressed() {
		SelectCpuDialog dialog = new SelectCpuDialog(getShell(), boardCpusList);
//		ChooseMCUDialog dialog = new ChooseMCUDialog(getShell());
		if (dialog.open() == Window.OK) {
			selectCpu = dialog.getSelectCpu();
			MCUNameField.setText(selectCpu.getCpuName());
			List<Core> cores = selectCpu.getCores();
			if (cores.size()<2) {
				coreSelected = cores.get(0);
				coreSelectFiled.setText("Core1");
				selectCoreBtn.setEnabled(false);
				// boardSelected.setCpu(selectCpu);
			}else {
				selectCoreBtn.setEnabled(true);
			}
		}
	}

	protected void handleImportButtonPressed() {

		SelectBoardDialog dialog;
		String sCpu = MCUNameField.getText();

		dialog = new SelectBoardDialog(getShell(), sCpu);

		if (dialog.open() == Window.OK) {
			boardCpusList = new ArrayList<Cpu>();
			boardSelected = dialog.getSelectBoard();
			boardSelectField.setText(boardSelected.getBoardName());
			List<OnBoardCpu> cpusOn = boardSelected.getOnBoardCpus();
			ReadCpuXml rcx = new ReadCpuXml();
			List<Cpu> cpusList = rcx.getAllCpus();
			for (int i = 0; i < cpusOn.size(); i++) {
				String cpuName = cpusOn.get(i).getCpuName();
				for(int j=0;j<cpusList.size();j++) {
					if(cpuName.equals(cpusList.get(j).getCpuName())){
						boardCpusList.add(cpusList.get(j));
						break;
					}
				}
			}
			if(boardCpusList.size()<2) {
				selectCpu = boardCpusList.get(0);
				MCUNameField.setText(selectCpu.getCpuName());
				List<Core> cores = selectCpu.getCores();
				if (cores.size()<2) {
					importMCUBtn.setEnabled(false);
					coreSelected = cores.get(0);
					coreSelectFiled.setText("Core1");
					selectCoreBtn.setEnabled(false);
					// boardSelected.setCpu(selectCpu);
				}else {
					selectCoreBtn.setEnabled(true);
				}
			}else {
				importMCUBtn.setEnabled(true);
			}
		}

	}

	@Override
	protected void configureShell(Shell shell) {
		// TODO Auto-generated method stub
		super.configureShell(shell);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, ICHelpContextIds.TODO_TASK_INPUT_DIALOG);
	}
}
