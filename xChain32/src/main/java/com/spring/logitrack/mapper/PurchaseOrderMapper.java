package com.spring.logitrack.mapper;

import com.spring.logitrack.dto.POLine.POLineCreateDTO;
import com.spring.logitrack.dto.POLine.POLineResponseDTO;
import com.spring.logitrack.dto.order.OrderResponseDTO;
import com.spring.logitrack.dto.purchaseOrder.PurchaseOrderCreateDTO;
import com.spring.logitrack.dto.purchaseOrder.PurchaseOrderResponseDTO;
import com.spring.logitrack.entity.*;
import com.spring.logitrack.entity.enums.POStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PurchaseOrderMapper {

    public PurchaseOrder toEntity(PurchaseOrderCreateDTO dto, Supplier supplier) {
        PurchaseOrder po = new PurchaseOrder();
        po.setSupplier(supplier);
        po.setStatus(POStatus.APPROVED);
        po.setCreatedAt(java.time.LocalDateTime.now());

        List<POLine> lines = new ArrayList<>();
        if (dto.getLines() != null) {
            for (POLineCreateDTO lineDTO : dto.getLines()) {
                POLine line = new POLine();
                line.setQty(lineDTO.getQty());
                lines.add(line);
            }
        }
        po.setLines(lines);
        return po;
    }

    public void patch(PurchaseOrder existing, PurchaseOrderCreateDTO dto) {
        if (dto.getLines() != null && !dto.getLines().isEmpty()) {
            List<POLine> newLines = new ArrayList<>();
            for (POLineCreateDTO lineDTO : dto.getLines()) {
                POLine line = new POLine();
                line.setQty(lineDTO.getQty());
                newLines.add(line);
            }
            existing.setLines(newLines);
        }

    }

    public PurchaseOrderResponseDTO toResponse(PurchaseOrder entity) {
        PurchaseOrderResponseDTO dto = new PurchaseOrderResponseDTO();
        dto.setId(entity.getId());
        dto.setSupplierId(entity.getSupplier().getId());
        dto.setSupplierName(entity.getSupplier().getName());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());

        List<POLineResponseDTO> lineDTOs = new ArrayList<>();
        for (POLine line : entity.getLines()) {
            POLineResponseDTO l = new POLineResponseDTO();
            l.setId(line.getId());
            l.setProductId(line.getProduct().getId());
            l.setProductName(line.getProduct().getName());
            l.setQty(line.getQty());
            l.setPrice(line.getPrice());
            lineDTOs.add(l);
        }
        dto.setLines(lineDTOs);

        if (entity.getOrder() != null) {
            dto.setOrder(toOrderResponse(entity.getOrder()));
        }

        return dto;
    }

    private OrderResponseDTO toOrderResponse(AbstractOrder order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setProductId(order.getProduct().getId());
        dto.setQty(order.getQty());
        dto.setExtraQty(order.getExtraQty());
        dto.setStatus(order.getStatus());

        if (order instanceof BackOrder bo) {
            dto.setType("BackOrder");
            dto.setSalesOrderId(bo.getSalesOrder() != null ? bo.getSalesOrder().getId() : null);
        } else if (order instanceof SimpleOrder) {
            dto.setType("SimpleOrder");
        }

        return dto;
    }
}
