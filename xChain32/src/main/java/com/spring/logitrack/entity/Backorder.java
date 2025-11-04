package com.spring.logitrack.entity;

import com.spring.logitrack.entity.enums.BackorderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "backorders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Backorder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sales_order_id", nullable = false)
    private SalesOrder salesOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Min(1)
    @Column(name = "qty_backordered", nullable = false)
    private int qtyBackordered;

    @Min(0)
    @Column(name = "extra_qty", nullable = false)
    private int extraQty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BackorderStatus status;
}
