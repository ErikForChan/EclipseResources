/*******************************************************************************
 * Copyright (c) 2017 Djyos Team.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.djyos.com
 *
 * Contributors:
 *     Djyos Team - Jiaming Chen
 *******************************************************************************/
package com.djyos.dide.ui.git;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import com.djyos.dide.ui.helper.DideHelper;

public class GitUpdateHandler extends AbstractHandler {

	String djysrcPath = DideHelper.getDjyosSrcPath();
	File gitFile = new File(djysrcPath + "/.git");

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		// TODO Auto-generated method stub
		GitHandler gitHandler = new GitHandler();
		gitHandler.remind_Update(1);
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
