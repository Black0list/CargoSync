package com.spring.logitrack.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductCreateDTO {

    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    @NotBlank
    private String sku;

    @DecimalMin("0.0")
    @NotNull
    private BigDecimal price;

    @NotBlank
    private String unit;

    @NotBlank
    private String imageUrl;
}
