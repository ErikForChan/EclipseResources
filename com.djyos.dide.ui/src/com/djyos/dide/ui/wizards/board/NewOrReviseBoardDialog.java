package com.djyos.dide.ui.wizards.board;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.djyos.dide.ui.DPluginImages;
import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.objects.Board;
import com.djyos.dide.ui.objects.Chip;
import com.djyos.dide.ui.objects.Component;
import com.djyos.dide.ui.objects.Core;
import com.djyos.dide.ui.objects.Cpu;
import com.djyos.dide.ui.objects.OnBoardCpu;
import com.djyos.dide.ui.objects.OnBoardMemory;
import com.djyos.dide.ui.swt.DjyosUI;
import com.djyos.dide.ui.wizards.component.ReadComponent;
import com.djyos.dide.ui.wizards.cpu.ReadCpuXml;

public class NewOrReviseBoardDialog extends StatusDialog {

	private Tree cpuArhives, cpuArhivesNeed, chipTree, chipOnTree, cpudrvTree, cpudrvOnTree;
	private Button gotoBtn = null, backBtn = null;
	private List<OnBoardCpu> onBoardCpus = new ArrayList<OnBoardCpu>();
	private Cpu newCpu;
	private Component newComponent;
	private Chip newChip;
	private Text boardNameField, mainClkField, rtcClkField;
	private TabFolder folder;
	private Label mainClkLabel;
	private Button rtcClkBtn;
	private Board boardInit;
	private List<Cpu> cpusList = null, cpusOn = null;
	private List<Component> peripheralsList = new ArrayList<Component>();;// �����б�
	private List<Component> peripheralsOn = new ArrayList<Component>();// �õ�������
	private List<Component> allPeripherals;// ��������
	private List<Chip> chipsList = null, chipsOn = null;
	private List<OnBoardMemory> memorys = new ArrayList<OnBoardMemory>();
	private List<OnBoardMemory> shared_memorys = new ArrayList<OnBoardMemory>();
	private List<Component> thePeripherals;// �������cpuʱ�õ�����ʱ��������
	private Composite boardAttributesCpt;
	private Group ConfigurationGroup;
	private String didePath = DideHelper.getDIDEPath();
	public String lastBoardName;
	private List<File> similarfiles = new ArrayList<File>();
	private String similarBoardPath;
	
	private Tree memoryTree;
	private Combo memoryTypeCombo;
	private Text addrField,sizeField;
	Button addBtn,deleteBtn;
	
	private Tree shared_memoryTree;
	private Combo shared_memoryTypeCombo;
	private Text shared_addrField,shared_sizeField;
	Button shared_addBtn,shared_deleteBtn;

	private List<Board> getBoards() {
		List<Board> boards = ReadBoardXml.getAllBoards();
		return boards;
	}

