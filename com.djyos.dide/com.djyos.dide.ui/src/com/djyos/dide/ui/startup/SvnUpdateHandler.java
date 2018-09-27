package com.djyos.dide.ui.startup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
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
import com.djyos.dide.ui.wizards.djyosProject.tools.SendEmail;
import com.ibm.icu.text.SimpleDateFormat;

public class SvnUpdateHandler {
	
	private DideHelper dideHelper = new DideHelper();
	final IWorkspace workspace = ResourcesPlugin.getWorkspace();
	IProject[] projects = workspace.getRoot().getProjects();
	
//	public void buildOnTime() {
//		new Timer().schedule(new TimerTask() {
//	        @Override
//	        public void run() {
//	        	visitSvn();
//	        }
//	    }, 0, 10 * 60 * 1000);
//	}
	
	public void visitSvn() {
		File svnVerFile = new File(dideHelper.getDIDEPath()+"IDE/configuration/.settings/com.djyos.svnVerson.prefs");
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
//				Date day=new Date();    
//
//				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
//
//				System.out.println(df.format(day)+"\n");   
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
						Long preSvnVersion = (long) 0;
//						Long preSvnVersion = getSvnVersion(svnVerFile);
						System.out.println("---------------------------------------------");
						System.out.println("revision: " + logEntry.getRevision());
						System.out.println("author: " + logEntry.getAuthor());
						System.out.println("date: " + logEntry.getDate());
						System.out.println("log message: " + logEntry.getMessage());
						
						if (preSvnVersion != svnVersion) {
							File errorFile = new File(dideHelper.getDIDEPath()+"IDE/configuration/errorResult.txt");
							if (!errorFile.exists()) {
								try {
									errorFile.createNewFile();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							String author = logEntry.getAuthor();
							String logMsg = logEntry.getMessage();
							int complieNum = 0;
							for(IProject p:projects) {
								createBuild(p);
//								IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(p);
//								IConfiguration[] cfgs = info.getManagedProject().getConfigurations();
//								for (IConfiguration cfg : cfgs) {
//									complieNum++;
//								}
							}
//							int count = complieNum;
//							IRunnableWithProgress runnable = new IRunnableWithProgress() {
//								@Override
//								public void run(IProgressMonitor monitor)
//										throws InvocationTargetException, InterruptedException {
//									monitor.beginTask("自动编译工程", count);	
//									for(IProject p:projects) {
//										System.out.println("p.getName(): " + p.getName());
//										createBuild(p,monitor);
//									}
//								}
//							};
//							
//							PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
//								public void run() {
//									IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
//									try {
//										ProgressMonitorDialog dialog = new ProgressMonitorDialog(window.getShell());
//										dialog.setCancelable(false);
//										dialog.run(true, true, runnable);
//									} catch (InvocationTargetException e) {
//										// TODO Auto-generated catch block
//										e.printStackTrace();
//									} catch (InterruptedException e) {
//										// TODO Auto-generated catch block
//										e.printStackTrace();
//									}
//							}
//							});
							setSvnVersion(svnVerFile, svnVersion);
							String errMsg = getFileContent(errorFile);
//							if(!errMsg.trim().equals("")) {
//								SendEmail email = new SendEmail();
//								email.send(errMsg);
//							}
						}
					}
				}
		       
			}
			
		}, 0, 10*60*1000);
	}
	
	private static String getFileContent(File file) {
		// TODO Auto-generated method stub
		BufferedReader br = null;  
        String line = null;  
        StringBuffer bufAll = new StringBuffer();  //保存修改过后的所有内容，不断增加         
        if(file.exists()) {
        	try {            
                br = new BufferedReader(new FileReader(file));              
                while ((line = br.readLine()) != null) {  
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
        }
		return bufAll.toString();
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
		System.out.println("setSvnVersion");
		BufferedReader br = null;  
        String line = null;  
        StringBuffer bufAll = new StringBuffer();  //保存修改过后的所有内容，不断增加
        boolean find = false;
        try {            
            br = new BufferedReader(new FileReader(svnVerFile));              
            while ((line = br.readLine()) != null) {  
                StringBuffer buf = new StringBuffer();  
                //修改内容核心代码
                if (line.startsWith("SVN_VERION")) {  
                	line = "SVN_VERION="+version;
                	find = true;
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
        if(!find) {
        	 bufAll.append("SVN_VERION="+version+"\n");           
        }
        dideHelper.writeFile(svnVerFile, bufAll.toString());
	}
	
	@SuppressWarnings("restriction")
	public boolean createBuild(IProject project) {
		File libFile = new File(project.getLocation().toString()+"/lib");
		
		build_project(project,"libos");

//			monitor.worked(1);
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	private void build_project(IProject project, String target) {
		// TODO Auto-generated method stub
		
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration[] cfgs = info.getManagedProject().getConfigurations();
		for (IConfiguration cfg : cfgs) {
			CommonBuilder cb = new CommonBuilder();
			IBuilder builder = cfg.getEditableBuilder();
			String cfgName = cfg.getName();
//			monitor.setTaskName("编译工程"+project.getName()+"-->"+cfgName+"...");
			if (cfgName.startsWith("target")) {
				CfgBuildInfo binfo = new CfgBuildInfo(builder, true);
//				BuildStatus status = new BuildStatus(builder);
//				status.setRebuild();
//				try {
//					cb.build(IncrementalProjectBuilder.FULL_BUILD, binfo, monitor);
//				} catch (CoreException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
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
	}
	
}
