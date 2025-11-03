package com.spring.logitrack.dto.salesOrder;

import com.spring.logitrack.dto.salesOrderLine.SalesOrderLineResponseDTO;
import com.spring.logitrack.entity.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class SalesOrderResponseDTO {
    private Long id;
    private String clientName;
    private String warehouseName;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private List<SalesOrderLineResponseDTO> lines;
}
