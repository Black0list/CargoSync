package com.spring.logitrack.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "warehouses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String code;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "warehouse", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private List<Inventory> inventories = new ArrayList<>();

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SalesOrder> sales = new ArrayList<>();
}
