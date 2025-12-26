package com.spring.logitrack.dto.product;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ProductResponseDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private String unit;
    private List<String> imageUrls;
    private boolean active;
}
