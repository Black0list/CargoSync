package com.spring.logitrack.service;

import com.spring.logitrack.dto.warehouse.WarehouseCreateDTO;
import com.spring.logitrack.dto.warehouse.WarehouseResponseDTO;
import com.spring.logitrack.entity.Warehouse;
import com.spring.logitrack.mapper.WarehouseMapper;
import com.spring.logitrack.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WarehouseService {

    private final WarehouseRepository repository;
    private final WarehouseMapper mapper;

    public WarehouseResponseDTO create(WarehouseCreateDTO dto) {
        if (repository.existsByCode(dto.getCode()))
            throw new IllegalArgumentException("Warehouse code already exists");
        Warehouse entity = mapper.toEntity(dto);
        return mapper.toResponse(repository.save(entity));
    }

    public List<WarehouseResponseDTO> list() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    public WarehouseResponseDTO getById(Long id) {
        Warehouse entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found"));
        return mapper.toResponse(entity);
    }

    public WarehouseResponseDTO getByName(String name) {
        Warehouse entity = repository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found"));
        return mapper.toResponse(entity);
    }

    public WarehouseResponseDTO update(Long id, WarehouseCreateDTO dto) {
        Warehouse entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found"));

        if (dto.getCode() != null && !entity.getCode().equalsIgnoreCase(dto.getCode())
                && repository.existsByCode(dto.getCode().toUpperCase()))
            throw new IllegalArgumentException("Warehouse code already exists");

        mapper.patch(entity, dto);
        return mapper.toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        Warehouse entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found"));
        repository.delete(entity);
    }
}
