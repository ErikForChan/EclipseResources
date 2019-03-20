package com.djyos.dide.ui.wizards.cpu;

import org.eclipse.cdt.ui.newui.AbstractPropertyDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class NewCpuDrvDialog extends AbstractPropertyDialog {

	public NewCpuDrvDialog(Shell _parent, String title) {
		super(_parent, title);
		// TODO Auto-generated constructor stub
	}

	private Text cpudrvField;
	private String cpudrvName;
	private Button b_ok;
	private Button b_ko;

	public String getCpudrvName() {
		return cpudrvName;
	}

	@Override
	public void buttonPressed(SelectionEvent e) {
		// TODO Auto-generated method stub
		if (e.widget.equals(b_ok)) {
			result = true;
			shell.dispose();
		} else if (e.widget.equals(b_ko)) {
			shell.dispose();
		}
	}

	@Override
	protected Control createDialogArea(Composite c) {
		// TODO Auto-generated method stub
		GridLayout layout = new GridLayout(4, true);
		layout.marginTop = 20;
		layout.verticalSpacing = 15;
		c.setLayout(layout);
		Label chipLabel = new Label(c, SWT.NONE);
		chipLabel.setText("cpuÕ‚…Ë√˚≥∆: ");

		GridData gd;
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		gd.widthHint = 300;
		cpudrvField = new Text(c, SWT.BORDER);
		cpudrvField.setLayoutData(gd);
		cpudrvField.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				// TODO Auto-generated method stub
				cpudrvName = cpudrvField.getText();
			}
		});

		new Label(c, 0).setLayoutData(new GridData());
		new Label(c, 0).setLayoutData(new GridData());
		b_ok = setupButton(c, IDialogConstants.OK_LABEL);
		b_ko = setupButton(c, IDialogConstants.CANCEL_LABEL);

		c.getShell().setDefaultButton(b_ok);
		c.pack();
		return c;
	}

}
