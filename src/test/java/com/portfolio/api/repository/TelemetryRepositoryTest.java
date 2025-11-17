package com.portfolio.api.repository;

import com.portfolio.api.model.entity.TelemetryRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TelemetryRepositoryTest {

    @Autowired
    private TelemetryRepository telemetryRepository;

    @BeforeEach
    void setUp() {
        telemetryRepository.deleteAll();
    }

    @Test
    void shouldFindMetricsByPeriod() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusHours(2);
        LocalDateTime end = now.plusHours(1);

        TelemetryRecord record1 = createRecord("simular-investimento", 150, true, 200, now.minusHours(1));
        TelemetryRecord record2 = createRecord("simular-investimento", 200, true, 200, now.minusMinutes(30));
        TelemetryRecord record3 = createRecord("perfil-risco", 100, true, 200, now.minusMinutes(45));
        TelemetryRecord record4 = createRecord("simular-investimento", 300, true, 200, now.minusDays(1)); // Outside period

        telemetryRepository.save(record1);
        telemetryRepository.save(record2);
        telemetryRepository.save(record3);
        telemetryRepository.save(record4);

        List<TelemetryRepository.ServiceMetrics> metrics = telemetryRepository.findMetricsByPeriod(start, end);

        assertThat(metrics).hasSize(2);

        TelemetryRepository.ServiceMetrics simulacaoMetrics = metrics.stream()
                .filter(m -> m.getServico().equals("simular-investimento"))
                .findFirst()
                .orElse(null);

        assertThat(simulacaoMetrics).isNotNull();
        assertThat(simulacaoMetrics.getQuantidadeChamadas()).isEqualTo(2L);
        assertThat(simulacaoMetrics.getMediaTempoRespostaMs()).isEqualTo(175.0);

        TelemetryRepository.ServiceMetrics perfilMetrics = metrics.stream()
                .filter(m -> m.getServico().equals("perfil-risco"))
                .findFirst()
                .orElse(null);

        assertThat(perfilMetrics).isNotNull();
        assertThat(perfilMetrics.getQuantidadeChamadas()).isEqualTo(1L);
        assertThat(perfilMetrics.getMediaTempoRespostaMs()).isEqualTo(100.0);
    }

    @Test
    void shouldReturnEmptyListWhenNoPeriodData() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusHours(2);
        LocalDateTime end = now.minusHours(1);

        TelemetryRecord record = createRecord("simular-investimento", 150, true, 200, now);
        telemetryRepository.save(record);

        List<TelemetryRepository.ServiceMetrics> metrics = telemetryRepository.findMetricsByPeriod(start, end);

        assertThat(metrics).isEmpty();
    }

    @Test
    void shouldGroupMetricsByService() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusHours(1);
        LocalDateTime end = now.plusHours(1);

        TelemetryRecord record1 = createRecord("service-a", 100, true, 200, now);
        TelemetryRecord record2 = createRecord("service-a", 200, true, 200, now.minusMinutes(15));
        TelemetryRecord record3 = createRecord("service-b", 150, true, 200, now.minusMinutes(30));

        telemetryRepository.save(record1);
        telemetryRepository.save(record2);
        telemetryRepository.save(record3);

        List<TelemetryRepository.ServiceMetrics> metrics = telemetryRepository.findMetricsByPeriod(start, end);

        assertThat(metrics).hasSize(2);
        assertThat(metrics).extracting(TelemetryRepository.ServiceMetrics::getServico)
                .containsExactlyInAnyOrder("service-a", "service-b");
    }

    @Test
    void shouldCalculateAverageResponseTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusHours(1);
        LocalDateTime end = now.plusHours(1);

        TelemetryRecord record1 = createRecord("test-service", 100, true, 200, now);
        TelemetryRecord record2 = createRecord("test-service", 200, true, 200, now.minusMinutes(15));
        TelemetryRecord record3 = createRecord("test-service", 300, true, 200, now.minusMinutes(30));

        telemetryRepository.save(record1);
        telemetryRepository.save(record2);
        telemetryRepository.save(record3);

        List<TelemetryRepository.ServiceMetrics> metrics = telemetryRepository.findMetricsByPeriod(start, end);

        assertThat(metrics).hasSize(1);
        assertThat(metrics.get(0).getServico()).isEqualTo("test-service");
        assertThat(metrics.get(0).getQuantidadeChamadas()).isEqualTo(3L);
        assertThat(metrics.get(0).getMediaTempoRespostaMs()).isEqualTo(200.0);
    }

    @Test
    void shouldOrderResultsByServiceName() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusHours(1);
        LocalDateTime end = now.plusHours(1);

        TelemetryRecord record1 = createRecord("z-service", 100, true, 200, now);
        TelemetryRecord record2 = createRecord("a-service", 150, true, 200, now.minusMinutes(15));
        TelemetryRecord record3 = createRecord("m-service", 200, true, 200, now.minusMinutes(30));

        telemetryRepository.save(record1);
        telemetryRepository.save(record2);
        telemetryRepository.save(record3);

        List<TelemetryRepository.ServiceMetrics> metrics = telemetryRepository.findMetricsByPeriod(start, end);

        assertThat(metrics).hasSize(3);
        assertThat(metrics).extracting(TelemetryRepository.ServiceMetrics::getServico)
                .containsExactly("a-service", "m-service", "z-service");
    }

    private TelemetryRecord createRecord(String servico, Integer tempoMs, boolean sucesso, int statusCode, LocalDateTime timestamp) {
        TelemetryRecord record = new TelemetryRecord();
        record.setServico(servico);
        record.setTempoRespostaMs(tempoMs);
        record.setSucesso(sucesso);
        record.setCodigoStatus(statusCode);
        record.setTimestamp(timestamp);
        return record;
    }
}
