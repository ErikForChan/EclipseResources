package org.eclipse.cdt.ui.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;

import org.eclipse.cdt.ui.wizards.DjyosMainWizardPage.BoardDetails;
import org.eclipse.cdt.ui.wizards.parsexml.Board;
import org.eclipse.cdt.ui.wizards.parsexml.Cpu;
import org.eclipse.cdt.ui.wizards.parsexml.CreateBoardXml;
import org.eclipse.cdt.ui.wizards.parsexml.ReadBoardByDom;
import org.eclipse.cdt.ui.wizards.parsexml.ReadCpuByJDom;
import org.eclipse.cdt.ui.wizards.parsexml.ReviseLinkToXML;

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
		private StringDialogField[] fDialogFields = new StringDialogField[3];
		private ComboDialogField[] fComboDialogFields = new ComboDialogField[2];
		private static Button[] radioBtns = new Button[2];
		int selectIndex = 0;
		String boardName = "";
		Board boardSelected;
		Cpu selectCpu;
		
		private String[] BoardDetailsComboLabels = {
				"Architecture","Family"
		};
		
		private String[] BoardDetailsTextLabels = {
				"CPU Name","External clock(Hz)","Iboot size"
		};
		
		private String[] Architectures = {
				"armv4","armv5","armv6","armv7","armv7-m","armv7e-m"
		};
		
		private String[] Families = {
				"cortex-m0","cortex-m3","cortex-m4","cortex-m7"
		};
		
		Composite detailsCpt,selectBaordCpt,baordDescCpt,createBaordCpt,MCUCpt;
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
			String eclipsePath = fullPath.substring(6,fullPath.length()-8);
			System.out.println("eclipsePath:  "+eclipsePath);
			return eclipsePath;
		}
		
		private  Listener cpuModifyListener = e -> {
			if(MCUNameField.getText().trim() != null) {
				Cpu defaultCpu = null;
				String cpuXmlPath = getEclipsePath()+"demo\\cpu.xml";
				ReadCpuByJDom rcb = new ReadCpuByJDom();
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
						System.out.println("The Board:  "+board);
					}
				}
				if(toCreat) {
					importMCUBtn.setEnabled(true);
					for(int i=1;i<fDialogFields.length;i++) {
						fDialogFields[i].getTextControl(detailsCpt).setEnabled(true);
					}
				}else {
					importMCUBtn.setEnabled(false);
					for(int i=1;i<fDialogFields.length;i++) {
						fDialogFields[i].getTextControl(detailsCpt).setEnabled(false);
					}
				}
			}
		};
		
		@Override
		protected void okPressed() {
			// TODO Auto-generated method stub
			String cpuXmlPath = getEclipsePath()+"demo\\cpu.xml";
			List<Cpu> cpus = new ArrayList<Cpu>();
			ReadCpuByJDom rcb = new ReadCpuByJDom();
			Cpu defaultCpu = null;
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
					System.out.println("The Board:  "+board);
				}
			}
			if(toCreat) {
				boardSelected.setExClk(fDialogFields[1].getTextControl(detailsCpt).getText());
				boardSelected.setIbootSize(fDialogFields[2].getTextControl(detailsCpt).getText());
				if(selectCpu == null) {
					selectCpu = defaultCpu;
				}
			}else {
				selectCpu = defaultCpu;
				boardSelected.setExClk(fDialogFields[1].getTextControl(detailsCpt).getText());
				boardSelected.setIbootSize(fDialogFields[2].getTextControl(detailsCpt).getText());
				System.out.println("fDialogFields[2].getTextControl(detailsCpt).getText(): "+fDialogFields[2].getTextControl(detailsCpt).getText());
			}

			if(selectCpu==null) {
				System.out.println("Default = dialog.getSelectCpu();");
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
		protected Control createDialogArea(Composite parent) {
			// TODO Auto-generated method stub
			CompilerImportBoardAdapter adapter = new CompilerImportBoardAdapter();
			Composite composite = (Composite) super.createDialogArea(parent);
			GridLayout layout = new GridLayout();
			layout.marginHeight = 25;
			layout.numColumns = 3;
			layout.marginLeft=5;
			selectBaordCpt = new Composite(composite, SWT.NONE);
			selectBaordCpt.setLayout(layout);
			selectBaordCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
			radioBtns[0] = new Button(selectBaordCpt, SWT.RADIO | SWT.LEFT);
			radioBtns[0].setText("Select board: ");
			radioBtns[0].setToolTipText("Select board");
			boardSelectField = new Text(selectBaordCpt, SWT.BORDER);
			boardSelectField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//			boardSelectField.setEnabled(false);
			Button importOrNewBtn = new Button(selectBaordCpt, SWT.PUSH);
			importOrNewBtn.setText(" Select...  ");
//			importOrNewBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			importOrNewBtn.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					handleImportButtonPressed();
				}
			});
			boardSelectField.addListener(SWT.Modify, boardModifyListener);
		
