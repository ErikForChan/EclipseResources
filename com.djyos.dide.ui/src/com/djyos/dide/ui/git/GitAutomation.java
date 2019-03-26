package com.djyos.dide.ui.git;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.ui.internal.ide.actions.BuildUtilities;

import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.job.BuildTarget;
import com.djyos.dide.ui.job.SendEmail;
import com.djyos.dide.ui.wizards.djyosProject.tools.FileTool;
import com.djyos.dide.ui.wizards.djyosProject.tools.PathTool;
import com.ibm.icu.text.SimpleDateFormat;

public class GitAutomation {
	
	final static IWorkspace workspace = ResourcesPlugin.getWorkspace();
	
	/**
	 * 每隔1小时检查是否有源码更新
	 */
	public static void start_gitMonitor() {
		Timer timer = new Timer(true);
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
//				File stup_complie_file = new File(DideHelper.getDIDEPath() + "complieAuto.txt");
//				if (stup_complie_file.exists()) {
					String djysrcPath = PathTool.getDjyosSrcPath();
					File gitLocalFile = new File(djysrcPath + "/.git");
					File local_master_file = new File(djysrcPath+"/.git/refs/heads/master");
					File remote_master_file = new File(djysrcPath+"/.git/refs/remotes/origin/master");
					if (gitLocalFile.exists()) {
						try {
							Git gitLocal = Git.open(gitLocalFile);
							String local_version = FileTool.readFile(local_master_file).trim();
							String remote_version = null;
							if(remote_master_file.exists()) {
								remote_version = FileTool.readFile(remote_master_file).trim();
							}else {
								remote_version = local_version;
							}
							System.out.println("local_version:  "+local_version);
							System.out.println("remote_version:  "+remote_version);
							if(!local_version.equalsIgnoreCase(remote_version)) {
//								MergeStatus status = pr.getMergeResult().getMergeStatus();
								PullResult pr =  gitLocal.pull().call();
								if(!pr.isSuccessful()) {
									Date d = new Date();
									SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
									DideHelper.printToConsole("更新djysrc与源码失败失败"+",时间: "+sdf.format(d), true);
								}else {
									System.out.println("pr.isSuccessful()....................................");
									gitLocal.fetch().call();
									Iterable<RevCommit> gitlog= gitLocal.log().call();
									List<RevCommit> commitList = GitHandler.copyIterator(gitlog.iterator());
									String email = commitList.get(0).getAuthorIdent().getEmailAddress();
									String version = commitList.get(0).getName();// 版本号
									String author = commitList.get(0).getAuthorIdent().getName();
									Date time = commitList.get(0).getAuthorIdent().getWhen();//时间
									build_workspace_projects(email);
									DideHelper.refresh_workspace();
								}
								
							}else {
								
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							DideHelper.printToConsole(e.getMessage(), true);
						}
					}
//				}
			}
		},0, 2 * 60 * 60 * 1000);
	}

	protected static void build_workspace_projects(String email) {
		// TODO Auto-generated method stub
		IProject[] projects = workspace.getRoot().getProjects();
		
		if (projects.length != 0) {
			CUIPlugin.getDefault().startGlobalConsole();
			Job buildJob = null;
			for (IProject project : projects) {
				ICProjectDescription projectDescription = CoreModel.getDefault().getProjectDescription(project, false);
				if (projectDescription != null) {
					ICConfigurationDescription[] cfgds = projectDescription.getConfigurations();
					ArrayList<ICConfigurationDescription> libcfgdList = new ArrayList<ICConfigurationDescription>();
					for (ICConfigurationDescription cfgd : cfgds) {
						if (cfgd.getName().startsWith("libos")) {
							libcfgdList.add(cfgd);
						}
					}
					ICConfigurationDescription[] libosCfgds = new ICConfigurationDescription[libcfgdList.size()];
					if (cfgds != null && cfgds.length > 0) {
						BuildUtilities.saveEditors(null);
						buildJob = new BuildTarget(libcfgdList.toArray(libosCfgds), 0,
								IncrementalProjectBuilder.INCREMENTAL_BUILD);
						buildJob.schedule();
					}
				}
			}
			
			if (buildJob != null) {
				Job job = buildJob;
				Timer t = new Timer(true);
				t.schedule(new TimerTask() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (job.getState() == Job.RUNNING) {
							System.out.println("----------Job.RUNNING");
						} else if (job.getState() == Job.WAITING) {
							System.out.println("----------Job.WAITING");
						} else if (job.getState() == Job.SLEEPING) {
							System.out.println("----------Job.SLEEPING");
						} else if (job.getState() == Job.NONE) {
							System.out.println("----------Job.NONE");
							File errorFile = new File(PathTool.getDIDEPath() + "IDE/readme/errorResult.txt"); //$NON-NLS-1$
							String err_msg = FileTool.readFile(errorFile);
							Job job1 = new SendEmail("测试,请勿回复： \n"+err_msg, email);
							job1.schedule();
							t.cancel();
						}
					}
				}, 0, 10 * 1000);
			}
		}
	}

}
