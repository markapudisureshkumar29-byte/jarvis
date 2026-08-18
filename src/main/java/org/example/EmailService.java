package org.example;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    private final String host;
    private final String port;
    private final String username;
    private final String password;

    public EmailService() {
        this.host = System.getenv("SMTP_HOST");
        this.port = System.getenv("SMTP_PORT");
        this.username = System.getenv("SMTP_USER");
        this.password = System.getenv("SMTP_PASS");
    }

    public boolean isConfigured() {
        return host != null && !host.isBlank()
                && port != null && !port.isBlank()
                && username != null && !username.isBlank()
                && password != null;
    }

    public String sendEmail(String to, String subject, String body) {
        if (!isConfigured()) {
            return "Email is not configured. Set SMTP_HOST, SMTP_PORT, SMTP_USER, and SMTP_PASS environment variables.";
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.ssl.trust", host);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
            return "Email sent successfully to " + to + ".";
        } catch (MessagingException e) {
            return "Failed to send email: " + e.getMessage();
        }
    }
}