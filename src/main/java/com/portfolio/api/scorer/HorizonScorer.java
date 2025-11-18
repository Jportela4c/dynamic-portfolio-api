package com.portfolio.api.scorer;

import com.portfolio.api.provider.dto.Investment;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class HorizonScorer {

    public int calculateHorizonScore(List<Investment> investments) {
        if (investments == null || investments.isEmpty()) {
            return 50;
        }

        LocalDate firstInvestment = investments.stream()
                .map(Investment::getData)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());
        long yearsSinceFirst = ChronoUnit.YEARS.between(firstInvestment, LocalDate.now());

        if (yearsSinceFirst >= 10) return 90;
        if (yearsSinceFirst >= 5) return 70;
        if (yearsSinceFirst >= 2) return 50;
        if (yearsSinceFirst >= 1) return 30;
        return 20;
    }
}
