package com.portfolio.api.service;

import com.portfolio.api.model.dto.response.TelemetryResponse;
import com.portfolio.api.model.dto.response.TelemetryResponse.Period;
import com.portfolio.api.model.dto.response.TelemetryResponse.ServiceMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PrometheusMetricsService {

    private final MeterRegistry meterRegistry;

    public PrometheusMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public TelemetryResponse getTelemetry() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);

        List<ServiceMetrics> servicos = new ArrayList<>();

        // Map endpoint URIs to service names matching THE SPEC
        servicos.add(buildServiceMetrics("simular-investimento", "/api/v1/simular-investimento", "POST"));
        servicos.add(buildServiceMetrics("perfil-risco", "/api/v1/perfil-risco/{clienteId}", "GET"));
        servicos.add(buildServiceMetrics("produtos-recomendados", "/api/v1/produtos-recomendados/{perfil}", "GET"));
        servicos.add(buildServiceMetrics("investimentos", "/api/v1/investimentos/{clienteId}", "GET"));
        servicos.add(buildServiceMetrics("simulacoes", "/api/v1/simulacoes", "GET"));
        servicos.add(buildServiceMetrics("simulacoes-agregadas", "/api/v1/simulacoes/por-produto-dia", "GET"));
        servicos.add(buildServiceMetrics("telemetria", "/api/v1/telemetria", "GET"));

        return TelemetryResponse.builder()
                .servicos(servicos)
                .periodo(new Period(startDate, endDate))
                .build();
    }

    private ServiceMetrics buildServiceMetrics(String serviceName, String uri, String method) {
        // Query Micrometer for this specific endpoint
        Timer timer = meterRegistry.find("http.server.requests")
                .tag("uri", uri)
                .tag("method", method)
                .timer();

        if (timer == null || timer.count() == 0) {
            return ServiceMetrics.builder()
                    .nome(serviceName)
                    .quantidadeChamadas(0L)
                    .mediaTempoRespostaMs(0L)
                    .build();
        }

        long count = timer.count();
        long avgMs = (long) timer.mean(TimeUnit.MILLISECONDS);

        return ServiceMetrics.builder()
                .nome(serviceName)
                .quantidadeChamadas(count)
                .mediaTempoRespostaMs(avgMs)
                .build();
    }
}
