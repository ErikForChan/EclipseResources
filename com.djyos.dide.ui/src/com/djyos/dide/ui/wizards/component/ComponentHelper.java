package com.djyos.dide.ui.wizards.component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.djyos.dide.ui.enums.ConfigureTarget;
import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.helper.LinkHelper;
import com.djyos.dide.ui.objects.Component;
import com.djyos.dide.ui.wizards.djyosProject.DjyosMessages;

public class ComponentHelper implements IComponentCommon {
	
	public static Component getComponentByName(String name,List<Component> compontents) {
		for(Component c:compontents) {
			if(c.getName().trim().equalsIgnoreCase(name.trim())) {
				return c;
			}
		}
		return null;
	}
	
	public void creatProjectConfiure(File file, String coreConfigure, boolean isApp, List<Component> appCheckedSort, List<Component> ibootCheckedSort, int index) {
		List<Component> compontentsCheckedSort = isApp?appCheckedSort:ibootCheckedSort;
		DideHelper.createNewFile(file);
		String defineInit = DjyosMessages.Automatically_Generated;
		defineInit += "#ifndef __PROJECT_CONFFIG_H__\r\n" + "#define __PROJECT_CONFFIG_H__\r\n\r\n"
				+ "#include \"stdint.h\"\r\n\r\n";
		if(index == 3) {
			defineInit += "#define CFG_RUNMODE_BAREAPP    1\r\n";
		}
		for (int i = 0; i < compontentsCheckedSort.size(); i++) {
			Component c = compontentsCheckedSort.get(i);
			if (c.getTarget().equals(ConfigureTarget.HEADER.getName()) && c.isSelect()) {
				if (c.getConfigure().contains("#define")) {
					defineInit += "//*******************************  Configure " + c.getName()
							+ "  ******************************************//\r\n";
					String[] configures = c.getConfigure().split("\n");
					for (int j = 0; j < configures.length; j++) {
						if (configures[j].contains("#define")) {
							String pureDefine = configures[j].trim().startsWith("//")
									? configures[j].replaceFirst("//", "")
									: configures[j];
							String annoName = null;
							String[] defines = pureDefine.split("//");
							if (defines.length > 1) {
								String[] infos = defines[1].split(",|，");
								if (infos[0].startsWith("\"") && infos[0].endsWith("\"")) {
									annoName = infos[0];
								}
							}
							defineInit += (annoName == null) ? configures[j]
									: configures[j].replace(annoName, "").replace(",", "").replace("，", "");
							defineInit += "\r\n";
						}
					}
				}
			}
		}
		defineInit += "//******************************* Core Clock ******************************************//\r\n";
		if(coreConfigure == null) {
			coreConfigure = String.format("%-9s", "#define") + String.format("%-32s", "CFG_CORE_MCLK")
			+ String.format("%-18s", "(" + 216 + "*Mhz)") + "//主频，内核要用，必须定义";
		}
		defineInit += coreConfigure;
		defineInit += "\r\n\r\n#endif";
//		System.out.println("defineInit:   "+defineInit);
		DideHelper.writeFile(file, defineInit,false);
	}

	@Override
	public String validateThirdCompt(List<Component> components, boolean isApp) {
		// TODO Auto-generated method stub
		for (int i = 0; i < components.size(); i++) {
			if (components.get(i).isSelect() && components.get(i).getAttribute().equals("third")) {
				String msg = getValidateMsg(components.get(i), isApp);
				if (msg != null) {
					return msg;
				}
			}
		}
		return null;
	}

