package com.portfolio.api.scorer;

import com.portfolio.api.provider.dto.Investment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Calculates amount score based on total investment volume.
 * <p>
 * THE SPEC Requirement: "Volume de investimentos"
 * - Higher volume typically correlates with higher risk tolerance
 * - Larger portfolios can absorb more volatility
 * <p>
 * Implementation based on improvement plan (04-risk-engine-improvement-plan.md)
 * Thresholds based on Brazilian retail investor market (ANBIMA guidelines)
 */
@Component
public class AmountScorer {

    // Thresholds based on Brazilian market context (in R$)
    private static final BigDecimal THRESHOLD_VERY_LOW = new BigDecimal("10000");    // R$ 10k
    private static final BigDecimal THRESHOLD_LOW = new BigDecimal("50000");         // R$ 50k
    private static final BigDecimal THRESHOLD_MODERATE = new BigDecimal("150000");   // R$ 150k
    private static final BigDecimal THRESHOLD_HIGH = new BigDecimal("500000");       // R$ 500k
    private static final BigDecimal THRESHOLD_VERY_HIGH = new BigDecimal("1000000"); // R$ 1M (Professional Investor)

    /**
     * Calculates amount score based on total invested volume.
     *
     * @param investments List of investments
     * @return Score 0-100 (higher volume = higher risk capacity)
     */
    public int calculateAmountScore(List<Investment> investments) {
        if (investments == null || investments.isEmpty()) {
            return 20; // Conservative - no investment history
        }

        // Calculate total invested amount
        BigDecimal totalAmount = investments.stream()
                .map(Investment::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return 20; // Conservative - no meaningful investment
        }

        // Score based on volume tiers
        // Reference: CVM Instruction 539/2013 for investor classification
        if (totalAmount.compareTo(THRESHOLD_VERY_HIGH) >= 0) {
            return 100;  // Professional Investor (R$ 1M+) - Very Aggressive capacity
        } else if (totalAmount.compareTo(THRESHOLD_HIGH) >= 0) {
            // R$ 500k - R$ 1M: High capacity (score 80-95)
            return calculateLinearScore(totalAmount, THRESHOLD_HIGH, THRESHOLD_VERY_HIGH, 80, 95);
        } else if (totalAmount.compareTo(THRESHOLD_MODERATE) >= 0) {
            // R$ 150k - R$ 500k: Moderate-High capacity (score 60-80)
            return calculateLinearScore(totalAmount, THRESHOLD_MODERATE, THRESHOLD_HIGH, 60, 80);
        } else if (totalAmount.compareTo(THRESHOLD_LOW) >= 0) {
            // R$ 50k - R$ 150k: Moderate capacity (score 40-60)
            return calculateLinearScore(totalAmount, THRESHOLD_LOW, THRESHOLD_MODERATE, 40, 60);
        } else if (totalAmount.compareTo(THRESHOLD_VERY_LOW) >= 0) {
            // R$ 10k - R$ 50k: Low capacity (score 25-40)
            return calculateLinearScore(totalAmount, THRESHOLD_VERY_LOW, THRESHOLD_LOW, 25, 40);
        } else {
            // < R$ 10k: Very low capacity (score 10-25)
            return calculateLinearScore(totalAmount, BigDecimal.ZERO, THRESHOLD_VERY_LOW, 10, 25);
        }
    }

    /**
     * Linear interpolation between two thresholds.
     */
    private int calculateLinearScore(BigDecimal amount, BigDecimal minThreshold, BigDecimal maxThreshold,
                                      int minScore, int maxScore) {
        BigDecimal range = maxThreshold.subtract(minThreshold);
        BigDecimal position = amount.subtract(minThreshold);
        double ratio = position.divide(range, 4, java.math.RoundingMode.HALF_UP).doubleValue();

        // Clamp ratio to [0, 1]
        ratio = Math.max(0, Math.min(1, ratio));

        return (int) Math.round(minScore + (ratio * (maxScore - minScore)));
    }
}
