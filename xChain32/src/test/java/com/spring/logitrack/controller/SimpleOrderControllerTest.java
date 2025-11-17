package com.spring.logitrack.controller;

import com.spring.logitrack.dto.order.OrderCreateDTO;
import com.spring.logitrack.dto.order.SimpleOrderResponseDTO;
import com.spring.logitrack.entity.enums.BackorderStatus;
import com.spring.logitrack.service.SimpleOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
class SimpleOrderControllerTest {

    @Mock
    private SimpleOrderService service;

    @InjectMocks
    private SimpleOrderController controller;

    private OrderCreateDTO createDTO;
    private SimpleOrderResponseDTO responseDTO;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        createDTO = new OrderCreateDTO();
        createDTO.setType("SIMPLE");
        createDTO.setProductId(5L);
        createDTO.setQty(10);
        createDTO.setExtraQty(0);
        createDTO.setStatus(BackorderStatus.PENDING);
        createDTO.setCreatedAt(LocalDateTime.now());

        responseDTO = new SimpleOrderResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setProductId(5L);
        responseDTO.setQty(10);
        responseDTO.setStatus(BackorderStatus.PENDING);
    }

    // ==========================================================
    // CREATE
    // ==========================================================
    @Test
    void create_success() {
        when(service.create(any())).thenReturn(responseDTO);

        var result = controller.create(createDTO);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void create_exception() {
        when(service.create(any())).thenThrow(new RuntimeException("Create failed"));

        try {
            controller.create(createDTO);
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("Create failed");
        }
    }

    // ==========================================================
    // GET BY ID
    // ==========================================================
    @Test
    void getById_success() {
        when(service.getById(10L)).thenReturn(responseDTO);

        var result = controller.getById(10L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void getById_exception() {
        when(service.getById(10L)).thenThrow(new RuntimeException("Not found"));

        try {
            controller.getById(10L);
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("Not found");
        }
    }

    // ==========================================================
    // UPDATE
    // ==========================================================
    @Test
    void update_success() {
        when(service.update(eq(7L), any())).thenReturn(responseDTO);

        var result = controller.update(7L, createDTO);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void update_exception() {
        when(service.update(eq(7L), any()))
                .thenThrow(new RuntimeException("Update failed"));

        try {
            controller.update(7L, createDTO);
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("Update failed");
        }
    }

    // ==========================================================
    // DELETE
    // ==========================================================
    @Test
    void delete_success() {
        doNothing().when(service).delete(5L);

        var result = controller.delete(5L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void delete_exception() {
        doThrow(new RuntimeException("Delete failed"))
                .when(service)
                .delete(22L);

        try {
            controller.delete(22L);
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("Delete failed");
        }
    }
}
