package org.eclipse.cdt.ui.wizards.board;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.CreatTBoardXml;
import org.eclipse.cdt.ui.wizards.board.onboardcpu.Board;

public class BoardCommonWizard extends BasicNewResourceWizard{

	protected  BoardMainWizard fMainPage = new BoardMainWizard("New Board");
	private String wz_title;
	private String wz_desc;
	private String eclipsePath = getEclipsePath();
	
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
	public void addPages() {
		// TODO Auto-generated method stub
		fMainPage.setTitle(wz_title);
		fMainPage.setDescription(wz_desc);
		addPage(fMainPage);
		super.addPages();
	}

	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		Board board = fMainPage.getBoard();
		String dirPath = eclipsePath+"djysrc\\bsp\\boarddrv\\"+board.getBoardName();
		try {
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {

					// 数字是totalWork
					monitor.beginTask("创建板件……", 100);

					/*
					 * 处理事务，worked方法表示工作了多少的进度
					 */
					File boardDir = new File(dirPath);
					boardDir.mkdirs();
					String xmlPath = dirPath+"\\Board_"+board.getBoardName()+".xml";
					File file = new File(xmlPath);
					CreatTBoardXml ctbx = new CreatTBoardXml();
					if(!file.exists()) {
						try {
							file.createNewFile();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					ctbx.creatBoardXml(board, file);
					monitor.worked(10);

					monitor.done();
				}
			};
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(
					PlatformUI.getWorkbench().getDisplay().getActiveShell());
			dialog.setCancelable(false);
			dialog.run(true, true, runnable);
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
		return true;
	}

}
