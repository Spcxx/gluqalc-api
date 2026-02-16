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
import java.util.*;

@Entity
@Table(name = "products")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(unique = true)
    private String barcode;

    @Column(nullable = false)
    private String name;

    private String brand;

    @Column(nullable = false, precision = 10, scale = 1)
    private BigDecimal energyKcal;

    @Column(nullable = false, precision = 10, scale = 1)
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

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private Set<ProductPortion> portions = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean published = false;

    @Column(nullable = false)
    private UUID createdBy;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product other)) return false;
        return getId() != null && Objects.equals(getId(), other.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
