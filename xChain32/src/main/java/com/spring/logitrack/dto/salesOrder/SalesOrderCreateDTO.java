package com.spring.logitrack.dto.salesOrder;

import com.spring.logitrack.dto.salesOrderLine.SalesOrderLineCreateDTO;
import com.spring.logitrack.entity.enums.OrderStatus;
import com.spring.logitrack.entity.enums.ShipmentStatus;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SalesOrderCreateDTO {

    @NotNull
    private Long clientId;

    @NotNull
    private Long warehouseId;

    @NotNull
    @NotBlank
    private String country;

    @NotNull
    @NotBlank
    private String city;

    private OrderStatus status;

    @NotNull
    @NotBlank
    private String street;

    @NotNull
    @NotBlank
    private String zip;

    @NotNull
    private List<SalesOrderLineCreateDTO> lines;
}
