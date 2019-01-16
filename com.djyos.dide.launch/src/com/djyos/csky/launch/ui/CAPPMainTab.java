package com.djyos.csky.launch.ui;


import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.djyos.csky.debug.core.DebugConfigurationSetting;
import com.djyos.csky.flash.download.ui.DownloadCfgPage;

import org.eclipse.cdt.launch.ui.CMainTab;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
//import org.eclipse.cdt.debug.core.CDebugUtils;
public class CAPPMainTab extends CMainTab {
	
	  public ILaunchConfiguration configs;	
	  private boolean isDebugInROMChecked = false;
	  private Button buttonDebugInROM;
	  private Button buttonExecutePreLoadScript;
	  private Label labelExecutePreLoadScript;
	  private Label labelExeFlashDriverInitScript;
	  private Label labelLoadFlashDriverFile;
	  private Label labelExeImageInitScript;
	  private Combo comboConnect;
	  private Label labelFirstReset;
	  private Button buttoneCosSystem;
	  private Button buttonLoadimage;
	  private Label labelLoadimage;
	  private Button buttonAutorun;
	  private Label labelConnect;
	  private Button buttonFirstReset;
	  private Combo comboFirstReset;
	  private Button buttonSecondReset;
	  private Label labelSecondReset;
	  private Combo comboSecondReset;
	  private String DriverPath = "";
	  private String DriverTemplate = "";
	  private String falshChipErase = "false";
	  private String falshEraseSector = "true";
	  private String falshDoNotErase = "false";
	  private String flashCommandTimeout = "60";
	  private Text textSResetCommand;
	  private Label labelSResetCommand;
	  private Group groupOption;
	  private Group groupLaunchSteps;
	  private Button buttonDriverFileSetting;
	  private Label labelDriverFile;
	  
	  public void createControl(Composite parent)
	  {
	    Composite comp = new Composite(parent, 0);
	    setControl(comp);
	    
	    GridLayout topLayout = new GridLayout();
	    comp.setLayout(topLayout);
	    createVerticalSpacer(comp, 1);

	    createProjectGroup(comp, 1);

	    createExeFileGroup(comp, 1);
	    createVerticalSpacer(comp, 1);

	    createGroupOption(comp);

	    createLaunchStepsGroup(comp);
	  }
	  

	  protected void createGroupOption(Composite parent)
	  {
	    Composite projComp = new Composite(parent, 0);
	    GridLayout projLayout = new GridLayout();
	    projLayout.numColumns = 1;
	    projComp.setLayout(projLayout);
	    GridData gd = new GridData(768);
	    projComp.setLayoutData(gd);

	    groupOption = new Group(projComp, 0);
	    groupOption.setText("Options");
	    groupOption.setEnabled(true);
	    GridLayout projLayout1 = new GridLayout(2, true);
	    groupOption.setLayout(projLayout1);
	    gd = new GridData(768);
	    groupOption.setLayoutData(gd);

	    creatDebugInROM(groupOption);

	    createeCosSystem(groupOption, 1);

	    createAutoRunOption(groupOption, 1);

	    createSResetCommand(groupOption);

	    createDriverFile(groupOption);
	  }

