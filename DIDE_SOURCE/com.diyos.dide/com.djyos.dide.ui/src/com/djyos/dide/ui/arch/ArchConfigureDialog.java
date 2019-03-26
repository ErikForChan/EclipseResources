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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.djyos.dide.ui.DPluginImages;

public class ArchConfigureDialog extends StatusDialog {

	private ScrolledComposite scrolledComposite;
	private Text groupNameField;
	private Group contentGroup;
	private Tree groupTree;
	private List<String> attributes = new ArrayList<String>();
	private Composite configContent;
	private String archTag = null;

	public ArchConfigureDialog(Shell parent, List<String> configs, String tag) {
		super(parent);
		// TODO Auto-generated constructor stub
		attributes = configs;
		archTag = tag;
		if (tag.equals("group")) {
			setTitle("新建分组");
		} else if (tag.equals("arch")) {
			setTitle("新建Arch");
		} else if (tag.startsWith("revise")) {
			if (tag.endsWith("group")) {
				setTitle("修改子目录配置");
			} else if (tag.endsWith("arch")) {
				setTitle("修改Arch配置");
			}
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		createContent(composite);
		return super.createDialogArea(parent);
	}

	private void createContent(Composite composite) {
		// TODO Auto-generated method stub
		Composite groupNameCpt = new Composite(composite, SWT.NONE);
		GridLayout groupNameLayout = new GridLayout();
		groupNameLayout.numColumns = 2;
		groupNameLayout.marginHeight = 20;
		groupNameCpt.setLayout(groupNameLayout);
		groupNameCpt.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label nameLabel = new Label(groupNameCpt, SWT.NONE);
		groupNameField = new Text(groupNameCpt, SWT.BORDER);
		groupNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		if (archTag.equals("group")) {
			nameLabel.setText("子目录名称: ");
		} else if (archTag.equals("arch")) {
			nameLabel.setText("Arch名称: ");
		} else if (archTag.startsWith("revise")) {
			if (archTag.endsWith("arch")) {
				nameLabel.setText("Arch名称: ");
			} else if (archTag.endsWith("group")) {
				nameLabel.setText("子目录名称: ");
			}
			// groupNameField.setText(tempName);
		}

		Composite groupCpt = new Composite(composite, SWT.NULL);
		GridLayout layoutAttributes = new GridLayout();
		layoutAttributes.numColumns = 2;
		groupCpt.setLayout(layoutAttributes);
		groupCpt.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite cpuGroupListCpt = new Composite(groupCpt, SWT.NULL);
		groupTree = new Tree(cpuGroupListCpt, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		groupTree.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		groupTree.setHeaderVisible(true);
		final TreeColumn columnGroupList = new TreeColumn(groupTree, SWT.NONE);
		columnGroupList.setText("Arch配置项");
		columnGroupList.setWidth(140);
		columnGroupList.setResizable(false);
		columnGroupList.setToolTipText("Cpu Attributes");
		columnGroupList.setImage(DPluginImages.CFG_CPMT_OBJ.createImage());
		groupTree.setSize(150, 250);
		List<String> cons = new ArrayList<String>();
		cons.add("工具链配置");
		cons.add("架构配置");
		cons.add("家族配置");
		cons.add("浮点配置");
		// cons.add("固件库");
		// 之前是attributes
		for (int i = 0; i < cons.size(); i++) {
			TreeItem t = new TreeItem(groupTree, SWT.NONE);
			t.setText(cons.get(i));
			if (!attributes.contains(cons.get(i))) {
				t.setImage(DPluginImages.CFG_DONE_VIEW.createImage());
			}
		}

		contentGroup = ControlFactory.createGroup(groupCpt, "分组配置", 1);
		contentGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		contentGroup.setLayout(new GridLayout(1, true));

		scrolledComposite = new ScrolledComposite(contentGroup, SWT.V_SCROLL | SWT.H_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		configContent = new Composite(scrolledComposite, SWT.NONE);
		configContent.setLayout(new GridLayout());
		configContent.setLayoutData(new GridData(GridData.FILL_BOTH));

		Point point = configContent.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		scrolledComposite.setContent(configContent);
		scrolledComposite.setMinHeight(point.y);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setMinWidth(300);

		groupTree.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TreeItem[] items = groupTree.getSelection();
				if (items.length > 0) {
					String selectConfigName = items[0].getText();
					contentGroup.setText(selectConfigName);
					scrolledComposite.dispose();

					scrolledComposite = new ScrolledComposite(contentGroup, SWT.V_SCROLL | SWT.H_SCROLL);
					scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

					configContent = new Composite(scrolledComposite, SWT.NONE);
					GridLayout layout = new GridLayout();
					layout.numColumns = 2;
					configContent.setLayout(layout);
					configContent.setLayoutData(new GridData(GridData.FILL_BOTH));

					switch (selectConfigName) {
					case "工具链配置":
						creatToolchainContent(configContent);
						break;
					case "架构配置":
						creatMarchContent(configContent);
						break;
					case "家族配置":
						creatMcpuContent(configContent);
						break;
					case "浮点配置":
						creatFloatContent(configContent);
						break;
					}

					Point point = configContent.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
					scrolledComposite.setContent(configContent);
					scrolledComposite.setMinHeight(point.y);
					scrolledComposite.setExpandHorizontal(true);
					scrolledComposite.setExpandVertical(true);
					scrolledComposite.setMinWidth(300);
					contentGroup.layout();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

	}

	protected void creatFloatContent(Composite coreConfigCpt) {
		// TODO Auto-generated method stub
		Label tcLabel = new Label(coreConfigCpt, SWT.NONE);
		tcLabel.setText("浮点： ");
		Text tcText = new Text(coreConfigCpt, SWT.BORDER);
		tcText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	protected void creatMcpuContent(Composite coreConfigCpt) {
		// TODO Auto-generated method stub
		Label tcLabel = new Label(coreConfigCpt, SWT.NONE);
		tcLabel.setText("家族： ");
		Text tcText = new Text(coreConfigCpt, SWT.BORDER);
		tcText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	protected void creatMarchContent(Composite coreConfigCpt) {
		// TODO Auto-generated method stub
		Label tcLabel = new Label(coreConfigCpt, SWT.NONE);
		tcLabel.setText("架构： ");
		Text tcText = new Text(coreConfigCpt, SWT.BORDER);
		tcText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	protected void creatToolchainContent(Composite coreConfigCpt) {
		// TODO Auto-generated method stub
		Label tcLabel = new Label(coreConfigCpt, SWT.NONE);
		tcLabel.setText("工具链： ");
		Text tcText = new Text(coreConfigCpt, SWT.BORDER);
		tcText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	@Override
	protected Point getInitialSize() {
		// TODO Auto-generated method stub
		return new Point(570, 560);
	}

	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		super.okPressed();
	}

}
