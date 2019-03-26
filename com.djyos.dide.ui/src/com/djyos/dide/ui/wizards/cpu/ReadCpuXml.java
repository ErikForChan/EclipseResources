package com.djyos.dide.ui.wizards.cpu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.djyos.dide.ui.arch.ReadArchXml;
import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.objects.Arch;
import com.djyos.dide.ui.objects.Core;
import com.djyos.dide.ui.objects.CoreMemory;
import com.djyos.dide.ui.objects.Cpu;

public class ReadCpuXml {
	private static DocumentBuilderFactory dbFactory = null;
	private static DocumentBuilder db = null;
	private static Document document = null;
	private static List<Cpu> cpus = new ArrayList<Cpu>();
	static File archSourceFile = new File(DideHelper.getDjyosSrcPath() + "/bsp/arch");
	static List<File> archXmlFiles = DideHelper.getArchXmlFiles(archSourceFile, new ArrayList<File>());
	static {
		try {
			dbFactory = DocumentBuilderFactory.newInstance();
			db = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	// 获取当前路径下所有Cpu信息，通过扫描各个目下的xml文件
	public static List<Cpu> getAllCpus() {

		cpus = new ArrayList<Cpu>();
		String sourcePath = DideHelper.getDIDEPath() + "djysrc/bsp/cpudrv";
		File sourceFile = new File(sourcePath);
		File[] files = sourceFile.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				getCpus(file);
			}
		}
		System.out.println("cpus.size():   " + cpus.size());
		return cpus;

	}

