package com.djyos.dide.ui.wizards.djyosProject;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
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
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

import com.djyos.dide.ui.wizards.board.Board;
import com.djyos.dide.ui.wizards.board.onboardcpu.OnBoardCpu;
import com.djyos.dide.ui.wizards.component.Component;
import com.djyos.dide.ui.wizards.component.ConfigureTarget;
import com.djyos.dide.ui.wizards.component.CreateCheckXml;
import com.djyos.dide.ui.wizards.component.ReadComponent;
import com.djyos.dide.ui.wizards.cpu.core.Core;
import com.djyos.dide.ui.wizards.djyosProject.tools.Calculator;
import com.djyos.dide.ui.wizards.djyosProject.tools.DPluginImages;
import com.djyos.dide.ui.wizards.djyosProject.tools.DideHelper;
import com.djyos.dide.ui.wizards.djyosProject.tools.LinkHelper;
import com.ibm.icu.text.DecimalFormat;

public class ComponentConfigWizard extends WizardPage {

	private Table table;
	private int lastchk;
	private TabFolder folder;
	private int checkcounter;
	private Board sBoard = null;
	private String defineInit;
	private Group configGroup = null;
	private OnBoardCpu onBoardCpu = null;
	private Text dependentText, mutexText;
	private TableEditor editor, editor1;
	private boolean appExist, ibootExist;
	private List<Component> compontentsList = null;
	private String depedentLabel = "依赖组件: ", mutexLabel = "互斥组件: ";
	private ArrayList<Control> tabelControls = new ArrayList<Control>();
	private ArrayList<Control> partControls = new ArrayList<Control>();
	private DecimalFormat df = new DecimalFormat("######0");
	private List<TreeItem> appRequiredItems = new ArrayList<TreeItem>();
	private List<TreeItem> ibootRequiredItems = new ArrayList<TreeItem>();
	private IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	private List<Component> appCompontents = new ArrayList<Component>();
	private List<Component> ibootCompontents = new ArrayList<Component>();
	private List<Component> appCompontentsChecked = new ArrayList<Component>();
	private List<Component> ibootCompontentsChecked = new ArrayList<Component>();
	public String warningMsg = null;

	private DideHelper dideHelper = new DideHelper();
	private LinkHelper linkHelper = new LinkHelper();

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

