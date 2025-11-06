package com.spring.logitrack.mapper;

import com.spring.logitrack.dto.POLine.POLineResponseDTO;
import com.spring.logitrack.dto.purchaseOrder.PurchaseOrderCreateDTO;
import com.spring.logitrack.dto.purchaseOrder.PurchaseOrderResponseDTO;
import com.spring.logitrack.entity.POLine;
import com.spring.logitrack.entity.PurchaseOrder;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PurchaseOrderMapper {

    @Mapping(source = "supplierId", target = "supplier.id")
    @Mapping(target = "status", expression = "java(com.spring.logitrack.entity.enums.POStatus.APPROVED)")
    PurchaseOrder toEntity(PurchaseOrderCreateDTO dto);

    @Mapping(source = "supplier.id",   target = "supplierId")
    @Mapping(source = "supplier.name", target = "supplierName")
    @Mapping(target = "lines", source = "lines")
    PurchaseOrderResponseDTO toResponse(PurchaseOrder entity);

    @Mapping(source = "product.id",   target = "productId")
    @Mapping(source = "product.name", target = "productName")
    POLineResponseDTO toResponse(POLine line);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patch(@MappingTarget PurchaseOrder entity, PurchaseOrderCreateDTO dto);
}
