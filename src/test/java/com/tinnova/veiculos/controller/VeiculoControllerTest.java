package com.tinnova.veiculos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinnova.veiculos.model.Veiculo;
import com.tinnova.veiculos.service.VeiculoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VeiculoController.class)
class VeiculoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VeiculoService veiculoService;

    @Autowired
    private ObjectMapper objectMapper;

    private Veiculo buildVeiculo() {
        Veiculo v = new Veiculo();
        v.setId(1L);
        v.setMarca("Toyota");
        v.setModelo("Corolla");
        v.setAno(2022);
        v.setCor("Preto");
        v.setPlaca("ABC1D23");
        v.setPrecoDolar(BigDecimal.valueOf(20000));
        v.setVendido(false);
        return v;
    }

    // =========================
    // CREATE
    // =========================
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /veiculos - Deve criar veículo (ADMIN)")
    void shouldCreateVeiculo() throws Exception {

        Veiculo veiculo = buildVeiculo();

        when(veiculoService.createVeiculo(any(Veiculo.class)))
                .thenReturn(veiculo);

        mockMvc.perform(post("/veiculos")
                        .with(csrf()) // 🔥 ESSENCIAL
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(veiculo)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.modelo").value("Corolla"));
    }
    // =========================
    // FIND ALL
    // =========================
    @Test
    @WithMockUser
    @DisplayName("GET /veiculos - Deve listar veículos paginados")

    void shouldFindAllVeiculos() throws Exception {

        Page<Veiculo> page =
                new PageImpl<>(List.of(buildVeiculo()));

        when(veiculoService.findVeiculosWithPaginationAndSorting(any()))
                .thenReturn(page);

        mockMvc.perform(get("/veiculos")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].modelo").value("Corolla"));
    }


    // =========================
    // PAGINATION
    // =========================
    @Test
    @DisplayName("GET /veiculos - Deve retornar página de veículos com paginação padrão")
    @WithMockUser(roles = {"USER"})
    void shouldReturnPagedVeiculos() throws Exception {

        // Arrange
        Veiculo veiculo = new Veiculo();
        veiculo.setId(1L);
        veiculo.setMarca("Toyota");
        veiculo.setModelo("Corolla");
        veiculo.setAno(2022);
        veiculo.setPrecoDolar(BigDecimal.valueOf(20000));

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Veiculo> page = new PageImpl<>(List.of(veiculo), pageRequest, 1);

        Mockito.when(veiculoService.findVeiculosWithPaginationAndSorting(pageRequest))
                .thenReturn(page);

        // Act + Assert
        mockMvc.perform(get("/veiculos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].marca").value("Toyota"))
                .andExpect(jsonPath("$.content[0].modelo").value("Corolla"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(veiculoService)
                .findVeiculosWithPaginationAndSorting(pageRequest);
    }

    // =========================
    // FIND BY ID
    // =========================
    @Test
    @WithMockUser
    @DisplayName("GET /veiculos/{id} - Deve buscar veículo por ID")
    void shouldFindVeiculoById() throws Exception {
        when(veiculoService.findVeiculoById(1L))
                .thenReturn(buildVeiculo());

        mockMvc.perform(get("/veiculos/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.placa").value("ABC1D23"));
    }

    // =========================
    // UPDATE
    // =========================
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /veiculos/{id} - Deve atualizar veículo (ADMIN)")
    void shouldUpdateVeiculo() throws Exception {

        Veiculo veiculo = buildVeiculo();
        veiculo.setMarca("Honda");

        when(veiculoService.updateVeiculo(eq(1L), any(Veiculo.class)))
                .thenReturn(veiculo);

        mockMvc.perform(put("/veiculos/{id}", 1L)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(veiculo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.marca").value("Honda"));
    }


    // =========================
    // DELETE
    // =========================
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /veiculos/{id} - Deve deletar veículo (ADMIN)")
    void shouldDeleteVeiculo() throws Exception {

        Mockito.doNothing()
                .when(veiculoService).deleteVeiculo(1L);

        mockMvc.perform(delete("/veiculos/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(veiculoService).deleteVeiculo(1L);
    }


    // =========================
    // SEARCH
    // =========================
    @Test
    @WithMockUser
    @DisplayName("GET /veiculos/busca - Deve buscar veículos com filtros")
    void shouldSearchVeiculos() throws Exception {

        when(veiculoService.searchVeiculos(
                any(), any(), any(), any(), any()))
                .thenReturn(List.of(buildVeiculo()));

        mockMvc.perform(get("/veiculos/busca")
                        .param("marca", "Toyota")
                        .param("ano", "2022")
                        .param("cor", "Preto")
                        .param("minPrecoBRL", "100000.00")
                        .param("maxPrecoBRL", "150000.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].marca").value("Toyota"));
    }



    // =========================
    // RELATÓRIO POR MARCA
    // =========================
    @Test
    @WithMockUser // USER ou ADMIN
    @DisplayName("GET /veiculos/relatorios/por-marca - Deve retornar relatório agrupado por marca")
    void shouldReturnVeiculosCountByMarca() throws Exception {

        Map<String, Long> report = Map.of(
                "Toyota", 2L,
                "Honda", 1L
        );

        Mockito.when(veiculoService.getVeiculosCountByMarca())
                .thenReturn(report);

        mockMvc.perform(get("/veiculos/relatorios/por-marca")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Toyota").value(2))
                .andExpect(jsonPath("$.Honda").value(1));
    }
}
