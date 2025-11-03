package com.spring.logitrack.dto.salesOrderLine;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SalesOrderLineCreateDTO {

    @NotNull
    private Long productId;

    @Min(1)
    private int qtyOrdered;

    @NotNull
    private BigDecimal price;
}
