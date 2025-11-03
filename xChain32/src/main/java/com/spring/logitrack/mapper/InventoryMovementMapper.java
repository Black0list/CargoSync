package com.spring.logitrack.mapper;

import com.spring.logitrack.dto.inventoryMovement.InventoryMovementCreateDTO;
import com.spring.logitrack.dto.inventoryMovement.InventoryMovementResponseDTO;
import com.spring.logitrack.entity.Inventory;
import com.spring.logitrack.entity.InventoryMovement;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface InventoryMovementMapper {

    @Mapping(target = "inventory", expression = "java(new Inventory(dto.getInventoryId(), null, null, 0, 0, null))")
    @Mapping(target = "occurredAt", expression = "java(java.time.LocalDateTime.now())")
    InventoryMovement toEntity(InventoryMovementCreateDTO dto);

    @Mapping(target = "inventoryId", source = "inventory.id")
    InventoryMovementResponseDTO toResponse(InventoryMovement entity);
}
