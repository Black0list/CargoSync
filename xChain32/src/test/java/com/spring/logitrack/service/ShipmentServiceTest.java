package com.spring.logitrack.service;

import com.spring.logitrack.dto.shipment.ShipmentCreateDTO;
import com.spring.logitrack.dto.shipment.ShipmentResponseDTO;
import com.spring.logitrack.entity.SalesOrder;
import com.spring.logitrack.entity.Shipment;
import com.spring.logitrack.entity.Warehouse;
import com.spring.logitrack.entity.enums.OrderStatus;
import com.spring.logitrack.entity.enums.ShipmentStatus;
import com.spring.logitrack.mapper.ShipmentMapper;
import com.spring.logitrack.repository.SalesOrderRepository;
import com.spring.logitrack.repository.ShipmentRepository;
import com.spring.logitrack.repository.WarehouseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShipmentServiceTest {

    @Mock private ShipmentRepository shipmentRepo;
    @Mock private SalesOrderRepository salesOrderRepo;
    @Mock private WarehouseRepository warehouseRepo;
    @Mock private ShipmentMapper mapper;

    @InjectMocks private ShipmentService service;

    private Shipment shipment;
    private ShipmentResponseDTO responseDTO;
    private ShipmentCreateDTO createDTO;
    private SalesOrder salesOrder;
    private Warehouse warehouse;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        shipment = new Shipment();
        shipment.setId(1L);
        responseDTO = new ShipmentResponseDTO();
        responseDTO.setId(1L);

        createDTO = new ShipmentCreateDTO();
        createDTO.setSalesOrderId(2L);
        createDTO.setWarehouseId(3L);

        salesOrder = new SalesOrder();
        salesOrder.setId(2L);
        salesOrder.setStatus(OrderStatus.RESERVED);

        warehouse = new Warehouse();
        warehouse.setId(3L);
    }

    @Test
    void create_success() {
        when(mapper.toEntity(createDTO)).thenReturn(shipment);
        when(salesOrderRepo.findById(2L)).thenReturn(Optional.of(salesOrder));
        when(warehouseRepo.findById(3L)).thenReturn(Optional.of(warehouse));
        when(shipmentRepo.save(shipment)).thenReturn(shipment);
        when(mapper.toResponse(shipment)).thenReturn(responseDTO);

        ShipmentResponseDTO result = service.create(createDTO);

        assertThat(result).isNotNull();
        verify(shipmentRepo).save(shipment);
        assertThat(salesOrder.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void create_salesOrderNotFound() {
        when(salesOrderRepo.findById(2L)).thenReturn(Optional.empty());
        when(mapper.toEntity(createDTO)).thenReturn(shipment);
        assertThatThrownBy(() -> service.create(createDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Sales order not found");
    }

    @Test
    void create_warehouseNotFound() {
        when(salesOrderRepo.findById(2L)).thenReturn(Optional.of(salesOrder));
        when(warehouseRepo.findById(3L)).thenReturn(Optional.empty());
        when(mapper.toEntity(createDTO)).thenReturn(shipment);
        assertThatThrownBy(() -> service.create(createDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Warehouse not found");
    }

    @Test
    void create_invalidOrderStatus() {
        salesOrder.setStatus(OrderStatus.CREATED);
        when(salesOrderRepo.findById(2L)).thenReturn(Optional.of(salesOrder));
        when(warehouseRepo.findById(3L)).thenReturn(Optional.of(warehouse));
        when(mapper.toEntity(createDTO)).thenReturn(shipment);
        assertThatThrownBy(() -> service.create(createDTO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Shipment can only be Planned when SalesOrder is RESERVED");
    }

    @Test
    void getById_success() {
        when(shipmentRepo.findById(1L)).thenReturn(Optional.of(shipment));
        when(mapper.toResponse(shipment)).thenReturn(responseDTO);
        ShipmentResponseDTO result = service.getById(1L);
        assertThat(result.getId()).isEqualTo(1L);
        verify(shipmentRepo).findById(1L);
    }

    @Test
    void getById_notFound() {
        when(shipmentRepo.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Shipment not found");
    }

    @Test
    void list_success() {
        when(shipmentRepo.findAll()).thenReturn(List.of(shipment));
        when(mapper.toResponse(shipment)).thenReturn(responseDTO);
        List<ShipmentResponseDTO> result = service.list();
        assertThat(result).hasSize(1);
        verify(shipmentRepo).findAll();
    }

    @Test
    void update_success() {
        when(shipmentRepo.findById(1L)).thenReturn(Optional.of(shipment));
        when(salesOrderRepo.findById(2L)).thenReturn(Optional.of(salesOrder)); // ✅ add this
        when(warehouseRepo.findById(3L)).thenReturn(Optional.of(warehouse));   // ✅ optional, if dto has warehouseId
        when(mapper.toResponse(any())).thenReturn(responseDTO);
        when(shipmentRepo.save(shipment)).thenReturn(shipment);

        ShipmentResponseDTO result = service.update(1L, createDTO);

        assertThat(result).isNotNull();
        verify(shipmentRepo).save(shipment);
    }


    @Test
    void update_notFound() {
        when(shipmentRepo.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update(1L, createDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Shipment not found");
    }

    @Test
    void updateStatus_toDelivered_success() {
        SalesOrder order = new SalesOrder();
        order.setStatus(OrderStatus.SHIPPED);
        shipment.setSalesOrder(order);

        when(shipmentRepo.findById(1L)).thenReturn(Optional.of(shipment));
        when(mapper.toResponse(shipment)).thenReturn(responseDTO);
        when(shipmentRepo.save(shipment)).thenReturn(shipment);

        ShipmentResponseDTO result = service.updateStatus(1L, ShipmentStatus.DELIVERED);

        assertThat(result).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        verify(shipmentRepo).save(shipment);
    }

    @Test
    void updateStatus_notFound() {
        when(shipmentRepo.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updateStatus(1L, ShipmentStatus.PLANNED))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Shipment not found");
    }

    @Test
    void delete_success() {
        when(shipmentRepo.existsById(1L)).thenReturn(true);
        service.delete(1L);
        verify(shipmentRepo).deleteById(1L);
    }

    @Test
    void delete_notFound() {
        when(shipmentRepo.existsById(1L)).thenReturn(false);
        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Shipment not found");
    }
}
