package org.eclipse.cdt.ui.wizards.djyosProject.tools;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.component.Component;
import org.eclipse.cdt.ui.wizards.component.ConfigureTarget;

public class LinkHelper {
	
	public ICLanguageSetting[] getLangSetting(ICResourceDescription rcDes) {
		switch (rcDes.getType()) {
		case ICSettingBase.SETTING_PROJECT:
		case ICSettingBase.SETTING_CONFIGURATION:
		case ICSettingBase.SETTING_FOLDER:
			ICFolderDescription foDes = (ICFolderDescription) rcDes;
			return foDes.getLanguageSettings();
		case ICSettingBase.SETTING_FILE:
			ICFileDescription fiDes = (ICFileDescription) rcDes;
			ICLanguageSetting langSetting = fiDes.getLanguageSetting();
			return (langSetting != null) ? new ICLanguageSetting[] { langSetting } : null;
		}
		return null;
	}
	
	public void changeIt(int kind,List<ICLanguageSettingEntry>  entries, ICLanguageSettingEntry[] ents, ICLanguageSetting lang) {
		List<ICLanguageSettingEntry> lsEntries = new ArrayList<ICLanguageSettingEntry>();
		for(int i=0;i<ents.length;i++) {
			lsEntries.add(ents[i]);
		}
		if (entries != null) {
			for(int i=0;i<entries.size();i++) {
				lsEntries.add(entries.get(i));
			}
		}
		setSettingEntries(kind, lsEntries, lang);
	}
	
	private void setSettingEntries(int kind, List<ICLanguageSettingEntry> incs, ICLanguageSetting lang) {
		lang.setSettingEntries(kind, incs);// ICLanguageSetting
	}
	
	//�����ų�
	public void setFileExclude(IFile ifle, ICConfigurationDescription cfg, boolean exclude) {
		try {
			ICSourceEntry[] newEntries = CDataUtil.setExcluded(ifle.getFullPath(), true, exclude, cfg.getSourceEntries());
			cfg.setSourceEntries(newEntries);
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
	}
	
	public void setExclude(IFolder ifle, ICConfigurationDescription cfg, boolean exclude) {
		try {
			ICSourceEntry[] newEntries = CDataUtil.setExcluded(ifle.getFullPath(), (ifle instanceof IFolder), exclude, cfg.getSourceEntries());
			cfg.setSourceEntries(newEntries);
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
	}
	
	public void fillSymbols(List<Component> compontentsChecked, List<ICLanguageSettingEntry> entries) {
		// TODO Auto-generated method stub
		for(int j=0;j<compontentsChecked.size();j++) {
			Component component = compontentsChecked.get(j);
			if(component.getTarget().equals(ConfigureTarget.CMDLINE.getName())) {
				String[] parametersDefined = component.getConfigure().split("\n");
				for (int i = 0; i < parametersDefined.length; i++) {
					if (parametersDefined[i].contains("#define")) {
						String parameter = parametersDefined[i];
						if(!parameter.startsWith("//")) {
							String[] defines = parameter.trim().split("//");
							String[] members = defines[0].trim().split("\\s+");
							ICLanguageSettingEntry entry = CDataUtil.createCMacroEntry(members[1], members.length>2?members[2]:"", 0);
							entries.add(entry);
						}
					}
				}
			}
		}
	}
	
	public void getFolders(IFolder ifolder,List<IFolder> folders){
		folders.add(ifolder);
		IFolder parentFolder = (IFolder) ifolder.getParent();
		if(!parentFolder.getName().equals("libos")) {
			getFolders(parentFolder,folders);
		}
	}
	
}
