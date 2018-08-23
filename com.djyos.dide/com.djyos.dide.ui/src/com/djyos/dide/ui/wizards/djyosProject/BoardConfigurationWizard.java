package com.djyos.dide.ui.wizards.djyosProject;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

import org.eclipse.cdt.utils.ui.controls.ControlFactory;

import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringDialogField;

public class BoardConfigurationWizard extends WizardPage{
	public static final String PAGE_ID = "org.eclipse.cdt.managedbuilder.ui.wizard.NewModelProjectWizardPage"; //$NON-NLS-1$

	private static final String EXTENSION_POINT_ID = "org.eclipse.cdt.ui.CDTWizard"; //$NON-NLS-1$
	private static final String ELEMENT_NAME = "wizard"; //$NON-NLS-1$
	private static final String CLASS_NAME = "class"; //$NON-NLS-1$
	public static final String DESC = "EntryDescriptor"; //$NON-NLS-1$
	/**
	 * Creates a new project creation wizard page.
	 *
	 * @param pageName the name of this page
	 */
	public BoardConfigurationWizard(String pageName) {
		super(pageName);
		setPageComplete(false);
	}

	@SuppressWarnings("restriction")
	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		initializeDialogUnits(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);
		GridLayout gd1 = new GridLayout(2,true);
		gd1.marginHeight=25;
		composite.setLayout(gd1);
		
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		
		Composite left = new Composite(composite, SWT.NULL);
		left.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		left.setLayout(new GridLayout(2, false));
		StringDialogField fTickField,fMain_ClkField,fLcd_XField;
		
		fTickField = new StringDialogField();
		fTickField.setLabelText("Tick/us: ");
		fTickField.getLabelControl(left)
				.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		fTickField.getTextControl(left).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		fMain_ClkField = new StringDialogField();
		fMain_ClkField.setLabelText("Main Clk/hz: ");
		fMain_ClkField.getLabelControl(left)
				.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		fMain_ClkField.getTextControl(left).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		fLcd_XField = new StringDialogField();
		fLcd_XField.setLabelText("Lcd X Size: ");
		fLcd_XField.getLabelControl(left)
				.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		fLcd_XField.getTextControl(left).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		Composite right = new Composite(composite, SWT.NULL);
		right.setLayout(new GridLayout(2, false));
		right.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		StringDialogField fHSEField,fLSEField,fLcd_YField;
		
		fHSEField = new StringDialogField();
		fHSEField.setLabelText("HSE/hz: ");
		fHSEField.getLabelControl(right)
				.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		fHSEField.getTextControl(right).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		fLSEField = new StringDialogField();
		fLSEField.setLabelText("LSE/hz: ");
		fLSEField.getLabelControl(right)
				.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		fLSEField.getTextControl(right).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		fLcd_YField = new StringDialogField();
		fLcd_YField.setLabelText("Lcd Y Size: ");
		fLcd_YField.getLabelControl(right)
				.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		fLcd_YField.getTextControl(right).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		// Show description on opening
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
		Dialog.applyDialogFont(composite);
		
		setPageComplete(true);

	}

	private void createDynamicGroup(Composite parent) {		
//		Composite c = new Composite(parent, SWT.CENTER);
//		c.setLayoutData(new GridData(GridData.FILL_BOTH));
//		c.setLayout(new GridLayout(2, true));
//		creatTemplateUI(parent);
//		Group group= ControlFactory.createGroup(parent, "Template Details ", 1);
//		GridData gd;
//		gd= (GridData) group.getLayoutData();
//		gd.grabExcessHorizontalSpace= true;
//		gd.horizontalAlignment= GridData.FILL;
//		group.setLayout(new GridLayout(2, false));
//		creatTemplateDetailsUI(group);
			
	}
}