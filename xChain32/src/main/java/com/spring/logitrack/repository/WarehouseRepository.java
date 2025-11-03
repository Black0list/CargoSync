package com.spring.logitrack.repository;

import com.spring.logitrack.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    boolean existsByCode(String code);
    Optional<Warehouse> findByName(String name);
}
