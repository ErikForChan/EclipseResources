package com.djyos.csky.debug.core;

import java.io.File;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

public class DebugConfigurationSetting {
	
//	  private static final String EMPTY_STR = "";
//	  public static final String DEBUGGER_NAME_RESET_NORMAL = "Normal";
//	  public static final String DEBUGGER_NAME_RESET_HARD = "Hard Reset";
//	  public static final String DEBUGGER_NAME_RESET_SOFT = "Soft Reset";
//	  public static final String DEBUGGER_NAME_DEFAULT = "csky-elf-gdb";
//	  public static final String DEBUGGER_NAME_ABIV2_DEFAULT = "csky-abiv2-elf-gdb";
//	  private static final String DEBUGLAUNCH_CONFIG_ID = "DebugLaunch";
//	  private static final String DEBUGLAUNCH_CONFIG = "launch";
//	  private static final String DEBUGLAUNCH_SCRIPT = "debugscript";
//	  private static final String DEBUGLAUNCH_FLASH = "flash";
//	  private static final String DEBUGLAUNCH_CONNECT = "connection";
	  private String strDriverPath = "";
	  private String strDriverTemplate = "";
//
	  private String strConnectMode = "Normal";
	  private String strLockConnectMode = "false";
//
	  private String strFirstResetMode = "Hard Reset";
	  private String strSecondResetMode = "Soft Reset";
	  private String strLockFirstReset = "false";
	  private String strLockSecondReset = "false";
//
	  private String strSoftResetCommand = "0";
	  private String strLockSoftResetCommand = "false";
//
	  private boolean bSaveByProperty = false;

	  public String defaultGdbCommand(ILaunchConfiguration configuration)
	  {
	    String gdbCommand = null;
	    String projectBuildID = "";
	    try {
	      projectBuildID = configuration.getAttribute("org.eclipse.cdt.launch.PROJECT_BUILD_CONFIG_ID_ATTR", "");
	    } catch (CoreException e) {
	      e.printStackTrace();
	    }
	    if (projectBuildID.toLowerCase().indexOf("abiv2") != -1)
	      gdbCommand = "csky-abiv2-elf-gdb";
	    else {
	      gdbCommand = "csky-elf-gdb";
	    }

	    return gdbCommand;
	  }
	  
	  private boolean isDebugInROM(ILaunchConfiguration config)
	  {
	    boolean isDebugInROM = false;
	    try {
	      ICProject cProject = getCProject(config);
	      if (cProject == null) {
	        return isDebugInROM;
	      }
	      IProject project = cProject.getProject();
	      ICProjectDescription descriptor = getProjectDescription(project,false);
	      ICStorageElement rootElement = descriptor.getStorage(
	        "DebugLaunch", false);
	      if (rootElement != null) {
	        ICStorageElement[] configElements = rootElement.getChildrenByName("launch");
	        if ((configElements != null) && (configElements.length > 0)) {
	          String attribute = configElements[0].getAttribute("BDebugInRom");
	          if ((attribute != null) && (attribute.equalsIgnoreCase("true"))) {
	            isDebugInROM = true;
	          }
	        }
	        ICStorageElement[] flashconfigElements = rootElement.getChildrenByName("flash");
	        if ((flashconfigElements != null) && (flashconfigElements.length > 0)) {
	          this.strDriverPath = flashconfigElements[0].getAttribute("FlashDriverPath");
	          this.strDriverTemplate = flashconfigElements[0].getAttribute("FlashTemplateName");
	        }
	      }
	    } catch (CoreException e) {
	      e.printStackTrace();
	    }
	    return isDebugInROM;
	  }
	  
