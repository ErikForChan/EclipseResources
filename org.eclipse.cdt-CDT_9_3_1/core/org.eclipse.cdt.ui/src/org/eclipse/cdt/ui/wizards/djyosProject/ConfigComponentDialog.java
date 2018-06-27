package org.eclipse.cdt.ui.wizards.djyosProject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.ui.wizards.component.Component;
import org.eclipse.cdt.ui.wizards.component.InitInfo;
import org.eclipse.cdt.ui.wizards.component.Parameter;

public class ConfigComponentDialog extends StatusDialog {

	private Hashtable<TableItem, TableItemControls> tablecontrols = new Hashtable<TableItem, TableItemControls>();
	private String configure = null;
	private String[] parametersDefined = null;
	private Component component;
	private Table table;
	private List<Parameter> parameters;
	private IProject curProject;
	private boolean isProperty;
	private String projectTag;
	private boolean[] isSelect = null;
	
	public ConfigComponentDialog(Shell parent,Component componentSelect,IProject project,boolean isPty,String tag) {
		super(parent);
		// TODO Auto-generated constructor stub
		component = componentSelect;
		configure = component.getConfigure();
//		System.out.println("configure:  "+configure+"  "+component.getName()+"  "+component.getCode());
		parametersDefined = configure.split("\n");
		isProperty = isPty;
		curProject = project;
		projectTag = tag;
		setTitle("配置组件");
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX );
	}
	
	public Component getComponent() {
		return component;
	}

	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		TableItem[] items = table.getItems();
		String newConfig = "";
		int itemCount = 0;
		//给所有define重设值
		for (int i = 0; i < parametersDefined.length; i++) {
			if(parametersDefined[i].contains("#define")) {
				String[] defines = parametersDefined[i].trim().split("//");
				String[] members = null;
            	String annoation = null;
            	if(parametersDefined[i].startsWith("//")) {
            		members = defines[1].trim().split("\\s+");
            		annoation = defines.length>2?defines[2]:"";
            		
            	}else {
            		members = defines[0].trim().split("\\s+");
            		annoation = defines.length>1?defines[1]:"";
            	}
	        	//define格式化
            	if(isSelect[i]) {
        			parametersDefined[i] = String.format("%-11s",members[0])+" "+String.format("%-32s", members[1])+" "+String.format("%-18s", items[itemCount].getText(1))+"//"+annoation;
        		}else {
        			parametersDefined[i] = String.format("%-11s","//"+members[0])+" "+String.format("%-32s", members[1])+" "+String.format("%-18s", items[itemCount].getText(1))+"//"+annoation;
        		}
							
				itemCount++;
			}
			newConfig += parametersDefined[i]+"\n";
		}
		component.setConfigure(newConfig);
		super.okPressed();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = (Composite) super.createDialogArea(parent);
		table = new Table(composite, SWT.NONE | SWT.FULL_SELECTION);
		GridData gd = new GridData(SWT.LEFT, SWT.TOP, false, true);
		gd.minimumHeight =100;
		table.setLayoutData(gd);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		// 创建表头的字符串数组  
        String[] tableHeader = {"参数", "值", "注释"};
        for (int i = 0; i < tableHeader.length; i++)  
        {  
            TableColumn tableColumn = new TableColumn(table, SWT.NONE);  
            tableColumn.setAlignment(SWT.LEFT);
            tableColumn.setText(tableHeader[i]);  

            // 设置表头可移动，默认为false  
            tableColumn.setMoveable(true);         
            if(i==tableHeader.length-1) {
            	tableColumn.setWidth(220);
            }else {
            	tableColumn.setWidth(130);
            }
        }  
        
        List<String> pjCgfs = new ArrayList<String>();
        
        //解析project_config.h文件,获取当前组件的所有预定义值
        if(isProperty) {
			File configFile = new File(curProject.getLocation().toString()+ "/src/"+projectTag+"/OS_prjcfg/project_config.h");
			FileReader reader;
			try {
				reader = new FileReader(configFile.getPath());
				BufferedReader br = new BufferedReader(reader);
				String str = null;
				String compName = component.getName();
				boolean start = false,stop=false;
				while ((str = br.readLine()) != null) {				
					if (start && str.contains("Configure")) {
						stop = true;
						break;
					}	
					if (start && !stop) {
						pjCgfs.add(str);//添加当前组件的所有预定义值
					}
					if (str.contains("Configure") && str.contains(compName)) {
						start = true;
					}									
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
        
        String tag = null;
        String[] infos = null;
        List<String> ranges = null;
        isSelect = new boolean[parametersDefined.length];
        for(int i=0;i<parametersDefined.length;i++) {
        	isSelect[i] =false;
        	String parameter = parametersDefined[i];
        	if(parameter.contains("//%$#@num") || parameter.contains("%$#@string") || parameter.contains("%$#@enum")
        			|| parameter.contains("%$#@select") || parameter.contains("%$#@free")) {
        		if(parameter.contains("//%$#@num")) {
            		tag = "int";
            	}else if(parameter.contains("%$#@string")) {
            		tag = "string";
            	}else if(parameter.contains("%$#@enum")) {
            		tag = "enum";
            	}else if(parameter.contains("%$#@select")) {
            		tag = "select";
            	}else if(parameter.contains("%$#@free")) {
            		tag = "free";
            	}
        		
				infos = parameter.split(",");
				ranges = new ArrayList<String>();
				if (!tag.equals("select") && !tag.equals("free")) {
					for (int j = 1; j < infos.length; j++) {//for (int j = 1; j < infos.length; j++)
						ranges.add(infos[j]);
					}
				}

        	}
	
        	if(parameter.contains("#define")) {
        		TableItem item = new TableItem(table, SWT.NONE);
        		Image image = new Image(PlatformUI.getWorkbench().getDisplay(), 1, 25);
        		item.setImage(image);
        		String[] defines = parameter.trim().split("//");
            	String[] members = null;
            	String annoation = null;
            	if(parameter.startsWith("//")) {
            		members = defines[1].trim().split("\\s+");
            		annoation = defines.length>2?defines[2]:"";
            	}else {
            		members = defines[0].trim().split("\\s+");
            		annoation = defines.length>1?defines[1]:"";
            	}
            	String realComptName = null;
        		String[] annos = annoation.split(",");
        		if(annos[0].trim().startsWith("\"") && annos[0].trim().endsWith("\"")) {
        			annoation = annoation.substring(annos[0].length()+1,annoation.length());
        			if(!annos[0].contains("name")) {
        				realComptName = annos[0].trim().replaceAll("\"", "");
        			}else {
        				realComptName = members[1];
        			}
        			
        		}else {
        			realComptName = members[1];
        		}
            	if(members.length>2) {
            		if(isProperty && pjCgfs.size()>0) {//如果是通过右键Properties打开的界面，则显示修改后的数值
            			for(String cfg:pjCgfs) {
            				if(cfg.contains(members[1])) {
            					String[] cfgs = cfg.trim().split("\\s+");
            					item.setText(new String[]{realComptName, cfgs[2].equals("default")?"":cfgs[2], defines.length>1?annoation:""});
            					break;
            				}
            			}
            			
            		}else {
            			item.setText(new String[]{realComptName, members[2].equals("default")?"":members[2], defines.length>1?annoation:""}); 
            		}
            		
            	}else {
            		item.setText(new String[]{realComptName, "", defines.length>1?annoation:""}); 
            	}
            	
            	
            	TableEditor editor = new TableEditor(table);
    			// 设置编辑单元格水平填充
    			editor.grabHorizontal = true;
    			
    			if(tag.equals("enum")) {
    				isSelect[i] = true;
    				Combo combo = new Combo(table, SWT.READ_ONLY);
    				combo.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
    				if(ranges!=null) {
    					combo.setItems(ranges.toArray(new String[ranges.size()]));
    				}
    				for(int j=0;j<ranges.size();j++) {
    					if(ranges.get(j).equals(item.getText(1))) {
    						combo.select(j);
    						break;
    					}
    				}
    				editor.setEditor(combo, item, 1);
    				combo.addSelectionListener(new SelectionListener() {
						
						@Override
						public void widgetSelected(SelectionEvent e) {
							// TODO Auto-generated method stub
							editor.getItem().setText(1, combo.getText());
						}
						
						@Override
						public void widgetDefaultSelected(SelectionEvent e) {
							// TODO Auto-generated method stub
							
						}
					});
    			}else if(tag.equals("select")) {
    				Button checkBtn = new Button(table,SWT.CHECK);
        			editor.setEditor(checkBtn, item, 1);
        			int cur = i;
        			if(parameter.startsWith("//")){
        				isSelect[i] = false;
        				checkBtn.setSelection(false);
        			}else {
        				isSelect[i] = true;
        				checkBtn.setSelection(true);
        			}
					checkBtn.addListener(SWT.CHECK, new Listener() {

						@Override
						public void handleEvent(Event event) {
							// TODO Auto-generated method stub
							boolean checked = checkBtn.getSelection();
							if(checked) {
								isSelect[cur] = true;
							}else {
								isSelect[cur] = false;
							}								
						}
					});
    			}else{
    				isSelect[i] = true;
    				// 创建一个文本框，用于输入文字
        			Text text = new Text(table, SWT.BORDER);
        			text.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
        			// 将文本框当前值，设置为表格中的值
        			text.setText(item.getText(1));
        			// 关键方法，将编辑单元格与文本框绑定到表格的第一列
        			editor.setEditor(text, item, 1);
        			String flag = tag;
        			List<String> rangesCopy = ranges;      			
        			// 当文本框改变值时,注册文本框改变事件，该事件改变表格中的数据,否则即使改变的文本框的值，对表格中的数据也不会影响
        			text.addModifyListener(new ModifyListener() {
        				public void modifyText(ModifyEvent e) {
        					String tempString = text.getText();
        					IWorkbenchWindow window = PlatformUI.getWorkbench()
        							.getActiveWorkbenchWindow();
        					if(rangesCopy.size()>0) {
        						if(flag.equals("int")) {
            						int min = Integer.parseInt(rangesCopy.get(0));
            	    				int max = Integer.parseInt(rangesCopy.get(1));
            	    				int curNum = -1;       
            	    				Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");  
            	    		        boolean isInt =  pattern.matcher(tempString).matches();
            	    				if(tempString.startsWith("0x")) {
            	    					curNum = Integer.parseInt(tempString.substring(2),16);
            	    				}else {
            	    					if(isInt) {
            	    						curNum = Integer.parseInt(tempString);
            	    					}else {
            	    						MessageDialog.openError(window.getShell(), "提示",
                        							"请填写在之"+min+"与"+max+"之间的整数");
            	    					}
            	    					
            	    				}             					
                    				if(curNum<min || curNum>max) {
                    					MessageDialog.openError(window.getShell(), "提示",
                    							"请填写在之"+min+"与"+max+"之间的整数");
                    				}
                    			}else if(flag.equals("string")) {
                    				int min = Integer.parseInt(rangesCopy.get(0));
                    				int max = Integer.parseInt(rangesCopy.get(1));
                    				if(tempString.length()<min || tempString.length()>max) {
                    					MessageDialog.openError(window.getShell(), "提示",
                    							"字符串长度不得小于"+min+"或者大于"+max);
                    				}
                    			} 
        					}
        					
        					editor.getItem().setText(1, text.getText());
        				}

        			});
    			}
    			
        	}	
        }
        	
		return super.createDialogArea(parent);
	}
	
	public class TableItemControls  
    {  
        Text text;  
  
        CCombo combo;  
  
        TableEditor tableeditor;  
  
        TableEditor tableeditor1;  
  
        public TableItemControls(Text text, CCombo combo,  
                TableEditor tableeditor, TableEditor tableeditor1)  
        {  
            // super();  
            this.text = text;  
            this.combo = combo;  
            this.tableeditor = tableeditor;  
            this.tableeditor1 = tableeditor1;  
        }  
  
        public void dispose()  
        {  
            text.dispose();  
            combo.dispose();  
            tableeditor.dispose();  
            tableeditor1.dispose();  
        }  
    };  

}
