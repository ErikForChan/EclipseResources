package org.eclipse.cdt.ui.wizards.djyosProject.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.ibm.icu.text.DecimalFormat;

import org.eclipse.cdt.ui.wizards.component.Component;

public class DideHelper {
	
	private IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	String fullPath = Platform.getInstallLocation().getURL().toString().replace("\\", "/");
	String didePath = fullPath.substring(6,(fullPath.substring(0,fullPath.length()-1)).lastIndexOf("/")+1);
	DecimalFormat df = new DecimalFormat("######0");
	
	public long toUnsigned(long s) {
		return s & 0xFFFFFFFFL;
	}

	/*
	 * 获取当前Eclipse的路径
	 */
	public String getDIDEPath() {
		return didePath;
	}
	
	public String getTemplatePath() {
		return fullPath.substring(6)+"djyosTemplate";
	}
	
	public String getDjyosSrcPath() {
		return didePath+"djysrc";
	}
	
	public String getUserBoardFilePath() {
		return didePath+"djysrc\\bsp\\boarddrv\\user";
	}
	
	public String getDemoBoardFilePath() {
		return didePath+"djysrc\\bsp\\boarddrv\\demo";
	}
	
	public String getRelativeUserBoardFilePath() {
		return "${DJYOS_SRC_LOCATION}/bsp/boarddrv/user/";
	}
	
	public String getRelativeDemoBoardFilePath() {
		return "${DJYOS_SRC_LOCATION}/bsp/boarddrv/demo/";
	}
	
	public void copyFolder(File src, File dest) throws IOException {  
	    if (src.isDirectory()) {  
	        if (!dest.exists()) {  
	            dest.mkdir();  
	        }  
	        String files[] = src.list();  
	        for (String file : files) {  
	            File srcFile = new File(src, file);  
	            File destFile = new File(dest, file);  
	            // 递归复制  
	            copyFolder(srcFile, destFile);  
	        }  
	    } else {  
	        InputStream in = new FileInputStream(src);  
	        OutputStream out = new FileOutputStream(dest);  
	  
	        byte[] buffer = new byte[1024];  
	  
	        int length;  
	          
	        while ((length = in.read(buffer)) > 0) {  
	            out.write(buffer, 0, length);  
	        }  
	        in.close();  
	        out.close();  
	    }  
	}  

