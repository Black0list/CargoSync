package com.spring.logitrack.service;

import com.spring.logitrack.dto.inventory.InventoryCreateDTO;
import com.spring.logitrack.dto.product.ProductCreateDTO;
import com.spring.logitrack.dto.product.ProductResponseDTO;
import com.spring.logitrack.entity.Inventory;
import com.spring.logitrack.entity.Product;
import com.spring.logitrack.entity.SalesOrder;
import com.spring.logitrack.entity.SalesOrderLine;
import com.spring.logitrack.entity.enums.OrderStatus;
import com.spring.logitrack.exception.BusinessException;
import com.spring.logitrack.exception.DuplicateResourceException;
import com.spring.logitrack.exception.ResourceNotFoundException;
import com.spring.logitrack.mapper.ProductMapper;
import com.spring.logitrack.repository.InventoryRepository;
import com.spring.logitrack.repository.ProductRepository;
import com.spring.logitrack.repository.SalesOrderLineRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository repo;
    @Mock private ProductMapper mapper;
    @Mock private InventoryService inventoryService;
    @Mock private InventoryRepository inventoryRepo;
    @Mock private SalesOrderLineRepository lineRepo;
    @Mock private S3Service s3Service;

    @InjectMocks private ProductService service;

    MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.png",
            "image/png",
            "hello".getBytes()
    );

    // -------------------------------------------------------------------
    // LIST
    // -------------------------------------------------------------------
    @Test
    void list_success() {
        Product p = new Product();
        p.setId(1L);
        ProductResponseDTO r = new ProductResponseDTO();
        r.setId(1L);

        when(repo.findAll()).thenReturn(List.of(p));
        when(mapper.toResponse(p)).thenReturn(r);

        var result = service.list();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    // -------------------------------------------------------------------
    // GET
    // -------------------------------------------------------------------
    @Test
    void get_success() {
        Product p = new Product();
        p.setId(1L);
        p.setActive(true);

        ProductResponseDTO r = new ProductResponseDTO();
        r.setId(1L);

        when(repo.findById(1L)).thenReturn(Optional.of(p));
        when(mapper.toResponse(p)).thenReturn(r);

        var result = service.get(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void get_inactive() {
        Product p = new Product();
        p.setActive(false);

        when(repo.findById(1L)).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> service.get(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Product is unavailable");
    }

    @Test
    void get_notFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Product not Found");
    }

    // -------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------
    @Test
    void create_success() {
        ProductCreateDTO dto = new ProductCreateDTO();
        dto.setImageUrls(List.of(file));
        dto.setWarehouseId(20L);
        dto.setSku("SKU-1");

        Product entity = new Product();
        entity.setId(1L);

        Product saved = new Product();
        saved.setId(1L);

        ProductResponseDTO response = new ProductResponseDTO();
        response.setId(1L);

        when(s3Service.uploadFile(file)).thenReturn("url1");
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repo.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        var result = service.create(dto);

        assertThat(result.getId()).isEqualTo(1L);
        verify(inventoryService).create(any(InventoryCreateDTO.class));
    }

    @Test
    void create_noImages() {
        ProductCreateDTO dto = new ProductCreateDTO();
        dto.setImageUrls(List.of());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least one image file is required.");
    }

    @Test
    void create_duplicateSku() {
        ProductCreateDTO dto = new ProductCreateDTO();
        dto.setSku("XYZ");
        dto.setWarehouseId(1L);

        // MUST have at least one fake image
        dto.setImageUrls(List.of(file));

        // Mock S3 returning a URL
        when(s3Service.uploadFile(file)).thenReturn("uploaded-url");

        // Map DTO → product entity
        Product entity = new Product();
        when(mapper.toEntity(dto)).thenReturn(entity);

        // Force SKU duplicate constraint
        when(repo.save(any()))
                .thenThrow(new DataIntegrityViolationException("products_sku_key"));

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("SKU already exists");
    }


    // -------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------
    @Test
    void update_success() {
        ProductCreateDTO dto = new ProductCreateDTO();
        dto.setSku("NEW");

        Product product = new Product();
        product.setId(1L);

        Product saved = new Product();
        saved.setId(1L);

        ProductResponseDTO response = new ProductResponseDTO();
        response.setId(1L);

        when(repo.findById(1L)).thenReturn(Optional.of(product));
        when(lineRepo.findAllByProduct_Id(1L)).thenReturn(List.of());
        doNothing().when(mapper).patch(product, dto);
        when(repo.save(product)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        var result = service.update(1L, dto);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void update_productNotFound() {
        ProductCreateDTO dto = new ProductCreateDTO();

        when(repo.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(10L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not Found");
    }

    @Test
    void update_reservedQty_throws() {
        ProductCreateDTO dto = new ProductCreateDTO();

        Product product = new Product();
        product.setId(1L);

        SalesOrderLine line = new SalesOrderLine();
        line.setQtyReserved(2);

        when(repo.findById(1L)).thenReturn(Optional.of(product));
        when(lineRepo.findAllByProduct_Id(1L)).thenReturn(List.of(line));

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Product has been reserved cant be deactivated");
    }

    @Test
    void update_salesOrderActive_throws() {
        ProductCreateDTO dto = new ProductCreateDTO();

        Product product = new Product();
        product.setId(1L);

        SalesOrder so = new SalesOrder();
        so.setStatus(OrderStatus.CREATED);

        SalesOrderLine line = new SalesOrderLine();
        line.setSalesOrder(so);
        line.setQtyReserved(0);

        when(repo.findById(1L)).thenReturn(Optional.of(product));
        when(lineRepo.findAllByProduct_Id(1L)).thenReturn(List.of(line));

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Product still related");
    }

    // -------------------------------------------------------------------
    // UPDATE STATUS
    // -------------------------------------------------------------------
    @Test
    void updateStatus_deactivate_inventoryQtyExists() {
        Product p = new Product();
        p.setId(1L);

        Inventory inv = new Inventory();
        inv.setQtyOnHand(5);

        when(repo.findBySku("SKU")).thenReturn(Optional.of(p));
        when(inventoryRepo.findAllByProduct_Id(1L)).thenReturn(List.of(inv));

        assertThatThrownBy(() -> service.updateStatus("SKU", false))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Product Still have qty in inventories");
    }

    @Test
    void updateStatus_deactivate_salesOrderActive() {
        Product p = new Product();
        p.setId(1L);

        SalesOrder so = new SalesOrder();
        so.setStatus(OrderStatus.CREATED);

        SalesOrderLine line = new SalesOrderLine();
        line.setSalesOrder(so);

        when(repo.findBySku("SKU")).thenReturn(Optional.of(p));
        when(inventoryRepo.findAllByProduct_Id(1L)).thenReturn(List.of());
        when(lineRepo.findAllByProduct_Id(1L)).thenReturn(List.of(line));

        assertThatThrownBy(() -> service.updateStatus("SKU", false))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void updateStatus_success() {
        Product p = new Product();
        p.setId(1L);
        p.setActive(true);

        Product saved = new Product();
        saved.setId(1L);
        saved.setActive(false);

        ProductResponseDTO response = new ProductResponseDTO();
        response.setId(1L);

        when(repo.findBySku("SKU")).thenReturn(Optional.of(p));
        when(inventoryRepo.findAllByProduct_Id(1L)).thenReturn(List.of());
        when(lineRepo.findAllByProduct_Id(1L)).thenReturn(List.of());
        when(repo.save(p)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        var result = service.updateStatus("SKU", false);

        assertThat(result.getId()).isEqualTo(1L);
    }

    // -------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------
    @Test
    void delete_hardUsed_throws() {
        Product p = new Product();
        when(repo.findById(1L)).thenReturn(Optional.of(p));
        when(lineRepo.existsByProduct(p)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L, true))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("cant delete product");
    }

    @Test
    void delete_hardSuccess() {
        Product p = new Product();
        when(repo.findById(1L)).thenReturn(Optional.of(p));
        when(lineRepo.existsByProduct(p)).thenReturn(false);

        service.delete(1L, true);

        verify(repo).delete(p);
    }

    @Test
    void delete_softSuccess() {
        Product p = new Product();
        p.setActive(true);

        when(repo.findById(1L)).thenReturn(Optional.of(p));

        service.delete(1L, false);

        assertThat(p.isActive()).isFalse();
        verify(repo).save(p);
    }

    @Test
    void delete_notFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L, true))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Product not Found");
    }
}
