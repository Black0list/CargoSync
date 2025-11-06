package com.spring.logitrack.entity;

import com.spring.logitrack.entity.AbstractOrder;
import com.spring.logitrack.entity.enums.BackorderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "simple_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SimpleOrder extends AbstractOrder {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BackorderStatus status;

    @Override
    public BackorderStatus getStatus() {
        return status;
    }
}