//			boardSelectField = new StringDialogField();
//			boardSelectField.setLabelText("Select board: ");
//			boardSelectField.getLabelControl(selectBaordCpt)
//					.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
//			boardSelectField.getTextControl(selectBaordCpt)
//					.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//			boardSelectField.setDialogFieldListener(adapter);
//			boardSelectField.getTextControl(selectBaordCpt).setEnabled(false);
			
//			layout.marginHeight = 10;
//			layout.numColumns = 3;
//			layout.marginLeft=5;
//			createBaordCpt = new Composite(composite, SWT.NONE);
//			createBaordCpt.setLayout(layout);
//			createBaordCpt.setLayoutData(new GridData(GridData.FILL_BOTH));
//			radioBtns[1] = new Button(createBaordCpt, SWT.RADIO | SWT.LEFT);
//			radioBtns[1].setText("Create board:");
//			radioBtns[1].setToolTipText("Create board");
//			boardCreateField = new Text(createBaordCpt, SWT.BORDER);
//			boardCreateField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//			boardCreateField.setEnabled(false);
//			boardCreateField = new StringDialogField();
//			boardCreateField.setLabelText("Create board:");		
//			boardCreateField.getLabelControl(createBaordCpt)
//					.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
//			boardCreateField.getTextControl(createBaordCpt).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//			boardCreateField.getTextControl(createBaordCpt).setToolTipText("填写此处后将自动设置为新建Board");

			MCUCpt = new Composite(composite, SWT.NONE);
			layout.marginHeight = 15;
			layout.numColumns = 3;
			layout.marginLeft=10;
			MCUCpt.setLayout(layout);
			MCUCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			Label MCULabel = new Label(MCUCpt, SWT.NONE);
			MCULabel.setLayoutData(new GridData(GridData.BEGINNING));
			MCULabel.setText(BoardDetailsTextLabels[0]+":         ");
			MCUNameField = new Text(MCUCpt, SWT.BORDER);
			MCUNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			MCUNameField.setEnabled(false);
			importMCUBtn = new Button(MCUCpt, SWT.PUSH);
			importMCUBtn.setText("Choose...");
			importMCUBtn.setEnabled(false);
			importMCUBtn.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					handleChooseButtonPressed();
				}

			});
			MCUNameField.addListener(SWT.Modify, cpuModifyListener);
					
			detailsCpt = new Composite(composite, SWT.NONE);
			layout.marginHeight = 5;
			layout.numColumns = 4;
			layout.marginLeft=10;
			layout.verticalSpacing = 20;
			detailsCpt.setLayout(layout);
			detailsCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			for(int i=1;i<fDialogFields.length;i++) {
				fDialogFields[i] = new StringDialogField();
				fDialogFields[i].setLabelText(BoardDetailsTextLabels[i]+":");
				fDialogFields[i].getLabelControl(detailsCpt)
						.setLayoutData(new GridData(GridData.BEGINNING));
				fDialogFields[i].getTextControl(detailsCpt)
						.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				fDialogFields[i].getTextControl(detailsCpt).setEnabled(false);
			}
			
//			for(int i=0;i<fComboDialogFields.length;i++) {
//				fComboDialogFields[i] = new ComboDialogField(SWT.READ_ONLY | SWT.DROP_DOWN);
//				fComboDialogFields[i].setLabelText(BoardDetailsComboLabels[i]+":");
//				fComboDialogFields[i].getLabelControl(detailsCpt)
//						.setLayoutData(new GridData(GridData.BEGINNING));
//				fComboDialogFields[i].getComboControl(detailsCpt)
//						.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//			}
//			fComboDialogFields[0].setItems(Architectures);
//			fComboDialogFields[0].selectItem(0);
//			fComboDialogFields[1].setItems(Families);
//			fComboDialogFields[1].selectItem(0);
			
