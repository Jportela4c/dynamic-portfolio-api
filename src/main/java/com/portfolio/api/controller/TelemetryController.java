package com.portfolio.api.controller;

import com.portfolio.api.model.dto.response.TelemetryResponse;
import com.portfolio.api.service.SimulationService;
import com.portfolio.api.service.TelemetryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TelemetryController {

    private final SimulationService simulationService;
    private final TelemetryService telemetryService;

    public TelemetryController(SimulationService simulationService,
                               TelemetryService telemetryService) {
        this.simulationService = simulationService;
        this.telemetryService = telemetryService;
    }

    @GetMapping("/telemetria")
    public ResponseEntity<TelemetryResponse> getTelemetry() {
        long startTime = System.currentTimeMillis();

        try {
            TelemetryResponse response = simulationService.getTelemetry();
            long responseTime = System.currentTimeMillis() - startTime;
            telemetryService.recordMetric("telemetria", responseTime, true, 200);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            telemetryService.recordMetric("telemetria", responseTime, false, 500);
            throw e;
        }
    }
}
