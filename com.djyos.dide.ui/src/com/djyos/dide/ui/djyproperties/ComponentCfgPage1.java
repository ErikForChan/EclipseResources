package com.djyos.dide.ui.djyproperties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

import com.djyos.dide.ui.messages.IComponentConstants;

public class ComponentCfgPage1 extends PropertyPage implements IComponentConstants{

	@Override
	protected Control createContents(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(parent, SWT.NONE);
		initializeDialogUnits(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);
		composite.setLayout(new GridLayout(1, true));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		initContent();
		return composite;
	}

	private void initContent() {
		// TODO Auto-generated method stub
		
	}

}
