package com.djyos.dide.ui.wizards.cpu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.djyos.dide.ui.DPluginImages;
import com.djyos.dide.ui.arch.ArchHelper;
import com.djyos.dide.ui.arch.ReadArchXml;
import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.objects.Arch;
import com.djyos.dide.ui.swt.DjyosUI;

public class SelectFamilyDialog extends StatusDialog implements ICpuConstants{
	
	private Tree archTree;
	private Arch selectArch;
	private String didePath = DideHelper.getDIDEPath();
	private List<TreeItem> archItems = new ArrayList<TreeItem>();
	
	public SelectFamilyDialog(Shell parent) {
		super(parent);
		// TODO Auto-generated constructor stub
		setTitle("选择内核");
	}
	
	public Arch getArch() {
		return selectArch;
	}
	
	@Override
	protected Point getInitialSize() {
		// TODO Auto-generated method stub
		return new Point(500, 500);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout layout = DjyosUI.DjyosGridLayout(1, 5, 5);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		initArchTree(composite);
		return super.createDialogArea(parent);
	}
	
	private void initArchTree(Composite composite) {
		// TODO Auto-generated method stub
		Composite archComposite = new Composite(composite, SWT.NULL);
		archTree = DideHelper.buildTree(archComposite, 480, 350, list_arch,(SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.CHECK));

		File file = new File(didePath + "djysrc/bsp/arch");
		File[] typeFiles = file.listFiles();
		for (int i = 0; i < typeFiles.length; i++) {
			if (DideHelper.travelContainsXml(typeFiles[i])) {
				TreeItem type = new TreeItem(archTree, SWT.NONE);
				type.setText(typeFiles[i].getName());
				type.setImage(DPluginImages.TREE_FLODER_VIEW.createImage());
				type.setData(typeFiles[i]);// 保存当前节点数据
				type.setGrayed(true);
				type.setChecked(true);
				new TreeItem(type, 0);
				ExpandTree(type);
			}
		}

		archTree.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem item = (TreeItem) e.item;
				if (item == null) {
					return;
				} else {
					if (item.getGrayed()) {
						item.setChecked(true);
					} else {
						for (TreeItem t : archItems) {
							if (t != item) {
								t.setChecked(false);
							}
						}

						// 获取当前的Arch对象
						ReadArchXml rax = new ReadArchXml();
						File xmlFile = new File(item.getData().toString());
						selectArch = new Arch();
						selectArch = rax.getMutiplyFileArch(xmlFile, selectArch);
					}
				}
			}

		});
	}
	
	private void ExpandTree(TreeItem root) {
		// TODO Auto-generated method stub
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
					if (files[i].isDirectory() && files[i].getName() != "include" && files[i].getName() != "src") {
						TreeItem item;
						boolean configed = ArchHelper.isFamily(files[i]);
						item = new TreeItem(root, SWT.NONE);
						item.setText(files[i].getName());
						if (configed) {// SWT.ERROR_CANNOT_SET_ENABLED
							item.setImage(DPluginImages.OBJ_ARCH_VIEW.createImage());
							item.setExpanded(false);
							File archFile = ArchHelper.getArchFile(files[i]);
							item.setData(archFile);
							archItems.add(item);
						} else {
							item.setImage(DPluginImages.TREE_FLODER_VIEW.createImage());
							item.setGrayed(true);
							item.setChecked(true);
							item.setData(files[i]);
							new TreeItem(item, 0);
							ExpandTree(item);
						}
					}
				}

			}
		}
	}

}