	@Override
	public String getValidateMsg(Component component, boolean isApp) {
		// TODO Auto-generated method stub
		String configure = component.getConfigure();
		String[] parametersDefined = configure.split("\n");
		String tag = null;
		int paraSelected = 0;
		String[] infos = null;
		for (int i = 0; i < parametersDefined.length; i++) {
			String parameter = parametersDefined[i].trim();
			if (DideHelper.isParaHead(parameter)) {
				tag = DideHelper.getTag(parameter, tag);
				if (tag.equals("select")) {
					infos = parameter.split(",|，");
				}
			} else if (parameter.contains("#define") && tag.equals("select")) {
				if (!parameter.startsWith("//")) {
					paraSelected++;
				}
			}
		}

		if(infos != null) {
			if (infos.length > 1) {
				if (paraSelected < Integer.parseInt(infos[1])) {
					String attribute = component.getAttribute();
					attribute = transalateCompt(attribute);
					return  (isApp ? "App" : "Iboot") + ": " + attribute + "[" + component.getName() + "]请至少勾选" + infos[1] + "个参数\n"
							+"配置文件路径:\t"+(component.getParentPath()+"\\"+component.getFileName());
				}
			}
		}
		
		return null;
	}

	@Override
	public String transalateCompt(String attribute) {
		// TODO Auto-generated method stub
		if (attribute.equals("system")) {
			attribute = "核心组件";
		} else if (attribute.equals("bsp")) {
			attribute = "bsp组件";
		} else if (attribute.equals("third")) {
			attribute = "三方组件";
		} else if (attribute.equals("user")) {
			attribute = "用户组件";
		}
		return attribute;
	}

	@Override
	public Component createNewComponent(Component c) {
		// TODO Auto-generated method stub
		Component component = new Component();
		component.setName(c.getName());
		component.setAnnotation(c.getAnnotation());
		component.setAttribute(c.getAttribute());
		component.setGrade(c.getGrade());
		component.setCode(c.getCode());
		component.setConfigure(c.getConfigure());
		component.setLinkFolder(c.getLinkFolder());
		component.setDependents(c.getDependents());
		component.setMutexs(c.getMutexs());
		component.setFileName(c.getFileName());
		component.setSelectable(c.getSelectable());
		component.setParent(c.getParent());
		component.setWeakDependents(c.getWeakDependents());
		component.setExcludes(c.getExcludes());
		component.setIncludes(c.getIncludes());
		component.setSelect(c.isSelect());
		component.setParentPath(c.getParentPath());
		component.setTarget(c.getTarget());
		return component;
	}

