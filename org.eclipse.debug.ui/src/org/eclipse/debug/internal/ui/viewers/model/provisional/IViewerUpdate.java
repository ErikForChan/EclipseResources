/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model.provisional;

import org.eclipse.debug.core.IRequest;
import org.eclipse.jface.viewers.TreePath;

/**
 * A context sensitive viewer update request.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 3.3
 */
public interface IViewerUpdate extends IRequest {

	/**
	 * Returns the context this update was requested in.
	 *
	 * @return context this update was requested in
	 */
	public IPresentationContext getPresentationContext();

    /**
     * Returns the model element associated with this request.
     *
     * @return associated model element
     */
    public Object getElement();

    /**
     * Returns the viewer tree path to the model element associated with this
     * request. An empty path indicates a root element.
     *
     * @return tree path, possibly empty
     */
    public TreePath getElementPath();

    /**
     * Returns the element that was the viewer input at the time the
     * request was made, possibly <code>null</code>.
     *
     * @return viewer input element, possibly <code>null</code>
     * @since 3.4
     */
    public Object getViewerInput();
}
