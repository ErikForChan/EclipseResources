/*******************************************************************************
 * Copyright (c) 2017 Djyos Team.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.djyos.com
 *
 * Contributors:
 *     Djyos Team - Jiaming Chen
 *******************************************************************************/
package com.djyos.dide.ui.djyproperties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.objects.Board;
import com.djyos.dide.ui.objects.Chip;
import com.djyos.dide.ui.objects.Component;
import com.djyos.dide.ui.objects.Core;
import com.djyos.dide.ui.objects.CoreMemory;
import com.djyos.dide.ui.objects.Cpu;
import com.djyos.dide.ui.objects.OnBoardCpu;
import com.djyos.dide.ui.objects.OnBoardMemory;
import com.djyos.dide.ui.wizards.board.ReadBoardXml;
import com.djyos.dide.ui.wizards.cpu.ReadCpuXml;
import com.djyos.dide.ui.wizards.djyosProject.ReadHardWareDesc;

@SuppressWarnings("restriction")
public class DjyosGeneralPage extends PropertyPage {
	private Board sBoard = null;
	private List<Cpu> cpusList = null;
	private Button hiddenLibosButton;
	boolean hiddenLibos;
	private IProject project;
	String didePjPrefsPath;

	@Override
	protected Control createContents(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(parent, SWT.NONE);
		initializeDialogUnits(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);
		composite.setLayout(new GridLayout(1, true));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		boolean srcExist = DideHelper.isDjysrcExist();
		if (srcExist) {
			createDynamicGroup(composite);
		} else {
			Label warningLabel = new Label(composite, SWT.NONE);
			warningLabel.setText("Djyos源码不存在，请重启DIDE根据提示下载");
		}

		return composite;
	}

