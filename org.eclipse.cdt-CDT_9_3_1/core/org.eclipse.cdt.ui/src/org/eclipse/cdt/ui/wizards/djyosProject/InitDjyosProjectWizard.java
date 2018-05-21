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

	private String depedents = "依赖组件: ";
	private String mutexs = "互斥组件: ";
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
	
	//生成moduleinit.h文件
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
		Text dependentText = new Text(aboveCpt, SWT.MULTI | SWT.WRAP);
		GridData depedentData = new GridData(GridData.FILL_BOTH);
		depedentData.grabExcessHorizontalSpace = true;
		dependentText.setLayoutData(depedentData);
		dependentText.setText(depedents);
		dependentText.setEditable(false);
		dependentText.setSize(SWT.HORIZONTAL, 50);
		//显示互斥组件
		Text mutexText = new Text(aboveCpt, SWT.MULTI | SWT.WRAP);
		mutexText.setLayoutData(depedentData);
		mutexText.setText(mutexs);
		mutexText.setEditable(false);
		// 组件显示界面
		Composite componentCpt = new Composite(infoArea, SWT.BORDER);
		GridLayout componentLayout = new GridLayout(6, true);
		componentCpt.setLayout(componentLayout);
		componentCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
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
//			component.setInit(compontentsList.get(i).getInit());
			component.setIncludeFile(compontentsList.get(i).getIncludeFile());
			component.setDependents(compontentsList.get(i).getDependents());
			component.setMutexs(compontentsList.get(i).getMutexs());
			allCompontents.add(component);
//			thePeripherals.add(component);
		}
		compontentBtns = new Button[allCompontents.size()];
		configBtns = new Button[allCompontents.size()];
		//添加扫描的组件到界面上
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
		//处理组件选择的监听相应
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
						boolean isMutex = false;//是否存在互斥，并且互斥组件已选中
						//互斥组件
						for(int j=0;j<mutes.size();j++) {
							String mutex = mutes.get(j);
							allMuts+= " "+mutex;
							for(int k=0;k<compontentBtns.length;k++) {
								if(mutex.equals(compontentBtns[k].getText())) {
									if(compontentBtns[k].getSelection()) {
										isMutex = true;
										compontentBtns[cur].setSelection(false);
										IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();  
										MessageDialog.openError(window.getShell(), "提示", mutex+"组件已勾选，与"+component.getName()+"互斥 ！");
									}
									break;
								}
							}
						}
						//如果互斥组件未选中，则执行又来关系的监听相应
						if(!isMutex) {
							compontentsChecked.add(component);
							//依赖组件
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
								dependentText.setText(depedents+" 无");
							}else {
								dependentText.setText(depedents+allDeps);
							}
							if(allMuts.equals("")) {
								mutexText.setText(mutexs+" 无");
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
		
		//设置滚动条属性
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
	 * 获取当前Eclipse的路径
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
			"//---------------------------------------------------------------------------\r\n" + 
			"";

}
