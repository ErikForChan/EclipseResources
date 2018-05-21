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
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

import org.eclipse.cdt.ui.wizards.component.Component;
import org.eclipse.cdt.ui.wizards.component.InitInfo;
import org.eclipse.cdt.ui.wizards.component.Parameter;
import org.eclipse.cdt.ui.wizards.component.ReadComponentXml;

import org.eclipse.cdt.internal.ui.djyproperties.Module;

public class InitDjyosProjectWizard extends WizardPage{

	private String depedents = "�������: ";
	private String mutexs = "�������: ";
	private String chipPath = getEclipsePath()+"djysrc\\bsp\\chipdrv";
	List<Component> compontentsList = null;
	List<Component> allCompontents = new ArrayList<Component>();
	List<Component> compontentsChecked = new ArrayList<Component>();
	private Button[] compontentBtns = null;
	private Button[] configBtns = null;
	public String moduleInit;
	
	protected InitDjyosProjectWizard(String pageName) {
		super(pageName);
		// TODO Auto-generated constructor stub
		setPageComplete(true);
	}
	
	public List<Component> getCompontentsChecked(){
		return compontentsChecked;
	}
	
	//����moduleinit.h�ļ�
	public void fillModuleinit(String path) {
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
		moduleInit = "";
		moduleInit+="#ifndef __MODULEINIT_H__\r\n" + 
				"#define __MODULEINIT_H__\r\n\n"+
				"#include \"stdint.h\"\n\n";
		
		for(int i = 0;i<compontentsChecked.size();i++) {
			moduleInit+="#define MODULEINIT_"+compontentsChecked.get(i).getName().toUpperCase()+"\n";
		}
//		for (int i = 0; i < allModules.size(); i++) {
//			if(! allModules.get(i).getParent().equals("root")) {
//				allModules.get(i).setVisited(false);
//			}		
//		}
//		childCount = 0;
//		for(int i = 0;i<allModules.size();i++) {
//			boolean checked = false;
//			Module childModule = allModules.get(i);
//			if(childModule.getParent().equals("root")) {
//				if(allItems[i].getChecked()) {
//					moduleInit+="#define MODULEINIT_"+allModules.get(i).getName()+"\n";
//					checked = true;
//				}
//				childCount++;
//				TreeItem curItem = allItems[i];
//				fillInit(curItem,childModule,0);
//				if(checked) {
//					moduleInit+="\n";
//				}	
//			}			
//		}
		moduleInit+="\n#endif";
		
		FileWriter writer;
		try {
			writer = new FileWriter(path);
			writer.write(moduleInit);
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
		File file = new File(path);
		if(!file.exists()) {
			file.delete();
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for(int i=0;i<compontentsChecked.size();i++) {
//			System.out.println(compontentsChecked.get(i).getName());
//			InitInfo initInfo = compontentsChecked.get(i).getInit();
//			String grade = initInfo.getGrade();
//			String funName = initInfo.getFunName();
			String grade = compontentsChecked.get(i).getGrade();
			String code = compontentsChecked.get(i).getCode();
			if(grade!=null) {
				if(grade.equals("runos")) {
					djyMain += "\t//"+code+"\n";
//					djyMain += "\t//"+ compontentsChecked.get(i).getAnnotation()
//							+"\n\t"+funName+"(";
//					for(int j=0;j<initInfo.getParameters().size();j++) {
//						Parameter curParameter = initInfo.getParameters().get(j);
//						if(j == initInfo.getParameters().size()-1) {
//							djyMain += curParameter.getValue();
//						}else {
//							djyMain += curParameter.getValue()+",";
//						}
//					}
//					djyMain+=");\n";
				}else if(grade.equals("first")){
					firstInit += "\t//"+code+"\n";
//					firstInit += "\t//"+ compontentsChecked.get(i).getAnnotation()
//							+"\n\t"+funName+"(";
//					for(int j=0;j<initInfo.getParameters().size();j++) {
//						Parameter curParameter = initInfo.getParameters().get(j);
//						if(j == initInfo.getParameters().size()-1) {
//							firstInit += curParameter.getValue();
//						}else {
//							firstInit += curParameter.getValue()+",";
//						}
//					}
//					firstInit+=");\n";
				}else if(grade.equals("last")){
					lastInit += "\t//"+code+"\n";
//					lastInit += "\t//"+ compontentsChecked.get(i).getAnnotation()
//							+"\n\t"+funName+"1(";
//					for(int j=0;j<initInfo.getParameters().size();j++) {
//						Parameter curParameter = initInfo.getParameters().get(j);
//						if(j == initInfo.getParameters().size()-1) {
//							lastInit += curParameter.getValue();
//						}else {
//							lastInit += curParameter.getValue()+",";
//						}
//					}
//					lastInit+=");\n";
				}
			}else {
				moduleInit += "\t//"+code+"\n";
//				moduleInit += "\t//"+ compontentsChecked.get(i).getAnnotation()
//						+"\n\t"+funName+"(";
//				List<Parameter> parameters = initInfo.getParameters();
//				for(int j=0;j<parameters.size();j++) {
//					Parameter curParameter = parameters.get(j);
//					if(j == initInfo.getParameters().size()-1) {
//						moduleInit += curParameter.getValue();
//					}else {
//						moduleInit += curParameter.getValue()+",";
//					}
//				}
//				moduleInit+=");\n";
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
		Text dependentText = new Text(aboveCpt, SWT.MULTI | SWT.WRAP);
		GridData depedentData = new GridData(GridData.FILL_BOTH);
		depedentData.grabExcessHorizontalSpace = true;
		dependentText.setLayoutData(depedentData);
		dependentText.setText(depedents);
		dependentText.setEditable(false);
		dependentText.setSize(SWT.HORIZONTAL, 50);
		//��ʾ�������
		Text mutexText = new Text(aboveCpt, SWT.MULTI | SWT.WRAP);
		mutexText.setLayoutData(depedentData);
		mutexText.setText(mutexs);
		mutexText.setEditable(false);
		// �����ʾ����
		Composite componentCpt = new Composite(infoArea, SWT.BORDER);
		GridLayout componentLayout = new GridLayout(6, true);
		componentCpt.setLayout(componentLayout);
		componentCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		ReadComponentXml rcx = new ReadComponentXml();
		compontentsList = rcx.getComponents();//��ȡcpudrv/src�µ����
		for(int i=0;i<compontentsList.size();i++) {
			Component component = new Component();
			component.setName(compontentsList.get(i).getName());
			component.setAnnotation(compontentsList.get(i).getAnnotation());
			component.setAttribute(compontentsList.get(i).getAttribute());
			component.setGrade(compontentsList.get(i).getGrade());
			component.setCode(compontentsList.get(i).getCode());
			component.setConfigure(compontentsList.get(i).getConfigure());
//			component.setInit(compontentsList.get(i).getInit());
			component.setIncludeFile(compontentsList.get(i).getIncludeFile());
			component.setDependents(compontentsList.get(i).getDependents());
			component.setMutexs(compontentsList.get(i).getMutexs());
			allCompontents.add(component);
//			thePeripherals.add(component);
		}
		compontentBtns = new Button[allCompontents.size()];
		configBtns = new Button[allCompontents.size()];
		//���ɨ��������������
		for(int i=0;i<allCompontents.size();i++) {
			Component component = allCompontents.get(i);
			compontentBtns[i] = new Button(componentCpt,SWT.CHECK);
			compontentBtns[i].setText(component.getName());
			configBtns[i] = new Button(componentCpt,SWT.PUSH);
//			configBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
			configBtns[i].setText("config");
			configBtns[i].setEnabled(false);
//			List<Parameter> parameters = component.getInit().getParameters();
			String configure  = component.getConfigure();
			if(configure==null || configure.endsWith("")) {
				configBtns[i].setVisible(false);
			}
			configBtns[i].addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
					ConfigComponentDialog dialog = new ConfigComponentDialog(getShell(),component);
					if (dialog.open() == Window.OK) {
						Component _Component = dialog.getComponent();
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// TODO Auto-generated method stub

				}
			});
		}
		//�������ѡ��ļ�����Ӧ
		for(int i=0;i<allCompontents.size();i++) {
			Component component = allCompontents.get(i);
			int cur = i;
			compontentBtns[i].addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
					if(compontentBtns[cur].getSelection()) {
						configBtns[cur].setEnabled(true);
						List<String> dependents = component.getDependents();
						List<String> mutes = component.getMutexs();
						String allDeps ="";
						String allMuts ="";
						boolean isMutex = false;//�Ƿ���ڻ��⣬���һ��������ѡ��
						//�������
						for(int j=0;j<mutes.size();j++) {
							String mutex = mutes.get(j);
							allMuts+= " "+mutex;
							for(int k=0;k<compontentBtns.length;k++) {
								if(mutex.equals(compontentBtns[k].getText())) {
									if(compontentBtns[k].getSelection()) {
										isMutex = true;
										compontentBtns[cur].setSelection(false);
										IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();  
										MessageDialog.openError(window.getShell(), "��ʾ", mutex+"����ѹ�ѡ����"+component.getName()+"���� ��");
									}
									break;
								}
							}
						}
						//����������δѡ�У���ִ��������ϵ�ļ�����Ӧ
						if(!isMutex) {
							compontentsChecked.add(component);
							//�������
							for(int j=0;j<dependents.size();j++) {
								String dependent = dependents.get(j);
								allDeps+= " "+dependent;
								for(int k=0;k<compontentBtns.length;k++) {
									if(dependent.equals(compontentBtns[k].getText())) {
										compontentBtns[k].setSelection(true);
										compontentsChecked.add(allCompontents.get(k));
										break;
									}
								}
							}
							if(allDeps.equals("")) {
								dependentText.setText(depedents+" ��");
							}else {
								dependentText.setText(depedents+allDeps);
							}
							if(allMuts.equals("")) {
								mutexText.setText(mutexs+" ��");
							}else {
								mutexText.setText(mutexs+allMuts);
							}
						}
					}else {
						configBtns[cur].setEnabled(false);
						for(int i=0;i<compontentsChecked.size();i++) {
							if(compontentsChecked.get(i).getName().equals(component.getName())) {
								compontentsChecked.remove(i);
								break;
							}
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

	/*
	 * ��ȡ��ǰEclipse��·��
	 */
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
			"// Copyright (c) 2014 ����Ȩ����������������޹�˾���С�����Ȩ�˱���һ��Ȩ����\r\n" + 
			"//\r\n" + 
			"// �����Ȩ�����ʹ���߷������������������£�����ʹ����ʹ�ü���ɢ����\r\n" + 
			"// �����װԭʼ�뼰����λ��ִ����ʽ��Ȩ�������۴˰�װ�Ƿ񾭸�����Ȼ��\r\n" + 
			"//\r\n" + 
			"// 1. ���ڱ����Դ�������ɢ�������뱣�������İ�Ȩ���桢�������б���\r\n" + 
			"//    ������������������\r\n" + 
			"// 2. ���ڱ��׼�����λ��ִ����ʽ����ɢ���������������ļ��Լ�������������\r\n" + 
			"//    ��ɢ����װ�е�ý�鷽ʽ����������֮��Ȩ���桢�������б��Լ�����\r\n" + 
			"//    ������������\r\n" + 
			"\r\n" + 
			"// ����������������Ǳ������Ȩ�������Լ�����������״��\"as is\"���ṩ��\r\n" + 
			"// �������װ�����κ���ʾ��Ĭʾ֮�������Σ������������ھ��������Լ��ض�Ŀ\r\n" + 
			"// �ĵ�������ΪĬʾ�Ե�������Ȩ�����˼������֮�����ߣ������κ�������\r\n" + 
			"// ���۳�����κ��������塢���۴�����Ϊ���Լ��ϵ���޹�ʧ������������Υ\r\n" + 
			"// Լ֮��Ȩ��������ʧ������ԭ��ȣ����𣬶����κ���ʹ�ñ������װ��������\r\n" + 
			"// �κ�ֱ���ԡ�����ԡ�ż���ԡ������ԡ��ͷ��Ի��κν�����𺦣�����������\r\n" + 
			"// �������Ʒ������֮���á�ʹ����ʧ��������ʧ��������ʧ��ҵ���жϵȵȣ���\r\n" + 
			"// �����κ����Σ����ڸ���ʹ���ѻ���ǰ��֪���ܻ���ɴ����𺦵���������Ȼ��\r\n" + 
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
			"    NULL, //����Ҫ��NULL��Ϊ�������\r\n" + 
			"\r\n" + 
			"};\r\n" + 
			"\r\n" + 
			"//----����ϵͳ�ں��������-----------------------------------------------------\r\n" + 
			"//���ܣ����úͳ�ʼ����ѡ������������û�����Ŀ¼�б���ʵ�ֱ����������ں˳�ʼ��\r\n" + 
			"//      �׶ε��á�\r\n" + 
			"//      ������ʵ���ں˲ü����ܣ�����ֻҪע�͵�\r\n" + 
			"//          ModuleInstall_Multiplex(0);\r\n" + 
			"//      ��һ�У���·��������ͱ��ü����ˡ�\r\n" + 
			"//      �û��ɴ�example��copy���ļ����Ѳ�Ҫ�����ע�͵���ǿ�ҽ��飬��Ҫɾ��,Ҳ\r\n" + 
			"//      ��Ҫ�޸ĵ���˳�򡣿��԰��û�ģ��ĳ�ʼ������Ҳ���뵽��������,�������\r\n" + 
			"//      ϵͳģ���ʼ������.\r\n" + 
			"//      ��Щ�����������ϵ,�ü�ʱ,ע���Ķ�ע��.\r\n" + 
			"//��������\r\n" + 
			"//���أ���\r\n" + 
			"//---------------------------------------------------------------------------\r\n" + 
			"";

}
