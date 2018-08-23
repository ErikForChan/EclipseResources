package com.djyos.dide.ui.djyproperties;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.PropertyPage;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.djyos.dide.ui.djyproperties.Module;

public class ModuleConfigurationPage extends PropertyPage{

	private List<Module> rootModules = new ArrayList<Module>();
	private List<Module> childModules = new ArrayList<Module>();
	private List<Module> allModules = new ArrayList<Module>();
	private TreeItem[] rootItems = null;
	private TreeItem[] childItems = null;
	private TreeItem[] allItems = null;
	int childCount = 0;
	public String moduleInit;
	
	public ModuleConfigurationPage() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	private IProject getProject(){
		Object	element	= getElement();
		IProject project	= null;
		
		if (element instanceof IProject) {
			project = (IProject) element;
		} else if (element instanceof IAdaptable) {
			project= ((IAdaptable)element).getAdapter(IProject.class);
		}
		return project;
	}
	
	private void getModules() {
		IProject project = getProject();
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

	public void reviseModuleCfg() {
		IProject project = getProject();
		String filePath = project.getLocation().toString() + "/data/ModuleCfg.xml";
		DocumentBuilder db = null;
		Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
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
		for (int i = 0; i < moduleList.getLength(); i++) {
			org.w3c.dom.Node node = moduleList.item(i);
			NamedNodeMap namedNodeMap = node.getAttributes();
			String name = namedNodeMap.getNamedItem("name").getTextContent();
			String checked = node.getTextContent();
			boolean isChecked = checked.equals("true");
			if (allItems[i].getChecked() && !isChecked) {
				node.setTextContent("true");
			}
			if (!allItems[i].getChecked() && isChecked) {
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
	
	public void reviseModuleInit() {
		IProject project = getProject();
		String initPath = project.getLocation().toString() + "/src/app/OS_prjcfg/cfg/moduleinit.h";
		String initPathIboot = project.getLocation().toString()+"/src/iboot/OS_prjcfg/cfg/moduleinit.h";
		File file_app = new File(initPath);
		File file_iboot = new File(initPathIboot);
		if(file_app.exists()) {
			file_app.delete();
			try {
				file_app.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fillModuleinit(initPath);
		}
		if(file_iboot.exists()) {
			file_iboot.delete();
			try {
				file_iboot.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fillModuleinit(initPathIboot);
		}
		
	}
	
	public void fillModuleinit(String path) {
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
	
	}
	
	private void fillInit(TreeItem curItem, Module childModule,int depth) {
		if (childModule.getHaveChild().equals("yes")) {
			depth++;
			String moduleName = childModule.getName();
			System.out.println("(allModules.size(): "+allModules.size());
			System.out.println("(allItems.length: "+allItems.length);
			for (int j = 0; j < allModules.size(); j++) {
				if(! allModules.get(j).getParent().equals("root")) {
					Module module = allModules.get(j);
					if (!module.isVisited()) {
						String parentName = module.getParent();
						if (parentName.equals(moduleName)) {
							System.out.println("(allItems[j]: "+j);
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
	
	@Override
	protected void performApply() {
		// TODO Auto-generated method stub
		super.performApply();
	}

	@Override
	protected void performDefaults() {
		// TODO Auto-generated method stub
		for(int i=0;i<rootModules.size();i++) {		
			Module childModule = rootModules.get(i);
			String moduleName = childModule.getName();
			if(childModule.isChecked()) {
				rootItems[i].setChecked(true);
			}else {
				rootItems[i].setChecked(false);
			}
		}	
		for(int i=0;i<childModules.size();i++) {		
			Module childModule = childModules.get(i);
			String moduleName = childModule.getName();
			if(childModule.isChecked()) {
				childItems[i].setChecked(true);	
			}else {
				childItems[i].setChecked(false);
			}
		}	
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		// TODO Auto-generated method stub
		reviseModuleCfg();
		reviseModuleInit();
		return super.performOk();
	}

	@Override
	protected Control createContents(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2,true));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		IProject project = getProject();
		Tree tree1 = new Tree(composite,SWT.CHECK);
		tree1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
		Tree tree2 = new Tree(composite,SWT.CHECK);
		tree2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
		setTitle("Module Configuration ("+project.getName()+")");
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
				childCount++;
				TreeItem curItem = allItems[i];
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
		return composite;
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
							System.out.println("childCount  "+childCount);
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
	
}
