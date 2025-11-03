package com.spring.logitrack.dto.inventory;

import lombok.Data;

@Data
public class InventoryResponseDTO {
    private Long id;
    private Long warehouseId;
    private Long productId;
    private int qtyOnHand;
    private int qtyReserved;
}