	  protected void createLaunchStepsGroup(Composite parent)
	  {
	    Composite projComp = new Composite(parent, 0);
	    GridLayout projLayout = new GridLayout();
	    projLayout.numColumns = 1;
	    projComp.setLayout(projLayout);
	    GridData gd = new GridData(768);
	    projComp.setLayoutData(gd);

	    groupLaunchSteps = new Group(projComp, 0);
	    groupLaunchSteps.setText("Launch Steps");
	    groupLaunchSteps.setEnabled(true);
	    GridLayout projLayout1 = new GridLayout();
	    projLayout1.numColumns = 2;
	    groupLaunchSteps.setLayout(projLayout1);
	    gd = new GridData(768);
	    gd.horizontalSpan = 1;
	    groupLaunchSteps.setLayoutData(gd);

	    createLaunchSteps(groupLaunchSteps);
	  }
	  protected void createLaunchSteps(Composite parent)
	  {
	    Composite launchcomp1 = new Composite(parent, 0);
	    GridLayout layout = new GridLayout();
	    layout.numColumns = 1;
	    layout.marginWidth = 0;
	    layout.marginHeight = 0;
	    launchcomp1.setLayout(layout);

	    int steps = 1;
	    createConnectOption(launchcomp1, steps);
	    steps++;

	    labelExeFlashDriverInitScript = createLaunchStepsLabel(launchcomp1, 
	      steps + "." + Messages.CAPPMainTab_12);
	    steps++;

	    labelLoadFlashDriverFile = createLaunchStepsLabel(launchcomp1, 
	      steps + "." + Messages.CAPPMainTab_13);
	    steps++;

	    createExePreLoadScriptOption(launchcomp1, steps);
	    steps++;

	    labelExeImageInitScript = createLaunchStepsLabel(launchcomp1, 
	      steps + "." + Messages.CAPPMainTab_16);
	    steps++;

	    Composite launchcomp2 = new Composite(parent, 0);
	    layout = new GridLayout();
	    layout.numColumns = 1;
	    layout.marginWidth = 0;
	    launchcomp2.setLayout(layout);
	    GridData gd = new GridData(1040);
	    launchcomp2.setLayoutData(gd);

	    createLoadimageOption(launchcomp2, steps);
	    steps++;

	    createFirstResetOption(launchcomp2, steps);
	    steps++;

	    createSecondResetOption(launchcomp2, steps);
	  }

	  protected Label createLaunchStepsLabel(Composite parent, String str)
	  {
	    Composite comp = new Composite(parent, 0);
	    GridLayout compLayout = new GridLayout();
	    compLayout.numColumns = 1;

	    compLayout.marginWidth = 20;
	    comp.setLayout(compLayout);

	    Label label = new Label(comp, 0);
	    label.setText(str);

	    return label;
	  }

	  protected void createFirstResetOption(Composite parent, int steps)
	  {
	    Composite comp = new Composite(parent, 0);
	    GridLayout layout = new GridLayout();
	    layout.numColumns = 3;
	    layout.marginWidth = 0;
	    comp.setLayout(layout);

	    buttonFirstReset = createCheckButton(comp, steps + ".");
	    GridData gd = new GridData(1);
	    gd.widthHint = 24;
	    buttonFirstReset.setLayoutData(gd);
	    buttonFirstReset.addSelectionListener(new SelectionAdapter() {
	      public void widgetSelected(SelectionEvent evt) {
	        updateLaunchConfigurationDialog();
	        updateFirstResetStatus(buttonFirstReset.getSelection());
	      }
	    });
	    labelFirstReset = new Label(comp, 0);
	    gd = new GridData(768);
	    labelFirstReset.setLayoutData(gd);
	    labelFirstReset.setText(Messages.CAPPMainTab_17);

	    comboFirstReset = new Combo(comp, 12);
	    comboFirstReset.add("Soft Reset", 0);
	    comboFirstReset.add("Hard Reset", 1);
	    comboFirstReset.setText("Hard Reset");

	    comboFirstReset.addSelectionListener(new SelectionAdapter() {
	      public void widgetSelected(SelectionEvent evt) {
	        updateLaunchConfigurationDialog();
	      }
	    });
	  }

	  protected void createSecondResetOption(Composite parent, int steps)
	  {
	    Composite comp = new Composite(parent, 0);
	    GridLayout layout = new GridLayout();
	    layout.numColumns = 3;
	    layout.marginWidth = 0;
	    comp.setLayout(layout);

	    buttonSecondReset = createCheckButton(comp, steps + ".");
	    GridData gd = new GridData(1);
	    gd.widthHint = 24;
	    buttonSecondReset.setLayoutData(gd);
	    buttonSecondReset.addSelectionListener(new SelectionAdapter() {
	      public void widgetSelected(SelectionEvent evt) {
	        updateLaunchConfigurationDialog();
	        updateSecondResetStatus(buttonSecondReset.getSelection());
	      }
	    });
	    labelSecondReset = new Label(comp, 0);
	    gd = new GridData(768);
	    labelSecondReset.setLayoutData(gd);
	    labelSecondReset.setText(Messages.CAPPMainTab_18);

	    comboSecondReset = new Combo(comp, 12);
	    comboSecondReset.add("Soft Reset", 0);
	    comboSecondReset.add("Hard Reset", 1);
	    comboSecondReset.setText("Soft Reset");

	    comboSecondReset.addSelectionListener(new SelectionAdapter() {
	      public void widgetSelected(SelectionEvent evt) {
	        updateLaunchConfigurationDialog();
	      }
	    });
	  }

