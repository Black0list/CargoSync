package com.spring.logitrack.service;

import com.spring.logitrack.dto.inventoryMovement.InventoryMovementCreateDTO;
import com.spring.logitrack.dto.inventoryMovement.InventoryMovementResponseDTO;
import com.spring.logitrack.entity.Inventory;
import com.spring.logitrack.entity.InventoryMovement;
import com.spring.logitrack.mapper.InventoryMovementMapper;
import com.spring.logitrack.repository.InventoryMovementRepository;
import com.spring.logitrack.repository.InventoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


@ActiveProfiles("test")
class InventoryMovementServiceTest {

    @Mock private InventoryMovementRepository movementRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private InventoryMovementMapper mapper;

    @InjectMocks private InventoryMovementService service;

    private Inventory inventory;
    private InventoryMovement movement;
    private InventoryMovementCreateDTO createDTO;
    private InventoryMovementResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        inventory = new Inventory();
        inventory.setId(1L);

        movement = new InventoryMovement();
        movement.setId(10L);

        createDTO = new InventoryMovementCreateDTO();
        createDTO.setInventoryId(1L);
        createDTO.setQty(5);

        responseDTO = new InventoryMovementResponseDTO();
        responseDTO.setId(10L);
        responseDTO.setQty(5);
    }

    @Test
    void create_success() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        when(mapper.toEntity(createDTO)).thenReturn(movement);
        when(movementRepository.save(movement)).thenReturn(movement);
        when(mapper.toResponse(movement)).thenReturn(responseDTO);

        InventoryMovementResponseDTO result = service.create(createDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        verify(inventoryRepository).findById(1L);
        verify(mapper).toEntity(createDTO);
        verify(movementRepository).save(movement);
        verify(mapper).toResponse(movement);
    }

    @Test
    void create_inventoryNotFound() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(createDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Inventory not found");
        verify(movementRepository, never()).save(any());
    }

    @Test
    void list_success() {
        InventoryMovement m1 = new InventoryMovement(); m1.setId(1L);
        InventoryMovement m2 = new InventoryMovement(); m2.setId(2L);

        InventoryMovementResponseDTO r1 = new InventoryMovementResponseDTO(); r1.setId(1L);
        InventoryMovementResponseDTO r2 = new InventoryMovementResponseDTO(); r2.setId(2L);

        when(movementRepository.findAll()).thenReturn(List.of(m1, m2));
        when(mapper.toResponse(m1)).thenReturn(r1);
        when(mapper.toResponse(m2)).thenReturn(r2);

        List<InventoryMovementResponseDTO> result = service.list();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(InventoryMovementResponseDTO::getId).containsExactly(1L, 2L);
        verify(movementRepository).findAll();
    }

    @Test
    void findByInventory_success() {
        InventoryMovement m = new InventoryMovement(); m.setId(3L);
        InventoryMovementResponseDTO r = new InventoryMovementResponseDTO(); r.setId(3L);

        when(movementRepository.findByInventory_Id(1L)).thenReturn(List.of(m));
        when(mapper.toResponse(m)).thenReturn(r);

        List<InventoryMovementResponseDTO> result = service.findByInventory(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(3L);
        verify(movementRepository).findByInventory_Id(1L);
    }

    @Test
    void delete_success() {
        when(movementRepository.findById(10L)).thenReturn(Optional.of(movement));
        service.delete(10L);
        verify(movementRepository).delete(movement);
    }

    @Test
    void delete_notFound() {
        when(movementRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Movement not found");
        verify(movementRepository, never()).delete(any());
    }
}
