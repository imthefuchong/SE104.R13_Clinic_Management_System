package com.example.service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.Properties;

public class EmailService {
    private static final String USERNAME;
    private static final String PASSWORD;

    static {
        Properties config = new Properties();
        try (InputStream input = EmailService.class.getResourceAsStream("/email.properties")) {
            if (input == null) {
                throw new RuntimeException("Không tìm thấy file email.properties trong resources!");
            }
            config.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }

        USERNAME = config.getProperty("email.username");
        PASSWORD = config.getProperty("email.password");
    }

    public static void sendOTP(String toEmail, String otp) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.starttls.enable", true);
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(USERNAME));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Xác thực OTP");
        message.setText("Mã OTP của bạn là: " + otp);

        Transport.send(message);
    }
}
