package com.djyos.dide.ui.bsp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.djyos.dide.ui.DPluginImages;

public class SelectUserDefinedFilePathDialog extends StatusDialog {

	private List<String> fileNames;
	private List<String> lastMembers;
	List<File> files = new ArrayList<>();
	List<File> objectFiles;
	private Tree tree;

	public List<File> getFiles() {
		return files;
	}

	public SelectUserDefinedFilePathDialog(Shell parent, List<String> openFilenames, List<File> openObjectFiles,
			List<String> openLastMembers) {
		super(parent);
		// TODO Auto-generated constructor stub
		setTitle("选择文件分类层次");
		fileNames = openFilenames;
		lastMembers = openLastMembers;
		objectFiles = openObjectFiles;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setSize(500, 500);
		GridLayout layout = new GridLayout();
		layout.marginTop = 5;
		layout.numColumns = 1;
		layout.marginLeft = 5;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		createContent(composite);
		return super.createDialogArea(parent);
	}

	private void createContent(Composite composite) {
		// TODO Auto-generated method stub
		Composite treeComposite = new Composite(composite, SWT.NONE);
		tree = new Tree(treeComposite, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.CHECK);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		for (int i = 0; i < fileNames.size(); i++) {
			TreeItem root;
			root = new TreeItem(tree, 0);
			File rootFile = getRootFile(objectFiles.get(i), lastMembers.get(i));
			root.setText(rootFile.getName() + "[选择" + fileNames.get(i) + "分类层次]");
			root.setImage(DPluginImages.CFG_CPMT_OBJ.createImage());
			root.setData("root");// 保存当前节点数据
			root.setData("filename", fileNames.get(i));

			List<String> parentFileNames = new ArrayList<String>();
			getParentFileNames(objectFiles.get(i), lastMembers.get(i), parentFileNames);
			expendFileTree(root, rootFile, parentFileNames);
		}
		tree.setSize(480, 350);
		tree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem item = (TreeItem) e.item;
				if (item == null) {
					return;
				} else {
					boolean checked = item.getChecked();
					TreeItem rootItem;
					if (item.getParentItem() != null) {
						rootItem = getRootItem(item);
					} else {
						rootItem = item;
					}
					if (checked) {
						rootItem.setChecked(false);
						String filename = rootItem.getData("filename").toString();
						String parentPath = item.getData().toString() + "/"
								+ (filename.endsWith(".h") ? "include" : "src");
						File parentFile = new File(parentPath);
						if (!parentFile.exists()) {
							parentFile.mkdir();
						}

						rootItem.setData("filePath", parentPath + "/" + filename);
						setChildTreeItems(rootItem, false);
						item.setChecked(true);
					} else {
						rootItem.setData("filePath", null);
					}
				}
			}
		});

	}

	private void setChildTreeItems(TreeItem rootItem, boolean checked) {
		TreeItem[] items = rootItem.getItems();
		for (TreeItem t : items) {
			t.setChecked(checked);
			setChildTreeItems(t, checked);
		}
	}

	private File getRootFile(File objectFile, String lastMember) {
		// TODO Auto-generated method stub
		File parentFile = objectFile.getParentFile();
		if (!parentFile.getName().equals(lastMember)) {
			objectFile = getRootFile(parentFile, lastMember);
		}
		return objectFile;
	}

	private void getParentFileNames(File objectFile, String lastMember, List<String> parentFileNames) {
		// TODO Auto-generated method stub
		if (objectFile.isDirectory()) {
			parentFileNames.add(objectFile.getName());
		}
		File parentFile = objectFile.getParentFile();
		if (!parentFile.getName().equals(lastMember)) {
			getParentFileNames(parentFile, lastMember, parentFileNames);
		}
	}

	protected TreeItem getRootItem(TreeItem item) {
		// TODO Auto-generated method stub
		TreeItem parentItem = item.getParentItem();
		if (parentItem.getData().toString().equals("root")) {
			return parentItem;
		} else {
			return getRootItem(parentItem);
		}
	}

	private void expendFileTree(TreeItem root, File file, List<String> parentFileNames) {
		// TODO Auto-generated method stub
		File[] files = file.listFiles();
		for (File f : files) {
			if (parentFileNames.contains(f.getName()) && f.isDirectory()) {
				TreeItem item = new TreeItem(root, 0);
				item.setText(f.getName());
				item.setImage(DPluginImages.TREE_FLODER_VIEW.createImage());
				item.setData(f.getPath());
				expendFileTree(item, f, parentFileNames);
			}
		}
	}

	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		TreeItem[] items = tree.getItems();
		for (TreeItem t : items) {
			if (t.getData("filePath") != null) {
				String filePath = t.getData("filePath").toString();
				if (filePath != null) {
					File file = new File(filePath);
					files.add(file);
				}
			}

		}
		super.okPressed();
	}
}
