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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShipmentService {
    private final ShipmentRepository shipmentRepo;
    private final SalesOrderRepository salesOrderRepo;
    private final WarehouseRepository warehouseRepo;
    private final ShipmentMapper mapper;

    @Transactional
    public ShipmentResponseDTO create(ShipmentCreateDTO dto) {
        Shipment shipment = mapper.toEntity(dto);

        SalesOrder order = salesOrderRepo.findById(dto.getSalesOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Sales order not found"));

        Warehouse warehouse = warehouseRepo.findById(dto.getWarehouseId())
                .orElseThrow(() -> new EntityNotFoundException("Warehouse not found"));

        if (order.getStatus() != OrderStatus.RESERVED) {
            throw new IllegalStateException("Shipment can only be Planned when SalesOrder is RESERVED");
        }

        order.setStatus(OrderStatus.SHIPPED);

        shipment.setSalesOrder(order);
        shipment.setWarehouse(warehouse);

        Shipment saved = shipmentRepo.save(shipment);

        return mapper.toResponse(saved);
    }

    public ShipmentResponseDTO getById(Long id) {
        Shipment shipment = shipmentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Shipment not found"));
        return mapper.toResponse(shipment);
    }

    public List<ShipmentResponseDTO> list() {
        return shipmentRepo.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public ShipmentResponseDTO update(Long id, ShipmentCreateDTO dto) {
        Shipment shipment = shipmentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Shipment not found"));

        mapper.patch(shipment, dto);

        if (dto.getSalesOrderId() != null) {
            SalesOrder order = salesOrderRepo.findById(dto.getSalesOrderId())
                    .orElseThrow(() -> new EntityNotFoundException("Sales order not found"));
            shipment.setSalesOrder(order);
        }

        if (dto.getWarehouseId() != null) {
            Warehouse warehouse = warehouseRepo.findById(dto.getWarehouseId())
                    .orElseThrow(() -> new EntityNotFoundException("Warehouse not found"));
            shipment.setWarehouse(warehouse);
        }

        return mapper.toResponse(shipmentRepo.save(shipment));
    }

    @Transactional
    public ShipmentResponseDTO updateStatus(Long id, ShipmentStatus status) {
        Shipment shipment = shipmentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Shipment not found"));

        shipment.setStatus(status);

        if (status == ShipmentStatus.DELIVERED) {
            SalesOrder order = shipment.getSalesOrder();
            order.setStatus(OrderStatus.DELIVERED);
        }

        return mapper.toResponse(shipmentRepo.save(shipment));
    }

    public void delete(Long id) {
        if (!shipmentRepo.existsById(id))
            throw new EntityNotFoundException("Shipment not found");
        shipmentRepo.deleteById(id);
    }
}
