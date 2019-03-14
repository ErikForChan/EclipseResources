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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.wizards.djyosProject.tools.DeleteFolderUtils;

public class GitHandler {
	// https://git.coding.net/djyos/djyos.git
	// https://gitee.com/djyos/source.git
	// https://github.com/ErikForChan/Arraylist_link.git
	DeleteFolderUtils df = new DeleteFolderUtils();
	String djysrcPath = DideHelper.getDjyosSrcPath();
	String comparePath = DideHelper.getDIDEPath() + "gitTemp";

	File didePrefsFile = new File(DideHelper.getDIDEPath() + "IDE/configuration/.settings/com.djyos.ui.prefs");
	public String remotePath = "https://git.coding.net/djyos/djyos.git";// djyos远程库路径
	private IWorkspace workspace = ResourcesPlugin.getWorkspace();
	File compareFile = new File(comparePath);

	public void remind_Update(int tag) {
		boolean hasDjysrc = false;
		File gitFile = new File(djysrcPath + "/.git");
		if (gitFile.exists()) {
			hasDjysrc = true;
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {

					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						public void run() {
							IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
							boolean toUpdate = false;
							if (tag == 1) {
								toUpdate = MessageDialog.openQuestion(window.getShell(), "提示", "是否要更新Djyos源码？");
							}
							if ((tag == 1 && toUpdate) || (tag == 0)) {
								check_Update(djysrcPath, tag);
							}
						}

					});
				}
			});
		}

		// 如果没有git源码，则下载Git源码
		if (!hasDjysrc) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					GitPromptDialog dialog = new GitPromptDialog(window.getShell(), "提示");
					if (dialog.open()) {
						GitUriDialog dialog1 = new GitUriDialog(window.getShell(), remotePath, "确认Git地址");
						if (dialog1.open()) {
							File djysrcFile = new File(djysrcPath);
							if (!djysrcFile.exists()) {
								djysrcFile.mkdirs();
							}
							clone_Repository(remotePath, djysrcFile);
						}
					}
					if (dialog.notPromptAgain()) {
						setDjyosUiPrefs(didePrefsFile, false, "SHOW_GIT_UPDATE_DIALOG");
					}
				}
			});

		}

	}

	/*
	 * 更新本地代码
	 */
	public void update_Local_Code(String projectPath) throws IOException, GitAPIException {

		IRunnableWithProgress runnable = new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				// TODO Auto-generated method stub
				monitor.beginTask("正在更新Djyos源码", 10);
				monitor.worked(5);
				boolean isOK = true;
				try {
					Git git = Git.open(new File(djysrcPath + "/.git"));
					String branchName = git.getRepository().getBranch();
					if (!branchName.equals("release")) {
						git.checkout().setCreateBranch(true).setName("release").call();
						git.pull().call();
					} else {
						git.pull().call();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					isOK = false;
					monitor.done();
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						public void run() {
							DideHelper.showErrorMessage("更新Djyos源码失败: " + e.getMessage());
						}
					});
				}
				monitor.worked(5);
				if (isOK) {
					monitor.done();
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						public void run() {
							IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
							MessageDialog.openInformation(window.getShell(), "提示", "更新Djyos源码完成");
						}
					});
					try {
						workspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		};

		try {

			ProgressMonitorDialog dialog = new ProgressMonitorDialog(
					PlatformUI.getWorkbench().getDisplay().getActiveShell());
			dialog.setCancelable(false);
			dialog.run(true, true, runnable);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void clone_Release(File srcFile) {
		// TODO Auto-generated method stub
		CloneCommand cc = Git.cloneRepository().setURI(remotePath);
		try {
			cc.setBranch("release").setDirectory(srcFile).call();
		} catch (InvalidRemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void prepare_Update(int tag) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				boolean update = false;
				if (tag == 0) {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					GitUpdateInfoDialog dialog = new GitUpdateInfoDialog(window.getShell(), "提示");
					if (dialog.open()) {
						boolean noticeMe = dialog.allowNoticeMe();
						String content = "SHOW_GIT_UPDATE_DIALOG=" + noticeMe;
						if (!didePrefsFile.exists()) {
							fill_GitPrefsFile(didePrefsFile, content);
						} else {
							if (!noticeMe) {
								setDjyosUiPrefs(didePrefsFile, false, "SHOW_GIT_UPDATE_DIALOG");
							}
						}
						update = true;
					}
				} else {
					update = true;
				}

				if (update) {
					// 更新本地代码
					try {
						update_Local_Code(djysrcPath);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (GitAPIException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
	}

	private String getCurVersion(Git gitLocal) {
		Iterable<RevCommit> gitlogCur;
		List<RevCommit> gitIistCur = null;
		try {
			gitlogCur = gitLocal.log().call();
			gitIistCur = copyIterator(gitlogCur.iterator());
		} catch (NoHeadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return gitIistCur.get(0).getName();
	}

	/*
	 * 检查git是否有更新
	 */
	public void check_Update(String projectPath, int tag) {

		Job backgroundJob = new Job("比较本地与远程版本信息") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("比较本地与远程版本信息", 10);
				File gitLocalFile = new File(projectPath + "/.git");
				File local_master_file = new File(djysrcPath+"/.git/refs/heads/master");
				File remote_master_file = new File(djysrcPath+"/.git/refs/remotes/origin/master");
				boolean update = true;
				try {
					monitor.worked(5);
					Git gitLocal = Git.open(gitLocalFile);
					gitLocal.fetch().call();
//					FetchResult fetchResult = gitLocal.fetch().call();
					String local_version = DideHelper.readFile(local_master_file).trim();
					String remote_version = DideHelper.readFile(remote_master_file).trim();
//					TrackingRefUpdate refUpdate = fetchResult.getTrackingRefUpdate("refs/remotes/origin/release");
					if (local_version.equalsIgnoreCase(remote_version)) {
						update = false;
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					update = false;
				}
				if (!update) {
					if (tag == 1) {
						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
							public void run() {
								IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
								MessageDialog.openInformation(window.getShell(), "提示", "无需更新，已经是最新版本");
							}
						});
					}
				} else {
					prepare_Update(tag);
					return Status.OK_STATUS;
				}
				monitor.worked(5);
				return Status.CANCEL_STATUS;
			}
		};
		backgroundJob.schedule();

	}

	/*
	 * 打开eclispe提醒是否需要更新代码
	 */
	protected String getFetchVersion(File gitLocalFile) {
		// TODO Auto-generated method stub
		File file = new File(gitLocalFile + "/FETCH_HEAD");
		BufferedReader buf = null;
		String line = null;
		String releaseMsg = null;
		if (file.exists()) {
			try {
				buf = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
				while ((line = buf.readLine()) != null) {
					// 修改内容核心代码
					if (line.contains("release")) {
						releaseMsg = line;
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (buf != null) {
					try {
						buf.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			if (releaseMsg != null) {
				String remoteVersion = releaseMsg.split("\\s+")[0];
				return remoteVersion;
			}
		}
		return "";

		// DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// Repository repository;
		// try {
		// repository = new RepositoryBuilder().setGitDir(gitLocalFile).build();
		// try (RevWalk walk = new RevWalk(repository)) {
		// Ref head = repository.findRef("HEAD");
		// walk.markStart(walk.parseCommit(head.getObjectId())); // 从HEAD开始遍历，
		// for (RevCommit commit : walk) {
		// RevTree tree = commit.getTree();
		// System.out.println("tree： " + tree);
		//
		// TreeWalk treeWalk = new TreeWalk(repository, repository.newObjectReader());
		// PathFilter f = PathFilter.create("pom.xml");
		// treeWalk.setFilter(f);
		// treeWalk.reset(tree);
		// treeWalk.setRecursive(false);
		// while (treeWalk.next()) {
		// PersonIdent authoIdent = commit.getAuthorIdent();
		// System.out.println("提交人： " + authoIdent.getName() + " <" +
		// authoIdent.getEmailAddress() + ">");
		// System.out.println("提交SHA1： " + commit.getId().name());
		// System.out.println("提交信息： " + commit.getShortMessage());
		// System.out.println("提交时间： " + format.format(authoIdent.getWhen()));
		//
		// ObjectId objectId = treeWalk.getObjectId(0);
		// ObjectLoader loader = repository.open(objectId);
		// loader.copyTo(System.out); // 提取blob对象的内容
		// }
		// }
		// }
		//
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	private boolean clone_Repository(String url, File file) {

		IRunnableWithProgress runnable = new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				// TODO Auto-generated method stub
				monitor.beginTask("正在下载Djyos源码...下载时间(大约10分钟)与您的网速有关，请耐心等待", 10);
				monitor.worked(7);
				clone_Release(file);
				monitor.worked(9);
				monitor.setTaskName("下载完成......");
				monitor.worked(1);
				monitor.done();
			}

		};

		try {
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(
					PlatformUI.getWorkbench().getDisplay().getActiveShell());
			dialog.setCancelable(false);
			dialog.run(true, true, runnable);
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					MessageDialog.openInformation(window.getShell(), "提示", "下载成功");
				}
			});
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					MessageDialog.openInformation(window.getShell(), "提示", "下载失败");
				}
			});
			File djysrcFile = new File(djysrcPath);
			deleteDir(djysrcFile);
			return false;
		}

	}

	private void fill_GitPrefsFile(File dideGitPrefsFile, String content) {
		// TODO Auto-generated method stub
		try {
			dideGitPrefsFile.createNewFile();
			FileWriter writer;
			try {
				writer = new FileWriter(dideGitPrefsFile);
				writer.write(content);
				writer.flush();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setDjyosUiPrefs(File didePrefsFile, boolean isTrue, String target) {
		// TODO Auto-generated method stub
		BufferedReader br = null;
		String line = null;
		boolean targetExist = false;
		StringBuffer bufAll = new StringBuffer(); // 保存修改过后的所有内容，不断增加
		try {
			br = new BufferedReader(new FileReader(didePrefsFile));
			while ((line = br.readLine()) != null) {
				// 修改内容核心代码
				if (line.startsWith(target)) {
					line = target + "=" + isTrue;
					targetExist = true;
				}
				bufAll.append(line + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					br = null;
				}
			}
		}
		if (!targetExist) {
			bufAll.append(target + "=" + isTrue + "\n");
		}
		didePrefsFile.delete();
		fill_GitPrefsFile(didePrefsFile, bufAll.toString());

	}

	/*
	 * 将Iterator转换成List
	 */
	public static List<RevCommit> copyIterator(Iterator<RevCommit> iter) {
		List<RevCommit> copy = new ArrayList<RevCommit>();
		while (iter.hasNext())
			copy.add(iter.next());
		return copy;
	}

	/*
	 * 删除文件夹
	 */
	public static void deleteDir(File file) {
		if (file.isHidden() || !file.isHidden()) {
			if (file.isDirectory()) {
				for (File f : file.listFiles())
					deleteDir(f);
			}
			file.delete();
		}
	}
}
