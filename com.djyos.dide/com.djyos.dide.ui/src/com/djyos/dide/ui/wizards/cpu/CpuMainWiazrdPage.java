package com.djyos.dide.ui.wizards.cpu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
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

import com.djyos.dide.ui.wizards.cpu.configDialogs.NewGroupOrCpuDialog;
import com.djyos.dide.ui.wizards.cpu.configDialogs.ResetConfigurationDialog;
import com.djyos.dide.ui.wizards.cpu.core.Core;
import com.djyos.dide.ui.wizards.cpu.core.memory.CoreMemory;
import com.djyos.dide.ui.wizards.djyosProject.tools.DeleteFolder;
import com.djyos.dide.ui.wizards.djyosProject.tools.DideHelper;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.IWizardItemsListListener;
import com.djyos.dide.ui.wizards.cpu.Cpu;
import com.djyos.dide.ui.wizards.cpu.ReadCpuXml;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;

import com.djyos.dide.ui.wizards.djyosProject.tools.DPluginImages;
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
	private Cpu cpuCreated = null;
	private Tree tree;
	private Text configInfoText = null;
	private MenuItem newGroupItem,newCpuItem,deleteItem,reviseItem;
	private IWorkbenchWindow window = PlatformUI.getWorkbench()
			.getActiveWorkbenchWindow();
	DideHelper dideHelper = new DideHelper();

	public Cpu getCpuCreated() {
		return cpuCreated;
	}

	protected CpuMainWiazrdPage(String pageName) {
		super(pageName);
		boolean djysrcExist = true;
		File djysrcFile = new File(dideHelper.getDjyosSrcPath());
		if(djysrcFile.exists()) {
			File[] files = djysrcFile.listFiles();
			if(files.length<2) {
				djysrcExist = false;
			}
		}else {
			djysrcExist = false;
		}
		if(!djysrcExist) {
			MessageDialog.openInformation(window.getShell(), "提示",
					"Djyos源码不存在，请重启Eclipse根据提示下载");
		}
		// TODO Auto-generated constructor stub
		setPageComplete(true);
	}	
	
	private String getDIDEPath() {
		String fullPath = Platform.getInstallLocation().getURL().toString();
		String eclipsePath = fullPath.substring(6,
				(fullPath.substring(0, fullPath.length() - 1)).lastIndexOf("/") + 1);
		return eclipsePath;
	}

	public void initPopup(){
        Menu menu=new Menu(tree);
        newGroupItem=new MenuItem(menu,SWT.PUSH);
        newGroupItem.setText("新建子目录");
        newGroupItem.setImage(DPluginImages.DESC_GROUP_VIEW.createImage());
        
        newCpuItem=new MenuItem(menu,SWT.PUSH);
        newCpuItem.setText("新建CPU");
        newCpuItem.setImage(DPluginImages.DESC_CPU_VIEW.createImage());
        
        reviseItem=new MenuItem(menu,SWT.PUSH);
        reviseItem.setText("修改配置");
        reviseItem.setImage(DPluginImages.CPU_REVISE_VIEW.createImage());
        
        deleteItem=new MenuItem(menu,SWT.PUSH);
        deleteItem.setText("删除");
        deleteItem.setImage(DPluginImages.CFG_DELETE_OBJ.createImage());

        tree.setMenu(menu);
    }
	
	long toUnsigned(long s) {

		return s & 0xFFFFFFFF;

	}

	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
//		String command="arm-none-eabi-objdump.exe -h F:\\djyos\\atomic.o";  
//	    String line = null;  
//	    StringBuilder sb = new StringBuilder();  
//	    Runtime runtime = Runtime.getRuntime();  
//	    try {  
//	    Process process = runtime.exec(command);  
//	    BufferedReader  bufferedReader = new BufferedReader  
//	            (new InputStreamReader(process.getInputStream()));  
//	        while ((line = bufferedReader.readLine()) != null) {  
//	            sb.append(line + "\n");  
//	            System.out.println(line);  
//	        }  
//	    } catch (IOException e) {  
//	        // TODO 自动生成的 catch 块  
//	        e.printStackTrace();  
//	    }  

