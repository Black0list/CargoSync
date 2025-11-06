package com.spring.logitrack.dto.POLine;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class POLineCreateDTO {
    @NotNull
    private Long productId;
    @Min(1)
    private int qty;
}


