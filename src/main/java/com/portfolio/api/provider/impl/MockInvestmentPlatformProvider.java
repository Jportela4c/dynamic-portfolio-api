package com.portfolio.api.provider.impl;

import com.portfolio.api.model.enums.TipoProduto;
import com.portfolio.api.provider.InvestmentPlatformProvider;
import com.portfolio.api.provider.dto.CustomerPortfolio;
import com.portfolio.api.provider.dto.Investment;
import com.portfolio.api.provider.dto.Position;
import com.portfolio.api.util.CpfValidator;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementação mock do InvestmentPlatformProvider com dados realistas de investimentos brasileiros.
 */
@Slf4j
@Service
@Profile("test")
public class MockInvestmentPlatformProvider implements InvestmentPlatformProvider {

    private final Map<String, CustomerPortfolio> mockPortfolios = new HashMap<>();
    private final Map<String, List<Position>> mockPositions = new HashMap<>();
    private final Map<String, List<Investment>> mockInvestmentHistory = new HashMap<>();

    @PostConstruct
    public void init() {
        log.info("Initializing MockInvestmentPlatformProvider with realistic Brazilian investment data");

        initCustomer12345678900();
        initCustomer98765432100();
        initCustomer11122233344();
        initCustomer44455566677();
        initCustomer55566677788();

        log.info("MockInvestmentPlatformProvider initialized with {} portfolios", mockPortfolios.size());
    }

    private void initCustomer12345678900() {
        String cpf = "12345678900";

        mockPortfolios.put(cpf, CustomerPortfolio.builder()
                .cpf(cpf)
                .totalInvestido(new BigDecimal("50000.00"))
                .valorAtual(new BigDecimal("53500.00"))
                .rentabilidadeTotal(new BigDecimal("0.07"))
                .dataUltimaAtualizacao(LocalDateTime.now())
                .build());

        mockPositions.put(cpf, Arrays.asList(
                Position.builder()
                        .codigoProduto("CDB_001")
                        .nomeProduto("CDB CAIXA 110% CDI")
                        .tipoProduto("CDB")
                        .valorAplicado(new BigDecimal("10000.00"))
                        .valorAtual(new BigDecimal("10650.00"))
                        .rentabilidade(new BigDecimal("0.065"))
                        .dataAplicacao(LocalDate.of(2025, 1, 15))
                        .dataVencimento(LocalDate.of(2026, 1, 15))
                        .liquidez("VENCIMENTO")
                        .build(),
                Position.builder()
                        .codigoProduto("LCA_002")
                        .nomeProduto("LCA CAIXA Agro")
                        .tipoProduto("LCA")
                        .valorAplicado(new BigDecimal("15000.00"))
                        .valorAtual(new BigDecimal("15450.00"))
                        .rentabilidade(new BigDecimal("0.03"))
                        .dataAplicacao(LocalDate.of(2025, 6, 1))
                        .dataVencimento(LocalDate.of(2026, 6, 1))
                        .liquidez("VENCIMENTO")
                        .build(),
                Position.builder()
                        .codigoProduto("TESOURO_SELIC")
                        .nomeProduto("Tesouro Selic 2027")
                        .tipoProduto("TESOURO_SELIC")
                        .valorAplicado(new BigDecimal("25000.00"))
                        .valorAtual(new BigDecimal("27400.00"))
                        .rentabilidade(new BigDecimal("0.096"))
                        .dataAplicacao(LocalDate.of(2024, 3, 10))
                        .dataVencimento(LocalDate.of(2027, 3, 1))
                        .liquidez("DIARIA")
                        .build()
        ));

        mockInvestmentHistory.put(cpf, Arrays.asList(
                Investment.builder()
                        .id(1L)
                        .tipo(TipoProduto.CDB)
                        .tipoOperacao("APLICACAO")
                        .valor(new BigDecimal("10000.00"))
                        .rentabilidade(new BigDecimal("0.065"))
                        .data(LocalDate.of(2025, 1, 15))
                        .nomeProduto("CDB CAIXA 110% CDI")
                        .build(),
                Investment.builder()
                        .id(2L)
                        .tipo(TipoProduto.MULTIMERCADO)
                        .tipoOperacao("RESGATE")
                        .valor(new BigDecimal("5000.00"))
                        .rentabilidade(new BigDecimal("0.08"))
                        .data(LocalDate.of(2025, 3, 10))
                        .nomeProduto("CAIXA FIC Multimercado")
                        .build(),
                Investment.builder()
                        .id(3L)
                        .tipo(TipoProduto.LCA)
                        .tipoOperacao("APLICACAO")
                        .valor(new BigDecimal("15000.00"))
                        .rentabilidade(new BigDecimal("0.03"))
                        .data(LocalDate.of(2025, 6, 1))
                        .nomeProduto("LCA CAIXA Agro")
                        .build()
        ));
    }

