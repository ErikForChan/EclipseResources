package org.eclipse.cdt.ui.wizards.cpu;

import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import org.eclipse.cdt.ui.CUIPlugin;

public class CpuCommonWizard extends BasicNewResourceWizard{

	protected CpuMainWiazrdPage fMianPage = new CpuMainWiazrdPage("New Cpu");
	private String wz_title;
	private String wz_desc;
	public CpuCommonWizard(String title, String desc) {
		// TODO Auto-generated constructor stub
		super();
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
		setNeedsProgressMonitor(true);
		setForcePreviousAndNextButtons(true);
		setWindowTitle(title);
		wz_title = title;
		wz_desc = desc;
	}

	@Override
	public boolean isCancelAvailable() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean needsPreviousAndNextButtons() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void addPages() {
		// TODO Auto-generated method stub
		fMianPage.setTitle(wz_title);
		fMianPage.setDescription(wz_desc);
		addPage(fMianPage);
		super.addPages();
	}

	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return true;
	}

}
