package com.djyos.dide.ui.startup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
import org.eclipse.jface.window.Window;
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
	public String remotePath = "https://git.coding.net/djyos/source.git";//djyos远程库路径	
	private IWorkspace workspace = ResourcesPlugin.getWorkspace();
	File compareFile = new File(comparePath);
	/*
	 * 删除文件夹
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
	 * 更新本地代码
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
	 * 将Iterator转换成List
	 */
	public static List<RevCommit> copyIterator(Iterator<RevCommit> iter) {  
	    List<RevCommit> copy = new ArrayList<RevCommit>();  
	    while (iter.hasNext())  
	        copy.add(iter.next());  
	    return copy;  
	} 

	/*
	 * 检查git是否有更新
	 */
	public boolean checkUpdate(String projectPath) {
        File compareFile = new File(comparePath);
   
		try {
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
	 * 打开eclispe提醒是否需要更新代码
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
				monitor.beginTask("正在下载Djyos源码...下载时间(大约2-10分钟)与您的网速有关，请耐心等待", 10);	
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
					MessageDialog.openInformation(window.getShell(),
							"提示","下载成功");
			}
			});
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					MessageDialog.openInformation(window.getShell(),
							"提示","下载失败");
			}
			});
			File djysrcFile = new File(djysrcPath);
			deleteDir(djysrcFile);
			return false;
		}
		
	}
	
	private void setGitPrefs(File dideGitPrefsFile, boolean noticeMe) {
		// TODO Auto-generated method stub
		BufferedReader br = null;  
        String line = null;  
        StringBuffer bufAll = new StringBuffer();  //保存修改过后的所有内容，不断增加         
        try {            
            br = new BufferedReader(new FileReader(dideGitPrefsFile));              
            while ((line = br.readLine()) != null) {  
                StringBuffer buf = new StringBuffer();  
                //修改内容核心代码
                if (line.startsWith("SHOW_GIT_UPDATE_DIALOG")) {  
                	line = "SHOW_GIT_UPDATE_DIALOG="+noticeMe;
                   
                }
                bufAll.append(line+"\n");            
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
        
        dideGitPrefsFile.delete();
        fillGitPrefsFile(dideGitPrefsFile,bufAll.toString());
        
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
        StringBuffer bufAll = new StringBuffer();  //保存修改过后的所有内容，不断增加         
        try {            
            br = new BufferedReader(new FileReader(dideGitPrefsFile));              
            while ((line = br.readLine()) != null) {  
                StringBuffer buf = new StringBuffer();  
                //修改内容核心代码
                if (line.startsWith("SHOW_GIT_UPDATE_DIALOG")) {  
                	String[] infos = line.split("=");
                	if(infos[1].trim().equals("false")) {
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
	
    public void remindUpdate() {
    	boolean isNeedUpdate = false;
    	boolean hasGitProject = false;
		File file = new File(djysrcPath);
		if (file.exists()) {
			hasGitProject = true;
			isNeedUpdate = checkUpdate(djysrcPath);
			String dideGitPrefsPath = dideHelper.getDIDEPath()+"IDE/configuration/.settings/com.djyos.ui.prefs";
			File dideGitPrefsFile = new File(dideGitPrefsPath);
			boolean showUpdate;
			if(dideGitPrefsFile.exists()) {
				showUpdate = showGitUpdate(dideGitPrefsFile);
			}else {
				showUpdate = true;
			}
			
			if (isNeedUpdate && showUpdate) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						
						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
							public void run() {
								boolean gotoUpdate = false;
								IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
//								try {
//									gotoUpdate = MessageDialog.openQuestion(window.getShell(),
//											new String(tips[0].getBytes(), "GBK"),
//											new String(tips[1].getBytes(), "GBK"));
//								} catch (UnsupportedEncodingException e2) {
//									// TODO Auto-generated catch block
//									e2.printStackTrace();
//								}
								GitUpdateInfoDialog dialog = new GitUpdateInfoDialog(window.getShell(),"Update Djyos Resource");
								if (dialog.open()) {
									
									boolean noticeMe = dialog.allowNoticeMe();
									String content = "SHOW_GIT_UPDATE_DIALOG="+noticeMe;								
									if(!dideGitPrefsFile.exists()) {
										fillGitPrefsFile(dideGitPrefsFile,content);
									}else {
										setGitPrefs(dideGitPrefsFile,noticeMe);
									}
									
									// 更新本地代码
									boolean finishUpdate = false;
									try {
										finishUpdate = updateCode(djysrcPath);
									} catch (Exception e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
									try {
										if(finishUpdate) {
											PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
												public void run() {
													IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
													MessageDialog.openInformation(window.getShell(),
															"提示","更新成功");
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
    	
		//如果没有git源码，则下载Git源码
		if (!hasGitProject) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					boolean gotoDownLoad = false;
					String[] downloadTips = {
			    			"提示","DIDE中还未有Djyos源码，是否自动下载源码？"
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
						GitUriDialog dialog = new GitUriDialog(window.getShell(),remotePath,"确认Git地址");
						if(dialog.open()) {
							String gitPath = dialog.getGitUri();
							File djysrcFile = new File(djysrcPath);
							if(!djysrcFile.exists()) {
								djysrcFile.mkdirs();
							}
//							GitCloneWizard wizard;
							// if (presetURI == null)
							// wizard = new GitCloneWizard();
							// else
//							wizard = new GitCloneWizard(remotePath);
//							wizard.setShowProjectImport(true);
//							WizardDialog dlg = new WizardDialog(window.getShell(), wizard);
//							dlg.setHelpAvailable(true);
//							dlg.open();
							cloneRepository(remotePath,djysrcPath);
						}
					}
			}
			});

		}
//		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
//
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
//				boolean gotoRestart = MessageDialog.openQuestion(window.getShell(),
//							"提示",
//							"是否重启？");
//				if(gotoRestart) {
//					PlatformUI.getWorkbench().restart(true);
//				}
//			}
//			
//		});
		
    }

}
