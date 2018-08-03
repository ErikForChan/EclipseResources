package org.eclipse.cdt.ui.wizards.djyosProject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
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

import com.ibm.icu.text.DecimalFormat;

import org.eclipse.cdt.ui.wizards.board.Board;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.OnBoardCpu;
import org.eclipse.cdt.ui.wizards.component.CmpntCheck;
import org.eclipse.cdt.ui.wizards.component.Component;
import org.eclipse.cdt.ui.wizards.component.ConfigureTarget;
import org.eclipse.cdt.ui.wizards.component.CreateCheckXml;
import org.eclipse.cdt.ui.wizards.component.ReadComponent;
import org.eclipse.cdt.ui.wizards.cpu.core.Core;
import org.eclipse.cdt.ui.wizards.djyosProject.tools.Calculator;
import org.eclipse.cdt.ui.wizards.djyosProject.tools.DideHelper;
import org.eclipse.cdt.ui.wizards.djyosProject.tools.LinkHelper;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;

import org.eclipse.cdt.internal.ui.CPluginImages;

public class ComponentConfigWizard extends WizardPage {
	private OnBoardCpu onBoardCpu = null;
	private Board sBoard = null;
	private Text dependentText, mutexText;
	private Table table;
	private TabFolder folder;
	private boolean[] isSelect = null;
	private String depedentLabel = "依赖组件: ", mutexLabel = "互斥组件: ";
	private Group configGroup = null;
	private ArrayList<Control> tabelControls = new ArrayList<Control>();
	private TableEditor editor, editor1;
	private String defineInit;
	private boolean appExist, ibootExist;
	List<Component> compontentsList = null;
	DecimalFormat df = new DecimalFormat("######0");
	private IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	List<Component> appCompontents = new ArrayList<Component>();
	List<Component> ibootCompontents = new ArrayList<Component>();
	List<Component> appCompontentsChecked = new ArrayList<Component>();
	List<Component> ibootCompontentsChecked = new ArrayList<Component>();

	List<Component> appCoreComponents = new ArrayList<Component>();
	List<Component> appBspComponents = new ArrayList<Component>();
	List<Component> appThirdComponents = new ArrayList<Component>();
	List<Component> appUserComponents = new ArrayList<Component>();

	List<Component> ibootCoreComponents = new ArrayList<Component>();
	List<Component> ibootBspComponents = new ArrayList<Component>();
	List<Component> ibootThirdComponents = new ArrayList<Component>();
	List<Component> ibootUserComponents = new ArrayList<Component>();

	List<Component> appCheckedSort = new ArrayList<Component>();
	List<Component> ibootCheckedSort = new ArrayList<Component>();
	
	private DideHelper dideHelper = new DideHelper();
	private LinkHelper linkHelper = new LinkHelper();

	public List<Component> getAppCompontentsChecked() {
		return appCompontentsChecked;
	}

	public List<Component> getIbootCompontentsChecked() {
		return ibootCompontentsChecked;
	}

	private String getDIDEPath() {
		String fullPath = Platform.getInstallLocation().getURL().toString();
		String eclipsePath = fullPath.substring(6,
				(fullPath.substring(0, fullPath.length() - 1)).lastIndexOf("/") + 1);
		return eclipsePath;
	}

