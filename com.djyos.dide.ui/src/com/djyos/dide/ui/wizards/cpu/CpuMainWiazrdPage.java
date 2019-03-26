package com.djyos.dide.ui.wizards.cpu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

import com.djyos.dide.ui.DPluginImages;
import com.djyos.dide.ui.arch.ArchHelper;
import com.djyos.dide.ui.arch.NewArchDialog;
import com.djyos.dide.ui.arch.ReadArchXml;
import com.djyos.dide.ui.arch.ReviceArchDialog;
import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.messages.ICpuConstants;
import com.djyos.dide.ui.messages.IPrompt;
import com.djyos.dide.ui.objects.Arch;
import com.djyos.dide.ui.objects.Core;
import com.djyos.dide.ui.objects.CoreMemory;
import com.djyos.dide.ui.objects.Cpu;
import com.djyos.dide.ui.wizards.djyosProject.tools.DeleteFolderUtils;

@SuppressWarnings("restriction")
public class CpuMainWiazrdPage extends WizardPage {

	public static final IPath ICONS_PATH = new Path("$nl$/icons"); //$NON-NLS-1$
	public static TreeItem fileItem = null, tmssItem = null;
	private Cpu cpu = new Cpu(), cpuCreated = null;
	private Tree tree;
	private Text configInfoText = null;
	private String didePath = DideHelper.getDIDEPath();
	private Menu menu;
	private MenuItem newGroupItem, newCpuItem, deleteItem, reviseItem, newDrvItem;
	private IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	private ReadArchXml rax = new ReadArchXml();

	public Cpu getCpuCreated() {
		return cpuCreated;
	}

	protected CpuMainWiazrdPage(String pageName) {
		super(pageName);
		// TODO Auto-generated constructor stub
		setPageComplete(true);
	}

	public void init_CpuTreePopup() {
		menu = new Menu(tree);
		newGroupItem = new MenuItem(menu, SWT.PUSH);
		newGroupItem.setText(ICpuConstants.newGroupLabel);
		newGroupItem.setImage(DPluginImages.OBJ_GROUP_VIEW.createImage());

		newCpuItem = new MenuItem(menu, SWT.PUSH);

		newDrvItem = new MenuItem(menu, SWT.PUSH);
		newDrvItem.setText(ICpuConstants.newCpudrvLabel);
		newDrvItem.setImage(DPluginImages.OBJ_DRV_VIEW.createImage());

		reviseItem = new MenuItem(menu, SWT.PUSH);
		reviseItem.setText(ICpuConstants.reviseConfigureLabel);
		reviseItem.setImage(DPluginImages.CPU_REVISE_VIEW.createImage());

		deleteItem = new MenuItem(menu, SWT.PUSH);
		deleteItem.setText(ICpuConstants.deleteLabel);
		deleteItem.setImage(DPluginImages.CFG_DELETE_OBJ.createImage());

		tree.setMenu(menu);
	}

	private void handle_MouseDown(TreeItem item) {
		if (item != null) {
			if (item.getData("type").equals("arch")) {
				// newGroupItem.set
				newCpuItem.setText(ICpuConstants.newArchLabel);
				newCpuItem.setImage(DPluginImages.OBJ_ARCH_VIEW.createImage());
			} else {
				newCpuItem.setText(ICpuConstants.newCpuLabel);
				newCpuItem.setImage(DPluginImages.OBJ_CPU_VIEW.createImage());
			}
			display_Details(item);
		}
	}

	private void handle_MouseDoubleClick(TreeItem item) {
		if (item != null) {
			if (!item.getText().contains("列表") && item.getData("type").equals("cpu")) {
				Handle_ReviceCpu();
			}
		}
	}

	private void handle_Delete_CpuTreeItem(TreeItem item) {
		boolean isSure = MessageDialog.openQuestion(window.getShell(), IPrompt.promptLabel,
				"您确认要删除[" + item.getText() + "]吗?");
		if (isSure) {
			String curFilePath = item.getData().toString();// 获取当前选中文件的路径
			DeleteFolderUtils dlf = new DeleteFolderUtils();
			dlf.deleteFolder(curFilePath);
			item.dispose();
		}
	}

