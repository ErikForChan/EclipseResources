package com.djyos.dide.ui.actions;

import java.util.HashSet;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.cdt.internal.ui.actions.ActionMessages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;

public class HandleDjyosAction extends AbstractHandler
		implements IWorkbenchWindowPulldownDelegate2, ICProjectDescriptionListener {

	private IAction actionMenuCache;

	@Override
	public Menu getMenu(Control parent) {
		// TODO Auto-generated method stub
		System.out.println("getMenu");
		Menu menu = new Menu(parent);
		addMenuListener(menu);
		return menu;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run(IAction action) {
		// TODO Auto-generated method stub

	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		if (actionMenuCache == null) {
			actionMenuCache = action;
		}
		// onSelectionChanged(action, selection);
		updateBuildConfigMenuToolTip(action);
	}

	@Override
	public Menu getMenu(Menu parent) {
		// TODO Auto-generated method stub
		Menu menu = new Menu(parent);
		addMenuListener(menu);
		return menu;
	}

	private void addMenuListener(Menu menu) {
		menu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(MenuEvent e) {
				fillMenu((Menu) e.widget);
			}
		});
	}

	protected void fillMenu(Menu menu) {
		// TODO Auto-generated method stub
		System.out.println("fillMenu");
		if (menu == null)
			return;
		MenuItem[] items = menu.getItems();
		for (MenuItem item2 : items)
			item2.dispose();

		MenuItem mi = new MenuItem(menu, 0);
		mi.setText("°²×°STÇý¶¯");
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		System.out.println("execute");
		return null;
	}

	@Override
	public void handleEvent(CProjectDescriptionEvent event) {
		// TODO Auto-generated method stub
		// CUIPlugin.getDefault().getPreferenceStore().setValue("clearConsole", true);
		if (actionMenuCache != null) {
			updateBuildConfigMenuToolTip(actionMenuCache);
		}
	}

	public void updateBuildConfigMenuToolTip(IAction action) {
		HashSet<IProject> fProjects = new HashSet<IProject>();
		String toolTipText = ""; //$NON-NLS-1$
		IWorkbenchPage page = CUIPlugin.getActivePage();
		if (page != null) {
			IWorkbenchPart part = page.getActivePart();
			if (part != null) {
				Object o = part.getAdapter(IResource.class);
				if (o != null && o instanceof IResource) {
					fProjects.add(((IResource) o).getProject());
				}
			}
		}
		if (fProjects.size() <= 5) {
			StringBuilder sb = new StringBuilder();
			for (IProject prj : fProjects) {
				if (prj != null) {
					ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(prj, false);
					if (prjd != null) {
						sb.append(NLS.bind(ActionMessages.BuildActiveConfigMenuAction_buildConfigTooltip,
								prjd.getActiveConfiguration().getName(), prj.getName()))
								.append(System.getProperty("line.separator")); //$NON-NLS-1$
					}
				}
			}
			toolTipText = sb.toString().trim();
		}
		if (toolTipText.length() == 0)
			toolTipText = ActionMessages.BuildActiveConfigMenuAction_defaultTooltip;
		action.setToolTipText(toolTipText);
	}
}
