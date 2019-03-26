package com.djyos.dide.debug.gdbjtag.csky.ui;

import java.io.File;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConstants;
import org.eclipse.cdt.debug.gdbjtag.ui.GDBJtagImages;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;

import ilg.gnuarmeclipse.debug.gdbjtag.data.CProjectAttributes;

import com.djyos.dide.debug.gdbjtag.csky.Activator;
import com.djyos.dide.debug.gdbjtag.csky.ConfigurationAttributes;
import com.djyos.dide.debug.gdbjtag.csky.DefaultPreferences;
import com.djyos.dide.debug.gdbjtag.csky.PersistentPreferences;

/**
 * @since 7.0
 */
public class TabDebugger extends AbstractLaunchConfigurationTab {

	// ------------------------------------------------------------------------

	private static final String TAB_NAME = "Debugger";
	private static final String TAB_ID = Activator.PLUGIN_ID + ".ui.debuggertab";

	// ------------------------------------------------------------------------

	private Button fDoStartGdbServer;
	private Text fGdbClientExecutable;
	private Text fGdbClientOtherOptions;
	private Text fGdbClientOtherCommands;

	private Button fDoConnectToRunning;
	//private Button fEnableSemihosting;
	//private Text fSemihostingCmdline;

//	private Button fDisableGraphics;
	private Button fUseDDC;

	private Text fTargetIpAddress;
	private Text fTargetPortNumber;

//	private Text fCSkyBoardName;
//	private Text fCSkyDeviceName;
	private Text fIceClkText;
	private Text fDelayMtcrText;
//	private Button fIsCSkyVerbose;

	private Text fGdbServerGdbPort;

	private Text fGdbServerExecutable;
	private Button fGdbServerBrowseButton;
	private Button fGdbServerVariablesButton;

	private Text fGdbServerOtherOptions;

//	private Button fDoGdbServerAllocateConsole;

	protected Button fUpdateThreadlistOnSuspend;

	// TODO: check usage
	protected String fSavedCmsisDeviceName;
	protected String fSavedCmsisBoardName;

	protected String fProjectName;
	
	private TabStartup fTabStartup;

	// ------------------------------------------------------------------------

	protected TabDebugger(TabStartup tabStartup) {
		super();

		fTabStartup = tabStartup;
		fSavedCmsisDeviceName = null;
		fSavedCmsisBoardName = null;
	}

	@Override
	public String getName() {
		return TAB_NAME;
	}

	@Override
	public Image getImage() {
		return GDBJtagImages.getDebuggerTabImage();
	}

