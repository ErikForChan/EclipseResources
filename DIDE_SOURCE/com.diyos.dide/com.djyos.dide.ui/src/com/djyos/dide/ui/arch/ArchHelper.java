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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.djyos.dide.ui.DPluginImages;
import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.helper.LinkHelper;
import com.djyos.dide.ui.objects.Core;
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

	public static void handArchLinks(Core core, File archSourceFile, List<File> archXmlFiles, IProject project,
			ICConfigurationDescription[] conds) {
		// 添加family的链接
		String type = core.getArch().getSerie();// 架构类型
		String family = core.getArch().getMcpu();// 架构家族
		List<File> excludeArchFiles = new ArrayList<File>();
		List<File> stepArchFiles = new ArrayList<File>();
		File[] typeFiles = archSourceFile.listFiles();
		for (File f : typeFiles) {
			if (!f.getName().equals(type)) {
				excludeArchFiles.add(f);
			}
		}
		// 当前的Arch
		File curArchFile = null;
		for (File f : archXmlFiles) {
			if (f.getParentFile().getName().equals(family)) {
				curArchFile = f.getParentFile();
			} else {
				excludeArchFiles.add(f.getParentFile());
				stepArchFiles.add(f.getParentFile());
			}
		}
		getExcludeArchFiles(excludeArchFiles, stepArchFiles);
		// 排除所有未被选中的arch
		for (File f : excludeArchFiles) {
			DideHelper.setFolderExclude(f, project, conds, true);
		}

		// 释放当前的arch
		if (curArchFile != null) {
			IFolder archtectureFolder = project.getFolder(
					"src/libos" + curArchFile.getPath().replace("\\", "/").replace(PathTool.getDjyosSrcPath(), ""));
			List<IFolder> archtectureFolders = new ArrayList<IFolder>();
			getArchFolders(archtectureFolder, archtectureFolders);
			LinkHelper.setFoldersInclude(archtectureFolders, conds);
		}
	}
	
	private static void getExcludeArchFiles(List<File> excludeArchFiles, List<File> stepArchFiles) {
		// TODO Auto-generated method stub
		List<File> tempArchFiles = new ArrayList<File>();
		// System.out.println("stepArchFile: "+stepArchFiles.get(0).getName());
		for (File f : stepArchFiles) {
			File parentFile = f.getParentFile();
			if (!tempArchFiles.contains(parentFile) && !parentFile.getName().equals("arch")) {
				tempArchFiles.add(parentFile);
			}
			if (!excludeArchFiles.contains(parentFile) && !parentFile.getName().equals("arch")) {
				excludeArchFiles.add(parentFile);
			}
		}
		if (tempArchFiles.size() > 0) {
			getExcludeArchFiles(excludeArchFiles, tempArchFiles);
		}
	}

	public static void getArchFolders(IFolder ifolder, List<IFolder> folders) {
		folders.add(ifolder);
		IFolder parentFolder = (IFolder) ifolder.getParent();
		if (!parentFolder.getName().equals("arch")) {
			getArchFolders(parentFolder, folders);
		}
	}
	
}
