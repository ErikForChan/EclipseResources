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
import com.djyos.dide.ui.objects.Board;

public class CreateBoardInfo {

	static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	public static void createBoardInfo(File file, List<Board> boards) {

		DocumentBuilder builder;
		try {
			factory.setIgnoringElementContentWhitespace(false);
			builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
			Element boardsElement = document.createElement("boards");
			for (int i = 0; i < boards.size(); i++) {
				Board b = boards.get(i);
				Element boardElement = document.createElement("board");
				boardElement.setTextContent(b.getBoardName());
				boardsElement.appendChild(boardElement);
			}
			document.appendChild(boardsElement);
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
