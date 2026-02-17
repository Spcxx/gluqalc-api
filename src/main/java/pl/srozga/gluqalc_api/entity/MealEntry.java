package pl.srozga.gluqalc_api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "meal_entries")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_category_id", nullable = false)
    private MealCategory mealCategory;

    @Column(nullable = false)
    private LocalDate consumedAt;
    @Column(nullable = false)
    private LocalTime consumedAtTime;

    // product snapshot
    private UUID productId;
    private String productName;
    private String brand;
    private String barcode;

    // portion snapshot
    private UUID portionId;
    private String portionName;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal weightInGrams;
    @Column(precision = 10, scale = 2)
    private BigDecimal portionUnitWeight;
    @Column(precision = 10, scale = 2)
    private BigDecimal portionQuantity;

    // nutrition snapshot
    @Column(precision = 10, scale = 1)
    private BigDecimal energyKcal;
    @Column(precision = 10, scale = 1)
    private BigDecimal carbohydrates;
    @Column(precision = 10, scale = 1)
    private BigDecimal sugars;
    @Column(precision = 10, scale = 1)
    private BigDecimal fat;
    @Column(precision = 10, scale = 1)
    private BigDecimal saturatedFat;
    @Column(precision = 10, scale = 1)
    private BigDecimal protein;
    @Column(precision = 10, scale = 1)
    private BigDecimal fiber;
    @Column(precision = 10, scale = 2)
    private BigDecimal salt;
    private Integer glycemicIndex;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MealEntry other)) return false;
        return getId() != null && Objects.equals(getId(), other.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
