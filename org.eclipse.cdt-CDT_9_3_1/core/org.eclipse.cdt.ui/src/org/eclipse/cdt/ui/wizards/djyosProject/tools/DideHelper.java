package org.eclipse.cdt.ui.wizards.djyosProject.tools;

import org.eclipse.core.runtime.Platform;

public class DideHelper {
	String fullPath = Platform.getInstallLocation().getURL().toString();
	String didePath = fullPath.substring(6,(fullPath.substring(0,fullPath.length()-1)).lastIndexOf("/")+1);
	
	/*
	 * ��ȡ��ǰEclipse��·��
	 */
	public String getDIDEPath() {
		return didePath;
	}
	
}
