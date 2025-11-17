package com.spring.logitrack.controller;

import com.spring.logitrack.dto.supplier.SupplierCreateDTO;
import com.spring.logitrack.dto.supplier.SupplierResponseDTO;
import com.spring.logitrack.service.SupplierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
class SupplierControllerTest {

    @Mock
    private SupplierService service;

    @InjectMocks
    private SupplierController controller;

    private SupplierCreateDTO createDTO;
    private SupplierResponseDTO responseDTO;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        createDTO = new SupplierCreateDTO();
        createDTO.setName("Test Supplier");
        createDTO.setEmail("test@supplier.com");
        createDTO.setContact("123456789");

        responseDTO = new SupplierResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setName("Test Supplier");
        responseDTO.setEmail("test@supplier.com");
        responseDTO.setContact("123456789");
    }

    // ======================================================================
    // CREATE
    // ======================================================================
    @Test
    void create_success() {
        when(service.create(any())).thenReturn(responseDTO);

        var result = controller.create(createDTO);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(((SupplierResponseDTO) result.getBody()).getId()).isEqualTo(1L);
    }

    @Test
    void create_exception() {
        when(service.create(any())).thenThrow(new RuntimeException("Invalid data"));

        var result = controller.create(createDTO);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(((Map<?, ?>) result.getBody()).get("message"))
                .isEqualTo("Invalid data");
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
    void get_success() {
        when(service.getById(5L)).thenReturn(responseDTO);

        var result = controller.get(5L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((SupplierResponseDTO) result.getBody()).getId()).isEqualTo(1L);
    }

    @Test
    void get_exception() {
        when(service.getById(5L)).thenThrow(new RuntimeException("Supplier not found"));

        try {
            controller.get(5L);
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("Supplier not found");
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
        assertThat(((SupplierResponseDTO) result.getBody()).getId()).isEqualTo(1L);
    }

    @Test
    void update_exception() {
        when(service.update(eq(3L), any()))
                .thenThrow(new RuntimeException("Update failed"));

        var result = controller.update(3L, createDTO);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(((Map<?, ?>) result.getBody()).get("message"))
                .isEqualTo("Update failed");
    }

    // ======================================================================
    // DELETE
    // ======================================================================
    @Test
    void delete_success() {
        doNothing().when(service).delete(9L);

        var result = controller.delete(9L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void delete_exception() {
        doThrow(new RuntimeException("Delete failed"))
                .when(service)
                .delete(9L);

        try {
            controller.delete(9L);
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("Delete failed");
        }
    }
}
