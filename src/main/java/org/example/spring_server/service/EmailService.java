package org.example.spring_server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Async
    public void sendWelcomeEmail(String toEmail, String displayName) {
        String subject = "Welcome to SportT5!";
        String body = """
                <h2>Welcome, %s!</h2>
                <p>Your account has been created successfully.</p>
                <p>Start exploring music and create your playlists today.</p>
                <br>
                <p>The SportT5 Team</p>
                """.formatted(displayName != null ? displayName : toEmail);
        send(toEmail, subject, body);
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        String resetLink = frontendUrl + "/auth/reset-password?token=" + resetToken;
        String subject = "Reset your SportT5 password";
        String body = """
                <h2>Password Reset Request</h2>
                <p>We received a request to reset your password.</p>
                <p>Click the link below to reset it. This link expires in <strong>1 hour</strong>.</p>
                <br>
                <a href="%s" style="
                    background-color: #1DB954;
                    color: white;
                    padding: 12px 24px;
                    text-decoration: none;
                    border-radius: 4px;
                    display: inline-block;
                ">Reset Password</a>
                <br><br>
                <p>If you didn't request this, you can safely ignore this email.</p>
                <p>The SportT5 Team</p>
                """.formatted(resetLink);
        send(toEmail, subject, body);
    }

    @Async
    public void sendSubscriptionConfirmationEmail(String toEmail, String displayName,
                                                  String planType, String expiresAt) {
        String subject = "Subscription Confirmed — " + planType + " Plan";
        String body = """
                <h2>Subscription Confirmed!</h2>
                <p>Hi %s,</p>
                <p>Your <strong>%s</strong> subscription is now active.</p>
                <p>Your subscription renews on <strong>%s</strong>.</p>
                <br>
                <p>Enjoy unlimited music!</p>
                <p>The SportT5 Team</p>
                """.formatted(
                displayName != null ? displayName : toEmail,
                planType,
                expiresAt);
        send(toEmail, subject, body);
    }

    @Async
    public void sendSubscriptionCancelledEmail(String toEmail, String displayName) {
        String subject = "Subscription Cancelled";
        String body = """
                <h2>Subscription Cancelled</h2>
                <p>Hi %s,</p>
                <p>Your subscription has been cancelled successfully.</p>
                <p>You will be downgraded to the free plan at the end of your billing period.</p>
                <br>
                <p>We hope to see you again!</p>
                <p>The SportT5 Team</p>
                """.formatted(displayName != null ? displayName : toEmail);
        send(toEmail, subject, body);
    }

    private void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email to " + to, e);
        }
    }
}