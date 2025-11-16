package com.portfolio.api.repository;

import com.portfolio.api.model.entity.Simulation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SimulationRepository extends JpaRepository<Simulation, Long> {

    List<Simulation> findByClienteId(Long clienteId);

    List<Simulation> findByClienteIdOrderByDataSimulacaoDesc(Long clienteId);

    @Query("""
            SELECT s.produtoNome as produto,
                   CAST(s.dataSimulacao AS date) as data,
                   COUNT(s) as quantidadeSimulacoes,
                   AVG(s.valorFinal) as mediaValorFinal
            FROM Simulation s
            GROUP BY s.produtoNome, CAST(s.dataSimulacao AS date)
            ORDER BY CAST(s.dataSimulacao AS date) DESC, s.produtoNome
            """)
    List<DailyAggregation> findDailyAggregations();

    interface DailyAggregation {
        String getProduto();
        LocalDate getData();
        Long getQuantidadeSimulacoes();
        BigDecimal getMediaValorFinal();
    }
}
