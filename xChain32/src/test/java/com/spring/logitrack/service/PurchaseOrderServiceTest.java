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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
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
        supplier = new Supplier();
        supplier.setId(1L);

        product = new Product();
        product.setId(10L);
        product.setPrice(BigDecimal.valueOf(50));
    }

    @Test
    @DisplayName("create: should create purchase order with lines when no orderId")
    void create_withLines_success() {
        POLineCreateDTO lineDTO = new POLineCreateDTO();
        lineDTO.setProductId(10L);
        lineDTO.setQty(5);

        PurchaseOrderCreateDTO dto = new PurchaseOrderCreateDTO();
        dto.setSupplierId(1L);
        dto.setLines(List.of(lineDTO));

        PurchaseOrder saved = PurchaseOrder.builder().id(99L).supplier(supplier).build();

        when(supplierRepo.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepo.findById(10L)).thenReturn(Optional.of(product));
        when(poRepo.saveAndFlush(any(PurchaseOrder.class))).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(new PurchaseOrderResponseDTO());

        PurchaseOrderResponseDTO result = service.create(dto);

        assertThat(result).isNotNull();
        verify(poRepo).saveAndFlush(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("create: should throw when supplier not found")
    void create_supplierNotFound() {
        PurchaseOrderCreateDTO dto = new PurchaseOrderCreateDTO();
        dto.setSupplierId(99L);

        when(supplierRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Supplier not found");
    }

    @Test
    @DisplayName("create: should throw when product not found in lines")
    void create_productNotFound() {
        POLineCreateDTO lineDTO = new POLineCreateDTO();
        lineDTO.setProductId(10L);
        lineDTO.setQty(3);

        PurchaseOrderCreateDTO dto = new PurchaseOrderCreateDTO();
        dto.setSupplierId(1L);
        dto.setLines(List.of(lineDTO));

        when(supplierRepo.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepo.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    @DisplayName("create: should throw when neither orderId nor lines provided")
    void create_missingOrderAndLines() {
        PurchaseOrderCreateDTO dto = new PurchaseOrderCreateDTO();
        dto.setSupplierId(1L);

        when(supplierRepo.findById(1L)).thenReturn(Optional.of(supplier));

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Either 'order' or 'lines' must be provided");
    }

    @Test
    @DisplayName("create: should throw when PO already exists for orderId")
    void create_duplicatePurchaseOrderForOrder() {
        PurchaseOrderCreateDTO dto = new PurchaseOrderCreateDTO();
        dto.setSupplierId(1L);
        dto.setOrderId(5L);

        when(supplierRepo.findById(1L)).thenReturn(Optional.of(supplier));
        when(poRepo.findByOrder_Id(5L)).thenReturn(Optional.of(new PurchaseOrder()));

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already exists for this order");
    }

    @Test
    @DisplayName("create: should create from SimpleOrder with extra lines")
    void create_fromSimpleOrderWithExtraQty() {
        POLineCreateDTO lineDTO = new POLineCreateDTO();
        lineDTO.setProductId(10L);
        lineDTO.setQty(2);

        PurchaseOrderCreateDTO dto = new PurchaseOrderCreateDTO();
        dto.setSupplierId(1L);
        dto.setOrderId(7L);
        dto.setLines(List.of(lineDTO));

        SimpleOrder simpleOrder = new SimpleOrder();
        simpleOrder.setId(7L);
        simpleOrder.setProduct(product);
        simpleOrder.setQty(3);

        PurchaseOrder saved = PurchaseOrder.builder()
                .id(123L)
                .supplier(supplier)
                .status(POStatus.APPROVED)
                .build();

        when(supplierRepo.findById(1L)).thenReturn(Optional.of(supplier));
        when(poRepo.findByOrder_Id(7L)).thenReturn(Optional.empty());
        when(simpleOrderRepo.findById(7L)).thenReturn(Optional.of(simpleOrder));
        when(poRepo.saveAndFlush(any(PurchaseOrder.class))).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(new PurchaseOrderResponseDTO());

        PurchaseOrderResponseDTO result = service.create(dto);

        assertThat(result).isNotNull();
        verify(simpleOrderRepo).findById(7L);
        verify(poRepo).saveAndFlush(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("create: should fallback to Backorder when SimpleOrder not found")
    void create_fromBackorderViaOrderId() {
        PurchaseOrderCreateDTO dto = new PurchaseOrderCreateDTO();
        dto.setSupplierId(1L);
        dto.setOrderId(9L);

        Backorder backorder = new Backorder();
        backorder.setId(9L);
        backorder.setProduct(product);
        backorder.setQty(4);

        PurchaseOrder saved = PurchaseOrder.builder()
                .id(456L)
                .supplier(supplier)
                .status(POStatus.APPROVED)
                .build();

        when(supplierRepo.findById(1L)).thenReturn(Optional.of(supplier));
        when(poRepo.findByOrder_Id(9L)).thenReturn(Optional.empty());
        when(simpleOrderRepo.findById(9L)).thenReturn(Optional.empty());
        when(backorderRepo.findById(9L)).thenReturn(Optional.of(backorder));
        when(poRepo.saveAndFlush(any(PurchaseOrder.class))).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(new PurchaseOrderResponseDTO());

        PurchaseOrderResponseDTO result = service.create(dto);

        assertThat(result).isNotNull();
        verify(backorderRepo).findById(9L);
        verify(poRepo).saveAndFlush(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("create: should throw when SimpleOrder and Backorder not found for orderId")
    void create_orderNotFoundForOrderId() {
        PurchaseOrderCreateDTO dto = new PurchaseOrderCreateDTO();
        dto.setSupplierId(1L);
        dto.setOrderId(9L);

        when(supplierRepo.findById(1L)).thenReturn(Optional.of(supplier));
        when(poRepo.findByOrder_Id(9L)).thenReturn(Optional.empty());
        when(simpleOrderRepo.findById(9L)).thenReturn(Optional.empty());
        when(backorderRepo.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Order not found with id: 9");
    }

    @Test
    @DisplayName("createFromBackOrder: should create PO from backorder")
    void createFromBackorder_success() {
        Backorder back = new Backorder();
        back.setId(5L);
        back.setQty(3);
        back.setProduct(product);

        PurchaseOrder saved = PurchaseOrder.builder().id(88L).supplier(supplier).build();

        when(backorderRepo.findById(5L)).thenReturn(Optional.of(back));
        when(supplierRepo.findById(1L)).thenReturn(Optional.of(supplier));
        when(poRepo.save(any(PurchaseOrder.class))).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(new PurchaseOrderResponseDTO());

        PurchaseOrderResponseDTO result = service.createFromBackOrder(5L, 1L);

        assertThat(result).isNotNull();
        verify(poRepo).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("createFromBackOrder: should throw when backorder not found")
    void createFromBackorder_backorderNotFound() {
        when(backorderRepo.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createFromBackOrder(5L, 1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("BackOrder not found");
    }

    @Test
    @DisplayName("createFromBackOrder: should throw when supplier not found")
    void createFromBackorder_supplierNotFound() {
        Backorder back = new Backorder();
        back.setId(5L);
        back.setProduct(product);
        back.setQty(3);

        when(backorderRepo.findById(5L)).thenReturn(Optional.of(back));
        when(supplierRepo.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createFromBackOrder(5L, 1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Supplier not found");
    }

    @Test
    @DisplayName("update: should patch and update supplier when provided")
    void update_successWithSupplierChange() {
        PurchaseOrder existing = PurchaseOrder.builder().id(1L).build();
        PurchaseOrderCreateDTO dto = new PurchaseOrderCreateDTO();
        dto.setSupplierId(1L);

        PurchaseOrder saved = PurchaseOrder.builder().id(1L).supplier(supplier).build();
        PurchaseOrderResponseDTO responseDTO = new PurchaseOrderResponseDTO();

        when(poRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(supplierRepo.findById(1L)).thenReturn(Optional.of(supplier));
        when(poRepo.save(existing)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(responseDTO);

        PurchaseOrderResponseDTO result = service.update(1L, dto);

        assertThat(result).isSameAs(responseDTO);
        verify(mapper).patch(existing, dto);
        verify(supplierRepo).findById(1L);
        verify(poRepo).save(existing);
    }

    @Test
    @DisplayName("update: should throw when PO not found")
    void update_poNotFound() {
        PurchaseOrderCreateDTO dto = new PurchaseOrderCreateDTO();

        when(poRepo.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("PurchaseOrder not found");
    }

    @Test
    @DisplayName("update: should throw when new supplier not found")
    void update_supplierNotFound() {
        PurchaseOrder existing = PurchaseOrder.builder().id(1L).build();
        PurchaseOrderCreateDTO dto = new PurchaseOrderCreateDTO();
        dto.setSupplierId(99L);

        when(poRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(supplierRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Supplier not found");
    }

    @Test
    @DisplayName("updateStatus: should update to RECEIVED for BackOrder and update inventory, sales order & backorder")
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

        Inventory inv = new Inventory();
        inv.setQtyOnHand(5);
        inv.setQtyReserved(1);
        inv.setProduct(product);
        inv.setWarehouse(warehouse);

        when(poRepo.findById(55L)).thenReturn(Optional.of(po));
        when(inventoryRepo.findTopByProduct_IdAndWarehouse_IdOrderByIdDesc(product.getId(), warehouse.getId()))
                .thenReturn(Optional.of(inv));
        when(poRepo.save(po)).thenReturn(po);
        when(mapper.toResponse(po)).thenReturn(new PurchaseOrderResponseDTO());

        PurchaseOrderResponseDTO result = service.updateStatus(55L, "RECEIVED");

        assertThat(result).isNotNull();
        assertThat(inv.getQtyOnHand()).isEqualTo(8);
        assertThat(inv.getQtyReserved()).isEqualTo(4);
        assertThat(line.getQtyReserved()).isEqualTo(5);
        assertThat(sales.getStatus()).isEqualTo(OrderStatus.RESERVED);
        assertThat(back.getStatus()).isEqualTo(BackorderStatus.FULFILLED);

        verify(poRepo).save(po);
        verify(inventoryRepo).save(inv);
        verify(salesOrderLineRepo).save(line);
        verify(salesOrderRepo).save(sales);
        verify(backorderRepo).save(back);
    }

    @Test
    @DisplayName("updateStatus: should throw when status is invalid")
    void updateStatus_invalidStatus() {
        PurchaseOrder po = new PurchaseOrder();
        po.setId(1L);

        when(poRepo.findById(1L)).thenReturn(Optional.of(po));

        assertThatThrownBy(() -> service.updateStatus(1L, "INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid status");
    }

    @Test
    @DisplayName("updateStatus: should throw when PO not found")
    void updateStatus_poNotFound() {
        when(poRepo.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateStatus(77L, "RECEIVED"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Purchase order not found");
    }

    @Test
    @DisplayName("updateStatus: should throw when status unchanged")
    void updateStatus_sameStatus() {
        PurchaseOrder po = new PurchaseOrder();
        po.setId(1L);
        po.setStatus(POStatus.APPROVED);

        when(poRepo.findById(1L)).thenReturn(Optional.of(po));

        assertThatThrownBy(() -> service.updateStatus(1L, "APPROVED"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already has status");
    }

    @Test
    @DisplayName("updateStatus: should throw when reverting RECEIVED to APPROVED")
    void updateStatus_revertReceivedToApproved() {
        PurchaseOrder po = new PurchaseOrder();
        po.setId(1L);
        po.setStatus(POStatus.RECEIVED);

        when(poRepo.findById(1L)).thenReturn(Optional.of(po));

        assertThatThrownBy(() -> service.updateStatus(1L, "APPROVED"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Can't revert status from RECEIVED to APPROVED");
    }

    @Test
    @DisplayName("updateStatus: should update to RECEIVED for SimpleOrder and call mapper")
    void updateStatus_receivedSimpleOrder() {
        SimpleOrder simpleOrder = new SimpleOrder();
        simpleOrder.setId(10L);
        simpleOrder.setProduct(product);
        simpleOrder.setQty(2);

        PurchaseOrder po = PurchaseOrder.builder()
                .id(1L)
                .status(POStatus.APPROVED)
                .order(simpleOrder)
                .build();

        PurchaseOrder saved = PurchaseOrder.builder()
                .id(1L)
                .status(POStatus.RECEIVED)
                .order(simpleOrder)
                .build();

        PurchaseOrderResponseDTO responseDTO = new PurchaseOrderResponseDTO();

        when(poRepo.findById(1L)).thenReturn(Optional.of(po));
        when(poRepo.save(po)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(responseDTO);

        PurchaseOrderResponseDTO result = service.updateStatus(1L, "RECEIVED");

        assertThat(result).isSameAs(responseDTO);
        assertThat(po.getStatus()).isEqualTo(POStatus.RECEIVED);
        verify(poRepo).save(po);
        verify(mapper).toResponse(saved);
    }

    @Test
    @DisplayName("list: should list all purchase orders")
    void listPurchaseOrders() {
        PurchaseOrder po = new PurchaseOrder();
        PurchaseOrderResponseDTO dto = new PurchaseOrderResponseDTO();

        when(poRepo.findAll()).thenReturn(List.of(po));
        when(mapper.toResponse(po)).thenReturn(dto);

        List<PurchaseOrderResponseDTO> result = service.list();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isSameAs(dto);
        verify(poRepo).findAll();
    }

    @Test
    @DisplayName("findBySupplier: should return mapped purchase orders")
    void findBySupplier_success() {
        PurchaseOrder po1 = new PurchaseOrder();
        PurchaseOrder po2 = new PurchaseOrder();
        PurchaseOrderResponseDTO dto1 = new PurchaseOrderResponseDTO();
        PurchaseOrderResponseDTO dto2 = new PurchaseOrderResponseDTO();

        when(poRepo.findBySupplier_Id(1L)).thenReturn(List.of(po1, po2));
        when(mapper.toResponse(po1)).thenReturn(dto1);
        when(mapper.toResponse(po2)).thenReturn(dto2);

        List<PurchaseOrderResponseDTO> result = service.findBySupplier(1L);

        assertThat(result).containsExactly(dto1, dto2);
        verify(poRepo).findBySupplier_Id(1L);
    }

    @Test
    @DisplayName("findBySupplier: should handle empty result list")
    void findBySupplier_empty() {
        when(poRepo.findBySupplier_Id(1L)).thenReturn(Collections.emptyList());

        List<PurchaseOrderResponseDTO> result = service.findBySupplier(1L);

        assertThat(result).isEmpty();
        verify(poRepo).findBySupplier_Id(1L);
    }

    @Test
    @DisplayName("delete: should delete purchase order successfully")
    void deletePO_success() {
        PurchaseOrder po = new PurchaseOrder();
        when(poRepo.findById(1L)).thenReturn(Optional.of(po));

        service.delete(1L);

        verify(poRepo).delete(po);
    }

    @Test
    @DisplayName("delete: should throw when deleting non-existing PO")
    void deletePO_notFound() {
        when(poRepo.findById(55L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(55L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("PurchaseOrder not found");
    }
}
