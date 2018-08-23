package com.djyos.dide.ui.wizards.djyosProject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.djyos.dide.ui.djyproperties.Module;
import com.djyos.dide.ui.wizards.djyosProject.DjyosCommonProjectWizard;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;

public class ModuleConfigurationWizard extends WizardPage{

	private static final String EXTENSION_POINT_ID = "org.eclipse.cdt.ui.CDTWizard"; //$NON-NLS-1$
	private static final String ELEMENT_NAME = "wizard"; //$NON-NLS-1$
	private static final String CLASS_NAME = "class"; //$NON-NLS-1$
	public static final String DESC = "EntryDescriptor"; //$NON-NLS-1$
	public boolean moduleCompleted = false;
	private List<Module> rootModules = new ArrayList<Module>();
	private List<Module> childModules = new ArrayList<Module>();
	private List<Module> allModules = new ArrayList<Module>();
	private TreeItem[] rootItems = null;
	private TreeItem[] childItems = null;
	private TreeItem[] allItems = null;
	int childCount = 0;
	public String moduleInit;
	/**
	 * Creates a new project creation wizard page.
	 *
	 * @param pageName the name of this page
	 */
	public ModuleConfigurationWizard(String pageName) {
		super(pageName);
		setPageComplete(true);
	}
	
	//moduleinit.h
	public void fillModuleinit(String path) {
		File file = new File(path);
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		moduleInit = "";
		moduleInit+="#ifndef __MODULEINIT_H__\r\n" + 
				"#define __MODULEINIT_H__\r\n\n"+
				"#include \"stdint.h\"\n\n";
		for (int i = 0; i < allModules.size(); i++) {
			if(! allModules.get(i).getParent().equals("root")) {
				allModules.get(i).setVisited(false);
			}		
		}
		childCount = 0;
		for(int i = 0;i<allModules.size();i++) {
			boolean checked = false;
			Module childModule = allModules.get(i);
			if(childModule.getParent().equals("root")) {
				if(allItems[i].getChecked()) {
					moduleInit+="#define MODULEINIT_"+allModules.get(i).getName()+"\n";
					checked = true;
				}
				childCount++;
				TreeItem curItem = allItems[i];
				fillInit(curItem,childModule,0);
				if(checked) {
					moduleInit+="\n";
				}	
			}			
		}
		moduleInit+="#endif";
		
		FileWriter writer;
		try {
			writer = new FileWriter(path);
			writer.write(moduleInit);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		reviseModuleCfg();
				
	}
	
	public void reviseModuleCfg() {
		DjyosCommonProjectWizard nmWizard = (DjyosCommonProjectWizard)getWizard();
		IProject project = nmWizard.getProject();
		String filePath = project.getLocation().toString()+"/data/ModuleCfg.xml";
	    DocumentBuilder db = null;  
	    Document document = null;
		DocumentBuilderFactory factory =  DocumentBuilderFactory.newInstance(); 
		File file = new File(filePath);
		factory.setIgnoringElementContentWhitespace(true);
		try {
			db = factory.newDocumentBuilder();
			try {
				document = db.parse(file);
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		NodeList moduleList = document.getElementsByTagName("module");
	    for(int i=0;i<moduleList.getLength();i++) {
	    	org.w3c.dom.Node node = moduleList.item(i);
			NamedNodeMap namedNodeMap = node.getAttributes();
			String name = namedNodeMap.getNamedItem("name").getTextContent();
			String checked = node.getTextContent();
			boolean isChecked = checked.equals("true");
			if(allItems[i].getChecked() && !isChecked) {
				node.setTextContent("true");
			}
			if(!allItems[i].getChecked() && isChecked) {
				node.setTextContent("false");
			}
	    }
	    
	    TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer former;
		try {
			former = tFactory.newTransformer();
			try {
				former.transform(new DOMSource(document), new StreamResult(file));
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	private void getModules() {
		DjyosCommonProjectWizard nmWizard = (DjyosCommonProjectWizard)getWizard();
		IProject project = nmWizard.getProject();
		DocumentBuilderFactory dbFactory = null;  
	    DocumentBuilder db = null;  
	    Document document = null;
	    String filePath = project.getLocation().toString()+"/data/ModuleCfg.xml";
	    try {  
            dbFactory = DocumentBuilderFactory.newInstance();  
            db = dbFactory.newDocumentBuilder();
            try {
				document = db.parse(filePath);
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } catch (ParserConfigurationException e) {  
            e.printStackTrace();  
        }
	    NodeList moduleList = document.getElementsByTagName("module");
	    for(int i=0;i<moduleList.getLength();i++) {
			org.w3c.dom.Node node = moduleList.item(i);
			NamedNodeMap namedNodeMap = node.getAttributes();
			String name = namedNodeMap.getNamedItem("name").getTextContent();
			String parent = namedNodeMap.getNamedItem("parent").getTextContent();
			String haveChild= namedNodeMap.getNamedItem("haveChild").getTextContent();
			String checked = node.getTextContent();
			boolean isChecked = checked.equals("true");
			Module module = new Module(name,parent,haveChild,isChecked,false);
			allModules.add(module);
			if(parent.equals("root")) {
				rootModules.add(module);				
			}else {
				childModules.add(module);
			}
	    }
	    allItems = new TreeItem[allModules.size()];
	    rootItems = new TreeItem[rootModules.size()];
	    childItems = new TreeItem[childModules.size()];
	}
	
	@SuppressWarnings("restriction")
	@Override
	public void createControl(Composite parent) {
		GridLayout gd = new GridLayout();
		gd.marginHeight=10;
		Composite composite = new Composite(parent, SWT.NONE);
		initializeDialogUnits(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);
		composite.setLayout(new GridLayout(2,true));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		Tree tree1 = new Tree(composite,SWT.CHECK);
		tree1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
		Tree tree2 = new Tree(composite,SWT.CHECK);
		tree2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
		getModules();
		for(int i=0;i<allModules.size();i++) {		
			Module childModule = allModules.get(i);
			if(childModule.getParent().equals("root")) {
				String moduleName = childModule.getName();
				if(i<rootModules.size()/2) {
					allItems[i] = new TreeItem(tree1, SWT.NONE);
				}else {
					allItems[i] = new TreeItem(tree2, SWT.NONE);
				}		
				allItems[i].setText(moduleName);
				if(childModule.isChecked()) {
					allItems[i].setChecked(true);
				}
				TreeItem curItem = allItems[i];
				childCount++;
				fillContents(curItem,childModule);
			}			
		}
		tree1.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				TreeItem item = (TreeItem) event.item;
				if (item == null) {
					return;
				}
				List authorities = new ArrayList<Object>();
				checkItem(item, authorities);	
			}
		});
		tree2.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				TreeItem item = (TreeItem) event.item;
				if (item == null) {
					return;
				}
				List authorities = new ArrayList<Object>();
				checkItem(item, authorities);	
			}
		});
		
//		// Show description on opening
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
		Dialog.applyDialogFont(composite);

	}
	
	private void fillInit(TreeItem curItem, Module childModule,int depth) {
		if (childModule.getHaveChild().equals("yes")) {
			depth++;
			String moduleName = childModule.getName();
			for (int j = 0; j < allModules.size(); j++) {
				if(! allModules.get(j).getParent().equals("root")) {
					Module module = allModules.get(j);
					if (!module.isVisited()) {
						String parentName = module.getParent();
						if (parentName.equals(moduleName)) {
							if(allItems[j].getChecked()) {
								for(int i=0;i<depth;i++) {
									moduleInit+="\t";
								}
								moduleInit+="#define MODULEINIT_"+module.getName()+"\n";
							}
							childCount++;
							allModules.get(j).setVisited(true);
							fillInit(allItems[childCount-1],module,depth);
						}
					}
				}			
			}
		}
	}
	
	private void fillContents(TreeItem curItem, Module childModule) {
		if (childModule.getHaveChild().equals("yes")) {
			String moduleName = childModule.getName();
			for (int j = 0; j < allModules.size(); j++) {
				if(! allModules.get(j).getParent().equals("root")) {
					Module module = allModules.get(j);
					if (!module.isVisited()) {
						String parentName = module.getParent();
						if (parentName.equals(moduleName)) {
							allItems[childCount] = new TreeItem(curItem, SWT.NONE);
							allItems[childCount].setText(module.getName());
							if(childModule.isChecked()) {
								allItems[childCount].setChecked(true);
							}
							childCount++;
							allModules.get(j).setVisited(true);
							fillContents(allItems[childCount-1], module);
						}
					}
				}			
			}
		}
	}
	
	private void checkItem(TreeItem item, List<Object> authorities) {
		checkItem(item, authorities, false, false);
	}

	private void checkItem(TreeItem item, List<Object> allCheckedElements, boolean isParent,
			boolean isChild) {
		// 此处树已经构建完成，所以在判断是父节点还是子节点是直接根据树的物理结构判断，而不需要再考虑逻辑
		if (!isChild) {
			if (item.getItemCount() != 0)
				isParent = true;
		}

		if (item.getParentItem() != null) {
			isChild = true;
		} else {
			isChild = false;
		}

		if (isParent) { // 是父节点时应该选中该选中该节点时级联选中其下的所有子节点，取消选中时同理
			TreeItem[] children = item.getItems();
			Object o = item.getData();
			if (item.getChecked()) { // 选中该父节点时，它所有的子节点也要被选中
				if (!allCheckedElements.contains(o))
					allCheckedElements.add(o);// 将选中的节点放入到allCheckedElements中
				for (int i = 0; i < children.length; i++) {
					children[i].setChecked(true);
					checkItem(children[i], allCheckedElements);
				}
			} else {// 取消选中时它的所有子节点也同时被取消选中
				if (allCheckedElements.contains(o))
					allCheckedElements.remove(o);// 如果选中的节点之前在allCheckedElements中，则移除
				for (int i = 0; i < children.length; i++) {
					children[i].setChecked(false);
					checkItem(children[i], allCheckedElements);
				}
			}
		}
	}

	@Override
	public IWizardPage getNextPage() {
		// TODO Auto-generated method stub
		System.out.println("getNextPage MC");
		return super.getNextPage();
	}
}