	public boolean checkParameter(Component component, Boolean isApp, IProject curProject) {

		List<String> pjCgfs = new ArrayList<String>();
		File configFile = new File(curProject.getLocation().toString() + "/src/" + (isApp ? "app" : "iboot")
				+ "/OS_prjcfg/project_config.h");
		FileReader reader;
		try {
			reader = new FileReader(configFile.getPath());
			BufferedReader br = new BufferedReader(reader);
			String str = null;
			boolean start = false, stop = false;
			while ((str = br.readLine()) != null) {
				if (start && str.contains("Configure")) {
					stop = true;
					break;
				}
				if (start && !stop) {
					pjCgfs.add(str);// 添加当前组件的所有预定义值
				}
				if (str.contains("Configure") && str.contains(component.getName())) {
					start = true;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String configure = component.getConfigure();
		String[] parametersDefined = configure.split("\n");
		String tag = null;
		String[] infos = null;
		List<String> ranges = null;
		for (int i = 0; i < parametersDefined.length; i++) {
			String parameter = parametersDefined[i];
			if (parameter.contains("%$#@num") || parameter.contains("%$#@string")
					|| parameter.contains("%$#@enum") || parameter.contains("%$#@select")
					|| parameter.contains("%$#@free")) {
				if (parameter.contains("%$#@num")) {
					tag = "int";
				} else if (parameter.contains("%$#@string")) {
					tag = "string";
				} else if (parameter.contains("%$#@enum")) {
					tag = "enum";
				} else if (parameter.contains("%$#@select")) {
					tag = "select";
				} else if (parameter.contains("%$#@free")) {
					tag = "free";
				}

				infos = parameter.split(",|，");
				ranges = new ArrayList<String>();
				if (!tag.equals("select") && !tag.equals("free")) {
					for (int j = 1; j < infos.length; j++) {
						ranges.add(infos[j]);
					}
				}

			} else if (parameter.contains("#define")) {
				String[] defines = parameter.trim().split("//");
				String[] members = null;
				if (parameter.startsWith("//")) {
					members = defines[1].trim().split("\\s+");
				} else {
					members = defines[0].trim().split("\\s+");
				}

				List<String> rangesCopy = ranges;
				if (rangesCopy.size() != 0) {
					String minString = rangesCopy.get(0), maxString = rangesCopy.get(1);
					if (tag.equals("enum")) {

					} else if (tag.equals("select")) {

					} else {
						if (tag.equals("int")) {
							try {
								int min;
								long max;
								if (minString.startsWith("0x")) {
									min = Integer.parseInt(minString.substring(2), 16);
								} else {
									min = Integer.parseInt(minString);
								}
								if (maxString.startsWith("0x")) {
									max = Long.parseLong(maxString.substring(2), 16);
								} else {
									max = Long.parseLong(maxString);
								}
								long curData = -1;
								if (pjCgfs.size() > 0) {// 如果已存在该组件的配置
									for (String cfg : pjCgfs) {
										if (cfg.contains(members[1])) {
											String[] cfgs = cfg.trim().split("\\s+");
											if (cfgs[2].startsWith("0x")) {
												curData = Long.parseLong(cfgs[2].substring(2), 16);
											} else if (cfgs[2].contains("+") || cfgs[2].contains("-")
													|| cfgs[2].contains("*") || cfgs[2].contains("/")) {
												String pureCal = cfgs[2].replaceAll("\\(|\\)", "");
												if (pureCal.startsWith("-")) {
													curData = toUnsigned(Long.parseLong(pureCal));
												} else {
													double result = Calculator.conversion(pureCal);
													BigDecimal bd = new BigDecimal(df.format(result));
													curData = Long.valueOf(bd.toPlainString());
												}
											} else {
												curData = Integer.parseInt(cfgs[2].replaceAll("\\(|\\)", ""));
											}

											break;
										}
									}

								} else {
									if (members[2].startsWith("0x")) {
										curData = Long.parseLong(members[2].substring(2), 16);
									} else if (members[2].contains("+") || members[2].contains("-")
											|| members[2].contains("*") || members[2].contains("/")) {
										String pureCal = members[2].replaceAll("\\(|\\)", "");
										if (pureCal.startsWith("-")) {
											curData = toUnsigned(Long.parseLong(pureCal));
										} else {
											double result = Calculator.conversion(pureCal);
											BigDecimal bd = new BigDecimal(df.format(result));
											curData = Long.valueOf(bd.toPlainString());
										}
									} else {
										curData = Integer.parseInt(members[2].replaceAll("\\(|\\)", ""));
									}
								}

								if (curData < min || curData > max) {
									return false;
								}
							} catch (Exception e) {
								// TODO: handle exception
								MessageDialog.openError(window.getShell(), "提示",
										"组件" + component.getName() + "配置信息有误，" + e.getMessage());
							}

						} else if (tag.equals("string")) {
							try {
								int min, max;
								String value = null;
								min = Integer.parseInt(minString);
								max = Integer.parseInt(maxString);
								if (pjCgfs.size() > 0) {// 如果已存在该组件的配置
									for (String cfg : pjCgfs) {
										String[] cdefines = cfg.split("//");
										if (cfg.contains(members[1])) {
											String[] cfgs = cdefines[0].trim().split("\\s+");
											int begin = cdefines[0].indexOf("\"");
											int end = cdefines[0].lastIndexOf("\"");
											value = cdefines[0].substring(begin + 1, end);
											break;
										}
									}

								} else {
									int begin = defines[0].indexOf("\"");
									int end = defines[0].lastIndexOf("\"");
									value = defines[0].substring(begin + 1, end);
								}
								if (value.length() < min || value.length() > max) {
									return false;
								}
							} catch (Exception e) {
								// TODO: handle exception
								MessageDialog.openError(window.getShell(), "提示",
										"组件" + component.getName() + "配置信息有误，" + e.getMessage());
							}
						}
					}
				}
			}
		}
		return true;

	}
	
	
	
}
