package com.portfolio.api.scorer;

import com.portfolio.api.mapper.ClientIdentifierMapper;
import com.portfolio.api.provider.InvestmentPlatformProvider;
import com.portfolio.api.provider.dto.Investment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AmountScorer {

    private final InvestmentPlatformProvider investmentPlatformProvider;
    private final ClientIdentifierMapper clientIdentifierMapper;

    public int calculateAmountScore(Long clienteId) {
        String cpf = clientIdentifierMapper.getCpfForClient(clienteId)
                .orElse(null);

        if (cpf == null) {
            return 0;
        }

        List<Investment> investments = investmentPlatformProvider.getInvestmentHistory(cpf);

        BigDecimal customerAmount = investments.stream()
                .map(Investment::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (customerAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 50;
        }

        return 50;
    }
}
