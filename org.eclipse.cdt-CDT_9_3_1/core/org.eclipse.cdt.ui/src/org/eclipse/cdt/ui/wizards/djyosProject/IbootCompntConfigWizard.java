package org.eclipse.cdt.ui.wizards.djyosProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
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
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

import org.eclipse.cdt.ui.wizards.board.Board;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.OnBoardCpu;
import org.eclipse.cdt.ui.wizards.component.CmpntCheck;
import org.eclipse.cdt.ui.wizards.component.Component;
import org.eclipse.cdt.ui.wizards.component.CreateCheckXml;
import org.eclipse.cdt.ui.wizards.component.ReadComponent;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;

import org.eclipse.cdt.internal.ui.CPluginImages;

public class IbootCompntConfigWizard extends WizardPage{
	private String depedents = "�������: ";
	private String mutexs = "�������: ";
	List<Component> compontentsList = null;
	List<Component> allCompontents = new ArrayList<Component>();
	List<Component> compontentsChecked = new ArrayList<Component>();
	List<Component> compontentsCheckedSort = new ArrayList<Component>();
	private ScrolledComposite scrolledComposite;
	List<Component> coreComponents = new ArrayList<Component>();
	List<Component> bspComponents = new ArrayList<Component>();
	List<Component> thirdComponents = new ArrayList<Component>();
	List<Component> userComponents = new ArrayList<Component>();
	List<CmpntCheck> cmpnts = new ArrayList<CmpntCheck>();
	private Button[] compontentBtns = null;
	private Button[] configBtns = null;
	public String moduleInit;
	public String defineInit;
	private Text dependentText;
	private Text mutexText;
	private OnBoardCpu onBoardCpu = null;
	private Board sBoard = null;
	
	protected IbootCompntConfigWizard(String pageName,OnBoardCpu cpu,Board board) {
		super(pageName);
		// TODO Auto-generated constructor stub
		onBoardCpu = cpu;
		sBoard = board;
		setPageComplete(true);
	}
	
	public List<Component> getCompontentsChecked(){
		return compontentsChecked;
	}
	
	public void initDefineForComponent(String path,Component component,String componentName) { 
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
		defineInit += "/****************************************************\r\n" + 
				" *  Automatically-generated file. Do not edit!	*\r\n" + 
				" ****************************************************/\r\n\n";
		defineInit += "#ifndef __"+componentName.toUpperCase()
		+"_CONFIG_H__\r\n" + "#define __"+componentName.toUpperCase()+"_CONFIG_H__\r\n\n"
				+ "#include \"stdint.h\"\n\n";

		String[] defines = component.getConfigure().split("\n");
		for(int i=0;i<defines.length;i++) {
			if(defines[i].contains("#define")) {
				defineInit += defines[i]+"\n";
			}
		}

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
		String lastInit = "\tprintf(\"\\r\\n: info : all modules are configured.\");\r\n" + 
				"\tprintf(\"\\r\\n: info : os starts.\\r\\n\");\n\n";
		String moduleInit = "";
		String djyMain = "";
		initHead = "/****************************************************\r\n" + 
				" *  Automatically-generated file. Do not edit!	*\r\n" + 
				" ****************************************************/\r\n\n";
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
		for(int i=0;i<compontentsChecked.size();i++) {
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
			String grade = compontentsCheckedSort.get(i).getGrade();
			String code = compontentsCheckedSort.get(i).getCode();
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
			
			String componentName = compontentsCheckedSort.get(i).getName();
			if(compontentsCheckedSort.get(i).getConfigure().contains("#define")) {
				String filePath = compontentsCheckedSort.get(i).getFileName();
				initDefineForComponent(path+"/OS_prjcfg/cfg/"+filePath.substring(filePath.lastIndexOf("\\")+1, filePath.length())+"_config.h",compontentsCheckedSort.get(i),filePath.substring(filePath.lastIndexOf("\\")+1, filePath.length()));
			}
			
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
		ccx.createCheck(cmpnts, ibootcheckFile);

	}
	
	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(parent, SWT.NONE);
		initializeDialogUnits(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);
		composite.setLayout(new GridLayout(1,true));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		//��������ӹ���
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
		
		//��ʾ�������
		dependentText = new Text(aboveCpt, SWT.MULTI | SWT.WRAP);
		GridData depedentData = new GridData(GridData.FILL_BOTH);
		depedentData.grabExcessHorizontalSpace = true;
		dependentText.setLayoutData(depedentData);
		dependentText.setText(depedents);
		dependentText.setEditable(false);
		dependentText.setSize(SWT.HORIZONTAL, 50);
		//��ʾ�������
		mutexText = new Text(aboveCpt, SWT.MULTI | SWT.WRAP);
		mutexText.setLayoutData(depedentData);
		mutexText.setText(mutexs);
		mutexText.setEditable(false);
		
		ReadComponent rc = new ReadComponent();
//		ReadComponentXml rcx = new ReadComponentXml();
//		compontentsList = rcx.getComponents(onBoardCpu,sBoard);//��ȡcpudrv/src�µ����
		compontentsList = rc.getComponents(onBoardCpu, sBoard);
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
			System.out.println(component.getName()+"_getSelectable:  "+component.getSelectable()+"             getConfigure:  "+component.getConfigure());
			//�����Ϊ��ѡ�Ҳ���Ҫ����ʱ������ʾ�ڽ�����
			if(component.getSelectable().equals("��ѡ") && (component.getConfigure() == null || component.getConfigure().equals(""))) {
				compontentsChecked.add(component);
			}else {
				allCompontents.add(component);
			}
			
//			thePeripherals.add(component);
		}
		
