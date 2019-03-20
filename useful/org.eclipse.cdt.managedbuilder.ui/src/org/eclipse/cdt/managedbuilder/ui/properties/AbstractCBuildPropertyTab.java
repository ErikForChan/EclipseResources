/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICMultiConfigDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.MultiConfiguration;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.core.runtime.IPath;

/**
 * Proposed parent for Build System property tabs.
 * 
 * In addition to AbstractCPropertyTab functionality,
 * provides several utility methods for configurations
 * handling.
 */
public abstract class AbstractCBuildPropertyTab extends AbstractCPropertyTab {
	public IConfiguration getConfigurationFromHoldsOptions(IHoldsOptions ho){
		if(ho instanceof IToolChain)
			return ((IToolChain)ho).getParent();
		else if(ho instanceof ITool)
			return getConfigurationFromTool((ITool)ho);
		return null;
	}
	public IConfiguration getConfigurationFromTool(ITool tool){
		return tool.getParentResourceInfo().getParent();
	}
	
	public IConfiguration getCfg() {
		return getCfg(getResDesc().getConfiguration());
	}
	
	public static IConfiguration getCfg(ICConfigurationDescription cfgd) {
		if (cfgd instanceof ICMultiConfigDescription) {
			ICConfigurationDescription[] cfds = (ICConfigurationDescription[])((ICMultiConfigDescription)cfgd).getItems();
			return new MultiConfiguration(cfds);
		} else 
			return ManagedBuildManager.getConfigurationForDescription(cfgd);
	}

	/**
	 * Returns ResourceInfo for given ResourceDescription.
	 * Creates resourceInfo if it has not exist before.
	 * @param cfgd
	 * @return
	 */
	public IResourceInfo getResCfg(ICResourceDescription cfgd) {
		IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgd.getConfiguration());
		
		if (page.isForProject()) 
			return cfg.getRootFolderInfo();
		
		IPath p = cfgd.getPath();
		IResourceInfo f = null;
		f = cfg.getResourceInfo(p, false);
		
		if (f != null && (!p.equals(f.getPath()))) {
			String s = p.toString().replace('/', '_').replace('\\', '_');
			if (page.isForFile()) 
				f = cfg.createFileInfo(p, (IFolderInfo)f, null,
						f.getId()+ s, f.getName() + s);
			else
				f = cfg.createFolderInfo(p, (IFolderInfo)f, 
						f.getId() + s, f.getName() + s);
		}
		if (f == null) {
			if (page.isForFile()) 
				f = cfg.createFileInfo(p);
			
			else
				f = cfg.createFolderInfo(p);
		}
		return f;
	}
}
