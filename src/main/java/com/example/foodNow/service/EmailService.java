package com.example.foodNow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Sends a simple text email.
     *
     * @param to      Recipient email address
     * @param subject Email subject
     * @param text    Email body text
     */
    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail); // Note: Some SMTP servers ignore this and use the authenticated user
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            log.info("Attempting to send email to: {}", to);
            emailSender.send(message);
            log.info("Email sent successfully to {}", to);
            // System.out for visibility if logger is configured to hide info
            System.out.println("SUCCESS: Email sent to " + to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            e.printStackTrace(); // Print full stack trace for debugging
            System.err.println("ERROR: Failed to send email to " + to + ". Reason: " + e.getMessage());
        }
    }

    public void sendPasswordChangeEmail(String to, String userName, String newPassword) {
        String subject = "Your FoodNow account password has been changed";
        String text = String.format("Hello %s,\n\n" +
                "Your FoodNow account password has been changed by an administrator.\n\n" +
                "Your new password is: %s\n\n" +
                "Please log in and change this password as soon as possible for security reasons.\n\n" +
                "If you did not request this change, please contact support immediately.\n\n" +
                "FoodNow Team", userName, newPassword);

        sendSimpleMessage(to, subject, text);
    }
}
