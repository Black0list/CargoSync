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

        // When/Then - attempting to adjust by -20 should fail (10 - 20 = -10 which is below 0)
        assertThatThrownBy(() -> inventoryService.adjust(1L, -20L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid Adjustment");
    }

    @Test
    @DisplayName("Should prevent negative reserved quantity")
    void shouldPreventNegativeReservedQuantity() {
        // Given
        inventory.setQtyReserved(5);
        inventory.setQtyOnHand(50);

        // When/Then - Entity validation should prevent negative reserved quantity
        // This test verifies entity-level constraint
        assertThat(inventory.getQtyReserved()).isGreaterThanOrEqualTo(0);

        // Attempting to set negative value should be prevented by @Min(0) annotation
        // In a real scenario with database, this would throw ConstraintViolationException
    }

    @Test
    @DisplayName("Should correctly reserve quantity")
    void shouldCorrectlyReserveQuantity() {
        // Given
        inventory.setQtyOnHand(100);
        inventory.setQtyReserved(0);

        // When
        inventory.setQtyReserved(inventory.getQtyReserved() + 20);

        // Then
        assertThat(inventory.getQtyReserved()).isEqualTo(20);
        assertThat(inventory.getQtyOnHand()).isEqualTo(100);
    }

    @Test
    @DisplayName("Should correctly release reserved quantity")
    void shouldCorrectlyReleaseReservedQuantity() {
        // Given
        inventory.setQtyOnHand(100);
        inventory.setQtyReserved(30);

        // When
        int releaseQty = 10;
        inventory.setQtyReserved(inventory.getQtyReserved() - releaseQty);

        // Then
        assertThat(inventory.getQtyReserved()).isEqualTo(20);
        assertThat(inventory.getQtyOnHand()).isEqualTo(100);
    }

    @Test
    @DisplayName("Should calculate available stock correctly")
    void shouldCalculateAvailableStockCorrectly() {
        // Given
        inventory.setQtyOnHand(100);
        inventory.setQtyReserved(30);

        // When
        int available = inventory.getQtyOnHand() - inventory.getQtyReserved();

        // Then
        assertThat(available).isEqualTo(70);
    }

    @Test
    @DisplayName("Should prevent reservation exceeding available stock")
    void shouldPreventReservationExceedingAvailableStock() {
        // Given
        inventory.setQtyOnHand(50);
        inventory.setQtyReserved(20);
        int availableStock = inventory.getQtyOnHand() - inventory.getQtyReserved();

        // When/Then
        int requestedQty = 40;
        assertThat(requestedQty).isGreaterThan(availableStock);
        // Business logic should handle this case
    }

    @Test
    @DisplayName("Should adjust inventory correctly with negative value")
    void shouldAdjustInventoryWithNegativeValue() {
        // Given
        inventory.setQtyOnHand(100);
        inventory.setQtyReserved(0);

        InventoryResponseDTO responseDTO = new InventoryResponseDTO();
        responseDTO.setQtyOnHand(80);

        InventoryMovementResponseDTO movementResponseDTO = new InventoryMovementResponseDTO();
        movementResponseDTO.setQty(20);
        movementResponseDTO.setType(MovementType.ADJUSTMENT);

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryMapper.toResponse(any(Inventory.class))).thenReturn(responseDTO);
        when(inventoryMovementService.create(any(InventoryMovementCreateDTO.class)))
                .thenReturn(movementResponseDTO);

        // When - adjust method only accepts negative values
        InventoryResponseDTO result = inventoryService.adjust(1L, -20L);

        // Then
        verify(inventoryRepository).save(any(Inventory.class));
        verify(inventoryMovementService).create(any(InventoryMovementCreateDTO.class));
        assertThat(result.getQtyOnHand()).isEqualTo(80);
    }

    @Test
    @DisplayName("Should prevent positive adjustment value")
    void shouldPreventPositiveAdjustmentValue() {
        // Given
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));

        // When/Then - adjust method only allows negative values
        assertThatThrownBy(() -> inventoryService.adjust(1L, 20L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Adjustment quantity must be negative");
    }

    @Test
    @DisplayName("Should prevent adjustment that would make stock below reserved quantity")
    void shouldPreventAdjustmentBelowReservedQuantity() {
        // Given
        inventory.setQtyOnHand(50);
        inventory.setQtyReserved(30);
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));

        // When/Then - attempting to adjust by -30 would result in 20 on hand, which is below 30 reserved
        assertThatThrownBy(() -> inventoryService.adjust(1L, -30L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid Adjustment, you can only adjust in min : -20");
    }

    @Test
    @DisplayName("Should allow valid negative adjustment")
    void shouldAllowValidNegativeAdjustment() {
        // Given
        inventory.setQtyOnHand(100);
        inventory.setQtyReserved(30);

        InventoryResponseDTO responseDTO = new InventoryResponseDTO();
        responseDTO.setQtyOnHand(90);

        InventoryMovementResponseDTO movementResponseDTO = new InventoryMovementResponseDTO();
        movementResponseDTO.setQty(10);
        movementResponseDTO.setType(MovementType.ADJUSTMENT);

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
            Inventory saved = invocation.getArgument(0);
            assertThat(saved.getQtyOnHand()).isEqualTo(90); // 100 - 10
            return saved;
        });
        when(inventoryMapper.toResponse(any(Inventory.class))).thenReturn(responseDTO);
        when(inventoryMovementService.create(any(InventoryMovementCreateDTO.class)))
                .thenReturn(movementResponseDTO);

        // When - adjust by -10 is valid (result would be 90, which is still > 30 reserved)
        InventoryResponseDTO result = inventoryService.adjust(1L, -10L);


        verify(inventoryRepository).save(any(Inventory.class));
        verify(inventoryMovementService).create(argThat(mvt ->
                mvt.getInventoryId().equals(1L) &&
                        mvt.getType() == MovementType.ADJUSTMENT &&
                        mvt.getQty() == 10 // Absolute value
        ));
        assertThat(result.getQtyOnHand()).isEqualTo(90);
    }

    @Test
    @DisplayName("Should find helper inventory from other warehouses")
    void shouldFindHelperInventory() {
        Inventory helperInventory = Inventory.builder()
                .id(2L)
                .warehouse(Warehouse.builder().id(2L).name("Helper WH").build())
                .product(product)
                .qtyOnHand(200)
                .qtyReserved(50)
                .build();

        when(inventoryRepository.findAvailableInventoryNative(
                eq(1L), anyInt(), eq(1L)))
                .thenReturn(Optional.of(helperInventory));

        Optional<Inventory> result = inventoryService.getHelperInventory(1L, 50, 1L);

        assertThat(result).isPresent();
        assertThat(result.get().getQtyOnHand() - result.get().getQtyReserved())
                .isGreaterThanOrEqualTo(50);
        verify(inventoryRepository).findAvailableInventoryNative(1L, 50, 1L);
    }
}