package com.spring.logitrack.controller;

import com.spring.logitrack.dto.order.OrderCreateDTO;
import com.spring.logitrack.dto.order.OrderResponseDTO;
import com.spring.logitrack.service.BackorderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/backorders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'ADMIN')")
public class BackorderController {

    private final BackorderService service;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> create(@Valid @RequestBody OrderCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/order/{salesOrderId}")
    public ResponseEntity<List<OrderResponseDTO>> findByOrder(@PathVariable Long salesOrderId) {
        return ResponseEntity.ok(service.findByOrder(salesOrderId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> update(@PathVariable Long id, @RequestBody OrderCreateDTO dto) {
        return ResponseEntity.ok(service.updateStatus(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
