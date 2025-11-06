package com.spring.logitrack.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "po_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class POLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Min(1)
    @Column(nullable = false)
    private int qty;

    @DecimalMin("0.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Override
    public String toString() {
        return "POLine{" +
                "id=" + id +
                ", purchaseOrder=" + purchaseOrder +
                ", product=" + product +
                ", qty=" + qty +
                ", price=" + price +
                '}';
    }
}
