package com.portfolio.api.controller;

import com.portfolio.api.model.dto.request.SimulationRequest;
import com.portfolio.api.model.dto.response.SimulationResponse;
import com.portfolio.api.service.SimulationService;
import com.portfolio.api.service.TelemetryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InvestmentSimulationController {

    private final SimulationService simulationService;
    private final TelemetryService telemetryService;

    public InvestmentSimulationController(SimulationService simulationService,
                                          TelemetryService telemetryService) {
        this.simulationService = simulationService;
        this.telemetryService = telemetryService;
    }

    @PostMapping("/simular-investimento")
    public ResponseEntity<SimulationResponse> simulateInvestment(@Valid @RequestBody SimulationRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            SimulationResponse response = simulationService.simulateInvestment(request);
            long responseTime = System.currentTimeMillis() - startTime;
            telemetryService.recordMetric("simular-investimento", responseTime, true, 200);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            telemetryService.recordMetric("simular-investimento", responseTime, false, 500);
            throw e;
        }
    }
}
