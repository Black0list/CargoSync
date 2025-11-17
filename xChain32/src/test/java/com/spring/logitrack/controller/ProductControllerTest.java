package com.spring.logitrack.controller;

import com.spring.logitrack.dto.product.ProductCreateDTO;
import com.spring.logitrack.dto.product.ProductResponseDTO;
import com.spring.logitrack.service.ProductService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
class ProductControllerTest {

    @Mock
    private ProductService service;

    @InjectMocks
    private ProductController controller;

    private ProductCreateDTO createDTO;
    private ProductResponseDTO responseDTO;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        createDTO = new ProductCreateDTO();

        responseDTO = new ProductResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setSku("ABC123");
    }

    @Test
    void list_success() {
        when(service.list()).thenReturn(List.of(responseDTO));

        var result = controller.list();

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat((List<?>) result.getBody()).hasSize(1);
    }

    @Test
    void list_exception() {
        when(service.list()).thenThrow(new RuntimeException("DB Error"));

        var result = controller.list();

        assertThat(result.getStatusCode().is5xxServerError()).isTrue();
    }

    @Test
    void get_success() {
        when(service.get(10L)).thenReturn(responseDTO);

        var result = controller.get(10L);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(((ProductResponseDTO) result.getBody()).getId()).isEqualTo(1L);
    }

    @Test
    void get_exception() {
        when(service.get(10L)).thenThrow(new RuntimeException("Not found"));

        var result = controller.get(10L);

        assertThat(result.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void create_success() {
        when(service.create(any())).thenReturn(responseDTO);

        var result = controller.create(createDTO);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(((ProductResponseDTO) result.getBody()).getId()).isEqualTo(1L);
    }

    @Test
    void create_exception() {
        when(service.create(any())).thenThrow(new RuntimeException("Invalid"));

        var result = controller.create(createDTO);

        assertThat(result.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void update_success() {
        when(service.update(eq(5L), any())).thenReturn(responseDTO);

        var result = controller.update(5L, createDTO);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(((ProductResponseDTO) result.getBody()).getId()).isEqualTo(1L);
    }

    @Test
    void update_exception() {
        when(service.update(eq(5L), any())).thenThrow(new RuntimeException("Error"));

        var result = controller.update(5L, createDTO);

        assertThat(result.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void patch_success() {
        when(service.updateStatus("SKU123", true)).thenReturn(responseDTO);

        var result = controller.update("SKU123", true);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void patch_exception() {
        when(service.updateStatus("SKU123", true)).thenThrow(new RuntimeException("Failed"));

        var result = controller.update("SKU123", true);

        assertThat(result.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void delete_success() {
        doNothing().when(service).delete(3L, false);

        var result = controller.delete(3L, false);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat((Map<String, String>) result.getBody())
                .containsEntry("message", "Product Successfully Deleted");
    }

    @Test
    void delete_exception() {
        doThrow(new RuntimeException("Delete error"))
                .when(service)
                .delete(3L, true);

        var result = controller.delete(3L, true);

        assertThat(result.getStatusCode().is4xxClientError()).isTrue();
    }
}
