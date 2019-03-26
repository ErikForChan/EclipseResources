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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
		String newBoardPath = DideHelper.getDIDEPath() + "djysrc/bsp/boarddrv";
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
				String arch = coreSelected.getArch().getMarch();
				String family = coreSelected.getArch().getMcpu();
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
				coreDesc += "\t�ܹ�: " + arch + "\n\t����: " + family + "\n\t����: " + fpuType + "\n\t��λ��ַ: " + resetAddr
						+ "\n\t�ڴ�:  " + memoryString + "\n";
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
				peripheralString += "  " + components.get(i).getName();
			}

			boardDetailsDesc.setText("Cpu: " + selectCpu.getCpuName() + "\n" + coreDesc + "������Ƶ��: "
					+ onBoardCpu.getMianClk() + "\n" + "Rtc��Ƶ��: " + onBoardCpu.getRtcClk() + "\n" + "�����豸: "
					+ chipString + "\n" + "Cpu����: " + peripheralString);
		}
	};

	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		String boardFolderPath = boardSelected.getBoardPath();
		File boardFolder = new File(boardFolderPath);
		File drvFolder = new File(boardFolderPath + "/drv");
		File ldsFolder = new File(boardFolderPath + "/lds");
		String validMsg = null;
		if (!drvFolder.exists()) {
			validMsg = "��ѡ���������������";
		} else {
			if (!ldsFolder.exists()) {
				validMsg = "��ѡ�����lds������";
			}
		}

		if (validMsg != null) {
			DideHelper.showErrorMessage(validMsg);
		} else {
			String cpuName = MCUNameField.getText().trim();
			String coreName = coreSelectFiled.getText().trim();
			String coreClk = fDialogFields[1].getTextControl(content).getText().trim();
			boardName = boardSelectField.getText().trim();
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (boardName.equals("")) {
				MessageDialog.openError(window.getShell(), "��ʾ", "��ѡ����");
			} else if (cpuName.equals("")) {
				MessageDialog.openError(window.getShell(), "��ʾ", "��ѡ��Cpu");
			} else if (coreName.equals("")) {
				MessageDialog.openError(window.getShell(), "��ʾ", "��ѡ���ں�");
			} else if (coreClk.equals("")) {
				MessageDialog.openError(window.getShell(), "��ʾ", "��ѡ���ں�ʱ��");
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
		setTitle("���ѡ��");
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
	}

	protected void handleSelectCoreButtonPressed() {
		// TODO Auto-generated method stub
		List<Core> cores = selectCpu.getCores();
		SelectCoreDialog dialog = new SelectCoreDialog(getShell(), cores);
		if (dialog.open() == Window.OK) {
			coreSelected = dialog.getSelectCore();
			for (int i = 0; i < cores.size(); i++) {
				if (coreSelected.equals(cores.get(i))) {
					coreSelectFiled.setText("Core" + (i + 1));
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
		String sCpu = MCUNameField.getText();

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
		return new Point(500, 600);
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
		boardSelectLabel.setText("���: ");
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
		CoreLabel.setText("�ں�" + ":    ");
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
		fDialogFields[1].getTextControl(content).addMouseTrackListener(new MouseTrackListener() {

			@Override
			public void mouseHover(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@SuppressWarnings("restriction")
			@Override
			public void mouseExit(MouseEvent e) {
				// TODO Auto-generated method stub
				String coreClk = fDialogFields[1].getTextControl(content).getText().trim();
				Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
				boolean isInt = pattern.matcher(coreClk).matches();
				if (!isInt) {
					fDialogFields[1].getTextControl(content).setText("");
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					MessageDialog.openError(window.getShell(), "��ʾ", "������������");
				}
			}

			@Override
			public void mouseEnter(MouseEvent e) {
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
		baordDescLabel.setText("�������:");
		boardDetailsDesc = new Text(baordDescCpt, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		boardDetailsDesc.setLayoutData(new GridData(GridData.FILL_BOTH));

		boardDetailsDesc.setText("Cpu: \n" + "\t�ܹ�: \n" + "\t����: \n" + "\t����: \n" + "\t��λ��ַ: \n" + "\t�ڴ�: \n"
				+ "������Ƶ��: \n" + "Rtc��Ƶ��: \n" + "�����豸: \n" + "Cpu����: ");

		boardDetailsDesc.setEditable(false);
		super.createDialogArea(parent);
		return super.createDialogArea(parent);
	}

	private String[] BoardDetailsTextLabels = { "CPU", "�ں�ʱ��" };
}
