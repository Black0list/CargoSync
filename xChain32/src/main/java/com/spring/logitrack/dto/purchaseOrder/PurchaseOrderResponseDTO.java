package com.spring.logitrack.dto.purchaseOrder;

import com.spring.logitrack.dto.POLine.POLineResponseDTO;
import com.spring.logitrack.entity.enums.POStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseOrderResponseDTO {
    private Long id;
    private Long supplierId;
    private String supplierName;
    private POStatus status;
    private LocalDateTime createdAt;
    private List<POLineResponseDTO> lines;
}

