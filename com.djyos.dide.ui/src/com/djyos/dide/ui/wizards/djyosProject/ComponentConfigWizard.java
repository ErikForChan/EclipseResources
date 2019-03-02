package com.djyos.dide.ui.wizards.djyosProject;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
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
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

import com.djyos.dide.ui.DPluginImages;
import com.djyos.dide.ui.helper.Calculator;
import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.messages.IComponentConstants;
import com.djyos.dide.ui.objects.Board;
import com.djyos.dide.ui.objects.Component;
import com.djyos.dide.ui.objects.Core;
import com.djyos.dide.ui.objects.OnBoardCpu;
import com.djyos.dide.ui.wizards.component.ComponentHelper;
import com.djyos.dide.ui.wizards.component.ReadComponent;

@SuppressWarnings("restriction")
public class ComponentConfigWizard extends WizardPage implements IComponentConstants {

	private ComponentHelper componentCommon = new ComponentHelper();

	private void initComponent(List<Component> typeCompontents, boolean isApp) {
		// TODO Auto-generated method stub
		for (int i = 0; i < compontentsList.size(); i++) {
			Component component = componentCommon.createNewComponent(compontentsList.get(i));
			typeCompontents.add(component);
		}

		for (int i = 0; i < typeCompontents.size(); i++) {
			String attribute = typeCompontents.get(i).getAttribute();
			if (attribute.equals("system") || attribute.equals("核心组件")) {
				if (isApp) {
					appCoreComponents.add(typeCompontents.get(i));
					if (!appCompontentsChecked.contains(typeCompontents.get(i))) {
						// typeCompontents.get(i).setSelect(true);
						appCompontentsChecked.add(typeCompontents.get(i));
					}
				} else {
					ibootCoreComponents.add(typeCompontents.get(i));
					if (!ibootCompontentsChecked.contains(typeCompontents.get(i))) {
						// typeCompontents.get(i).setSelect(true);
						ibootCompontentsChecked.add(typeCompontents.get(i));
					}
				}
			} else if (attribute.equals("bsp") || attribute.equals("bsp组件")) {
				if (isApp) {
					appBspComponents.add(typeCompontents.get(i));
				} else {
					ibootBspComponents.add(typeCompontents.get(i));
				}
			} else if (attribute.equals("third") || attribute.equals("第三方组件")) {
				if (isApp) {
					appThirdComponents.add(typeCompontents.get(i));
				} else {
					ibootThirdComponents.add(typeCompontents.get(i));
				}
			} else if (attribute.equals("user") || attribute.equals("用户组件")) {
				if (isApp) {
					appUserComponents.add(typeCompontents.get(i));
				} else {
					ibootUserComponents.add(typeCompontents.get(i));
				}
			}
		}
	}

	public void creatProjectConfiure(String projectLocation, Core core, boolean isApp, int index) {
		String cfgPath = projectLocation + "/src/"+(isApp?"app":"iboot")+"/OS_prjcfg/project_config.h";
		File file = new File(cfgPath);
		String coreConfigure = String.format("%-9s", "#define") + String.format("%-32s", "CFG_CORE_MCLK")
		+ String.format("%-18s", "(" + core.getCoreClk() + "*Mhz)") + "//主频，内核要用，必须定义";
		componentCommon.creatProjectConfiure(file, coreConfigure, isApp, appCheckedSort, ibootCheckedSort, index);
	}

	public void initProject(String projectLocation, boolean isApp) {
		if (isApp) {
			handleInitProject(appCompontentsChecked, appCheckedSort, projectLocation, isApp);
		} else {
			handleInitProject(ibootCompontentsChecked, ibootCheckedSort, projectLocation, isApp);
		}
	}

