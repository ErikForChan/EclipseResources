package com.djyos.dide.ui.git;

import org.eclipse.cdt.ui.newui.AbstractPropertyDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class GitUpdateInfoDialog extends AbstractPropertyDialog {

	private Label infoLabel;
	private Button selectButton;
	private Button b_ok;
	private Button b_ko;
	private boolean noticeMe = true;

	public GitUpdateInfoDialog(Shell _parent, String title) {
		super(_parent, title);
		// TODO Auto-generated constructor stub
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
		layout.marginTop = 15;
		layout.verticalSpacing = 25;
		c.setLayout(layout);
		c.setLayoutData(new GridData(GridData.BEGINNING));
		GridData gd;
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 4;
		gd.widthHint = 300;
		infoLabel = new Label(c, SWT.NONE);
		infoLabel.setText("Git中Djyos源码有更新,是否更新代码？");
		infoLabel.setLayoutData(gd);

		selectButton = new Button(c, SWT.CHECK);
		selectButton.setText("不再提醒");
		selectButton.setLayoutData(gd);
		selectButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				noticeMe = !noticeMe;
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

	public boolean allowNoticeMe() {
		return noticeMe;
	}
}
