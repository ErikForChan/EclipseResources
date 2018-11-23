package com.djyos.dide.ui.messages;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.ibm.icu.text.DecimalFormat;

public interface IComponentConstants {

	static final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	static final DecimalFormat df = new DecimalFormat("######0");
	static final String[] tableHeader = { "����", "ֵ", "ע��" };
	static final String depedentLabel = "�������: ";
	static final String mutexLabel = "�������: ";
	static final String djyStart = "ptu32_t __djy_main(void)\r\n" + "{\n";
	static final String djyEnd = "\tdjy_main();\r\n" + "\treturn 0;\r\n" + "}\n\n";
	static final String initStart = "void Sys_ModuleInit(void)\r\n" + "{\n";
	static final String initEnd = "\treturn ;\r\n" + "}\n\n";
	static final String evttMain = "\tevtt_main = Djy_EvttRegist(EN_CORRELATIVE,CN_PRIO_RRS,0,0,\r\n"
			+ "\t__djy_main,NULL,CFG_MAINSTACK_LIMIT, \"main function\");\r\n"
			+ "\t//�¼���������������Ϊ0,�����shell����,�������ɼ�shell�����в���\r\n" + "\tDjy_EventPop(evtt_main,NULL,0,NULL,0,0);\n\n";
}
