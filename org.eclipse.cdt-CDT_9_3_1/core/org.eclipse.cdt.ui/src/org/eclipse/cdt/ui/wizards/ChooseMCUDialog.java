package org.eclipse.cdt.ui.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import org.eclipse.cdt.ui.wizards.parsexml.AddCpuToXML;
import org.eclipse.cdt.ui.wizards.parsexml.Board;
import org.eclipse.cdt.ui.wizards.parsexml.Cpu;
import org.eclipse.cdt.ui.wizards.parsexml.ReadCpuByJDom;

import org.eclipse.cdt.internal.ui.newui.Messages;

public class ChooseMCUDialog extends StatusDialog{

	public ChooseMCUDialog(Shell parent) {
		super(parent);
		setTitle("ChooseCPU");
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX );		
	}
	
	String fullPath = Platform.getInstallLocation().getURL().toString();
	String eclipsePath = fullPath.substring(6,(fullPath.substring(0,fullPath.length()-1)).lastIndexOf("/")+1);
	String cpuXmlPath = eclipsePath+"/demo/cpu.xml";
	private Button addMCUBtn;
	private Label MCUTypeLabel;
	private Combo MCUTypeCombo;
	private Table MCUListTable;
	private Text MCUEditText;
	private TableViewer tv;
	private TableColumn[] tableColumns = new TableColumn[8];
	private String[] MCUDetails = {"Device","Core","Architecture","Flash Start","Flash Size","RAM Start","RAM Size","FPU Type"};
	List<Cpu> cpus = new ArrayList<Cpu>();
	List<Cpu> cpusFiltered = new ArrayList<Cpu>();
	int selectIndex = 0;

	public Cpu getSelectCpu() {
		return cpusFiltered.get(selectIndex);
	}
	
	public List<Cpu> getCpusFiltered(List<Cpu> allCpus,String keyWord){
		
		cpusFiltered = new ArrayList<Cpu>();
		Pattern pattern = Pattern.compile(keyWord,Pattern.CASE_INSENSITIVE);
		for(int i=0;i<allCpus.size();i++) {
			Matcher matcher = pattern.matcher((allCpus.get(i)).getDevice());
			Matcher matcher2 = pattern.matcher((allCpus.get(i)).getCore());
			Matcher matcher3 = pattern.matcher((allCpus.get(i)).getArchitecture());
			if(matcher.find() || matcher2.find() || matcher3.find()) {
				cpusFiltered.add(allCpus.get(i));
			}
		}
		return cpusFiltered;
		
	}
	
	private  Listener searchModifyListener = e -> {
		//setLocationForSelection();
		String keyword = MCUEditText.getText().trim();
		List<Cpu> cpusFiltered = getCpusFiltered(cpus,keyword);
		tv.setInput(cpusFiltered);
	};
	
	public class CpuLabelProvider extends LabelProvider implements ITableLabelProvider {  
        public String getColumnText(Object element, int columnIndex) {  
            if (element instanceof Cpu){  
                Cpu cpu = (Cpu)element;  
                if(columnIndex == 0){  
                    return cpu.getDevice();  
                }else if(columnIndex == 1){  
                    return cpu.getCore();  
                }else if (columnIndex ==2){  
                    return cpu.getArchitecture();  
                }else if (columnIndex == 3){  
                    return cpu.getFlashStart();  
                }else if (columnIndex == 4){  
                    return cpu.getFlashSize();  
                }else if (columnIndex == 5){  
                    return cpu.getRamStart();  
                }else if (columnIndex == 6){  
                    return cpu.getRamSize();  
                }else if (columnIndex == 7){  
                    return cpu.getFpuType();  
                }  
            }  
            return null;  
        }  
        public Image getColumnImage(Object element, int columnIndex) {  
            return null;  
        }  
    }  
	
	public void setMCUList() {
		ReadCpuByJDom rcb = new ReadCpuByJDom();
		try {
			cpus = rcb.getCpus(cpuXmlPath);
			cpusFiltered = cpus;
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		tv.setInput(cpus);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = (Composite) super.createDialogArea(parent);
		GridData gd;
		
		Composite MCUTypeCpt = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth = false;
		layout.marginWidth = 0;
		layout.verticalSpacing=10;
		MCUTypeCpt.setLayout(layout);
		MCUTypeCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		MCUTypeLabel = new Label(MCUTypeCpt, SWT.None);
		MCUTypeLabel.setText("Cpu Search :");
		MCUEditText = new Text(MCUTypeCpt, SWT.BORDER);
		MCUEditText.addListener(SWT.Modify, searchModifyListener);
		
		addMCUBtn = new Button(MCUTypeCpt, SWT.PUSH);
		addMCUBtn.setText("New Cpu");
		addMCUBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleAddButtonPressed();
			}

			private void handleAddButtonPressed() {
				// TODO Auto-generated method stub
				AddCpuDialog dialog = new AddCpuDialog(getShell());
				AddCpuToXML actx = new AddCpuToXML();
				if (dialog.open() == Window.OK) {
					Cpu cpu = dialog.getCpu();
					if(cpu == null) {
						System.out.println("selectCpu == null");
					}
					actx.addCpu(cpu,cpuXmlPath);
					setMCUList();
				}
			}
		});
//		MCUEditText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		MCUTypeCombo = new Combo(MCUTypeCpt, SWT.None);
//		MCUTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
		
		Composite MCUListCpt = new Composite(composite, SWT.NONE);
		layout.marginWidth = 0;
		MCUListCpt.setLayout(layout);
		MCUListCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_CENTER));
		MCUListTable = new Table(MCUListCpt, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.H_SCROLL | SWT.FULL_SELECTION);
		MCUListTable.setHeaderVisible(true);
		MCUListTable.setLinesVisible(true);
		MCUListTable.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectIndex = MCUListTable.getSelectionIndex();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
//				if (buttonIsEnabled(2) && table.getSelectionIndex() != -1)
//					buttonPressed(2);
			}
		});

		tv = new TableViewer(MCUListTable);
		tv.setContentProvider(new IStructuredContentProvider() {

			@Override
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof List) {
					return ((List) inputElement).toArray();
				} else {
					return new Object[0];
				}
			}

			@Override
			public void dispose() {
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		
		tv.setLabelProvider(new CpuLabelProvider());
		for(int i=0;i<tableColumns.length;i++) {
			tableColumns[i] = new TableColumn(MCUListTable, SWT.LEFT);
			tableColumns[i].setText(MCUDetails[i]);
			tableColumns[i].setWidth(100);
		}
		
		setMCUList();

		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 3;
		MCUListTable.setLayoutData(gd);
		
		return super.createDialogArea(parent);
	}
	
}
