package com.spring.logitrack.service;

import com.spring.logitrack.dto.product.ProductCreateDTO;
import com.spring.logitrack.dto.product.ProductResponseDTO;
import com.spring.logitrack.entity.Product;
import com.spring.logitrack.exception.DuplicateResourceException;
import com.spring.logitrack.mapper.ProductMapper;
import com.spring.logitrack.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository repo;
    private final ProductMapper mapper;

    @Autowired
    public ProductService(ProductRepository repo, ProductMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
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
            Product product = mapper.toEntity(dto);
            Product saved = repo.save(product);
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
        if (hard) repo.delete(product);
        else {
            product.setActive(false);
            repo.save(product);
        }
    }
}
