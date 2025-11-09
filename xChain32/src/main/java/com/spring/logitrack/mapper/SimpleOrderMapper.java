package com.spring.logitrack.mapper;

import com.spring.logitrack.dto.order.OrderCreateDTO;
import com.spring.logitrack.dto.order.OrderResponseDTO;
import com.spring.logitrack.dto.order.SimpleOrderResponseDTO;
import com.spring.logitrack.entity.SimpleOrder;
import org.mapstruct.*;

@Mapper(componentModel = "spring", imports = java.time.LocalDateTime.class)
public interface SimpleOrderMapper {

    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    SimpleOrder toEntity(OrderCreateDTO dto);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "type", expression = "java(entity.getClass().getSimpleName())")
    SimpleOrderResponseDTO toResponse(SimpleOrder entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patch(@MappingTarget SimpleOrder entity, OrderCreateDTO dto);
}
