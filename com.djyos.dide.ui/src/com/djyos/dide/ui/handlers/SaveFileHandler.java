package com.djyos.dide.ui.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.handlers.SaveHandler;

import com.djyos.dide.ui.wizards.component.CmpntCheck;
import com.djyos.dide.ui.wizards.component.ReadCheckXml;
import com.djyos.dide.ui.wizards.djyosProject.tools.DideHelper;
import com.djyos.dide.ui.wizards.djyosProject.tools.LinkHelper;

public class SaveFileHandler extends SaveHandler{

	List<String> allStrings = new ArrayList<String>();
	private String didePath = new DideHelper().getDIDEPath();
	LinkHelper linkHelper = new LinkHelper();
	String srcLocation = didePath+"djysrc";
	private String compName;
	
	@Override
	public Object execute(ExecutionEvent event) {
		// TODO Auto-generated method stub
		super.execute(event);
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if(activePart.getTitleToolTip().contains("libos")) {
			String infos[] = activePart.getTitleToolTip().split("libos");
			String filePath  = srcLocation + infos[1];
			File file = new File(filePath);
			if(isComponent(file) && filePath.contains("third")) {
				List<String> excludeStrings = new ArrayList<String>();
				List<String> includeStrings = new ArrayList<String>();
				getInExclude(excludeStrings,includeStrings);
				travelProjects(file,excludeStrings,includeStrings);
			}
		}
		return null;
	}

