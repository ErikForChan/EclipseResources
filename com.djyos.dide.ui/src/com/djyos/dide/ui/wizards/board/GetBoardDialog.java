package com.djyos.dide.ui.wizards.board;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.objects.Board;
import com.djyos.dide.ui.objects.Chip;
import com.djyos.dide.ui.objects.Component;
import com.djyos.dide.ui.objects.Core;
import com.djyos.dide.ui.objects.CoreMemory;
import com.djyos.dide.ui.objects.Cpu;
import com.djyos.dide.ui.objects.OnBoardCpu;
import com.djyos.dide.ui.wizards.cpu.ReadCpuXml;
import com.djyos.dide.ui.wizards.cpu.SelectCoreDialog;
import com.djyos.dide.ui.wizards.cpu.SelectCpuDialog;
import com.djyos.dide.ui.wizards.djyosProject.tools.PathTool;
import com.djyos.dide.ui.wizards.djyosProject.tools.UnitData;

@SuppressWarnings("restriction")
public class GetBoardDialog extends StatusDialog {

	private Text boardSelectField, boardDetailsDesc, MCUNameField, coreSelectFiled;
	private StringDialogField[] fDialogFields = new StringDialogField[2];
	private String boardName = "";
	public String boardModuleTrimPath = "";
	private Board boardSelected;
	private Core coreSelected;
	private List<Cpu> boardCpusList = new ArrayList<Cpu>();
	private List<Chip> chips = null;
	private List<Component> components = null;
	private Cpu selectCpu;
	public Cpu defaultCpu;
	private List<Cpu> cpusList = ReadCpuXml.getAllCpus();
	private Composite content, baordDescCpt;
	private Button importMCUBtn, selectCoreBtn;

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

	public List<String> getBoards() {
		List<String> boards = new ArrayList<String>();
		String newBoardPath = PathTool.getDIDEPath() + "djysrc/bsp/boarddrv";
		File boardSrc = new File(newBoardPath);
		String files[] = boardSrc.list();
		for (String file : files) {
			boards.add(file);
		}
		return boards;
	}

	private Listener coreModifyListener = e -> {
		if (MCUNameField.getText().trim() != null) {
			String cpuName = MCUNameField.getText();
			for (int i = 0; i < boardCpusList.size(); i++) {
				if (boardCpusList.get(i).getCpuName().equals(cpuName)) {
					selectCpu = boardCpusList.get(i);
				}
			}
			String coreDesc = "";
			if (coreSelected != null) {
				String toolchain = coreSelected.getArch().getToolchain();
				String arch = coreSelected.getArch().getMarch();
				String family = coreSelected.getArch().getMcpu();
				String fpuType = coreSelected.getFpuType();
				String resetAddr = coreSelected.getResetAddr();
				String memoryString = "";
				List<CoreMemory> romMems = new ArrayList<CoreMemory>(),ramMems = new ArrayList<CoreMemory>();
				for (int k = 0; k < coreSelected.getMemorys().size(); k++) {
					CoreMemory memory = coreSelected.getMemorys().get(k);
					if (memory.getType().equals("ROM")) {
						romMems.add(memory);
					} else if (memory.getType().equals("RAM")) {
						ramMems.add(memory);
					}
				}
				
				for(CoreMemory m:romMems) {
					String countStr = romMems.size()>1?String.valueOf(romMems.indexOf(m)+1):"";
					memoryString += "\n\t\tFlashStart"+countStr+":\t" + m.getStartAddr() + "\n\t\tFlashSize"+countStr+":\t"
							+ m.getSize();
				}
				
				for (CoreMemory m : ramMems) {
					String countStr = ramMems.size()>1?String.valueOf(ramMems.indexOf(m)+1):"";
					memoryString += "\n\t\tRamStart"+countStr+":\t" + m.getStartAddr() + "\n\t\tRamSize"+countStr+":\t"
							+ m.getSize();
				}
				
				if (toolchain != null) {
					coreDesc += "\t工具链: " +toolchain;
				}
				if (arch != null) {
					coreDesc += "\n\t架构: " +arch;
				}
				if (family != null) {
					coreDesc += "\n\t家族: " +family;
				}
				if (fpuType != null) {
					coreDesc += "\n\t浮点: " +fpuType;
				}
				if (resetAddr != null) {
					coreDesc += "\n\t复位地址: " +resetAddr;
				}
				coreDesc += "\n\t内存:  " + memoryString +  "\n";
			}

			String chipString = "";
			String peripheralString = "";
			OnBoardCpu onBoardCpu = new OnBoardCpu();

			for (int i = 0; i < boardSelected.getOnBoardCpus().size(); i++) {
				if (boardSelected.getOnBoardCpus().get(i).getCpuName().equals(selectCpu.getCpuName())) {
					onBoardCpu = boardSelected.getOnBoardCpus().get(i);
					break;
				}
			}

			chips = onBoardCpu.getChips();
			components = onBoardCpu.getPeripherals();

			for (int i = 0; i < chips.size(); i++) {
				chipString += "  " + chips.get(i).getChipName();
			}
			for (int i = 0; i < components.size(); i++) {
				peripheralString += "\n" + components.get(i).getName();
			}

			boardDetailsDesc.setText(
					"Cpu: " + selectCpu.getCpuName() + "\n" + coreDesc + 
					"主晶振频率: " + UnitData.UnitMhz(String.valueOf(onBoardCpu.getMianClk())) + "\n" + 
					"Rtc钟频率: " + onBoardCpu.getRtcClk() + "\n" + 
					"板载设备: " + chipString + "\n" + 
					"Cpu外设: " + peripheralString);
		}
	};

	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		String boardFolderPath = boardSelected.getBoardFolderPath();
		File drvFolder = new File(boardFolderPath + "/drv");
		File ldsFolder = new File(boardFolderPath + "/lds");
		String validMsg = null;
		if (!drvFolder.exists()) {
			validMsg = "所选板件的驱动不存在";
		} else {
			if (!ldsFolder.exists()) {
				validMsg = "所选板件的lds不存在";
			}
		}

