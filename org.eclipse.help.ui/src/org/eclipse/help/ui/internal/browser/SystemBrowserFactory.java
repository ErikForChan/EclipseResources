/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.browser;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.browser.IBrowser;
import org.eclipse.help.browser.IBrowserFactory;
import org.eclipse.osgi.service.environment.Constants;
public class SystemBrowserFactory implements IBrowserFactory {
	/**
	 * Constructor.
	 */
	public SystemBrowserFactory() {
		super();
	}

	@Override
	public boolean isAvailable() {
		return Constants.WS_WIN32.equalsIgnoreCase(Platform.getOS());
	}

	@Override
	public IBrowser createBrowser() {
		return new SystemBrowserAdapter();
	}
}
