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
		super("�����ʼ�");
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
    //�������˻���
    public static String senderAccount = "cjm@djyos.com"; //$NON-NLS-1$
    //�������˻�����
    public static String senderPassword = "Djy123456"; //$NON-NLS-1$
    
	public void send(String errMsg) {
		// ָ�������ʼ�������Ϊ localhost
//		String host = "localhost";

	  //1�������ʼ��������Ĳ�������
      Properties props = new Properties();
      //�����û�����֤��ʽ
      props.setProperty("mail.smtp.auth", "true"); //$NON-NLS-1$ //$NON-NLS-2$
      //���ô���Э��
      props.setProperty("mail.transport.protocol", "smtp");//$NON-NLS-1$ //$NON-NLS-2$
      //���÷����˵�SMTP��������ַ
      props.setProperty("mail.smtp.host", "smtp.exmail.qq.com");//$NON-NLS-1$ //$NON-NLS-2$

		// ��ȡĬ��session����
		Session session = Session.getDefaultInstance(props);
		 Message msg;
		try {
			msg = getMimeMessage(session,errMsg);
			 //4������session�����ȡ�ʼ��������Transport
	        Transport transport = session.getTransport();
	        //���÷����˵��˻���������
	        transport.connect(senderAccount, senderPassword);
	        //�����ʼ��������͵������ռ��˵�ַ��message.getAllRecipients() ��ȡ�������ڴ����ʼ�����ʱ��ӵ������ռ���, ������, ������
	        transport.sendMessage(msg,msg.getAllRecipients());
	         
	        //���ֻ�뷢�͸�ָ�����ˣ���������д��
	        //transport.sendMessage(msg, new Address[]{new InternetAddress("xxx@qq.com")});
	         
	        //5���ر��ʼ�����
	        transport.close();
			System.out.println("Sent message successfully....");
		} catch (Exception mex) {
			mex.printStackTrace();
		}
	}
	
	 /**
     * ��ô���һ���ʼ���ʵ������
     * @param session
	 * @param errMsg 
     * @return
     * @throws MessagingException
     * @throws AddressException
     */
    public static MimeMessage getMimeMessage(Session session, String errMsg) throws Exception{
        //����һ���ʼ���ʵ������
        MimeMessage msg = new MimeMessage(session);
        MimeMultipart mp = new MimeMultipart();
        //���÷����˵�ַ
        msg.setFrom(new InternetAddress(senderAddress));
        /**
         * �����ռ��˵�ַ���������Ӷ���ռ��ˡ����͡����ͣ�����������һ�д�����д����
         * MimeMessage.RecipientType.TO:����
         * MimeMessage.RecipientType.CC������
         * MimeMessage.RecipientType.BCC������
         */
        msg.setRecipient(MimeMessage.RecipientType.TO,new InternetAddress(recipientAddress));
        //�����ʼ�����
        msg.setSubject("�������Զ�����: "+errMsg,"UTF-8");//$NON-NLS-1$ //$NON-NLS-2$
        //�����ʼ�����
        BodyPart bp = new MimeBodyPart(); 
        bp.setContent("target�������: \n"+errMsg, "text/html;charset=utf-8");//$NON-NLS-1$ //$NON-NLS-2$
        mp.addBodyPart(bp);
        
        msg.setContent(mp);
        //�����ʼ��ķ���ʱ��,Ĭ����������
        msg.setSentDate(new Date());
        msg.saveChanges(); 
        return msg;
    }

}
