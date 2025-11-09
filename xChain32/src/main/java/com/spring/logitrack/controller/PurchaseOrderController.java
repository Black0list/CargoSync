package com.spring.logitrack.controller;

import com.spring.logitrack.dto.purchaseOrder.PurchaseOrderCreateDTO;
import com.spring.logitrack.dto.purchaseOrder.PurchaseOrderResponseDTO;
import com.spring.logitrack.dto.salesOrder.SalesOrderResponseWithWarningsDTO;
import com.spring.logitrack.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService service;

    @PostMapping
    public ResponseEntity<PurchaseOrderResponseDTO> create(@Valid @RequestBody PurchaseOrderCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PostMapping("/backorder/{backorderId}/supplier/{supplierId}")
    public ResponseEntity<PurchaseOrderResponseDTO> createFromBackOrder(@PathVariable Long backorderId, @PathVariable Long supplierId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createFromBackOrder(backorderId, supplierId));
    }

    @GetMapping
    public ResponseEntity<List<PurchaseOrderResponseDTO>> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<PurchaseOrderResponseDTO>> bySupplier(@PathVariable Long supplierId) {
        return ResponseEntity.ok(service.findBySupplier(supplierId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            PurchaseOrderResponseDTO order = service.updateStatus(id, status);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
