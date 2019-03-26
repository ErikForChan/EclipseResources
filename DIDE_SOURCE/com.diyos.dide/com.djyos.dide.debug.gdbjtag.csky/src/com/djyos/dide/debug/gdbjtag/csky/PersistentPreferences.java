package com.djyos.dide.debug.gdbjtag.csky;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class PersistentPreferences {

	// ------------------------------------------------------------------------

	// Tab Debugger
	public static final String GDB_SERVER = "gdb.server.";

	public static final String GDB_SERVER_DO_START = GDB_SERVER + "doStart";

	public static final String GDB_SERVER_EXECUTABLE = GDB_SERVER + "executable";

	public static final String GDB_SERVER_OTHER_OPTIONS = GDB_SERVER + "other";

	// GDB Client Setup
	public static final String GDB_CLIENT = "gdb.client.";

	public static final String GDB_CLIENT_EXECUTABLE = GDB_CLIENT + "executable";

	public static final String GDB_CLIENT_OTHER_OPTIONS = GDB_CLIENT + "other";

	public static final String GDB_CLIENT_COMMANDS = GDB_CLIENT + "commands";

	// Tab Startup
	public static final String GDB_CSKY = "gdb.csky.";

	public static final String GDB_CSKY_DO_INITIAL_RESET = GDB_CSKY + "doInitialReset";

	public static final String GDB_CSKY_INIT_OTHER = GDB_CSKY + "init.other";

	public static final String GDB_CSKY_ENABLE_SEMIHOSTING = GDB_CSKY + "enableSemihosting";

	public static final String GDB_CSKY_BOARD_NAME = GDB_CSKY + "boardName";

	public static final String GDB_CSKY_MACHINE_NAME = GDB_CSKY + "machineName";

	public static final String GDB_CSKY_DEVICE_NAME = GDB_CSKY + "deviceName";

	private static final String GDB_CSKY_Use_DDC = GDB_CSKY + "useDDC";

	public static final String GDB_CSKY_DISABLE_GRAPHICS = GDB_CSKY + "disableGraphics";

	// Run Commands
	public static final String GDB_CSKY_DO_DEBUG_IN_RAM = GDB_CSKY + "doDebugInRam";

	public static final String GDB_CSKY_DO_PRERUN_RESET = GDB_CSKY + "doPreRunReset";

	public static final String GDB_CSKY_PRERUN_OTHER = GDB_CSKY + "preRun.other";

	// ----- Defaults ---------------------------------------------------------

	public static final String EXECUTABLE_NAME = "executable.name";
	public static final String EXECUTABLE_NAME_OS = EXECUTABLE_NAME + ".%s";
	public static final String INSTALL_FOLDER = "install.folder";
	public static final String SEARCH_PATH = "search.path";
	public static final String SEARCH_PATH_OS = SEARCH_PATH + ".%s";

	public static final String FOLDER_STRICT = "folder.strict";

	public static final String TAB_MAIN_CHECK_PROGRAM = "tab.main.checkProgram";
	public static final boolean TAB_MAIN_CHECK_PROGRAM_DEFAULT = false;

	// ----- Getters ----------------------------------------------------------

	private static String getString(String id, String defaultValue) {

		return Platform.getPreferencesService().getString(Activator.PLUGIN_ID, id, defaultValue, null);
	}

	private static boolean getBoolean(String id, boolean defaultValue) {

		return Platform.getPreferencesService().getBoolean(Activator.PLUGIN_ID, id, defaultValue, null);
	}

	// ----- Setters ----------------------------------------------------------

	private static void putWorkspaceString(String id, String value) {

		value = value.trim();

		// Access the instanceScope
		Preferences preferences = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		preferences.put(id, value);
	}

	public static void flush() {

		try {
			InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).flush();
		} catch (BackingStoreException e) {
			Activator.log(e);
		}
	}
	
	// ----- gdb server doStart -----------------------------------------------
	public static boolean getGdbServerDoStart() {

		return Boolean
				.valueOf(getString(GDB_SERVER_DO_START, Boolean.toString(DefaultPreferences.SERVER_DO_START_DEFAULT)));
	}

	public static void putGdbServerDoStart(boolean value) {

		putWorkspaceString(GDB_SERVER_DO_START, Boolean.toString(value));
	}

	// ----- gdb server executable --------------------------------------------
	public static String getGdbServerExecutable() {

		String value = getString(GDB_SERVER_EXECUTABLE, null);
		if (value != null) {
			return value;
		}
		return DefaultPreferences.getGdbServerExecutable();
	}

	public static void putGdbServerExecutable(String value) {

		putWorkspaceString(GDB_SERVER_EXECUTABLE, value);
	}

	// ----- gdb server other options -----------------------------------------
	public static String getGdbServerOtherOptions() {

		return getString(GDB_SERVER_OTHER_OPTIONS, DefaultPreferences.SERVER_OTHER_OPTIONS_DEFAULT);
	}

	public static void putGdbServerOtherOptions(String value) {

		putWorkspaceString(GDB_SERVER_OTHER_OPTIONS, value);
	}

	// ----- gdb client executable --------------------------------------------
	public static String getGdbClientExecutable() {

		String value = getString(GDB_CLIENT_EXECUTABLE, null);
		if (value != null) {
			return value;
		}
		return DefaultPreferences.getGdbClientExecutable();
	}

	public static void putGdbClientExecutable(String value) {

		putWorkspaceString(GDB_CLIENT_EXECUTABLE, value);
	}

	// ----- gdb client other options -----------------------------------------
	public static String getGdbClientOtherOptions() {

		return getString(GDB_CLIENT_OTHER_OPTIONS, DefaultPreferences.CLIENT_OTHER_OPTIONS_DEFAULT);
	}

	public static void putGdbClientOtherOptions(String value) {

		putWorkspaceString(GDB_CLIENT_OTHER_OPTIONS, value);
	}

	// ----- gdb client commands ----------------------------------------------
	public static String getGdbClientCommands() {

		return getString(GDB_CLIENT_COMMANDS, DefaultPreferences.CLIENT_COMMANDS_DEFAULT);
	}

	public static void putGdbClientCommands(String value) {

		putWorkspaceString(GDB_CLIENT_COMMANDS, value);
	}

	// ----- CSKY do initial reset -----------------------------------------
	public static boolean getCSkyDoInitialReset() {

		return Boolean.valueOf(
				getString(GDB_CSKY_DO_INITIAL_RESET, Boolean.toString(DefaultPreferences.DO_INITIAL_RESET_DEFAULT)));
	}

	public static void putCSkyDoInitialReset(boolean value) {

		putWorkspaceString(GDB_CSKY_DO_INITIAL_RESET, Boolean.toString(value));
	}

	// ----- CSKY enable semihosting ---------------------------------------
	public static boolean getCSkyEnableSemihosting() {

		String value = getString(GDB_CSKY_ENABLE_SEMIHOSTING, null);
		if (value != null) {
			return Boolean.valueOf(value);
		}
		return DefaultPreferences.getCSkyEnableSemihosting();

	}

	public static void putCSkyEnableSemihosting(boolean value) {

		putWorkspaceString(GDB_CSKY_ENABLE_SEMIHOSTING, Boolean.toString(value));
	}

	// ----- CSKY init other -----------------------------------------------
	public static String getCSkyInitOther() {

		return getString(GDB_CSKY_INIT_OTHER, DefaultPreferences.INIT_OTHER_DEFAULT);
	}

	public static void putCSkyInitOther(String value) {

		putWorkspaceString(GDB_CSKY_INIT_OTHER, value);
	}

	// ----- CSKY is verbose ---------------------------------------------
	public static Boolean getCSkyUseDDC() {
		return getBoolean(GDB_CSKY_Use_DDC, DefaultPreferences.SERVER_GDB_USE_DDC_DEFAULT);
	}

	public static void putCSkyUseDDC(boolean value) {

		putWorkspaceString(GDB_CSKY_Use_DDC, Boolean.toString(value));
	}

	// ----- CSKY debug in ram ---------------------------------------------
	public static boolean getCSkyDebugInRam() {

		return Boolean.valueOf(
				getString(GDB_CSKY_DO_DEBUG_IN_RAM, Boolean.toString(DefaultPreferences.DO_DEBUG_IN_RAM_DEFAULT)));
	}

	public static void putCSkyDebugInRam(boolean value) {

		putWorkspaceString(GDB_CSKY_DO_DEBUG_IN_RAM, Boolean.toString(value));
	}

	// ----- CSKY do prerun reset ------------------------------------------
	public static boolean getCSkyDoPreRunReset() {

		return Boolean.valueOf(
				getString(GDB_CSKY_DO_PRERUN_RESET, Boolean.toString(DefaultPreferences.DO_PRERUN_RESET_DEFAULT)));
	}

	public static void putCSkyDoPreRunReset(boolean value) {

		putWorkspaceString(GDB_CSKY_DO_PRERUN_RESET, Boolean.toString(value));
	}

	// ----- CSKY init other --------------------------------------------------
	public static String getCSkyPreRunOther() {

		return getString(GDB_CSKY_PRERUN_OTHER, DefaultPreferences.PRERUN_OTHER_DEFAULT);
	}

	public static void putCSkyPreRunOther(String value) {

		putWorkspaceString(GDB_CSKY_PRERUN_OTHER, value);
	}

	// ----- CSKY debug in ram ---------------------------------------------
	public static boolean getCSkyDisableGraphics() {

		return Boolean.valueOf(
				getString(GDB_CSKY_DISABLE_GRAPHICS, Boolean.toString(DefaultPreferences.DISABLE_GRAPHICS_DEFAULT)));
	}

	public static void putCSkyDisableGraphics(boolean value) {

		putWorkspaceString(GDB_CSKY_DO_DEBUG_IN_RAM, Boolean.toString(value));
	}

	// ------------------------------------------------------------------------
}
