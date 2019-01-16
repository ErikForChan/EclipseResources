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

	public String validateThirdCompt(List<Component> components, boolean isApp);

	public String getValidateMsg(Component component, boolean isApp);

	public String transalateCompt(String attribute);

	public Component createNewComponent(Component c);

	// 创建配置参数的Table
	public void creatConfigTable(Composite parent, Table table, String[] tableHeader);

	public void handleRequiredDepnds(boolean isApp, TreeItem item, TabFolder folder, List<Component> appCompontents,
			List<Component> ibootCompontents, List<Component> appCompontentsChecked,
			List<Component> ibootCompontentsChecked);

	public void travelItems_Depedent(TreeItem treeItem, Component itemCompt, boolean isApp, List<String> visitedComp,
			List<Component> appCompontents, List<Component> ibootCompontents, List<Component> appCompontentsChecked,
			List<Component> ibootCompontentsChecked, TabFolder folder);

	public Component getComponentByPath(String itemName, List<Component> compontents);

	public boolean isInteger(char c);

	public boolean isSymbolExist(ICConfigurationDescription cond, String parameter);

	public boolean isParentCompExist(List<Component> components, String parentName);

	public String getAIType(TreeItem item);

	public boolean haveChildren(Component parent, List<Component> componentList);

	public boolean isDepedent(TreeItem treeItem, TreeItem eventTreeItem, String type, Component itemCompt,
			boolean isApp, List<Component> appCompontents, List<Component> ibootCompontents,
			List<Component> appCompontentsChecked, List<Component> ibootCompontentsChecked);

	public List<String> getExpandParas(Component componentSelect, List<Component> compontentsList);

	public void resetConfigure(Component componentSelect, boolean[] isSelect, Table table);

	public String getRealCompName(String fAnno, String[] members, List<String> paras, List<String> ranges, String tag);

	public boolean travelItems_Mutex(TreeItem treeItem, Component itemCompt, TreeItem eventTreeItem);

	public void createCheckXml(boolean isApp, String projectLocation, List<Component> appCompontents,
			List<Component> ibootCompontents);
}
