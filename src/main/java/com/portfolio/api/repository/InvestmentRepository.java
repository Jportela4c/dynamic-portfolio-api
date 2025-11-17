package com.portfolio.api.repository;

import com.portfolio.api.model.entity.Investment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface InvestmentRepository extends JpaRepository<Investment, Long> {

    List<Investment> findByClienteIdOrderByDataDesc(Long clienteId);

    @Query("SELECT COUNT(i) FROM Investment i WHERE i.clienteId = :clienteId")
    Long countByClienteId(@Param("clienteId") Long clienteId);

    @Query("SELECT SUM(i.valor) FROM Investment i WHERE i.clienteId = :clienteId")
    BigDecimal sumValorByClienteId(@Param("clienteId") Long clienteId);

    @Query("SELECT SUM(i.valor) FROM Investment i GROUP BY i.clienteId")
    List<BigDecimal> getAllClientVolumes();

    boolean existsByClienteId(Long clienteId);
}
