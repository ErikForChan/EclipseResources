package com.djyos.dide.ui.git;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.djyos.dide.ui.startup.GitHandler;
import com.djyos.dide.ui.wizards.djyosProject.tools.DideHelper;

public class GitUpdateHandler extends AbstractHandler {

	private DideHelper dideHelper = new DideHelper();
	String djysrcPath = dideHelper.getDjyosSrcPath();
	File gitFile = new File(djysrcPath + "/.git");
	// File didePrefsFile = new File(dideHelper.getDIDEPath() +
	// "IDE/configuration/.settings/com.djyos.ui.prefs");
	// File gitFile = new File(djysrcPath + "/.git");

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		// TODO Auto-generated method stub
		GitHandler gitHandler = new GitHandler();
		gitHandler.remindUpdate(1);
		// if (!gitFile.exists()) {
		//
		// }
		// boolean toUpdate = MessageDialog.openQuestion(window.getShell(), "提示",
		// "是否要更新Djyos源码？");
		// boolean finishUpdate = false;
		// if (toUpdate) {
		// try {
		// finishUpdate = updateCode(djysrcPath);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (GitAPIException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		//
		// if (finishUpdate) {
		// MessageDialog.openInformation(window.getShell(), "提示", "更新成功");
		// }

		return null;
	}

	/*
	 * 更新本地代码
	 */
	public boolean updateCode(String projectPath) throws IOException, GitAPIException {
		try {
			Git git = Git.open(new File(projectPath + "/.git"));
			git.pull().setRemoteBranchName("master").call();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

}
