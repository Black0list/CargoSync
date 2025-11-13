package com.spring.logitrack.dto.shipment;

import com.spring.logitrack.entity.enums.ShipmentStatus;
import lombok.Data;

@Data
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
    private Long salesOrderId;
    private Long warehouseId;
}
