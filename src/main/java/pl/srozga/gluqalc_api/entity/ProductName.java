package pl.srozga.gluqalc_api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pl.srozga.gluqalc_api.common.ProductNameSource;
import pl.srozga.gluqalc_api.common.ProductNameType;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "product_names")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductName {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductNameType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductNameSource source;

    @Column(nullable = false)
    private boolean approved;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
}
