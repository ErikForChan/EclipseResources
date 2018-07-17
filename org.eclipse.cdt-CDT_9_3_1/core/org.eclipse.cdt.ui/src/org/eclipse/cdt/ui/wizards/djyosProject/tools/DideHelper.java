package org.eclipse.cdt.ui.wizards.djyosProject.tools;

import org.eclipse.core.runtime.Platform;

public class DideHelper {
	String fullPath = Platform.getInstallLocation().getURL().toString();
	String didePath = fullPath.substring(6,(fullPath.substring(0,fullPath.length()-1)).lastIndexOf("/")+1);
	
	/*
	 * 获取当前Eclipse的路径
	 */
	public String getDIDEPath() {
		return didePath;
	}
	
}
