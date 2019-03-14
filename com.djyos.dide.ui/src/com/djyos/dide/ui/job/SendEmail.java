package com.djyos.dide.ui.job;

import java.util.Date;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class SendEmail extends Job{
	
	static String recipientAddress;
	String email_msg;
	
	public SendEmail(String email_msg, String email) {
		super("发送邮件");
		// TODO Auto-generated constructor stub
		this.recipientAddress = email;
		this.email_msg = email_msg;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		
		send(email_msg);
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		monitor.done();
		return Status.OK_STATUS;
	}
	
	@Override
	public boolean belongsTo(Object family) {
		return ResourcesPlugin.FAMILY_MANUAL_BUILD == family;
	}
	
	public static String senderAddress = "cjm@djyos.com"; //$NON-NLS-1$
    //发件人账户名
    public static String senderAccount = "cjm@djyos.com"; //$NON-NLS-1$
    //发件人账户密码
    public static String senderPassword = "Djy123456"; //$NON-NLS-1$
    
	public void send(String errMsg) {
		// 指定发送邮件的主机为 localhost
//		String host = "localhost";

	  //1、连接邮件服务器的参数配置
      Properties props = new Properties();
      //设置用户的认证方式
      props.setProperty("mail.smtp.auth", "true"); //$NON-NLS-1$ //$NON-NLS-2$
      //设置传输协议
      props.setProperty("mail.transport.protocol", "smtp");//$NON-NLS-1$ //$NON-NLS-2$
      //设置发件人的SMTP服务器地址
      props.setProperty("mail.smtp.host", "smtp.exmail.qq.com");//$NON-NLS-1$ //$NON-NLS-2$

		// 获取默认session对象
		Session session = Session.getDefaultInstance(props);
		 Message msg;
		try {
			msg = getMimeMessage(session,errMsg);
			 //4、根据session对象获取邮件传输对象Transport
	        Transport transport = session.getTransport();
	        //设置发件人的账户名和密码
	        transport.connect(senderAccount, senderPassword);
	        //发送邮件，并发送到所有收件人地址，message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
	        transport.sendMessage(msg,msg.getAllRecipients());
	         
	        //如果只想发送给指定的人，可以如下写法
	        //transport.sendMessage(msg, new Address[]{new InternetAddress("xxx@qq.com")});
	         
	        //5、关闭邮件连接
	        transport.close();
			System.out.println("Sent message successfully....");
		} catch (Exception mex) {
			mex.printStackTrace();
		}
	}
	
	 /**
     * 获得创建一封邮件的实例对象
     * @param session
	 * @param errMsg 
     * @return
     * @throws MessagingException
     * @throws AddressException
     */
    public static MimeMessage getMimeMessage(Session session, String errMsg) throws Exception{
        //创建一封邮件的实例对象
        MimeMessage msg = new MimeMessage(session);
        MimeMultipart mp = new MimeMultipart();
        //设置发件人地址
        msg.setFrom(new InternetAddress(senderAddress));
        /**
         * 设置收件人地址（可以增加多个收件人、抄送、密送），即下面这一行代码书写多行
         * MimeMessage.RecipientType.TO:发送
         * MimeMessage.RecipientType.CC：抄送
         * MimeMessage.RecipientType.BCC：密送
         */
        msg.setRecipient(MimeMessage.RecipientType.TO,new InternetAddress(recipientAddress));
        //设置邮件主题
        msg.setSubject("服务器自动发送: "+errMsg,"UTF-8");//$NON-NLS-1$ //$NON-NLS-2$
        //设置邮件正文
        BodyPart bp = new MimeBodyPart(); 
        bp.setContent("target编译出错: \n"+errMsg, "text/html;charset=utf-8");//$NON-NLS-1$ //$NON-NLS-2$
        mp.addBodyPart(bp);
        
        msg.setContent(mp);
        //设置邮件的发送时间,默认立即发送
        msg.setSentDate(new Date());
        msg.saveChanges(); 
        return msg;
    }

}
