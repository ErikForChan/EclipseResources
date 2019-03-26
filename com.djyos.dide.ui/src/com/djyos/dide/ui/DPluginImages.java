package com.djyos.dide.ui;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;

public class DPluginImages {
	public static final IPath ICONS_PATH = new Path("$nl$/icons"); //$NON-NLS-1$
	private static final String ICONS = "icons/"; //$NON-NLS-1$
	public static final String T_OBJ = "obj16/"; //$NON-NLS-1$
	public static final String T_WIZBAN = "wizban/"; //$NON-NLS-1$
	public static final String T_LCL = "lcl16/"; //$NON-NLS-1$
	public static final String T_DLCL = "dlcl16/"; //$NON-NLS-1$
	public static final String T_ELCL = "elcl16/"; //$NON-NLS-1$
	public static final String T_TOOL = "tool16/"; //$NON-NLS-1$
	public static final String T_VIEW = "view16/"; //$NON-NLS-1$
	public static final String T_OVR = "ovr16/"; //$NON-NLS-1$

	// 新建Cpu和新建板件的图标
	public static final ImageDescriptor OBJ_CPU_VIEW = createUnManaged(T_OVR, "cpu_img.gif"); //$NON-NLS-1$
	public static final ImageDescriptor OBJ_ARCH_VIEW = createUnManaged(T_OVR, "arch_img.png"); //$NON-NLS-1$
	public static final ImageDescriptor OBJ_BOARD_VIEW = createUnManaged(T_OVR, "board_img.gif"); //$NON-NLS-1$
	public static final ImageDescriptor OBJ_GROUP_VIEW = createUnManaged(T_OVR, "group_view.gif"); //$NON-NLS-1$
	public static final ImageDescriptor OBJ_CORE_VIEW = createUnManaged(T_OVR, "core_img.gif"); //$NON-NLS-1$

	public static final ImageDescriptor TREE_FLODER_VIEW = createUnManaged(T_OVR, "fldr_obj.gif"); //$NON-NLS-1$
	public static final ImageDescriptor CFG_DONE_VIEW = createUnManaged(T_OVR, "cfg_done.png"); //$NON-NLS-1$
	public static final ImageDescriptor CFG_CMPT_VIEW = createUnManaged(T_OVR, "config-component.gif"); //$NON-NLS-1$
	public static final ImageDescriptor CFG_REVISE_VIEW = createUnManaged(T_OVR, "config-revise.gif"); //$NON-NLS-1$
	public static final ImageDescriptor CFG_DELETE_OBJ = createUnManaged(T_OVR, "delete_obj.png"); //$NON-NLS-1$
	public static final ImageDescriptor CFG_COMPONENT_OBJ = createUnManaged(T_OVR, "component.gif"); //$NON-NLS-1$
	public static final ImageDescriptor CFG_CPMT_OBJ = createUnManaged(T_OVR, "cpmt.gif"); //$NON-NLS-1$
	public static final ImageDescriptor CFG_OPENFILE_VIEW = createUnManaged(T_OVR, "open_file.gif"); //$NON-NLS-1$
	public static final ImageDescriptor CFG_COMPTERROR_VIEW = createUnManaged(T_OVR, "component_warning.png"); //$NON-NLS-1$
	public static final ImageDescriptor CPU_REVISE_VIEW = createUnManaged(T_OVR, "cpu_revise.gif"); //$NON-NLS-1$
	public static final ImageDescriptor OBJ_CHIP_VIEW = createUnManaged(T_OVR, "chip_img.png"); //$NON-NLS-1$
	public static final ImageDescriptor OBJ_BSP_VIEW = createUnManaged(T_OVR, "bsp_img.png"); //$NON-NLS-1$
	public static final ImageDescriptor OBJ_DRV_VIEW = createUnManaged(T_OVR, "drv_img.png"); //$NON-NLS-1$
	public static final ImageDescriptor OBJ_FILE_VIEW = createUnManaged(T_OVR, "file_img.png"); //$NON-NLS-1$
	public static final ImageDescriptor OBJ_STEP_VIEW = createUnManaged(T_OVR, "step_img.gif");

	/**
	 * Creates an image descriptor for the given prefix and name in the JDT UI
	 * bundle. The path can contain variables like $NL$. If no image could be found,
	 * the 'missing image descriptor' is returned.
	 */
	private static ImageDescriptor createUnManaged(String prefix, String name) {
		try {
			return create(prefix, name, true);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return ImageDescriptor.getMissingImageDescriptor();
	}

	private static ImageDescriptor create(String prefix, String name, boolean useMissingImageDescriptor) {
		return DUIPlugin.getImageDescriptor(ICONS + prefix + name);
	}

}
