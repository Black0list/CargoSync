package com.spring.logitrack.service;

import com.spring.logitrack.dto.supplier.SupplierCreateDTO;
import com.spring.logitrack.dto.supplier.SupplierResponseDTO;
import com.spring.logitrack.entity.Supplier;
import com.spring.logitrack.mapper.SupplierMapper;
import com.spring.logitrack.repository.SupplierRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository repo;

    @Mock
    private SupplierMapper mapper;

    @InjectMocks
    private SupplierService service;

    // --------------------------------------------------------
    // CREATE SUPPLIER
    // --------------------------------------------------------
    @Test
    void create_success() {
        SupplierCreateDTO dto = new SupplierCreateDTO();
        dto.setName("Test Supplier");
        dto.setEmail("test@test.com");
        dto.setContact("0611111111");

        Supplier entity = Supplier.builder()
                .id(1L)
                .name(dto.getName())
                .email(dto.getEmail())
                .contact(dto.getContact())
                .build();

        SupplierResponseDTO response = new SupplierResponseDTO();
        response.setId(1L);
        response.setName(dto.getName());
        response.setEmail(dto.getEmail());
        response.setContact(dto.getContact());

        when(repo.existsByEmail(dto.getEmail())).thenReturn(false);
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repo.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        SupplierResponseDTO result = service.create(dto);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(dto.getEmail());
    }

    @Test
    void create_emailAlreadyExists() {
        SupplierCreateDTO dto = new SupplierCreateDTO();
        dto.setName("Duplicate");
        dto.setEmail("taken@test.com");
        dto.setContact("0612345678");

        when(repo.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("Email already exists");
    }

    // --------------------------------------------------------
    // LIST SUPPLIERS
    // --------------------------------------------------------
    @Test
    void list_success() {
        Supplier entity = Supplier.builder()
                .id(1L).name("A").email("a@a.com").contact("0601010101").build();

        SupplierResponseDTO dto = new SupplierResponseDTO();
        dto.setId(1L);
        dto.setName("A");
        dto.setEmail("a@a.com");
        dto.setContact("0601010101");

        when(repo.findAll()).thenReturn(List.of(entity));
        when(mapper.toResponse(entity)).thenReturn(dto);

        List<SupplierResponseDTO> result = service.list();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("A");
    }

    // --------------------------------------------------------
    // GET BY ID
    // --------------------------------------------------------
    @Test
    void getById_success() {
        Supplier entity = Supplier.builder()
                .id(1L).name("A").email("a@a.com").contact("0666223344").build();

        SupplierResponseDTO dto = new SupplierResponseDTO();
        dto.setId(1L);
        dto.setName("A");
        dto.setEmail("a@a.com");
        dto.setContact("0666223344");

        when(repo.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(dto);

        SupplierResponseDTO result = service.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getById_notFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Supplier not found");
    }

    // --------------------------------------------------------
    // UPDATE SUPPLIER
    // --------------------------------------------------------
    @Test
    void update_success() {
        SupplierCreateDTO dto = new SupplierCreateDTO();
        dto.setName("Updated");
        dto.setEmail("updated@test.com");
        dto.setContact("0707070707");

        Supplier existing = Supplier.builder()
                .id(1L).name("Old").email("old@test.com").contact("0600000000").build();

        Supplier updated = Supplier.builder()
                .id(1L).name(dto.getName()).email(dto.getEmail()).contact(dto.getContact()).build();

        SupplierResponseDTO response = new SupplierResponseDTO();
        response.setId(1L);
        response.setName(dto.getName());
        response.setEmail(dto.getEmail());
        response.setContact(dto.getContact());

        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.existsByEmail(dto.getEmail())).thenReturn(false);

        // Simulate patch logic
        doAnswer(inv -> {
            Supplier s = inv.getArgument(0);
            SupplierCreateDTO d = inv.getArgument(1);
            s.setName(d.getName());
            s.setEmail(d.getEmail());
            s.setContact(d.getContact());
            return null;
        }).when(mapper).patch(existing, dto);

        when(repo.save(existing)).thenReturn(updated);
        when(mapper.toResponse(updated)).thenReturn(response);

        SupplierResponseDTO result = service.update(1L, dto);

        assertThat(result.getEmail()).isEqualTo(dto.getEmail());
    }

    @Test
    void update_emailAlreadyExists() {
        SupplierCreateDTO dto = new SupplierCreateDTO();
        dto.setName("X");
        dto.setEmail("conflict@test.com");
        dto.setContact("0666000000");

        Supplier existing = Supplier.builder()
                .id(1L).name("Old").email("old@test.com").contact("0666111111").build();

        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void update_notFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        SupplierCreateDTO dto = new SupplierCreateDTO();
        dto.setName("X");
        dto.setEmail("x@test.com");
        dto.setContact("0606060606");

        assertThatThrownBy(() -> service.update(99L, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Supplier not found");
    }

    // --------------------------------------------------------
    // DELETE SUPPLIER
    // --------------------------------------------------------
    @Test
    void delete_success() {
        Supplier entity = Supplier.builder()
                .id(1L).name("A").email("a@a.com").contact("0600000000").build();

        when(repo.findById(1L)).thenReturn(Optional.of(entity));

        service.delete(1L);

        verify(repo).delete(entity);
    }

    @Test
    void delete_notFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Supplier not found");
    }
}
