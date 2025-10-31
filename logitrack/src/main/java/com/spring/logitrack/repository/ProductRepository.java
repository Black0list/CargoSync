package com.spring.logitrack.repository;

import com.spring.logitrack.entity.Product;
import com.spring.logitrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
