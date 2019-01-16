/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.errorparsers.xlc.tests;

import junit.framework.TestCase;

import org.eclipse.cdt.core.IMarkerGenerator;

public class TestLinkerInfo extends TestCase {
	String err_msg;
	/**
	 * This function tests parseLine function of the
	 * XlcErrorParser class. Informational message generated by
	 * xlc linker is given as 
	 * input for testing.  
	 */
	public void testparseLine()
	{
		XlcErrorParserTester aix = new XlcErrorParserTester();
		aix.parseLine(err_msg);
		assertEquals("", aix.getFileName(0));
		assertEquals(0, aix.getLineNumber(0));
		assertEquals(IMarkerGenerator.SEVERITY_INFO, aix.getSeverity(0));
		assertEquals("Use the -bloadmap or -bnoquiet option to obtain more information.",aix.getMessage(0));
	}
	public TestLinkerInfo( String name)
	{
		super(name);
		err_msg = "ld: 0711-345 Use the -bloadmap or -bnoquiet option to obtain more information.";
	}
}
