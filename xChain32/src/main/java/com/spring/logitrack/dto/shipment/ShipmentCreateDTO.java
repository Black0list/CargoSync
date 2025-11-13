package com.spring.logitrack.dto.shipment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShipmentCreateDTO {

    @NotNull
    private Long salesOrderId;

    @NotNull
    private Long warehouseId;

    @NotBlank
    private String carrier;

    @NotBlank
    private String trackingNumber;

    @NotBlank
    private String street;

    @NotBlank
    private String city;

    @NotBlank
    private String state;

    @NotBlank
    private String postalCode;

    @NotBlank
    private String country;
}
