package com.portfolio.api.scorer;

import com.portfolio.api.mapper.ClientIdentifierMapper;
import com.portfolio.api.provider.InvestmentPlatformProvider;
import com.portfolio.api.provider.dto.Investment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FrequencyScorer {

    private final InvestmentPlatformProvider investmentPlatformProvider;
    private final ClientIdentifierMapper clientIdentifierMapper;

    public int calculateFrequencyScore(Long clienteId) {
        String cpf = clientIdentifierMapper.getCpfForClient(clienteId)
                .orElse(null);

        if (cpf == null) {
            return 0;
        }

        List<Investment> investments = investmentPlatformProvider.getInvestmentHistory(cpf);

        if (investments.isEmpty()) {
            return 0;
        }

        LocalDate firstInvestment = investments.stream()
                .map(Investment::getData)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());
        long daysSinceFirst = ChronoUnit.DAYS.between(firstInvestment, LocalDate.now());

        double yearsActive = daysSinceFirst / 365.0;

        if (yearsActive < 0.1) {
            yearsActive = 0.1;
        }

        double transactionsPerYear = investments.size() / yearsActive;

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
