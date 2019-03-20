package com.djyos.dide.debug.gdbjtag.csky.ui;

import com.djyos.dide.debug.gdbjtag.csky.Activator;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.eclipse.osgi.util.NLS;

public class Messages {

	// ------------------------------------------------------------------------

	private static final String MESSAGES = Activator.PLUGIN_ID + ".ui.messages"; //$NON-NLS-1$

	public static String CSkyPagePropertyPage_description;
	public static String CSkyPagePropertyPage_executable_label;
	public static String CSkyPagePropertyPage_executable_folder;

	public static String Variable_executable_description;
	public static String Variable_path_description;

	// ------------------------------------------------------------------------

	static {
		// initialise resource bundle
		NLS.initializeMessages(MESSAGES, Messages.class);
	}

	private static ResourceBundle RESOURCE_BUNDLE;
	static {
		try {
			RESOURCE_BUNDLE = ResourceBundle.getBundle(MESSAGES);
		} catch (MissingResourceException e) {
			Activator.log(e);
		}
	}

	private Messages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	public static ResourceBundle getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	// ------------------------------------------------------------------------
}
