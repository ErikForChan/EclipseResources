package org.eclipse.cdt.ui.wizards;

import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.cdt.ui.wizards.parsexml.Cpu;

import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringDialogField;

public class AddCpuDialog extends StatusDialog{

	public Cpu cpu;
	
	private StringDialogField[] fDialogFields = new StringDialogField[8];
	private String[] cpuDetailsLabels = {
			"device","core","architecture","fpuType",
			"flashAddr","flashSize","ramAddr","ramSize"
	};
	
	public AddCpuDialog(Shell parent) {
		super(parent);
		setTitle("AddMCU");
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX );		
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 20;
		layout.numColumns = 4;
		layout.marginLeft=5;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		for(int i=0;i<fDialogFields.length;i++) {
			fDialogFields[i] = new StringDialogField();
			fDialogFields[i].setLabelText(cpuDetailsLabels[i]+": ");
			fDialogFields[i].getLabelControl(composite)
					.setLayoutData(new GridData(GridData.BEGINNING));
			fDialogFields[i].getTextControl(composite)
					.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		return super.createDialogArea(parent);
	}
	
	public Cpu getCpu() {
		Cpu cpu =new Cpu();
		cpu.device = fDialogFields[0].getText();
		cpu.core = fDialogFields[1].getText();
		cpu.architecture = fDialogFields[2].getText();
		cpu.fpuType = fDialogFields[3].getText();
		cpu.flashStart = fDialogFields[4].getText();
		cpu.flashSize = fDialogFields[5].getText();
		cpu.ramStart = fDialogFields[6].getText();
		cpu.ramSize = fDialogFields[7].getText();
		return cpu;
	}

	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		cpu = getCpu();
		super.okPressed();
	}
	
	

}
