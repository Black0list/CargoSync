package com.spring.logitrack.controller;

import com.spring.logitrack.dto.backorder.BackorderCreateDTO;
import com.spring.logitrack.dto.backorder.BackorderResponseDTO;
import com.spring.logitrack.service.BackorderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/backorders")
@RequiredArgsConstructor
public class BackorderController {

    private final BackorderService service;

    @PostMapping
    public ResponseEntity<BackorderResponseDTO> create(@Valid @RequestBody BackorderCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<BackorderResponseDTO>> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/order/{salesOrderId}")
    public ResponseEntity<List<BackorderResponseDTO>> findByOrder(@PathVariable Long salesOrderId) {
        return ResponseEntity.ok(service.findByOrder(salesOrderId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BackorderResponseDTO> update(@PathVariable Long id, @RequestBody BackorderCreateDTO dto) {
        return ResponseEntity.ok(service.updateStatus(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
