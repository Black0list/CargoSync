package com.spring.logitrack.dto.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryCreateDTO {

    @NotNull
    private Long warehouseId;

    @NotNull
    private Long productId;

    @Min(0)
    private int qtyOnHand;

    @Min(0)
    private int qtyReserved;
}
