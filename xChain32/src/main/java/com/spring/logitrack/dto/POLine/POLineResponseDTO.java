package com.spring.logitrack.dto.POLine;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class POLineResponseDTO {
    private Long id;
    private Long productId;
    private String productName;
    private int qty;
    private BigDecimal price;
}
