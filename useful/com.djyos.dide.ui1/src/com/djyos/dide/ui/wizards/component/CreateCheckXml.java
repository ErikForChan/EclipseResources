package com.djyos.dide.ui.wizards.component;

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

public class CreateCheckXml {
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	public void createCheck(List<Component> cmpnts, File file) {
		factory.setIgnoringElementContentWhitespace(false);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();

			Element componentElement = document.createElement("component");
			for (int i = 0; i < cmpnts.size(); i++) {
				Element elment = document.createElement(cmpnts.get(i).getName());
				elment.setTextContent(cmpnts.get(i).isSelect() ? "true" : "false");
				componentElement.appendChild(elment);
			}
			document.appendChild(componentElement);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");// 增加换行缩进，但此时缩进默认为0
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");// 设置缩进为2
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
