package com.spring.logitrack.service;

import com.spring.logitrack.dto.salesOrder.SalesOrderCreateDTO;
import com.spring.logitrack.dto.salesOrderLine.SalesOrderLineCreateDTO;
import com.spring.logitrack.dto.salesOrder.SalesOrderResponseDTO;
import com.spring.logitrack.dto.salesOrder.SalesOrderResponseWithWarningsDTO;
import com.spring.logitrack.entity.*;
import com.spring.logitrack.entity.enums.OrderStatus;
import com.spring.logitrack.mapper.SalesOrderMapper;
import com.spring.logitrack.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SalesOrderServiceTest {

    @Mock private SalesOrderRepository salesOrderRepo;
    @Mock private UserRepository userRepo;
    @Mock private WarehouseRepository warehouseRepo;
    @Mock private ProductRepository productRepo;
    @Mock private InventoryRepository inventoryRepo;
    @Mock private InventoryService inventoryService;
    @Mock private InventoryMovementService inventoryMovementService;
    @Mock private BackOrderService backorderService;
    @Mock private SalesOrderMapper mapper;

    @InjectMocks private SalesOrderService service;

    private User user;
    private Warehouse warehouse;
    private Product product;
    private SalesOrder order;
    private Inventory inventory;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        warehouse = new Warehouse();
        warehouse.setId(1L);
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setSku("SKU1");
        product.setActive(true);
        product.setPrice(BigDecimal.valueOf(10));
        order = new SalesOrder();
        order.setId(1L);
        order.setWarehouse(warehouse);
        order.setStatus(OrderStatus.CREATED);
        inventory = new Inventory();
        inventory.setId(1L);
        inventory.setProduct(product);
        inventory.setWarehouse(warehouse);
        inventory.setQtyOnHand(10);
        inventory.setQtyReserved(0);
    }

    @Test
    @DisplayName("should create new SalesOrder successfully")
    void createOrder_success() {
        SalesOrderCreateDTO dto = new SalesOrderCreateDTO();
        dto.setClientId(1L);
        dto.setWarehouseId(1L);
        SalesOrderLineCreateDTO line = new SalesOrderLineCreateDTO();
        line.setProductId(1L);
        line.setQtyOrdered(5);
        dto.setLines(List.of(line));

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(warehouseRepo.findById(1L)).thenReturn(Optional.of(warehouse));
        when(productRepo.findById(1L)).thenReturn(Optional.of(product));
        when(salesOrderRepo.save(any(SalesOrder.class))).thenReturn(order);
        when(mapper.toResponse(any(SalesOrder.class))).thenReturn(new SalesOrderResponseDTO());
        when(mapper.toResponse(any(SalesOrderResponseDTO.class), anyList()))
                .thenReturn(new SalesOrderResponseWithWarningsDTO());

        SalesOrderResponseWithWarningsDTO result = service.create(dto);

        assertThat(result).isNotNull();
        verify(salesOrderRepo).save(any(SalesOrder.class));
    }

    @Test
    @DisplayName("should throw when client not found")
    void createOrder_clientNotFound() {
        SalesOrderCreateDTO dto = new SalesOrderCreateDTO();
        dto.setClientId(99L);
        when(userRepo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Client not found");
    }

    @Test
    @DisplayName("should get order by id")
    void getOrder_success() {
        when(salesOrderRepo.findById(1L)).thenReturn(Optional.of(order));
        when(mapper.toResponse(order)).thenReturn(new SalesOrderResponseDTO());
        SalesOrderResponseDTO dto = service.get(1L);
        assertThat(dto).isNotNull();
        verify(salesOrderRepo).findById(1L);
    }

    @Test
    @DisplayName("should throw when order not found")
    void getOrder_notFound() {
        when(salesOrderRepo.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Sales order not found");
    }

    @Test
    @DisplayName("should reserve when stock is enough")
    void reserve_withEnoughStock() {
        SalesOrderLine line = new SalesOrderLine();
        line.setProduct(product);
        line.setQtyOrdered(5);
        order.setLines(List.of(line));

        when(salesOrderRepo.findById(1L)).thenReturn(Optional.of(order));
        when(inventoryRepo.findTopByProduct_IdAndWarehouse_IdOrderByIdDesc(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(salesOrderRepo.save(any(SalesOrder.class))).thenReturn(order);
        when(mapper.toResponse(any(SalesOrder.class))).thenReturn(new SalesOrderResponseDTO());
        when(mapper.toResponse(any(SalesOrderResponseDTO.class), anyList()))
                .thenReturn(new SalesOrderResponseWithWarningsDTO());

        SalesOrderResponseWithWarningsDTO result = service.reserve(1L);

        assertThat(result).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.RESERVED);
        verify(inventoryRepo, atLeastOnce()).save(any(Inventory.class));
    }

    @Test
    @DisplayName("should throw when order not found on reserve")
    void reserve_orderNotFound() {
        when(salesOrderRepo.findById(10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.reserve(10L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("should handle inactive product in reservation")
    void reserve_inactiveProduct() {
        product.setActive(false);
        SalesOrderLine line = new SalesOrderLine();
        line.setProduct(product);
        line.setQtyOrdered(3);
        order.setLines(List.of(line));

        when(salesOrderRepo.findById(1L)).thenReturn(Optional.of(order));
        when(inventoryRepo.findTopByProduct_IdAndWarehouse_IdOrderByIdDesc(anyLong(), anyLong()))
                .thenReturn(Optional.of(inventory));
        when(salesOrderRepo.save(any(SalesOrder.class))).thenReturn(order);
        when(mapper.toResponse(any(SalesOrder.class))).thenReturn(new SalesOrderResponseDTO());
        when(mapper.toResponse(any(SalesOrderResponseDTO.class), anyList()))
                .thenReturn(new SalesOrderResponseWithWarningsDTO());

        SalesOrderResponseWithWarningsDTO result = service.reserve(1L);

        assertThat(result).isNotNull();
        verify(salesOrderRepo).save(any(SalesOrder.class));
    }
}
