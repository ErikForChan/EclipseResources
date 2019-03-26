package com.djyos.dide.ui.wizards.component;

import java.util.List;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.djyos.dide.ui.objects.Component;
import com.ibm.icu.text.DecimalFormat;

public interface IComponentCommon {
	
	static final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	static final DecimalFormat df = new DecimalFormat("######0");
	static final String[] tableHeader = { "参数", "值", "注释" };
	static final String depedentLabel = "依赖组件: ";
	static final String mutexLabel = "互斥组件: ";
	static final String djyStart = "ptu32_t __djy_main(void)\r\n" + "{\n";
	static final String djyEnd = "\tdjy_main();\r\n" + "\treturn 0;\r\n" + "}\n\n";
	static final String initStart = "void Sys_ModuleInit(void)\r\n" + "{\n";
	static final String initEnd = "\treturn ;\r\n" + "}\n\n";
	static final String evttMain = "\tevtt_main = Djy_EvttRegist(EN_CORRELATIVE,CN_PRIO_RRS,0,0,\r\n"
			+ "\t__djy_main,NULL,CFG_MAINSTACK_LIMIT, \"main function\");\r\n"
			+ "\t//事件的两个参数暂设为0,如果用shell启动,可用来采集shell命令行参数\r\n" + "\tDjy_EventPop(evtt_main,NULL,0,NULL,0,0);\n\n";

}
