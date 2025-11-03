package com.spring.logitrack.dto.inventoryMovement;

import com.spring.logitrack.entity.enums.MovementType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryMovementResponseDTO {
    private Long id;
    private Long inventoryId;
    private MovementType type;
    private int qty;
    private LocalDateTime occurredAt;
}
