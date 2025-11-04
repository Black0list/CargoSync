package com.spring.logitrack.service;

import com.spring.logitrack.dto.inventory.InventoryCreateDTO;
import com.spring.logitrack.dto.inventory.InventoryResponseDTO;
import com.spring.logitrack.dto.inventoryMovement.InventoryMovementCreateDTO;
import com.spring.logitrack.entity.Inventory;
import com.spring.logitrack.entity.Product;
import com.spring.logitrack.entity.Warehouse;
import com.spring.logitrack.entity.enums.MovementType;
import com.spring.logitrack.mapper.InventoryMapper;
import com.spring.logitrack.repository.InventoryRepository;
import com.spring.logitrack.repository.ProductRepository;
import com.spring.logitrack.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

    private final InventoryRepository repository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final InventoryMapper mapper;
    private final InventoryMovementService inventoryMovementService;

    public InventoryResponseDTO create(InventoryCreateDTO dto) {
        Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found"));
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Inventory entity = mapper.toEntity(dto);
        entity.setWarehouse(warehouse);
        entity.setProduct(product);

        return mapper.toResponse(repository.save(entity));
    }

    public List<InventoryResponseDTO> list() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    public InventoryResponseDTO getById(Long id) {
        Inventory entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));
        return mapper.toResponse(entity);
    }

    public InventoryResponseDTO update(Long id, InventoryCreateDTO dto) {
        Inventory entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));
        Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found"));
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        entity.setWarehouse(warehouse);
        entity.setProduct(product);
        entity.setQtyOnHand(dto.getQtyOnHand());
        entity.setQtyReserved(dto.getQtyReserved());

        if(dto.getQtyOnHand() <= 0 || dto.getQtyReserved() <= 0){
            throw new RuntimeException("Cant provide a Zero negative quantity");
        }

        return mapper.toResponse(repository.save(entity));
    }

    public InventoryResponseDTO adjust(Long id,  Long adjust) {
        Inventory entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));

        if(adjust >= 0) throw new RuntimeException("Adjustment quantity must be negative, like : -2");

        boolean valid = entity.getQtyOnHand() + adjust >= entity.getQtyReserved();

        int qtyAllowed = entity.getQtyOnHand() - entity.getQtyReserved();

        if(!valid) throw new RuntimeException("Invalid Adjustment, you can only adjust in min : -"+qtyAllowed);
        entity.setQtyOnHand(entity.getQtyOnHand() + adjust.intValue());

        InventoryMovementCreateDTO inventoryMvtDTO = new InventoryMovementCreateDTO();
        inventoryMvtDTO.setInventoryId(entity.getId());
        inventoryMvtDTO.setType(MovementType.ADJUSTMENT);
        inventoryMvtDTO.setQty(Math.abs(adjust.intValue()));


        inventoryMovementService.create(inventoryMvtDTO);
        return mapper.toResponse(repository.save(entity));
    }


    public void delete(Long id) {
        Inventory entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));
        repository.delete(entity);
    }

    public Optional<Inventory> getHelperInventory(Long id, int qty, Long WarehouseId) {
        return repository.findOneByProduct_IdAndQtyOnHandIsGreaterThanEqualAndWarehouse_IdNot(id, qty, WarehouseId);
    }
}
