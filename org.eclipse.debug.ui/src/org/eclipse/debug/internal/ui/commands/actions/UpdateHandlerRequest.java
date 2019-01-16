/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.commands.actions;

import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.debug.internal.core.commands.DebugCommandRequest;

/**
 * Collects whether a handler is enabled for a set of elements and
 * reports its vote to an action updater collecting results from
 * other handlers.
 *
 * @since 3.3
 *
 */
public class UpdateHandlerRequest extends DebugCommandRequest implements IEnabledStateRequest {

	private boolean fEnabled = false;
	private ActionsUpdater fUpdater;

	public UpdateHandlerRequest(Object[] elements, ActionsUpdater updater) {
		super(elements);
		fUpdater = updater;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor#setResult(boolean)
	 */
	@Override
	public synchronized void setEnabled(boolean result) {
		fEnabled = result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#done()
	 */
	@Override
	public synchronized void done() {
		fUpdater.setEnabled(fEnabled);
	}

}
