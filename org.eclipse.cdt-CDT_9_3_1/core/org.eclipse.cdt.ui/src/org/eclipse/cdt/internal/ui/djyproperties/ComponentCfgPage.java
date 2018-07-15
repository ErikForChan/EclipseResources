package org.eclipse.cdt.internal.ui.djyproperties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.eclipse.cdt.ui.wizards.board.Board;
import org.eclipse.cdt.ui.wizards.board.ReadBoardXml;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.OnBoardCpu;
import org.eclipse.cdt.ui.wizards.component.CmpntCheck;
import org.eclipse.cdt.ui.wizards.component.Component;
import org.eclipse.cdt.ui.wizards.component.CreateCheckXml;
import org.eclipse.cdt.ui.wizards.component.ReadCheckXml;
import org.eclipse.cdt.ui.wizards.component.ReadComponent;
import org.eclipse.cdt.ui.wizards.djyosProject.DjyosMessages;
import org.eclipse.cdt.ui.wizards.djyosProject.tools.Calculator;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;

import org.eclipse.cdt.internal.ui.CPluginImages;

public class ComponentCfgPage extends PropertyPage{
	private OnBoardCpu onBoardCpu = null;
	private Board sBoard = null;
	private IWorkbenchWindow window = PlatformUI.getWorkbench()
			.getActiveWorkbenchWindow();
	private Text dependentText, mutexText;
	private Table table;
	private TabFolder folder;
	private boolean[] isSelect = null;
	private String depedentLabel = "依赖组件: ", mutexLabel = "互斥组件: ";
	private Group configGroup = null;
	private ArrayList<Control> tabelControls = new ArrayList<Control>();
	private ArrayList<String> comptVisited = new ArrayList<String>();
	private TableEditor editor,editor1;
	private String defineInit;
	private boolean appExist = false,ibootExist = false;
	List<Component> compontentsList = null;
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
	
	
	private String getDIDEPath() {
		String fullPath = Platform.getInstallLocation().getURL().toString();
		String eclipsePath = fullPath.substring(6,(fullPath.substring(0,fullPath.length()-1)).lastIndexOf("/")+1);
		return eclipsePath;
	}
	
	private IProject getProject(){
		Object	element	= getElement();
		IProject project = null;
		
		if (element instanceof IProject) {
			project = (IProject) element;
		} else if (element instanceof IAdaptable) {
			project= ((IAdaptable)element).getAdapter(IProject.class);
		}
		return project;
	}
	
	@Override
	protected Control createContents(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(parent, SWT.NONE);
		initializeDialogUnits(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite,
				IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);
		composite.setLayout(new GridLayout(1, true));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		createDynamicGroup(composite);
		return composite;
	}
	
	private List<CmpntCheck> appCmpntChecks = null;
	private List<CmpntCheck> ibootCmpntChecks = null;
	