	private void initComponent(List<Component> typeCompontents, boolean isApp) {
		// TODO Auto-generated method stub
		for (int i = 0; i < compontentsList.size(); i++) {
			Component component = createNewComponent(compontentsList.get(i));
			// 当组件为必选且不需要配置时，不显示在界面上
			// if (component.getSelectable().equals("required") &&
			// (!component.getConfigure().contains("#define"))) {
			// appCompontentsChecked.add(component);
			// } else {
			// appCompontents.add(component);
			// }
			// System.out.println("组件: "+component.getName()+" 类型:
			// "+component.getAttribute());
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

	public void creatProjectConfiure(String projectLocation, Core core, boolean isApp) {
		List<Component> compontentsCheckedSort = null;
		String cfgPath = null;
		// File file = new File(path+(isApp?"/app":"/iboot")+"/initPrj.c");
		if (isApp) {
			compontentsCheckedSort = appCheckedSort;
			cfgPath = projectLocation + "/src/app/OS_prjcfg/project_config.h";
		} else {
			compontentsCheckedSort = ibootCheckedSort;
			cfgPath = projectLocation + "/src/iboot/OS_prjcfg/project_config.h";
		}
		File file = new File(cfgPath);
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
			if (c.isSelect() && c.getTarget().equals(ConfigureTarget.HEADER.getName())) {
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
		defineInit += String.format("%-9s", "#define") + String.format("%-32s", "CFG_CORE_MCLK")
				+ String.format("%-18s", "(" + core.getCoreClk() + "*Mhz)") + "//主频，内核要用，必须定义";
		defineInit += "\n\n#endif";

		dideHelper.writeFile(file, defineInit);
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
		String content = "", firstInit = "\tuint16_t evtt_main;\n\n", lastInit = "", moduleInit = "", gpioInit = "",
				djyMain = "", shellInit = "";
		String earlyCode = "", mediumCode = "", laterCode = "";
		initHead = DjyosMessages.Automatically_Generated;
		initHead += "#include \"project_config.h\"\n" + "#include \"stdint.h\"\n" + "#include \"stddef.h\"\n"
				+ "#include \"cpu_peri.h\"\n" + "extern ptu32_t djy_main(void);\n";
		File file = new File(projectLocation + "/src/" + (isApp ? "app" : "iboot") + "/initPrj.c");
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < typeCompontentsChecked.size(); i++) {
			handleDependents(typeCompontentsChecked.get(i), typeCompontentsChecked, typeCheckedSort);
			// List<String> dependents = typeCompontentsChecked.get(i).getDependents();
			// List<String> weakDependents =
			// typeCompontentsChecked.get(i).getWeakDependents();
			// for (int j = 0; j < typeCompontentsChecked.size(); j++) {
			// if (!typeCheckedSort.contains(typeCompontentsChecked.get(j))) {
			// if (dependents.contains(typeCompontentsChecked.get(j).getName())) {
			// typeCheckedSort.add(typeCompontentsChecked.get(j));
			// } else if (weakDependents.contains(typeCompontentsChecked.get(j).getName()))
			// {
			// typeCheckedSort.add(typeCompontentsChecked.get(j));
			// }
			// }
			// }
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
		lastInit += "\tprintf(\"\\r\\n: info : all modules are configured.\");\r\n"
				+ "\tprintf(\"\\r\\n: info : os starts.\\r\\n\");\n\n";

		content += initHead;
		content += "\n" + djyStart + djyMain + djyEnd;
		content += initStart + firstInit + gpioInit + shellInit
				+ "\t//-------------------early-------------------------//\n" + earlyCode
				+ "\t//-------------------medium-------------------------//\n" + mediumCode
				+ "\t//-------------------later-------------------------//\n" + laterCode + lastInit + initEnd;
		dideHelper.writeFile(file, content);

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
		Composite composite = new Composite(parent, SWT.NONE);
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
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
	}

	private void handleRequiredDepnds(boolean isApp, TreeItem item) {
		// TODO Auto-generated method stub
		String type = isApp ? "App" : "Iboot";
		Component itemCompt;
		if (isApp) {
			itemCompt = getAppComponent(item.getData().toString());
		} else {
			itemCompt = getIbootComponent(item.getData().toString());
		}

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

	/*
	 * 判断组件是否有子组件
	 */
	private boolean haveChildren(Component parent, List<Component> componentList) {
		for (Component compt : componentList) {
			if (compt.getParent().equals(parent.getName())) {
				return true;
			}
		}
		return false;
	}

	private void createDynamicGroup(Composite composite) {
		// TODO Auto-generated method stub
		creatDepedentCpt(composite);

		ReadComponent rc = new ReadComponent();
		compontentsList = rc.getComponents(onBoardCpu, sBoard);
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

	// 创建依赖组件界面
	private void creatDepedentCpt(Composite composite) {
		// TODO Auto-generated method stub
		Composite depedentCpt = new Composite(composite, SWT.BORDER);
		depedentCpt.setLayout(new GridLayout(2, true));
		depedentCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_CENTER));
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

	// 检查所有组件参数是否合法
	private boolean checkParameter(Component component) {
		String configure = component.getConfigure();
		String[] parametersDefined = configure.split("\n");
		String tag = null;
		String[] infos = null;
		List<String> ranges = null;
		List<String> paras = new ArrayList<String>();
		for (int i = 0; i < parametersDefined.length; i++) {
			String parameter = parametersDefined[i];

			if (parameter.contains("%$#@num") || parameter.contains("%$#@string") || parameter.contains("%$#@enum")
					|| parameter.contains("%$#@object_para") || parameter.contains("%$#@select")
					|| parameter.contains("%$#@free") || parameter.contains("%$#@object_num")) {
				tag = dideHelper.getTag(parameter, tag);

				infos = parameter.split(",|，");
				ranges = new ArrayList<String>();
				if (!tag.equals("select") && !tag.equals("free")) {
					for (int j = 1; j < infos.length; j++) {
						ranges.add(infos[j]);
					}
				}
			} else if (parameter.contains("#define")) {
				String[] defines = parameter.trim().split("//");
				String[] members = null;
				String annoation = null;
				if (parameter.startsWith("//")) {
					members = defines[1].trim().split("\\s+");
				} else {
					members = defines[0].trim().split("\\s+");
				}
				paras.add(members[1]);
				if (tag.equals("enum")) {

				} else if (tag.equals("select")) {

				} else {
					List<String> rangesCopy = ranges;
					if (rangesCopy.size() != 0) {
						String minString = rangesCopy.get(0);
						String maxString = rangesCopy.get(1);
						if (tag.equals("int")) {
							try {
								if (!dideHelper.handleIntPara(minString, maxString, new ArrayList<String>(), members)) {
									return false;
								}
							} catch (Exception e) {
								// TODO: handle exception
								MessageDialog.openError(window.getShell(), "提示",
										"组件" + component.getName() + "配置信息有误，" + e.getMessage());
							}
						} else if (tag.equals("string")) {
							try {
								if (!dideHelper.handleStringPara(minString, maxString, new ArrayList<String>(), members,
										defines)) {
									return false;
								}
							} catch (Exception e) {
								// TODO: handle exception
								MessageDialog.openError(window.getShell(), "提示",
										"组件" + component.getName() + "配置信息有误，" + e.getMessage());
							}
						}
					}

				}

			}
		}
		if (paras.size() > 1) {
			Set set = new HashSet<>(paras);
			if (set.size() < paras.size()) {
				return false;
			}
		}
		return true;
	}

	// 判断当前组件是否存在父组件
	private boolean isParentCompExist(List<Component> components, String parentName) {
		for (Component component : components) {
			if (component.getName().equals(parentName)) {
				return true;
			}
		}
		return false;
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
			creatTabAppContent(appTypeComponents, componentTree);
		}

		if (ibootExist) {
			creatTabIbootContent(ibootTypeComponents, componentTree);
		}

		openFileItem.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				TreeItem[] items = componentTree.getSelection();
				if (items.length > 0) {
					Component itemCompt = null;
					String type = getAIType(items[0]);
					if (type.equals("App")) {
						itemCompt = getAppComponent(items[0].getData().toString());
					} else {
						itemCompt = getIbootComponent(items[0].getData().toString());
					}
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
								itemCompt = getAppComponent(item.getData().toString());
							} else {
								isApp = false;
								itemCompt = getIbootComponent(item.getData().toString());
							}
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
								itemCompt = getAppComponent(item.getData().toString());
							} else {
								isApp = false;
								itemCompt = getIbootComponent(item.getData().toString());
							}
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
												itemCompt.setSelect(false);
												if (isApp) {
													appCompontentsChecked.remove(itemCompt);
												} else {
													ibootCompontentsChecked.remove(itemCompt);
												}
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
		// 组件树的点击事件
		componentTree.addListener(SWT.MouseDown, new Listener() {

			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub

				Point point = new Point(event.x, event.y);
				TreeItem item = componentTree.getItem(point);

				if (item != null) {
					if (item.getText().equals("App") || item.getText().equals("Iboot")) {
						openFileItem.setEnabled(false);
					} else {
						openFileItem.setEnabled(true);
					}
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
					if (!item.getText().equals("App") && !item.getText().equals("Iboot")) {
						String itemText = item.getText();
						String type = getAIType(item);

						Component itemCompt;
						if (type.equals("App")) {
							itemCompt = getAppComponent(item.getData().toString());
						} else {
							itemCompt = getIbootComponent(item.getData().toString());
						}
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
						} else {
							configGroup.setText(type + "组件 [" + itemText + "] 配置");
							table.setEnabled(true);
						}
						initTable(itemCompt);
					} else {
						configGroup.setText("组件配置[请选择要配置的组件]");
						table.setEnabled(false);
					}
				}
			}
		});

		return componentTree;
	}