//		long a = Integer.parseInt("1");
//		long b = Long.parseLong("0xFFFFFFFF".substring(2), 16);
//		long c = Integer.parseInt("-1");; //0xFFFFFFFF
//		if(a>b) {
//			System.out.println("a= "+toUnsigned(a));
//			System.out.println("b= "+toUnsigned(b));
//			System.out.println("c= "+toUnsigned(c));
//			System.out.println("a>b");
//		}else {
//			System.out.println("a= "+toUnsigned(a));
//			System.out.println("b= "+toUnsigned(b));
//			System.out.println("c= "+toUnsigned(c));
//			System.out.println("b>a");
//		}
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
		String extraString = "右键可添加Cpu和子目录 :";
		String QAQ = "\tIDE分级目录的形式管理操作系统支持的众多Cpu，本界面用于管理Cpu的分类,\n包括添加Cpu和分组,手动拖拽即可移动分组";
		String descTitle = "分组/Cpu描述";

		Label extraLabel = new Label(infoArea,SWT.NULL);
		extraLabel.setText(extraString);
		extraLabel.setForeground(infoArea.getDisplay().getSystemColor(SWT.COLOR_RED));
		FontData newFontData = extraLabel.getFont().getFontData()[0];
		newFontData.setStyle(SWT.ITALIC | SWT.BOLD);
		newFontData.setHeight(8);
		extraLabel.setFont(new Font(infoArea.getDisplay(),newFontData));
		
		Composite contentCpt = new Composite(infoArea, SWT.NULL);
		GridLayout contentLayout = new GridLayout();
		contentLayout.numColumns = 2;
		contentCpt.setLayout(contentLayout);
		contentCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite sourceTreeCpt = new Composite(contentCpt, SWT.NULL);
		tree = new Tree(sourceTreeCpt, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setSize(200, 260);
		initPopup();
		
		Composite sashForm = new Composite(contentCpt, SWT.NULL);
		sashForm.setLayout(new RowLayout());
		configInfoText = new Text(sashForm, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		configInfoText.setEditable(false);
		configInfoText.setLayoutData(new RowData(350,250));

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
									item.setImage(DPluginImages.TREE_FLODER_VIEW.createImage());
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
					cpuCreated = dialog.getCpuCreated();
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
									item.setImage(DPluginImages.DESC_CPU_VIEW.createImage());
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
		
		reviseItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				List<String> configs = null;
				cpu = new Cpu();
				TreeItem[] items = tree.getSelection();
				String curFilePath = items[0].getData().toString();//获取当前选中文件的路径
				String tag = "cpu";
				
				if(items.length>0) {
					File curFile = new File(curFilePath);//当前选中文件
					File xmlParentFile = getXmlFile(curFile);	
					boolean exiteDirectory= isExiteDirectory(curFile);
					if(exiteDirectory) {
						tag = "group";
					}
					if(xmlParentFile!=null) {
						if(xmlParentFile.getName().contains("group")) {
							tag = "group";
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
				
				NewGroupOrCpuDialog dialog = new NewGroupOrCpuDialog(getShell(),configs,cpu,curFilePath,"revise_"+tag);
//				ResetConfigurationDialog dialog = new ResetConfigurationDialog(getShell(),configs,cpu,curFilePath,isGroup);
				if (dialog.open() == Window.OK) {
					TreeItem parentItem = items[0].getParentItem();
					parentItem.removeAll();
					new TreeItem(parentItem, 0);
					parentItem.setExpanded(false);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});

		deleteItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = tree.getSelection();
				IWorkbenchWindow window = PlatformUI.getWorkbench()
	    				.getActiveWorkbenchWindow();
	        	boolean isSure = MessageDialog.openQuestion(window.getShell(), "提示",
	        			"您确认要删除["+items[0].getText()+"]吗?");
	        	if(isSure) {      		
					String curFilePath = items[0].getData().toString();//获取当前选中文件的路径
					DeleteFolder dlf = new DeleteFolder();
					dlf.deleteFolder(curFilePath);
					items[0].dispose();
	        	}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});

		File file = new File(eclipsePath + "djysrc\\bsp");
		File[] roots = file.listFiles();
		for (int i = 0; i < roots.length; i++) {
			// TreeItem root = new TreeItem(tree, 0);
			if (roots[i].getName().equals("cpudrv")) {
				TreeItem root = new TreeItem(tree, 0);
				root.setText("Djyos");
				root.setImage(DPluginImages.TREE_FLODER_VIEW.createImage());
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
								item.setImage(DPluginImages.TREE_FLODER_VIEW.createImage());
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
					String isDropable =  isFileDropable(srcFile,new File(eventItem.getData().toString()));
					if(isDropable == null) {
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
						
						DeleteFolder dlf = new DeleteFolder();
						dlf.deleteFolder(fileItem.getData().toString());
						// 删除原来的节点
						if (tree != null) {
							fileItem.dispose();
						}
					}else {
						IWorkbenchWindow window = PlatformUI.getWorkbench()
    							.getActiveWorkbenchWindow();
						MessageDialog.openError(window.getShell(), "提示",
								isDropable);
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
					if ((files[i].isHidden() == false || files[i].getName().endsWith(".xml"))) {
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
									item.setImage(DPluginImages.DESC_CPU_VIEW.createImage());
									item.setExpanded(false);
								}else {
									item.setImage(DPluginImages.TREE_FLODER_VIEW.createImage());
									new TreeItem(item, 0);
								}
								// 叶子节点对应的数值为相应文件夹的File对象
								item.setData(files[i]);						
							}
						}
						
					}
				}
			}
		});

		tree.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				String descTitleChang = null;
				Point point = new Point(event.x, event.y);
				TreeItem item = tree.getItem(point);
				if(item != null) {
					if(item.getText().equals("Djyos")) {
						deleteItem.setEnabled(false);
						reviseItem.setEnabled(false);
					}else {
						deleteItem.setEnabled(true);
						reviseItem.setEnabled(true);
					}
				}
				
				if ((item != null) && (item.getData() != null)) {
					File file = new File(item.getData().toString()); 
					if(item.getText().equals("Djyos")) {
						descTitleChang = descTitle;
					}

//					if(getXmlFile(file)==null) {
//						reviseItem.setEnabled(false);
//					}else {
//						reviseItem.setEnabled(true);
//					}
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
						
						String descContent = "";
						descContent = traverseParents(file,descContent);
						
						//显示当前选中分组/Cpu的配置信息
						if(descTitleChang!=null) {
							configInfoText.setText(descContent);
						}	

					} else {
//						addCpuBtn.setEnabled(false);
						newCpuItem.setEnabled(false);
					}
				}
				
			}

		});
		
		tree.addListener(SWT.MouseDoubleClick, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				List<String> configs = null;
				cpu = new Cpu();
				TreeItem[] items = tree.getSelection();
				String curFilePath = items[0].getData().toString();//获取当前选中文件的路径
				String tag = "cpu";
				
				if(items.length>0) {
					File curFile = new File(curFilePath);//当前选中文件
					File xmlParentFile = getXmlFile(curFile);	
					if(xmlParentFile!=null) {
						if(xmlParentFile.getName().contains("group")) {
							tag = "group";
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
				
				NewGroupOrCpuDialog dialog = new NewGroupOrCpuDialog(getShell(),configs,cpu,curFilePath,"revise_"+tag);
				if (dialog.open() == Window.OK) {
					TreeItem parentItem = items[0].getParentItem();
					parentItem.removeAll();
					new TreeItem(parentItem, 0);
					parentItem.setExpanded(false);
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
	
	protected String isFileDropable(File srcFile, File destFile) {
		// TODO Auto-generated method stub
		File tempSrcFile = new File(srcFile.getPath());
		File tempDestFile = new File(destFile.getPath());
		while(!tempSrcFile.getName().equals("cpudrv")) {		
			tempSrcFile = tempSrcFile.getParentFile();
			if(tempSrcFile.getName().equals(destFile.getName())) {
				return "子目录不可托拉到父目录！";
			}
		}
		
		tempSrcFile = new File(srcFile.getPath());
		tempDestFile = new File(destFile.getPath());
		while(!tempDestFile.getName().equals("cpudrv")) {
			tempDestFile = tempDestFile.getParentFile();
			if(tempDestFile.getName().equals(srcFile.getName())) {
				return "父目录不可托拉到子目录！";
			}
		}
		
		tempDestFile = new File(destFile.getPath());
		File[] destFiles = tempDestFile.listFiles();
		for(File file : destFiles) {
			if(file.getName().endsWith(".xml") && file.getName().contains("cpu_") && !file.getName().contains("group") ) {
				return "不可托拉到Cpu目录下！";
			}
		}
		return null;
	}

	private boolean containsXml(File file) {
		File[] files = file.listFiles();
		for(File f:files) {
			if(f.isDirectory()) {
				if(containsXml(f)) {
					return true;
				}
			}else {
				if(f.getName().endsWith(".xml")) {
					return true;
				}
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
	
	private String traverseParents(File curFile,String descContent) {
		if (!curFile.getName().contains("cpudrv")) {
			File xmlFile = getXmlFile(curFile);
			if(xmlFile != null) {
				try {
					Cpu cpu = rcx.getCpuInfos(xmlFile);

					if (cpu.getCoreNum() != 0) {
						if(xmlFile.getName().contains("group")) {
							descContent += "子目录["+curFile.getName()+"]配置：";
						}else {
							descContent += "Cpu  ["+curFile.getName()+"]配置：";
						}
						
						for (int i = 0; i < cpu.getCoreNum(); i++) {
							Core core = cpu.getCores().get(i);
							descContent += "\n内核" + (i + 1) + "：";
							if (core.getType() != null) {
								descContent += core.getType();
							}
							if (core.getArch() != null) {
								descContent += "，架构：" + core.getArch();
							}
							if (core.getFamily() != null) {
								descContent += "，家族：" + core.getFamily();
							}
							if (core.getFpuType() != null) {
								descContent += "\n\t浮点：" + core.getFpuType();
							}
							if (core.getResetAddr() != null) {
								descContent += "\n\t复位地址：" + core.getResetAddr();
							}
							if(core.getMemorys().size()!=0) {
								List<CoreMemory> memorys = core.getMemorys();
								for(int j = 0; j < memorys.size(); j++) {
									descContent += "\n\t内存" + (j + 1) + "：";
									if (memorys.get(j).getType() != null) {
										descContent +=  memorys.get(j).getType();
									}
									if (memorys.get(j).getStartAddr() != null) {
										descContent += "，起始地址：" + memorys.get(j).getStartAddr();
									}
									if (memorys.get(j).getSize() != null) {
										descContent += "，大小：" + memorys.get(j).getSize();
									}		
								}
							}
							descContent += "\n----------------------------------------------------------\n";
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			File parentFile = curFile.getParentFile();
			descContent = traverseParents(parentFile, descContent);
			
		}
		return descContent;
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
	private boolean isExiteDirectory(File parentFile) {
		File file =null;
		File[] files = parentFile.listFiles();
		for(int i=0;i<files.length;i++){
			if(files[i].isDirectory()) {
				return true;
			}
		}
		return false;
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
