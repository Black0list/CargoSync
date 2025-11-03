package com.spring.logitrack.mapper;

import com.spring.logitrack.dto.salesOrder.SalesOrderCreateDTO;
import com.spring.logitrack.dto.salesOrder.SalesOrderResponseDTO;
import com.spring.logitrack.dto.salesOrderLine.SalesOrderLineResponseDTO;
import com.spring.logitrack.entity.SalesOrder;
import com.spring.logitrack.entity.SalesOrderLine;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SalesOrderMapper {

    SalesOrderMapper INSTANCE = Mappers.getMapper(SalesOrderMapper.class);

    @Mapping(source = "client.name", target = "clientName")
    @Mapping(source = "warehouse.name", target = "warehouseName")
    @Mapping(source = "lines", target = "lines")
    SalesOrderResponseDTO toResponse(SalesOrder entity);

    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "qtyOrdered", target = "qtyOrdered")
    @Mapping(source = "qtyReserved", target = "qtyReserved")
    @Mapping(source = "price", target = "price")
    SalesOrderLineResponseDTO toResponse(SalesOrderLine line);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patch(@MappingTarget SalesOrder entity, SalesOrderCreateDTO dto);
}
