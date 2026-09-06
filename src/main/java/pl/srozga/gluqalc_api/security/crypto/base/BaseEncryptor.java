package pl.srozga.gluqalc_api.security.crypto.base;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;

public abstract class BaseEncryptor {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;
    private final Key key;

    public BaseEncryptor(@Value("${app.security.db-encryption.key}") String secret) throws Exception {
        byte[] keyBytes = java.security.MessageDigest.getInstance("SHA-256")
                .digest(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        this.key = new SecretKeySpec(keyBytes, ALGORITHM);
    }

    @SneakyThrows
    protected String encryptInternal(String attribute) {
        if (attribute == null)
            return null;

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        byte[] iv = new byte[IV_LENGTH];
        new java.security.SecureRandom().nextBytes(iv);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);

        byte[] encryptedBytes = cipher.doFinal(attribute.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        byte[] combined = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

        return java.util.Base64.getEncoder().encodeToString(combined);
    }

    @SneakyThrows
    protected String decryptInternal(String dbData) {
        if (dbData == null)
            return null;

        byte[] combined = Base64.getDecoder().decode(dbData);
        if (combined.length < IV_LENGTH)
            throw new IllegalArgumentException("Encrypted data is too short to contain an IV.");

        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(combined, 0, iv, 0, IV_LENGTH);

        int encryptedBytesLength = combined.length - IV_LENGTH;
        byte[] encryptedBytes = new byte[encryptedBytesLength];
        System.arraycopy(combined, IV_LENGTH, encryptedBytes, 0, encryptedBytesLength);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

        return new String(cipher.doFinal(encryptedBytes), StandardCharsets.UTF_8);
    }
}
