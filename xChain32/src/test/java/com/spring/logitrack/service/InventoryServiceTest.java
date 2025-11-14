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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


@ActiveProfiles("test")
class InventoryServiceTest {

    @Mock private InventoryRepository repository;
    @Mock private WarehouseRepository warehouseRepository;
    @Mock private ProductRepository productRepository;
    @Mock private InventoryMapper mapper;
    @Mock private InventoryMovementService inventoryMovementService;

    @InjectMocks private InventoryService service;

    private Inventory inventory;
    private Warehouse warehouse;
    private Product product;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        warehouse = new Warehouse();
        warehouse.setId(1L);
        product = new Product();
        product.setId(1L);
        inventory = new Inventory();
        inventory.setId(1L);
        inventory.setWarehouse(warehouse);
        inventory.setProduct(product);
        inventory.setQtyOnHand(10);
        inventory.setQtyReserved(2);
    }

    @Test
    @DisplayName("should create inventory successfully")
    void createInventory_success() {
        InventoryCreateDTO dto = new InventoryCreateDTO();
        dto.setWarehouseId(1L);
        dto.setProductId(1L);
        dto.setQtyOnHand(10);
        dto.setQtyReserved(0);

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(mapper.toEntity(dto)).thenReturn(inventory);
        when(repository.save(inventory)).thenReturn(inventory);
        when(mapper.toResponse(inventory)).thenReturn(new InventoryResponseDTO());

        InventoryResponseDTO result = service.create(dto);

        assertThat(result).isNotNull();
        verify(repository).save(inventory);
    }

    @Test
    @DisplayName("should throw when warehouse not found on create")
    void createInventory_warehouseNotFound() {
        InventoryCreateDTO dto = new InventoryCreateDTO();
        dto.setWarehouseId(99L);
        when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Warehouse not found");
    }

    @Test
    @DisplayName("should list all inventories")
    void listInventories() {
        when(repository.findAll()).thenReturn(List.of(inventory));
        when(mapper.toResponse(inventory)).thenReturn(new InventoryResponseDTO());
        List<InventoryResponseDTO> result = service.list();
        assertThat(result).hasSize(1);
        verify(repository).findAll();
    }

    @Test
    @DisplayName("should get inventory by id")
    void getById_success() {
        when(repository.findById(1L)).thenReturn(Optional.of(inventory));
        when(mapper.toResponse(inventory)).thenReturn(new InventoryResponseDTO());
        InventoryResponseDTO result = service.getById(1L);
        assertThat(result).isNotNull();
        verify(repository).findById(1L);
    }

    @Test
    @DisplayName("should throw when inventory not found on get")
    void getById_notFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Inventory not found");
    }

    @Test
    @DisplayName("should update inventory successfully")
    void updateInventory_success() {
        InventoryCreateDTO dto = new InventoryCreateDTO();
        dto.setWarehouseId(1L);
        dto.setProductId(1L);
        dto.setQtyOnHand(5);
        dto.setQtyReserved(1);

        when(repository.findById(1L)).thenReturn(Optional.of(inventory));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(repository.save(any())).thenReturn(inventory);
        when(mapper.toResponse(any())).thenReturn(new InventoryResponseDTO());

        InventoryResponseDTO result = service.update(1L, dto);

        assertThat(result).isNotNull();
        verify(repository).save(inventory);
    }

    @Test
    @DisplayName("should throw when qtyOnHand or qtyReserved negative")
    void updateInventory_negativeQty() {
        InventoryCreateDTO dto = new InventoryCreateDTO();
        dto.setWarehouseId(1L);
        dto.setProductId(1L);
        dto.setQtyOnHand(-1);
        dto.setQtyReserved(0);

        when(repository.findById(1L)).thenReturn(Optional.of(inventory));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cant provide a Zero negative quantity");
    }

    @Test
    @DisplayName("should adjust inventory successfully with valid negative qty")
    void adjustInventory_success() {
        when(repository.findById(1L)).thenReturn(Optional.of(inventory));
        when(repository.save(inventory)).thenReturn(inventory);
        when(mapper.toResponse(inventory)).thenReturn(new InventoryResponseDTO());

        InventoryResponseDTO result = service.adjust(1L, -2L);

        assertThat(result).isNotNull();
        verify(inventoryMovementService).create(any(InventoryMovementCreateDTO.class));
        verify(repository).save(inventory);
    }

    @Test
    @DisplayName("should throw when adjustment is positive")
    void adjustInventory_positive() {
        when(repository.findById(1L)).thenReturn(Optional.of(inventory));
        assertThatThrownBy(() -> service.adjust(1L, 2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Adjustment quantity must be negative");
    }

    @Test
    @DisplayName("should throw invalid adjustment if exceeds available")
    void adjustInventory_invalidAdjustment() {
        when(repository.findById(1L)).thenReturn(Optional.of(inventory));
        assertThatThrownBy(() -> service.adjust(1L, -20L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid Adjustment");
    }

    @Test
    @DisplayName("should delete inventory successfully")
    void deleteInventory_success() {
        when(repository.findById(1L)).thenReturn(Optional.of(inventory));
        service.delete(1L);
        verify(repository).delete(inventory);
    }

    @Test
    @DisplayName("should throw when inventory not found on delete")
    void deleteInventory_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Inventory not found");
    }

    @Test
    @DisplayName("should call repository for helper inventory")
    void getHelperInventory_success() {
        when(repository.findAvailableInventoryNative(1L, 5, 1L))
                .thenReturn(Optional.of(inventory));
        Optional<Inventory> result = service.getHelperInventory(1L, 5, 1L);
        assertThat(result).isPresent();
        verify(repository).findAvailableInventoryNative(1L, 5, 1L);
    }
}