	  public void setConfugrationDefaults(ILaunchConfigurationWorkingCopy config)
	  {
	    try {
	      ICProject cProject = getCProject(config);
	      if (cProject == null) {
	        return;
	      }
	      String attribute = "";
	      IProject project = cProject.getProject();
	      ICProjectDescription descriptor = getProjectDescription(project, false);
	      ICStorageElement rootElement = descriptor.getStorage("DebugLaunch", false);
	      if (rootElement != null) {
	        ICStorageElement[] configElements = rootElement.getChildrenByName("launch");
	        if ((configElements != null) && (configElements.length > 0) && (!bSaveByProperty))
	        {
	          attribute = configElements[0].getAttribute("BEcosSystem");
	          if (attribute != null) {
	            config.setAttribute(
	              "eCosDebug", 
	              Boolean.valueOf(attribute).booleanValue());
	          }

	          attribute = configElements[0].getAttribute("BLoadImage");
	          if (attribute != null) {
	            config.setAttribute(
	              "use_loadimage", 
	              Boolean.valueOf(attribute).booleanValue());
	          }

	          attribute = configElements[0].getAttribute("BAutoRun");
	          if (attribute != null) {
	            config.setAttribute(
	              "use_autorun", 
	              Boolean.valueOf(attribute).booleanValue());
	          }

	          attribute = configElements[0].getAttribute("BPreloadScript");
	          if (attribute != null) {
	            config.setAttribute(
	              "Execute preload script", 
	              Boolean.valueOf(attribute).booleanValue());
	          }

	          attribute = configElements[0].getAttribute("BFirstReset");
	          if (attribute != null) {
	            config.setAttribute(
	              "FirstReset", 
	              Boolean.valueOf(attribute).booleanValue());
	          }

	          attribute = configElements[0].getAttribute("LockFirstReset");
	          if (attribute != null) {
	            strLockFirstReset = attribute;
	          }
	          config.setAttribute("Lock First Reset", Boolean.valueOf(strLockFirstReset).booleanValue());

	          attribute = configElements[0].getAttribute("BSecondReset");
	          if (attribute != null) {
	            config.setAttribute(
	              "SecondReset", 
	              Boolean.valueOf(attribute).booleanValue());
	          }

	          attribute = configElements[0].getAttribute("LockSecondReset");
	          if (attribute != null) {
	            strLockSecondReset = attribute;
	          }
	          config.setAttribute("Lock Second Reset", Boolean.valueOf(strLockSecondReset).booleanValue());

	          attribute = configElements[0].getAttribute("Connect");
	          if (attribute != null) {
	            strConnectMode = attribute;
	          }
	          config.setAttribute("ConnectMode", strConnectMode);

	          attribute = configElements[0].getAttribute("LockConnect");
	          if (attribute != null) {
	            strLockConnectMode = attribute;
	          }
	          config.setAttribute("Lock Connect mode selection", Boolean.valueOf(strLockConnectMode).booleanValue());

	          attribute = configElements[0].getAttribute("FirstReset");
	          if (attribute != null) {
	            strFirstResetMode = attribute;
	          }
	          config.setAttribute("FirstReset mode", strFirstResetMode);

	          attribute = configElements[0].getAttribute("SecondReset");
	          if (attribute != null) {
	            strSecondResetMode = attribute;
	          }
	          config.setAttribute("SecondReset mode", strSecondResetMode);

	          attribute = configElements[0].getAttribute("SResetCommand");
	          if (attribute != null) {
	            strSoftResetCommand = attribute;
	          }
	          config.setAttribute("SReset Command", strSoftResetCommand);

	          attribute = configElements[0].getAttribute("LockSResetCommand");
	          if (attribute != null) {
	            strLockSoftResetCommand = attribute;
	          }
	          config.setAttribute("Lock Soft Reset Command", Boolean.valueOf(strLockSoftResetCommand).booleanValue());

	          attribute = configElements[0].getAttribute("BNoGraphic");
	          if (attribute != null) {
	            config.setAttribute(
	              "use nographic option", 
	              Boolean.valueOf(attribute).booleanValue());
	          }

	          attribute = configElements[0].getAttribute("BOutputLog");
	          if (attribute != null) {
	            config.setAttribute(
	              "output log", 
	              Boolean.valueOf(attribute).booleanValue());
	          }

	          attribute = configElements[0].getAttribute("SimOtherFlags");
	          if (attribute != null) {
	            config.setAttribute(
	              "sim_other_flags", 
	              attribute);
	          }

	          attribute = configElements[0].getAttribute("Machine");
	          if ((attribute != null) && (!attribute.equals("")))
	            config.setAttribute("sim_machine", 
	              "${ProjDirPath}" + File.separator + attribute);
	          else {
	            config.setAttribute("sim_machine", "");
	          }
	          String tmpString = "";

	          attribute = configElements[0].getAttribute("LockLaunchOption");
	          if (attribute != null)
	            tmpString = attribute;
	          else {
	            tmpString = "false";
	          }
	          config.setAttribute("LockLaunchOption", Boolean.valueOf(tmpString).booleanValue());

	          attribute = configElements[0].getAttribute("LockLaunchSteps");
	          if (attribute != null)
	            tmpString = attribute;
	          else {
	            tmpString = "false";
	          }
	          config.setAttribute("LockLaunchSteps", Boolean.valueOf(tmpString).booleanValue());

	          attribute = configElements[0].getAttribute("LockDebugResetType");
	          if (attribute != null)
	            tmpString = attribute;
	          else {
	            tmpString = "false";
	          }
	          config.setAttribute("LockDebugResetType", Boolean.valueOf(tmpString).booleanValue());

	          attribute = configElements[0].getAttribute("DebugResetType");
	          if (attribute != null)
	            tmpString = attribute;
	          else {
	            tmpString = "Hard Reset";
	          }
	          config.setAttribute("DebugResetType", tmpString);
	        }

	        ICStorageElement[] configElements_flash = rootElement.getChildrenByName("flash");
	        if ((configElements_flash != null) && (configElements_flash.length > 0)) {
	          String tmpString = "";

	          attribute = configElements_flash[0].getAttribute("BChipErase");
	          if (attribute != null)
	            tmpString = attribute;
	          else {
	            tmpString = "false";
	          }
	          config.setAttribute(
	            "chip_erase", 
	            Boolean.valueOf(tmpString).booleanValue());

	          attribute = configElements_flash[0].getAttribute("BEraseSectors");
	          if (attribute != null)
	            tmpString = attribute;
	          else {
	            tmpString = "true";
	          }
	          config.setAttribute(
	            "erase_sector", 
	            Boolean.valueOf(tmpString).booleanValue());

	          attribute = configElements_flash[0].getAttribute("BNotErase");
	          if (attribute != null)
	            tmpString = attribute;
	          else {
	            tmpString = "false";
	          }
	          config.setAttribute(
	            "do_not_erase", 
	            Boolean.valueOf(tmpString).booleanValue());

	          attribute = configElements_flash[0].getAttribute("BEraseRange");
	          if (attribute != null)
	            tmpString = attribute;
	          else {
	            tmpString = "false";
	          }
	          config.setAttribute(
	            "erase_range", 
	            Boolean.valueOf(tmpString).booleanValue());

	          attribute = configElements_flash[0].getAttribute("EraseStart");
	          if (attribute != null) {
	            config.setAttribute(
	              "erase_range_start", 
	              attribute);
	          }

	          attribute = configElements_flash[0].getAttribute("EraseLength");
	          if (attribute != null) {
	            config.setAttribute(
	              "erase_range_length", 
	              attribute);
	          }

	          attribute = configElements_flash[0].getAttribute("CommandTimeout");
	          if (attribute != null)
	            tmpString = attribute;
	          else {
	            tmpString = "60";
	          }
	          config.setAttribute(
	            "command_timeout", 
	            tmpString);
	        }

	        boolean isDebugInROM = isDebugInROM(config);
	        if (!bSaveByProperty)
	        {
	          config.setAttribute("DebugInFlash", isDebugInROM);
	        }

	        config.setAttribute("DriverFilePath", strDriverPath);
	        config.setAttribute("DriverTemplate", strDriverTemplate);
	      }
	    } catch (CoreException e) {
	      e.printStackTrace();
	    }
	  }
	  