	  protected void createeCosSystem(Composite parent, int colSpan)
	  {
	    Composite mainComp = new Composite(parent, 0);
	    GridLayout mainLayout = new GridLayout();
	    mainLayout.numColumns = 1;
	    mainLayout.marginHeight = 0;
	    mainLayout.marginWidth = 0;
	    mainComp.setLayout(mainLayout);
	    GridData gd = new GridData(768);
	    mainComp.setLayoutData(gd);

	    buttoneCosSystem = createCheckButton(mainComp, Messages.CAPPMainTab_eCosSystem);
	    buttoneCosSystem.addSelectionListener(new SelectionAdapter()
	    {
	      public void widgetSelected(SelectionEvent evt) {
	         updateLaunchConfigurationDialog();
	      }
	    });
	  }

	  protected void createConnectOption(Composite parent, int steps)
	  {
	    Composite comp = new Composite(parent, 0);
	    GridLayout layout = new GridLayout();
	    layout.numColumns = 2;
	    layout.marginWidth = 20;
	    comp.setLayout(layout);
	    labelConnect = new Label(comp, 0);
	    labelConnect.setText(steps + "." + Messages.CAPPMainTab_11);

	    comboConnect = new Combo(comp, 12);
	    comboConnect.add("Normal", 0);
	    comboConnect.add("Hard Reset", 1);
	    comboConnect.setText("Normal");

	    comboConnect.addSelectionListener(new SelectionAdapter() {
	      public void widgetSelected(SelectionEvent evt) {
	        updateLaunchConfigurationDialog();
	      }
	    });
	  }

	  protected void createExePreLoadScriptOption(Composite parent, int steps)
	  {
	    Composite comp = new Composite(parent, 0);
	    GridLayout layout = new GridLayout();
	    layout.numColumns = 2;
	    comp.setLayout(layout);

	    buttonExecutePreLoadScript = createCheckButton(comp, steps + ".");
	    GridData gd = new GridData(1);
	    gd.widthHint = 24;
	    buttonExecutePreLoadScript.setLayoutData(gd);
	   	    
	    buttonExecutePreLoadScript.addSelectionListener(new SelectionAdapter() {
	      public void widgetSelected(SelectionEvent evt) {
	        labelExecutePreLoadScript.setEnabled(buttonExecutePreLoadScript.getSelection());
	        updateLaunchConfigurationDialog();
	      }
	    });

	    labelExecutePreLoadScript = new Label(comp, 0);
	    gd = new GridData(768);
	    labelExecutePreLoadScript.setLayoutData(gd);
	    labelExecutePreLoadScript.setText(Messages.CAPPMainTab_14);
	    
	  }

	  protected void createLoadimageOption(Composite parent, int steps)
	  {
	    Composite mainComp = new Composite(parent, 0);
	    GridLayout mainLayout = new GridLayout();
	    mainLayout.numColumns = 2;
	    mainLayout.marginHeight = 0;
	    mainLayout.marginWidth = 0;
	    mainComp.setLayout(mainLayout);
	    GridData gd = new GridData(768);
	    mainComp.setLayoutData(gd);

	    buttonLoadimage = createCheckButton(mainComp, steps + ".");
	    gd = new GridData(1);
	    gd.widthHint = 24;
	    buttonLoadimage.setLayoutData(gd);
	    
	    buttonLoadimage.addSelectionListener(new SelectionAdapter()
	    {
	      public void widgetSelected(SelectionEvent evt) {
	        labelLoadimage.setEnabled(buttonLoadimage.getSelection());
	        updateLaunchConfigurationDialog();
	      }
	    });
	    labelLoadimage = new Label(mainComp, 0);
	    gd = new GridData(768);
	    labelLoadimage.setLayoutData(gd);
	    labelLoadimage.setText(Messages.CAPPMainTab_1);
	  }

