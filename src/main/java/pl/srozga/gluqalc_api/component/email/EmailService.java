package pl.srozga.gluqalc_api.component.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String emailFrom;

    @Async
    public void sendVerificationEmail(String to, String token) {
        sendEmail(to, "Email Verification Code", "Use the code below to verify your email address:", token);
    }

    @Async
    public void sendPasswordResetEmail(String to, String token) {
        sendEmail(to, "Password Reset Code", "Use the code below to reset your password:", token);
    }

    @Async
    public void sendEmailChangeConfirmationEmail(String to, String token) {
        sendEmail(to, "Confirm Email Change", "Use the code below to confirm your new email address:", token);
    }

    @Async
    public void sendAccountDeletionEmail(String to, String token) {
        sendEmail(to, "Account Deletion Request", "Use the code below to confirm the deletion of your account. This action cannot be undone:", token);
    }

    @Async
    public void sendSecurityAlertEmail(String to, String message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            String htmlContent = String.format("""
                    <div style="font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ffcccc; border-radius: 8px; max-width: 600px; margin: 0 auto;">
                          <h2 style="color: #d9534f; text-align: center;">Security Alert</h2>
                          <p style="color: #333; font-size: 16px; line-height: 1.5;">%s</p>
                          <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                          <p style="color: #777; font-size: 12px; text-align: center;">If you did not request this change, please contact our support team immediately and reset your password.</p>
                    </div>
                    """, message);

            helper.setText(htmlContent, true);
            helper.setTo(to);
            helper.setSubject("Security Alert: Your account details were changed");
            helper.setFrom(emailFrom);

            mailSender.send(mimeMessage);
            log.info("Sent security alert email to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send security alert email to {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendAccountDeletedConfirmationEmail(String to) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            String htmlContent = """
                    <div style="font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd; border-radius: 8px; max-width: 600px; margin: 0 auto; text-align: center;">
                          <h2 style="color: #555;">Account Successfully Deleted</h2>
                          <p style="color: #333; font-size: 16px; line-height: 1.5; margin-top: 20px;">Your Gluqalc account and all associated data have been scheduled for permanent deletion.</p>
                          <p style="color: #555; font-size: 14px; margin-top: 15px;">We are sorry to see you go! You are always welcome back if you change your mind.</p>
                          <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                          <p style="color: #999; font-size: 12px;">If you did not authorize this action, please contact our support immediately.</p>
                    </div>
                    """;

            helper.setText(htmlContent, true);
            helper.setTo(to);
            helper.setSubject("Your Gluqalc Account Has Been Deleted");
            helper.setFrom(emailFrom);

            mailSender.send(mimeMessage);
            log.info("Sent account deletion final confirmation email to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send account deletion final confirmation email to {}: {}", to, e.getMessage());
        }
    }

    private void sendEmail(String to, String subject, String message, String token) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            String htmlContent = String.format("""
                    <div style="font-family: Arial, sans-serif; text-align: center; padding: 20px;">
                          <h2 style="color: #333;">%s</h2>
                          <p style="color: #555;">%s</p>
                          <h1 style="color: #007BFF; letter-spacing: 5px; font-size: 32px; background: #f4f4f4; display: inline-block; padding: 10px 20px; border-radius: 8px;">%s</h1>
                          <p style="color: #777; font-size: 12px; margin-top: 20px;">This code is valid for 15 minutes.</p>
                          <p style="color: #777; font-size: 10px; margin-top: 10px;">If you did not request this email, please ignore it.</p>
                    </div>
                    """, subject, message, token);

            helper.setText(htmlContent, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom(emailFrom);

            mailSender.send(mimeMessage);
            log.info("Sent email to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
