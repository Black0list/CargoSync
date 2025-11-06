package com.spring.logitrack.entity;

import com.spring.logitrack.entity.enums.BackorderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class AbstractOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Min(1)
    @Column(name = "qty", nullable = false)
    private int qty;

    @Min(0)
    @Column(name = "extra_qty", nullable = false)
    private int extraQty;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public abstract BackorderStatus getStatus();

    @Override
    public String toString() {
        return "AbstractOrder{" +
                "id=" + id +
                ", product=" + product +
                ", qty=" + qty +
                ", extraQty=" + extraQty +
                ", createdAt=" + createdAt +
                '}';
    }
}
