package com.djyos.dide.ui.wizards.cpu;

import java.io.File;
import java.io.StringWriter;

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
import com.djyos.dide.ui.objects.Core;
import com.djyos.dide.ui.objects.CoreMemory;
import com.djyos.dide.ui.objects.Cpu;

public class CreateCpuXml {

	static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	static void fillCpuCore(Core curCore, Document document, Element coreElem) {
		if (curCore.getName() != null) {
			Element name = document.createElement("name");
			name.setTextContent(curCore.getName());
			coreElem.appendChild(name);
		}
		if (curCore.getArch().getSerie() != null) {
			Element type = document.createElement("type");
			type.setTextContent(curCore.getArch().getSerie());
			coreElem.appendChild(type);
		}
		if (curCore.getArch().getMarch() != null) {
			Element arch = document.createElement("arch");
			arch.setTextContent(curCore.getArch().getMarch());
			coreElem.appendChild(arch);
		}
		if (curCore.getArch().getMcpu() != null) {
			Element family = document.createElement("family");
			family.setTextContent(curCore.getArch().getMcpu());
			coreElem.appendChild(family);
		}
		if (curCore.getFloatABI() != null) {
			Element floatABI = document.createElement("floatABI");
			floatABI.setTextContent(curCore.getFloatABI());
			coreElem.appendChild(floatABI);
		}
		if (curCore.getFpuType() != null) {
			Element fpuType = document.createElement("fpuType");
			fpuType.setTextContent(curCore.getFpuType());
			coreElem.appendChild(fpuType);
		}
		if (curCore.getResetAddr() != null) {
			Element resetAddr = document.createElement("resetAddr");
			resetAddr.setTextContent(curCore.getResetAddr());
			coreElem.appendChild(resetAddr);
		}
		if (curCore.getMemorys().size() != 0) {

			for (int j = 0; j < curCore.getMemorys().size(); j++) {
				CoreMemory curMemory = curCore.getMemorys().get(j);
				Element memory = document.createElement("memory");
				Element type = document.createElement("type");
				type.setTextContent(curMemory.getType());
				memory.appendChild(type);
				Element startAddr = document.createElement("startAddr");
				startAddr.setTextContent(curMemory.getStartAddr());
				memory.appendChild(startAddr);
				Element size = document.createElement("size");
				String memorySize = curMemory.getSize();
				if (!memorySize.contains("k")) {
					memorySize += "k";
				}
				size.setTextContent(memorySize);
				memory.appendChild(size);
				coreElem.appendChild(memory);
			}

		}
	}
	
	static void createNewGroupXml(Cpu cpu, File file) {
		try {
			factory.setIgnoringElementContentWhitespace(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();

			Element cpuElement = document.createElement("cpu");
			for (int i = 0; i < cpu.getCores().size(); i++) {
				Element coreElem = document.createElement("core");
				coreElem.setAttribute("id", String.valueOf(i + 1));
				Core curCore = cpu.getCores().get(i);
				fillCpuCore(curCore,document,coreElem);
				cpuElement.appendChild(coreElem);
			}
			if(cpu.getShared_memorys().size() > 0) {
				Element smhElem = document.createElement("shared_memory");
				for(CoreMemory m : cpu.getShared_memorys()) {
					Element smElem = document.createElement("memory");
					if(m.getType() != null) {
						smElem.setAttribute("type", m.getType());
					}
					if(m.getStartAddr() != null) {
						smElem.setAttribute("startAddr", m.getStartAddr());
					}
					if(m.getSize() != null) {
						String memorySize = m.getSize();
						if (!memorySize.contains("k")) {
							memorySize += "k";
						}
						smElem.setAttribute("size", memorySize);
					}
					smhElem.appendChild(smElem);
				}
				cpuElement.appendChild(smhElem);
			}
			
			
			document.appendChild(cpuElement);
			trans(document,file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			DideHelper.showErrorMessage("文件" + file.getName() + "创建失败！ " + e.getMessage());
		}

	}
	
	static boolean createNewCpuXml(Cpu cpu, File file, String completeName) {
		try {
			factory.setIgnoringElementContentWhitespace(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
			Element cpuElement = document.createElement("cpu");
			Element cpuName = document.createElement("cpuName");
			cpuName.setTextContent(completeName);
			cpuElement.appendChild(cpuName);

			for (int i = 0; i < cpu.getCores().size(); i++) {
				Element coreElem = document.createElement("core");
				coreElem.setAttribute("id", String.valueOf(i + 1));
				Core curCore = cpu.getCores().get(i);
				fillCpuCore(curCore,document,coreElem);
				cpuElement.appendChild(coreElem);
			}
			
			document.appendChild(cpuElement);
			trans(document,file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			DideHelper.showErrorMessage("文件" + file.getName() + "创建失败！ " + e.getMessage());
		}
		return true;
	}
	
	//基本用不到这个方法 当啮合个数为0的时候才会调用
	static void createPublicXml(Core core, File file) {
		try {
			factory.setIgnoringElementContentWhitespace(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
			Element cpuElement = document.createElement("cpu");
			fillCore(core,document,cpuElement);
			document.appendChild(cpuElement);
			trans(document,file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			DideHelper.showErrorMessage("文件" + file.getName() + "创建失败！ " + e.getMessage());
		}
	}
	
	static void fillCore(Core core, Document document, Element cpuElement) {
		if (core.getName() != null) {
			Element name = document.createElement("name");
			name.setTextContent(core.getName());
			cpuElement.appendChild(name);
		}
		if (core.getArch().getSerie() != null) {
			Element type = document.createElement("type");
			type.setTextContent(core.getArch().getSerie());
			cpuElement.appendChild(type);
		}
		if (core.getArch().getMarch() != null) {
			Element arch = document.createElement("arch");
			arch.setTextContent(core.getArch().getMarch());
			cpuElement.appendChild(arch);
		}
		if (core.getArch().getMcpu() != null) {
			Element family = document.createElement("family");
			family.setTextContent(core.getArch().getMcpu());
			cpuElement.appendChild(family);
		}
		if (core.getFloatABI() != null) {
			Element floatABI = document.createElement("floatABI");
			floatABI.setTextContent(core.getFloatABI());
			cpuElement.appendChild(floatABI);
		}
		if (core.getFpuType() != null) {
			Element fpuType = document.createElement("fpuType");
			fpuType.setTextContent(core.getFpuType());
			cpuElement.appendChild(fpuType);
		}
		if (core.getResetAddr() != null) {
			Element resetAddr = document.createElement("resetAddr");
			resetAddr.setTextContent(core.getResetAddr());
			cpuElement.appendChild(resetAddr);
		}
		if (core.getMemorys().size() != 0) {
			for (int j = 0; j < core.getMemorys().size(); j++) {
				CoreMemory curMemory = core.getMemorys().get(j);
				Element memory = document.createElement("memory");
				Element type = document.createElement("type");
				type.setTextContent(curMemory.getType());
				memory.appendChild(type);
				Element startAddr = document.createElement("startAddr");
				startAddr.setTextContent(curMemory.getStartAddr());
				memory.appendChild(startAddr);
				Element size = document.createElement("size");
				String memorySize = curMemory.getSize();
				if (!memorySize.contains("k")) {
					memorySize += "k";
				}
				size.setTextContent(memorySize);
				memory.appendChild(size);
				cpuElement.appendChild(memory);
			}
		}
	}
	
	private static void trans(Document document, File file) {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = transformerFactory.newTransformer();
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
		}
	}
}
