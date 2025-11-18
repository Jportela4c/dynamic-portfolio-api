package com.portfolio.api.scorer;

import com.portfolio.api.provider.dto.Investment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ProductRiskScorer {

    private static final Map<String, Integer> PRODUCT_RISK_LEVELS = Map.of(
            "TESOURO_DIRETO", 2,
            "LCI", 3,
            "LCA", 3,
            "CDB", 4,
            "FUNDO_RENDA_FIXA", 5,
            "FUNDO_MULTIMERCADO", 7,
            "FUNDO_ACOES", 9,
            "FII", 8
    );

    public int calculateProductRiskScore(List<Investment> investments) {
        if (investments == null || investments.isEmpty()) {
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
