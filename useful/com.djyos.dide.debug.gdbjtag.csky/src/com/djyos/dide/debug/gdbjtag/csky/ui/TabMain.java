package com.djyos.dide.debug.gdbjtag.csky.ui;

import com.djyos.dide.debug.gdbjtag.csky.ConfigurationAttributes;
import com.djyos.dide.debug.gdbjtag.csky.DefaultPreferences;
import com.djyos.dide.debug.gdbjtag.csky.PersistentPreferences;

import org.eclipse.cdt.dsf.gdb.internal.ui.launching.CMainTab;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.widgets.Text;

/**
 * @since 7.0
 */
@SuppressWarnings("restriction")
public class TabMain extends CMainTab {

	public TabMain() {
		super((DefaultPreferences.getTabMainCheckProgram() ? 0 : CMainTab.DONT_CHECK_PROGRAM)
				| CMainTab.INCLUDE_BUILD_SETTINGS);
	}

}