	private void createDynamicGroup(Composite composite) {
		// TODO Auto-generated method stub
		IProject project = getProject();
		ReadCheckXml rccx = new ReadCheckXml();
		File appCheckFile = new File(project.getLocation().toString()+"/data/app_component_check.xml");
		File ibootCheckFile = new File(project.getLocation().toString()+"/data/iboot_component_check.xml");

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

		Composite infoArea = new Composite(composite, SWT.NULL);
		infoArea.setLayout(new GridLayout(1, true));
		infoArea.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		getBoardAndCpu();
		ReadComponent rc = new ReadComponent();
		compontentsList = rc.getComponents(onBoardCpu, sBoard);
		
		if(appCheckFile.exists()) {
			try {
				appCmpntChecks = rccx.getCmpntChecks(appCheckFile);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			appExist = true;
			for(int i=0;i<compontentsList.size();i++) {
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
				component.setSelect(compontentsList.get(i).isSelect());
				component.setParentPath(compontentsList.get(i).getParentPath());
				//当组件为必选且不需要配置时，不显示在界面上
				if(component.getSelectable().equals("必选") && (!component.getConfigure().contains("#define"))) {
					appCompontentsChecked.add(component);
				}else {
					appCompontents.add(component);
				}
			}
			
			for(int i=0;i<appCompontents.size();i++) {
				if(appCompontents.get(i).getAttribute().equals("核心组件")) {
					appCoreComponents.add(appCompontents.get(i));
				}else if(appCompontents.get(i).getAttribute().equals("bsp组件")) {
					appBspComponents.add(appCompontents.get(i));
				}else if(appCompontents.get(i).getAttribute().equals("第三方组件")) {
					appThirdComponents.add(appCompontents.get(i));
				}else if(appCompontents.get(i).getAttribute().equals("用户组件")) {
					appUserComponents.add(appCompontents.get(i));
				}
			}
		}
		if(ibootCheckFile.exists()) {
			try {
				ibootCmpntChecks = rccx.getCmpntChecks(ibootCheckFile);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			ibootExist = true;
			for(int i=0;i<compontentsList.size();i++) {
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
				component.setSelect(compontentsList.get(i).isSelect());
				component.setParentPath(compontentsList.get(i).getParentPath());
				//当组件为必选且不需要配置时，不显示在界面上
				if(component.getSelectable().equals("必选") && (!component.getConfigure().contains("#define"))) {
					ibootCompontentsChecked.add(component);
				}else {
					ibootCompontents.add(component);
				}
			}
			
			for(int i=0;i<ibootCompontents.size();i++) {
				if(ibootCompontents.get(i).getAttribute().equals("核心组件")) {
					ibootCoreComponents.add(ibootCompontents.get(i));
				}else if(ibootCompontents.get(i).getAttribute().equals("bsp组件")) {
					ibootBspComponents.add(ibootCompontents.get(i));
				}else if(ibootCompontents.get(i).getAttribute().equals("第三方组件")) {
					ibootThirdComponents.add(ibootCompontents.get(i));
				}else if(ibootCompontents.get(i).getAttribute().equals("用户组件")) {
					ibootUserComponents.add(ibootCompontents.get(i));
				}
			}
		}
		
		// 组件显示界面
		folder = new TabFolder(infoArea, SWT.NONE);
		folder.setLayout(new TabFolderLayout());
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));

		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText("核心组件"); //$NON-NLS-1$
		item.setControl(createTabContent(folder,appCoreComponents,ibootCoreComponents));

		item = new TabItem(folder, SWT.NONE);
		item.setText("bsp组件"); //$NON-NLS-1$
		item.setControl(createTabContent(folder,appBspComponents,ibootBspComponents));

		item = new TabItem(folder, SWT.NONE);
		item.setText("三方组件"); //$NON-NLS-1$
		item.setControl(createTabContent(folder,appThirdComponents,ibootThirdComponents));

		item = new TabItem(folder, SWT.NONE);
		item.setText("用户组件"); //$NON-NLS-1$
		item.setControl(createTabContent(folder,appUserComponents,ibootUserComponents));

		Control[] controls = folder.getChildren();
		Tree coreTree = (Tree)controls[0];
		TreeItem[] coreItems = coreTree.getItems();
		configGroup = ControlFactory.createGroup(infoArea, "组件配置[请选中要配置的组件]", 1);
		configGroup.setLayout(new GridLayout(1, false));
		configGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		creatConfigTable(configGroup);
		table.setEnabled(false);

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

	private boolean checkParameter(Component component,Boolean isApp) {
		
		IProject curProject = getProject();
		List<String> pjCgfs = new ArrayList<String>();
		File configFile = new File(
				curProject.getLocation().toString() + "/src/" + (isApp?"app":"iboot") + "/OS_prjcfg/project_config.h");
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
				if (str.contains("Configure") && str.contains(component.getName())) {
					start = true;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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

				infos = parameter.split(",");
				ranges = new ArrayList<String>();
				if (!tag.equals("select") && !tag.equals("free")) {
					for (int j = 1; j < infos.length; j++) {// for (int j = 1; j < infos.length; j++)
						ranges.add(infos[j]);
					}
				}

			}else if (parameter.contains("#define")) {
				String[] defines = parameter.trim().split("//");
				String[] members = null;
				if (parameter.startsWith("//")) {
					members = defines[1].trim().split("\\s+");
				} else {
					members = defines[0].trim().split("\\s+");
				}
				
				if (tag.equals("enum")) {
					
				} else if (tag.equals("select")) {

					
				} else {
					List<String> rangesCopy = ranges;
					
					if(tag.equals("int")) {
						if(rangesCopy.size() != 0) {
							String minString = rangesCopy.get(0), maxString = rangesCopy.get(1);
							int min;
							long max;
							try {
								if(minString.startsWith("0x")) {
									min = Integer.parseInt(minString.substring(2), 16);
								}else {
									min = Integer.parseInt(minString);
								}
								if(maxString.startsWith("0x")) {
									max = Long.parseLong(maxString.substring(2), 16);
								}else {
									max = Long.parseLong(maxString);
								}
								long curData;
								if(pjCgfs.size()>0) {//如果是通过右键Properties打开的界面，则显示修改后的数值
			            			for(String cfg:pjCgfs) {
			            				if(cfg.contains(members[1])) {
			            					String[] cfgs = cfg.trim().split("\\s+");
			        						if (cfgs[2].startsWith("0x")) {
			        							curData = Long.parseLong(cfgs[2].substring(2), 16);
			        						}else if(members[2].contains("+") || members[2].contains("-") || members[2].contains("*") || members[2].contains("/")) {
			        							String pureCal = members[2].replaceAll("\\(|\\)", "");
												if(pureCal.startsWith("-")) {
													curData = toUnsigned(Long.parseLong(pureCal));
												}else {
													double result = Calculator.conversion(pureCal);
													BigDecimal bd = new BigDecimal(String.valueOf(result));
													curData = Long.valueOf(bd.toPlainString());	
												}
			    							}else {
			        							curData = Integer.parseInt(cfgs[2]);
			        						}
			        						if(curData<min || curData>max) {
			        							return false;
			        						}
			            					break;
			            				}
			            			}
			            			
			            		}
							} catch (Exception e) {
								// TODO: handle exception
								MessageDialog.openError(window.getShell(), "提示",
										"组件"+component.getName()+"配置信息有误，"+e.getMessage());
							}
							
						}
					
					}
				}

			}
		}
		return true;
		
	}
	
	long toUnsigned(long s) {
		return s & 0xFFFFFFFFL;
	}
	
