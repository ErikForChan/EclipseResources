package com.djyos.dide.debug.gdbjtag.csky;

import com.djyos.dide.debug.gdbjtag.csky.ui.Messages;

import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.IValueVariableInitializer;

public class VariableInitializer implements IValueVariableInitializer {

	// ------------------------------------------------------------------------

	public static final String VARIABLE_CSKY_EXECUTABLE = "csky_executable";
	public static final String VARIABLE_CSKY_PATH = "csky_path";

	static final String UNDEFINED_PATH = "undefined_path";

	// ------------------------------------------------------------------------

	@Override
	public void initialize(IValueVariable variable) {

		String value;

		if (VARIABLE_CSKY_EXECUTABLE.equals(variable.getName())) {

			value = DefaultPreferences.getExecutableName();
			if (value == null) {
				value = DefaultPreferences.SERVER_EXECUTABLE_DEFAULT_NAME;
			}
			variable.setValue(value);
			variable.setDescription(Messages.Variable_executable_description);

		} else if (VARIABLE_CSKY_PATH.equals(variable.getName())) {

			value = DefaultPreferences.getInstallFolder();
			if (value == null) {
				value = UNDEFINED_PATH;
			}
			variable.setValue(value);
			variable.setDescription(Messages.Variable_path_description);

		}
	}

	// ------------------------------------------------------------------------
}
