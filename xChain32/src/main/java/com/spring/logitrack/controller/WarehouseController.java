package com.spring.logitrack.controller;

import com.spring.logitrack.dto.warehouse.WarehouseCreateDTO;
import com.spring.logitrack.dto.warehouse.WarehouseResponseDTO;
import com.spring.logitrack.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'ADMIN')")
public class WarehouseController {

    private final WarehouseService service;

    @PostMapping
    public ResponseEntity<WarehouseResponseDTO> create(@Valid @RequestBody WarehouseCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<WarehouseResponseDTO>> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WarehouseResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WarehouseResponseDTO> update(@PathVariable Long id, @RequestBody WarehouseCreateDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
