package com.djyos.dide.ui.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.djyos.dide.shell.KeepShell;
import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.helper.LinkHelper;
import com.djyos.dide.ui.helper.ShellHelper;
import com.djyos.dide.ui.objects.CmpntCheck;
import com.djyos.dide.ui.objects.Component;
import com.djyos.dide.ui.startup.HandleProjectImport;
import com.djyos.dide.ui.wizards.component.ComponentHelper;
import com.djyos.dide.ui.wizards.component.ReadComponent;
import com.djyos.dide.ui.wizards.component.ReadComponentCheckXml;
import com.djyos.dide.ui.wizards.djyosProject.ReadHardWareDesc;

public class FileHandler implements IResourceChangeListener {
	
	final IWorkspace workspace = ResourcesPlugin.getWorkspace();
	IProject[] projects = workspace.getRoot().getProjects();
	List<String> projectsExists = getProjectsExisted();

	
	private List<String> getProjectsExisted(){
		List<String> projectsExists =  new ArrayList<String>();
		for (IProject p : projects) {
			projectsExists.add(p.getName());
		}
		return projectsExists;
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		// TODO Auto-generated method stub
		try {
			event.getDelta().accept(new IResourceDeltaVisitor() {
				@Override
				public boolean visit(IResourceDelta delta) throws CoreException {
					IResource resource = delta.getResource();
					if (resource instanceof IFile) {
						switch (delta.getKind()) {
						
						case IResourceDelta.ADDED:
							// handle added resource
							IProject project = resource.getProject();
							File stup_complie_file = new File(DideHelper.getDIDEPath() + "complieAuto.txt");
							File hardWardInfoFile = new File(
									project.getLocation().toString() + "/data/hardware_info.xml");
							
							if (stup_complie_file.exists()) {
								//自动编译库
								BuildOsAuto(resource,project,stup_complie_file);
							}

							if (resource.getName().equals(".project")) {
								//导入工程时，设置默认命令，对工程进行排除编译的工作
//								projectsExists.add(resource.getProject().getName());
								ExcludeWhenImport(resource,project,hardWardInfoFile);
							}
							
							if (resource.getName().endsWith(".a") && resource.getName().startsWith("libos")) {
//								System.out.println("event.getType():  "+event.getType()+"  "+event.getBuildKind()
//												+" "+delta.getFlags());
//								Analysis_aFile(resource);
							}
							
							//用户新增的文件，除了djyos、component、当前板件、当前CPU、当前arch，APP 。都排除编译
							ExcludeNewFile(resource,project,hardWardInfoFile);
							
							break;
						case IResourceDelta.REMOVED:
							// handle removed resource
							if (resource.getName().equals(".project")) {
								projectsExists.remove(resource.getProject().getName());
							}
							break;
						case IResourceDelta.CHANGED:
							// handle changed resource
							break;
						}
					}
					return true;
				}
			});
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void Analysis_aFile(IResource resource) {
		// TODO Auto-generated method stub
		File libos_file = resource.getLocation().toFile();
		List<File> o_files = new ArrayList<File>();
//		File os_file = ShellHelper.release_a_to_os(libos_file);
//		File[] o_files = os_file.listFiles();
		File libos_folder = new File(libos_file.getParentFile().getPath()+"/src/libos");
		long startTime=System.currentTimeMillis();   //获取开始时间
		ShellHelper.get_src_ofiles(libos_folder, o_files);
		long endTime=System.currentTimeMillis(); //获取结束时间
		System.out.println("获取所有.o的程序运行时间： "+(endTime-startTime)+"  ms");
		List<String> symbols = new ArrayList<String>();
		System.out.println("o_files.size()： "+o_files.size());
		
		if(o_files.size() < 1) {
			DideHelper.printToConsole("当前编译选项的src目录下不存在.0", true);
		}else {
			DideHelper.printToConsole("正在分析"+resource.getName()+"...请稍后，可查看右下方进度条", true);
			Job backgroundJob = new Job("正在分析"+resource.getName()) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					long startTime=System.currentTimeMillis();   //获取开始时间
					monitor.beginTask("正在分析"+resource.getName(), o_files.size() + 1);
					monitor.worked(1);
					for(File f:o_files) {
						Map<String, String> map = DideHelper.get_o_symbol(f);
						String symbol = map.get("symbol");
						if(symbol != null) {
							symbols.add(symbol);
							System.out.println("symbol： "+symbol);
						}
						monitor.worked(1);
					}
					DideHelper.printToConsole("分析libos_Iboot.a结束", true);
					boolean isApp = libos_file.getParentFile().getName().contains("App") ? true
							: false;
					KeepShell.create_keepshell(isApp, resource.getProject(), symbols);
					
					long endTime=System.currentTimeMillis(); //获取结束时间
					System.out.println("分析所有.o的程序运行时间： "+(endTime-startTime)+"  ms");
					return Status.CANCEL_STATUS;
				}
			};
			backgroundJob.schedule();
			DideHelper.refresh_workspace();
		}
	}

