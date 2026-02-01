package com.tinnova.veiculos.service;

import com.tinnova.veiculos.exception.ResourceNotFoundException;
import com.tinnova.veiculos.exception.ValidationException;
import com.tinnova.veiculos.model.Veiculo;
import com.tinnova.veiculos.repository.VeiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VeiculoServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private CambioService cambioService;

    @InjectMocks
    private VeiculoService veiculoService;

    private Veiculo veiculo;

    @BeforeEach
    void setUp() {
        veiculo = new Veiculo();
        veiculo.setId(1L);
        veiculo.setMarca("Toyota");
        veiculo.setModelo("Corolla");
        veiculo.setAno(2022);
        veiculo.setCor("Preto");
        veiculo.setPlaca("ABC1D23");
        veiculo.setPrecoDolar(BigDecimal.valueOf(20000));
        veiculo.setVendido(false);
    }

    @Test
    @DisplayName("Deve criar veículo com sucesso")
    void shouldCreateVeiculo() {
        when(veiculoRepository.existsByPlacaAndDeletedFalse("ABC1D23"))
                .thenReturn(false);
        when(veiculoRepository.save(veiculo))
                .thenReturn(veiculo);

        Veiculo saved = veiculoService.createVeiculo(veiculo);

        assertNotNull(saved);
        assertEquals("Toyota", saved.getMarca());
        verify(veiculoRepository).save(veiculo);
    }

    @Test
    @DisplayName("Deve lançar ValidationException ao tentar criar veículo com placa duplicada")
    void shouldThrowValidationExceptionWhenPlacaExists() {
        when(veiculoRepository.existsByPlacaAndDeletedFalse("ABC1D23"))
                .thenReturn(true);

        assertThrows(ValidationException.class, () ->
                veiculoService.createVeiculo(veiculo)
        );

        verify(veiculoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve retornar veículo por ID")
    void shouldFindVeiculoById() {
        when(veiculoRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(veiculo));

        Veiculo found = veiculoService.findVeiculoById(1L);

        assertEquals(1L, found.getId());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando veículo não existir")
    void shouldThrowResourceNotFoundException() {
        when(veiculoRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                veiculoService.findVeiculoById(1L)
        );
    }

    @Test
    @DisplayName("Deve atualizar veículo")
    void shouldUpdateVeiculo() {
        Veiculo updated = new Veiculo();
        updated.setMarca("Honda");
        updated.setVendido(true);

        when(veiculoRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(veiculo));
        when(veiculoRepository.save(any(Veiculo.class)))
                .thenReturn(veiculo);

        Veiculo result = veiculoService.updateVeiculo(1L, updated);

        assertEquals("Honda", result.getMarca());
        assertTrue(result.isVendido());
    }

    @Test
    @DisplayName("Deve deletar veículo logicamente")
    void shouldDeleteVeiculo() {
        when(veiculoRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(veiculo));

        veiculoService.deleteVeiculo(1L);

        verify(veiculoRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve retornar veículos paginados")
    void shouldReturnPaginatedVeiculos() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Veiculo> page = new PageImpl<>(List.of(veiculo));

        when(veiculoRepository.findAll(pageable))
                .thenReturn(page);

        Page<Veiculo> result =
                veiculoService.findVeiculosWithPaginationAndSorting(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Deve retornar quantidade de veículos por marca")
    void shouldReturnVeiculosCountByMarca() {
        when(veiculoRepository.findAll())
                .thenReturn(List.of(veiculo));

        Map<String, Long> result =
                veiculoService.getVeiculosCountByMarca();

        assertEquals(1L, result.get("Toyota"));
    }

    @Test
    @DisplayName("Deve buscar veículos por filtros e range de preço")
    void shouldSearchVeiculosWithFilters() {
        when(cambioService.getCotacaoDolarBRL())
                .thenReturn(BigDecimal.valueOf(5));

        when(veiculoRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(List.of(veiculo));

        List<Veiculo> result =
                veiculoService.searchVeiculos(
                        "Toyota",
                        2022,
                        "Preto",
                        BigDecimal.valueOf(100000),
                        BigDecimal.valueOf(150000)
                );

        assertFalse(result.isEmpty());
        verify(veiculoRepository)
                .findAll(any(org.springframework.data.jpa.domain.Specification.class));
    }

}
