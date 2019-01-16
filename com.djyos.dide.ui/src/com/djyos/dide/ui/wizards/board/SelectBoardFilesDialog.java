package com.djyos.dide.ui.wizards.board;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.djyos.dide.ui.DPluginImages;
import com.djyos.dide.ui.objects.Board;
import com.djyos.dide.ui.swt.DjyosUI;

public class SelectBoardFilesDialog extends StatusDialog {

	private Tree boardFileTree;
	private TreeItem root;
	private String similarBoardPath;
	Board board = null;
	List<File> files = new ArrayList<File>();
	private Listener selectionListener = e -> {
		TreeItem item = (TreeItem) e.item;
		if (item == null) {
			return;
		} else {
			boolean checked = item.getChecked();
			travelChildItems(item, checked);
			travelParentItems(item, checked);
		}
	};

	public SelectBoardFilesDialog(Shell parent, Board boardSelected) {
		super(parent);
		// TODO Auto-generated constructor stub
		setTitle("选择相似文件");
		// setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		board = boardSelected;
	}

	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		travelBoardTree(root);
		super.okPressed();
	}

	public List<File> getSelectFiles() {
		return files;
	}

	public String getSimilarBoardPath() {
		return similarBoardPath;
	}

	private void travelBoardTree(TreeItem root2) {
		// TODO Auto-generated method stub
		TreeItem[] items = root2.getItems();
		for (TreeItem t : items) {
			if (t.getChecked()) {
				File f = new File(t.getData().toString());
				files.add(f);
			}
			travelBoardTree(t);
		}
	}

	private void travelParentItems(TreeItem item, boolean checked) {
		// TODO Auto-generated method stub
		TreeItem parentItem = item.getParentItem();
		if (parentItem != null && checked) {
			parentItem.setGrayed(true);
			parentItem.setChecked(true);
			TreeItem[] brotherItems = parentItem.getItems();
			boolean allChecked = true;
			boolean oneGrayed = false;
			for (TreeItem t : brotherItems) {
				if (!t.getChecked()) {
					allChecked = false;
				}
				if (t.getGrayed()) {
					oneGrayed = true;
				}
			}
			if (allChecked && !oneGrayed) {
				parentItem.setGrayed(false);
			}
			travelParentItems(parentItem, checked);
		} else if (parentItem != null && !checked) {
			if (parentItem != null) {
				parentItem.setGrayed(true);
				TreeItem[] brotherItems = parentItem.getItems();
				boolean allCanceled = true;
				for (TreeItem t : brotherItems) {
					if (t.getChecked()) {
						allCanceled = false;
					}
				}
				if (allCanceled) {
					parentItem.setGrayed(false);
					parentItem.setChecked(false);
				}
			}
			travelParentItems(parentItem, checked);
		}
	}

	private void travelChildItems(TreeItem item, boolean checked) {
		// TODO Auto-generated method stub
		if (checked) {
			item.setGrayed(false);
		}
		TreeItem[] childItems = item.getItems();
		for (TreeItem t : childItems) {
			t.setChecked(checked);
			travelChildItems(t, checked);
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setSize(500, 500);
		GridLayout layout = DjyosUI.DjyosGridLayout(1, 5, 5);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite treeComposite = new Composite(composite, SWT.NONE);
		boardFileTree = new Tree(treeComposite, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.CHECK);
		boardFileTree.setLayoutData(new GridData(GridData.FILL_BOTH));
		root = new TreeItem(boardFileTree, 0);
		root.setText(board.getBoardName());
		root.setImage(DPluginImages.OBJ_BOARD_VIEW.createImage());
		root.setData(board.getBoardFolderPath());// 保存当前节点数据
		String boardPath = board.getBoardFolderPath();
		File boardFile = new File(boardPath);

		ExpadFileTree(root, boardFile);
		boardFileTree.setSize(480, 350);
		boardFileTree.addListener(SWT.Selection, selectionListener);

		return super.createDialogArea(parent);
	}

	private void ExpadFileTree(TreeItem root, File file) {
		// TODO Auto-generated method stub
		File[] files = file.listFiles();
		for (File f : files) {
			if (!f.getName().endsWith(".xml")) {
				TreeItem t = new TreeItem(root, SWT.NONE);
				t.setText(f.getName());
				t.setData(f.getPath());
				if (f.isDirectory()) {
					t.setImage(DPluginImages.TREE_FLODER_VIEW.createImage());
					ExpadFileTree(t, f);
				} else {
					t.setImage(DPluginImages.OBJ_FILE_VIEW.createImage());
				}
			}
		}
	}

}
