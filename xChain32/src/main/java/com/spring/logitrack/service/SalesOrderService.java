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
import jakarta.persistence.EntityNotFoundException;
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

        List<String> warnings = new ArrayList<>();

        for (SalesOrderLineCreateDTO lineDTO : dto.getLines()) {
            Product product = productRepo.findById(lineDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (!product.isActive()) {
                warnings.add("Product '" + product.getName() + "' is inactive and was skipped.");
                continue;
            }

            // No stock verification yet — only create order lines
            SalesOrderLine line = new SalesOrderLine();
            line.setSalesOrder(order);
            line.setProduct(product);
            line.setPrice(product.getPrice());
            line.setQtyOrdered(lineDTO.getQtyOrdered());
            line.setQtyReserved(0); // will be updated later during reservation
            order.getLines().add(line);
        }

        try {
            SalesOrder saved = orderRepo.save(order);
            SalesOrderResponseDTO response = mapper.toResponse(saved);
            return mapper.toResponse(response, warnings);
        } catch (Exception e) {
            throw new DataIntegrityViolationException("Error while saving order: " + e.getMessage());
        }
    }

    // =============== PHASE 2 : RESERVE =================== //
    public SalesOrderResponseWithWarningsDTO reserve(Long orderId) {
        SalesOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Sales order not found"));

        if(order.getStatus().equals(OrderStatus.RESERVED)){
            throw new RuntimeException("This Order already reserved");
        }

        List<String> warnings = new ArrayList<>();
        Warehouse warehouse = order.getWarehouse();

        for (SalesOrderLine line : order.getLines()) {
            Product product = line.getProduct();

            if (!product.isActive()) {
                warnings.add("Product '" + product.getName() + "' is inactive and was skipped during reservation.");
                continue;
            }

            Optional<Inventory> optInv = inventoryRepository
                    .findTopByProduct_IdAndWarehouse_IdOrderByIdDesc(product.getId(), warehouse.getId());

            if (optInv.isEmpty()) {
                warnings.add("Inventory not found for SKU '" + product.getSku() + "'.");
                continue;
            }

            Inventory inventory = optInv.get();
            int available = inventory.getQtyOnHand() - inventory.getQtyReserved();

            if (line.getQtyOrdered() > available) {
                int qtyNeeded = line.getQtyOrdered() - available;
                System.out.println("Qyy : "+qtyNeeded);

                Optional<Inventory> optHelper = inventoryService
                        .getHelperInventory(product.getId(), qtyNeeded, warehouse.getId());

                if (optHelper.isPresent()) {
                    Inventory helper = optHelper.get();
                    MakeExchangeBetweenWareHouses(helper, inventory, qtyNeeded);
                    inventory.setQtyOnHand(inventory.getQtyOnHand() + qtyNeeded);
                    inventory.setQtyReserved(inventory.getQtyReserved() + line.getQtyOrdered());
                    line.setQtyReserved(line.getQtyOrdered());
                    order.setStatus(OrderStatus.RESERVED);
                } else {
                    // Create backorder
                    BackorderCreateDTO backorder = new BackorderCreateDTO();
                    backorder.setSalesOrderId(order.getId());
                    backorder.setProductId(product.getId());
                    backorder.setQty(qtyNeeded);
                    backorder.setExtraQty(0);
                    backorder.setStatus(BackorderStatus.PENDING);
                    backorderService.create(backorder);

                    inventory.setQtyReserved(inventory.getQtyReserved() + available);
                    line.setQtyReserved(available);
                    order.setStatus(OrderStatus.BACKORDER);

                    warnings.add("Backorder created for SKU '" + product.getSku()
                            + "' due to insufficient stock (" + qtyNeeded + " units).");
                }

            } else {
                // enough stock
                inventory.setQtyReserved(inventory.getQtyReserved() + line.getQtyOrdered());
                line.setQtyReserved(line.getQtyOrdered());
                order.setStatus(OrderStatus.RESERVED);
            }

            inventoryRepository.save(inventory);
        }

        SalesOrder saved = orderRepo.save(order);
        return mapper.toResponse(mapper.toResponse(saved), warnings);
    }

    // Exchange stock between warehouses
    @Transactional
    protected void MakeExchangeBetweenWareHouses(Inventory inventoryHelper, Inventory inventory, int qty) {
        inventoryHelper.setQtyOnHand(inventoryHelper.getQtyOnHand() - qty);

        InventoryMovementCreateDTO outDTO = new InventoryMovementCreateDTO();
        outDTO.setInventoryId(inventoryHelper.getId());
        outDTO.setType(MovementType.OUTBOUND);
        outDTO.setQty(qty);

        InventoryMovementCreateDTO inDTO = new InventoryMovementCreateDTO();
        inDTO.setInventoryId(inventory.getId());
        inDTO.setType(MovementType.INBOUND);
        inDTO.setQty(qty);

        inventoryRepository.save(inventoryHelper);
        inventoryRepository.save(inventory);
        inventoryMovementService.create(outDTO);
        inventoryMovementService.create(inDTO);
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

    public SalesOrderResponseWithWarningsDTO updateStatus(Long id, String status) {
        SalesOrder order = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Sales order not found"));

        if(status.equals(OrderStatus.RESERVED.name())){
            return reserve(order.getId());
        } else {
            order.setStatus(Enum.valueOf(com.spring.logitrack.entity.enums.OrderStatus.class, status));
            return mapper.toResponse(mapper.toResponse(orderRepo.save(order)), new ArrayList<>());
        }
    }
}
