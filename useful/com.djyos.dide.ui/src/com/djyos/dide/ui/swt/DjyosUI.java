package com.djyos.dide.ui.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class DjyosUI {

	public static GridLayout DjyosGridLayout(int numColumns,int mLeft,int mHeight) {
		
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		layout.marginHeight = mHeight;
		layout.marginLeft = mLeft;
		return layout;
		
	}
	
	public static Composite DjyosComposite(Composite parent, int style) {
		Composite composite = new Composite(parent, style);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		return composite;
	}
	
	public static void enableControls(Composite c, boolean isEnable) {
		Control[] controls = c.getChildren();
		for (Control control : controls) {
			control.setEnabled(isEnable);
		}
	}
	
}
