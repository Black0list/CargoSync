package com.spring.logitrack.dto.backorder;

import com.spring.logitrack.entity.enums.BackorderStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BackorderCreateDTO {

    @NotNull
    private Long salesOrderId;

    @NotNull
    private Long productId;

    @Min(1)
    private int qtyBackordered;

    @Min(10)
    private int extraQty;

    @NotNull
    private BackorderStatus status;
}
