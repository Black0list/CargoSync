package com.spring.logitrack.mapper;

import com.spring.logitrack.dto.warehouse.WarehouseCreateDTO;
import com.spring.logitrack.dto.warehouse.WarehouseResponseDTO;
import com.spring.logitrack.entity.Warehouse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {

    @Mapping(target = "code", expression = "java(dto.getCode().toUpperCase())")
    Warehouse toEntity(WarehouseCreateDTO dto);

    @Mapping(source = "manager.name", target = "manager")
    WarehouseResponseDTO toResponse(Warehouse entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "code", expression = "java(dto.getCode() != null ? dto.getCode().toUpperCase() : warehouse.getCode())")
    void patch(@MappingTarget Warehouse warehouse, WarehouseCreateDTO dto);
}
