package com.djyos.dide.debug.gdbjtag.csky.ui;

import com.djyos.dide.debug.gdbjtag.csky.Activator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class ButtonRestartActionDelegate implements IWorkbenchWindowActionDelegate {

	@Override
	public void run(IAction action) {
		if (Activator.getInstance().isDebugging()) {
			System.out.println("Restart.run(" + action + ")");
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (Activator.getInstance().isDebugging()) {
			System.out.println("Restart.selectionChanged(" + action + "," + selection + ")");
		}
	}

	@Override
	public void dispose() {
		if (Activator.getInstance().isDebugging()) {
			System.out.println("Restart.dispose()");
		}
	}

	@Override
	public void init(IWorkbenchWindow window) {
		if (Activator.getInstance().isDebugging()) {
			System.out.println("Restart.init()");
		}
	}

}
