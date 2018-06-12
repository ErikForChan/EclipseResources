package org.eclipse.cdt.ui.wizards.djyosProject;

import org.eclipse.osgi.util.NLS;
import org.eclipse.cdt.internal.ui.newui.Messages;

public class DjyosMessages extends NLS{
	public static String Cpu_RelativePath;
	public static String Board_RelativePath;
	public static String Configuration_Debug;
	public static String Configuration_Release;
	public static String Arch_SuperClassId;
	public static String Family_SuperClassId;
	public static String FpuABI_SuperClassId;
	public static String FpuType_SuperClassId;
	public static String Arch_Prefix;
	public static String Family_Prefix;
	public static String FpuABI_Prefix;
	public static String FpuType_Prefix;
	static {
		// Initialize resource bundle.
		NLS.initializeMessages(DjyosMessages.class.getName(), DjyosMessages.class);
	}

	private DjyosMessages() {
	}

}
