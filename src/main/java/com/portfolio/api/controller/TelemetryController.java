package com.portfolio.api.controller;

import com.portfolio.api.model.dto.response.TelemetryResponse;
import com.portfolio.api.service.SimulationService;
import com.portfolio.api.service.TelemetryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Telemetria", description = "Endpoints para consulta de métricas e telemetria dos serviços")
public class TelemetryController {

    private final SimulationService simulationService;
    private final TelemetryService telemetryService;

    public TelemetryController(SimulationService simulationService,
                               TelemetryService telemetryService) {
        this.simulationService = simulationService;
        this.telemetryService = telemetryService;
    }

    @Operation(
        summary = "Consultar telemetria dos serviços",
        description = "Retorna métricas de performance dos serviços, incluindo quantidade de chamadas e tempo médio de resposta"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Telemetria obtida com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TelemetryResponse.class)))
    })
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