//			radioBtns[0].addSelectionListener(new SelectionAdapter() {
//				@Override
//				public void widgetSelected(SelectionEvent e) {
//					if(radioBtns[0].getSelection()) {
//						radioBtns[1].setSelection(false);
//						boardCreateField.setEnabled(false);
//						importMCUBtn.setEnabled(false);
//					
//						fDialogFields[1].getTextControl(detailsCpt).setEnabled(false);
//						fDialogFields[2].getTextControl(detailsCpt).setEnabled(false);
//						fComboDialogFields[0].getComboControl(detailsCpt).setEnabled(false);
//						fComboDialogFields[1].getComboControl(detailsCpt).setEnabled(false);
//
//						importOrNewBtn.setEnabled(true);
//						selectIndex = 0;
//					}
//				}
//
//			});
//
//			radioBtns[1].addSelectionListener(new SelectionAdapter() {
//				@Override
//				public void widgetSelected(SelectionEvent e) {
//					if(radioBtns[1].getSelection()) {
//						radioBtns[0].setSelection(false);
//						boardCreateField.setEnabled(true);
//						importMCUBtn.setEnabled(true);
//						fDialogFields[1].getTextControl(detailsCpt).setEnabled(true);
//						fDialogFields[2].getTextControl(detailsCpt).setEnabled(true);
//						fComboDialogFields[0].getComboControl(detailsCpt).setEnabled(true);
//						fComboDialogFields[1].getComboControl(detailsCpt).setEnabled(true);
//						importOrNewBtn.setEnabled(false);
//						selectIndex = 1;
//					}
//				}
//
//			});
			
			//sc.setItems(WEEK); String[] WEEK = { "Monday", "Tuesday", "Wednesday"};
			
//			detailsCpt = new Composite(composite, SWT.NONE);
//			layout.marginHeight = 15;
//			layout.numColumns = 4;
//			layout.marginLeft=10;
//			layout.verticalSpacing = 20;
//			detailsCpt.setLayout(layout);
//			detailsCpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//			
//			for(int i=0;i<fDialogFields.length;i++) {
//				fDialogFields[i] = new StringDialogField();
//				fDialogFields[i].setLabelText(BoardDetailsLabels[i]+":");
//				fDialogFields[i].getLabelControl(detailsCpt)
//						.setLayoutData(new GridData(GridData.BEGINNING));
//				fDialogFields[i].getTextControl(detailsCpt)
//						.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//				if(i%2==0) {
//					fDialogFields[i].getTextControl(detailsCpt).setEnabled(false);
//				}
//			}
			
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
			
			return super.createDialogArea(parent);
		}
		
		protected void handleChooseButtonPressed() {
	
			ChooseMCUDialog dialog = new ChooseMCUDialog(getShell());
			if (dialog.open() == Window.OK) {
				selectCpu = dialog.getSelectCpu();
				if(selectCpu==null) {
    				System.out.println("selectCpu = dialog.getSelectCpu();");
    			}
				if(selectCpu == null) {
					System.out.println("selectCpu == null");
				}
				MCUNameField.setText(selectCpu.getDevice());
//				fComboDialogFields[0].setText(selectCpu.getArchitecture());
//				fComboDialogFields[1].setText(selectCpu.getCore());
				boardSelected = new Board();
				boardSelected.setCpu(selectCpu);
//				boardDetailsDesc.setText("Device: "+selectCpu.getDevice()+"			Core: "+selectCpu.getCore()+"\n"+
//						"Architecture: "+selectCpu.getArchitecture()+"		FpuType: "+selectCpu.getFpuType()+"\n"+
//						"Flash Start: "+selectCpu.getFlashStart()+"		Flash Size: "+selectCpu.getFlashSize()+"\n"+
//						"Ram Start: "+selectCpu.getRamStart()+"		Ram Size: "+selectCpu.getRamSize()
//						);
				// BoardDetails details = dialog.getResult();
				// fBoardNameField.setText(details.boardName);
			}
		}
	
		/**
		 * The browse button has been selected. Select the location.
		 */
		protected void handleImportButtonPressed() {
	
			String dirName = getEclipsePath()+"djysrc\\bsp\\boarddrv";
			FileDialog dialog = new FileDialog(selectBaordCpt.getShell(), SWT.OPEN | SWT.MULTI);
			dialog.setText("Choose Board");
			dialog.setFilterPath(dirName);
			String selectedDirectory = dialog.open();
	
			if (selectedDirectory != null) {
				String boardName = selectedDirectory.substring(selectedDirectory.lastIndexOf("\\") + 1,
						selectedDirectory.lastIndexOf("."));
				boardSelectField.setText(boardName);
			}
	
			ReadBoardByDom rbbd = new ReadBoardByDom();
			try {
				boardSelected = rbbd.getBoard(selectedDirectory);
				MCUNameField.setText(boardSelected.cpu.getDevice());
				selectCpu = boardSelected.cpu;
				fDialogFields[1].getTextControl(detailsCpt).setText(boardSelected.getExClk());
				fDialogFields[2].getTextControl(detailsCpt).setText(boardSelected.getIbootSize());
//				fComboDialogFields[0].getComboControl(detailsCpt).setText(boardSelected.cpu.getArchitecture());
//				fComboDialogFields[1].getComboControl(detailsCpt).setText(boardSelected.cpu.getCore());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
		}

		@Override
		protected void configureShell(Shell shell) {
			// TODO Auto-generated method stub
			super.configureShell(shell);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, ICHelpContextIds.TODO_TASK_INPUT_DIALOG);
		} 
}
