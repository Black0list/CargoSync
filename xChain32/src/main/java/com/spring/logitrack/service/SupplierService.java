package com.spring.logitrack.service;

import com.spring.logitrack.dto.supplier.SupplierCreateDTO;
import com.spring.logitrack.dto.supplier.SupplierResponseDTO;
import com.spring.logitrack.entity.Supplier;
import com.spring.logitrack.mapper.SupplierMapper;
import com.spring.logitrack.repository.SupplierRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierService {

    private final SupplierRepository repo;
    private final SupplierMapper mapper;

    public SupplierResponseDTO create(SupplierCreateDTO dto) {
        if (repo.existsByEmail(dto.getEmail())) {
            throw new DataIntegrityViolationException("Email already exists");
        }

        Supplier entity = mapper.toEntity(dto);
        Supplier saved = repo.save(entity);
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<SupplierResponseDTO> list() {
        return repo.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SupplierResponseDTO getById(Long id) {
        Supplier entity = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found"));
        return mapper.toResponse(entity);
    }

    public SupplierResponseDTO update(Long id, SupplierCreateDTO dto) {
        Supplier entity = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found"));

        if (dto.getEmail() != null && !dto.getEmail().equalsIgnoreCase(entity.getEmail())
                && repo.existsByEmail(dto.getEmail())) {
            throw new DataIntegrityViolationException("Email already exists");
        }

        mapper.patch(entity, dto);
        return mapper.toResponse(repo.save(entity));
    }

    public void delete(Long id) {
        Supplier entity = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found"));
        repo.delete(entity);
    }
}
