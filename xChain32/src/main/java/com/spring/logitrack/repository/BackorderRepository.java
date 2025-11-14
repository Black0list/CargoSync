package com.spring.logitrack.repository;

import com.spring.logitrack.entity.Backorder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BackorderRepository extends JpaRepository<Backorder, Long> {
    List<Backorder> findBySalesOrder_Id(Long salesOrderId);
    List<Backorder> findByStatus(String status);
}