	  protected void createAutoRunOption(Composite parent, int colSpan)
	  {
	    Composite mainComp = new Composite(parent, 0);
	    GridLayout mainLayout = new GridLayout();
	    mainLayout.numColumns = 1;
	    mainLayout.marginHeight = 0;
	    mainLayout.marginWidth = 0;
	    mainComp.setLayout(mainLayout);
	    GridData gd = new GridData(768);
	    mainComp.setLayoutData(gd);

	    buttonAutorun = createCheckButton(mainComp, Messages.CAPPMainTab_2);
	    buttonAutorun.addSelectionListener(new SelectionAdapter()
	    {
	      public void widgetSelected(SelectionEvent evt) {
	        updateLaunchConfigurationDialog();
	      }
	    });
	  }

	  protected void createSResetCommand(Composite parent)
	  {
	    Composite mainComp = new Composite(parent, 0);
	    GridLayout mainLayout = new GridLayout();
	    mainLayout.numColumns = 2;
	    mainComp.setLayout(mainLayout);
	    GridData gd = new GridData(768);
	    mainComp.setLayoutData(gd);

	    labelSResetCommand = new Label(mainComp, 0);
	    labelSResetCommand.setText("SReset Command: 0x");

	    textSResetCommand = new Text(mainComp, 2052);
	    gd = new GridData(768);
	    textSResetCommand.setLayoutData(gd);
	    textSResetCommand.setTextLimit(8);
	    textSResetCommand.addVerifyListener(new VerifyListener() {
	      public void verifyText(VerifyEvent e) {
	        textControl(e);
	      }
	    });
	    textSResetCommand.addModifyListener(new ModifyListener() {
	      public void modifyText(ModifyEvent evt) {
	        updateLaunchConfigurationDialog();
	      }
	    });
	  }

	  private void textControl(VerifyEvent e)
	  {
	    Pattern pattern = Pattern.compile("[0-9a-fA-F]*");
	    Matcher matcher = pattern.matcher(e.text);
	    if (matcher.matches())
	      e.doit = true;
	    else if (e.text.length() > 0)
	      e.doit = false;
	    else
	      e.doit = true;
	  }

	  private void creatDebugInROM(Composite comp)
	  {
	    Composite projComp = new Composite(comp, 0);
	    GridLayout projLayout = new GridLayout();
	    projLayout.numColumns = 1;
	    projLayout.marginHeight = 0;
	    projLayout.marginWidth = 0;
	    projComp.setLayout(projLayout);
	    GridData gd = new GridData(768);

	    projComp.setLayoutData(gd);

	    buttonDebugInROM = createCheckButton(projComp, Messages.CAPPMainTab_5);
	    buttonDebugInROM.setSelection(false);
	    gd = new GridData();
	    gd.horizontalSpan = 1;
	    buttonDebugInROM.setLayoutData(gd);	   
	    buttonDebugInROM.addSelectionListener(new SelectionAdapter() {
	      public void widgetSelected(SelectionEvent evt) {
	        isDebugInROMChecked = buttonDebugInROM.getSelection();
	        setDebugGroup(isDebugInROMChecked);
	        updateLaunchConfigurationDialog();
	      }
	    });
	  }

