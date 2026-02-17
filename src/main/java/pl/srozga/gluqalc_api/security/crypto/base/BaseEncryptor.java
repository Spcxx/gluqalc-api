package pl.srozga.gluqalc_api.security.crypto.base;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

public abstract class BaseEncryptor {
    private static final String ALGORITHM = "AES";
    private final Key key;

    public BaseEncryptor(@Value("${app.security.db-encryption.key}") String secret) throws Exception {
        this.key = new SecretKeySpec(secret.getBytes(), ALGORITHM);
    }

    @SneakyThrows
    protected String encryptInternal(String attribute) {
        if (attribute == null)
            return null;
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] bytes = cipher.doFinal(attribute.getBytes());
        return Base64.getEncoder().encodeToString(bytes);
    }

    @SneakyThrows
    protected String decryptInternal(String dbData) {
        if (dbData == null)
            return null;
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] bytes = Base64.getDecoder().decode(dbData);
        return new String(cipher.doFinal(bytes));
    }
}
