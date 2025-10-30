package com.spring.logitrack.dto;

import com.spring.logitrack.entity.enums.ShipmentStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShipmentResponseDTO {
    private Long id;
    private String carrier;
    private String trackingNumber;
    private ShipmentStatus status;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}
