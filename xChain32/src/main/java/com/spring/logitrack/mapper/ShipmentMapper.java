package com.spring.logitrack.mapper;

import com.spring.logitrack.dto.shipment.ShipmentCreateDTO;
import com.spring.logitrack.dto.shipment.ShipmentResponseDTO;
import com.spring.logitrack.entity.Shipment;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ShipmentMapper {

    Shipment toEntity(ShipmentCreateDTO dto);

    ShipmentResponseDTO toResponse(Shipment entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patch(@MappingTarget Shipment shipment, ShipmentCreateDTO dto);
}