	  private void createDriverFile(Group group)
	  {
	    Composite projComp = new Composite(group, 0);
	    GridLayout projLayout = new GridLayout();
	    projLayout.numColumns = 3;
	    projComp.setLayout(projLayout);
	    GridData gd = new GridData(768);
	    gd.horizontalSpan = 3;
	    projComp.setLayoutData(gd);
	    labelDriverFile = new Label(projComp, 0);
	    labelDriverFile.setText(Messages.CAPPMainTab_6);

	    buttonDriverFileSetting = createPushButton(projComp, Messages.CAPPMainTab_Setting, null);
	    buttonDriverFileSetting.addSelectionListener(new SelectionAdapter() {
	      public void widgetSelected(SelectionEvent evt) {
	        IProject project = null;
	        try {
	          project = getSelProject();
	        } catch (CoreException e) {
	          Display.getDefault().asyncExec(new Runnable() {
	            public void run() {
	              MessageDialog.openError(null, "", e.getMessage());
	            } } );
	        }
	        if (project == null) {
	          return;
	        }
	        DownloadCfgPage.setbUserFlash(false);
	        PreferencesUtil.createPropertyDialogOn(getShell(), project, 
	          "com.djyos.csky.flash.download.ui.DownloadCfgPage", new String[] { "com.djyos.csky.flash.download.ui.DownloadCfgPage" }, null).open();
	        DownloadCfgPage.setbUserFlash(true);
	        ICProjectDescription descriptor = getProjectDescription(project, false);
	        try
	        {
	          ICStorageElement rootElement = descriptor.getStorage("DebugLaunch", false);
	          if (rootElement != null) {
	            ICStorageElement[] flashconfigElements = rootElement.getChildrenByName("flash");
	            if ((flashconfigElements != null) && (flashconfigElements.length > 0))
	            {
	              DriverPath = flashconfigElements[0].getAttribute(Messages.CAPPMainTab_AttrCheck_16);
	              DriverTemplate = flashconfigElements[0].getAttribute(Messages.CAPPMainTab_AttrCheck_17);
	              falshChipErase = flashconfigElements[0].getAttribute(Messages.CAPPMainTab_Flash_Chip_Erase);
	              falshEraseSector = flashconfigElements[0].getAttribute(Messages.CAPPMainTab_Flash_Erase_Sector);
	              falshDoNotErase = flashconfigElements[0].getAttribute(Messages.CAPPMainTab_Flash_Do_Not_Erase);

	              String strCT = flashconfigElements[0].getAttribute(Messages.CAPPMainTab_Flash_Command_Timeout);
	              if (strCT != null)
	                flashCommandTimeout = strCT;
	            }
	          }
	        }
	        catch (CoreException e) {
	          e.printStackTrace();
	        }

	        updateLaunchConfigurationDialog();
	      }

	      private IProject getSelProject() throws CoreException {
	        ICProject cproject = CoreModel.getDefault().getCModel().getCProject(configs
	          .getAttribute("org.eclipse.cdt.launch.PROJECT_ATTR", ""));
	        return cproject == null ? null : cproject.getProject();
	      }
	    });
	    
	  }

	  private void handleROMGroupState(ILaunchConfiguration config)
	  {
	    buttonDebugInROM.setSelection(isDebugInROMChecked);

	    setDebugGroup(buttonDebugInROM.getSelection());
	  }

	  private void setDebugGroup(boolean sel) {
	    boolean bLockLaunchSteps = false;
	    try {
	      bLockLaunchSteps = configs.getAttribute("LockLaunchSteps", false);
	    } catch (CoreException e) {
	      e.printStackTrace();
	    }
	    if (!bLockLaunchSteps) {
	      buttonExecutePreLoadScript.setEnabled(sel);
	      labelExecutePreLoadScript.setEnabled(buttonExecutePreLoadScript.getSelection());
	      labelExeFlashDriverInitScript.setEnabled(sel);
	      labelLoadFlashDriverFile.setEnabled(sel);
	      buttonDriverFileSetting.setEnabled(sel);
	      labelDriverFile.setEnabled(sel);
	    }
	  }

	  public void initializeFrom(ILaunchConfiguration config)
	  {
		 this.configs = config;

	    updateProjectFromConfig(config);
	    updateProgramFromConfig(config);
	    updateeCosSystemFromConfig(config);
	    updatePreloadScriptFromConfig(config);
	    updateLoadimageFromConfig(config);
	    updateAutorunFromConfig(config);
	    updateDebugInROMConfig(config);
	    updateConnectFromConfig(config);
	    updateLaunchStepsFromConfig(config);

	    this.configs = config;
	  }

