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
            emailSender.send(message);
            log.info("Email sent to {} with subject: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            // We do not throw exception here to prevent rolling back the user creation
            // transaction just because email failed?
            // Depends on business requirement. Usually, email failure shouldn't block
            // account creation unless critical.
            // Requirement says "Envoi par email". If it fails, admin might need to resend
            // manually.
            // For now, we log error.
        }
    }
}
