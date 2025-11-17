package com.portfolio.api.service;

import com.portfolio.api.exception.InvalidSimulationException;
import com.portfolio.api.exception.ProductNotFoundException;
import com.portfolio.api.model.dto.request.SimulationRequest;
import com.portfolio.api.model.dto.response.DailyAggregationResponse;
import com.portfolio.api.model.dto.response.SimulationHistoryResponse;
import com.portfolio.api.model.dto.response.SimulationResponse;
import com.portfolio.api.model.dto.response.TelemetryResponse;
import com.portfolio.api.model.entity.Product;
import com.portfolio.api.model.entity.Simulation;
import com.portfolio.api.repository.SimulationRepository;
import com.portfolio.api.repository.TelemetryRepository;
import com.portfolio.api.util.InvestmentCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SimulationService {

    private final SimulationRepository simulationRepository;
    private final TelemetryRepository telemetryRepository;
    private final ProductService productService;
    private final InvestmentCalculator investmentCalculator;

    public SimulationService(SimulationRepository simulationRepository,
                             TelemetryRepository telemetryRepository,
                             ProductService productService,
                             InvestmentCalculator investmentCalculator) {
        this.simulationRepository = simulationRepository;
        this.telemetryRepository = telemetryRepository;
        this.productService = productService;
        this.investmentCalculator = investmentCalculator;
    }

    @Transactional
    public SimulationResponse simulateInvestment(SimulationRequest request) {

        Product product = productService.findMatchingProduct(
                request.getTipoProduto(),
                request.getValor(),
                request.getPrazoMeses()
        ).orElseThrow(() -> new ProductNotFoundException("Product not available"));

        if (request.getValor().compareTo(product.getValorMinimo()) < 0) {
            throw new InvalidSimulationException("Invalid investment value");
        }

        if (request.getPrazoMeses() < product.getPrazoMinimoMeses()) {
            throw new InvalidSimulationException("Invalid investment term");
        }

        if (product.getPrazoMaximoMeses() != null && request.getPrazoMeses() > product.getPrazoMaximoMeses()) {
            throw new InvalidSimulationException("Invalid investment term");
        }

        BigDecimal finalValue = investmentCalculator.calculateFinalValue(
                request.getValor(),
                product.getRentabilidade(),
                request.getPrazoMeses()
        );

        Simulation simulation = new Simulation();
        simulation.setClienteId(request.getClienteId());
        simulation.setProdutoId(product.getId());
        simulation.setProdutoNome(product.getNome());
        simulation.setValorInvestido(request.getValor());
        simulation.setValorFinal(finalValue);
        simulation.setPrazoMeses(request.getPrazoMeses());
        simulation.setDataSimulacao(LocalDateTime.now());

        simulationRepository.save(simulation);

        return SimulationResponse.builder()
                .produtoValidado(SimulationResponse.ProductValidated.builder()
                        .id(product.getId())
                        .nome(product.getNome())
                        .tipo(product.getTipo())
                        .rentabilidade(product.getRentabilidade())
                        .risco(product.getRisco())
                        .build())
                .resultadoSimulacao(SimulationResponse.SimulationResult.builder()
                        .valorFinal(finalValue)
                        .rentabilidadeEfetiva(product.getRentabilidade())
                        .prazoMeses(request.getPrazoMeses())
                        .build())
                .dataSimulacao(simulation.getDataSimulacao())
                .build();
    }

    public List<SimulationHistoryResponse> getAllSimulations() {
        List<Simulation> simulations = simulationRepository.findAll();

        return simulations.stream()
                .map(sim -> SimulationHistoryResponse.builder()
                        .id(sim.getId())
                        .clienteId(sim.getClienteId())
                        .produto(sim.getProdutoNome())
                        .valorInvestido(sim.getValorInvestido())
                        .valorFinal(sim.getValorFinal())
                        .prazoMeses(sim.getPrazoMeses())
                        .dataSimulacao(sim.getDataSimulacao())
                        .build())
                .collect(Collectors.toList());
    }

    public List<DailyAggregationResponse> getDailyAggregations() {
        List<SimulationRepository.DailyAggregation> aggregations =
                simulationRepository.findDailyAggregations();

        return aggregations.stream()
                .map(agg -> DailyAggregationResponse.builder()
                        .produto(agg.getProduto())
                        .data(agg.getData())
                        .quantidadeSimulacoes(agg.getQuantidadeSimulacoes())
                        .mediaValorFinal(agg.getMediaValorFinal())
                        .build())
                .collect(Collectors.toList());
    }

    public TelemetryResponse getTelemetry() {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        List<TelemetryRepository.ServiceMetrics> metrics =
                telemetryRepository.findMetricsByPeriod(startOfMonth, endOfMonth);

        List<TelemetryResponse.ServiceMetrics> serviceMetrics = metrics.stream()
                .map(m -> TelemetryResponse.ServiceMetrics.builder()
                        .nome(m.getServico())
                        .quantidadeChamadas(m.getQuantidadeChamadas())
                        .mediaTempoRespostaMs(m.getMediaTempoRespostaMs().longValue())
                        .build())
                .collect(Collectors.toList());

        return TelemetryResponse.builder()
                .servicos(serviceMetrics)
                .periodo(TelemetryResponse.Period.builder()
                        .inicio(startOfMonth.toLocalDate())
                        .fim(endOfMonth.toLocalDate())
                        .build())
                .build();
    }
}
