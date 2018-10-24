package com.djyos.dide.ui.startup;

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
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.RefUpdate.Result;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.djyos.dide.ui.wizards.djyosProject.tools.DeleteFolder;
import com.djyos.dide.ui.wizards.djyosProject.tools.DideHelper;

public class GitHandler {
	// https://git.coding.net/djyos/source.git
	// https://gitee.com/djyos/source.git
	// https://github.com/ErikForChan/Arraylist_link.git
	DeleteFolder df = new DeleteFolder();
	DideHelper dideHelper = new DideHelper();
	String djysrcPath = dideHelper.getDjyosSrcPath();
	String comparePath = dideHelper.getDIDEPath() + "gitTemp";

	File didePrefsFile = new File(dideHelper.getDIDEPath() + "IDE/configuration/.settings/com.djyos.ui.prefs");
	public String remotePath = "https://git.coding.net/djyos/source.git";// djyosԶ�̿�·��
	private IWorkspace workspace = ResourcesPlugin.getWorkspace();
	File compareFile = new File(comparePath);

	/*
	 * ɾ���ļ���
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

	/*
	 * ���±��ش���
	 */
	public void updateCode(String projectPath) throws IOException, GitAPIException {

		IRunnableWithProgress runnable = new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				// TODO Auto-generated method stub
				monitor.beginTask("���ڸ���DjyosԴ��", 10);
				monitor.worked(5);
				boolean isOK = true;
				String errMsg = null;
				try {
					Git git = Git.open(new File(djysrcPath + "/.git"));
					git.pull().setRemoteBranchName("master").call();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					isOK = false;
					monitor.done();
					// errMsg = e.getMessage();
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						public void run() {
							IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
							MessageDialog.openError(window.getShell(), "��ʾ", "����DjyosԴ��ʧ��: " + e.getMessage());
						}
					});
					e.printStackTrace();
				}
				monitor.worked(5);
				if (isOK) {
					monitor.done();
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						public void run() {
							IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
							MessageDialog.openInformation(window.getShell(), "��ʾ", "����DjyosԴ�����");
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
		// try {
		// Git git = Git.open(new File(projectPath + "/.git"));
		// git.pull().setRemoteBranchName("master").call();
		// return true;
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// return false;
		// }
	}

	/*
	 * ��Iteratorת����List
	 */
	public static List<RevCommit> copyIterator(Iterator<RevCommit> iter) {
		List<RevCommit> copy = new ArrayList<RevCommit>();
		while (iter.hasNext())
			copy.add(iter.next());
		return copy;
	}

	private void prepareUpdate(int tag) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				boolean update = false;
				if (tag == 0) {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					GitUpdateInfoDialog dialog = new GitUpdateInfoDialog(window.getShell(), "��ʾ");
					if (dialog.open()) {
						boolean noticeMe = dialog.allowNoticeMe();
						String content = "SHOW_GIT_UPDATE_DIALOG=" + noticeMe;
						if (!didePrefsFile.exists()) {
							fillGitPrefsFile(didePrefsFile, content);
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
					// ���±��ش���
					try {
						updateCode(djysrcPath);
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

	/*
	 * ���git�Ƿ��и���
	 */
	public void checkUpdate(String projectPath, int tag) {
		// File compareFile = new File(comparePath);
		// if (compareFile.exists()) {
		// deleteDir(compareFile);
		// compareFile.delete();
		// }
		boolean isOK;

		Job backgroundJob = new Job("�Ƚϱ�����Զ�̰汾��Ϣ") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("�Ƚϱ�����Զ�̰汾��Ϣ", 10);
				File gitLocalFile = new File(projectPath + "/.git");
				boolean update = true;
				try {
					monitor.worked(5);
					Git gitLocal = Git.open(gitLocalFile);
					Iterable<RevCommit> gitlogCur = gitLocal.log().call();
					List<RevCommit> gitIistCur = copyIterator(gitlogCur.iterator());
					String curVersion = gitIistCur.get(0).getName();
					String preFetchVersion = getFetchVersion(gitLocalFile);

					if (curVersion.equals(preFetchVersion)) {
						FetchResult fetchResult = gitLocal.fetch().call();
						TrackingRefUpdate refUpdate = fetchResult.getTrackingRefUpdate("refs/remotes/origin/master");
						if (refUpdate != null) {
							Result result = refUpdate.getResult();
							monitor.worked(3);
							String remoteVersion = getFetchVersion(gitLocalFile);
							System.out.println("curVersion :  " + curVersion);
							System.out.println("remoteVersion :  " + remoteVersion);
							monitor.worked(1);
							if (curVersion.equals(remoteVersion)) {
								update = false;
							}
						} else {
							update = false;
						}
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					update = false;
				}
				if (!update) {
					if (tag == 1) {
						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
							public void run() {
								IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
								MessageDialog.openInformation(window.getShell(), "��ʾ", "������£��Ѿ������°汾");
							}
						});
					}
				} else {
					prepareUpdate(tag);
					return Status.OK_STATUS;
				}
				monitor.worked(1);
				return Status.CANCEL_STATUS;
			}
		};
		backgroundJob.schedule();

	}

	/*
	 * ��eclispe�����Ƿ���Ҫ���´���
	 */
	protected String getFetchVersion(File gitLocalFile) {
		// TODO Auto-generated method stub
		File file = new File(gitLocalFile + "/FETCH_HEAD");
		BufferedReader buf = null;
		String line = null;
		try {
			buf = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
			line = buf.readLine();
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
		String remoteVersion = line.split("\\s+")[0];
		return remoteVersion;
	}

	private boolean cloneRepository(String url, String localPath) {

		IRunnableWithProgress runnable = new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				// TODO Auto-generated method stub
				monitor.beginTask("��������DjyosԴ��...����ʱ��(��Լ10����)�����������йأ������ĵȴ�", 10);
				monitor.worked(7);
				CloneCommand cc = Git.cloneRepository().setURI(url);
				try {
					cc.setDirectory(new File(localPath)).call();
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
				monitor.worked(9);
				monitor.setTaskName("�������......");
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
					MessageDialog.openInformation(window.getShell(), "��ʾ", "���سɹ�");
				}
			});
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					MessageDialog.openInformation(window.getShell(), "��ʾ", "����ʧ��");
				}
			});
			File djysrcFile = new File(djysrcPath);
			deleteDir(djysrcFile);
			return false;
		}

	}

	private void fillGitPrefsFile(File dideGitPrefsFile, String content) {
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

	private boolean showGitUpdate(File dideGitPrefsFile) {
		// TODO Auto-generated method stub
		BufferedReader br = null;
		String line = null;
		StringBuffer bufAll = new StringBuffer(); // �����޸Ĺ�����������ݣ���������
		try {
			br = new BufferedReader(new FileReader(dideGitPrefsFile));
			while ((line = br.readLine()) != null) {
				StringBuffer buf = new StringBuffer();
				// �޸����ݺ��Ĵ���
				if (line.startsWith("SHOW_GIT_UPDATE_DIALOG")) {
					String[] infos = line.split("=");
					if (infos[1].trim().equals("false")) {
						return false;
					}
					break;
				}
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
		return true;
	}

	public void remindUpdate(int tag) {
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
								toUpdate = MessageDialog.openQuestion(window.getShell(), "��ʾ", "�Ƿ�Ҫ����DjyosԴ�룿");
							}

							if ((tag == 1 && toUpdate) || (tag == 0)) {
								checkUpdate(djysrcPath, tag);
								deleteDir(compareFile);
							}
						}

					});
				}
			});
		}

		// ���û��gitԴ�룬������GitԴ��
		if (!hasDjysrc) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					boolean gotoDownLoad = false;
					GitPromptDialog dialog = new GitPromptDialog(window.getShell(), "��ʾ");
					// String[] downloadTips = { "��ʾ", "DIDE�л�δ��DjyosԴ�룬�Ƿ��Զ�����Դ�룿" };
					if (dialog.open()) {
						GitUriDialog dialog1 = new GitUriDialog(window.getShell(), remotePath, "ȷ��Git��ַ");
						if (dialog1.open()) {
							String gitPath = dialog1.getGitUri();
							File djysrcFile = new File(djysrcPath);
							if (!djysrcFile.exists()) {
								djysrcFile.mkdirs();
							}
							cloneRepository(remotePath, djysrcPath);
						}
					}
					if (dialog.notPromptAgain()) {
						setDjyosUiPrefs(didePrefsFile, false, "SHOW_GIT_UPDATE_DIALOG");
					}
				}
			});

		}
		// PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
		//
		// @Override
		// public void run() {
		// // TODO Auto-generated method stub
		// IWorkbenchWindow window =
		// PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// boolean gotoRestart = MessageDialog.openQuestion(window.getShell(),
		// "��ʾ",
		// "�Ƿ�������");
		// if(gotoRestart) {
		// PlatformUI.getWorkbench().restart(true);
		// }
		// }
		//
		// });

	}

	private void setDjyosUiPrefs(File didePrefsFile, boolean isTrue, String target) {
		// TODO Auto-generated method stub
		BufferedReader br = null;
		String line = null;
		boolean targetExist = false;
		StringBuffer bufAll = new StringBuffer(); // �����޸Ĺ�����������ݣ���������
		try {
			br = new BufferedReader(new FileReader(didePrefsFile));
			while ((line = br.readLine()) != null) {
				StringBuffer buf = new StringBuffer();
				// �޸����ݺ��Ĵ���
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
		fillGitPrefsFile(didePrefsFile, bufAll.toString());

	}
}
