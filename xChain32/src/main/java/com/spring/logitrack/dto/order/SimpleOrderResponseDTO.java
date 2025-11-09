package com.spring.logitrack.dto.order;

import com.spring.logitrack.entity.enums.BackorderStatus;
import lombok.Data;

@Data
public class SimpleOrderResponseDTO {
    private Long id;
    private String type;
    private Long productId;
    private int qty;
    private int extraQty;
    private BackorderStatus status;
}
