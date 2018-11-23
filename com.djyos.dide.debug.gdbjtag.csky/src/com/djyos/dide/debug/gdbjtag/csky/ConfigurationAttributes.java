package com.djyos.dide.debug.gdbjtag.csky;

public interface ConfigurationAttributes {

	public static final String PREFIX = Activator.PLUGIN_ID;

	// TabDebugger

	// Must be in sync with plugin.xml definition
	public static final String JTAG_DEVICE = "GNU CSKY";

	public static final String DO_START_GDB_SERVER = PREFIX + ".doStartGdbServer"; //$NON-NLS-1$

	public static final String GDB_SERVER_EXECUTABLE = PREFIX + ".gdbServerExecutable"; //$NON-NLS-1$

	public static final String GDB_SERVER_MACHINE_NAME = PREFIX + ".gdbServerMachineName"; //$NON-NLS-1$
//	public static final String GDB_SERVER_BOARD_NAME = PREFIX + ".gdbServerBoardName"; //$NON-NLS-1$
//
//	public static final String GDB_SERVER_DEVICE_NAME = PREFIX + ".gdbServerDeviceName"; //$NON-NLS-1$
	public static final String GDB_SERVER_ICE_CLK = PREFIX + ".gdbServerIceClk"; //$NON-NLS-1$

	public static final String GDB_SERVER_DELAY_MTCR = PREFIX + ".gdbServerDelayMtcr"; //$NON-NLS-1$

	public static final String GDB_SERVER_USE_DDC = PREFIX + ".gdbServerUseDDC"; //$NON-NLS-1$
	
	public static final String GDB_SERVER_GDB_PORT_NUMBER = PREFIX + ".gdbServerGdbPortNumber"; //$NON-NLS-1$

	public static final String GDB_SERVER_OTHER = PREFIX + ".gdbServerOther"; //$NON-NLS-1$

	public static final String DO_GDB_SERVER_ALLOCATE_CONSOLE = PREFIX + ".doGdbServerAllocateConsole"; //$NON-NLS-1$

	//public static final String IS_GDB_SERVER_VERBOSE = PREFIX + ".isGdbServerVerbose"; //$NON-NLS-1$

	public static final String GDB_CLIENT_EXECUTABLE_DEFAULT = "csky-elfabiv2-gdb.exe";

	public static final String GDB_CLIENT_OTHER_OPTIONS = PREFIX + ".gdbClientOtherOptions"; //$NON-NLS-1$

	public static final String GDB_CLIENT_OTHER_COMMANDS = PREFIX + ".gdbClientOtherCommands"; //$NON-NLS-1$

	public static final boolean UPDATE_THREAD_LIST_DEFAULT = false;

	// TabStartup
	//public static final String DO_FIRST_RESET = PREFIX + ".doFirstReset"; //$NON-NLS-1$

	public static final String ENABLE_SEMIHOSTING = PREFIX + ".enableSemihosting"; //$NON-NLS-1$

	public static final String SEMIHOSTING_CMDLINE = PREFIX + ".semihosting.cmdline"; //$NON-NLS-1$

	public static final String DISABLE_GRAPHICS = PREFIX + ".disableGraphics"; //$NON-NLS-1$ ;

	public static final String OTHER_INIT_COMMANDS = PREFIX + ".otherInitCommands"; //$NON-NLS-1$

	public static final String DO_DEBUG_IN_RAM = PREFIX + ".doDebugInRam"; //$NON-NLS-1$

	//public static final String DO_SECOND_RESET = PREFIX + ".doSecondReset"; //$NON-NLS-1$

	public static final String OTHER_RUN_COMMANDS = PREFIX + ".otherRunCommands"; //$NON-NLS-1$

	public static final String DO_CONTINUE = PREFIX + ".doContinue"; //$NON-NLS-1$

}
