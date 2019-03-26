package com.djyos.dide.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;

public class HandleDjyosAction implements IWorkbenchWindowPulldownDelegate2 {

	@Override
	public Menu getMenu(Control parent) {
		// TODO Auto-generated method stub
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
		if (menu == null)
			return;
		MenuItem[] items = menu.getItems();
		for (MenuItem item2 : items)
			item2.dispose();

		MenuItem mi = new MenuItem(menu, 0);
		mi.setText("°²×°STÇý¶¯");
	}
}