	  protected ICProjectDescription getProjectDescription(IProject project, boolean write)
	  {
	    return CCorePlugin.getDefault().getProjectDescription(project, write);
	  }
	  
	  public void setDebuggerConfugrationDefaults(ILaunchConfigurationWorkingCopy config) {
		    try {
		      ICProject cProject = getCProject(config);
		      if (cProject == null) {
		        return;
		      }
		      String attribute = "";
		      IProject project = cProject.getProject();
		      ICProjectDescription descriptor = getProjectDescription(project, false);
		      ICStorageElement rootElement = descriptor.getStorage("DebugLaunch", false);
		      if (rootElement != null) {
		        ICStorageElement[] configElements = rootElement.getChildrenByName("launch");
		        if ((configElements != null) && (configElements.length > 0))
		        {
		          attribute = configElements[0].getAttribute("BStopAt");
		          if (attribute != null) {
		            config.setAttribute(
		              "org.eclipse.cdt.launch.DEBUGGER_STOP_AT_MAIN", 
		              Boolean.valueOf(attribute).booleanValue());
		          }

		          attribute = configElements[0].getAttribute("StopAtFunction");
		          if (attribute != null) {
		            config.setAttribute(
		              "org.eclipse.cdt.launch.DEBUGGER_STOP_AT_MAIN_SYMBOL", 
		              attribute);
		          }

		          attribute = configElements[0].getAttribute("RegisterGroups");
		          if (attribute != null)
		            config.setAttribute(
		              "org.eclipse.cdt.launch.DEBUGGER_REGISTER_GROUPS", 
		              attribute);
		        }
		      }
		    }
		    catch (CoreException e) {
		      e.printStackTrace();
		    }
		  }
	  
