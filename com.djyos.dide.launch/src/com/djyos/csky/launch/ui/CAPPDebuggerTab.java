package com.djyos.csky.launch.ui;


import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.debug.ui.ICDebuggerPage;
import org.eclipse.cdt.dsf.gdb.internal.ui.launching.CDebuggerTab;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.djyos.csky.debug.core.DebugConfigurationSetting;


public class CAPPDebuggerTab extends CDebuggerTab{ 

	  public ScrolledComposite scrollContainer;
	  public Composite compContents;
	  public ICDebugConfiguration configSelect;
	  public static final String TAB_ID = "com.djyos.csky.launch.ui.CAPPDebuggerTab";
	  public static final String JTAGDEBUGGER_MODE_RUN = "jtag_run";

	  public CAPPDebuggerTab()
	  {
	    super(SessionType.REMOTE,false);
	  }	

	  
	  public void createControl(Composite parent)
	  {
	    scrollContainer = new ScrolledComposite(parent, 768);
	    scrollContainer.setLayoutData(new GridData(1808));
	    scrollContainer.setLayout(new FillLayout());
	    scrollContainer.setExpandHorizontal(true);
	    scrollContainer.setExpandVertical(true);

	    compContents = new Composite(scrollContainer, 0);
	    setControl(scrollContainer);
	    LaunchUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(getControl(), 
	      "org.eclipse.cdt.launch.launch_configuration_dialog_debugger_tab");
	    int numberOfColumns = fAttachMode ? 2 : 1;
	    GridLayout layout = new GridLayout(numberOfColumns, false);
	    compContents.setLayout(layout);
	    GridData gd = new GridData(1, 2, true, false);
	    compContents.setLayoutData(gd);

	    createOptionsComposite(compContents);

	    createDebuggerGroup(compContents, 2);

	    scrollContainer.setContent(compContents);
	  }

	  public void initializeFrom(ILaunchConfiguration config)
	  {
	    setInitializing(true);

	    setLaunchConfiguration(config);
	    ICDebuggerPage dynamicTab = getDynamicTab();
	    if (dynamicTab != null) {
	      dynamicTab.initializeFrom(config);
	    }
	    String id = "com.csky.cds.debug.core.CDSBareGDBCDebugger";

	    loadDebuggerPage(config, id);

	    initializeCommonControls(config);

	    setInitializing(false);
	  }

	  protected void loadDebuggerPage(ILaunchConfiguration config, String selection)
	  {
	    getPlatform(config);
	    ICDebugConfiguration[] debugConfigs = CDebugCorePlugin.getDefault().getActiveDebugConfigurations();

	    for (ICDebugConfiguration debugconfig : debugConfigs) {
	      if (debugconfig.getID().equalsIgnoreCase(selection)) {
	        configSelect = debugconfig;
	      }
	    }

	    handleDebuggerChanged();
	    updateLaunchConfigurationDialog();
	  }
	  protected ICDebugConfiguration getConfigForCurrentDebugger()
	  {
	    return configSelect;
	  }

	protected void contentsChanged()
	  {
	    scrollContainer.setMinSize(compContents.computeSize(-1, -1));
	  }

	  public void setDefaults(ILaunchConfigurationWorkingCopy config)
	  {
	    super.setDefaults(config);

	    DebugConfigurationSetting DebugConfigSetting = new DebugConfigurationSetting();
	    DebugConfigSetting.setDebuggerConfugrationDefaults(config);
	    DebugConfigSetting.setDebuggerMainTabConfugrationDefaults(config);
	    DebugConfigSetting.setDebuggerConnectionTabConfugrationDefaults(config);

	    config.setAttribute("server_mode", "jtag_run");
	  }
	
}