	@Override
	public void createControl(Composite parent) {

		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		GridLayout layout = new GridLayout();
		comp.setLayout(layout);

		createGdbServerGroup(comp);

		createGdbClientControls(comp);

		createRemoteControl(comp);

		fUpdateThreadlistOnSuspend = new Button(comp, SWT.CHECK);
		fUpdateThreadlistOnSuspend.setText(Messages.getString("DebuggerTab.update_thread_list_on_suspend_Text"));
		fUpdateThreadlistOnSuspend
				.setToolTipText(Messages.getString("DebuggerTab.update_thread_list_on_suspend_ToolTipText"));

		Link restoreDefaults;
		GridData gd;
		{
			restoreDefaults = new Link(comp, SWT.NONE);
			restoreDefaults.setText(Messages.getString("DebuggerTab.restoreDefaults_Link"));
			restoreDefaults.setToolTipText(Messages.getString("DebuggerTab.restoreDefaults_ToolTipText"));

			gd = new GridData();
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = SWT.RIGHT;
			gd.horizontalSpan = ((GridLayout) comp.getLayout()).numColumns;
			restoreDefaults.setLayoutData(gd);
		}

		// --------------------------------------------------------------------

		fUpdateThreadlistOnSuspend.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		restoreDefaults.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				initializeFromDefaults();
				scheduleUpdateJob();
			}
		});
	}

	private void browseButtonSelected(String title, Text text) {
		FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
		dialog.setText(title);
		String str = text.getText().trim();
		int lastSeparatorIndex = str.lastIndexOf(File.separator);
		if (lastSeparatorIndex != -1)
			dialog.setFilterPath(str.substring(0, lastSeparatorIndex));
		str = dialog.open();
		if (str != null)
			text.setText(str);
	}

	private void variablesButtonSelected(Text text) {
		StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
		if (dialog.open() == StringVariableSelectionDialog.OK) {
			text.insert(dialog.getVariableExpression());
		}
	}

	@SuppressWarnings("unused")
	private void createOptionsControl(Composite parent) {

		Group group = new Group(parent, SWT.NONE);
		group.setText(Messages.getString("DebuggerTab.interfaceGroup_Text"));
		GridLayout layout = new GridLayout();
		group.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);

		Composite comp = new Composite(group, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		comp.setLayout(layout);
	}

	private void createGdbServerGroup(Composite parent) {

		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		group.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		group.setText(Messages.getString("DebuggerTab.gdbServerGroup_Text"));

		Composite comp = new Composite(group, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 5;
		layout.marginHeight = 0;
		comp.setLayout(layout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		comp.setLayoutData(gd);

		Label label;
		{
			Composite local = new Composite(comp, SWT.NONE);
			layout = new GridLayout();
			layout.numColumns = 2;
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			local.setLayout(layout);

			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = ((GridLayout) comp.getLayout()).numColumns;
			local.setLayoutData(gd);

			{
				fDoStartGdbServer = new Button(local, SWT.CHECK);
				fDoStartGdbServer.setText(Messages.getString("DebuggerTab.doStartGdbServer_Text"));
				fDoStartGdbServer.setToolTipText(Messages.getString("DebuggerTab.doStartGdbServer_ToolTipText"));
				gd = new GridData(GridData.FILL_HORIZONTAL);
				fDoStartGdbServer.setLayoutData(gd);

				fDoConnectToRunning = new Button(local, SWT.CHECK);
				fDoConnectToRunning.setText(Messages.getString("DebuggerTab.noReset_Text"));
				fDoConnectToRunning.setToolTipText(Messages.getString("DebuggerTab.noReset_ToolTipText"));
				gd = new GridData(GridData.FILL_HORIZONTAL);
				fDoConnectToRunning.setLayoutData(gd);
			}
		}

		{
			label = new Label(comp, SWT.NONE);
			label.setText(Messages.getString("DebuggerTab.gdbServerExecutable_Label"));
			label.setToolTipText(Messages.getString("DebuggerTab.gdbServerExecutable_ToolTipText"));

			Composite local = new Composite(comp, SWT.NONE);
			layout = new GridLayout();
			layout.numColumns = 3;
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			local.setLayout(layout);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = ((GridLayout) comp.getLayout()).numColumns - 1;
			local.setLayoutData(gd);
			{
				fGdbServerExecutable = new Text(local, SWT.SINGLE | SWT.BORDER);
				gd = new GridData(GridData.FILL_HORIZONTAL);
				fGdbServerExecutable.setLayoutData(gd);

				fGdbServerBrowseButton = new Button(local, SWT.NONE);
				fGdbServerBrowseButton.setText(Messages.getString("DebuggerTab.gdbServerExecutableBrowse"));

				fGdbServerVariablesButton = new Button(local, SWT.NONE);
				fGdbServerVariablesButton.setText(Messages.getString("DebuggerTab.gdbServerExecutableVariable"));
			}
		}
		
		{
			label = new Label(comp, SWT.NONE);
			label.setText(Messages.getString("DebuggerTab.gdbServerClk_Label"));
			
			Composite comp2 = new Composite(comp, SWT.NONE);
			layout = new GridLayout();
			layout.numColumns = 2;
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			comp2.setLayout(layout);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = ((GridLayout) comp.getLayout()).numColumns - 1;
			comp2.setLayoutData(gd);
			{
				fIceClkText = new Text(comp2, SWT.SINGLE | SWT.BORDER);
				gd = new GridData();
				gd.widthHint = 100;
				gd.horizontalSpan = ((GridLayout) comp2.getLayout()).numColumns - 1;
				fIceClkText.setLayoutData(gd);
	
			    label = new Label(comp2, SWT.NONE);
			    label.setText("KHZ"); //$NON-NLS-1$					 

			}
		}
			
		{
			label = new Label(comp, SWT.NONE);
			label.setText(Messages.getString("DebuggerTab.gdbServerDelayMtcr_Label"));
		
			Composite comp2 = new Composite(comp, SWT.NONE);
			layout = new GridLayout();
			layout.numColumns =2;
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			comp2.setLayout(layout);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = ((GridLayout) comp.getLayout()).numColumns - 1;
			comp2.setLayoutData(gd);
			{
				fDelayMtcrText = new Text(comp2, SWT.SINGLE | SWT.BORDER);
				gd = new GridData();
				gd.widthHint = 100;
				gd.horizontalSpan = ((GridLayout) comp2.getLayout()).numColumns - 1;
				fDelayMtcrText.setLayoutData(gd);
	
			    label = new Label(comp2, SWT.NONE);
			    label.setText("ms"); //$NON-NLS-1$							
			}
		}

		{
			label = new Label(comp, SWT.NONE);
			label.setText(Messages.getString("DebuggerTab.gdbServerGdbPort_Label"));
			label.setToolTipText(Messages.getString("DebuggerTab.gdbServerGdbPort_ToolTipText"));

			fGdbServerGdbPort = new Text(comp, SWT.SINGLE | SWT.BORDER);
			fGdbServerGdbPort.setToolTipText(Messages.getString("DebuggerTab.gdbServerGdbPort_ToolTipText"));

			gd = new GridData();
			gd.widthHint = 60;
			gd.horizontalSpan = ((GridLayout) comp.getLayout()).numColumns - 1;
			fGdbServerGdbPort.setLayoutData(gd);
		}

		{
			label = new Label(comp, SWT.NONE);
			label.setText(Messages.getString("DebuggerTab.gdbServerOther_Label")); //$NON-NLS-1$
			label.setToolTipText(Messages.getString("DebuggerTab.gdbServerOther_ToolTipText"));
			gd = new GridData();
			gd.verticalAlignment = SWT.TOP;
			label.setLayoutData(gd);

			fGdbServerOtherOptions = new Text(comp, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
			fGdbServerOtherOptions.setToolTipText(Messages.getString("DebuggerTab.gdbServerOther_ToolTipText"));

			gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.heightHint = 60;
			gd.horizontalSpan = ((GridLayout) comp.getLayout()).numColumns - 1;
			fGdbServerOtherOptions.setLayoutData(gd);
		}

		{
			fUseDDC = new Button(comp, SWT.CHECK);
			fUseDDC.setText(Messages.getString("DebuggerTab.gdbServerUseDDC_Label"));

			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = ((GridLayout) comp.getLayout()).numColumns;
			fUseDDC.setLayoutData(gd);
		}

		// ----- Actions ------------------------------------------------------

		ModifyListener scheduleUpdateJobModifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				scheduleUpdateJob();
			}
		};

		SelectionAdapter scheduleUpdateJobSelectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				scheduleUpdateJob();
			}
		};

		fDoStartGdbServer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doStartGdbServerChanged();
				if (fDoStartGdbServer.getSelection()) {
					fTargetIpAddress.setText(DefaultPreferences.REMOTE_IP_ADDRESS_LOCALHOST);
				}
				scheduleUpdateJob();
			}
		});
		
		fDoConnectToRunning.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				fTabStartup.doConnectToRunningChanged(fDoConnectToRunning.getSelection());

				scheduleUpdateJob();
			}
		});

		fGdbServerExecutable.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {

				scheduleUpdateJob(); // provides much better performance for
										// Text listeners
			}
		});

		fGdbServerBrowseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browseButtonSelected(Messages.getString("DebuggerTab.gdbServerExecutableBrowse_Title"),
						fGdbServerExecutable);
			}
		});

		fGdbServerVariablesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				variablesButtonSelected(fGdbServerExecutable);
			}
		});

		fIceClkText.addModifyListener(scheduleUpdateJobModifyListener);

		fDelayMtcrText.addModifyListener(scheduleUpdateJobModifyListener);

		fGdbServerGdbPort.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {

				// make the target port the same
				fTargetPortNumber.setText(fGdbServerGdbPort.getText());
				scheduleUpdateJob();
			}
		});

		fGdbServerOtherOptions.addModifyListener(scheduleUpdateJobModifyListener);

		fUseDDC.addSelectionListener(scheduleUpdateJobSelectionAdapter);

	}

	private void createGdbClientControls(Composite parent) {

		Group group = new Group(parent, SWT.NONE);
		group.setText(Messages.getString("DebuggerTab.gdbSetupGroup_Text"));
		GridLayout layout = new GridLayout();
		group.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);

		Composite comp = new Composite(group, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		comp.setLayout(layout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		comp.setLayoutData(gd);

		Label label;
		Button browseButton;
		Button variableButton;
		{
			label = new Label(comp, SWT.NONE);
			label.setText(Messages.getString("DebuggerTab.gdbCommand_Label"));
			label.setToolTipText(Messages.getString("DebuggerTab.gdbCommand_ToolTipText"));

			Composite local = new Composite(comp, SWT.NONE);
			layout = new GridLayout();
			layout.numColumns = 3;
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			local.setLayout(layout);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			local.setLayoutData(gd);
			{
				fGdbClientExecutable = new Text(local, SWT.SINGLE | SWT.BORDER);
				gd = new GridData(GridData.FILL_HORIZONTAL);
				fGdbClientExecutable.setLayoutData(gd);

				browseButton = new Button(local, SWT.NONE);
				browseButton.setText(Messages.getString("DebuggerTab.gdbCommandBrowse"));

				variableButton = new Button(local, SWT.NONE);
				variableButton.setText(Messages.getString("DebuggerTab.gdbCommandVariable"));
			}
		}

		{
			label = new Label(comp, SWT.NONE);
			label.setText(Messages.getString("DebuggerTab.gdbOtherOptions_Label"));
			label.setToolTipText(Messages.getString("DebuggerTab.gdbOtherOptions_ToolTipText"));
			gd = new GridData();
			label.setLayoutData(gd);

			fGdbClientOtherOptions = new Text(comp, SWT.SINGLE | SWT.BORDER);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			fGdbClientOtherOptions.setLayoutData(gd);
		}

		{
			label = new Label(comp, SWT.NONE);
			label.setText(Messages.getString("DebuggerTab.gdbOtherCommands_Label"));
			label.setToolTipText(Messages.getString("DebuggerTab.gdbOtherCommands_ToolTipText"));
			gd = new GridData();
			gd.verticalAlignment = SWT.TOP;
			label.setLayoutData(gd);

			fGdbClientOtherCommands = new Text(comp, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
			gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.heightHint = 60;
			fGdbClientOtherCommands.setLayoutData(gd);
		}

		// ----- Actions ------------------------------------------------------

		fGdbClientExecutable.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {

				scheduleUpdateJob(); // provides much better performance for
										// Text listeners
			}
		});

		fGdbClientOtherOptions.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				scheduleUpdateJob();
			}
		});

		fGdbClientOtherCommands.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				scheduleUpdateJob();
			}
		});

		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browseButtonSelected(Messages.getString("DebuggerTab.gdbCommandBrowse_Title"), fGdbClientExecutable);
			}
		});

		variableButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				variablesButtonSelected(fGdbClientExecutable);
			}
		});
	}

	private void createRemoteControl(Composite parent) {

		Group group = new Group(parent, SWT.NONE);
		group.setText(Messages.getString("DebuggerTab.remoteGroup_Text"));
		GridLayout layout = new GridLayout();
		group.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);

		Composite comp = new Composite(group, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		comp.setLayout(layout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		comp.setLayoutData(gd);

		// Create entry fields for TCP/IP connections
		Label label;
		{
			label = new Label(comp, SWT.NONE);
			label.setText(Messages.getString("DebuggerTab.ipAddressLabel")); //$NON-NLS-1$

			fTargetIpAddress = new Text(comp, SWT.BORDER);
			gd = new GridData();
			gd.widthHint = 125;
			fTargetIpAddress.setLayoutData(gd);

			label = new Label(comp, SWT.NONE);
			label.setText(Messages.getString("DebuggerTab.portNumberLabel")); //$NON-NLS-1$

			fTargetPortNumber = new Text(comp, SWT.BORDER);
			gd = new GridData();
			gd.widthHint = 125;
			fTargetPortNumber.setLayoutData(gd);
		}

		// ---- Actions -------------------------------------------------------

		// Add watchers for user data entry
		fTargetIpAddress.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				scheduleUpdateJob(); // provides much better performance for
										// Text listeners
			}
		});
		fTargetPortNumber.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				e.doit = Character.isDigit(e.character) || Character.isISOControl(e.character);
			}
		});
		fTargetPortNumber.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				scheduleUpdateJob(); // provides much better performance for
										// Text listeners
			}
		});

	}

	private void doStartGdbServerChanged() {

		boolean enabled = fDoStartGdbServer.getSelection();

		fDoConnectToRunning.setEnabled(enabled);
		fGdbServerExecutable.setEnabled(enabled);
		fGdbServerBrowseButton.setEnabled(enabled);
		fGdbServerVariablesButton.setEnabled(enabled);
		fGdbServerOtherOptions.setEnabled(enabled);

		fGdbServerGdbPort.setEnabled(enabled);

		fIceClkText.setEnabled(enabled);
		fDelayMtcrText.setEnabled(enabled);
		fUseDDC.setEnabled(enabled);

		// Disable remote target params when the server is started
		fTargetIpAddress.setEnabled(!enabled);
		fTargetPortNumber.setEnabled(!enabled);
	}

	/**
	 * Get the project name associated to this launch configuration.
	 * 
	 * @param configuration
	 * @return a String with the project name, or empty.
	 */
	private String getProjectName(ILaunchConfiguration configuration) {

		if (configuration != null) {
			// Get project name from launch configuration.
			try {
				String str = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME,
						(String) null);
				fProjectName = str.replace(' ', '_');
			} catch (CoreException e) {
				;
			}
		}

		if (fProjectName == null) {
			fProjectName = "";
		}

		return fProjectName;
	}
	private String getClientExecutableDefault(ILaunchConfiguration configuration) {
	String clientExecutableDefault=null;
	String fbuildProject = getProjectName(configuration);

	ICProject buildProject = CoreModel.getDefault().getCModel().getCProject(fbuildProject);
	IProject project=buildProject.getProject();
	ICProjectDescription local_prjd = CoreModel.getDefault().getProjectDescription(project);
	if (local_prjd == null) {
		return null;
	}
	ICConfigurationDescription[] conds = local_prjd.getConfigurations();
	ICConfigurationDescription cfgDesc = conds[0];	
	IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgDesc);
	String toolChainName = cfg.getToolChain().getName();
	if (toolChainName.indexOf("abiv2") != -1) {
		clientExecutableDefault = "csky-elfabiv2-gdb.exe";
	} else if(toolChainName.indexOf("abiv1") != -1){
		clientExecutableDefault = "csky-elf-gdb.exe";
	}
	return clientExecutableDefault;
}
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {

		if (Activator.getInstance().isDebugging()) {
			System.out.println("TabDebugger: initializeFrom() " + configuration.getName());
		}

		try {
			Boolean booleanDefault;
			String stringDefault;

			// CSKY GDB server
			{
				// Start server locally
				booleanDefault = PersistentPreferences.getGdbServerDoStart();
				fDoStartGdbServer.setSelection(
						configuration.getAttribute(ConfigurationAttributes.DO_START_GDB_SERVER, booleanDefault));

				fDoConnectToRunning.setSelection(
						configuration.getAttribute(ConfigurationAttributes.DO_CONNECT_TO_RUNNING,
								DefaultPreferences.DO_CONNECT_TO_RUNNING_DEFAULT));

				// Executable
				stringDefault = PersistentPreferences.getGdbServerExecutable();
				fGdbServerExecutable.setText(
						configuration.getAttribute(ConfigurationAttributes.GDB_SERVER_EXECUTABLE, stringDefault));

				String boardName = CProjectAttributes.getCmsisBoardName(configuration);
				fSavedCmsisBoardName = boardName;
				String deviceName = CProjectAttributes.getCmsisDeviceName(configuration);
				fSavedCmsisDeviceName = deviceName;

				fIceClkText.setText(
						Integer.toString(configuration.getAttribute(ConfigurationAttributes.GDB_SERVER_ICE_CLK,
								DefaultPreferences.SERVER_GDB_ICE_CLK_DEFAULT)));
				fDelayMtcrText.setText(
						Integer.toString(configuration.getAttribute(ConfigurationAttributes.GDB_SERVER_DELAY_MTCR,
								DefaultPreferences.SERVER_GDB_DELAY_MTCR_DEFAULT)));
				// Ports
				fGdbServerGdbPort.setText(
						Integer.toString(configuration.getAttribute(ConfigurationAttributes.GDB_SERVER_GDB_PORT_NUMBER,
								DefaultPreferences.SERVER_GDB_PORT_NUMBER_DEFAULT)));

				// Other options
				stringDefault = PersistentPreferences.getGdbServerOtherOptions();
				fGdbServerOtherOptions
						.setText(configuration.getAttribute(ConfigurationAttributes.GDB_SERVER_OTHER, stringDefault));

				booleanDefault = PersistentPreferences.getCSkyUseDDC();
				fUseDDC.setSelection(
						configuration.getAttribute(ConfigurationAttributes.GDB_SERVER_USE_DDC, booleanDefault));

			}

			// GDB Client Setup
			{
				// Executable
				stringDefault = PersistentPreferences.getGdbClientExecutable();
				String gdbCommandAttr = configuration.getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME,
						stringDefault);
				fGdbClientExecutable.setText(gdbCommandAttr);

				// Other options
				stringDefault = PersistentPreferences.getGdbClientOtherOptions();
				fGdbClientOtherOptions.setText(
						configuration.getAttribute(ConfigurationAttributes.GDB_CLIENT_OTHER_OPTIONS, stringDefault));

				stringDefault = PersistentPreferences.getGdbClientCommands();
				fGdbClientOtherCommands.setText(
						configuration.getAttribute(ConfigurationAttributes.GDB_CLIENT_OTHER_COMMANDS, stringDefault));
			}

			// Remote target
			{
				fTargetIpAddress.setText(configuration.getAttribute(IGDBJtagConstants.ATTR_IP_ADDRESS,
						DefaultPreferences.REMOTE_IP_ADDRESS_DEFAULT)); // $NON-NLS-1$

				int storedPort = 0;
				storedPort = configuration.getAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER, 0); // Default
																								// 0

				// 0 means undefined, use default
				if ((storedPort <= 0) || (65535 < storedPort)) {
					storedPort = DefaultPreferences.REMOTE_PORT_NUMBER_DEFAULT;
				}

				String portString = Integer.toString(storedPort); // $NON-NLS-1$
				fTargetPortNumber.setText(portString);

			}

			doStartGdbServerChanged();

			// Force thread update
			boolean updateThreadsOnSuspend = configuration.getAttribute(
					IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND,
					ConfigurationAttributes.UPDATE_THREAD_LIST_DEFAULT);
			fUpdateThreadlistOnSuspend.setSelection(updateThreadsOnSuspend);

		} catch (CoreException e) {
			Activator.log(e.getStatus());
		}
	}

	public void initializeFromDefaults() {

		if (Activator.getInstance().isDebugging()) {
			System.out.println("TabDebugger: initializeFromDefaults()");
		}

		String stringDefault;
		boolean booleanDefault;

		// CSKY GDB server
		{
			// Start server locally
			booleanDefault = DefaultPreferences.getGdbServerDoStart();
			fDoStartGdbServer.setSelection(booleanDefault);
			
			fDoConnectToRunning.setSelection(DefaultPreferences.DO_CONNECT_TO_RUNNING_DEFAULT);

			// Executable
			stringDefault = DefaultPreferences.getGdbServerExecutable();
			fGdbServerExecutable.setText(stringDefault);
			
			//ICE CLK
			fIceClkText.setText(Integer.toString(DefaultPreferences.SERVER_GDB_ICE_CLK_DEFAULT));

			//Delay for 'mtcr'
			fDelayMtcrText.setText(Integer.toString(DefaultPreferences.SERVER_GDB_DELAY_MTCR_DEFAULT));

			// Ports
			fGdbServerGdbPort.setText(Integer.toString(DefaultPreferences.SERVER_GDB_PORT_NUMBER_DEFAULT));

			// Other options
			stringDefault = DefaultPreferences.getGdbServerOtherOptions();
			fGdbServerOtherOptions.setText(stringDefault);

			fUseDDC.setSelection(DefaultPreferences.SERVER_GDB_USE_DDC_DEFAULT);
		}

		// GDB Client Setup
		{
			stringDefault = DefaultPreferences.getGdbClientExecutable();
			// Executable
			fGdbClientExecutable.setText(stringDefault);

			// Other options
			stringDefault = DefaultPreferences.getGdbClientOtherOptions();
			fGdbClientOtherOptions.setText(stringDefault);

			stringDefault = DefaultPreferences.getGdbClientCommands();
			fGdbClientOtherCommands.setText(stringDefault);
		}

		// Remote target
		{
			fTargetIpAddress.setText(DefaultPreferences.REMOTE_IP_ADDRESS_DEFAULT); // $NON-NLS-1$

			String portString = Integer.toString(DefaultPreferences.REMOTE_PORT_NUMBER_DEFAULT); // $NON-NLS-1$
			fTargetPortNumber.setText(portString);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
	 */
	@Override
	public String getId() {
		return TAB_ID;
	}

	@Override
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		if (Activator.getInstance().isDebugging()) {
			System.out.println("TabDebugger: activated() " + workingCopy.getName());
		}
	}

	@Override
	public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) {
		if (Activator.getInstance().isDebugging()) {
			System.out.println("TabDebugger: deactivated() " + workingCopy.getName());
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {

		if (Activator.getInstance().isDebugging()) {
			System.out.println("TabDebugger: performApply() " + configuration.getName());
		}

		{
			// legacy definition; although the jtag device class is not used,
			// it must be there, to avoid NPEs
			configuration.setAttribute(IGDBJtagConstants.ATTR_JTAG_DEVICE, ConfigurationAttributes.JTAG_DEVICE);
		}

		boolean booleanValue;
		String stringValue;
		int intValue;

		// CSKY server
		{
			// Start server
			booleanValue = fDoStartGdbServer.getSelection();
			configuration.setAttribute(ConfigurationAttributes.DO_START_GDB_SERVER, booleanValue);
			PersistentPreferences.putGdbServerDoStart(booleanValue);

			// Connect to running
			configuration.setAttribute(ConfigurationAttributes.DO_CONNECT_TO_RUNNING,
					fDoConnectToRunning.getSelection());
			
			// Executable
			stringValue = fGdbServerExecutable.getText().trim();
			configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_EXECUTABLE, stringValue);
			PersistentPreferences.putGdbServerExecutable(stringValue);

			//ICE CLK
			intValue = Integer.parseInt(fIceClkText.getText().trim());
			configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_ICE_CLK, intValue);

			//Delay for 'mtcr'
			intValue = Integer.parseInt(fDelayMtcrText.getText().trim());
			configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_DELAY_MTCR, intValue);

			// Ports
			intValue = Integer.parseInt(fGdbServerGdbPort.getText().trim());
			configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_GDB_PORT_NUMBER, intValue);

			// Other options
			stringValue = fGdbServerOtherOptions.getText().trim();
			configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_OTHER, stringValue);
			PersistentPreferences.putGdbServerOtherOptions(stringValue);

			booleanValue = fUseDDC.getSelection();
			configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_USE_DDC , booleanValue);
			PersistentPreferences.putCSkyUseDDC(booleanValue);
		}

		// GDB client
		{
			// always use remote
			configuration.setAttribute(IGDBJtagConstants.ATTR_USE_REMOTE_TARGET,
					DefaultPreferences.USE_REMOTE_TARGET_DEFAULT);

			stringValue = fGdbClientExecutable.getText().trim();
			// configuration.setAttribute(
			// IMILaunchConfigurationConstants.ATTR_DEBUG_NAME,
			// clientExecutable);
			configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME, stringValue); // DSF
			PersistentPreferences.putGdbClientExecutable(stringValue);

			stringValue = fGdbClientOtherOptions.getText().trim();
			configuration.setAttribute(ConfigurationAttributes.GDB_CLIENT_OTHER_OPTIONS, stringValue);
			PersistentPreferences.putGdbClientOtherOptions(stringValue);

			stringValue = fGdbClientOtherCommands.getText().trim();
			configuration.setAttribute(ConfigurationAttributes.GDB_CLIENT_OTHER_COMMANDS, stringValue);
			PersistentPreferences.putGdbClientCommands(stringValue);
		}

		{
			if (fDoStartGdbServer.getSelection()) {
				configuration.setAttribute(IGDBJtagConstants.ATTR_IP_ADDRESS, "localhost");

				try {
					int port;
					port = Integer.parseInt(fGdbServerGdbPort.getText().trim());
					configuration.setAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER, port);
				} catch (NumberFormatException e) {
					Activator.log(e);
				}
			} else {
				String ip = fTargetIpAddress.getText().trim();
				configuration.setAttribute(IGDBJtagConstants.ATTR_IP_ADDRESS, ip);

				try {
					int port = Integer.valueOf(fTargetPortNumber.getText().trim()).intValue();
					configuration.setAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER, port);
				} catch (NumberFormatException e) {
					Activator.log(e);
				}
			}
		}

		// Force thread update
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND,
				fUpdateThreadlistOnSuspend.getSelection());

		PersistentPreferences.flush();
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {

		if (Activator.getInstance().isDebugging()) {
			System.out.println("TabDebugger: setDefaults() " + configuration.getName());
		}

		boolean defaultBoolean;
		String defaultString;

		configuration.setAttribute(IGDBJtagConstants.ATTR_JTAG_DEVICE, ConfigurationAttributes.JTAG_DEVICE);

		// These are inherited from the generic implementation.
		// Some might need some trimming.
		{
			configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND,
					IGDBLaunchConfigurationConstants.DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND_DEFAULT);
		}

		// CSKY GDB server setup
		{
			defaultBoolean = PersistentPreferences.getGdbServerDoStart();
			configuration.setAttribute(ConfigurationAttributes.DO_START_GDB_SERVER, defaultBoolean);

			configuration.setAttribute(ConfigurationAttributes.DO_CONNECT_TO_RUNNING,
					DefaultPreferences.DO_CONNECT_TO_RUNNING_DEFAULT);
			
			defaultString = PersistentPreferences.getGdbServerExecutable();
			configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_EXECUTABLE, defaultString);

			configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_ICE_CLK,
					DefaultPreferences.SERVER_GDB_ICE_CLK_DEFAULT);
			
			configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_DELAY_MTCR,
					DefaultPreferences.SERVER_GDB_DELAY_MTCR_DEFAULT);
			
			configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_GDB_PORT_NUMBER,
					DefaultPreferences.SERVER_GDB_PORT_NUMBER_DEFAULT);

			defaultString = PersistentPreferences.getGdbServerOtherOptions();
			configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_OTHER, defaultString);

			defaultBoolean = PersistentPreferences.getCSkyEnableSemihosting();
			configuration.setAttribute(ConfigurationAttributes.ENABLE_SEMIHOSTING, defaultBoolean);

			configuration.setAttribute(ConfigurationAttributes.SEMIHOSTING_CMDLINE, getProjectName(configuration));

			configuration.setAttribute(ConfigurationAttributes.DO_GDB_SERVER_ALLOCATE_CONSOLE,
					DefaultPreferences.DO_GDB_SERVER_ALLOCATE_CONSOLE_DEFAULT);

			defaultBoolean = PersistentPreferences.getCSkyUseDDC();
			configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_USE_DDC, defaultBoolean);
		}

		// GDB client setup
		{
			configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME,
					getClientExecutableDefault(configuration));
//			configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME,
//					PersistentPreferences.getGdbClientExecutable());
			defaultString = PersistentPreferences.getGdbClientOtherOptions();
			configuration.setAttribute(ConfigurationAttributes.GDB_CLIENT_OTHER_OPTIONS,
					DefaultPreferences.getGdbClientOtherOptions());

			defaultString = PersistentPreferences.getGdbClientCommands();
			configuration.setAttribute(ConfigurationAttributes.GDB_CLIENT_OTHER_COMMANDS, defaultString);
		}

		// Remote Target
		{
			configuration.setAttribute(IGDBJtagConstants.ATTR_USE_REMOTE_TARGET,
					DefaultPreferences.USE_REMOTE_TARGET_DEFAULT);

			configuration.setAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER,
					DefaultPreferences.REMOTE_PORT_NUMBER_DEFAULT);
		}

		// Force thread update
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND,
				ConfigurationAttributes.UPDATE_THREAD_LIST_DEFAULT);
	}

	// ------------------------------------------------------------------------
}
