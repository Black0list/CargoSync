package com.spring.logitrack.controller;

import com.spring.logitrack.dto.order.OrderCreateDTO;
import com.spring.logitrack.dto.order.OrderResponseDTO;
import com.spring.logitrack.service.BackorderService;
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
class BackorderControllerTest {

    @Mock
    private BackorderService service;

    @InjectMocks
    private BackorderController controller;

    private OrderCreateDTO createDTO;
    private OrderResponseDTO responseDTO;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        createDTO = new OrderCreateDTO();
        responseDTO = new OrderResponseDTO();
        responseDTO.setId(1L);
    }

    @Test
    void create_success() {
        when(service.create(any())).thenReturn(responseDTO);

        ResponseEntity<OrderResponseDTO> result = controller.create(createDTO);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void list_success() {
        when(service.list()).thenReturn(List.of(responseDTO));

        ResponseEntity<List<OrderResponseDTO>> result = controller.list();

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).hasSize(1);
    }

    @Test
    void findByOrder_success() {
        when(service.findByOrder(10L)).thenReturn(List.of(responseDTO));

        ResponseEntity<List<OrderResponseDTO>> result = controller.findByOrder(10L);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).hasSize(1);
    }

    @Test
    void update_success() {
        when(service.updateStatus(eq(5L), any())).thenReturn(responseDTO);

        ResponseEntity<OrderResponseDTO> result = controller.update(5L, createDTO);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void delete_success() {
        doNothing().when(service).delete(3L);

        ResponseEntity<Void> result = controller.delete(3L);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        verify(service).delete(3L);
    }
}
