package com.portfolio.api.provider.impl;

import com.portfolio.api.provider.TransactionSystemProvider;
import com.portfolio.api.provider.dto.BehavioralIndicators;
import com.portfolio.api.provider.dto.Transaction;
import com.portfolio.api.provider.dto.TransactionSummary;
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
 * Implementação mock do TransactionSystemProvider com dados realistas de movimentações brasileiras.
 * Active when: NOT in prod profile (dev, test, default).
 */
@Slf4j
@Service
@Profile("!prod")
public class MockTransactionSystemProvider implements TransactionSystemProvider {

    private final Map<String, List<Transaction>> mockTransactions = new HashMap<>();
    private final Map<String, BehavioralIndicators> mockBehavioralIndicators = new HashMap<>();

    @PostConstruct
    public void init() {
        log.info("Initializing MockTransactionSystemProvider with realistic Brazilian transaction data");

        initCustomer12345678900();
        initCustomer98765432100();
        initCustomer11122233344();
        initCustomer44455566677();
        initCustomer55566677788();

        log.info("MockTransactionSystemProvider initialized with {} customer transaction histories", mockTransactions.size());
    }

    private void initCustomer12345678900() {
        String cpf = "12345678900";

        mockTransactions.put(cpf, Arrays.asList(
                Transaction.builder()
                        .id(123L)
                        .tipo("APLICACAO")
                        .produto("CDB")
                        .valor(new BigDecimal("10000.00"))
                        .data(LocalDateTime.of(2025, 1, 15, 9, 30))
                        .canal("INTERNET_BANKING")
                        .build(),
                Transaction.builder()
                        .id(124L)
                        .tipo("RESGATE")
                        .produto("POUPANCA")
                        .valor(new BigDecimal("2000.00"))
                        .data(LocalDateTime.of(2025, 1, 20, 14, 15))
                        .canal("APP_MOBILE")
                        .build(),
                Transaction.builder()
                        .id(125L)
                        .tipo("APLICACAO")
                        .produto("LCA")
                        .valor(new BigDecimal("15000.00"))
                        .data(LocalDateTime.of(2025, 6, 1, 10, 0))
                        .canal("AGENCIA")
                        .build(),
                Transaction.builder()
                        .id(126L)
                        .tipo("RESGATE")
                        .produto("FUNDO_MULTIMERCADO")
                        .valor(new BigDecimal("5000.00"))
                        .data(LocalDateTime.of(2025, 3, 10, 11, 30))
                        .canal("INTERNET_BANKING")
                        .build()
        ));

        mockBehavioralIndicators.put(cpf, BehavioralIndicators.builder()
                .pontuacaoRisco(65)
                .perfilCalculado("MODERADO")
                .tendenciaAlocacao("RENDA_FIXA")
                .nivelAtividade("ALTO")
                .build());
    }

    private void initCustomer98765432100() {
        String cpf = "98765432100";

        mockTransactions.put(cpf, Arrays.asList(
                Transaction.builder()
                        .id(127L)
                        .tipo("APLICACAO")
                        .produto("POUPANCA")
                        .valor(new BigDecimal("20000.00"))
                        .data(LocalDateTime.of(2024, 1, 1, 8, 0))
                        .canal("AGENCIA")
                        .build(),
                Transaction.builder()
                        .id(128L)
                        .tipo("APLICACAO")
                        .produto("LCI")
                        .valor(new BigDecimal("10000.00"))
                        .data(LocalDateTime.of(2025, 2, 1, 9, 0))
                        .canal("INTERNET_BANKING")
                        .build()
        ));

        mockBehavioralIndicators.put(cpf, BehavioralIndicators.builder()
                .pontuacaoRisco(30)
                .perfilCalculado("CONSERVADOR")
                .tendenciaAlocacao("LIQUIDEZ")
                .nivelAtividade("BAIXO")
                .build());
    }

    private void initCustomer11122233344() {
        String cpf = "11122233344";

        mockTransactions.put(cpf, Arrays.asList(
                Transaction.builder()
                        .id(129L)
                        .tipo("APLICACAO")
                        .produto("FUNDO_ACOES")
                        .valor(new BigDecimal("50000.00"))
                        .data(LocalDateTime.of(2024, 1, 10, 10, 0))
                        .canal("INTERNET_BANKING")
                        .build(),
                Transaction.builder()
                        .id(130L)
                        .tipo("APLICACAO")
                        .produto("CDB")
                        .valor(new BigDecimal("100000.00"))
                        .data(LocalDateTime.of(2024, 6, 1, 11, 0))
                        .canal("AGENCIA")
                        .build(),
                Transaction.builder()
                        .id(131L)
                        .tipo("APLICACAO")
                        .produto("FUNDO_ACOES")
                        .valor(new BigDecimal("30000.00"))
                        .data(LocalDateTime.of(2024, 9, 15, 14, 30))
                        .canal("APP_MOBILE")
                        .build(),
                Transaction.builder()
                        .id(132L)
                        .tipo("RESGATE")
                        .produto("CDB")
                        .valor(new BigDecimal("20000.00"))
                        .data(LocalDateTime.of(2025, 2, 1, 10, 0))
                        .canal("INTERNET_BANKING")
                        .build()
        ));

        mockBehavioralIndicators.put(cpf, BehavioralIndicators.builder()
                .pontuacaoRisco(85)
                .perfilCalculado("AGRESSIVO")
                .tendenciaAlocacao("RENDA_VARIAVEL")
                .nivelAtividade("MUITO_ALTO")
                .build());
    }