	  protected void updateConnectFromConfig(ILaunchConfiguration config)
	  {
	    String ConnectMode = "Normal";
	    try {
	      ConnectMode = config.getAttribute("ConnectMode", "Normal");
	    } catch (CoreException e) {
	      Display.getDefault().asyncExec(new Runnable() {
	        public void run() {
	          MessageDialog.openError(null, Messages.CAPPMainTab_ERROR, e.getMessage());
	        }
	      });
	    }
	    comboConnect.setText(ConnectMode);
	  }

	  protected void updateFirstResetStatus(boolean sel)
	  {
	    labelFirstReset.setEnabled(sel);
	    comboFirstReset.setEnabled(sel);
	  }

	  protected void updateSecondResetStatus(boolean sel)
	  {
	    labelSecondReset.setEnabled(sel);
	    comboSecondReset.setEnabled(sel);
	  }

	  protected void updateLaunchStepsFromConfig(ILaunchConfiguration config)
	  {
	    boolean bLockLaunchOption = false;
	    boolean bLockLaunchSteps = false;

	    boolean usereset1 = false;
	    boolean usereset2 = false;
	    boolean lockreset1 = false;
	    boolean lockreset2 = false;
	    boolean locksresetcommand = false;
	    boolean lockconnect = false;
	    String ResetMode1 = "";
	    String ResetMode2 = "";
	    String SwResetCommand = "0";
	    try {
	      bLockLaunchOption = config.getAttribute("LockLaunchOption", false);
	      bLockLaunchSteps = config.getAttribute("LockLaunchSteps", false);

	      usereset1 = config.getAttribute("FirstReset", false);
	      usereset2 = config.getAttribute("SecondReset", false);
	      lockreset1 = config.getAttribute("Lock First Reset", false);
	      lockreset2 = config.getAttribute("Lock Second Reset", false);
	      ResetMode1 = config.getAttribute("FirstReset mode", "Hard Reset");
	      ResetMode2 = config.getAttribute("SecondReset mode", "Soft Reset");
	      SwResetCommand = config.getAttribute("SReset Command", "0");
	      locksresetcommand = config.getAttribute("Lock Soft Reset Command", false);
	      lockconnect = config.getAttribute("Lock Connect mode selection", false);
	    } catch (CoreException e) {
	      Display.getDefault().asyncExec(new Runnable() {
	        public void run() {
	          MessageDialog.openError(null, Messages.CAPPMainTab_ERROR, e.getMessage());
	        }
	      });
	    }
	    buttonDebugInROM.setEnabled(!bLockLaunchOption);
	    buttoneCosSystem.setEnabled(!bLockLaunchOption);
	    buttonAutorun.setEnabled(!bLockLaunchOption);

	    textSResetCommand.setText(SwResetCommand);
	    textSResetCommand.setEnabled(!locksresetcommand);
	    labelSResetCommand.setEnabled(!locksresetcommand);

	    labelConnect.setEnabled(!bLockLaunchSteps);
	    comboConnect.setEnabled((!lockconnect) && (!bLockLaunchSteps));

	    labelExeFlashDriverInitScript.setEnabled(!bLockLaunchSteps);

	    labelLoadFlashDriverFile.setEnabled(!bLockLaunchSteps);

	    buttonExecutePreLoadScript.setEnabled(!bLockLaunchSteps);
	    labelExecutePreLoadScript.setEnabled(!bLockLaunchSteps);

	    labelExeImageInitScript.setEnabled(!bLockLaunchSteps);

	    buttonLoadimage.setEnabled(!bLockLaunchSteps);
	    labelLoadimage.setEnabled(!bLockLaunchSteps);

	    buttonFirstReset.setSelection(usereset1);
	    buttonFirstReset.setEnabled((!lockreset1) && (!bLockLaunchSteps));
	    labelFirstReset.setEnabled((usereset1) && (!bLockLaunchSteps));
	    comboFirstReset.setText(ResetMode1);
	    comboFirstReset.setEnabled((usereset1) && (!lockreset1) && (!bLockLaunchSteps));

	    buttonSecondReset.setSelection(usereset2);
	    buttonSecondReset.setEnabled((!lockreset2) && (!bLockLaunchSteps));
	    labelSecondReset.setEnabled((usereset2) && (!bLockLaunchSteps));
	    comboSecondReset.setText(ResetMode2);
	    comboSecondReset.setEnabled((usereset2) && (!lockreset2) && (!bLockLaunchSteps));

	    if (!bLockLaunchSteps)
	      setDebugGroup(buttonDebugInROM.getSelection());
	  }

