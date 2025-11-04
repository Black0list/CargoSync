package com.spring.logitrack.mapper;

import com.spring.logitrack.dto.backorder.BackorderCreateDTO;
import com.spring.logitrack.dto.backorder.BackorderResponseDTO;
import com.spring.logitrack.entity.Backorder;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BackorderMapper {
    Backorder toEntity(BackorderCreateDTO dto);

    @Mapping(target = "salesOrderId", source = "salesOrder.id")
    @Mapping(target = "productId", source = "product.id")
    BackorderResponseDTO toResponse(Backorder entity);
}
