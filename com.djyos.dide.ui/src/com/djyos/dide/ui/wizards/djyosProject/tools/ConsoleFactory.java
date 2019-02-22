package com.djyos.dide.ui.wizards.djyosProject.tools;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

public class ConsoleFactory implements IConsoleFactory{

	private static MessageConsole console = new MessageConsole("", null);
	static boolean exists = false;
	
	@Override
	public void openConsole() {
		// TODO Auto-generated method stub
		showConsole();
	}

	/** */
	/**
	 * ����:��ʾ����̨
	 * */
	private static void showConsole() {
		// TODO Auto-generated method stub
		if (console != null) {
			// �õ�Ĭ�Ͽ���̨������
			IConsoleManager manager = ConsolePlugin.getDefault()
					.getConsoleManager();
 
			// �õ����еĿ���̨ʵ��
			IConsole[] existing = manager.getConsoles();
			exists = false;
			// �´�����MessageConsoleʵ�������ھͼ��뵽����̨������������ʾ����
			for (int i = 0; i < existing.length; i++) {
				if (console == existing[i])
					exists = true;
			}
			if (!exists) {
				manager.addConsoles(new IConsole[] { console });
			}
 
			// console.activate();
 
		}
	}
	
	/** */
	/**
	 * ����:�رտ���̨
	 * */
	public static void closeConsole() {
		IConsoleManager manager = ConsolePlugin.getDefault()
				.getConsoleManager();
		if (console != null) {
			manager.removeConsoles(new IConsole[] { console });
		}
	}
 
	/**
	 * ��ȡ����̨
	 * 
	 * @return
	 */
	public static MessageConsole getConsole() {
 
		showConsole();
 
		return console;
	}

}
