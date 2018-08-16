package org.eclipse.cdt.ui.startup;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

public class SaveFileHandler implements IResourceChangeListener{

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		// TODO Auto-generated method stub
		try {
			event.getDelta().accept(new IResourceDeltaVisitor() {	
				@Override
				public boolean visit(IResourceDelta delta) throws CoreException {
					// TODO Auto-generated method stub
					IResource resource = delta.getResource();
					//System.out.println("resource.getName(): "+resource.getName());
					if (resource instanceof IFile)
					{				
						switch (delta.getKind()) {
						case IResourceDelta.ADDED:
							// handle added resource
							
							break;
						case IResourceDelta.REMOVED:
							// handle removed resource
							
							break;
						case IResourceDelta.CHANGED:
							// handle changed resource
							System.out.println("resourceREMOVED ×ÊÔ´Ãû:"+resource.getName());		
							IProject project = resource.getProject();
							break;					
						}
					}
					//return true to continue visiting children.
					return true;
				}
			});
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}
