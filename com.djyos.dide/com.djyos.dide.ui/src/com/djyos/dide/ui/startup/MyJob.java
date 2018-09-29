package com.djyos.dide.ui.startup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.djyos.dide.ui.wizards.djyosProject.tools.DideHelper;

public class MyJob extends Job{
	private DideHelper dideHelper = new DideHelper();
	private File svnVerFile;
	private long svnVersion;
	
	public MyJob(String name,File file, long version) {
		
		super(name);
		svnVerFile = file;
		svnVersion = version;
		// TODO Auto-generated constructor stub
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		CUIPlugin.getDefault().startGlobalConsole();
		
		setSvnVersion(svnVerFile,svnVersion);
		
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		monitor.done();
		return Status.OK_STATUS;
	}
	
	private void setSvnVersion(File svnVerFile,long version) {
		// TODO Auto-generated method stub
		System.out.println("setSvnVersion");
		BufferedReader br = null;  
        String line = null;  
        StringBuffer bufAll = new StringBuffer();  //保存修改过后的所有内容，不断增加
        boolean find = false;
        try {            
            br = new BufferedReader(new FileReader(svnVerFile));              
            while ((line = br.readLine()) != null) {  
                StringBuffer buf = new StringBuffer();  
                //修改内容核心代码
                if (line.startsWith("SVN_VERION")) {  
                	line = "SVN_VERION="+version;
                	find = true;
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
        if(!find) {
        	 bufAll.append("SVN_VERION="+version+"\n");           
        }
        dideHelper.writeFile(svnVerFile, bufAll.toString());
	}

	
}
