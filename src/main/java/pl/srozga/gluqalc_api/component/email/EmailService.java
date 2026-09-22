package pl.srozga.gluqalc_api.component.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String emailFrom;

    @Async
    public void sendVerificationEmail(String to, String token) {
        Context context = createBaseContext(
                "You have created a GluQalc account. Please use the code below to proceed.",
                "If you did not request this email, please ignore it."
        );

        setupCodeSection(context, "Your email verification code:", token, (int) EmailVerificationTokenService.TOKEN_EXPIRATION.toMinutes());

        sendGenericEmail(to, token + " - GluQalc Email Verification Code", context);
    }

    @Async
    public void sendPasswordResetEmail(String to, String token) {
        Context context = createBaseContext(
                "You have requested a password reset for your GluQalc account. Please use the code below to proceed.",
                "If you did not request a password reset, please ignore this email."
        );

        setupCodeSection(context, "Your password reset code:", token, (int) EmailVerificationTokenService.TOKEN_EXPIRATION.toMinutes());

        sendGenericEmail(to, token + " - GluQalc Password Reset Code", context);
    }

    @Async
    public void sendEmailChangeConfirmationEmail(String to, String token) {
        Context context = createBaseContext(
                "You have requested to change your email address. Please confirm this change using the code below.",
                "If you did not request this change, please secure your account immediately."
        );

        setupCodeSection(context, "Your confirmation code:", token, (int) EmailVerificationTokenService.TOKEN_EXPIRATION.toMinutes());

        sendGenericEmail(to, token + " - GluQalc Email Change Confirmation Code", context);
    }

    @Async
    public void sendAccountDeletionEmail(String to, String token) {
        Context context = createBaseContext(
                "You have requested to delete your GluQalc account. This action is irreversible. Use the code below to confirm.",
                "If you did not request account deletion, please contact our support team immediately."
        );

        setupCodeSection(context, "Your deletion confirmation code:", token, 15);

        sendGenericEmail(to, token + " - GluQalc Account Deletion Code", context);
    }

    @Async
    public void sendSecurityAlertEmail(String to, String alertMessage) {
        Context context = createBaseContext(
                "We have detected important security activity on your account that requires your attention.",
                "If you did not authorize this action, please contact our support team immediately and reset your password."
        );

        setupAlertSection(context, alertMessage);

        sendGenericEmail(to, "GluQalc Security Alert", context);
    }

    @Async
    public void sendAccountDeletedConfirmationEmail(String to) {
        Context context = createBaseContext(
                "Your GluQalc account and all associated data have been scheduled for permanent deletion.",
                "If you did not authorize this action, please contact our support immediately."
        );

        context.setVariable("showCode", false);
        context.setVariable("showAlert", false);

        sendGenericEmail(to, "GluQalc Account Deleted", context);
    }

    private Context createBaseContext(String message, String footerMessage) {
        Context context = new Context();
        context.setVariable("message", message);
        context.setVariable("footerMessage", footerMessage);
        return context;
    }

    private void setupCodeSection(Context context, String subMessage, String code, int validityMinutes) {
        context.setVariable("showCode", true);
        context.setVariable("showAlert", false);
        context.setVariable("subMessage", subMessage);
        context.setVariable("code", code);
        context.setVariable("validityMinutes", validityMinutes);
    }

    private void setupAlertSection(Context context, String alertMessage) {
        context.setVariable("showCode", false);
        context.setVariable("showAlert", true);
        context.setVariable("subMessage", alertMessage);
    }

    private void sendGenericEmail(String to, String subject, Context context) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String htmlContent = templateEngine.process("email", context);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom(emailFrom, "GluQalc");

            helper.setText(htmlContent, true);

            ClassPathResource logoImage = new ClassPathResource("static/logo.png");
            if (logoImage.exists())
                helper.addInline("logoImage", logoImage);
            else
                log.warn("Logo image not found at static/logo.png");

            mailSender.send(mimeMessage);
            log.info("Sent email [{}] to {}", subject, to);
        } catch (Exception e) {
            log.error("Failed to send email [{}] to {}: {}", subject, to, e.getMessage());
        }
    }
}