		compontentBtns = new Button[allCompontents.size()];
		configBtns = new Button[allCompontents.size()];
		
		for(int i=0;i<allCompontents.size();i++) {
			if(allCompontents.get(i).getAttribute().equals("�������")) {
//				System.out.println("�������:  "+allCompontents.get(i).getName());
				coreComponents.add(allCompontents.get(i));
			}else if(allCompontents.get(i).getAttribute().equals("bsp���")) {
				bspComponents.add(allCompontents.get(i));
			}else if(allCompontents.get(i).getAttribute().equals("���������")) {
				thirdComponents.add(allCompontents.get(i));
			}else if(allCompontents.get(i).getAttribute().equals("�û����")) {
				userComponents.add(allCompontents.get(i));
			}
		}
		
		// �����ʾ����
		TabFolder folder= new TabFolder(infoArea, SWT.NONE);
		folder.setLayout(new TabFolderLayout());
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		TabItem item= new TabItem(folder, SWT.NONE);
		item.setText("�������"); //$NON-NLS-1$
		item.setControl(createCoreTabContent(folder,coreComponents));

		item= new TabItem(folder, SWT.NONE);
		item.setText("bsp���"); //$NON-NLS-1$
		item.setControl(createBspTabContent(folder));
		
		item= new TabItem(folder, SWT.NONE);
		item.setText("�������"); //$NON-NLS-1$
		item.setControl(createThirdTabContent(folder));
		
		item= new TabItem(folder, SWT.NONE);
		item.setText("�û����"); //$NON-NLS-1$
		item.setControl(createUserTabContent(folder));
		
