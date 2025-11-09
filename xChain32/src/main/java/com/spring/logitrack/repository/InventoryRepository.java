package com.spring.logitrack.repository;

import com.spring.logitrack.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findTopByProduct_IdAndWarehouse_IdOrderByIdDesc(Long productId, Long warehouseId);
    Optional<Inventory> findOneByProduct_IdAndQtyOnHandIsGreaterThanEqualAndWarehouse_IdNot(Long productId, int qty, Long warehouseId);
    @Query(value = """
    SELECT *
    FROM inventories i
    WHERE i.product_id = :productId
      AND (i.qty_on_hand - i.qty_reserved) >= :qty
      AND i.warehouse_id <> :warehouseId
    LIMIT 1
""", nativeQuery = true)
    Optional<Inventory> findAvailableInventoryNative(
            @Param("productId") Long productId,
            @Param("qty") int qty,
            @Param("warehouseId") Long warehouseId);


}
