/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.xlc;

import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;

public class XlcCScannerExtensionConfiguration extends GCCScannerExtensionConfiguration {

	private static XlcCScannerExtensionConfiguration instance;
	
	
	private XlcCScannerExtensionConfiguration() {}
	
	public static synchronized XlcCScannerExtensionConfiguration getInstance() {
		if(instance == null)
			instance = new XlcCScannerExtensionConfiguration();
		return instance;
	}

	@Override
	public boolean supportUTFLiterals() {
		return true;
	}

	@Override
	public char[] supportAdditionalNumericLiteralSuffixes() {
        return "dflij".toCharArray(); //$NON-NLS-1$
    }
}
