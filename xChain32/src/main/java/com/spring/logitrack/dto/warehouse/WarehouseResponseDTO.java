package com.spring.logitrack.dto.warehouse;

import lombok.Data;

@Data
public class WarehouseResponseDTO {
    private Long id;
    private String code;
    private String name;
    private String location;
    private boolean active;
}
