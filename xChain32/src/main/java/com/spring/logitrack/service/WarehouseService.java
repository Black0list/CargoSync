package com.spring.logitrack.service;

import com.spring.logitrack.dto.warehouse.WarehouseCreateDTO;
import com.spring.logitrack.dto.warehouse.WarehouseResponseDTO;
import com.spring.logitrack.entity.User;
import com.spring.logitrack.entity.Warehouse;
import com.spring.logitrack.entity.enums.Role;
import com.spring.logitrack.mapper.WarehouseMapper;
import com.spring.logitrack.repository.UserRepository;
import com.spring.logitrack.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class WarehouseService {

    private final WarehouseRepository repository;
    private final WarehouseMapper mapper;
    private final UserRepository userRepo;

    public WarehouseResponseDTO create(WarehouseCreateDTO dto) {
        if (repository.existsByCode(dto.getCode()))
            throw new IllegalArgumentException("Warehouse code already exists");

        Optional<User> user = userRepo.findByIdAndRole(dto.getManagerId(), Role.WAREHOUSE_MANAGER);

        if(user.isEmpty()){
            throw new NoSuchElementException("Manager not found");
        }

        if(!user.get().isActive()){
            throw new NoSuchElementException("Manager is not active");
        }

        Warehouse entity = Warehouse.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .active(true)
                .location(dto.getLocation())
                .manager(user.get()).build();

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

        boolean hasSales = !entity.getSales().isEmpty();
        boolean hasInventories = !entity.getInventories().isEmpty();

        if (hasSales || hasInventories) {
            throw new IllegalStateException("Cannot delete warehouse, it must be deactivated");
        }

        repository.delete(entity);
    }
}
