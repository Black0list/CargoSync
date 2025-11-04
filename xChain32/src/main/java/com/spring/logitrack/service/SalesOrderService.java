package com.spring.logitrack.service;

import com.spring.logitrack.dto.backorder.BackorderCreateDTO;
import com.spring.logitrack.dto.inventoryMovement.InventoryMovementCreateDTO;
import com.spring.logitrack.dto.salesOrder.SalesOrderCreateDTO;
import com.spring.logitrack.dto.salesOrder.SalesOrderResponseDTO;
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

    public SalesOrderResponseDTO create(SalesOrderCreateDTO dto) {
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


        dto.getLines().forEach(lineDTO -> {
            Product product = productRepo.findById(lineDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if(!product.isActive()){
                throw new RuntimeException("Product with name : "+product.getName()+" ,is not active");
            }
            Optional<Inventory> inventory = Optional.ofNullable(inventoryRepository.findInventoryByProduct_IdAndWarehouse_Id(product.getId(), warehouse.getId()));

            if(inventory.isEmpty()){
                throw new RuntimeException("Inventory of product with sku : "+product.getSku()+",not Found");
            }

            int QtyReserved;

            if(lineDTO.getQtyOrdered() > inventory.get().getQtyOnHand()){
                QtyReserved = inventory.get().getQtyOnHand();

                int qtyNeeded = lineDTO.getQtyOrdered() - inventory.get().getQtyOnHand();

                inventory.get().setQtyReserved(inventory.get().getQtyOnHand() + inventory.get().getQtyReserved());
                inventory.get().setQtyOnHand(0);


                // CHECK OTHER WAREHOUSES FOR EXTRA QUANTITY
                Optional<Inventory> inventoryHelper = inventoryService.getHelperInventory(product.getId(), qtyNeeded, inventory.get().getWarehouse().getId());

                if(inventoryHelper.isPresent()){
                    // MAKE EXCHANGE BETWEEN WAREHOUSES
                    MakeExchangeBetweenWareHouses(inventoryHelper.get(), inventory.get(), qtyNeeded);
                    QtyReserved += qtyNeeded;
                } else {
                    // MAKE BACKORDER
                    System.out.println("=========================== Supplier Called ===========================");
                    BackorderCreateDTO backorderCreateDTO = new BackorderCreateDTO();
                    backorderCreateDTO.setQtyBackordered(qtyNeeded);
                    backorderCreateDTO.setSalesOrderId(saved.getId());
                    backorderCreateDTO.setStatus(BackorderStatus.PENDING);
                    backorderCreateDTO.setExtraQty(0);
                    backorderCreateDTO.setProductId(product.getId());

                    backorderService.create(backorderCreateDTO);
                }

            } else {
                QtyReserved = lineDTO.getQtyOrdered();
                inventory.get().setQtyReserved(inventory.get().getQtyReserved() + lineDTO.getQtyOrdered());
                inventory.get().setQtyOnHand(inventory.get().getQtyOnHand() - lineDTO.getQtyOrdered());
                order.setStatus(OrderStatus.RESERVED);
            }

            SalesOrderLine line = new SalesOrderLine();
            line.setSalesOrder(order);
            line.setProduct(product);
            line.setPrice(product.getPrice());
            line.setQtyOrdered(lineDTO.getQtyOrdered());
            line.setQtyReserved(QtyReserved);
            order.getLines().add(line);

            inventoryRepository.save(inventory.get());
        });

        try {
            return mapper.toResponse(orderRepo.save(order));
        } catch (Exception e) {
            throw new DataIntegrityViolationException("Error while saving order: " + e.getMessage());
        }
    }

    @Transactional()
    protected void MakeExchangeBetweenWareHouses(Inventory inventoryHelper, Inventory inventory, int qty) {
        inventoryHelper.setQtyOnHand(inventoryHelper.getQtyOnHand() - qty);
        inventory.setQtyReserved(inventory.getQtyReserved() + qty);

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
