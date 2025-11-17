package com.spring.logitrack.controller;

import com.spring.logitrack.dto.shipment.ShipmentCreateDTO;
import com.spring.logitrack.dto.shipment.ShipmentResponseDTO;
import com.spring.logitrack.entity.enums.ShipmentStatus;
import com.spring.logitrack.service.ShipmentService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
class ShipmentControllerTest {

    @Mock
    private ShipmentService service;

    @InjectMocks
    private ShipmentController controller;

    private ShipmentCreateDTO createDTO;
    private ShipmentResponseDTO responseDTO;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        createDTO = new ShipmentCreateDTO();

        responseDTO = new ShipmentResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setStatus(ShipmentStatus.PLANNED);
    }

    @Test
    void create_success() {
        when(service.create(any())).thenReturn(responseDTO);

        ResponseEntity<ShipmentResponseDTO> result = controller.create(createDTO);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void getById_success() {
        when(service.getById(10L)).thenReturn(responseDTO);

        ResponseEntity<ShipmentResponseDTO> result = controller.getById(10L);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody().getStatus()).isEqualTo(ShipmentStatus.PLANNED);
    }

    @Test
    void list_success() {
        when(service.list()).thenReturn(List.of(responseDTO));

        ResponseEntity<List<ShipmentResponseDTO>> result = controller.list();

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).hasSize(1);
    }

    @Test
    void update_success() {
        when(service.update(eq(5L), any())).thenReturn(responseDTO);

        ResponseEntity<ShipmentResponseDTO> result = controller.update(5L, createDTO);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void updateStatus_success() {
        when(service.updateStatus(3L, ShipmentStatus.DELIVERED)).thenReturn(responseDTO);

        ResponseEntity<ShipmentResponseDTO> result = controller.updateStatus(3L, ShipmentStatus.DELIVERED);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void delete_success() {
        doNothing().when(service).delete(7L);

        ResponseEntity<Void> result = controller.delete(7L);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
    }
}
