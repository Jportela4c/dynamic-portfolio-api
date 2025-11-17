package com.portfolio.api.controller;

import com.portfolio.api.model.dto.response.TelemetryResponse;
import com.portfolio.api.service.SimulationService;
import com.portfolio.api.service.TelemetryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TelemetryController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.portfolio\\.api\\.security\\..*"
    ))
class TelemetryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SimulationService simulationService;

    @MockBean
    private TelemetryService telemetryService;

    @Test
    void shouldReturnTelemetryMetrics() throws Exception {
        // Arrange
        List<TelemetryResponse.ServiceMetrics> servicos = Arrays.asList(
            TelemetryResponse.ServiceMetrics.builder()
                .nome("SimulationService")
                .quantidadeChamadas(150L)
                .mediaTempoRespostaMs(45L)
                .build(),
            TelemetryResponse.ServiceMetrics.builder()
                .nome("RiskProfileService")
                .quantidadeChamadas(80L)
                .mediaTempoRespostaMs(30L)
                .build()
        );

        TelemetryResponse.Period periodo = TelemetryResponse.Period.builder()
            .inicio(LocalDate.of(2025, 1, 1))
            .fim(LocalDate.of(2025, 1, 31))
            .build();

        TelemetryResponse response = TelemetryResponse.builder()
            .servicos(servicos)
            .periodo(periodo)
            .build();

        when(simulationService.getTelemetry()).thenReturn(response);
        doNothing().when(telemetryService).recordMetric(anyString(), anyLong(), anyBoolean(), anyInt());

        // Act & Assert
        mockMvc.perform(get("/telemetria")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.servicos[0].nome").value("SimulationService"))
            .andExpect(jsonPath("$.servicos[0].quantidadeChamadas").value(150))
            .andExpect(jsonPath("$.servicos[0].mediaTempoRespostaMs").value(45))
            .andExpect(jsonPath("$.servicos[1].nome").value("RiskProfileService"))
            .andExpect(jsonPath("$.servicos[1].quantidadeChamadas").value(80))
            .andExpect(jsonPath("$.servicos[1].mediaTempoRespostaMs").value(30))
            .andExpect(jsonPath("$.periodo.inicio").value("2025-01-01"))
            .andExpect(jsonPath("$.periodo.fim").value("2025-01-31"));

        verify(simulationService).getTelemetry();
        verify(telemetryService).recordMetric(eq("telemetria"), anyLong(), eq(true), eq(200));
    }
}
