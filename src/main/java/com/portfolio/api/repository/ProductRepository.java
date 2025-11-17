package com.portfolio.api.repository;

import com.portfolio.api.model.entity.Product;
import com.portfolio.api.model.enums.TipoProduto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByPerfilAdequadoAndAtivoTrue(String perfilAdequado);

    List<Product> findByTipoAndAtivoTrueAndValorMinimoLessThanEqual(
            TipoProduto tipo,
            BigDecimal valor
    );

    @Query("SELECT p FROM Product p WHERE p.tipo = :tipo AND p.ativo = true " +
           "AND p.valorMinimo <= :valor AND p.prazoMinimoMeses <= :prazoMeses " +
           "AND (p.prazoMaximoMeses IS NULL OR p.prazoMaximoMeses >= :prazoMeses) " +
           "ORDER BY p.rentabilidade DESC LIMIT 1")
    Optional<Product> findFirstByTipoAndAtivoTrueAndValorMinimoLessThanEqualAndPrazoMinimoMesesLessThanEqualOrderByRentabilidadeDesc(
            @Param("tipo") TipoProduto tipo,
            @Param("valor") BigDecimal valor,
            @Param("prazoMeses") Integer prazoMeses
    );
}
