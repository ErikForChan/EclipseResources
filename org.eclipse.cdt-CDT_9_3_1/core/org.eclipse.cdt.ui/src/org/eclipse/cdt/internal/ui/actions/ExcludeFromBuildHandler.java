/*******************************************************************************
 * Copyright (c) 2007, 2015 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     Nokia - converted from action to handler
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.newui.AbstractPage;

import org.eclipse.cdt.internal.ui.newui.Messages;

/**
 * Handler for command that excludes resources from build.
 */
public class ExcludeFromBuildHandler extends AbstractHandler {

	protected ArrayList<IResource> objects;
	protected ArrayList<String> cfgNames;

	@Override
	public void setEnabled(Object context) {
		ISelection selection = getSelection(context);
		setEnabledFromSelection(selection);
	}

	protected ISelection getSelection(Object context) {
		Object s = HandlerUtil.getVariable(context, ISources.ACTIVE_MENU_SELECTION_NAME);
		if (s instanceof ISelection) {
			return (ISelection) s;
		}
		return null;
	}

	public void setEnabledFromSelection(ISelection selection) {
		objects = null;
		cfgNames = null;
		boolean cfgsOK = true;

		if ((selection != null) && !selection.isEmpty()) {
			// case for context menu
			Object[] obs = null;
			if (selection instanceof IStructuredSelection) {
				obs = ((IStructuredSelection) selection).toArray();
			} else if (selection instanceof ITextSelection) {
				IFile file = getFileFromActiveEditor();
				if (file != null)
					obs = Collections.singletonList(file).toArray();
			}
			if (obs != null && obs.length > 0) {
				for (int i = 0; i < obs.length && cfgsOK; i++) {
					// if project selected, don't do anything
					if ((obs[i] instanceof IProject) || (obs[i] instanceof ICProject)) {
						cfgsOK = false;
						break;
					}
					IResource res = null;
					// only folders and files may be affected by this action
					if (obs[i] instanceof ICContainer || obs[i] instanceof ITranslationUnit) {
						res = ((ICElement) obs[i]).getResource();
					} else if (obs[i] instanceof IResource) {
						// project's configuration cannot be deleted
						res = (IResource) obs[i];
					}
					if (res != null) {
						ICConfigurationDescription[] cfgds = getCfgsRead(res);
						if (cfgds == null || cfgds.length == 0)
							continue;

						if (objects == null)
							objects = new ArrayList<IResource>();
						objects.add(res);
						if (cfgNames == null) {
							cfgNames = new ArrayList<String>(cfgds.length);
							for (int j = 0; j < cfgds.length; j++) {
								if (!canExclude(res, cfgds[j])) {
									cfgNames = null;
									cfgsOK = false;
									break;
								}
								cfgNames.add(cfgds[j].getName());
							}
						} else {
							if (cfgNames.size() != cfgds.length) {
								cfgsOK = false;
							} else {
								for (int j = 0; j < cfgds.length; j++) {
									if (!canExclude(res, cfgds[j])
											|| !cfgNames.contains(cfgds[j].getName())) {
										cfgsOK = false;
										break;
									}
								}
							}
						}
					}
				}
			}
		}
		setBaseEnabled(cfgsOK && (objects != null));
	}

