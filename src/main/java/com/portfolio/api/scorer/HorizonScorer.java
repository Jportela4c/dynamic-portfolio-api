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
public class HorizonScorer {

    private final InvestmentPlatformProvider investmentPlatformProvider;
    private final ClientIdentifierMapper clientIdentifierMapper;

    public int calculateHorizonScore(Long clienteId) {
        String cpf = clientIdentifierMapper.getCpfForClient(clienteId)
                .orElse(null);

        if (cpf == null) {
            return 50;
        }

        List<Investment> investments = investmentPlatformProvider.getInvestmentHistory(cpf);

        if (investments.isEmpty()) {
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
