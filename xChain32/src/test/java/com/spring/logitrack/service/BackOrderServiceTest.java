package com.spring.logitrack.service;

import com.spring.logitrack.dto.order.OrderCreateDTO;
import com.spring.logitrack.dto.order.OrderResponseDTO;
import com.spring.logitrack.entity.BackOrder;
import com.spring.logitrack.entity.Product;
import com.spring.logitrack.entity.SalesOrder;
import com.spring.logitrack.mapper.BackOrderMapper;
import com.spring.logitrack.repository.BackOrderRepository;
import com.spring.logitrack.repository.ProductRepository;
import com.spring.logitrack.repository.SalesOrderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class BackOrderServiceTest {

    @Mock private BackOrderRepository backorderRepository;
    @Mock private ProductRepository productRepository;
    @Mock private SalesOrderRepository salesOrderRepository;
    @Mock private BackOrderMapper mapper;

    @InjectMocks private BackOrderService service;

    private OrderCreateDTO createDTO;
    private SalesOrder salesOrder;
    private Product product;
    private BackOrder backOrder;
    private OrderResponseDTO responseDTO;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        createDTO = new OrderCreateDTO();
        createDTO.setSalesOrderId(1L);
        createDTO.setProductId(2L);
        createDTO.setQty(5);

        salesOrder = new SalesOrder();
        salesOrder.setId(1L);

        product = new Product();
        product.setId(2L);

        backOrder = new BackOrder();
        backOrder.setId(10L);

        responseDTO = new OrderResponseDTO();
        responseDTO.setId(10L);
    }

    @Test
    void create_success() {
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(salesOrder));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(mapper.toEntity(createDTO)).thenReturn(backOrder);
        when(backorderRepository.save(backOrder)).thenReturn(backOrder);
        when(mapper.toResponse(backOrder)).thenReturn(responseDTO);

        OrderResponseDTO result = service.create(createDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        verify(backorderRepository).save(backOrder);
    }

    @Test
    void create_salesOrderNotFound() {
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(createDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Sales order not found");
    }

    @Test
    void create_productNotFound() {
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(salesOrder));
        when(productRepository.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(createDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void create_dataIntegrityViolation() {
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(salesOrder));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(mapper.toEntity(createDTO)).thenReturn(backOrder);
        when(backorderRepository.save(backOrder)).thenThrow(new RuntimeException("constraint"));
        assertThatThrownBy(() -> service.create(createDTO))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("Error while saving backorder");
    }

    @Test
    void list_success() {
        BackOrder b1 = new BackOrder(); b1.setId(1L);
        BackOrder b2 = new BackOrder(); b2.setId(2L);
        OrderResponseDTO r1 = new OrderResponseDTO(); r1.setId(1L);
        OrderResponseDTO r2 = new OrderResponseDTO(); r2.setId(2L);

        when(backorderRepository.findAll()).thenReturn(List.of(b1, b2));
        when(mapper.toResponse(b1)).thenReturn(r1);
        when(mapper.toResponse(b2)).thenReturn(r2);

        List<OrderResponseDTO> result = service.list();

        assertThat(result).hasSize(2);
        verify(backorderRepository).findAll();
    }

    @Test
    void findByOrder_success() {
        BackOrder b = new BackOrder(); b.setId(1L);
        OrderResponseDTO r = new OrderResponseDTO(); r.setId(1L);
        when(backorderRepository.findBySalesOrder_Id(1L)).thenReturn(List.of(b));
        when(mapper.toResponse(b)).thenReturn(r);

        List<OrderResponseDTO> result = service.findByOrder(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(backorderRepository).findBySalesOrder_Id(1L);
    }

    @Test
    void updateStatus_success() {
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setQty(3);
        when(backorderRepository.findById(10L)).thenReturn(Optional.of(backOrder));
        when(mapper.toResponse(any())).thenReturn(responseDTO);
        when(backorderRepository.save(backOrder)).thenReturn(backOrder);

        OrderResponseDTO result = service.updateStatus(10L, dto);

        assertThat(result).isNotNull();
        verify(backorderRepository).save(backOrder);
    }

    @Test
    void updateStatus_withSalesOrderAndProduct() {
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setQty(5);
        dto.setSalesOrderId(1L);
        dto.setProductId(2L);

        when(backorderRepository.findById(10L)).thenReturn(Optional.of(backOrder));
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(salesOrder));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(backorderRepository.save(backOrder)).thenReturn(backOrder);
        when(mapper.toResponse(backOrder)).thenReturn(responseDTO);

        OrderResponseDTO result = service.updateStatus(10L, dto);

        assertThat(result.getId()).isEqualTo(10L);
        verify(backorderRepository).save(backOrder);
    }

    @Test
    void updateStatus_notFound() {
        when(backorderRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updateStatus(99L, new OrderCreateDTO()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Backorder not found");
    }

    @Test
    void delete_success() {
        when(backorderRepository.findById(10L)).thenReturn(Optional.of(backOrder));
        service.delete(10L);
        verify(backorderRepository).delete(backOrder);
    }

    @Test
    void delete_notFound() {
        when(backorderRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Backorder not found");
        verify(backorderRepository, never()).delete(any());
    }
}
