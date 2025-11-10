package com.spring.logitrack.repository;

import com.spring.logitrack.entity.Product;
import com.spring.logitrack.entity.SalesOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesOrderLineRepository extends JpaRepository<SalesOrderLine, Long> {
    boolean existsByProduct(Product product);
    List<SalesOrderLine> findAllByProduct_Id(Long product_id);
}
