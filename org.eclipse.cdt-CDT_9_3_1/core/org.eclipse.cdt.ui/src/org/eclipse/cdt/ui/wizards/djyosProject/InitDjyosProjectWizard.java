package org.eclipse.cdt.ui.wizards.djyosProject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

public class InitDjyosProjectWizard extends WizardPage{

	private String depedents = "依赖组件: ";
	private String mutexs = "互斥组件: ";
	private List<String> compontents = new ArrayList<>();
	private String chipPath = getEclipsePath()+"djysrc\\bsp\\chipdrv";
	
	protected InitDjyosProjectWizard(String pageName) {
		super(pageName);
		// TODO Auto-generated constructor stub
		setPageComplete(true);
	}

	private void getComponents(String path) {
		File file = new File(path);
		File[] files = file.listFiles();
		for(int i=0;i<files.length;i++) {
			File[] cfiles = files[i].listFiles();
			for(int j=0;j<cfiles.length;j++) {
				if(cfiles[j].getName().endsWith(".xml")) {
					
				}
			}
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
		
//		aboveCpt.setSize(point0.x, 150);
		//依赖关系显示界面
//		Composite relationCpt = new Composite(aboveCpt,SWT.BORDER);
//		RowLayout relationLayout = new RowLayout();
//		relationCpt.setLayout(relationLayout);
//		relationCpt.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL));
//		relationCpt.setLayoutData( new RowData(SWT.HORIZONTAL,170));
		//显示依赖组件
//		RowData rowData = new RowData();
//		rowData.height = 50;		
//		rowData.width = SWT.HORIZONTAL;

		Text dependentText = new Text(aboveCpt, SWT.MULTI | SWT.WRAP);
		dependentText.setLayoutData(new GridData(GridData.FILL_BOTH));
		dependentText.setText(depedents);
		dependentText.setEditable(false);
//		//显示互斥组件
		Text mutexText = new Text(aboveCpt, SWT.MULTI | SWT.WRAP);
		mutexText.setLayoutData(new GridData(GridData.FILL_BOTH));
		mutexText.setText(mutexs);
		mutexText.setEditable(false);
		// 组件显示界面
		Composite componentCpt = new Composite(infoArea, SWT.BORDER);
		GridLayout componentLayout = new GridLayout(3, true);
		componentCpt.setLayout(componentLayout);
		componentCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		compontents.add("GPIO");
		compontents.add("Driver");
		compontents.add("SD");
		compontents.add("UART");
		compontents.add("LowPower");
		compontents.add("Time");
		//添加扫描的组件到界面上
		for(int i=0;i<compontents.size();i++) {
			Composite compontentConfigCpt = new Composite(componentCpt,SWT.NONE);
			GridLayout compontentConfigLayout = new GridLayout(2,true);
			compontentConfigLayout.numColumns = 2;
			compontentConfigCpt.setLayout(compontentConfigLayout);
			compontentConfigCpt.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL));
			Button compontentBtn = new Button(compontentConfigCpt,SWT.CHECK);
			compontentBtn.setText(compontents.get(i));
			Button configBtn = new Button(compontentConfigCpt,SWT.PUSH);
			configBtn.setText("config");
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
	

}
