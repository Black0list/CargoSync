package com.spring.logitrack.repository;

import com.spring.logitrack.entity.Product;
import com.spring.logitrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
}