	public Board getBoard() {
		Board newBoard = new Board();
		String boardName = lastBoardName;
		newBoard.setBoardName(boardName);
		if (boardInit == null) {
			newBoard.setBoardFolderPath((DideHelper.getUserBoardFilePath() + "/" + lastBoardName).replace("\\", "/"));
		}
		newBoard.setOnBoardCpus(onBoardCpus);
		newBoard.setShare_memorys(shared_memorys);
		return newBoard;
	}
	
	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		lastBoardName = boardNameField.getText().trim();
		String vaildMsg = vaildPage();
		if (vaildMsg != null) {
			DideHelper.showErrorMessage(vaildMsg);
		} else {
			Board board = getBoard();
			String dirPath = DideHelper.getUserBoardFilePath() + "/" + lastBoardName;
			String xmlPath = dirPath + "/Board_" + lastBoardName + ".xml";
			if (boardInit != null) {
				File boardFile = new File(boardInit.getBoardFolderPath());
				File[] files = boardFile.listFiles();
				for (File f : files) {
					if (f.getName().startsWith("Board") && f.getName().endsWith(".xml")) {
						xmlPath = f.getPath();
						f.delete();
					}
				}
			}
			File file = new File(xmlPath);
			try {
				IRunnableWithProgress runnable = new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						if (boardInit != null) {
							monitor.beginTask("�޸İ��������Ϣ����", 100);
						} else {
							monitor.beginTask("�����������", 100);
							File boardDir = new File(dirPath);
							if (!boardDir.exists()) {
								boardDir.mkdirs();
							}
						}

						CreatBoardXml ctbx = new CreatBoardXml();
						DideHelper.createNewFile(file);
						ctbx.creatBoardXml(board, file);

						for (File f : similarfiles) {
							// �������Ƶ��ļ�
							File newFile = new File(dirPath + f.getPath().replace(similarBoardPath, ""));
							if (f.isDirectory()) {
								if (!newFile.exists()) {
									newFile.mkdir();
								}
							} else {
								if (!newFile.exists()) {
									try {
										newFile.createNewFile();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									String content = DideHelper.readFile(f);
									DideHelper.writeFile(newFile, content);
								}
							}
						}
						monitor.worked(10);
						monitor.done();
					}
				};
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(
						PlatformUI.getWorkbench().getDisplay().getActiveShell());
				dialog.setCancelable(false);
				dialog.run(true, true, runnable);
			} catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
			}
			super.okPressed();
		}
	}

	public NewOrReviseBoardDialog(Shell parent, boolean toNew, Board board) {
		super(parent);
		// TODO Auto-generated constructor stub
		if (toNew) {
			setTitle("�½����");
		} else {
			setTitle("ά�����");
			boardInit = board;
			shared_memorys = boardInit.getShare_memorys();
		}
		setShellStyle(getShellStyle() | SWT.CLOSE | SWT.RESIZE);
	}

	@Override
	protected Point getInitialSize() {
		// TODO Auto-generated method stub
		return new Point(600, 820);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		createDynamicGroup(composite);
		return super.createDialogArea(parent);
	}

	public String vaildPage() {
		String curBoardName = boardNameField.getText().trim();
		if (curBoardName.trim().equals("")) {
			return "����д�������";
		}
		if (boardInit == null) {
			List<Board> boards = getBoards();
			for (Board board : boards) {
				if (board.getBoardName().equals(curBoardName)) {
					return "�ð���Ѵ��ڣ������������ƣ�";
				}
			}
		}
		for (OnBoardCpu onBoardCpu : onBoardCpus) {
			String cpuName = onBoardCpu.getCpuName();
			List<OnBoardMemory> memorys = onBoardCpu.getMemorys();
			for (int i = 0; i < memorys.size(); i++) {
				OnBoardMemory memory = memorys.get(i);
				if (memory.getStartAddr() == null || memory.getSize() == null || memory.getType() == null) {
					return "��������д[" + cpuName + "]�Ĵ洢��Ϣ";
				} else {
					if (memory.getSize().equals("0") || memory.getSize().equals("0x")) {
						return onBoardCpu.getCpuName() + "�Ĵ洢memory" + (i + 1) + "��С�����0";
					}
				}
			}
			if (onBoardCpu.getMianClk() == 0) {
				return "��������д[" + cpuName + "]�ľ���Ƶ��";
			}
			if (onBoardCpu.getRtcClk() == -1) {
				return "��������д[" + cpuName + "]��RtcƵ��";
			}
		}
		return null;
	}

	public void changeCpusOn(String cpuName, boolean toAdd) {
		if (toAdd) {
			for (int i = 0; i < cpusList.size(); i++) {
				String curName = cpusList.get(i).getCpuName();
				if (curName.equals(cpuName)) {
					Cpu curCpu = cpusList.get(i);
					newCpu = new Cpu(curCpu.getCpuName(), curCpu.getCores());
					int count = 0;
					if (cpusOn.size() != 0) {
						for (int j = 0; j < cpusOn.size(); j++) {
							if (cpusOn.get(j).getCpuName().contains(newCpu.getCpuName())) {
								count++;
							}
						}
					}

					newCpu.setCpuName(count == 0 ? newCpu.getCpuName() : newCpu.getCpuName() + "(" + count + ")");
					cpusOn.add(newCpu);
					break;
				}
			}
		} else {
			for (int i = 0; i < cpusOn.size(); i++) {
				if (cpusOn.get(i).getCpuName().equals(cpuName)) {
					newCpu = cpusOn.get(i);
					cpusOn.remove(i);
					break;
				}
			}
		}

	}

	public void changePeripheralsOn(String cpudrvName, boolean toAdd) {
		if (toAdd) {
			for (int i = 0; i < peripheralsList.size(); i++) {
				String curName = peripheralsList.get(i).getName();
				System.out.println("curName: " + curName);
				if (curName.equals(cpudrvName)) {
					newComponent = new Component();
					newComponent.setName(curName);
					System.out.println("true");
					peripheralsList.remove(i);
					peripheralsOn.add(newComponent);

					break;
				}
			}
		} else {
			for (int i = 0; i < peripheralsOn.size(); i++) {
				if (peripheralsOn.get(i).getName().equals(cpudrvName)) {
					newComponent = peripheralsOn.get(i);
					peripheralsOn.remove(i);
					peripheralsList.add(newComponent);
					break;
				}
			}
		}

	}

	public void changeChipsOn(String chipName, boolean toAdd) {
		if (toAdd) {
			for (int i = 0; i < chipsList.size(); i++) {
				String curName = chipsList.get(i).getChipName();
				if (curName.equals(chipName)) {
					newChip = new Chip();
					newChip.setChipName(curName);
					String curChip = chipsList.get(i).getChipName();
					int count = 0;
					if (chipsOn.size() != 0) {
						for (int j = 0; j < chipsOn.size(); j++) {
							if (chipsOn.get(j).getChipName().contains(curChip)) {
								count++;
							}
						}
					}
					if (count != 0) {
						newChip.setChipName(newChip.getChipName() + "(" + count + ")");
					}
					chipsOn.add(newChip);
					break;
				}
			}
		} else {
			for (int i = 0; i < chipsOn.size(); i++) {
				if (chipsOn.get(i).getChipName().equals(chipName)) {
					newChip = chipsOn.get(i);
					chipsOn.remove(i);
					break;
				}
			}
		}

	}

	private Control createMemoryContent(TabFolder folder, boolean isPrivate) {
		// TODO Auto-generated method stub
		Composite contentCpt = new Composite(folder, SWT.NONE);
		GridLayout contentLayout = DjyosUI.DjyosGridLayout(2, 0, 0);
		contentCpt.setLayout(contentLayout);

		Composite treeCpt = new Composite(contentCpt, SWT.NONE);
		treeCpt.setLayout(new GridLayout());
		treeCpt.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		memoryTree = DideHelper.buildTree(treeCpt, 180, 220, "˽�а���Memory",(SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL));
		Composite btnCpt = new Composite(treeCpt, SWT.NONE);
		btnCpt.setLayout(new GridLayout(2, true));
		btnCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_CENTER));
		addBtn = new Button(btnCpt, SWT.PUSH);
		addBtn.setText("���");
		deleteBtn = new Button(btnCpt, SWT.PUSH);
		deleteBtn.setText("ɾ��");

		addBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int memoryCount = memoryTree.getItemCount();
				TreeItem t = new TreeItem(memoryTree, SWT.NONE);
				TreeItem[] items = memoryTree.getItems();
				int max = 0;
				if (memoryCount > 0) {
					String maxString = items[memoryCount - 1].getText();
					max = Integer.parseInt(maxString.substring(6, maxString.length()));
				}
				t.setText("memory" + (max + 1));
				OnBoardMemory memory = new OnBoardMemory();
				memory.setName("memory" + (max + 1));

				TreeItem[] cpuItems = cpuArhivesNeed.getSelection();
				if (cpuItems.length > 0) {
					String selectCpuName = cpuItems[0].getText();
					if(isPrivate) {
						memorys.add(memory);
						for (int i = 0; i < onBoardCpus.size(); i++) {
							if (onBoardCpus.get(i).getCpuName().equals(selectCpuName)) {
								onBoardCpus.get(i).setMemorys(memorys);
							}
						}
					}else {
						shared_memorys.add(memory);
					}
				}
			}
		});

		deleteBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = memoryTree.getSelection();
				if (items.length > 0) {
					if(isPrivate) {
						for (int i = 0; i < memorys.size(); i++) {
							if (memorys.get(i).getName().equals(items[0].getText().trim())) {
								memorys.remove(i);
							}
						}
						TreeItem[] cpuItems = cpuArhivesNeed.getSelection();
						if (cpuItems.length > 0) {
							String selectCpuName = cpuItems[0].getText();
							for (int i = 0; i < onBoardCpus.size(); i++) {
								if (onBoardCpus.get(i).getCpuName().equals(selectCpuName)) {
									onBoardCpus.get(i).setMemorys(memorys);
								}
							}
						}
					}else {
						for (int i = 0; i < shared_memorys.size(); i++) {
							if (shared_memorys.get(i).getName().equals(items[0].getText().trim())) {
								shared_memorys.remove(i);
							}
						}
					}
					
					items[0].dispose();
				}
				if (memoryTree.getItemCount() < 1) {
					enableMemory(false);
				}
			}
		});

		Composite detailsCpt = new Composite(contentCpt, SWT.BORDER);
		GridLayout detailsLayout = DjyosUI.DjyosGridLayout(2, 0, 5);
		detailsCpt.setLayout(detailsLayout);
		detailsCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		Label typeLabel = new Label(detailsCpt, SWT.NONE);
		typeLabel.setText("����: ");

		memoryTypeCombo = new Combo(detailsCpt, SWT.READ_ONLY);
		memoryTypeCombo.add("ROM");
		memoryTypeCombo.add("FLASH");
		memoryTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		memoryTypeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int comboIndex = memoryTypeCombo.getSelectionIndex();
				TreeItem[] items = memoryTree.getSelection();
				if (items.length > 0) {
					String selectMemory = items[0].getText().trim();
					List<OnBoardMemory> tempMemorys = null;
					if(isPrivate) {
						tempMemorys = memorys;
					}else {
						tempMemorys = shared_memorys;
					}
					for (int i = 0; i < tempMemorys.size(); i++) {
						OnBoardMemory memory = tempMemorys.get(i);
						if (memory.getName().equals(selectMemory)) {
							String type = memoryTypeCombo.getItem(comboIndex);
							memory.setType(type);
							break;
						}
					}
				}
			}
		});

		Label addrLabel = new Label(detailsCpt, SWT.NONE);
		addrLabel.setText("��ַ: ");
		addrField = new Text(detailsCpt, SWT.BORDER);
		addrField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		addrField.addMouseTrackListener(new MouseTrackListener() {

			@Override
			public void mouseHover(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExit(MouseEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = memoryTree.getSelection();
				String addr = addrField.getText().trim();
				if (items.length > 0) {
					if (!addr.equals("")) {
						String selectMemory = items[0].getText().trim();
						int curNum = -1;
						Pattern pattern = Pattern.compile("^([1-9]\\d*|[0]{1,1})$"); // ��0������
						boolean isInt = pattern.matcher(addr).matches();
						if (addr.startsWith("0x")) {
							curNum = Integer.parseInt(addr.substring(2), 16);
						} else {
							if (isInt) {
								curNum = Integer.parseInt(addr);
							}
						}
						if (curNum < 0) {
							addrField.setText("");
							DideHelper.showErrorMessage("������������(����0)");
						} else {
							List<OnBoardMemory> tempMemorys = null;
							if(isPrivate) {
								tempMemorys = memorys;
							}else {
								tempMemorys = shared_memorys;
							}
							for (int i = 0; i < tempMemorys.size(); i++) {
								OnBoardMemory memory = tempMemorys.get(i);
								if (memory.getName().equals(selectMemory)) {
									memory.setStartAddr(addr);
									break;
								}
							}
						}
					} else {
						// setPageComplete(false);
					}

				}
			}

			@Override
			public void mouseEnter(MouseEvent e) {
				// TODO Auto-generated method stub

			}
		});

		Label sizeLabel = new Label(detailsCpt, SWT.NONE);
		sizeLabel.setText("��С: ");
		sizeField = new Text(detailsCpt, SWT.BORDER);
		sizeField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		sizeField.addMouseTrackListener(new MouseTrackListener() {

			@Override
			public void mouseHover(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExit(MouseEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = memoryTree.getSelection();
				String size = sizeField.getText().trim().replace("k", "");
				if (items.length > 0) {
					String selectMemory = items[0].getText().trim();
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if (!size.equals("")) {
						int curNum = -1;
						Pattern pattern = Pattern.compile("^[+]{0,1}(\\d+)$"); // ^[-\\+]?[\\d]*$
						boolean isInt = pattern.matcher(size).matches();
						if (size.startsWith("0x") && !size.trim().equals("0x")) {
							curNum = Integer.parseInt(size.substring(2), 16);
						} else {
							if (isInt) {
								curNum = Integer.parseInt(size);
							}
						}
						if (curNum < 0) {
							sizeField.setText("");
							MessageDialog.openInformation(window.getShell(), "��ʾ", "������������(������0)");
						} else {
							List<OnBoardMemory> tempMemorys = null;
							if(isPrivate) {
								tempMemorys = memorys;
							}else {
								tempMemorys = shared_memorys;
							}
							for (int i = 0; i < tempMemorys.size(); i++) {
								OnBoardMemory memory = tempMemorys.get(i);
								if (memory.getName().equals(selectMemory)) {
									memory.setSize(size);
									break;
								}
							}
						}
					} else {
						// setPageComplete(false);
					}

				}
			}

			@Override
			public void mouseEnter(MouseEvent e) {
				// TODO Auto-generated method stub

			}
		});
		
		memoryTree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				enableMemory(true);
				TreeItem[] items = memoryTree.getSelection();
				if (items.length > 0) {
					String selectMemory = items[0].getText().trim();
					OnBoardMemory selectedMemory = null;
					if(isPrivate) {
						for (int i = 0; i < memorys.size(); i++) {
							if (memorys.get(i).getName().equals(selectMemory)) {
								selectedMemory = memorys.get(i);
								break;
							}
						}
					}else {
						for (int i = 0; i < shared_memorys.size(); i++) {
							if (shared_memorys.get(i).getName().equals(selectMemory)) {
								selectedMemory = shared_memorys.get(i);
								break;
							}
						}
					}
					
					String[] typeItems = memoryTypeCombo.getItems();
					if (selectedMemory.getType() != null) {
						for (int i = 0; i < typeItems.length; i++) {
							if (typeItems[i].equals(selectedMemory.getType())) {
								memoryTypeCombo.select(i);
								break;
							}
						}
					}

					if (selectedMemory.getStartAddr() != null) {
						addrField.setText(selectedMemory.getStartAddr());
					} else {
						addrField.setText("");
					}
					if (selectedMemory.getSize() != null) {
						sizeField.setText(selectedMemory.getSize());
					} else {
						sizeField.setText("");
					}
					
				}
			}
		});
		
		enableMemory(false);
		return contentCpt;
	}
	
	private Control createSharedMemoryContent(TabFolder folder) {
		// TODO Auto-generated method stub
		Composite contentCpt = new Composite(folder, SWT.NONE);
		GridLayout contentLayout = DjyosUI.DjyosGridLayout(2, 0, 0);
		contentCpt.setLayout(contentLayout);

		Composite treeCpt = new Composite(contentCpt, SWT.NONE);
		treeCpt.setLayout(new GridLayout());
		treeCpt.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		shared_memoryTree = DideHelper.buildTree(treeCpt, 180, 220, "�������Memory",(SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL));
		Composite btnCpt = new Composite(treeCpt, SWT.NONE);
		btnCpt.setLayout(new GridLayout(2, true));
		btnCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_CENTER));
		
		shared_addBtn = new Button(btnCpt, SWT.PUSH);
		shared_addBtn.setText("���");
		shared_deleteBtn = new Button(btnCpt, SWT.PUSH);
		shared_deleteBtn.setText("ɾ��");

		shared_addBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int memoryCount = shared_memoryTree.getItemCount();
				TreeItem t = new TreeItem(shared_memoryTree, SWT.NONE);
				TreeItem[] items =shared_memoryTree.getItems();
				int max = 0;
				if (memoryCount > 0) {
					String maxString = items[memoryCount - 1].getText();
					max = Integer.parseInt(maxString.substring(6, maxString.length()));
				}
				t.setText("memory" + (max + 1));
				OnBoardMemory memory = new OnBoardMemory();
				memory.setName("memory" + (max + 1));
				shared_memorys.add(memory);
			}
		});

		shared_deleteBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = memoryTree.getSelection();
				if (items.length > 0) {
					for (int i = 0; i < shared_memorys.size(); i++) {
						if (shared_memorys.get(i).getName().equals(items[0].getText().trim())) {
							shared_memorys.remove(i);
						}
					}
					items[0].dispose();
				}
				if (shared_memoryTree.getItemCount() < 1) {
					enableSharedMemory(false);
				}
			}
		});

		Composite detailsCpt = new Composite(contentCpt, SWT.BORDER);
		GridLayout detailsLayout = DjyosUI.DjyosGridLayout(2, 0, 5);
		detailsCpt.setLayout(detailsLayout);
		detailsCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		Label typeLabel = new Label(detailsCpt, SWT.NONE);
		typeLabel.setText("����: ");

		shared_memoryTypeCombo = new Combo(detailsCpt, SWT.READ_ONLY);
		shared_memoryTypeCombo.add("ROM");
		shared_memoryTypeCombo.add("FLASH");
		shared_memoryTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		shared_memoryTypeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int comboIndex = shared_memoryTypeCombo.getSelectionIndex();
				TreeItem[] items = shared_memoryTree.getSelection();
				if (items.length > 0) {
					String selectMemory = items[0].getText().trim();
					for (int i = 0; i < shared_memorys.size(); i++) {
						OnBoardMemory memory = shared_memorys.get(i);
						if (memory.getName().equals(selectMemory)) {
							String type = shared_memoryTypeCombo.getItem(comboIndex);
							memory.setType(type);
							break;
						}
					}
				}
			}
		});

		Label addrLabel = new Label(detailsCpt, SWT.NONE);
		addrLabel.setText("��ַ: ");
		shared_addrField = new Text(detailsCpt, SWT.BORDER);
		shared_addrField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		shared_addrField.addMouseTrackListener(new MouseTrackListener() {

			@Override
			public void mouseHover(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExit(MouseEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = shared_memoryTree.getSelection();
				String addr = shared_addrField.getText().trim();
				if (items.length > 0) {
					if (!addr.equals("")) {
						String selectMemory = items[0].getText().trim();
						int curNum = -1;
						Pattern pattern = Pattern.compile("^([1-9]\\d*|[0]{1,1})$"); // ��0������
						boolean isInt = pattern.matcher(addr).matches();
						if (addr.startsWith("0x")) {
							curNum = Integer.parseInt(addr.substring(2), 16);
						} else {
							if (isInt) {
								curNum = Integer.parseInt(addr);
							}
						}
						if (curNum < 0) {
							shared_addrField.setText("");
							DideHelper.showErrorMessage("������������(����0)");
						} else {
							for (int i = 0; i < shared_memorys.size(); i++) {
								OnBoardMemory memory = shared_memorys.get(i);
								if (memory.getName().equals(selectMemory)) {
									memory.setStartAddr(addr);
									break;
								}
							}
						}
					}
				}
			}

			@Override
			public void mouseEnter(MouseEvent e) {
				// TODO Auto-generated method stub

			}
		});

		Label sizeLabel = new Label(detailsCpt, SWT.NONE);
		sizeLabel.setText("��С: ");
		shared_sizeField = new Text(detailsCpt, SWT.BORDER);
		shared_sizeField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// ControlFactory.createLabel(detailsCpt, "K");
		shared_sizeField.addMouseTrackListener(new MouseTrackListener() {

			@Override
			public void mouseHover(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExit(MouseEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items =shared_memoryTree.getSelection();
				String size = shared_sizeField.getText().trim().replace("k", "");
				if (items.length > 0) {
					String selectMemory = items[0].getText().trim();
					if (!size.equals("")) {
						int curNum = -1;
						Pattern pattern = Pattern.compile("^[+]{0,1}(\\d+)$"); // ^[-\\+]?[\\d]*$
						boolean isInt = pattern.matcher(size).matches();
						if (size.startsWith("0x") && !size.trim().equals("0x")) {
							curNum = Integer.parseInt(size.substring(2), 16);
						} else {
							if (isInt) {
								curNum = Integer.parseInt(size);
							}
						}
						if (curNum < 0) {
							shared_sizeField.setText("");
							DideHelper.showErrorMessage("������������(������0)");
						} else {
							for (int i = 0; i < shared_memorys.size(); i++) {
								OnBoardMemory memory = shared_memorys.get(i);
								if (memory.getName().equals(selectMemory)) {
									memory.setSize(size);
									break;
								}
							}
						}
					} else {
						// setPageComplete(false);
					}

				}
			}

			@Override
			public void mouseEnter(MouseEvent e) {
				// TODO Auto-generated method stub

			}
		});
		
		shared_memoryTree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				enableSharedMemory(true);
				TreeItem[] items = shared_memoryTree.getSelection();
				if (items.length > 0) {
					int index = shared_memoryTree.indexOf(items[0]);
//					String selectMemory = items[0].getText().trim();
					OnBoardMemory selectedMemory = shared_memorys.get(index);
//					for (int i = 0; i < shared_memorys.size(); i++) {
//						if (shared_memorys.get(i).getName().equals(selectMemory)) {
//							selectedMemory = shared_memorys.get(i);
//							break;
//						}
//					}
					
					String[] typeItems = shared_memoryTypeCombo.getItems();
					if (selectedMemory.getType() != null) {
						for (int i = 0; i < typeItems.length; i++) {
							if (typeItems[i].equals(selectedMemory.getType())) {
								shared_memoryTypeCombo.select(i);
								break;
							}
						}
					}

					if (selectedMemory.getStartAddr() != null) {
						shared_addrField.setText(selectedMemory.getStartAddr());
					} else {
						shared_addrField.setText("");
					}
					if (selectedMemory.getSize() != null) {
						shared_sizeField.setText(selectedMemory.getSize());
					} else {
						shared_sizeField.setText("");
					}
					
				}
			}
		});
		
		if(boardInit != null) {
//			shared_memoryTree.removeAll();
//			shared_memoryTypeCombo.deselectAll();
//			shared_addrField.setText("");
//			shared_sizeField.setText("");
			List<OnBoardMemory> shared_memorys = boardInit.getShare_memorys();
			for (int i = 0; i < shared_memorys.size(); i++) {
				shared_memorys.get(i).setName("memory" + (i + 1));
				String memoryOnName = shared_memorys.get(i).getName();
				TreeItem t = new TreeItem(shared_memoryTree, SWT.NONE);
				t.setText(memoryOnName);
			}
			shared_memoryTypeCombo.select(shared_memorys.get(0).getType().equals("FLASH")?1:0);
			shared_addrField.setText(shared_memorys.get(0).getStartAddr());
			shared_sizeField.setText(shared_memorys.get(0).getSize());
			shared_memoryTree.setSelection(shared_memoryTree.getItems()[0]);
		}
		
		enableSharedMemory(false);
		return contentCpt;
	}

	private Control createChipContent(TabFolder folder) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(folder, SWT.NULL);
		GridLayout layout = DjyosUI.DjyosGridLayout(3, 0, 5);
		composite.setLayout(layout);

		Composite compositeTree = new Composite(composite, SWT.NULL);
		chipTree = DideHelper.buildTree(compositeTree, 200, 220, "оƬ�б�", (SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL));

		Composite btnCpt = new Composite(composite, SWT.NULL);
		btnCpt.setLayout(new GridLayout());
		btnCpt.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.HORIZONTAL_ALIGN_CENTER));
		Button goto2Btn, back2Btn;
		goto2Btn = new Button(btnCpt, SWT.PUSH);
		goto2Btn.setText("   ����  ");
		back2Btn = new Button(btnCpt, SWT.PUSH);
		back2Btn.setText(" ����    ");
		goto2Btn.setEnabled(false);
		back2Btn.setEnabled(false);

		goto2Btn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = chipTree.getSelection();
				if (items.length > 0) {
					String selectChipName = items[0].getText();
					changeChipsOn(selectChipName, true);
					updateChipOn();
					TreeItem[] cpuItems = cpuArhivesNeed.getSelection();
					if (cpuItems.length > 0) {
						String selectCpuName = cpuItems[0].getText();
						for (int i = 0; i < onBoardCpus.size(); i++) {
							if (onBoardCpus.get(i).getCpuName().equals(selectCpuName)) {
								onBoardCpus.get(i).setChips(chipsOn);
								break;
							}
						}
					}

				}
			}
		});

		back2Btn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = chipOnTree.getSelection();
				if (items.length > 0) {
					String selectChipName = items[0].getText();
					changeChipsOn(selectChipName, false);

					TreeItem[] cpuItems = cpuArhivesNeed.getSelection();
					if (cpuItems.length > 0) {
						String selectCpuName = cpuItems[0].getText();
						for (int i = 0; i < onBoardCpus.size(); i++) {
							if (onBoardCpus.get(i).getCpuName().equals(selectCpuName)) {
								onBoardCpus.get(i).setChips(chipsOn);
								break;
							}
						}
					}

					items[0].dispose();
				}
			}
		});
		chipTree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				goto2Btn.setEnabled(true);
				back2Btn.setEnabled(false);
			}
		});
		Composite chipTreeOn = new Composite(composite, SWT.NULL);
		chipOnTree = DideHelper.buildTree(chipTreeOn, 200, 220, "����оƬ", (SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL));
		chipOnTree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				goto2Btn.setEnabled(false);
				back2Btn.setEnabled(true);
				TreeItem[] items = chipOnTree.getSelection();
				if (items.length > 0) {
					String selectChip = items[0].getText().trim();
					for (int i = 0; i < chipsOn.size(); i++) {
						if (chipsOn.get(i).getChipName().equals(selectChip)) {
							break;
						}
					}
				}
			}
		});

		File chipFile = new File(didePath + "djysrc/bsp/chipdrv");
		File[] files = chipFile.listFiles();
		chipsList = new ArrayList<Chip>();
		for (int i = 0; i < files.length; i++) {
			TreeItem t = new TreeItem(chipTree, SWT.NONE);
			t.setText(files[i].getName());
			Chip chip = new Chip();
			chip.setChipName(files[i].getName());
			chipsList.add(chip);
		}

		return composite;
	}

	private Control createPeripheralsContent(TabFolder folder) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(folder, SWT.NULL);
		GridLayout layout = DjyosUI.DjyosGridLayout(3, 0, 5);
		composite.setLayout(layout);

		Composite compositeTree = new Composite(composite, SWT.NULL);
		cpudrvTree = DideHelper.buildTree(compositeTree, 200, 220, "�����б�", (SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL));

		Composite btnCpt = new Composite(composite, SWT.NULL);
		btnCpt.setLayout(new GridLayout());
		btnCpt.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.HORIZONTAL_ALIGN_CENTER));
		Button goto1Btn, back1Btn;
		goto1Btn = new Button(btnCpt, SWT.PUSH);
		goto1Btn.setText("   ����  ");
		back1Btn = new Button(btnCpt, SWT.PUSH);
		back1Btn.setText(" ����    ");
		goto1Btn.setEnabled(false);
		back1Btn.setEnabled(false);

		goto1Btn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = cpudrvTree.getSelection();
				if (items.length > 0) {
					String selectCpudrvName = items[0].getText();
					changePeripheralsOn(selectCpudrvName, true);
					updatePeripheralsOn();

					TreeItem[] cpuItems = cpuArhivesNeed.getSelection();
					if (cpuItems.length > 0) {
						String selectCpuName = cpuItems[0].getText();
						for (int i = 0; i < onBoardCpus.size(); i++) {
							if (onBoardCpus.get(i).getCpuName().equals(selectCpuName)) {
								onBoardCpus.get(i).setPeripherals(peripheralsOn);
							}
						}
					}
					items[0].dispose();
				}
			}
		});

		back1Btn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				goto1Btn.setEnabled(false);
				back1Btn.setEnabled(true);
				TreeItem[] items = cpudrvOnTree.getSelection();
				if (items.length > 0) {
					String selectCpudrvName = items[0].getText();
					changePeripheralsOn(selectCpudrvName, false);
					updatePeripheralsList();
					TreeItem[] cpuItems = cpuArhivesNeed.getSelection();
					if (cpuItems.length > 0) {
						String selectCpuName = cpuItems[0].getText();
						for (int i = 0; i < onBoardCpus.size(); i++) {
							if (onBoardCpus.get(i).getCpuName().equals(selectCpuName)) {
								onBoardCpus.get(i).setPeripherals(peripheralsOn);
							}
						}
					}
					items[0].dispose();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				goto1Btn.setEnabled(true);
				back1Btn.setEnabled(false);
			}
		});

		cpudrvTree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				goto1Btn.setEnabled(true);
				back1Btn.setEnabled(false);
			}
		});

		Composite compositeTreeOn = new Composite(composite, SWT.NULL);
		int style = (SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		cpudrvOnTree = DideHelper.buildTree(compositeTreeOn,200, 220, "�õ�������",style);
		cpudrvOnTree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				goto1Btn.setEnabled(false);
				back1Btn.setEnabled(true);
			}
		});
		peripheralsOn = new ArrayList<Component>();

		return composite;
	}

	private Control createClkContent(TabFolder folder) {
		// TODO Auto-generated method stub
		Composite contentCpt = new Composite(folder, SWT.BORDER_DOT);
		GridLayout layoutContent = DjyosUI.DjyosGridLayout(3, 0, 5);
		contentCpt.setLayout(layoutContent);
		contentCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | SWT.BORDER));

		mainClkLabel = new Label(contentCpt, SWT.NONE);
		mainClkLabel.setText("����Ƶ��: ");
		mainClkField = new Text(contentCpt, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		mainClkField.setLayoutData(data);
		ControlFactory.createLabel(contentCpt, "Mhz");
		mainClkField.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				// TODO Auto-generated method stub
				Pattern pattern = Pattern.compile("^[+]{0,1}(\\d+)$|^[+]{0,1}(\\d+\\.\\d+)$");
				String mainClkString = mainClkField.getText().trim();
				if (mainClkString.trim().length() > 0) {
					boolean isMainClkInt = pattern.matcher(mainClkString).matches();
					if (isMainClkInt) {
						float mianClk = Float.parseFloat(mainClkString);
						TreeItem[] items = cpuArhivesNeed.getSelection();
						if (items.length > 0) {
							String selectCpuName = items[0].getText();
							for (int i = 0; i < onBoardCpus.size(); i++) {
								if (onBoardCpus.get(i).getCpuName().equals(selectCpuName)) {
									onBoardCpus.get(i).setMianClk(mianClk);
								}
							}
						}
					} else {
						DideHelper.showErrorMessage("����������");
					}

				}
			}
		});

		rtcClkBtn = new Button(contentCpt, SWT.CHECK);
		rtcClkBtn.setText("rtcʱ��: ");
		rtcClkField = new Text(contentCpt, SWT.BORDER);
		rtcClkField.setEnabled(false);
		rtcClkField.setLayoutData(data);
		rtcClkBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				boolean isSelected = rtcClkBtn.getSelection();
				rtcClkField.setEnabled(isSelected);
				TreeItem[] items = cpuArhivesNeed.getSelection();
				if (items.length > 0) {
					String selectCpuName = items[0].getText();
					for (int i = 0; i < onBoardCpus.size(); i++) {
						if (onBoardCpus.get(i).getCpuName().equals(selectCpuName)) {
							onBoardCpus.get(i).setRtcClk(isSelected?(-1):0);
						}
					}
				}
			}
		});
		ControlFactory.createLabel(contentCpt, "hz");
		rtcClkField.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				// TODO Auto-generated method stub
				Pattern pattern = Pattern.compile("^[+]{0,1}(\\d+)$|^[+]{0,1}(\\d+\\.\\d+)$");
				String rtcClkString = rtcClkField.getText().trim();
				if (!rtcClkString.equals("")) {
					boolean isRtcClkInt = pattern.matcher(rtcClkString).matches();
					if (isRtcClkInt) {
						TreeItem[] items = cpuArhivesNeed.getSelection();
						if (items.length > 0) {
							String selectCpuName = items[0].getText();
							for (int i = 0; i < onBoardCpus.size(); i++) {
								if (onBoardCpus.get(i).getCpuName().equals(selectCpuName)) {
									onBoardCpus.get(i).setRtcClk(Float.parseFloat(rtcClkString));
								}
							}
						}
					} else {
						DideHelper.showErrorMessage("����������");
					}
				}
			}
		});

		return contentCpt;
	}

	private void createDynamicGroup(Composite composite) {
		// TODO Auto-generated method stub
		ScrolledComposite scrolledComposite = new ScrolledComposite(composite, SWT.V_SCROLL | SWT.H_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite infoArea = new Composite(scrolledComposite, SWT.NONE);
		GridLayout infoLayout = DjyosUI.DjyosGridLayout(1, 0, 3);
		infoArea.setLayout(infoLayout);
		GridData data = new GridData(GridData.FILL_BOTH);
		infoArea.setLayoutData(data);

		cpusList = ReadCpuXml.getAllCpus();
		Composite boardNameCpt = new Composite(infoArea, SWT.NULL);
		GridLayout layoutBoardName = new GridLayout();
		layoutBoardName.numColumns = 3;
		boardNameCpt.setLayout(layoutBoardName);
		boardNameCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Text nameLabel = new Text(boardNameCpt, SWT.NONE);
		nameLabel.setText("�������: ");
		nameLabel.setEditable(false);
		boardNameField = new Text(boardNameCpt, SWT.BORDER);
		GridData boardNameData = new GridData(GridData.FILL_HORIZONTAL);
		boardNameField.setLayoutData(boardNameData);
		boardNameField.setMessage("����������");
		if (boardInit != null) {
			boardNameField.setText(boardInit.getBoardName());
		}

		// ѡ�����ư��
		Button selectSimilarBoardBtn = new Button(boardNameCpt, SWT.PUSH);
		selectSimilarBoardBtn.setText("ѡ�����ư��");
		selectSimilarBoardBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				Handle_Select_SimilarBoard();
			}
		});

		Composite newSearchCpt = new Composite(infoArea, SWT.NULL);
		GridLayout layoutnewSearchCpt = new GridLayout();
		layoutnewSearchCpt.numColumns = 2;
		newSearchCpt.setLayout(layoutnewSearchCpt);
		newSearchCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Button newCpuBtn = new Button(newSearchCpt, SWT.PUSH);
		newCpuBtn.setText("�½�Cpu");
		newCpuBtn.setImage(DPluginImages.OBJ_CPU_VIEW.createImage());
