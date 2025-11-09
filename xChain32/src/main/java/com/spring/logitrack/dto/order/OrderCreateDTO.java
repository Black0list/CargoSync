package com.spring.logitrack.dto.order;

import com.spring.logitrack.entity.enums.BackorderStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderCreateDTO {
    @NotNull
    private String type;
    @NotNull
    private Long productId;
    @Min(1)
    private int qty;
    @Min(0)
    private int extraQty;
    @NotNull
    private LocalDateTime createdAt = LocalDateTime.now();
    @NotNull
    private BackorderStatus status;
    private Long salesOrderId;  // for backorder
}