	// 创建Tab的内容:Iboot组件
	private void creatTabIbootContent(List<Component> ibootTypeComponents, Tree componentTree) {
		// TODO Auto-generated method stub
		List<Component> iFirstList = new ArrayList<Component>();
		for (int i = 0; i < ibootTypeComponents.size(); i++) {
			String parentName = ibootTypeComponents.get(i).getParent();
			if (!isParentCompExist(ibootTypeComponents, parentName)) {
				iFirstList.add(ibootTypeComponents.get(i));
			}
		}

		TreeItem rootIboot = new TreeItem(componentTree, 0);
		rootIboot.setImage(DPluginImages.CFG_CPMT_OBJ.createImage());
		rootIboot.setText("Iboot");
		rootIboot.setGrayed(true);
		rootIboot.setChecked(true);
		for (Component component : iFirstList) {
			TreeItem item;
			if (component.getSelectable().equals("required") || component.getSelectable().equals("必选组件")) {
				item = new TreeItem(rootIboot, SWT.ERROR_CANNOT_SET_ENABLED);
				item.setChecked(true);
				component.setSelect(true);
				ibootCompontentsChecked.add(component);
			} else {
				item = new TreeItem(rootIboot, 0);
			}
			item.setData(component.getParentPath());
			item.setData("anno", component.getAnnotation());
			item.setText(component.getName());
			boolean pass = checkParameter(component);
			if (pass) {
				item.setImage(DPluginImages.CFG_COMPONENT_OBJ.createImage());
			} else {
				item.setImage(DPluginImages.CFG_COMPTERROR_VIEW.createImage());
			}
			if (component.getSelectable().equals("required") || component.getSelectable().equals("必选组件")) {
				ibootRequiredItems.add(item);
			}
			// 叶子节点对应的数值为相应文件夹的File对象
			if (haveChildren(component, ibootTypeComponents)) {
				fillItem(item, ibootTypeComponents, rootIboot, false);
			}
		}
	}

