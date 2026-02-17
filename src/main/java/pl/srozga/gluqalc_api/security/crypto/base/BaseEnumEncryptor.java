package pl.srozga.gluqalc_api.security.crypto.base;

import jakarta.persistence.AttributeConverter;

public abstract class BaseEnumEncryptor<E extends Enum<E>> extends BaseEncryptor implements AttributeConverter<E, String> {
    private final Class<E> enumClass;

    protected BaseEnumEncryptor(String secretKey, Class<E> enumClass) throws Exception {
        super(secretKey);
        this.enumClass = enumClass;
    }

    @Override
    public String convertToDatabaseColumn(E attribute) {
        return attribute == null ? null : encryptInternal(attribute.name());
    }

    @Override
    public E convertToEntityAttribute(String dbData) {
        if (dbData == null)
            return null;

        String decrypted = decryptInternal(dbData);

        try {
            return Enum.valueOf(enumClass, decrypted);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
