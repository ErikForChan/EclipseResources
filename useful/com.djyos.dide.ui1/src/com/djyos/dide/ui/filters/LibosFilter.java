/*******************************************************************************
 * Copyright (c) 2017 Djyos Team.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.djyos.com
 *
 * Contributors:
 *     Djyos Team - Jiaming Chen
 *******************************************************************************/
package com.djyos.dide.ui.filters;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class LibosFilter extends ViewerFilter {

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		// TODO Auto-generated method stub
		if (element instanceof IFolder) {
			IFolder folder = (IFolder) element;
			if (folder.getName().equals("data")) {// || folder.getName().equals("libos")
				return false;
			}
		}

		return true;
	}

}
