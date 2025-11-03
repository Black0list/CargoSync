package com.spring.logitrack.dto.salesOrderLine;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SalesOrderLineResponseDTO {
    private String productName;
    private int qtyOrdered;
    private int qtyReserved;
    private BigDecimal price;
}
