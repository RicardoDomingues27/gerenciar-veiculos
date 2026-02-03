package com.tinnova.veiculos.controller;

import com.tinnova.veiculos.model.Veiculo;
import com.tinnova.veiculos.service.VeiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/veiculos")
@Tag(name = "Veículos API", description = "Gerenciamento completo de veículos e relatórios")
public class VeiculoController {

    @Autowired
    private VeiculoService veiculoService;

    // GET /veiculos
    @GetMapping
    @Operation(summary = "Retorna todos os veículos com paginação e ordenação (USER/ADMIN)")
    public ResponseEntity<Page<Veiculo>> getAllVeiculos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {
        // Lógica para processar o parâmetro de ordenação 'sort'
        Pageable pageable = PageRequest.of(page, size);
        Page<Veiculo> veiculos = veiculoService.findVeiculosWithPaginationAndSorting(pageable);
        return ResponseEntity.ok(veiculos);
    }

    // GET /veiculos?marca={marca}&ano={ano}&cor={cor} e GET /veiculos?minPreco={valorMaximo}&maxPreco={valorMinimo}
    @GetMapping("/busca")
    @Operation(summary = "Busca veículos por filtros combinados e range de preço (USER/ADMIN)")
    public ResponseEntity<List<Veiculo>> searchVeiculos(
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) String cor,
            @RequestParam(required = false) BigDecimal minPreco,
            @RequestParam(required = false) BigDecimal maxPreco) {
        List<Veiculo> veiculos = veiculoService.searchVeiculos(marca, ano, cor, minPreco, maxPreco);
        return ResponseEntity.ok(veiculos);
    }

    // GET /veiculos/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Retorna os detalhes do veículo por ID (USER/ADMIN)")
    public ResponseEntity<Veiculo> getVeiculoById(@PathVariable Long id) {
        Veiculo veiculo = veiculoService.findVeiculoById(id);
        return ResponseEntity.ok(veiculo);
    }

    // POST /veiculos (somente ADMIN)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Somente ADMIN
    @Operation(summary = "Adiciona um novo veículo (Somente ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "403", description = "Acesso negado para USER")
    public ResponseEntity<Veiculo> createVeiculo(@RequestBody Veiculo veiculo) {
        Veiculo created = veiculoService.createVeiculo(veiculo);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT /veiculos/{id} (somente ADMIN)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza completamente os dados de um veículo (Somente ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Veiculo> updateVeiculo(@PathVariable Long id, @RequestBody Veiculo veiculoDetails) {
        Veiculo updated = veiculoService.updateVeiculo(id, veiculoDetails);
        return ResponseEntity.ok(updated);
    }

    // PATCH /veiculos/{id} (somente ADMIN)
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza parcialmente os dados de um veículo (Somente ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Veiculo> patchVeiculo(@PathVariable Long id, @RequestBody Map<String, Object> fields) {
        // Implementar lógica de aplicação parcial de campos, similar ao PUT mas com reflexão ou DTOs específicos.
        // Este é um esqueleto, a lógica real requer mais detalhes.
        return ResponseEntity.ok(veiculoService.findVeiculoById(id));
    }

    // DELETE /veiculos/{id} (somente ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove um veículo (soft delete) (Somente ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteVeiculo(@PathVariable Long id) {
        veiculoService.deleteVeiculo(id);
        return ResponseEntity.noContent().build();
    }

    // GET /veiculos/relatorios/por-marca
    @GetMapping("/relatorios/por-marca")
    @Operation(summary = "Retorna relatório de quantidade de veículos agrupados por marca (USER/ADMIN)")
    public ResponseEntity<Map<String, Long>> getVeiculosCountByMarca() {
        Map<String, Long> report = veiculoService.getVeiculosCountByMarca();
        return ResponseEntity.ok(report);
    }
}
