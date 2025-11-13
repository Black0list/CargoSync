package com.spring.logitrack.dto.product;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ProductCreateDTO {

    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    @NotBlank
    @Size(min = 10)
    private String description;

    @NotBlank
    private String sku;

    @DecimalMin("0.0")
    @NotNull
    private BigDecimal price;

    @NotNull
    private Long warehouseId;

    @NotBlank
    private String unit;

    @Column(nullable = false)
    private boolean active = true;

    @NotEmpty(message = "At least one image URL is required")
    private List<MultipartFile> imageUrls;

}
