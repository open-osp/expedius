package com.colcamex.www.util;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.*;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Email {
	
	private static Logger logger = LogManager.getLogger("Email");
	
	public static void sendEmail(String subject, String body, ExpediusProperties expediusProperties) {

		boolean emailOn = false;
		
		if(expediusProperties.containsKey("EMAIL_ON")) {
			String email = expediusProperties.getProperty("EMAIL_ON");
			if( email.equalsIgnoreCase("yes") ) {
				emailOn = true;
			}
		}
		
		if( (emailOn) && (body != null) && (subject != null) ) {
			
			Properties properties = System.getProperties();
			properties.setProperty("mail.smtp.host", expediusProperties.getProperty("EMAIL_HOST"));
	
			String to = expediusProperties.getProperty("ADMIN_EMAIL");
			String from = to;
			
			Session session = Session.getDefaultInstance(properties, null);
			session.setDebug(true);
			
			Message message = new MimeMessage(session);
			
			logger.debug("Sending email to: "+to);
			
			try {
				message.setFrom(new InternetAddress(from));
				message.setRecipients(Message.RecipientType.TO, new InternetAddress[]{new InternetAddress(to)});
				message.setSubject(subject);
				message.setText(body);
				
				Transport.send(message);
	
				
			} catch (AddressException e) {
				logger.log(Level.ERROR, "Address Exception for Email", e);
			} catch (MessagingException e) {
				logger.log(Level.ERROR, "Messaging Exception for Email", e);
			}
		} else {
			
			logger.info("Email server not set: no email was sent");
			//do nothing
		}
		
	}
	
	
}
