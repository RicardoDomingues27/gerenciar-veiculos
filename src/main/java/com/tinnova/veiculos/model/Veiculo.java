package com.tinnova.veiculos.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data // Lombok para getters/setters
@SQLDelete(sql = "UPDATE veiculo SET deleted = true WHERE id=?")
@Where(clause = "deleted = false")
public class Veiculo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String veiculo;
    private String marca;
    private String modelo;
    private Integer ano;
    private String cor;
    private String placa; // Adicione um campo de placa para a validação de duplicidade
    private BigDecimal precoDolar; // Armazenado em dólar, conforme o desafio
    private boolean vendido;
    private LocalDateTime created;
    private LocalDateTime updated;
    private boolean deleted = Boolean.FALSE; // Campo para soft delete

    @PrePersist
    protected void onCreate() {
        created = LocalDateTime.now();
        updated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
    }
}
