package com.spring.logitrack.repository;

import com.spring.logitrack.entity.BackOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BackorderRepository extends JpaRepository<BackOrder, Long> {
    List<BackOrder> findBySalesOrder_Id(Long salesOrderId);
    List<BackOrder> findByStatus(String status);
}
