package com.spring.logitrack.service;

import com.spring.logitrack.dto.inventory.InventoryCreateDTO;
import com.spring.logitrack.dto.product.ProductCreateDTO;
import com.spring.logitrack.dto.product.ProductResponseDTO;
import com.spring.logitrack.entity.Product;
import com.spring.logitrack.exception.DuplicateResourceException;
import com.spring.logitrack.mapper.ProductMapper;
import com.spring.logitrack.repository.ProductRepository;
import com.spring.logitrack.repository.SalesOrderLineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository repo;
    private final ProductMapper mapper;
    private final InventoryService inventoryService;
    private final SalesOrderLineRepository lineRepository;
    private final S3Service s3Service;

    @Autowired
    public ProductService(ProductRepository repo, ProductMapper mapper, InventoryService inventoryService, SalesOrderLineRepository salesOrderLineRepository, S3Service s3Service) {
        this.repo = repo;
        this.mapper = mapper;
        this.inventoryService = inventoryService;
        this.lineRepository = salesOrderLineRepository;
        this.s3Service = s3Service;
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
                    .orElseThrow(() -> new RuntimeException("Product not Found"));
            mapper.patch(product, dto);
            return mapper.toResponse(repo.save(product));
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("products_sku_key")) {
                throw new DuplicateResourceException("SKU already exists: " + dto.getSku());
            }
            throw e;
        }
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
