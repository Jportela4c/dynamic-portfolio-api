package com.portfolio.api.service;

import com.portfolio.api.model.entity.TelemetryRecord;
import com.portfolio.api.repository.TelemetryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TelemetryService {

    private final TelemetryRepository telemetryRepository;

    public TelemetryService(TelemetryRepository telemetryRepository) {
        this.telemetryRepository = telemetryRepository;
    }

    @Transactional
    public void recordMetric(String serviceName, long responseTimeMs, boolean success, int statusCode) {
        TelemetryRecord record = new TelemetryRecord();
        record.setServico(serviceName);
        record.setTempoRespostaMs((int) responseTimeMs);
        record.setSucesso(success);
        record.setCodigoStatus(statusCode);
        record.setTimestamp(LocalDateTime.now());

        telemetryRepository.save(record);
    }
}
