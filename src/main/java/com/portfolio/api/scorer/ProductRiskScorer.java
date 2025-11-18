package com.portfolio.api.scorer;

import com.portfolio.api.mapper.ClientIdentifierMapper;
import com.portfolio.api.model.enums.TipoProduto;
import com.portfolio.api.provider.InvestmentPlatformProvider;
import com.portfolio.api.provider.dto.Investment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProductRiskScorer {

    private static final Map<String, Integer> PRODUCT_RISK_LEVELS = Map.of(
            "TESOURO_DIRETO", 2,
            "LCI", 3,
            "LCA", 3,
            "CDB", 4,
            "FUNDO_RENDA_FIXA", 5,
            "FUNDO_MULTIMERCADO", 7,
            "FUNDO_ACOES", 9,
            "FII", 8
    );

    private final InvestmentPlatformProvider investmentPlatformProvider;
    private final ClientIdentifierMapper clientIdentifierMapper;

    public int calculateProductRiskScore(Long clienteId) {
        String cpf = clientIdentifierMapper.getCpfForClient(clienteId)
                .orElse(null);

        if (cpf == null) {
            return 50;
        }

        List<Investment> investments = investmentPlatformProvider.getInvestmentHistory(cpf);

        if (investments.isEmpty()) {
            return 50;
        }

        double totalValue = investments.stream()
                .mapToDouble(i -> i.getValor().doubleValue())
                .sum();

        double weightedRiskSum = investments.stream()
                .mapToDouble(i -> {
                    int riskLevel = PRODUCT_RISK_LEVELS.getOrDefault(
                            i.getTipo(),
                            5
                    );
                    double weight = i.getValor().doubleValue() / totalValue;
                    return riskLevel * weight;
                })
                .sum();

        return (int) Math.round((weightedRiskSum / 10.0) * 100);
    }
}
