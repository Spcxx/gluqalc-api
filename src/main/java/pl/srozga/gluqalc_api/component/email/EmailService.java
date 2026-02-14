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

    @Value("${app.api-url}")
    private String apiUrl;

    @Value("${app.email-from}")
    private String emailFrom;

    @Async
    public void sendVerificationEmail(String to, String token) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            String htmlContent = String.format("""
                    <div style="font-family: Arial, sans-serif; text-align: center; padding: 20px;">
                          <h2 style="color: #333;">E-mail verification</h2>
                          <p style="color: #555;">Use the code below to verify your account:</p>
                          <h1 style="color: #007BFF; letter-spacing: 5px; font-size: 32px; background: #f4f4f4; display: inline-block; padding: 10px 20px; border-radius: 8px;">%s</h1>
                          <p style="color: #777; font-size: 12px; margin-top: 20px;">This code is valid for 15 minutes.</p>
                          <p style="color: #777; font-size: 10px; margin-top: 10px;">If you did not request this email, please ignore it.</p>
                    </div>
                    """, token);

            helper.setText(htmlContent, true);
            helper.setTo(to);
            helper.setSubject("E-mail verification code - GluQalc");
            helper.setFrom(emailFrom);

            mailSender.send(mimeMessage);
            log.info("Sent verification email to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to {}: {}", to, e.getMessage());
        }
    }
}
