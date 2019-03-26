package org.eclipse.cdt.managedbuilder.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.core.runtime.Platform;

public class CDTDideHelper {
	String fullPath = Platform.getInstallLocation().getURL().toString().replace("\\", "/");
	String didePath = fullPath.substring(6,(fullPath.substring(0,fullPath.length()-1)).lastIndexOf("/")+1);
	
	public boolean isNoaBuild() {
		return isAutoBuild("NOA_BUILDLIBOS");
	}
	
	private boolean isAutoBuild(String target) {
		File didePrefsFile = new File(didePath+"IDE/configuration/.settings/com.djyos.ui.prefs");
		return targetIsTrue(didePrefsFile,target);
	}
	
	public boolean targetIsTrue(File file, String target) {
		// TODO Auto-generated method stub
		BufferedReader br = null;  
        String line = null;  
        StringBuffer bufAll = new StringBuffer();  //保存修改过后的所有内容，不断增加         
        try {            
            br = new BufferedReader(new FileReader(file));              
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
	
	
}
