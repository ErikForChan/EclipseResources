package org.eclipse.cdt.ui.wizards;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.internal.resources.Folder;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;

import org.eclipse.cdt.ui.wizards.parsexml.Board;
import org.eclipse.cdt.ui.wizards.parsexml.Cpu;
import org.eclipse.cdt.ui.wizards.parsexml.CreateBoardXml;
import org.eclipse.cdt.ui.wizards.parsexml.ReadBoardByDom;
import org.eclipse.cdt.ui.wizards.parsexml.ReadCpuByDom;
import org.eclipse.cdt.ui.wizards.parsexml.ReviseLinkToXML;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.preferences.PreferencesMessages;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ComboDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringDialogField;

public class SelectOrCreateBoardDialog extends StatusDialog{
	
		private class CompilerImportBoardAdapter implements IDialogFieldListener {
			@Override
			public void dialogFieldChanged(DialogField field) {
				doValidation();
			}
		}
		
		private void doValidation() {
			StatusInfo status = new StatusInfo();
			String newText = boardSelectField.getText();
			if (newText.isEmpty()) {
				status.setError(PreferencesMessages.TodoTaskInputDialog_error_enterName); 
			} else {
				if (newText.indexOf(',') != -1) {
					status.setError(PreferencesMessages.TodoTaskInputDialog_error_comma); 
				}
			}
			updateStatus(status);
		}
		
		public String getBoardName() {
			return boardName;
		}
		
		public Cpu getSelectCpu() {
			return selectCpu;
		}
		
		public Board getSelectBoard() {
			return boardSelected;
		}

		private Text boardSelectField;
		private Text boardCreateField;
		private StringDialogField[] fDialogFields = new StringDialogField[2];
		private ComboDialogField[] fComboDialogFields = new ComboDialogField[2];
		private static Button[] radioBtns = new Button[2];
		int selectIndex = 0;
		String boardName = "";
		Board boardSelected;
		Cpu selectCpu;
		String boardModuleTrimPath = "";
		Cpu defaultCpu;
				
		private String[] BoardDetailsComboLabels = {
				"Architecture","Family"
		};
		
		private String[] BoardDetailsTextLabels = {
				"CPU Name","External clock"
		};
		
		private String[] Architectures = {
				"armv4","armv5","armv6","armv7","armv7-m","armv7e-m"
		};
		
		private String[] Families = {
				"cortex-m0","cortex-m3","cortex-m4","cortex-m7"
		};
		
		Composite content,baordDescCpt;
		Text boardDetailsDesc,MCUNameField;
		Button importMCUBtn;
		boolean toCreat = false;
		
		public boolean isToCreat() {
			return toCreat;
		}
		
		public List<String> getBoards(){
			List<String> boards = new ArrayList<String>();
			String newBoardPath = getEclipsePath()+"djysrc/bsp/boarddrv";
			File boardSrc = new File(newBoardPath);
			String files[] = boardSrc.list();  
	        for (String file : files) {  
	        	boards.add(file);
	        }  
			return boards;
		}
		
		public String getEclipsePath() {
			String fullPath = Platform.getInstallLocation().getURL().toString();
			String eclipsePath = fullPath.substring(6,(fullPath.substring(0,fullPath.length()-1)).lastIndexOf("/")+1);
			return eclipsePath;
		}
		
