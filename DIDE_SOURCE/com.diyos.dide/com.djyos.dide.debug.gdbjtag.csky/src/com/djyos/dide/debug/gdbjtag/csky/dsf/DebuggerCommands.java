package com.djyos.dide.debug.gdbjtag.csky.dsf;

import ilg.gnuarmeclipse.core.EclipseUtils;
import ilg.gnuarmeclipse.core.StringUtils;
import ilg.gnuarmeclipse.debug.gdbjtag.Activator;
import ilg.gnuarmeclipse.debug.gdbjtag.DebugUtils;
import ilg.gnuarmeclipse.debug.gdbjtag.dsf.GnuArmDebuggerCommandsService;
import com.djyos.dide.debug.gdbjtag.csky.ConfigurationAttributes;
import com.djyos.dide.debug.gdbjtag.csky.DefaultPreferences;

import java.util.List;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConstants;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.osgi.framework.BundleContext;

public class DebuggerCommands extends GnuArmDebuggerCommandsService {

	// ------------------------------------------------------------------------

	public DebuggerCommands(DsfSession session, ILaunchConfiguration lc, String mode) {
		super(session, lc, mode, true); // do double backslash
	}

	// ------------------------------------------------------------------------

	@Override
	protected BundleContext getBundleContext() {
		return Activator.getInstance().getBundle().getBundleContext();
	}

	// ------------------------------------------------------------------------

	@Override
	public IStatus addGdbInitCommandsCommands(List<String> commandsList) {

		String otherInits = CDebugUtils.getAttribute(fAttributes, ConfigurationAttributes.GDB_CLIENT_OTHER_COMMANDS,
				DefaultPreferences.getGdbClientCommands()).trim();

		otherInits = DebugUtils.resolveAll(otherInits, fAttributes);
		DebugUtils.addMultiLine(otherInits, commandsList);

		return Status.OK_STATUS;
	}

	// ------------------------------------------------------------------------

	@Override
	public IStatus addGnuArmResetCommands(List<String> commandsList) {

		IStatus status = addFirstResetCommands(commandsList);
		if (!status.isOK()) {
			return status;
		}

		status = addLoadSymbolsCommands(commandsList);

		if (!status.isOK()) {
			return status;
		}
		
		boolean doConnectToRunning = CDebugUtils.getAttribute(fAttributes,
				ConfigurationAttributes.DO_CONNECT_TO_RUNNING, DefaultPreferences.DO_CONNECT_TO_RUNNING_DEFAULT);
	
		if (!doConnectToRunning) {
			if (CDebugUtils.getAttribute(fAttributes, IGDBJtagConstants.ATTR_LOAD_IMAGE,
					IGDBJtagConstants.DEFAULT_LOAD_IMAGE)
					&& !CDebugUtils.getAttribute(fAttributes, ConfigurationAttributes.DO_DEBUG_IN_RAM,
							DefaultPreferences.getCSkyDebugInRam())) {
	
				status = addLoadImageCommands(commandsList);
	
				if (!status.isOK()) {
					return status;
				}
			}
		}
		return Status.OK_STATUS;
	}

	@Override
	public IStatus addGnuArmStartCommands(List<String> commandsList) {

		boolean doReset = !CDebugUtils.getAttribute(fAttributes, ConfigurationAttributes.DO_CONNECT_TO_RUNNING,
				DefaultPreferences.DO_CONNECT_TO_RUNNING_DEFAULT);
		
		IStatus status = addStartRestartCommands(doReset, commandsList);

		if (!status.isOK()) {
			return status;
		}

		return Status.OK_STATUS;
	}

	// ------------------------------------------------------------------------

	@Override
	public IStatus addFirstResetCommands(List<String> commandsList) {

//		boolean noReset = CDebugUtils.getAttribute(fAttributes, ConfigurationAttributes.DO_CONNECT_TO_RUNNING,
//				DefaultPreferences.DO_CONNECT_TO_RUNNING_DEFAULT);
//		if (noReset) {
//			if (CDebugUtils.getAttribute(fAttributes, ConfigurationAttributes.DO_FIRST_RESET,
//					DefaultPreferences.getCSkyDoInitialReset())) {
//	
//				String commandStr = DefaultPreferences.DO_INITIAL_RESET_COMMAND;
//				commandsList.add(commandStr);
//	
//				// Although the manual claims that reset always does a
//				// halt, better issue it explicitly
//				commandStr = DefaultPreferences.HALT_COMMAND;
//				commandsList.add(commandStr);
//			}
//		}
		String otherInits = CDebugUtils.getAttribute(fAttributes, ConfigurationAttributes.OTHER_INIT_COMMANDS,
				DefaultPreferences.getCSkyInitOther()).trim();

		otherInits = DebugUtils.resolveAll(otherInits, fAttributes);
		if (fDoDoubleBackslash && EclipseUtils.isWindows()) {
			otherInits = StringUtils.duplicateBackslashes(otherInits);
		}
		DebugUtils.addMultiLine(otherInits, commandsList);

		return Status.OK_STATUS;
	}

	@Override
	public IStatus addStartRestartCommands(boolean doReset, List<String> commandsList) {

//		if (!doReset) {
//			if (CDebugUtils.getAttribute(fAttributes, ConfigurationAttributes.DO_SECOND_RESET,
//					DefaultPreferences.getCSkyDoPreRunReset())) {
//				String commandStr = DefaultPreferences.DO_PRERUN_RESET_COMMAND;
//				commandsList.add(commandStr);
//
//				// Although the manual claims that reset always does a
//				// halt, better issue it explicitly
//				commandStr = DefaultPreferences.HALT_COMMAND;
//				commandsList.add(commandStr);
//			}
//		}

		if (CDebugUtils.getAttribute(fAttributes, IGDBJtagConstants.ATTR_LOAD_IMAGE,
				IGDBJtagConstants.DEFAULT_LOAD_IMAGE)
				&& CDebugUtils.getAttribute(fAttributes, ConfigurationAttributes.DO_DEBUG_IN_RAM,
						DefaultPreferences.getCSkyDebugInRam())) {

			IStatus status = addLoadImageCommands(commandsList);

			if (!status.isOK()) {
				return status;
			}
		}

		String userCmd = CDebugUtils.getAttribute(fAttributes, ConfigurationAttributes.OTHER_RUN_COMMANDS,
				DefaultPreferences.getCSkyPreRunOther()).trim();

		userCmd = DebugUtils.resolveAll(userCmd, fAttributes);

		if (fDoDoubleBackslash && EclipseUtils.isWindows()) {
			userCmd = StringUtils.duplicateBackslashes(userCmd);
		}

		DebugUtils.addMultiLine(userCmd, commandsList);

		addSetPcCommands(commandsList);

		addStopAtCommands(commandsList);

		// commandsList.add("monitor reg");

		if (CDebugUtils.getAttribute(fAttributes, ConfigurationAttributes.DO_CONTINUE,
				DefaultPreferences.DO_CONTINUE_DEFAULT)) {
			commandsList.add(DefaultPreferences.DO_CONTINUE_COMMAND);
		}

		return Status.OK_STATUS;
	}

	// ------------------------------------------------------------------------
}
