package org.eclipse.cdt.ui.wizards.cpu;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.xml.sax.SAXException;

import org.eclipse.cdt.ui.wizards.IWizardItemsListListener;
import org.eclipse.cdt.ui.wizards.cpu.configDialogs.NewCpuDialog;
import org.eclipse.cdt.ui.wizards.cpu.configDialogs.NewGroupDialog;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;

import org.eclipse.cdt.internal.ui.newui.Messages;

public class CpuMainWiazrdPage extends WizardPage{

	private Tree cpuAttributes;
	private String currentAttributeId = null;
	private static String currentAttributeIdGlobal = null;
	private static final int[] DEFAULT_ENTRIES_SASH_WEIGHTS = new int[] { 10, 30 };
	private Combo[] pathCombos = new Combo[6];
	private String eclipsePath = getEclipsePath();
	private ReadCpuXml rcx = new ReadCpuXml();
	private Cpu cpu = new Cpu();
	private Tree tree;
	
	protected CpuMainWiazrdPage(String pageName) {
		super(pageName);
		// TODO Auto-generated constructor stub
		setPageComplete(true);
	}	
	
	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		initializeDialogUnits(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);
		
		ScrolledComposite scrolledComposite = new ScrolledComposite(composite, SWT.V_SCROLL
                | SWT.H_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite infoArea = new Composite(scrolledComposite, SWT.NONE);
		GridLayout infoLayout = new GridLayout();
		infoLayout.marginHeight = 10;
		infoLayout.verticalSpacing = 5;
		infoLayout.numColumns = 1;
		infoArea.setLayout(infoLayout);
		GridData data = new GridData(GridData.FILL_BOTH);
		infoArea.setLayoutData(data);
		
		Text projectTypeDesc;
		String QAQ = "·��ѡ��\n"
				+ "\t·���Ļ���Ҫ���а�����ϵ���ӷ���Ӧ�����ϼ�������Ӽ�\n"
				+ "\t��stm32/stm32f/stm32f1/stm32f103\n";
//				+ "\t�������µ�CPU��������ͨ��������ѡ����Ӧ��CPU��������\n"
//				+ "\t���½�CPU���飬��ͨ��������ѡ���µ�����������\n"
//				+ "\t����ϸ�����飬��ͨ��������ѡ����Ҫ��ϸ���ķ���\n";
		projectTypeDesc = new Text(infoArea, SWT.MULTI | SWT.WRAP);
		projectTypeDesc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		projectTypeDesc.setText(QAQ);
		
		Composite contentCpt = new Composite(infoArea, SWT.NULL);
		GridLayout contentLayout = new GridLayout();
		contentLayout.marginHeight = 10;
		contentLayout.numColumns = 2;
		contentCpt.setLayout(contentLayout);
		contentCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite sourceTreeCpt = new Composite(contentCpt, SWT.NONE);
		sourceTreeCpt.setLayout(new GridLayout());
		sourceTreeCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree = new Tree(sourceTreeCpt, SWT.BORDER);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
//		TreeItem computer = new TreeItem(tree, SWT.NONE);
//		computer.setText("Cpudrv");
		
		Composite btnCpt = new Composite(contentCpt, SWT.NONE);
		GridLayout btnLayout = new GridLayout();
		btnLayout.marginHeight = 10;
		btnLayout.numColumns = 1;
		btnLayout.verticalSpacing = 10;
		btnCpt.setLayout(btnLayout);
		Button addGroupBtn = new Button(btnCpt,SWT.PUSH);
		addGroupBtn.setText("��ӷ���");

		addGroupBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				List<String> configs = null;
				cpu = new Cpu();
				TreeItem[] items = tree.getSelection();
				String curFilePath = items[0].getData().toString();//��ȡ��ǰѡ���ļ���·��
				if(items.length>0) {
					File curFile = new File(curFilePath);//��ǰѡ���ļ�
					File xmlParentFile = getXmlFile(curFile);	
					if(xmlParentFile!=null) {
						try {
							rcx.unitCpu(cpu,xmlParentFile);
						} catch (SAXException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}					
					}
					traverseParents(curFile);		
					configs = getConfigs(cpu,false);
				}
				NewGroupDialog dialog = new NewGroupDialog(getShell(),configs,cpu,curFilePath);
				if (dialog.open() == Window.OK) {
//					tree.removeAll();
//					File file = new File(eclipsePath+"djysrc\\bsp");
//					File[] roots = file.listFiles();
//					for (int i = 0; i < roots.length; i++) {
//						// TreeItem root = new TreeItem(tree, 0);
//						if(roots[i].getName().equals("cpudrv")) {
//							TreeItem root = new TreeItem(tree, 0);
//							root.setText(roots[i].toString());
//							root.setData(roots[i]);// ���浱ǰ�ڵ�����
//							new TreeItem(root, 0);// �ѵ�ǰ�ڵ���ΪĿ¼�ڵ�
//						}
//					}
					final TreeItem root = items[0];
					root.removeAll();
					File file = (File) root.getData();
					File[] files = file.listFiles();// ����File�����ļ������ļ���������d
					// �����ļ���Ŀ¼���������жϵ�ǰ·���Ƿ�Ϊ�ļ��У�����File�����ļ��У�������

					if (files != null) {
						for (int i = 0; i < files.length; i++) {
								TreeItem item = new TreeItem(root, 0);
								item.setText(files[i].getName());
								// Ҷ�ӽڵ��Ӧ����ֵΪ��Ӧ�ļ��е�File����
								item.setData(files[i]);

								// ��ǰΪ�ļ�Ŀ¼�������ļ���ʱ���������Ŀ���Ա�ֻ����ʾ�ļ��У��������ļ��У���������ʾ�ļ����µ��ļ�
								if (files[i].isDirectory()) {
									new TreeItem(item, 0);
								}
						}
					}
				}

			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		addGroupBtn.setEnabled(false);
		
		Button addCpuBtn = new Button(btnCpt,SWT.PUSH);
		addCpuBtn.setText("���CPU");
		addCpuBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				List<String> configs = null;
				cpu = new Cpu();
				TreeItem[] items = tree.getSelection();
				String curFilePath = items[0].getData().toString();//��ȡ��ǰѡ���ļ���·��
				
				if(items.length>0) {
					File curFile = new File(curFilePath);//��ǰѡ���ļ�
					File xmlParentFile = getXmlFile(curFile);	
					if(xmlParentFile!=null) {
						try {
							rcx.unitCpu(cpu,xmlParentFile);
						} catch (SAXException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}					
					}
					traverseParents(curFile);		
					configs = getConfigs(cpu,true);
				}
				
				NewCpuDialog dialog = new NewCpuDialog(getShell(),configs,cpu,curFilePath);
				if (dialog.open() == Window.OK) {
					final TreeItem root = items[0];
					root.removeAll();
					File file = (File) root.getData();
					File[] files = file.listFiles();// ����File�����ļ������ļ���������d
					// �����ļ���Ŀ¼���������жϵ�ǰ·���Ƿ�Ϊ�ļ��У�����File�����ļ��У�������

					if (files != null) {
						for (int i = 0; i < files.length; i++) {
								TreeItem item = new TreeItem(root, 0);
								item.setText(files[i].getName());
								// Ҷ�ӽڵ��Ӧ����ֵΪ��Ӧ�ļ��е�File����
								item.setData(files[i]);

								// ��ǰΪ�ļ�Ŀ¼�������ļ���ʱ���������Ŀ���Ա�ֻ����ʾ�ļ��У��������ļ��У���������ʾ�ļ����µ��ļ�
								if (files[i].isDirectory()) {
									new TreeItem(item, 0);
								}
						}
					}
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		addCpuBtn.setEnabled(false);
		
		Button configBtn = new Button(btnCpt,SWT.PUSH);
		configBtn.setText("��������");
		configBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		configBtn.setEnabled(false);

		File file = new File(eclipsePath+"djysrc\\bsp");
		File[] roots = file.listFiles();
		for (int i = 0; i < roots.length; i++) {
			// TreeItem root = new TreeItem(tree, 0);
			if(roots[i].getName().equals("cpudrv")) {
				TreeItem root = new TreeItem(tree, 0);
				root.setText(roots[i].toString());
				root.setData(roots[i]);// ���浱ǰ�ڵ�����
				new TreeItem(root, 0);// �ѵ�ǰ�ڵ���ΪĿ¼�ڵ�
			}
		}

		tree.addListener(SWT.Expand, new Listener() {
			public void handleEvent(final Event event) {
				final TreeItem root = (TreeItem) event.item;
				TreeItem[] items = root.getItems();
				for (int i = 0; i < items.length; i++) {
					if (items[i].getData() != null)
						return;
					items[i].dispose();
				}
				File file = (File) root.getData();
				File[] files = file.listFiles();
				if (files == null)
					return;
				for (int i = 0; i < files.length; i++) {
					if ((files[i].isHidden() == false)) {// �жϵ�ǰ·���Ƿ�Ϊ�����ļ����ļ���
						TreeItem item = new TreeItem(root, 0);
						item.setText(files[i].getName());
						// Ҷ�ӽڵ��Ӧ����ֵΪ��Ӧ�ļ��е�File����
						item.setData(files[i]);
						// ��ǰΪ�ļ�Ŀ¼�������ļ���ʱ���������Ŀ���Ա�ֻ����ʾ�ļ��У��������ļ��У���������ʾ�ļ����µ��ļ�
						if (files[i].isDirectory()) {
							new TreeItem(item, 0);
						}
					}
				}
			}
		});

		// ��굥��ѡ������Ҷ�ӽڵ��¼�
//		final Treetest tr = new Treetest();
		tree.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				Point point = new Point(event.x, event.y);
				TreeItem item = tree.getItem(point);
				if ((item != null) && (item.getData() != null)) {
					File file = new File(item.getData().toString()); 
					if(file.isDirectory()) {
						addGroupBtn.setEnabled(true);
						addCpuBtn.setEnabled(true);
						File[] files = file.listFiles();
						boolean configed = false;
						for(int i=0;i<files.length;i++) {
							if(files[i].getName().endsWith(".xml")) {
								configBtn.setEnabled(true);
								configed = true;
								if(! files[i].getName().contains("group")) {
									addGroupBtn.setEnabled(false);
									addCpuBtn.setEnabled(false);
								}
								break;
							}
						}
						if(!configed) {
							configBtn.setEnabled(false);
						}
					}else {
						addGroupBtn.setEnabled(false);
						addCpuBtn.setEnabled(false);
					}				
				}
			}
		});
		
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

	protected List<String> getConfigs(Cpu cpu2,boolean isCpu) {
		// TODO Auto-generated method stub
		List<String> cons = new ArrayList<String>();
		if(cpu2.getCores().size() == 0){
			cons.add("�ں˸���");
			cons.add("��λ����");
			cons.add("��������");
			cons.add("�ں�����");
			if(isCpu) {
				cons.add("�洢����");
			}
		}else {
			if(cpu2.getCoreNum() == 0) {
				cons.add("�ں˸���");
			}
			if(cpu2.getCores().get(0).getResetAddr() == null){
				cons.add("��λ����");
			}
			if(cpu2.getCores().get(0).getMemorys().size() == 0){
				if(isCpu) {
					cons.add("�洢����");
				}
			}
			if(cpu2.getCores().get(0).getFpuType() == null){
				cons.add("��������");
			}
			if(cpu2.getCores().get(0).getArch() == null){
				cons.add("�ں�����");
			}
		}
		
		return cons;
	}

	private void traverseParents(File curFile) {
		if(!curFile.getName().contains("cpudrv")) {
			File parentFile = curFile.getParentFile();
			if(!parentFile.getName().contains("cpudrv")){
				File xmlFile = getXmlFile(parentFile);
				try {
					rcx.unitCpu(cpu,xmlFile);
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				System.out.println(cpu.getCpuName()+"\n"+
//						cpu.getCores().get(0).getFpuType()+"\n"+
//						cpu.getCores().get(1).getFpuType()+"\n"+
//						cpu.getCores().get(0).getResetAddr()+"\n");
				traverseParents(parentFile);
			}
		}
	}
		
	private File getXmlFile(File parentFile) {
		File file =null;
		File[] files = parentFile.listFiles();
		for(int i=0;i<files.length;i++){
			if(files[i].getName().endsWith(".xml")) {
				file = files[i];
			}
		}
		return file;
	}
	
	public String getEclipsePath() {
		String fullPath = Platform.getInstallLocation().getURL().toString();
		String eclipsePath = fullPath.substring(6,(fullPath.substring(0,fullPath.length()-1)).lastIndexOf("/")+1);
		return eclipsePath;
	}

	@Override
	public IWizardPage getNextPage() {
		// TODO Auto-generated method stub
		return super.getNextPage();
	}

}