	  protected void updateeCosSystemFromConfig(ILaunchConfiguration config)
	  {
	    boolean useeCosSystem = false;
	    try {
	      useeCosSystem = config.getAttribute("eCosDebug", false);
	    } catch (CoreException e) {
	      Display.getDefault().asyncExec(new Runnable() {
	        public void run() {
	          MessageDialog.openError(null, Messages.CAPPMainTab_ERROR, e.getMessage());
	        } } );
	    }
	    buttoneCosSystem.setSelection(useeCosSystem);
	  }

	  protected void updatePreloadScriptFromConfig(ILaunchConfiguration config)
	  {
	    boolean bExePreloadScript = false;
	    try {
	      bExePreloadScript = config.getAttribute("Execute preload script", false);
	    } catch (CoreException e) {
	      Display.getDefault().asyncExec(new Runnable() {
	        public void run() {
	          MessageDialog.openError(null, Messages.CAPPMainTab_ERROR, e.getMessage());
	        }
	      });
	    }
	    buttonExecutePreLoadScript.setSelection(bExePreloadScript);
	  }

	  protected void updateLoadimageFromConfig(ILaunchConfiguration config)
	  {
	    boolean useLoadimage = true;
	    try {
	      useLoadimage = config.getAttribute("use_loadimage", true);
	    } catch (CoreException e) {
	      Display.getDefault().asyncExec(new Runnable() {
	        public void run() {
	          MessageDialog.openError(null, Messages.CAPPMainTab_ERROR, e.getMessage());
	        } } );
	    }
	    buttonLoadimage.setSelection(useLoadimage);
	  }

	  protected void updateAutorunFromConfig(ILaunchConfiguration config)
	  {
	    boolean useAutorun = true;
	    try {
	      useAutorun = config.getAttribute("use_autorun", true);
	    } catch (CoreException e) {
	      Display.getDefault().asyncExec(new Runnable() {
	        public void run() {
	          MessageDialog.openError(null, Messages.CAPPMainTab_ERROR, e.getMessage());
	        } } );
	    }
	    buttonAutorun.setSelection(useAutorun);
	  }

	  private void updateDebugInROMConfig(ILaunchConfiguration config)
	  {
	    isDebugInROMChecked = false;
	    try
	    {
	      isDebugInROMChecked = config.getAttribute("DebugInFlash", IsDebugInROM(config));
	    } catch (CoreException e) {
	      Display.getDefault().asyncExec(new Runnable() {
	        public void run() {
	          MessageDialog.openError(null, Messages.CAPPMainTab_ERROR, e.getMessage());
	        } } );
	    }
	    handleROMGroupState(config);
	  }

	  private boolean IsDebugInROM(ILaunchConfiguration config)
	  {
	    boolean isDebugInROM = false;
	    try {
	      ICProject cProject = DebugConfigurationSetting.getCProject(config);
	      if (cProject == null) {
	        return isDebugInROM;
	      }
	      IProject project = cProject.getProject();

	      ICProjectDescription descriptor = getProjectDescription(project, false);
	      ICStorageElement rootElement = descriptor.getStorage("DebugLaunch", false);
	      if (rootElement != null) {
	        ICStorageElement[] configElements = rootElement.getChildrenByName("launch");
	        if ((configElements != null) && (configElements.length > 0) && 
	          (configElements[0].getAttribute(Messages.CAPPMainTab_AttrCheck_1) != null) && 
	          (configElements[0].getAttribute(Messages.CAPPMainTab_AttrCheck_1).equalsIgnoreCase("true"))) {
	          isDebugInROM = true;
	        }

	        ICStorageElement[] flashconfigElements = rootElement.getChildrenByName("flash");
	        if ((flashconfigElements != null) && (flashconfigElements.length > 0))
	        {
	          DriverPath = flashconfigElements[0].getAttribute(Messages.CAPPMainTab_AttrCheck_16);
	          DriverTemplate = flashconfigElements[0].getAttribute(Messages.CAPPMainTab_AttrCheck_17);
	        }
	      }
	    }
	    catch (CoreException e)
	    {
	      e.printStackTrace();
	    }

	    return isDebugInROM;
	  }

