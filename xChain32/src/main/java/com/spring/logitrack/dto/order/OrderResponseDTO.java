package com.spring.logitrack.dto.order;

import com.spring.logitrack.entity.enums.BackorderStatus;
import lombok.Data;

@Data
public class OrderResponseDTO {
    private Long id;
    private String type; // "SimpleOrder" or "BackOrder"
    private Long productId;
    private int qty;
    private int extraQty;
    private BackorderStatus status;
    private Long salesOrderId; // null for SimpleOrder
}
