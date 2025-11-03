package com.spring.logitrack.mapper;

import com.spring.logitrack.dto.inventory.InventoryCreateDTO;
import com.spring.logitrack.dto.inventory.InventoryResponseDTO;
import com.spring.logitrack.entity.Inventory;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    Inventory toEntity(InventoryCreateDTO dto);

    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "productId", source = "product.id")
    InventoryResponseDTO toResponse(Inventory entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patch(@MappingTarget Inventory inventory, InventoryCreateDTO dto);
}
