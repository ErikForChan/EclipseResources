/*******************************************************************************
 * Copyright (c) 2017 Djyos Team.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.djyos.com
 *
 * Contributors:
 *     Djyos Team - Jiaming Chen
 *******************************************************************************/
package com.djyos.dide.ui.arch;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.djyos.dide.ui.objects.Arch;

public class ReadArchXml {
	private static DocumentBuilderFactory dbFactory = null;
	private static DocumentBuilder db = null;
	private static Document document = null;

	public Arch getMutiplyFileArch(File file, Arch arch) {
		File parentFile;
		if (file.isDirectory()) {
			File archfile = getChildArchFile(file);
			if (archfile != null) {
				arch = getSingleFileArch(archfile, arch);
			}
			parentFile = file;

		} else {
			arch = getSingleFileArch(file, arch);
			parentFile = file.getParentFile();
		}

		if (!parentFile.getParentFile().getName().equals("arch")) {
			arch = getMutiplyFileArch(parentFile.getParentFile(), arch);
		}

		return arch;
	}

	private File getChildArchFile(File parentFile) {
		// TODO Auto-generated method stub
		File[] files = parentFile.listFiles();
		for (File f : files) {
			if (f.getName().startsWith("arch") && f.getName().endsWith(".xml")) {
				return f;
			}
		}
		return null;
	}

	public Arch getSingleFileArch(File file, Arch arch) {

		dbFactory = DocumentBuilderFactory.newInstance();
		try {
			db = dbFactory.newDocumentBuilder();
			document = db.parse(file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (file.getName().equals("arch.xml")) {
			arch.setArchPath(file.getPath());
			String serie = getArchType(file);
			arch.setSerie(serie);
		}
		NodeList archList = document.getElementsByTagName("arch");
		if (archList.getLength() > 0) {
			NodeList list = archList.item(0).getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					String nodeName = node.getNodeName();
					String content = node.getFirstChild().getTextContent();
					switch (nodeName) {
					// case "serie":
					// arch.setSerie(content);
					// break;
					// case "architecture":
					// arch.setArchitecture(content);
					// break;
					// case "family":
					// arch.setFamily(content);
					// break;
					case "toolchain":
						arch.setToolchain(content.trim());
						break;
					case "compile-option":
						NodeList list1 = node.getChildNodes();
						for (int j = 0; j < list1.getLength(); j++) {
							Node cNode = list1.item(j);
							if (cNode.getNodeType() == Node.ELEMENT_NODE) {
								String cNodeName = cNode.getNodeName();
								String cContent = cNode.getFirstChild().getTextContent();
								switch (cNodeName) {
								case "march":
									arch.setMarch(cContent);
									break;
								case "mcpu":
									arch.setMcpu(cContent);
									break;
								}
							}
						}
						break;
					case "vendor-option":
						NodeList list2 = node.getChildNodes();
						for (int j = 0; j < list2.getLength(); j++) {
							Node cNode = list2.item(j);
							if (cNode.getNodeType() == Node.ELEMENT_NODE) {
								String cNodeName = cNode.getNodeName();
								String cContent = cNode.getFirstChild().getTextContent();
								switch (cNodeName) {
								case "fpu-type":
									arch.setFpuType(cContent);
									break;
								}
							}
						}
						break;
					}
				}
			}
		}
		return arch;

	}

	private String getArchType(File file) {
		// TODO Auto-generated method stub
		File parentFile = file.getParentFile();
		if (parentFile.getName().equals("arch")) {
			return file.getName();
		} else {
			return getArchType(parentFile);
		}
	}
}
