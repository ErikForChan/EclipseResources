package com.djyos.dide.ui.wizards.djyosProject.tools;

import java.io.File;

public class PathTool {
	
	public static String didePath = new File(System.getProperty("user.dir")).getParentFile().getPath().replace("\\", "/")+ "/";;
			
	public static String resetPath(String path) {
		return path.replace("\\", "/");
	}

	public static String getDIDEPath() {
		return didePath;
	}

	public static String getArchPath() {
		return didePath + "djysrc/bsp/arch";
	}

	public static String getDjyosCookiePath() {
		return didePath + "IDE/djyos/cookies";
	}

	public static String getStepByStepPath() {
		return didePath + "IDE/djyos/cookies/FileTemp/StepByStep";
	}

	/**
	 * ��ȡģ���·��
	 * @return
	 */
	public static String getTemplatePath() {
		return System.getProperty("user.dir") + "/djyos/cookies";
	}

	/**
	 * ��ȡDjysrc��·��
	 * @return
	 */
	public static String getDjyosSrcPath() {
		return didePath + "djysrc";
	}

	/**
	 * ��ȡ�û������·��
	 * @return
	 */
	public static String getUserBoardFilePath() {
		String userBoardPath = didePath + "djysrc/bsp/boarddrv/user";
		File userBoardFile = new File(userBoardPath);
		if (!userBoardFile.exists()) {
			userBoardFile.mkdirs();
		}
		return userBoardPath;
	}

	/**
	 *  ��ȡDjyos����ľ���·��
	 * @return
	 */
	public static String getDemoBoardFilePath() {
		String demoBoardPath = didePath + "djysrc/bsp/boarddrv/demo";
		File demoBoardFile = new File(demoBoardPath);
		if (!demoBoardFile.exists()) {
			demoBoardFile.mkdirs();
		}
		return demoBoardPath;
	}

	/**
	 * ��ȡ�û���������·��
	 * @return
	 */
	public static String getRelativeUserBoardFilePath() {
		return "${DJYOS_SRC_LOCATION}/bsp/boarddrv/user/";
	}

	/**
	 * ��ȡDjyos��������·��
	 * @return
	 */
	public static String getRelativeDemoBoardFilePath() {
		return "${DJYOS_SRC_LOCATION}/bsp/boarddrv/demo/";
	}
}