    private void initCustomer98765432100() {
        String cpf = "98765432100";

        mockPortfolios.put(cpf, CustomerPortfolio.builder()
                .cpf(cpf)
                .totalInvestido(new BigDecimal("30000.00"))
                .valorAtual(new BigDecimal("30900.00"))
                .rentabilidadeTotal(new BigDecimal("0.03"))
                .dataUltimaAtualizacao(LocalDateTime.now())
                .build());

        mockPositions.put(cpf, Arrays.asList(
                Position.builder()
                        .codigoProduto("POUPANCA_CAIXA")
                        .nomeProduto("Poupança CAIXA")
                        .tipoProduto("POUPANCA")
                        .valorAplicado(new BigDecimal("20000.00"))
                        .valorAtual(new BigDecimal("20600.00"))
                        .rentabilidade(new BigDecimal("0.03"))
                        .dataAplicacao(LocalDate.of(2024, 1, 1))
                        .dataVencimento(null)
                        .liquidez("DIARIA")
                        .build(),
                Position.builder()
                        .codigoProduto("LCI_001")
                        .nomeProduto("LCI CAIXA Imobiliário")
                        .tipoProduto("LCI")
                        .valorAplicado(new BigDecimal("10000.00"))
                        .valorAtual(new BigDecimal("10300.00"))
                        .rentabilidade(new BigDecimal("0.03"))
                        .dataAplicacao(LocalDate.of(2025, 2, 1))
                        .dataVencimento(LocalDate.of(2026, 2, 1))
                        .liquidez("VENCIMENTO")
                        .build()
        ));

        mockInvestmentHistory.put(cpf, Arrays.asList(
                Investment.builder()
                        .id(4L)
                        .tipo(TipoProduto.POUPANCA)
                        .tipoOperacao("APLICACAO")
                        .valor(new BigDecimal("20000.00"))
                        .rentabilidade(new BigDecimal("0.03"))
                        .data(LocalDate.of(2024, 1, 1))
                        .nomeProduto("Poupança CAIXA")
                        .build(),
                Investment.builder()
                        .id(5L)
                        .tipo(TipoProduto.LCI)
                        .tipoOperacao("APLICACAO")
                        .valor(new BigDecimal("10000.00"))
                        .rentabilidade(new BigDecimal("0.03"))
                        .data(LocalDate.of(2025, 2, 1))
                        .nomeProduto("LCI CAIXA Imobiliário")
                        .build()
        ));
    }

    private void initCustomer11122233344() {
        String cpf = "11122233344";

        mockPortfolios.put(cpf, CustomerPortfolio.builder()
                .cpf(cpf)
                .totalInvestido(new BigDecimal("150000.00"))
                .valorAtual(new BigDecimal("165000.00"))
                .rentabilidadeTotal(new BigDecimal("0.10"))
                .dataUltimaAtualizacao(LocalDateTime.now())
                .build());

        mockPositions.put(cpf, Arrays.asList(
                Position.builder()
                        .codigoProduto("ACOES")
                        .nomeProduto("CAIXA FIC Ações Ibovespa")
                        .tipoProduto("ACOES")
                        .valorAplicado(new BigDecimal("50000.00"))
                        .valorAtual(new BigDecimal("57000.00"))
                        .rentabilidade(new BigDecimal("0.14"))
                        .dataAplicacao(LocalDate.of(2024, 1, 10))
                        .dataVencimento(null)
                        .liquidez("D+1")
                        .build(),
                Position.builder()
                        .codigoProduto("CDB_PREMIUM")
                        .nomeProduto("CDB CAIXA Premium 120% CDI")
                        .tipoProduto("CDB")
                        .valorAplicado(new BigDecimal("100000.00"))
                        .valorAtual(new BigDecimal("108000.00"))
                        .rentabilidade(new BigDecimal("0.08"))
                        .dataAplicacao(LocalDate.of(2024, 6, 1))
                        .dataVencimento(LocalDate.of(2026, 6, 1))
                        .liquidez("VENCIMENTO")
                        .build()
        ));

        mockInvestmentHistory.put(cpf, Arrays.asList(
                Investment.builder()
                        .id(6L)
                        .tipo(TipoProduto.ACOES)
                        .tipoOperacao("APLICACAO")
                        .valor(new BigDecimal("50000.00"))
                        .rentabilidade(new BigDecimal("0.14"))
                        .data(LocalDate.of(2024, 1, 10))
                        .nomeProduto("CAIXA FIC Ações Ibovespa")
                        .build(),
                Investment.builder()
                        .id(7L)
                        .tipo(TipoProduto.CDB)
                        .tipoOperacao("APLICACAO")
                        .valor(new BigDecimal("100000.00"))
                        .rentabilidade(new BigDecimal("0.08"))
                        .data(LocalDate.of(2024, 6, 1))
                        .nomeProduto("CDB CAIXA Premium 120% CDI")
                        .build()
        ));
    }

