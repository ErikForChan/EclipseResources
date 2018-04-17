package org.eclipse.cdt.ui.wizards;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WorkingSetGroup;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea.IErrorMessageReporter;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.parsexml.Board;
import org.eclipse.cdt.ui.wizards.parsexml.Cpu;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;

import org.eclipse.cdt.internal.ui.newui.Messages;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringDialogField;

@SuppressWarnings("restriction")
public class DjyosMainWizardPage extends WizardPage{
	public static final String PAGE_ID = "org.eclipse.cdt.managedbuilder.ui.wizard.NewModelProjectWizardPage"; //$NON-NLS-1$

	private static final String EXTENSION_POINT_ID = "org.eclipse.cdt.ui.CDTWizard"; //$NON-NLS-1$
	private static final String ELEMENT_NAME = "wizard"; //$NON-NLS-1$
	private static final String CLASS_NAME = "class"; //$NON-NLS-1$
	public static final String DESC = "EntryDescriptor"; //$NON-NLS-1$
	
	// Widgets
	private Tree tree;
	private Composite right;
	private Button showSup;
	private Label rightLabel;

	Text fProjectNameField;
	Text fBoardNameField;
	
	public CWizardHandler h_selected;
	private Label categorySelectedLabel;
	private String initialProjectFieldValue;
	private WorkingSetGroup workingSetGroup;
	
	static Text projectTypeDesc;
	private static Button[] radioBtns = new Button[4];
	Combo boardCombo;
	Board board;
	String boardName;
	Cpu selectedCpu;
	Board selectedBoard;
	boolean isToCreat;
	String boardModuleTrimPath;
	boolean clickedNext = true;
	String projectPath;
	Cpu defaultCpu;
	
	public boolean isToCreat() {
		return isToCreat;
	}
	
	public String getBoardName() {
		return fBoardNameField.getText().trim();
	}
	
	public Cpu getSelectCpu() {
		return selectedCpu;
	}
	
	public Board getSelectBoard() {
		return selectedBoard;
	}
	
	@SuppressWarnings("restriction")
	public static ProjectContentsLocationArea locationArea;

	boolean nameValid = false;
	
	private  Listener nameModifyListener = e -> {
		setLocationForSelection();
		boolean valid = validatePageBefore();
		if(nameValid!=valid) {
			setPageComplete(valid);
			nameValid = valid;
		}

	};
	
	private  Listener boardModifyListener = e -> {
		//setLocationForSelection();
		boolean valid = validatePageBefore();
		setPageComplete(valid);
	};
	
	void setLocationForSelection() {
		locationArea.updateProjectName(getProjectNameFieldValue());
	}
	
	public String getProjectNameFieldValue() {
		if (fProjectNameField == null) {
			return null; //$NON-NLS-1$
		}
		return fProjectNameField.getText().trim();
	}
	
	private String getBoardNameFieldValue() {
		if (fBoardNameField == null) {
			return ""; //$NON-NLS-1$
		}
		return fBoardNameField.getText().trim();
	}
	
	public DjyosMainWizardPage(String pageName) {
		super(pageName);
		setPageComplete(false);
	}

	@SuppressWarnings("restriction")
	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		initializeDialogUnits(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 25;
		layout.verticalSpacing = 20;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
//		Button pageDescBtn = new Button(composite, SWT.PUSH | SWT.RIGHT);
//		pageDescBtn.setText("Page Description");
//		pageDescBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
//		pageDescBtn.addSelectionListener(new SelectionAdapter(){  
//            public void widgetSelected(SelectionEvent e){         	
//            	try {
//					Desktop.getDesktop().open(new File("E:/GoogleJava.pdf"));
//				} catch (IOException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//            }
//        });  
		
		creatTemplateUI(composite);
		createProjectAndBoardGroup(composite);
		//createBoardGroup(composite);
		locationArea = new ProjectContentsLocationArea(getErrorReporter(), composite);
		if (initialProjectFieldValue != null) {
			locationArea.updateProjectName(initialProjectFieldValue);
		}
		// Scale the button based on the rest of the dialog
		setButtonLayoutData(locationArea.getBrowseButton());
		// Show description on opening
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
		Dialog.applyDialogFont(composite);
	}