	  public void setDebuggerMainTabConfugrationDefaults(ILaunchConfigurationWorkingCopy config)
	  {
	    config.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME, defaultGdbCommand(config));
	    try {
	      ICProject cProject = getCProject(config);
	      if (cProject == null) {
	        return;
	      }
	      String attribute = "";
	      IProject project = cProject.getProject();
	      ICProjectDescription descriptor = getProjectDescription(project, false);
	      ICStorageElement rootElement = descriptor.getStorage("DebugLaunch", false);
	      if (rootElement != null)
	      {
	        ICStorageElement[] configElements = rootElement.getChildrenByName("debugscript");
	        if ((configElements != null) && (configElements.length > 0))
	        {
	          attribute = configElements[0].getAttribute("LockScriptSelect");
	          if (attribute != null) {
	            config.setAttribute(
	              "LockScriptSelect", 
	              Boolean.valueOf(attribute).booleanValue());
	          }

	          attribute = configElements[0].getAttribute("PreloadScriptPath");
	          if (attribute != null) {
	            config.setAttribute(
	              ".gdb_preload", 
	              attribute);
	          }

	          attribute = configElements[0].getAttribute("InitScriptPath");
	          if (attribute != null) {
	            config.setAttribute(IGDBLaunchConfigurationConstants.ATTR_GDB_INIT, 
	              attribute);
	          }

	          attribute = configElements[0].getAttribute("ContinueScriptPath");
	          if (attribute != null) {
	            config.setAttribute(
	              ".gdb_continue", 
	              attribute);
	          }

	          attribute = configElements[0].getAttribute("StopScriptPath");
	          if (attribute != null) {
	            config.setAttribute(
	              ".gdb_stop", 
	              attribute);
	          }

	          attribute = configElements[0].getAttribute("HResetScriptPath");
	          if (attribute != null) {
	            config.setAttribute(
	              ".gdb_hwreset", 
	              attribute);
	          }

	          attribute = configElements[0].getAttribute("SResetScriptPath");
	          if (attribute != null)
	            config.setAttribute(
	              ".gdb_swreset", 
	              attribute);
	        }
	      }
	    }
	    catch (CoreException e) {
	      e.printStackTrace();
	    }
	  }
	  
	  public void setDebuggerConnectionTabConfugrationDefaults(ILaunchConfigurationWorkingCopy config) {
		    try {
		      ICProject cProject = getCProject(config);
		      if (cProject == null) {
		        return;
		      }
		      String attribute = "";
		      IProject project = cProject.getProject();
		      ICProjectDescription descriptor = getProjectDescription(project, false);
		      ICStorageElement rootElement = descriptor.getStorage("DebugLaunch", false);
		      if (rootElement != null)
		      {
		        ICStorageElement[] configElements = rootElement.getChildrenByName("connection");
		        if ((configElements != null) && (configElements.length > 0))
		        {
		          attribute = configElements[0].getAttribute("LockDebugConnection");
		          if (attribute != null) {
		            config.setAttribute(
		              "LockDebugConnection", 
		              Boolean.valueOf(attribute).booleanValue());
		          }

		          attribute = configElements[0].getAttribute("ICECLK");
		          if (attribute != null) {
		            config.setAttribute(
		              "ice_clk", 
		              attribute);
		          }

		          attribute = configElements[0].getAttribute("Delayformtcr");
		          if (attribute != null) {
		            config.setAttribute(
		              "mtcr_delay", 
		              attribute);
		          }

		          attribute = configElements[0].getAttribute("JtagServerIP");
		          if (attribute != null) {
		            config.setAttribute(IGDBLaunchConfigurationConstants.ATTR_HOST,attribute);
		          }

		          attribute = configElements[0].getAttribute("JtagServerPort");
		          if (attribute != null) {
		            config.setAttribute(IGDBLaunchConfigurationConstants.ATTR_PORT,attribute);
		          }

		          if (((attribute = configElements[0].getAttribute("BJtagServer")) != null) && 
		            (attribute.equals("true"))) {
		            config.setAttribute("debug_target", 
		              "jtag_server");
		          } else if (((attribute = configElements[0].getAttribute("BSimulator")) != null) && 
		            (attribute.equals("true"))) {
		            config.setAttribute("debug_target", 
		              "simulator");
		            config.setAttribute("sim_mode", 
		              "Qemu System Mode");
		          } else {
		            config.setAttribute("debug_target", 
		              "local_jtag");
		          }

		          attribute = configElements[0].getAttribute("BUseDDC");
		          if (attribute != null) {
		            config.setAttribute(
		              "use_ddc", 
		              Boolean.valueOf(attribute).booleanValue());
		          }

		          attribute = configElements[0].getAttribute("LocalJTAGFlags");
		          if (attribute != null)
		            config.setAttribute(
		              "localjtag_flags", 
		              attribute);
		        }
		      }
		    }
		    catch (CoreException e) {
		      e.printStackTrace();
		    }
		  }
	  
	    public static ICProject getCProject(ILaunchConfiguration configuration) throws CoreException {
	        String projectName = getProjectName(configuration);
	        if (projectName != null) {
	            projectName = projectName.trim();
	            if (projectName.length() > 0) {
	                IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	                ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(project);
	                if (cProject != null && cProject.exists()) {
	                    return cProject;
	                }
	            }
	        }
	        return null;
	    }
	    
	    public static String getProjectName(ILaunchConfiguration configuration) throws CoreException {
	        return configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
	    }
	  

}