	// 创建Tab的内容:App组件
	private void creatTabAppContent(List<Component> appTypeComponents, Tree componentTree) {
		// TODO Auto-generated method stub
		List<Component> aFirstList = new ArrayList<Component>();
		for (int i = 0; i < appTypeComponents.size(); i++) {
			String parentName = appTypeComponents.get(i).getParent();
			if (!isParentCompExist(appTypeComponents, parentName)) {
				aFirstList.add(appTypeComponents.get(i));
			}
		}
		TreeItem rootApp = new TreeItem(componentTree, 0);
		rootApp.setImage(DPluginImages.CFG_CPMT_OBJ.createImage());
		rootApp.setText("App");
		rootApp.setGrayed(true);
		rootApp.setChecked(true);
		for (Component component : aFirstList) {
			String configure = component.getConfigure();
			TreeItem item;
			if (component.getSelectable().equals("required") || component.getSelectable().equals("必选组件")) {
				item = new TreeItem(rootApp, SWT.ERROR_CANNOT_SET_ENABLED);
				item.setChecked(true);
				component.setSelect(true);
				appCompontentsChecked.add(component);
			} else {
				item = new TreeItem(rootApp, 0);
			}
			item.setData(component.getParentPath());
			item.setData("anno", component.getAnnotation());
			item.setText(component.getName());
			boolean pass = checkParameter(component);
			if (pass) {
				item.setImage(DPluginImages.CFG_COMPONENT_OBJ.createImage());
			} else {
				item.setImage(DPluginImages.CFG_COMPTERROR_VIEW.createImage());
			}
			if (component.getSelectable().equals("required") || component.getSelectable().equals("必选组件")) {
				appRequiredItems.add(item);
			}
			if (haveChildren(component, appTypeComponents)) {
				fillItem(item, appTypeComponents, rootApp, true);
			}
		}
	}

