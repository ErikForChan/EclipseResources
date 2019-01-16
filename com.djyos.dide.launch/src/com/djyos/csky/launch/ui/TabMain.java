package com.djyos.csky.launch.ui;

import com.djyos.csky.launch.DefaultPreferences;

import org.eclipse.cdt.launch.ui.CMainTab;

@SuppressWarnings("restriction")
public class TabMain extends CMainTab {

	/**
	 * If the preference is set to true, check program and disable Debug button
	 * if not found. The default GDB Hardware Debug plug-in behaviour is to do
	 * not check program, to allow project-less debug sessions.
	 */
	public TabMain() {
		super((DefaultPreferences.getTabMainCheckProgram() ? 0 : CMainTab.DONT_CHECK_PROGRAM)
				| CMainTab.INCLUDE_BUILD_SETTINGS);
	}
}
