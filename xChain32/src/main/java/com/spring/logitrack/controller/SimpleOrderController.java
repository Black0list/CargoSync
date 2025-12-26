package com.spring.logitrack.controller;

import com.spring.logitrack.dto.order.OrderCreateDTO;
import com.spring.logitrack.dto.order.SimpleOrderResponseDTO;
import com.spring.logitrack.service.SimpleOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/simple-orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'ADMIN')")
public class SimpleOrderController {

    private final SimpleOrderService simpleOrderService;

    @PostMapping
    public ResponseEntity<SimpleOrderResponseDTO> create(@Valid @RequestBody OrderCreateDTO dto) {
        SimpleOrderResponseDTO response = simpleOrderService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SimpleOrderResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(simpleOrderService.getById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SimpleOrderResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody OrderCreateDTO dto
    ) {
        return ResponseEntity.ok(simpleOrderService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        simpleOrderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
