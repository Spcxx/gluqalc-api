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
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "portion_changes")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortionChange {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "portion_id", nullable = false)
    private UUID portionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    private String name;
    @Column(precision = 10, scale = 1)
    private BigDecimal weightInGrams;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PortionChange other)) return false;
        return getId() != null && Objects.equals(getId(), other.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
