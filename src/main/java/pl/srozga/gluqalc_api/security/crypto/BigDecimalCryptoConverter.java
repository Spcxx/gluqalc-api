package pl.srozga.gluqalc_api.security.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.srozga.gluqalc_api.security.crypto.base.BaseEncryptor;

import java.math.BigDecimal;

@Component
@Converter
public class BigDecimalCryptoConverter extends BaseEncryptor implements AttributeConverter<BigDecimal, String> {
    public BigDecimalCryptoConverter(@Value("${app.security.db-encryption.key}") String secretKey) throws Exception {
        super(secretKey);
    }

    @Override
    public String convertToDatabaseColumn(BigDecimal s) {
        return s == null ? null : encryptInternal(s.toString());
    }

    @Override
    public BigDecimal convertToEntityAttribute(String s) {
        String decrypted = decryptInternal(s);
        return decrypted == null ? null : new BigDecimal(decrypted);
    }
}
