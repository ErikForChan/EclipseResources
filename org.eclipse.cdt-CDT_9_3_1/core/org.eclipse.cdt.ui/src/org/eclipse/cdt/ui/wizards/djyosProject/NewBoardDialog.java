package org.eclipse.cdt.ui.wizards.djyosProject;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.core.CCorePreferenceConstants;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.preferences.PreferencesMessages;
import org.eclipse.cdt.internal.ui.preferences.TodoTaskConfigurationBlock.TodoTask;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringDialogField;

public class NewBoardDialog extends StatusDialog{
	
	private class CompilerImportBoardAdapter implements IDialogFieldListener {
		@Override
		public void dialogFieldChanged(DialogField field) {
			doValidation();
		}
	}
	
	private void doValidation() {
		StatusInfo status = new StatusInfo();
		String newText = boardNameField.getText();
		if (newText.isEmpty()) {
			status.setError(PreferencesMessages.TodoTaskInputDialog_error_enterName); 
		} else {
			if (newText.indexOf(',') != -1) {
				status.setError(PreferencesMessages.TodoTaskInputDialog_error_comma); 
			}
		}
		updateStatus(status);
	}
	
	private StringDialogField boardNameField;
	private StringDialogField[] fDialogFields = new StringDialogField[6];
	private String[] BoardDetailsLabels = {
			"MCU","High speed external clock",
			"Architecture","Low speed external clock",
			"family","Iboot size"
	};
	StringDialogField fMCUField,fHigh_ClkField,fLow_XField,fArchField,fFamilyField,fSizeField;
	Composite detailsCpt,baordCpt,MCUCpt;
	Text boardDetailsDesc,MCUNameField;
	Button importMCUBtn;
	
	public NewBoardDialog(Shell parent) {
		super(parent);
		setTitle("NewBoardDialog");
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX );		
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// TODO Auto-generated method stub
		CompilerImportBoardAdapter adapter = new CompilerImportBoardAdapter();
		Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 25;
		layout.numColumns = 2;
		layout.marginLeft=10;
		baordCpt = new Composite(composite, SWT.NONE);
		baordCpt.setLayout(layout);
		baordCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
		boardNameField = new StringDialogField();
		boardNameField.setLabelText("Board name:");
		boardNameField.getLabelControl(baordCpt)
				.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		boardNameField.getTextControl(baordCpt)
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		boardNameField.setDialogFieldListener(adapter);
		
		MCUCpt = new Composite(composite, SWT.NONE);
		layout.marginHeight = 15;
		layout.numColumns = 3;
		layout.marginLeft=10;
		MCUCpt.setLayout(layout);
		MCUCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label MCULabel = new Label(MCUCpt, SWT.NONE);
		MCULabel.setLayoutData(new GridData(GridData.BEGINNING));
		MCULabel.setText(BoardDetailsLabels[0]+":");
		MCUNameField = new Text(MCUCpt, SWT.BORDER);
		MCUNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		importMCUBtn = new Button(MCUCpt, SWT.PUSH);
		importMCUBtn.setText("Choose...");
		importMCUBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleChooseButtonPressed();
			}

		});
				
		detailsCpt = new Composite(composite, SWT.NONE);
		layout.marginHeight = 5;
		layout.numColumns = 4;
		layout.marginLeft=10;
		layout.verticalSpacing = 20;
		detailsCpt.setLayout(layout);
		detailsCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		for(int i=1;i<fDialogFields.length;i++) {
			fDialogFields[i] = new StringDialogField();
			fDialogFields[i].setLabelText(BoardDetailsLabels[i]+":");
			fDialogFields[i].getLabelControl(detailsCpt)
					.setLayoutData(new GridData(GridData.BEGINNING));
			fDialogFields[i].getTextControl(detailsCpt)
					.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		
		return super.createDialogArea(parent);
	}
	
	protected void handleChooseButtonPressed() {
		
		ChooseMCUDialog dialog = new ChooseMCUDialog(getShell());
		if (dialog.open() == Window.OK) {
			System.out.println("ChooseMCU");
//			BoardDetails details = dialog.getResult();
//			fBoardNameField.setText(details.boardName);
		}
		
//		String dirName = "E:\\djysdk\\djysrc\\bsp\\boarddrv";
//		FileDialog dialog =  new FileDialog(MCUNameField.getShell(), SWT.OPEN | SWT.MULTI);
//		dialog.setText("Choose Board");
//		dialog.setFilterPath(dirName);
//		String selectedDirectory = dialog.open();
//		String boardName = selectedDirectory.substring(selectedDirectory.lastIndexOf("\\")+1, selectedDirectory.length());
//		boardNameField.setText(boardName);

//		DirectoryDialog dialog = new DirectoryDialog(MCUNameField
//				.getShell(), SWT.SHEET);
//		dialog
//				.setMessage("Choose Board");
//
//		String dirName = "E:\\djysdk\\djysrc\\bsp\\boarddrv";
//
//		if (dirName.length() == 0) {
//			dialog.setFilterPath(IDEWorkbenchPlugin.getPluginWorkspace()
//					.getRoot().getLocation().toOSString());
//		} else {
//			File path = new File(dirName);
//			if (path.exists()) {
//				dialog.setFilterPath(new Path(dirName).toOSString());
//			}
//		}

	//	dialog.open();
//		String selectedDirectory = dialog.open();
//		if (selectedDirectory != null) {
//			String boardName = selectedDirectory
//			previouslyBrowsedDirectory = selectedDirectory;
//			directoryPathField.setText(previouslyBrowsedDirectory);
//			updateProjectsList(selectedDirectory);
		}
	
	@Override
	protected void configureShell(Shell shell) {
		// TODO Auto-generated method stub
		super.configureShell(shell);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, ICHelpContextIds.TODO_TASK_INPUT_DIALOG);
	}

}
