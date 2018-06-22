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
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.xml.sax.SAXException;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.IWizardItemsListListener;
import org.eclipse.cdt.ui.wizards.cpu.configDialogs.NewGroupOrCpuDialog;
import org.eclipse.cdt.ui.wizards.cpu.configDialogs.ResetConfigurationDialog;
import org.eclipse.cdt.ui.wizards.cpu.core.Core;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.newui.Messages;

public class CpuMainWiazrdPage extends WizardPage{

	public static final IPath ICONS_PATH= new Path("$nl$/icons"); //$NON-NLS-1$
	public static TreeItem fileItem = null,tmssItem = null;
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
	private MenuItem newGroupItem,newCpuItem;
	private Group contentGroup;
	
	protected CpuMainWiazrdPage(String pageName) {
		super(pageName);
		// TODO Auto-generated constructor stub
		setPageComplete(true);
	}	
	
	public void initPopup(){
        Menu menu=new Menu(tree);
        newGroupItem=new MenuItem(menu,SWT.PUSH);
        newGroupItem.setText("新建子目录");
        newGroupItem.setImage(CPluginImages.DESC_GROUP_VIEW.createImage());
        newCpuItem=new MenuItem(menu,SWT.PUSH);
        newCpuItem.setText("新建CPU");
        newCpuItem.setImage(CPluginImages.DESC_CPU_VIEW.createImage());

        tree.setMenu(menu);
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

		Point cPoint = composite.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		Text projectTypeDesc;
		String QAQ = "\tIDE分级目录的形式管理操作系统支持的众多Cpu，本界面用于管理Cpu的分类,\n包括添加Cpu和分组,手动拖拽即可移动分组,右键可添加Cpu和子目录";
		String descTitle = "分组/Cpu描述";
		projectTypeDesc = new Text(infoArea, SWT.MULTI | SWT.WRAP);
		GridData textGridData = new GridData(GridData.FILL_HORIZONTAL);
		projectTypeDesc.setLayoutData(textGridData);
		projectTypeDesc.setText(QAQ);
		projectTypeDesc.setEditable(false);
		
		Composite contentCpt = new Composite(infoArea, SWT.NULL);
		GridLayout contentLayout = new GridLayout();
		contentLayout.numColumns = 2;
		contentCpt.setLayout(contentLayout);
		contentCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite sourceTreeCpt = new Composite(contentCpt, SWT.NONE);
		sourceTreeCpt.setLayout(new GridLayout());
		sourceTreeCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree = new Tree(sourceTreeCpt, SWT.BORDER);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));

		initPopup();
		
		contentGroup = ControlFactory.createGroup(contentCpt, descTitle, 1);
		contentGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		contentGroup.setLayout(new GridLayout());
		
		configInfoText = new Text(contentGroup, SWT.MULTI | SWT.WRAP);
		configInfoText.setText("");
		configInfoText.setLayoutData(new GridData(GridData.FILL_BOTH));
		configInfoText.setEditable(false);

		newGroupItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				List<String> configs = null;
				cpu = new Cpu();
				TreeItem[] items = tree.getSelection();
				String curFilePath = items[0].getData().toString();//获取当前选中文件的路径
				if(items.length>0) {
					File curFile = new File(curFilePath);//当前选中文件
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
				NewGroupOrCpuDialog dialog = new NewGroupOrCpuDialog(getShell(),configs,cpu,curFilePath,"group");
				if (dialog.open() == Window.OK) {
					String newFileName = dialog.getGroupName();
					final TreeItem root = items[0];
					root.removeAll();
					File file = (File) root.getData();
					File[] files = file.listFiles();// 返回File对象（文件夹与文件）的数组d
					// 采用文件夹目录过滤器来判断当前路径是否为文件夹，返回File对象（文件夹）的数组

					if (files != null) {
						for (int i = 0; i < files.length; i++) {
							
							// 当前为文件目录而不是文件的时候，添加新项目，以便只是显示文件夹（包括空文件夹），而不显示文件夹下的文件
							if (files[i].isDirectory()) {
								boolean isNeed = containsXml(files[i]);
								if(isNeed) {
									TreeItem item = new TreeItem(root, 0);
									item.setText(files[i].getName());
									item.setImage(CPluginImages.TREE_FLODER_VIEW.createImage());
									// 叶子节点对应的数值为相应文件夹的File对象
									item.setData(files[i]);
									new TreeItem(item, 0);
									if (files[i].getName().equals(newFileName)) {
										tree.select(item);
									}
								}
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

		newCpuItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				List<String> configs = null;
				cpu = new Cpu();
				TreeItem[] items = tree.getSelection();
				String curFilePath = items[0].getData().toString();//获取当前选中文件的路径
				
				if(items.length>0) {
					File curFile = new File(curFilePath);//当前选中文件
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
				
				NewGroupOrCpuDialog dialog = new NewGroupOrCpuDialog(getShell(),configs,cpu,curFilePath,"cpu");
				if (dialog.open() == Window.OK) {
					String newFileName = dialog.getGroupName();
					final TreeItem root = items[0];
					root.removeAll();
					File file = (File) root.getData();
					File[] files = file.listFiles();// 返回File对象（文件夹与文件）的数组d
					// 采用文件夹目录过滤器来判断当前路径是否为文件夹，返回File对象（文件夹）的数组

					if (files != null) {
						for (int i = 0; i < files.length; i++) {
							// 当前为文件目录而不是文件的时候，添加新项目，以便只是显示文件夹（包括空文件夹），而不显示文件夹下的文件
							if (files[i].isDirectory()) {
								boolean isNeed = containsXml(files[i]);
								if(isNeed) {
									TreeItem item = new TreeItem(root, 0);
									item.setText(files[i].getName());
									item.setImage(CPluginImages.DESC_CPU_VIEW.createImage());
									// 叶子节点对应的数值为相应文件夹的File对象
									item.setData(files[i]);
									new TreeItem(item, 0);
									if (files[i].getName().equals(newFileName)) {
										tree.select(item);
									}	
								}							
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
//		addCpuBtn.setEnabled(false);
		
//		Button configBtn = new Button(btnCpt,SWT.PUSH);
//		configBtn.setText("重设配置");
//		configBtn.setImage(CPluginImages.CFG_REVISE_VIEW.createImage());
//		configBtn.setVisible(false);
//		configBtn.addSelectionListener(new SelectionListener() {
//			
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				// TODO Auto-generated method stub
//				List<String> configs = null;
//				cpu = new Cpu();
//				TreeItem[] items = tree.getSelection();
//				String curFilePath = items[0].getData().toString();//获取当前选中文件的路径
//				boolean isGroup = false;
//				
//				if(items.length>0) {
//					File curFile = new File(curFilePath);//当前选中文件
//					File xmlParentFile = getXmlFile(curFile);	
//					if(xmlParentFile!=null) {
//						if(xmlParentFile.getName().contains("group")) {
//							isGroup = true;
//						}
//						try {
//							rcx.unitCpu(cpu,xmlParentFile);
//						} catch (Exception e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}					
//					}
//					traverseParents(curFile);		
//					configs = getConfigs(cpu,false);
//				}
//				
//				ResetConfigurationDialog dialog = new ResetConfigurationDialog(getShell(),configs,cpu,curFilePath,isGroup);
//				if (dialog.open() == Window.OK) {
//					
//				}
//				
//			}
//			
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
//		configBtn.setEnabled(false);

		File file = new File(eclipsePath + "djysrc\\bsp");
		File[] roots = file.listFiles();
		for (int i = 0; i < roots.length; i++) {
			// TreeItem root = new TreeItem(tree, 0);
			if (roots[i].getName().equals("cpudrv")) {
				TreeItem root = new TreeItem(tree, 0);
				root.setText("Djyos");
				root.setImage(CPluginImages.TREE_FLODER_VIEW.createImage());
				root.setData(roots[i]);// 保存当前节点数据
				File dfile = (File) root.getData();
				File[] files = dfile.listFiles();
				for (int j = 0; j < files.length; j++) {
					if ((files[j].isHidden() == false)) {// 判断当前路径是否为隐藏文件与文件夹
						boolean toExpand = false;
						if(files[j].isDirectory()) {
							boolean isNeed =  containsXml(files[j]);
							if(isNeed) {
								toExpand = true;
							}
						}
						if (toExpand) {
							// 当前为文件目录而不是文件的时候，添加新项目，以便只是显示文件夹（包括空文件夹），而不显示文件夹下的文件
								TreeItem item = new TreeItem(root, 0);
								item.setText(files[j].getName());
								item.setImage(CPluginImages.TREE_FLODER_VIEW.createImage());
								// 叶子节点对应的数值为相应文件夹的File对象
								item.setData(files[j]);
								new TreeItem(item, 0);
						}
						
					}
				}
				root.setExpanded(true);
				break;
			}
		}

		// 定义拖放源对象
		DragSource dragSource = new DragSource(tree, DND.DROP_MOVE | DND.DROP_COPY);
		// 设置传输的数据为文本型String类型
		dragSource.setTransfer(new Transfer[] { TextTransfer.getInstance() });
		// 注册拖放源时的事件处理
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

		// 定义拖放目标对象
		DropTarget dropTarget = new DropTarget(tree, DND.DROP_MOVE | DND.DROP_DEFAULT | DND.DROP_COPY);
		// 设置目标对象可传输的数据类型
		dropTarget.setTransfer(new Transfer[] { TextTransfer.getInstance() });
		// 注册目标对象的事件处理
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
				// 首先获得目标对象中拖拽的树节点
				TreeItem eventItem = (TreeItem) event.item;
				if (fileItem.getText().equals("Test File Manager"))
					return;

				if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
					// 获得数据源设置的字符串
					String s = (String) event.data;
					// 在tmss位置插入一个节点				
					File srcFile = new File(fileItem.getData().toString());
					File destFile = new File(eventItem.getData().toString()+"\\"+s);
					boolean isDropable =  isFileDropable(srcFile,destFile);
					if(isDropable) {
						if (eventItem.getParentItem() == null)
							tmssItem = new TreeItem(eventItem, SWT.NONE);
						else
							tmssItem = new TreeItem(eventItem, SWT.NONE);

						tmssItem.setText(s);
						try {
							copyFolder(srcFile, destFile);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						DeleteFolder(fileItem.getData().toString());
						// 删除原来的节点
						if (tree != null) {
							fileItem.dispose();
						}
					}else {
						IWorkbenchWindow window = PlatformUI.getWorkbench()
    							.getActiveWorkbenchWindow();
						MessageDialog.openError(window.getShell(), "提示",
    							"父子目录不可互相拖拉");
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
					if ((files[i].isHidden() == false || files[i].getName().endsWith(".xml"))) {// 判断当前路径是否为隐藏文件与文件夹
						boolean toExpand = false;
						if(files[i].isDirectory()) {
							boolean isNeed =  containsXml(files[i]);
							if(isNeed) {
								toExpand = true;
							}
						}
						if (toExpand) {

							// 当前为文件目录而不是文件的时候，添加新项目，以便只是显示文件夹（包括空文件夹），而不显示文件夹下的文件
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
								// 叶子节点对应的数值为相应文件夹的File对象
								item.setData(files[i]);
								new TreeItem(item, 0);
							}
						}
						
					}
				}
			}
		});

		// 鼠标单击选择树的叶子节点事件
//		final Treetest tr = new Treetest();
		tree.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				String descTitleChang = null;
				Point point = new Point(event.x, event.y);
				TreeItem item = tree.getItem(point);
				if ((item != null) && (item.getData() != null)) {
					File file = new File(item.getData().toString()); 
					if(item.getText().equals("Djyos")) {
						descTitleChang = descTitle;
					}
					if(file.isDirectory()) {
						newGroupItem.setEnabled(true);
						newCpuItem.setEnabled(true);
						File[] files = file.listFiles();
						boolean configed = false;
						for(int i=0;i<files.length;i++) {
							if(files[i].getName().endsWith(".xml")) {
//								configBtn.setEnabled(true);
								configed = true;
								descTitleChang="分组("+item.getText()+")描述";
								if(! files[i].getName().contains("group")) {
									newGroupItem.setEnabled(false);
									newCpuItem.setEnabled(false);
									descTitleChang="Cpu("+item.getText()+")描述";
								}
								break;
							}
						}
						if(!configed) {
//							configBtn.setEnabled(false);
						}
						
						cpu = new Cpu();
						String curFilePath = item.getData().toString();// 获取当前选中文件的路径
						File curFile = new File(curFilePath);// 当前选中文件
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
								descContent+="内核"+(i+1)+": ";
								if(core.getType()!=null) {
									descContent+="\n类型: "+core.getType();
								}
								if(core.getArch()!=null) {
									descContent+="\n架构: "+core.getArch();
								}
								if(core.getFamily()!=null) {
									descContent+="\n家族: "+core.getFamily();
								}
								if(core.getFpuType()!=null) {
									descContent+="\n浮点: "+core.getFpuType();
								}					
								if(core.getResetAddr()!=null) {
									descContent+="\n复位地址: "+core.getResetAddr();
								}
								descContent+="\n";
							}
						}
						//显示当前选中分组/Cpu的配置信息
						if(descTitleChang!=null) {
							contentGroup.setText(descTitleChang);
							configInfoText.setText(descContent);
						}	

					} else {
//						addCpuBtn.setEnabled(false);
						newCpuItem.setEnabled(false);
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
	
	protected boolean isFileDropable(File srcFile, File destFile) {
		// TODO Auto-generated method stub
		while(!srcFile.getName().equals("cpudrv")) {
			srcFile = srcFile.getParentFile();
			if(srcFile.getName().equals(destFile.getName())) {
				return false;
			}
		}
		while(!destFile.getName().equals("cpudrv")) {
			destFile = destFile.getParentFile();
			if(destFile.getName().equals(srcFile.getName())) {
				return false;
			}
		}
		if(srcFile.getName().equals("cpudrv")){
			return false;
		}
		return true;
	}

	private boolean containsXml(File file) {
		File[] files = file.listFiles();
		for(File f:files) {
			if(f.getName().endsWith(".xml")) {
				return true;
			}
		}
		return false;
	}
	
	protected List<String> getConfigs(Cpu cpu2,boolean isCpu) {
		// TODO Auto-generated method stub
		List<String> cons = new ArrayList<String>();
		if(cpu2.getCores().size() == 0){
			cons.add("内核个数");
			cons.add("复位配置");
			cons.add("浮点配置");
			cons.add("内核配置");
			cons.add("存储配置");
//			if(isCpu) {
//				cons.add("存储配置");
//			}
		}else {
			if(cpu2.getCoreNum() == 0) {
				cons.add("内核个数");
			}
			if(cpu2.getCores().get(0).getResetAddr() == null){
				cons.add("复位配置");
			}
			if(cpu2.getCores().get(0).getMemorys().size() == 0){
				cons.add("存储配置");
//				if(isCpu) {
//					cons.add("存储配置");
//				}
			}
			if(cpu2.getCores().get(0).getFpuType() == null){
				cons.add("浮点配置");
			}
			if(cpu2.getCores().get(0).getArch() == null){
				cons.add("内核配置");
			}
		}
//		if(cpu2.getFirmwareLib()==null) {
//			cons.add("固件库");
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
	 * 拷贝工程到另外一个目录
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
	            // 递归复制  
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
	 *  根据路径删除指定的目录，无论存在与否 
	 *@param sPath  要删除的目录path 
	 *@return 删除成功返回 true，否则返回 false。 
	 */  
	public boolean DeleteFolder(String sPath) {  
	    boolean flag = false;  
	    File file = new File(sPath);  
	    // 判断目录或文件是否存在  
	    if (!file.exists()) {  // 不存在返回 false  
	        return flag;  
	    } else {  
	        // 判断是否为文件  
	        if (file.isFile()) {  // 为文件时调用删除文件方法  
	            return deleteFile(sPath);  
	        } else {  // 为目录时调用删除目录方法  
	            return deleteDirectory(sPath);  
	        }  
	    }  
	}  
	
	/** 
	 * 删除单个文件 
	 * @param   sPath 被删除文件path 
	 * @return 删除成功返回true，否则返回false 
	 */  
	public boolean deleteFile(String sPath) {  
	    boolean flag = false;  
	    File file = new File(sPath);  
	    // 路径为文件且不为空则进行删除  
	    if (file.isFile() && file.exists()) {  
	        file.delete();  
	        flag = true;  
	    }  
	    return flag;  
	}  
	
	/** 
	 * 删除目录以及目录下的文件 
	 * @param   sPath 被删除目录的路径 
	 * @return  目录删除成功返回true，否则返回false 
	 */  
	public boolean deleteDirectory(String sPath) {  
	    //如果sPath不以文件分隔符结尾，自动添加文件分隔符  
	    if (!sPath.endsWith(File.separator)) {  
	        sPath = sPath + File.separator;  
	    }  
	    File dirFile = new File(sPath);  
	    //如果dir对应的文件不存在，或者不是一个目录，则退出  
	    if (!dirFile.exists() || !dirFile.isDirectory()) {  
	        return false;  
	    }  
	    boolean flag = true;  
	    //删除文件夹下的所有文件(包括子目录)  
	    File[] files = dirFile.listFiles();  
	    for (int i = 0; i < files.length; i++) {  
	        //删除子文件  
	        if (files[i].isFile()) {  
	            flag = deleteFile(files[i].getAbsolutePath());  
	            if (!flag) break;  
	        } //删除子目录  
	        else {  
	            flag = deleteDirectory(files[i].getAbsolutePath());  
	            if (!flag) break;  
	        }  
	    }  
	    if (!flag) return false;  
	    //删除当前目录  
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