	private void handleInitProject(List<Component> typeCompontentsChecked, List<Component> typeCheckedSort,
			String projectLocation, boolean isApp) {
		// TODO Auto-generated method stub
		String content = "", firstInit = "\tuint16_t evtt_main;\n\n", lastInit = "", gpioInit = "", djyMain = "",
				shellInit = "";
		String earlyCode = "", mediumCode = "", laterCode = "";
		initHead = DjyosMessages.Automatically_Generated;
		initHead += "#include \"project_config.h\"\n" + "#include \"djyos.h\"\n" + "#include \"stdint.h\"\n"
				+ "#include \"stddef.h\"\n" + "#include \"cpu_peri.h\"\n" + "extern ptu32_t djy_main(void);\n";
		File file = new File(projectLocation + "/src/" + (isApp ? "app" : "iboot") + "/initPrj.c");
		DideHelper.createNewFile(file);
		for (int i = 0; i < typeCompontentsChecked.size(); i++) {
			handleDependents(typeCompontentsChecked.get(i), typeCompontentsChecked, typeCheckedSort);
			if (!typeCheckedSort.contains(typeCompontentsChecked.get(i))) {
				typeCheckedSort.add(typeCompontentsChecked.get(i));
			}
		}
		for (int i = 0; i < typeCheckedSort.size(); i++) {
			Component c = typeCheckedSort.get(i);
			if (c.isSelect()) {
				String grade = c.getGrade();
				String code = c.getCode();
				String componentName = c.getName();
				List<String> dependents = c.getDependents();

				// 添加分区参数
				String[] configures = c.getConfigure().split("\n");
				String tag = null;
				List<String> paraNames = new ArrayList<String>();
				for (String parameter : configures) {
					if (DideHelper.isParaHead(parameter)) {
						tag = DideHelper.getTag(parameter, tag);
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

				if (grade != null && code != null && !codeStrings.trim().equals("")) {
					if (dependents.contains("cpu_peri_gpio")) {
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
		content += initHead;
		content += "\n" + djyStart + djyMain + djyEnd;
		content += initStart + firstInit + gpioInit + shellInit
				+ "\t//-------------------early-------------------------//\n" + earlyCode
				+ "\t//-------------------medium-------------------------//\n" + mediumCode
				+ "\t//-------------------later-------------------------//\n" + laterCode + lastInit + initEnd;
		DideHelper.writeFile(file, content,false);
		componentCommon.createCheckXml(isApp, projectLocation, appCompontents, ibootCompontents);

	}

	private void handleDependents(Component component, List<Component> typeCompontentsChecked,
			List<Component> typeCheckedSort) {
		// TODO Auto-generated method stub
		List<String> dependents = component.getDependents();
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

	protected ComponentConfigWizard(String pageName, OnBoardCpu cpu, Board board, boolean haveApp, boolean haveIboot) {
		super(pageName);
		// TODO Auto-generated constructor stub
		onBoardCpu = cpu;
		sBoard = board;
		appExist = haveApp;
		ibootExist = haveIboot;
		setPageComplete(true);
	}

	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		String workspacePath = workspace.getRoot().getLocation().toString();
		File checkLog = new File(workspacePath+"/check_component.log");
		DideHelper.createNewFile(checkLog);
		
		Composite composite = new Composite(parent, SWT.NONE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);
		composite.setLayout(new GridLayout(1, true));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		createDynamicGroup(composite);
		for (TreeItem t : appRequiredItems) {
			componentCommon.handleRequiredDepnds(true, t, folder, appCompontents, ibootCompontents,
					appCompontentsChecked, ibootCompontentsChecked);
		}
		for (TreeItem t : ibootRequiredItems) {
			componentCommon.handleRequiredDepnds(false, t, folder, appCompontents, ibootCompontents,
					appCompontentsChecked, ibootCompontentsChecked);
		}
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
		
		String errCheckMsg = DideHelper.readFile(checkLog);
		if((errCheckMsg != null) && (!errCheckMsg.trim().equals(""))) {
			DideHelper.showErrorMessage("部分配置文件有错误，请查看控制台详细信息");
			DideHelper.printToConsole(errCheckMsg, true);
		}
	}

	public void creatDepedentCpt(Composite composite) {
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

	private void createDynamicGroup(Composite composite) {
		// TODO Auto-generated method stub
		creatDepedentCpt(composite);

		compontentsList = ReadComponent.getComponents(onBoardCpu, sBoard);
		if (appExist) {
			initComponent(appCompontents, true);
		}
		if (ibootExist) {
			initComponent(ibootCompontents, false);
		}
		// 组件显示界面
		SashForm sashForm = new SashForm(composite, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

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
		item.setText("三方组件"); //$NON-NLS-1$
		item.setControl(createTabContent(folder, appThirdComponents, ibootThirdComponents));

		item = new TabItem(folder, SWT.NONE);
		item.setText("用户组件"); //$NON-NLS-1$
		item.setControl(createTabContent(folder, appUserComponents, ibootUserComponents));

		configGroup = ControlFactory.createGroup(sashForm, "组件配置[请选中要配置的组件]", 1);
		configGroup.setLayout(new GridLayout(1, false));
		configGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		creatConfigTable(configGroup);
		table.setEnabled(false);

		sashForm.setWeights(new int[] { 1, 1 });// 内部容器之间宽度比例

	}

	public void creatConfigTable(Composite parent) {
		// TODO Auto-generated method stub
		table = new Table(parent, SWT.NONE | SWT.FULL_SELECTION | SWT.H_SCROLL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.minimumHeight = 80;
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
			if (i == tableHeader.length - 1) {
				tableColumn.setWidth(200);
			} else {
				tableColumn.setWidth(130);
			}
		}
	}

	// 创建用于存放组件的Tab
	private Control createTabContent(TabFolder folder, List<Component> appTypeComponents,
			List<Component> ibootTypeComponents) {
		// TODO Auto-generated method stub
		Tree componentTree = new Tree(folder, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.CHECK);
		componentTree.setLayoutData(new GridData(GridData.FILL_BOTH));
		componentTree.setSize(SWT.FILL, 300);

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
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				TreeItem[] items = componentTree.getSelection();
				if (items.length > 0) {
					Component itemCompt = null;
					String type = componentCommon.getAIType(items[0]);
					itemCompt = componentCommon.getComponentByPath(items[0].getData().toString(),
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

		// 组件树的选择事件
		componentTree.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				TreeItem item = (TreeItem) event.item;
				if (item == null) {
					return;
				} else {
					for (Control control : tabelControls) {
						control.dispose();
					}
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
					if (item.getText().equals("App") || item.getText().equals("Iboot")) {
						openFileItem.setEnabled(false);
						item.setChecked(true);
						configGroup.setText("组件配置[请选择要配置的组件]");
						table.setEnabled(false);
					} else {
						openFileItem.setEnabled(true);
						String itemText = item.getText();
						String type = componentCommon.getAIType(item);
						if (type != null) {
							boolean isApp = type.equals("App") ? true : false;
							Component itemCompt = componentCommon.getComponentByPath(item.getData().toString(),
									isApp ? appCompontents : ibootCompontents);
							if (itemCompt != null) {
								List<String> depedents = itemCompt.getDependents();
								List<String> mutexs = itemCompt.getMutexs();
								String allDeps = "", allMuts = "";
								// 互斥组件
								for (int k = 0; k < mutexs.size(); k++) {
									allMuts += (k != 0 ? "，" : "") + mutexs.get(k);
								}
								for (int k = 0; k < depedents.size(); k++) {
									allDeps += (k != 0 ? "，" : "") + depedents.get(k);
								}
								dependentText.setText(
										allDeps.equals("") ? (depedentLabel + " 无") : (depedentLabel + allDeps));
								mutexText.setText(allMuts.equals("") ? (mutexLabel + " 无") : ((mutexLabel + allMuts)));

								String configure = itemCompt.getConfigure();
								if (!configure.contains("#define")) {
									configGroup.setText("组件 [" + itemText + "] 无需配置");
									table.setEnabled(false);
								} else {
									configGroup.setText(type + "组件 [" + itemText + "] 配置");
									table.setEnabled(true);
								}
								initTable(itemCompt, isApp, item);

								Control[] controls = folder.getChildren();
								if (item.getChecked()) {
									componentTree.setSelection(item);
									// 判断当前选中组件与已选中组件是否有互斥，如果没有互斥则处理组件依赖
									for (Control c : controls) {
										Tree tempTree = (Tree) c;
										TreeItem[] fChilds = tempTree.getItems();
										for (TreeItem treeItem : fChilds) {
											if (treeItem.getText().equals(type)) {
												boolean isMutex = componentCommon.travelItems_Mutex(treeItem, itemCompt,
														item);
												if (!isMutex) {
													List<String> visitedComp = new ArrayList<String>();
													componentCommon.travelItems_Depedent(treeItem, itemCompt, isApp,
															visitedComp, appCompontents, ibootCompontents,
															appCompontentsChecked, ibootCompontentsChecked, folder);
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
								} else {
									if (itemCompt.getSelectable().equals("required")
											|| itemCompt.getSelectable().equals("必选组件")) {
										item.setChecked(true);
										MessageDialog.openError(window.getShell(), "提示", "该组件为必选组件，不可取消！");
									} else {
										boolean isDepedent = true;
										for (Control c : controls) {
											Tree tempTree = (Tree) c;
											TreeItem[] fChilds = tempTree.getItems();
											for (TreeItem treeItem : fChilds) {
												if (treeItem.getText().equals(type)) {
													isDepedent = componentCommon.isDepedent(treeItem, item, type,
															itemCompt, isApp, appCompontents, ibootCompontents,
															appCompontentsChecked, ibootCompontentsChecked);
													if (isDepedent) {
														itemCompt.setSelect(false);
														(isApp?appCompontentsChecked:ibootCompontentsChecked).remove(itemCompt);
													}
													break;
												}
											}
											if (!isDepedent) {
												break;
											}
										}
									}
								}
							}
						}
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
			if (!componentCommon.isParentCompExist(targetComponents, parentName)) {
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

		if (targetComponents.size() > 0) {
			if (targetComponents.get(0).getAttribute().equals("third")) {
				TreeItem firewareItem = new TreeItem(root, 0);
				firewareItem.setImage(DPluginImages.TREE_FLODER_VIEW.createImage());
				firewareItem.setText("firmware");
				firewareItem.setData("firmware");
				firewareItem.setData("anno", "firmware");
				firewareItem.setGrayed(true);
				firewareItem.setChecked(true);
				for (Component component : firstList) {
					createTreeCompItem(component, firewareItem, targetComponents, isApp);
				}
			}
		}

		for (Component component : firstList) {
			createTreeCompItem(component, root, targetComponents, isApp);
		}
	}

	private void createTreeCompItem(Component component, TreeItem root, List<Component> targetComponents,
			boolean isApp) {

		if ((root.getText().equals("firmware") && component.getParentPath().contains("firmware")
				|| (!root.getText().equals("firmware") && !component.getParentPath().contains("firmware")))) {
			createChild(component, root, targetComponents, isApp);
		}

	}

	private void createChild(Component component, TreeItem root, List<Component> targetComponents, boolean isApp) {
		// TODO Auto-generated method stub
		TreeItem item;
		item = new TreeItem(root, 0);
		if (component.getSelectable().equals("required") || component.getSelectable().equals("必选组件")) {
			item.setChecked(true);
			component.setSelect(true);
			(isApp ? appCompontentsChecked : ibootCompontentsChecked).add(component);
		}
		item.setData(component.getParentPath());
		item.setData("anno", component.getAnnotation());
		item.setText(component.getName());
		boolean pass = DideHelper.checkParameter(component,false,null);
		if (pass) {
			item.setImage(DPluginImages.CFG_COMPONENT_OBJ.createImage());
		} else {
			item.setImage(DPluginImages.CFG_COMPTERROR_VIEW.createImage());
		}
		if (component.getSelectable().equals("required") || component.getSelectable().equals("必选组件")) {
			(isApp ? appRequiredItems : ibootRequiredItems).add(item);
		}
		// 叶子节点对应的数值为相应文件夹的File对象
		if (componentCommon.haveChildren(component, targetComponents)) {
			fillItem(item, targetComponents, root, false);
		}
	}

	// 添加子组件
	private void fillItem(TreeItem parentItem, List<Component> compontentsList, TreeItem rootItem, boolean isApp) {
		// TODO Auto-generated method stub
		String itemName = parentItem.getText();
		List<Component> childList = new ArrayList<Component>();
		for (int i = 0; i < compontentsList.size(); i++) {
			if (compontentsList.get(i).getParent().equals(itemName)) {
				childList.add(compontentsList.get(i));
			}
		}
		for (Component child : childList) {
			TreeItem item;
			if (child.getSelectable().equals("required") || child.getSelectable().equals("必选组件")) {
				item = new TreeItem(parentItem, SWT.ERROR_CANNOT_SET_ENABLED);
				item.setChecked(true);
				child.setSelect(true);
				if (isApp) {
					appCompontentsChecked.add(child);
				} else {
					ibootCompontentsChecked.add(child);
				}
			} else {
				item = new TreeItem(parentItem, 0);
			}
			item.setData(child.getParentPath());
			item.setData("anno", child.getAnnotation());
			item.setText(child.getName());
			boolean pass =  DideHelper.checkParameter(child,false,null);
			if (pass) {
				item.setImage(DPluginImages.CFG_COMPONENT_OBJ.createImage());
			} else {
				item.setImage(DPluginImages.CFG_COMPTERROR_VIEW.createImage());
			}
			if (isApp) {
				if (child.getSelectable().equals("required") || child.getSelectable().equals("必选组件")) {
					appRequiredItems.add(item);
				}
			} else {
				if (child.getSelectable().equals("required") || child.getSelectable().equals("必选组件")) {
					ibootRequiredItems.add(item);
				}
			}
			if (componentCommon.haveChildren(child, compontentsList)) {
				fillItem(item, compontentsList, rootItem, isApp);
			}
		}
	}

	// 初始化配置参数的表格
	private void initTable(Component componentSelect, boolean isApp, TreeItem eventItem) {
		tabelControls.clear();
		checkcounter = 0;
		int partNum = 0;
		String configure = componentSelect.getConfigure();
		String[] parametersDefined = configure.split("\n");
		String tag = null;
		String[] infos = null;
		List<String> ranges = null, paras = new ArrayList<String>(), expendParas = new ArrayList<String>();
		boolean[] isSelect = new boolean[parametersDefined.length];
		Button checkBtn[] = new Button[parametersDefined.length];
		for (int i = 0; i < parametersDefined.length; i++) {
			isSelect[i] = false;
			String parameter = null;
			parameter = parametersDefined[i].trim();
			if (DideHelper.isParaHead(parameter)) {
				tag = DideHelper.getTag(parameter, tag);
				infos = parameter.split(",|，");
				ranges = new ArrayList<String>();
				if (!tag.equals("free")) {
					for (int j = 1; j < infos.length; j++) {
						ranges.add(infos[j].trim());
					}
				}
			} else if (parameter.contains("#define") && !tag.equals("obj_para")) {
				TableItem item = new TableItem(table, SWT.NONE);
				Image image = new Image(PlatformUI.getWorkbench().getDisplay(), 1, 25);
				item.setImage(image);
				String[] defines = parameter.trim().split("//");
				String[] members = null;
				String annoation = null;
				String realComptName = null;
				if (parameter.startsWith("//")) {
					members = defines[1].trim().split("\\s+");
					annoation = defines.length > 2 ? defines[2] : "";
				} else {
					members = defines[0].trim().split("\\s+");
					annoation = defines.length > 1 ? defines[1] : "";
				}

				String[] annos = annoation.split(",|，");
				if (annos[0].trim().startsWith("\"") && annos[0].trim().endsWith("\"")) {
					annoation = annoation.substring(annos[0].length(), annoation.length()).replaceFirst(",|，", "");
				}
				realComptName = componentCommon.getRealCompName(annos[0].trim(), members, paras, ranges, tag);
				setItemText(configure, members, item, realComptName, defines, annoation);

				editor = new TableEditor(table);
				// 设置编辑单元格水平填充
				editor.grabHorizontal = true;
				editor1 = new TableEditor(table);
				// 设置编辑单元格水平填充
				editor1.grabHorizontal = true;

				if (tag.equals("enum")) {
					handleEnumPara(i, isSelect, ranges, item, componentSelect, tag, parameter);
				} else if (tag.equals("obj_num")) {
					handleObjnumPara(i, checkBtn, parameter, ranges, isSelect, componentSelect, item, members, tag);
					partNum = Integer.parseInt(members[2]);
				} else if (tag.equals("select")) {
					handleSelectPara(i, checkBtn, parameter, ranges, isSelect, componentSelect, item, tag, isApp);
				} else {
					isSelect[i] = true;
					// 创建一个文本框，用于输入文字
					Text text = new Text(table, SWT.BORDER);
					text.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
					// 关键方法，将编辑单元格与文本框绑定到表格的第一列
					editor.setEditor(text, item, 1);
					tabelControls.add(text);
					String flag = tag;
					List<String> rangesCopy = ranges;

					if(item.getData("value") != null) {
						String dataValue = item.getData("value").toString();
						if (rangesCopy.size() > 0) {
							String minString = rangesCopy.get(0);
							String maxString = rangesCopy.get(1);
							if (tag.equals("int")) {
								text.setText(DideHelper.getridParentheses(dataValue));
								handleIntPara(minString, maxString, members, text);
							} else if (tag.equals("string")) {
								if (dataValue.replace("\"", "").trim().equals("")) {
									text.setMessage("字符串,以 \" 开头结尾");
								} else {
									text.setText(dataValue);
								}
								handleStringPara(minString, maxString, defines, text);
							} else {
								text.setText(dataValue);
							}
						} else {
							text.setText(dataValue);
						}
					}
					
					text.addModifyListener(new ModifyListener() {
						
						@Override
						public void modifyText(ModifyEvent e) {
							// TODO Auto-generated method stub
							String tempString = text.getText().replace("\"", "");
							if (rangesCopy.size() > 0) {
								if (flag.equals("int")) {
									String minString = rangesCopy.get(0);
									String maxString = rangesCopy.get(1);
									double min = minString.startsWith("0x")?Integer.parseInt(minString.substring(2), 16):Integer.parseInt(minString);
									long max = maxString.startsWith("0x")?Long.parseLong(maxString.substring(2), 16):Long.parseLong(maxString);
									long curNum = -1;
									Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
									boolean isInt = pattern.matcher(tempString).matches();
									if (tempString.startsWith("0x")) {
										curNum = Long.parseLong(tempString.substring(2), 16);
									} else if (tempString.contains("+") || tempString.contains("-")
											|| tempString.contains("*") || tempString.contains("/")) {
										String pureCal = DideHelper.getridParentheses(tempString);
										double result = Calculator.conversion(pureCal);
										BigDecimal bd = new BigDecimal(df.format(result));
										curNum = Long.valueOf(bd.toPlainString());
									} else {
										if (isInt && !tempString.trim().equals("")) {
											curNum = Integer.parseInt(DideHelper.getridParentheses(tempString));
										}
									}
									if (curNum < min || curNum > max) {
										text.setForeground(table.getDisplay().getSystemColor(SWT.COLOR_RED));
									}else {
										text.setForeground(table.getDisplay().getSystemColor(SWT.COLOR_BLACK));
									}
								} else if (flag.equals("string")) {
									if (rangesCopy.size() > 0) {
										int min = Integer.parseInt(rangesCopy.get(0));
										int max = Integer.parseInt(rangesCopy.get(1));
										if (tempString.length() < min || tempString.length() > max) {
											text.setForeground(table.getDisplay().getSystemColor(SWT.COLOR_RED));
										}else {
											text.setForeground(table.getDisplay().getSystemColor(SWT.COLOR_BLACK));
										}
									}
								}
							}
						}
					});
					
					text.addFocusListener(new FocusListener() {
						
						@Override
						public void focusLost(FocusEvent e) {
							// TODO Auto-generated method stub
							String tempString = text.getText().replace("\"", "");
							boolean toCalculate = false;
							if (rangesCopy.size() > 0) {
								if (flag.equals("int")) {
									String minString = rangesCopy.get(0);
									String maxString = rangesCopy.get(1);
									double min = minString.startsWith("0x")?Integer.parseInt(minString.substring(2), 16):Integer.parseInt(minString);
									long max = maxString.startsWith("0x")?Long.parseLong(maxString.substring(2), 16):Long.parseLong(maxString);
									
									long curNum = -1;
									Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
									boolean isInt = pattern.matcher(tempString).matches();
									if (tempString.startsWith("0x")) {
										curNum = Long.parseLong(tempString.substring(2), 16);
									} else if (tempString.contains("+") || tempString.contains("-")
											|| tempString.contains("*") || tempString.contains("/")) {
										toCalculate = true;
										String pureCal = DideHelper.getridParentheses(tempString);
										double result = Calculator.conversion(pureCal);
										BigDecimal bd = new BigDecimal(df.format(result));
										curNum = Long.valueOf(bd.toPlainString());
									} else {
										if (isInt && !tempString.trim().equals("")) {
											curNum = Integer.parseInt(DideHelper.getridParentheses(tempString));
										}
									}
									if (curNum < min || curNum > max) {
										text.setText("");
										DideHelper.showErrorMessage("请填写在" + min + "与" + max + "之间的整数");
									}
								} else if (flag.equals("string")) {
									if (rangesCopy.size() > 0) {
										int min = Integer.parseInt(rangesCopy.get(0));
										int max = Integer.parseInt(rangesCopy.get(1));
										if (tempString.length() < min || tempString.length() > max) {
											text.setText("");
											DideHelper.showErrorMessage("字符串长度不得小于" + min + "或者大于" + max);
										}
									}

								}
							}
							if (text.getForeground().equals(table.getDisplay().getSystemColor(SWT.COLOR_RED))) {
								text.setForeground(table.getDisplay().getSystemColor(SWT.COLOR_BLACK));
							}
							if(!text.getText().trim().equals("")) {
								eventItem.setImage(DPluginImages.CFG_COMPONENT_OBJ.createImage());
							}

							if (toCalculate) {
								item.setData("value", "(" + text.getText() + ")");
							} else {
								item.setData("value", text.getText());
							}

							componentCommon.resetConfigure(componentSelect, isSelect, table);
						}
						
						@Override
						public void focusGained(FocusEvent e) {
							// TODO Auto-generated method stub
							
						}
					});
				}
				// 创建一个文本框，用于输入文字
				Text annoText = new Text(table, SWT.BORDER);
				annoText.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
				// 将文本框当前值，设置为表格中的值
				annoText.setText(item.getText(2));
				annoText.addFocusListener(new FocusListener() {
					
					@Override
					public void focusLost(FocusEvent e) {
						// TODO Auto-generated method stub
						String anno = annoText.getText();
						item.setText(2, anno);
						componentCommon.resetConfigure(componentSelect, isSelect, table);
					}
					
					@Override
					public void focusGained(FocusEvent e) {
						// TODO Auto-generated method stub
						
					}
				});
				editor1.setEditor(annoText, item, 2);
				tabelControls.add(annoText);
			} else if (parameter.contains("#define") && tag.equals("obj_para")) {
				isSelect[i] = true;
			}
		}
		expendParas = componentCommon.getExpandParas(componentSelect, compontentsList);
		fillParts(partNum, expendParas, componentSelect, isSelect);
		componentCommon.resetConfigure(componentSelect, isSelect, table);
	}

	// 重置当前组件的Configure
	private void handleStringPara(String minString, String maxString, String[] defines, Text text) {
		int min = Integer.parseInt(minString);
		int max = Integer.parseInt(maxString);
		String value = defines[0].split("\\s+")[2].replace("\"", "");
//		System.out.println("value:  "+value);
		if(value != null) {
			if (value.length() < min || value.length() > max) {
				text.setForeground(table.getDisplay().getSystemColor(SWT.COLOR_RED));
			}
		}
	}

	private void handleIntPara(String minString, String maxString, String[] members, Text text) {
		int min = minString.startsWith("0x")?Integer.parseInt(minString.substring(2), 16):Integer.parseInt(minString);
		long max = maxString.startsWith("0x")?Long.parseLong(maxString.substring(2), 16):Long.parseLong(maxString);
		long curData;
		if (members[2].startsWith("0x")) {
			curData = Long.parseLong(members[2].substring(2), 16);
		} else if (members[2].contains("+") || members[2].contains("-") || members[2].contains("*")
				|| members[2].contains("/")) {
			String pureCal = DideHelper.getridParentheses(members[2]);
			if (pureCal.startsWith("-")) {
				curData = DideHelper.toUnsigned(Long.parseLong(pureCal));
			} else {
				double result = Calculator.conversion(pureCal);
				BigDecimal bd = new BigDecimal(df.format(result));
				curData = Long.valueOf(bd.toPlainString());
			}
		} else {
			curData = Long.parseLong(DideHelper.getridParentheses(members[2]));
		}
		if (curData < min || curData > max) {
			text.setForeground(table.getDisplay().getSystemColor(SWT.COLOR_RED));
		}
	}

	private void handleSelectPara(int i, Button[] checkBtn, String parameter, List<String> ranges, boolean[] isSelect,
			Component componentSelect, TableItem item, String tag, boolean isApp) {
		checkBtn[i] = new Button(table, SWT.CHECK);
		editor.setEditor(checkBtn[i], item, 1);
		int cur = i;
		int range1 = 0;
		int rangeSize = ranges.size();
		if (rangeSize > 0) {
			range1 = Integer.parseInt(ranges.get(0));
		}

		if (parameter.startsWith("//")) {
			isSelect[i] = false;
			checkBtn[i].setSelection(false);
		} else {
			isSelect[i] = true;
			checkcounter += 1;
			if (range1 == 1) {
				lastchk = i;
			}
			checkBtn[i].setSelection(true);
		}

		int chkran = range1;
		checkBtn[i].addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				boolean checked = checkBtn[cur].getSelection();
				if (checked) {
					checkcounter += 1;
					if (rangeSize > 0) {
						if ((checkcounter > chkran)) {
							if (chkran > 1) {
								checkcounter = chkran;
								checkBtn[cur].setSelection(false);
								MessageDialog.openError(window.getShell(), "提示", "不得勾选多于" + chkran + "项");
							} else if (chkran == 1) {
								checkcounter = chkran;
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
				componentCommon.resetConfigure(componentSelect, isSelect, table);
			}
		});
		tabelControls.add(checkBtn[i]);
	}

	private void handleEnumPara(int i, boolean[] isSelect, List<String> ranges, TableItem item,
			Component componentSelect, String tag, String parameter) {
		isSelect[i] = true;
		if(ranges.size() == 2 && ranges.contains("true") && ranges.contains("false")) {
			Button booleanBtn = new Button(table, SWT.CHECK);
			String[] paras = parameter.trim().split("\\s+");
			if(paras.length > 2) {
				String bValue = paras[2];
				if(bValue.equalsIgnoreCase("true")) {
					booleanBtn.setSelection(true);
				}
			}
			
			booleanBtn.setLayoutData(new GridData(GridData.FILL_BOTH));
			editor.setEditor(booleanBtn, item, 1);
			tabelControls.add(booleanBtn);
			booleanBtn.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
					boolean checked = booleanBtn.getSelection();
					item.setData("value", checked?"true":"false");
					componentCommon.resetConfigure(componentSelect, isSelect, table);
				}
			});
		}else {
			Combo combo = new Combo(table, SWT.READ_ONLY);
			combo.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
			if (ranges != null) {
				combo.setItems(ranges.toArray(new String[ranges.size()]));
			}
			for (int j = 0; j < ranges.size(); j++) {
				if (ranges.get(j).equals(item.getData("value").toString())) {
					combo.select(j);
					break;
				}
			}
			editor.setEditor(combo, item, 1);
			tabelControls.add(combo);
			combo.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
					item.setData("value", combo.getText());
					componentCommon.resetConfigure(componentSelect, isSelect, table);
				}
			});
		}

	}

	private void handleObjnumPara(int index, Button[] checkBtn, String parameter, List<String> ranges,
			boolean[] isSelect, Component componentSelect, TableItem item, String[] members, String tag) {
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
				List<String> expendParas = componentCommon.getExpandParas(componentSelect, compontentsList);
				fillParts(partCount, expendParas, componentSelect, isSelect);
//				item.setText(1, combo.getText());
				item.setData("value", combo.getText());
				componentCommon.resetConfigure(componentSelect, isSelect, table);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
	}

	protected void fillParts(int partCount, List<String> expendParas, Component componentSelect, boolean[] isSelect) {
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
				boolean isNFSelect = componentCommon.isInteger(annoation.charAt(0));
				setItemText(componentSelect.getConfigure(), members, item, realComptName + "_" + i, defines,
						isNFSelect ? annoation.replace("0", String.valueOf(i)) : i + annoation.trim());
				Text text = new Text(table, SWT.BORDER);
				text.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
				// 将文本框当前值，设置为表格中的值
				text.setText(DideHelper.getridParentheses(item.getData("value").toString()));
				text.addModifyListener(new ModifyListener() {

					@Override
					public void modifyText(ModifyEvent e) {
						// TODO Auto-generated method stub
						item.setData("value", text.getText());
//						item.setText(1, text.getText());
						componentCommon.resetConfigure(componentSelect, isSelect, table);
					}
				});
				editor.setEditor(text, item, 1);
				partControls.add(text);
			}
		}
	}

	private void setItemText(String configure, String[] members, TableItem item, String realComptName, String[] defines,
			String annoation) {
		if (members.length == 3) {
			item.setData("value",members[2].equals("default") ? "" : members[2]);
			item.setText(new String[] { realComptName, "", defines.length > 1 ? annoation : "" });
		} else if (members.length == 2) {
			String[] parametersDefined = configure.split("\n");
			boolean valueExisted = false;
			for (int i = 0; i < parametersDefined.length; i++) {
				String parameter = parametersDefined[i];
				String[] paras = parameter.split("//")[0].trim().split("\\s+");
				if (Arrays.asList(paras).contains(realComptName)) {
					valueExisted = true;
					item.setData("value",paras.length > 2 ? paras[2] : "");
					item.setText(new String[] { realComptName, "", defines.length > 1 ? annoation : "" });
					break;
				}
			}
			if (!valueExisted) {
				item.setText(new String[] { realComptName, "", defines.length > 1 ? annoation : "" });
			}
		} else if (members.length == 4) {
			int begin = defines[0].indexOf("\"");
			int end = defines[0].lastIndexOf("\"");
			item.setData("value",defines[0].substring(begin, end + 1));
			item.setText(new String[] { realComptName, "", defines.length > 1 ? annoation : "" });
		}
	}

	public List<Component> getAppCompontentsChecked() {
		return appCompontentsChecked;
	}

	public List<Component> getIbootCompontentsChecked() {
		return ibootCompontentsChecked;
	}

	private Table table;

	private int lastchk;

	private TabFolder folder;

	private int checkcounter;

	private Board sBoard = null;

	private Group configGroup = null;

	private OnBoardCpu onBoardCpu = null;

	private Text dependentText, mutexText;

	private TableEditor editor, editor1;

	private boolean appExist, ibootExist;

	private List<Component> compontentsList = null;

	private ArrayList<Control> tabelControls = new ArrayList<Control>(), partControls = new ArrayList<Control>();

	private List<TreeItem> appRequiredItems = new ArrayList<TreeItem>(), ibootRequiredItems = new ArrayList<TreeItem>();

	private List<Component> appCompontents = new ArrayList<Component>(), ibootCompontents = new ArrayList<Component>(),
			appCompontentsChecked = new ArrayList<Component>(), ibootCompontentsChecked = new ArrayList<Component>(),
			appCoreComponents = new ArrayList<Component>(), appBspComponents = new ArrayList<Component>(),
			appThirdComponents = new ArrayList<Component>(), appUserComponents = new ArrayList<Component>(),
			ibootCoreComponents = new ArrayList<Component>(), ibootBspComponents = new ArrayList<Component>(),
			ibootThirdComponents = new ArrayList<Component>(), ibootUserComponents = new ArrayList<Component>();

	private List<Component> appCheckedSort = new ArrayList<Component>(), ibootCheckedSort = new ArrayList<Component>();
	private String initHead = null;

	public String validateThirdCompt(List<Component> thirdCompontents, boolean isApp) {
		// TODO Auto-generated method stub
		return componentCommon.validateThirdCompt(thirdCompontents, isApp);
	}
}
