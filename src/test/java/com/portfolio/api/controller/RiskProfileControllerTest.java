package com.portfolio.api.controller;

import com.portfolio.api.model.dto.response.RiskProfileResponse;
import com.portfolio.api.service.RiskProfileService;
import com.portfolio.api.service.TelemetryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(controllers = RiskProfileController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.portfolio\\.api\\.security\\..*"
    ))
class RiskProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RiskProfileService riskProfileService;

    @MockBean
    private TelemetryService telemetryService;

    @Test
    void shouldReturnRiskProfileForClient() throws Exception {
        // Arrange
        RiskProfileResponse response = RiskProfileResponse.builder()
            .clienteId(123L)
            .perfil("MODERADO")
            .pontuacao(65)
            .descricao("Cliente com perfil moderado, busca equilíbrio entre rentabilidade e segurança")
            .build();

        when(riskProfileService.calculateRiskProfile(123L)).thenReturn(response);
        doNothing().when(telemetryService).recordMetric(anyString(), anyLong(), anyBoolean(), anyInt());

        // Act & Assert
        mockMvc.perform(get("/perfil-risco/123")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.clienteId").value(123))
            .andExpect(jsonPath("$.perfil").value("MODERADO"))
            .andExpect(jsonPath("$.pontuacao").value(65))
            .andExpect(jsonPath("$.descricao").value("Cliente com perfil moderado, busca equilíbrio entre rentabilidade e segurança"));

        verify(riskProfileService).calculateRiskProfile(123L);
        verify(telemetryService).recordMetric(eq("perfil-risco"), anyLong(), eq(true), eq(200));
    }

    @Test
    void shouldReturnConservativeProfile() throws Exception {
        // Arrange
        RiskProfileResponse response = RiskProfileResponse.builder()
            .clienteId(456L)
            .perfil("CONSERVADOR")
            .pontuacao(30)
            .descricao("Cliente com perfil conservador, prioriza segurança e liquidez")
            .build();

        when(riskProfileService.calculateRiskProfile(456L)).thenReturn(response);
        doNothing().when(telemetryService).recordMetric(anyString(), anyLong(), anyBoolean(), anyInt());

        // Act & Assert
        mockMvc.perform(get("/perfil-risco/456")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.clienteId").value(456))
            .andExpect(jsonPath("$.perfil").value("CONSERVADOR"))
            .andExpect(jsonPath("$.pontuacao").value(30));

        verify(riskProfileService).calculateRiskProfile(456L);
        verify(telemetryService).recordMetric(eq("perfil-risco"), anyLong(), eq(true), eq(200));
    }

    @Test
    void shouldReturnAggressiveProfile() throws Exception {
        // Arrange
        RiskProfileResponse response = RiskProfileResponse.builder()
            .clienteId(789L)
            .perfil("AGRESSIVO")
            .pontuacao(85)
            .descricao("Cliente com perfil agressivo, busca maior rentabilidade")
            .build();

        when(riskProfileService.calculateRiskProfile(789L)).thenReturn(response);
        doNothing().when(telemetryService).recordMetric(anyString(), anyLong(), anyBoolean(), anyInt());

        // Act & Assert
        mockMvc.perform(get("/perfil-risco/789")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.clienteId").value(789))
            .andExpect(jsonPath("$.perfil").value("AGRESSIVO"))
            .andExpect(jsonPath("$.pontuacao").value(85));

        verify(riskProfileService).calculateRiskProfile(789L);
        verify(telemetryService).recordMetric(eq("perfil-risco"), anyLong(), eq(true), eq(200));
    }
}