	// 判断当前组件是否被依赖
	protected boolean isDepedent(TreeItem treeItem, TreeItem eventTreeItem, String type, Component itemCompt,
			boolean isApp) {
		// TODO Auto-generated method stub
		TreeItem[] items = treeItem.getItems();
		List<String> eventDepedents = itemCompt.getDependents();
		boolean isDepedent = true;
		for (TreeItem item : items) {
			Component tempCompt;
			if (type.equals("App")) {
				tempCompt = getAppComponent(item.getData().toString());
			} else {
				tempCompt = getIbootComponent(item.getData().toString());
			}
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
		visitedComp.add(itemCompt.getName());
		List<String> depedents = itemCompt.getDependents();
		TreeItem[] items = treeItem.getItems();
		for (TreeItem item : items) {
			if (!item.getChecked()) {
				if (depedents.contains(item.getText())) {
					item.setChecked(true);
					Component curComp;
					if (isApp) {
						curComp = getAppComponent(item.getData().toString());
						if (!appCompontentsChecked.contains(curComp)) {
							appCompontentsChecked.add(curComp);
						}
					} else {
						curComp = getIbootComponent(item.getData().toString());
						if (!ibootCompontentsChecked.contains(curComp)) {
							ibootCompontentsChecked.add(curComp);
						}
					}
					curComp.setSelect(true);

					Control[] controls = folder.getChildren();
					for (Control c : controls) {
						String type = isApp ? "App" : "Iboot";
						Tree tempTree = (Tree) c;
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

	// 获取当前组件的类型:App或者Iboot
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
			String configure = child.getConfigure();
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
			boolean pass = checkParameter(child);
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
			if (haveChildren(child, compontentsList)) {
				fillItem(item, compontentsList, rootItem, isApp);
			}
		}
	}

	// 创建配置参数的Table
	private void creatConfigTable(Composite parent) {
		table = new Table(parent, SWT.NONE | SWT.FULL_SELECTION | SWT.H_SCROLL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.minimumHeight = 80;
		table.setLayoutData(gd);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		// 创建表头的字符串数组
		String[] tableHeader = { "参数", "值", "注释" };
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

	// 初始化配置参数的表格
	private void initTable(Component componentSelect) {
		tabelControls.clear();
		checkcounter = 0;
		int partNum = 0;
		boolean selectExist = false;
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
			// if(parametersDefined[i].contains("*")) {
			// parameter = parametersDefined[i].trim().split("\\*")[0];
			// }else {
			// parameter = parametersDefined[i].trim();
			// }
			parameter = parametersDefined[i].trim();
			if (dideHelper.isParaHead(parameter)) {
				tag = dideHelper.getTag(parameter, tag);
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

				realComptName = getRealCompName(annoation, members, paras, ranges, tag);
				setItemText(configure, members, item, realComptName, defines, annoation);

				editor = new TableEditor(table);
				// 设置编辑单元格水平填充
				editor.grabHorizontal = true;
				editor1 = new TableEditor(table);
				// 设置编辑单元格水平填充
				editor1.grabHorizontal = true;

				if (tag.equals("enum")) {
					handleEnumPara(i, isSelect, ranges, item, componentSelect, tag);
				} else if (tag.equals("obj_num")) {
					handleObjnumPara(i, checkBtn, parameter, ranges, isSelect, componentSelect, item, members, tag);
					partNum = Integer.parseInt(members[2]);
				} else if (tag.equals("select")) {
					selectExist = true;
					handleSelectPara(i, checkBtn, parameter, ranges, isSelect, componentSelect, item, tag);
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

					if (rangesCopy.size() > 0) {
						String minString = rangesCopy.get(0);
						String maxString = rangesCopy.get(1);
						if (tag.equals("int")) {
							text.setText(dideHelper.getridParentheses(item.getText(1)));
							handleIntPara(minString, maxString, members, text);
						} else if (tag.equals("string")) {
							if (item.getText(1).replace("\"", "").trim().equals("")) {
								text.setMessage("字符串,以 \" 开头结尾");
							} else {
								text.setText(item.getText(1));
							}
							handleStringPara(minString, maxString, defines, text);
						} else {
							text.setText(item.getText(1));
						}
					} else {
						text.setText(item.getText(1));
					}

					// 当文本框改变值时,注册文本框改变事件，该事件改变表格中的数据,否则即使改变的文本框的值，对表格中的数据也不会影响
					text.addModifyListener(new ModifyListener() {
						public void modifyText(ModifyEvent e) {
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
							}
							if (toCalculate) {
								item.setText(1, "(" + text.getText() + ")");
							} else {
								item.setText(1, text.getText());
							}

							resetConfigure(componentSelect, isSelect);
						}

					});
				}
				// 创建一个文本框，用于输入文字
				Text annoText = new Text(table, SWT.BORDER);
				annoText.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
				// 将文本框当前值，设置为表格中的值
				annoText.setText(item.getText(2));
				annoText.addModifyListener(new ModifyListener() {

					@Override
					public void modifyText(ModifyEvent e) {
						// TODO Auto-generated method stub
						String anno = annoText.getText();
						item.setText(2, anno);
						resetConfigure(componentSelect, isSelect);
					}
				});
				editor1.setEditor(annoText, item, 2);
				tabelControls.add(annoText);
			} else if (parameter.contains("#define") && tag.equals("obj_para")) {
				isSelect[i] = true;
			}
		}
		if (selectExist) {
			checkError(componentSelect);
		}
		expendParas = getExpandParas(componentSelect);
		fillParts(partNum, expendParas, componentSelect, isSelect);
		resetConfigure(componentSelect, isSelect);

	}

	private String getRealCompName(String annoation, String[] members, List<String> paras, List<String> ranges,
			String tag) {
		// TODO Auto-generated method stub
		String realComptName = null;
		String[] annos = annoation.split(",|，");
		if (annos[0].trim().startsWith("\"") && annos[0].trim().endsWith("\"")) {
			annoation = annoation.substring(annos[0].length(), annoation.length()).replaceFirst(",|，", "");
			if (!annos[0].contains("name")) {
				realComptName = annos[0].trim().replaceAll("\"", "");
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
				String[] defines = parameter.trim().split("//");
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
		componentSelect.setConfigure(newConfig);
	}

	private void handleStringPara(String minString, String maxString, String[] defines, Text text) {
		int min, max;
		String value = null;
		min = Integer.parseInt(minString);
		max = Integer.parseInt(maxString);
		int begin = defines[0].indexOf("\"");
		int end = defines[0].lastIndexOf("\"");
		value = defines[0].substring(begin + 1, end);
		if (value.length() < min || value.length() > max) {
			text.setForeground(table.getDisplay().getSystemColor(SWT.COLOR_RED));
		}
	}

	private void handleIntPara(String minString, String maxString, String[] members, Text text) {
		int min;
		long max, curData;
		;
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
		if (members[2].startsWith("0x")) {
			curData = Long.parseLong(members[2].substring(2), 16);
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
			curData = Long.parseLong(dideHelper.getridParentheses(members[2]));
		}
		if (curData < min || curData > max) {
			text.setForeground(table.getDisplay().getSystemColor(SWT.COLOR_RED));
		}
	}

	private void handleSelectPara(int i, Button[] checkBtn, String parameter, List<String> ranges, boolean[] isSelect,
			Component componentSelect, TableItem item, String tag) {
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
					if (rangeSize > 0) {
						checkcounter += 1;
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
					checkError(componentSelect);
				}
				resetConfigure(componentSelect, isSelect);
			}
		});
		tabelControls.add(checkBtn[i]);
	}

	private void checkError(Component componentSelect) {
		if (checkcounter < 1) {
			warningMsg = componentSelect.getAttribute() + "[" + componentSelect.getName() + "]未勾选参数";
		} else {
			warningMsg = null;
		}
	}

	private void handleEnumPara(int i, boolean[] isSelect, List<String> ranges, TableItem item,
			Component componentSelect, String tag) {
		isSelect[i] = true;
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
				item.setText(1, combo.getText());
				resetConfigure(componentSelect, isSelect);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

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
				List<String> expendParas = getExpandParas(componentSelect);
				fillParts(partCount, expendParas, componentSelect, isSelect);
				item.setText(1, combo.getText());
				resetConfigure(componentSelect, isSelect);
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
				setItemText(componentSelect.getConfigure(), members, item, realComptName + "_" + i, defines,
						isNFSelect ? annoation.replace("0", String.valueOf(i)) : i + annoation.trim());
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

	private void setItemText(String configure, String[] members, TableItem item, String realComptName, String[] defines,
			String annoation) {
		if (members.length == 3) {
			item.setText(new String[] { realComptName, members[2].equals("default") ? "" : members[2],
					defines.length > 1 ? annoation : "" });
		} else if (members.length == 2) {
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
		} else if (members.length == 4) {
			int begin = defines[0].indexOf("\"");
			int end = defines[0].lastIndexOf("\"");
			item.setText(new String[] { realComptName, defines[0].substring(begin, end + 1),
					defines.length > 1 ? annoation : "" });
		}
	}

	public static boolean isInteger(char c) {
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
		return pattern.matcher(String.valueOf(c)).matches();
	}

	public List<Component> getAppCompontentsChecked() {
		return appCompontentsChecked;
	}

	public List<Component> getIbootCompontentsChecked() {
		return ibootCompontentsChecked;
	}

	// 通过节点名获取组件
	private Component getAppComponent(String itemName) {
		for (Component component : appCompontents) {
			if (component.getParentPath().equals(itemName)) {
				return component;
			}
		}
		return null;
	}

	// 通过节点名获取组件
	private Component getIbootComponent(String itemName) {
		for (Component component : ibootCompontents) {
			if (component.getParentPath().equals(itemName)) {
				return component;
			}
		}
		return null;
	}

	// 获取扩展的参数
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

	String djyStart = "ptu32_t __djy_main(void)\r\n" + "{\n";
	String djyEnd = "\tdjy_main();\r\n" + "\treturn 0;\r\n" + "}\n\n";
	String initStart = "void Sys_ModuleInit(void)\r\n" + "{\n";
	String initEnd = "\treturn ;\r\n" + "}\n\n";
	String initHead = null;
	String evttMain = "\tevtt_main = Djy_EvttRegist(EN_CORRELATIVE,CN_PRIO_RRS,0,0,\r\n"
			+ "\t__djy_main,NULL,CFG_MAINSTACK_LIMIT, \"main function\");\r\n"
			+ "\t//事件的两个参数暂设为0,如果用shell启动,可用来采集shell命令行参数\r\n" + "\tDjy_EventPop(evtt_main,NULL,0,NULL,0,0);\n\n";
}
