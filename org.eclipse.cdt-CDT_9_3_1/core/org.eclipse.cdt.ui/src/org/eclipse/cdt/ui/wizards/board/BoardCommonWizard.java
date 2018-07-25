package org.eclipse.cdt.ui.wizards.board;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.OnBoardCpu;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.OnBoardMemory;

public class BoardCommonWizard extends BasicNewResourceWizard{

	protected  BoardMainWizard1 fMainPage = new BoardMainWizard1("New Board");
	private String wz_title;
	private String wz_desc;
	private String eclipsePath = getEclipsePath();
	private IWorkbenchWindow window = PlatformUI.getWorkbench()
			.getActiveWorkbenchWindow();
	
	/*
	 * 获取当前Eclipse的路径
	 */
	public String getEclipsePath() {
		String fullPath = Platform.getInstallLocation().getURL().toString();
		String eclipsePath = fullPath.substring(6,(fullPath.substring(0,fullPath.length()-1)).lastIndexOf("/")+1);
		return eclipsePath;
	}
	
	public BoardCommonWizard(String title, String desc) {
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
	public boolean needsPreviousAndNextButtons() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addPages() {
		// TODO Auto-generated method stub
		fMainPage.setTitle(wz_title);
		fMainPage.setDescription(wz_desc);
		addPage(fMainPage);
		super.addPages();
	}
	
	@Override
	public boolean isCancelAvailable() {
		// TODO Auto-generated method stub
		return false;
	}

	
	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return true;
	}

}
