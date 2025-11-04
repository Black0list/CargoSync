package com.spring.logitrack.dto.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryUpdateDTO {

    @NotNull
    private Long warehouseId;

    @NotNull
    private Long productId;

    @Min(-1)
    private int qtyOnHand;
}