//		newCpuBtn.setBackground(new Color(newSearchCpt.getDisplay(), 235, 84, 2));
		newCpuBtn.setForeground(newSearchCpt.getDisplay().getSystemColor(SWT.COLOR_RED));
		Font font = new Font(newSearchCpt.getDisplay(), "���ķ���", 10, SWT.BOLD);
//		FontData newFontData = font.getFontData()[0];
		newCpuBtn.setFont(font);
		newCpuBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		newCpuBtn.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				IAction action = DideHelper.getAction("com.djyos.dide.ui.wizards.NewDWizard1");
				action.run();
				cpuArhives.removeAll();
				for (int i = 0; i < cpusList.size(); i++) {
					TreeItem t = new TreeItem(cpuArhives, SWT.NONE);
					t.setImage(DPluginImages.OBJ_CPU_VIEW.createImage());
					t.setText(cpusList.get(i).getCpuName());
				}
				super.widgetSelected(e);
			}

		});

		Composite searchCpuCpt = new Composite(newSearchCpt, SWT.NULL);
		GridLayout layoutSearchCpuCpt = new GridLayout();
		layoutSearchCpuCpt.numColumns = 2;
		searchCpuCpt.setLayout(layoutSearchCpuCpt);
		searchCpuCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label searchCpuLabel = new Label(searchCpuCpt, SWT.NONE);
		searchCpuLabel.setText("����Cpu: ");
		Text searchCpuField = new Text(searchCpuCpt, SWT.BORDER);
		searchCpuField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		searchCpuField.setMessage("������ؼ���");
		searchCpuField.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				// TODO Auto-generated method stub
				String cpuText = searchCpuField.getText();
				cpuArhives.removeAll();
				for (int i = 0; i < cpusList.size(); i++) {
					if (cpusList.get(i).getCpuName().contains(cpuText)) {
						TreeItem t = new TreeItem(cpuArhives, SWT.NONE);
						t.setText(cpusList.get(i).getCpuName());
						t.setImage(DPluginImages.OBJ_CPU_VIEW.createImage());
					}
				}
			}
		});

		boardAttributesCpt = new Composite(infoArea, SWT.NULL);
		GridLayout layoutAttributes = new GridLayout();
		layoutAttributes.numColumns = 3;
		boardAttributesCpt.setLayout(layoutAttributes);

		createTreeForCpus(boardAttributesCpt);
		createTransferButtons(boardAttributesCpt);
		createTreeForNeedCpus(boardAttributesCpt);

		cpuArhives.setSize(230, 240);
		cpuArhivesNeed.setSize(230, 240);
		createContentForAttribute(infoArea);

		if (cpuArhivesNeed.getItemCount() > 0) {
			cpuArhivesNeed.select(cpuArhivesNeed.getItem(0));
			displayOnboardCpuInfo();
		}

		Point point = infoArea.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		scrolledComposite.setContent(infoArea);
		scrolledComposite.setMinHeight(point.y);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
	}

	protected void Handle_Select_SimilarBoard() {
		// TODO Auto-generated method stub
		SelectBoardDialog dialog = new SelectBoardDialog(getShell());
		if (dialog.open() == Window.OK) {
			Board boardSelected = dialog.getSelectBoard();
			similarBoardPath = boardSelected.getBoardFolderPath();
			SelectBoardFilesDialog fDialog = new SelectBoardFilesDialog(getShell(), boardSelected);
			if (fDialog.open() == Window.OK) {
				similarfiles = fDialog.getSelectFiles();
			}
			initNeedCpus(boardSelected);
			cpuArhivesNeed.select(cpuArhivesNeed.getItem(0));
			displayOnboardCpuInfo();
		}
	}

	private void createContentForAttribute(Composite parent) {
		// TODO Auto-generated method stub

		ConfigurationGroup = ControlFactory.createGroup(parent, "��ѡ����Ҫ���õİ���cpu", 1);
		ConfigurationGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		ConfigurationGroup.setLayout(new GridLayout());

		folder = new TabFolder(ConfigurationGroup, SWT.NONE | SWT.H_SCROLL | SWT.V_SCROLL);
		folder.setLayout(new TabFolderLayout());
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));

		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText("ʱ��"); //$NON-NLS-1$
		item.setControl(createClkContent(folder));

		item = new TabItem(folder, SWT.NONE);
		item.setText("Cpu����"); //$NON-NLS-1$
		item.setControl(createPeripheralsContent(folder));

		TabItem chipItem = new TabItem(folder, SWT.NONE);
		chipItem.setText("����оƬ"); //$NON-NLS-1$
		chipItem.setControl(createChipContent(folder));

		item = new TabItem(folder, SWT.NONE);
		item.setText("˽�а��ش洢"); //$NON-NLS-1$
		item.setControl(createMemoryContent(folder,true));
		
		item = new TabItem(folder, SWT.NONE);
		item.setText("������ش洢"); //$NON-NLS-1$
		item.setControl(createSharedMemoryContent(folder));

		enableOperate(false);
		// item.setControl(createNewCpuContent(folder));
	}

	private void initNeedCpus(Board board) {
		cpuArhivesNeed.removeAll();
		List<OnBoardCpu> onCpus = board.getOnBoardCpus();
		onBoardCpus = onCpus;
		for (OnBoardCpu onBoardCpu : onCpus) {
			TreeItem t = new TreeItem(cpuArhivesNeed, SWT.NONE);
			t.setImage(DPluginImages.OBJ_CPU_VIEW.createImage());
			t.setText(onBoardCpu.getCpuName());
		}
	}

	private void createTreeForNeedCpus(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(parent, SWT.NULL);
		cpuArhivesNeed = DideHelper.buildTree(composite, 230, 240, "����Cpu", (SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL));
		
		if (boardInit != null) {
			initNeedCpus(boardInit);
		}
		cpuArhivesNeed.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				displayOnboardCpuInfo();
			}
		});
		cpusOn = new ArrayList<Cpu>();
	}

	protected void displayOnboardCpuInfo() {
		// TODO Auto-generated method stub
		gotoBtn.setEnabled(false);
		backBtn.setEnabled(true);
		TreeItem[] items = cpuArhivesNeed.getSelection();
		if (items.length > 0) {
			String selectCpuName = items[0].getText();
			OnBoardCpu onBoardCpu = DideHelper.getOnBoardByCpu(onBoardCpus, selectCpuName);
			enableOperate(true);
			ConfigurationGroup.setText("����Cpu[" + selectCpuName + "]����");
			mainClkField.setText(onBoardCpu.getMianClk()!=0?String.valueOf(onBoardCpu.getMianClk()):"");
			rtcClkField.setText(onBoardCpu.getRtcClk() != 0?String.valueOf(onBoardCpu.getRtcClk()):"");
			
			allPeripherals = new ArrayList<Component>();
			String cpuPath = getCpuPath(selectCpuName);
			List<String> cpuSrcPaths = new ArrayList<String>();
			getCpuSrcPaths(new File(cpuPath), cpuSrcPaths);
			for (String path : cpuSrcPaths) {
				List<Component> somePeripherals = ReadComponent.getSrcPeripherals(new File(path));
				allPeripherals.addAll(somePeripherals);
			}
			
			List<Component> boardPeripherals = onBoardCpu.getPeripherals();
			thePeripherals = new ArrayList<Component>();
			for (int i = 0; i < allPeripherals.size(); i++) {
				Component component = new Component();
				Component component1 = new Component();
				component.setName(allPeripherals.get(i).getName());
				component1.setName(allPeripherals.get(i).getName());
				thePeripherals.add(component);
				peripheralsList.add(component1);
			}
			cpudrvTree.removeAll();
			cpudrvOnTree.removeAll();
			// ��������Ϊ��
			if (boardPeripherals.size() == 0) {
				peripheralsOn = new ArrayList<Component>();
				for (int i = 0; i < allPeripherals.size(); i++) {
					TreeItem t = new TreeItem(cpudrvTree, SWT.NONE);
					t.setText(allPeripherals.get(i).getName());
				}
			} else { // �������費Ϊ��
				peripheralsOn = boardPeripherals;
				for (int j = 0; j < boardPeripherals.size(); j++) {// u1 u2
					String peripheraOnlName = boardPeripherals.get(j).getName();
					for (int i = 0; i < thePeripherals.size(); i++) {// nand u1 u2
						String peripheralName = thePeripherals.get(i).getName();
						if (peripheralName.equals(peripheraOnlName)) {
							TreeItem t = new TreeItem(cpudrvOnTree, SWT.NONE);
							t.setText(peripheraOnlName);
							thePeripherals.remove(i);
							break;
						}
					}
				}
				for (int j = 0; j < thePeripherals.size(); j++) {
					TreeItem t = new TreeItem(cpudrvTree, SWT.NONE);
					t.setText(thePeripherals.get(j).getName());
				}
			}

			List<Chip> chips = onBoardCpu.getChips();
			chipOnTree.removeAll();
			chipsOn = chips;
			for (int i = 0; i < chips.size(); i++) {
				TreeItem t = new TreeItem(chipOnTree, SWT.NONE);
				t.setText(chips.get(i).getChipName());
			}

			List<OnBoardMemory> memorysOn = onBoardCpu.getMemorys();
			memoryTree.removeAll();
			memoryTypeCombo.deselectAll();
			addrField.setText("");
			sizeField.setText("");
			memorys = memorysOn;
			if (memorys.size() > 0) {
				for (int i = 0; i < memorys.size(); i++) {
					memorys.get(i).setName("memory" + (i + 1));
					String memoryOnName = memorys.get(i).getName();
					TreeItem t = new TreeItem(memoryTree, SWT.NONE);
					t.setText(memoryOnName);
				}
				memoryTypeCombo.select(memorys.get(0).getType().equalsIgnoreCase("FLASH")?1:0);
				addrField.setText(memorys.get(0).getStartAddr());
				sizeField.setText(memorys.get(0).getSize());
				memoryTree.setSelection(memoryTree.getItems()[0]);
			}
		}
		
		
	}

	private void createTransferButtons(Composite parent) {
		// TODO Auto-generated method stub
		Composite btnCpt = new Composite(parent, SWT.NULL);
		btnCpt.setLayout(new GridLayout());
		btnCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL |  GridData.VERTICAL_ALIGN_CENTER | GridData.HORIZONTAL_ALIGN_CENTER));

		gotoBtn = new Button(btnCpt, SWT.PUSH);
		gotoBtn.setText("   ����  ");
		gotoBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		backBtn = new Button(btnCpt, SWT.PUSH);
		backBtn.setText(" ����    ");
		backBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		gotoBtn.setEnabled(false);
		backBtn.setEnabled(false);

		gotoBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = cpuArhives.getSelection();
				if (items.length > 0) {
					String selectCpuName = items[0].getText();
					changeCpusOn(selectCpuName, true);

					OnBoardCpu onBoardCpu = new OnBoardCpu();
					onBoardCpu.setCpuName(selectCpuName);
					onBoardCpus.add(onBoardCpu);
					updateCpuOn();
				}
			}
		});

		backBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = cpuArhivesNeed.getSelection();
				if (items.length > 0) {
					String selectCpuName = items[0].getText();
					changeCpusOn(selectCpuName, false);
					for (int i = 0; i < onBoardCpus.size(); i++) {
						if (onBoardCpus.get(i).getCpuName().equals(selectCpuName)) {
							onBoardCpus.remove(i);
						}
					}
					items[0].dispose();
					// updateCpuList();
				}
			}
		});

	}

	private void createTreeForCpus(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(parent, SWT.NULL);
		cpuArhives = DideHelper.buildTree(composite, 230, 240, "Cpu�б�", (SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL));

		for (int i = 0; i < cpusList.size(); i++) {
			TreeItem t = new TreeItem(cpuArhives, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
			t.setImage(DPluginImages.OBJ_CPU_VIEW.createImage());
			t.setText(cpusList.get(i).getCpuName());
		}
		
		cpuArhives.addMouseMoveListener(new MouseMoveListener() {

			@Override
			public void mouseMove(MouseEvent e) {
				// TODO Auto-generated method stub
				Point point = new Point(e.x, e.y);
				TreeItem item = cpuArhives.getItem(point);
				String descContent = "";
				if (item != null) {
					for (Cpu cpu : cpusList) {
						if (cpu.getCpuName().equals(item.getText())) {
							if (cpu.getCores().size() != 0) {
								for (int j = 0; j < cpu.getCores().size(); j++) {
									Core core = cpu.getCores().get(j);
									descContent += "�ں�" + (j + 1) + ": ";
									if (core.getArch().getSerie() != null) {
										descContent += "\n����: " + core.getArch().getSerie();
									}
									if (core.getArch().getMarch() != null) {
										descContent += "\n�ܹ�: " + core.getArch().getMarch();
									}
									if (core.getArch().getMcpu() != null) {
										descContent += "\n����: " + core.getArch().getMcpu();
									}
									if (core.getFpuType() != null) {
										descContent += "\n����: " + core.getFpuType();
									}
									if (core.getResetAddr() != null) {
										descContent += "\n��λ��ַ: " + core.getResetAddr();
									}
									descContent += "\n";
								}
								cpuArhives.setToolTipText(descContent);
							}
							break;
						}
					}
				}
			}
		});

		cpuArhives.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				gotoBtn.setEnabled(true);
				backBtn.setEnabled(false);
			}
		});
	}

	private void updatePeripheralsList() {
		TreeItem t = new TreeItem(cpudrvTree, SWT.NONE);
		if (newComponent != null) {
			t.setText(newComponent.getName());
		}
	}

	private void updateCpuOn() {
		if (newCpu != null) {
			TreeItem t = new TreeItem(cpuArhivesNeed, SWT.NONE);
			t.setImage(DPluginImages.OBJ_CPU_VIEW.createImage());
			t.setText(newCpu.getCpuName());
		}
	}

	private void updateChipOn() {
		TreeItem t = new TreeItem(chipOnTree, SWT.NONE);
		t.setText(newChip.getChipName());
	}

	private void updatePeripheralsOn() {
		TreeItem t = new TreeItem(cpudrvOnTree, SWT.NONE);
		t.setText(newComponent.getName());
	}

	private String getCpuPath(String cpuName) {
		for (Cpu c : cpusList) {
			if (c.getCpuName().equals(cpuName)) {
				return c.getCpuFolderPath();
			}
		}
		return null;
	}

	protected void getCpuSrcPaths(File cpuFile, List<String> cpuSrcPaths) {
		// TODO Auto-generated method stub
		File[] files = cpuFile.listFiles();
		for (File file : files) {
			String path = file.getPath();
			if (file.getName().equals("src")) {
				cpuSrcPaths.add(path);
				break;
			}
		}
		if (!cpuFile.getParentFile().getName().equals("cpudrv")) {
			getCpuSrcPaths(cpuFile.getParentFile(), cpuSrcPaths);
		}
	}
	

	private void enableOperate(boolean enable) {
		mainClkLabel.setEnabled(enable);
		mainClkField.setEnabled(enable);
		rtcClkBtn.setEnabled(enable);
		chipTree.setEnabled(enable);
		chipOnTree.setEnabled(enable);
		cpudrvTree.setEnabled(enable);
		cpudrvOnTree.setEnabled(enable);
		memoryTree.setEnabled(enable);
		shared_memoryTree.setEnabled(enable);
		shared_addBtn.setEnabled(enable);
	}

	public void enableMemory(boolean enable) {
		deleteBtn.setEnabled(enable);
		addrField.setEnabled(enable);
		sizeField.setEnabled(enable);
		memoryTypeCombo.setEnabled(enable);
	}
	
	public void enableSharedMemory(boolean enable) {
		shared_deleteBtn.setEnabled(enable);
		shared_addrField.setEnabled(enable);
		shared_sizeField.setEnabled(enable);
		shared_memoryTypeCombo.setEnabled(enable);
	}
}
