package com.djyos.csky.launch;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import com.djyos.csky.launch.PersistentPreferences;

import com.djyos.csky.launch.Activator;

public class DefaultPreferences {
	
	// ------------------------------------------------------------------------

	// Normally loaded from defaults, but also used in
	// DefaultPreferenceInitializer.
	protected static final boolean TAB_MAIN_CHECK_PROGRAM_DEFAULT = false;
	
	// ------------------------------------------------------------------------

	/**
	 * The DefaultScope preference store.
	 */
	private static IEclipsePreferences fgPreferences;
	// ------------------------------------------------------------------------

	public static IEclipsePreferences getPreferences() {

		if (fgPreferences == null) {
			fgPreferences = DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		}

		return fgPreferences;
	}
	
	public static boolean getTabMainCheckProgram() {
		return getBoolean(PersistentPreferences.TAB_MAIN_CHECK_PROGRAM, TAB_MAIN_CHECK_PROGRAM_DEFAULT);
	}

	public static boolean getBoolean(String key, boolean defaultValue) {

		return getPreferences().getBoolean(key, defaultValue);
	}
}
