package com.spring.logitrack.service;

import com.spring.logitrack.dto.order.OrderCreateDTO;
import com.spring.logitrack.dto.order.SimpleOrderResponseDTO;
import com.spring.logitrack.entity.Product;
import com.spring.logitrack.entity.SimpleOrder;
import com.spring.logitrack.mapper.SimpleOrderMapper;
import com.spring.logitrack.repository.ProductRepository;
import com.spring.logitrack.repository.SimpleOrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SimpleOrderService {

    private final SimpleOrderRepository simpleOrderRepository;
    private final ProductRepository productRepository;
    private final SimpleOrderMapper mapper;

    @Transactional
    public SimpleOrderResponseDTO create(OrderCreateDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        SimpleOrder entity = mapper.toEntity(dto);
        entity.setProduct(product);
        entity.setCreatedAt(LocalDateTime.now()); // ensure timestamp

        try {
            SimpleOrder saved = simpleOrderRepository.save(entity);
            return mapper.toResponse(saved);
        } catch (Exception e) {
            throw new DataIntegrityViolationException("Error while saving simple order: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public SimpleOrderResponseDTO getById(Long id) {
        SimpleOrder entity = simpleOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Simple order not found"));
        return mapper.toResponse(entity);
    }

    @Transactional
    public SimpleOrderResponseDTO update(Long id, OrderCreateDTO dto) {
        SimpleOrder existing = simpleOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Simple order not found"));

        mapper.patch(existing, dto);

        if (dto.getProductId() != null) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found"));
            existing.setProduct(product);
        }

        return mapper.toResponse(simpleOrderRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        SimpleOrder existing = simpleOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Simple order not found"));
        simpleOrderRepository.delete(existing);
    }
}
