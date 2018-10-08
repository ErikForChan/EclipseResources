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
	
//	//发件人地址
//    public static String senderAddress = "chenjm@sznari.com";
//    //收件人地址
//    public static String recipientAddress = "guxj@sznari.com";
//    //发件人账户名
//    public static String senderAccount = "chenjm@sznari.com";
//    //发件人账户密码
//    public static String senderPassword = "sunri@2017";
//
//	public void send(String errMsg) {
//		//1、连接邮件服务器的参数配置
//        Properties props = new Properties();
//        //设置用户的认证方式
//        props.setProperty("mail.smtp.auth", "true");
//        //设置传输协议
//        props.setProperty("mail.transport.protocol", "smtp");
//        //设置发件人的SMTP服务器地址
//        props.setProperty("mail.smtp.host", "mail.sznari.com");
//        //2、创建定义整个应用程序所需的环境信息的 Session 对象
//        Session session = Session.getInstance(props);
//        //设置调试信息在控制台打印出来
//        session.setDebug(true);
//        //3、创建邮件的实例对象
//        Message msg;
//		try {
//			msg = getMimeMessage(session,errMsg);
//			 //4、根据session对象获取邮件传输对象Transport
//	        Transport transport = session.getTransport();
//	        //设置发件人的账户名和密码
//	        transport.connect(senderAccount, senderPassword);
//	        //发送邮件，并发送到所有收件人地址，message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
//	        transport.sendMessage(msg,msg.getAllRecipients());
//	         
//	        //如果只想发送给指定的人，可以如下写法
//	        //transport.sendMessage(msg, new Address[]{new InternetAddress("xxx@qq.com")});
//	         
//	        //5、关闭邮件连接
//	        transport.close();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	 /**
//     * 获得创建一封邮件的实例对象
//     * @param session
//	 * @param errMsg 
//     * @return
//     * @throws MessagingException
//     * @throws AddressException
//     */
//    public static MimeMessage getMimeMessage(Session session, String errMsg) throws Exception{
//    	List<String> fileList = new ArrayList<String>();
//    	fileList.add("F:\\djysdk\\svn库维护规则.txt");
//    	fileList.add("F:\\djysdk\\源码维护人列表.txt");
//        //创建一封邮件的实例对象
//        MimeMessage msg = new MimeMessage(session);
//        MimeMultipart mp = new MimeMultipart();
//        //设置发件人地址
//        msg.setFrom(new InternetAddress(senderAddress));
//        /**
//         * 设置收件人地址（可以增加多个收件人、抄送、密送），即下面这一行代码书写多行
//         * MimeMessage.RecipientType.TO:发送
//         * MimeMessage.RecipientType.CC：抄送
//         * MimeMessage.RecipientType.BCC：密送
//         */
//        msg.setRecipient(MimeMessage.RecipientType.TO,new InternetAddress(recipientAddress));
//        //设置邮件主题
//        msg.setSubject("服务器编译结果自动发送: "+errMsg,"UTF-8");
//        //设置邮件正文
//        BodyPart bp = new MimeBodyPart(); 
//        bp.setContent("target编译出错: \n"+errMsg, "text/html;charset=utf-8");
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
//       // msg.setContent("简单的纯文本邮件！", "text/html;charset=UTF-8");
//        //设置邮件的发送时间,默认立即发送
//        msg.setSentDate(new Date());
//        msg.saveChanges(); 
//        return msg;
//    }
}