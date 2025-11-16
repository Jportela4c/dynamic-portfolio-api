package com.portfolio.api.util;

import com.portfolio.api.model.entity.Simulation;
import com.portfolio.api.repository.SimulationRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LiquidityPreferenceCalculator {

    private final SimulationRepository simulationRepository;

    public LiquidityPreferenceCalculator(SimulationRepository simulationRepository) {
        this.simulationRepository = simulationRepository;
    }

    public int calculateLiquidityScore(Long clienteId) {
        List<Simulation> simulations = simulationRepository.findByClienteIdOrderByDataSimulacaoDesc(clienteId);

        if (simulations.isEmpty()) {
            return 50;
        }

        double avgTermMonths = simulations.stream()
                .mapToInt(Simulation::getPrazoMeses)
                .average()
                .orElse(12.0);

        return calculateTermScore(avgTermMonths);
    }

    private int calculateTermScore(double avgTermMonths) {
        if (avgTermMonths >= 36) return 100;
        if (avgTermMonths >= 24) return 75;
        if (avgTermMonths >= 12) return 50;
        if (avgTermMonths >= 6) return 25;
        return 10;
    }
}
