package com.djyos.dide.debug.gdbjtag.csky.preferences;

import ilg.gnuarmeclipse.core.preferences.DirectoryNotStrictVariableFieldEditor;
import ilg.gnuarmeclipse.core.preferences.StringVariableFieldEditor;
import com.djyos.dide.debug.gdbjtag.csky.Activator;
import com.djyos.dide.debug.gdbjtag.csky.DefaultPreferences;
import com.djyos.dide.debug.gdbjtag.csky.PersistentPreferences;
import com.djyos.dide.debug.gdbjtag.csky.VariableInitializer;
import com.djyos.dide.debug.gdbjtag.csky.ui.Messages;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page uses special filed editors, that get the default values from the
 * preferences store, but the values are from the variables store.
 */
public class CSkyPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	// ------------------------------------------------------------------------

	public static final String ID = "com.djyos.dide.debug.gdbjtag.csky.preferencePage";

	// ------------------------------------------------------------------------

	public CSkyPage() {
		super(GRID);

		// Not really used, the field editors directly access the variables
		// store.
		setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, Activator.PLUGIN_ID));

		setDescription(Messages.CSkyPagePropertyPage_description);
	}

	// ------------------------------------------------------------------------

	// Contributed by IWorkbenchPreferencePage
	@Override
	public void init(IWorkbench workbench) {

		if (Activator.getInstance().isDebugging()) {
			System.out.println("OpenOcdPage.init()");
		}
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	@Override
	protected void createFieldEditors() {

		FieldEditor executable;
		executable = new StringVariableFieldEditor(PersistentPreferences.EXECUTABLE_NAME,
				VariableInitializer.VARIABLE_CSKY_EXECUTABLE, Messages.Variable_executable_description,
				Messages.CSkyPagePropertyPage_executable_label, getFieldEditorParent());
		addField(executable);

		boolean isStrict;
		isStrict = DefaultPreferences.getBoolean(PersistentPreferences.FOLDER_STRICT, true);

		FieldEditor folder;
		folder = new DirectoryNotStrictVariableFieldEditor(PersistentPreferences.INSTALL_FOLDER,
				VariableInitializer.VARIABLE_CSKY_PATH, Messages.Variable_path_description,
				Messages.CSkyPagePropertyPage_executable_folder, getFieldEditorParent(), isStrict);
		addField(folder);
	}

	// ------------------------------------------------------------------------
}

