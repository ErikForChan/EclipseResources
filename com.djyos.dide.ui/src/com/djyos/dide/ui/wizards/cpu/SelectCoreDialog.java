package com.djyos.dide.ui.wizards.cpu;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.djyos.dide.ui.DPluginImages;
import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.objects.Core;
import com.djyos.dide.ui.objects.CoreMemory;

public class SelectCoreDialog extends StatusDialog {
	private String detailsDesc = null;
	private Text detailsField;
	private Tree coreTree;
	private List<Core> cores = new ArrayList<Core>();
	private Core coreSelected;

	public Core getSelectCore() {
		return coreSelected;
	}

	public SelectCoreDialog(Shell parent, List<Core> coresList) {
		super(parent);
		cores = coresList;
		setTitle("选择内核");
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		// TODO Auto-generated constructor stub
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
		Composite coreCpt = new Composite(composite, SWT.NONE);
		GridLayout coreLayout = new GridLayout();
		coreLayout.numColumns = 2;
		coreCpt.setLayout(coreLayout);
		coreCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createTreeForCores(coreCpt);
		detailsField = new Text(coreCpt, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		detailsField.setLayoutData(new GridData(GridData.FILL_BOTH));
		detailsField.setEditable(false);
		return super.createDialogArea(parent);
	}

	protected void okPressed() {
		// TODO Auto-generated method stub
		TreeItem[] items = coreTree.getSelection();
		if (items.length > 0) {
			String coreName = items[0].getText();
			for (int i = 0; i < cores.size(); i++) {
				String cName = DideHelper.getCoreName(cores.get(i),i);
				if (coreName.contains(cName)) {
					coreSelected = cores.get(i);
					break;
				}
			}
		}
		super.okPressed();
	}

	private void createTreeForCores(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		coreTree = DideHelper.buildTree(composite, 170, 200, "内核名称", SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		for (int i = 0; i < cores.size(); i++) {
			TreeItem t = new TreeItem(coreTree, SWT.NONE);
			String coreName = DideHelper.getCoreName(cores.get(i),i);
			t.setText(coreName);
			t.setImage(DPluginImages.OBJ_CORE_VIEW.createImage());
		}

		coreTree.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

				TreeItem[] items = coreTree.getSelection();
				if (items.length > 0) {
					String coreName = items[0].getText();
					detailsDesc = "";
					for (int j = 0; j < cores.size(); j++) {
						Core core = cores.get(j);
						String cName = DideHelper.getCoreName(core,j);
						if (coreName.contains(cName)) {
							String type = core.getArch().getSerie();
							String arch = core.getArch().getMarch();
							String family = core.getArch().getMcpu();
							String fpuType = core.getFpuType();
							String resetAddr = core.getResetAddr();
							String memoryString = "";
							for (int k = 0; k < core.getMemorys().size(); k++) {
								CoreMemory memory = core.getMemorys().get(k);
								if (memory.getType().equals("ROM")) {
									memoryString += "\n\tFlashStart: " + memory.getStartAddr() + "\n\tFlashSize: "
											+ memory.getSize();
								} else if (memory.getType().equals("RAM")) {
									memoryString += "\n\tRamStart: " + memory.getStartAddr() + "\n\tRamSize: "
											+ memory.getSize();
								}
							}
							if(type != null) {
								detailsDesc += ("系列: " + type);
							}
							if(arch!=null) {
								detailsDesc += ("\n架构: " + arch);
							}
							if(family!=null) {
								detailsDesc += ("\n家族: " + family);
							}
							if(fpuType!=null) {
								detailsDesc += ("\n浮点: " + fpuType);
							}
							detailsDesc += "\n复位地址: " + resetAddr + "\n存储:  " + memoryString + "\n\n";
							break;
						}

					}
					detailsField.setText(detailsDesc);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				okPressed();
			}
		});
	}
}
