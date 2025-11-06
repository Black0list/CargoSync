package com.spring.logitrack.service;


import com.spring.logitrack.dto.POLine.POLineCreateDTO;
import com.spring.logitrack.dto.purchaseOrder.PurchaseOrderCreateDTO;
import com.spring.logitrack.dto.purchaseOrder.PurchaseOrderResponseDTO;
import com.spring.logitrack.entity.*;
import com.spring.logitrack.entity.enums.POStatus;
import com.spring.logitrack.mapper.PurchaseOrderMapper;
import com.spring.logitrack.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;



@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseOrderService {

    private final PurchaseOrderRepository poRepo;
    private final SupplierRepository supplierRepo;
    private final ProductRepository productRepo;
    private final BackorderRepository backorderRepo;
    private final PurchaseOrderMapper mapper;

    public PurchaseOrderResponseDTO create(PurchaseOrderCreateDTO dto) {
        Supplier supplier = supplierRepo.findById(dto.getSupplierId())
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found"));

        PurchaseOrder po = mapper.toEntity(dto);
        po.setSupplier(supplier);

        // 🔥 Important: avoid persisting partially mapped lines from MapStruct
        po.getLines().clear();

        for (POLineCreateDTO lineDTO : dto.getLines()) {
            Product product = productRepo.findById(lineDTO.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found"));

            POLine line = POLine.builder()
                    .purchaseOrder(po)
                    .product(product)
                    .qty(lineDTO.getQty())
                    .price(product.getPrice())
                    .build();

            po.getLines().add(line);
        }

        return mapper.toResponse(poRepo.save(po));
    }



    public PurchaseOrderResponseDTO createFromBackOrder(Long backorderId, Long supplierId) {
        BackOrder backOrder = backorderRepo.findById(backorderId)
                .orElseThrow(() -> new EntityNotFoundException("BackOrder not found"));
        Supplier supplier = supplierRepo.findById(supplierId)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found"));

        PurchaseOrder po = PurchaseOrder.builder()
                .supplier(supplier)
                .status(POStatus.APPROVED)
                .build();

        POLine line = POLine.builder()
                .purchaseOrder(po)
                .product(backOrder.getProduct())
                .qty(backOrder.getQty())
                .price(backOrder.getProduct().getPrice())
                .build();

        po.getLines().add(line);
        return mapper.toResponse(poRepo.save(po));
    }


    public PurchaseOrderResponseDTO update(Long id, PurchaseOrderCreateDTO dto) {
        PurchaseOrder existing = poRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PurchaseOrder not found"));

        mapper.patch(existing, dto);

        if (dto.getSupplierId() != null) {
            Supplier supplier = supplierRepo.findById(dto.getSupplierId())
                    .orElseThrow(() -> new EntityNotFoundException("Supplier not found"));
            existing.setSupplier(supplier);
        }

        return mapper.toResponse(poRepo.save(existing));
    }

    public List<PurchaseOrderResponseDTO> list() {
        return poRepo.findAll().stream().map(mapper::toResponse).toList();
    }

    public List<PurchaseOrderResponseDTO> findBySupplier(Long supplierId) {
        return poRepo.findBySupplier_Id(supplierId).stream().map(mapper::toResponse).toList();
    }

    public void delete(Long id) {
        PurchaseOrder entity = poRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PurchaseOrder not found"));
        poRepo.delete(entity);
    }
}
