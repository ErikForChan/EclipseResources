package com.djyos.dide.ui.wizards.djyosProject;

import org.eclipse.osgi.util.NLS;
import org.eclipse.cdt.internal.ui.newui.Messages;
import com.djyos.dide.ui.wizards.djyosProject.DjyosMessages;

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
	public static String Iboot_App_Project;
	public static String Iboot_Only_Project;
	public static String App_Only_Project;
	public static String App_Project;
	public static String Template_Label;
	public static String Ibootsize_Label;
	public static String App_Wizard_Title;
	public static String Iboot_Wizard_Title;
	public static String App_Wizard_Desc;
	public static String Iboot_Wizard_Desc;
	public static String Automatically_Generated;
	public static String Init_End_Prompt;
	public static String Define_Endif;
	static {
		// Initialize resource bundle.
		NLS.initializeMessages(DjyosMessages.class.getName(), DjyosMessages.class);
	}

	private DjyosMessages() {
	}

}
