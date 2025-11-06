package com.spring.logitrack.dto.backorder;

import com.spring.logitrack.entity.enums.BackorderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BackorderResponseDTO {
    private Long id;
    private Long salesOrderId;
    private Long productId;
    private int qty;
    private int extraQty;
    private LocalDateTime createdAt;
    private BackorderStatus status;
}
