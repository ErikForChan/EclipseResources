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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

import com.djyos.dide.ui.DPluginImages;
import com.djyos.dide.ui.bsp.BspTemplate;
import com.djyos.dide.ui.bsp.ReadBspTemplate;
import com.djyos.dide.ui.bsp.SelectUserDefinedFilePathDialog;
import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.objects.Arch;
import com.djyos.dide.ui.objects.Board;
import com.djyos.dide.ui.objects.BspStep;
import com.djyos.dide.ui.objects.Cpu;
import com.djyos.dide.ui.wizards.djyosProject.ReadHardWareDesc;

@SuppressWarnings("restriction")
public class StepByStepPage extends PropertyPage {

	private Tree stepTree;

	private Group configGroup = null;

	private File stepPrefsFile = null;

	private String stepDesc = "";

	private List<String> savedSteps = null;

	private File hardWardInfoFile;

	private Board projectBoard;

	private Cpu projectCpu;

	private Arch projectArch;

	final IWorkspace workspace = ResourcesPlugin.getWorkspace();

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
		DideHelper.createNewFile(stepPrefsFile);
		boolean isError = false;
		stepDesc = "";
		TreeItem[] roots = stepTree.getItems();
		for (TreeItem t : roots) {
			if (t.getChecked()) {
				stepDesc += t.getText() + ": ";
				checkChilds(t, stepDesc);
				stepDesc += ";\n";
			}
		}

		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) {
				monitor.beginTask("存储配置...", 10);
				// handleOK(monitor);
				DideHelper.writeFile(stepPrefsFile, stepDesc);
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
			isError = true;
		}
		return !isError;
	}

	@Override
	protected Control createContents(Composite parent) {
		// TODO Auto-generated method stub
		IProject project = getProject();
		Composite composite = new Composite(parent, SWT.NONE);
		initializeDialogUnits(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);
		composite.setLayout(new GridLayout(1, true));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		IFile file = project.getFile("src/lds/bsp.lds");
		if (file.exists()) {
			stepPrefsFile = new File(project.getLocation().toString() + "/.settings/com.djyos.ui.step.prefs");
			hardWardInfoFile = new File(project.getLocation().toString() + "/data/hardware_info.xml");
			List<String> hardwares = ReadHardWareDesc.getHardWares(hardWardInfoFile);
			projectBoard = DideHelper.getBoardByName(hardwares.get(0));
			projectCpu = DideHelper.getCpuByName(hardwares.get(1));
			projectArch = projectCpu.getCores().get(0).getArch();
			createDynamicGroup(composite);
		} else {
			Label label = new Label(composite, SWT.NONE);
			label.setText("此工程 [" + project.getName() + "] 不是bsp工程");
		}

		return composite;
	}

	private Listener selectionListener = e -> {
		TreeItem item = (TreeItem) e.item;
		if (item == null) {
			return;
		} else {
			if (!item.getGrayed()) {
				// 勾选后父组件与子组件的反应
				boolean checked = item.getChecked();
				travelChildItems(item, checked);
				travelParentItems(item, checked);
			}
		}
	};

	private Listener doubleSelectionListener = e -> {
		Point point = new Point(e.x, e.y);
		TreeItem item = stepTree.getItem(point);
		if (item == null) {
			return;
		} else {
			if (!item.getGrayed()) {
				// 处理勾选后的动作
				@SuppressWarnings("unchecked")
				List<BspTemplate> templates = (List<BspTemplate>) item.getData("templates");

				if (templates != null) {
					List<File> files = new ArrayList<File>();
					List<String> openFilenames = new ArrayList<String>();
					List<String> parentFileNames = new ArrayList<String>();
					List<String> lastMembers = new ArrayList<String>();
					List<File> objectFiles = new ArrayList<File>();
					for (BspTemplate t : templates) {
						if (hardWardInfoFile.exists()) {
							String fileName = t.getFileName();
							String location = t.getLocation();
							// System.out.println("fileName: " + fileName + "\nlocation: " + location);
							String[] members = location.split("/");
							String lastMember = members[members.length - 1].trim();
							if (location.startsWith("userdef")) {
								File objectFile = null;
								if (lastMember.equals("arch")) {
									objectFile = new File(projectArch.getArchPath());
								} else if (lastMember.equals("cpudrv")) {
									objectFile = new File(projectCpu.getParentPath());
								}
								objectFiles.add(objectFile);
								lastMembers.add(lastMember);
								getParentFileNames(objectFile, lastMember, parentFileNames);
								openFilenames.add(fileName);
							} else {
								if (location.startsWith("board")) {
									File file = new File(
											projectBoard.getBoardPath() + "/" + lastMember + "/" + fileName);
									initNewFile(file);
									files.add(file);
								}
							}
						}
					}

					if (openFilenames.size() > 0) {
						SelectUserDefinedFilePathDialog dialog = new SelectUserDefinedFilePathDialog(getShell(),
								openFilenames, objectFiles, lastMembers);
						if (dialog.open() == Window.OK) {
							List<File> newFiles = dialog.getFiles();
							for (File f : newFiles) {
								if (f.exists()) {
									boolean cover = MessageDialog.openQuestion(getShell(), "提示",
											f.getName() + "已存在，是否覆盖?");
									if (cover) {
										initNewFile(f);
									}
								} else {
									initNewFile(f);
								}

							}
							for (File f : newFiles) {
								DideHelper.openFileInDide(f);
							}
						}
					} else {
						for (File f : files) {
							DideHelper.openFileInDide(f);
						}
						MessageDialog.openInformation(getShell(), "提示", "配置成功");
					}

				}
			}

			try {
				workspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	};

	private void createDynamicGroup(Composite composite) {
		// TODO Auto-generated method stub
		savedSteps = new ArrayList<String>();
		if (stepPrefsFile.exists()) {
			String desc = DideHelper.readFile(stepPrefsFile);
			String[] allFuns = desc.split(";");
			for (String fun : allFuns) {
				if (fun.trim().contains(":")) {
					String[] funCur = fun.trim().split(":");
					String[] stepsCur = funCur[1].trim().split(",");
					for (String step : stepsCur) {
						savedSteps.add(step.trim());
					}
				}
			}
		}

		SashForm sashForm = new SashForm(composite, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

		stepTree = new Tree(sashForm, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.CHECK);
		stepTree.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		initStepTree();
		stepTree.addListener(SWT.MouseDoubleClick, doubleSelectionListener);
		stepTree.addListener(SWT.Selection, selectionListener);
		stepTree.addMouseMoveListener(new MouseMoveListener() {

			@Override
			public void mouseMove(MouseEvent e) {
				// TODO Auto-generated method stub
				Point point = new Point(e.x, e.y);
				TreeItem item = stepTree.getItem(point);
				if (item != null) {
					if (!item.getData().toString().equals("Steps")) {
						stepTree.setToolTipText("双击可开始该步骤的配置");
					}
				}

				// if (item != null) {
				// if (!item.getText().equals("App") && !item.getText().equals("Iboot")) {
				// String descContent = item.getData("anno").toString();
				// componentTree.setToolTipText(descContent);
				// }
				// }
			}
		});

		configGroup = ControlFactory.createGroup(sashForm, "步骤说明", 1);
		configGroup.setLayout(new GridLayout(1, false));
		GridData groupData = new GridData(GridData.FILL_BOTH);
		groupData.grabExcessHorizontalSpace = true;
		configGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

		Text stepDesc = new Text(configGroup, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
		stepDesc.setEditable(false);
		stepDesc.setLayoutData(new GridData(GridData.FILL_BOTH));
		stepDesc.setText("步骤说明...");

		stepTree.addListener(SWT.MouseDown, new Listener() {

			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				Point point = new Point(event.x, event.y);
				TreeItem item = stepTree.getItem(point);
				if (item == null) {
					return;
				} else {
					stepDesc.setText("\t" + item.getData().toString());
				}

			}
		});
		sashForm.setWeights(new int[] { 1, 1 });// 内部容器之间宽度比例

	}

	private void initNewFile(File file) {
		// TODO Auto-generated method stub
		DideHelper.createNewFile(file);
		String content = DideHelper.readFile(new File(DideHelper.getStepByStepPath() + "/" + file.getName()));
		DideHelper.writeFile(file, content);
	}

	private void getParentFileNames(File objectFile, String lastMember, List<String> parentFileNames) {
		// TODO Auto-generated method stub
		if (objectFile.isDirectory()) {
			parentFileNames.add(objectFile.getName());
		}
		File parentFile = objectFile.getParentFile();
		if (!parentFile.getName().equals(lastMember)) {
			objectFile = getRootFile(parentFile, lastMember);
		}
	}

	private File getRootFile(File objectFile, String lastMember) {
		// TODO Auto-generated method stub
		File parentFile = objectFile.getParentFile();
		if (!parentFile.getName().equals(lastMember)) {
			objectFile = getRootFile(parentFile, lastMember);
		}
		return objectFile;
	}

	private void travelParentItems(TreeItem item, boolean checked) {
		// TODO Auto-generated method stub
		TreeItem parentItem = item.getParentItem();
		if (parentItem != null && checked) {
			parentItem.setGrayed(true);
			parentItem.setChecked(true);
			TreeItem[] brotherItems = parentItem.getItems();
			boolean allChecked = true;
			boolean oneGrayed = false;
			for (TreeItem t : brotherItems) {
				if (!t.getChecked()) {
					allChecked = false;
				}
				if (t.getGrayed()) {
					oneGrayed = true;
				}
			}
			if (allChecked && !oneGrayed) {
				parentItem.setGrayed(false);
			}
			travelParentItems(parentItem, checked);
		} else if (parentItem != null && !checked) {
			if (parentItem != null) {
				parentItem.setGrayed(true);
				TreeItem[] brotherItems = parentItem.getItems();
				boolean allCanceled = true;
				for (TreeItem t : brotherItems) {
					if (t.getChecked()) {
						allCanceled = false;
					}
				}
				if (allCanceled) {
					parentItem.setGrayed(false);
					parentItem.setChecked(false);
				}
			}
			travelParentItems(parentItem, checked);
		}
	}

	private void travelChildItems(TreeItem item, boolean checked) {
		// TODO Auto-generated method stub
		if (checked) {
			item.setGrayed(false);
		}
		TreeItem[] childItems = item.getItems();
		for (TreeItem t : childItems) {
			t.setChecked(checked);
			travelChildItems(t, checked);
		}
	}

	protected void checkChilds(TreeItem t, String stepDesc) {
		// TODO Auto-generated method stub
		TreeItem[] items = t.getItems();
		int lastCheck = -1;
		for (int i = 0; i < items.length; i++) {
			if (items[i].getChecked()) {
				lastCheck = i;
			}
		}
		for (int i = 0; i < items.length; i++) {
			if (items[i].getChecked()) {
				stepDesc += items[i].getText() + (i == lastCheck ? "" : ", ");
			}
		}
	}

	private void initStepTree() {
		// TODO Auto-generated method stub
		TreeItem root = new TreeItem(stepTree, 0);
		root.setText("Steps");
		root.setImage(DPluginImages.CFG_CPMT_OBJ.createImage());
		root.setData("Steps");// 保存当前节点数据
		String tag = fillChild(root);
		if (tag != null) {
			root.setChecked(true);
			if (tag.equals("G")) {
				root.setGrayed(true);
			}
		}
	}

	private String fillChild(TreeItem root) {
		boolean select = false, notSelect = false;
		List<BspStep> steps = ReadBspTemplate
				.getBspSteps(new File(DideHelper.getStepByStepPath() + "/BspTemplate.xml"));
		for (BspStep step : steps) {
			TreeItem item = new TreeItem(root, 0);
			item.setText(step.getName());
			item.setImage(DPluginImages.OBJ_STEP_VIEW.createImage());
			item.setData(step.getInfo());// 保存当前节点数据
			item.setData("templates", step.getTemplates());// 保存当前节点数据
			if (stepPrefsFile.exists()) {
				if (savedSteps.contains(step.getName())) {
					item.setChecked(true);
					select = true;
				} else {
					notSelect = true;
				}
			}
		}

		if (select) {
			if (notSelect) {
				return "G";
			} else {
				return "S";
			}
		} else {
			return null;
		}
	}

}