	protected void BuildOsAuto(IResource resource, IProject project, File stup_complie_file) {
		// TODO Auto-generated method stub
		if (resource.getName().endsWith(".a") && resource.getName().startsWith("libos")) {
			IResource pResource = resource.getParent();
			String libCfgName = pResource.getName();
			System.out.println("libCfgName: "+libCfgName);
			String commonName = libCfgName.replace("libos", "");
			String targetName = project.getName() + commonName;
			DideHelper.buildTarget(project, targetName);
		}
	}

	protected void ExcludeWhenImport(IResource resource, IProject project, File hardWardInfoFile) {
		// TODO Auto-generated method stub
		if (hardWardInfoFile.exists()) {

			ConfigurationHandler cfgHandler = new ConfigurationHandler();
			cfgHandler.setDefaultArchiverCmd(project);

			HandleProjectImport projectListener = new HandleProjectImport();
			projectListener.handleProjectElemExculde(project);
		}
	}

	protected void ExcludeNewFile(IResource resource, IProject project, File hardWardInfoFile) {
		// TODO Auto-generated method stub
		if (resource.getName().endsWith(".c") && projectsExists.contains(project.getName())) {
			final ICProjectDescription local_prjd = CoreModel.getDefault().getProjectDescription(project);
			if (local_prjd != null && hardWardInfoFile.exists()) {
				File appCheckFile = new File(project.getLocation().toString() + "/data/app_component_check.xml");
				File ibootCheckFile = new File(project.getLocation().toString() + "/data/iboot_component_check.xml");
				if (appCheckFile.exists()) {
					Handle_Exclude(project,resource,hardWardInfoFile,local_prjd,true);
				}
				if (ibootCheckFile.exists()) {
					Handle_Exclude(project,resource,hardWardInfoFile,local_prjd,false);
				}
				
				DideHelper.saveProjectDescription(project, local_prjd);
			}
		}
	}

	private void Handle_Exclude(IProject project, IResource resource, File hardWardInfoFile, ICProjectDescription local_prjd, boolean isApp) {
		// TODO Auto-generated method stub
		File check_file = new File(project.getLocation().toString() + "/data/"+(isApp?"app":"iboot")+"_component_check.xml");
		ICConfigurationDescription[] conds = local_prjd.getConfigurations();
		String relativePath = resource.getProjectRelativePath().toString();
		IFile ifile = project.getFile(relativePath);
		List<String> hardwares = ReadHardWareDesc.getHardWares(hardWardInfoFile);
		String boardName = hardwares.get(0);
		String cpuName = hardwares.get(1);
		boolean toExclude = false;
		
		List<CmpntCheck> compts_checks = ReadComponentCheckXml.getCmpntChecks(check_file);
		List<Component> compt_object_checks = new ArrayList<Component>();
		List<Component> compontents = ReadComponent.getAllComponents(DideHelper.getCpuByName(cpuName), DideHelper.getBoardByName(boardName));
		for(CmpntCheck cc:compts_checks) {
			if(cc.isChecked().equals("true")) {
				Component c = ComponentHelper.getComponentByName(cc.getCmpntName(), compontents);
				compt_object_checks.add(c);
			}
		}
		
		for(Component c:compt_object_checks) {
			if(relativePath.contains(c.getFileName())) {
				if(relativePath.contains("libos") && (relativePath.contains("bsp") || relativePath.contains("third"))) {
					if(relativePath.contains("cpudrv") && relativePath.contains("src")) {
						toExclude = false;
					}else if(relativePath.contains(boardName) && relativePath.contains("drv")) {
						toExclude = false;						
					}else {
						toExclude = true;		
					}
				}
				
				if(toExclude) {
//					System.out.println("relativePath： "+relativePath);
					String libos_flag = isApp?"libos_App":"libos_Iboot";
					for (int j = 0; j < conds.length; j++) {
						if (conds[j].getName().startsWith(libos_flag)) {
							File curFile = ifile.getLocation().toFile();
							IFolder foler = null;
							if(curFile.isDirectory()) {
								foler = project.getFolder(relativePath);
							}else {
								String path =curFile.getParentFile().getPath().substring(project.getLocation().toString().length() + 1);
								foler = project.getFolder(path);
							}
							LinkHelper.setFolderExclude(foler, conds[j], true);
						}
					}
				}
				break;
			}
		}
	}

}
