package pl.srozga.gluqalc_api.component.passwordValidation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Slf4j
@Service
public class PwnedPasswordService {
    private final RestClient restClient;

    public PwnedPasswordService() {
        this.restClient = RestClient.create("https://api.pwnedpasswords.com");
    }

    public boolean isPasswordPwned(String password) {
        if (password == null || password.isBlank())
            return false;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
            String sha1Hash = HexFormat.of().formatHex(digest).toUpperCase();

            String prefix = sha1Hash.substring(0, 5);
            String suffix = sha1Hash.substring(5);

            String response = restClient.get()
                    .uri("/range/{prefix}", prefix)
                    .header("User-Agent", "GluQalc API")
                    .retrieve()
                    .body(String.class);

            if (response == null) return false;

            return response.lines()
                    .map(line -> line.split(":")[0])
                    .anyMatch(lineSuffix -> lineSuffix.equals(suffix));

        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-1 algorithm not found", e);
            return false;
        } catch (RestClientException e) {
            log.error("Failed to connect to HIBP API", e);
            return false;
        }
    }
}