	private void createDynamicGroup(Composite parent) {
		// TODO Auto-generated method stub
		project = getProject();
		didePjPrefsPath = project.getLocation().toString() + "/.settings/com.djyos.ui.prefs";
		File didePrefsFile = new File(didePjPrefsPath);

		if (didePrefsFile.exists()) {
			hiddenLibos = DideHelper.targetIsTrue(didePrefsFile, "HIDDEN_LIBOS_COMPILER");
		} else {
			hiddenLibos = false;
		}

		hiddenLibosButton = new Button(parent, SWT.CHECK);
		hiddenLibosButton.setText("隐藏本工程的Libos编译选项");
		hiddenLibosButton.setSelection(hiddenLibos);

		Group descGroup = ControlFactory.createGroup(parent, "硬件描述", 1);
		descGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		descGroup.setLayout(new GridLayout());
		cpusList = ReadCpuXml.getAllCpus();
		getBoardAndCpu();
		List<OnBoardCpu> onBoardCpus = sBoard.getOnBoardCpus();
		String infos = "";
		infos += "板件：" + sBoard.getBoardName();
		if (onBoardCpus.size() > 1) {
			for (int i = 0; i < onBoardCpus.size(); i++) {
				List<Component> peripherals = onBoardCpus.get(i).getPeripherals();
				List<Chip> chips = onBoardCpus.get(i).getChips();
				List<OnBoardMemory> memorys = onBoardCpus.get(i).getMemorys();
				infos += "\n\t板载Cpu" + (i + 1) + "：" + onBoardCpus.get(i).getCpuName();
				for (Cpu cpu : cpusList) {
					if (cpu.getCpuName().equals(onBoardCpus.get(i).getCpuName())) {
						List<Core> cores = cpu.getCores();
						for (int j = 0; j < cores.size(); j++) {
							infos += "\n\t\t内核" + (j + 1) + "：";
							infos += "\n\t\t\t类型：" + cores.get(j).getArch().getSerie();
							infos += "\n\t\t\t架构：" + cores.get(j).getArch().getMarch();
							infos += "\n\t\t\t家族：" + cores.get(j).getArch().getMcpu();
							infos += "\n\t\t\t浮点：" + cores.get(j).getFpuType();
							List<CoreMemory> coreMemorys = cores.get(j).getMemorys();
							infos += "\n\t\t\t片内内存：";
							if (coreMemorys.size() == 0) {
								infos += "无";
							} else {
								for (int k = 0; k < coreMemorys.size(); k++) {
									infos += "\n\t\t\t\tMemory" + (k + 1) + "：" + coreMemorys.get(k).getType() + "，"
											+ coreMemorys.get(k).getStartAddr() + "，" + coreMemorys.get(k).getSize();
								}
							}

						}
						break;
					}
				}
				if (onBoardCpus.get(i).getRtcClk() != 0) {
					infos += "\n\tRtc频率：" + onBoardCpus.get(i).getRtcClk();
				}
				infos += "\n\t板载设备：";
				if (chips.size() == 0) {
					infos += "无";
				} else {
					for (int j = 0; j < chips.size(); j++) {
						infos += (j != 0 ? "，" : "") + chips.get(j).getChipName();
					}
				}

				infos += "\n\t主晶振频率：" + onBoardCpus.get(i).getMianClk();
				infos += "\n\tCpu外设：";
				if (peripherals.size() == 0) {
					infos += "无";
				} else {
					for (int j = 0; j < peripherals.size(); j++) {
						infos += peripherals.get(j).getName() + (j != peripherals.size() - 1 ? "，" : "");
						if (j % 3 == 0 && j != 0) {
							infos += "\n\t";
						}
					}
				}

				infos += "\n\t片外内存：";
				if (memorys.size() == 0) {
					infos += "无";
				} else {
					for (int j = 0; j < memorys.size(); j++) {
						infos += "\n\t\tMemory" + (j + 1) + "：" + memorys.get(j).getType() + "，"
								+ memorys.get(j).getStartAddr() + "，" + memorys.get(j).getSize();
					}
				}

			}

		} else if (onBoardCpus.size() == 1) {
			List<Component> peripherals = onBoardCpus.get(0).getPeripherals();
			List<Chip> chips = onBoardCpus.get(0).getChips();
			List<OnBoardMemory> memorys = onBoardCpus.get(0).getMemorys();
			infos += "\n\t板载Cpu" + "：" + onBoardCpus.get(0).getCpuName();

			for (Cpu cpu : cpusList) {
				if (cpu.getCpuName().equals(onBoardCpus.get(0).getCpuName())) {
					List<Core> cores = cpu.getCores();
					for (int j = 0; j < cores.size(); j++) {
						infos += "\n\t\t内核" + (j + 1) + "：";
						infos += "\n\t\t\t类型：" + cores.get(j).getArch().getSerie();
						infos += "\n\t\t\t架构：" + cores.get(j).getArch().getMarch();
						infos += "\n\t\t\t家族：" + cores.get(j).getArch().getMcpu();
						infos += "\n\t\t\t浮点：" + cores.get(j).getFpuType();
						List<CoreMemory> coreMemorys = cores.get(j).getMemorys();
						infos += "\n\t\t\t片内内存：";
						if (coreMemorys.size() == 0) {
							infos += "无";
						} else {
							for (int k = 0; k < coreMemorys.size(); k++) {
								infos += "\n\t\t\t\tMemory" + (k + 1) + "：" + coreMemorys.get(k).getType() + "，"
										+ coreMemorys.get(k).getStartAddr() + "，" + coreMemorys.get(k).getSize();
							}
						}
					}
					break;
				}
			}
			if (onBoardCpus.get(0).getRtcClk() != 0) {
				infos += "\n\tRtc频率：" + onBoardCpus.get(0).getRtcClk();
			}
			infos += "\n\t板载设备：";
			if (chips.size() == 0) {
				infos += "无";
			} else {
				for (int j = 0; j < chips.size(); j++) {
					infos += (j != 0 ? "，" : "") + chips.get(j).getChipName();
				}
			}

			infos += "\n\t主晶振频率：" + onBoardCpus.get(0).getMianClk();
			infos += "\n\tCpu外设：";
			if (memorys.size() == 0) {
				infos += "无";
			} else {
				for (int j = 0; j < peripherals.size(); j++) {
					infos += peripherals.get(j).getName() + (j != peripherals.size() - 1 ? "，" : "");
					if (j % 3 == 0 && j != 0) {
						infos += "\n\t";
					}
				}
			}
			infos += "\n\t片外内存：";
			if (memorys.size() == 0) {
				infos += "无";
			} else {
				for (int j = 0; j < memorys.size(); j++) {
					infos += "\n\t\tMemory" + (j + 1) + "：" + memorys.get(j).getType() + "，"
							+ memorys.get(j).getStartAddr() + "，" + memorys.get(j).getSize();
				}
			}

		}

		Label descLabel = new Label(descGroup, SWT.NONE);
		descLabel.setText(infos);
	}

