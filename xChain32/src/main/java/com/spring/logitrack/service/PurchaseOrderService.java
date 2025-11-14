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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseOrderService {

    private final PurchaseOrderRepository poRepo;
    private final SupplierRepository supplierRepo;
    private final ProductRepository productRepo;
    private final SimpleOrderRepository simpleOrderRepo;
    private final BackorderRepository backorderRepo;
    private final SalesOrderLineRepository salesOrderLineRepo;
    private final SalesOrderRepository salesOrderRepo;
    private final InventoryRepository inventoryRepo;
    private final PurchaseOrderMapper mapper;

    @Transactional
    public PurchaseOrderResponseDTO create(PurchaseOrderCreateDTO dto) {
        Supplier supplier = supplierRepo.findById(dto.getSupplierId())
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found"));

        PurchaseOrder po = PurchaseOrder.builder()
                .supplier(supplier)
                .status(POStatus.APPROVED)
                .build();

        // Prevent duplicate POs for same order
        if (dto.getOrderId() != null) {
            poRepo.findByOrder_Id(dto.getOrderId())
                    .ifPresent(existing -> {
                        throw new IllegalStateException(
                                "A PurchaseOrder already exists for this order (ID: " + dto.getOrderId() + ")");
                    });
        }

        if (dto.getOrderId() != null) {
            Long orderId = dto.getOrderId();

            AbstractOrder order = simpleOrderRepo.findById(orderId)
                    .map(o -> (AbstractOrder) o)
                    .orElseGet(() -> backorderRepo.findById(orderId)
                            .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId)));

            order.setPurchaseOrder(po);
            po.setOrder(order);

            Product product = order.getProduct();
            int orderQty = order.getQty();

            int extraQty = 0;
            if (dto.getLines() != null && !dto.getLines().isEmpty()) {
                extraQty = dto.getLines().stream()
                        .filter(l -> l.getProductId().equals(product.getId()))
                        .mapToInt(POLineCreateDTO::getQty)
                        .sum();
            }

            int totalQty = orderQty + extraQty;

            POLine line = POLine.builder()
                    .purchaseOrder(po)
                    .product(product)
                    .qty(totalQty)
                    .price(product.getPrice())
                    .build();

            po.getLines().add(line);
        }
        else if (dto.getLines() != null && !dto.getLines().isEmpty()) {
            for (var lineDTO : dto.getLines()) {
                Product product = productRepo.findById(lineDTO.getProductId())
                        .orElseThrow(() -> new EntityNotFoundException("Product not found"));

                POLine line = POLine.builder()
                        .purchaseOrder(po)
                        .product(product)
                        .qty(lineDTO.getQty())
                        .price(product.getPrice())
                        .build();

                po.getLines().add(line);
            }
        } else {
            throw new IllegalArgumentException("Either 'order' or 'lines' must be provided");
        }

        PurchaseOrder saved = poRepo.saveAndFlush(po);
        return mapper.toResponse(saved);
    }



    public PurchaseOrderResponseDTO createFromBackOrder(Long backorderId, Long supplierId) {
        Backorder backOrder = backorderRepo.findById(backorderId)
                .orElseThrow(() -> new EntityNotFoundException("BackOrder not found"));
        Supplier supplier = supplierRepo.findById(supplierId)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found"));

        PurchaseOrder po = PurchaseOrder.builder()
                .supplier(supplier)
                .status(POStatus.APPROVED)
                .build();

        POLine line = POLine.builder()
                .purchaseOrder(po)
                .product(backOrder.getProduct())
                .qty(backOrder.getQty())
                .price(backOrder.getProduct().getPrice())
                .build();

        po.getLines().add(line);
        return mapper.toResponse(poRepo.save(po));
    }

    public PurchaseOrderResponseDTO update(Long id, PurchaseOrderCreateDTO dto) {
        PurchaseOrder existing = poRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PurchaseOrder not found"));

        mapper.patch(existing, dto);

        if (dto.getSupplierId() != null) {
            Supplier supplier = supplierRepo.findById(dto.getSupplierId())
                    .orElseThrow(() -> new EntityNotFoundException("Supplier not found"));
            existing.setSupplier(supplier);
        }

        return mapper.toResponse(poRepo.save(existing));
    }

    @Transactional
    public PurchaseOrderResponseDTO updateStatus(Long id, String status) {
        System.out.println("==========================================");
        PurchaseOrder order = poRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found"));

        POStatus newStatus;
        try {
            newStatus = POStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

//        if (order.getStatus() == newStatus)
//            throw new IllegalStateException("Purchase order already has status: " + newStatus);
//
//        if (order.getStatus().equals(POStatus.RECEIVED) && newStatus.equals(POStatus.APPROVED))
//            throw new IllegalStateException("Can't revert status from RECEIVED to APPROVED");

        if(newStatus == POStatus.RECEIVED){
            if (order.getOrder().getClass().getSimpleName().equals("BackOrder")){
                applyModificationsForBackOrder(order);
            } else {
                applyModificationsForSimpleOrder(order);
            }
        }

        order.setStatus(newStatus);
        return mapper.toResponse(poRepo.save(order));
    }

    private void applyModificationsForSimpleOrder(PurchaseOrder order) {

    }

    private void applyModificationsForBackOrder(PurchaseOrder order) {
        Backorder backOrder = (Backorder) order.getOrder();
        SalesOrder salesOrder = backOrder.getSalesOrder();
        Warehouse warehouse = salesOrder.getWarehouse();
        Product product = backOrder.getProduct();

        SalesOrderLine salesOrderLine = salesOrder.getLines().stream()
                .filter(line -> line.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "No SalesOrderLine found for product ID: " + product.getId()));

        salesOrderLine.setQtyReserved(salesOrderLine.getQtyReserved()+backOrder.getQty());
        salesOrder.setStatus(OrderStatus.RESERVED);

        Optional<Inventory> inventory = Optional.ofNullable(inventoryRepo.findTopByProduct_IdAndWarehouse_IdOrderByIdDesc(product.getId(), warehouse.getId()).orElseThrow(() -> new EntityNotFoundException("Inventory of Product not found")));

        inventory.get().setQtyOnHand(inventory.get().getQtyOnHand()+backOrder.getQty());
        inventory.get().setQtyReserved(inventory.get().getQtyReserved()+backOrder.getQty());

        backOrder.setStatus(BackorderStatus.FULFILLED);
        inventoryRepo.save(inventory.get());
        salesOrderLineRepo.save(salesOrderLine);
        salesOrderRepo.save(salesOrder);
        backorderRepo.save(backOrder);
    }

    public List<PurchaseOrderResponseDTO> list() {
        return poRepo.findAll().stream().map(mapper::toResponse).toList();
    }

    public List<PurchaseOrderResponseDTO> findBySupplier(Long supplierId) {
        return poRepo.findBySupplier_Id(supplierId).stream().map(mapper::toResponse).toList();
    }

    public void delete(Long id) {
        PurchaseOrder entity = poRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PurchaseOrder not found"));
        poRepo.delete(entity);
    }
}
