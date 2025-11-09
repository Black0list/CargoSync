package com.spring.logitrack.dto.purchaseOrder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.spring.logitrack.dto.POLine.POLineCreateDTO;
import com.spring.logitrack.dto.order.OrderCreateDTO;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PurchaseOrderCreateDTO {

    @NotNull
    private Long supplierId;

    private List<POLineCreateDTO> lines;

    @JsonProperty("order")
    private Long orderId;
}
