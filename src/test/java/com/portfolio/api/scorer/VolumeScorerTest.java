package com.portfolio.api.scorer;

import com.portfolio.api.repository.InvestmentRepository;
import com.portfolio.api.scorer.VolumeScorer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VolumeScorerTest {

    @Mock
    private InvestmentRepository investmentRepository;

    @InjectMocks
    private VolumeScorer calculator;

    @Test
    void shouldReturnZeroForNoVolume() {
        when(investmentRepository.sumValorByClienteId(1L)).thenReturn(BigDecimal.ZERO);

        int score = calculator.calculateVolumeScore(1L);

        assertEquals(0, score);
    }

    @Test
    void shouldReturnZeroForNullVolume() {
        when(investmentRepository.sumValorByClienteId(1L)).thenReturn(null);

        int score = calculator.calculateVolumeScore(1L);

        assertEquals(0, score);
    }

    @Test
    void shouldReturn50ForSingleClient() {
        when(investmentRepository.sumValorByClienteId(1L)).thenReturn(new BigDecimal("10000"));
        when(investmentRepository.getAllCustomerVolumes()).thenReturn(Collections.singletonList(new BigDecimal("10000")));

        int score = calculator.calculateVolumeScore(1L);

        assertEquals(50, score);
    }

    @Test
    void shouldReturn50ForEmptyClientList() {
        when(investmentRepository.sumValorByClienteId(1L)).thenReturn(new BigDecimal("10000"));
        when(investmentRepository.getAllCustomerVolumes()).thenReturn(Collections.emptyList());

        int score = calculator.calculateVolumeScore(1L);

        assertEquals(50, score);
    }

    @Test
    void shouldCalculatePercentileRankCorrectly() {
        List<BigDecimal> allVolumes = Arrays.asList(
                new BigDecimal("1000"),
                new BigDecimal("5000"),
                new BigDecimal("10000"),
                new BigDecimal("50000"),
                new BigDecimal("100000")
        );

        when(investmentRepository.sumValorByClienteId(1L)).thenReturn(new BigDecimal("10000"));
        when(investmentRepository.getAllCustomerVolumes()).thenReturn(allVolumes);

        int score = calculator.calculateVolumeScore(1L);

        assertEquals(40, score);
    }

    @Test
    void shouldReturnHighScoreForTopPercentile() {
        List<BigDecimal> allVolumes = Arrays.asList(
                new BigDecimal("1000"),
                new BigDecimal("5000"),
                new BigDecimal("10000"),
                new BigDecimal("20000"),
                new BigDecimal("100000")
        );

        when(investmentRepository.sumValorByClienteId(1L)).thenReturn(new BigDecimal("100000"));
        when(investmentRepository.getAllCustomerVolumes()).thenReturn(allVolumes);

        int score = calculator.calculateVolumeScore(1L);

        assertEquals(80, score);
    }
}
