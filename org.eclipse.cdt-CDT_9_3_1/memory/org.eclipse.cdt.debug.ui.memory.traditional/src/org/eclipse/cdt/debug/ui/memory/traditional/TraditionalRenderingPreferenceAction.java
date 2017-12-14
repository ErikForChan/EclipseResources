/*******************************************************************************
 * Copyright (c) 2006-2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.traditional;


import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class TraditionalRenderingPreferenceAction extends ActionDelegate implements IViewActionDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		IPreferencePage page = new TraditionalRenderingPreferencePage();
		showPreferencePage("org.eclipse.cdt.debug.ui.memory.traditional.TraditionalRenderingPreferencePage", page);	 //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
	}
	
	protected void showPreferencePage(String id, IPreferencePage page) {
		BusyIndicator.showWhile(TraditionalRenderingPlugin.getStandardDisplay(), new Runnable() {
			public void run() {
        		PreferencesUtil.createPreferenceDialogOn(TraditionalRenderingPlugin.getShell(), id, new String[] { id }, null).open();
			}
		});		
	}

}

