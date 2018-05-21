package org.eclipse.cdt.ui.wizards.djyosProject;

import java.util.Hashtable;
import java.util.List;

import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import org.eclipse.cdt.ui.wizards.component.Component;
import org.eclipse.cdt.ui.wizards.component.InitInfo;
import org.eclipse.cdt.ui.wizards.component.Parameter;

public class ConfigComponentDialog extends StatusDialog {

	private Hashtable<TableItem, TableItemControls> tablecontrols = new Hashtable<TableItem, TableItemControls>();
    String configure = null;
    String[] parametersDefined = null;
	private Component component;
	private Table table;
	List<Parameter> parameters;
	
	public ConfigComponentDialog(Shell parent,Component componentSelect) {
		super(parent);
		// TODO Auto-generated constructor stub
		component = componentSelect;
		configure = component.getConfigure();
		parametersDefined = configure.split("\n");
		setTitle("�������");
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
		for (int i = 0; i < items.length; i++) {
			String[] members = parametersDefined[i].split("\\s+");
			parametersDefined[i] = members[0]+" "+members[1]+" "+items[i].getText(1)+" // "+members[4]+"\n";
			newConfig+=parametersDefined[i];
//			parameters.get(i).setValue(items[i].getText(1));
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
		gd.heightHint = 100;
		table.setLayoutData(gd);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		// ������ͷ���ַ�������  
        String[] tableHeader = {"����", "ֵ", "ע��"};
        for (int i = 0; i < tableHeader.length; i++)  
        {  
            TableColumn tableColumn = new TableColumn(table, SWT.NONE);  
            tableColumn.setAlignment(SWT.CENTER);
            tableColumn.setText(tableHeader[i]);  
            // ���ñ�ͷ���ƶ���Ĭ��Ϊfalse  
            tableColumn.setMoveable(true);         
            if(i==tableHeader.length-1) {
            	tableColumn.setWidth(150);
            }else {
            	tableColumn.setWidth(80);
            }
        }  
        
        for(int i=0;i<parametersDefined.length;i++) {
        	TableItem item = new TableItem(table, SWT.NONE);
        	String[] members = parametersDefined[i].split("\\s+");
//   s     	item.setText(new String[]{"����"+(i+1), parameters.get(i).getType(), "", parameters.get(i).getAnnotation()}); 
        	item.setText(new String[]{members[1], members[2].equals("default  ")?"":members[2], members[4]}); 
        }
        	
        	
//        InitInfo initInfo = component.getInit();
//        parameters = initInfo.getParameters();
//        for(int i=0;i<parameters.size();i++) {
//        	 TableItem item = new TableItem(table, SWT.NONE);
//             item.setText(new String[]{"����"+(i+1), parameters.get(i).getType(), "", parameters.get(i).getAnnotation()}); 
//        }
	
        TableItem[] items = table.getItems(); 
		for (int i = 0; i < items.length; i++) {
			TableEditor editor = new TableEditor(table);
			// ���ñ༭��Ԫ��ˮƽ���
			editor.grabHorizontal = true;
			// ����һ���ı���������������
			Text text = new Text(table, SWT.BORDER);
			// ���ı���ǰֵ������Ϊ����е�ֵ
			text.setText(items[i].getText(1));
			// �ؼ����������༭��Ԫ�����ı���󶨵����ĵ�һ��
			editor.setEditor(text, items[i], 1);

			// ���ı���ı�ֵʱ��ע���ı���ı��¼������¼��ı����е����ݡ�
			// ����ʹ�ı���ı����ֵ���Ա���е�����Ҳ����Ӱ��
			text.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					editor.getItem().setText(1, text.getText());
				}

			});
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