	private Control createTabContent(TabFolder folder,List<Component> appTypeComponents,List<Component> ibootTypeComponents) {
		// TODO Auto-generated method stub
		//configGroup
		Tree componentTree = new Tree(folder, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.CHECK);
		componentTree.setLayoutData(new GridData(GridData.FILL_BOTH));
		componentTree.setSize(SWT.FILL,300);
		
		Menu menu = new Menu(componentTree);
		MenuItem openFileItem = new MenuItem(menu, SWT.PUSH);
		openFileItem.setText("打开文件");
		openFileItem.setImage(CPluginImages.CFG_OPENFILE_VIEW.createImage());
		componentTree.setMenu(menu);
		
		if(appExist) {
			List<Component> aFirstList = new ArrayList<Component>();
			for(int i=0;i<appTypeComponents.size();i++) {
				if(appTypeComponents.get(i).getParent().equals("none")) {
					aFirstList.add(appTypeComponents.get(i));
				}
			}
			TreeItem rootApp = new TreeItem(componentTree, 0);
			rootApp.setImage(CPluginImages.CFG_CPMT_OBJ.createImage());
			rootApp.setText("App");
			for(Component component : aFirstList) {
				String configure  = component.getConfigure();
				TreeItem item ;			
				if(component.getSelectable().equals("必选"))
				{
					item = new TreeItem(rootApp, SWT.ERROR_CANNOT_SET_ENABLED);
					item.setChecked(true);
					component.setSelect(true);
					appCompontentsChecked.add(component);
				}else {
					item = new TreeItem(rootApp, 0);
				}
				if(!item.getChecked()) {
					for(CmpntCheck check:appCmpntChecks) {
						if(check.getCmpntName().equals(component.getName())) {
							if(check.isChecked().equals("true")) {
								item.setChecked(true);
								component.setSelect(true);
								appCompontentsChecked.add(component);
							}
							break;
						}
					}
				}
				item.setText(component.getName());
				item.setData(component.getParentPath());
				
				boolean pass = checkParameter(component,true);
				if(pass) {
					item.setImage(CPluginImages.CFG_COMPONENT_OBJ.createImage());
				}else {
					item.setImage(CPluginImages.CFG_COMPTERROR_VIEW.createImage());
				}			
				
				if(haveChildren(component,appTypeComponents)) {
					fillItem(item,appTypeComponents,rootApp,true);
				}	
			}
		}
		
		if(ibootExist) {
			List<Component> iFirstList = new ArrayList<Component>();
			for(int i=0;i<ibootTypeComponents.size();i++) {
				if(ibootTypeComponents.get(i).getParent().equals("none")) {
					iFirstList.add(ibootTypeComponents.get(i));
				}
			}

			TreeItem rootIboot = new TreeItem(componentTree, 0);
			rootIboot.setImage(CPluginImages.CFG_CPMT_OBJ.createImage());
			rootIboot.setText("Iboot");
			for(Component component : iFirstList) {
				TreeItem item ;
				if(component.getSelectable().equals("必选")) 
				{
					item = new TreeItem(rootIboot, SWT.ERROR_CANNOT_SET_ENABLED);
					item.setChecked(true);
					component.setSelect(true);
					ibootCompontentsChecked.add(component);
				}else {
					item = new TreeItem(rootIboot, 0);
				}
				if(!item.getChecked()) {
					for(CmpntCheck check:ibootCmpntChecks) {
						if(check.getCmpntName().equals(component.getName())) {
							if(check.isChecked().equals("true")) {
								item.setChecked(true);
								component.setSelect(true);
								ibootCompontentsChecked.add(component);
							}
							break;
						}
					}
				}
				item.setData(component.getParentPath());
				item.setText(component.getName());
				boolean pass = checkParameter(component,false);
				if(pass) {
					item.setImage(CPluginImages.CFG_COMPONENT_OBJ.createImage());
				}else {
					item.setImage(CPluginImages.CFG_COMPTERROR_VIEW.createImage());
				}		
				// 叶子节点对应的数值为相应文件夹的File对象
				if(haveChildren(component,ibootTypeComponents)) {
					fillItem(item,ibootTypeComponents,rootIboot,false);
				}		
			}
		}
		
		openFileItem.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = componentTree.getSelection();
				if(items.length>0) {
					Component itemCompt = null;
					String type = getAIType(items[0]);
					if(type.equals("App")) {
						itemCompt = getAppComponent(items[0].getData().toString());
					}else {
						itemCompt = getIbootComponent(items[0].getData().toString());
					}
					
					IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path(itemCompt.getParentPath()));
					fileStore =  fileStore.getChild(itemCompt.getFileName());
					IFileInfo fetchInfo = fileStore.fetchInfo();
					if (!fetchInfo.isDirectory() && fetchInfo.exists()) {
						IWorkbenchPage page =  window.getActivePage();
						try {
							IDE.openEditorOnFileStore(page, fileStore);
						} catch (PartInitException e1) {
							String msg =  NLS.bind(IDEWorkbenchMessages.OpenLocalFileAction_message_errorOnOpen, fileStore.getName());
							IDEWorkbenchPlugin.log(msg,e1.getStatus());
							MessageDialog.open(MessageDialog.ERROR,window.getShell(), IDEWorkbenchMessages.OpenLocalFileAction_title, msg, SWT.SHEET);
						}
					} 
					
				}			
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		//组件树的选择事件
		componentTree.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				TreeItem item = (TreeItem) event.item;
				if (item == null) {
					return;
				}else {
					Component itemCompt ;
					Control[] controls = folder.getChildren();
					String type = getAIType(item);		
					if(item.getChecked()) {
						componentTree.setSelection(item);
						//判断当前选中组件与已选中组件是否有互斥，如果没有互斥则处理组件依赖
						if(type!=null) {
							
							boolean isApp ;
							if(type.equals("App")) {
								isApp = true;
								itemCompt = getAppComponent(item.getData().toString());
							}else{
								isApp = false;
								itemCompt = getIbootComponent(item.getData().toString());
							}
							for(Control c:controls) {
								Tree tempTree = (Tree)c;
								TreeItem[] fChilds = tempTree.getItems();
								for(TreeItem treeItem : fChilds) {
									if(treeItem.getText().equals(type)) {
										boolean isMutex = travelItems_Mutex(treeItem,itemCompt,item);
										if(! isMutex) {
											travelItems_Depedent(treeItem,itemCompt,isApp);
											itemCompt.setSelect(true);
											if(isApp) {
												appCompontentsChecked.add(itemCompt);
											}else {
												ibootCompontentsChecked.add(itemCompt);
											}
										}
										break;
									}
								}
							}
						}
					}else {
						//取消选中当前组件时，判断已选中组件是否依赖于此组件，如果有依赖，则不允许取消并提示
						if(type!=null) {
							boolean isApp ;
							if(type.equals("App")) {
								isApp = true;
								itemCompt = getAppComponent(item.getData().toString());
							}else{
								isApp = false;
								itemCompt = getIbootComponent(item.getData().toString());
							}
							if(itemCompt.getSelectable().equals("必选")) {
								item.setChecked(true);
								MessageDialog.openError(window.getShell(), "提示",
		    							"该组件为必选组件，不可取消！");
							}else {
								for(Control c:controls) {
									Tree tempTree = (Tree)c;
									TreeItem[] fChilds = tempTree.getItems();
									for(TreeItem treeItem : fChilds) {
										if(treeItem.getText().equals(type)) {
											boolean isDepedent = isDepedent(treeItem,item,type);
											if(isDepedent) {
												if(isApp) {
													appCompontentsChecked.remove(itemCompt);
												}else {
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
		
		componentTree.addMouseTrackListener(new MouseTrackListener() {
			
			@Override
			public void mouseHover(MouseEvent e) {
				// TODO Auto-generated method stub
				Point point = new Point(e.x, e.y); 
				TreeItem item = componentTree.getItem(point);
				String descContent = null;
				if(item != null) {
					if(!item.getText().equals("App") && !item.getText().equals("Iboot") ) {
						String itemText = item.getText();
						String type = getAIType(item);
						boolean isApp;
						Component itemCompt ;
						if(type.equals("App")) {
							isApp = true;
							itemCompt = getAppComponent(item.getData().toString());
						}else{
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
		
		//组件树的点击事件
		componentTree.addListener(SWT.MouseDown, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				
				//dependentText mutexText
				Point point = new Point(event.x, event.y);
				TreeItem item = componentTree.getItem(point);
				
				if(item != null) {
					if(item.getText().startsWith("App") || item.getText().startsWith("Iboot")) {
						openFileItem.setEnabled(false);
					}else {
						openFileItem.setEnabled(true);
					}
					for(Control control:tabelControls){
						control.dispose();
						}
					if(editor!=null) {
						editor.dispose();
					}
					if(editor1!=null) {
						editor1.dispose();
					}
					
					table.removeAll();
					if(!item.getText().equals("App") && !item.getText().equals("Iboot") ) {
						String itemText = item.getText();
						String type = getAIType(item);
						boolean isApp;
						Component itemCompt ;
						if(type.equals("App")) {
							isApp = true;
							itemCompt = getAppComponent(item.getData().toString());
						}else{
							isApp = false;
							itemCompt = getIbootComponent(item.getData().toString());
						}
						List<String> depedents = itemCompt.getDependents();
						List<String> mutexs = itemCompt.getMutexs();
						String allDeps = "";
						String allMuts = "";
						// 互斥组件
						for(int k=0;k<mutexs.size();k++) {
							allMuts += (k!=0?"，":"") + mutexs.get(k);					
						}
						for(int k=0;k<depedents.size();k++) {
							allDeps += (k!=0?"，":"") +depedents.get(k);
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
						if(!configure.contains("#define")) {
							configGroup.setText("组件 ["+itemText+"] 无需配置");
							table.setEnabled(false);
//							item.setForeground(folder.getDisplay().getSystemColor(SWT.COLOR_GRAY));
						}else {
							configGroup.setText(type+"组件 ["+itemText+"] 配置");
							table.setEnabled(true);
						}					
						initTable(itemCompt,isApp,item);	
					}else {
						configGroup.setText("组件配置[请选择要配置的组件]");
						table.setEnabled(false);
					}			
				}		
			}
		});
		
		return componentTree;
	}
	
	@Override
	public boolean performOk() {
		// TODO Auto-generated method stub
		IProject project = getProject();
		List<File> cfgFiles = new ArrayList<File>();
		File appCfgFile = new File(project.getLocation().toString()+"/src/app/OS_prjcfg/project_config.h");
		File ibootCfgFile = new File(project.getLocation().toString()+"/src/iboot/OS_prjcfg/project_config.h");
		if(appExist) {
			cfgFiles.add(appCfgFile);
		}
		if(ibootExist) {
			cfgFiles.add(ibootCfgFile);
		}		
		
		try {
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Configuration Reset...", 100);		
					for(File file:cfgFiles) {
						String str = null;
						String coreConfigure = null;
						boolean isApp;
						if(file.getPath().contains("app")) {
							isApp = true;
						}else {
							isApp = false;
						}
						try {
							FileReader reader = new FileReader(file.getPath());
							BufferedReader br = new BufferedReader(reader);
							reader = new FileReader(file.getPath());
							while ((str = br.readLine()) != null) {
								if(str.contains("CFG_CORE_MCLK")) {
									coreConfigure = str;
									break;
								}
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						initProject(project.getLocation().toString()+"/src",isApp);
						creatProjectConfiure(file,coreConfigure,isApp);
						monitor.worked(100/cfgFiles.size());
					}			
					
					monitor.done();
				}
			};
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(
					PlatformUI.getWorkbench().getDisplay().getActiveShell());
			dialog.setCancelable(false);
			dialog.run(true, true, runnable);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return super.performOk();
	}
	
	@Override
	protected void performDefaults() {
		// TODO Auto-generated method stub
//		appCheckedSort = new ArrayList<Component>();
//		ibootCheckedSort = new ArrayList<Component>();
		appCompontentsChecked = new ArrayList<Component>();
		ibootCompontentsChecked = new ArrayList<Component>();
		Control[] controls = folder.getChildren();
		for(Control c:controls) {
			Tree tempTree = (Tree)c;
			TreeItem[] fChilds = tempTree.getItems();
			for(TreeItem treeItem : fChilds) {
				if(treeItem.getText().equals("App")) {
					checkOrignalTreeItem(treeItem,appCmpntChecks,true);
				}else if(treeItem.getText().equals("Iboot")) {
					checkOrignalTreeItem(treeItem,ibootCmpntChecks,false);
				}			
			}
		}
		super.performDefaults();
	}
	
	private void checkOrignalTreeItem(TreeItem treeItem, List<CmpntCheck> cmpntChecks, boolean isApp) {
		// TODO Auto-generated method stub
		TreeItem[] childItems = treeItem.getItems();
		for(TreeItem  item : childItems) {
			for(CmpntCheck check : cmpntChecks) {
				if(check.getCmpntName().equals(item.getText())) {
					if(check.isChecked().equals("true")) {
						item.setChecked(true);
						if(isApp) {
							Component curComponent = getAppComponent(item.getData().toString());
							appCompontentsChecked.add(curComponent);
						}else {
							Component curComponent = getIbootComponent(item.getData().toString());
							ibootCompontentsChecked.add(curComponent);
						}				
					}else {
						item.setChecked(false);
					}
					break;
				}
			}
			TreeItem[] nextItems = item.getItems();
			for(TreeItem t : nextItems) {
				checkOrignalTreeItem(t,cmpntChecks, isApp);
			}
		}
	}

	private void getBoardAndCpu() {
		String cpuName=null,boardName=null;
		IProject project = getProject();
		IFile pfile = project.getFile(".project");
		DocumentBuilderFactory factory =  DocumentBuilderFactory.newInstance();  
		factory.setIgnoringElementContentWhitespace(true);    
		Document doc = null;
		DocumentBuilder db;
		try {
			db = factory.newDocumentBuilder();
			doc = db.parse(pfile.getLocation().toFile());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// 获取根节点
        Element root = doc.getDocumentElement();
        NodeList linkList = doc.getElementsByTagName("link");
        boolean boardGet = false;
        boolean cpuGet = false;
        for(int i=0;i<linkList.getLength();i++) {
        	Node node = linkList.item(i);
        	NodeList cList = node.getChildNodes();
			for (int j = 1; j < cList.getLength(); j += 2) {
				org.w3c.dom.Node cNode = cList.item(j);
				String nodeName = cNode.getNodeName();
				String linkContent = cNode.getTextContent();		
				//节点名字为name
				if(nodeName.equals("name")) {
					if(linkContent.contains("src/libos/bsp/boarddrv/")) {												
						boardName = linkContent.replaceAll("src/libos/bsp/boarddrv/", "");
						boardGet = true;
					}else if(linkContent.contains("src/libos/bsp/cpudrv/")) {							
						cpuName = linkContent.replaceAll("src/libos/bsp/cpudrv/", "");
						cpuGet = true;	
					}
					break;
				}  	        	
			}      
			if(boardGet && cpuGet){
				break;
			}
        }

		ReadBoardXml rbx = new ReadBoardXml();
		List<Board> boards = new ArrayList<Board>();
		List<String> paths = new ArrayList<String>();	
		String userBoardFilePath = getDIDEPath()+"djysrc\\bsp\\boarddrv\\user";
		String demoBoardFilePath = getDIDEPath()+"djysrc\\bsp\\boarddrv\\demo";
		paths.add(userBoardFilePath);
		paths.add(demoBoardFilePath);
		for(int i=0;i<paths.size();i++) {
			File boardFile = new File(paths.get(i));
			File[] files = boardFile.listFiles();
			for(int j=0;j<files.length;j++){
					File file = files[j];
					File[] mfiles = file.listFiles();
					for(int k=0;k<mfiles.length;k++) {
						if(mfiles[k].getName().endsWith(".xml")) {
							try {
								Board board = rbx.getBoard(mfiles[k]);
								boards.add(board);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;
						}
					}
			}
		}
		
		for(int i=0;i<boards.size();i++) {
			if(boards.get(i).getBoardName().equals(boardName)) {
				sBoard = boards.get(i);
				break;
			}
		}
		
		if(sBoard!=null) {
			List<OnBoardCpu> onBoardCpus = sBoard.getOnBoardCpus();
			for(int i=0;i<onBoardCpus.size();i++) {
				if(onBoardCpus.get(i).getCpuName().equals(cpuName)) {
					onBoardCpu = onBoardCpus.get(i);
					break;
				}
			}
		}
		
	}
	
	protected void resetConfigure(Component componentSelect) {
			// TODO Auto-generated method stub
			TableItem[] items = table.getItems();
			String newConfig = "";
			int itemCount = 0;
			String[] parametersDefined = componentSelect.getConfigure().split("\n");
			//给所有define重设值
			for (int i = 0; i < parametersDefined.length; i++) {
				if(parametersDefined[i].contains("#define")) {
					String[] defines = parametersDefined[i].trim().split("//");
					String[] members = null;
	            	String annoation = null;
	            	if(parametersDefined[i].startsWith("//")) {
	            		members = defines[1].trim().split("\\s+");
	            		annoation = defines.length>2?defines[2]:"";
	            		
	            	}else {
	            		members = defines[0].trim().split("\\s+");
	            		annoation = defines.length>1?defines[1]:"";
	            	}
		        	//define格式化
	            	if(isSelect[i]) {
	        			parametersDefined[i] = String.format("%-11s",members[0])+" "+String.format("%-32s", members[1])+" "+String.format("%-18s", items[itemCount].getText(1))+"//"+items[itemCount].getText(2);
	        		}else {
	        			parametersDefined[i] = String.format("%-11s","//"+members[0])+" "+String.format("%-32s", members[1])+" "+String.format("%-18s", items[itemCount].getText(1))+"//"+items[itemCount].getText(2);
	        		}
								
					itemCount++;
				}
				newConfig += parametersDefined[i]+"\n";
			}
			componentSelect.setConfigure(newConfig);
		}
			
	private Component getAppComponent(String itemName) {
			for(Component component:appCompontents) {
				if(component.getParentPath().equals(itemName)) {
					return component;
				}
			}	
			return null;
		}
		
	private Component getIbootComponent(String itemName) {
			for(Component component:ibootCompontents) {
				if(component.getParentPath().equals(itemName)) {
					return component;
				}
			}	
			return null;
		}
		
	private boolean haveChildren(Component parent,List<Component> componentList) {
			for(Component compt:componentList) {
				if(compt.getParent().equals(parent.getName())) {
					return true;
				}
			}
			return false;
		}
		
	private void fillItem(TreeItem parentItem, List<Component> compontentsList,TreeItem rootItem,boolean isApp) {
			// TODO Auto-generated method stub
			String itemName = parentItem.getText();
			List<Component> childList = new ArrayList<Component>();
			for(int i=0;i<compontentsList.size();i++) {
				if(compontentsList.get(i).getParent().equals(itemName)) {
					childList.add(compontentsList.get(i));
				}
			}
			for(Component child : childList) {
				String configure  = child.getConfigure();
				TreeItem item;
				if(child.getSelectable().equals("必选")) 
				{
					item = new TreeItem(parentItem, SWT.ERROR_CANNOT_SET_ENABLED);
					item.setChecked(true);
					child.setSelect(true);
					if(isApp) {
						appCompontentsChecked.add(child);
					}else {
						ibootCompontentsChecked.add(child);
					}
				}else {
					item = new TreeItem(parentItem, 0);
				}
				if(!item.getChecked()) {
					if(isApp) {
						for(CmpntCheck check:appCmpntChecks) {
							if(check.getCmpntName().equals(child.getName())) {
								if(check.isChecked().equals("true")) {
									item.setChecked(true);
									child.setSelect(true);
									appCompontentsChecked.add(child);
								}
								break;
							}
						}
					}else {
						for(CmpntCheck check:ibootCmpntChecks) {
							if(check.getCmpntName().equals(child.getName())) {
								if(check.isChecked().equals("true")) {
									item.setChecked(true);
									child.setSelect(true);
									ibootCompontentsChecked.add(child);
								}
								break;
							}
						}
					}
				
				}
				item.setData(child.getParentPath());
				item.setText(child.getName());
				boolean pass = checkParameter(child,isApp);
				if(pass) {
					item.setImage(CPluginImages.CFG_COMPONENT_OBJ.createImage());
				}else {
					item.setImage(CPluginImages.CFG_COMPTERROR_VIEW.createImage());
				}		
				
				if(haveChildren(child,compontentsList)) {
					fillItem(item,compontentsList,rootItem,isApp);
				}
			}
		}
			
	private String getAIType(TreeItem item) {
			TreeItem parentItem = item.getParentItem();
			if(parentItem!=null) {
				if(parentItem.getText().equals("App")) {
					return "App";
				}else if(parentItem.getText().equals("Iboot")) {
					return "Iboot";
				}else {
					return getAIType(parentItem);
				}
			}else {
				return null;
			}	
		}
			
	protected boolean travelItems_Mutex(TreeItem treeItem, Component itemCompt,TreeItem eventTreeItem) {
			// TODO Auto-generated method stub
			boolean isMutx = false;
			List<String> mutexs = itemCompt.getMutexs();
			TreeItem[] items = treeItem.getItems();
			for(TreeItem item : items) {
				if(mutexs.contains(item.getText())) {
					if(item.getChecked()) {
						eventTreeItem.setChecked(false);
						isMutx = true;
						IWorkbenchWindow window = PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow();
						MessageDialog.openError(window.getShell(), "提示",
								item.getText() + "组件已勾选，与" + itemCompt.getName() + "互斥 ！");				
					}	
				}else {
					if(item.getItems().length>0 && !isMutx) {
						isMutx = travelItems_Mutex(item,itemCompt,eventTreeItem);
					}
				}
			}
			return isMutx;
		}
			
	protected void travelItems_Depedent(TreeItem treeItem, Component itemCompt, boolean isApp) {
			// TODO Auto-generated method stub
			List<String> depedents = itemCompt.getDependents();
			TreeItem[] items = treeItem.getItems();
			for(TreeItem item : items) {
				if(depedents.contains(item.getText())) {
					item.setChecked(true);
					if(isApp) {
						Component curComponent = getAppComponent(item.getData().toString());
						curComponent.setSelect(true);
						appCompontentsChecked.add(curComponent);
					}else {
						Component curComponent = getIbootComponent(item.getData().toString());
						curComponent.setSelect(true);
						ibootCompontentsChecked.add(curComponent);
					}
				}
				travelItems_Depedent(item,itemCompt,isApp);
			}
		}
		
	protected boolean isDepedent(TreeItem treeItem, TreeItem eventTreeItem ,String type) {
			// TODO Auto-generated method stub
			TreeItem[] items = treeItem.getItems();
			boolean isDepedent = true;
			for(TreeItem item : items) {
				Component tempCompt ;
				if(type.equals("App")) {
					tempCompt = getAppComponent(item.getData().toString());
				}else{
					tempCompt = getIbootComponent(item.getData().toString());
				}
				if(item.getChecked()) {
					if(tempCompt.getDependents().contains(eventTreeItem.getText())) {
						eventTreeItem.setChecked(true);
						isDepedent = false;
						IWorkbenchWindow window = PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow();
						MessageDialog.openError(window.getShell(), "提示",
								"该组件被" + tempCompt.getName() + " 等已勾选的组件依赖，不可取消勾选");
					}
				}
	
				if(item.getItems().length>0 && isDepedent) {
					isDepedent = isDepedent(item,eventTreeItem,type);
				}
			}
			return isDepedent;
		}
		
	private void initTable(Component componentSelect,boolean isApp,TreeItem eventItem) {
		tabelControls.clear();
		String compName = componentSelect.getName();
		IProject curProject = getProject();
		List<String> pjCgfs = new ArrayList<String>();
		if(!comptVisited.contains(compName)) {
			File configFile = new File(
					curProject.getLocation().toString() + "/src/" + (isApp?"app":"iboot") + "/OS_prjcfg/project_config.h");
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
					if (str.contains("Configure") && str.contains(compName)) {
						start = true;
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
		
		String configure = componentSelect.getConfigure();
		String[] parametersDefined = configure.split("\n");
		String tag = null;
		String[] infos = null;
		List<String> ranges = null;
		isSelect = new boolean[parametersDefined.length];
		for (int i = 0; i < parametersDefined.length; i++) {
			isSelect[i] = false;
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

				infos = parameter.split(",");
				ranges = new ArrayList<String>();
				if (!tag.equals("select") && !tag.equals("free")) {
					for (int j = 1; j < infos.length; j++) {// for (int j = 1; j < infos.length; j++)
						ranges.add(infos[j]);
					}
				}

			}else if (parameter.contains("#define")) {
				TableItem item = new TableItem(table, SWT.NONE);
				Image image = new Image(PlatformUI.getWorkbench().getDisplay(), 1, 25);
				item.setImage(image);
				String[] defines = parameter.trim().split("//");
				String[] members = null;
				String annoation = null;
				if (parameter.startsWith("//")) {
					members = defines[1].trim().split("\\s+");
					annoation = defines.length > 2 ? defines[2] : "";
				} else {
					members = defines[0].trim().split("\\s+");
					annoation = defines.length > 1 ? defines[1] : "";
				}
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
				
				if (tag.equals("int") && ranges.size()>0){
					String min = ranges.get(0);
					String max = ranges.get(1);
					realComptName = realComptName+"( "+min+"~"+max+" )";
				}
				
				String dataString = null;
				if (members.length > 2) {
					if(pjCgfs.size()>0) {//如果是通过右键Properties打开的界面，则显示修改后的数值
            			for(String cfg:pjCgfs) {
            				if(cfg.contains(members[1])) {
            					String[] cfgs = cfg.trim().split("\\s+");
            					dataString = cfgs[2];
            					item.setText(new String[]{realComptName, cfgs[2].equals("default")?"":cfgs[2], defines.length>1?annoation:""});
            					break;
            				}
            			}
            			
            		}else {
            			
            			dataString = members[2];
            			item.setText(new String[] { realComptName, members[2].equals("default") ? "" : members[2],
    							defines.length > 1 ? annoation : "" });
            		}
				
				} else {
					item.setText(new String[] { realComptName, "", defines.length > 1 ? annoation : "" });
				}

				editor = new TableEditor(table);
				editor1 = new TableEditor(table);
				// 设置编辑单元格水平填充
				editor.grabHorizontal = true;
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
							comptVisited.add(compName);
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
							comptVisited.add(compName);
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

					if(tag.equals("int")) {
						if (rangesCopy.size() > 0) {
							String minString = rangesCopy.get(0);
							String maxString = rangesCopy.get(1);
							int min;
							long max;
							if(minString.startsWith("0x")) {
								min = Integer.parseInt(minString.substring(2), 16);
							}else {
								min = Integer.parseInt(minString);
							}
							if(maxString.startsWith("0x")) {
								max = Long.parseLong(maxString.substring(2), 16);
							}else {
								max = Long.parseLong(maxString);
							}
							
							long curData;
							if (dataString.startsWith("0x")) {
								curData = Long.parseLong(dataString.substring(2), 16);
							}else if(members[2].contains("+") || members[2].contains("-") || members[2].contains("*") || members[2].contains("/")) {
								String pureCal = members[2].replaceAll("\\(|\\)", "");
								if(pureCal.startsWith("-")) {
									curData = toUnsigned(Long.parseLong(pureCal));
								}else {
									double result = Calculator.conversion(pureCal);
									BigDecimal bd = new BigDecimal(String.valueOf(result));
									curData = Long.valueOf(bd.toPlainString());	
								}
							}else {
								curData = Integer.parseInt(dataString);
							}
							if(curData<min || curData>max) {
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
									double min ;
									long max ;
									if(minString.startsWith("0x")) {
										min = Integer.parseInt(minString.substring(2), 16);
									}else {
										min = Integer.parseInt(minString);
									}
									if(maxString.startsWith("0x")) {
										max = Long.parseLong(maxString.substring(2), 16);
									}else {
										max = Long.parseLong(maxString);
									}
									long curNum = -1;
									Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
									boolean isInt = pattern.matcher(tempString).matches();
									if (tempString.startsWith("0x")) {
										curNum = Long.parseLong(tempString.substring(2), 16);
									}else if(tempString.contains("+") || tempString.contains("-") || tempString.contains("*") || tempString.contains("/")) {
										toCalculate = true;
										String pureCal = tempString.replaceAll("\\(|\\)", "");
										double result = Calculator.conversion(pureCal);
										BigDecimal bd = new BigDecimal(String.valueOf(result));
										curNum = Long.valueOf(bd.toPlainString());	
//										BigDecimal bd = new BigDecimal(String.valueOf(result));
//										curNum = Integer.valueOf(bd.toPlainString());
									} else {
										if (isInt) {
											curNum = Integer.parseInt(tempString);
										}
									}
									if (curNum < min || curNum > max) {
										text.setText("");
										MessageDialog.openError(window.getShell(), "提示",
												"请填写在之" + min + "与" + max + "之间的整数");
									}
								} else if (flag.equals("string")) {
									if(rangesCopy.size()>0) {
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
							if(text.getForeground().equals(table.getDisplay().getSystemColor(SWT.COLOR_RED))) {
								text.setForeground(table.getDisplay().getSystemColor(SWT.COLOR_BLACK));
//								boolean isRight = true;
//								TableItem[] tableItems = table.getItems();
//								for (TableItem tableItem : tableItems) {
//									
//									if (tableItem.getForeground(1)
//											.equals(table.getDisplay().getSystemColor(SWT.COLOR_RED))) {
//										isRight = false;
//										break;
//									}
//								}
//								if (isRight) {
//									eventItem.setImage(CPluginImages.CFG_COMPONENT_OBJ.createImage());
//								}
							}

							comptVisited.add(compName);
							if(toCalculate) {
								item.setText(1, "("+text.getText()+")");
							}else {
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
	
	public void creatProjectConfiure(File file,String coreConfigure,boolean isApp){
		List<Component> compontentsCheckedSort = null;
		if(isApp) {
			compontentsCheckedSort = appCheckedSort;
		}else {
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
		for(int i=0;i<compontentsCheckedSort.size();i++) {		
			if(compontentsCheckedSort.get(i).getConfigure().contains("#define")) {
				defineInit += "//******************************* Configure "+compontentsCheckedSort.get(i).getName()+
												" ******************************************//\n";
				String[] configures = compontentsCheckedSort.get(i).getConfigure().split("\n");
				for(int j=0;j<configures.length;j++) {
					if(configures[j].contains("#define")) {
							String pureDefine = null;
							String annoName = null;
							if(configures[j].trim().startsWith("//")) {
								pureDefine = configures[j].replaceFirst("//", "");
							}else {
								pureDefine = configures[j];
							}
							String[] defines = pureDefine.split("//");
							String[] infos = defines[1].split(",|，");
							if(infos[0].startsWith("\"") && infos[0].endsWith("\"")) {
								annoName = infos[0];
		            		}
							if(annoName == null) {
								defineInit += configures[j]+"\n";	
							}else {
								defineInit += configures[j].replace(annoName,"").replace(",", "").replace("，", "")+"\n";	
							}						
					}
				}
			}		
		}
		defineInit += "//******************************* Core Clock ******************************************//\n";
		defineInit += coreConfigure;
		defineInit += "\n\n#endif";
		
		FileWriter writer;
		try {
			writer = new FileWriter(file);
			writer.write(defineInit);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public void initProject(String path,boolean isApp) {
		String content = "";
		String firstInit = "";
		String lastInit = "\tprintf(\"\\r\\n: info : all modules are configured.\");\r\n" + 
				"\tprintf(\"\\r\\n: info : os starts.\\r\\n\");\n\n";
		String moduleInit = "";
		String djyMain = "";
		initHead = DjyosMessages.Automatically_Generated;
		initHead += "#include \"project_config.h\"\n";
		File file = new File(path+(isApp?"/app":"/iboot")+"/initPrj.c");
		if(file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(isApp) {
			for(int i=0;i<appCompontentsChecked.size();i++) {
				List<String> dependents = appCompontentsChecked.get(i).getDependents();
				List<String> weakDependents = appCompontentsChecked.get(i).getWeakDependents();
				for(int j=i+1;j<appCompontentsChecked.size();j++) {
					if(weakDependents.contains(appCompontentsChecked.get(j))) {
						appCheckedSort.add(appCompontentsChecked.get(j));
					}
				}
				if(!appCheckedSort.contains(appCompontentsChecked.get(i))) {
					appCheckedSort.add(appCompontentsChecked.get(i));
				}		
			}
			for(int i=0;i<appCheckedSort.size();i++) {
				String grade = appCheckedSort.get(i).getGrade();
				String code = appCheckedSort.get(i).getCode();
				String codeStrings = "";
				if(code!=null) {
					String[] codes = code.split("\n");
					for(int j=0;j<codes.length;j++) {
						if(codes[j].contains("#include")) {
							initHead += codes[j].trim()+"\n";
						}else {
							codeStrings += "\t"+codes[j].trim()+"\n";
						}
					}
				}
				
				String componentName = appCheckedSort.get(i).getName();
				
				if(grade!=null && code!=null) {
					if (grade.equals("main")) {//初始化时机为main
						djyMain +=  codeStrings + "\n";
					} else if (grade.equals("init")){//初始化时机为init
						if (componentName.equals("Stdio_KnlInOut")) {
							firstInit +=  codeStrings + "\n";
						} else if (componentName.equals("Heap_Dynamic")) {
							lastInit += codeStrings + "\n";
						} else {
							moduleInit += codeStrings + "\n";
						}
					}
				}
			}
		}else {
			for(int i=0;i<ibootCompontentsChecked.size();i++) {
				List<String> dependents = ibootCompontentsChecked.get(i).getDependents();
				List<String> weakDependents = ibootCompontentsChecked.get(i).getWeakDependents();
				for(int j=i+1;j<ibootCompontentsChecked.size();j++) {
					if(weakDependents.contains(ibootCompontentsChecked.get(j))) {
						ibootCheckedSort.add(ibootCompontentsChecked.get(j));
					}
				}
				if(!ibootCheckedSort.contains(ibootCompontentsChecked.get(i))) {
					ibootCheckedSort.add(ibootCompontentsChecked.get(i));
				}		
			}
			for(int i=0;i<ibootCheckedSort.size();i++) {
				String grade = ibootCheckedSort.get(i).getGrade();
				String code = ibootCheckedSort.get(i).getCode();
				String codeStrings = "";
				if(code!=null) {
					String[] codes = code.split("\n");
					for(int j=0;j<codes.length;j++) {
						if(codes[j].contains("#include")) {
							initHead += codes[j].trim()+"\n";
						}else {
							codeStrings += "\t"+codes[j].trim()+"\n";
						}
					}
				}
				
				String componentName = ibootCheckedSort.get(i).getName();
				
				if(grade!=null && code!=null) {
					if (grade.equals("main")) {//初始化时机为main
						djyMain +=  codeStrings + "\n";
					} else if (grade.equals("init")){//初始化时机为init
						if (componentName.equals("Stdio_KnlInOut")) {
							firstInit +=  codeStrings + "\n";
						} else if (componentName.equals("Heap_Dynamic")) {
							lastInit += codeStrings + "\n";
						} else {
							moduleInit += codeStrings + "\n";
						}
					}
				}
			}
		}
	
		content+=initHead;
		content+="\n"+initStart+firstInit+moduleInit+lastInit+initEnd;
		content+=djyStart+djyMain+djyEnd;
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
		if(isApp) {
			xmlName = "app_component_check.xml";
			curCompontents = appCompontents;
		}else {
			xmlName = "iboot_component_check.xml";
			curCompontents = ibootCompontents;
		}
		CreateCheckXml ccx = new CreateCheckXml();
		File checkFile = new File(path + "/../" + "data/"+xmlName);
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
	
	String djyStart = "ptu32_t __djy_main(void)\r\n" + 
			"{\n";
	String djyEnd = "\tdjy_main();\r\n" + 
			"\treturn 0;\r\n" + 
			"}\n\n";
	String initStart = "void Sys_ModuleInit(void)\r\n" + 
			"{\n";
	String initEnd = "\treturn ;\r\n" + 
			"}\n\n";
	String initHead = null;
	
}
