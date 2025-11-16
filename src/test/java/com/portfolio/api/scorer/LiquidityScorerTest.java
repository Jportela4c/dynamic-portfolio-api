package com.portfolio.api.scorer;

import com.portfolio.api.model.entity.Simulation;
import com.portfolio.api.repository.SimulationRepository;
import com.portfolio.api.scorer.LiquidityScorer;
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
class LiquidityScorerTest {

    @Mock
    private SimulationRepository simulationRepository;

    @InjectMocks
    private LiquidityScorer calculator;

    @Test
    void shouldReturn50ForNoSimulations() {
        when(simulationRepository.findByClienteIdOrderByDataSimulacaoDesc(1L))
                .thenReturn(Collections.emptyList());

        int score = calculator.calculateLiquidityScore(1L);

        assertEquals(50, score);
    }

    @Test
    void shouldReturn100ForLongTermInvestments() {
        List<Simulation> simulations = Arrays.asList(
                createSimulation(36),
                createSimulation(48),
                createSimulation(60)
        );

        when(simulationRepository.findByClienteIdOrderByDataSimulacaoDesc(1L))
                .thenReturn(simulations);

        int score = calculator.calculateLiquidityScore(1L);

        assertEquals(100, score);
    }

    @Test
    void shouldReturn75ForMediumTermInvestments() {
        List<Simulation> simulations = Arrays.asList(
                createSimulation(24),
                createSimulation(30),
                createSimulation(24)
        );

        when(simulationRepository.findByClienteIdOrderByDataSimulacaoDesc(1L))
                .thenReturn(simulations);

        int score = calculator.calculateLiquidityScore(1L);

        assertEquals(75, score);
    }

    @Test
    void shouldReturn50ForAnnualInvestments() {
        List<Simulation> simulations = Arrays.asList(
                createSimulation(12),
                createSimulation(12),
                createSimulation(12)
        );

        when(simulationRepository.findByClienteIdOrderByDataSimulacaoDesc(1L))
                .thenReturn(simulations);

        int score = calculator.calculateLiquidityScore(1L);

        assertEquals(50, score);
    }

    @Test
    void shouldReturn25ForShortTermInvestments() {
        List<Simulation> simulations = Arrays.asList(
                createSimulation(6),
                createSimulation(9),
                createSimulation(6)
        );

        when(simulationRepository.findByClienteIdOrderByDataSimulacaoDesc(1L))
                .thenReturn(simulations);

        int score = calculator.calculateLiquidityScore(1L);

        assertEquals(25, score);
    }

    @Test
    void shouldReturn10ForVeryShortTermInvestments() {
        List<Simulation> simulations = Arrays.asList(
                createSimulation(3),
                createSimulation(3),
                createSimulation(3)
        );

        when(simulationRepository.findByClienteIdOrderByDataSimulacaoDesc(1L))
                .thenReturn(simulations);

        int score = calculator.calculateLiquidityScore(1L);

        assertEquals(10, score);
    }

    private Simulation createSimulation(int prazoMeses) {
        Simulation simulation = new Simulation();
        simulation.setClienteId(1L);
        simulation.setProdutoId(1L);
        simulation.setProdutoNome("CDB");
        simulation.setValorInvestido(new BigDecimal("10000"));
        simulation.setValorFinal(new BigDecimal("11200"));
        simulation.setPrazoMeses(prazoMeses);
        return simulation;
    }
}
