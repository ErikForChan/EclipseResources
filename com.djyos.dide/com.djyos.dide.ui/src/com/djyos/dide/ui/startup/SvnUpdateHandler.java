package com.djyos.dide.ui.startup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.CommonBuilder;
import org.eclipse.cdt.managedbuilder.internal.core.CommonBuilder.BuildStatus;
import org.eclipse.cdt.managedbuilder.internal.core.CommonBuilder.CfgBuildInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import com.djyos.dide.ui.wizards.djyosProject.tools.DideHelper;

public class SvnUpdateHandler {
	
	private DideHelper dideHelper = new DideHelper();
	final IWorkspace workspace = ResourcesPlugin.getWorkspace();
	IProject[] projects = workspace.getRoot().getProjects();
	
	public void visitSvn() {
		File svnVerFile = new File(dideHelper.getDIDEPath()+"/IDE/configuration/.settings/com.djyos.svnVerson.prefs");
		if(!svnVerFile.exists()) {
			try {
				svnVerFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Timer timer = new Timer(true);
		timer.schedule(new TimerTask() {

			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String url = "https://xiangmuzuserver/svn/硬件组开发库/platform_soft/djyos/trunk";
		        String name = "chenjm@sznari.com";
		        String password = "sunri@2017";
		        long startRevision = 43903;
		        long endRevision = -1; // HEAD (the latest) revision
		 
		        Collection logEntries = null;
		        SVNURL svnurl;
				try {
					svnurl = SVNURL.parseURIEncoded(url);
					DAVRepositoryFactory.setup();
					ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(name,
							password);
					SVNRepository repos = SVNRepositoryFactory.create(svnurl);
					repos.setAuthenticationManager(authManager);
					logEntries = repos.log(new String[] { "" }, null, startRevision, endRevision, true, true);
				} catch (SVNException e) {
					e.printStackTrace();
				}
				
				int entryNum = 0;
				for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
					SVNLogEntry logEntry = (SVNLogEntry) entries.next();
					entryNum++;
					if (entryNum == logEntries.size()) {
						long svnVersion = logEntry.getRevision();
						Long preSvnVersion = getSvnVersion(svnVerFile);
						
						System.out.println("---------------------------------------------");
						System.out.println("revision: " + logEntry.getRevision());
						System.out.println("author: " + logEntry.getAuthor());
						System.out.println("date: " + logEntry.getDate());
						System.out.println("log message: " + logEntry.getMessage());
						
						if (preSvnVersion != svnVersion) {
							String author = logEntry.getAuthor();
							String logMsg = logEntry.getMessage();
							for(IProject p:projects) {
								createBuild(p);
							}
							setSvnVersion(svnVerFile, svnVersion);
						}
					}
				}
		       
			}
			
		}, 0, 10*60*1000);
	}
	
	private long getSvnVersion(File svnVerFile) {
		// TODO Auto-generated method stub
		BufferedReader br = null;  
        String line = null;  
        StringBuffer bufAll = new StringBuffer();  //保存修改过后的所有内容，不断增加         
        if(svnVerFile.exists()) {
        	try {            
                br = new BufferedReader(new FileReader(svnVerFile));              
                while ((line = br.readLine()) != null) {  
                    StringBuffer buf = new StringBuffer();  
                    //修改内容核心代码
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
	
	private void setSvnVersion(File svnVerFile,long version) {
		// TODO Auto-generated method stub
		BufferedReader br = null;  
        String line = null;  
        StringBuffer bufAll = new StringBuffer();  //保存修改过后的所有内容，不断增加         
        try {            
            br = new BufferedReader(new FileReader(svnVerFile));              
            while ((line = br.readLine()) != null) {  
                StringBuffer buf = new StringBuffer();  
                //修改内容核心代码
                if (line.startsWith("SVN_VERION")) {  
                	line = "SVN_VERION="+version;
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
        dideHelper.writeFile(svnVerFile, bufAll.toString());
	}
	
	@SuppressWarnings("restriction")
	public boolean createBuild(IProject project) {
		CommonBuilder cb = new CommonBuilder();
		boolean isClean = false;
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration[] cfgs = info.getManagedProject().getConfigurations();
		File libFile = new File(project.getLocation().toString()+"/lib");
		
		for (IConfiguration cfg : cfgs) {
			IBuilder builder = cfg.getEditableBuilder();
			String cfgName = cfg.getName();
			if (cfgName.startsWith("libos")) {
				CfgBuildInfo binfo = new CfgBuildInfo(builder, true);
				BuildStatus status = new BuildStatus(builder);
				status.setRebuild();
				IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
				final ISchedulingRule rule = ruleFactory.modifyRule(binfo.getProject());
				Job backgroundJob = new Job("Building "+cfgName) {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						// TODO Auto-generated method stub
						try {
							ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
								@Override
								public void run(IProgressMonitor monitor) throws CoreException {
									boolean isClean = cb.build(IncrementalProjectBuilder.FULL_BUILD, binfo, monitor);
								}
							}, rule, IWorkspace.AVOID_UPDATE, monitor);
						} catch (CoreException e) {
							return e.getStatus();
						}
						return Status.OK_STATUS;
					}
				};
				backgroundJob.setRule(rule);
				backgroundJob.schedule();
			}
		}
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
//	 Timer timer=new Timer(30*60*1000,new ActionListener(
//			    public void actionPerformed()
//			    {
//			    }
//			    )).start();
}
