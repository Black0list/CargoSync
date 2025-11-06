package com.spring.logitrack.dto.purchaseOrder;

import com.spring.logitrack.dto.POLine.POLineCreateDTO;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PurchaseOrderCreateDTO {
    @NotNull
    private Long supplierId;
    @NotEmpty
    private List<POLineCreateDTO> lines;
}