	private void createProjectAndBoardGroup(Composite parent) {
		Composite projectGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.verticalSpacing = 20;
		projectGroup.setLayout(layout);
		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// new project label
		Label projectLabel = new Label(projectGroup, SWT.NONE);
		projectLabel.setText(IDEWorkbenchMessages.WizardNewProjectCreationPage_nameLabel);
		// new project name entry field
		fProjectNameField = new Text(projectGroup, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		fProjectNameField.setLayoutData(data);
		fProjectNameField.addListener(SWT.Modify, nameModifyListener);
		BidiUtils.applyBidiProcessing(fProjectNameField, BidiUtils.BTD_DEFAULT);
		Button testBtn = new Button(projectGroup, SWT.PUSH);
		testBtn.setVisible(false);
		
		Label boardLabel = new Label(projectGroup, SWT.NONE);
		boardLabel.setText("Board name:");
		// new project name entry field
//		Composite selectOrCreateGroup = new Composite(projectGroup, SWT.NONE);
//		GridLayout selectOrCreateGl = new GridLayout();
//		selectOrCreateGl.numColumns = 2;
//		selectOrCreateGroup.setLayout(selectOrCreateGl);
//		selectOrCreateGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fBoardNameField = new Text(projectGroup, SWT.BORDER);
		fBoardNameField.setLayoutData(data);
		fBoardNameField.setEnabled(false);
		fBoardNameField.addListener(SWT.Modify, boardModifyListener);
		fBoardNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		BidiUtils.applyBidiProcessing(fBoardNameField, BidiUtils.BTD_DEFAULT);
		Button selectOrCreateOrNewBtn = new Button(projectGroup, SWT.PUSH);
		selectOrCreateOrNewBtn.setText("Select/Create ...");
		selectOrCreateOrNewBtn.addSelectionListener(new SelectionAdapter(){  
            public void widgetSelected(SelectionEvent e){         	
            		SelectOrCreateBoardDialog dialog = new SelectOrCreateBoardDialog(getShell());
            		if (dialog.open() == Window.OK) {
            			boardName = dialog.getBoardName();
            			selectedCpu = dialog.getSelectCpu();
            			defaultCpu = dialog.defaultCpu;
            			selectedBoard = dialog.getSelectBoard();
            			fBoardNameField.setText(boardName);
            			isToCreat = dialog.isToCreat();
            			boardModuleTrimPath = dialog.boardModuleTrimPath;
        			}
            }
        });  
//		fBoardNameField.addListener(SWT.Modify, nameModifyListener);
//		BidiUtils.applyBidiProcessing(fBoardNameField, BidiUtils.BTD_DEFAULT);
		
	}
		
	private void copyHoldsOptions(IHoldsOptions src, IHoldsOptions dst, IResourceInfo res){
		if(src instanceof ITool) {
			ITool t1 = (ITool)src;
			ITool t2 = (ITool)dst;
			if (t1.getCustomBuildStep()) return;
			t2.setToolCommand(t1.getToolCommand());
			t2.setCommandLinePattern(t1.getCommandLinePattern());
		}
		IOption op1[] = src.getOptions();
		IOption op2[] = dst.getOptions();
		for(int i = 0; i < op1.length; i++) {
			//setOption(op1[i], op2[i], dst, res);
			String enumVal;
			try {
				switch (op1[i].getValueType()) {
				case IOption.BOOLEAN :
					boolean boolVal = op1[i].getBooleanValue();
					ManagedBuildManager.setOption(res, dst, op2[i], boolVal);
					break;
				case IOption.ENUMERATED :
				case IOption.TREE :
					enumVal = op1[i].getStringValue();
					String enumId = op1[i].getId(enumVal);
					System.out.println("enumVal:   "+enumVal +"\nenumId: "+enumId);
					String out = (enumId != null && enumId.length() > 0) ? enumId : enumVal;
					ManagedBuildManager.setOption(res, dst, op2[i], enumVal);
					break;
				case IOption.STRING :
					ManagedBuildManager.setOption(res, dst, op2[i], op1[i].getStringValue());
					break;
				case IOption.INCLUDE_PATH :
				case IOption.PREPROCESSOR_SYMBOLS :
				case IOption.INCLUDE_FILES:
				case IOption.MACRO_FILES:
				case IOption.UNDEF_INCLUDE_PATH:
				case IOption.UNDEF_PREPROCESSOR_SYMBOLS:
				case IOption.UNDEF_INCLUDE_FILES:
				case IOption.UNDEF_LIBRARY_PATHS:
				case IOption.UNDEF_LIBRARY_FILES:
				case IOption.UNDEF_MACRO_FILES:
//					if (((Option)op1).isDirty())
//						isIndexerAffected = true;
					@SuppressWarnings("unchecked")
					String[] data = ((List<String>)op1[i].getValue()).toArray(new String[0]);
					ManagedBuildManager.setOption(res, dst, op2[i], data);
					break;
				case IOption.LIBRARIES :
				case IOption.LIBRARY_PATHS:
				case IOption.LIBRARY_FILES:
				case IOption.STRING_LIST :
				case IOption.OBJECTS :
					@SuppressWarnings("unchecked")
					String[] data2 = ((List<String>)op1[i].getValue()).toArray(new String[0]);
					ManagedBuildManager.setOption(res, dst, op2[i], data2);
					break;
				default :
					break;
				}
				
			} catch (BuildException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
	}
	
	public static void creatTemplateUI(Composite parent) {
		Group group1 = ControlFactory.createGroup(parent, "Choose Template ", 1);
		GridLayout gl = new GridLayout(2, false);
		gl.marginHeight = 10;
		group1.setLayout(gl);

		Composite RADIOCpt = new Composite(group1, SWT.NONE);
		GridLayout radioGl = new GridLayout();
		radioGl.verticalSpacing = 20;
		RADIOCpt.setLayout(radioGl);
		RADIOCpt.setLayoutData(new GridData(SWT.VERTICAL));
		String[] templateLabels = { "Iboot And App Project", "Iboot Only Project", "App Project",
				"Bare App Project" };

		String[] templateDescs = {
				"用于开发iboot和App的工程,App由iboot启动",
				"用于开发iboot的工程",
				"用于开发App的工程",
				"用于开发无需iboot,自启动运行的App工程"
		};
		for (int i = 0; i < radioBtns.length; i++) {
			radioBtns[i] = new Button(RADIOCpt, SWT.RADIO | SWT.LEFT);
			radioBtns[i].setText(templateLabels[i]);
//			radioBtns[i].setToolTipText(templateLabels[i]);
			int a = i;
			radioBtns[i].addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					projectTypeDesc.setText(templateDescs[a]);
				}

			});
		}
		radioBtns[0].setSelection(true);

		Composite right = new Composite(group1, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginLeft = 2;
		right.setLayout(layout);
		right.setLayoutData(new GridData(GridData.FILL_BOTH | SWT.VERTICAL));
		Label templateLabel = new Label(right, SWT.NONE);
		templateLabel.setText("Project Tepmlate Description: ");
		projectTypeDesc = new Text(right, SWT.MULTI | SWT.WRAP);
		projectTypeDesc.setLayoutData(new GridData(GridData.FILL_BOTH));
		projectTypeDesc.setText(templateDescs[0]);

	}

	@Override
	public boolean canFlipToNextPage() {
		// TODO Auto-generated method stub
		clickedNext = false;
		projectPath = locationArea.locationPathField.getText();
		return super.canFlipToNextPage();
	}

	@Override
	public IWizardPage getNextPage() {
		System.out.println("getNextPage DW");
		DjyosCommonProjectWizard nmWizard = (DjyosCommonProjectWizard)getWizard();
		if(! nmWizard.addedMemory) {
			System.out.println("nmWizard.addedMemory...");
			nmWizard.memoryPage = new MemoryMapWizard("basicMemoryMapPage");
			nmWizard.memoryPage.setTitle("Memory Map");
			nmWizard.memoryPage.setDescription("Define flash and RAM sizes");
			nmWizard.addPage(nmWizard.memoryPage);
			nmWizard.addedMemory = true;		
		}else {
			if(clickedNext) {
				nmWizard.importTemplate(projectPath);
				final IWorkspace workspace = ResourcesPlugin.getWorkspace();
				IProject project = workspace.getRoot().getProject(getProjectNameFieldValue());
				String eclipsePath = nmWizard.getEclipsePath();
				int index = getTemplateIndex();
				String projectPath = locationArea.locationPathField.getText();
				if(!projectPath.contains(getProjectName())) {
					projectPath = projectPath+"/"+getProjectName();
				}
				if(index==0) {
					nmWizard.createModuleTrim(boardModuleTrimPath, projectPath+"/src/app/module-trim.c");
					nmWizard.createModuleTrim(boardModuleTrimPath, projectPath+"/src/iboot/module-trim.c");
				}else if(index==1) {
					nmWizard.createModuleTrim(boardModuleTrimPath, projectPath+"/src/iboot/module-trim.c");
				}else if(index==2){
					nmWizard.createModuleTrim(boardModuleTrimPath, projectPath+"/src/app/module-trim.c");
				}else if(index==3){
					nmWizard.createModuleTrim(boardModuleTrimPath, projectPath+"/src/app/module-trim.c");
				}
			}		
		}
		clickedNext = true;		
		return super.getNextPage();
	}

	public URI getProjectLocation() {
		return useDefaults() ? null : getLocationURI();
	}
	
	public URI getLocationURI() {
		return locationArea.getProjectLocationURI();
	}

	public String getProjectName() {
		if (fProjectNameField == null) {
			return initialProjectFieldValue;
		}

		return getProjectNameFieldValue();
	}
		
	public int getTemplateIndex() {
		int index = 0;
		for (int i = 0; i < radioBtns.length; i++) {
			if(radioBtns[i].getSelection()) {
				index = i;
			}
		}
		return index;
	}	
	
	private IErrorMessageReporter getErrorReporter() {
		return (errorMessage, infoOnly) -> {
			if (infoOnly) {
				setMessage(errorMessage, IStatus.INFO);
				setErrorMessage(null);
			} else
				setErrorMessage(errorMessage);
			boolean valid = errorMessage == null;
			if (valid) {
				valid = validatePageBefore();
			}

			setPageComplete(valid);
		};
	}

	public IProject getProjectHandle() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectName());
	}
	
	public void setExistedMessage() {
		setMessage("Target project existed !");
	}
	
	@SuppressWarnings("restriction")
	protected boolean validatePageBefore() {
		IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();

		String projectFieldContents = getProjectNameFieldValue();
		if (projectFieldContents.equals("")) { //$NON-NLS-1$
			setErrorMessage(null);
			setMessage(IDEWorkbenchMessages.WizardNewProjectCreationPage_projectNameEmpty);
			return false;
		}
		
		String boardFieldContents = getBoardNameFieldValue();
		if (boardFieldContents.equals("")) { //$NON-NLS-1$
			setErrorMessage(null);
			setMessage("Board must be selected !!!");
			return false;
		}

		IStatus nameStatus = workspace.validateName(projectFieldContents, IResource.PROJECT);
		if (!nameStatus.isOK()) {
			setErrorMessage(nameStatus.getMessage());
			return false;
		}

//		IProject handle = getProjectHandle();
//		if (handle.exists()) {
//			setErrorMessage(IDEWorkbenchMessages.WizardNewProjectCreationPage_projectExistsMessage);
//			return false;
//		}

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectNameFieldValue());
		locationArea.setExistingProject(project);

		String validLocationMessage = locationArea.checkValidLocation();
		if (validLocationMessage != null) { // there is no destination location given
			setErrorMessage(validLocationMessage);
			return false;
		}

		setErrorMessage(null);
		setMessage(null);
		return true;
	}

	protected boolean validatePage() {
		setMessage(null);
		if (validatePageBefore())
			return false;

		if (getProjectName().indexOf('#') >= 0) {
			setErrorMessage(Messages.CDTMainWizardPage_0);
			return false;
		}

		boolean bad = true; // should we treat existing project as error

		if (bad) { // Skip this check if project already created
			try {
				IFileStore fs;
				URI p = getProjectLocation();
				if (p == null) {
					fs = EFS.getStore(ResourcesPlugin.getWorkspace().getRoot().getLocationURI());
					fs = fs.getChild(getProjectName());
				} else
					fs = EFS.getStore(p);
				IFileInfo f = fs.fetchInfo();
				if (f.exists()) {
					if (f.isDirectory()) {
						if (f.getAttribute(EFS.ATTRIBUTE_READ_ONLY)) {
							setErrorMessage(Messages.CMainWizardPage_DirReadOnlyError);
							return false;
						}
						else
							setMessage(Messages.CMainWizardPage_7, IMessageProvider.WARNING);
					} else {
						setErrorMessage(Messages.CMainWizardPage_6);
						return false;
					}
				}
			} catch (CoreException e) {
				CUIPlugin.log(e.getStatus());
			}
		}

		if (tree.getItemCount() == 0) {
			setErrorMessage(Messages.CMainWizardPage_3);
			return false;
		}

		// it is not an error, but we cannot continue
		if (h_selected == null) {
			setErrorMessage(null);
			return false;
		}

		String s = h_selected.getErrorMessage();
		if (s != null) {
			setErrorMessage(s);
			return false;
		}

		setErrorMessage(null);
		return true;
	}
	
    public boolean useDefaults() {
		return locationArea.isDefault();
	}
}