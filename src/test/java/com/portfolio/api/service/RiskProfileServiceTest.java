package com.portfolio.api.service;

import com.portfolio.api.model.dto.response.RiskProfileResponse;
import com.portfolio.api.repository.InvestmentRepository;
import com.portfolio.api.util.RiskProfileCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskProfileServiceTest {

    @Mock
    private InvestmentRepository investmentRepository;

    @Mock
    private RiskProfileCalculator riskProfileCalculator;

    @InjectMocks
    private RiskProfileService riskProfileService;

    @Test
    void shouldCalculateRiskProfile() {
        when(investmentRepository.countByClienteId(123L)).thenReturn(5L);
        when(investmentRepository.sumValorByClienteId(123L))
                .thenReturn(new BigDecimal("50000"));
        when(riskProfileCalculator.calculateScore(new BigDecimal("50000"), 5L))
                .thenReturn(50);
        when(riskProfileCalculator.classifyProfile(50))
                .thenReturn("Moderado");
        when(riskProfileCalculator.getProfileDescription("Moderado"))
                .thenReturn("Perfil equilibrado entre segurança e rentabilidade.");

        RiskProfileResponse response = riskProfileService.calculateRiskProfile(123L);

        assertNotNull(response);
        assertEquals(123L, response.getClienteId());
        assertEquals("Moderado", response.getPerfil());
        assertEquals(50, response.getPontuacao());
        assertNotNull(response.getDescricao());
    }

    @Test
    void shouldHandleNullTotalVolume() {
        when(investmentRepository.countByClienteId(anyLong())).thenReturn(0L);
        when(investmentRepository.sumValorByClienteId(anyLong())).thenReturn(null);
        when(riskProfileCalculator.calculateScore(BigDecimal.ZERO, 0L)).thenReturn(0);
        when(riskProfileCalculator.classifyProfile(0)).thenReturn("Conservador");
        when(riskProfileCalculator.getProfileDescription("Conservador"))
                .thenReturn("Perfil de baixo risco, focado em segurança e liquidez.");

        RiskProfileResponse response = riskProfileService.calculateRiskProfile(999L);

        assertNotNull(response);
        assertEquals("Conservador", response.getPerfil());
    }
}