	private void getBoardAndCpu() {
		String srcPath = project.getLocation().toString() + "/src";
		File hardWardInfoFile = new File(srcPath + "/../" + "data/hardware_info.xml");
		List<String> hardwares = ReadHardWareDesc.getHardWares(hardWardInfoFile);
		String cpuName = hardwares.get(1);
		String boardName = hardwares.get(0);

		List<Board> boards = ReadBoardXml.getAllBoards();

		for (int i = 0; i < boards.size(); i++) {
			if (boards.get(i).getBoardName().equals(boardName)) {
				sBoard = boards.get(i);
				break;
			}
		}

		if (sBoard != null) {
			List<OnBoardCpu> onBoardCpus = sBoard.getOnBoardCpus();
			for (int i = 0; i < onBoardCpus.size(); i++) {
				if (onBoardCpus.get(i).getCpuName().equals(cpuName)) {
					onBoardCpus.get(i);
					break;
				}
			}
		}

	}

	private void setDjyosUiPrefs(File didePrefsFile, boolean isTrue, String target) {
		// TODO Auto-generated method stub
		BufferedReader br = null;
		String line = null;
		boolean targetExist = false;
		StringBuffer bufAll = new StringBuffer(); // 保存修改过后的所有内容，不断增加
		try {
			br = new BufferedReader(new FileReader(didePrefsFile));
			while ((line = br.readLine()) != null) {
				// 修改内容核心代码
				if (line.startsWith(target)) {
					line = target + "=" + isTrue;
					targetExist = true;
				}
				bufAll.append(line + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					br = null;
				}
			}
		}
		if (!targetExist) {
			bufAll.append(target + "=" + isTrue + "\n");
		}
		didePrefsFile.delete();
		fillDidePrefsFile(didePrefsFile, bufAll.toString());

	}

	private void fillDidePrefsFile(File dideGitPrefsFile, String content) {
		// TODO Auto-generated method stub
		try {
			dideGitPrefsFile.createNewFile();
			FileWriter writer;
			try {
				writer = new FileWriter(dideGitPrefsFile);
				writer.write(content);
				writer.flush();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private IProject getProject() {
		Object element = getElement();
		IProject project = null;

		if (element instanceof IProject) {
			project = (IProject) element;
		} else if (element instanceof IAdaptable) {
			project = ((IAdaptable) element).getAdapter(IProject.class);
		}
		return project;
	}

	@Override
	public boolean performOk() {
		// TODO Auto-generated method stub
		boolean hiddenLibosOption = hiddenLibosButton.getSelection();
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) {
				monitor.beginTask("保存设置...", 10);
				handleOK(monitor, hiddenLibosOption);
				monitor.worked(10);
				monitor.done();
				monitor.setTaskName("完成");
			}
		};
		try {
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(
					PlatformUI.getWorkbench().getDisplay().getActiveShell());
			dialog.setCancelable(false);
			dialog.run(true, true, runnable);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	protected void handleOK(IProgressMonitor monitor, boolean hiddenLibosOption) {
		// TODO Auto-generated method stub
		File didePrefsFile = new File(didePjPrefsPath);
		if (didePrefsFile.exists()) {
			if (hiddenLibos != hiddenLibosOption) {
				setDjyosUiPrefs(didePrefsFile, hiddenLibosOption, "HIDDEN_LIBOS_COMPILER");
			}
		} else {
			if (hiddenLibos != hiddenLibosOption) {
				fillDidePrefsFile(didePrefsFile, "HIDDEN_LIBOS_COMPILER=" + hiddenLibosOption);
			}
		}
	}

	@Override
	public boolean isDjyos() {
		// TODO Auto-generated method stub
		return true;
	}

}
