package com.portfolio.api.model.entity;

import com.portfolio.api.model.enums.TipoProduto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "produtos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 50)
    private TipoProduto tipo;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal rentabilidade;

    @Column(nullable = false, length = 20)
    private String risco;

    @Column(name = "valor_minimo", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorMinimo;

    @Column(name = "prazo_minimo_meses", nullable = false)
    private Integer prazoMinimoMeses;

    @Column(name = "prazo_maximo_meses")
    private Integer prazoMaximoMeses;

    @Column(name = "perfil_adequado", length = 20)
    private String perfilAdequado;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
