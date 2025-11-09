package com.spring.logitrack.entity;

import com.spring.logitrack.entity.OrderType;
import com.spring.logitrack.entity.enums.BackorderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class AbstractOrder implements OrderType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Min(1)
    @Column(nullable = false)
    private int qty;

    @Min(0)
    @Column(nullable = false)
    private int extraQty;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (createdAt == null)
            createdAt = LocalDateTime.now();
    }

    @OneToOne(mappedBy = "order")
    private PurchaseOrder purchaseOrder;

    public abstract BackorderStatus getStatus();
}
