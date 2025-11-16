package com.portfolio.api.repository;

import com.portfolio.api.model.entity.TelemetryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TelemetryRepository extends JpaRepository<TelemetryRecord, Long> {

    @Query("""
            SELECT t.servico as servico,
                   COUNT(t) as quantidadeChamadas,
                   AVG(t.tempoRespostaMs) as mediaTempoRespostaMs
            FROM TelemetryRecord t
            WHERE t.timestamp >= :inicio AND t.timestamp <= :fim
            GROUP BY t.servico
            ORDER BY t.servico
            """)
    List<ServiceMetrics> findMetricsByPeriod(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    interface ServiceMetrics {
        String getServico();
        Long getQuantidadeChamadas();
        Double getMediaTempoRespostaMs();
    }
}
