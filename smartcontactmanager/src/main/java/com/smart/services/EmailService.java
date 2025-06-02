package com.smart.services;

import java.util.Properties;

import org.springframework.stereotype.Service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    public boolean sendEmail(String subject, String message, String to) {
        boolean success = false;

        String from = "pandeyswaraj926@gmail.com";

        // Variable for Gmail
        String host = "smtp.gmail.com"; // corrected the comma to dot

        // Get the system properties
        Properties properties = System.getProperties();
        System.out.println("PROPERTIES: " + properties);

        // Setting important information to properties object
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        // Step 1: Get the Session object
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // Use an app-specific password or enable less secure apps in Gmail
                return new PasswordAuthentication("pandeyswaraj926@gmail.com", "fjdp uiki bywu aqwz");
            }
        });

        session.setDebug(true); // Enable debug messages

        // Step 2: Compose the message
        MimeMessage mimeMessage = new MimeMessage(session);
        try {
            mimeMessage.setFrom(new InternetAddress(from));
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            mimeMessage.setSubject(subject);
          
            //mimeMessage.setText(message);
              mimeMessage.setContent(message,"text/html");
             
            
            // Step 3: Send the message
            Transport.send(mimeMessage);
            System.out.println("Mail sent successfully...");
            success = true;

        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return success;
    }
}