    private void initCustomer44455566677() {
        String cpf = "44455566677";

        mockTransactions.put(cpf, Collections.singletonList(
                Transaction.builder()
                        .id(133L)
                        .tipo("APLICACAO")
                        .produto("TESOURO_DIRETO")
                        .valor(new BigDecimal("15000.00"))
                        .data(LocalDateTime.of(2025, 1, 20, 15, 0))
                        .canal("INTERNET_BANKING")
                        .build()
        ));

        mockBehavioralIndicators.put(cpf, BehavioralIndicators.builder()
                .pontuacaoRisco(50)
                .perfilCalculado("MODERADO")
                .tendenciaAlocacao("RENDA_FIXA")
                .nivelAtividade("MEDIO")
                .build());
    }

    private void initCustomer55566677788() {
        String cpf = "55566677788";

        mockTransactions.put(cpf, Collections.singletonList(
                Transaction.builder()
                        .id(134L)
                        .tipo("APLICACAO")
                        .produto("POUPANCA")
                        .valor(new BigDecimal("8000.00"))
                        .data(LocalDateTime.of(2024, 6, 1, 9, 0))
                        .canal("AGENCIA")
                        .build()
        ));

        mockBehavioralIndicators.put(cpf, BehavioralIndicators.builder()
                .pontuacaoRisco(25)
                .perfilCalculado("CONSERVADOR")
                .tendenciaAlocacao("LIQUIDEZ")
                .nivelAtividade("BAIXO")
                .build());
    }

    @Override
    public TransactionSummary getTransactionSummary(String cpf, LocalDate inicio, LocalDate fim) {
        String normalizedCpf = CpfValidator.normalize(cpf);
        List<Transaction> transactions = getTransactions(normalizedCpf, inicio, fim);

        BigDecimal totalAplicacoes = BigDecimal.ZERO;
        BigDecimal totalResgates = BigDecimal.ZERO;
        int countAplicacoes = 0;
        int countResgates = 0;

        for (Transaction transaction : transactions) {
            if ("APLICACAO".equals(transaction.getTipo())) {
                totalAplicacoes = totalAplicacoes.add(transaction.getValor());
                countAplicacoes++;
            } else if ("RESGATE".equals(transaction.getTipo())) {
                totalResgates = totalResgates.add(transaction.getValor());
                countResgates++;
            }
        }

        BigDecimal preferenciaLiquidez = calculatePreferenciaLiquidez(transactions);
        BigDecimal preferenciaProfitabilidade = BigDecimal.ONE.subtract(preferenciaLiquidez);

        double frequenciaMediaDias = calculateFrequenciaMedia(transactions, inicio, fim);

        return TransactionSummary.builder()
                .cpf(normalizedCpf)
                .periodo(TransactionSummary.Periodo.builder()
                        .dataInicio(inicio)
                        .dataFim(fim)
                        .build())
                .resumo(TransactionSummary.Resumo.builder()
                        .totalMovimentacoes(transactions.size())
                        .volumeTotalAplicacoes(totalAplicacoes)
                        .volumeTotalResgates(totalResgates)
                        .frequenciaMediaDias(frequenciaMediaDias)
                        .preferenciaLiquidez(preferenciaLiquidez)
                        .preferenciaProfitabilidade(preferenciaProfitabilidade)
                        .build())
                .build();
    }

    @Override
    public List<Transaction> getTransactions(String cpf, LocalDate inicio, LocalDate fim) {
        String normalizedCpf = CpfValidator.normalize(cpf);
        List<Transaction> allTransactions = mockTransactions.getOrDefault(normalizedCpf, Collections.emptyList());

        return allTransactions.stream()
                .filter(t -> !t.getData().toLocalDate().isBefore(inicio) && !t.getData().toLocalDate().isAfter(fim))
                .toList();
    }

    @Override
    public BehavioralIndicators getBehavioralIndicators(String cpf) {
        String normalizedCpf = CpfValidator.normalize(cpf);
        BehavioralIndicators indicators = mockBehavioralIndicators.get(normalizedCpf);

        if (indicators != null) {
            log.debug("Behavioral indicators found for CPF: {}", CpfValidator.mask(normalizedCpf));
        } else {
            log.debug("Behavioral indicators not found for CPF: {}", CpfValidator.mask(normalizedCpf));
        }

        return indicators;
    }

    private BigDecimal calculatePreferenciaLiquidez(List<Transaction> transactions) {
        long liquidezCount = transactions.stream()
                .filter(t -> "POUPANCA".equals(t.getProduto()) || "TESOURO_SELIC".equals(t.getProduto()))
                .count();

        if (transactions.isEmpty()) {
            return new BigDecimal("0.50");
        }

        return BigDecimal.valueOf((double) liquidezCount / transactions.size())
                .setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private double calculateFrequenciaMedia(List<Transaction> transactions, LocalDate inicio, LocalDate fim) {
        if (transactions.isEmpty()) {
            return 0.0;
        }

        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(inicio, fim);
        if (daysBetween == 0) {
            return 0.0;
        }

        return (double) daysBetween / transactions.size();
    }
}
