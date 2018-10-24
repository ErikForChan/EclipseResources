package com.djyos.dide.ui.wizards.djyosProject;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
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

import com.djyos.dide.ui.wizards.cpu.Cpu;
import com.djyos.dide.ui.wizards.cpu.core.Core;
import com.djyos.dide.ui.wizards.cpu.core.memory.CoreMemory;

public class SelectCpuDialog extends StatusDialog {

	private String detailsDesc = null;
	private Text detailsField, cpuEditText;
	private Label cpuSearchLabel;
	private Tree cpuTree;
	private List<Cpu> cpus = new ArrayList<Cpu>(), cpusFiltered;
	private Cpu cpuSelected;

	public Cpu getSelectCpu() {
		return cpuSelected;
	}

	public SelectCpuDialog(Shell parent, List<Cpu> boardCpusList) {
		super(parent);
		cpus = boardCpusList;
		setTitle("选择Cpu");
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
		Composite composite = (Composite) super.createDialogArea(parent);
		Composite cpuCpt = new Composite(composite, SWT.NONE);
		GridLayout cpuLayout = new GridLayout();
		cpuLayout.numColumns = 2;
		cpuCpt.setLayout(cpuLayout);
		cpuCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createTreeForCpus(cpuCpt);
		cpuTree.setSize(170, 200);
		detailsField = new Text(cpuCpt, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		detailsField.setLayoutData(new GridData(GridData.FILL_BOTH));
		detailsField.setEditable(false);
		Button newBoradBtn = new Button(composite, SWT.PUSH);
		newBoradBtn.setText("Create new Cpu");
		newBoradBtn.setVisible(false);
		// 点击新建板件后弹出新建板件的向导
		newBoradBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				// BoardMainWizard boardWizard = new BoardMainWizard();
				// boardWizard.
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
		TreeItem[] items = cpuTree.getSelection();
		if (items.length > 0) {
			String cpuName = items[0].getText();
			for (int i = 0; i < cpusFiltered.size(); i++) {
				if (cpusFiltered.get(i).getCpuName().equals(cpuName)) {
					cpuSelected = cpusFiltered.get(i);
					break;
				}
			}
		}
		super.okPressed();
	}

	private void createTreeForCpus(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		cpuTree = new Tree(composite, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		cpuTree.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		cpuTree.setHeaderVisible(true);
		cpusFiltered = cpus;
		for (int i = 0; i < cpus.size(); i++) {
			// boolean newTree = true;
			// Cpu cpu = cpus.get(i);
			// if (cpu.getCores().size() < 2) {
			// Arch arch = cpu.getCores().get(0).getArch();
			// String tc = arch.getToolchain();
			// String tcStart = toolchain.split("-")[0];
			// if (!tc.startsWith(tcStart)) {
			// newTree = false;
			// }
			// }
			//
			// if (newTree) {
			TreeItem t = new TreeItem(cpuTree, SWT.NONE);
			t.setText(cpus.get(i).getCpuName());
			// }

		}

		cpuTree.addListener(SWT.MouseDoubleClick, new Listener() {

			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				okPressed();
			}
		});

		cpuTree.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

				TreeItem[] items = cpuTree.getSelection();
				if (items.length > 0) {
					String cpuName = items[0].getText();
					detailsDesc = "";
					for (int i = 0; i < cpus.size(); i++) {
						if (cpus.get(i).getCpuName().equals(cpuName)) {
							Cpu cpu = cpus.get(i);
							List<Core> cores = cpu.getCores();
							for (int j = 0; j < cores.size(); j++) {
								Core core = cores.get(j);
								String arch = core.getArch().getMarch();
								String family = core.getArch().getMcpu();
								String fpuType = core.getFpuType();
								String resetAddr = core.getResetAddr();
								String memoryString = "";
								for (int k = 0; k < core.getMemorys().size(); k++) {
									CoreMemory memory = core.getMemorys().get(k);
									if (memory.getType().equals("ROM")) {
										memoryString += "\n\tFlashStart: " + memory.getStartAddr() + "\n\tFlashSize: "
												+ memory.getSize() + "k";
									} else if (memory.getType().equals("RAM")) {
										memoryString += "\n\tRamStart: " + memory.getStartAddr() + "\n\tRamSize: "
												+ memory.getSize() + "k";
									}
								}
								detailsDesc += "Arch: " + arch + "\nFamily: " + family + "\nFputype: " + fpuType
										+ "\nResetAddr: " + resetAddr + "\nMemory:  " + memoryString + "\n\n";
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
		final TreeColumn columnCpus = new TreeColumn(cpuTree, SWT.NONE);
		columnCpus.setText("Cpu名称");
		columnCpus.setWidth(140);
		columnCpus.setResizable(false);
		columnCpus.setToolTipText("Cpu");
	}

}
