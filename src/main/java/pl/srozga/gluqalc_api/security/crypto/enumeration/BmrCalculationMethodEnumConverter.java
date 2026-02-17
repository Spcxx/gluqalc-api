package pl.srozga.gluqalc_api.security.crypto.enumeration;

import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.srozga.gluqalc_api.common.BmrCalculationMethod;
import pl.srozga.gluqalc_api.security.crypto.base.BaseEnumEncryptor;

@Component
@Converter
public class BmrCalculationMethodEnumConverter extends BaseEnumEncryptor<BmrCalculationMethod> {
    public BmrCalculationMethodEnumConverter(@Value("${app.security.db-encryption.key}") String secretKey) throws Exception {
        super(secretKey, BmrCalculationMethod.class);
    }
}
