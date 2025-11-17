package com.spring.logitrack.controller;

import com.spring.logitrack.dto.inventory.InventoryCreateDTO;
import com.spring.logitrack.dto.inventory.InventoryResponseDTO;
import com.spring.logitrack.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class InventoryControllerTest {

    @Mock
    private InventoryService service;

    @InjectMocks
    private InventoryController controller;

    private InventoryCreateDTO createDTO;
    private InventoryResponseDTO responseDTO;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        createDTO = new InventoryCreateDTO();

        responseDTO = new InventoryResponseDTO();
        responseDTO.setId(1L);
    }

    @Test
    void create_success() {
        when(service.create(any())).thenReturn(responseDTO);

        ResponseEntity<InventoryResponseDTO> result = controller.create(createDTO);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void list_success() {
        when(service.list()).thenReturn(List.of(responseDTO));

        ResponseEntity<List<InventoryResponseDTO>> result = controller.list();

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).hasSize(1);
    }

    @Test
    void getById_success() {
        when(service.getById(10L)).thenReturn(responseDTO);

        ResponseEntity<InventoryResponseDTO> result = controller.getById(10L);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void update_success() {
        when(service.update(eq(5L), any())).thenReturn(responseDTO);

        ResponseEntity<InventoryResponseDTO> result = controller.update(5L, createDTO);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void patch_success() {
        when(service.adjust(eq(3L), eq(20L))).thenReturn(responseDTO);

        ResponseEntity<InventoryResponseDTO> result = controller.patch(3L, 20L);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void delete_success() {
        doNothing().when(service).delete(7L);

        ResponseEntity<Void> result = controller.delete(7L);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        verify(service).delete(7L);
    }
}