    private void initCustomer44455566677() {
        String cpf = "44455566677";

        mockPortfolios.put(cpf, CustomerPortfolio.builder()
                .cpf(cpf)
                .totalInvestido(new BigDecimal("15000.00"))
                .valorAtual(new BigDecimal("15750.00"))
                .rentabilidadeTotal(new BigDecimal("0.05"))
                .dataUltimaAtualizacao(LocalDateTime.now())
                .build());

        mockPositions.put(cpf, Collections.singletonList(
                Position.builder()
                        .codigoProduto("TESOURO_IPCA")
                        .nomeProduto("Tesouro IPCA+ 2029")
                        .tipoProduto("TESOURO_SELIC")
                        .valorAplicado(new BigDecimal("15000.00"))
                        .valorAtual(new BigDecimal("15750.00"))
                        .rentabilidade(new BigDecimal("0.05"))
                        .dataAplicacao(LocalDate.of(2025, 1, 20))
                        .dataVencimento(LocalDate.of(2029, 8, 15))
                        .liquidez("DIARIA")
                        .build()
        ));

        mockInvestmentHistory.put(cpf, Collections.singletonList(
                Investment.builder()
                        .id(8L)
                        .tipo(TipoProduto.TESOURO_SELIC)
                        .tipoOperacao("APLICACAO")
                        .valor(new BigDecimal("15000.00"))
                        .rentabilidade(new BigDecimal("0.05"))
                        .data(LocalDate.of(2025, 1, 20))
                        .nomeProduto("Tesouro IPCA+ 2029")
                        .build()
        ));
    }

    private void initCustomer55566677788() {
        String cpf = "55566677788";

        mockPortfolios.put(cpf, CustomerPortfolio.builder()
                .cpf(cpf)
                .totalInvestido(new BigDecimal("8000.00"))
                .valorAtual(new BigDecimal("8200.00"))
                .rentabilidadeTotal(new BigDecimal("0.025"))
                .dataUltimaAtualizacao(LocalDateTime.now())
                .build());

        mockPositions.put(cpf, Collections.singletonList(
                Position.builder()
                        .codigoProduto("POUPANCA_CAIXA")
                        .nomeProduto("Poupança CAIXA")
                        .tipoProduto("POUPANCA")
                        .valorAplicado(new BigDecimal("8000.00"))
                        .valorAtual(new BigDecimal("8200.00"))
                        .rentabilidade(new BigDecimal("0.025"))
                        .dataAplicacao(LocalDate.of(2024, 6, 1))
                        .dataVencimento(null)
                        .liquidez("DIARIA")
                        .build()
        ));

        mockInvestmentHistory.put(cpf, Collections.singletonList(
                Investment.builder()
                        .id(9L)
                        .tipo(TipoProduto.POUPANCA)
                        .tipoOperacao("APLICACAO")
                        .valor(new BigDecimal("8000.00"))
                        .rentabilidade(new BigDecimal("0.025"))
                        .data(LocalDate.of(2024, 6, 1))
                        .nomeProduto("Poupança CAIXA")
                        .build()
        ));
    }

    @Override
    public CustomerPortfolio getPortfolio(String cpf) {
        String normalizedCpf = CpfValidator.normalize(cpf);
        CustomerPortfolio portfolio = mockPortfolios.get(normalizedCpf);

        if (portfolio != null) {
            log.debug("Portfolio found for CPF: {}", CpfValidator.mask(normalizedCpf));
        } else {
            log.debug("Portfolio not found for CPF: {}", CpfValidator.mask(normalizedCpf));
        }

        return portfolio;
    }

    @Override
    public List<Investment> getInvestmentHistory(String cpf) {
        String normalizedCpf = CpfValidator.normalize(cpf);
        return mockInvestmentHistory.getOrDefault(normalizedCpf, Collections.emptyList());
    }

    @Override
    public List<Position> getCurrentPositions(String cpf) {
        String normalizedCpf = CpfValidator.normalize(cpf);
        return mockPositions.getOrDefault(normalizedCpf, Collections.emptyList());
    }
}
