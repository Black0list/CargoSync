package com.spring.logitrack.service;

import com.spring.logitrack.dto.POLine.POLineCreateDTO;
import com.spring.logitrack.dto.purchaseOrder.PurchaseOrderCreateDTO;
import com.spring.logitrack.dto.purchaseOrder.PurchaseOrderResponseDTO;
import com.spring.logitrack.entity.*;
import com.spring.logitrack.entity.enums.BackorderStatus;
import com.spring.logitrack.entity.enums.OrderStatus;
import com.spring.logitrack.entity.enums.POStatus;
import com.spring.logitrack.mapper.PurchaseOrderMapper;
import com.spring.logitrack.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class PurchaseOrderServiceTest {

    @Mock private PurchaseOrderRepository poRepo;
    @Mock private SupplierRepository supplierRepo;
    @Mock private ProductRepository productRepo;
    @Mock private SimpleOrderRepository simpleOrderRepo;
    @Mock private BackorderRepository backorderRepo;
    @Mock private SalesOrderLineRepository salesOrderLineRepo;
    @Mock private SalesOrderRepository salesOrderRepo;
    @Mock private InventoryRepository inventoryRepo;
    @Mock private PurchaseOrderMapper mapper;

    @InjectMocks
    private PurchaseOrderService service;

    private Supplier supplier;
    private Product product;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        supplier = new Supplier();
        supplier.setId(1L);

        product = new Product();
        product.setId(10L);
        product.setPrice(new BigDecimal(50.0));
    }

    // ==========================================================
    // CREATE PO
    // ==========================================================
    @Test
    @DisplayName("should create purchase order successfully with lines")
    void createPurchaseOrder_success() {

        POLineCreateDTO lineDTO = new POLineCreateDTO();
        lineDTO.setProductId(10L);
        lineDTO.setQty(5);

        PurchaseOrderCreateDTO dto = new PurchaseOrderCreateDTO();
        dto.setSupplierId(1L);
        dto.setLines(List.of(lineDTO));

        PurchaseOrder saved = PurchaseOrder.builder().id(99L).supplier(supplier).build();

        when(supplierRepo.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepo.findById(10L)).thenReturn(Optional.of(product));
        when(poRepo.saveAndFlush(any())).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(new PurchaseOrderResponseDTO());

        PurchaseOrderResponseDTO result = service.create(dto);

        assertThat(result).isNotNull();
        verify(poRepo).saveAndFlush(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("should throw when supplier not found on create")
    void createPurchaseOrder_supplierNotFound() {
        PurchaseOrderCreateDTO dto = new PurchaseOrderCreateDTO();
        dto.setSupplierId(99L);

        when(supplierRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Supplier not found");
    }

    // ==========================================================
    // CREATE FROM BACKORDER
    // ==========================================================
    @Test
    @DisplayName("should create PO from backorder")
    void createFromBackorder_success() {

        Backorder back = new Backorder();
        back.setId(5L);
        back.setQty(3);
        back.setProduct(product);

        when(backorderRepo.findById(5L)).thenReturn(Optional.of(back));
        when(supplierRepo.findById(1L)).thenReturn(Optional.of(supplier));

        PurchaseOrder saved = PurchaseOrder.builder().id(88L).supplier(supplier).build();
        when(poRepo.save(any())).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(new PurchaseOrderResponseDTO());

        PurchaseOrderResponseDTO result = service.createFromBackOrder(5L, 1L);

        assertThat(result).isNotNull();
        verify(poRepo).save(any(PurchaseOrder.class));
    }

    // ==========================================================
    // UPDATE STATUS — RECEIVED (BACKORDER)
    // ==========================================================
    @Test
    @DisplayName("should update status to RECEIVED for BackOrder and update inventory, sales order & backorder")
    void updateStatus_receivedBackorder_success() {

        Warehouse warehouse = new Warehouse();
        warehouse.setId(2L);

        SalesOrderLine line = new SalesOrderLine();
        line.setProduct(product);
        line.setQtyReserved(2);

        SalesOrder sales = new SalesOrder();
        sales.setId(20L);
        sales.setWarehouse(warehouse);
        sales.setLines(List.of(line));
        sales.setStatus(OrderStatus.CREATED);

        Backorder back = new Backorder();
        back.setId(100L);
        back.setQty(3);
        back.setProduct(product);
        back.setSalesOrder(sales);

        PurchaseOrder po = PurchaseOrder.builder()
                .id(55L)
                .status(POStatus.APPROVED)
                .order(back)
                .build();

        // ⭐ REQUIRED FIX — PO must contain lines (your service relies on it indirectly)
        po.setLines(List.of(
                POLine.builder()
                        .product(product)
                        .qty(3)
                        .price(new BigDecimal(50.0))
                        .build()
        ));

        Inventory inv = new Inventory();
        inv.setQtyOnHand(5);
        inv.setQtyReserved(1);
        inv.setProduct(product);
        inv.setWarehouse(warehouse);

        when(poRepo.findById(55L)).thenReturn(Optional.of(po));
        when(inventoryRepo.findTopByProduct_IdAndWarehouse_IdOrderByIdDesc(product.getId(), warehouse.getId()))
                .thenReturn(Optional.of(inv));

        // ⭐ FIXED
        when(poRepo.save(any())).thenReturn(po);
        when(mapper.toResponse(any())).thenReturn(new PurchaseOrderResponseDTO());

        PurchaseOrderResponseDTO result = service.updateStatus(55L, "RECEIVED");

        assertThat(result).isNotNull();

        // Inventory updated
        assertThat(inv.getQtyOnHand()).isEqualTo(8);      // 5 + 3
        assertThat(inv.getQtyReserved()).isEqualTo(4);    // 1 + 3

        // Sales order lines updated
        assertThat(line.getQtyReserved()).isEqualTo(5);   // 2 + 3

        // Sales order updated
        assertThat(sales.getStatus()).isEqualTo(OrderStatus.RESERVED);

        // Backorder updated
        assertThat(back.getStatus()).isEqualTo(BackorderStatus.FULFILLED);

        verify(poRepo).save(po);
        verify(inventoryRepo).save(inv);
        verify(salesOrderLineRepo).save(line);
        verify(salesOrderRepo).save(sales);
        verify(backorderRepo).save(back);
    }

    // ==========================================================
    // INVALID STATUS
    // ==========================================================
    @Test
    @DisplayName("should throw when status is invalid")
    void updateStatus_invalidStatus() {

        PurchaseOrder po = new PurchaseOrder();
        po.setId(1L);

        when(poRepo.findById(1L)).thenReturn(Optional.of(po));

        assertThatThrownBy(() -> service.updateStatus(1L, "INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid status");
    }

    // ==========================================================
    // PO NOT FOUND
    // ==========================================================
    @Test
    @DisplayName("should throw when purchase order not found")
    void updateStatus_poNotFound() {

        when(poRepo.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateStatus(77L, "RECEIVED"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Purchase order not found");
    }

    // ==========================================================
    // LIST
    // ==========================================================
    @Test
    @DisplayName("should list all purchase orders")
    void listPurchaseOrders() {

        PurchaseOrder po = new PurchaseOrder();

        when(poRepo.findAll()).thenReturn(List.of(po));
        when(mapper.toResponse(po)).thenReturn(new PurchaseOrderResponseDTO());

        List<PurchaseOrderResponseDTO> result = service.list();

        assertThat(result).hasSize(1);
        verify(poRepo).findAll();
    }

    // ==========================================================
    // DELETE
    // ==========================================================
    @Test
    @DisplayName("should delete purchase order successfully")
    void deletePO_success() {

        PurchaseOrder po = new PurchaseOrder();
        when(poRepo.findById(1L)).thenReturn(Optional.of(po));

        service.delete(1L);

        verify(poRepo).delete(po);
    }

    @Test
    @DisplayName("should throw when deleting non-existing PO")
    void deletePO_notFound() {

        when(poRepo.findById(55L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(55L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("PurchaseOrder not found");
    }
}
