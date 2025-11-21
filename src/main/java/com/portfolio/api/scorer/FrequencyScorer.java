package com.portfolio.api.scorer;

import com.portfolio.api.provider.dto.Investment;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Calculates frequency score based on transaction activity.
 * <p>
 * THE SPEC Requirement: "Frequência de movimentações"
 * - Conservative profile: "baixa movimentação"
 * - Higher frequency = higher risk tolerance
 * <p>
 * Implementation based on improvement plan (04-risk-engine-improvement-plan.md)
 * Research reference: Barber & Odean (2000) - Trading frequency correlates with risk tolerance
 * DOI: https://doi.org/10.1111/0022-1082.00226
 */
@Component
public class FrequencyScorer {

    /**
     * Calculates frequency score based on total transaction count across all investments.
     *
     * @param investments List of investments with transaction data
     * @return Score 0-100 (higher = more frequent trading = higher risk tolerance)
     */
    public int calculateFrequencyScore(List<Investment> investments) {
        if (investments == null || investments.isEmpty()) {
            return 0;
        }

        // Calculate total transaction count across all investments
        int totalTransactions = investments.stream()
                .mapToInt(inv -> inv.getTransactionCount() != null ? inv.getTransactionCount() : 0)
                .sum();

        if (totalTransactions == 0) {
            return 20; // Conservative - no transaction activity
        }

        // Find the time period (earliest to latest transaction across all investments)
        LocalDate earliestDate = investments.stream()
                .map(Investment::getFirstTransactionDate)
                .filter(date -> date != null)
                .min(LocalDate::compareTo)
                .orElse(null);

        LocalDate latestDate = investments.stream()
                .map(Investment::getLastTransactionDate)
                .filter(date -> date != null)
                .max(LocalDate::compareTo)
                .orElse(null);

        // Calculate years active
        double yearsActive;
        if (earliestDate != null && latestDate != null) {
            long daysBetween = ChronoUnit.DAYS.between(earliestDate, latestDate);
            yearsActive = daysBetween / 365.0;
        } else {
            // Fallback: use investment data dates
            LocalDate firstInvestment = investments.stream()
                    .map(Investment::getData)
                    .min(LocalDate::compareTo)
                    .orElse(LocalDate.now());
            long daysSinceFirst = ChronoUnit.DAYS.between(firstInvestment, LocalDate.now());
            yearsActive = daysSinceFirst / 365.0;
        }

        // Minimum period to avoid division by zero
        if (yearsActive < 0.1) {
            yearsActive = 0.1;
        }

        // Calculate transactions per year
        double transactionsPerYear = totalTransactions / yearsActive;

        // Score based on trading frequency
        // Research: Barber & Odean (2000) - high frequency = higher risk tolerance
        if (transactionsPerYear >= 12) {
            return 100;  // Monthly or more = Aggressive
        } else if (transactionsPerYear >= 4) {
            return 70;   // Quarterly = Moderate-Aggressive
        } else if (transactionsPerYear >= 1) {
            return 40;   // Annual = Moderate
        } else {
            return 20;   // Less than annual = Conservative
        }
    }
}