	private IFile getFileFromActiveEditor() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				IEditorPart editor = page.getActiveEditor();
				if (editor != null) {
					IEditorInput input = editor.getEditorInput();
					if (input != null)
						return input.getAdapter(IFile.class);
				}
			}
		}
		return null;
	}

	private void addIncludeFile(File file, String content) {
		// 创建文件工厂实例
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setIgnoringElementContentWhitespace(false);

		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			// 创建Document对象
			Document xmldoc = db.parse(file);
			List<String> componentPaths = new ArrayList<String>();

			NodeList componentList = xmldoc.getElementsByTagName("file");
			for (int i = 0; i < componentList.getLength(); i++) {
				String componentPath = componentList.item(i).getFirstChild().getTextContent();
				componentPaths.add(componentPath);
			}

			if (!componentPaths.contains(content)) {
				Element root = xmldoc.getDocumentElement();
				Element compt = xmldoc.createElement("file");
				compt.setTextContent(content);
				root.appendChild(compt);
				root.appendChild(compt);
				// 保存
				TransformerFactory factory = TransformerFactory.newInstance();
				Transformer former = factory.newTransformer();
				former.transform(new DOMSource(xmldoc), new StreamResult(file));
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private void deleteIncFile(File file, String content) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setIgnoringElementContentWhitespace(true);

		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			// 创建Document对象
			Document xmldoc = db.parse(file);

			Element root = xmldoc.getDocumentElement();
			NodeList componentList = xmldoc.getElementsByTagName("file");
			for (int i = 0; i < componentList.getLength(); i++) {
				String componentPath = componentList.item(i).getFirstChild().getTextContent();
				if (componentPath.equals(content)) {
					root.removeChild(componentList.item(i));
					break;
				}
			}
			// 保存
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer former = factory.newTransformer();
			former.transform(new DOMSource(xmldoc), new StreamResult(file));

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private boolean canExclude(IResource res, ICConfigurationDescription cfg) {
		IPath p = res.getFullPath();
		ICSourceEntry[] ent = cfg.getSourceEntries();
		boolean state = CDataUtil.isExcluded(p, ent);
		return CDataUtil.canExclude(p, (res instanceof IFolder), !state, ent);
	}

	private void setExclude(IResource res, ICConfigurationDescription cfg, boolean exclude) {

		String srcLocation = new File(System.getProperty("user.dir")).getParentFile().getPath().replace("\\",
				"/") + "/djysrc";
		IProject project = res.getProject();

		File diskFile = new File(project.getLocation().toString() + "/data/user_handled_files.xml");
		String resRelativePath = res.getLocation().toString().replace(srcLocation, "");
		if (!exclude) {
			if (!diskFile.exists()) {
				try {
					diskFile.createNewFile();
					createIncludeFile(diskFile, resRelativePath);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				System.out.println(
						"res.getLocation().toString().replace(srcLocation, \"\"):  " + resRelativePath);
				addIncludeFile(diskFile, resRelativePath);
			}
			// IFile userHandledFile = project.getFile("data/user_handled_files.xml");
		} else {
			deleteIncFile(diskFile, resRelativePath);
		}

		try {
			// System.out.println("setExclude2: " + res.getFullPath() + " " + (res
			// instanceof IFolder));
			ICSourceEntry[] newEntries = CDataUtil.setExcluded(res.getFullPath(), (res instanceof IFolder),
					exclude, cfg.getSourceEntries());
			cfg.setSourceEntries(newEntries);
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
	}

	private void createIncludeFile(File diskFile, String resRelativePath) {
		// TODO Auto-generated method stub
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			factory.setIgnoringElementContentWhitespace(false);
			builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
			Element rootElement = document.createElement("IncludeFiles");

			Element compt = document.createElement("file");
			compt.setTextContent(resRelativePath);
			rootElement.appendChild(compt);

			document.appendChild(rootElement);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");// 增加换行缩进，但此时缩进默认为0
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");// 设置缩进为2
			transformer.setOutputProperty("encoding", "UTF-8");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(writer));
			transformer.transform(new DOMSource(document), new StreamResult(diskFile));
			writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		openDialog();
		return null;
	}

	private ICConfigurationDescription[] getCfgsRead(IResource res) {
		IProject p = res.getProject();
		if (!p.isOpen())
			return null;
		if (!CoreModel.getDefault().isNewStyleProject(p))
			return null;
		ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(p, false);
		if (prjd == null)
			return null;
		return prjd.getConfigurations();
	}

	private void openDialog() {
		if (objects == null || objects.size() == 0)
			return;
		// create list of configurations to delete

		ListSelectionDialog dialog = new ListSelectionDialog(CUIPlugin.getActiveWorkbenchShell(), cfgNames,
				createSelectionDialogContentProvider(), new LabelProvider() {
				}, ActionMessages.ExcludeFromBuildAction_0);
		dialog.setTitle(ActionMessages.ExcludeFromBuildAction_1);

		boolean[] status = new boolean[cfgNames.size()];
		Iterator<IResource> it = objects.iterator();
		while (it.hasNext()) {
			IResource res = it.next();
			ICConfigurationDescription[] cfgds = getCfgsRead(res);
			IPath p = res.getFullPath();
			for (int i = 0; i < cfgds.length; i++) {
				boolean b = CDataUtil.isExcluded(p, cfgds[i].getSourceEntries());
				if (b)
					status[i] = true;
			}
		}
		ArrayList<String> lst = new ArrayList<String>();
		for (int i = 0; i < status.length; i++)
			if (status[i])
				lst.add(cfgNames.get(i));
		if (lst.size() > 0)
			dialog.setInitialElementSelections(lst);

		if (dialog.open() == Window.OK) {
			Object[] selected = dialog.getResult(); // may be empty
			Iterator<IResource> it2 = objects.iterator();
			while (it2.hasNext()) {
				IResource res = it2.next();
				IProject p = res.getProject();
				if (!p.isOpen())
					continue;
				// get writable description
				ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(p, true);
				if (prjd == null)
					continue;
				ICConfigurationDescription[] cfgds = prjd.getConfigurations();
				for (int i = 0; i < cfgds.length; i++) {
					boolean exclude = false;
					for (int j = 0; j < selected.length; j++) {
						if (cfgds[i].getName().equals(selected[j])) {
							exclude = true;
							break;
						}
					}
					setExclude(res, cfgds[i], exclude);
				}
				try {
					CoreModel.getDefault().setProjectDescription(p, prjd);
				} catch (CoreException e) {
					CUIPlugin.logError(Messages.AbstractPage_11 + e.getLocalizedMessage());
				}
				AbstractPage.updateViews(res);
			}
		}
	}

	private IStructuredContentProvider createSelectionDialogContentProvider() {
		return new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {
				return cfgNames.toArray();
			}

			@Override
			public void dispose() {
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		};
	}

}
