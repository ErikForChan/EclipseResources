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
import com.djyos.dide.ui.objects.Cpu;
import com.djyos.dide.ui.wizards.djyosProject.tools.PathTool;

public class CreateCpuInfo {

	private String didePath = PathTool.getDIDEPath();
	String srcLocation = didePath + "djysrc";

	public static void createCpuInfo(File file, List<Cpu> cpus) {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			factory.setIgnoringElementContentWhitespace(false);
			builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
			Element cpusElement = document.createElement("cpus");
			for (int i = 0; i < cpus.size(); i++) {
				Cpu c = cpus.get(i);
				Element cpuElement = document.createElement("cpu");
				// String relativePath = c.getParentPath().replace(srcLocation, "");
				cpuElement.setTextContent(c.getCpuName());
				cpusElement.appendChild(cpuElement);
			}
			document.appendChild(cpusElement);
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
