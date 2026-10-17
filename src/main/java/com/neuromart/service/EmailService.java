package com.neuromart.service;

import com.neuromart.exception.HandleError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:ujjvalpateliya@gmail.com}")
    private String senderEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String messageText) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("NeuroMarth <" + senderEmail + ">");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(messageText);

            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Error sending email to {}: {}", to, e.getMessage());
            throw new HandleError("Email couldn't be sent , please try again later", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
