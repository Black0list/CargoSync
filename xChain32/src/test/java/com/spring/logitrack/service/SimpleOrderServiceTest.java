package com.spring.logitrack.service;

import com.spring.logitrack.dto.order.OrderCreateDTO;
import com.spring.logitrack.dto.order.SimpleOrderResponseDTO;
import com.spring.logitrack.entity.Product;
import com.spring.logitrack.entity.SimpleOrder;
import com.spring.logitrack.mapper.SimpleOrderMapper;
import com.spring.logitrack.repository.ProductRepository;
import com.spring.logitrack.repository.SimpleOrderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimpleOrderServiceTest {

    @Mock
    private SimpleOrderRepository simpleOrderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SimpleOrderMapper mapper;

    @InjectMocks
    private SimpleOrderService service;

    // ---------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------
    @Test
    void create_success() {
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setProductId(10L);
        dto.setQty(5);

        Product product = Product.builder().id(10L).name("Test Product").build();

        SimpleOrder entity = new SimpleOrder();
        entity.setQty(dto.getQty());

        SimpleOrder saved = new SimpleOrder();
        saved.setId(1L);
        saved.setQty(dto.getQty());
        saved.setProduct(product);

        SimpleOrderResponseDTO response = new SimpleOrderResponseDTO();
        response.setId(1L);
        response.setQty(dto.getQty());
        response.setProductId(10L);

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(simpleOrderRepository.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        SimpleOrderResponseDTO result = service.create(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getProductId()).isEqualTo(10L);
    }

    @Test
    void create_productNotFound() {
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setProductId(99L);

        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Product not found");
    }

    @Test
    void create_repositoryThrowsException() {
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setProductId(10L);
        dto.setQty(5);

        Product product = Product.builder().id(10L).build();
        SimpleOrder entity = new SimpleOrder();

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(simpleOrderRepository.save(entity)).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("Error while saving simple order");
    }

    // ---------------------------------------------------------
    // GET BY ID
    // ---------------------------------------------------------
    @Test
    void getById_success() {
        SimpleOrder entity = new SimpleOrder();
        entity.setId(1L);
        entity.setQty(3);

        SimpleOrderResponseDTO response = new SimpleOrderResponseDTO();
        response.setId(1L);
        response.setQty(3);

        when(simpleOrderRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        SimpleOrderResponseDTO result = service.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getQty()).isEqualTo(3);
    }

    @Test
    void getById_notFound() {
        when(simpleOrderRepository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(77L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Simple order not found");
    }

    // ---------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------
    @Test
    void update_success() {
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setQty(20);
        dto.setProductId(5L);

        Product product = Product.builder().id(5L).name("Updated Product").build();

        SimpleOrder existing = new SimpleOrder();
        existing.setId(1L);
        existing.setQty(10);

        SimpleOrder updated = new SimpleOrder();
        updated.setId(1L);
        updated.setQty(20);
        updated.setProduct(product);

        SimpleOrderResponseDTO response = new SimpleOrderResponseDTO();
        response.setId(1L);
        response.setQty(20);
        response.setProductId(5L);

        when(simpleOrderRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        // Simulate patch
        doAnswer(inv -> {
            SimpleOrder e = inv.getArgument(0);
            OrderCreateDTO d = inv.getArgument(1);
            e.setQty(d.getQty());
            return null;
        }).when(mapper).patch(existing, dto);

        when(simpleOrderRepository.save(existing)).thenReturn(updated);
        when(mapper.toResponse(updated)).thenReturn(response);

        SimpleOrderResponseDTO result = service.update(1L, dto);

        assertThat(result.getQty()).isEqualTo(20);
        assertThat(result.getProductId()).isEqualTo(5L);
    }

    @Test
    void update_notFound() {
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setQty(5);

        when(simpleOrderRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(100L, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Simple order not found");
    }

    @Test
    void update_productNotFound() {
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setQty(5);
        dto.setProductId(999L);

        SimpleOrder existing = new SimpleOrder();
        existing.setId(1L);

        when(simpleOrderRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Product not found");
    }

    // ---------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------
    @Test
    void delete_success() {
        SimpleOrder order = new SimpleOrder();
        order.setId(1L);

        when(simpleOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        service.delete(1L);

        verify(simpleOrderRepository).delete(order);
    }

    @Test
    void delete_notFound() {
        when(simpleOrderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Simple order not found");
    }
}
