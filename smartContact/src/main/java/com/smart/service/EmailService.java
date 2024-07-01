package com.smart.service;

import java.util.Properties;

import org.springframework.stereotype.Service;


import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;

@Service
public class EmailService {

	public boolean sendEmail(String subject, String message, String to) {
		boolean f=false;
		String from="goyalparul254@gmail.com";
		
		//variable for email
		String host ="smtp.gmail.com";
		
		
		//get the system properties
		Properties properties = System.getProperties();
		System.out.println("PROPERTIES " + properties);
		
		
		//setting important information to properties object
		
		//host set
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");
		
		//step1: to get the session object...
		
		Session session = Session.getInstance(properties, new Authenticator(){
		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication("goyalparul254@gmail.com","tonj mjal xcez zafu");
		
		}
	});
	session.setDebug(true);
	
	//step2: compose the message [text,multi media]
	MimeMessage m= new MimeMessage(session);
	
	try {
		
		//from email
		m.setFrom(from);
		
		//adding recepient to message
		m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		
		 //adding subject to message
		m.setSubject(subject);
		
		//adding text to message
		//m.setText(message);
		
		m.setContent(message,"text/html");
		//send
	
	//step3: send the message using Transport class
	Transport.send(m);
	
	System.out.println("Sent success....................");
	f=true;
	}catch (Exception e) {
		
		e.printStackTrace();
	}
	return f;
	}
}
