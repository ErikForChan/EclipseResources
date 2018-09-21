package com.djyos.dide.ui.perference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.preferences.PreferencesMessages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.dialogs.WorkbenchPreferencePage;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

import com.djyos.dide.ui.wizards.djyosProject.tools.DideHelper;

@SuppressWarnings("restriction")
public class DjyosPerference extends PreferencePage implements IWorkbenchPreferencePage {
	
	boolean showUpdate;
	private DideHelper dideHelper = new DideHelper();
	private Button showGitUpdateButton;
	File didePrefsFile = new File(dideHelper.getDIDEPath()+"IDE/configuration/.settings/com.djyos.ui.prefs");
	
	public DjyosPerference() {
		super();
		setPreferenceStore(CUIPlugin.getDefault().getPreferenceStore());
		setDescription("General settings for DIDE development:");
	}

	private boolean targetIsTrue(File didePrefsFile, String target) {
		// TODO Auto-generated method stub
		BufferedReader br = null;  
        String line = null;  
        StringBuffer bufAll = new StringBuffer();  //保存修改过后的所有内容，不断增加         
        try {            
            br = new BufferedReader(new FileReader(didePrefsFile));              
            while ((line = br.readLine()) != null) {  
                StringBuffer buf = new StringBuffer();  
                //修改内容核心代码
                if (line.startsWith(target)) {  
                	String[] infos = line.split("=");
                	if(infos[1].trim().equals("false")) {
                		return false;
                	}
                    break;
                }
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
            if (br != null) {  
                try {  
                    br.close();  
                } catch (IOException e) {  
                    br = null;  
                }  
            }  
        }  
		return true;
	}

    /**
     * The user has pressed Ok. Store/apply this page's values appropriately.
     */
    @Override
	public boolean performOk() {
    	boolean noticeMe = showGitUpdateButton.getSelection();
		if(!didePrefsFile.exists()) {
			fillGitPrefsFile(didePrefsFile,"SHOW_GIT_UPDATE_DIALOG="+noticeMe);
		}else {
			if (showUpdate!=noticeMe) {
				setDjyosUiPrefs(didePrefsFile,noticeMe,"SHOW_GIT_UPDATE_DIALOG");
			}
		}
        return super.performOk();
    }
    
    private void setDjyosUiPrefs(File didePrefsFile, boolean isTrue, String target) {
		// TODO Auto-generated method stub
		BufferedReader br = null;  
        String line = null;  
        boolean targetExist = false;
        StringBuffer bufAll = new StringBuffer();  //保存修改过后的所有内容，不断增加         
        try {            
            br = new BufferedReader(new FileReader(didePrefsFile));              
            while ((line = br.readLine()) != null) {  
                StringBuffer buf = new StringBuffer();  
                //修改内容核心代码
                if (line.startsWith(target)) {  
                	line = target+"="+isTrue;
                	targetExist = true;
                }
                bufAll.append(line+"\n");            
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
            if (br != null) {  
                try {  
                    br.close();  
                } catch (IOException e) {  
                    br = null;  
                }  
            }  
        }  
        if(!targetExist) {
        	bufAll.append(target+"="+isTrue+"\n");
        }
        didePrefsFile.delete();
        fillGitPrefsFile(didePrefsFile,bufAll.toString());
        
	}
    
    private void fillGitPrefsFile(File dideGitPrefsFile, String content) {
		// TODO Auto-generated method stub
		try {
			dideGitPrefsFile.createNewFile();
			FileWriter writer;
			try {
				writer = new FileWriter(dideGitPrefsFile);
				writer.write(content);
				writer.flush();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    
    
	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), ICHelpContextIds.C_PREF_PAGE);
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}
	@Override
	protected Control createContents(Composite parent) {
		// TODO Auto-generated method stub
		initializeDialogUnits(parent);
		
		Composite composite = new Composite(parent,SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth= 0;
		layout.verticalSpacing= convertVerticalDLUsToPixels(10);
		layout.horizontalSpacing= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		if(didePrefsFile.exists()) {
			showUpdate = targetIsTrue(didePrefsFile,"SHOW_GIT_UPDATE_DIALOG");
		}else {
			showUpdate = true;
		}

		showGitUpdateButton = new Button(composite,SWT.CHECK);
		showGitUpdateButton.setText("Djyos源码有更新时提示我");
		showGitUpdateButton.setSelection(showUpdate);
		
		Dialog.applyDialogFont(composite);
		return composite;
	}
}
