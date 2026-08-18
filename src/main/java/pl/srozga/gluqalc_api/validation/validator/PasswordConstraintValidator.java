package pl.srozga.gluqalc_api.validation.validator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import org.springframework.core.io.ClassPathResource;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import pl.srozga.gluqalc_api.component.passwordValidation.PwnedPasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;
import pl.srozga.gluqalc_api.validation.ValidPassword;

import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {
    private final PwnedPasswordService pwnedPasswordService;
    private final Set<String> blacklist = new HashSet<>();
    private static final Pattern REPETITION_PATTERN = Pattern.compile("(.)\\1{3,}");

    @PostConstruct
    public void initBlacklist() {
        blacklist.add("gluqalc");
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("passwords/blacklist.txt").getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null)
                blacklist.add(line.trim().toLowerCase());
        } catch (Exception e) {
            log.warn("Failed to load password blacklist from file.", e);
        }
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.length() < 8)
            return false;

        if (blacklist.contains(password.toLowerCase())) {
            buildViolation(context, "Password is too common.");
            return false;
        }

        if (REPETITION_PATTERN.matcher(password).find()) {
            buildViolation(context, "Password contains too many repeating characters.");
            return false;
        }

        if (pwnedPasswordService.isPasswordPwned(password)) {
            buildViolation(context, "This password has appeared in a data breach.");
            return false;
        }

        return true;
    }

    private void buildViolation(ConstraintValidatorContext context, String customMessage) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(customMessage)
                .addConstraintViolation();
    }
}