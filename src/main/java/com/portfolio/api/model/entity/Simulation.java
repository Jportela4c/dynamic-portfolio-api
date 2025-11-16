package com.portfolio.api.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "simulacoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Simulation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(name = "produto_nome", nullable = false, length = 100)
    private String produtoNome;

    @Column(name = "valor_investido", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorInvestido;

    @Column(name = "valor_final", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorFinal;

    @Column(name = "prazo_meses", nullable = false)
    private Integer prazoMeses;

    @Column(name = "data_simulacao")
    private LocalDateTime dataSimulacao;

    @PrePersist
    protected void onCreate() {
        dataSimulacao = LocalDateTime.now();
    }
}
