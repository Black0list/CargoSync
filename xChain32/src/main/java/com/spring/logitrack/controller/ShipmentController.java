package com.spring.logitrack.controller;

import com.spring.logitrack.dto.shipment.ShipmentCreateDTO;
import com.spring.logitrack.dto.shipment.ShipmentResponseDTO;
import com.spring.logitrack.entity.enums.ShipmentStatus;
import com.spring.logitrack.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService service;

    @PostMapping
    public ResponseEntity<ShipmentResponseDTO> create(@Valid @RequestBody ShipmentCreateDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ShipmentResponseDTO>> list() {
        return ResponseEntity.ok(service.list());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ShipmentResponseDTO> update(@PathVariable Long id, @RequestBody ShipmentCreateDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ShipmentResponseDTO> updateStatus(@PathVariable Long id, @RequestParam ShipmentStatus status) {
        return ResponseEntity.ok(service.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
