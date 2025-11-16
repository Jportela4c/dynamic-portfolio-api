package com.portfolio.api.util;

import com.portfolio.api.model.entity.Investment;
import com.portfolio.api.repository.InvestmentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ProductRiskPreferenceCalculator {

    private static final Map<String, Integer> PRODUCT_RISK_LEVELS = Map.of(
            "Tesouro Direto", 2,
            "LCI", 3,
            "LCA", 3,
            "CDB", 4,
            "Fundo Renda Fixa", 5,
            "Fundo Multimercado", 7,
            "Fundo Ações", 9,
            "FII", 8
    );

    private final InvestmentRepository investmentRepository;

    public ProductRiskPreferenceCalculator(InvestmentRepository investmentRepository) {
        this.investmentRepository = investmentRepository;
    }

    public int calculateProductRiskScore(Long clienteId) {
        List<Investment> investments = investmentRepository
                .findByClienteIdOrderByDataDesc(clienteId);

        if (investments.isEmpty()) {
            return 50;
        }

        double totalValue = investments.stream()
                .mapToDouble(i -> i.getValor().doubleValue())
                .sum();

        double weightedRiskSum = investments.stream()
                .mapToDouble(i -> {
                    int riskLevel = PRODUCT_RISK_LEVELS.getOrDefault(
                            i.getTipo(),
                            5
                    );
                    double weight = i.getValor().doubleValue() / totalValue;
                    return riskLevel * weight;
                })
                .sum();

        return (int) Math.round((weightedRiskSum / 10.0) * 100);
    }
}
