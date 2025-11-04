package com.spring.logitrack.service;

import com.spring.logitrack.dto.backorder.BackorderCreateDTO;
import com.spring.logitrack.dto.backorder.BackorderResponseDTO;
import com.spring.logitrack.entity.*;
import com.spring.logitrack.mapper.BackorderMapper;
import com.spring.logitrack.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BackorderService {

    private final BackorderRepository backorderRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final ProductRepository productRepository;
    private final BackorderMapper mapper;

    public BackorderResponseDTO create(BackorderCreateDTO dto) {
        SalesOrder order = salesOrderRepository.findById(dto.getSalesOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Sales order not found"));
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        Backorder backorder = mapper.toEntity(dto);
        backorder.setSalesOrder(order);
        backorder.setProduct(product);

        return mapper.toResponse(backorderRepository.save(backorder));
    }

    @Transactional(readOnly = true)
    public List<BackorderResponseDTO> list() {
        return backorderRepository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BackorderResponseDTO> findByOrder(Long salesOrderId) {
        return backorderRepository.findBySalesOrder_Id(salesOrderId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public BackorderResponseDTO updateStatus(Long id, BackorderCreateDTO dto) {
        Backorder entity = backorderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Backorder not found"));
        entity.setStatus(dto.getStatus());
        entity.setExtraQty(dto.getExtraQty());
        entity.setQtyBackordered(dto.getQtyBackordered());
        return mapper.toResponse(backorderRepository.save(entity));
    }

    public void delete(Long id) {
        Backorder entity = backorderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Backorder not found"));
        backorderRepository.delete(entity);
    }
}
