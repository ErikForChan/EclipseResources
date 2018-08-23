package com.djyos.dide.ui.startup;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.egit.ui.Activator;
import org.eclipse.egit.ui.JobFamilies;
import org.eclipse.egit.ui.internal.UIText;
import org.eclipse.egit.ui.internal.clone.GitCloneWizard;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import com.djyos.dide.ui.wizards.djyosProject.tools.DeleteFolder;
import com.djyos.dide.ui.wizards.djyosProject.tools.DideHelper;

public class GitHandler {
//https://git.coding.net/djyos/source.git  https://gitee.com/djyos/source.git https://github.com/ErikForChan/Arraylist_link.git
	DeleteFolder df = new DeleteFolder();
	DideHelper dideHelper = new DideHelper();
	String djysrcPath= dideHelper.getDjyosSrcPath();
	String comparePath= dideHelper.getDIDEPath()+"gitTemp";
	public String remotePath = "https://git.coding.net/djyos/source.git";//djyosԶ�̿�·��	
	private IWorkspace workspace = ResourcesPlugin.getWorkspace();
	File compareFile = new File(comparePath);
	
	UsernamePasswordCredentialsProvider usernamePasswordCredentialsProvider =new
            UsernamePasswordCredentialsProvider("1043490933@qq.com","chenjiaming917");
	/*
	 * ɾ���ļ���
	 */
	public static void deleteDir(File file) {
		if(file.isHidden() || !file.isHidden()) {
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
	public boolean updateCode(String projectPath) throws IOException, GitAPIException {
			try {
				Git git = Git.open( new File(projectPath+"/.git") );
				git.pull().setRemoteBranchName("master")
				.call();
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}		
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

	/*
	 * ���git�Ƿ��и���
	 */
	public boolean checkUpdate(String projectPath) {
        File compareFile = new File(comparePath);
   
		try {
			// Git git= cloneCommand.setURI(remotePath) //����Զ��URI
			// .setBranch("master") //����clone�����ķ�֧
			// .setDirectory(saveFile) //�������ش��·��
			// .setCredentialsProvider(usernamePasswordCredentialsProvider) //����Ȩ����֤
			// .call();
			try {
				if(compareFile.exists()) {
					deleteDir(compareFile);
					compareFile.delete();
				}
				File gitLocalFile = new File(projectPath+"/.git");
				if(gitLocalFile.exists()) {
					Git git = Git.cloneRepository().setURI(remotePath).setDirectory(compareFile).call();
					Git gitLocal = Git.open(gitLocalFile);

					Iterable<RevCommit> gitlogCur = gitLocal.log().call();
					Iterable<RevCommit> gitlogRemote = git.log().call();

					Iterator<RevCommit> gitIterCur = gitlogCur.iterator();
					Iterator<RevCommit> gitIterRemote = gitlogRemote.iterator();

					List<RevCommit> gitIistCur = copyIterator(gitIterCur);
					List<RevCommit> gitIistRemote = copyIterator(gitIterRemote);

					String curVersion = gitIistCur.get(0).getName();
					String remoteVersion = gitIistRemote.get(0).getName();

					boolean curEqualsRemote = curVersion.equals(remoteVersion);

					if (!curEqualsRemote) {
						return true;
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

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
		return false;
	}
	
	/*
	 * ��eclispe�����Ƿ���Ҫ���´���
	 */
	
	private boolean cloneRepository(String url, String localPath) {
		
//		final Job job = new Job(NLS.bind(UIText.GitCloneWizard_jobName,
//				url)) {
//			@Override
//			protected IStatus run(final IProgressMonitor monitor) {
//				CloneCommand cc = Git.cloneRepository().setURI(url);
//				try {
//					cc.setDirectory(new File(localPath)).call();
//				} catch (InvalidRemoteException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (TransportException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (GitAPIException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				return Status.OK_STATUS;
//			}
//
//			@Override
//			public boolean belongsTo(Object family) {
//				if (JobFamilies.CLONE.equals(family))
//					return true;
//				return super.belongsTo(family);
//			}
//		};
//		job.setUser(true);
//		job.schedule();
//		return true;
		
		IRunnableWithProgress runnable = new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				// TODO Auto-generated method stub
				monitor.beginTask("��������DjyosԴ��...����ʱ��(��Լ2-10����)�����������йأ������ĵȴ�", 10);	
				System.out.println("��ʼ����......");
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
					MessageDialog.openInformation(window.getShell(),
							"��ʾ","���سɹ�");
			}
			});
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					MessageDialog.openInformation(window.getShell(),
							"��ʾ","����ʧ��");
			}
			});
			return false;
		}
		
//		try {
//			System.out.println("��ʼ����......");
//			CloneCommand cc = Git.cloneRepository().setURI(url);
//			cc.setDirectory(new File(localPath)).call();
//			System.out.println("�������......");
//			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
//			MessageDialog.openInformation(window.getShell(),
//					"��ʾ","���سɹ�");
//			return true;
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
	}
	
