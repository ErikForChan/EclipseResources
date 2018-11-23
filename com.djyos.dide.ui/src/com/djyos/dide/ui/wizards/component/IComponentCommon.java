package com.djyos.dide.ui.wizards.component;

import java.util.List;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TreeItem;

import com.djyos.dide.ui.objects.Component;

public interface IComponentCommon {

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

	public Component getComponentByName(String itemName, List<Component> compontents);

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
