package com.spring.logitrack.repository;

import com.spring.logitrack.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Inventory findInventoryByProduct_IdAndWarehouse_Id(Long productId, Long warehouseId);
    Optional<Inventory> findOneByProduct_IdAndQtyOnHandIsGreaterThanEqualAndWarehouse_IdNot(Long productId, int qty, Long warehouseId);
}
