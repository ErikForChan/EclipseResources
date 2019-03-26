package com.djyos.dide.ui.handlers;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.startup.HandleProjectImport;

public class FileHandler implements IResourceChangeListener {

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		// TODO Auto-generated method stub
		try {
			event.getDelta().accept(new IResourceDeltaVisitor() {
				@Override
				public boolean visit(IResourceDelta delta) throws CoreException {
					IResource resource = delta.getResource();
					if (resource instanceof IFile) {
						switch (delta.getKind()) {
						case IResourceDelta.ADDED:
							// handle added resource
							IProject project = resource.getProject();
							File stup_complie_file = new File(DideHelper.getDIDEPath() + "auto_complier.txt");

							if (stup_complie_file.exists()) {
								if (resource.getName().endsWith(".a") && resource.getName().startsWith("libos")) {
									String libCfgName = resource.getName().replace(".a", "");
									String commonName = libCfgName.replace("libos", "");
									String targetName = project.getName() + commonName;
									DideHelper.buildTarget(project, targetName);
								}
							}

							if (resource.getName().equals(".project")) {
								File hardWardInfoFile = new File(
										project.getLocation().toString() + "/data/hardware_info.xml");
								if (hardWardInfoFile.exists()) {

									ConfigurationHandler cfgHandler = new ConfigurationHandler();
									cfgHandler.setDefaultArchiverCmd(project);

									HandleProjectImport projectListener = new HandleProjectImport();
									projectListener.handleProjectElemExculde(project);
								}
							}
							break;
						case IResourceDelta.REMOVED:
							// handle removed resource

							break;
						case IResourceDelta.CHANGED:
							// handle changed resource
							break;
						}
					}
					return true;
				}
			});
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
