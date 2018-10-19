package com.djyos.dide.ui.filters;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class LibosFilter extends ViewerFilter{

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		// TODO Auto-generated method stub
		if (element instanceof IFolder) {
			IFolder folder = (IFolder) element;
			if(folder.getName().equals("data") || folder.getName().equals("libos")) {
				return false;
			}
		}
		
		return true;
	}

}
