package com.djyos.dide.ui.djyproperties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

import com.djyos.dide.ui.wizards.board.Board;
import com.djyos.dide.ui.wizards.board.onboardcpu.OnBoardCpu;
import com.djyos.dide.ui.wizards.component.CmpntCheck;
import com.djyos.dide.ui.wizards.component.Component;
import com.djyos.dide.ui.wizards.component.ConfigureTarget;
import com.djyos.dide.ui.wizards.component.CreateCheckXml;
import com.djyos.dide.ui.wizards.component.ReadCheckXml;
import com.djyos.dide.ui.wizards.component.ReadComponent;
import com.djyos.dide.ui.wizards.djyosProject.DjyosMessages;
import com.djyos.dide.ui.wizards.djyosProject.ReadHardWareDesc;
import com.djyos.dide.ui.wizards.djyosProject.tools.Calculator;
import com.djyos.dide.ui.wizards.djyosProject.tools.DPluginImages;
import com.djyos.dide.ui.wizards.djyosProject.tools.DideHelper;
import com.djyos.dide.ui.wizards.djyosProject.tools.LinkHelper;
import com.ibm.icu.text.DecimalFormat;

public class ComponentCfgPage extends PropertyPage {

	private DideHelper dideHelper = new DideHelper();
	private LinkHelper linkHelper = new LinkHelper();
	private String warningMsg = null;

