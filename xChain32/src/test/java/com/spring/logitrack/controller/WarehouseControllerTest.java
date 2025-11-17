package com.spring.logitrack.controller;

import com.spring.logitrack.dto.warehouse.WarehouseCreateDTO;
import com.spring.logitrack.dto.warehouse.WarehouseResponseDTO;
import com.spring.logitrack.service.WarehouseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
class WarehouseControllerTest {

    @Mock
    private WarehouseService service;

    @InjectMocks
    private WarehouseController controller;

    private WarehouseCreateDTO createDTO;
    private WarehouseResponseDTO responseDTO;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        createDTO = new WarehouseCreateDTO();
        createDTO.setCode("WH-01");
        createDTO.setName("Main Warehouse");
        createDTO.setLocation("Casablanca");
        createDTO.setManagerId(99L);
        createDTO.setActive(true);

        responseDTO = new WarehouseResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setCode("WH-01");
        responseDTO.setName("Main Warehouse");
        responseDTO.setLocation("Casablanca");
        responseDTO.setManager("John Manager");
        responseDTO.setActive(true);
    }

    // ======================================================================
    // CREATE
    // ======================================================================
    @Test
    void create_success() {
        when(service.create(any())).thenReturn(responseDTO);

        var result = controller.create(createDTO);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void create_exception() {
        when(service.create(any())).thenThrow(new RuntimeException("Invalid warehouse"));

        try {
            controller.create(createDTO);
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("Invalid warehouse");
        }
    }

    // ======================================================================
    // LIST
    // ======================================================================
    @Test
    void list_success() {
        when(service.list()).thenReturn(List.of(responseDTO));

        var result = controller.list();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((List<?>) result.getBody()).size()).isEqualTo(1);
    }

    // ======================================================================
    // GET BY ID
    // ======================================================================
    @Test
    void getById_success() {
        when(service.getById(5L)).thenReturn(responseDTO);

        var result = controller.getById(5L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void getById_exception() {
        when(service.getById(5L)).thenThrow(new RuntimeException("Warehouse not found"));

        try {
            controller.getById(5L);
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("Warehouse not found");
        }
    }

    // ======================================================================
    // UPDATE
    // ======================================================================
    @Test
    void update_success() {
        when(service.update(eq(3L), any())).thenReturn(responseDTO);

        var result = controller.update(3L, createDTO);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void update_exception() {
        when(service.update(eq(3L), any()))
                .thenThrow(new RuntimeException("Update failed"));

        try {
            controller.update(3L, createDTO);
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("Update failed");
        }
    }

    // ======================================================================
    // DELETE
    // ======================================================================
    @Test
    void delete_success() {
        doNothing().when(service).delete(10L);

        var result = controller.delete(10L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void delete_exception() {
        doThrow(new RuntimeException("Delete failed"))
                .when(service)
                .delete(10L);

        try {
            controller.delete(10L);
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("Delete failed");
        }
    }
}
