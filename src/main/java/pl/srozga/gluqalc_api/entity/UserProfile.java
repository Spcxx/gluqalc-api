package pl.srozga.gluqalc_api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pl.srozga.gluqalc_api.common.BmrCalculationMethod;
import pl.srozga.gluqalc_api.common.UserGender;
import pl.srozga.gluqalc_api.security.crypto.AttributeEncryptor;
import pl.srozga.gluqalc_api.security.crypto.BigDecimalCryptoConverter;
import pl.srozga.gluqalc_api.security.crypto.IntegerCryptoConverter;
import pl.srozga.gluqalc_api.security.crypto.enumeration.BmrCalculationMethodEnumConverter;
import pl.srozga.gluqalc_api.security.crypto.enumeration.UserGenderEnumConverter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    // sensitive data
    @Convert(converter = UserGenderEnumConverter.class)
    private UserGender gender;
    @Convert(converter = BigDecimalCryptoConverter.class)
    private BigDecimal weightInKg;
    @Convert(converter = BigDecimalCryptoConverter.class)
    private BigDecimal heightInCm;
    @Convert(converter = AttributeEncryptor.class)
    private String birthDate;
    @Convert(converter = BigDecimalCryptoConverter.class)

    private BigDecimal physicalActivityLevel;
    @Convert(converter = IntegerCryptoConverter.class)
    private Integer kcalGoalDifference;
    @Convert(converter = AttributeEncryptor.class)
    @Column(name = "weekly_kcal_distribution")
    private String weeklyKcalDistributionJson;
    @Convert(converter = BigDecimalCryptoConverter.class)
    private BigDecimal bodyFatPercentage;
    @Convert(converter = BmrCalculationMethodEnumConverter.class)
    private BmrCalculationMethod bmrMethod = BmrCalculationMethod.MIFFLIN_ST_JEOR;
    @Convert(converter = AttributeEncryptor.class)
    @Column(name = "macro_strategy")
    private String macroStrategyJson;

    @Convert(converter = BigDecimalCryptoConverter.class)
    private BigDecimal insulinSensitivityFactor;
    @Convert(converter = BigDecimalCryptoConverter.class)
    private BigDecimal insulinFatProteinRatio;
    @Convert(converter = AttributeEncryptor.class)
    @Column(name = "hourly_carb_ratio")
    private String hourlyCarbRatioJson;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserProfile other)) return false;
        return getId() != null && Objects.equals(getId(), other.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
