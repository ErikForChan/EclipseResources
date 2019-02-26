package com.djyos.dide.shell;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IProject;

import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.wizards.djyosProject.DjyosMessages;

public class KeepShell {
	
	public static void create_keepshell(boolean isApp,IProject project,List<String> symbols) {
		
		String defineInit = DjyosMessages.Automatically_Generated;
		String fun_name = "void keep_shell(void)\n";
		String first_code = "\tvolatile void *keep;\n\n";
		String all_keeps = "";
		File ks_file = project.getFile("src/"+(isApp?"app":"iboot")+"/OS_prjcfg/keepshell.c").getLocation().toFile();
		DideHelper.createNewFile(ks_file);
		for (String s : symbols) {
			all_keeps += "\textern void *"+s+";\n";
			all_keeps += "\tkeep = (void *)"+s+";\n\n";;
		}
		String content = defineInit + fun_name + "{\n\n" + first_code + all_keeps + "}";
		DideHelper.writeFile(ks_file, content, false);
		
	}
	
}
