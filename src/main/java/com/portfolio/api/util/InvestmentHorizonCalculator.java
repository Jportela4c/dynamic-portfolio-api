package com.portfolio.api.util;

import com.portfolio.api.model.entity.Investment;
import com.portfolio.api.repository.InvestmentRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class InvestmentHorizonCalculator {

    private final InvestmentRepository investmentRepository;

    public InvestmentHorizonCalculator(InvestmentRepository investmentRepository) {
        this.investmentRepository = investmentRepository;
    }

    public int calculateHorizonScore(Long clienteId) {
        List<Investment> investments = investmentRepository.findByClienteIdOrderByDataDesc(clienteId);

        if (investments.isEmpty()) {
            return 50;
        }

        LocalDate firstInvestment = investments.get(investments.size() - 1).getData();
        long yearsSinceFirst = ChronoUnit.YEARS.between(firstInvestment, LocalDate.now());

        if (yearsSinceFirst >= 10) return 90;
        if (yearsSinceFirst >= 5) return 70;
        if (yearsSinceFirst >= 2) return 50;
        if (yearsSinceFirst >= 1) return 30;
        return 20;
    }
}
