package org.eclipse.cdt.managedbuilder.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
//import javax.mail.Authenticator;
//import javax.mail.BodyPart;
//import javax.mail.Message;
//import javax.mail.Multipart;
//import javax.mail.PasswordAuthentication;
//import javax.mail.Session;
//import javax.mail.Transport;
//import javax.mail.internet.InternetAddress;
//import javax.mail.internet.MimeBodyPart;
//import javax.mail.internet.MimeMessage;
//import javax.mail.internet.MimeMultipart;
//import javax.mail.internet.MimeUtility;

public class SendErrorEmail {
	
//	//�����˵�ַ
//    public static String senderAddress = "chenjm@sznari.com";
//    //�ռ��˵�ַ
//    public static String recipientAddress = "guxj@sznari.com";
//    //�������˻���
//    public static String senderAccount = "chenjm@sznari.com";
//    //�������˻�����
//    public static String senderPassword = "sunri@2017";
//
//	public void send(String errMsg) {
//		//1�������ʼ��������Ĳ�������
//        Properties props = new Properties();
//        //�����û�����֤��ʽ
//        props.setProperty("mail.smtp.auth", "true");
//        //���ô���Э��
//        props.setProperty("mail.transport.protocol", "smtp");
//        //���÷����˵�SMTP��������ַ
//        props.setProperty("mail.smtp.host", "mail.sznari.com");
//        //2��������������Ӧ�ó�������Ļ�����Ϣ�� Session ����
//        Session session = Session.getInstance(props);
//        //���õ�����Ϣ�ڿ���̨��ӡ����
//        session.setDebug(true);
//        //3�������ʼ���ʵ������
//        Message msg;
//		try {
//			msg = getMimeMessage(session,errMsg);
//			 //4������session�����ȡ�ʼ��������Transport
//	        Transport transport = session.getTransport();
//	        //���÷����˵��˻���������
//	        transport.connect(senderAccount, senderPassword);
//	        //�����ʼ��������͵������ռ��˵�ַ��message.getAllRecipients() ��ȡ�������ڴ����ʼ�����ʱ��ӵ������ռ���, ������, ������
//	        transport.sendMessage(msg,msg.getAllRecipients());
//	         
//	        //���ֻ�뷢�͸�ָ�����ˣ���������д��
//	        //transport.sendMessage(msg, new Address[]{new InternetAddress("xxx@qq.com")});
//	         
//	        //5���ر��ʼ�����
//	        transport.close();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	 /**
//     * ��ô���һ���ʼ���ʵ������
//     * @param session
//	 * @param errMsg 
//     * @return
//     * @throws MessagingException
//     * @throws AddressException
//     */
//    public static MimeMessage getMimeMessage(Session session, String errMsg) throws Exception{
//    	List<String> fileList = new ArrayList<String>();
//    	fileList.add("F:\\djysdk\\svn��ά������.txt");
//    	fileList.add("F:\\djysdk\\Դ��ά�����б�.txt");
//        //����һ���ʼ���ʵ������
//        MimeMessage msg = new MimeMessage(session);
//        MimeMultipart mp = new MimeMultipart();
//        //���÷����˵�ַ
//        msg.setFrom(new InternetAddress(senderAddress));
//        /**
//         * �����ռ��˵�ַ���������Ӷ���ռ��ˡ����͡����ͣ�����������һ�д�����д����
//         * MimeMessage.RecipientType.TO:����
//         * MimeMessage.RecipientType.CC������
//         * MimeMessage.RecipientType.BCC������
//         */
//        msg.setRecipient(MimeMessage.RecipientType.TO,new InternetAddress(recipientAddress));
//        //�����ʼ�����
//        msg.setSubject("�������������Զ�����: "+errMsg,"UTF-8");
//        //�����ʼ�����
//        BodyPart bp = new MimeBodyPart(); 
//        bp.setContent("target�������: \n"+errMsg, "text/html;charset=utf-8");
//        mp.addBodyPart(bp);
////        for(String s:fileList) {
////        	bp = new MimeBodyPart();
////        	FileDataSource fds = new FileDataSource(s); 
////            bp.setDataHandler(new DataHandler(fds)); 
////            bp.setFileName(MimeUtility.encodeText(fds.getName(), "UTF-8", "B"));
////            mp.addBodyPart(bp);
////        }
//        
//        msg.setContent(mp);
//       // msg.setContent("�򵥵Ĵ��ı��ʼ���", "text/html;charset=UTF-8");
//        //�����ʼ��ķ���ʱ��,Ĭ����������
//        msg.setSentDate(new Date());
//        msg.saveChanges(); 
//        return msg;
//    }
}