	public void creatProjectConfiure(String srcPath, Core core, boolean isApp) {
		List<Component> compontentsCheckedSort = null;
		String cfgPath = null;
		// File file = new File(path+(isApp?"/app":"/iboot")+"/initPrj.c");
		if (isApp) {
			compontentsCheckedSort = appCheckedSort;
			cfgPath = srcPath + "/app/OS_prjcfg/project_config.h";
		} else {
			compontentsCheckedSort = ibootCheckedSort;
			cfgPath = srcPath + "/iboot/OS_prjcfg/project_config.h";
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
			System.out.println("compontentsCheckedSort: "+compontentsCheckedSort.get(i).getName());
			if(compontentsCheckedSort.get(i).getTarget().equals(ConfigureTarget.HEADER.getName())) {
				if (compontentsCheckedSort.get(i).getConfigure().contains("#define")) {
					defineInit += "//*******************************  Configure " + compontentsCheckedSort.get(i).getName() + "  ******************************************//\n";
					String[] configures = compontentsCheckedSort.get(i).getConfigure().split("\n");
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
							if(defines.length>1) {
								String[] infos = defines[1].split(",|，");
								if (infos[0].startsWith("\"") && infos[0].endsWith("\"")) {
									annoName = infos[0];
								}
							}
							
							if (annoName == null) {
								defineInit += configures[j] + "\n";
							} else {
								defineInit += configures[j].replace(annoName, "").replace(",", "").replace("，",
										"") + "\n";
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

		FileWriter writer;
		try {
			writer = new FileWriter(cfgPath);
			writer.write(defineInit);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void initProject(String path, boolean isApp) {
		String content = "",firstInit = "\tuint16_t evtt_main;\n\n",lastInit = "",moduleInit = "",gpioInit = "",djyMain = "",shellInit="",
				inoutInit = "\textern void Stdio_KnlInOutInit(char * StdioIn, char *StdioOut);\n"
						+"\tStdio_KnlInOutInit(CFG_STDIO_IN_NAME,CFG_STDIO_OUT_NAME);\n\n";
		initHead = DjyosMessages.Automatically_Generated;
		initHead += "#include \"project_config.h\"\n"
				+ "#include \"stdint.h\"\n"
				+ "#include \"stddef.h\"\n"
				+ "#include \"cpu_peri.h\"\n"
				+ "extern ptu32_t djy_main(void);\n";
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
			for (int i = 0; i < appCompontentsChecked.size(); i++) {
				List<String> dependents = appCompontentsChecked.get(i).getDependents();
				List<String> weakDependents = appCompontentsChecked.get(i).getWeakDependents();
				for (int j = 0; j < appCompontentsChecked.size(); j++) {
					if (!appCheckedSort.contains(appCompontentsChecked.get(j))) {
						if (dependents.contains(appCompontentsChecked.get(j).getName())) {
							appCheckedSort.add(appCompontentsChecked.get(j));
						}else if (weakDependents.contains(appCompontentsChecked.get(j).getName())) {
							appCheckedSort.add(appCompontentsChecked.get(j));
						}
					}
				}
				if (!appCheckedSort.contains(appCompontentsChecked.get(i))) {
					appCheckedSort.add(appCompontentsChecked.get(i));
				}
			}
			for (int i = 0; i < appCheckedSort.size(); i++) {
				String grade = appCheckedSort.get(i).getGrade();
				String code = appCheckedSort.get(i).getCode();
				List<String> dependents = appCheckedSort.get(i).getDependents();
				String codeStrings = "";
				if (code != null) {
					String[] codes = code.split("\n");
					for (int j = 0; j < codes.length; j++) {
						if (codes[j].contains("#include")) {
							initHead += codes[j].trim() + "\n";
						} else {
							codeStrings += "\t" + codes[j].trim() + "\n";
						}
					}
				}

				String componentName = appCheckedSort.get(i).getName();

				if (grade != null && code != null) {
					if (grade.equals("main")) {// 初始化时机为main
						djyMain += codeStrings + "\n";
					} else if (grade.equals("init")) {// 初始化时机为init
						if (dependents.contains("cpu_peri_gpio")) {
							gpioInit += codeStrings + "\n";
						}else if (componentName.equals("heap")) {
							lastInit += evttMain+codeStrings + "\n";
						}else if (componentName.equals("shell")) {
							shellInit += inoutInit+ 
									codeStrings + "\n";
						}
						else {
							moduleInit += codeStrings + "\n";
						}
					}
				}
			}
		} else {
			for (int i = 0; i < ibootCompontentsChecked.size(); i++) {
				List<String> dependents = ibootCompontentsChecked.get(i).getDependents();
				List<String> weakDependents = ibootCompontentsChecked.get(i).getWeakDependents();
				for (int j = 0; j < ibootCompontentsChecked.size(); j++) {
					if (!ibootCheckedSort.contains(ibootCompontentsChecked.get(j))) {
						if (dependents.contains(ibootCompontentsChecked.get(j).getName())) {
							ibootCheckedSort.add(ibootCompontentsChecked.get(j));
						}else if (weakDependents.contains(ibootCompontentsChecked.get(j).getName())) {
							ibootCheckedSort.add(ibootCompontentsChecked.get(j));
						}
					}
				}
				if (!ibootCheckedSort.contains(ibootCompontentsChecked.get(i))) {
					ibootCheckedSort.add(ibootCompontentsChecked.get(i));
				}
			}
			for (int i = 0; i < ibootCheckedSort.size(); i++) {
				String grade = ibootCheckedSort.get(i).getGrade();
				String code = ibootCheckedSort.get(i).getCode();
				List<String> dependents = ibootCheckedSort.get(i).getDependents();
				String codeStrings = "";
				if (code != null) {
					String[] codes = code.split("\n");
					for (int j = 0; j < codes.length; j++) {
						if (codes[j].contains("#include")) {
							initHead += codes[j].trim() + "\n";
						} else {
							codeStrings += "\t" + codes[j].trim() + "\n";
						}
					}
				}

				String componentName = ibootCheckedSort.get(i).getName();

				if (grade != null && code != null) {
					if (grade.equals("main")) {// 初始化时机为main
						djyMain += codeStrings + "\n";
					} else if (grade.equals("init")) {// 初始化时机为init
						if (dependents.contains("cpu_peri_gpio")) {
							gpioInit += codeStrings + "\n";
						}else if (componentName.equals("heap")) {
							lastInit += evttMain+codeStrings + "\n";
						}else if (componentName.equals("shell")) {
							shellInit += inoutInit+ 
									codeStrings + "\n";
						}else {
							moduleInit += codeStrings + "\n";
						}
					}
				}
			}
		}
		lastInit+="\tprintf(\"\\r\\n: info : all modules are configured.\");\r\n"
				+ "\tprintf(\"\\r\\n: info : os starts.\\r\\n\");\n\n";
		content += initHead;
		content += "\n" + djyStart + djyMain + djyEnd;
		content += initStart + firstInit + gpioInit + shellInit + moduleInit + lastInit + initEnd;
		FileWriter writer;
		try {
			writer = new FileWriter(file);
			writer.write(content);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

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
		File checkFile = new File(path + "/../" + "data/" + xmlName);
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

	protected ComponentConfigWizard(String pageName, OnBoardCpu cpu, Board board, boolean haveApp,
			boolean haveIboot) {
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
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite,
				IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);
		composite.setLayout(new GridLayout(1, true));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		createDynamicGroup(composite);
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
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
			initAppComponent();
		}

		if (ibootExist) {
			initibootComponent();
		}

		// Group folderGroup = ControlFactory.createGroup(composite, "组件分类", 1);
		// folderGroup.setLayout(new GridLayout(1, false));
		// folderGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
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
		
		sashForm.setWeights(new int[] {1, 1});// 内部容器之间宽度比例

	}

	private void initibootComponent() {
		// TODO Auto-generated method stub
		for (int i = 0; i < compontentsList.size(); i++) {
			Component component = new Component();
			component.setName(compontentsList.get(i).getName());
			component.setAnnotation(compontentsList.get(i).getAnnotation());
			component.setAttribute(compontentsList.get(i).getAttribute());
			component.setGrade(compontentsList.get(i).getGrade());
			component.setCode(compontentsList.get(i).getCode());
			component.setConfigure(compontentsList.get(i).getConfigure());
			component.setLinkFolder(compontentsList.get(i).getLinkFolder());
			component.setDependents(compontentsList.get(i).getDependents());
			component.setMutexs(compontentsList.get(i).getMutexs());
			component.setFileName(compontentsList.get(i).getFileName());
			component.setSelectable(compontentsList.get(i).getSelectable());
			component.setParent(compontentsList.get(i).getParent());
			component.setWeakDependents(compontentsList.get(i).getWeakDependents());
			component.setExcludes(compontentsList.get(i).getExcludes());
			component.setIncludes(compontentsList.get(i).getIncludes());
			component.setSelect(compontentsList.get(i).isSelect());
			component.setParentPath(compontentsList.get(i).getParentPath());
			component.setTarget(compontentsList.get(i).getTarget());
			// 当组件为必选且不需要配置时，不显示在界面上
			if (component.getSelectable().equals("必选")
					&& (!component.getConfigure().contains("#define"))) {
				ibootCompontentsChecked.add(component);
			} else {
				ibootCompontents.add(component);
			}
		}

		for (int i = 0; i < ibootCompontents.size(); i++) {
			if (ibootCompontents.get(i).getAttribute().equals("核心组件")) {
				ibootCoreComponents.add(ibootCompontents.get(i));
			} else if (ibootCompontents.get(i).getAttribute().equals("bsp组件")) {
				ibootBspComponents.add(ibootCompontents.get(i));
			} else if (ibootCompontents.get(i).getAttribute().equals("第三方组件")) {
				ibootThirdComponents.add(ibootCompontents.get(i));
			} else if (ibootCompontents.get(i).getAttribute().equals("用户组件")) {
				ibootUserComponents.add(ibootCompontents.get(i));
			}
		}
	}

	private void initAppComponent() {
		// TODO Auto-generated method stub
		for (int i = 0; i < compontentsList.size(); i++) {
			Component component = new Component();
			component.setName(compontentsList.get(i).getName());
			component.setAnnotation(compontentsList.get(i).getAnnotation());
			component.setAttribute(compontentsList.get(i).getAttribute());
			component.setGrade(compontentsList.get(i).getGrade());
			component.setCode(compontentsList.get(i).getCode());
			component.setConfigure(compontentsList.get(i).getConfigure());
			component.setLinkFolder(compontentsList.get(i).getLinkFolder());
			component.setDependents(compontentsList.get(i).getDependents());
			component.setMutexs(compontentsList.get(i).getMutexs());
			component.setFileName(compontentsList.get(i).getFileName());
			component.setSelectable(compontentsList.get(i).getSelectable());
			component.setParent(compontentsList.get(i).getParent());
			component.setWeakDependents(compontentsList.get(i).getWeakDependents());
			component.setExcludes(compontentsList.get(i).getExcludes());
			component.setIncludes(compontentsList.get(i).getIncludes());
			component.setSelect(compontentsList.get(i).isSelect());
			component.setParentPath(compontentsList.get(i).getParentPath());
			component.setTarget(compontentsList.get(i).getTarget());
			List<String> excludes = component.getExcludes();
			// 当组件为必选且不需要配置时，不显示在界面上
			if (component.getSelectable().equals("必选")
					&& (!component.getConfigure().contains("#define"))) {
				appCompontentsChecked.add(component);
			} else {
				appCompontents.add(component);
			}
		}

		for (int i = 0; i < appCompontents.size(); i++) {
			if (appCompontents.get(i).getAttribute().equals("核心组件")) {
				appCoreComponents.add(appCompontents.get(i));
			} else if (appCompontents.get(i).getAttribute().equals("bsp组件")) {
				appBspComponents.add(appCompontents.get(i));
			} else if (appCompontents.get(i).getAttribute().equals("第三方组件")) {
				appThirdComponents.add(appCompontents.get(i));
			} else if (appCompontents.get(i).getAttribute().equals("用户组件")) {
				appUserComponents.add(appCompontents.get(i));
			}
		}
	}

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

	private boolean checkParameter(Component component) {
		String configure = component.getConfigure();
		String[] parametersDefined = configure.split("\n");
		String tag = null;
		String[] infos = null;
		List<String> ranges = null;
		for (int i = 0; i < parametersDefined.length; i++) {
			String parameter = parametersDefined[i];

			if (parameter.contains("%$#@num") || parameter.contains("%$#@string")
					|| parameter.contains("%$#@enum") || parameter.contains("%$#@select")
					|| parameter.contains("%$#@free")) {
				if (parameter.contains("%$#@num")) {
					tag = "int";
				} else if (parameter.contains("%$#@string")) {
					tag = "string";
				} else if (parameter.contains("%$#@enum")) {
					tag = "enum";
				} else if (parameter.contains("%$#@select")) {
					tag = "select";
				} else if (parameter.contains("%$#@free")) {
					tag = "free";
				}

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
				if (tag.equals("enum")) {

				} else if (tag.equals("select")) {

				} else {
					List<String> rangesCopy = ranges;
					if (rangesCopy.size() != 0) {
						String minString = rangesCopy.get(0);
						String maxString = rangesCopy.get(1);
						if (tag.equals("int")) {
							try {
								int min;
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

								long curData;
								if (members[2].startsWith("0x")) {
									curData = Long.parseLong(members[2].substring(2), 16);
								} else if (members[2].contains("+") || members[2].contains("-")
										|| members[2].contains("*") || members[2].contains("/")) {
									String pureCal = members[2].replaceAll("\\(|\\)", "");
									if (pureCal.startsWith("-")) {
										curData = dideHelper.toUnsigned(Long.parseLong(pureCal));
									} else {
										double result = Calculator.conversion(pureCal);
										BigDecimal bd = new BigDecimal(df.format(result));
										curData = Long.valueOf(bd.toPlainString());
									}
								} else {
									curData = Integer.parseInt(members[2].replaceAll("\\(|\\)", ""));
								}
								if (curData < min || curData > max) {
									return false;
								}
							} catch (Exception e) {
								// TODO: handle exception
								MessageDialog.openError(window.getShell(), "提示",
										"组件" + component.getName() + "配置信息有误，" + e.getMessage());
							}
						} else if (tag.equals("string")) {
							try {
								int min, max;
								String value = null;
								min = Integer.parseInt(minString);
								max = Integer.parseInt(maxString);
								int begin = defines[0].indexOf("\"");
        						int end = defines[0].lastIndexOf("\"");
        						value = defines[0].substring(begin+1, end);
								if (value.length() < min || value.length() > max) {
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
		return true;
	}

	private boolean isParentCompExist(List<Component> components,String parentName) {
		for(Component component:components) {
			if(component.getName().equals(parentName)) {
				return true;
			}
		}
		return false;
	}
	
	private Control createTabContent(TabFolder folder, List<Component> appTypeComponents,
			List<Component> ibootTypeComponents) {
		// TODO Auto-generated method stub
		Tree componentTree = new Tree(folder, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.CHECK);
		componentTree.setLayoutData(new GridData(GridData.FILL_BOTH));
		componentTree.setSize(SWT.FILL, 300);

		Menu menu = new Menu(componentTree);
		MenuItem openFileItem = new MenuItem(menu, SWT.PUSH);
		openFileItem.setText("打开文件");
		openFileItem.setImage(CPluginImages.CFG_OPENFILE_VIEW.createImage());
		componentTree.setMenu(menu);

		if (appExist) {
			creatTabAppContent(appTypeComponents,componentTree);
		}

		if (ibootExist) {
			creatTabIbootContent(ibootTypeComponents,componentTree);
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
					IFileStore fileStore = EFS.getLocalFileSystem()
							.getStore(new Path(itemCompt.getParentPath()));
					fileStore = fileStore.getChild(itemCompt.getFileName());
					IFileInfo fetchInfo = fileStore.fetchInfo();
					if (!fetchInfo.isDirectory() && fetchInfo.exists()) {
						IWorkbenchPage page = window.getActivePage();
						try {
							IDE.openEditorOnFileStore(page, fileStore);
						} catch (PartInitException e1) {
							String msg = NLS.bind(
									IDEWorkbenchMessages.OpenLocalFileAction_message_errorOnOpen,
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

		componentTree.addMouseTrackListener(new MouseTrackListener() {

			@Override
			public void mouseHover(MouseEvent e) {
				// TODO Auto-generated method stub
				Point point = new Point(e.x, e.y);
				TreeItem item = componentTree.getItem(point);
				String descContent = null;
				if (item != null) {
					if (!item.getText().equals("App") && !item.getText().equals("Iboot")) {
						String itemText = item.getText();
						String type = getAIType(item);
						boolean isApp;
						Component itemCompt;
						if (type.equals("App")) {
							isApp = true;
							itemCompt = getAppComponent(item.getData().toString());
						} else {
							isApp = false;
							itemCompt = getIbootComponent(item.getData().toString());
						}
						descContent = itemCompt.getAnnotation();
						componentTree.setToolTipText(descContent);
					}
				}

			}

			@Override
			public void mouseExit(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEnter(MouseEvent e) {
				// TODO Auto-generated method stub

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
					if(item.getText().equals("App") || item.getText().equals("Iboot")) {
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
											travelItems_Depedent(treeItem, itemCompt, isApp);
											itemCompt.setSelect(true);
											if (isApp) {
												appCompontentsChecked.add(itemCompt);
											} else {
												ibootCompontentsChecked.add(itemCompt);
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
							if (itemCompt.getSelectable().equals("必选")) {
								item.setChecked(true);
								MessageDialog.openError(window.getShell(), "提示", "该组件为必选组件，不可取消！");
							} else {
								for (Control c : controls) {
									Tree tempTree = (Tree) c;
									TreeItem[] fChilds = tempTree.getItems();
									for (TreeItem treeItem : fChilds) {
										if (treeItem.getText().equals(type)) {
											boolean isDepedent = isDepedent(treeItem, item, type,itemCompt,isApp);
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

	private void creatTabIbootContent(List<Component> ibootTypeComponents, Tree componentTree) {
		// TODO Auto-generated method stub
		List<Component> iFirstList = new ArrayList<Component>();
		for(int i=0;i<ibootTypeComponents.size();i++) {
			String parentName = ibootTypeComponents.get(i).getParent();
			if(!isParentCompExist(ibootTypeComponents,parentName)) {
				iFirstList.add(ibootTypeComponents.get(i));
			}
		}

		TreeItem rootIboot = new TreeItem(componentTree, 0);
		rootIboot.setImage(CPluginImages.CFG_CPMT_OBJ.createImage());
		rootIboot.setText("Iboot");
		rootIboot.setGrayed(true);
		rootIboot.setChecked(true);
		for (Component component : iFirstList) {
			TreeItem item;
			if (component.getSelectable().equals("必选")) {
				item = new TreeItem(rootIboot, SWT.ERROR_CANNOT_SET_ENABLED);
				item.setChecked(true);
				component.setSelect(true);
				ibootCompontentsChecked.add(component);
			} else {
				item = new TreeItem(rootIboot, 0);
			}
			item.setData(component.getParentPath());
			item.setText(component.getName());
			boolean pass = checkParameter(component);
			if (pass) {
				item.setImage(CPluginImages.CFG_COMPONENT_OBJ.createImage());
			} else {
				item.setImage(CPluginImages.CFG_COMPTERROR_VIEW.createImage());
			}
			// 叶子节点对应的数值为相应文件夹的File对象
			if (haveChildren(component, ibootTypeComponents)) {
				fillItem(item, ibootTypeComponents, rootIboot, false);
			}
		}
	}

	private void creatTabAppContent(List<Component> appTypeComponents, Tree componentTree) {
		// TODO Auto-generated method stub
		List<Component> aFirstList = new ArrayList<Component>();
		for(int i=0;i<appTypeComponents.size();i++) {
			String parentName = appTypeComponents.get(i).getParent();
			if(!isParentCompExist(appTypeComponents,parentName)) {
				aFirstList.add(appTypeComponents.get(i));
			}
		}
		TreeItem rootApp = new TreeItem(componentTree, 0);
		rootApp.setImage(CPluginImages.CFG_CPMT_OBJ.createImage());
		rootApp.setText("App");
		rootApp.setGrayed(true);
		rootApp.setChecked(true);
		for (Component component : aFirstList) {
			String configure = component.getConfigure();
			TreeItem item;
			if (component.getSelectable().equals("必选")) {
				item = new TreeItem(rootApp, SWT.ERROR_CANNOT_SET_ENABLED);
				item.setChecked(true);
				component.setSelect(true);
				appCompontentsChecked.add(component);
			} else {
				item = new TreeItem(rootApp, 0);
			}
			item.setData(component.getParentPath());
			item.setText(component.getName());
			boolean pass = checkParameter(component);
			if (pass) {
				item.setImage(CPluginImages.CFG_COMPONENT_OBJ.createImage());
			} else {
				item.setImage(CPluginImages.CFG_COMPTERROR_VIEW.createImage());
			}

			if (haveChildren(component, appTypeComponents)) {
				fillItem(item, appTypeComponents, rootApp, true);
			}
		}
	}

	protected boolean isDepedent(TreeItem treeItem, TreeItem eventTreeItem, String type, Component itemCompt, boolean isApp) {
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
				if(tempCompt.getDependents().contains(eventTreeItem.getText())) {
					if(eventDepedents.contains(tempCompt.getName())){
						item.setChecked(false);
						if(isApp) {
							appCompontentsChecked.remove(tempCompt);
						}else {
							ibootCompontentsChecked.remove(tempCompt);
						}
					}else {
						eventTreeItem.setChecked(true);
						isDepedent = false;
						IWorkbenchWindow window = PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow();
						MessageDialog.openError(window.getShell(), "提示",
								"该组件被" + tempCompt.getName() + " 等已勾选的组件依赖，不可取消勾选");
					}
				}
			}

			if (item.getItems().length > 0 && isDepedent) {
				isDepedent = isDepedent(item,eventTreeItem,type,itemCompt,isApp);
			}
		}
		return isDepedent;
	}

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

	protected void travelItems_Depedent(TreeItem treeItem, Component itemCompt, boolean isApp) {
		// TODO Auto-generated method stub
		List<String> depedents = itemCompt.getDependents();
		TreeItem[] items = treeItem.getItems();
		for (TreeItem item : items) {
			if (depedents.contains(item.getText())) {
				item.setChecked(true);
				if (isApp) {
					Component curComponent = getAppComponent(item.getData().toString());
					curComponent.setSelect(true);
					appCompontentsChecked.add(curComponent);
				} else {
					Component curComponent = getIbootComponent(item.getData().toString());
					curComponent.setSelect(true);
					ibootCompontentsChecked.add(curComponent);
				}
			}
			travelItems_Depedent(item, itemCompt, isApp);
		}
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

	private void fillItem(TreeItem parentItem, List<Component> compontentsList, TreeItem rootItem,
			boolean isApp) {
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
			if (child.getSelectable().equals("必选")) {
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
			item.setText(child.getName());
			boolean pass = checkParameter(child);
			if (pass) {
				item.setImage(CPluginImages.CFG_COMPONENT_OBJ.createImage());
			} else {
				item.setImage(CPluginImages.CFG_COMPTERROR_VIEW.createImage());
			}

			if (haveChildren(child, compontentsList)) {
				fillItem(item, compontentsList, rootItem, isApp);
			}
		}
	}

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

	private void initTable(Component componentSelect) {
		tabelControls.clear();
		String configure = componentSelect.getConfigure();
		String[] parametersDefined = configure.split("\n");
		String tag = null;
		String[] infos = null;
		List<String> ranges = null;
		isSelect = new boolean[parametersDefined.length];
		for (int i = 0; i < parametersDefined.length; i++) {
			isSelect[i] = false;
			String parameter = parametersDefined[i];
			if (parameter.contains("//%$#@num") || parameter.contains("%$#@string")
					|| parameter.contains("%$#@enum") || parameter.contains("%$#@select")
					|| parameter.contains("%$#@free")) {
				if (parameter.contains("//%$#@num")) {
					tag = "int";
				} else if (parameter.contains("%$#@string")) {
					tag = "string";
				} else if (parameter.contains("%$#@enum")) {
					tag = "enum";
				} else if (parameter.contains("%$#@select")) {
					tag = "select";
				} else if (parameter.contains("%$#@free")) {
					tag = "free";
				}

				infos = parameter.split(",|，");
				ranges = new ArrayList<String>();
				if (!tag.equals("select") && !tag.equals("free")) {
					for (int j = 1; j < infos.length; j++) {// for (int j = 1; j < infos.length; j++)
						ranges.add(infos[j]);
					}
				}

			} else if (parameter.contains("#define")) {
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
					annoation = annoation.substring(annos[0].length(), annoation.length()).replaceFirst(",|，",
							"");
					if (!annos[0].contains("name")) {
						realComptName = annos[0].trim().replaceAll("\"", "");
					} else {
						realComptName = members[1];
					}

				} else {
					realComptName = members[1];
				}
				if (tag.equals("int") && ranges.size() > 0) {
					String min = ranges.get(0);
					String max = ranges.get(1);
					realComptName = realComptName + "( " + min + "~" + max + " )";
				}else if (tag.equals("string") && ranges.size()>0){
					String min = ranges.get(0);
					String max = ranges.get(1);
					realComptName = realComptName+"(长度："+min+"~"+max+" )";
				}
				if (members.length == 3) {
					item.setText(new String[] { realComptName, members[2].equals("default") ? "" : members[2],
							defines.length > 1 ? annoation : "" });
				} else if (members.length == 2){
					item.setText(new String[] { realComptName, "", defines.length > 1 ? annoation : "" });
				}else if (members.length == 4){
					int begin = defines[0].indexOf("\"");
					int end = defines[0].lastIndexOf("\"");
					item.setText(new String[] { realComptName, defines[0].substring(begin, end+1), defines.length > 1 ? annoation : "" });
				}

				editor = new TableEditor(table);
				// 设置编辑单元格水平填充
				editor.grabHorizontal = true;
				editor1 = new TableEditor(table);
				// 设置编辑单元格水平填充
				editor1.grabHorizontal = true;

				if (tag.equals("enum")) {
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
							resetConfigure(componentSelect);
						}

						@Override
						public void widgetDefaultSelected(SelectionEvent e) {
							// TODO Auto-generated method stub

						}
					});
				} else if (tag.equals("select")) {
					Button checkBtn = new Button(table, SWT.CHECK);
					editor.setEditor(checkBtn, item, 1);
					int cur = i;
					if (parameter.startsWith("//")) {
						isSelect[i] = false;
						checkBtn.setSelection(false);
					} else {
						isSelect[i] = true;
						checkBtn.setSelection(true);
					}
					checkBtn.addListener(SWT.CHECK, new Listener() {

						@Override
						public void handleEvent(Event event) {
							// TODO Auto-generated method stub
							boolean checked = checkBtn.getSelection();
							if (checked) {
								isSelect[cur] = true;
							} else {
								isSelect[cur] = false;
							}
							resetConfigure(componentSelect);
						}
					});
					tabelControls.add(checkBtn);
				} else {
					isSelect[i] = true;
					// 创建一个文本框，用于输入文字
					Text text = new Text(table, SWT.BORDER);
					text.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
					// 将文本框当前值，设置为表格中的值
					text.setText(item.getText(1).replaceAll("\\(|\\)", ""));
					// 关键方法，将编辑单元格与文本框绑定到表格的第一列
					editor.setEditor(text, item, 1);
					tabelControls.add(text);
					String flag = tag;
					List<String> rangesCopy = ranges;

					if (rangesCopy.size() > 0) {
						String minString = rangesCopy.get(0);
						String maxString = rangesCopy.get(1);
						if (tag.equals("int")) {
							int min;
							long max,curData;;
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
							} else if (members[2].contains("+") || members[2].contains("-")
									|| members[2].contains("*") || members[2].contains("/")) {
								String pureCal = members[2].replaceAll("\\(|\\)", "");
								if (pureCal.startsWith("-")) {
									curData = dideHelper.toUnsigned(Long.parseLong(pureCal));
								} else {
									double result = Calculator.conversion(pureCal);
									BigDecimal bd = new BigDecimal(df.format(result));
									curData = Long.valueOf(bd.toPlainString());
								}
							} else {
								curData = Long.parseLong(members[2].replaceAll("\\(|\\)", ""));
							}
							if (curData < min || curData > max) {
								text.setForeground(table.getDisplay().getSystemColor(SWT.COLOR_RED));
							}
					}else if(tag.equals("string")) {
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
										String pureCal = tempString.replaceAll("\\(|\\)", "");
										double result = Calculator.conversion(pureCal);
										BigDecimal bd = new BigDecimal(df.format(result));
										curNum = Long.valueOf(bd.toPlainString());
										// System.out.println(result);
										// BigDecimal bd = new BigDecimal(String.valueOf(result));
										// System.out.println(bd.toPlainString());
										// curNum = Integer.valueOf(bd.toPlainString());
									} else {
										if (isInt) {
											curNum = Integer.parseInt(tempString.replaceAll("\\(|\\)", ""));
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
							if (text.getForeground()
									.equals(table.getDisplay().getSystemColor(SWT.COLOR_RED))) {
								text.setForeground(table.getDisplay().getSystemColor(SWT.COLOR_BLACK));
							}
							if (toCalculate) {
								item.setText(1, "(" + text.getText() + ")");
							} else {
								item.setText(1, text.getText());
							}

							resetConfigure(componentSelect);
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
						resetConfigure(componentSelect);
					}
				});
				editor1.setEditor(annoText, item, 2);
				tabelControls.add(annoText);
			}
		}
	}

	protected void resetConfigure(Component componentSelect) {
		// TODO Auto-generated method stub
		TableItem[] items = table.getItems();
		String newConfig = "";
		int itemCount = 0;
		String[] parametersDefined = componentSelect.getConfigure().split("\n");
		// 给所有define重设值
		for (int i = 0; i < parametersDefined.length; i++) {
			if (parametersDefined[i].contains("#define")) {
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
					parametersDefined[i] = String.format("%-11s", members[0]) + " "
							+ String.format("%-32s", members[1]) + " "
							+ String.format("%-18s", items[itemCount].getText(1)) + "//"
							+ items[itemCount].getText(2);
				} else {
					parametersDefined[i] = String.format("%-11s", "//" + members[0]) + " "
							+ String.format("%-32s", members[1]) + " "
							+ String.format("%-18s", items[itemCount].getText(1)) + "//"
							+ items[itemCount].getText(2);
				}

				itemCount++;
			}
			newConfig += parametersDefined[i] + "\n";
		}
		componentSelect.setConfigure(newConfig);
	}

	private Component getAppComponent(String itemName) {
		for (Component component : appCompontents) {
			if (component.getParentPath().equals(itemName)) {
				return component;
			}
		}
		return null;
	}

	private Component getIbootComponent(String itemName) {
		for (Component component : ibootCompontents) {
			if (component.getParentPath().equals(itemName)) {
				return component;
			}
		}
		return null;
	}

	String djyStart = "ptu32_t __djy_main(void)\r\n" + "{\n";
	String djyEnd = "\tdjy_main();\r\n" + "\treturn 0;\r\n" + "}\n\n";
	String initStart = "void Sys_ModuleInit(void)\r\n" + "{\n";
	String initEnd = "\treturn ;\r\n" + "}\n\n";
	String initHead = null;
	String evttMain = "\tevtt_main = Djy_EvttRegist(EN_CORRELATIVE,CN_PRIO_RRS,0,0,\r\n"
			+ "\t__djy_main,NULL,CFG_MAINSTACK_LIMIT, \"main function\");\r\n"
			+ "\t//事件的两个参数暂设为0,如果用shell启动,可用来采集shell命令行参数\r\n"
			+ "\tDjy_EventPop(evtt_main,NULL,0,NULL,0,0);\n\n";
}
