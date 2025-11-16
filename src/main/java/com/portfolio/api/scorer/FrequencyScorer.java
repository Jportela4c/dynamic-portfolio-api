package com.portfolio.api.scorer;

import com.portfolio.api.model.entity.Investment;
import com.portfolio.api.repository.InvestmentRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class FrequencyScorer {

    private final InvestmentRepository investmentRepository;

    public FrequencyScorer(InvestmentRepository investmentRepository) {
        this.investmentRepository = investmentRepository;
    }

    public int calculateFrequencyScore(Long clienteId) {
        Long transactionCount = investmentRepository.countByClienteId(clienteId);

        if (transactionCount == null || transactionCount == 0) {
            return 0;
        }

        List<Investment> investments = investmentRepository.findByClienteIdOrderByDataDesc(clienteId);

        if (investments.isEmpty()) {
            return 0;
        }

        LocalDate firstInvestment = investments.get(investments.size() - 1).getData();
        long daysSinceFirst = ChronoUnit.DAYS.between(firstInvestment, LocalDate.now());

        double yearsActive = daysSinceFirst / 365.0;

        if (yearsActive < 0.1) {
            yearsActive = 0.1;
        }

        double transactionsPerYear = transactionCount / yearsActive;

        if (transactionsPerYear >= 12) {
            return 100;
        } else if (transactionsPerYear >= 4) {
            return 70;
        } else if (transactionsPerYear >= 1) {
            return 40;
        } else {
            return 20;
        }
    }
}
