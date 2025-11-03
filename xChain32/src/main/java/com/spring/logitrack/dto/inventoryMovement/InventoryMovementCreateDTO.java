package com.spring.logitrack.dto.inventoryMovement;

import com.spring.logitrack.entity.enums.MovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryMovementCreateDTO {

    @NotNull
    private Long inventoryId;

    @NotNull
    private MovementType type;

    @Min(1)
    private int qty;
}
