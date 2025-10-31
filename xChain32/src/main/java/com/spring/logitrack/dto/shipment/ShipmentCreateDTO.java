package com.spring.logitrack.dto.shipment;

import com.spring.logitrack.entity.enums.ShipmentStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShipmentCreateDTO {
    @NotBlank
    private String carrier;

    @NotBlank
    private String trackingNumber;

    private ShipmentStatus status;

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

    private Long salesOrderId;
    private Long warehouseId;
}
