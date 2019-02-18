package com.djyos.dide.ui.autotesting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.internal.ide.actions.BuildUtilities;
//import org.tmatesoft.svn.core.SVNException;
//import org.tmatesoft.svn.core.SVNLogEntry;
//import org.tmatesoft.svn.core.SVNURL;
//import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
//import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
//import org.tmatesoft.svn.core.io.SVNRepository;
//import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
//import org.tmatesoft.svn.core.wc.SVNWCUtil;

import com.djyos.dide.ui.builder.BuildTarget;
import com.djyos.dide.ui.helper.DideHelper;

@SuppressWarnings("restriction")
public class SvnUpdateHandler {

	final IWorkspace workspace = ResourcesPlugin.getWorkspace();
	IProject[] projects = workspace.getRoot().getProjects();
	IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
	File svnVerFile = new File(DideHelper.getDIDEPath() + "IDE/configuration/.settings/com.djyos.svnVerson.prefs");

	public void visitSvn() {
		if (!svnVerFile.exists()) {
			try {
				svnVerFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// Detect_Svn_Schedule();

		Job buildJob = null;
		if (projects.length != 0) {

			CUIPlugin.getDefault().startGlobalConsole();
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
		}

	}

	private void Detect_Svn_Schedule() {
//		Timer timer = new Timer(true);
//		timer.schedule(new TimerTask() {
//
//			@SuppressWarnings("deprecation")
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				String url = "https://xiangmuzuserver/svn/硬件组开发库/platform_soft/djyos/trunk";
//				String name = "chenjm@sznari.com";
//				String password = "sunri@2017";
//				long startRevision = 43903;
//				long endRevision = -1; // HEAD (the latest) revision
//
//				Collection logEntries = null;
//				SVNURL svnurl;
//				try {
//					svnurl = SVNURL.parseURIEncoded(url);
//					DAVRepositoryFactory.setup();
//					ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(name,
//							password);
//					SVNRepository repos = SVNRepositoryFactory.create(svnurl);
//					repos.setAuthenticationManager(authManager);
//					logEntries = repos.log(new String[] { "" }, null, startRevision, endRevision, true, true);
//				} catch (SVNException e) {
//					e.printStackTrace();
//				}
//				int entryNum = 0;
//				for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
//					SVNLogEntry logEntry = (SVNLogEntry) entries.next();
//					entryNum++;
//					if (entryNum == logEntries.size()) {
//						long svnVersion = logEntry.getRevision();
//						// Long preSvnVersion = (long) 0;
//						Long preSvnVersion = getSvnVersion(svnVerFile);
//						// System.out.println("---------------------------------------------");
//						// System.out.println("revision: " + logEntry.getRevision());
//						// System.out.println("author: " + logEntry.getAuthor());
//						// System.out.println("date: " + logEntry.getDate());
//						// System.out.println("log message: " + logEntry.getMessage());
//						if (preSvnVersion != svnVersion) {
//							File errorFile = new File(DideHelper.getDIDEPath() + "IDE/configuration/errorResult.txt");
//							DideHelper.createNewFile(errorFile);
//
//							Job buildJob = null;
//							if (projects.length != 0) {
//								CUIPlugin.getDefault().startGlobalConsole();
//								for (IProject project : projects) {
//									ICProjectDescription projectDescription = CoreModel.getDefault()
//											.getProjectDescription(project, false);
//									if (projectDescription != null) {
//										ICConfigurationDescription[] cfgds = projectDescription.getConfigurations();
//										ArrayList<ICConfigurationDescription> libcfgdList = new ArrayList<ICConfigurationDescription>();
//										for (ICConfigurationDescription cfgd : cfgds) {
//											if (cfgd.getName().startsWith("libos")) {
//												libcfgdList.add(cfgd);
//											}
//										}
//										ICConfigurationDescription[] libosCfgds = new ICConfigurationDescription[libcfgdList
//												.size()];
//										if (cfgds != null && cfgds.length > 0) {
//											BuildUtilities.saveEditors(null);
//											buildJob = new BuildTarget(libcfgdList.toArray(libosCfgds), 0,
//													IncrementalProjectBuilder.INCREMENTAL_BUILD);
//											buildJob.schedule();
//										}
//									}
//								}
//							}
//
//							setSvnVersion(svnVerFile, svnVersion);
//
//							// if (buildJob != null) {
//							// Job job = buildJob;
//							// Timer t = new Timer(true);
//							// t.schedule(new TimerTask() {
//							//
//							// @Override
//							// public void run() {
//							// // TODO Auto-generated method stub
//							// if (job.getState() == Job.RUNNING) {
//							// System.out.println("----------Job.RUNNING");
//							// } else if (job.getState() == Job.WAITING) {
//							// System.out.println("----------Job.WAITING");
//							// } else if (job.getState() == Job.SLEEPING) {
//							// System.out.println("----------Job.SLEEPING");
//							// } else if (job.getState() == Job.NONE) {
//							// System.out.println("----------Job.NONE");
//							// String errMsg = DideHelper.getFileContent(errorFile);
//							// if (!errMsg.trim().equals("")) {
//							// SendEmail email = new SendEmail();
//							// email.send_Email(errMsg);
//							// t.cancel();
//							// }
//							// }
//							// }
//							// }, 0, 10 * 1000);
//							// // buildJob =
//							// // new BuildTarget(null, 0, 0, true);
//							// // buildJob.schedule();
//							//
//							// }
//						}
//					}
//				}
//
//			}
//
//		}, 0, 2 * 60 * 60 * 1000);
	}

	private long getSvnVersion(File svnVerFile) {
		// TODO Auto-generated method stub
		BufferedReader br = null;
		String line = null;
		if (svnVerFile.exists()) {
			try {
				br = new BufferedReader(new FileReader(svnVerFile));
				while ((line = br.readLine()) != null) {
					// 修改内容核心代码
					if (line.startsWith("SVN_VERION")) {
						String[] infos = line.split("=");
						return Long.parseLong(infos[1].trim());
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
		}
		return 0;
	}

	private void setSvnVersion(File svnVerFile, long version) {
		// TODO Auto-generated method stub
		System.out.println("setSvnVersion");
		BufferedReader br = null;
		String line = null;
		StringBuffer bufAll = new StringBuffer(); // 保存修改过后的所有内容，不断增加
		boolean find = false;
		try {
			br = new BufferedReader(new FileReader(svnVerFile));
			while ((line = br.readLine()) != null) {
				// 修改内容核心代码
				if (line.startsWith("SVN_VERION")) {
					line = "SVN_VERION=" + version;
					find = true;
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
		if (!find) {
			bufAll.append("SVN_VERION=" + version + "\n");
		}
		DideHelper.writeFile(svnVerFile, bufAll.toString(),false);
	}

}
