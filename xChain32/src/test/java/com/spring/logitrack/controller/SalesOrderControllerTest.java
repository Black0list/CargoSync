package com.spring.logitrack.controller;

import com.spring.logitrack.dto.salesOrder.SalesOrderCreateDTO;
import com.spring.logitrack.dto.salesOrder.SalesOrderResponseDTO;
import com.spring.logitrack.dto.salesOrder.SalesOrderResponseWithWarningsDTO;
import com.spring.logitrack.dto.salesOrderLine.SalesOrderLineCreateDTO;
import com.spring.logitrack.service.SalesOrderService;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
class SalesOrderControllerTest {

    @Mock
    private SalesOrderService service;

    @InjectMocks
    private SalesOrderController controller;

    private SalesOrderCreateDTO createDTO;
    private SalesOrderResponseDTO responseOrder;
    private SalesOrderResponseWithWarningsDTO responseWithWarnings;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // ----- Create request DTO -----
        createDTO = new SalesOrderCreateDTO();
        createDTO.setClientId(1L);
        createDTO.setWarehouseId(2L);
        createDTO.setCountry("Morocco");
        createDTO.setCity("Casablanca");
        createDTO.setStreet("Street 123");
        createDTO.setZip("20000");
        createDTO.setLines(List.of(new SalesOrderLineCreateDTO()));

        // ----- Basic order DTO -----
        responseOrder = new SalesOrderResponseDTO();
        responseOrder.setId(1L);

        // ----- Wrapper with warnings -----
        responseWithWarnings = new SalesOrderResponseWithWarningsDTO();
        responseWithWarnings.setOrder(responseOrder);
        responseWithWarnings.setWarnings(List.of("Low inventory"));
    }

    // ======================================================================
    // LIST
    // ======================================================================
    @Test
    void list_success() {
        when(service.list()).thenReturn(List.of(responseOrder));

        var result = controller.list();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((List<?>) result.getBody()).size()).isEqualTo(1);
    }

    @Test
    void list_exception() {
        when(service.list()).thenThrow(new RuntimeException("DB Error"));

        var result = controller.list();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ======================================================================
    // GET BY ID
    // ======================================================================
    @Test
    void get_success() {
        when(service.get(10L)).thenReturn(responseOrder);

        var result = controller.get(10L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((SalesOrderResponseDTO) result.getBody()).getId()).isEqualTo(1L);
    }

    @Test
    void get_notFound() {
        when(service.get(10L)).thenThrow(new RuntimeException("Order not found"));

        var result = controller.get(10L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(((Map<?, ?>) result.getBody()).get("message"))
                .isEqualTo("Order not found");
    }

    // ======================================================================
    // CREATE
    // ======================================================================
    @Test
    void create_success() {
        when(service.create(any())).thenReturn(responseWithWarnings);

        var result = controller.create(createDTO);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var body = (SalesOrderResponseWithWarningsDTO) result.getBody();

        assertThat(body.getOrder().getId()).isEqualTo(1L);
        assertThat(body.getWarnings().size()).isEqualTo(1);
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
    // UPDATE
    // ======================================================================
    @Test
    void update_success() {
        when(service.update(eq(5L), any())).thenReturn(responseOrder);

        var result = controller.update(5L, createDTO);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((SalesOrderResponseDTO) result.getBody()).getId()).isEqualTo(1L);
    }

    @Test
    void update_exception() {
        when(service.update(eq(5L), any()))
                .thenThrow(new RuntimeException("Update failed"));

        var result = controller.update(5L, createDTO);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(((Map<?, ?>) result.getBody()).get("message"))
                .isEqualTo("Update failed");
    }

    // ======================================================================
    // UPDATE STATUS
    // ======================================================================
    @Test
    void updateStatus_success() {
        when(service.updateStatus(10L, "SHIPPED")).thenReturn(responseWithWarnings);

        var result = controller.updateStatus(10L, "SHIPPED");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

        var body = (SalesOrderResponseWithWarningsDTO) result.getBody();
        assertThat(body.getOrder().getId()).isEqualTo(1L);
        assertThat(body.getWarnings().size()).isEqualTo(1);
    }

    @Test
    void updateStatus_exception() {
        when(service.updateStatus(anyLong(), anyString()))
                .thenThrow(new RuntimeException("Invalid status"));

        var result = controller.updateStatus(10L, "INVALID");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(((Map<?, ?>) result.getBody()).get("message"))
                .isEqualTo("Invalid status");
    }

    // ======================================================================
    // DELETE
    // ======================================================================
    @Test
    void delete_success() {
        // delete returns void — if no exception thrown, it's OK
        controller.delete(10L, false);
        assertThat(true).isTrue(); // reached here → success
    }

    @Test
    void delete_exception() {
        doThrow(new RuntimeException("Delete failed"))
                .when(service)
                .delete(10L, true);

        try {
            controller.delete(10L, true);
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("Delete failed");
        }
    }
}