    public void remindUpdate() {
    	System.out.println("remindUpdate");
    	boolean isNeedUpdate = false;
    	boolean hasGitProject = false;
		File file = new File(djysrcPath);
		if (file.exists()) {
			hasGitProject = true;
			isNeedUpdate = checkUpdate(djysrcPath);
			if (isNeedUpdate) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						
						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
							public void run() {
								boolean gotoUpdate = false;
								String[] tips = {
						    			"������ʾ","git��DjyosԴ���и���,�Ƿ���´��룿"
						    	};
								IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
								try {
									gotoUpdate = MessageDialog.openQuestion(window.getShell(),
											new String(tips[0].getBytes(), "GBK"),
											new String(tips[1].getBytes(), "GBK"));
								} catch (UnsupportedEncodingException e2) {
									// TODO Auto-generated catch block
									e2.printStackTrace();
								}
								if (gotoUpdate) {
									// ���±��ش���
									boolean finishUpdate = false;
									try {
										finishUpdate = updateCode(djysrcPath);
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									} catch (GitAPIException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
									try {
										if(finishUpdate) {
//											compareFile.delete();
											PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
												public void run() {
													IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
													MessageDialog.openInformation(window.getShell(),
															"��ʾ","���³ɹ�");
											}
											});
										}
										workspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
									} catch (CoreException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
						}
						});				
					}
				});

			}
			deleteDir(compareFile);
		}
    	
		//���û��gitԴ�룬������GitԴ��
		if (!hasGitProject) {
//			cloneRepository(remotePath,djysrcPath);
//			File oldDjysrcFile = new File(djysrcPath);
//			if(oldDjysrcFile.exists()) {
//				deleteDir(oldDjysrcFile);
//			}
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					boolean gotoDownLoad = false;
					String[] downloadTips = {
			    			"��ʾ","DIDE�л�δ��DjyosԴ�룬�Ƿ��Զ�����Դ�룿"
			    	};
					try {
						gotoDownLoad = MessageDialog.openQuestion(window.getShell(),
								new String(downloadTips[0].getBytes(), "GBK"),
								new String(downloadTips[1].getBytes(), "GBK"));
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(gotoDownLoad) {
						File djysrcFile = new File(djysrcPath);
						if(!djysrcFile.exists()) {
							djysrcFile.mkdirs();
						}
//						GitCloneWizard wizard;
						// if (presetURI == null)
						// wizard = new GitCloneWizard();
						// else
//						wizard = new GitCloneWizard(remotePath);
//						wizard.setShowProjectImport(true);
//						WizardDialog dlg = new WizardDialog(window.getShell(), wizard);
//						dlg.setHelpAvailable(true);
//						dlg.open();
						cloneRepository(remotePath,djysrcPath);
					}
			
			}
			});

		}
    }
}