		private  Listener cpuModifyListener = e -> {
			if(MCUNameField.getText().trim() != null) {
				Cpu defaultCpu = null;
				String cpuXmlPath = getEclipsePath()+"demo\\cpu.xml";
				ReadCpuByDom rcb = new ReadCpuByDom();
				List<Cpu> cpus = null;
				try {
					cpus = rcb.getCpus(cpuXmlPath);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				String cpuName = MCUNameField.getText();
				for(int i=0;i<cpus.size();i++) {
					if(cpus.get(i).getDevice().equals(cpuName)) {
						selectCpu = cpus.get(i);
					}
				}
				boardDetailsDesc.setText("Device: "+selectCpu.getDevice()+
						"\n"+"Core: "+selectCpu.getCore()+
						"\n"+"Architecture: "+selectCpu.getArchitecture()+
						"\n"+"FpuType: "+selectCpu.getFpuType()+
						"\n"+"Flash Start: "+selectCpu.getFlashStart()+
						"\n"+"Flash Size: "+selectCpu.getFlashSize()+
						"\n"+"Ram Start: "+selectCpu.getRamStart()+
						"\n"+"Ram Size: "+selectCpu.getRamSize()
						);
			}
		};
		
		private  Listener boardModifyListener = e -> {
			if(boardSelectField.getText().trim() != null) {
				List<String> boards = getBoards();
				toCreat = true;
				boardName = boardSelectField.getText();
				for(String board:boards) {
					if(board.equals(boardName)) {
						toCreat = false;
					}
				}
				if(toCreat) {
//					importMCUBtn.setEnabled(true);
					for(int i=1;i<fDialogFields.length;i++) {
						fDialogFields[i].getTextControl(content).setEnabled(true);
					}
				}else {
//					importMCUBtn.setEnabled(false);
					fDialogFields[1].getTextControl(content).setEnabled(false);
				}
			}
		};
		
		@Override
		protected void okPressed() {
			// TODO Auto-generated method stub
			String cpuXmlPath = getEclipsePath()+"demo\\cpu.xml";
			List<Cpu> cpus = new ArrayList<Cpu>();
			ReadCpuByDom rcb = new ReadCpuByDom();
			try {
				cpus = rcb.getCpus(cpuXmlPath);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String cpuName = MCUNameField.getText();
			for(int i=0;i<cpus.size();i++) {
				if(cpus.get(i).getDevice().equals(cpuName)) {
					defaultCpu = cpus.get(i);
				}
			}
			
			boolean toCreat = true;
			List<String> boards = getBoards();
			boardName = boardSelectField.getText();
			for(String board:boards) {
				if(board.equals(boardName)) {
					toCreat = false;
				}
			}
			if(toCreat) {
//				boardSelected = new Board();
//				boardSelected.setBoardName(boardName);
//				boardSelected.setExClk(fDialogFields[1].getTextControl(content).getText());
//				selectCpu = defaultCpu;
//				boardSelected.setCpu(selectCpu);
				boardSelected.setExClk(fDialogFields[1].getTextControl(content).getText());
				if(selectCpu == null) {
					selectCpu = defaultCpu;
				}
				boardSelected.setCpu(selectCpu);
			}else {
				selectCpu = defaultCpu;
				boardSelected.setExClk(fDialogFields[1].getTextControl(content).getText());
			}
			super.okPressed();
		}
		
		private void copyFolder(File src, File dest) throws IOException {  
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
		
		private void copyFileToFolder(File src, File dest, String boardName) throws IOException {  
		    if (src.isDirectory()) {  
		        if (!dest.exists()) {  
		            dest.mkdir(); 
		            dest.renameTo(new File(dest.getAbsolutePath().substring(0, dest.getAbsolutePath().lastIndexOf("\\"))+"\\"+boardName));
		            dest = new File(dest.getAbsolutePath().substring(0, dest.getAbsolutePath().lastIndexOf("\\"))+"\\"+boardName);
		        }  
		        String files[] = src.list();  
		        for (String file : files) {  
		            File srcFile = new File(src, file);  
		            File destFile = new File(dest, file); 
		            System.out.println(destFile.getName());
		            // 递归复制  
		            copyFileToFolder(srcFile, destFile,boardName);  
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

		public SelectOrCreateBoardDialog(Shell parent) {
			super(parent);
			setTitle("SelectOrCreateBoardDialog");
			setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX );
		}
		
		

		@Override
		protected Point getInitialSize() {
			// TODO Auto-generated method stub
			return new Point(500,500);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			// TODO Auto-generated method stub
			String tipText = "板件模板会陆续添加.";
			CompilerImportBoardAdapter adapter = new CompilerImportBoardAdapter();
			Composite composite = (Composite) super.createDialogArea(parent);
			composite.setSize(500,500);
			GridLayout layout = new GridLayout();
			layout.marginHeight = 5;
			layout.numColumns = 1;
			layout.marginLeft=5;
			Composite tipCpt = new Composite(composite, SWT.NONE);
			tipCpt.setLayout(layout);
			tipCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			Label tipLabel = new Label(tipCpt,SWT.NONE);
			tipLabel.setText(tipText);
			tipLabel.setForeground(tipCpt.getDisplay().getSystemColor(SWT.COLOR_RED));
			
			content = new Composite(composite, SWT.NONE);
			layout.numColumns = 3;
			content.setLayout(layout);
			content.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//			selectBaordCpt = new Composite(composite, SWT.NONE);
//			selectBaordCpt.setLayout(layout);
//			selectBaordCpt.setLayoutData(new GridData(GridData.FILL_BOTH));

			Label boardSelectLabel = new Label(content,SWT.NONE);
			boardSelectLabel.setText("Select board: ");
			boardSelectField = new Text(content, SWT.BORDER);
			boardSelectField.setSize(50, 10);
			boardSelectField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//			boardSelectField.setEnabled(false);
			Button importOrNewBtn = new Button(content, SWT.PUSH);
			importOrNewBtn.setText(" Select...  ");
			importOrNewBtn.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					handleImportButtonPressed();
				}
			});
			boardSelectField.addListener(SWT.Modify, boardModifyListener);

//			MCUCpt = new Composite(composite, SWT.NONE);
//			layout.marginHeight = 15;
//			layout.numColumns = 3;
//			layout.marginLeft=10;
//			MCUCpt.setLayout(layout);
//			MCUCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			Label MCULabel = new Label(content, SWT.NONE);
			MCULabel.setLayoutData(new GridData(GridData.BEGINNING));
			MCULabel.setText(BoardDetailsTextLabels[0]+":    ");
			MCUNameField = new Text(content, SWT.BORDER);
			MCUNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			MCUNameField.setEnabled(false);
			importMCUBtn = new Button(content, SWT.PUSH);
			importMCUBtn.setText("Choose...");
//			importMCUBtn.setEnabled(false);
			importMCUBtn.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					handleChooseButtonPressed();
				}

			});
			MCUNameField.addListener(SWT.Modify, cpuModifyListener);
					
//			detailsCpt = new Composite(composite, SWT.NONE);
//			layout.marginHeight = 5;
//			layout.numColumns = 4;
//			layout.marginLeft=10;
//			layout.verticalSpacing = 20;
//			detailsCpt.setLayout(layout);
//			detailsCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			fDialogFields[1] = new StringDialogField();
			fDialogFields[1].setLabelText(BoardDetailsTextLabels[1] + ":");
			fDialogFields[1].getLabelControl(content).setLayoutData(new GridData(GridData.BEGINNING));
			fDialogFields[1].getTextControl(content).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			fDialogFields[1].getTextControl(content).setEnabled(false);
			ControlFactory.createLabel(content, "MHz");
			
