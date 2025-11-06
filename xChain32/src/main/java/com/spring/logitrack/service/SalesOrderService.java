package com.spring.logitrack.service;

import com.spring.logitrack.dto.backorder.BackorderCreateDTO;
import com.spring.logitrack.dto.inventoryMovement.InventoryMovementCreateDTO;
import com.spring.logitrack.dto.salesOrder.SalesOrderCreateDTO;
import com.spring.logitrack.dto.salesOrder.SalesOrderResponseDTO;
import com.spring.logitrack.dto.salesOrder.SalesOrderResponseWithWarningsDTO;
import com.spring.logitrack.dto.salesOrderLine.SalesOrderLineCreateDTO;
import com.spring.logitrack.entity.*;
import com.spring.logitrack.entity.enums.BackorderStatus;
import com.spring.logitrack.entity.enums.MovementType;
import com.spring.logitrack.entity.enums.OrderStatus;
import com.spring.logitrack.mapper.SalesOrderMapper;
import com.spring.logitrack.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SalesOrderService {

    private final SalesOrderRepository orderRepo;
    private final UserRepository userRepo;
    private final WarehouseRepository warehouseRepo;
    private final ProductRepository productRepo;
    private final SalesOrderMapper mapper;
    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;
    private final InventoryMovementService inventoryMovementService;
    private final BackorderService backorderService;

    @Autowired
    public SalesOrderService(SalesOrderRepository orderRepo, UserRepository userRepo,
                             WarehouseRepository warehouseRepo, ProductRepository productRepo,
                             SalesOrderMapper mapper, InventoryRepository inventoryRepository, InventoryService inventoryService , InventoryMovementService inventoryMovementService, BackorderService backorderService) {
        this.orderRepo = orderRepo;
        this.userRepo = userRepo;
        this.warehouseRepo = warehouseRepo;
        this.productRepo = productRepo;
        this.mapper = mapper;
        this.inventoryRepository = inventoryRepository;
        this.inventoryService = inventoryService;
        this.inventoryMovementService = inventoryMovementService;
        this.backorderService = backorderService;
    }

    public List<SalesOrderResponseDTO> list() {
        return orderRepo.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    public SalesOrderResponseDTO get(Long id) {
        SalesOrder order = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Sales order not found"));
        return mapper.toResponse(order);
    }

    public SalesOrderResponseWithWarningsDTO create(SalesOrderCreateDTO dto) {
        User client = userRepo.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found"));
        Warehouse warehouse = warehouseRepo.findById(dto.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        SalesOrder order = SalesOrder.builder()
                .client(client)
                .warehouse(warehouse)
                .country(dto.getCountry())
                .city(dto.getCity())
                .street(dto.getStreet())
                .zip(dto.getZip())
                .status(OrderStatus.CREATED)
                .build();

        SalesOrder saved = orderRepo.save(order);

        List<String> warnings = new ArrayList<>();

        for (SalesOrderLineCreateDTO lineDTO : dto.getLines()) {

            // 1. Retrieve product
            Product product = productRepo.findById(lineDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // Skip inactive product but record warning
            if (!product.isActive()) {
                String warning = "Product with name '" + product.getName() + "' is inactive and was skipped.";
                warnings.add(warning);
                continue;
            }

            // 2. Get inventory
            Optional<Inventory> optInventory =
                    inventoryRepository.findTopByProduct_IdAndWarehouse_IdOrderByIdDesc(
                            product.getId(), warehouse.getId());

            if (optInventory.isEmpty()) {
                String warning = "Inventory not found for product SKU '" + product.getSku() + "'.";
                warnings.add(warning);
                continue;
            }

            Inventory inventory = optInventory.get();
            int availableQty = inventory.getQtyOnHand() - inventory.getQtyReserved();
            int qtyReserved;

            // 3. Check if enough stock available
            if (lineDTO.getQtyOrdered() > availableQty) {

                qtyReserved = availableQty;
                int qtyNeeded = lineDTO.getQtyOrdered() - availableQty;

                // 4. Try helper warehouse
                Optional<Inventory> optHelper = inventoryService
                        .getHelperInventory(product.getId(), qtyNeeded, inventory.getWarehouse().getId());

                if (optHelper.isPresent()) {
                    Inventory helper = optHelper.get();

                    // Exchange stock between warehouses
                    MakeExchangeBetweenWareHouses(helper, inventory, qtyNeeded);

                    qtyReserved += qtyNeeded;
                    inventory.setQtyReserved(inventory.getQtyReserved() + lineDTO.getQtyOrdered());
                    inventory.setQtyOnHand(inventory.getQtyOnHand() + qtyNeeded);
                    order.setStatus(OrderStatus.RESERVED);

                } else {
                    // Create backorder
                    System.out.println("Quantity is ========== : "+qtyNeeded);
                    BackorderCreateDTO backorder = new BackorderCreateDTO();
                    backorder.setQty(qtyNeeded);
                    backorder.setSalesOrderId(saved.getId());
                    backorder.setStatus(BackorderStatus.PENDING);
                    backorder.setExtraQty(0);
                    backorder.setProductId(product.getId());
                    backorderService.create(backorder);

                    inventory.setQtyReserved(inventory.getQtyReserved() + qtyReserved);

                    String warning = "Backorder created for product SKU '" + product.getSku()
                            + "' due to insufficient quantity (" + qtyNeeded + " units).";
                    warnings.add(warning);
                    order.setStatus(OrderStatus.BACKORDER);
                }

            } else {
                // Sufficient stock
                qtyReserved = lineDTO.getQtyOrdered();
                inventory.setQtyReserved(inventory.getQtyReserved() + lineDTO.getQtyOrdered());
                inventory.setQtyOnHand(inventory.getQtyOnHand() + lineDTO.getQtyOrdered());
                order.setStatus(OrderStatus.RESERVED);
            }

            // 5. Add line
            SalesOrderLine line = new SalesOrderLine();
            line.setSalesOrder(order);
            line.setProduct(product);
            line.setPrice(product.getPrice());
            line.setQtyOrdered(lineDTO.getQtyOrdered());
            line.setQtyReserved(qtyReserved);
            order.getLines().add(line);

            // 6. Save inventory
            inventoryRepository.save(inventory);
        }

        try {
            SalesOrder savedOrder = orderRepo.save(order);
            SalesOrderResponseDTO responseDTO = mapper.toResponse(savedOrder);
            return mapper.toResponse(responseDTO, warnings);
        } catch (Exception e) {
            throw new DataIntegrityViolationException("Error while saving order: " + e.getMessage());
        }
    }

    @Transactional()
    protected void MakeExchangeBetweenWareHouses(Inventory inventoryHelper, Inventory inventory, int qty) {
        inventoryHelper.setQtyOnHand(inventoryHelper.getQtyOnHand() - qty);

        InventoryMovementCreateDTO invMvtHelperDTO = new InventoryMovementCreateDTO();
        invMvtHelperDTO.setInventoryId(inventoryHelper.getId());
        invMvtHelperDTO.setType(MovementType.OUTBOUND);
        invMvtHelperDTO.setQty(qty);

        InventoryMovementCreateDTO invMvtDTO = new InventoryMovementCreateDTO();
        invMvtDTO.setInventoryId(inventory.getId());
        invMvtDTO.setType(MovementType.INBOUND);
        invMvtDTO.setQty(qty);

        inventoryRepository.save(inventoryHelper);
        inventoryRepository.save(inventory);
        inventoryMovementService.create(invMvtHelperDTO);
        inventoryMovementService.create(invMvtDTO);
    }

    public SalesOrderResponseDTO update(Long id, SalesOrderCreateDTO dto) {
        SalesOrder existing = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Sales order not found"));

        if((existing.getStatus().equals(OrderStatus.SHIPPED) || existing.getStatus().equals(OrderStatus.DELIVERED)) && dto.getStatus().equals(OrderStatus.CANCELLED)){
            throw new RuntimeException("You cant Cant cancel the order its "+existing.getStatus().toString().toLowerCase());
        }

        mapper.patch(existing, dto);
        return mapper.toResponse(orderRepo.save(existing));
    }

    public void delete(Long id, boolean hard) {
        SalesOrder order = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Sales order not found"));
        orderRepo.delete(order);
    }

    public SalesOrderResponseDTO updateStatus(Long id, String status) {
        SalesOrder order = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Sales order not found"));
        order.setStatus(Enum.valueOf(com.spring.logitrack.entity.enums.OrderStatus.class, status));
        return mapper.toResponse(orderRepo.save(order));
    }
}
