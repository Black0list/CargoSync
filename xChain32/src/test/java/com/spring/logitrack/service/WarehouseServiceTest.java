package com.spring.logitrack.service;

import com.spring.logitrack.dto.warehouse.WarehouseCreateDTO;
import com.spring.logitrack.dto.warehouse.WarehouseResponseDTO;
import com.spring.logitrack.entity.SalesOrder;
import com.spring.logitrack.entity.Inventory;
import com.spring.logitrack.entity.User;
import com.spring.logitrack.entity.Warehouse;
import com.spring.logitrack.entity.enums.Role;
import com.spring.logitrack.mapper.WarehouseMapper;
import com.spring.logitrack.repository.UserRepository;
import com.spring.logitrack.repository.WarehouseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock private WarehouseRepository repository;
    @Mock private WarehouseMapper mapper;
    @Mock private UserRepository userRepo;
    @Mock private UserService userService;

    @InjectMocks private WarehouseService service;

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    @Test
    void create_success() {
        WarehouseCreateDTO dto = new WarehouseCreateDTO();
        dto.setCode("WH1");
        dto.setName("Main");
        dto.setLocation("City");
        dto.setManagerId(10L);

        User manager = new User();
        manager.setId(10L);
        manager.setRole(Role.WAREHOUSE_MANAGER);
        manager.setActive(true);

        Warehouse saved = Warehouse.builder()
                .id(1L)
                .code("WH1")
                .name("Main")
                .location("City")
                .active(true)
                .manager(manager)
                .build();

        WarehouseResponseDTO response = new WarehouseResponseDTO();
        response.setId(1L);

        when(repository.existsByCode("WH1")).thenReturn(false);
        when(userRepo.findByIdAndRole(10L, Role.WAREHOUSE_MANAGER)).thenReturn(Optional.of(manager));
        when(repository.save(any(Warehouse.class))).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        WarehouseResponseDTO result = service.create(dto);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void create_codeAlreadyExists() {
        WarehouseCreateDTO dto = new WarehouseCreateDTO();
        dto.setCode("WH1");

        when(repository.existsByCode("WH1")).thenReturn(true);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Warehouse code already exists");
    }

    @Test
    void create_managerNotFound() {
        WarehouseCreateDTO dto = new WarehouseCreateDTO();
        dto.setCode("WH1");
        dto.setManagerId(99L);

        when(repository.existsByCode("WH1")).thenReturn(false);
        when(userRepo.findByIdAndRole(99L, Role.WAREHOUSE_MANAGER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Manager not found");
    }

    @Test
    void create_managerNotActive() {
        WarehouseCreateDTO dto = new WarehouseCreateDTO();
        dto.setCode("WH1");
        dto.setManagerId(12L);

        User manager = new User();
        manager.setId(12L);
        manager.setActive(false);

        when(repository.existsByCode("WH1")).thenReturn(false);
        when(userRepo.findByIdAndRole(12L, Role.WAREHOUSE_MANAGER)).thenReturn(Optional.of(manager));

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Manager is not active");
    }

    // -------------------------------------------------------------------------
    // LIST
    // -------------------------------------------------------------------------
    @Test
    void list_success() {
        Warehouse entity = new Warehouse();
        entity.setId(1L);

        WarehouseResponseDTO dto = new WarehouseResponseDTO();
        dto.setId(1L);

        when(repository.findAll()).thenReturn(List.of(entity));
        when(mapper.toResponse(entity)).thenReturn(dto);

        var list = service.list();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getId()).isEqualTo(1L);
    }

    // -------------------------------------------------------------------------
    // GET BY ID
    // -------------------------------------------------------------------------
    @Test
    void getById_success() {
        Warehouse entity = new Warehouse();
        entity.setId(5L);

        WarehouseResponseDTO dto = new WarehouseResponseDTO();
        dto.setId(5L);

        when(repository.findById(5L)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(dto);

        var result = service.getById(5L);

        assertThat(result.getId()).isEqualTo(5L);
    }

    @Test
    void getById_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Warehouse not found");
    }

    // -------------------------------------------------------------------------
    // GET BY NAME
    // -------------------------------------------------------------------------
    @Test
    void getByName_success() {
        Warehouse entity = new Warehouse();
        entity.setId(1L);

        WarehouseResponseDTO dto = new WarehouseResponseDTO();
        dto.setId(1L);

        when(repository.findByName("WH")).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(dto);

        var result = service.getByName("WH");

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getByName_notFound() {
        when(repository.findByName("NOT_EXIST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByName("NOT_EXIST"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Warehouse not found");
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------
    @Test
    void update_success() {
        WarehouseCreateDTO dto = new WarehouseCreateDTO();
        dto.setCode("WH-NEW");

        Warehouse entity = new Warehouse();
        entity.setId(1L);
        entity.setCode("OLD");

        Warehouse saved = new Warehouse();
        saved.setId(1L);
        saved.setCode("WH-NEW");

        WarehouseResponseDTO response = new WarehouseResponseDTO();
        response.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.existsByCode("WH-NEW")).thenReturn(false);

        // simulate patch
        doAnswer(inv -> {
            Warehouse w = inv.getArgument(0);
            WarehouseCreateDTO d = inv.getArgument(1);
            w.setCode(d.getCode());
            return null;
        }).when(mapper).patch(entity, dto);

        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        var result = service.update(1L, dto);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void update_notFound() {
        WarehouseCreateDTO dto = new WarehouseCreateDTO();

        when(repository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(10L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Warehouse not found");
    }

    @Test
    void update_codeExists() {
        WarehouseCreateDTO dto = new WarehouseCreateDTO();
        dto.setCode("NEW");

        Warehouse entity = new Warehouse();
        entity.setId(1L);
        entity.setCode("OLD");

        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.existsByCode("NEW")).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Warehouse code already exists");
    }


    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------
    @Test
    void delete_success() {
        Warehouse entity = new Warehouse();
        entity.setId(1L);

        // no sales, no inventories
        entity.setSales(List.of());
        entity.setInventories(List.of());

        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        service.delete(1L);

        verify(repository).delete(entity);
    }

    @Test
    void delete_hasSalesOrInventories_throws() {
        Warehouse entity = new Warehouse();
        entity.setId(1L);

        entity.setSales(List.of(new SalesOrder()));
        entity.setInventories(List.of()); // also fails if inventories not empty

        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot delete warehouse, it must be deactivated");
    }

    @Test
    void delete_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Warehouse not found");
    }
}
