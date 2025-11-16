package com.portfolio.api.controller;

import com.portfolio.api.model.dto.response.InvestmentResponse;
import com.portfolio.api.service.InvestmentService;
import com.portfolio.api.service.TelemetryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class InvestmentHistoryController {

    private final InvestmentService investmentService;
    private final TelemetryService telemetryService;

    public InvestmentHistoryController(InvestmentService investmentService,
                                       TelemetryService telemetryService) {
        this.investmentService = investmentService;
        this.telemetryService = telemetryService;
    }

    @GetMapping("/investimentos/{clienteId}")
    public ResponseEntity<List<InvestmentResponse>> getInvestmentHistory(@PathVariable Long clienteId) {
        long startTime = System.currentTimeMillis();

        try {
            List<InvestmentResponse> response = investmentService.getClientInvestments(clienteId);
            long responseTime = System.currentTimeMillis() - startTime;
            telemetryService.recordMetric("investimentos", responseTime, true, 200);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            telemetryService.recordMetric("investimentos", responseTime, false, 500);
            throw e;
        }
    }
}
