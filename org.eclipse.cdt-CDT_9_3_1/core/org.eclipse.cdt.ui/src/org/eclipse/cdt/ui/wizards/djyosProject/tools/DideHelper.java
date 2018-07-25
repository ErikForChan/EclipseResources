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
	
	public String getTemplatePath() {
		return fullPath.substring(6)+"djyosTemplate";
	}
	
	public String getDjyosSrcPath() {
		return didePath+"djysrc";
	}
	
	public String getUserBoardFilePath() {
		return didePath+"djysrc\\bsp\\boarddrv\\user";
	}
	
	public String getDemoBoardFilePath() {
		return didePath+"djysrc\\bsp\\boarddrv\\demo";
	}
	
	public String getRelativeUserBoardFilePath() {
		return "${DJYOS_SRC_LOCATION}/bsp/boarddrv/user/";
	}
	
	public String getRelativeDemoBoardFilePath() {
		return "${DJYOS_SRC_LOCATION}/bsp/boarddrv/demo/";
	}
	
}
