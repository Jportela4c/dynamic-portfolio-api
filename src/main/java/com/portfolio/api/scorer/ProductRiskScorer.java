package com.portfolio.api.scorer;

import com.portfolio.api.model.enums.TipoProduto;
import com.portfolio.api.provider.dto.Investment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ProductRiskScorer {

    private static final Map<TipoProduto, Integer> PRODUCT_RISK_LEVELS = Map.ofEntries(
            // Treasure Titles - Very low risk
            Map.entry(TipoProduto.TESOURO_SELIC, 2),
            Map.entry(TipoProduto.TESOURO_PREFIXADO, 2),
            Map.entry(TipoProduto.TESOURO_IPCA, 2),
            Map.entry(TipoProduto.TESOURO_RENDA_MAIS, 2),
            Map.entry(TipoProduto.TESOURO_EDUCA_MAIS, 2),
            // Bank Fixed Incomes - Low to medium risk
            Map.entry(TipoProduto.LCI, 3),
            Map.entry(TipoProduto.LCA, 3),
            Map.entry(TipoProduto.RDB, 3),
            Map.entry(TipoProduto.CDB, 4),
            // Poupança - Very low risk
            Map.entry(TipoProduto.POUPANCA, 1),
            // Funds - Medium to high risk
            Map.entry(TipoProduto.RENDA_FIXA, 5),
            Map.entry(TipoProduto.CAMBIAL, 6),
            Map.entry(TipoProduto.MULTIMERCADO, 7),
            Map.entry(TipoProduto.ACOES, 9)
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
