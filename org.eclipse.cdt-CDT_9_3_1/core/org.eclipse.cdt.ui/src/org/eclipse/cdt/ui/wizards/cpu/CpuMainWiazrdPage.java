package org.eclipse.cdt.ui.wizards.cpu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DragDetectEvent;
import org.eclipse.swt.events.DragDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
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
import org.eclipse.swt.widgets.Display;
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

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.IWizardItemsListListener;
import org.eclipse.cdt.ui.wizards.cpu.configDialogs.NewCpuDialog;
import org.eclipse.cdt.ui.wizards.cpu.configDialogs.NewGroupDialog;
import org.eclipse.cdt.ui.wizards.cpu.configDialogs.ResetConfigurationDialog;
import org.eclipse.cdt.ui.wizards.cpu.core.Core;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.newui.Messages;

public class CpuMainWiazrdPage extends WizardPage{

	public static final IPath ICONS_PATH= new Path("$nl$/icons"); //$NON-NLS-1$
	public static TreeItem fileItem = null;
	public static TreeItem tmssItem = null;
	private Tree cpuAttributes;
	private String currentAttributeId = null;
	private static String currentAttributeIdGlobal = null;
	private static final int[] DEFAULT_ENTRIES_SASH_WEIGHTS = new int[] { 10, 30 };
	private Combo[] pathCombos = new Combo[6];
	private String eclipsePath = getEclipsePath();
	private ReadCpuXml rcx = new ReadCpuXml();
	private Cpu cpu = new Cpu();
	private Tree tree;
	private Text configInfoText = null;
	
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
		infoLayout.numColumns = 1;
		infoArea.setLayout(infoLayout);
		GridData data = new GridData(GridData.FILL_BOTH);
		infoArea.setLayoutData(data);
		
//		Composite descCpt = new Composite(infoArea, SWT.NULL);
//		descCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		Point cPoint = composite.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		Text projectTypeDesc;
		String QAQ = "\tIDE�ּ�Ŀ¼����ʽ�������ϵͳ֧�ֵ��ڶ�Cpu�����������ڹ���Cpu�ķ���,����\n���Cpu�ͷ���,�ֶ���ק�����ƶ�����";
		String descTitle = "����/Cpu����:";
		projectTypeDesc = new Text(infoArea, SWT.MULTI | SWT.WRAP);
		GridData textGridData = new GridData(GridData.FILL_HORIZONTAL);
//		textGridData.grabExcessHorizontalSpace = true;
//		textGridData.minimumWidth = cPoint.x;
		projectTypeDesc.setLayoutData(textGridData);
		projectTypeDesc.setText(QAQ);
		projectTypeDesc.setEditable(false);

		
		Composite contentCpt = new Composite(infoArea, SWT.NULL);
		GridLayout contentLayout = new GridLayout();
		contentLayout.numColumns = 3;
		contentCpt.setLayout(contentLayout);
		contentCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite sourceTreeCpt = new Composite(contentCpt, SWT.NONE);
		sourceTreeCpt.setLayout(new GridLayout());
		sourceTreeCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree = new Tree(sourceTreeCpt, SWT.BORDER);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));

		Text configInfoText = new Text(contentCpt, SWT.MULTI | SWT.WRAP);
		configInfoText.setText(descTitle);
		configInfoText.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite btnCpt = new Composite(contentCpt, SWT.NULL);
		GridLayout btnLayout = new GridLayout();
		btnLayout.numColumns = 1;
		btnLayout.verticalSpacing = 10;
		btnCpt.setLayout(btnLayout);

		Button addGroupBtn = new Button(btnCpt,SWT.PUSH);
		addGroupBtn.setText("��ӷ���");
		addGroupBtn.setImage(CPluginImages.DESC_GROUP_VIEW.createImage());

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
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}					
					}
					traverseParents(curFile);		
					configs = getConfigs(cpu,false);
				}
				NewGroupDialog dialog = new NewGroupDialog(getShell(),configs,cpu,curFilePath);
				if (dialog.open() == Window.OK) {
					final TreeItem root = items[0];
					root.removeAll();
					File file = (File) root.getData();
					File[] files = file.listFiles();// ����File�����ļ������ļ���������d
					// �����ļ���Ŀ¼���������жϵ�ǰ·���Ƿ�Ϊ�ļ��У�����File�����ļ��У�������

					if (files != null) {
						for (int i = 0; i < files.length; i++) {
								// ��ǰΪ�ļ�Ŀ¼�������ļ���ʱ���������Ŀ���Ա�ֻ����ʾ�ļ��У��������ļ��У���������ʾ�ļ����µ��ļ�
								if (files[i].isDirectory() && files[i].getName()!="include" && files[i].getName()!="src") {
									TreeItem item = new TreeItem(root, 0);
									item.setText(files[i].getName());
									item.setImage(CPluginImages.TREE_FLODER_VIEW.createImage());
									// Ҷ�ӽڵ��Ӧ����ֵΪ��Ӧ�ļ��е�File����
									item.setData(files[i]);
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
		addCpuBtn.setImage(CPluginImages.DESC_CPU_VIEW.createImage());
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
						}catch (Exception e1) {
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

								// ��ǰΪ�ļ�Ŀ¼�������ļ���ʱ���������Ŀ���Ա�ֻ����ʾ�ļ��У��������ļ��У���������ʾ�ļ����µ��ļ�
								if (files[i].isDirectory() && files[i].getName()!="include" && files[i].getName()!="src") {
									TreeItem item = new TreeItem(root, 0);
									item.setText(files[i].getName());
									item.setImage(CPluginImages.TREE_FLODER_VIEW.createImage());
									// Ҷ�ӽڵ��Ӧ����ֵΪ��Ӧ�ļ��е�File����
									item.setData(files[i]);
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
		configBtn.setImage(CPluginImages.CFG_REVISE_VIEW.createImage());
		configBtn.setVisible(false);
		configBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				List<String> configs = null;
				cpu = new Cpu();
				TreeItem[] items = tree.getSelection();
				String curFilePath = items[0].getData().toString();//��ȡ��ǰѡ���ļ���·��
				boolean isGroup = false;
				
				if(items.length>0) {
					File curFile = new File(curFilePath);//��ǰѡ���ļ�
					File xmlParentFile = getXmlFile(curFile);	
					if(xmlParentFile!=null) {
						if(xmlParentFile.getName().contains("group")) {
							isGroup = true;
						}
						try {
							rcx.unitCpu(cpu,xmlParentFile);
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}					
					}
					traverseParents(curFile);		
					configs = getConfigs(cpu,false);
				}
				
				ResetConfigurationDialog dialog = new ResetConfigurationDialog(getShell(),configs,cpu,curFilePath,isGroup);
				if (dialog.open() == Window.OK) {
					
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		configBtn.setEnabled(false);

		File file = new File(eclipsePath + "djysrc\\bsp");
		File[] roots = file.listFiles();
		for (int i = 0; i < roots.length; i++) {
			// TreeItem root = new TreeItem(tree, 0);
			if (roots[i].getName().equals("cpudrv")) {
				TreeItem root = new TreeItem(tree, 0);
				root.setText("Djyos");
				root.setImage(CPluginImages.TREE_FLODER_VIEW.createImage());
				root.setData(roots[i]);// ���浱ǰ�ڵ�����
				new TreeItem(root, 0);// �ѵ�ǰ�ڵ���ΪĿ¼�ڵ�
			}
		}

		// �����Ϸ�Դ����
		DragSource dragSource = new DragSource(tree, DND.DROP_MOVE | DND.DROP_COPY);
		// ���ô��������Ϊ�ı���String����
		dragSource.setTransfer(new Transfer[] { TextTransfer.getInstance() });
		// ע���Ϸ�Դʱ���¼�����
		dragSource.addDragListener(new DragSourceListener() {

			@Override
			public void dragStart(DragSourceEvent event) {
				// TODO Auto-generated method stub
				System.out.println("dragStart");
				if (tree.getSelectionCount() == 0)
					event.doit = false;
			}

			@Override
			public void dragSetData(DragSourceEvent event) {
				// TODO Auto-generated method stub
				System.out.println("dragSetData");
				if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
					event.data = tree.getSelection()[0].getText(0);
					fileItem = tree.getSelection()[0];
					System.out.println(fileItem);
				}
			}

			@Override
			public void dragFinished(DragSourceEvent event) {
				// TODO Auto-generated method stub
				System.out.println("dragFinished");
			}
		});

		// �����Ϸ�Ŀ�����
		DropTarget dropTarget = new DropTarget(tree, DND.DROP_MOVE | DND.DROP_DEFAULT | DND.DROP_COPY);
		// ����Ŀ�����ɴ������������
		dropTarget.setTransfer(new Transfer[] { TextTransfer.getInstance() });
		// ע��Ŀ�������¼�����
		dropTarget.addDropListener(new DropTargetListener() {

			@Override
			public void dropAccept(DropTargetEvent event) {
				// TODO Auto-generated method stub

			}

			@Override
			public void drop(DropTargetEvent event) {
				// TODO Auto-generated method stub
				if (event.item == null)
					return;
				// ���Ȼ��Ŀ���������ק�����ڵ�
				TreeItem eventItem = (TreeItem) event.item;
				if (fileItem.getText().equals("Test File Manager"))
					return;

				if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
					// �������Դ���õ��ַ���
					String s = (String) event.data;
					// ��tmssλ�ò���һ���ڵ�
					if (eventItem.getParentItem() == null)
						tmssItem = new TreeItem(eventItem, SWT.NONE);
					else
						tmssItem = new TreeItem(eventItem, SWT.NONE);

					tmssItem.setText(s);
					File srcFile = new File(fileItem.getData().toString());
					File destFile = new File(eventItem.getData().toString()+"\\"+s);
					try {
						copyFolder(srcFile, destFile);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					DeleteFolder(fileItem.getData().toString());
					// ɾ��ԭ���Ľڵ�
					if (tree != null) {
						fileItem.dispose();
					}
				}
			}

			@Override
			public void dragOver(DropTargetEvent event) {
				// TODO Auto-generated method stub
				event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SELECT;
			}

			@Override
			public void dragOperationChanged(DropTargetEvent event) {
				// TODO Auto-generated method stub
				 if (event.detail == DND.DROP_DEFAULT)
				     event.detail = DND.DROP_COPY;
			}

			@Override
			public void dragLeave(DropTargetEvent event) {
				// TODO Auto-generated method stub

			}

			@Override
			public void dragEnter(DropTargetEvent event) {
				// TODO Auto-generated method stub
				  if (event.detail == DND.DROP_DEFAULT)
					     event.detail = DND.DROP_COPY;
			}
		});
		
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
					if ((files[i].isHidden() == false || files[i].getName().endsWith(".xml"))) {// �жϵ�ǰ·���Ƿ�Ϊ�����ļ����ļ���
						boolean toExpand = false;
						if(files[i].isDirectory()) {
							if(!files[i].getName().equals("include") && !files[i].getName().equals("src")) {
								toExpand = true;
							}
						}else if(files[i].isFile()){
//							if(files[i].getName().endsWith(".xml")) {
//								toExpand = true;
//							}
						}
						if (toExpand) {

							// ��ǰΪ�ļ�Ŀ¼�������ļ���ʱ���������Ŀ���Ա�ֻ����ʾ�ļ��У��������ļ��У���������ʾ�ļ����µ��ļ�
							if (files[i].isDirectory() && files[i].getName() != "include"
									&& files[i].getName() != "src") {
								TreeItem item = new TreeItem(root, 0);
								item.setText(files[i].getName());
								boolean configed = false;
								File[] cfiles = files[i].listFiles();
								for (int j = 0; j < cfiles.length; j++) {
									if (cfiles[j].getName().endsWith(".xml") && !cfiles[j].getName().contains("group")) {
										configed = true;
										break;
									}
								}
								if (configed) {
									item.setImage(CPluginImages.DESC_CPU_VIEW.createImage());
									item.setExpanded(false);
								}else {
									item.setImage(CPluginImages.TREE_FLODER_VIEW.createImage());
								}
								// Ҷ�ӽڵ��Ӧ����ֵΪ��Ӧ�ļ��е�File����
								item.setData(files[i]);
								new TreeItem(item, 0);
							}
						}
						
					}
				}
			}
		});

		// ��굥��ѡ������Ҷ�ӽڵ��¼�
//		final Treetest tr = new Treetest();
		tree.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				String descTitleChang = null;
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
								descTitleChang="����("+item.getText()+")������";
								if(! files[i].getName().contains("group")) {
									addGroupBtn.setEnabled(false);
									addCpuBtn.setEnabled(false);
									descTitleChang="Cpu("+item.getText()+")������";
								}
								break;
							}
						}
						if(!configed) {
							configBtn.setEnabled(false);
						}
						
						cpu = new Cpu();
						String curFilePath = item.getData().toString();// ��ȡ��ǰѡ���ļ���·��
						File curFile = new File(curFilePath);// ��ǰѡ���ļ�
						File xmlParentFile = getXmlFile(curFile);
						if (xmlParentFile != null) {
							try {
								rcx.unitCpu(cpu, xmlParentFile);
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
						traverseParents(curFile);
						
						String descContent = "";
						if(cpu.getCoreNum()!=0) {
							for(int i=0;i<cpu.getCoreNum();i++) {
								Core core = cpu.getCores().get(i);
								descContent+="\n�ں�"+(i+1)+": ";
								if(core.getType()!=null) {
									descContent+="\n����: "+core.getType();
								}
								if(core.getArch()!=null) {
									descContent+="\n�ܹ�: "+core.getArch();
								}
								if(core.getFamily()!=null) {
									descContent+="\n����: "+core.getFamily();
								}
								if(core.getFpuType()!=null) {
									descContent+="\n����: "+core.getFpuType();
								}					
								if(core.getResetAddr()!=null) {
									descContent+="\n��λ��ַ: "+core.getResetAddr();
								}
								
							}
						}
						//��ʾ��ǰѡ�з���/Cpu��������Ϣ
						if(descTitleChang!=null) {
							configInfoText.setText(descTitleChang
									+ descContent);
						}	

					} else {
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
			cons.add("�洢����");
//			if(isCpu) {
//				cons.add("�洢����");
//			}
		}else {
			if(cpu2.getCoreNum() == 0) {
				cons.add("�ں˸���");
			}
			if(cpu2.getCores().get(0).getResetAddr() == null){
				cons.add("��λ����");
			}
			if(cpu2.getCores().get(0).getMemorys().size() == 0){
				cons.add("�洢����");
//				if(isCpu) {
//					cons.add("�洢����");
//				}
			}
			if(cpu2.getCores().get(0).getFpuType() == null){
				cons.add("��������");
			}
			if(cpu2.getCores().get(0).getArch() == null){
				cons.add("�ں�����");
			}
		}
//		if(cpu2.getFirmwareLib()==null) {
//			cons.add("�̼���");
//		}
		return cons;
	}

	private void traverseParents(File curFile) {
		if(!curFile.getName().contains("cpudrv")) {
			File parentFile = curFile.getParentFile();
			if(!parentFile.getName().contains("cpudrv")){
				File xmlFile = getXmlFile(parentFile);
				try {
					rcx.unitCpu(cpu,xmlFile);
				}catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				traverseParents(parentFile);
			}
		}
	}
	
	/*
	 * �������̵�����һ��Ŀ¼
	 */
	private void copyFolder(File src, File dest) throws IOException {  
	    if (src.isDirectory()) {  
	        if (!dest.exists()) {  
	            dest.mkdir();  
	        }  
	        String files[] = src.list();  
	        for (String file : files) {  
	            File srcFile = new File(src, file);  
	            File destFile = new File(dest, file);  
	            // �ݹ鸴��  
	            copyFolder(srcFile, destFile);  
	        }  
	    } else {  
	        InputStream in = new FileInputStream(src);  
	        OutputStream out = new FileOutputStream(dest);  
	  
	        byte[] buffer = new byte[1024];  
	  
	        int length;  
	          
	        while ((length = in.read(buffer)) > 0) {  
	            out.write(buffer, 0, length);  
	        }  
	        in.close();  
	        out.close();  
	    }  
	}  
	
	/** 
	 *  ����·��ɾ��ָ����Ŀ¼�����۴������ 
	 *@param sPath  Ҫɾ����Ŀ¼path 
	 *@return ɾ���ɹ����� true�����򷵻� false�� 
	 */  
	public boolean DeleteFolder(String sPath) {  
	    boolean flag = false;  
	    File file = new File(sPath);  
	    // �ж�Ŀ¼���ļ��Ƿ����  
	    if (!file.exists()) {  // �����ڷ��� false  
	        return flag;  
	    } else {  
	        // �ж��Ƿ�Ϊ�ļ�  
	        if (file.isFile()) {  // Ϊ�ļ�ʱ����ɾ���ļ�����  
	            return deleteFile(sPath);  
	        } else {  // ΪĿ¼ʱ����ɾ��Ŀ¼����  
	            return deleteDirectory(sPath);  
	        }  
	    }  
	}  
	
	/** 
	 * ɾ�������ļ� 
	 * @param   sPath ��ɾ���ļ�path 
	 * @return ɾ���ɹ�����true�����򷵻�false 
	 */  
	public boolean deleteFile(String sPath) {  
	    boolean flag = false;  
	    File file = new File(sPath);  
	    // ·��Ϊ�ļ��Ҳ�Ϊ�������ɾ��  
	    if (file.isFile() && file.exists()) {  
	        file.delete();  
	        flag = true;  
	    }  
	    return flag;  
	}  
	
	/** 
	 * ɾ��Ŀ¼�Լ�Ŀ¼�µ��ļ� 
	 * @param   sPath ��ɾ��Ŀ¼��·�� 
	 * @return  Ŀ¼ɾ���ɹ�����true�����򷵻�false 
	 */  
	public boolean deleteDirectory(String sPath) {  
	    //���sPath�����ļ��ָ�����β���Զ�����ļ��ָ���  
	    if (!sPath.endsWith(File.separator)) {  
	        sPath = sPath + File.separator;  
	    }  
	    File dirFile = new File(sPath);  
	    //���dir��Ӧ���ļ������ڣ����߲���һ��Ŀ¼�����˳�  
	    if (!dirFile.exists() || !dirFile.isDirectory()) {  
	        return false;  
	    }  
	    boolean flag = true;  
	    //ɾ���ļ����µ������ļ�(������Ŀ¼)  
	    File[] files = dirFile.listFiles();  
	    for (int i = 0; i < files.length; i++) {  
	        //ɾ�����ļ�  
	        if (files[i].isFile()) {  
	            flag = deleteFile(files[i].getAbsolutePath());  
	            if (!flag) break;  
	        } //ɾ����Ŀ¼  
	        else {  
	            flag = deleteDirectory(files[i].getAbsolutePath());  
	            if (!flag) break;  
	        }  
	    }  
	    if (!flag) return false;  
	    //ɾ����ǰĿ¼  
	    if (dirFile.delete()) {  
	        return true;  
	    } else {  
	        return false;  
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
