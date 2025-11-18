package com.portfolio.api.scorer;

import com.portfolio.api.provider.dto.Investment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class AmountScorer {

    public int calculateAmountScore(List<Investment> investments) {
        if (investments == null || investments.isEmpty()) {
            return 50;
        }

        BigDecimal customerAmount = investments.stream()
                .map(Investment::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (customerAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 50;
        }

        return 50;
    }
}
