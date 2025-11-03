package com.spring.logitrack.dto.warehouse;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WarehouseCreateDTO {
    @NotBlank
    private String code;

    @NotBlank
    private String name;

    @NotBlank
    private String location;
}