	protected void travelProjects(File file, List<String> excludeStrings, List<String> includeStrings) {
		// TODO Auto-generated method stub
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject[] projects = workspace.getRoot().getProjects();
		for(IProject project:projects) {
			List<CmpntCheck> appCmpntChecks = null;
			List<CmpntCheck> ibootCmpntChecks = null;
			final ICProjectDescription local_prjd =  CoreModel.getDefault().getProjectDescription(project);
			ICConfigurationDescription[] conds = local_prjd.getConfigurations();	//获取工程的所有Configuration		
			File appCheckFile = new File(project.getLocation().toString() + "/data/app_component_check.xml");
			File ibootCheckFile = new File(project.getLocation().toString() + "/data/iboot_component_check.xml");
			
			if (appCheckFile.exists()) {
				try {
					ReadCheckXml rccx = new ReadCheckXml();
					appCmpntChecks = rccx.getCmpntChecks(appCheckFile);
					handleProject(project,appCmpntChecks,file,true,conds,excludeStrings,includeStrings);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
			if (ibootCheckFile.exists()) {
				try {
					ReadCheckXml rccx = new ReadCheckXml();
					ibootCmpntChecks = rccx.getCmpntChecks(ibootCheckFile);
					handleProject(project,ibootCmpntChecks,file,false,conds,excludeStrings,includeStrings);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
			try {
				CoreModel.getDefault().setProjectDescription(project, local_prjd);
			} catch (CoreException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	private void handleProject(IProject project, List<CmpntCheck> appCmpntChecks, File file, boolean isApp, ICConfigurationDescription[] conds, List<String> excludeStrings, List<String> includeStrings) {
		// TODO Auto-generated method stub
		String relativePath = file.getParentFile().getPath().replace("\\", "/").replace(srcLocation, "");
		for(CmpntCheck cc:appCmpntChecks) {
			if(cc.getCmpntName().equals(compName)) {
				if(cc.isChecked().equals("true")) {
					notExcludeFiles(project,file.getParentFile(),isApp,conds);
					System.out.println("excludeStrings.size():  "+excludeStrings.size());
					excludeFiles(project,relativePath,isApp,conds,excludeStrings);
					includePaths(project,relativePath,isApp,conds,includeStrings);
				}
				break;
			}
		}
	}

	private void includePaths(IProject project, String relativePath, boolean isApp, ICConfigurationDescription[] conds, List<String> includeStrings) {
		// TODO Auto-generated method stub
		List<String> myLinks = new ArrayList<String>();
		for(String str:includeStrings) {
			myLinks.add("${DJYOS_SRC_LOCATION}"+relativePath+str);
		}
		List<ICLanguageSettingEntry>  entries = new ArrayList<ICLanguageSettingEntry>();
		for(int k=0;k<myLinks.size();k++) {
			ICLanguageSettingEntry entry = CDataUtil.createCIncludePathEntry(myLinks.get(k), 0);
			entries.add(entry);
		}
		List<ICLanguageSettingEntry>  assemblyEntries = new ArrayList<ICLanguageSettingEntry>();
		for(int k=0;k<myLinks.size();k++) {
			if(myLinks.get(k).endsWith("include")) {
				ICLanguageSettingEntry entry = CDataUtil.createCIncludePathEntry(myLinks.get(k), 0);
				assemblyEntries.add(entry);
			}
		}
		
		for(ICConfigurationDescription cond:conds) {
			ICResourceDescription rds = cond.getRootFolderDescription();
			ICLanguageSetting[] languageSettings = linkHelper.getLangSetting(rds);
			for (int j=0; j<languageSettings.length; j++) {
				ICLanguageSetting lang = languageSettings[j];//获取语言类型	
				//Assembly添加链接
				if(j==0) {	
					linkHelper.changeIt(ICSettingEntry.INCLUDE_PATH,assemblyEntries,lang.getSettingEntries(ICSettingEntry.INCLUDE_PATH),lang);
				}
				//GNU C/C++ 添加链接
				else {
					linkHelper.changeIt(ICSettingEntry.INCLUDE_PATH,entries,lang.getSettingEntries(ICSettingEntry.INCLUDE_PATH),lang);
				}			
			}
		}
	}

	private void excludeFiles(IProject project, String relativePath, boolean isApp,
			ICConfigurationDescription[] conds, List<String> excludeStrings) {
		// TODO Auto-generated method stub
		for(String str:excludeStrings) {
			IFile ifile = project.getFile("src/libos"+relativePath+str);
			for(ICConfigurationDescription cond:conds) {
				if(isApp) {
					if(cond.getName().contains("libos_App")) {
						linkHelper.setFileExclude(ifile, cond, true);
					}
				}else {
					if (cond.getName().contains("libos_Iboot")) {
						linkHelper.setFileExclude(ifile, cond, true);
					}
				}
			}
		}
	}

	private void notExcludeFiles(IProject project, File parentFile, boolean isApp, ICConfigurationDescription[] conds) {
		// TODO Auto-generated method stub
		String srcLocation = didePath+"djysrc";
		File[] files = parentFile.listFiles();
		for(File file:files) {
			String relativePath = file.getPath().replace("\\", "/").replace(srcLocation, "");
			IFile ifile = project.getFile("src/libos"+relativePath);
			IFolder ifolder = project.getFolder("src/libos"+relativePath);
			for(ICConfigurationDescription cond:conds) {
				if(isApp) {
					if(cond.getName().contains("libos_App")) {
						if(file.isFile()) {
							linkHelper.setFileExclude(ifile, cond, false);
						}else if(file.isDirectory()){
							linkHelper.setExclude(ifolder, cond, false);
						}
					}
				}else {
					if (cond.getName().contains("libos_Iboot")) {
						if(file.isFile()) {
							linkHelper.setFileExclude(ifile, cond, false);
						}else if(file.isDirectory()){
							linkHelper.setExclude(ifolder, cond, false);
						}
					}
				}
			}
			if(file.isDirectory()) {
				notExcludeFiles(project,file,isApp,conds);
			}
		}
	}

	protected void getInExclude(List<String> excludeStrings, List<String> includeStrings) {
		// TODO Auto-generated method stub
		int eStart = 0,eStop = 0,nStart = 0,nStop = 0;
		if(allStrings.size() > 0) {
			for(int i=0;i<allStrings.size();i++) {
				if(allStrings.get(i).contains("%$#@exclude")){
					eStart = i;
				}else if(allStrings.get(i).contains("%$#@end exclude")){
					eStop = i;
				}else if(allStrings.get(i).contains("%$#@include path")){
					nStart = i;
				}else if(allStrings.get(i).contains("%$#@end include path")){
					nStop = i;
				}
			}
			
			for(int i=eStart+1;i<eStop;i++) {
				String pureString = allStrings.get(i).replaceFirst("//", "");
				if(!pureString.trim().equals("")) {
					String[] curExcludes = pureString.split(";");
					for(String exclude:curExcludes) {
						System.out.println("exclude:  "+exclude);
						excludeStrings.add(exclude.replace("..", "").replace("\\", "/"));
					}
				}
			}
			for(int i=nStart+1;i<nStop;i++) {
				String[] curIncludes = allStrings.get(i).replaceFirst("//", "").split(";");
				for(String include:curIncludes) {
					System.out.println("include:  "+include);
					includeStrings.add(include.replace("..", "").replace("\\", "/"));
				}
			}
			
		}
	}

	protected boolean isComponent(File file) {
		// TODO Auto-generated method stub
		FileReader reader = null;
		try {
			reader = new FileReader(file.getPath());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader br = new BufferedReader(reader);
		String str = null;
		boolean start = false, stop = false;
		try {
			while ((str = br.readLine()) != null) {
				if (str.contains("@#$%component configure")) {
					start = true;
				}
				if (str.contains("component name")) {
					String[] menbers = str.split("\"");
					compName = menbers[1];
				}
				if (start && !stop) {
					allStrings.add(str);
				}
				if (str.contains("@#$%component end configure")) {
					stop = true;
					break;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (allStrings.size() > 0) {
			return true;
		}

		return false;
	}
	
}
