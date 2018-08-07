package org.djyos.handlestart;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

public class ResourceUpdater implements IResourceChangeListener {

	private static Logger LOGGER = Logger.getLogger("");
	/*
	 * 对编译生成的bin文件进行修改
	 */
	void changeBin(IResource resource) {
		// TODO Auto-generated method stub
			System.out.println("iboot.bin入口: ");
			IFile ifile = (IFile) resource;
			File file = ifile.getLocation().toFile();
			String MD5Code = getMD5Code(file);// 获取file的MD5码 
			System.out.println("MD5Code: "+MD5Code);
			try {
				RandomAccessFile RAFile = new RandomAccessFile(file, "rwd");
				try {
					RAFile.seek(128);
					RAFile.write(MD5Code.getBytes(),0, MD5Code.length());
					System.out.println("结束！");
					RAFile.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();	
			}		
	}
	/*
	 * 将文件换成MD5码的字节
	 */
	private static byte[] createMD5Code(File file) {  
        InputStream fis = null;  
        try {  
            fis =  new FileInputStream(file);
            byte[] buffer = new byte[1024];  
            MessageDigest complete = MessageDigest.getInstance("MD5");  
            int numRead = -1;  
            int byteCount = 0;
            while ((numRead = fis.read(buffer)) != -1) {  
            	byteCount++;
            	if(byteCount>256){
            		complete.update(buffer, 0, numRead);  
            	}
            }  
            return complete.digest();  
        } catch (FileNotFoundException e) {  
            LOGGER.info(e.getMessage());
        } catch (NoSuchAlgorithmException e) {  
        	LOGGER.info(e.getMessage());
        } catch (IOException e) {  
        	LOGGER.info(e.getMessage());
        } finally {  
            try {  
                if (null != fis) {  
                    fis.close();  
                }  
            } catch (IOException e) {  
            	LOGGER.info(e.getMessage());
            }  
        }  
        return null;  
  
    }  
	
	/*
	 * 通过文件获取MD5编码
	 */
	public static String getMD5Code(File file) {

		byte[] b = createMD5Code(file);
		if (null == b) {
			LOGGER.info("Error:create md5 string failure!");
			return null;
		}
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < b.length; i++) {
			result.append(Integer.toString((b[i] & 0xff) + 0x100, 16)
					.substring(1));
		}
		return result.toString();
	}
	
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		// TODO Auto-generated method stub
		
		try {
			event.getDelta().accept(new IResourceDeltaVisitor() {	
				@Override
				public boolean visit(IResourceDelta delta) throws CoreException {
					// TODO Auto-generated method stub
					IResource resource = delta.getResource();
					//System.out.println("resource.getName(): "+resource.getName());
					if (resource instanceof IFile && resource.getName().equals("iboot.bin"))
					{
						
						switch (delta.getKind()) {
						case IResourceDelta.ADDED:
							// handle added resource
							changeBin(resource);
							break;
						case IResourceDelta.REMOVED:
							// handle removed resource
							System.out.println("resourceREMOVED 资源名:"+resource.getName());
							break;
						case IResourceDelta.CHANGED:
							// handle changed resource		
							changeBin(resource);											
							break;					
						}
					}
					//return true to continue visiting children.
					return true;
				}
			});
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
