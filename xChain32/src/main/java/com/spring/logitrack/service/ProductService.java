package com.spring.logitrack.service;

import com.spring.logitrack.dto.inventory.InventoryCreateDTO;
import com.spring.logitrack.dto.product.ProductCreateDTO;
import com.spring.logitrack.dto.product.ProductResponseDTO;
import com.spring.logitrack.entity.Inventory;
import com.spring.logitrack.entity.Product;
import com.spring.logitrack.entity.SalesOrderLine;
import com.spring.logitrack.entity.enums.OrderStatus;
import com.spring.logitrack.exception.BusinessException;
import com.spring.logitrack.exception.DuplicateResourceException;
import com.spring.logitrack.exception.ResourceNotFoundException;
import com.spring.logitrack.mapper.ProductMapper;
import com.spring.logitrack.repository.InventoryRepository;
import com.spring.logitrack.repository.ProductRepository;
import com.spring.logitrack.repository.SalesOrderLineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository repo;
    private final ProductMapper mapper;
    private final InventoryService inventoryService;
    private final InventoryRepository inventoryRepo;
    private final SalesOrderLineRepository lineRepository;
    private final S3Service s3Service;

    @Autowired
    public ProductService(
            ProductRepository repo,
            ProductMapper mapper,
            InventoryService inventoryService,
            SalesOrderLineRepository salesOrderLineRepository,
            S3Service s3Service,
            InventoryRepository inventoryRepo
    ) {
        this.repo = repo;
        this.mapper = mapper;
        this.inventoryService = inventoryService;
        this.lineRepository = salesOrderLineRepository;
        this.s3Service = s3Service;
        this.inventoryRepo = inventoryRepo;
    }


    public List<ProductResponseDTO> list() {
        return repo.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public ProductResponseDTO get(Long id) {
        Product product = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not Found"));

        if (product.isActive()) {
            return mapper.toResponse(product);
        } else {
            throw new RuntimeException("Product is unavailable");
        }
    }

    public ProductResponseDTO create(ProductCreateDTO dto) {
        try {
            if (dto.getImageUrls() == null || dto.getImageUrls().isEmpty()) {
                throw new IllegalArgumentException("At least one image file is required.");
            }

            List<String> urls = dto.getImageUrls().stream()
                    .map(s3Service::uploadFile)
                    .toList();

            Product product = mapper.toEntity(dto);
            product.setImageUrls(urls);

            Product saved = repo.save(product);

            InventoryCreateDTO inventoryDTO = new InventoryCreateDTO();
            inventoryDTO.setProductId(saved.getId());
            inventoryDTO.setQtyReserved(0);
            inventoryDTO.setQtyOnHand(0);
            inventoryDTO.setWarehouseId(dto.getWarehouseId());
            inventoryService.create(inventoryDTO);

            return mapper.toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("products_sku_key")) {
                throw new DuplicateResourceException("SKU already exists: " + dto.getSku());
            }
            throw e;
        }
    }


    public ProductResponseDTO update(Long id, ProductCreateDTO dto) {
        try {
            Product product = repo.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not Found"));

            List<SalesOrderLine> lines =  lineRepository.findAllByProduct_Id(product.getId());

            for (SalesOrderLine line : lines){
                if(Objects.nonNull(line.getSalesOrder())){
                    if(line.getSalesOrder().getStatus().equals(OrderStatus.CREATED) || line.getSalesOrder().getStatus().equals(OrderStatus.RESERVED)){
                        throw new BusinessException("Product still related to a sales order with status : "+line.getSalesOrder().getStatus());
                    }
                }

                if(line.getQtyReserved() > 0){
                    throw new BusinessException("Product has been reserved cant be deactivated");
                }
            }


            mapper.patch(product, dto);
            return mapper.toResponse(repo.save(product));
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("products_sku_key")) {
                throw new DuplicateResourceException("SKU already exists: " + dto.getSku());
            }
            throw e;
        }
    }

    public ProductResponseDTO updateStatus(String sku, boolean status){
            Product product = repo.findBySku(sku)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not Found"));

            if(!status){
                List<SalesOrderLine> lines =  lineRepository.findAllByProduct_Id(product.getId());
                List<Inventory> inventories = inventoryRepo.findAllByProduct_Id(product.getId());

                for (Inventory inv : inventories){
                    if(inv.getQtyReserved() > 0 || inv.getQtyOnHand() > 0){
                        throw new BusinessException("Product Still have qty in inventories");
                    }
                }

                for (SalesOrderLine line : lines){
                    System.out.println(line.getSalesOrder());
                    if(Objects.nonNull(line.getSalesOrder())){
                        if(line.getSalesOrder().getStatus().equals(OrderStatus.CREATED) || line.getSalesOrder().getStatus().equals(OrderStatus.RESERVED) || line.getSalesOrder().getStatus().equals(OrderStatus.BACKORDER)){
                            throw new BusinessException("Product still related to a sales order with status : "+line.getSalesOrder().getStatus());
                        }
                    }

                    if(line.getQtyReserved() > 0){
                        throw new BusinessException("Product has been reserved cant be deactivated");
                    }
                }
            }

            product.setActive(status);
            return mapper.toResponse(repo.save(product));
    }

    public void delete(Long id, boolean hard) {
        Product product = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not Found"));
        if (hard) {
            boolean isUsed = lineRepository.existsByProduct(product);
            if(isUsed){
                throw new RuntimeException("cant delete product,its still related to some commands");
            } else {
                repo.delete(product);
            }
        } else {
            product.setActive(false);
            repo.save(product);
        }
    }
}
