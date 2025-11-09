package com.spring.logitrack.entity;

import com.spring.logitrack.entity.Product;
import com.spring.logitrack.entity.enums.BackorderStatus;

import java.time.LocalDateTime;

public interface OrderType {

    Long getId();

    Product getProduct();
    void setProduct(Product product);

    int getQty();
    void setQty(int qty);

    int getExtraQty();
    void setExtraQty(int extraQty);

    LocalDateTime getCreatedAt();
    void setCreatedAt(LocalDateTime createdAt);

    BackorderStatus getStatus();
}
