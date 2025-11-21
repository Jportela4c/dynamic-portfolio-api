package com.portfolio.api.scorer;

import com.portfolio.api.model.enums.TipoProduto;
import com.portfolio.api.provider.dto.Investment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Calculates liquidity preference score based on actual investment choices.
 * <p>
 * THE SPEC Requirement: "Preferência por liquidez ou rentabilidade"
 * - Analyzes ACTUAL investments (not simulations)
 * - Evaluates liquidity characteristics of chosen products
 * <p>
 * Implementation based on improvement plan (04-risk-engine-improvement-plan.md)
 * Reference: Kahneman & Tversky (1979) - Time preference and risk attitudes
 */
@Component
public class LiquidityScorer {

    // Product liquidity levels (1 = highly liquid, 10 = illiquid/long-term)
    private static final Map<TipoProduto, Integer> PRODUCT_LIQUIDITY_LEVELS = Map.ofEntries(
            // Highly liquid products (D+0 to D+1)
            Map.entry(TipoProduto.POUPANCA, 1),
            Map.entry(TipoProduto.TESOURO_SELIC, 2),
            Map.entry(TipoProduto.RENDA_FIXA, 3),
            // Medium liquidity (D+1 to D+30)
            Map.entry(TipoProduto.CDB, 5),
            Map.entry(TipoProduto.LCI, 5),
            Map.entry(TipoProduto.LCA, 5),
            Map.entry(TipoProduto.RDB, 6),
            Map.entry(TipoProduto.CAMBIAL, 6),
            Map.entry(TipoProduto.MULTIMERCADO, 6),
            // Lower liquidity (longer terms)
            Map.entry(TipoProduto.TESOURO_PREFIXADO, 7),
            Map.entry(TipoProduto.TESOURO_IPCA, 7),
            Map.entry(TipoProduto.TESOURO_RENDA_MAIS, 9),
            Map.entry(TipoProduto.TESOURO_EDUCA_MAIS, 9),
            // Variable income (more illiquid due to market risk)
            Map.entry(TipoProduto.ACOES, 8)
    );

    /**
     * Calculates liquidity score based on actual investment product choices.
     * <p>
     * Lower liquidity preference = longer investment horizon = higher risk tolerance
     *
     * @param investments List of actual investments
     * @return Score 0-100 (higher = lower liquidity need = higher risk tolerance)
     */
    public int calculateLiquidityScore(List<Investment> investments) {
        if (investments == null || investments.isEmpty()) {
            return 50; // Neutral - no data
        }

        // Calculate weighted average of liquidity levels
        double totalValue = investments.stream()
                .mapToDouble(i -> i.getValor().doubleValue())
                .sum();

        if (totalValue <= 0) {
            return 50; // Neutral - no meaningful investments
        }

        double weightedLiquiditySum = investments.stream()
                .mapToDouble(i -> {
                    int liquidityLevel = PRODUCT_LIQUIDITY_LEVELS.getOrDefault(
                            i.getTipo(),
                            5  // Default to medium liquidity
                    );
                    double weight = i.getValor().doubleValue() / totalValue;
                    return liquidityLevel * weight;
                })
                .sum();

        // Normalize to 0-100 scale
        // Higher liquidity level (less liquid products) = higher risk tolerance
        // Scale: 1-10 → 0-100
        return (int) Math.round((weightedLiquiditySum / 10.0) * 100);
    }
}
