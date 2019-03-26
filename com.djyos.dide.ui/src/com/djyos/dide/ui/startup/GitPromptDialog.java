package com.djyos.dide.ui.startup;

import org.eclipse.cdt.ui.newui.AbstractPropertyDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class GitPromptDialog extends AbstractPropertyDialog {

	private Label promptLabel;
	private Button noPromptBtn;
	private Button b_ok;
	private Button b_ko;
	private boolean toPaomptAgain = false;

	public GitPromptDialog(Shell _parent, String title) {
		super(_parent, title);
		// TODO Auto-generated constructor stub
	}

	public boolean notPromptAgain() {
		return toPaomptAgain;
	}

	@Override
	public void buttonPressed(SelectionEvent e) {
		// TODO Auto-generated method stub
		toPaomptAgain = noPromptBtn.getSelection();
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
		GridLayout layout = new GridLayout(3, true);
		layout.marginTop = 20;
		layout.verticalSpacing = 15;
		c.setLayout(layout);

		GridData gd;
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		gd.widthHint = 300;
		promptLabel = new Label(c, SWT.NONE);
		promptLabel.setText("DIDE中不存在Djyos源码，是否自动下载源码？");
		promptLabel.setLayoutData(gd);

		noPromptBtn = new Button(c, SWT.CHECK);
		noPromptBtn.setText(" 不再提醒");
		noPromptBtn.setLayoutData(gd);

		new Label(c, 0).setLayoutData(new GridData());
		b_ok = setupButton(c, IDialogConstants.OK_LABEL);
		b_ko = setupButton(c, IDialogConstants.CANCEL_LABEL);

		c.getShell().setDefaultButton(b_ok);
		c.pack();
		return c;
	}

}
