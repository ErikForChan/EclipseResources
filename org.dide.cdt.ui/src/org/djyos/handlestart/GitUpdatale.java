package org.djyos.handlestart;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.egit.ui.internal.clone.GitCloneWizard;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class GitUpdatale {
	public String remotePath = "https://gitee.com/djyos/source.git";//远程库路径
	
	public String savePath = "C:\\Users\\admin\\git\\source";//本地路径
	
	UsernamePasswordCredentialsProvider usernamePasswordCredentialsProvider =new
            UsernamePasswordCredentialsProvider("1043490933@qq.com","chenjiaming917");
	/*
	 * 删除文件夹
	 */
	public static void deleteDir(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles())
                deleteDir(f);
        }
        file.delete();
    }
	
	/*
	 * 更新本地代码
	 */
	public void updateCode(String projectPath) throws IOException, GitAPIException {
		UsernamePasswordCredentialsProvider usernamePasswordCredentialsProvider =new
	             UsernamePasswordCredentialsProvider("1043490933@qq.com","chenjiaming917");
			try {
				Git git = Git.open( new File(projectPath+"/.git") );
				git.pull().setRemoteBranchName("master").
				 setCredentialsProvider(usernamePasswordCredentialsProvider).call();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
         //克隆代码库命令
         CloneCommand cloneCommand = Git.cloneRepository();
         File saveFile = new File(savePath);
         if(saveFile.exists())  {
        	 deleteDir(saveFile);
         }

        try {      	
			Git git= cloneCommand.setURI(remotePath) //设置远程URI
			         .setBranch("master") //设置clone下来的分支
			         .setDirectory(saveFile) //设置下载存放路径
			         .setCredentialsProvider(usernamePasswordCredentialsProvider) //设置权限验证
			         .call();	
	        try {
	        	//git仓库地址
				Git gitLocal = Git.open(new File(projectPath+"/.git"));
	        	
	        	Iterable<RevCommit> gitlogCur= gitLocal.log().call();  
	        	Iterable<RevCommit> gitlogRemote= git.log().call(); 
	        	
	        	Iterator<RevCommit> gitIterCur  = gitlogCur.iterator();
	        	Iterator<RevCommit> gitIterRemote  = gitlogRemote.iterator();
	        	
	        	List<RevCommit> gitIistCur = copyIterator(gitIterCur);
	        	List<RevCommit> gitIistRemote = copyIterator(gitIterRemote);
	        	
	        	String curVersion = gitIistCur.get(0).getName();
	        	String remoteVersion = gitIistRemote.get(0).getName();
	        	
	        	boolean curEqualsRemote = curVersion.equals(remoteVersion);
	        	
				if(!curEqualsRemote) {
					return true;
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
    public void remindUpdate() {
    	
    	boolean isNeedUpdate = false;
    	boolean hasGitProject = false;
    	String[] tips = {
    			"更新提示","git有更新,是否更新代码？"
    	};
    	IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    	for(IProject project : projects) {
    		String projectPath = project.getLocation().toString();
    		File file = new File(projectPath+"/.git");
    		if(file.exists()) {
    			hasGitProject = true;
    			isNeedUpdate = checkUpdate(projectPath);  			
    			if(isNeedUpdate) {
    	    		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {  
    	                public void run() {     	                	
    	                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();  
    	                    boolean gotoUpdate = false;
							try {
								gotoUpdate = MessageDialog.openQuestion(
										window.getShell(),
										new String(tips[0].getBytes(),"GBK"),
										new String(tips[1].getBytes(),"GBK"));
							} catch (UnsupportedEncodingException e2) {
								// TODO Auto-generated catch block
								e2.printStackTrace();
							}	   
    	                    if(gotoUpdate) {
    	                    	//更新本地代码
    	                    	try {
									updateCode(projectPath);
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								} catch (GitAPIException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
    	                    	try {
									project.refreshLocal(IResource.DEPTH_INFINITE, null);
								} catch (CoreException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
    	                    }
    	                }  
    	            }); 
    	    		
    	    	}
    		}		
    	}
    	
    	if(!hasGitProject) {
    		String presetURI="https://gitee.com/djyos/source.git";
    		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {  
                @SuppressWarnings("restriction")
    			public void run() {     	                	
                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();  
                    GitCloneWizard wizard;
//            		if (presetURI == null)
//            			wizard = new GitCloneWizard();
//            		else
            		wizard = new GitCloneWizard(presetURI);
            		wizard.setShowProjectImport(true);
            		WizardDialog dlg = new WizardDialog(window.getShell(), wizard);
            		dlg.setHelpAvailable(true);
            		dlg.open();
                }  
            }); 
    		
    	}
         
    }
}
