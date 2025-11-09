package com.spring.logitrack.mapper;

import com.spring.logitrack.dto.supplier.SupplierCreateDTO;
import com.spring.logitrack.dto.supplier.SupplierResponseDTO;
import com.spring.logitrack.entity.Supplier;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    Supplier toEntity(SupplierCreateDTO dto);

    SupplierResponseDTO toResponse(Supplier entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patch(@MappingTarget Supplier entity, SupplierCreateDTO dto);
}