			baordDescCpt = new Composite(composite, SWT.NONE);
			GridLayout gl = new GridLayout();
			gl.numColumns = 1;
			gl.marginHeight=10;
			gl.marginLeft=10;
			baordDescCpt.setLayout(gl);
			baordDescCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			Label baordDescLabel = new Label(baordDescCpt, SWT.NONE);
			baordDescLabel.setText("Board Description:");
			boardDetailsDesc = new Text(baordDescCpt,SWT.MULTI | SWT.WRAP | SWT.BORDER);
			boardDetailsDesc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			boardDetailsDesc.setText("Device: "+""+
			"\n"+"Core: "+""+
			"\n"+"Architecture: "+""+
			"\n"+"FpuType: "+""+
			"\n"+"Flash Start: "+""+
			"\n"+"Flash Size: "+""+
			"\n"+"Ram Start: "+""+
			"\n"+"Ram Size: "+""
			);
			
			boardDetailsDesc.setEditable(false);
			boardDetailsDesc.setBounds(0, 0, 200, 700);
			super.createDialogArea(parent);
			return super.createDialogArea(parent);
		}
		
		protected void handleChooseButtonPressed() {
	
			ChooseMCUDialog dialog = new ChooseMCUDialog(getShell());
			if (dialog.open() == Window.OK) {
				selectCpu = dialog.getSelectCpu();
				MCUNameField.setText(selectCpu.getDevice());
				if(boardSelected != null) {
					boardSelected.setCpu(selectCpu);
				}			
			}
		}
	
		/**
		 * The browse button has been selected. Select the location.
		 */
		protected void handleImportButtonPressed() {
	
			ChooseBoardDialog dialog;
			String sCpu = MCUNameField.getText();
			
			dialog = new ChooseBoardDialog(getShell(),sCpu);
			
			if (dialog.open() == Window.OK) {
				boardSelected = dialog.getSelectBoard();
				boardModuleTrimPath = getEclipsePath()+"djysrc/bsp/boarddrv/"+boardSelected.getBoardName()+"/module-trim.bak";
				boardSelectField.setText(boardSelected.getBoardName());
				MCUNameField.setText(boardSelected.cpu.getDevice());
				selectCpu = boardSelected.cpu;
				fDialogFields[1].getTextControl(content).setText(boardSelected.getExClk());
			}
	
		}

		@Override
		protected void configureShell(Shell shell) {
			// TODO Auto-generated method stub
			super.configureShell(shell);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, ICHelpContextIds.TODO_TASK_INPUT_DIALOG);
		} 
}