		// �������ѡ��ļ�����Ӧ
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
					ConfigComponentDialog dialog = new ConfigComponentDialog(getShell(),curComponent);
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
						boolean isMutex = false;// �Ƿ���ڻ��⣬���һ��������ѡ��
						// �������
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
										MessageDialog.openError(window.getShell(), "��ʾ",
												mutex + "����ѹ�ѡ����" + curComponent.getName() + "���� ��");
									}
									break;
								}
							}
						}
						// ����������δѡ�У���ִ��������ϵ�ļ�����Ӧ
						if (!isMutex) {
							// ��������������Ϊ�գ���ѭ��������ϵ���ҳ��������������
							if (curComponent.getDependents().size() != 0) {
								allDeps = getAllDependents(curComponent, allDeps);
							}
							compontentsChecked.add(curComponent);	
						}
						
						if (allDeps.equals("")) {
							dependentText.setText(depedents + " ��");
						} else {
							dependentText.setText(depedents + allDeps);
						}
						if (allMuts.equals("")) {
							mutexText.setText(mutexs + " ��");
						} else {
							mutexText.setText(mutexs + allMuts);
						}
						
						cmpnts.get(cur).setChecked("true");
						
					} else {
						
						List<String> backDepedents = new ArrayList<String>(); 
						for (int i = 0; i < compontentsChecked.size(); i++) {
							if(compontentsChecked.get(i).getDependents().contains(curComponent.getName())) {
								backDepedents.add(compontentsChecked.get(i).getName());
							}						
						}
						if (backDepedents.size() == 0) {
							for (int i = 0; i < compontentsChecked.size(); i++) {
								if (compontentsChecked.get(i).getName().equals(curComponent.getName())) {
									compontentsChecked.remove(i);
									break;
								}
							}
							configBtns[cur].setEnabled(false);
							cmpnts.get(cur).setChecked("false");
						}else {
							String infos = "";
							for(String back:backDepedents) {
								infos+=" "+back;
							}
							compontentBtns[cur].setSelection(true);
							IWorkbenchWindow window = PlatformUI.getWorkbench()
        							.getActiveWorkbenchWindow();
							MessageDialog.openError(window.getShell(), "��ʾ",
        							"�������"+infos+" ���ѹ�ѡ���������������ȡ����ѡ");
						}
						
					}

				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// TODO Auto-generated method stub

				}
			});
		}

		//���ù���������
		Point point = infoArea.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		scrolledComposite.setContent(infoArea);
		scrolledComposite.setMinHeight(point.y);
		scrolledComposite.setExpandHorizontal(true);
	    scrolledComposite.setExpandVertical(true);
	    
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
		Dialog.applyDialogFont(composite);
	}

	private Control createCoreTabContent(TabFolder folder,List<Component> coreComponents) {
		// TODO Auto-generated method stub
//		scrolledComposite = new ScrolledComposite(folder, SWT.V_SCROLL
//                | SWT.H_SCROLL);
//		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite componentCpt = new Composite(folder, SWT.BORDER);
		GridLayout componentLayout = new GridLayout(4, true);
		componentCpt.setLayout(componentLayout);
		componentCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
	
		//���ɨ��������������
		for(int i=0;i<coreComponents.size();i++) {
			Component component = coreComponents.get(i);
			compontentBtns[i] = new Button(componentCpt,SWT.CHECK);
			compontentBtns[i].setText(component.getName());
			CmpntCheck cmpntCheck = new CmpntCheck(component.getName(),"false");
			
			configBtns[i] = new Button(componentCpt,SWT.PUSH);
			configBtns[i].setText("config");
			configBtns[i].setImage(CPluginImages.CFG_CMPT_VIEW.createImage());
			configBtns[i].setEnabled(false);//���ð�ťĬ�ϲ��ɲ���
			//�����Ϊ��ѡ������Ҫ����ʱ��Ĭ�����ظ��������ť�ɲ���
			if(component.getSelectable().equals("��ѡ")) {
				compontentBtns[i].setSelection(true);
				cmpntCheck.setChecked("true");
				compontentBtns[i].setEnabled(false);
				configBtns[i].setEnabled(true);
				compontentsChecked.add(component);
			}
			String configure  = component.getConfigure();
			if(!configure.contains("#define")) {
				configBtns[i].setVisible(false);
			}
			cmpnts.add(cmpntCheck);
		}
//		
//		Point point = componentCpt.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
//		scrolledComposite.setContent(componentCpt);
//		scrolledComposite.setMinHeight(point.y);
//		scrolledComposite.setExpandHorizontal(true);
//	    scrolledComposite.setExpandVertical(true);
//	    folder.layout();
		return componentCpt;
	}

	private Control createBspTabContent(TabFolder folder) {
		// TODO Auto-generated method stub
		Composite componentCpt = new Composite(folder, SWT.BORDER);
		GridLayout componentLayout = new GridLayout(4, true);
		componentCpt.setLayout(componentLayout);
		componentCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
	
		int preSize = coreComponents.size();
		//���ɨ��������������
		for(int i=0;i<bspComponents.size();i++) {
			Component component = bspComponents.get(i);
			compontentBtns[preSize+i] = new Button(componentCpt,SWT.CHECK);
			compontentBtns[preSize+i].setText(component.getName());
			CmpntCheck cmpntCheck = new CmpntCheck(component.getName(),"false");
			
			configBtns[preSize+i] = new Button(componentCpt,SWT.PUSH);
			configBtns[preSize+i].setText("config");
			configBtns[preSize+i].setImage(CPluginImages.CFG_CMPT_VIEW.createImage());
			configBtns[preSize+i].setEnabled(false);//���ð�ťĬ�ϲ��ɲ���
			//�����Ϊ��ѡ������Ҫ����ʱ��Ĭ�����ظ��������ť�ɲ���
			if(component.getSelectable().equals("��ѡ")) {
				compontentBtns[preSize+i].setSelection(true);
				cmpntCheck.setChecked("true");
				compontentBtns[preSize+i].setEnabled(false);
				configBtns[preSize+i].setEnabled(true);
				compontentsChecked.add(component);
			}
			String configure  = component.getConfigure();
			if(!configure.contains("#define")) {
				configBtns[preSize+i].setVisible(false);
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
		//���ɨ��������������
		for(int i=0;i<thirdComponents.size();i++) {
			Component component = thirdComponents.get(i);
			compontentBtns[preSize+i] = new Button(componentCpt,SWT.CHECK);
			compontentBtns[preSize+i].setText(component.getName());
			CmpntCheck cmpntCheck = new CmpntCheck(component.getName(),"false");
			
			configBtns[preSize+i] = new Button(componentCpt,SWT.PUSH);
			configBtns[preSize+i].setText("config");
			configBtns[preSize+i].setImage(CPluginImages.CFG_CMPT_VIEW.createImage());
			configBtns[preSize+i].setEnabled(false);//���ð�ťĬ�ϲ��ɲ���
			//�����Ϊ��ѡ������Ҫ����ʱ��Ĭ�����ظ��������ť�ɲ���
			if(component.getSelectable().equals("��ѡ")) {
				compontentBtns[preSize+i].setSelection(true);
				cmpntCheck.setChecked("true");
				compontentBtns[preSize+i].setEnabled(false);
				configBtns[preSize+i].setEnabled(true);
				compontentsChecked.add(component);
			}
			String configure  = component.getConfigure();
			if(!configure.contains("#define")) {
				configBtns[preSize+i].setVisible(false);
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
		//���ɨ��������������
		for(int i=0;i<userComponents.size();i++) {
			Component component = userComponents.get(i);
			compontentBtns[preSize+i] = new Button(componentCpt,SWT.CHECK);
			compontentBtns[preSize+i].setText(component.getName());
			CmpntCheck cmpntCheck = new CmpntCheck(component.getName(),"false");
			
			configBtns[preSize+i] = new Button(componentCpt,SWT.PUSH);
			configBtns[preSize+i].setText("config");
			configBtns[preSize+i].setImage(CPluginImages.CFG_CMPT_VIEW.createImage());
			configBtns[preSize+i].setEnabled(false);//���ð�ťĬ�ϲ��ɲ���
			//�����Ϊ��ѡ������Ҫ����ʱ��Ĭ�����ظ��������ť�ɲ���
			if(component.getSelectable().equals("��ѡ")) {
				compontentBtns[preSize+i].setSelection(true);
				cmpntCheck.setChecked("true");
				compontentBtns[preSize+i].setEnabled(false);
				configBtns[preSize+i].setEnabled(true);
				compontentsChecked.add(component);
			}
			String configure  = component.getConfigure();
			if(!configure.contains("#define")) {
				configBtns[preSize+i].setVisible(false);
			}
			cmpnts.add(cmpntCheck);
		}
		
		return componentCpt;
	}

	private String getAllDependents(Component component,String allDeps){
		List<String> dependents = component.getDependents();
		//�������
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

	private String getEclipsePath() {
		String fullPath = Platform.getInstallLocation().getURL().toString();
		String eclipsePath = fullPath.substring(6,(fullPath.substring(0,fullPath.length()-1)).lastIndexOf("/")+1);
		return eclipsePath;
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
