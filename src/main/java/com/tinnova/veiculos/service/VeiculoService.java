package com.tinnova.veiculos.service;

import com.tinnova.veiculos.exception.ResourceNotFoundException;
import com.tinnova.veiculos.exception.ValidationException;
import com.tinnova.veiculos.model.Veiculo;
import com.tinnova.veiculos.repository.VeiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VeiculoService {

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private CambioService cambioService;

    
    public Veiculo createVeiculo(Veiculo veiculo) {
        if (veiculoRepository.existsByPlacaAndDeletedFalse(veiculo.getPlaca())) {
            throw new ValidationException("Veículo com a placa " + veiculo.getPlaca() + " já existe.");
        }
        return veiculoRepository.save(veiculo);
    }

    public List<Veiculo> findAllVeiculos() {
        return veiculoRepository.findAll(); 
    }

    public Page<Veiculo> findVeiculosWithPaginationAndSorting(Pageable pageable) {
        return veiculoRepository.findAll(pageable); 
    }

    public Veiculo findVeiculoById(Long id) {
        return veiculoRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado com ID: " + id));
    }

    public Veiculo updateVeiculo(Long id, Veiculo veiculoDetails) {
        Veiculo veiculo = findVeiculoById(id); 
        
        veiculo.setMarca(veiculoDetails.getMarca());
        veiculo.setVendido(veiculoDetails.isVendido());
        return veiculoRepository.save(veiculo);
    }

    
    public void deleteVeiculo(Long id) {
        Veiculo veiculo = findVeiculoById(id);
        veiculoRepository.deleteById(id);
    }

    
    public Map<String, Long> getVeiculosCountByMarca() {
        return veiculoRepository.findAll().stream()
                .collect(Collectors.groupingBy(Veiculo::getMarca, Collectors.counting()));
    }

   
    public List<Veiculo> searchVeiculos(String marca, Integer ano, String cor, BigDecimal minPrecoBRL, BigDecimal maxPrecoBRL) {
        Specification<Veiculo> spec = Specification.where(null);

        // Conversão de BRL para Dólar para o filtro (Preço é armazenado em USD)
        BigDecimal cotacaoDolar = cambioService.getCotacaoDolarBRL();

        if (marca != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("marca"), marca));
        if (ano != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("ano"), ano));
        if (cor != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("cor"), cor));

        if (minPrecoBRL != null) {
            BigDecimal minPrecoDolar = minPrecoBRL.divide(cotacaoDolar, 2, BigDecimal.ROUND_HALF_UP);
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("precoDolar"), minPrecoDolar));
        }
        if (maxPrecoBRL != null) {
            BigDecimal maxPrecoDolar = maxPrecoBRL.divide(cotacaoDolar, 2, BigDecimal.ROUND_HALF_UP);
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("precoDolar"), maxPrecoDolar));
        }

        return veiculoRepository.findAll(spec);
    }
}
