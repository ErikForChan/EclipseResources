package com.djyos.csky.abiv1.managedbuild.cross;

import org.eclipse.cdt.managedbuilder.core.IManagedOutputNameProvider;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class FlashImageManagedOutputNameProvider implements IManagedOutputNameProvider {

	// ------------------------------------------------------------------------

	public IPath[] getOutputNames(ITool tool, IPath[] primaryInputNames) {

		String value = null;

		IOption option = tool.getOptionBySuperClassId(Option.OPTION_CREATEFLASH_CHOICE);
		if (option != null)
			value = (String) option.getValue();
		// System.out.println(value);

		String ext = "unknown";
		if (value != null) {
			if (value.endsWith("." + Option.CHOICE_IHEX))
				ext = "hex";
			else if (value.endsWith("." + Option.CHOICE_SREC))
				ext = "srec";
			else if (value.endsWith("." + Option.CHOICE_SYMBOLSREC))
				ext = "symbolsrec";
			else if (value.endsWith("." + Option.CHOICE_BINARY))
				ext = "bin";
		}

		IPath[] iPath = new IPath[1];
		iPath[0] = new Path("${BuildArtifactFileBaseName}." + ext);
		return iPath;
	}

	// ------------------------------------------------------------------------
}

