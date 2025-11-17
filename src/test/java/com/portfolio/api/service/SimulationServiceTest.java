package com.portfolio.api.service;

import com.portfolio.api.exception.InvalidSimulationException;
import com.portfolio.api.exception.ProductNotFoundException;
import com.portfolio.api.model.dto.request.SimulationRequest;
import com.portfolio.api.model.dto.response.SimulationResponse;
import com.portfolio.api.model.enums.TipoProduto;
import com.portfolio.api.model.entity.Product;
import com.portfolio.api.repository.SimulationRepository;
import com.portfolio.api.repository.TelemetryRepository;
import com.portfolio.api.util.InvestmentCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimulationServiceTest {

    @Mock
    private SimulationRepository simulationRepository;

    @Mock
    private ProductService productService;

    @Mock
    private InvestmentCalculator investmentCalculator;

    @InjectMocks
    private SimulationService simulationService;

    private Product testProduct;
    private SimulationRequest testRequest;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setNome("CDB Test");
        testProduct.setTipo(TipoProduto.CDB);
        testProduct.setRentabilidade(new BigDecimal("0.12"));
        testProduct.setRisco("Baixo");
        testProduct.setValorMinimo(new BigDecimal("1000"));
        testProduct.setPrazoMinimoMeses(6);
        testProduct.setPrazoMaximoMeses(60);

        testRequest = new SimulationRequest();
        testRequest.setClienteId(123L);
        testRequest.setValor(new BigDecimal("10000"));
        testRequest.setPrazoMeses(12);
        testRequest.setTipoProduto(TipoProduto.CDB);
    }

    @Test
    void shouldSimulateInvestmentSuccessfully() {
        when(productService.findMatchingProduct(any(TipoProduto.class), any(), anyInt()))
                .thenReturn(Optional.of(testProduct));
        when(investmentCalculator.calculateFinalValue(any(), any(), anyInt()))
                .thenReturn(new BigDecimal("11200.00"));

        SimulationResponse response = simulationService.simulateInvestment(testRequest);

        assertNotNull(response);
        assertNotNull(response.getProdutoValidado());
        assertEquals("CDB Test", response.getProdutoValidado().getNome());
        assertEquals(new BigDecimal("11200.00"), response.getResultadoSimulacao().getValorFinal());
        verify(simulationRepository, times(1)).save(any());
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        when(productService.findMatchingProduct(any(TipoProduto.class), any(), anyInt()))
                .thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () ->
                simulationService.simulateInvestment(testRequest)
        );
    }

    @Test
    void shouldThrowExceptionWhenValueBelowMinimum() {
        testRequest.setValor(new BigDecimal("500"));

        when(productService.findMatchingProduct(any(TipoProduto.class), any(), anyInt()))
                .thenReturn(Optional.of(testProduct));

        assertThrows(InvalidSimulationException.class, () ->
                simulationService.simulateInvestment(testRequest)
        );
    }

    @Test
    void shouldThrowExceptionForZeroValue() {
        testRequest.setValor(BigDecimal.ZERO);

        when(productService.findMatchingProduct(any(TipoProduto.class), any(), anyInt()))
                .thenReturn(Optional.of(testProduct));

        assertThrows(InvalidSimulationException.class, () ->
                simulationService.simulateInvestment(testRequest)
        );
    }

    @Test
    void shouldThrowExceptionForNegativeValue() {
        testRequest.setValor(new BigDecimal("-1000"));

        when(productService.findMatchingProduct(any(TipoProduto.class), any(), anyInt()))
                .thenReturn(Optional.of(testProduct));

        assertThrows(InvalidSimulationException.class, () ->
                simulationService.simulateInvestment(testRequest)
        );
    }

    @Test
    void shouldAcceptValueExactlyAtMinimum() {
        testRequest.setValor(new BigDecimal("1000"));

        when(productService.findMatchingProduct(any(TipoProduto.class), any(), anyInt()))
                .thenReturn(Optional.of(testProduct));
        when(investmentCalculator.calculateFinalValue(any(), any(), anyInt()))
                .thenReturn(new BigDecimal("1120.00"));

        SimulationResponse response = simulationService.simulateInvestment(testRequest);

        assertNotNull(response);
        verify(simulationRepository, times(1)).save(any());
    }

    @Test
    void shouldAcceptTermExactlyAtMinimum() {
        testRequest.setPrazoMeses(6);

        when(productService.findMatchingProduct(any(TipoProduto.class), any(), anyInt()))
                .thenReturn(Optional.of(testProduct));
        when(investmentCalculator.calculateFinalValue(any(), any(), anyInt()))
                .thenReturn(new BigDecimal("10600.00"));

        SimulationResponse response = simulationService.simulateInvestment(testRequest);

        assertNotNull(response);
        verify(simulationRepository, times(1)).save(any());
    }

    @Test
    void shouldAcceptTermExactlyAtMaximum() {
        testRequest.setPrazoMeses(60);

        when(productService.findMatchingProduct(any(TipoProduto.class), any(), anyInt()))
                .thenReturn(Optional.of(testProduct));
        when(investmentCalculator.calculateFinalValue(any(), any(), anyInt()))
                .thenReturn(new BigDecimal("16000.00"));

        SimulationResponse response = simulationService.simulateInvestment(testRequest);

        assertNotNull(response);
        verify(simulationRepository, times(1)).save(any());
    }

    @Test
    void shouldHandleVeryLargeValue() {
        testRequest.setValor(new BigDecimal("999999999.99"));

        when(productService.findMatchingProduct(any(TipoProduto.class), any(), anyInt()))
                .thenReturn(Optional.of(testProduct));
        when(investmentCalculator.calculateFinalValue(any(), any(), anyInt()))
                .thenReturn(new BigDecimal("1119999999.99"));

        SimulationResponse response = simulationService.simulateInvestment(testRequest);

        assertNotNull(response);
        verify(simulationRepository, times(1)).save(any());
    }
}
