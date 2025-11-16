package com.portfolio.api.controller;

import com.portfolio.api.model.dto.response.RiskProfileResponse;
import com.portfolio.api.service.RiskProfileService;
import com.portfolio.api.service.TelemetryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RiskProfileController {

    private final RiskProfileService riskProfileService;
    private final TelemetryService telemetryService;

    public RiskProfileController(RiskProfileService riskProfileService,
                                 TelemetryService telemetryService) {
        this.riskProfileService = riskProfileService;
        this.telemetryService = telemetryService;
    }

    @GetMapping("/perfil-risco/{clienteId}")
    public ResponseEntity<RiskProfileResponse> getRiskProfile(@PathVariable Long clienteId) {
        long startTime = System.currentTimeMillis();

        try {
            RiskProfileResponse response = riskProfileService.calculateRiskProfile(clienteId);
            long responseTime = System.currentTimeMillis() - startTime;
            telemetryService.recordMetric("perfil-risco", responseTime, true, 200);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            telemetryService.recordMetric("perfil-risco", responseTime, false, 500);
            throw e;
        }
    }
}
