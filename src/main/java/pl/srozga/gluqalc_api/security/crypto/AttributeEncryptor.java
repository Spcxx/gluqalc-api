package pl.srozga.gluqalc_api.security.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.srozga.gluqalc_api.security.crypto.base.BaseEncryptor;

@Component
@Converter
public class AttributeEncryptor extends BaseEncryptor implements AttributeConverter<String, String> {
    public AttributeEncryptor(@Value("${app.security.db-encryption.key}") String secret) throws Exception {
        super(secret);
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return encryptInternal(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return decryptInternal(dbData);
    }
}
