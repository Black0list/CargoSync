package com.spring.logitrack.dto.warehouse;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WarehouseCreateDTO {
    @NotBlank
    private String code;

    @NotBlank
    private String name;

    @NotBlank
    private String location;

    @NotNull
    private Long managerId;

    private boolean active = true;
}