	// 遍历父目录，当父目录名为cpudrv时停止扫描
	private static void traverseParents(Cpu cpu, File parentFile) {
		if (!parentFile.getName().contains("cpudrv")) {
			File xmlFile = DideHelper.getXmlFile(parentFile);
			try {
				if (xmlFile != null) {
					unitCpu(cpu, xmlFile);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			parentFile = parentFile.getParentFile();
			traverseParents(cpu, parentFile);
		}
	}

	// 获取Cpu.xml的信息
	public static void getCpus(File sourceFile) {// Atmel stm32
		File[] files = sourceFile.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				if (!file.getName().equals("include") && !file.getName().equals("src")) {
					getCpus(file);// 如果为目录，则继续扫描该目录
				}
			} else if (file.getName().endsWith(".xml")) {// 如果为文件，且为xml格式的文件，则遍历所有父目录，获取当前cpu的信息
				try {
					if (file.isFile() && !file.getName().contains("group") && file.getName().contains("cpu_")) {
						Cpu cpu = new Cpu();
						File parentFile = file.getParentFile();
						traverseParents(cpu, parentFile);
						Cpu newCpu = new Cpu(cpu.getCpuName(), cpu.getParentPath(), cpu.getCores());
						cpus.add(newCpu);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static Cpu getCpuInfos(File file) throws Exception {
		Cpu cpu = new Cpu();
		if (file.getName().startsWith("cpu_") && !file.getName().contains("group_")) {
			cpu.setParentPath(file.getParentFile().getPath());
		}
		document = db.parse(file);
		NodeList nameList = document.getElementsByTagName("cpuName");
		for (int i = 0; i < nameList.getLength(); i++) {
			String cpuName = nameList.item(i).getFirstChild().getTextContent();
			cpu.setCpuName(cpuName);
		}

		NodeList coreList = document.getElementsByTagName("core");
		if (coreList.getLength() == 0) {

		} else {
			cpu.setCoreNum(coreList.getLength());
			List<Core> cores = new ArrayList<Core>();

			for (int i = 0; i < coreList.getLength(); i++) {
				Core core;
				core = new Core();

				Node node = coreList.item(i);
				NamedNodeMap namedNodeMap = node.getAttributes();
				String id = namedNodeMap.getNamedItem("id").getTextContent();
				core.setId(Integer.parseInt(id));
				handleMultipleCoreListNode(node, core, false);
				cores.add(core);
			}
			cpu.setCores(cores);
		}

		return cpu;
	}

	private static void handleMultipleCoreListNode(Node node, Core core, boolean memoryConfiged) {
		// TODO Auto-generated method stub
		NodeList cList = node.getChildNodes();
		for (int j = 0; j < cList.getLength(); j++) {
			Node cNode = cList.item(j);
			if (cNode.getNodeType() == Node.ELEMENT_NODE) {
				String nodeName = cNode.getNodeName();
				if (cNode.getFirstChild() != null) {
					String content = cNode.getFirstChild().getTextContent();
					switch (nodeName) {
					case "type":
						if (core.getArch().getSerie() == null) {
							core.getArch().setSerie(content);
						}
						break;
					case "resetAddr":
						if (core.getResetAddr() == null) {
							core.setResetAddr(content);
						}
						break;
					case "family":
						if (core.getArch().getMcpu() == null) {
							core.getArch().setMcpu(content);
							SetArch(core, content);
						}
						break;
					case "arch":
						if (core.getArch().getMarch() == null) {
							core.getArch().setMarch(content);
						}
						break;
					case "fpuType":
						if (core.getFpuType() == null) {
							core.setFpuType(content);
						}
						break;
					case "floatABI":
						if (core.getFloatABI() == null) {
							core.setFloatABI(content);
						}
						break;
					case "memory":
						NodeList memoryList = cNode.getChildNodes();
						if (!memoryConfiged) {
							CoreMemory memory = new CoreMemory();
							for (int k = 1; k < memoryList.getLength(); k += 2) {
								org.w3c.dom.Node mNode = memoryList.item(k);
								String mName = mNode.getNodeName();
								String mContent = mNode.getFirstChild().getTextContent();
								if (mName.equals("type")) {
									memory.setType(mContent);
								} else if (mName.equals("startAddr")) {
									memory.setStartAddr(mContent);
								} else if (mName.equals("size")) {
									memory.setSize(mContent.substring(0, mContent.length()));
								}
							}
							core.getMemorys().add(memory);
						}
						break;
					}
				}

			}
		}
	}

	private static void SetArch(Core core, String family) {
		Arch myArch = new Arch();
		for (File f : archXmlFiles) {
			if (f.getParentFile().getName().equals(family)) {
				ReadArchXml rax = new ReadArchXml();
				myArch = rax.getMutiplyFileArch(f, myArch);
				break;
			}
		}
		core.setArch(myArch);
	}

	private static void handleNoListOneCoreNode(Core core, NodeList cList) {
		for (int i = 0; i < cList.getLength(); i++) {
			Node node = cList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				String nodeName = node.getNodeName();
				String content = node.getFirstChild().getTextContent();
				switch (nodeName) {
				case "type":
					core.getArch().setSerie(content);
					break;
				case "resetAddr":
					core.setResetAddr(content);
					break;
				case "family":
					core.getArch().setFamily(content);
					SetArch(core, content);
					break;
				case "arch":
					core.getArch().setArchitecture(content);
					break;
				case "fpuType":
					core.setFpuType(content);
					break;
				case "floatABI":
					core.setFloatABI(content);
					break;
				}
			}
		}

	}

	private static void handleNoListMultipleCoreNode(NodeList cList, List<Core> cores) {
		for (int i = 0; i < cList.getLength(); i++) {
			Node node = cList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				String nodeName = node.getNodeName();
				String content = node.getFirstChild().getTextContent();
				switch (nodeName) {
				case "type":
					for (int j = 0; j < cores.size(); j++) {
						if (cores.get(j).getArch().getSerie() == null) {
							cores.get(j).getArch().setSerie(content);
						}
					}
					break;
				case "resetAddr":
					for (int j = 0; j < cores.size(); j++) {
						if (cores.get(j).getResetAddr() == null) {
							cores.get(j).setResetAddr(content);
						}

					}
					break;
				case "family":
					for (int j = 0; j < cores.size(); j++) {
						if (cores.get(j).getArch().getMcpu() == null) {
							cores.get(j).getArch().setMcpu(content);
							SetArch(cores.get(j), content);
						}
					}

					break;
				case "arch":
					for (int j = 0; j < cores.size(); j++) {
						if (cores.get(j).getArch().getMarch() == null) {
							cores.get(j).getArch().setMarch(content);
						}
					}

					break;
				case "fpuType":
					for (int j = 0; j < cores.size(); j++) {
						if (cores.get(j).getFpuType() == null) {
							cores.get(j).setFpuType(content);
						}
					}
					break;
				case "floatABI":
					for (int j = 0; j < cores.size(); j++) {
						if (cores.get(j).getFloatABI() == null) {
							cores.get(j).setFloatABI(content);
						}
					}
					break;
				}
			}
		}
	}

	public static Cpu unitCpu(Cpu cpu, File file) throws Exception {
		// 将给定 URI 的内容解析为一个 XML 文档,并返回Document对象
		// System.out.println("cpuFile: "+file.getName());
		document = db.parse(file);
		if (file.getName().startsWith("cpu_") && !file.getName().contains("group_")) {
			cpu.setParentPath(file.getParentFile().getPath());
		}

		NodeList nameList = document.getElementsByTagName("cpuName");
		for (int i = 0; i < nameList.getLength(); i++) {
			String cpuName = nameList.item(i).getFirstChild().getTextContent();
			cpu.setCpuName(cpuName);
		}

		NodeList coreList = document.getElementsByTagName("core");
		if (coreList.getLength() == 0) {
			NodeList cpuList = document.getElementsByTagName("cpu");
			Node cNode = cpuList.item(0);
			NodeList cList = cNode.getChildNodes();
			List<Core> cores = cpu.getCores();
			if (cores.size() == 0) {// 未配置内核个数
				Core core = new Core();
				handleNoListOneCoreNode(core, cList);
				cores.add(core);
			} else {
				handleNoListMultipleCoreNode(cList, cores);
			}
		} else {
			if (cpu.getCoreNum() == 0) {
				cpu.setCoreNum(coreList.getLength());
			}
			List<Core> cores;
			boolean isClean = true;
			if (cpu.getCores().size() == 0) {
				cores = new ArrayList<Core>();
			} else {
				isClean = false;
				cores = cpu.getCores();
			}
			for (int i = 0; i < coreList.getLength(); i++) {
				Core core;
				boolean memoryConfiged = false;
				if (cpu.getCores().size() == 0) {
					core = new Core();
				} else {
					core = cpu.getCores().get(i);
				}
				if (core.getMemorys().size() != 0) {
					memoryConfiged = true;
				}
				Node node = coreList.item(i);
				NamedNodeMap namedNodeMap = node.getAttributes();
				String id = namedNodeMap.getNamedItem("id").getTextContent();
				core.setId(Integer.parseInt(id));
				handleMultipleCoreListNode(node, core, memoryConfiged);
				if (isClean) {
					cores.add(core);
				}

			}
			if (isClean) {
				cpu.setCores(cores);
			}

		}
		return cpu;
	}
}
