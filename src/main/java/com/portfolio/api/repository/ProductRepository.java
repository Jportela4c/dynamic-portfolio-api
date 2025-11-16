package com.portfolio.api.repository;

import com.portfolio.api.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByPerfilAdequadoAndAtivoTrue(String perfilAdequado);

    List<Product> findByTipoAndAtivoTrueAndValorMinimoLessThanEqual(
            String tipo,
            BigDecimal valor
    );

    Optional<Product> findFirstByTipoAndAtivoTrueAndValorMinimoLessThanEqualAndPrazoMinimoMesesLessThanEqualOrderByRentabilidadeDesc(
            String tipo,
            BigDecimal valor,
            Integer prazoMeses
    );
}
