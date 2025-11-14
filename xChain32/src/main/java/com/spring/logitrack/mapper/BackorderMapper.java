package com.spring.logitrack.mapper;

import com.spring.logitrack.dto.order.OrderCreateDTO;
import com.spring.logitrack.dto.order.OrderResponseDTO;
import com.spring.logitrack.entity.Backorder;
import com.spring.logitrack.entity.OrderType;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BackorderMapper {
    Backorder toEntity(OrderCreateDTO dto);

    @Mapping(target = "salesOrderId", source = "salesOrder.id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "type", expression = "java(entity.getClass().getSimpleName())")
    OrderResponseDTO toResponse(Backorder entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patch(@MappingTarget Backorder entity, OrderCreateDTO dto);
}
