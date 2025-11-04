package com.spring.logitrack.service;

import com.spring.logitrack.dto.inventory.InventoryCreateDTO;
import com.spring.logitrack.dto.inventory.InventoryResponseDTO;
import com.spring.logitrack.entity.Inventory;
import com.spring.logitrack.entity.Product;
import com.spring.logitrack.entity.Warehouse;
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

        if(dto.getQtyOnHand() < 0 || dto.getQtyReserved() < 0){
            throw new RuntimeException("Cant provide a negative quantity");
        }

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
