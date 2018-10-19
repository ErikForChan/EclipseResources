package com.djyos.csky.abiv1.managedbuild.cross;

import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCommandGenerator;
import org.eclipse.cdt.utils.cdtvariables.CdtVariableResolver;
import org.eclipse.cdt.utils.cdtvariables.IVariableSubstitutor;

public class LikerScriptCommandGenerator implements IOptionCommandGenerator {

	// ------------------------------------------------------------------------

	@Override
	public String generateCommand(IOption option, IVariableSubstitutor macroSubstitutor) {

		StringBuffer command = new StringBuffer();
		try {
			int valueType = option.getValueType();

			if (valueType == IOption.STRING) {
				// for compatibility with projects created with previous
				// versions, keep accepting single strings

				String value = option.getStringValue();
				value = CdtVariableResolver.resolveToString(value, macroSubstitutor);

				command.append("-T ");
				command.append(Utils.quoteWhitespaces(value));
			} else if (valueType == IOption.STRING_LIST) {
				for (String value : option.getStringListValue()) {

					if (value != null) {
						value = value.trim();
					}

					if (value.length() > 0) {

						value = CdtVariableResolver.resolveToString(value, macroSubstitutor);
						command.append("-T ");
						command.append(Utils.quoteWhitespaces(value));
						command.append(" ");
					}
				}
			}
		} catch (BuildException e) {
			Activator.log(e);
		} catch (CdtVariableException e) {
			Activator.log(e);
		}

		return command.toString().trim();
	}

	// ------------------------------------------------------------------------
}