	  public boolean isValid(ILaunchConfiguration config)
	  {
	    if (!super.isValid(config)) {
	      return false;
	    }
	    try
	    {
	      if ((config.getAttribute("DebugInFlash", false)) && 
	        (config.getAttribute("debug_on_simulator", false))) {
	        setErrorMessage(Messages.CAPPMainTab_DebugInRomErr);
	        return false;
	      }
	      if ((config.getAttribute("eCosDebug", false)) && 
	        (config.getAttribute("debug_on_simulator", false))) {
	        setErrorMessage(Messages.CAPPMainTab_eCosSystemErr);
	        return false;
	      }
	      String connectmode = config.getAttribute("ConnectMode", "Normal");
	      if ((connectmode.equals("Hard Reset")) && 
	        (config.getAttribute("debug_on_simulator", false))) {
	        setErrorMessage(Messages.CAPPMainTab_ConnectHardResetErr);
	        return false;
	      }
	    } catch (CoreException e) {
	      e.printStackTrace();

	      setErrorMessage(null);
	      setMessage(null);
	    }
	    return true;
	  }

	  public void performApply(ILaunchConfigurationWorkingCopy config)
	  {
	    ICProject cProject = getCProject();
	    if ((cProject != null) && (cProject.exists()))
	    {
	      config.setMappedResources(new IResource[] { cProject.getProject() });
	    }
	    else
	    {
	      config.setMappedResources(null);
	    }

	    config.setAttribute("org.eclipse.cdt.launch.PROJECT_ATTR", fProjText.getText());

	    config.setAttribute("org.eclipse.cdt.launch.PROGRAM_NAME", fProgText.getText());

	    if (buttoneCosSystem.getSelection())
	      config.setAttribute("eCosDebug", true);
	    else {
	      config.setAttribute("eCosDebug", false);
	    }

	    config.setAttribute("use_loadimage", buttonLoadimage.getSelection());

	    config.setAttribute("use_autorun", buttonAutorun.getSelection());

	    config.setAttribute("DebugInFlash", buttonDebugInROM.getSelection());

	    config.setAttribute("Execute preload script", buttonExecutePreLoadScript.getSelection());

	    config.setAttribute("ConnectMode", comboConnect.getText());

	    if (buttonFirstReset.getSelection()) {
	      config.setAttribute("FirstReset", true);
	    }
	    else
	    {
	      config.setAttribute("FirstReset", false);
	    }
	    config.setAttribute("FirstReset mode", comboFirstReset.getText());

	    if (buttonSecondReset.getSelection()) {
	      config.setAttribute("SecondReset", true);
	    }
	    else
	    {
	      config.setAttribute("SecondReset", false);
	    }
	    config.setAttribute("SecondReset mode", comboSecondReset.getText());
	    config.setAttribute("SReset Command", textSResetCommand.getText());

	    config.setAttribute("DriverFilePath", DriverPath);
	    config.setAttribute("DriverTemplate", DriverTemplate);

	    config.setAttribute("chip_erase", Boolean.valueOf(falshChipErase).booleanValue());
	    config.setAttribute("erase_sector", Boolean.valueOf(falshEraseSector).booleanValue());
	    config.setAttribute("do_not_erase", Boolean.valueOf(falshDoNotErase).booleanValue());
	    config.setAttribute("command_timeout", flashCommandTimeout);
	  }

	  public void setDefaults(ILaunchConfigurationWorkingCopy config)
	  {
	    super.setDefaults(config);

	    DebugConfigurationSetting DebugConfigSetting = new DebugConfigurationSetting();
	    DebugConfigSetting.setConfugrationDefaults(config);
	  }

	  protected ICProjectDescription getProjectDescription(IProject project, boolean write)
	  {
	    return CCorePlugin.getDefault().getProjectDescription(project, write);
	  }
}
