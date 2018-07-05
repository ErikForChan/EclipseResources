package org.eclipse.cdt.ui.wizards.djyosProject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

import org.eclipse.cdt.ui.wizards.board.Board;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.OnBoardCpu;
import org.eclipse.cdt.ui.wizards.component.CmpntCheck;
import org.eclipse.cdt.ui.wizards.component.Component;
import org.eclipse.cdt.ui.wizards.component.CreateCheckXml;
import org.eclipse.cdt.ui.wizards.component.ReadComponent;
import org.eclipse.cdt.ui.wizards.cpu.core.Core;
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
	private String depedentLabel = "�������: ", mutexLabel = "�������: ";
	private Group configGroup = null;
	private ArrayList<Control> tabelControls = new ArrayList<Control>();
	private TableEditor editor;
	private String defineInit;
	private boolean appExist,ibootExist;
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
	
	public void creatProjectConfiure(String srcPath,Core core,boolean isApp){
		List<Component> compontentsCheckedSort = null;
		String cfgPath = null;
		//File file = new File(path+(isApp?"/app":"/iboot")+"/initPrj.c");
		if(isApp) {
			compontentsCheckedSort = appCheckedSort;
			cfgPath = srcPath+"/app/OS_prjcfg/project_config.h";
		}else {
			compontentsCheckedSort = ibootCheckedSort;
			cfgPath = srcPath+"/iboot/OS_prjcfg/project_config.h";
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
		for(int i=0;i<compontentsCheckedSort.size();i++) {		
			if(compontentsCheckedSort.get(i).getConfigure().contains("#define")) {
				defineInit += "//******************************* Configure "+compontentsCheckedSort.get(i).getName()+
												" ******************************************//\n";
//				String filePath = compontentsCheckedSort.get(i).getFileName();
				//filePath.substring(filePath.lastIndexOf("\\")+1, filePath.length()
//				initDefineForComponent(path+"/OS_prjcfg/cfg/"+compontentsCheckedSort.get(i).getName()+"_config.h",compontentsCheckedSort.get(i));
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
							String[] infos = defines[1].split(",|��");
							if(infos[0].startsWith("\"") && infos[0].endsWith("\"")) {
								annoName = infos[0];
		            		}
							if(annoName == null) {
								defineInit += configures[j]+"\n";	
							}else {
								defineInit += configures[j].replace(annoName,"").replace(",", "").replace("��", "")+"\n";	
							}						
					}
				}
			}		
		}
		defineInit += "//******************************* Core Clock ******************************************//\n";
		defineInit += String.format("%-9s", "#define")+String.format("%-32s","CFG_CORE_MCLK")+String.format("%-18s", "("+core.getCoreClk()+"*Mhz)")+"//��Ƶ���ں�Ҫ�ã����붨��";
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
					if (grade.equals("main")) {//��ʼ��ʱ��Ϊmain
						djyMain +=  codeStrings + "\n";
					} else if (grade.equals("init")){//��ʼ��ʱ��Ϊinit
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
					if (grade.equals("main")) {//��ʼ��ʱ��Ϊmain
						djyMain +=  codeStrings + "\n";
					} else if (grade.equals("init")){//��ʼ��ʱ��Ϊinit
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
		
		// ����component_check.xml�ļ�
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
	
	public void appInitProject(String path) {
		String content = "";
		String firstInit = "";
		String lastInit = "\tprintf(\"\\r\\n: info : all modules are configured.\");\r\n" + 
				"\tprintf(\"\\r\\n: info : os starts.\\r\\n\");\n\n";
		String moduleInit = "";
		String djyMain = "";
		initHead = DjyosMessages.Automatically_Generated;
		initHead += "#include \"project_config.h\"\n";
		File file = new File(path+"/initPrj.c");
		if(file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
				if (grade.equals("main")) {//��ʼ��ʱ��Ϊmain
					djyMain +=  codeStrings + "\n";
				} else if (grade.equals("init")){//��ʼ��ʱ��Ϊinit
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
		
		// ����component_check.xml�ļ�
		CreateCheckXml ccx = new CreateCheckXml();
		File appcheckFile = new File(path + "/../" + "data/app_component_check.xml");
		if (appcheckFile.exists()) {
			appcheckFile.delete();
		}
		try {
			appcheckFile.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ccx.createCheck(appCompontents, appcheckFile);

	}
		
	public void ibootInitProject(String path) {
		String content = "";
		String firstInit = "";
		String lastInit = "\tprintf(\"\\r\\n: info : all modules are configured.\");\r\n" + 
				"\tprintf(\"\\r\\n: info : os starts.\\r\\n\");\n\n";
		String moduleInit = "";
		String djyMain = "";
		initHead = DjyosMessages.Automatically_Generated;
		initHead += "#include \"project_config.h\"\n";
		File file = new File(path+"/initPrj.c");
		if(file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
				if (grade.equals("main")) {//��ʼ��ʱ��Ϊmain
					djyMain +=  codeStrings + "\n";
				} else if (grade.equals("init")){//��ʼ��ʱ��Ϊinit
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
		
		// ����component_check.xml�ļ�
		CreateCheckXml ccx = new CreateCheckXml();
		File ibootcheckFile = new File(path + "/../../" + "data/iboot_component_check.xml");
		if (ibootcheckFile.exists()) {
			ibootcheckFile.delete();
		}
		try {
			ibootcheckFile.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ccx.createCheck(ibootCompontents, ibootcheckFile);

	}
	
	protected ComponentConfigWizard(String pageName,OnBoardCpu cpu,Board board,boolean haveApp,boolean haveIboot) {
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
	 * �ж�����Ƿ��������
	 */
	private boolean haveChildren(Component parent,List<Component> componentList) {
		for(Component compt:componentList) {
			if(compt.getParent().equals(parent.getName())) {
				return true;
			}
		}
		return false;
	}

	private void createDynamicGroup(Composite composite) {
		// TODO Auto-generated method stub
		Composite depedentCpt = new Composite(composite, SWT.BORDER);
		depedentCpt.setLayout(new GridLayout(2, true));
		depedentCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_CENTER));
		// ��ʾ�������
		dependentText = new Text(depedentCpt, SWT.MULTI | SWT.WRAP);
		GridData depedentData = new GridData(GridData.FILL_BOTH);
		depedentData.grabExcessHorizontalSpace = true;
		dependentText.setLayoutData(depedentData);
		dependentText.setText(depedentLabel);
		dependentText.setEditable(false);
		dependentText.setSize(SWT.HORIZONTAL, 50);
		// ��ʾ�������
		mutexText = new Text(depedentCpt, SWT.MULTI | SWT.WRAP);
		mutexText.setLayoutData(depedentData);
		mutexText.setText(mutexLabel);
		mutexText.setEditable(false);
		
		ScrolledComposite scrolledComposite = new ScrolledComposite(composite, SWT.V_SCROLL
                | SWT.H_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));	

		Composite infoArea = new Composite(scrolledComposite, SWT.NULL);
		infoArea.setLayout(new GridLayout(1, true));
		infoArea.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		ReadComponent rc = new ReadComponent();
		compontentsList = rc.getComponents(onBoardCpu, sBoard);
		if(appExist){
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
				//�����Ϊ��ѡ�Ҳ���Ҫ����ʱ������ʾ�ڽ�����
				if(component.getSelectable().equals("��ѡ") && (!component.getConfigure().contains("#define"))) {
					appCompontentsChecked.add(component);
				}else {
					appCompontents.add(component);
				}
			}
			
			for(int i=0;i<appCompontents.size();i++) {
				if(appCompontents.get(i).getAttribute().equals("�������")) {
					appCoreComponents.add(appCompontents.get(i));
				}else if(appCompontents.get(i).getAttribute().equals("bsp���")) {
					appBspComponents.add(appCompontents.get(i));
				}else if(appCompontents.get(i).getAttribute().equals("���������")) {
					appThirdComponents.add(appCompontents.get(i));
				}else if(appCompontents.get(i).getAttribute().equals("�û����")) {
					appUserComponents.add(appCompontents.get(i));
				}
			}
		}
		
		if(ibootExist) {
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
				//�����Ϊ��ѡ�Ҳ���Ҫ����ʱ������ʾ�ڽ�����
				if(component.getSelectable().equals("��ѡ") && (!component.getConfigure().contains("#define"))) {
					ibootCompontentsChecked.add(component);
				}else {
					ibootCompontents.add(component);
				}
			}

			for(int i=0;i<ibootCompontents.size();i++) {
				if(ibootCompontents.get(i).getAttribute().equals("�������")) {
					ibootCoreComponents.add(ibootCompontents.get(i));
				}else if(ibootCompontents.get(i).getAttribute().equals("bsp���")) {
					ibootBspComponents.add(ibootCompontents.get(i));
				}else if(ibootCompontents.get(i).getAttribute().equals("���������")) {
					ibootThirdComponents.add(ibootCompontents.get(i));
				}else if(ibootCompontents.get(i).getAttribute().equals("�û����")) {
					ibootUserComponents.add(ibootCompontents.get(i));
				}
			}
		}

		// �����ʾ����
		folder = new TabFolder(infoArea, SWT.NONE);
		folder.setLayout(new TabFolderLayout());
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));

		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText("�������"); //$NON-NLS-1$
		item.setControl(createTabContent(folder,appCoreComponents,ibootCoreComponents));

		item = new TabItem(folder, SWT.NONE);
		item.setText("bsp���"); //$NON-NLS-1$
		item.setControl(createTabContent(folder,appBspComponents,ibootBspComponents));

		item = new TabItem(folder, SWT.NONE);
		item.setText("�������"); //$NON-NLS-1$
		item.setControl(createTabContent(folder,appThirdComponents,ibootThirdComponents));

		item = new TabItem(folder, SWT.NONE);
		item.setText("�û����"); //$NON-NLS-1$
		item.setControl(createTabContent(folder,appUserComponents,ibootUserComponents));

		Control[] controls = folder.getChildren();
		Tree coreTree = (Tree)controls[0];
		TreeItem[] coreItems = coreTree.getItems();
		configGroup = ControlFactory.createGroup(infoArea, "�������[��ѡ��Ҫ���õ����]", 1);
		configGroup.setLayout(new GridLayout(1, false));
		configGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		creatConfigTable(configGroup);
		table.setEnabled(false);
		
		Point point = infoArea.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		scrolledComposite.setContent(infoArea);
		scrolledComposite.setMinHeight(point.y);
		scrolledComposite.setExpandHorizontal(true);
	    scrolledComposite.setExpandVertical(true);
	}

	private Control createTabContent(TabFolder folder,List<Component> appTypeComponents,List<Component> ibootTypeComponents) {
		// TODO Auto-generated method stub
		//configGroup
		Tree componentTree = new Tree(folder, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.CHECK);
		componentTree.setLayoutData(new GridData(GridData.FILL_BOTH));
		componentTree.setSize(SWT.FILL,300);
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
				if(component.getSelectable().equals("��ѡ")) 
				{
					item = new TreeItem(rootApp, SWT.ERROR_CANNOT_SET_ENABLED);
					item.setChecked(true);
					component.setSelect(true);
					appCompontentsChecked.add(component);
				}else {
					item = new TreeItem(rootApp, 0);
				}
				item.setText(component.getName());
				item.setImage(CPluginImages.CFG_COMPONENT_OBJ.createImage());
				
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
				if(component.getSelectable().equals("��ѡ")) 
				{
					item = new TreeItem(rootIboot, SWT.ERROR_CANNOT_SET_ENABLED);
					item.setChecked(true);
					component.setSelect(true);
					ibootCompontentsChecked.add(component);
				}else {
					item = new TreeItem(rootIboot, 0);
				}
				item.setText(component.getName());
				item.setImage(CPluginImages.CFG_COMPONENT_OBJ.createImage());
				// Ҷ�ӽڵ��Ӧ����ֵΪ��Ӧ�ļ��е�File����
				if(haveChildren(component,ibootTypeComponents)) {
					fillItem(item,ibootTypeComponents,rootIboot,false);
				}		
			}
		}

		//�������ѡ���¼�
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
						//�жϵ�ǰѡ���������ѡ������Ƿ��л��⣬���û�л��������������
						if(type!=null) {
							boolean isApp ;
							if(type.equals("App")) {
								isApp = true;
								itemCompt = getAppComponent(item.getText());
							}else{
								isApp = false;
								itemCompt = getIbootComponent(item.getText());
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
						//ȡ��ѡ�е�ǰ���ʱ���ж���ѡ������Ƿ������ڴ�����������������������ȡ������ʾ
						if(type!=null) {
							boolean isApp ;
							if(type.equals("App")) {
								isApp = true;
								itemCompt = getAppComponent(item.getText());
							}else{
								isApp = false;
								itemCompt = getIbootComponent(item.getText());
							}
							for(Control c:controls) {
								Tree tempTree = (Tree)c;
								TreeItem[] fChilds = tempTree.getItems();
								for(TreeItem treeItem : fChilds) {
									if(treeItem.getText().equals(type)) {
										boolean isDepedent = isDepedent(treeItem,item,type);
										if(isDepedent) {
											itemCompt.setSelect(false);
											if(isApp) {
												appCompontentsChecked.remove(itemCompt);
											}else {
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
		});
		//������ĵ���¼�
		componentTree.addListener(SWT.MouseDown, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				
				//dependentText mutexText
				Point point = new Point(event.x, event.y);
				TreeItem item = componentTree.getItem(point);
				if(item != null) {
					for(Control control:tabelControls){
						control.dispose();
						}
					if(editor!=null) {
						editor.dispose();
					}
					
					table.removeAll();
					if(!item.getText().equals("App") && !item.getText().equals("Iboot") ) {
						String itemText = item.getText();
						String type = getAIType(item);
						
						Component itemCompt ;
						if(type.equals("App")) {
							itemCompt = getAppComponent(item.getText());
						}else{
							itemCompt = getIbootComponent(item.getText());
						}
						List<String> depedents = itemCompt.getDependents();
						List<String> mutexs = itemCompt.getMutexs();
						String allDeps = "";
						String allMuts = "";
						// �������
						for(String mutex : mutexs) {
							allMuts += " " + mutex;					
						}
						for(String dep : depedents) {
							allDeps += " "+dep;
						}

						if (allDeps.equals("")) {
							dependentText.setText(depedentLabel + " ��");
						} else {
							dependentText.setText(depedentLabel + allDeps);
						}
						if (allMuts.equals("")) {
							mutexText.setText(mutexLabel + " ��");
						} else {
							mutexText.setText(mutexLabel + allMuts);
						}
						
						String configure = itemCompt.getConfigure();	
						if(!configure.contains("#define")) {
							configGroup.setText("��� ["+itemText+"] ��������");
							table.setEnabled(false);
//							item.setForeground(folder.getDisplay().getSystemColor(SWT.COLOR_GRAY));
						}else {
							configGroup.setText(type+"��� ["+itemText+"] ����");
							table.setEnabled(true);
						}					
						initTable(itemCompt);	
					}else {
						configGroup.setText("�������[��ѡ��Ҫ���õ����]");
						table.setEnabled(false);
					}			
				}		
			}
		});
		
		componentTree.addListener(SWT.NO_FOCUS, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				Point point = new Point(event.x, event.y);
				TreeItem item = componentTree.getItem(point);
				if(item != null) {
					System.out.println("NO_FOCUS:  "+item.getText());
					TableItem[] tItems = table.getItems();
					for(TableItem t : tItems) {
						System.out.println(t.getText(0)+"   "+t.getText(1)+"   "+t.getText(2));
					}
				}
			}
		});
		return componentTree;
	}

	protected boolean isDepedent(TreeItem treeItem, TreeItem eventTreeItem ,String type) {
		// TODO Auto-generated method stub
		TreeItem[] items = treeItem.getItems();
		boolean isDepedent = true;
		for(TreeItem item : items) {
			Component tempCompt ;
			if(type.equals("App")) {
				tempCompt = getAppComponent(item.getText());
			}else{
				tempCompt = getIbootComponent(item.getText());
			}
			if(item.getChecked()) {
				if(tempCompt.getDependents().contains(eventTreeItem.getText())) {
					eventTreeItem.setChecked(true);
					isDepedent = false;
					IWorkbenchWindow window = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow();
					MessageDialog.openError(window.getShell(), "��ʾ",
							"�������" + tempCompt.getName() + " ���ѹ�ѡ���������������ȡ����ѡ");
				}
			}

			if(item.getItems().length>0 && isDepedent) {
				isDepedent = isDepedent(item,eventTreeItem,type);
			}
		}
		return isDepedent;
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
					MessageDialog.openError(window.getShell(), "��ʾ",
							item.getText() + "����ѹ�ѡ����" + itemCompt.getName() + "���� ��");				
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
					Component curComponent = getAppComponent(item.getText());
					curComponent.setSelect(true);
					appCompontentsChecked.add(curComponent);
				}else {
					Component curComponent = getIbootComponent(item.getText());
					curComponent.setSelect(true);
					ibootCompontentsChecked.add(curComponent);
				}
			}
			travelItems_Depedent(item,itemCompt,isApp);
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
			if(child.getSelectable().equals("��ѡ")) 
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
			item.setText(child.getName());
			item.setImage(CPluginImages.CFG_COMPONENT_OBJ.createImage());
			
			if(haveChildren(child,compontentsList)) {
				fillItem(item,compontentsList,rootItem,isApp);
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
		// ������ͷ���ַ�������
		String[] tableHeader = { "����", "ֵ", "ע��" };
		for (int i = 0; i < tableHeader.length; i++) {
			TableColumn tableColumn = new TableColumn(table, SWT.NONE | SWT.CENTER);
			tableColumn.setAlignment(SWT.LEFT);
			tableColumn.setText(tableHeader[i]);

			// ���ñ�ͷ���ƶ���Ĭ��Ϊfalse
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

				infos = parameter.split(",");
				ranges = new ArrayList<String>();
				if (!tag.equals("select") && !tag.equals("free")) {
					for (int j = 1; j < infos.length; j++) {// for (int j = 1; j < infos.length; j++)
						ranges.add(infos[j]);
					}
				}

			}

			if (parameter.contains("#define")) {
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
				String[] annos = annoation.split(",");
				if (annos[0].trim().startsWith("\"") && annos[0].trim().endsWith("\"")) {
					annoation = annoation.substring(annos[0].length() + 1, annoation.length());
					if (!annos[0].contains("name")) {
						realComptName = annos[0].trim().replaceAll("\"", "");
					} else {
						realComptName = members[1];
					}

				} else {
					realComptName = members[1];
				}
				if (members.length > 2) {
					item.setText(new String[] { realComptName, members[2].equals("default") ? "" : members[2],
							defines.length > 1 ? annoation : "" });

				} else {
					item.setText(new String[] { realComptName, "", defines.length > 1 ? annoation : "" });
				}

				editor = new TableEditor(table);
				// ���ñ༭��Ԫ��ˮƽ���
				editor.grabHorizontal = true;

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
					// ����һ���ı���������������
					Text text = new Text(table, SWT.BORDER);
					text.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
					// ���ı���ǰֵ������Ϊ����е�ֵ
					text.setText(item.getText(1));
					// �ؼ����������༭��Ԫ�����ı���󶨵����ĵ�һ��
					editor.setEditor(text, item, 1);
					tabelControls.add(text);
					String flag = tag;
					List<String> rangesCopy = ranges;
					// ���ı���ı�ֵʱ,ע���ı���ı��¼������¼��ı����е�����,����ʹ�ı���ı����ֵ���Ա���е�����Ҳ����Ӱ��
					text.addModifyListener(new ModifyListener() {
						public void modifyText(ModifyEvent e) {
							String tempString = text.getText();
							IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
							if (rangesCopy.size() > 0) {
								if (flag.equals("int")) {
									int min = Integer.parseInt(rangesCopy.get(0));
									int max = Integer.parseInt(rangesCopy.get(1));
									int curNum = -1;
									Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
									boolean isInt = pattern.matcher(tempString).matches();
									if (tempString.startsWith("0x")) {
										curNum = Integer.parseInt(tempString.substring(2), 16);
									} else {
										if (isInt) {
											curNum = Integer.parseInt(tempString);
										} else {
											MessageDialog.openError(window.getShell(), "��ʾ",
													"����д��֮" + min + "��" + max + "֮�������");
										}

									}
									if (curNum < min || curNum > max) {
										MessageDialog.openError(window.getShell(), "��ʾ",
												"����д��֮" + min + "��" + max + "֮�������");
									}
								} else if (flag.equals("string")) {
									int min = Integer.parseInt(rangesCopy.get(0));
									int max = Integer.parseInt(rangesCopy.get(1));
									if (tempString.length() < min || tempString.length() > max) {
										MessageDialog.openError(window.getShell(), "��ʾ",
												"�ַ������Ȳ���С��" + min + "���ߴ���" + max);
									}
								}
							}

							item.setText(1, text.getText());
							resetConfigure(componentSelect);
						}

					});
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
		//������define����ֵ
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
	        	//define��ʽ��
            	if(isSelect[i]) {
        			parametersDefined[i] = String.format("%-11s",members[0])+" "+String.format("%-32s", members[1])+" "+String.format("%-18s", items[itemCount].getText(1))+"//"+annoation;
        		}else {
        			parametersDefined[i] = String.format("%-11s","//"+members[0])+" "+String.format("%-32s", members[1])+" "+String.format("%-18s", items[itemCount].getText(1))+"//"+annoation;
        		}
							
				itemCount++;
			}
			newConfig += parametersDefined[i]+"\n";
		}
		componentSelect.setConfigure(newConfig);
	}

	private Component getAppComponent(String itemName) {
		for(Component component:appCompontents) {
			if(component.getName().equals(itemName)) {
				return component;
			}
		}	
		return null;
	}
	
	private Component getIbootComponent(String itemName) {
		for(Component component:ibootCompontents) {
			if(component.getName().equals(itemName)) {
				return component;
			}
		}	
		return null;
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
