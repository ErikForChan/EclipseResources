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

import com.djyos.dide.ui.wizards.djyosProject.tools.DideHelper;

public class CreateCheckXml {
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	private DideHelper dideHelper = new DideHelper();

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
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");// ���ӻ�������������ʱ����Ĭ��Ϊ0
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");// ��������Ϊ2
			transformer.setOutputProperty("encoding", "UTF-8");

			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(writer));
			transformer.transform(new DOMSource(document), new StreamResult(file));
			writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			dideHelper.showErrorMessage("�ļ�" + file.getName() + "����ʧ�ܣ� " + e.getMessage());
		}

	}
}
