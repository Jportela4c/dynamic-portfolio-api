package com.portfolio.api.provider.impl;

import com.portfolio.api.model.entity.Customer;
import com.portfolio.api.model.enums.TipoProduto;
import com.portfolio.api.provider.InvestmentPlatformProvider;
import com.portfolio.api.provider.OFBAuthProvider;
import com.portfolio.api.provider.dto.CustomerPortfolio;
import com.portfolio.api.provider.dto.Investment;
import com.portfolio.api.provider.dto.Position;
import com.portfolio.api.repository.CustomerRepository;
import com.portfolio.api.service.external.OFBInvestmentDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OFBInvestmentPlatformProvider implements InvestmentPlatformProvider {

    private final OFBInvestmentDataService ofbInvestmentDataService;
    private final OFBAuthProvider ofbAuthProvider;
    private final CustomerRepository customerRepository;

    @Override
    public CustomerPortfolio getPortfolio(String cpf) {
        log.debug("Fetching portfolio for CPF: {}", maskCpf(cpf));

        try {
            String token = authenticateWithCpf(cpf);
            List<OFBInvestmentDataService.InvestmentData> investments = ofbInvestmentDataService.fetchInvestments(token);

            BigDecimal totalInvestido = investments.stream()
                    .map(inv -> BigDecimal.valueOf(inv.getInvestedAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal valorAtual = investments.stream()
                    .map(inv -> BigDecimal.valueOf(inv.getCurrentValue()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal rentabilidadeTotal = valorAtual.subtract(totalInvestido);

            return CustomerPortfolio.builder()
                    .cpf(cpf)
                    .totalInvestido(totalInvestido.setScale(2, RoundingMode.HALF_UP))
                    .valorAtual(valorAtual.setScale(2, RoundingMode.HALF_UP))
                    .rentabilidadeTotal(rentabilidadeTotal.setScale(2, RoundingMode.HALF_UP))
                    .dataUltimaAtualizacao(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error fetching portfolio from OFB provider for CPF: {}", maskCpf(cpf), e);
            throw new RuntimeException("Falha ao buscar portfólio do provedor OFB", e);
        }
    }

    @Override
    public List<Investment> getInvestmentHistory(String cpf) {
        log.debug("Fetching investment history for CPF: {}", maskCpf(cpf));

        try {
            String token = authenticateWithCpf(cpf);
            List<OFBInvestmentDataService.InvestmentData> investments = ofbInvestmentDataService.fetchInvestments(token);

            return investments.stream()
                    .map(this::mapToInvestment)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching investment history from OFB provider for CPF: {}", maskCpf(cpf), e);
            throw new RuntimeException("Falha ao buscar histórico de investimentos do provedor OFB", e);
        }
    }

    @Override
    public List<Position> getCurrentPositions(String cpf) {
        log.debug("Fetching current positions for CPF: {}", maskCpf(cpf));

        try {
            String token = authenticateWithCpf(cpf);
            List<OFBInvestmentDataService.InvestmentData> investments = ofbInvestmentDataService.fetchInvestments(token);

            return investments.stream()
                    .map(this::mapToPosition)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching positions from OFB provider for CPF: {}", maskCpf(cpf), e);
            throw new RuntimeException("Falha ao buscar posições do provedor OFB", e);
        }
    }

    private Investment mapToInvestment(OFBInvestmentDataService.InvestmentData data) {
        BigDecimal valor = BigDecimal.valueOf(data.getInvestedAmount()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal valorAtual = BigDecimal.valueOf(data.getCurrentValue()).setScale(2, RoundingMode.HALF_UP);

        // Calculate profitability as rate (per THE SPEC requirement)
        // rentabilidade = (valorAtual - valor) / valor
        // Example: (24417.43 - 20116.19) / 20116.19 = 0.2138 (21.38%)
        BigDecimal rentabilidade = valorAtual.subtract(valor)
                .divide(valor, 4, RoundingMode.HALF_UP);

        return Investment.builder()
                .id((long) Math.abs(data.getInvestmentId().hashCode()))
                .tipo(mapStringToTipoProduto(data.getType()))
                .tipoOperacao("APLICACAO")
                .valor(valor)
                .rentabilidade(rentabilidade)
                .data(LocalDate.now())
                .nomeProduto(data.getIssuerName() != null ? data.getIssuerName() : data.getType())
                .transactionCount(data.getTransactionCount())
                .firstTransactionDate(data.getFirstTransactionDate())
                .lastTransactionDate(data.getLastTransactionDate())
                .build();
    }

    private Position mapToPosition(OFBInvestmentDataService.InvestmentData data) {
        BigDecimal valorAplicado = BigDecimal.valueOf(data.getInvestedAmount()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal valorAtual = BigDecimal.valueOf(data.getCurrentValue()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal rentabilidade = valorAtual.subtract(valorAplicado);

        LocalDate dataVencimento = null;
        if (data.getMaturityDate() != null && !data.getMaturityDate().isEmpty()) {
            try {
                dataVencimento = LocalDate.parse(data.getMaturityDate(), DateTimeFormatter.ISO_DATE);
            } catch (Exception e) {
                log.warn("Failed to parse maturity date: {}", data.getMaturityDate());
            }
        }

        return Position.builder()
                .codigoProduto(data.getInvestmentId())
                .nomeProduto(data.getIssuerName() != null ? data.getIssuerName() : data.getType())
                .tipoProduto(data.getType())
                .valorAplicado(valorAplicado)
                .valorAtual(valorAtual)
                .rentabilidade(rentabilidade.setScale(2, RoundingMode.HALF_UP))
                .dataAplicacao(LocalDate.now())
                .dataVencimento(dataVencimento)
                .liquidez("D+0")
                .build();
    }

    private String authenticateWithCpf(String cpf) {
        try {
            Customer customer = customerRepository.findByCpf(cpf)
                    .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado para o CPF"));

            return ofbAuthProvider.authenticateCustomer(customer.getId());
        } catch (Exception e) {
            log.error("Authentication failed for CPF: {}", maskCpf(cpf), e);
            throw new RuntimeException("Falha ao autenticar cliente", e);
        }
    }

    private String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 11) {
            return "***";
        }
        return cpf.substring(0, 3) + ".***.***-" + cpf.substring(9);
    }

    private TipoProduto mapStringToTipoProduto(String type) {
        if (type == null) {
            return TipoProduto.UNKNOWN;
        }
        try {
            return TipoProduto.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown investment type from OFB: {}. Returning UNKNOWN", type);
            return TipoProduto.UNKNOWN;
        }
    }
}