		if (validMsg != null) {
			DideHelper.showErrorMessage(validMsg);
		} else {
			String cpuName = MCUNameField.getText().trim();
			String coreName = coreSelectFiled.getText().trim();
			String coreClk = fDialogFields[1].getTextControl(content).getText().trim();
			boardName = boardSelectField.getText().trim();
			if (boardName.equals("")) {
				DideHelper.showErrorMessage("请选择板件");
			} else if (cpuName.equals("")) {
				DideHelper.showErrorMessage("请选择Cpu");
			} else if (coreName.equals("")) {
				DideHelper.showErrorMessage("请选择内核");
			} else if (coreClk.equals("")) {
				DideHelper.showErrorMessage("请选择内核时钟");
			} else {
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

	}

	public GetBoardDialog(Shell parent) {
		super(parent);
		setTitle("板件选择");
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
	}

	protected void handleSelectCoreButtonPressed() {
		// TODO Auto-generated method stub
		List<Core> cores = selectCpu.getCores();
		SelectCoreDialog dialog = new SelectCoreDialog(getShell(), cores);
		if (dialog.open() == Window.OK) {
			coreSelected = dialog.getSelectCore();
			for (int i = 0; i < cores.size(); i++) {
				String coreName = (cores.get(i).getName() == null)?("Core" + (i + 1)):cores.get(i).getName();
				if (coreSelected.equals(cores.get(i))) {
					coreSelectFiled.setText(coreName);
				}
			}
		}

	}

	protected void handleChooseButtonPressed() {
		SelectCpuDialog dialog = new SelectCpuDialog(getShell(), boardCpusList);
		if (dialog.open() == Window.OK) {
			selectCpu = dialog.getSelectCpu();
			MCUNameField.setText(selectCpu.getCpuName());
			List<Core> cores = selectCpu.getCores();
			if (cores.size() < 2) {
				coreSelected = cores.get(0);
				coreSelectFiled.setText("Core1");
				selectCoreBtn.setEnabled(false);
				// boardSelected.setCpu(selectCpu);
			} else {
				selectCoreBtn.setEnabled(true);
			}
		}
	}

	protected void handleImportButtonPressed() {

		SelectBoardDialog dialog;
		dialog = new SelectBoardDialog(getShell());

		if (dialog.open() == Window.OK) {
			boardCpusList = new ArrayList<Cpu>();
			boardSelected = dialog.getSelectBoard();
			boardSelectField.setText(boardSelected.getBoardName());
			List<OnBoardCpu> cpusOn = boardSelected.getOnBoardCpus();
			for (int i = 0; i < cpusOn.size(); i++) {
				String cpuName = cpusOn.get(i).getCpuName();
				for (int j = 0; j < cpusList.size(); j++) {
					if (cpuName.equals(cpusList.get(j).getCpuName())) {
						boardCpusList.add(cpusList.get(j));
						break;
					}
				}
			}
			if (boardCpusList.size() < 2) {
				selectCpu = boardCpusList.get(0);
				MCUNameField.setText(selectCpu.getCpuName());
				List<Core> cores = selectCpu.getCores();
				if (cores.size() < 2) {
					importMCUBtn.setEnabled(false);
					coreSelected = cores.get(0);
					coreSelectFiled.setText("Core1");
					selectCoreBtn.setEnabled(false);
					// boardSelected.setCpu(selectCpu);
				} else {
					selectCoreBtn.setEnabled(true);
				}
			} else {
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

	@Override
	protected Point getInitialSize() {
		// TODO Auto-generated method stub
		return new Point(600, 660);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setSize(500, 500);
		GridLayout layout = new GridLayout();
		layout.marginTop = 5;
		layout.marginLeft = 5;

		content = new Composite(composite, SWT.NONE);
		layout.numColumns = 3;
		content.setLayout(layout);
		content.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label boardSelectLabel = new Label(content, SWT.NONE);
		boardSelectLabel.setText("板件: ");
		boardSelectField = new Text(content, SWT.BORDER);
		boardSelectField.setSize(50, 10);
		boardSelectField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		boardSelectField.setEnabled(false);
		Button importOrNewBtn = new Button(content, SWT.PUSH);
		importOrNewBtn.setText(" Select...  ");
		importOrNewBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
//				ColorDialog d = new ColorDialog(composite.getShell(), SWT.None);
//				d.open();
//				FontDialog d = new FontDialog(composite.getShell(), SWT.None);
//				WizardDialog d = new WizardDialog(parentShell, newWizard)
//				d.open();
				handleImportButtonPressed();
			}
		});
		// boardSelectField.addListener(SWT.Modify, boardModifyListener);

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

		Label CoreLabel = new Label(content, SWT.NONE);
		CoreLabel.setLayoutData(new GridData(GridData.BEGINNING));
		CoreLabel.setText("内核" + ":    ");
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
		fDialogFields[1].getTextControl(content).addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				String coreClk = fDialogFields[1].getTextControl(content).getText().trim();
				Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
				boolean isInt = pattern.matcher(coreClk).matches();
				if (!isInt) {
					fDialogFields[1].getTextControl(content).setText("");
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					MessageDialog.openError(window.getShell(), "提示", "请输入正整数");
				}
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		ControlFactory.createLabel(content, "MHz");

		baordDescCpt = new Composite(composite, SWT.NONE);
		GridLayout gl = new GridLayout();
		gl.numColumns = 1;
		gl.marginLeft = 10;
		baordDescCpt.setLayout(gl);
		baordDescCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		Label baordDescLabel = new Label(baordDescCpt, SWT.NONE);
		baordDescLabel.setText("板件描述:");
		boardDetailsDesc = new Text(baordDescCpt, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		boardDetailsDesc.setLayoutData(new GridData(GridData.FILL_BOTH));

		boardDetailsDesc.setText("Cpu: \n" + "\t架构: \n" + "\t家族: \n" + "\t浮点: \n" + "\t复位地址: \n" + "\t内存: \n"
				+ "主晶振频率: \n" + "Rtc钟频率: \n" + "板载设备: \n" + "Cpu外设: ");

		boardDetailsDesc.setEditable(false);
		super.createDialogArea(parent);
		return super.createDialogArea(parent);
	}

	private String[] BoardDetailsTextLabels = { "CPU", "内核时钟" };
}
