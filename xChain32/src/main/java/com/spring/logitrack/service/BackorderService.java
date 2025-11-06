package com.spring.logitrack.service;

import com.spring.logitrack.dto.backorder.BackorderCreateDTO;
import com.spring.logitrack.dto.backorder.BackorderResponseDTO;
import com.spring.logitrack.entity.BackOrder;
import com.spring.logitrack.entity.Product;
import com.spring.logitrack.entity.SalesOrder;
import com.spring.logitrack.mapper.BackorderMapper;
import com.spring.logitrack.repository.BackorderRepository;
import com.spring.logitrack.repository.ProductRepository;
import com.spring.logitrack.repository.SalesOrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class BackorderService {

    private final BackorderRepository backorderRepository;
    private final ProductRepository productRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final BackorderMapper mapper;

    public BackorderResponseDTO create(BackorderCreateDTO dto) {
        SalesOrder order = salesOrderRepository.findById(dto.getSalesOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Sales order not found"));
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        BackOrder entity = mapper.toEntity(dto);
        entity.setSalesOrder(order);
        entity.setProduct(product);

        try {
            BackOrder saved = backorderRepository.save(entity);
            return mapper.toResponse(saved);
        } catch (Exception e) {
            throw new DataIntegrityViolationException("Error while saving backorder: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<BackorderResponseDTO> list() {
        return backorderRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BackorderResponseDTO> findByOrder(Long salesOrderId) {
        return backorderRepository.findBySalesOrder_Id(salesOrderId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public BackorderResponseDTO updateStatus(Long id, BackorderCreateDTO dto) {
        BackOrder existing = backorderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Backorder not found"));

        if(Objects.isNull(dto.getQty())) dto.setQty(existing.getQty());

        mapper.patch(existing, dto);

        System.out.println("Existing : ==="+existing);
        System.out.println("DTO : ==="+dto);


        if (dto.getSalesOrderId() != null) {
            SalesOrder order = salesOrderRepository.findById(dto.getSalesOrderId())
                    .orElseThrow(() -> new EntityNotFoundException("Sales order not found"));
            existing.setSalesOrder(order);
        }

        if (dto.getProductId() != null) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found"));
            existing.setProduct(product);
        }

        BackOrder saved = backorderRepository.save(existing);
        return mapper.toResponse(saved);
    }

    public void delete(Long id) {
        BackOrder entity = backorderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Backorder not found"));
        backorderRepository.delete(entity);
    }
}
