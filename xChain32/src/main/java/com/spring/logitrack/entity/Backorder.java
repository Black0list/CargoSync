package com.spring.logitrack.entity;

import com.spring.logitrack.entity.AbstractOrder;
import com.spring.logitrack.entity.enums.BackorderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "backorders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BackOrder extends AbstractOrder {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sales_order_id", nullable = false)
    private SalesOrder salesOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BackorderStatus status;

    @Override
    public BackorderStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "BackOrder{" +
                "salesOrder=" + salesOrder +
                ", status=" + status +
                '}';
    }
}

