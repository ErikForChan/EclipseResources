package com.djyos.dide.ui.helper;

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;

import com.djyos.dide.ui.enums.ConfigureTarget;
import com.djyos.dide.ui.objects.Component;

public class LinkHelper {

	public static ICLanguageSetting[] getLangSetting(ICResourceDescription rcDes) {
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

	public static void changeIt(int kind, List<ICLanguageSettingEntry> entries, ICLanguageSettingEntry[] ents,
			ICLanguageSetting lang) {
		List<ICLanguageSettingEntry> bak = new ArrayList<ICLanguageSettingEntry>();
		lang.setSettingEntries(kind, bak);
		List<ICLanguageSettingEntry> lsEntries = new ArrayList<ICLanguageSettingEntry>();
		for (int i = 0; i < ents.length; i++) {
			lsEntries.add(ents[i]);
		}
		if (entries != null) {
			for (int i = 0; i < entries.size(); i++) {
				lsEntries.add(entries.get(i));
			}
		}
		setSettingEntries(kind, lsEntries, lang);
	}

	private static void setSettingEntries(int kind, List<ICLanguageSettingEntry> incs, ICLanguageSetting lang) {
		lang.setSettingEntries(kind, incs);// ICLanguageSetting
	}

	// 编译排除
	public static void setFileExclude(IFile ifle, ICConfigurationDescription cfg, boolean exclude) {
		try {
			ICSourceEntry[] newEntries = CDataUtil.setExcluded(ifle.getFullPath(), true, exclude,
					cfg.getSourceEntries());
			cfg.setSourceEntries(newEntries);
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
	}
	
	// include多个文件夹
	public static void setFoldersInclude(List<IFolder> folders, ICConfigurationDescription[] conds) {
		// TODO Auto-generated method stub
		for (int i = 0; i < conds.length; i++) {
			if (conds[i].getName().contains("libos")) {
				for (int k = folders.size() - 1; k >= 0; k--) {
					setFolderExclude(folders.get(k), conds[i], false);
				}
			}
		}
	}
	
	//排除工程中libos选项中对folder的编译
	public static void setProjectFolderExclude(IFolder ifolder, ICConfigurationDescription[] conds, boolean exclude) {
		for (int j = 0; j < conds.length; j++) {
			if(conds[j].getName().startsWith("libos")) {
				setFolderExclude(ifolder, conds[j], exclude);
			}
		}
	}

	//排除文件夹的编译
	public static void setFolderExclude(IFolder ifle, ICConfigurationDescription cfg, boolean exclude) {
		try {
			ICSourceEntry[] newEntries = CDataUtil.setExcluded(ifle.getFullPath(), (ifle instanceof IFolder), exclude,
					cfg.getSourceEntries());
			cfg.setSourceEntries(newEntries);
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
	}

	public static void fillSymbols(List<Component> compontentsChecked, List<ICLanguageSettingEntry> entries, List<ICLanguageSettingEntry> entriesMACROExist) {
		// TODO Auto-generated method stub
		for (int j = 0; j < compontentsChecked.size(); j++) {
			Component component = compontentsChecked.get(j);
			for(String symbol:component.getSymbols()) {
				if(symbol.contains("*")) {
					String[] sbs = symbol.split("\\*");
					ICLanguageSettingEntry entry = CDataUtil.createCMacroEntry(sbs[0],sbs[1], 0);
					entries.add(entry);
				}else {
					ICLanguageSettingEntry entry = CDataUtil.createCMacroEntry(symbol,"", 0);
					entries.add(entry);
				}
			}
			if (component.getTarget().equals(ConfigureTarget.CMDLINE.getName())) {
				// System.out.println("CMDLINE component: "+component.getName());
				String[] parametersDefined = component.getConfigure().split("\n");
				for (int i = 0; i < parametersDefined.length; i++) {
					String parameter = parametersDefined[i];
					if (parameter.contains("#define")) {
						String[] defines = parameter.trim().split("//");
						if (!parameter.trim().startsWith("//")) {
							String[] members = defines[0].trim().split("\\s+");
							ICLanguageSettingEntry entry = CDataUtil.createCMacroEntry(members[1],
									members.length > 2 ? members[2] : "", 0);
							entries.add(entry);
						}
						else {
							for (ICLanguageSettingEntry macro : entriesMACROExist) {
								if (parameter.contains(macro.getName())) {
									String[] members = defines[1].trim().split("\\s+");
									ICLanguageSettingEntry entry = CDataUtil.createCMacroEntry(members[1],
											members.length > 2 ? members[2] : "", 0);
									entries.add(entry);
									break;
								}
							}
						}
					}
				}
			}
		}
	}

	public static void getFolders(IFolder ifolder, List<IFolder> folders) {
		folders.add(ifolder);
		IFolder parentFolder = (IFolder) ifolder.getParent();
		if (!parentFolder.getName().equals("libos")) {
			getFolders(parentFolder, folders);
		}
	}

}
