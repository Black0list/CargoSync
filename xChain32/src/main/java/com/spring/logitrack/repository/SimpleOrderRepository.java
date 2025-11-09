package com.spring.logitrack.repository;

import com.spring.logitrack.entity.SimpleOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SimpleOrderRepository extends JpaRepository<SimpleOrder, Long> {
}
