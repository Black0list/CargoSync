package com.spring.logitrack.controller;

import com.spring.logitrack.dto.inventory.InventoryCreateDTO;
import com.spring.logitrack.dto.inventory.InventoryResponseDTO;
import com.spring.logitrack.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'ADMIN')")
public class InventoryController {

    private final InventoryService service;

    @PostMapping
    public ResponseEntity<InventoryResponseDTO> create(@Valid @RequestBody InventoryCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponseDTO>> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryResponseDTO> update(@PathVariable Long id, @RequestBody InventoryCreateDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<InventoryResponseDTO> patch(@PathVariable Long id, @RequestParam Long adjust) {
        return ResponseEntity.ok(service.adjust(id, adjust));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
