package com.djyos.dide.ui.wizards.djyosProject.info;

import java.io.File;
import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.objects.Component;

public class CreateComponentInfo {

	static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	static String srcLocation = DideHelper.getDjyosSrcPath();

	public static void createComponentInfo(File file, List<Component> components) {

		DocumentBuilder builder;
		try {
			factory.setIgnoringElementContentWhitespace(false);
			builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
			Element csElement = document.createElement("components");
			for (int i = 0; i < components.size(); i++) {
				Component c = components.get(i);
				String componentPath = c.getParentPath().replace("\\", "/");
				Element cElement = document.createElement("component");
				String relativePath = componentPath.replace(srcLocation, "");
				cElement.setTextContent(relativePath + "/" + c.getFileName());
				csElement.appendChild(cElement);
			}
			document.appendChild(csElement);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");// 增加换行缩进，但此时缩进默认为0
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");// 设置缩进为2
			transformer.setOutputProperty("encoding", "UTF-8");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(writer));
			transformer.transform(new DOMSource(document), new StreamResult(file));
			writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			DideHelper.showErrorMessage("文件" + file.getName() + "创建失败！ " + e.getMessage());
		}

	}
}
