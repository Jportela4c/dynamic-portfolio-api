package com.portfolio.api.controller;

import com.portfolio.api.model.dto.response.DailyAggregationResponse;
import com.portfolio.api.model.dto.response.SimulationHistoryResponse;
import com.portfolio.api.service.SimulationService;
import com.portfolio.api.service.TelemetryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SimulationController {

    private final SimulationService simulationService;
    private final TelemetryService telemetryService;

    public SimulationController(SimulationService simulationService,
                                TelemetryService telemetryService) {
        this.simulationService = simulationService;
        this.telemetryService = telemetryService;
    }

    @GetMapping("/simulacoes")
    public ResponseEntity<List<SimulationHistoryResponse>> getAllSimulations() {
        long startTime = System.currentTimeMillis();

        try {
            List<SimulationHistoryResponse> response = simulationService.getAllSimulations();
            long responseTime = System.currentTimeMillis() - startTime;
            telemetryService.recordMetric("simulacoes", responseTime, true, 200);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            telemetryService.recordMetric("simulacoes", responseTime, false, 500);
            throw e;
        }
    }

    @GetMapping("/simulacoes/por-produto-dia")
    public ResponseEntity<List<DailyAggregationResponse>> getDailyAggregations() {
        long startTime = System.currentTimeMillis();

        try {
            List<DailyAggregationResponse> response = simulationService.getDailyAggregations();
            long responseTime = System.currentTimeMillis() - startTime;
            telemetryService.recordMetric("simulacoes-por-produto-dia", responseTime, true, 200);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            telemetryService.recordMetric("simulacoes-por-produto-dia", responseTime, false, 500);
            throw e;
        }
    }
}
