package com.tinnova.veiculos.repository;

import com.tinnova.veiculos.model.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, Long>, JpaSpecificationExecutor<Veiculo> {
    boolean existsByPlacaAndDeletedFalse(String placa);
    Optional<Veiculo> findByIdAndDeletedFalse(Long id);
}
