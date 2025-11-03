package com.spring.logitrack.repository;

import com.spring.logitrack.entity.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
    List<InventoryMovement> findByInventory_Id(Long inventoryId);
}