	private Listener treeMouseDownListener = e -> {
		Point point = new Point(e.x, e.y);
		TreeItem item = tree.getItem(point);
		handle_MouseDown(item);
	};

	private Listener treeDoubleClickListener = e -> {
		Point point = new Point(e.x, e.y);
		TreeItem item = tree.getItem(point);
		handle_MouseDoubleClick(item);
	};

	private void setMenuEnable(boolean enable) {
		newGroupItem.setEnabled(enable);
		newCpuItem.setEnabled(enable);
		newDrvItem.setEnabled(enable);
		reviseItem.setEnabled(enable);
		deleteItem.setEnabled(enable);
	}

	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub

		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		initializeDialogUnits(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);

		// IToolChain[] r_tcs = ManagedBuildManager.getRealToolChains();
		// IResourceInfo resourceInfo = cfg.getRootFolderInfo();
		// IToolListModification mod = getModification(tcmmgr,resourceInfo);
		// IFolderInfoModification foim = (IFolderInfoModification)mod;
		// for(IToolChain tc:r_tcs) {
		// System.out.println("tc.getName(): "+tc.getName());
		// }
		boolean srcExist = DideHelper.isDjysrcExist();
		if (srcExist) {
			createDynamicGroup(composite);
		} else {
			MessageDialog.openInformation(window.getShell(), IPrompt.promptLabel, IPrompt.djysrcNotExit);
		}
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
		Dialog.applyDialogFont(composite);
	}

	private void createDynamicGroup(Composite composite) {
		// TODO Auto-generated method stub

		ScrolledComposite scrolledComposite = new ScrolledComposite(composite, SWT.V_SCROLL | SWT.H_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite infoArea = new Composite(scrolledComposite, SWT.NONE);
		GridLayout infoLayout = new GridLayout();
		infoLayout.numColumns = 1;
		infoArea.setLayout(infoLayout);
		GridData data = new GridData(GridData.FILL_BOTH);
		infoArea.setLayoutData(data);

		Label extraLabel = new Label(infoArea, SWT.NULL);
		extraLabel.setText(ICpuConstants.promptLabel);
		extraLabel.setForeground(infoArea.getDisplay().getSystemColor(SWT.COLOR_RED));
		FontData newFontData = extraLabel.getFont().getFontData()[0];
		newFontData.setStyle(SWT.ITALIC | SWT.BOLD);
		newFontData.setHeight(8);
		extraLabel.setFont(new Font(infoArea.getDisplay(), newFontData));

		SashForm sashForm = new SashForm(infoArea, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree = new Tree(sashForm, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		tree.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		init_CpuTreePopup();

		configInfoText = new Text(sashForm, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		configInfoText.setEditable(false);
		configInfoText.setText(ICpuConstants.infoPromptLabel);
		init_CpuTree();
		ArchHelper.initArchTree(tree);
		handle_TreeDrag();
		sashForm.setWeights(new int[] { 1, 2 });// 内部容器之间宽度比例

		newGroupItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = tree.getSelection();
				String type = items[0].getData("type").toString();
				if (type.equals("arch")) {
					handle_ArchItemClick("group");
				} else {
					Handle_NewCpuOrGroup_Click("group");
				}
			}
		});

		newCpuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = tree.getSelection();
				String type = items[0].getData("type").toString();
				if (type.equals("arch")) {
					handle_ArchItemClick("arch");
				} else {
					Handle_NewCpuOrGroup_Click("cpu");
				}
			}
		});

		newDrvItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = tree.getSelection();
				TreeItem item = items[0];
				Handle_NewDrv_Click(item);
			}
		});

		reviseItem.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = tree.getSelection();
				String type = items[0].getData("type").toString();
				if (type.equals("arch")) {
					handle_ArchItemClick("revise");
				} else {
					Handle_ReviceCpu();
				}
			}

		});

		deleteItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = tree.getSelection();
				handle_Delete_CpuTreeItem(items[0]);
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
						if (files[i].isDirectory()) {
							boolean isNeed = DideHelper.travelContainsXml(files[i]);
							if (isNeed) {
								toExpand = true;
							}
						}
						if (toExpand) {

							// 当前为文件目录而不是文件的时候，添加新项目，以便只是显示文件夹（包括空文件夹），而不显示文件夹下的文件
							if (files[i].isDirectory() && files[i].getName() != "include"
									&& files[i].getName() != "src") {
								TreeItem item = new TreeItem(root, 0);
								item.setText(files[i].getName());
								boolean configed = isCpu(files[i]);
								if (configed) {
									item.setImage(DPluginImages.OBJ_CPU_VIEW.createImage());
									item.setExpanded(false);
								} else {
									item.setImage(DPluginImages.TREE_FLODER_VIEW.createImage());
									new TreeItem(item, 0);
								}
								// 叶子节点对应的数值为相应文件夹的File对象
								item.setData(files[i]);
								item.setData("type", item.getParentItem().getData("type"));
							}
						}

					}
				}
			}
		});

		tree.addListener(SWT.MouseDown, treeMouseDownListener);

		tree.addListener(SWT.MouseDoubleClick, treeDoubleClickListener);

		Point point = infoArea.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		scrolledComposite.setContent(infoArea);
		scrolledComposite.setMinHeight(point.y);

		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

	}

	protected void Handle_NewDrv_Click(TreeItem item) {
		// TODO Auto-generated method stub

		NewCpuDrvDialog dialog = new NewCpuDrvDialog(getShell(), ICpuConstants.addCpudrvLabel);
		if (dialog.open()) {
			String cpudrvName = dialog.getCpudrvName();

			File srcFolder = new File(item.getData().toString() + "/src");
			File incFolder = new File(item.getData().toString() + "/include");
			if (!srcFolder.exists()) {
				srcFolder.mkdir();
			}
			if (!incFolder.exists()) {
				incFolder.mkdir();
			}
			File srcFile = new File(item.getData().toString() + "/src/cpu_peri_" + cpudrvName + ".c");
			File incFile = new File(item.getData().toString() + "/include/cpu_peri_" + cpudrvName + ".h");
			DideHelper.createNewFile(incFile);
			DideHelper.createNewFile(srcFile);
			String path = DideHelper.getTemplatePath();
			File CFile = new File(path + "/FileTemp/CFileTemplate");
			File HFile = new File(path + "/FileTemp/HFileTemplate");
			String CString = DideHelper.readFile(CFile);
			String HString = DideHelper.readFile(HFile);
			DideHelper.writeFile(srcFile, CString);
			DideHelper.writeFile(incFile, HString);
			DideHelper.openFileInDide(srcFile);
			DideHelper.openFileInDide(incFile);

			try {
				final IWorkspace workspace = ResourcesPlugin.getWorkspace();
				boolean exist = MessageDialog.openQuestion(getShell(), IPrompt.promptLabel,
						ICpuConstants.exitCpudrvInterfaceQus);
				if (exist) {
					getShell().dispose();
					workspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
				}
			} catch (CoreException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}
	}

	protected void handle_ArchItemClick(String tag) {
		// TODO Auto-generated method stub
		TreeItem[] items = tree.getSelection();
		String curFilePath = items[0].getData().toString();// 获取当前选中文件的路径
		File curFile = new File(curFilePath);// 当前选中文件
		File xmlFile = DideHelper.getXmlFile(curFile);

		List<String> configs = null;
		Arch arch = new Arch();
		arch = rax.getMutiplyFileArch(curFile, arch);
		configs = getArchConfigs(arch);
		if (tag.equals("revise")) {
			ReviceArchDialog dialog = new ReviceArchDialog(getShell(), configs,
					tag + "_" + (xmlFile.getName().equals("arch.xml") ? "arch" : "group"));
			dialog.open();
		} else {
			NewArchDialog dialog = new NewArchDialog(getShell(), configs, tag);
			dialog.open();
		}

	}

	private List<String> getArchConfigs(Arch arch) {
		// TODO Auto-generated method stub
		List<String> configs = new ArrayList<String>();
		if (arch.getToolchain() == null) {
			configs.add(ICpuConstants.toolchainCfgLabel);
		}
		if (arch.getMarch() == null) {
			configs.add(ICpuConstants.archCfgLabel);
		}
		if (arch.getMcpu() == null) {
			configs.add(ICpuConstants.familyCfgLabel);
		}
		if (arch.getFpuType() == null) {
			configs.add(ICpuConstants.floatCfgLabel);
		}
		return configs;
	}

	protected void Handle_NewCpuOrGroup_Click(String tag) {
		// TODO Auto-generated method stub
		List<String> configs = null;
		cpu = new Cpu();
		TreeItem[] items = tree.getSelection();
		String curFilePath = items[0].getData().toString();// 获取当前选中文件的路径
		if (items.length > 0) {
			File curFile = new File(curFilePath);// 当前选中文件
			File xmlFile = DideHelper.getXmlFile(curFile);
			if (xmlFile != null) {
				try {
					ReadCpuXml.unitCpu(cpu, xmlFile);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			traverseParents(curFile);
			configs = getCpuConfigs(cpu, false);
		}

		NewGroupOrCpuDialog dialog = new NewGroupOrCpuDialog(getShell(), configs, cpu, curFilePath, tag);
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
						boolean isNeed = DideHelper.travelContainsXml(files[i]);
						if (isNeed) {
							TreeItem item = new TreeItem(root, 0);
							item.setText(files[i].getName());
							if (tag.equals("cpu")) {
								item.setImage(DPluginImages.OBJ_CPU_VIEW.createImage());
							} else {
								item.setImage(DPluginImages.TREE_FLODER_VIEW.createImage());
							}

							// 叶子节点对应的数值为相应文件夹的File对象
							item.setData(files[i]);
							item.setData("type", "cpu");
							new TreeItem(item, 0);
							if (files[i].getName().equals(newFileName)) {
								tree.select(item);
								display_Details(item);
							}
						}
					}
				}
			}
			cpuCreated = dialog.getCpuCreated();
		}
	}

	private void init_CpuTree() {
		// TODO Auto-generated method stub
		File file = new File(didePath + "djysrc\\bsp");
		File[] roots = file.listFiles();
		for (int i = 0; i < roots.length; i++) {
			if (roots[i].getName().equals("cpudrv")) {
				TreeItem root = new TreeItem(tree, 0);
				root.setText(ICpuConstants.cpuListTitle);
				root.setImage(DPluginImages.CFG_CPMT_OBJ.createImage());
				root.setData(roots[i]);// 保存当前节点数据
				root.setData("type", "cpu");
				File dfile = (File) root.getData();
				File[] files = dfile.listFiles();
				for (int j = 0; j < files.length; j++) {
					if ((files[j].isHidden() == false)) {// 判断当前路径是否为隐藏文件与文件夹
						boolean toExpand = false;
						if (files[j].isDirectory()) {
							boolean isNeed = DideHelper.travelContainsXml(files[j]);
							if (isNeed) {
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
							item.setData("type", "cpu");
							new TreeItem(item, 0);
						}

					}
				}
				root.setExpanded(true);
				break;
			}
		}
	}

	private void handle_TreeDrag() {
		// TODO Auto-generated method stub
		// 定义拖放源对象
		DragSource dragSource = new DragSource(tree, DND.DROP_MOVE | DND.DROP_COPY);
		// 设置传输的数据为文本型String类型
		dragSource.setTransfer(new Transfer[] { TextTransfer.getInstance() });
		// 注册拖放源时的事件处理
		dragSource.addDragListener(new DragSourceListener() {

			@Override
			public void dragStart(DragSourceEvent event) {
				// TODO Auto-generated method stub
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

				if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
					// 获得数据源设置的字符串
					String s = (String) event.data;
					// 在tmss位置插入一个节点
					File srcFile = new File(fileItem.getData().toString());
					File destFile = new File(eventItem.getData().toString() + "\\" + s);
					String isDropable = isFileDropable(srcFile, new File(eventItem.getData().toString()));
					if (isDropable == null) {
						tmssItem = new TreeItem(eventItem, SWT.NONE);
						boolean configed = isCpu(srcFile);
						if (configed) {
							tmssItem.setImage(DPluginImages.OBJ_CPU_VIEW.createImage());
							tmssItem.setExpanded(false);
						} else {
							tmssItem.setImage(DPluginImages.TREE_FLODER_VIEW.createImage());
							new TreeItem(tmssItem, 0);
						}

						tmssItem.setText(s);
						tmssItem.setData(destFile);
						DideHelper.copyFolder(srcFile, destFile);

						DeleteFolderUtils dlf = new DeleteFolderUtils();
						dlf.deleteFolder(fileItem.getData().toString());
						// 删除原来的节点
						if (tree != null) {
							fileItem.dispose();
						}
					} else {
						IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						MessageDialog.openError(window.getShell(), IPrompt.promptLabel, isDropable);
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

	}

	protected void Handle_ReviceCpu() {
		// TODO Auto-generated method stub
		List<String> configs = null;
		cpu = new Cpu();
		TreeItem[] items = tree.getSelection();
		String curFilePath = items[0].getData().toString();// 获取当前选中文件的路径
		String tag = "cpu";

		if (items.length > 0) {
			File curFile = new File(curFilePath);// 当前选中文件
			File xmlParentFile = DideHelper.getXmlFile(curFile);
			boolean exiteDirectory = isExiteDirectory(curFile);
			if (exiteDirectory) {
				tag = "group";
			}
			if (xmlParentFile != null) {
				if (xmlParentFile.getName().contains("group")) {
					tag = "group";
				}
				try {
					ReadCpuXml.unitCpu(cpu, xmlParentFile);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			traverseParents(curFile);
			configs = getCpuConfigs(cpu, false);
		}

		NewGroupOrCpuDialog dialog = new NewGroupOrCpuDialog(getShell(), configs, cpu, curFilePath, "revise_" + tag);
		if (dialog.open() == Window.OK) {
			TreeItem parentItem = items[0].getParentItem();
			String reviseName = dialog.getGroupName();
			items[0].dispose();
			TreeItem item = new TreeItem(parentItem, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL, 0);
			item.setText(reviseName);
			if (tag.endsWith("cpu")) {
				item.setImage(DPluginImages.OBJ_CPU_VIEW.createImage());
				item.setExpanded(false);
			} else {
				item.setImage(DPluginImages.TREE_FLODER_VIEW.createImage());
				new TreeItem(item, 0);
			}
			item.setData(new File(parentItem.getData().toString() + "/" + reviseName));
			tree.select(item);
			display_Details(item);
		}
	}

	private String isFileDropable(File srcFile, File destFile) {
		// TODO Auto-generated method stub
		File tempSrcFile = new File(srcFile.getPath());
		File tempDestFile = new File(destFile.getPath());

		if (tempSrcFile.getName().equals(destFile.getName())) {
			return ICpuConstants.notDragToSelf;
		}
		File parentSrcFile = tempSrcFile.getParentFile();
		if (parentSrcFile.getName().equals(destFile.getName())) {
			return "该目录下已经存在[" + tempSrcFile.getName() + "]！";
		}

		tempSrcFile = new File(srcFile.getPath());
		tempDestFile = new File(destFile.getPath());
		while (!tempDestFile.getName().equals("cpudrv")) {
			tempDestFile = tempDestFile.getParentFile();
			if (tempDestFile.getName().equals(srcFile.getName())) {
				return ICpuConstants.notDragToChild;
			}
		}

		tempDestFile = new File(destFile.getPath());
		File[] destFiles = tempDestFile.listFiles();
		for (File file : destFiles) {
			if (file.getName().endsWith(".xml") && file.getName().contains("cpu_")
					&& !file.getName().contains("group")) {
				return ICpuConstants.notDragToCpu;
			}
		}
		return null;
	}

	protected List<String> getCpuConfigs(Cpu cpu2, boolean isCpu) {
		// TODO Auto-generated method stub
		List<String> cons = new ArrayList<String>();
		if (cpu2.getCores().size() == 0) {
			cons.add(ICpuConstants.coreNumLabel);
			cons.add(ICpuConstants.resetLabel);
			cons.add(ICpuConstants.floatCfgLabel);
			cons.add(ICpuConstants.coreCfgLabel);
			cons.add(ICpuConstants.memoryCfgLabel);
		} else {
			if (cpu2.getCoreNum() == 0) {
				cons.add(ICpuConstants.coreNumLabel);
			}
			if (cpu2.getCores().get(0).getResetAddr() == null) {
				cons.add(ICpuConstants.resetLabel);
			}
			if (cpu2.getCores().get(0).getMemorys().size() == 0) {
				cons.add(ICpuConstants.memoryCfgLabel);
			}
			if (cpu2.getCores().get(0).getFpuType() == null) {
				cons.add(ICpuConstants.floatCfgLabel);
			}
			if (cpu2.getCores().get(0).getArch().getMcpu() == null) {
				cons.add(ICpuConstants.coreCfgLabel);
			}
		}
		return cons;
	}

	private void traverseParents(File curFile) {
		if (!curFile.getName().contains("cpudrv")) {
			File parentFile = curFile.getParentFile();
			if (!parentFile.getName().contains("cpudrv")) {
				File xmlFile = DideHelper.getXmlFile(parentFile);
				try {
					if (xmlFile != null) {
						ReadCpuXml.unitCpu(cpu, xmlFile);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				traverseParents(parentFile);
			}
		}
	}

	private String traverseParents(File curFile, String descContent, String type) {

		File xmlFile = DideHelper.getXmlFile(curFile);
		if (type.equals("cpu") && !curFile.getName().contains("cpudrv")) {
			if (xmlFile != null) {
				try {
					Cpu cpu = ReadCpuXml.getCpuInfos(xmlFile);
					if (cpu.getCoreNum() != 0) {
						if (xmlFile.getName().contains("group")) {
							descContent += "子目录[" + curFile.getName() + "]配置：";
						} else {
							descContent += "Cpu  [" + curFile.getName() + "]配置：";
						}
						descContent += "\n" + ICpuConstants.coreNumLabel + "： " + cpu.getCoreNum();
						for (int i = 0; i < cpu.getCoreNum(); i++) {
							Core core = cpu.getCores().get(i);
							descContent += "\n内核" + (i + 1) + "：";
							if (core.getArch().getSerie() != null) {
								descContent += core.getArch().getSerie();
							}
							if (core.getArch().getMarch() != null) {
								descContent += "，架构：" + core.getArch().getMarch();
							}
							if (core.getArch().getMcpu() != null) {
								descContent += "，家族：" + core.getArch().getMcpu();
							}
							if (core.getFpuType() != null) {
								descContent += "\n\t浮点：" + core.getFpuType();
							}
							if (core.getResetAddr() != null) {
								descContent += "\n\t复位地址：" + core.getResetAddr();
							}
							if (core.getMemorys().size() != 0) {
								List<CoreMemory> memorys = core.getMemorys();
								for (int j = 0; j < memorys.size(); j++) {
									descContent += "\n\t内存" + (j + 1) + "：";
									if (memorys.get(j).getType() != null) {
										descContent += memorys.get(j).getType();
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
			descContent = traverseParents(parentFile, descContent, type);

		} else if (type.equals("arch") && !curFile.getName().equals("arch")) {
			if (xmlFile != null) {
				Arch arch = rax.getSingleFileArch(xmlFile, new Arch());
				if (!xmlFile.getName().equals("arch.xml")) {
					descContent += "子目录[" + curFile.getName() + "]配置：";
				} else {
					descContent += "Arch  [" + curFile.getName() + "]配置：";
				}
				if (arch.getSerie() != null) {
					descContent += "\n系列：" + arch.getSerie();
				}
				if (arch.getMarch() != null) {
					descContent += "\n架构：" + arch.getMarch();
				}
				if (arch.getMcpu() != null) {
					descContent += "\n家族：" + arch.getMcpu();
				}
				if (arch.getToolchain() != null) {
					descContent += "\n工具链：" + arch.getToolchain();
				}
				if (arch.getFpuType() != null) {
					descContent += "\n浮点：" + arch.getFpuType();
				}
				descContent += "\n----------------------------------------------------------\n";
			}
			File parentFile = curFile.getParentFile();
			descContent = traverseParents(parentFile, descContent, type);
		}
		return descContent;
	}

	private boolean isExiteDirectory(File parentFile) {
		File[] files = parentFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public IWizardPage getNextPage() {
		// TODO Auto-generated method stub
		return super.getNextPage();
	}

	private boolean isCpu(File file) {
		File[] cfiles = file.listFiles();
		for (int j = 0; j < cfiles.length; j++) {
			if (cfiles[j].getName().endsWith(".xml") && !cfiles[j].getName().contains("group")) {
				return true;
			}
		}
		return false;
	}

	protected void display_Details(TreeItem item) {
		String descTitleChang = null;
		if (item.getText().contains("列表")) {
			newDrvItem.setEnabled(false);
			newGroupItem.setEnabled(true);
			newCpuItem.setEnabled(false);
			deleteItem.setEnabled(false);
			reviseItem.setEnabled(false);
			if (item.getText().contains("Arch")) {
				newGroupItem.setEnabled(false);
			}
			configInfoText.setText("选中子目录或者Cpu/Arch即可显示相应的配置信息");
		} else {
			deleteItem.setEnabled(true);
			reviseItem.setEnabled(true);
			if (item.getData() != null) {
				String type = item.getData("type").toString();
				File file = new File(item.getData().toString());
				if (item.getText().contains("列表")) {
					descTitleChang = descTitle;
				}
				if (file.isDirectory()) {
					newGroupItem.setEnabled(true);
					newCpuItem.setEnabled(true);
					File[] files = file.listFiles();
					for (int i = 0; i < files.length; i++) {
						if (files[i].getName().endsWith(".xml")) {
							descTitleChang = "分组(" + item.getText() + ")描述";
							if (type.equals("arch")) {
								newDrvItem.setEnabled(false);
								setMenuEnable(false);
								menu.setVisible(false);
								if (files[i].getName().equals("arch.xml")) {
									newGroupItem.setEnabled(false);
									newCpuItem.setEnabled(false);
									descTitleChang = "Arch(" + item.getText() + ")描述";
								}
							} else {
								newDrvItem.setEnabled(true);
								if (!files[i].getName().contains("group")) {
									newGroupItem.setEnabled(false);
									newCpuItem.setEnabled(false);
									descTitleChang = "Cpu(" + item.getText() + ")描述";
								}
							}
							break;
						}
					}

					String descContent = "";
					descContent = traverseParents(file, descContent, type);

					// 显示当前选中分组/Cpu的配置信息
					if (descTitleChang != null) {
						configInfoText.setText(descContent);
					}

				} else {
					newCpuItem.setEnabled(false);
				}
			}
		}
		menu.setVisible(false);
	}

	private String descTitle = "分组/Cpu描述";
}