	@Override
	public void creatConfigTable(Composite parent, Table table, String[] tableHeader) {
		// TODO Auto-generated method stub
		table = new Table(parent, SWT.NONE | SWT.FULL_SELECTION | SWT.H_SCROLL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.minimumHeight = 80;
		table.setLayoutData(gd);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		// 创建表头的字符串数组
		for (int i = 0; i < tableHeader.length; i++) {
			TableColumn tableColumn = new TableColumn(table, SWT.NONE | SWT.CENTER);
			tableColumn.setAlignment(SWT.LEFT);
			tableColumn.setText(tableHeader[i]);
			// 设置表头可移动，默认为false
			tableColumn.setMoveable(true);
			if (i == tableHeader.length - 1) {
				tableColumn.setWidth(200);
			} else {
				tableColumn.setWidth(130);
			}
		}
	}

	@Override
	public void handleRequiredDepnds(boolean isApp, TreeItem item, TabFolder folder, List<Component> appCompontents,
			List<Component> ibootCompontents, List<Component> appCompontentsChecked,
			List<Component> ibootCompontentsChecked) {
		// TODO Auto-generated method stub
		String type = isApp ? "App" : "Iboot";
		Component itemCompt = getComponentByPath(item.getData().toString(), isApp ? appCompontents : ibootCompontents);

		Control[] controls = folder.getChildren();
		for (Control c : controls) {
			Tree tempTree = (Tree) c;
			TreeItem[] fChilds = tempTree.getItems();
			for (TreeItem treeItem : fChilds) {
				if (treeItem.getText().equals(type)) {
					List<String> visitedComp = new ArrayList<String>();
					travelItems_Depedent(treeItem, itemCompt, isApp, visitedComp, appCompontents, ibootCompontents,
							appCompontentsChecked, ibootCompontentsChecked, folder);
					itemCompt.setSelect(true);
					if (isApp) {
						if (!appCompontentsChecked.contains(itemCompt)) {
							appCompontentsChecked.add(itemCompt);
						}
					} else {
						if (!ibootCompontentsChecked.contains(itemCompt)) {
							ibootCompontentsChecked.add(itemCompt);
						}
					}
					break;
				}
			}
		}
	}

	@Override
	public void travelItems_Depedent(TreeItem treeItem, Component itemCompt, boolean isApp, List<String> visitedComp,
			List<Component> appCompontents, List<Component> ibootCompontents, List<Component> appCompontentsChecked,
			List<Component> ibootCompontentsChecked, TabFolder folder) {
		// TODO Auto-generated method stub
		visitedComp.add(itemCompt.getName());
		List<String> depedents = itemCompt.getDependents();
		TreeItem[] items = treeItem.getItems();
		for (TreeItem item : items) {
			if (depedents.contains(item.getText())) {
				if (!item.getChecked()) {
					item.setChecked(true);
					Component curComp = getComponentByPath(item.getData().toString(),
							isApp ? appCompontents : ibootCompontents);
					;
					if (isApp) {
						if (!appCompontentsChecked.contains(curComp)) {
							appCompontentsChecked.add(curComp);
						}
					} else {
						if (!ibootCompontentsChecked.contains(curComp)) {
							ibootCompontentsChecked.add(curComp);
						}
					}
					curComp.setSelect(true);
					Control[] controls = folder.getChildren();
					for (Control c : controls) {
						Tree tempTree = (Tree) c;
						String type = isApp ? "App" : "Iboot";
						TreeItem[] fChilds = tempTree.getItems();
						for (TreeItem t : fChilds) {
							if (t.getText().equals(type)) {
								travelItems_Depedent(t, curComp, isApp, visitedComp, appCompontents, ibootCompontents,
										appCompontentsChecked, ibootCompontentsChecked, folder);
							}
						}
					}
				}
			}

			travelItems_Depedent(item, itemCompt, isApp, visitedComp, appCompontents, ibootCompontents,
					appCompontentsChecked, ibootCompontentsChecked, folder);
		}
	}

	@Override
	public Component getComponentByPath(String itemName, List<Component> compontents) {
		// TODO Auto-generated method stub
		for (Component component : compontents) {
			if (component.getParentPath().equals(itemName)) {
				return component;
			}
		}
		return null;
	}

	@Override
	public boolean isInteger(char c) {
		// TODO Auto-generated method stub
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
		return pattern.matcher(String.valueOf(c)).matches();
	}

	@Override
	public boolean isSymbolExist(ICConfigurationDescription cond, String parameter) {
		// TODO Auto-generated method stub
		ICResourceDescription rds = cond.getRootFolderDescription();
		ICLanguageSetting[] languageSettings = LinkHelper.getLangSetting(rds);
		for (int n = 0; n < languageSettings.length; n++) {
			if (n == 0) {
				ICLanguageSetting lang = languageSettings[n];
				List<ICLanguageSettingEntry> entries = lang.getSettingEntriesList(ICSettingEntry.MACRO);
				for (ICLanguageSettingEntry entry : entries) {
					if (parameter.contains(entry.getName())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean isParentCompExist(List<Component> components, String parentName) {
		// TODO Auto-generated method stub
		for (Component component : components) {
			if (component.getName().equals(parentName)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getAIType(TreeItem item) {
		// TODO Auto-generated method stub
		TreeItem parentItem = item.getParentItem();
		if (parentItem != null) {
			if (parentItem.getText().equals("App")) {
				return "App";
			} else if (parentItem.getText().equals("Iboot")) {
				return "Iboot";
			} else {
				return getAIType(parentItem);
			}
		} else {
			return null;
		}
	}

	@Override
	public boolean haveChildren(Component parent, List<Component> componentList) {
		// TODO Auto-generated method stub
		for (Component compt : componentList) {
			if (compt.getParent().equals(parent.getName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isDepedent(TreeItem treeItem, TreeItem eventTreeItem, String type, Component itemCompt,
			boolean isApp, List<Component> appCompontents, List<Component> ibootCompontents,
			List<Component> appCompontentsChecked, List<Component> ibootCompontentsChecked) {
		// TODO Auto-generated method stub
		TreeItem[] items = treeItem.getItems();
		List<String> eventDepedents = itemCompt.getDependents();
		boolean isDepedent = true;
		for (TreeItem item : items) {
			Component tempCompt = getComponentByPath(item.getData().toString(),
					isApp ? appCompontents : ibootCompontents);
			if (item.getChecked() && tempCompt != null) {
				if (tempCompt.getDependents().contains(eventTreeItem.getText())) {
					if (eventDepedents.contains(tempCompt.getName())) {
						item.setChecked(false);
						if (isApp) {
							if (!appCompontentsChecked.contains(tempCompt)) {
								appCompontentsChecked.remove(tempCompt);
							}
						} else {
							if (!ibootCompontentsChecked.contains(tempCompt)) {
								ibootCompontentsChecked.remove(tempCompt);
							}
						}
					} else {
						eventTreeItem.setChecked(true);
						isDepedent = false;
						IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						MessageDialog.openError(window.getShell(), "提示",
								"该组件被" + tempCompt.getName() + " 等已勾选的组件依赖，不可取消勾选");
						break;
					}
				}
			}

			if (item.getItems().length > 0 && isDepedent) {
				isDepedent = isDepedent(item, eventTreeItem, type, itemCompt, isApp, appCompontents, ibootCompontents,
						appCompontentsChecked, ibootCompontentsChecked);
			}
		}
		return isDepedent;
	}

	@Override
	public List<String> getExpandParas(Component componentSelect, List<Component> compontentsList) {
		// TODO Auto-generated method stub
		List<String> expandParas = new ArrayList<String>();
		for (int j = 0; j < compontentsList.size(); j++) {
			if (componentSelect.getParentPath().equals(compontentsList.get(j).getParentPath())) {
				String configure = compontentsList.get(j).getConfigure();
				String[] parametersDefined = configure.split("\n");
				String tag = null;
				for (int i = 0; i < parametersDefined.length; i++) {
					String parameter = parametersDefined[i];
					if (DideHelper.isParaHead(parameter)) {
						tag = DideHelper.getTag(parameter, tag);
					}

					if (parameter.contains("#define") && tag.equals("obj_para")) {
						expandParas.add(parameter);
					}
				}
			}
		}
		return expandParas;
	}

	@Override
	public void resetConfigure(Component componentSelect, boolean[] isSelect, Table table) {
		// TODO Auto-generated method stub
		TableItem[] items = table.getItems();
		String newConfig = "";
		int itemCount = 0;
		String tag = null;
		String[] parametersDefined = componentSelect.getConfigure().split("\n");
		// 给所有define重设值
		for (int i = 0; i < parametersDefined.length; i++) {
			String parameter = parametersDefined[i];
//			System.out.println("parameter:  "+parameter);
			if (DideHelper.isParaHead(parameter)) {
				tag = DideHelper.getTag(parameter, tag);
			}
			if (parameter.contains("#define") && !tag.equals("obj_para")) {
				String pure_config = parametersDefined[i].startsWith("//")?parametersDefined[i].replaceFirst("//", ""):parametersDefined[i];
				String[] defines = parametersDefined[i].trim().split("//");
				if(defines.length > 1) {
					String name = defines[1].contains("\"")?"\""+defines[1].split("\"")[1]+"\",":"";
					String[] members = pure_config.trim().split("\\s+");
					// define格式化
					if (isSelect[i]) {
						parametersDefined[i] = String.format("%-11s", members[0]) + " " + String.format("%-32s", members[1])
								+ " " + String.format("%-18s", items[itemCount].getData("value")) + "//" + name
								+ items[itemCount].getText(2);
					} else {
						parametersDefined[i] = String.format("%-11s", "//" + members[0]) + " "
								+ String.format("%-32s", members[1]) + " "
								+ String.format("%-18s", items[itemCount].getData("value")) + "//" + name
								+ items[itemCount].getText(2);
					}
				}
				itemCount++;
			}
			if (tag != null) {
				if (!tag.equals("obj_para")) {
					newConfig += parametersDefined[i] + "\n";
				} else if (!parameter.contains("#define")) {
					newConfig += parametersDefined[i] + "\n";
				}
			} else {
				newConfig += parametersDefined[i] + "\n";
			}
		}
//		for (int i = itemCount; i < table.getItemCount(); i++) {
//			String configure = String.format("%-11s", "#define") + " " + String.format("%-32s", items[i].getText(0))
//					+ " " + String.format("%-18s", items[i].getData("value")) + "//" + items[i].getText(2);
//			newConfig += configure + "\n";
//		}
//		System.out.println("newConfig:  "+newConfig);
		componentSelect.setConfigure(newConfig);
	}

	@Override
	public String getRealCompName(String fAnno, String[] members, List<String> paras, List<String> ranges, String tag) {
		// TODO Auto-generated method stub
		String realComptName = null;
		if (fAnno.startsWith("\"") && fAnno.endsWith("\"")) {
			if (!fAnno.contains("name")) {
				realComptName = fAnno.trim().replaceAll("\"", "");
			} else {
				realComptName = members[1];
			}
		} else {
			realComptName = members[1];
		}

		if (paras.contains(members[1])) {
			realComptName = "（参数重名）" + realComptName;
		}
		paras.add(members[1]);// 添加当前的参数名到paras

		if(ranges.size() > 1) {
			String min = ranges.get(0);
			String max = ranges.get(1);
			if (tag.equals("int")) {
				realComptName = realComptName + "( " + min + "~" + max + " )";
			} else if (tag.equals("string")) {
				realComptName = realComptName + "(长度：" + min + "~" + max + " )";
			}
		}
		return realComptName;
	}

	@Override
	public boolean travelItems_Mutex(TreeItem treeItem, Component itemCompt, TreeItem eventTreeItem) {
		// TODO Auto-generated method stub
		boolean isMutx = false;
		List<String> mutexs = itemCompt.getMutexs();
		TreeItem[] items = treeItem.getItems();
		for (TreeItem item : items) {
			if (mutexs.contains(item.getText())) {
				if (item.getChecked()) {
					eventTreeItem.setChecked(false);
					isMutx = true;
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					MessageDialog.openError(window.getShell(), "提示",
							item.getText() + "组件已勾选，与" + itemCompt.getName() + "互斥 ！");
				}
			} else {
				if (item.getItems().length > 0 && !isMutx) {
					isMutx = travelItems_Mutex(item, itemCompt, eventTreeItem);
				}
			}
		}
		return isMutx;
	}

	@Override
	public void createCheckXml(boolean isApp, String projectLocation, List<Component> appCompontents,
			List<Component> ibootCompontents) {
		// TODO Auto-generated method stub
		String xmlName = isApp?"app_component_check.xml":"iboot_component_check.xml";
		List<Component> curCompontents = isApp?appCompontents:ibootCompontents;
		CreateCheckXml ccx = new CreateCheckXml();
		File checkFile = new File(projectLocation + "/data/" + xmlName);
		DideHelper.createNewFile(checkFile);
		ccx.createCheck(curCompontents, checkFile);
	}

}
