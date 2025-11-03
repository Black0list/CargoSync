package com.spring.logitrack.service;

import com.spring.logitrack.dto.inventoryMovement.InventoryMovementCreateDTO;
import com.spring.logitrack.dto.inventoryMovement.InventoryMovementResponseDTO;
import com.spring.logitrack.entity.Inventory;
import com.spring.logitrack.entity.InventoryMovement;
import com.spring.logitrack.mapper.InventoryMovementMapper;
import com.spring.logitrack.repository.InventoryMovementRepository;
import com.spring.logitrack.repository.InventoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryMovementService {

    private final InventoryMovementRepository movementRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryMovementMapper mapper;

    public InventoryMovementResponseDTO create(InventoryMovementCreateDTO dto) {
        Inventory inventory = inventoryRepository.findById(dto.getInventoryId())
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found"));

        InventoryMovement movement = mapper.toEntity(dto);
        movement.setInventory(inventory);

        inventoryRepository.save(inventory);
        return mapper.toResponse(movementRepository.save(movement));
    }

    @Transactional(readOnly = true)
    public List<InventoryMovementResponseDTO> list() {
        return movementRepository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryMovementResponseDTO> findByInventory(Long inventoryId) {
        return movementRepository.findByInventory_Id(inventoryId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public void delete(Long id) {
        InventoryMovement movement = movementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Movement not found"));
        movementRepository.delete(movement);
    }
}
