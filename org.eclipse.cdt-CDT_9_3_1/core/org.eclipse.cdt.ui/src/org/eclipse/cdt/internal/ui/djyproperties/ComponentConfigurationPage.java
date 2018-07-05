package org.eclipse.cdt.internal.ui.djyproperties;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

import org.eclipse.cdt.ui.wizards.component.CmpntCheck;
import org.eclipse.cdt.ui.wizards.component.Component;
import org.eclipse.cdt.ui.wizards.component.CreateCheckXml;
import org.eclipse.cdt.ui.wizards.component.ReadCheckXml;
import org.eclipse.cdt.ui.wizards.component.ReadComponentXml;
import org.eclipse.cdt.ui.wizards.djyosProject.ConfigComponentDialog;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;

import org.eclipse.cdt.internal.ui.CPluginImages;

public class ComponentConfigurationPage extends PropertyPage{

	private String depedents = "依赖组件: ";
	private String mutexs = "互斥组件: ";
	private String chipPath = getEclipsePath()+"djysrc\\bsp\\chipdrv";
	List<Component> compontentsList = null;
	List<Component> allCompontents = new ArrayList<Component>();
	List<Component> compontentsChecked = new ArrayList<Component>();
	List<Component> compontentsCheckedSort = new ArrayList<Component>();
	private ScrolledComposite scrolledComposite;
	List<Component> coreComponents = new ArrayList<Component>();
	List<Component> bspComponents = new ArrayList<Component>();
	List<Component> thirdComponents = new ArrayList<Component>();
	List<Component> userComponents = new ArrayList<Component>();
	private Button[] compontentBtns = null;
	private Button[] configBtns = null;
	public String moduleInit;
	public String defineInit;
	private Text dependentText;
	private Text mutexText;
	private List<CmpntCheck> cmpntChecks = null;
	List<CmpntCheck> cmpnts = new ArrayList<CmpntCheck>();
	private String eclipsePath = getEclipsePath();

	private String getEclipsePath() {
		String fullPath = Platform.getInstallLocation().getURL().toString();
		String eclipsePath = fullPath.substring(6,(fullPath.substring(0,fullPath.length()-1)).lastIndexOf("/")+1);
		return eclipsePath;
	}
	
