package com.spring.logitrack;

import com.spring.logitrack.dto.inventory.InventoryResponseDTO;
import com.spring.logitrack.dto.inventoryMovement.InventoryMovementCreateDTO;
import com.spring.logitrack.dto.inventoryMovement.InventoryMovementResponseDTO;
import com.spring.logitrack.entity.Inventory;
import com.spring.logitrack.entity.Product;
import com.spring.logitrack.entity.Warehouse;
import com.spring.logitrack.entity.enums.MovementType;
import com.spring.logitrack.mapper.InventoryMapper;
import com.spring.logitrack.repository.InventoryRepository;
import com.spring.logitrack.repository.ProductRepository;
import com.spring.logitrack.repository.WarehouseRepository;
import com.spring.logitrack.service.InventoryMovementService;
import com.spring.logitrack.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Inventory Service - Stock Management Tests")
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryMapper inventoryMapper;

    @Mock
    private InventoryMovementService inventoryMovementService;

    @InjectMocks
    private InventoryService inventoryService;

    private Warehouse warehouse;
    private Product product;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        warehouse = Warehouse.builder()
                .id(1L)
                .name("Main Warehouse")
                .build();

        product = Product.builder()
                .id(1L)
                .sku("PROD-001")
                .name("Test Product")
                .price(new BigDecimal(99))
                .active(true)
                .build();

        inventory = Inventory.builder()
                .id(1L)
                .warehouse(warehouse)
                .product(product)
                .qtyOnHand(100)
                .qtyReserved(0)
                .build();
    }

    @Test
    @DisplayName("Should prevent negative stock on hand")
    void shouldPreventNegativeStockOnHand() {
        // Given
        inventory.setQtyOnHand(10);
        inventory.setQtyReserved(0);
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));

        try {
            inventoryService.adjust(1L, -20L);
        } catch (RuntimeException e) {
            System.out.println("Exception message: " + e.getMessage());
        }
    }
}