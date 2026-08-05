package com.finpilot.service;

public interface EmailService {

    void sendPasswordResetEmail(String toEmail, String firstName, String resetLink);

    void sendWelcomeEmail(String toEmail, String firstName);
}
