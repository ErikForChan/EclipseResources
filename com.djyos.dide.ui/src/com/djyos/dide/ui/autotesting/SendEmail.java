package com.djyos.dide.ui.autotesting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class SendEmail {

	// �����˵�ַ
	public static String senderAddress = "chenjm@sznari.com";
	// �ռ��˵�ַ
	public static String recipientAddress = "guxj@sznari.com";
	// �������˻���
	public static String senderAccount = "chenjm@sznari.com";
	// �������˻�����
	public static String senderPassword = "sunri@2017";

	private String getMail(File file, String target) {
		// TODO Auto-generated method stub
		BufferedReader br = null;
		String line = null;
		StringBuffer bufAll = new StringBuffer(); // �����޸Ĺ�����������ݣ���������
		try {
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				StringBuffer buf = new StringBuffer();
				// �޸����ݺ��Ĵ���
				if (line.startsWith(target)) {
					String[] infos = line.split("="); //$NON-NLS-1$
					return infos[1].trim();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					br = null;
				}
			}
		}
		return null;
	}

	public void send_Email(String errMsg) {

		// String fullPath =
		// Platform.getInstallLocation().getURL().toString().replace("\\", "/");
		// String didePath =
		// fullPath.substring(6,(fullPath.substring(0,fullPath.length()-1)).lastIndexOf("/")+1);
		// File stup_complie_file = new File(didePath+"auto_complier.txt");
		// //$NON-NLS-1$
		// if(stup_complie_file.exists()) {
		// senderAddress = getMail(stup_complie_file,"SENDER"); //$NON-NLS-1$
		// recipientAddress = getMail(stup_complie_file,"RECEIVER"); //$NON-NLS-1$
		// senderAccount = getMail(stup_complie_file,"SENDER"); //$NON-NLS-1$
		// senderPassword = getMail(stup_complie_file,"SENDER_PWD"); //$NON-NLS-1$
		// }
		//
		// //1�������ʼ��������Ĳ�������
		// Properties props = new Properties();
		// //�����û�����֤��ʽ
		// props.setProperty("mail.smtp.auth", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		// //���ô���Э��
		// props.setProperty("mail.transport.protocol", "smtp");//$NON-NLS-1$
		// //$NON-NLS-2$
		// //���÷����˵�SMTP��������ַ
		// props.setProperty("mail.smtp.host", "mail.sznari.com");//$NON-NLS-1$
		// //$NON-NLS-2$
		// //2��������������Ӧ�ó�������Ļ�����Ϣ�� Session ����
		// Session session = Session.getInstance(props);
		// //���õ�����Ϣ�ڿ���̨��ӡ����
		// session.setDebug(true);
		// //3�������ʼ���ʵ������
		// Message msg;
		// try {
		// msg = getMimeMessage(session,errMsg);
		// //4������session�����ȡ�ʼ��������Transport
		// Transport transport = session.getTransport();
		// //���÷����˵��˻���������
		// transport.connect(senderAccount, senderPassword);
		// //�����ʼ��������͵������ռ��˵�ַ��message.getAllRecipients() ��ȡ�������ڴ����ʼ�����ʱ��ӵ������ռ���, ������, ������
		// transport.sendMessage(msg,msg.getAllRecipients());
		//
		// //���ֻ�뷢�͸�ָ�����ˣ���������д��
		// //transport.sendMessage(msg, new Address[]{new
		// InternetAddress("xxx@qq.com")});
		//
		// //5���ر��ʼ�����
		// transport.close();
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	/**
	 * ��ô���һ���ʼ���ʵ������
	 * 
	 * @param session
	 * @param errMsg
	 * @return
	 * @throws MessagingException
	 * @throws AddressException
	 */
	// public static MimeMessage getMimeMessage(Session session, String errMsg)
	// throws Exception{
	//// List<String> fileList = new ArrayList<String>();
	//// fileList.add("F:\\djysdk\\svn��ά������.txt");
	//// fileList.add("F:\\djysdk\\Դ��ά�����б�.txt");
	// //����һ���ʼ���ʵ������
	// MimeMessage msg = new MimeMessage(session);
	// MimeMultipart mp = new MimeMultipart();
	// //���÷����˵�ַ
	// msg.setFrom(new InternetAddress(senderAddress));
	// /**
	// * �����ռ��˵�ַ���������Ӷ���ռ��ˡ����͡����ͣ�����������һ�д�����д����
	// * MimeMessage.RecipientType.TO:����
	// * MimeMessage.RecipientType.CC������
	// * MimeMessage.RecipientType.BCC������
	// */
	// msg.setRecipient(MimeMessage.RecipientType.TO,new
	// InternetAddress(recipientAddress));
	// //�����ʼ�����
	// SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");//�������ڸ�ʽ
	// msg.setSubject("["+df.format(new
	// Date())+"]�������������Զ�����:","UTF-8");//$NON-NLS-1$ //$NON-NLS-2$
	// //�����ʼ�����
	// BodyPart bp = new MimeBodyPart();
	//
	// bp.setContent("����target�������: \n"+errMsg,
	// "text/html;charset=utf-8");//$NON-NLS-1$ //$NON-NLS-2$
	// mp.addBodyPart(bp);
	//// for(String s:fileList) {
	//// bp = new MimeBodyPart();
	//// FileDataSource fds = new FileDataSource(s);
	//// bp.setDataHandler(new DataHandler(fds));
	//// bp.setFileName(MimeUtility.encodeText(fds.getName(), "UTF-8", "B"));
	//// mp.addBodyPart(bp);
	//// }
	//
	// msg.setContent(mp);
	// // msg.setContent("�򵥵Ĵ��ı��ʼ���", "text/html;charset=UTF-8");
	// //�����ʼ��ķ���ʱ��,Ĭ����������
	// msg.setSentDate(new Date());
	// msg.saveChanges();
	// return msg;
	// }
}