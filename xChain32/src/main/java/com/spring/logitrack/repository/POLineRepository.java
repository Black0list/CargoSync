package com.spring.logitrack.repository;

import com.spring.logitrack.entity.POLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface POLineRepository extends JpaRepository<POLine, Long> { }
