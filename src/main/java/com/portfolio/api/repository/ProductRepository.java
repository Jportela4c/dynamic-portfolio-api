package com.portfolio.api.repository;

import com.portfolio.api.model.entity.Product;
import com.portfolio.api.model.enums.TipoProduto;
import org.springframework.data.jpa.repository.JpaRepository;
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

    Optional<Product> findFirstByTipoAndAtivoTrueAndValorMinimoLessThanEqualAndPrazoMinimoMesesLessThanEqualOrderByRentabilidadeDesc(
            TipoProduto tipo,
            BigDecimal valor,
            Integer prazoMeses
    );
}
