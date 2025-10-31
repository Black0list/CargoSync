package com.spring.logitrack.dto.product;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductResponseDTO {
    private Long id;
    private String sku;
    private String name;
    private BigDecimal price;
    private String unit;
    private String imageUrl;
}
