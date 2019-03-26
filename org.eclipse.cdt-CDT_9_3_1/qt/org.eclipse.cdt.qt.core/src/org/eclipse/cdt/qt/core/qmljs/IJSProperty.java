/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.qt.core.qmljs;

/**
 * A JavaScript object property from the <a href="https://github.com/estree/estree/blob/master/spec.md#property">ESTree
 * Specification</a>
 */
public interface IJSProperty extends IQmlASTNode {
	@Override
	default String getType() {
		return "Property"; //$NON-NLS-1$
	}

	/**
	 * @return {@link IJSLiteral}, or {@link IJSIdentifier}
	 */
	public IQmlASTNode getKey();

	public IJSExpression getValue();

	public String getKind();
}
