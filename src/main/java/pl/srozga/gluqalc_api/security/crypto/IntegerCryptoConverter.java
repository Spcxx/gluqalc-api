package pl.srozga.gluqalc_api.security.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.srozga.gluqalc_api.security.crypto.base.BaseEncryptor;

@Component
@Converter
public class IntegerCryptoConverter extends BaseEncryptor implements AttributeConverter<Integer, String>  {
    public IntegerCryptoConverter(@Value("${app.security.db-encryption.key}") String secretKey) throws Exception {
        super(secretKey);
    }

    @Override
    public String convertToDatabaseColumn(Integer s) {
        return s == null ? null : encryptInternal(s.toString());
    }

    @Override
    public Integer convertToEntityAttribute(String s) {
        String decrypted = decryptInternal(s);
        return decrypted == null ? null : Integer.valueOf(decrypted);
    }
}