	private boolean handleOK(IProgressMonitor monitor) {

		// for(Component c:appCompontents) {
		// if(c.getName().equals("fatfilesystem")) {
		// System.out.println("fatfilesystem3.......................: "+c.isSelect());
		// }
		// }

		IProject project = getProject();
		final ICProjectDescription local_prjd = CoreModel.getDefault().getProjectDescription(project);
		ICConfigurationDescription[] conds = local_prjd.getConfigurations(); // 获取工程的所有Configuration
		File appCfgFile = new File(project.getLocation().toString() + "/src/app/OS_prjcfg/project_config.h");
		File ibootCfgFile = new File(project.getLocation().toString() + "/src/iboot/OS_prjcfg/project_config.h");
		List<File> cfgFiles = new ArrayList<File>();
		if (selectChanged) {
			if (appExist) {
				boolean isOK = handleCheckAndExclude(appCompontentsChecked, appCompontents, appCompontentsInit, project,
						conds, true);
				if (!isOK) {
					return false;
				}
				if (appConfigureChanged) {
					cfgFiles.add(appCfgFile);
				}
			}
			if (ibootExist) {
				boolean isOK = handleCheckAndExclude(ibootCompontentsChecked, ibootCompontents, ibootCompontentsInit,
						project, conds, false);
				if (!isOK) {
					return false;
				}
				if (ibootConfigureChanged) {
					cfgFiles.add(ibootCfgFile);
				}
			}
		}

		for (File file : cfgFiles) {
			String str = null, coreConfigure = null;
			boolean isApp;
			if (file.getPath().contains("app")) {
				isApp = true;
			} else {
				isApp = false;
			}
			try {
				FileReader reader = new FileReader(file.getPath());
				BufferedReader br = new BufferedReader(reader);
				reader = new FileReader(file.getPath());
				while ((str = br.readLine()) != null) {
					if (str.contains("CFG_CORE_MCLK")) {
						coreConfigure = str;
						break;
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			handleInitFiles(project, isApp, file, coreConfigure);
		}

		monitor.worked(5);
		try {
			for (int i = 0; i < conds.length; i++) {
				String conName = conds[i].getName();
				if (conName.contains("App")) {
					linkComponentGUN(appCompontentsChecked, conds[i]);
				} else if (conName.contains("Iboot")) {
					linkComponentGUN(ibootCompontentsChecked, conds[i]);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			MessageDialog.openError(window.getShell(), "提示", "为" + "修改工程链接失败！" + e.getMessage());
			return false;
		}

		monitor.worked(2);
		try {
			CoreModel.getDefault().setProjectDescription(project, local_prjd);
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}

		monitor.worked(1);
		return true;
	}

	/*
	 * --------------------------1、override的重写函数------------------------------------
	 * ---
	 */
	@Override
	protected Control createContents(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(parent, SWT.NONE);
		initializeDialogUnits(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);
		composite.setLayout(new GridLayout(1, true));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		createDynamicGroup(composite);
		for (TreeItem t : appRequiredItems) {
			handleRequiredDepnds(true, t);
		}
		for (TreeItem t : ibootRequiredItems) {
			handleRequiredDepnds(false, t);
		}
		return composite;
	}

	@Override
	protected void performDefaults() {
		// TODO Auto-generated method stub
		appCompontentsChecked = new ArrayList<Component>();
		ibootCompontentsChecked = new ArrayList<Component>();
		Control[] controls = folder.getChildren();
		for (Control c : controls) {
			Tree tempTree = (Tree) c;
			TreeItem[] fChilds = tempTree.getItems();
			for (TreeItem treeItem : fChilds) {
				if (treeItem.getText().equals("App")) {
					checkOrignalTreeItem(treeItem, appCmpntChecks, true);
				} else if (treeItem.getText().equals("Iboot")) {
					checkOrignalTreeItem(treeItem, ibootCmpntChecks, false);
				}
			}
		}
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		// TODO Auto-generated method stub
		boolean isError = false;
		if (warningMsg != null) {
			isError = true;
			MessageDialog.openError(window.getShell(), "提示", warningMsg);
		} else {
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) {
					monitor.beginTask("配置工程...", 10);
					handleOK(monitor);
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
		}
		return !isError;
	}

	private void createDynamicGroup(Composite composite) {
		// TODO Auto-generated method stub
		IProject project = getProject();
		ReadCheckXml rccx = new ReadCheckXml();
		File appCheckFile = new File(project.getLocation().toString() + "/data/app_component_check.xml");
		File ibootCheckFile = new File(project.getLocation().toString() + "/data/iboot_component_check.xml");
		creatDepedentCpt(composite);// 创建依赖互斥组件显示
		try {
			getBoardAndCpu();
		} catch (Exception e) {
			// TODO: handle exception
			MessageDialog.openError(window.getShell(), "提示", "当前工程的组件、cpu读取错误！");
		}

		if (sBoard == null) {
			MessageDialog.openError(window.getShell(), "提示", "该工程的板件不存在!");
		} else if (onBoardCpu == null) {
			MessageDialog.openError(window.getShell(), "提示", "该工程的Cpu不存在!");
		} else {
			ReadComponent rc = new ReadComponent();
			compontentsList = rc.getComponents(onBoardCpu, sBoard);

			if (appCheckFile.exists()) {
				try {
					appCmpntChecks = rccx.getCmpntChecks(appCheckFile);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				appExist = true;

				initComponent(appCompontents, true);
			}
			if (ibootCheckFile.exists()) {
				try {
					ibootCmpntChecks = rccx.getCmpntChecks(ibootCheckFile);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				ibootExist = true;
				initComponent(ibootCompontents, false);
			}

			SashForm sashForm = new SashForm(composite, SWT.VERTICAL);
			sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
			// 组件显示界面
			folder = new TabFolder(sashForm, SWT.NONE);
			folder.setLayout(new TabFolderLayout());
			folder.setLayoutData(new GridData(GridData.FILL_BOTH));

			TabItem item = new TabItem(folder, SWT.NONE);
			item.setText("核心组件"); //$NON-NLS-1$
			item.setControl(createTabContent(folder, appCoreComponents, ibootCoreComponents));

			item = new TabItem(folder, SWT.NONE);
			item.setText("bsp组件"); //$NON-NLS-1$
			item.setControl(createTabContent(folder, appBspComponents, ibootBspComponents));

			item = new TabItem(folder, SWT.NONE);
			item.setText("第三方组件"); //$NON-NLS-1$
			item.setControl(createTabContent(folder, appThirdComponents, ibootThirdComponents));

			item = new TabItem(folder, SWT.NONE);
			item.setText("用户组件"); //$NON-NLS-1$
			item.setControl(createTabContent(folder, appUserComponents, ibootUserComponents));

			Control[] controls = folder.getChildren();
			Tree coreTree = (Tree) controls[0];
			TreeItem[] coreItems = coreTree.getItems();
			configGroup = ControlFactory.createGroup(sashForm, "组件配置[请选中要配置的组件]", 1);
			configGroup.setLayout(new GridLayout(1, false));
			GridData groupData = new GridData(GridData.FILL_BOTH);
			groupData.grabExcessHorizontalSpace = true;
			configGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
			creatConfigTable(configGroup);
			table.setEnabled(false);

			sashForm.setWeights(new int[] { 1, 1 });// 内部容器之间宽度比例
		}

	}

	/*
	 * ------------------------2、创建依赖组件界面-------------------------------------------
	 * ----
	 */
	private void creatDepedentCpt(Composite composite) {
		// TODO Auto-generated method stub
		Composite depedentCpt = new Composite(composite, SWT.BORDER);
		depedentCpt.setLayout(new GridLayout(2, true));
		depedentCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_CENTER));
		// 显示依赖组件
		dependentText = new Text(depedentCpt, SWT.MULTI | SWT.WRAP);
		GridData depedentData = new GridData(GridData.FILL_BOTH);
		depedentData.grabExcessHorizontalSpace = true;
		dependentText.setLayoutData(depedentData);
		dependentText.setText(depedentLabel);
		dependentText.setEditable(false);
		dependentText.setSize(SWT.HORIZONTAL, 50);
		// 显示互斥组件
		mutexText = new Text(depedentCpt, SWT.MULTI | SWT.WRAP);
		mutexText.setLayoutData(depedentData);
		mutexText.setText(mutexLabel);
		mutexText.setEditable(false);
	}

	/*
	 * --------------------------3、创建Iboot/App组件树-----------------------------------
	 * ---
	 */
	private Control createTabContent(TabFolder folder, List<Component> appTypeComponents,
			List<Component> ibootTypeComponents) {
		// TODO Auto-generated method stub
		// configGroup

		Tree componentTree = new Tree(folder, SWT.H_SCROLL | SWT.V_SCROLL | SWT.CHECK);
		componentTree.setLayoutData(new GridData(GridData.FILL_BOTH));
		componentTree.setSize(SWT.DEFAULT, 200);

		Menu menu = new Menu(componentTree);
		MenuItem openFileItem = new MenuItem(menu, SWT.PUSH);
		openFileItem.setText("打开文件");
		openFileItem.setImage(DPluginImages.CFG_OPENFILE_VIEW.createImage());
		componentTree.setMenu(menu);

		if (appExist) {
			createTabTargetContent(appTypeComponents, componentTree, true);
		}

		if (ibootExist) {
			createTabTargetContent(ibootTypeComponents, componentTree, false);
		}

		openFileItem.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = componentTree.getSelection();
				if (items.length > 0) {
					String type = getAIType(items[0]);
					Component itemCompt = getComponentByName(items[0].getData().toString(),
							type.equals("App") ? appCompontents : ibootCompontents);

					IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(itemCompt.getParentPath()));
					fileStore = fileStore.getChild(itemCompt.getFileName());
					IFileInfo fetchInfo = fileStore.fetchInfo();
					if (!fetchInfo.isDirectory() && fetchInfo.exists()) {
						IWorkbenchPage page = window.getActivePage();
						try {
							IDE.openEditorOnFileStore(page, fileStore);
						} catch (PartInitException e1) {
							String msg = NLS.bind(IDEWorkbenchMessages.OpenLocalFileAction_message_errorOnOpen,
									fileStore.getName());
							IDEWorkbenchPlugin.log(msg, e1.getStatus());
							MessageDialog.open(MessageDialog.ERROR, window.getShell(),
									IDEWorkbenchMessages.OpenLocalFileAction_title, msg, SWT.SHEET);
						}
					}

				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		// 组件树的选择事件
		componentTree.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				selectChanged = true;
				TreeItem item = (TreeItem) event.item;
				if (item == null) {
					return;
				} else {
					if (item.getText().equals("App") || item.getText().equals("Iboot")) {
						item.setChecked(true);
					}
					Component itemCompt;
					Control[] controls = folder.getChildren();
					String type = getAIType(item);
					if (item.getChecked()) {
						componentTree.setSelection(item);
						// 判断当前选中组件与已选中组件是否有互斥，如果没有互斥则处理组件依赖
						if (type != null) {
							boolean isApp;
							if (type.equals("App")) {
								isApp = true;
								appConfigureChanged = true;
							} else {
								isApp = false;
								ibootConfigureChanged = true;
							}
							itemCompt = getComponentByName(item.getData().toString(),
									isApp ? appCompontents : ibootCompontents);
							for (Control c : controls) {
								Tree tempTree = (Tree) c;
								TreeItem[] fChilds = tempTree.getItems();
								for (TreeItem treeItem : fChilds) {
									if (treeItem.getText().equals(type)) {
										boolean isMutex = travelItems_Mutex(treeItem, itemCompt, item);
										if (!isMutex) {
											List<String> visitedComp = new ArrayList<String>();
											travelItems_Depedent(treeItem, itemCompt, isApp, visitedComp);
											itemCompt.setSelect(true);
											if (isApp) {
												if (!appCompontentsChecked.contains(itemCompt)) {
													appCompontentsChecked.add(itemCompt);
												}
											} else {
												if (!ibootCompontentsChecked.contains(itemCompt)) {
													ibootCompontentsChecked.add(itemCompt);
												}
											}
										}
										break;
									}
								}
							}
						}
					} else {
						// 取消选中当前组件时，判断已选中组件是否依赖于此组件，如果有依赖，则不允许取消并提示
						if (type != null) {
							boolean isApp;
							if (type.equals("App")) {
								isApp = true;
								appConfigureChanged = true;
							} else {
								isApp = false;
								ibootConfigureChanged = true;
							}
							itemCompt = getComponentByName(item.getData().toString(),
									isApp ? appCompontents : ibootCompontents);
							if (itemCompt.getSelectable().equals("required")
									|| itemCompt.getSelectable().equals("必选组件")) {
								item.setChecked(true);
								MessageDialog.openError(window.getShell(), "提示", "该组件为必选组件，不可取消！");
							} else {
								for (Control c : controls) {
									Tree tempTree = (Tree) c;
									TreeItem[] fChilds = tempTree.getItems();
									for (TreeItem treeItem : fChilds) {
										if (treeItem.getText().equals(type)) {
											boolean isDepedent = isDepedent(treeItem, item, type, itemCompt, isApp);
											if (isDepedent) {
												if (isApp) {
													appCompontentsChecked.remove(itemCompt);
												} else {
													ibootCompontentsChecked.remove(itemCompt);
												}
												itemCompt.setSelect(false);
											}
											break;
										}
									}
								}
							}

						}
					}
				}
			}
		});

		componentTree.addMouseMoveListener(new MouseMoveListener() {

			@Override
			public void mouseMove(MouseEvent e) {
				// TODO Auto-generated method stub
				Point point = new Point(e.x, e.y);
				TreeItem item = componentTree.getItem(point);
				if (item != null) {
					if (!item.getText().equals("App") && !item.getText().equals("Iboot")) {
						String descContent = item.getData("anno").toString();
						componentTree.setToolTipText(descContent);
					}
				}
			}
		});

		// 组件树的点击事件
		componentTree.addListener(SWT.MouseDown, new Listener() {

			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub

				// dependentText mutexText
				Point point = new Point(event.x, event.y);
				TreeItem item = componentTree.getItem(point);

				if (item != null) {
					if (item.getText().startsWith("App") || item.getText().startsWith("Iboot")) {
						openFileItem.setEnabled(false);
					} else {
						openFileItem.setEnabled(true);
					}
					for (Control control : tabelControls) {
						control.dispose();
					}
					// partControls
					for (Control control : partControls) {
						control.dispose();
					}
					if (editor != null) {
						editor.dispose();
					}
					if (editor1 != null) {
						editor1.dispose();
					}

					table.removeAll();
					if (!item.getText().equals("App") && !item.getText().equals("Iboot")) {
						String itemText = item.getText();
						String type = getAIType(item);
						boolean isApp;
						Component itemCompt;
						if (type.equals("App")) {
							isApp = true;
						} else {
							isApp = false;
						}
						itemCompt = getComponentByName(item.getData().toString(),
								isApp ? appCompontents : ibootCompontents);
						List<String> depedents = itemCompt.getDependents();
						List<String> mutexs = itemCompt.getMutexs();
						String allDeps = "";
						String allMuts = "";
						// 互斥组件
						for (int k = 0; k < mutexs.size(); k++) {
							allMuts += (k != 0 ? "，" : "") + mutexs.get(k);
						}
						for (int k = 0; k < depedents.size(); k++) {
							allDeps += (k != 0 ? "，" : "") + depedents.get(k);
						}

						if (allDeps.equals("")) {
							dependentText.setText(depedentLabel + " 无");
						} else {
							dependentText.setText(depedentLabel + allDeps);
						}
						if (allMuts.equals("")) {
							mutexText.setText(mutexLabel + " 无");
						} else {
							mutexText.setText(mutexLabel + allMuts);
						}

						String configure = itemCompt.getConfigure();
						if (!configure.contains("#define")) {
							configGroup.setText("组件 [" + itemText + "] 无需配置");
							table.setEnabled(false);
							// item.setForeground(folder.getDisplay().getSystemColor(SWT.COLOR_GRAY));
						} else {
							configGroup.setText(type + "组件 [" + itemText + "] 配置");
							table.setEnabled(true);
						}
						initTable(itemCompt, isApp, item);
					} else {
						configGroup.setText("组件配置[请选择要配置的组件]");
						table.setEnabled(false);
					}
				}
			}
		});

		return componentTree;
	}

	private void createTabTargetContent(List<Component> targetComponents, Tree componentTree, boolean isApp) {
		List<Component> firstList = new ArrayList<Component>();
		for (int i = 0; i < targetComponents.size(); i++) {
			String parentName = targetComponents.get(i).getParent();
			if (!isParentCompExist(targetComponents, parentName)) {
				firstList.add(targetComponents.get(i));
			}
		}

		TreeItem root = new TreeItem(componentTree, 0);
		root.setImage(DPluginImages.CFG_CPMT_OBJ.createImage());
		if (isApp) {
			root.setText("App");
		} else {
			root.setText("Iboot");
		}

		root.setGrayed(true);
		root.setChecked(true);
		for (Component component : firstList) {
			createTreeCompItem(component, root, targetComponents, isApp);
		}
	}

	// 添加子组件
	private void fillItem(TreeItem parentItem, List<Component> targetComponents, TreeItem rootItem, boolean isApp) {
		// TODO Auto-generated method stub
		String itemName = parentItem.getText();
		List<Component> childList = new ArrayList<Component>();
		for (int i = 0; i < targetComponents.size(); i++) {
			if (targetComponents.get(i).getParent().equals(itemName)) {
				childList.add(targetComponents.get(i));
			}
		}
		for (Component child : childList) {
			createTreeCompItem(child, parentItem, targetComponents, isApp);
		}
	}

	private void createTreeCompItem(Component component, TreeItem root, List<Component> targetComponents,
			boolean isApp) {
		TreeItem item;// component,root,targetComponents,isApp
		if (component.getSelectable().equals("required") || component.getSelectable().equals("必选组件")) {
			item = new TreeItem(root, 0);
			item.setChecked(true);
			component.setSelect(true);
			(isApp ? appCompontentsChecked : ibootCompontentsChecked).add(component);
		} else {
			item = new TreeItem(root, 0);
		}
		if (!item.getChecked()) {
			for (CmpntCheck check : isApp ? appCmpntChecks : ibootCmpntChecks) {
				if (check.getCmpntName().equals(component.getName())) {
					// if(component.getName().equals("fatfilesystem")) {
					// System.out.println("fatfilesystem1: "+check.isChecked());
					// }
					if (check.isChecked().equals("true")) {
						item.setChecked(true);
						component.setSelect(true);
						(isApp ? appCompontentsChecked : ibootCompontentsChecked).add(component);
					}
					// if(component.getName().equals("fatfilesystem")) {
					// System.out.println("fatfilesystem2: "+component.isSelect());
					// }
					// for(Component c:appCompontents) {
					// if(c.getName().equals("fatfilesystem")) {
					// System.out.println("fatfilesystem4--------------------------------.:
					// "+c.isSelect());
					// }
					// }
					break;
				}
			}
		}

		boolean pass = dideHelper.checkParameter(component, false, getProject());
		if (pass) {
			item.setImage(DPluginImages.CFG_COMPONENT_OBJ.createImage());
		} else {
			item.setImage(DPluginImages.CFG_COMPTERROR_VIEW.createImage());
		}
		item.setData(component.getParentPath());
		item.setData("anno", component.getAnnotation());
		item.setText(component.getName());

		Component itemCompt = getComponentByName(item.getData().toString(), isApp ? appCompontents : ibootCompontents);
		initConfiguration(itemCompt, false);
		if (component.getSelectable().equals("required") || component.getSelectable().equals("必选组件")) {
			(isApp ? appRequiredItems : ibootRequiredItems).add(item);
		}

		// 如果当前组件有子组件，则添加子组件
		if (haveChildren(component, targetComponents)) {
			fillItem(item, targetComponents, root, isApp);
		}
	}

	/*
	 * ----------------------------4、组件初始化------------------------------------------
	 * -
	 */
	private void initComponent(List<Component> typeCompontents, boolean isApp) {
		// TODO Auto-generated method stub
		for (int i = 0; i < compontentsList.size(); i++) {
			Component component = createNewComponent(compontentsList.get(i));
			typeCompontents.add(component);
		}

		if (isApp) {
			setInitComponents(typeCompontents, appCompontentsInit, appCmpntChecks);
			for (int i = 0; i < typeCompontents.size(); i++) {
				String attribute = typeCompontents.get(i).getAttribute();
				if (attribute.equals("system") || attribute.equals("核心组件")) {
					appCoreComponents.add(typeCompontents.get(i));
					if (!appCompontentsChecked.contains(typeCompontents.get(i))) {
						appCompontentsChecked.add(typeCompontents.get(i));
					}
				} else if (attribute.equals("bsp") || attribute.equals("bsp组件")) {
					appBspComponents.add(typeCompontents.get(i));
				} else if (attribute.equals("third") || attribute.equals("第三方组件")) {
					appThirdComponents.add(typeCompontents.get(i));
				} else if (attribute.equals("user") || attribute.equals("用户组件")) {
					appUserComponents.add(typeCompontents.get(i));
				}
			}
		} else {
			setInitComponents(typeCompontents, ibootCompontentsInit, ibootCmpntChecks);
			for (int i = 0; i < typeCompontents.size(); i++) {
				String attribute = typeCompontents.get(i).getAttribute();
				if (attribute.equals("system") || attribute.equals("核心组件")) {
					ibootCoreComponents.add(typeCompontents.get(i));
					if (!ibootCoreComponents.contains(typeCompontents.get(i))) {
						ibootCoreComponents.add(typeCompontents.get(i));
					}
				} else if (attribute.equals("bsp") || attribute.equals("bsp组件")) {
					ibootBspComponents.add(typeCompontents.get(i));
				} else if (attribute.equals("third") || attribute.equals("第三方组件")) {
					ibootThirdComponents.add(typeCompontents.get(i));
				} else if (attribute.equals("user") || attribute.equals("用户组件")) {
					ibootUserComponents.add(typeCompontents.get(i));
				}
			}
		}

	}

	// 初始化所有组件
	private void setInitComponents(List<Component> components, List<Component> componentsInit,
			List<CmpntCheck> cmpntChecks) {
		// TODO Auto-generated method stub
		List<String> checkNames = getChecks(cmpntChecks);
		for (int i = 0; i < components.size(); i++) {
			Component component = createNewComponent(compontentsList.get(i));
			if (!checkNames.contains(component.getName())) {
				component.setSelect(false);
			}
			// 当组件为必选且不需要配置时，不显示在界面上
			componentsInit.add(component);
		}
	}

	// 初始化当前组件的Configure
	private void initConfiguration(Component component, boolean isApp) {
		// TODO Auto-generated method stub
		String compName = component.getName();
		IProject curProject = getProject();
		List<String> pjCgfs = new ArrayList<String>();

		File configFile = new File(curProject.getLocation().toString() + "/src/" + (isApp ? "app" : "iboot")
				+ "/OS_prjcfg/project_config.h");
		FileReader reader;
		try {
			reader = new FileReader(configFile.getPath());
			BufferedReader br = new BufferedReader(reader);
			String str = null;
			boolean start = false, stop = false;
			while ((str = br.readLine()) != null) {
				if (start && str.contains("Configure")) {
					stop = true;
					break;
				}
				if (start && !stop) {
					pjCgfs.add(str);// 添加当前组件的所有预定义值
				}

				String[] strs = str.split("\\s+");
				List<String> strsList = Arrays.asList(strs);
				if (strsList.contains("Configure") && strsList.contains(compName)) {
					start = true;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] parametersDefined = component.getConfigure().split("\n");
		if (pjCgfs.size() > 0) {
			initHeaderConfigure(component, pjCgfs);
		} else {
			if (component.getTarget().equals(ConfigureTarget.CMDLINE.getName())) {
				String newConfig = "";
				final ICProjectDescription local_prjd = CoreModel.getDefault().getProjectDescription(curProject);
				ICConfigurationDescription[] conds = local_prjd.getConfigurations(); // 获取工程的所有Configuration
				for (int i = 0; i < parametersDefined.length; i++) {
					String parameter = parametersDefined[i];
					if (parameter.startsWith("//")) {
						boolean symolsExist = false;
						for (ICConfigurationDescription cond : conds) {
							ICLanguageSetting[] languageSettings = linkHelper
									.getLangSetting(cond.getRootFolderDescription());
							ICLanguageSetting lang = languageSettings[1];
							List<ICLanguageSettingEntry> entries = lang.getSettingEntriesList(ICSettingEntry.MACRO);
							if (isApp && cond.getName().contains("App")) {
								for (ICLanguageSettingEntry entry : entries) {
									if (parameter.contains(entry.getName())) {
										symolsExist = true;
										break;
									}
								}
							} else if (!isApp && cond.getName().contains("Iboot")) {
								for (ICLanguageSettingEntry entry : entries) {
									if (parameter.contains(entry.getName())) {
										symolsExist = true;
										break;
									}
								}
							}
							if (symolsExist) {
								parametersDefined[i] = parameter.replaceFirst("//", "");
								break;
							}
						}
					}
					newConfig += parametersDefined[i] + "\n";
				}
				component.setConfigure(newConfig);
			}
		}
	}

	/*
	 * --------------------------------5、 配置表界面--------------------------
	 */
	private void creatConfigTable(Composite parent) {
		table = new Table(parent, SWT.NONE | SWT.FULL_SELECTION | SWT.H_SCROLL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.minimumHeight = 120;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessHorizontalSpace = true;
		table.setLayoutData(gd);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		// 创建表头的字符串数组
		for (int i = 0; i < tableHeader.length; i++) {
			TableColumn tableColumn = new TableColumn(table, SWT.NONE | SWT.CENTER);
			tableColumn.setAlignment(SWT.LEFT);
			tableColumn.setText(tableHeader[i]);

			// 设置表头可移动，默认为false
			tableColumn.setMoveable(true);
			int tWidth = table.getBorderWidth();
			if (i == tableHeader.length - 1) {
				tableColumn.setWidth(200);
			} else {
				tableColumn.setWidth(150);
			}
		}
	}

	private String setItemText(String configure, String[] members, List<String> pjCgfs, String dataString,
			TableItem item, String realComptName, String[] defines, String annoation) {
		// TODO Auto-generated method stub
		if (pjCgfs.size() > 0) {
			boolean itemFilled = false;
			for (String cfg : pjCgfs) {
				String[] cdefines = cfg.split("//");
				String[] cfgs = cdefines[0].trim().split("\\s+");
				if (Arrays.asList(cfgs).contains(members[1]) || Arrays.asList(cfgs).contains(realComptName)) {
					if (cfgs.length == 2) {
						item.setText(new String[] { realComptName, "", defines.length > 1 ? annoation : "" });
					} else if (cfgs.length == 3) {

						dataString = cfgs[2].equals("default") ? "" : cfgs[2];
						item.setText(new String[] { realComptName, dataString, cdefines.length > 1 ? annoation : "" });

					} else if (cfgs.length == 4) {

						int begin = cdefines[0].indexOf("\"");
						int end = cdefines[0].lastIndexOf("\"");
						dataString = cdefines[0].substring(begin, end + 1);
						item.setText(new String[] { realComptName, dataString, cdefines.length > 1 ? annoation : "" });

					}
					itemFilled = true;
					break;

				}
			}
			if (!itemFilled && Arrays.asList(members).contains("#define")) {
				item.setText(new String[] { realComptName, "", defines.length > 1 ? annoation : "" });
			}

		} else {
			if (members.length == 2) {
				String[] parametersDefined = configure.split("\n");
				boolean valueExisted = false;
				for (int i = 0; i < parametersDefined.length; i++) {
					String parameter = parametersDefined[i];
					String[] paras = parameter.split("//")[0].trim().split("\\s+");
					if (Arrays.asList(paras).contains(realComptName)) {
						valueExisted = true;
						item.setText(new String[] { realComptName, paras.length > 2 ? paras[2] : "",
								defines.length > 1 ? annoation : "" });
						break;
					}
				}
				if (!valueExisted) {
					item.setText(new String[] { realComptName, "", defines.length > 1 ? annoation : "" });
				}
			} else if (members.length == 3) {
				dataString = members[2].equals("default") ? "" : members[2];
				item.setText(new String[] { realComptName, dataString, defines.length > 1 ? annoation : "" });
			} else if (members.length == 4) {
				int begin = defines[0].indexOf("\"");
				int end = defines[0].lastIndexOf("\"");
				dataString = defines[0].substring(begin, end + 1);
				item.setText(new String[] { realComptName, dataString, defines.length > 1 ? annoation : "" });
			}
		}
		return dataString;
	}

	private List<String> getExpandParas(Component componentSelect) {
		List<String> expandParas = new ArrayList<String>();
		for (int j = 0; j < compontentsList.size(); j++) {
			if (componentSelect.getParentPath().equals(compontentsList.get(j).getParentPath())) {
				String configure = compontentsList.get(j).getConfigure();
				String[] parametersDefined = configure.split("\n");
				String tag = null;
				for (int i = 0; i < parametersDefined.length; i++) {
					String parameter = parametersDefined[i];
					if (parameter.contains("%$#@num") || parameter.contains("%$#@string")
							|| parameter.contains("%$#@enum") || parameter.contains("%$#@object_para")
							|| parameter.contains("%$#@select") || parameter.contains("%$#@free")
							|| parameter.contains("%$#@object_num")) {
						tag = dideHelper.getTag(parameter, tag);
					}

					if (parameter.contains("#define") && tag.equals("obj_para")) {
						expandParas.add(parameter);
					}
				}
			}
		}
		return expandParas;
	}

	private void handleRequiredDepnds(boolean isApp, TreeItem item) {
		// TODO Auto-generated method stub
		String type = isApp ? "App" : "Iboot";
		Component itemCompt = getComponentByName(item.getData().toString(), isApp ? appCompontents : ibootCompontents);
		;

		Control[] controls = folder.getChildren();
		for (Control c : controls) {
			Tree tempTree = (Tree) c;
			TreeItem[] fChilds = tempTree.getItems();
			for (TreeItem treeItem : fChilds) {
				if (treeItem.getText().equals(type)) {
					List<String> visitedComp = new ArrayList<String>();
					travelItems_Depedent(treeItem, itemCompt, isApp, visitedComp);
					itemCompt.setSelect(true);
					if (isApp) {
						if (!appCompontentsChecked.contains(itemCompt)) {
							appCompontentsChecked.add(itemCompt);
						}
					} else {
						if (!ibootCompontentsChecked.contains(itemCompt)) {
							ibootCompontentsChecked.add(itemCompt);
						}
					}

					break;
				}
			}
		}
	}

	// 创建配置列表
	private void initTable(Component componentSelect, boolean isApp, TreeItem eventItem) {
		tabelControls.clear();
		checkcounter = 0;
		String compName = componentSelect.getName();
		IProject curProject = getProject();
		List<String> pjCgfs = new ArrayList<String>();
		if (!comptVisited.contains(compName)) {
			File configFile = new File(curProject.getLocation().toString() + "/src/" + (isApp ? "app" : "iboot")
					+ "/OS_prjcfg/project_config.h");
			getPrjCfgs(pjCgfs, configFile, compName);
		}

		int partNum = 0;
		boolean selectExist = false;
		String tag = null, configure = componentSelect.getConfigure();
		String[] infos = null, parametersDefined = configure.split("\n");
		List<String> ranges = null, paras = new ArrayList<String>(), expendParas = new ArrayList<String>();
		boolean[] isSelect = new boolean[parametersDefined.length];

		Button checkBtn[] = new Button[parametersDefined.length];
		for (int i = 0; i < parametersDefined.length; i++) {
			isSelect[i] = false;
			String parameter = null;
			// if (parametersDefined[i].contains("*")) {
			// parameter = parametersDefined[i].trim().split("\\*")[0];
			// } else {
			// parameter = parametersDefined[i].trim();
			// }
			parameter = parametersDefined[i].trim();
			if (parameter.contains("%$#@num") || parameter.contains("%$#@string") || parameter.contains("%$#@enum")
					|| parameter.contains("%$#@object_para") || parameter.contains("%$#@select")
					|| parameter.contains("%$#@free") || parameter.contains("%$#@object_num")) {
				tag = dideHelper.getTag(parameter, tag);
				infos = parameter.split(",|，");
				ranges = new ArrayList<String>();
				if (tag.equals("int") || tag.equals("string") || tag.equals("select") || tag.equals("obj_num")
						|| tag.equals("enum")) {
					// if (!tag.equals("select") && !tag.equals("free")) {
					for (int j = 1; j < infos.length; j++) {
						ranges.add(infos[j].trim());
					}
				}

			} else if (parameter.contains("#define") && !tag.equals("obj_para")) {
				TableItem item = new TableItem(table, SWT.NONE);
				Image image = new Image(PlatformUI.getWorkbench().getDisplay(), 1, 25);
				item.setImage(image);
				String[] defines = parameter.trim().split("//"), members = null;
				String annoation = null;
				if (parameter.startsWith("//")) {
					members = defines[1].trim().split("\\s+");
					annoation = defines.length > 2 ? defines[2] : "";
				} else {
					members = defines[0].trim().split("\\s+");
					annoation = defines.length > 1 ? defines[1] : "";
				}
				String dataString = null;
				String[] annos = annoation.split(",|，");
				if (annos[0].trim().startsWith("\"") && annos[0].trim().endsWith("\"")) {
					annoation = annoation.substring(annos[0].length(), annoation.length()).replaceFirst(",|，", "");
				}
				String realComptName = getRealCompName(annos[0].trim(), members, ranges, tag, paras);
				dataString = setItemText(configure, members, pjCgfs, dataString, item, realComptName, defines,
						annoation);
				editor = new TableEditor(table);
				editor.grabHorizontal = true;// 设置编辑单元格水平填充
				editor1 = new TableEditor(table);
				editor1.grabHorizontal = true;// 设置编辑单元格水平填充

				if (tag.equals("enum")) {
					handleEnumPara(i, isSelect, ranges, item, isApp, compName, componentSelect, tag);
				} else if (tag.equals("obj_num")) {
					handleObjnumPara(i, pjCgfs, checkBtn, curProject, parameter, ranges, isSelect, isApp,
							componentSelect, compName, item, members, tag);
					partNum = Integer.parseInt(members[2]);
				} else if (tag.equals("select")) {
					selectExist = true;
					handleSelectPara(i, checkBtn, curProject, parameter, ranges, isSelect, isApp, componentSelect,
							compName, item, tag);
				} else {
					isSelect[i] = true;
					// 创建一个文本框，用于输入文字
					Text text = new Text(table, SWT.BORDER);
					text.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));

					String flag = tag;
					List<String> rangesCopy = ranges;
					if (rangesCopy.size() > 0) {
						String minString = rangesCopy.get(0);
						String maxString = rangesCopy.get(1);
						if (tag.equals("int")) {
							text.setText(dideHelper.getridParentheses(item.getText(1)));
							handleIntPara(minString, maxString, dataString, members, text);
						} else if (tag.equals("string")) {
							if (item.getText(1).replace("\"", "").trim().equals("")) {
								text.setMessage("字符串,以 \" 开头结尾");
							} else {
								text.setText(item.getText(1));
							}
							handleStringPara(minString, maxString, pjCgfs, members, text);
						} else {
							text.setText(item.getText(1));
						}
					} else {
						text.setText(item.getText(1));
					}
					// 关键方法，将编辑单元格与文本框绑定到表格的第一列
					editor.setEditor(text, item, 1);
					tabelControls.add(text);
					text.addModifyListener(new ModifyListener() {
						public void modifyText(ModifyEvent e) {
							configureChanged = true;
							if (isApp) {
								appConfigureChanged = true;
							} else {
								ibootConfigureChanged = true;
							}
							String tempString = text.getText();
							boolean toCalculate = false;
							IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
							if (rangesCopy.size() > 0) {
								if (flag.equals("int")) {
									String minString = rangesCopy.get(0);
									String maxString = rangesCopy.get(1);
									double min;
									long max;
									if (minString.startsWith("0x")) {
										min = Integer.parseInt(minString.substring(2), 16);
									} else {
										min = Integer.parseInt(minString);
									}
									if (maxString.startsWith("0x")) {
										max = Long.parseLong(maxString.substring(2), 16);
									} else {
										max = Long.parseLong(maxString);
									}
									long curNum = -1;
									Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
									boolean isInt = pattern.matcher(tempString).matches();
									if (tempString.startsWith("0x")) {
										curNum = Long.parseLong(tempString.substring(2), 16);
									} else if (tempString.contains("+") || tempString.contains("-")
											|| tempString.contains("*") || tempString.contains("/")) {
										toCalculate = true;
										String pureCal = dideHelper.getridParentheses(tempString);
										double result = Calculator.conversion(pureCal);
										BigDecimal bd = new BigDecimal(df.format(result));
										curNum = Long.valueOf(bd.toPlainString());
									} else {
										if (isInt) {
											curNum = Integer.parseInt(dideHelper.getridParentheses(tempString));
										}
									}
									if (curNum < min || curNum > max) {
										text.setText("");
										MessageDialog.openError(window.getShell(), "提示",
												"请填写在之" + min + "与" + max + "之间的整数");
									}
								} else if (flag.equals("string")) {
									if (rangesCopy.size() > 0) {
										int min = Integer.parseInt(rangesCopy.get(0));
										int max = Integer.parseInt(rangesCopy.get(1));
										if (tempString.length() < min || tempString.length() > max) {
											text.setText("");
											MessageDialog.openError(window.getShell(), "提示",
													"字符串长度不得小于" + min + "或者大于" + max);
										}
									}

								}
							}
							if (text.getForeground().equals(table.getDisplay().getSystemColor(SWT.COLOR_RED))) {
								text.setForeground(table.getDisplay().getSystemColor(SWT.COLOR_BLACK));
								// boolean isRight = true;
								// TableItem[] tableItems = table.getItems();
								// for (TableItem tableItem : tableItems) {
								//
								// if (tableItem.getForeground(1)
								// .equals(table.getDisplay().getSystemColor(SWT.COLOR_RED))) {
								// isRight = false;
								// break;
								// }
								// }
								// if (isRight) {
								// eventItem.setImage(CPluginImages.CFG_COMPONENT_OBJ.createImage());
								// }
							}

							comptVisited.add(compName);
							if (toCalculate) {
								item.setText(1, "(" + text.getText() + ")");
							} else {
								item.setText(1, text.getText());
							}
							resetConfigure(componentSelect, isSelect);
						}

					});
				}
				Text annoText = new Text(table, SWT.BORDER);
				annoText.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
				annoText.setText(item.getText(2));
				editor1.setEditor(annoText, item, 2);
				tabelControls.add(annoText);
				String flag = tag;
				annoText.addModifyListener(new ModifyListener() {

					@Override
					public void modifyText(ModifyEvent e) {
						// TODO Auto-generated method stub
						configureChanged = true;
						if (isApp) {
							appConfigureChanged = true;
						} else {
							ibootConfigureChanged = true;
						}
						String anno = annoText.getText();
						item.setText(2, anno);
						resetConfigure(componentSelect, isSelect);
					}
				});

			} else if (parameter.contains("#define") && tag.equals("obj_para")) {
				isSelect[i] = true;
				// expendParas.add(parameter);
			}
		}

		if (selectExist) {
			checkError(componentSelect, checkcounter);
		}
		expendParas = getExpandParas(componentSelect);
		fillParts(pjCgfs, partNum, expendParas, componentSelect, isSelect);
		resetConfigure(componentSelect, isSelect);
	}

	private void handleObjnumPara(int index, List<String> pjCgfs, Button[] checkBtn, IProject curProject,
			String parameter, List<String> ranges, boolean[] isSelect, boolean isApp, Component componentSelect,
			String compName, TableItem item, String[] members, String tag) {
		// TODO Auto-generated method stub
		isSelect[index] = true;
		String defaultValue = members[2];
		Combo combo = new Combo(table, SWT.READ_ONLY);
		combo.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
		if (ranges != null) {
			if (ranges.contains("..")) {
				int min = Integer.parseInt(ranges.get(0)), max = Integer.parseInt(ranges.get(ranges.size() - 1));
				List<String> strs = new ArrayList<String>();
				for (int i = min; i <= max; i++) {
					strs.add(String.valueOf(i));
				}
				combo.setItems(strs.toArray(new String[strs.size()]));
				for (int i = 0; i < strs.size(); i++) {
					if (strs.get(i).equals(defaultValue)) {
						combo.select(i);
						break;
					}
				}
			} else {
				combo.setItems(ranges.toArray(new String[ranges.size()]));
				for (int i = 0; i < ranges.size(); i++) {
					if (ranges.get(i).equals(defaultValue)) {
						combo.select(i);
						break;
					}
				}
			}
			int defaultIndex = Integer.parseInt(defaultValue);
			for (int i = 0; i < defaultIndex; i++) {

			}
		}
		int initCount = table.getItemCount();
		editor.setEditor(combo, item, 1);
		tabelControls.add(combo);

		combo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int partCount = Integer.parseInt(combo.getText());
				int itemCount = table.getItemCount();
				if (initCount != itemCount) {
					table.remove(initCount, itemCount - 1);
				}
				for (Control control : partControls) {
					control.dispose();
				}
				List<String> expendParas = getExpandParas(componentSelect);
				fillParts(pjCgfs, partCount, expendParas, componentSelect, isSelect);
				item.setText(1, combo.getText());
				resetConfigure(componentSelect, isSelect);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
	}

	protected void fillParts(List<String> pjCgfs, int partCount, List<String> expendParas, Component componentSelect,
			boolean[] isSelect) {
		// TODO Auto-generated method stub
		for (int i = 0; i < partCount; i++) {
			for (String para : expendParas) {
				TableItem item = new TableItem(table, SWT.NONE);
				Image image = new Image(PlatformUI.getWorkbench().getDisplay(), 1, 25);
				item.setImage(image);
				editor = new TableEditor(table);
				editor.grabHorizontal = true;// 设置编辑单元格水平填充

				String[] defines = para.trim().split("//"), members = null;
				String annoation = null;
				if (para.startsWith("//")) {
					members = defines[1].trim().split("\\s+");
					annoation = defines.length > 2 ? defines[2] : "";
				} else {
					members = defines[0].trim().split("\\s+");
					annoation = defines.length > 1 ? defines[1] : "";
				}

				String dataString = null;
				String[] annos = annoation.split(",|，");
				if (annos[0].trim().startsWith("\"") && annos[0].trim().endsWith("\"")) {
					annoation = annoation.substring(annos[0].length(), annoation.length()).replaceFirst(",|，", "");
				}
				String realComptName = null;
				if (annoation.startsWith("\"") && annoation.endsWith("\"")) {
					if (!annoation.contains("name")) {
						realComptName = annoation.trim().replaceAll("\"", "");
					} else {
						realComptName = members[1];
					}

				} else {
					realComptName = members[1];
				}
				boolean isNFSelect = isInteger(annoation.charAt(0));
				setItemText(componentSelect.getConfigure(), members, pjCgfs, dataString, item, realComptName + "_" + i,
						defines, isNFSelect ? annoation.replace("0", String.valueOf(i)) : i + annoation.trim());
				Text text = new Text(table, SWT.BORDER);
				text.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
				// 将文本框当前值，设置为表格中的值
				text.setText(dideHelper.getridParentheses(item.getText(1)));
				text.addModifyListener(new ModifyListener() {

					@Override
					public void modifyText(ModifyEvent e) {
						// TODO Auto-generated method stub
						item.setText(1, text.getText());
						resetConfigure(componentSelect, isSelect);
					}
				});
				editor.setEditor(text, item, 1);
				partControls.add(text);
			}
		}
	}

	// 处理String类型的参数
	private void handleStringPara(String minString, String maxString, List<String> pjCgfs, String[] members,
			Text text) {
		// TODO Auto-generated method stub
		int min, max, stringLength = -1;
		min = Integer.parseInt(minString);
		max = Integer.parseInt(maxString);
		if (pjCgfs.size() > 0) {// 如果已存在该组件的配置
			for (String cfg : pjCgfs) {
				if (cfg.contains(members[1])) {
					String[] cfgs = cfg.trim().split("\\s+");
					stringLength = cfgs[2].replace("\"", "").length();
					break;
				}
			}

		} else {
			stringLength = members[2].replace("\"", "").length();
		}
		if (stringLength < min || stringLength > max) {
			text.setForeground(table.getDisplay().getSystemColor(SWT.COLOR_RED));
		}
	}

	// 处理Int类型的参数
	private void handleIntPara(String minString, String maxString, String dataString, String[] members, Text text) {
		// TODO Auto-generated method stub
		int min;
		long max, curData;
		if (minString.startsWith("0x")) {
			min = Integer.parseInt(minString.substring(2), 16);
		} else {
			min = Integer.parseInt(minString);
		}
		if (maxString.startsWith("0x")) {
			max = Long.parseLong(maxString.substring(2), 16);
		} else {
			max = Long.parseLong(maxString);
		}

		if (dataString.startsWith("0x")) {
			curData = Long.parseLong(dataString.substring(2), 16);
		} else if (members[2].contains("+") || members[2].contains("-") || members[2].contains("*")
				|| members[2].contains("/")) {
			String pureCal = dideHelper.getridParentheses(members[2]);
			if (pureCal.startsWith("-")) {
				curData = dideHelper.toUnsigned(Long.parseLong(pureCal));
			} else {
				double result = Calculator.conversion(pureCal);
				BigDecimal bd = new BigDecimal(df.format(result));
				curData = Long.valueOf(bd.toPlainString());
			}
		} else {
			curData = Integer.parseInt(dideHelper.getridParentheses(dataString));
		}
		if (curData < min || curData > max) {
			text.setForeground(table.getDisplay().getSystemColor(SWT.COLOR_RED));
		}
	}

	// 处理Select类型的参数
	private void handleSelectPara(int index, Button[] checkBtn, IProject curProject, String parameter,
			List<String> ranges, boolean[] isSelect, boolean isApp, Component componentSelect, String compName,
			TableItem item, String tag) {//
		// TODO Auto-generated method stub
		int cur = index;
		checkBtn[index] = new Button(table, SWT.CHECK);
		editor.setEditor(checkBtn[index], item, 1);
		boolean symolsExist = false;
		final ICProjectDescription local_prjd = CoreModel.getDefault().getProjectDescription(curProject);
		ICConfigurationDescription[] conds = local_prjd.getConfigurations(); // 获取工程的所有Configuration

		for (int m = 0; m < conds.length; m++) {
			ICConfigurationDescription cond = conds[m];
			if (isApp) {
				if (cond.getName().contains("libos_App")) {
					symolsExist = isSymbolExist(cond, parameter);
				}
			} else {
				if (cond.getName().contains("libos_Iboot")) {
					symolsExist = isSymbolExist(cond, parameter);
				}
			}

		}

		int maxSelect = 0, rangeSize = ranges.size();
		if (rangeSize > 0) {
			maxSelect = Integer.parseInt(ranges.get(0));// 获取最多可以选择的符号个数
		}
		int chkMaxNum = maxSelect;

		if (symolsExist) {
			isSelect[index] = true;
			checkcounter += 1;
			if (maxSelect == 1) {
				lastchk = index;
			}
			checkBtn[index].setSelection(true);
		} else {
			isSelect[index] = false;
			checkBtn[index].setSelection(false);
		}
		tabelControls.add(checkBtn[index]);

		checkBtn[index].addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				configureChanged = true;
				if (isApp) {
					appConfigureChanged = true;
				} else {
					ibootConfigureChanged = true;
				}
				boolean checked = checkBtn[cur].getSelection();
				if (checked) {
					if (rangeSize > 0) {
						checkcounter += 1;
						if ((checkcounter > chkMaxNum)) {
							if (chkMaxNum > 1) {
								checkcounter = chkMaxNum;
								checkBtn[cur].setSelection(false);
								MessageDialog.openError(window.getShell(), "提示", "不得勾选多于" + chkMaxNum + "项");
							} else if (chkMaxNum == 1) {
								checkcounter = chkMaxNum;
								checkBtn[lastchk].setSelection(false);
								isSelect[lastchk] = false;
								isSelect[cur] = true;
							}
						} else {
							isSelect[cur] = true;
						}
						lastchk = cur;
					}

				} else {
					isSelect[cur] = false;
					if (rangeSize > 0) {
						checkcounter -= 1;
						if (checkcounter < 0) {
							checkcounter = 0;
						}
					}
				}
				checkError(componentSelect, checkcounter);
				comptVisited.add(compName);
				// 重置组件的配置
				resetConfigure(componentSelect, isSelect);
				System.out.println("checkcounter:  " + checkcounter);
			}
		});

	}

	private void checkError(Component componentSelect, int num) {
		if (num < 1) {
			warningMsg = componentSelect.getAttribute() + "[" + componentSelect.getName() + "]未勾选参数";
		} else {
			warningMsg = null;
		}
	}

	// 处理Enum类型的参数
	private void handleEnumPara(int index, boolean[] isSelect, List<String> ranges, TableItem item, boolean isApp,
			String compName, Component componentSelect, String tag) {
		// TODO Auto-generated method stub
		isSelect[index] = true;
		Combo combo = new Combo(table, SWT.READ_ONLY);
		combo.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
		if (ranges != null) {
			combo.setItems(ranges.toArray(new String[ranges.size()]));
		}
		for (int j = 0; j < ranges.size(); j++) {
			if (ranges.get(j).equals(item.getText(1))) {
				combo.select(j);
				break;
			}
		}
		editor.setEditor(combo, item, 1);
		tabelControls.add(combo);
		combo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				configureChanged = true;
				if (isApp) {
					appConfigureChanged = true;
				} else {
					ibootConfigureChanged = true;
				}
				item.setText(1, combo.getText());
				comptVisited.add(compName);
				resetConfigure(componentSelect, isSelect);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
	}

	// 获取显示在界面上的参数名
	private String getRealCompName(String fAnno, String[] members, List<String> ranges, String tag,
			List<String> paras) {
		// TODO Auto-generated method stub
		String realComptName = null;
		if (fAnno.startsWith("\"") && fAnno.endsWith("\"")) {
			if (!fAnno.contains("name")) {
				realComptName = fAnno.trim().replaceAll("\"", "");
			} else {
				realComptName = members[1];
			}

		} else {
			realComptName = members[1];
		}

		if (paras.contains(members[1])) {
			realComptName = "（参数重名）" + realComptName;
		}
		paras.add(members[1]);// 添加当前的参数名到paras

		if (tag.equals("int") && ranges.size() > 0) {
			String min = ranges.get(0);
			String max = ranges.get(1);
			realComptName = realComptName + "( " + min + "~" + max + " )";
		} else if (tag.equals("string") && ranges.size() > 0) {
			String min = ranges.get(0);
			String max = ranges.get(1);
			realComptName = realComptName + "(长度：" + min + "~" + max + " )";
		}
		return realComptName;
	}

	/*
	 * ----------------------------------------------6、遍历组件-------------------------
	 * -------
	 */

	// 遍历与App/Iboot有关的组件，如果与当前选中的组件互斥，则返回true
	protected boolean travelItems_Mutex(TreeItem treeItem, Component itemCompt, TreeItem eventTreeItem) {
		// TODO Auto-generated method stub
		boolean isMutx = false;
		List<String> mutexs = itemCompt.getMutexs();
		TreeItem[] items = treeItem.getItems();
		for (TreeItem item : items) {
			if (mutexs.contains(item.getText())) {
				if (item.getChecked()) {
					eventTreeItem.setChecked(false);
					isMutx = true;
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					MessageDialog.openError(window.getShell(), "提示",
							item.getText() + "组件已勾选，与" + itemCompt.getName() + "互斥 ！");
				}
			} else {
				if (item.getItems().length > 0 && !isMutx) {
					isMutx = travelItems_Mutex(item, itemCompt, eventTreeItem);
				}
			}
		}
		return isMutx;
	}

	// 遍历与App/Iboot有关的组件，如果被当前选中的组件依赖，则选中该组件
	protected void travelItems_Depedent(TreeItem treeItem, Component itemCompt, boolean isApp,
			List<String> visitedComp) {
		// TODO Auto-generated method stub
		List<String> depedents = itemCompt.getDependents();
		visitedComp.add(itemCompt.getName());
		TreeItem[] items = treeItem.getItems();
		for (TreeItem item : items) {
			if (depedents.contains(item.getText())) {
				if (!item.getChecked()) {
					item.setChecked(true);
					Component curComp = getComponentByName(item.getData().toString(),
							isApp ? appCompontents : ibootCompontents);
					;
					if (isApp) {
						if (!appCompontentsChecked.contains(curComp)) {
							appCompontentsChecked.add(curComp);
						}
					} else {
						if (!ibootCompontentsChecked.contains(curComp)) {
							ibootCompontentsChecked.add(curComp);
						}
					}
					curComp.setSelect(true);
					Control[] controls = folder.getChildren();
					for (Control c : controls) {
						Tree tempTree = (Tree) c;
						String type = isApp ? "App" : "Iboot";
						TreeItem[] fChilds = tempTree.getItems();
						for (TreeItem t : fChilds) {
							if (t.getText().equals(type)) {
								travelItems_Depedent(t, curComp, isApp, visitedComp);
							}
						}
					}
				}
			}

			travelItems_Depedent(item, itemCompt, isApp, visitedComp);
		}
	}

	// 初始化配置参数的表格
	public void creatProjectConfiure(File file, String coreConfigure, boolean isApp) {
		List<Component> compontentsCheckedSort = null;
		if (isApp) {
			compontentsCheckedSort = appCheckedSort;
		} else {
			compontentsCheckedSort = ibootCheckedSort;
		}
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		defineInit = DjyosMessages.Automatically_Generated;
		defineInit += "#ifndef __PROJECT_CONFFIG_H__\r\n" + "#define __PROJECT_CONFFIG_H__\r\n\n"
				+ "#include \"stdint.h\"\n\n";
		for (int i = 0; i < compontentsCheckedSort.size(); i++) {
			Component c = compontentsCheckedSort.get(i);
			if (c.getTarget().equals(ConfigureTarget.HEADER.getName()) && c.isSelect()) {
				if (c.getConfigure().contains("#define")) {
					defineInit += "//*******************************  Configure " + c.getName()
							+ "  ******************************************//\n";
					String[] configures = c.getConfigure().split("\n");
					for (int j = 0; j < configures.length; j++) {
						if (configures[j].contains("#define")) {
							String pureDefine = null;
							String annoName = null;
							if (configures[j].trim().startsWith("//")) {
								pureDefine = configures[j].replaceFirst("//", "");
							} else {
								pureDefine = configures[j];
							}
							String[] defines = pureDefine.split("//");
							if (defines.length > 1) {
								String[] infos = defines[1].split(",|，");
								if (infos[0].startsWith("\"") && infos[0].endsWith("\"")) {
									annoName = infos[0];
								}
							}
							if (annoName == null) {
								defineInit += configures[j] + "\n";
							} else {
								defineInit += configures[j].replace(annoName, "").replace(",", "").replace("，", "")
										+ "\n";
							}
						}
					}
				}
			}
		}
		defineInit += "//******************************* Core Clock ******************************************//\n";
		defineInit += coreConfigure;
		defineInit += "\n\n#endif";
		dideHelper.writeFile(file, defineInit);
	}

	// 通过依赖关系排序
	private void handleDependents(Component component, List<Component> typeCompontentsChecked,
			List<Component> typeCheckedSort) {
		// TODO Auto-generated method stub
		List<String> dependents = component.getDependents();
		List<String> weakDependents = component.getWeakDependents();

		for (String dep : dependents) {
			for (int j = 0; j < typeCompontentsChecked.size(); j++) {
				Component c = typeCompontentsChecked.get(j);
				if (dep.equals(c.getName())) {
					if (!typeCheckedSort.contains(c)) {
						if (c.getDependents().contains(component.getName())) {
							typeCheckedSort.add(c);
						}
						handleDependents(c, typeCompontentsChecked, typeCheckedSort);
						if (!typeCheckedSort.contains(c)) {
							typeCheckedSort.add(c);
						}
					}
					break;
				}
			}
		}
	}

	/*
	 * ----------------------------------------------
	 * 7、文件操作-----------------------------------
	 */
	private void initHeaderConfigure(Component component, List<String> pjCgfs) {
		String[] parametersDefined = component.getConfigure().split("\n");
		String newConfig = "", tag = null;
		int itemCount = 0;
		try {
			for (int i = 0; i < parametersDefined.length; i++) {
				String parameter = parametersDefined[i];
				if (parameter.contains("%$#@num") || parameter.contains("%$#@string") || parameter.contains("%$#@enum")
						|| parameter.contains("%$#@object_para") || parameter.contains("%$#@select")
						|| parameter.contains("%$#@free") || parameter.contains("%$#@object_num")) {
					tag = dideHelper.getTag(parameter, tag);
				}

				if (parameter.contains("#define") && pjCgfs.size() > itemCount && !tag.equals("obj_para")) {
					String[] pjs = pjCgfs.get(itemCount).split("\\s+");
					String[] params = parameter.split("\\s+");
					if (pjs[1].equals(params[1])) {
						String[] pjDefines = pjCgfs.get(itemCount).split("//");
						String[] paramDefines = parameter.split("//");
						parameter = pjDefines[0] + " // " + paramDefines[1];
						itemCount++;
					}
				}

				newConfig += parameter + "\n";
			}

		} catch (Exception e) {
			// TODO: handle exception
			MessageDialog.openError(window.getShell(), "提示",
					"组件" + component.getName() + "配置信息初始化错误：" + e.getMessage());
		}
		component.setConfigure(newConfig);
	}

	// 创建initPrj.c文件
	public void initProject(String path, boolean isApp) {

		File file = new File(path + (isApp ? "/app" : "/iboot") + "/initPrj.c");
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (isApp) {
			handleInitProject(file, appCompontentsChecked, appCheckedSort);
		} else {
			handleInitProject(file, ibootCompontentsChecked, ibootCheckedSort);
		}

	}

	private void handleInitProject(File file, List<Component> typeCompontentsChecked, List<Component> typeCheckedSort) {
		// TODO Auto-generated method stub
		String content = "", firstInit = "\tuint16_t evtt_main;\n\n", lastInit = "", moduleInit = "", gpioInit = "",
				djyMain = "", shellInit = "";
		String earlyCode = "", mediumCode = "", laterCode = "";
		initHead = DjyosMessages.Automatically_Generated;
		initHead += "#include \"project_config.h\"\n" + "#include \"stdint.h\"\n" + "#include \"stddef.h\"\n"
				+ "#include \"cpu_peri.h\"\n" + "extern ptu32_t djy_main(void);\n";

		for (int i = 0; i < typeCompontentsChecked.size(); i++) {
			handleDependents(typeCompontentsChecked.get(i), typeCompontentsChecked, typeCheckedSort);// 通过依赖关系排序
			if (!typeCheckedSort.contains(typeCompontentsChecked.get(i))) {
				typeCheckedSort.add(typeCompontentsChecked.get(i));
			}
		}

		for (int i = 0; i < typeCheckedSort.size(); i++) {
			if (typeCheckedSort.get(i).isSelect()) {
				String grade = typeCheckedSort.get(i).getGrade();
				String code = typeCheckedSort.get(i).getCode();
				List<String> dependents = typeCheckedSort.get(i).getDependents();

				// 添加分区参数
				String[] configures = typeCheckedSort.get(i).getConfigure().split("\n");
				String tag = null;
				List<String> paraNames = new ArrayList<String>();
				for (String parameter : configures) {
					if (parameter.contains("%$#@num") || parameter.contains("%$#@string")
							|| parameter.contains("%$#@enum") || parameter.contains("%$#@object_para")
							|| parameter.contains("%$#@select") || parameter.contains("%$#@free")
							|| parameter.contains("%$#@object_num")) {
						tag = dideHelper.getTag(parameter, tag);
					}
					String[] members = parameter.split("\\s+");
					if (parameter.contains("#define") && tag.equals("obj_para")) {
						paraNames.add(members[1]);
					}
				}

				String codeStrings = "";
				if (code != null) {
					String[] codes = code.split("\n");
					for (int j = 0; j < codes.length; j++) {
						if (codes[j].contains("#include")) {
							initHead += codes[j].trim() + "\n";
						} else {
							// 如果函包含可变参，则将配置好的参数替换...
							if (codes[j].contains("...") && paraNames.size() > 0) {
								String replaceParas = "";
								for (String name : paraNames) {
									if (name.equals(paraNames.get(paraNames.size() - 1))) {
										replaceParas += name;
									} else {
										replaceParas += name + ", ";
									}

								}
								codes[j] = codes[j].replace("...", replaceParas);
							}

							codeStrings += "\t" + codes[j].trim() + "\n";
						}
					}
				}

				String componentName = typeCheckedSort.get(i).getName();

				if (grade != null && code != null && !codeStrings.trim().equals("")) {
					if (dependents.contains("cpu_peri_gpio") || codeStrings.contains("GpioInit")) {
						gpioInit += codeStrings + "\n";
					} else if (componentName.equals("heap")) {
						lastInit += evttMain + codeStrings + "\n";
					} else if (componentName.equals("shell")) {
						shellInit += codeStrings + "\n";
					} else {
						if (grade.equals("early")) {
							earlyCode += codeStrings + "\n";
						} else if (grade.equals("medium")) {
							mediumCode += codeStrings + "\n";
						} else if (grade.equals("later")) {
							laterCode += codeStrings + "\n";
						}
					}
				}
			}

		}
		lastInit += "\tprintf(\"\\r\\n: info : all modules are configured.\");\r\n"
				+ "\tprintf(\"\\r\\n: info : os starts.\\r\\n\");\n\n";
		content += initHead;
		content += "\n" + djyStart + djyMain + djyEnd;
		content += initStart + firstInit + gpioInit + shellInit
				+ "\t//-------------------early-------------------------//\n" + earlyCode
				+ "\t//-------------------medium-------------------------//\n" + mediumCode
				+ "\t//-------------------later-------------------------//\n" + laterCode + lastInit + initEnd;
		dideHelper.writeFile(file, content);
	}

	private void createComptCheck(String projectLocation, boolean isApp) {
		// 生成component_check.xml文件
		String xmlName = null;
		List<Component> curCompontents = new ArrayList<Component>();
		if (isApp) {
			xmlName = "app_component_check.xml";
			curCompontents = appCompontents;
		} else {
			xmlName = "iboot_component_check.xml";
			curCompontents = ibootCompontents;
		}
		CreateCheckXml ccx = new CreateCheckXml();
		File checkFile = new File(projectLocation + "/data/" + xmlName);
		if (checkFile.exists()) {
			checkFile.delete();
		}
		try {
			checkFile.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ccx.createCheck(curCompontents, checkFile);
	}

	/*
	 * -------------------------------------------------8、工具------------------------
	 * -----------
	 */
	private Component createNewComponent(Component c) {
		Component component = new Component();
		component.setName(c.getName());
		component.setAnnotation(c.getAnnotation());
		component.setAttribute(c.getAttribute());
		component.setGrade(c.getGrade());
		component.setCode(c.getCode());
		component.setConfigure(c.getConfigure());
		component.setLinkFolder(c.getLinkFolder());
		component.setDependents(c.getDependents());
		component.setMutexs(c.getMutexs());
		component.setFileName(c.getFileName());
		component.setSelectable(c.getSelectable());
		component.setParent(c.getParent());
		component.setWeakDependents(c.getWeakDependents());
		component.setExcludes(c.getExcludes());
		component.setIncludes(c.getIncludes());
		component.setSelect(c.isSelect());
		component.setParentPath(c.getParentPath());
		component.setTarget(c.getTarget());
		return component;
	}

	// 通过hardware_info.xml获取当前工程所用到的板件和Cpu
	private void getBoardAndCpu() {
		IProject project = getProject();
		String projectLocation = project.getLocation().toString();
		File hardWardInfoFile = new File(projectLocation + "/data/hardware_info.xml");
		ReadHardWareDesc rhwd = new ReadHardWareDesc();
		List<String> hardwares = rhwd.getHardWares(hardWardInfoFile);
		String cpuName = hardwares.get(1);
		String boardName = hardwares.get(0);

		sBoard = dideHelper.getProjectBoard(boardName);
		if (sBoard != null) {
			List<OnBoardCpu> onBoardCpus = sBoard.getOnBoardCpus();
			for (int i = 0; i < onBoardCpus.size(); i++) {
				if (onBoardCpus.get(i).getCpuName().equals(cpuName)) {
					onBoardCpu = onBoardCpus.get(i);
					break;
				}
			}
		}
	}

	// 通过组件名获取当前组件对象
	private Component getComponentByName(String itemName, List<Component> compontents) {
		for (Component component : compontents) {
			if (component.getParentPath().equals(itemName)) {
				return component;
			}
		}
		return null;
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

	private String getAIType(TreeItem item) {
		TreeItem parentItem = item.getParentItem();
		if (parentItem != null) {
			if (parentItem.getText().equals("App")) {
				return "App";
			} else if (parentItem.getText().equals("Iboot")) {
				return "Iboot";
			} else {
				return getAIType(parentItem);
			}
		} else {
			return null;
		}
	}

	private boolean haveChildren(Component parent, List<Component> componentList) {
		for (Component compt : componentList) {
			if (compt.getParent().equals(parent.getName())) {
				return true;
			}
		}
		return false;
	}

	// 获取projectconfigure.h中的参数配置
	private void getPrjCfgs(List<String> pjCgfs, File configFile, String compName) {
		// TODO Auto-generated method stub
		FileReader reader;
		try {
			reader = new FileReader(configFile.getPath());
			BufferedReader br = new BufferedReader(reader);
			String str = null;
			boolean start = false, stop = false;
			while ((str = br.readLine()) != null) {
				String[] infos = str.split("\\s+");
				if (start && str.contains("Configure")) {
					stop = true;
					break;
				}
				if (start && !stop) {
					pjCgfs.add(str);// 添加当前组件的所有预定义值
				}
				if (str.contains("Configure") && infos[2].equals(compName)) {
					start = true;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 获取被选中的组件
	private List<String> getChecks(List<CmpntCheck> cmpntChecks) {
		// TODO Auto-generated method stub
		List<String> checkNames = new ArrayList<String>();
		for (CmpntCheck check : cmpntChecks) {
			checkNames.add(check.getCmpntName());
		}
		return checkNames;
	}

	/*
	 * ------------------------------------------9、判断函数-----------------------------
	 * ---------
	 */
	private boolean isParentCompExist(List<Component> components, String parentName) {
		for (Component component : components) {
			if (component.getName().equals(parentName)) {
				return true;
			}
		}
		return false;
	}

	protected boolean isDepedent(TreeItem treeItem, TreeItem eventTreeItem, String type, Component itemCompt,
			boolean isApp) {
		// TODO Auto-generated method stub
		TreeItem[] items = treeItem.getItems();
		List<String> eventDepedents = itemCompt.getDependents();
		boolean isDepedent = true;
		for (TreeItem item : items) {
			Component tempCompt = getComponentByName(item.getData().toString(),
					isApp ? appCompontents : ibootCompontents);
			if (item.getChecked()) {
				if (tempCompt.getDependents().contains(eventTreeItem.getText())) {
					if (eventDepedents.contains(tempCompt.getName())) {
						item.setChecked(false);
						if (isApp) {
							if (!appCompontentsChecked.contains(tempCompt)) {
								appCompontentsChecked.remove(tempCompt);
							}
						} else {
							if (!ibootCompontentsChecked.contains(tempCompt)) {
								ibootCompontentsChecked.remove(tempCompt);
							}
						}
					} else {
						eventTreeItem.setChecked(true);
						isDepedent = false;
						IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						MessageDialog.openError(window.getShell(), "提示",
								"该组件被" + tempCompt.getName() + " 等已勾选的组件依赖，不可取消勾选");
					}
				}
			}

			if (item.getItems().length > 0 && isDepedent) {
				isDepedent = isDepedent(item, eventTreeItem, type, itemCompt, isApp);
			}
		}
		return isDepedent;
	}

	// Symbol是否存在
	private boolean isSymbolExist(ICConfigurationDescription cond, String parameter) {
		ICResourceDescription rds = cond.getRootFolderDescription();
		ICLanguageSetting[] languageSettings = linkHelper.getLangSetting(rds);
		for (int n = 0; n < languageSettings.length; n++) {
			if (n == 0) {
				ICLanguageSetting lang = languageSettings[n];
				List<ICLanguageSettingEntry> entries = lang.getSettingEntriesList(ICSettingEntry.MACRO);
				for (ICLanguageSettingEntry entry : entries) {
					if (parameter.contains(entry.getName())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean isInteger(char c) {
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
		return pattern.matcher(String.valueOf(c)).matches();
	}

	@Override
	public boolean isDjyos() {
		// TODO Auto-generated method stub
		if (configureChanged || selectChanged) {
			System.out.println("true");
			return true;
		} else {
			System.out.println("false");
			return false;
		}
		// return revised;
	}

	@Override
	public boolean isPageDragable() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * -------------------------------------------10、Apply--------------------------
	 * --------
	 */
	private boolean handleCheckAndExclude(List<Component> compontentsChecked, List<Component> compontents,
			List<Component> compontentsInit, IProject project, ICConfigurationDescription[] conds, boolean isApp) {
		try {
			createComptCheck(project.getLocation().toString(), isApp);
		} catch (Exception e) {
			// TODO: handle exception
			dideHelper.showErrorMessage("创建" + (isApp ? "app" : "iboot") + "_component_check.xml失败！" + e.getMessage());
			return false;
		}

		try {
			resetExclude(compontentsChecked, compontents, compontentsInit, isApp, conds, project);
		} catch (Exception e) {
			// TODO: handle exception
			dideHelper.showErrorMessage("为" + (isApp ? "app" : "iboot") + "链接组件失败！" + e.getMessage());
			return false;
		}
		return true;
	}

	private boolean handleInitFiles(IProject project, boolean isApp, File file, String coreConfigure) {
		try {
			initProject(project.getLocation().toString() + "/src", isApp);
		} catch (Exception e) {
			// TODO: handle exception
			MessageDialog.openError(window.getShell(), "提示",
					"为" + (isApp ? "app" : "iboot") + "创建initPrj.c失败！" + e.getMessage());
			return false;
		}
		try {
			creatProjectConfiure(file, coreConfigure, isApp);
		} catch (Exception e) {
			// TODO: handle exception
			MessageDialog.openError(window.getShell(), "提示",
					"为" + (isApp ? "app" : "iboot") + "创建project_config.h失败！" + e.getMessage());
			return false;
		}
		return true;
	}

	// 添加组件链接
	protected void linkComponentGUN(List<Component> compontentsChecked, ICConfigurationDescription cond) {
		// TODO Auto-generated method stub
		String srcLocation = didePath + "djysrc";
		List<String> myLinks = new ArrayList<String>();
		List<String> includes = new ArrayList<String>();
		// System.out.println("compontentsChecked.size(): "+compontentsChecked.size());
		for (int i = 0; i < compontentsChecked.size(); i++) {
			Component component = compontentsChecked.get(i);
			// System.out.println("component.getName(): "+component.getName());
			String componentPath = component.getParentPath().replace("\\", "/");
			String relativePath = componentPath.replace(srcLocation, "").replace("\\", "/");
			List<String> includeFiles = component.getIncludes();// includes
			for (String include : includeFiles) {
				includes.add(relativePath + include);
			}
		}
		for (String include : includes) {
			myLinks.add("${DJYOS_SRC_LOCATION}" + include);
		}

		ICLanguageSetting[] languageSettings = linkHelper.getLangSetting(cond.getRootFolderDescription());
		List<ICLanguageSettingEntry> defaultEntries = new ArrayList<ICLanguageSettingEntry>();
		List<ICLanguageSettingEntry> entriesMACROExist = languageSettings[1]
				.getSettingEntriesList(ICSettingEntry.MACRO);
		for (ICLanguageSettingEntry macro : entriesMACROExist) {
			if (macro.getName().contains("DEBUG")) {
				defaultEntries.add(macro);
			}
		}
		ICLanguageSettingEntry[] ents = new ICLanguageSettingEntry[defaultEntries.size()];
		for (int i = 0; i < defaultEntries.size(); i++) {
			ents[i] = defaultEntries.get(i);
		}
		List<ICLanguageSettingEntry> _entries = new ArrayList<ICLanguageSettingEntry>();
		linkHelper.fillSymbols(compontentsChecked, _entries);
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		for (int k = 0; k < myLinks.size(); k++) {
			ICLanguageSettingEntry entry = CDataUtil.createCIncludePathEntry(myLinks.get(k), 0);
			entries.add(entry);
		}

		for (int j = 0; j < languageSettings.length; j++) {
			ICLanguageSetting lang = languageSettings[j];// 获取语言类型
			// 重置MACRO
			linkHelper.changeIt(ICSettingEntry.MACRO, _entries, ents, lang);
			// Assembly添加链接
			if (j == 0) {

			} else {// GNU C/C++ 添加链接
				linkHelper.changeIt(ICSettingEntry.INCLUDE_PATH, entries,
						lang.getSettingEntries(ICSettingEntry.INCLUDE_PATH), lang);
			}

		}

	}

	// 重置文件排除
	private void resetExclude(List<Component> compontentsChecked, List<Component> components,
			List<Component> componentsInit, boolean isApp, ICConfigurationDescription[] conds, IProject project) {
		String srcLocation = dideHelper.getDIDEPath() + "djysrc";
		List<String> excludes = new ArrayList<String>();
		for (int i = 0; i < components.size(); i++) {
			Component comp = components.get(i);
			String componentPath = comp.getParentPath().replace("\\", "/");
			String relativePath = componentPath.replace(srcLocation, "").replace("\\", "/");
			String notExcludeFolder = "src/libos" + relativePath;
			String notExcludeFile = "src/libos" + relativePath + "/" + comp.getFileName();
			List<String> excludeFiles = comp.getExcludes();

			for (String exclude : excludeFiles) {
				excludes.add("src/libos" + relativePath + exclude);
			}

			IFolder ifolder = project.getFolder(notExcludeFolder);
			IFile ifile = project.getFile(notExcludeFile);
			boolean isCoreComp = components.get(i).getAttribute().equals("system");
			if (componentsInit.get(i).isSelect() != components.get(i).isSelect() || isCoreComp) {
				for (int j = 0; j < conds.length; j++) {
					if (isApp) {
						if (conds[j].getName().contains("libos_App")) {
							List<IFolder> includeFolders = new ArrayList<IFolder>();
							linkHelper.getFolders(ifolder, includeFolders);
							// System.out.println("comp: "+comp.getName()+" isSelect:"+comp.isSelect());
							if (comp.isSelect() || isCoreComp) {
								for (int k = includeFolders.size() - 1; k >= 0; k--) {
									linkHelper.setExclude(includeFolders.get(k), conds[j], false);
								}
								if (comp.getFileName().endsWith(".c")) {
									linkHelper.setFileExclude(ifile, conds[j], false);
								}
							} else {
								// if(comp.getFileName().endsWith(".c")) {
								// linkHelper.setFileExclude(ifile, conds[j], true);
								// }else if(comp.getFileName().endsWith(".h")) {
								// linkHelper.setExclude(ifolder, conds[j], true);
								// }
								linkHelper.setExclude(ifolder, conds[j], true);
							}
						}
					} else {
						if (conds[j].getName().contains("libos_Iboot")) {
							List<IFolder> includeFolders = new ArrayList<IFolder>();
							linkHelper.getFolders(ifolder, includeFolders);
							if (comp.isSelect() || isCoreComp) {
								for (int k = includeFolders.size() - 1; k >= 0; k--) {
									linkHelper.setExclude(includeFolders.get(k), conds[j], false);
								}
								if (comp.getFileName().endsWith(".c")) {
									linkHelper.setFileExclude(ifile, conds[j], false);
								}
							} else {
								// if(comp.getFileName().endsWith(".c")) {
								// linkHelper.setFileExclude(ifile, conds[j], true);
								// }else if(comp.getFileName().endsWith(".h")) {
								// linkHelper.setExclude(ifolder, conds[j], true);
								// }
								linkHelper.setExclude(ifolder, conds[j], true);
							}
						}
					}
				}
			}

			// 隐藏不需要编译的文件
			for (int j = 0; j < excludes.size(); j++) {
				IFile ifle = project.getFile(excludes.get(j));
				for (int k = 0; k < conds.length; k++) {
					if (isApp) {
						if (conds[k].getName().contains("libos_App")) {
							linkHelper.setFileExclude(ifle, conds[k], true);
						}
					} else {
						if (conds[k].getName().contains("libos_Iboot")) {
							linkHelper.setFileExclude(ifle, conds[k], true);
						}
					}
				}
			}

		}
	}

	/*
	 * --------------------------------------------11、Default-----------------------
	 * ----------------
	 */
	private void checkOrignalTreeItem(TreeItem treeItem, List<CmpntCheck> cmpntChecks, boolean isApp) {
		// TODO Auto-generated method stub
		TreeItem[] childItems = treeItem.getItems();
		for (TreeItem item : childItems) {
			for (CmpntCheck check : cmpntChecks) {
				if (check.getCmpntName().equals(item.getText())) {
					if (check.isChecked().equals("true")) {
						item.setChecked(true);
						Component curComponent = getComponentByName(item.getData().toString(),
								isApp ? appCompontents : ibootCompontents);
						(isApp ? appCompontentsChecked : ibootCompontentsChecked).add(curComponent);
					} else {
						item.setChecked(false);
					}
					break;
				}
			}
			TreeItem[] nextItems = item.getItems();
			for (TreeItem t : nextItems) {
				checkOrignalTreeItem(t, cmpntChecks, isApp);
			}
		}
	}

	// 重置当前组件的Configure
	protected void resetConfigure(Component componentSelect, boolean[] isSelect) {
		// TODO Auto-generated method stub
		TableItem[] items = table.getItems();
		String newConfig = "";
		int itemCount = 0;
		String tag = null;
		String[] parametersDefined = componentSelect.getConfigure().split("\n");
		// 给所有define重设值
		for (int i = 0; i < parametersDefined.length; i++) {
			String parameter = parametersDefined[i];
			if (parameter.contains("%$#@num") || parameter.contains("%$#@string") || parameter.contains("%$#@enum")
					|| parameter.contains("%$#@object_para") || parameter.contains("%$#@select")
					|| parameter.contains("%$#@free") || parameter.contains("%$#@object_num")) {
				tag = dideHelper.getTag(parameter, tag);
			}
			if (parameter.contains("#define") && !tag.equals("obj_para")) {
				String[] defines = parametersDefined[i].trim().split("//");
				String[] members = null;
				String annoation = null;
				if (parametersDefined[i].startsWith("//")) {
					members = defines[1].trim().split("\\s+");
					annoation = defines.length > 2 ? defines[2] : "";

				} else {
					members = defines[0].trim().split("\\s+");
					annoation = defines.length > 1 ? defines[1] : "";
				}
				// define格式化
				if (isSelect[i]) {
					parametersDefined[i] = String.format("%-11s", members[0]) + " " + String.format("%-32s", members[1])
							+ " " + String.format("%-18s", items[itemCount].getText(1)) + "//"
							+ items[itemCount].getText(2);
				} else {
					parametersDefined[i] = String.format("%-11s", "//" + members[0]) + " "
							+ String.format("%-32s", members[1]) + " "
							+ String.format("%-18s", items[itemCount].getText(1)) + "//" + items[itemCount].getText(2);
				}

				itemCount++;
			}
			if (tag != null) {
				if (!tag.equals("obj_para")) {
					newConfig += parametersDefined[i] + "\n";
				} else if (!parameter.contains("#define")) {
					newConfig += parametersDefined[i] + "\n";
				}
			} else {
				newConfig += parametersDefined[i] + "\n";
			}

		}
		for (int i = itemCount; i < table.getItemCount(); i++) {
			String configure = String.format("%-11s", "#define") + " " + String.format("%-32s", items[i].getText(0))
					+ " " + String.format("%-18s", items[i].getText(1)) + "//" + items[i].getText(2);
			newConfig += configure + "\n";
		}
		// System.out.println("newConfig: "+newConfig);
		componentSelect.setConfigure(newConfig);
	}

	private Table table;
	private int lastchk, checkcounter;
	private TabFolder folder;
	private String defineInit;
	private Board sBoard = null;
	private Group configGroup = null;
	private TableEditor editor, editor1;
	private OnBoardCpu onBoardCpu = null;
	private Text dependentText, mutexText;
	private DecimalFormat df = new DecimalFormat("######0");
	private String didePath = new DideHelper().getDIDEPath();
	private List<TreeItem> appRequiredItems = new ArrayList<TreeItem>();
	private List<TreeItem> ibootRequiredItems = new ArrayList<TreeItem>();
	private ArrayList<Control> tabelControls = new ArrayList<Control>();
	private ArrayList<Control> partControls = new ArrayList<Control>();
	private ArrayList<String> comptVisited = new ArrayList<String>();
	private boolean appExist = false, ibootExist = false, appConfigureChanged = false, ibootConfigureChanged = false,
			selectChanged = false, configureChanged = false;
	private IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

	private List<Component> compontentsList = null;
	private List<Component> appCompontents = new ArrayList<Component>();
	private List<Component> ibootCompontents = new ArrayList<Component>();
	private List<Component> appCompontentsInit = new ArrayList<Component>();
	private List<Component> ibootCompontentsInit = new ArrayList<Component>();
	private List<Component> appCompontentsChecked = new ArrayList<Component>();
	private List<Component> ibootCompontentsChecked = new ArrayList<Component>();

	private List<Component> appCoreComponents = new ArrayList<Component>();
	private List<Component> appBspComponents = new ArrayList<Component>();
	private List<Component> appThirdComponents = new ArrayList<Component>();
	private List<Component> appUserComponents = new ArrayList<Component>();

	private List<Component> ibootCoreComponents = new ArrayList<Component>();
	private List<Component> ibootBspComponents = new ArrayList<Component>();
	private List<Component> ibootThirdComponents = new ArrayList<Component>();
	private List<Component> ibootUserComponents = new ArrayList<Component>();

	private List<Component> appCheckedSort = new ArrayList<Component>();
	private List<Component> ibootCheckedSort = new ArrayList<Component>();

	private List<CmpntCheck> appCmpntChecks = null;
	private List<CmpntCheck> ibootCmpntChecks = null;

	private String[] tableHeader = { "参数", "值", "注释" };
	private String depedentLabel = "依赖组件: ", mutexLabel = "互斥组件: ";
	private String djyStart = "ptu32_t __djy_main(void)\r\n" + "{\n";
	private String djyEnd = "\tdjy_main();\r\n" + "\treturn 0;\r\n" + "}\n\n";
	private String initStart = "void Sys_ModuleInit(void)\r\n" + "{\n";
	private String initEnd = "\treturn ;\r\n" + "}\n\n";
	private String initHead = null;
	private String evttMain = "\tevtt_main = Djy_EvttRegist(EN_CORRELATIVE,CN_PRIO_RRS,0,0,\r\n"
			+ "\t__djy_main,NULL,CFG_MAINSTACK_LIMIT, \"main function\");\r\n"
			+ "\t//事件的两个参数暂设为0,如果用shell启动,可用来采集shell命令行参数\r\n" + "\tDjy_EventPop(evtt_main,NULL,0,NULL,0,0);\n\n";
}
