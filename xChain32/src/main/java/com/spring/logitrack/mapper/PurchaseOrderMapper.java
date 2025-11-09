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

    // ✅ Create new PurchaseOrder from DTO
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

    // ✅ PATCH existing PurchaseOrder (partial update)
    public void patch(PurchaseOrder existing, PurchaseOrderCreateDTO dto) {
        // Update supplier handled in service (don’t touch here)

        // If new lines provided, replace them
        if (dto.getLines() != null && !dto.getLines().isEmpty()) {
            List<POLine> newLines = new ArrayList<>();
            for (POLineCreateDTO lineDTO : dto.getLines()) {
                POLine line = new POLine();
                line.setQty(lineDTO.getQty());
                newLines.add(line);
            }
            existing.setLines(newLines);
        }

        // Keep status, createdAt, and order intact
    }

    // ✅ Convert Entity → Response DTO
    public PurchaseOrderResponseDTO toResponse(PurchaseOrder entity) {
        PurchaseOrderResponseDTO dto = new PurchaseOrderResponseDTO();
        dto.setId(entity.getId());
        dto.setSupplierId(entity.getSupplier().getId());
        dto.setSupplierName(entity.getSupplier().getName());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());

        // Map lines
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

        // Map the order (SimpleOrder or BackOrder)
        if (entity.getOrder() != null) {
            dto.setOrder(toOrderResponse(entity.getOrder()));
        }

        return dto;
    }

    // ✅ Convert polymorphic order entity → response DTO
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