	public void initDefineForComponent(String path,Component component) { 
		File file = new File(path);
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		defineInit = "";
		defineInit += "#ifndef __INITPROJECT_H__\r\n" + "#define __INITPROJECT_H__\r\n\n"
				+ "#include \"stdint.h\"\n\n";

		if (component.getConfigure() != null)
			defineInit += component.getConfigure();

		defineInit += "\n#endif";
		FileWriter writer;
		try {
			writer = new FileWriter(path);
			writer.write(defineInit);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void initProject(String path) {
		String content = "";
		String firstInit = "";
		String lastInit = "";
		String moduleInit = "";
		String djyMain = "";
		File file = new File(path+"/initPrj.c");
		if(file.exists()) {
			file.delete();
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for(int i=0;i<compontentsChecked.size();i++) {
			System.out.println("compontentsChecked: "+compontentsChecked.get(i).getName());
			List<String> dependents = compontentsChecked.get(i).getDependents();
			List<String> weakDependents = compontentsChecked.get(i).getWeakDependents();
			for(int j=i+1;j<compontentsChecked.size();j++) {
				if(weakDependents.contains(compontentsChecked.get(j))) {
					compontentsCheckedSort.add(compontentsChecked.get(j));
				}
			}
			if(!compontentsCheckedSort.contains(compontentsChecked.get(i))) {
				compontentsCheckedSort.add(compontentsChecked.get(i));
			}		
		}
		for(int i=0;i<compontentsCheckedSort.size();i++) {
			System.out.println("compontentsCheckedSort: "+compontentsCheckedSort.get(i).getName());
			String grade = compontentsCheckedSort.get(i).getGrade();
			String code = compontentsCheckedSort.get(i).getCode();
			String componentName = compontentsCheckedSort.get(i).getName();
			if(compontentsCheckedSort.get(i).getConfigure()!=null && !compontentsCheckedSort.get(i).getConfigure().equals("")) {
				initDefineForComponent(path+"/OS_prjcfg/cfg/"+compontentsCheckedSort.get(i).getFileName()+"_config.h",compontentsCheckedSort.get(i));
			}
			
			if(grade!=null) {
				if (grade.equals("main")) {//初始化时机为main
					djyMain += "\t//" + compontentsCheckedSort.get(i).getAnnotation() + "\n\t" + code + "\n";
				} else if (grade.equals("init")){//初始化时机为init
					if (componentName.equals("Stdio_KnlInOut")) {
						firstInit += "\t//" + compontentsCheckedSort.get(i).getAnnotation() + "\n\t" + code
								+ "\n";
					} else if (componentName.equals("Heap_Dynamic")) {
						lastInit += "\t//" + compontentsCheckedSort.get(i).getAnnotation() + "\n\t" + code + "\n";
					} else {
						moduleInit += "\t//" + compontentsCheckedSort.get(i).getAnnotation() + "\n\t" + code
								+ "\n";
					}
				}
			}
		}
		content+=initHead;
		content+=initStart+firstInit+moduleInit+lastInit+initEnd;
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
		
		IProject project = getProject();
		// 生成component_check.xml文件
		File checkFile = new File(project.getLocation().toString()+ "/data/component_check.xml");
		if (checkFile.exists()) {
			checkFile.delete();			
		}
		try {
			checkFile.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CreateCheckXml ccx = new CreateCheckXml();
//		ccx.createCheck(cmpnts, checkFile);
		
	}

	@Override
	protected void performDefaults() {
		// TODO Auto-generated method stub
		cmpnts = new ArrayList<CmpntCheck>();
		compontentsChecked = new ArrayList<Component>();
		for(int i=0;i<compontentsList.size();i++) {
			//当组件为必选且不需要配置时，不显示在界面上
			if(compontentsList.get(i).getSelectable().equals("必选") && (compontentsList.get(i).getConfigure() == null || compontentsList.get(i).getConfigure().equals(""))) {
				compontentsChecked.add(compontentsList.get(i));
			}
		}
		for (int i = 0; i < compontentBtns.length; i++) {
			compontentBtns[i].setSelection(false);
			Component component = null;
			if(i<coreComponents.size()) {
				component = coreComponents.get(i);
			}else if(i<coreComponents.size()+bspComponents.size()) {
				component = bspComponents.get(i-coreComponents.size());
			}else if(i<coreComponents.size()+bspComponents.size()+thirdComponents.size()) {
				component = thirdComponents.get(i-coreComponents.size()-bspComponents.size());
			}else if(i<allCompontents.size()) {
				component = userComponents.get(i-coreComponents.size()-bspComponents.size()-thirdComponents.size());
			}			
			CmpntCheck cmpntCheck = new CmpntCheck(component.getName(), "false");
			// 当组件为必选且且需要配置时，默认尊重该组件，按钮可操作
			if (component.getSelectable().equals("必选")) {
				compontentBtns[i].setSelection(true);
				compontentBtns[i].setEnabled(false);
				cmpntCheck.setChecked("true");
				configBtns[i].setEnabled(true);
				compontentsChecked.add(component);
			}
			String configure = component.getConfigure();
			if (configure == null || configure.equals("")) {
				configBtns[i].setVisible(false);
			}

			if (cmpntChecks.get(i).isChecked().equals("true") && !compontentBtns[i].getSelection()) {
				compontentBtns[i].setSelection(true);
				configBtns[i].setEnabled(true);
				cmpntCheck.setChecked("true");
			}
			cmpnts.add(cmpntCheck);
			
		}
		super.performDefaults();
	}
	
	@Override
	public boolean performOk() {
		// TODO Auto-generated method stub
		IProject project = getProject();
		File appFile = new File(project.getLocation().toString()+"/src/app/initPrj.c");
		File ibootFile = new File(project.getLocation().toString()+"/src/iboot/initPrj.c");
		
		try {
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Configuration Reset...", 100);
					if(appFile.exists()) {
						File cfgFile = new File(project.getLocation().toString()+"/src/app/OS_prjcfg/cfg");
						File[] files = cfgFile.listFiles();
//						appFile.delete();
						for(File file:files) {
							file.delete();
						}
						initProject(project.getLocation().toString()+"/src/app");
					}
					monitor.worked(50);
					if(ibootFile.exists()) {
						File cfgFile = new File(project.getLocation().toString()+"/src/iboot/OS_prjcfg/cfg");
						File[] files = cfgFile.listFiles();
						for(File file:files) {
							file.delete();
						}
						initProject(project.getLocation().toString()+"/src/iboot");
					}
					monitor.worked(50);
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
	protected Control createContents(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(parent, SWT.NONE);
		initializeDialogUnits(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);
		composite.setLayout(new GridLayout(1,true));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		//给界面添加滚动
		ScrolledComposite scrolledComposite = new ScrolledComposite(composite, SWT.V_SCROLL
                | SWT.H_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite infoArea = new Composite(scrolledComposite, SWT.NONE);
		infoArea.setLayout(new GridLayout());
		GridData data = new GridData(GridData.FILL_BOTH);
		infoArea.setLayoutData(data);
		
		Point point0 = parent.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		Composite aboveCpt = new Composite(infoArea,SWT.BORDER);
		
		aboveCpt.setLayout(new GridLayout(2,true));
		GridData data1 = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_CENTER);
		data1.minimumHeight = 150;
		aboveCpt.setLayoutData(data1);
		
		//显示依赖组件
		dependentText = new Text(aboveCpt, SWT.MULTI | SWT.WRAP);
		GridData depedentData = new GridData(GridData.FILL_BOTH);
		depedentData.grabExcessHorizontalSpace = true;
		dependentText.setLayoutData(depedentData);
		dependentText.setText(depedents);
		dependentText.setEditable(false);
		dependentText.setSize(SWT.HORIZONTAL, 50);
		//显示互斥组件
		mutexText = new Text(aboveCpt, SWT.MULTI | SWT.WRAP);
		mutexText.setLayoutData(depedentData);
		mutexText.setText(mutexs);
		mutexText.setEditable(false);
		
		ReadComponentXml rcx = new ReadComponentXml();
		compontentsList = rcx.getComponents();//获取cpudrv/src下的组件
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
			
			System.out.println(component.getName()+"_getSelectable:  "+component.getSelectable()+"             getConfigure:  "+component.getConfigure());
			//当组件为必选且不需要配置时，不显示在界面上
			if(component.getSelectable().equals("必选") && (component.getConfigure() == null || component.getConfigure().equals(""))) {
				compontentsChecked.add(component);
			}else {
				allCompontents.add(component);
			}
			
//			thePeripherals.add(component);
		}
		
		compontentBtns = new Button[allCompontents.size()];
		configBtns = new Button[allCompontents.size()];
		
		for(int i=0;i<allCompontents.size();i++) {
			if(allCompontents.get(i).getAttribute().equals("核心组件")) {
				System.out.println("核心组件:  "+allCompontents.get(i).getName());
				coreComponents.add(allCompontents.get(i));
			}else if(allCompontents.get(i).getAttribute().equals("bsp组件")) {
				bspComponents.add(allCompontents.get(i));
			}else if(allCompontents.get(i).getAttribute().equals("第三方组件")) {
				thirdComponents.add(allCompontents.get(i));
			}else if(allCompontents.get(i).getAttribute().equals("用户组件")) {
				userComponents.add(allCompontents.get(i));
			}
		}
		
		IProject project = getProject();
		File checkFile = new File(project.getLocation().toString()+"/data/component_check.xml");
		ReadCheckXml rccx = new ReadCheckXml();
		try {
			cmpntChecks = rccx.getCmpntChecks(checkFile);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// 组件显示界面
		TabFolder folder= new TabFolder(infoArea, SWT.NONE);
		folder.setLayout(new TabFolderLayout());
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		TabItem item= new TabItem(folder, SWT.NONE);
		item.setText("核心组件"); //$NON-NLS-1$
		item.setControl(createCoreTabContent(folder,coreComponents));

		item= new TabItem(folder, SWT.NONE);
		item.setText("bsp组件"); //$NON-NLS-1$
		item.setControl(createBspTabContent(folder));
		
		item= new TabItem(folder, SWT.NONE);
		item.setText("三方组件"); //$NON-NLS-1$
		item.setControl(createThirdTabContent(folder));
		
		item= new TabItem(folder, SWT.NONE);
		item.setText("用户组件"); //$NON-NLS-1$
		item.setControl(createUserTabContent(folder));
		
		// 处理组件选择的监听相应
		for (int i = 0; i < configBtns.length; i++) {
			Component component = null;
			int preSize = 0;
			if(i < coreComponents.size()) {
				component = coreComponents.get(i);
			}else if(i < coreComponents.size()+bspComponents.size()) {
				preSize = coreComponents.size();
				component = bspComponents.get(i-preSize);
			}else if(i < coreComponents.size()+bspComponents.size()+thirdComponents.size()) {
				preSize = coreComponents.size()+bspComponents.size();
				component = thirdComponents.get(i-preSize);
			}else{
				preSize = coreComponents.size()+bspComponents.size()+thirdComponents.size();
				component = userComponents.get(i-preSize);
			}
			
			Component curComponent = component;		
			int cur = i;
			configBtns[i].addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
					ConfigComponentDialog dialog = new ConfigComponentDialog(getShell(),curComponent,project,true,null);
					if (dialog.open() == Window.OK) {
						Component _Component = dialog.getComponent();
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// TODO Auto-generated method stub

				}
			});
			compontentBtns[i].addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
					if (compontentBtns[cur].getSelection()) {
						configBtns[cur].setEnabled(true);
						// List<String> dependents = component.getDependents();
						List<String> mutes = curComponent.getMutexs();
						String allDeps = "";
						String allMuts = "";
						boolean isMutex = false;// 是否存在互斥，并且互斥组件已选中
						// 互斥组件
						for (int j = 0; j < mutes.size(); j++) {
							String mutex = mutes.get(j);
							allMuts += " " + mutex;
							for (int k = 0; k < compontentBtns.length; k++) {
								if (mutex.equals(compontentBtns[k].getText())) {
									if (compontentBtns[k].getSelection()) {
										isMutex = true;
										compontentBtns[cur].setSelection(false);
										IWorkbenchWindow window = PlatformUI.getWorkbench()
												.getActiveWorkbenchWindow();
										MessageDialog.openError(window.getShell(), "提示",
												mutex + "组件已勾选，与" + curComponent.getName() + "互斥 ！");
									}
									break;
								}
							}
						}
						// 如果互斥组件未选中，则执行又来关系的监听相应
						if (!isMutex) {
							// 组件的依赖组件不为空，则循环依赖关系，找出所有依赖的组件
							if (curComponent.getDependents().size() != 0) {
								allDeps = getAllDependents(curComponent, allDeps);
							}
							compontentsChecked.add(curComponent);	
						}
						
						if (allDeps.equals("")) {
							dependentText.setText(depedents + " 无");
						} else {
							dependentText.setText(depedents + allDeps);
						}
						if (allMuts.equals("")) {
							mutexText.setText(mutexs + " 无");
						} else {
							mutexText.setText(mutexs + allMuts);
						}
						cmpnts.get(cur).setChecked("true");
						
					} else {
						configBtns[cur].setEnabled(false);
						for (int i = 0; i < compontentsChecked.size(); i++) {
							if (compontentsChecked.get(i).getName().equals(curComponent.getName())) {
								compontentsChecked.remove(i);
								break;
							}
						}
						cmpnts.get(cur).setChecked("false");
					}

				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// TODO Auto-generated method stub

				}
			});
			
		}

		//设置滚动条属性
		Point point = infoArea.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		scrolledComposite.setContent(infoArea);
		scrolledComposite.setMinHeight(point.y);
		scrolledComposite.setExpandHorizontal(true);
	    scrolledComposite.setExpandVertical(true);
	    
		return composite;
	}
	
	private Control createCoreTabContent(TabFolder folder,List<Component> coreComponents) {
		// TODO Auto-generated method stub
		
		Composite componentCpt = new Composite(folder, SWT.BORDER);
		GridLayout componentLayout = new GridLayout(4, true);
		componentCpt.setLayout(componentLayout);
		componentCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
	
		//添加扫描的组件到界面上
		for(int i=0;i<coreComponents.size();i++) {
			Component component = coreComponents.get(i);
			compontentBtns[i] = new Button(componentCpt,SWT.CHECK);
			compontentBtns[i].setText(component.getName());
			CmpntCheck cmpntCheck = new CmpntCheck(component.getName(),"false");
			
			configBtns[i] = new Button(componentCpt,SWT.PUSH);
			configBtns[i].setText("config");
			configBtns[i].setImage(CPluginImages.CFG_CMPT_VIEW.createImage());
			configBtns[i].setEnabled(false);//配置按钮默认不可操作
			//当组件为必选且且需要配置时，默认尊重该组件，按钮可操作
			if(component.getSelectable().equals("必选")) {
				compontentBtns[i].setSelection(true);
				compontentBtns[i].setEnabled(false);
				cmpntCheck.setChecked("true");
				configBtns[i].setEnabled(true);
				compontentsChecked.add(component);
			}
			String configure  = component.getConfigure();
			if(configure==null || configure.equals("")) {
				configBtns[i].setVisible(false);
			}
			
			if(cmpntChecks.get(i).isChecked().equals("true") && !compontentBtns[i].getSelection()) {
				compontentBtns[i].setSelection(true);
				configBtns[i].setEnabled(true);
				cmpntCheck.setChecked("true");
			}
			
			cmpnts.add(cmpntCheck);
			
		}

		return componentCpt;
	}

	private Control createBspTabContent(TabFolder folder) {
		// TODO Auto-generated method stub
		Composite componentCpt = new Composite(folder, SWT.BORDER);
		GridLayout componentLayout = new GridLayout(4, true);
		componentCpt.setLayout(componentLayout);
		componentCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
	
		int preSize = coreComponents.size();
		//添加扫描的组件到界面上
		for(int i=0;i<bspComponents.size();i++) {
			Component component = bspComponents.get(i);
			compontentBtns[preSize+i] = new Button(componentCpt,SWT.CHECK);
			compontentBtns[preSize+i].setText(component.getName());
			CmpntCheck cmpntCheck = new CmpntCheck(component.getName(),"false");
			
			configBtns[preSize+i] = new Button(componentCpt,SWT.PUSH);
			configBtns[preSize+i].setText("config");
			configBtns[preSize+i].setImage(CPluginImages.CFG_CMPT_VIEW.createImage());
			configBtns[preSize+i].setEnabled(false);//配置按钮默认不可操作
			//当组件为必选且且需要配置时，默认尊重该组件，按钮可操作
			if(component.getSelectable().equals("必选")) {
				compontentBtns[preSize+i].setSelection(true);
				compontentBtns[preSize+i].setEnabled(false);
				cmpntCheck.setChecked("true");
				configBtns[preSize+i].setEnabled(true);
				compontentsChecked.add(component);
			}
			String configure  = component.getConfigure();
			if(configure==null || configure.equals("")) {
				configBtns[preSize+i].setVisible(false);
			}
			
			if(cmpntChecks.get(preSize+i).isChecked().equals("true") && !compontentBtns[preSize+i].getSelection()) {
				compontentBtns[preSize+i].setSelection(true);
				configBtns[preSize+i].setEnabled(true);
				cmpntCheck.setChecked("true");
			}
			cmpnts.add(cmpntCheck);
		}
		
		return componentCpt;
	}

	private Control createThirdTabContent(TabFolder folder) {
		// TODO Auto-generated method stub
		Composite componentCpt = new Composite(folder, SWT.BORDER);
		GridLayout componentLayout = new GridLayout(4, true);
		componentCpt.setLayout(componentLayout);
		componentCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
	
		int preSize = coreComponents.size()+bspComponents.size();
		//添加扫描的组件到界面上
		for(int i=0;i<thirdComponents.size();i++) {
			Component component = thirdComponents.get(i);
			compontentBtns[preSize+i] = new Button(componentCpt,SWT.CHECK);
			compontentBtns[preSize+i].setText(component.getName());
			CmpntCheck cmpntCheck = new CmpntCheck(component.getName(),"false");
			
			configBtns[preSize+i] = new Button(componentCpt,SWT.PUSH);
			configBtns[preSize+i].setText("config");
			configBtns[preSize+i].setImage(CPluginImages.CFG_CMPT_VIEW.createImage());
			configBtns[preSize+i].setEnabled(false);//配置按钮默认不可操作
			//当组件为必选且且需要配置时，默认尊重该组件，按钮可操作
			if(component.getSelectable().equals("必选")) {
				compontentBtns[preSize+i].setSelection(true);
				compontentBtns[preSize+i].setEnabled(false);
				cmpntCheck.setChecked("true");
				configBtns[preSize+i].setEnabled(true);
				compontentsChecked.add(component);
			}
			String configure  = component.getConfigure();
			if(configure==null || configure.equals("")) {
				configBtns[preSize+i].setVisible(false);
			}
			if(cmpntChecks.get(preSize+i).isChecked().equals("true") && !compontentBtns[preSize+i].getSelection()) {
				compontentBtns[preSize+i].setSelection(true);
				configBtns[preSize+i].setEnabled(true);
				cmpntCheck.setChecked("true");
			}
			cmpnts.add(cmpntCheck);
		}
		
		return componentCpt;
	}

	private Control createUserTabContent(TabFolder folder) {
		// TODO Auto-generated method stub
		Composite componentCpt = new Composite(folder, SWT.BORDER);
		GridLayout componentLayout = new GridLayout(4, true);
		componentCpt.setLayout(componentLayout);
		componentCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
	
		int preSize = coreComponents.size()+bspComponents.size()+thirdComponents.size();
		//添加扫描的组件到界面上
		for(int i=0;i<userComponents.size();i++) {
			Component component = userComponents.get(i);
			compontentBtns[preSize+i] = new Button(componentCpt,SWT.CHECK);
			compontentBtns[preSize+i].setText(component.getName());
			CmpntCheck cmpntCheck = new CmpntCheck(component.getName(),"false");
			
			configBtns[preSize+i] = new Button(componentCpt,SWT.PUSH);
			configBtns[preSize+i].setText("config");
			configBtns[preSize+i].setImage(CPluginImages.CFG_CMPT_VIEW.createImage());
			configBtns[preSize+i].setEnabled(false);//配置按钮默认不可操作
			//当组件为必选且且需要配置时，默认尊重该组件，按钮可操作
			if(component.getSelectable().equals("必选")) {
				compontentBtns[preSize+i].setSelection(true);
				compontentBtns[preSize+i].setEnabled(false);
				cmpntCheck.setChecked("true");
				configBtns[preSize+i].setEnabled(true);
				compontentsChecked.add(component);
			}
			String configure  = component.getConfigure();
			if(configure==null || configure.equals("")) {
				configBtns[preSize+i].setVisible(false);
			}
			if(cmpntChecks.get(preSize+i).isChecked().equals("true") && !compontentBtns[preSize+i].getSelection()) {
				compontentBtns[preSize+i].setSelection(true);
				configBtns[preSize+i].setEnabled(true);
				cmpntCheck.setChecked("true");
			}
			cmpnts.add(cmpntCheck);
		}
		
		return componentCpt;
	}

	private String getAllDependents(Component component,String allDeps){
		List<String> dependents = component.getDependents();
		//依赖组件
		for(int j=0;j<dependents.size();j++) {
			String dependence = dependents.get(j);
			allDeps+= " "+dependence;
			for(int k=0;k<compontentBtns.length;k++) {
				if(dependence.equals(compontentBtns[k].getText())) {
					Component curComponent = null;
					int preSize = 0;
					if(k < coreComponents.size()) {
						curComponent = coreComponents.get(k);
					}else if(k < coreComponents.size()+bspComponents.size()) {
						preSize = coreComponents.size();
						curComponent = bspComponents.get(k-preSize);
					}else if(k < coreComponents.size()+bspComponents.size()+thirdComponents.size()) {
						preSize = coreComponents.size()+bspComponents.size();
						curComponent = thirdComponents.get(k-preSize);
					}else{
						preSize = coreComponents.size()+bspComponents.size()+thirdComponents.size();
						curComponent = userComponents.get(k-preSize);
					}
					
					if(curComponent.getDependents().size()!=0) {
						getAllDependents(curComponent,allDeps);
					}
					if(!compontentBtns[k].getSelection()) {
						compontentBtns[k].setSelection(true);
						cmpnts.get(k).setChecked("true");
						configBtns[k].setEnabled(true);
						compontentsChecked.add(curComponent);
					}					
					break;
				}
			}
		}
		return allDeps;
	}
	
	private IProject getProject(){
		Object	element	= getElement();
		IProject project	= null;
		
		if (element instanceof IProject) {
			project = (IProject) element;
		} else if (element instanceof IAdaptable) {
			project= ((IAdaptable)element).getAdapter(IProject.class);
		}
		return project;
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
	String initHead = "//----------------------------------------------------\r\n" + 
			"// Copyright (c) 2014, SHENZHEN PENGRUI SOFT CO LTD. All rights reserved.\r\n" + 
			"\r\n" + 
			"// Redistribution and use in source and binary forms, with or without\r\n" + 
			"// modification, are permitted provided that the following conditions are met:\r\n" + 
			"\r\n" + 
			"// 1. Redistributions of source code must retain the above copyright notice,\r\n" + 
			"//    this list of conditions and the following disclaimer.\r\n" + 
			"// 2. Redistributions in binary form must reproduce the above copyright notice,\r\n" + 
			"//    this list of conditions and the following disclaimer in the documentation\r\n" + 
			"//    and/or other materials provided with the distribution.\r\n" + 
			"\r\n" + 
			"// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\"\r\n" + 
			"// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE\r\n" + 
			"// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE\r\n" + 
			"// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE\r\n" + 
			"// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR\r\n" + 
			"// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF\r\n" + 
			"// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS\r\n" + 
			"// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN\r\n" + 
			"// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)\r\n" + 
			"// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE\r\n" + 
			"// POSSIBILITY OF SUCH DAMAGE.\r\n" + 
			"//-----------------------------------------------------------------------------\r\n" + 
			"// Copyright (c) 2014 著作权由深圳鹏瑞软件有限公司所有。著作权人保留一切权利。\r\n" + 
			"//\r\n" + 
			"// 这份授权条款，在使用者符合下列条件的情形下，授予使用者使用及再散播本\r\n" + 
			"// 软件包装原始码及二进位可执行形式的权利，无论此包装是否经改作皆然：\r\n" + 
			"//\r\n" + 
			"// 1. 对于本软件源代码的再散播，必须保留上述的版权宣告、本条件列表，以\r\n" + 
			"//    及下述的免责声明。\r\n" + 
			"// 2. 对于本套件二进位可执行形式的再散播，必须连带以文件以及／或者其他附\r\n" + 
			"//    于散播包装中的媒介方式，重制上述之版权宣告、本条件列表，以及下述\r\n" + 
			"//    的免责声明。\r\n" + 
			"\r\n" + 
			"// 免责声明：本软件是本软件版权持有人以及贡献者以现状（\"as is\"）提供，\r\n" + 
			"// 本软件包装不负任何明示或默示之担保责任，包括但不限于就适售性以及特定目\r\n" + 
			"// 的的适用性为默示性担保。版权持有人及本软件之贡献者，无论任何条件、\r\n" + 
			"// 无论成因或任何责任主义、无论此责任为因合约关系、无过失责任主义或因非违\r\n" + 
			"// 约之侵权（包括过失或其他原因等）而起，对于任何因使用本软件包装所产生的\r\n" + 
			"// 任何直接性、间接性、偶发性、特殊性、惩罚性或任何结果的损害（包括但不限\r\n" + 
			"// 于替代商品或劳务之购用、使用损失、资料损失、利益损失、业务中断等等），\r\n" + 
			"// 不负任何责任，即在该种使用已获事前告知可能会造成此类损害的情形下亦然。\r\n" + 
			"//-----------------------------------------------------------------------------\r\n" + 
			"#include \"stdint.h\"\r\n" + 
			"#include \"stdio.h\"\r\n" + 
			"#include \"board-config.h\"\r\n" + 
			"#include \"driver.h\"\r\n" + 
			"#include \"cpu_peri.h\"\r\n" + 
			"#include \"cpu_peri_uart.h\"\r\n" + 
			"#include \"uartctrl.h\"\r\n" + 
			"#include \"gkernel.h\"\r\n" + 
			"#include \"djyos.h\"\r\n" + 
			"#include \"core_config.h\"\r\n" + 
			"#include \"IO_config.h\"\r\n" + 
			"#include \"timer.h\"\r\n" + 
			"#include \"lowpower.h\"\r\n" + 
			"#include \"list.h\"\r\n" + 
			"#include \"..\\heap\\heap-in.h\"\r\n" + 
			"#include \"ymodem.h\"\r\n" + 
			"\r\n" + 
			"static  const char *gdd_input_dev[]={\r\n" + 
			"\r\n" + 
			"    TOUCH_DEV_NAME,\r\n" + 
			"    KBD_DEV_NAME,\r\n" + 
			"    NULL, //必须要以NULL作为结束标记\r\n" + 
			"\r\n" + 
			"};\r\n" + 
			"\r\n" + 
			"//----操作系统内核组件配置-----------------------------------------------------\r\n" + 
			"//功能：配置和初始化可选功能组件，在用户工程目录中必须实现本函数，在内核初始化\r\n" + 
			"//      阶段调用。\r\n" + 
			"//      本函数实现内核裁剪功能，例如只要注释掉\r\n" + 
			"//          ModuleInstall_Multiplex(0);\r\n" + 
			"//      这一行，多路复用组件就被裁剪掉了。\r\n" + 
			"//      用户可从example中copy本文件，把不要的组件注释掉，强烈建议，不要删除,也\r\n" + 
			"//      不要修改调用顺序。可以把用户模块的初始化代码也加入到本函数中,建议跟在\r\n" + 
			"//      系统模块初始化后面.\r\n" + 
			"//      有些组件有依赖关系,裁剪时,注意阅读注释.\r\n" + 
			"//参数：无\r\n" + 
			"//返回：无\r\n" + 
			"//---------------------------------------------------------------------------\r\n\n"
			+ "align_type InitStack[CFG_INITSTACK_SIZE/sizeof(align_type)] = {'d'};\r\n" + 
			"";
	
}
