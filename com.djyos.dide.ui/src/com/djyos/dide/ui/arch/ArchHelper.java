/*******************************************************************************
 * Copyright (c) 2017 Djyos Team.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.djyos.com
 *
 * Contributors:
 *     Djyos Team - Jiaming Chen
 *******************************************************************************/
package com.djyos.dide.ui.arch;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.djyos.dide.ui.DPluginImages;
import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.wizards.djyosProject.tools.PathTool;

public class ArchHelper {

	private static String didePath = PathTool.getDIDEPath();

	public static void initArchTree(Tree tree) {
		// TODO Auto-generated method stub
		TreeItem root = new TreeItem(tree, 0);
		root.setText("Arch列表");
		root.setImage(DPluginImages.CFG_CPMT_OBJ.createImage());
		root.setData("Arch");// 保存当前节点数据
		root.setData("type", "arch");
		File file = new File(didePath + "djysrc/bsp/arch");
		File[] typeFiles = file.listFiles();
		for (int i = 0; i < typeFiles.length; i++) {
			if (DideHelper.travelContainsXml(typeFiles[i])) {
				TreeItem type = new TreeItem(root, SWT.NONE);
				type.setText(typeFiles[i].getName());
				type.setImage(DPluginImages.TREE_FLODER_VIEW.createImage());
				type.setData("type", "arch");
				type.setData(typeFiles[i]);// 保存当前节点数据
				type.setGrayed(true);
				type.setChecked(true);
				new TreeItem(type, 0);
				ExpandTree(type);
			}
		}
		root.setExpanded(true);
	}

	private static void ExpandTree(TreeItem root) {
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
						boolean configed = isFamily(files[i]);
						item = new TreeItem(root, SWT.NONE);
						item.setText(files[i].getName());
						item.setData(files[i]);
						if (configed) {// SWT.ERROR_CANNOT_SET_ENABLED
							item.setImage(DPluginImages.OBJ_ARCH_VIEW.createImage());
							item.setExpanded(false);
						} else {
							item.setImage(DPluginImages.TREE_FLODER_VIEW.createImage());
							item.setGrayed(true);
							item.setChecked(true);
							new TreeItem(item, 0);
							ExpandTree(item);
						}
						item.setData("type", "arch");
					}
				}

			}
		}
	}

	public static File getArchFile(File file) {
		// TODO Auto-generated method stub
		File[] files = file.listFiles();
		for (File f : files) {
			if (f.getName().equals("arch.xml")) {
				return f;
			}
		}
		return null;
	}

	public static boolean isFamily(File file) {
		// TODO Auto-generated method stub
		File[] cfiles = file.listFiles();
		for (int j = 0; j < cfiles.length; j++) {
			if (cfiles[j].getName().equals("arch.xml")) {
				return true;
			}
		}
		return false;
	}

}
