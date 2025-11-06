package com.spring.logitrack.dto.backorder;

import com.spring.logitrack.entity.enums.BackorderStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BackorderCreateDTO {

    @NotNull
    private Long salesOrderId;

    @NotNull
    private Long productId;

    @Min(1)
    private Integer qty;

    @Min(0)
    private Integer extraQty;

    @NotNull
    private LocalDateTime createdAt = LocalDateTime.now();

    @NotNull
    private BackorderStatus status;
}
