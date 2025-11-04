package com.spring.logitrack.dto.backorder;

import com.spring.logitrack.entity.enums.BackorderStatus;
import lombok.Data;

@Data
public class BackorderResponseDTO {
    private Long id;
    private Long salesOrderId;
    private Long productId;
    private int qtyBackordered;
    private int extraQty;
    private BackorderStatus status;
}
