package com.portfolio.api.repository;

import com.portfolio.api.model.entity.Simulation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SimulationRepositoryTest {

    @Autowired
    private SimulationRepository simulationRepository;

    @BeforeEach
    void setUp() {
        simulationRepository.deleteAll();
    }

    @Test
    void shouldFindSimulationsByClientId() {
        Simulation sim1 = createSimulation(123L, "CDB Product", new BigDecimal("10000"), new BigDecimal("11000"));
        Simulation sim2 = createSimulation(123L, "LCI Product", new BigDecimal("20000"), new BigDecimal("22000"));
        Simulation sim3 = createSimulation(456L, "CDB Product", new BigDecimal("15000"), new BigDecimal("16500"));

        simulationRepository.save(sim1);
        simulationRepository.save(sim2);
        simulationRepository.save(sim3);

        List<Simulation> simulations = simulationRepository.findByClienteId(123L);

        assertThat(simulations).hasSize(2);
        assertThat(simulations).extracting(Simulation::getClienteId).containsOnly(123L);
    }

    @Test
    void shouldFindSimulationsByClientIdOrderedByDate() {
        LocalDateTime now = LocalDateTime.now();
        Simulation sim1 = createSimulation(123L, "Product 1", new BigDecimal("10000"), new BigDecimal("11000"));
        sim1.setDataSimulacao(now.minusDays(2));

        Simulation sim2 = createSimulation(123L, "Product 2", new BigDecimal("20000"), new BigDecimal("22000"));
        sim2.setDataSimulacao(now.minusDays(1));

        Simulation sim3 = createSimulation(123L, "Product 3", new BigDecimal("15000"), new BigDecimal("16500"));
        sim3.setDataSimulacao(now);

        simulationRepository.save(sim1);
        simulationRepository.save(sim2);
        simulationRepository.save(sim3);

        List<Simulation> simulations = simulationRepository.findByClienteIdOrderByDataSimulacaoDesc(123L);

        assertThat(simulations).hasSize(3);
        assertThat(simulations.get(0).getProdutoNome()).isEqualTo("Product 3");
        assertThat(simulations.get(1).getProdutoNome()).isEqualTo("Product 2");
        assertThat(simulations.get(2).getProdutoNome()).isEqualTo("Product 1");
    }

    @Test
    void shouldReturnEmptyListWhenNoSimulationsForClient() {
        Simulation sim = createSimulation(123L, "Product", new BigDecimal("10000"), new BigDecimal("11000"));
        simulationRepository.save(sim);

        List<Simulation> simulations = simulationRepository.findByClienteId(999L);

        assertThat(simulations).isEmpty();
    }

    @Test
    void shouldFindDailyAggregations() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1);

        Simulation sim1 = createSimulation(123L, "CDB Product", new BigDecimal("10000"), new BigDecimal("11000"));
        sim1.setDataSimulacao(today);

        Simulation sim2 = createSimulation(456L, "CDB Product", new BigDecimal("20000"), new BigDecimal("22000"));
        sim2.setDataSimulacao(today);

        Simulation sim3 = createSimulation(789L, "LCI Product", new BigDecimal("15000"), new BigDecimal("16500"));
        sim3.setDataSimulacao(yesterday);

        simulationRepository.save(sim1);
        simulationRepository.save(sim2);
        simulationRepository.save(sim3);

        List<SimulationRepository.DailyAggregation> aggregations = simulationRepository.findDailyAggregations();

        assertThat(aggregations).isNotEmpty();
        assertThat(aggregations).hasSizeGreaterThanOrEqualTo(2);

        SimulationRepository.DailyAggregation todayCDB = aggregations.stream()
                .filter(a -> a.getProduto().equals("CDB Product") && a.getData().equals(today.toLocalDate()))
                .findFirst()
                .orElse(null);

        assertThat(todayCDB).isNotNull();
        assertThat(todayCDB.getQuantidadeSimulacoes()).isEqualTo(2L);
        assertThat(todayCDB.getMediaValorFinal()).isEqualByComparingTo(new BigDecimal("16500.00"));
    }

    @Test
    void shouldGroupAggregationsByProductAndDate() {
        LocalDateTime today = LocalDateTime.now();

        Simulation sim1 = createSimulation(123L, "CDB Product", new BigDecimal("10000"), new BigDecimal("11000"));
        sim1.setDataSimulacao(today);

        Simulation sim2 = createSimulation(456L, "LCI Product", new BigDecimal("20000"), new BigDecimal("22000"));
        sim2.setDataSimulacao(today);

        simulationRepository.save(sim1);
        simulationRepository.save(sim2);

        List<SimulationRepository.DailyAggregation> aggregations = simulationRepository.findDailyAggregations();

        assertThat(aggregations).hasSizeGreaterThanOrEqualTo(2);
        assertThat(aggregations).extracting(SimulationRepository.DailyAggregation::getProduto)
                .contains("CDB Product", "LCI Product");
    }

    private Simulation createSimulation(Long clienteId, String produtoNome, BigDecimal valorInvestido, BigDecimal valorFinal) {
        Simulation simulation = new Simulation();
        simulation.setClienteId(clienteId);
        simulation.setProdutoId(1L);
        simulation.setProdutoNome(produtoNome);
        simulation.setValorInvestido(valorInvestido);
        simulation.setPrazoMeses(12);
        simulation.setValorFinal(valorFinal);
        simulation.setDataSimulacao(LocalDateTime.now());
        return simulation;
    }
}
