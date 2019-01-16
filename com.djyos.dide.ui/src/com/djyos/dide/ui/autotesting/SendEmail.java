package com.djyos.dide.ui.autotesting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class SendEmail {

	// 发件人地址
	public static String senderAddress = "chenjm@sznari.com";
	// 收件人地址
	public static String recipientAddress = "guxj@sznari.com";
	// 发件人账户名
	public static String senderAccount = "chenjm@sznari.com";
	// 发件人账户密码
	public static String senderPassword = "sunri@2017";

	private String getMail(File file, String target) {
		// TODO Auto-generated method stub
		BufferedReader br = null;
		String line = null;
		StringBuffer bufAll = new StringBuffer(); // 保存修改过后的所有内容，不断增加
		try {
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				StringBuffer buf = new StringBuffer();
				// 修改内容核心代码
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
		// //1、连接邮件服务器的参数配置
		// Properties props = new Properties();
		// //设置用户的认证方式
		// props.setProperty("mail.smtp.auth", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		// //设置传输协议
		// props.setProperty("mail.transport.protocol", "smtp");//$NON-NLS-1$
		// //$NON-NLS-2$
		// //设置发件人的SMTP服务器地址
		// props.setProperty("mail.smtp.host", "mail.sznari.com");//$NON-NLS-1$
		// //$NON-NLS-2$
		// //2、创建定义整个应用程序所需的环境信息的 Session 对象
		// Session session = Session.getInstance(props);
		// //设置调试信息在控制台打印出来
		// session.setDebug(true);
		// //3、创建邮件的实例对象
		// Message msg;
		// try {
		// msg = getMimeMessage(session,errMsg);
		// //4、根据session对象获取邮件传输对象Transport
		// Transport transport = session.getTransport();
		// //设置发件人的账户名和密码
		// transport.connect(senderAccount, senderPassword);
		// //发送邮件，并发送到所有收件人地址，message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
		// transport.sendMessage(msg,msg.getAllRecipients());
		//
		// //如果只想发送给指定的人，可以如下写法
		// //transport.sendMessage(msg, new Address[]{new
		// InternetAddress("xxx@qq.com")});
		//
		// //5、关闭邮件连接
		// transport.close();
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	/**
	 * 获得创建一封邮件的实例对象
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
	//// fileList.add("F:\\djysdk\\svn库维护规则.txt");
	//// fileList.add("F:\\djysdk\\源码维护人列表.txt");
	// //创建一封邮件的实例对象
	// MimeMessage msg = new MimeMessage(session);
	// MimeMultipart mp = new MimeMultipart();
	// //设置发件人地址
	// msg.setFrom(new InternetAddress(senderAddress));
	// /**
	// * 设置收件人地址（可以增加多个收件人、抄送、密送），即下面这一行代码书写多行
	// * MimeMessage.RecipientType.TO:发送
	// * MimeMessage.RecipientType.CC：抄送
	// * MimeMessage.RecipientType.BCC：密送
	// */
	// msg.setRecipient(MimeMessage.RecipientType.TO,new
	// InternetAddress(recipientAddress));
	// //设置邮件主题
	// SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");//设置日期格式
	// msg.setSubject("["+df.format(new
	// Date())+"]服务器编译结果自动发送:","UTF-8");//$NON-NLS-1$ //$NON-NLS-2$
	// //设置邮件正文
	// BodyPart bp = new MimeBodyPart();
	//
	// bp.setContent("以下target编译出错: \n"+errMsg,
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
	// // msg.setContent("简单的纯文本邮件！", "text/html;charset=UTF-8");
	// //设置邮件的发送时间,默认立即发送
	// msg.setSentDate(new Date());
	// msg.saveChanges();
	// return msg;
	// }
}