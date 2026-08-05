package com.finpilot.service.impl;

import com.finpilot.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    @Async
    public void sendPasswordResetEmail(String toEmail, String firstName, String resetLink) {
        String subject = "FinPilot - Reset Your Password";
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
                    <h2 style="color:#1a73e8;">FinPilot</h2>
                    <p>Hi %s,</p>
                    <p>We received a request to reset your password. Click the button below to choose a new one.
                    This link will expire in 30 minutes.</p>
                    <p style="margin: 24px 0;">
                        <a href="%s" style="background:#1a73e8;color:#fff;padding:12px 24px;
                        border-radius:6px;text-decoration:none;">Reset Password</a>
                    </p>
                    <p>If you did not request this, you can safely ignore this email.</p>
                    <p>— The FinPilot Team</p>
                </div>
                """.formatted(firstName, resetLink);

        send(toEmail, subject, body);
    }

    @Override
    @Async
    public void sendWelcomeEmail(String toEmail, String firstName) {
        String subject = "Welcome to FinPilot!";
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
                    <h2 style="color:#1a73e8;">Welcome to FinPilot, %s!</h2>
                    <p>Your account has been created successfully. Start tracking your income,
                    expenses, budgets and savings goals all in one place.</p>
                    <p>— The FinPilot Team</p>
                </div>
                """.formatted(firstName);

        send(toEmail, subject, body);
    }

    private void send(String toEmail, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }
}
