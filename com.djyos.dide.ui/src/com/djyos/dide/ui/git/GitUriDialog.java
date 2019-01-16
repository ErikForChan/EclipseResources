package com.djyos.dide.ui.git;

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

public class GitUriDialog extends AbstractPropertyDialog {

	private Text uriField;
	private Label uriLabel;
	private String gitPath;
	private Button b_ok;
	private Button b_ko;

	public GitUriDialog(Shell parent, String remotePath, String title) {
		super(parent, title);
		// TODO Auto-generated constructor stub
		gitPath = remotePath;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// TODO Auto-generated method stub
		GridLayout layout = new GridLayout(4, true);
		layout.marginTop = 20;
		layout.verticalSpacing = 15;
		parent.setLayout(layout);

		uriLabel = new Label(parent, SWT.NONE);
		uriLabel.setText("Gitµÿ÷∑: ");
		uriLabel.setLayoutData(new GridData(GridData.BEGINNING));

		GridData gd;
		uriField = new Text(parent, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		gd.widthHint = 300;
		uriField.setLayoutData(gd);
		uriField.setText(gitPath);
		uriField.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				// TODO Auto-generated method stub
				gitPath = uriField.getText().trim();
			}
		});

		new Label(parent, 0).setLayoutData(new GridData());
		new Label(parent, 0).setLayoutData(new GridData());
		b_ok = setupButton(parent, IDialogConstants.OK_LABEL);
		b_ko = setupButton(parent, IDialogConstants.CANCEL_LABEL);

		parent.getShell().setDefaultButton(b_ok);
		parent.pack();
		return parent;
	}

	public String getGitUri() {
		return gitPath;
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

}
